package ca.stefanm.ibus.gui.pim.calendar.setup.views

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import ca.stefanm.ibus.autoDiscover.AutoDiscover
import ca.stefanm.ibus.gui.menu.Notification
import ca.stefanm.ibus.gui.menu.navigator.NavigationNode
import ca.stefanm.ibus.gui.menu.navigator.NavigationNodeTraverser
import ca.stefanm.ibus.gui.menu.navigator.Navigator
import ca.stefanm.ibus.gui.menu.notifications.NotificationHub
import ca.stefanm.ibus.gui.menu.widgets.BmwSingleLineHeader
import ca.stefanm.ibus.gui.menu.widgets.ItemChipOrientation
import ca.stefanm.ibus.gui.menu.widgets.modalMenu.ModalMenuService
import ca.stefanm.ibus.gui.menu.widgets.modalMenu.keyboard.Keyboard
import ca.stefanm.ibus.gui.menu.widgets.screenMenu.HalfScreenMenu
import ca.stefanm.ibus.gui.menu.widgets.screenMenu.TextMenuItem
import ca.stefanm.ibus.gui.menu.widgets.themes.ThemeWrapper
import ca.stefanm.ibus.gui.pim.calendar.repo.backend.PimBackendSettings
import ca.stefanm.ibus.gui.pim.calendar.repo.backend.PimBackendSettingsRepository
import ca.stefanm.ibus.gui.pim.calendar.repo.backend.RemoteCalendarRepository
import ca.stefanm.ibus.gui.pim.calendar.repo.backend.RemoteCalendarRepository.BridgeSummary
import ca.stefanm.ibus.gui.pim.calendar.repo.backend.RemoteCalendarRepository.ConnectionState
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Calendar setup: where the PIM bridge (e39-rpi-pim) is, whether it and
 * Akonadi behind it are running, and which calendars it has; the accounts
 * the calendars come from are on [AccountListScreen].
 */
@AutoDiscover
class CalendarSetupRootScreen @Inject constructor(
    private val navigationNodeTraverser: NavigationNodeTraverser,
    private val modalMenuService: ModalMenuService,
    private val notificationHub: NotificationHub,
    private val settingsRepository: PimBackendSettingsRepository,
    private val remoteCalendarRepository: RemoteCalendarRepository,
) : NavigationNode<Nothing>{
    override val thisClass: Class<out NavigationNode<Nothing>>
        get() = CalendarSetupRootScreen::class.java

    companion object {
        /** Calendars listed on the screen; the rest are counted. */
        const val MAX_CALENDARS_SHOWN = 4
    }

    private fun apply(change: () -> Unit) {
        try {
            change()
        } catch (e: IllegalArgumentException) {
            notificationHub.postNotificationBackground(
                Notification(
                    Notification.NotificationImage.ALERT_CIRCLE,
                    topText = "Not changed.",
                    contentText = e.message ?: ""
                )
            )
        }
    }

    override fun provideMainContent(): @Composable
        (incomingResult: Navigator.IncomingResult?) -> Unit = @Composable { params ->

        val settings = settingsRepository.settings.collectAsState().value
        val state = remoteCalendarRepository.connectionState.collectAsState().value
        val summary = remember { mutableStateOf<BridgeSummary?>(null) }
        val scope = rememberCoroutineScope()

        fun check() {
            scope.launch {
                summary.value = remoteCalendarRepository.checkConnection().getOrNull()
            }
        }
        LaunchedEffect(settings) { check() }

        Column {
            BmwSingleLineHeader("Calendar Setup")

            Column(Modifier.background(ThemeWrapper.ThemeHandle.current.colors.menuBackground)) {
                statusLines(settings, state, summary.value).forEach { line ->
                    TextMenuItem(
                        title = line,
                        isSelectable = false,
                        onClicked = {}
                    ).toView(Modifier.fillMaxWidth(), ItemChipOrientation.NONE)()
                }

                Column(Modifier.fillMaxWidth().fillMaxHeight(), verticalArrangement = Arrangement.Bottom) {
                    HalfScreenMenu.TwoColumn(
                        leftItems = listOf(
                            TextMenuItem(
                                title = "Go Back",
                                onClicked = { navigationNodeTraverser.goBack() }
                            ),
                            TextMenuItem(
                                title = "Set Host...",
                                onClicked = {
                                    modalMenuService.showKeyboard(Keyboard.KeyboardType.FULL, settings.host) {
                                        apply { settingsRepository.setHost(it) }
                                    }
                                }
                            ),
                            TextMenuItem(
                                title = "Set Port...",
                                onClicked = {
                                    modalMenuService.showKeyboard(Keyboard.KeyboardType.NUMERIC, settings.port.toString()) {
                                        apply {
                                            settingsRepository.setPort(
                                                requireNotNull(it.trim().toIntOrNull()) { "Invalid port '$it'" }
                                            )
                                        }
                                    }
                                }
                            ),
                        ),
                        rightItems = listOf(
                            TextMenuItem(
                                title = "Accounts...",
                                onClicked = { navigationNodeTraverser.navigateToNode(AccountListScreen::class.java) }
                            ),
                            TextMenuItem(
                                title = "Test Connection",
                                onClicked = { check() }
                            ),
                            TextMenuItem(
                                title = "Reset to ${PimBackendSettings.DEFAULT.target}",
                                onClicked = { apply { settingsRepository.resetToDefaults() } }
                            ),
                        )
                    )
                }
            }
        }
    }

    private fun statusLines(
        settings: PimBackendSettings,
        state: ConnectionState,
        summary: BridgeSummary?,
    ): List<String> = buildList {
        add("PIM bridge: ${settings.target}")
        add(
            when (state) {
                is ConnectionState.Connecting -> "Status: connecting..."
                is ConnectionState.Connected -> "Status: connected"
                is ConnectionState.Unavailable -> "Status: ${state.reason}"
            }
        )
        if (summary != null && state is ConnectionState.Connected) {
            add(
                "Akonadi: " + summary.akonadiState.removePrefix("AKONADI_STATE_").lowercase().replace('_', ' ') +
                    (if (summary.brokenReason.isNotEmpty()) " (${summary.brokenReason})" else "") +
                    ", KDE ${summary.akonadiVersion}, bridge ${summary.bridgeVersion}"
            )
            if (summary.akonadiRunning) {
                if (summary.calendars.isEmpty()) {
                    add("No calendars yet")
                } else {
                    add("Calendars: " + summary.calendars.take(MAX_CALENDARS_SHOWN).joinToString(", ") +
                        if (summary.calendars.size > MAX_CALENDARS_SHOWN) {
                            " and ${summary.calendars.size - MAX_CALENDARS_SHOWN} more"
                        } else "")
                }
            }
        }
    }
}
