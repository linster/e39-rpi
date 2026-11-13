package ca.stefanm.ibus.lib.system

import ca.stefanm.ibus.lib.logging.Logger
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.util.concurrent.TimeUnit
import javax.inject.Inject

/**
 * Restarting the HMI and rebooting the computer, e.g. when the Pico asks
 * (PicoToPi PiSoftPowerRestartX / PiSoftPowerRestartPi).
 *
 * On the e39-rpi image (e39-rpi-yocto) the HMI is the systemd user service
 * [HMI_UNIT] of the unprivileged user hmi, and the image has no polkit or
 * sudo:
 * - Restarting the HMI: a user may manage its own user units, so
 *   `systemctl --user restart` (no-block: the restart stops this process).
 * - Rebooting needs root: the HMI writes the request to [POWER_REQUEST], a
 *   folder only hmi may write to; a root path unit (e39rpi-power.path,
 *   meta-e39rpi-gui) sees it and reboots.
 *
 * Elsewhere (a laptop, the old Raspbian image) these fail with a reason
 * rather than doing anything.
 */
class PiLifecycle @Inject constructor(
    private val logger: Logger,
) {
    companion object {
        const val TAG = "PiLifecycle"
        const val HMI_UNIT = "e39rpi-hmi.service"
        val POWER_REQUEST = File("/run/e39rpi-power/request")
    }

    /** Started by systemd as a service? (systemd sets INVOCATION_ID for its services.) */
    private fun runBySystemd(): Boolean = System.getenv("INVOCATION_ID") != null

    /** Ask systemd to restart the HMI (this process). */
    suspend fun restartHmi(): Result<Unit> = withContext(Dispatchers.IO) {
        runCatching {
            check(runBySystemd()) { "the HMI wasn't started by systemd ($HMI_UNIT), so it can't restart itself" }
            logger.w(TAG, "Restarting the HMI ($HMI_UNIT)")
            val process = ProcessBuilder("systemctl", "--user", "--no-block", "restart", HMI_UNIT)
                .redirectErrorStream(true)
                .start()
            check(process.waitFor(10, TimeUnit.SECONDS)) { "systemctl didn't answer" }
            check(process.exitValue() == 0) {
                "systemctl --user restart $HMI_UNIT: ${process.inputStream.bufferedReader().readText().trim()}"
            }
        }.onFailure { logger.e(TAG, "Can't restart the HMI: ${it.message}", it) }
    }

    /** Ask the image's e39rpi-power helper (root) to reboot. */
    suspend fun reboot(): Result<Unit> = withContext(Dispatchers.IO) {
        runCatching {
            check(POWER_REQUEST.parentFile.isDirectory) {
                "no ${POWER_REQUEST.parent} (the image's e39rpi-power helper isn't installed)"
            }
            logger.w(TAG, "Asking for a reboot ($POWER_REQUEST)")
            POWER_REQUEST.writeText("reboot\n")
        }.onFailure { logger.e(TAG, "Can't reboot: ${it.message}", it) }
    }
}
