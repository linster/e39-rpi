package ca.stefanm.ibus.gui.pim.email.views

import androidx.compose.foundation.ScrollbarAdapter
import androidx.compose.foundation.ScrollbarStyle
import androidx.compose.foundation.VerticalScrollbar
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.scrollBy
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import ca.stefanm.ibus.annotations.screenflow.ScreenDoc
import ca.stefanm.ibus.autoDiscover.AutoDiscover
import ca.stefanm.ibus.car.bordmonitor.input.InputEvent
import ca.stefanm.ibus.di.ApplicationModule
import ca.stefanm.ibus.gui.menu.Notification
import ca.stefanm.ibus.gui.menu.navigator.NavigationNode
import ca.stefanm.ibus.gui.menu.navigator.NavigationNodeTraverser
import ca.stefanm.ibus.gui.menu.navigator.Navigator
import ca.stefanm.ibus.gui.menu.notifications.NotificationHub
import ca.stefanm.ibus.gui.menu.widgets.BmwSingleLineHeader
import ca.stefanm.ibus.gui.menu.widgets.halveIfNotPixelDoubled
import ca.stefanm.ibus.gui.menu.widgets.knobListener.KnobListenerService
import ca.stefanm.ibus.gui.menu.widgets.knobListener.dynamic.KnobObserverBuilderState
import ca.stefanm.ibus.gui.menu.widgets.knobListener.dynamic.toDynamicLambda
import ca.stefanm.ibus.gui.menu.widgets.modalMenu.ModalMenuService
import ca.stefanm.ibus.gui.menu.widgets.modalMenu.SidePanelMenu
import ca.stefanm.ibus.gui.menu.widgets.modalMenu.SidePanelMenu.InfoLabel
import ca.stefanm.ibus.gui.menu.widgets.screenMenu.SmoothScroll
import ca.stefanm.ibus.gui.menu.widgets.screenMenu.TextMenuItem
import ca.stefanm.ibus.gui.menu.widgets.themes.ThemeWrapper
import ca.stefanm.ibus.gui.pim.email.repo.MailFolder
import ca.stefanm.ibus.gui.pim.email.repo.api.MessageReaderRepo
import ca.stefanm.ibus.gui.pim.email.repo.api.ReaderContent
import ca.stefanm.ibus.gui.pim.email.views.parts.MailToolbar
import ca.stefanm.ibus.gui.pim.email.views.parts.MessageBodyText
import ca.stefanm.ibus.gui.pim.email.views.parts.MessageBodyView
import ca.stefanm.ibus.gui.pim.email.views.parts.MailToolbarButton
import ca.stefanm.ibus.gui.pim.email.views.parts.folderIcon
import ca.stefanm.ibus.lib.logging.Logger
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Named

/**
 * One message: headers, text, attachment names. It opens with the knob
 * scrolling the text (KNOB_LISTENER_MAIN_AUX, as in the PDF viewer);
 * pressing the knob moves it to the toolbar: back, scroll, newer, older
 * (within the list page it was opened from), and actions (read/unread,
 * flag, move, delete) in a side pane.
 */
