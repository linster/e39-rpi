package ca.stefanm.ibus.gui.generalSettings

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import ca.stefanm.ibus.annotations.screenflow.ScreenDoc
import ca.stefanm.ibus.autoDiscover.AutoDiscover
import ca.stefanm.ibus.configuration.ConfigurationStorage
import ca.stefanm.ibus.configuration.E39Config
import ca.stefanm.ibus.gui.menu.Notification
import ca.stefanm.ibus.gui.menu.navigator.NavigationNode
import ca.stefanm.ibus.gui.menu.navigator.NavigationNodeTraverser
import ca.stefanm.ibus.gui.menu.navigator.Navigator
import ca.stefanm.ibus.gui.menu.notifications.NotificationHub
import ca.stefanm.ibus.gui.menu.widgets.modalMenu.ModalMenuService
import ca.stefanm.ibus.gui.menu.widgets.modalMenu.SidePanelMenu
import ca.stefanm.ibus.gui.menu.widgets.screenMenu.FullScreenMenu.OneColumnSmoothScreen
import ca.stefanm.ibus.gui.menu.widgets.screenMenu.MenuItem
import ca.stefanm.ibus.gui.menu.widgets.screenMenu.SmoothScroll
import ca.stefanm.ibus.gui.menu.widgets.screenMenu.TextMenuItem
import ca.stefanm.ibus.lib.audio.AudioOutput
import ca.stefanm.ibus.lib.audio.AudioOutputs
import ca.stefanm.ibus.lib.audio.BluetoothAudioRole
import ca.stefanm.ibus.lib.audio.BluetoothAudioRoleApplier
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

@ScreenDoc(
    screenName = "AudioSettingsScreen",
    description = "Audio: the outputs and their volume, and whether Bluetooth audio comes from phones " +
            "(the device is a speaker for them) or goes to a speaker.",
    navigatesTo = [
        ScreenDoc.NavigateTo(AudioOutputsScreen::class),
    ]
)
@ScreenDoc.AllowsGoBack
@AutoDiscover
class AudioSettingsScreen @Inject constructor(
    private val navigationNodeTraverser: NavigationNodeTraverser,
    private val modalMenuService: ModalMenuService,
    private val notificationHub: NotificationHub,
    private val configurationStorage: ConfigurationStorage,
    private val applier: BluetoothAudioRoleApplier,
) : NavigationNode<Nothing> {

    override val thisClass: Class<out NavigationNode<Nothing>>
        get() = AudioSettingsScreen::class.java

    override fun provideMainContent(): @Composable (incomingResult: Navigator.IncomingResult?) -> Unit = { params ->
        val scope = rememberCoroutineScope()
        val role = remember(params) { mutableStateOf(configuredRole()) }

        val items: List<MenuItem> = buildList {
            if (!applier.canChange()) {
                add(TextMenuItem("Audio is set up by the device", isSelectable = false, onClicked = {}))
                add(TextMenuItem("image; nothing to change here", isSelectable = false, onClicked = {}))
                return@buildList
            }
            add(TextMenuItem("Outputs and volume", onClicked = {
                navigationNodeTraverser.navigateToNode(AudioOutputsScreen::class.java)
            }))
            add(TextMenuItem("Bluetooth audio:", isSelectable = false, onClicked = {}))
            BluetoothAudioRole.entries.forEach { choice ->
                add(TextMenuItem(TimeZoneLabels.mark(choice == role.value, choice.label), onClicked = {
                    if (choice != role.value) confirmRole(scope, choice) { role.value = configuredRole() }
                }))
            }
        }

        key(role.value) {
            SmoothScroll.MenuWindowSmoothScrollContext("AudioSettingsScreen").OneColumnSmoothScreen(
                header = "Audio",
                prependGoBackEntry = true,
                items = items,
            )
        }
    }

    private fun configuredRole() =
        BluetoothAudioRole.fromConfig(configurationStorage.config[E39Config.AudioConfig.bluetoothRole])

    private fun confirmRole(scope: CoroutineScope, role: BluetoothAudioRole, refresh: () -> Unit) {
        modalMenuService.showSidePaneOverlay(true) {
            SidePanelMenu.SidePanelMenu(
                title = "Bluetooth audio",
                text = {
                    SidePanelMenu.InfoLabel(
                        when (role) {
                            BluetoothAudioRole.SINK -> "Phones play to this device, which plays to the car's audio."
                            BluetoothAudioRole.SOURCE -> "This device plays to a Bluetooth speaker. Pair it in Settings, Bluetooth."
                        }
                    )
                    SidePanelMenu.InfoLabel("Bluetooth audio stops for a moment.")
                },
                buttons = listOf(
                    TextMenuItem("Switch to: ${role.label}", onClicked = {
                        modalMenuService.closeSidePaneOverlay(true)
                        configurationStorage.setBluetoothAudioRole(role.configValue)
                        scope.launch {
                            applier.apply(role).fold(
                                onSuccess = {
                                    notificationHub.postNotificationBackground(
                                        Notification(Notification.NotificationImage.BLUETOOTH, "Bluetooth audio", role.label)
                                    )
                                },
                                onFailure = {
                                    notificationHub.postNotificationBackground(
                                        Notification(Notification.NotificationImage.ALERT_TRIANGLE,
                                            "Bluetooth audio not changed", it.message ?: it.toString(),
                                            Notification.NotificationDuration.LONG)
                                    )
                                }
                            )
                            refresh()
                        }
                    }),
                    TextMenuItem("Cancel", onClicked = { modalMenuService.closeSidePaneOverlay(true) }),
                )
            )
        }
    }
}

