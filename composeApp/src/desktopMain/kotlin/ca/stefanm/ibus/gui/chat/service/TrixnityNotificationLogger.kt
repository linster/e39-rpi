package ca.stefanm.ibus.gui.chat.service

import ca.stefanm.ibus.gui.menu.notifications.NotificationHub
import ca.stefanm.ibus.lib.logging.Logger
import javax.inject.Inject


/// Used to hook into Trixnity's logger infra and spam the notification tray
class TrixnityNotificationLogger @Inject constructor(
    private val logger: Logger,
    private val notificationHub: NotificationHub
){



}