@ScreenDoc(
    screenName = "MessageReaderScreen",
    description = "Mail: reading a message",
    navigatesTo = []
)
@ScreenDoc.AllowsGoBack
@AutoDiscover
class MessageReaderScreen @Inject constructor(
    @Named(ApplicationModule.KNOB_LISTENER_MAIN)
    private val knobListenerServiceMain: KnobListenerService,
    @Named(ApplicationModule.KNOB_LISTENER_MAIN_AUX)
    private val knobListenerServiceMainAux: KnobListenerService,
    private val logger: Logger,
    private val navigationNodeTraverser: NavigationNodeTraverser,
    private val modalMenuService: ModalMenuService,
    private val notificationHub: NotificationHub,
    private val readerRepo: MessageReaderRepo,
) : NavigationNode<Nothing> {
    override val thisClass: Class<out NavigationNode<Nothing>> = this::class.java

    companion object {
        const val TAG = "MessageReaderScreen"

        /** [pageIds]: the list the message was picked from, for newer/older. */
        fun open(navigationNodeTraverser: NavigationNodeTraverser, messageId: String, pageIds: List<String> = listOf(messageId)) {
            navigationNodeTraverser.navigateToNodeWithParameters(MessageReaderScreen::class.java, Params(messageId, pageIds))
        }
    }

    data class Params(val messageId: String, val pageIds: List<String>)

    /** What's on screen, and what the actions change. */
    private class ReaderState(params: Params) {
        val messageId = mutableStateOf(params.messageId)
        val ids = mutableStateOf(params.pageIds)
        val content = mutableStateOf<ReaderContent?>(null)
        val error = mutableStateOf<String?>(null)
        /** True: the knob scrolls the text; false: it moves through the toolbar. */
        val scrolling = mutableStateOf(true)

        fun neighbour(forward: Boolean) = MessageReaderRepo.neighbour(ids.value, messageId.value, forward)
    }

    override fun provideMainContent(): @Composable (incomingResult: Navigator.IncomingResult?) -> Unit = content@{ incoming ->
        val params = incoming?.requestParameters as? Params
        if (params == null) {
            LaunchedEffect(Unit) {
                logger.w(TAG, "Opened without a message")
                navigationNodeTraverser.goBack()
            }
            return@content
        }
        val state = remember(params) { ReaderState(params) }
        val scope = rememberCoroutineScope()
        val scrollState = rememberScrollState()

        LaunchedEffect(state.messageId.value) {
            state.content.value = null
            state.error.value = null
            scrollState.scrollTo(0)
            readerRepo.open(state.messageId.value).fold(
                onSuccess = { state.content.value = it },
                onFailure = {
                    logger.w(TAG, "Opening ${state.messageId.value}: ${it.message}")
                    state.error.value = it.message ?: it.toString()
                },
            )
        }

        // Scrolling: the aux listener has the knob, the main one (toolbar)
        // waits. A press goes back to the toolbar.
        LaunchedEffect(state.scrolling.value) {
            if (state.scrolling.value) {
                knobListenerServiceMain.disableListener()
                knobListenerServiceMainAux.enableListener()
            } else {
                knobListenerServiceMainAux.disableListener()
                knobListenerServiceMain.enableListener()
            }
            knobListenerServiceMainAux.knobTurnEvents(false).collect { event ->
                if (!state.scrolling.value) return@collect
                when (event) {
                    InputEvent.NavKnobPressed -> state.scrolling.value = false
                    is InputEvent.NavKnobTurned -> scrollState.scrollBy(60f * event.direction.toSign())
                    InputEvent.NextTrack -> scrollState.scrollBy(scrollState.viewportSize * 0.9f)
                    InputEvent.PrevTrack -> scrollState.scrollBy(-scrollState.viewportSize * 0.9f)
                    else -> {}
                }
            }
        }
        DisposableEffect(Unit) {
            onDispose {
                knobListenerServiceMainAux.disableListener()
                knobListenerServiceMain.enableListener()
            }
        }

        val knobState = KnobObserverBuilderState.setupListener(knobListenerServiceMain, logger, TAG)
        Column(
            Modifier
                .background(ThemeWrapper.ThemeHandle.current.colors.menuBackground)
                .fillMaxSize()
        ) {
            BmwSingleLineHeader(state.content.value?.subject ?: "Mail")
            MailToolbar(
                knobState = knobState,
                highlightNone = state.scrolling.value,
                buttons = listOf(
                    MailToolbarButton("Back") { navigationNodeTraverser.goBack() },
                    MailToolbarButton(if (state.scrolling.value) "↕ Press: menu" else "↕ Scroll", 2f) {
                        state.scrolling.value = !state.scrolling.value
                    },
                    MailToolbarButton(if (state.neighbour(false) != null) "▲ Newer" else "▲", 1.3f) {
                        state.neighbour(false)?.let { state.messageId.value = it }
                    },
                    MailToolbarButton(if (state.neighbour(true) != null) "▼ Older" else "▼", 1.3f) {
                        state.neighbour(true)?.let { state.messageId.value = it }
                    },
                    MailToolbarButton("☰") {
                        state.scrolling.value = false
                        state.content.value?.let { showActions(scope, state, it) }
                    },
                ),
            )
            Box(Modifier.fillMaxSize()) {
                Column(
                    Modifier
                        .fillMaxWidth()
                        .padding(end = 24.dp)
                        .verticalScroll(scrollState)
                        .padding(horizontal = 16.dp.halveIfNotPixelDoubled(), vertical = 8.dp.halveIfNotPixelDoubled())
                ) {
                    val content = state.content.value
                    when {
                        content != null -> key(content.summary.id) { MessageBodyView(content) }
                        state.error.value != null -> MessageBodyText("Couldn't open this message:\n${state.error.value}")
                        else -> MessageBodyText("Loading...")
                    }
                }
                VerticalScrollbar(
                    modifier = Modifier.fillMaxHeight().align(Alignment.TopEnd),
                    adapter = ScrollbarAdapter(scrollState),
                    style = ScrollbarStyle(
                        minimalHeight = 16.dp,
                        thickness = 16.dp,
                        shape = RoundedCornerShape(8.dp),
                        hoverDurationMillis = 300,
                        unhoverColor = ThemeWrapper.ThemeHandle.current.colors.textMenuColorAccent,
                        hoverColor = ThemeWrapper.ThemeHandle.current.colors.textMenuColorAccent
                    )
                )
            }
        }
    }

    private fun showActions(scope: CoroutineScope, state: ReaderState, content: ReaderContent) {
        val summary = content.summary
        modalMenuService.showSidePaneOverlay(darkenBackground = true) {
            SidePanelMenu.SidePanelMenu(
                title = content.subject,
                text = @Composable {
                    InfoLabel(summary.from.display, FontWeight.Bold)
                    content.folder?.let { InfoLabel("In ${it.accountName}: ${it.name}") }
                },
                buttons = listOf(
                    TextMenuItem(if (summary.seen) "Mark Unread" else "Mark Read", onClicked = {
                        modalMenuService.closeSidePaneOverlay(true)
                        scope.launch { readerRepo.setSeen(summary, !summary.seen).update(state, content, "Not changed") }
                    }),
                    TextMenuItem(if (summary.flagged) "Remove Flag" else "Flag", onClicked = {
                        modalMenuService.closeSidePaneOverlay(true)
                        scope.launch { readerRepo.setFlagged(summary, !summary.flagged).update(state, content, "Not changed") }
                    }),
                    TextMenuItem("Move to Folder...", onClicked = {
                        modalMenuService.closeSidePaneOverlay(true)
                        scope.launch { showMovePane(scope, state, content, readerRepo.moveTargets(summary)) }
                    }),
                    TextMenuItem(if (content.inTrash) "Delete for Good" else "Delete", onClicked = {
                        modalMenuService.closeSidePaneOverlay(true)
                        scope.launch {
                            readerRepo.delete(summary).fold(
                                onSuccess = { gone(state, if (content.inTrash) "Deleted" else "Moved to the trash") },
                                onFailure = { notifyFailure("Not deleted", it) },
                            )
                        }
                    }),
                    TextMenuItem("Go Back", onClicked = { modalMenuService.closeSidePaneOverlay(true) }),
                )
            )
        }
    }

    private fun showMovePane(scope: CoroutineScope, state: ReaderState, content: ReaderContent, targets: List<MailFolder>) {
        modalMenuService.showSidePaneOverlayWithKnobListener(darkenBackground = true) { knobListenerServiceModal ->
            SidePanelMenu.SidePanelMenu(title = "Move to") {
                SmoothScroll.SmoothScroll(
                    modifier = Modifier.fillMaxWidth(),
                    knobListenerService = knobListenerServiceModal,
                    tag = "$TAG move",
                    logger = logger,
                    items = buildList {
                        add(TextMenuItem("Go Back", onClicked = { modalMenuService.closeSidePaneOverlay(true) }).toDynamicLambda())
                        targets.forEach { folder ->
                            add(TextMenuItem("${folderIcon(folder.role)} ${folder.path.joinToString(" / ")}", onClicked = {
                                modalMenuService.closeSidePaneOverlay(true)
                                scope.launch {
                                    readerRepo.move(content.summary, folder).fold(
                                        onSuccess = { gone(state, "Moved to ${folder.name}") },
                                        onFailure = { notifyFailure("Not moved", it) },
                                    )
                                }
                            }).toDynamicLambda())
                        }
                    },
                )
            }
        }
    }

    private fun Result<ca.stefanm.ibus.gui.pim.email.repo.MailSummary>.update(state: ReaderState, content: ReaderContent, failure: String) {
        fold(
            onSuccess = { summary ->
                if (state.content.value?.summary?.id == summary.id) {
                    state.content.value = content.copy(message = content.message.copy(summary = summary))
                }
            },
            onFailure = { notifyFailure(failure, it) },
        )
    }

    /** The message left this folder: on to the next one in the list, or back to the list. */
    private fun gone(state: ReaderState, what: String) {
        notificationHub.postNotificationBackground(Notification(Notification.NotificationImage.MESSAGE_CIRCLE, what))
        val next = state.neighbour(true) ?: state.neighbour(false)
        state.ids.value = state.ids.value - state.messageId.value
        if (next == null) navigationNodeTraverser.goBack() else state.messageId.value = next
    }

    private fun notifyFailure(top: String, e: Throwable) {
        logger.w(TAG, "$top: ${e.message}")
        notificationHub.postNotificationBackground(
            Notification(Notification.NotificationImage.ALERT_TRIANGLE, top, e.message ?: e.toString())
        )
    }
}
