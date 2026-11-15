package ca.stefanm.ibus.gui.menu.widgets.modalMenu.volume

import ca.stefanm.ibus.gui.generalSettings.AudioLabels
import ca.stefanm.ibus.gui.menu.widgets.modalMenu.ModalMenuService
import ca.stefanm.ibus.lib.audio.AudioOutputs
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * A volume slider for the default audio output, on the knob (the video
 * player's volume button). Settings -> Audio has every output.
 */
class VolumePaneService @Inject constructor(
    private val modalMenuService: ModalMenuService,
    private val audioOutputs: AudioOutputs,
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    fun showVolumePane() {
        if (!audioOutputs.canChange()) return
        scope.launch {
            val output = audioOutputs.default() ?: return@launch
            val start = AudioLabels.sliderValue(output.volumePercent)
            val changes = AudioLabels.SliderChanges(start)
            modalMenuService.showIntSlider(
                initialValue = start,
                validItems = AudioLabels.SLIDER_RANGE,
                hintText = "Volume",
                onCurrentValueChanged = { percent ->
                    if (changes.isChange(percent)) scope.launch { audioOutputs.setVolume(output, percent) }
                },
            )
        }
    }
}
