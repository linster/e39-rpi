package ca.stefanm.ibus.lib.audio

import ca.stefanm.ibus.lib.logging.Logger
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import kotlin.math.cbrt
import kotlin.math.roundToInt

/**
 * An audio output (a PipeWire sink): the Pi's audio out to the radio's Aux-in,
 * QEMU's emulated sound card, a Bluetooth speaker (Bluetooth source mode).
 *
 * [volumePercent] is on WirePlumber's cubic scale, like `wpctl`: PipeWire
 * stores linear channel volumes (0.064), wpctl shows and sets their cube root
 * (0.40, i.e. 40 %), which matches how loud it sounds.
 */
data class AudioOutput(
    val id: Int,
    val name: String,
    val description: String,
    val volumePercent: Int,
    val muted: Boolean,
    val isDefault: Boolean,
) {
    val isBluetooth: Boolean get() = name.startsWith("bluez_output")
}

/** Reads `pw-dump`'s JSON (no PipeWire needed: unit-tested). */
object PwDump {

    fun outputs(json: String): List<AudioOutput> {
        val objects = runCatching { JsonParser.parseString(json).asJsonArray }.getOrNull() ?: return emptyList()
        val defaultName = objects.asSequence()
            .mapNotNull { it.asObjectOrNull() }
            .filter { it.string("type") == "PipeWire:Interface:Metadata" }
            .flatMap { (it.get("metadata")?.takeIf { m -> m.isJsonArray }?.asJsonArray ?: emptyList()).asSequence() }
            .mapNotNull { it.asObjectOrNull() }
            .firstOrNull { it.string("key") == "default.audio.sink" }
            ?.let { defaultSinkName(it.get("value")) }

        return objects.mapNotNull { element ->
            val obj = element.asObjectOrNull() ?: return@mapNotNull null
            val info = obj.getAsJsonObjectOrNull("info") ?: return@mapNotNull null
            val props = info.getAsJsonObjectOrNull("props") ?: return@mapNotNull null
            if (props.string("media.class") != "Audio/Sink") return@mapNotNull null
            val name = props.string("node.name") ?: return@mapNotNull null
            val params = info.getAsJsonObjectOrNull("params")?.get("Props")
                ?.takeIf { it.isJsonArray }?.asJsonArray?.firstOrNull()?.asObjectOrNull()
            val channels = params?.get("channelVolumes")?.takeIf { it.isJsonArray }?.asJsonArray
                ?.mapNotNull { runCatching { it.asDouble }.getOrNull() }.orEmpty()
            AudioOutput(
                id = obj.get("id").asInt,
                name = name,
                description = props.string("node.description") ?: props.string("node.nick") ?: name,
                volumePercent = cubicPercent(channels),
                muted = params?.get("mute")?.let { runCatching { it.asBoolean }.getOrNull() } ?: false,
                isDefault = name == defaultName,
            )
        }.sortedWith(compareByDescending<AudioOutput> { it.isDefault }.thenBy { it.description })
    }

    /** Linear channel volumes -> percent on wpctl's cubic scale. */
    fun cubicPercent(linearChannelVolumes: List<Double>): Int {
        if (linearChannelVolumes.isEmpty()) return 100
        return (cbrt(linearChannelVolumes.average()) * 100).roundToInt()
    }

    // pw-dump gives the metadata value as an object ({"name": ...}), older
    // versions as a JSON string.
    private fun defaultSinkName(value: JsonElement?): String? = when {
        value == null || value.isJsonNull -> null
        value.isJsonObject -> value.asJsonObject.string("name")
        value.isJsonPrimitive -> runCatching { JsonParser.parseString(value.asString).asJsonObject.string("name") }.getOrNull()
        else -> null
    }

    private fun JsonElement.asObjectOrNull(): JsonObject? = if (isJsonObject) asJsonObject else null
    private fun JsonObject.getAsJsonObjectOrNull(key: String): JsonObject? = get(key)?.asObjectOrNull()
    private fun JsonObject.string(key: String): String? =
        get(key)?.takeIf { it.isJsonPrimitive }?.asString
}

/**
 * The audio outputs through PipeWire's tools, which the HMI can use directly:
 * PipeWire and WirePlumber run as the HMI's user (e39-rpi-yocto: per-user
 * services in hmi's login session). WirePlumber remembers each output's
 * volume across restarts.
 *
 * Only where the image says the HMI manages audio (E39_HMI_MANAGES_AUDIO=1 in
 * the launcher's environment): on a laptop these would change the
 * developer's own volume.
 */
class AudioOutputs @Inject constructor(
    private val logger: Logger,
) {
    companion object {
        const val TAG = "AudioOutputs"

        fun managedHere(): Boolean = System.getenv("E39_HMI_MANAGES_AUDIO") == "1"
    }

    fun canChange(): Boolean = managedHere()

    suspend fun list(): List<AudioOutput> =
        if (!managedHere()) emptyList()
        else run("pw-dump").getOrNull()?.let { PwDump.outputs(it) }.orEmpty()

    suspend fun default(): AudioOutput? = list().firstOrNull { it.isDefault }

    suspend fun setVolume(output: AudioOutput, percent: Int): Result<Unit> {
        logger.i(TAG, "${output.description} (${output.id}): volume $percent%")
        return wpctl("set-volume", output.id.toString(), "%.2f".format(java.util.Locale.ROOT, percent.coerceIn(0, 150) / 100.0))
    }

    suspend fun setMuted(output: AudioOutput, muted: Boolean): Result<Unit> {
        logger.i(TAG, "${output.description} (${output.id}): ${if (muted) "mute" else "unmute"}")
        return wpctl("set-mute", output.id.toString(), if (muted) "1" else "0")
    }

    suspend fun makeDefault(output: AudioOutput): Result<Unit> {
        logger.i(TAG, "${output.description} (${output.id}): default output")
        return wpctl("set-default", output.id.toString())
    }

    private suspend fun wpctl(vararg args: String): Result<Unit> =
        if (!managedHere()) Result.failure(IllegalStateException("Audio isn't managed by the HMI on this system"))
        else run("wpctl", *args).map { }

    private suspend fun run(vararg command: String): Result<String> = withContext(Dispatchers.IO) {
        runCatching {
            val process = ProcessBuilder(*command).redirectErrorStream(true).start()
            val output = process.inputStream.bufferedReader().readText()
            if (!process.waitFor(10, TimeUnit.SECONDS)) {
                process.destroyForcibly()
                error("${command.first()} timed out")
            }
            if (process.exitValue() != 0) error("${command.joinToString(" ")}: ${output.trim().take(200)}")
            output
        }.onFailure { logger.w(TAG, "Failed: ${it.message}") }
    }
}
