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

    companion object {
        // One state for the whole process: the car platform's services
        // (MailNotifierService) get their own Dagger component and with it
        // their own instance, which must see settings changed on screen.
        private var shared: Pair<MutableStateFlow<List<String>>, MutableStateFlow<MailNotificationSettings>>? = null
    }

    private val state = synchronized(Companion) {
        shared ?: (MutableStateFlow(config[E39Config.PimConfig.Mail.quickReplies]) to MutableStateFlow(readNotifications()))
            .also { shared = it }
    }

    private val _quickReplies = state.first
    val quickReplies: StateFlow<List<String>> = _quickReplies.asStateFlow()

    private val _notifications = state.second
    val notifications: StateFlow<MailNotificationSettings> = _notifications.asStateFlow()

    /** Fake or remote data: takes effect after a restart (MailModule). */
    val useFakeData: Boolean get() = config[E39Config.PimConfig.Mail.useFakeData]

    fun setUseFakeData(fake: Boolean) {
        config[E39Config.PimConfig.Mail.useFakeData] = fake
    }

    /** IllegalArgumentException for an empty one; nothing changes for one that's there already. */
    fun addQuickReply(text: String) = setQuickReplies(QuickReplyEdits.add(_quickReplies.value, text))

    /** The reply at [index] becomes [text]; IllegalArgumentException for an empty one or a duplicate. */
    fun replaceQuickReply(index: Int, text: String) = setQuickReplies(QuickReplyEdits.replace(_quickReplies.value, index, text))

    fun removeQuickReply(text: String) = setQuickReplies(_quickReplies.value - text)

    fun moveQuickReply(from: Int, to: Int) = setQuickReplies(QuickReplyEdits.move(_quickReplies.value, from, to))

    fun setQuickReplies(replies: List<String>) {
        config[E39Config.PimConfig.Mail.quickReplies] = replies
        _quickReplies.value = replies
    }

    /** Notifications on, nothing muted, the default quick replies (UI tests). */
    fun resetToDefaults() {
        setQuickReplies(E39Config.PimConfig.Mail.quickReplies.default)
        config[E39Config.PimConfig.Mail.notificationsEnabled] = true
        config[E39Config.PimConfig.Mail.sentNotificationsEnabled] = true
        config[E39Config.PimConfig.Mail.mutedAccountIds] = emptyList()
        config[E39Config.PimConfig.Mail.mutedFolderIds] = emptyList()
        _notifications.value = readNotifications()
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

/** Editing the quick replies list (no duplicates, no empty ones). */
object QuickReplyEdits {
    fun add(list: List<String>, text: String): List<String> {
        val cleaned = text.trim()
        require(cleaned.isNotEmpty()) { "Empty reply" }
        return if (cleaned in list) list else list + cleaned
    }

    fun replace(list: List<String>, index: Int, text: String): List<String> {
        val cleaned = text.trim()
        require(cleaned.isNotEmpty()) { "Empty reply" }
        require(index in list.indices) { "No reply $index" }
        require(list.withIndex().none { (i, r) -> i != index && r == cleaned }) { "\"$cleaned\" is there already" }
        return list.toMutableList().also { it[index] = cleaned }
    }

    /** [to] is clamped to the list; moving off an end does nothing. */
    fun move(list: List<String>, from: Int, to: Int): List<String> {
        if (from !in list.indices) return list
        val l = list.toMutableList()
        l.add(to.coerceIn(0, l.size - 1), l.removeAt(from))
        return l
    }
}
