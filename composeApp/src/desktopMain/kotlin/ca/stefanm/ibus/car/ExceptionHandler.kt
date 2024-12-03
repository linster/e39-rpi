package ca.stefanm.ca.stefanm.ibus.car

import ca.stefanm.ibus.gui.menu.Notification
import ca.stefanm.ca.stefanm.ibus.gui.menu.notifications.NotificationHub
import ca.stefanm.ca.stefanm.ibus.lib.logging.Logger
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineName
import javax.inject.Inject
import kotlin.coroutines.CoroutineContext

/// Default Exception Handler for Car Services
class ExceptionHandler @Inject constructor(
    private val notificationHub: NotificationHub,
    private val logger: Logger
) {

    val handler : CoroutineExceptionHandler = CoroutineExceptionHandler { coroutineContext, throwable ->
        logger.e("CarServiceUncaughtExceptionHandler", throwable.message.toString(), throwable)
        notificationHub.postNotificationBackground(Notification(
            Notification.NotificationImage.ALERT_OCTAGON,
            "Uncaught Car Services Exception ${throwable.message}"
        ))
    }

}