@ScreenDoc(
    screenName = "AudioOutputsScreen",
    description = "Each audio output (the Pi's audio out, a Bluetooth speaker) with its volume: " +
            "set the volume with the knob, mute, or make it the default output."
)
@ScreenDoc.AllowsGoBack
@AutoDiscover
class AudioOutputsScreen @Inject constructor(
    private val modalMenuService: ModalMenuService,
    private val notificationHub: NotificationHub,
    private val audioOutputs: AudioOutputs,
) : NavigationNode<Nothing> {

    override val thisClass: Class<out NavigationNode<Nothing>>
        get() = AudioOutputsScreen::class.java

    override fun provideMainContent(): @Composable (incomingResult: Navigator.IncomingResult?) -> Unit = { params ->
        val scope = rememberCoroutineScope()
        val outputs = remember(params) { mutableStateOf<List<AudioOutput>?>(null) }

        // Outputs come and go (a Bluetooth speaker connects); volumes change
        // elsewhere too (a phone's absolute volume).
        LaunchedEffect(params) {
            while (true) {
                outputs.value = audioOutputs.list()
                delay(2_000)
            }
        }

        val items: List<MenuItem> = when (val list = outputs.value) {
            null -> listOf(TextMenuItem("Looking for outputs...", isSelectable = false, onClicked = {}))
            emptyList<AudioOutput>() -> listOf(TextMenuItem("No audio outputs", isSelectable = false, onClicked = {}))
            else -> list.map { output ->
                TextMenuItem(AudioLabels.outputLine(output), onClicked = { showOutput(scope, output) })
            }
        }

        key(outputs.value) {
            SmoothScroll.MenuWindowSmoothScrollContext("AudioOutputsScreen").OneColumnSmoothScreen(
                header = "Outputs and volume",
                prependGoBackEntry = true,
                items = items,
            )
        }
    }

    private fun showOutput(scope: CoroutineScope, output: AudioOutput) {
        modalMenuService.showSidePaneOverlay(true) {
            SidePanelMenu.SidePanelMenu(
                title = output.description,
                text = {
                    SidePanelMenu.InfoLabel(AudioLabels.outputDetails(output))
                },
                buttons = buildList {
                    add(TextMenuItem("Volume...", onClicked = {
                        modalMenuService.closeSidePaneOverlay(true)
                        val start = AudioLabels.sliderValue(output.volumePercent)
                        val changes = AudioLabels.SliderChanges(start)
                        modalMenuService.showIntSlider(
                            initialValue = start,
                            validItems = AudioLabels.SLIDER_RANGE,
                            hintText = "Volume: ${output.description}",
                            onCurrentValueChanged = { percent ->
                                if (changes.isChange(percent)) scope.launch { audioOutputs.setVolume(output, percent) }
                            },
                        )
                    }))
                    add(TextMenuItem(if (output.muted) "Unmute" else "Mute", onClicked = {
                        modalMenuService.closeSidePaneOverlay(true)
                        scope.launch { report(audioOutputs.setMuted(output, !output.muted), "Mute") }
                    }))
                    if (!output.isDefault) {
                        add(TextMenuItem("Use as default output", onClicked = {
                            modalMenuService.closeSidePaneOverlay(true)
                            scope.launch { report(audioOutputs.makeDefault(output), "Default output") }
                        }))
                    }
                    add(TextMenuItem("Go Back", onClicked = { modalMenuService.closeSidePaneOverlay(true) }))
                }
            )
        }
    }

    private fun report(result: Result<Unit>, what: String) {
        result.onFailure {
            notificationHub.postNotificationBackground(
                Notification(Notification.NotificationImage.ALERT_TRIANGLE, "$what not changed", it.message ?: it.toString())
            )
        }
    }
}

/** Text for the audio screens (no Compose: unit-tested). */
object AudioLabels {
    val SLIDER_RANGE: IntProgression = 0..100 step 5

    /** "* Built-in Audio Analog Stereo 40%": the default output first, starred. */
    fun outputLine(output: AudioOutput): String =
        (if (output.isDefault) "\u2605 " else "   ") + output.description + "  " +
                (if (output.muted) "muted" else "${output.volumePercent}%")

    fun outputDetails(output: AudioOutput): String = buildString {
        append("Volume ${output.volumePercent}%")
        if (output.muted) append(", muted")
        if (output.isDefault) append(". The default output: everything plays here.")
        if (output.isBluetooth) append(" (Bluetooth)")
    }

    /** The slider's starting point: the volume, on the slider's 5 % steps. */
    fun sliderValue(percent: Int): Int = ((percent.coerceIn(0, 100) + 2) / 5) * 5

    /**
     * The slider reports its starting value as soon as it opens; passing that
     * on would set the volume to the slider's rounded 5 % step (41 % -> 40 %)
     * just by opening it. Only real changes count, including going back to
     * the start ("Reset").
     */
    class SliderChanges(start: Int) {
        private var last = start
        fun isChange(value: Int): Boolean = (value != last).also { last = value }
    }
}
