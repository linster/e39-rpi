package ca.stefanm.ibus.gui.pim.email.views

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import ca.stefanm.ibus.annotations.screenflow.ScreenDoc
import ca.stefanm.ibus.autoDiscover.AutoDiscover
import ca.stefanm.ibus.di.ApplicationModule
import ca.stefanm.ibus.gui.menu.Notification
import ca.stefanm.ibus.gui.menu.navigator.NavigationNode
import ca.stefanm.ibus.gui.menu.navigator.NavigationNodeTraverser
import ca.stefanm.ibus.gui.menu.navigator.Navigator
import ca.stefanm.ibus.gui.menu.notifications.NotificationHub
import ca.stefanm.ibus.gui.menu.widgets.ArbitraryContentsMenuItem
import ca.stefanm.ibus.gui.menu.widgets.BmwSingleLineHeader
import ca.stefanm.ibus.gui.menu.widgets.ItemChipOrientation
import ca.stefanm.ibus.gui.menu.widgets.knobListener.KnobListenerService
import ca.stefanm.ibus.gui.menu.widgets.knobListener.dynamic.KnobObserverBuilderState
import ca.stefanm.ibus.gui.menu.widgets.modalMenu.ModalMenuService
import ca.stefanm.ibus.gui.menu.widgets.modalMenu.SidePanelMenu
import ca.stefanm.ibus.gui.menu.widgets.modalMenu.SidePanelMenu.InfoLabel
import ca.stefanm.ibus.gui.menu.widgets.screenMenu.SmoothScroll
import ca.stefanm.ibus.gui.menu.widgets.screenMenu.TextMenuItem
import ca.stefanm.ibus.gui.menu.widgets.themes.ThemeWrapper
import ca.stefanm.ibus.gui.pim.email.repo.api.MessageListRepo
import ca.stefanm.ibus.gui.pim.email.repo.api.MessageListState
import ca.stefanm.ibus.gui.pim.email.repo.api.MessagePager
import ca.stefanm.ibus.gui.pim.email.views.parts.MailInfoText
import ca.stefanm.ibus.gui.pim.email.views.parts.MailListItem
import ca.stefanm.ibus.gui.pim.email.views.parts.MailToolbar
import ca.stefanm.ibus.gui.pim.email.views.parts.MailToolbarButton
import ca.stefanm.ibus.gui.pim.email.views.parts.MessageRowView
import ca.stefanm.ibus.lib.logging.Logger
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Named

/**
 * A folder's messages, newest first, a page (MailRepository.PAGE_SIZE) at
 * a time: the toolbar and the rows share the knob, so the loop through
 * them stays short. Toolbar: back, newer page, older page, menu (unread
 * only, mark the page read).
 */
