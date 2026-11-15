package ca.stefanm.ibus.lib.audio

import ca.stefanm.ibus.gui.generalSettings.AudioLabels
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

internal class AudioTest {

    // Trimmed from pw-dump in qemuarm64 (the emulated sound card), plus a
    // Bluetooth speaker and a source, which must not be listed.
    private fun dump(defaultValue: String) = """
        [
          { "id": 30, "type": "PipeWire:Interface:Metadata", "metadata": [
              { "subject": 0, "key": "default.audio.sink", "type": "Spa:String:JSON", "value": $defaultValue }
          ] },
          { "id": 41, "type": "PipeWire:Interface:Node", "info": {
              "props": { "media.class": "Audio/Sink",
                         "node.name": "alsa_output.platform-4010000000.pcie-pci-0000_00_05.0.analog-stereo",
                         "node.description": "Built-in Audio Analog Stereo" },
              "params": { "Props": [ { "volume": 1.0, "mute": false, "channelVolumes": [ 0.063997, 0.063997 ] } ] } } },
          { "id": 42, "type": "PipeWire:Interface:Node", "info": {
              "props": { "media.class": "Audio/Source", "node.name": "alsa_input.analog-stereo",
                         "node.description": "Built-in Audio Analog Stereo" },
              "params": { "Props": [ { "mute": false, "channelVolumes": [ 1.0, 1.0 ] } ] } } },
          { "id": 77, "type": "PipeWire:Interface:Node", "info": {
              "props": { "media.class": "Audio/Sink", "node.name": "bluez_output.00_11_22_33_44_55.1",
                         "node.nick": "Speaker" },
              "params": { "Props": [ { "mute": true, "channelVolumes": [ 1.0, 1.0 ] } ] } } }
        ]
    """.trimIndent()

    @Test
    fun listsTheSinksWithVolumeMuteAndTheDefaultFirst() {
        val outputs = PwDump.outputs(dump("""{ "name": "bluez_output.00_11_22_33_44_55.1" }"""))
        assertEquals(listOf(77, 41), outputs.map { it.id })
        val speaker = outputs[0]
        assertTrue(speaker.isDefault)
        assertTrue(speaker.isBluetooth)
        assertTrue(speaker.muted)
        assertEquals("Speaker", speaker.description)
        assertEquals(100, speaker.volumePercent)
        val builtIn = outputs[1]
        assertFalse(builtIn.isDefault)
        assertFalse(builtIn.isBluetooth)
        assertEquals("Built-in Audio Analog Stereo", builtIn.description)
        // 0.064 linear is what wpctl shows as 0.40.
        assertEquals(40, builtIn.volumePercent)
    }

    @Test
    fun defaultSinkAlsoAsAJsonString() {
        val outputs = PwDump.outputs(dump("\"{\\\"name\\\": \\\"alsa_output.platform-4010000000.pcie-pci-0000_00_05.0.analog-stereo\\\"}\""))
        assertEquals(41, outputs.first { it.isDefault }.id)
    }

    @Test
    fun badInputGivesNoOutputs() {
        assertEquals(emptyList<AudioOutput>(), PwDump.outputs("not json"))
        assertEquals(emptyList<AudioOutput>(), PwDump.outputs("{}"))
    }

    @Test
    fun cubicScaleLikeWpctl() {
        assertEquals(40, PwDump.cubicPercent(listOf(0.064, 0.064)))
        assertEquals(100, PwDump.cubicPercent(listOf(1.0)))
        assertEquals(0, PwDump.cubicPercent(listOf(0.0, 0.0)))
        assertEquals(100, PwDump.cubicPercent(emptyList()))
    }

    @Test
    fun labels() {
        val output = AudioOutput(41, "alsa_output.x", "Built-in Audio", 40, muted = false, isDefault = true)
        assertEquals("\u2605 Built-in Audio  40%", AudioLabels.outputLine(output))
        assertEquals("   Built-in Audio  muted", AudioLabels.outputLine(output.copy(isDefault = false, muted = true)))
        assertEquals(40, AudioLabels.sliderValue(41))
        assertEquals(45, AudioLabels.sliderValue(43))
        assertEquals(100, AudioLabels.sliderValue(130))
    }

    @Test
    fun openingTheSliderChangesNothingResetDoes() {
        val changes = AudioLabels.SliderChanges(40)
        assertFalse(changes.isChange(40))  // reported as it opens
        assertTrue(changes.isChange(45))
        assertTrue(changes.isChange(50))
        assertFalse(changes.isChange(50))
        assertTrue(changes.isChange(40))   // Reset
    }

    @Test
    fun rolesWriteWirePlumberFragments() {
        assertEquals(BluetoothAudioRole.SINK, BluetoothAudioRole.fromConfig("sink"))
        assertEquals(BluetoothAudioRole.SOURCE, BluetoothAudioRole.fromConfig("source"))
        assertEquals(BluetoothAudioRole.SINK, BluetoothAudioRole.fromConfig("nonsense"))
        assertEquals(BluetoothAudioRole.SINK, BluetoothAudioRole.fromConfig(null))

        val source = BluetoothAudioRole.SOURCE.wirePlumberFragment()
        assertTrue(source, source.contains("monitor.bluez.properties = {\n  bluez5.roles = [ a2dp_source bap_source ]\n}"))
        // No hands-free roles for a speaker; the E39 keeps hands-free.
        assertFalse(source.contains("hfp"))
        assertTrue(BluetoothAudioRole.SINK.wirePlumberFragment().contains("[ a2dp_sink bap_sink hfp_hf ]"))
    }
}
