package ca.stefanm.ibus.gui.pim.email.repo

import ca.stefanm.ibus.configuration.ConfigurationStorage
import ca.stefanm.ibus.configuration.E39Config
import ca.stefanm.ibus.di.ApplicationScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

data class MailNotificationSettings(
    val newMail: Boolean,
    val sent: Boolean,
    val mutedAccountIds: Set<String>,
    val mutedFolderIds: Set<String>,
) {
    fun notifiesFor(event: MailEvent.NewMail): Boolean =
        newMail && event.accountId !in mutedAccountIds && event.folderId !in mutedFolderIds
}

/** The mail app's settings (E39Config.PimConfig.Mail): quick replies and notifications. */
@ApplicationScope
class MailSettingsRepository @Inject constructor(
    private val configurationStorage: ConfigurationStorage,
) {
    private val config get() = configurationStorage.config

    private val _quickReplies = MutableStateFlow(config[E39Config.PimConfig.Mail.quickReplies])
    val quickReplies: StateFlow<List<String>> = _quickReplies.asStateFlow()

    private val _notifications = MutableStateFlow(readNotifications())
    val notifications: StateFlow<MailNotificationSettings> = _notifications.asStateFlow()

    /** Fake or remote data: takes effect after a restart (MailModule). */
    val useFakeData: Boolean get() = config[E39Config.PimConfig.Mail.useFakeData]

    fun setUseFakeData(fake: Boolean) {
        config[E39Config.PimConfig.Mail.useFakeData] = fake
    }

    fun addQuickReply(text: String) {
        val cleaned = text.trim()
        require(cleaned.isNotEmpty()) { "Empty reply" }
        if (cleaned !in _quickReplies.value) setQuickReplies(_quickReplies.value + cleaned)
    }

    fun removeQuickReply(text: String) = setQuickReplies(_quickReplies.value - text)

    fun moveQuickReply(from: Int, to: Int) {
        val list = _quickReplies.value.toMutableList()
        list.add(to.coerceIn(0, list.size - 1), list.removeAt(from))
        setQuickReplies(list)
    }

    fun setQuickReplies(replies: List<String>) {
        config[E39Config.PimConfig.Mail.quickReplies] = replies
        _quickReplies.value = replies
    }

    fun setNewMailNotifications(enabled: Boolean) {
        config[E39Config.PimConfig.Mail.notificationsEnabled] = enabled
        _notifications.value = readNotifications()
    }

    fun setSentNotifications(enabled: Boolean) {
        config[E39Config.PimConfig.Mail.sentNotificationsEnabled] = enabled
        _notifications.value = readNotifications()
    }

    fun setAccountMuted(accountId: String, muted: Boolean) {
        val current = config[E39Config.PimConfig.Mail.mutedAccountIds]
        config[E39Config.PimConfig.Mail.mutedAccountIds] = if (muted) (current + accountId).distinct() else current - accountId
        _notifications.value = readNotifications()
    }

    fun setFolderMuted(folderId: String, muted: Boolean) {
        val current = config[E39Config.PimConfig.Mail.mutedFolderIds]
        config[E39Config.PimConfig.Mail.mutedFolderIds] = if (muted) (current + folderId).distinct() else current - folderId
        _notifications.value = readNotifications()
    }

    private fun readNotifications() = MailNotificationSettings(
        newMail = config[E39Config.PimConfig.Mail.notificationsEnabled],
        sent = config[E39Config.PimConfig.Mail.sentNotificationsEnabled],
        mutedAccountIds = config[E39Config.PimConfig.Mail.mutedAccountIds].toSet(),
        mutedFolderIds = config[E39Config.PimConfig.Mail.mutedFolderIds].toSet(),
    )
}
