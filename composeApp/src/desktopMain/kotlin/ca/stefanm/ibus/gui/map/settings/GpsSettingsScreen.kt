package ca.stefanm.ibus.gui.map.settings

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.rememberUpdatedState
import ca.stefanm.ibus.annotations.screenflow.ScreenDoc
import ca.stefanm.ibus.autoDiscover.AutoDiscover
import ca.stefanm.ibus.gui.map.location.GpsStatusText
import ca.stefanm.ibus.gui.map.location.GpsdSettings
import ca.stefanm.ibus.gui.map.location.GpsdSettingsRepository
import ca.stefanm.ibus.gui.map.location.LocationRepository
import ca.stefanm.ibus.gui.menu.Notification
import ca.stefanm.ibus.gui.menu.navigator.NavigationNode
import ca.stefanm.ibus.gui.menu.navigator.Navigator
import ca.stefanm.ibus.gui.menu.notifications.NotificationHub
import ca.stefanm.ibus.gui.menu.widgets.ItemChipOrientation
import ca.stefanm.ibus.gui.menu.widgets.MenuItem
import ca.stefanm.ibus.gui.menu.widgets.knobListener.dynamic.KnobObserverBuilderScope
import ca.stefanm.ibus.gui.menu.widgets.knobListener.dynamic.toDynamicLambda
import ca.stefanm.ibus.gui.menu.widgets.modalMenu.ModalMenuService
import ca.stefanm.ibus.gui.menu.widgets.modalMenu.keyboard.Keyboard
import ca.stefanm.ibus.gui.menu.widgets.screenMenu.FullScreenMenu.OneColumnSmoothScreenCustomViews
import ca.stefanm.ibus.gui.menu.widgets.screenMenu.SmoothScroll
import ca.stefanm.ibus.gui.menu.widgets.screenMenu.TextMenuItem
import javax.inject.Inject

/** Where the car is according to the location services, and gpsd's host and port. */
@ScreenDoc(
    screenName = "GpsSettingsScreen",
    description = "Shows the GPS position and its source, state and satellites, and sets gpsd's host and port.",
    navigatesTo = []
)
@ScreenDoc.AllowsGoBack
@AutoDiscover
class GpsSettingsScreen @Inject constructor(
    private val modalMenuService: ModalMenuService,
    private val notificationHub: NotificationHub,
    private val locationRepository: LocationRepository,
    private val gpsdSettingsRepository: GpsdSettingsRepository,
) : NavigationNode<Nothing> {

    companion object {
        const val TAG = "GpsSettingsScreen"
    }

    override val thisClass: Class<out NavigationNode<Nothing>>
        get() = GpsSettingsScreen::class.java

    /**
     * Runs [change]; a refused value (IllegalArgumentException) becomes a notification.
     * Not named apply: inside a scope lambda (e.g. Column { }) that resolves to Kotlin's T.apply,
     * which catches nothing.
     */
    private fun tryChange(change: () -> Unit) {
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

        val lines = GpsStatusText.lines(
            source = locationRepository.source.collectAsState().value,
            state = locationRepository.state.collectAsState().value,
            fix = locationRepository.fix.collectAsState().value,
            satellites = locationRepository.satellites.collectAsState().value,
            gpsd = gpsdSettingsRepository.settings.collectAsState().value,
        )
        // Each status row reads its line from here: the rows update in place as the
        // position changes, without rebuilding the list (or moving the knob).
        val currentLines = rememberUpdatedState(lines)

        val statusRows = lines.indices.map { i ->
            val row: @Composable KnobObserverBuilderScope.(Int, Int) -> Unit = { allocatedIndex, currentIndex ->
                // Not selectable: no chip, nothing on a press.
                MenuItem(
                    label = currentLines.value.getOrElse(i) { "" },
                    chipOrientation = ItemChipOrientation.NONE,
                    isSelected = allocatedIndex == currentIndex,
                    onClicked = CallWhen(currentIndexIs = allocatedIndex) { }
                )
            }
            row
        }

        val actions = listOf(
            TextMenuItem(
                title = "Set gpsd Host...",
                onClicked = {
                    // The current host: the list keeps its first onClicked.
                    modalMenuService.showKeyboard(Keyboard.KeyboardType.FULL, gpsdSettingsRepository.settings.value.host) {
                        tryChange { gpsdSettingsRepository.setHost(it) }
                    }
                }
            ),
            TextMenuItem(
                title = "Set gpsd Port...",
                onClicked = {
                    modalMenuService.showKeyboard(Keyboard.KeyboardType.NUMERIC, gpsdSettingsRepository.settings.value.port.toString()) {
                        tryChange {
                            gpsdSettingsRepository.setPort(
                                requireNotNull(it.trim().toIntOrNull()) { "Invalid port '$it'" }
                            )
                        }
                    }
                }
            ),
            TextMenuItem(
                title = "Reset to ${GpsdSettings.DEFAULT.target}",
                onClicked = { tryChange { gpsdSettingsRepository.resetToDefaults() } }
            ),
        ).map { it.toDynamicLambda() }

        SmoothScroll.MenuWindowSmoothScrollContext(TAG).OneColumnSmoothScreenCustomViews(
            header = "GPS",
            prependGoBackEntry = true,
            items = statusRows + actions,
        )
    }
}
