package ca.stefanm.ibus.lib.system

import java.io.File

/**
 * A slow task on the device's first boot, as the image reports it in
 * /run/e39rpi-firstboot/<name>.status (e39-rpi-yocto: meta-e39rpi-pim's
 * load-image writes e39pim.status while it loads the PIM container image,
 * ~20 min in QEMU). The file is key=value lines:
 *
 *     state=running|done|failed
 *     title=First-boot setup   (the notification's first line; short: the
 *                              box fits one title line and two text lines)
 *     text=Calendars & mail    (the second line; while running, the HMI
 *                              adds ": about N min left")
 *     minutes=20               (the expected duration)
 *     started=1763170000       (epoch seconds)
 */
data class FirstBootTask(
    val name: String,
    val state: State,
    val title: String,
    val text: String,
    val minutes: Int?,
    val startedEpochSeconds: Long?,
) {
    enum class State { RUNNING, DONE, FAILED }

    enum class NotificationKind { STARTED, REMINDER, DONE, FAILED }

    /** Minutes left by the estimate (at least 1), or null without one. */
    fun minutesLeft(nowEpochSeconds: Long): Int? {
        val minutes = minutes ?: return null
        val started = startedEpochSeconds ?: return minutes
        val elapsed = ((nowEpochSeconds - started).coerceAtLeast(0) / 60).toInt()
        return (minutes - elapsed).coerceAtLeast(1)
    }

    /** The notification's two lines for [kind]. */
    fun notificationText(kind: NotificationKind, nowEpochSeconds: Long): Pair<String, String> = when (kind) {
        NotificationKind.STARTED, NotificationKind.REMINDER -> title to (
            minutesLeft(nowEpochSeconds)?.let { left ->
                if (text.isEmpty()) "About $left min left" else "$text: about $left min left"
            } ?: text
        )
        NotificationKind.DONE, NotificationKind.FAILED -> title to text
    }

    companion object {
        const val REMINDER_INTERVAL_SECONDS = 5 * 60L

        // The HMI's services start before its window is shown (~35 s in
        // QEMU), so the first notification can come and go unseen: the first
        // reminder comes sooner.
        const val FIRST_REMINDER_SECONDS = 60L

        fun parse(name: String, content: String): FirstBootTask? {
            val values = content.lineSequence()
                .mapNotNull { line -> line.split('=', limit = 2).takeIf { it.size == 2 } }
                .associate { (key, value) -> key.trim() to value.trim() }
            val state = when (values["state"]) {
                "running" -> State.RUNNING
                "done" -> State.DONE
                "failed" -> State.FAILED
                else -> return null
            }
            val title = values["title"]?.takeIf { it.isNotEmpty() } ?: return null
            return FirstBootTask(
                name = name,
                state = state,
                title = title,
                text = values["text"].orEmpty(),
                minutes = values["minutes"]?.toIntOrNull(),
                startedEpochSeconds = values["started"]?.toLongOrNull(),
            )
        }

        /**
         * Whether to show a notification for [current], given the state seen
         * last time ([previous], null if the task is new to this HMI run),
         * how long ago its last notification was and how many it has had. A
         * task that was already done when the HMI started (an HMI restart)
         * shows nothing.
         */
        fun notificationKind(
            previous: State?,
            current: FirstBootTask,
            secondsSinceLastNotification: Long?,
            notificationsSoFar: Int = 0,
        ): NotificationKind? =
            when (current.state) {
                State.RUNNING -> {
                    val interval = if (notificationsSoFar <= 1) FIRST_REMINDER_SECONDS else REMINDER_INTERVAL_SECONDS
                    when {
                        previous != State.RUNNING -> NotificationKind.STARTED
                        secondsSinceLastNotification == null || secondsSinceLastNotification >= interval -> NotificationKind.REMINDER
                        else -> null
                    }
                }
                State.DONE -> if (previous == State.RUNNING) NotificationKind.DONE else null
                State.FAILED -> if (previous != State.FAILED) NotificationKind.FAILED else null
            }
    }
}

class FirstBootStatusReader(private val directory: File = File(DIRECTORY)) {
    companion object {
        const val DIRECTORY = "/run/e39rpi-firstboot"
    }

    /** The tasks with a readable status file, by name; none off the device. */
    fun read(): List<FirstBootTask> =
        (directory.listFiles { file -> file.isFile && file.name.endsWith(".status") } ?: emptyArray())
            .sortedBy { it.name }
            .mapNotNull { file ->
                val text = runCatching { file.readText() }.getOrNull() ?: return@mapNotNull null
                FirstBootTask.parse(file.name.removeSuffix(".status"), text)
            }
}
