package ca.stefanm.ibus.gui.pim.email.notifier

import ca.stefanm.ibus.annotations.services.PlatformServiceInfo
import ca.stefanm.ibus.car.di.ConfiguredCarModule
import ca.stefanm.ibus.car.di.ConfiguredCarScope
import ca.stefanm.ibus.car.platform.LongRunningService
import ca.stefanm.ibus.car.platform.PimServiceGroup
import ca.stefanm.ibus.gui.menu.Notification
import ca.stefanm.ibus.gui.menu.notifications.NotificationHub
import ca.stefanm.ibus.gui.pim.email.repo.MailEvent
import ca.stefanm.ibus.gui.pim.email.repo.MailRepository
import ca.stefanm.ibus.gui.pim.email.repo.MailSettingsRepository
import ca.stefanm.ibus.lib.logging.Logger
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import javax.inject.Inject
import javax.inject.Named

@PlatformServiceInfo(
    name = "MailNotifierService",
    description = "Shows a notification for new mail, and for mail sent or not sent (PIM bridge)."
)
@PimServiceGroup
@ConfiguredCarScope
class MailNotifierService @Inject constructor(
    private val mailRepository: MailRepository,
    private val mailSettingsRepository: MailSettingsRepository,
    private val notificationHub: NotificationHub,
    private val logger: Logger,
    @Named(ConfiguredCarModule.SERVICE_COROUTINE_SCOPE) coroutineScope: CoroutineScope,
    @Named(ConfiguredCarModule.SERVICE_COROUTINE_DISPATCHER) dispatcher: CoroutineDispatcher,
) : LongRunningService(coroutineScope, dispatcher) {

    companion object {
        const val TAG = "MailNotifier"
    }

    override suspend fun doWork() {
        mailRepository.events.collect { event ->
            val settings = mailSettingsRepository.notifications.value
            val notification = when (event) {
                is MailEvent.NewMail -> if (settings.notifiesFor(event)) newMail(event) else null
                is MailEvent.Sent -> if (settings.sent) Notification(
                    image = Notification.NotificationImage.MESSAGE_SQUARE,
                    topText = "Mail sent",
                    contentText = event.subject,
                ) else null
                is MailEvent.SendFailed -> if (settings.sent) Notification(
                    image = Notification.NotificationImage.ALERT_TRIANGLE,
                    topText = "Mail not sent: ${event.subject}",
                    contentText = event.reason,
                    duration = Notification.NotificationDuration.LONG,
                ) else null
            }
            logger.d(TAG, "$event -> ${notification?.topText ?: "(no notification)"}")
            notification?.let { notificationHub.postNotificationBackground(it) }
        }
    }

    private fun newMail(event: MailEvent.NewMail) = Notification(
        image = Notification.NotificationImage.MESSAGE_SQUARE,
        topText = if (event.count == 1) {
            "New mail from ${event.latest.from.display}"
        } else {
            "${event.count} new messages, latest from ${event.latest.from.display}"
        },
        contentText = event.latest.subject.ifBlank { "(no subject)" },
        duration = Notification.NotificationDuration.LONG,
    )
}
