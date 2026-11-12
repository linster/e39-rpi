package ca.stefanm.ibus.gui.map.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import ca.stefanm.ibus.gui.map.repo.MapsBackendSettings
import ca.stefanm.ibus.gui.map.repo.MapsBackendSettingsRepository
import ca.stefanm.ibus.gui.map.repo.RemotePoiRepository
import ca.stefanm.ibus.gui.map.repo.RemotePoiRepository.ConnectionState
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
import javax.inject.Inject

/** Host and port of the maps backend (POIs, routes), and whether it's reachable. */
class MapsBackendSettingsScreen @Inject constructor(
    private val navigationNodeTraverser: NavigationNodeTraverser,
    private val modalMenuService: ModalMenuService,
    private val notificationHub: NotificationHub,
    private val settingsRepository: MapsBackendSettingsRepository,
    private val remotePoiRepository: RemotePoiRepository,
) : NavigationNode<Nothing> {

    override val thisClass: Class<out NavigationNode<Nothing>>
        get() = MapsBackendSettingsScreen::class.java

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

    override fun provideMainContent(): @Composable (incomingResult: Navigator.IncomingResult?) -> Unit = {

        val settings = settingsRepository.settings.collectAsState().value
        val state = remotePoiRepository.connectionState.collectAsState().value
        val poiCount = remotePoiRepository.pois.collectAsState().value.size

        Column {
            BmwSingleLineHeader("Maps Backend")

            Column(Modifier.background(ThemeWrapper.ThemeHandle.current.colors.menuBackground)) {
                listOf(
                    "Server: ${settings.target}",
                    when (state) {
                        is ConnectionState.Connecting -> "Status: connecting..."
                        is ConnectionState.Connected -> "Status: connected, $poiCount POIs"
                        is ConnectionState.Unavailable -> "Status: ${state.reason}"
                    }
                ).forEach { line ->
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
                                title = "Test Connection",
                                onClicked = { remotePoiRepository.refresh() }
                            ),
                            TextMenuItem(
                                title = "Reset to ${MapsBackendSettings.DEFAULT.target}",
                                onClicked = { apply { settingsRepository.resetToDefaults() } }
                            ),
                        )
                    )
                }
            }
        }
    }
}