@ScreenDoc(
    screenName = "MessageListScreen",
    description = "Mail: a folder's messages, paged",
    navigatesTo = [
        ScreenDoc.NavigateTo(MessageReaderScreen::class),
    ]
)
@ScreenDoc.AllowsGoBack
@AutoDiscover
class MessageListScreen @Inject constructor(
    @Named(ApplicationModule.KNOB_LISTENER_MAIN)
    private val knobListenerServiceMain: KnobListenerService,
    private val logger: Logger,
    private val navigationNodeTraverser: NavigationNodeTraverser,
    private val modalMenuService: ModalMenuService,
    private val notificationHub: NotificationHub,
    private val messageListRepo: MessageListRepo,
) : NavigationNode<Nothing> {
    override val thisClass: Class<out NavigationNode<Nothing>> = this::class.java

    companion object {
        const val TAG = "MessageListScreen"

        fun open(navigationNodeTraverser: NavigationNodeTraverser, folderId: String) {
            navigationNodeTraverser.navigateToNodeWithParameters(MessageListScreen::class.java, Params(folderId))
        }
    }

    data class Params(val folderId: String)

    override fun provideMainContent(): @Composable (incomingResult: Navigator.IncomingResult?) -> Unit = content@{ params ->
        val folderId = (params?.requestParameters as? Params)?.folderId
        if (folderId == null) {
            LaunchedEffect(Unit) {
                logger.w(TAG, "Opened without a folder")
                navigationNodeTraverser.goBack()
            }
            return@content
        }
        val pager = remember(folderId) { messageListRepo.pager(folderId) }
        val state = remember(pager) { pager.state }.collectAsState(null).value
        val scope = rememberCoroutineScope()

        // A new page (or filter, or row count) is a new screen for the knob:
        // fresh positions for the toolbar and the rows, in step with
        // SmoothScroll's (it counts rows after the toolbar's buttons).
        key(folderId, state?.page?.page, state?.unreadOnly, state?.rows?.size) {
            val knobState = KnobObserverBuilderState.setupListener(knobListenerServiceMain, logger, TAG)
            Column(
                Modifier
                    .background(ThemeWrapper.ThemeHandle.current.colors.menuBackground)
                    .fillMaxSize()
            ) {
                BmwSingleLineHeader(headerText(state))
                MailToolbar(
                    knobState = knobState,
                    buttons = listOf(
                        MailToolbarButton("Back", 1.5f) { navigationNodeTraverser.goBack() },
                        MailToolbarButton(if (state?.page?.hasPrevious == true) "◀ Newer" else "◀", 1.5f) { pager.previous() },
                        MailToolbarButton(if (state?.page?.hasNext == true) "Older ▶" else "▶", 1.5f) { pager.next() },
                        MailToolbarButton("☰") { state?.let { showMenu(scope, pager, it) } },
                    ),
                )
                SmoothScroll.SmoothScroll(
                    modifier = Modifier,
                    knobState = knobState,
                    tag = TAG,
                    logger = logger,
                    items = rows(state),
                )
            }
        }
    }

    private fun headerText(state: MessageListState?): String {
        val folder = state?.folder ?: return "Mail"
        val name = if (folder.path.size > 1) folder.path.joinToString(" / ") else folder.name
        val filter = if (state.unreadOnly) " · unread" else ""
        return "${folder.accountName}: $name$filter · ${state.rangeLabel}"
    }

    private fun rows(state: MessageListState?): List<MailListItem> = when {
        state == null -> listOf(info("Loading..."))
        state.rows.isEmpty() -> listOf(info(if (state.unreadOnly) "No unread messages" else "No messages"))
        else -> state.rows.map { row ->
            { allocatedIndex, currentIndex ->
                MessageRowView(
                    row = row,
                    isSelected = allocatedIndex == currentIndex,
                    onClicked = CallWhen(currentIndexIs = allocatedIndex) {
                        MessageReaderScreen.open(navigationNodeTraverser, row.id, state.messageIds)
                    },
                )
            }
        }
    }

    private fun info(text: String): MailListItem = { allocatedIndex, currentIndex ->
        ArbitraryContentsMenuItem(
            chipOrientation = ItemChipOrientation.NONE,
            isSelected = allocatedIndex == currentIndex,
            onClicked = CallWhen(currentIndexIs = allocatedIndex) {},
        ) { MailInfoText(text) }
    }

    private fun showMenu(scope: CoroutineScope, pager: MessagePager, state: MessageListState) {
        modalMenuService.showSidePaneOverlay(darkenBackground = true) {
            SidePanelMenu.SidePanelMenu(
                title = state.folder?.name ?: "Mail",
                text = @Composable {
                    InfoLabel(state.rangeLabel, FontWeight.Bold)
                    InfoLabel("Page ${state.pageLabel}")
                    state.folder?.let { InfoLabel("${it.unreadCount} unread of ${it.totalCount}") }
                },
                buttons = buildList {
                    add(TextMenuItem(if (state.unreadOnly) "Show All Messages" else "Show Unread Only", onClicked = {
                        modalMenuService.closeSidePaneOverlay(true)
                        pager.setUnreadOnly(!state.unreadOnly)
                    }))
                    if (state.rows.any { it.unread }) {
                        add(TextMenuItem("Mark Page as Read", onClicked = {
                            modalMenuService.closeSidePaneOverlay(true)
                            scope.launch { pager.markRead(state).onFailure { notifyFailure("Not marked read", it) } }
                        }))
                    }
                    if (state.page.hasPrevious) {
                        add(TextMenuItem("Newest Messages", onClicked = {
                            modalMenuService.closeSidePaneOverlay(true)
                            pager.firstPage()
                        }))
                    }
                    add(TextMenuItem("Go Back", onClicked = { modalMenuService.closeSidePaneOverlay(true) }))
                }
            )
        }
    }

    private fun notifyFailure(top: String, e: Throwable) {
        logger.w(TAG, "$top: ${e.message}")
        notificationHub.postNotificationBackground(
            Notification(Notification.NotificationImage.ALERT_TRIANGLE, top, e.message ?: e.toString())
        )
    }
}
