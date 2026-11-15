package ca.stefanm.ibus.lib.system

import ca.stefanm.ibus.annotations.services.PlatformServiceInfo
import ca.stefanm.ibus.car.di.ConfiguredCarModule
import ca.stefanm.ibus.car.di.ConfiguredCarScope
import ca.stefanm.ibus.car.platform.LongRunningService
import ca.stefanm.ibus.car.platform.PimServiceGroup
import ca.stefanm.ibus.gui.menu.Notification
import ca.stefanm.ibus.gui.menu.notifications.NotificationHub
import ca.stefanm.ibus.lib.logging.Logger
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import java.time.Instant
import javax.inject.Inject
import javax.inject.Named

@PlatformServiceInfo(
    name = "FirstBootNotifierService",
    description = "Shows the progress of slow first-boot tasks (/run/e39rpi-firstboot), " +
            "e.g. setting up the PIM bridge's container (~20 min in QEMU)."
)
@PimServiceGroup
@ConfiguredCarScope
class FirstBootNotifierService @Inject constructor(
    private val notificationHub: NotificationHub,
    private val logger: Logger,
    @Named(ConfiguredCarModule.SERVICE_COROUTINE_SCOPE) coroutineScope: CoroutineScope,
    @Named(ConfiguredCarModule.SERVICE_COROUTINE_DISPATCHER) dispatcher: CoroutineDispatcher,
) : LongRunningService(coroutineScope, dispatcher) {

    companion object {
        const val TAG = "FirstBootNotifier"
        const val POLL_INTERVAL_MS = 5_000L
    }

    private val reader = FirstBootStatusReader()

    override suspend fun doWork() {
        val seen = mutableMapOf<String, FirstBootTask.State>()
        val lastNotified = mutableMapOf<String, Long>()
        val notificationCount = mutableMapOf<String, Int>()
        while (true) {
            val now = Instant.now().epochSecond
            val tasks = withContext(Dispatchers.IO) { reader.read() }
            // A task whose file is gone is forgotten: if it comes back, it's news.
            (seen.keys - tasks.map { it.name }.toSet()).forEach { gone ->
                seen.remove(gone); lastNotified.remove(gone); notificationCount.remove(gone)
            }
            for (task in tasks) {
                val kind = FirstBootTask.notificationKind(
                    previous = seen[task.name],
                    current = task,
                    secondsSinceLastNotification = lastNotified[task.name]?.let { now - it },
                    notificationsSoFar = notificationCount[task.name] ?: 0,
                )
                seen[task.name] = task.state
                if (kind != null) {
                    lastNotified[task.name] = now
                    notificationCount[task.name] = (notificationCount[task.name] ?: 0) + 1
                    val (top, content) = task.notificationText(kind, now)
                    logger.i(TAG, "${task.name}: $kind: $top / $content")
                    notificationHub.postNotificationBackground(
                        Notification(
                            image = when (kind) {
                                FirstBootTask.NotificationKind.FAILED -> Notification.NotificationImage.ALERT_TRIANGLE
                                FirstBootTask.NotificationKind.DONE -> Notification.NotificationImage.MESSAGE_CIRCLE
                                else -> Notification.NotificationImage.ALERT_CIRCLE
                            },
                            topText = top,
                            contentText = content,
                            duration = if (kind == FirstBootTask.NotificationKind.DONE) {
                                Notification.NotificationDuration.SHORT
                            } else {
                                Notification.NotificationDuration.LONG
                            },
                        )
                    )
                }
            }
            delay(POLL_INTERVAL_MS)
        }
    }
}
