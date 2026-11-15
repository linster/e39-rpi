package ca.stefanm.ibus.lib.audio

import ca.stefanm.ibus.lib.logging.Logger
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.util.concurrent.TimeUnit
import javax.inject.Inject

/**
 * Which way Bluetooth audio flows. PipeWire registers the Bluetooth audio
 * roles (A2DP endpoints, hands-free profiles) with BlueZ that WirePlumber's
 * `bluez5.roles` lists; phones and speakers see the device as whatever is
 * registered.
 */
enum class BluetoothAudioRole(
    val configValue: String,
    val label: String,
    val roles: List<String>,
) {
    /** The E39: phones play to the device, which plays to the radio's Aux-in. */
    SINK("sink", "Receive from phones", listOf("a2dp_sink", "bap_sink", "hfp_hf")),

    /**
     * A device that plays to a Bluetooth speaker: the speaker is an output.
     * No hands-free roles: a speaker with a microphone would switch to the
     * hands-free profile, in phone-call quality.
     */
    SOURCE("source", "Play to a speaker", listOf("a2dp_source", "bap_source"));

    /** A WirePlumber config fragment that sets the roles. */
    fun wirePlumberFragment(): String = """
        |# Written by the e39-rpi HMI (Settings -> Audio): Bluetooth audio role "$configValue".
        |monitor.bluez.properties = {
        |  bluez5.roles = [ ${roles.joinToString(" ")} ]
        |}
        |""".trimMargin()

    companion object {
        val DEFAULT = SINK

        fun fromConfig(value: String?): BluetoothAudioRole =
            entries.firstOrNull { it.configValue == value } ?: DEFAULT
    }
}

/**
 * Applies a [BluetoothAudioRole]: writes WirePlumber's config fragment in the
 * HMI user's ~/.config/wireplumber/wireplumber.conf.d and restarts
 * WirePlumber (a user service of the same user, like PipeWire), which then
 * registers the new roles with BlueZ. Bluetooth audio drops for a moment;
 * connected devices reconnect.
 *
 * Only where the image says the HMI manages audio ([AudioOutputs.managedHere]).
 */
class BluetoothAudioRoleApplier @Inject constructor(
    private val logger: Logger,
) {
    companion object {
        const val TAG = "BluetoothAudioRole"
        const val FRAGMENT_NAME = "60-e39rpi-bluetooth-role.conf"
    }

    private val fragment = File(System.getProperty("user.home"), ".config/wireplumber/wireplumber.conf.d/$FRAGMENT_NAME")

    fun canChange(): Boolean = AudioOutputs.managedHere()

    /** The role WirePlumber was last told, or null if nothing was written yet. */
    fun current(): BluetoothAudioRole? =
        runCatching { fragment.readText() }.getOrNull()?.let { text ->
            BluetoothAudioRole.entries.firstOrNull { it.wirePlumberFragment() == text }
        }

    /**
     * Writes the fragment if it says something else, and then restarts
     * WirePlumber. Returns whether anything changed.
     */
    suspend fun apply(role: BluetoothAudioRole): Result<Boolean> = withContext(Dispatchers.IO) {
        runCatching {
            check(canChange()) { "Audio isn't managed by the HMI on this system" }
            val text = role.wirePlumberFragment()
            if (runCatching { fragment.readText() }.getOrNull() == text) return@runCatching false
            fragment.parentFile.mkdirs()
            val temp = File(fragment.parentFile, "$FRAGMENT_NAME.new")
            temp.writeText(text)
            check(temp.renameTo(fragment)) { "Couldn't write $fragment" }
            logger.i(TAG, "Bluetooth audio role: ${role.configValue}; restarting WirePlumber")
            val process = ProcessBuilder("systemctl", "--user", "restart", "wireplumber.service")
                .redirectErrorStream(true).start()
            val output = process.inputStream.bufferedReader().readText()
            check(process.waitFor(30, TimeUnit.SECONDS)) { "Restarting WirePlumber timed out" }
            check(process.exitValue() == 0) { "Restarting WirePlumber: ${output.trim().take(200)}" }
            true
        }.onFailure { logger.w(TAG, "Couldn't apply ${role.configValue}: ${it.message}") }
    }
}
