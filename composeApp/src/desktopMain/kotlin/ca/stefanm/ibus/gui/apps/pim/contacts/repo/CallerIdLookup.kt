package ca.stefanm.ibus.gui.pim.contacts.repo

import ca.stefanm.ibus.di.ApplicationScope
import kotlinx.coroutines.withTimeoutOrNull
import javax.inject.Inject
import kotlin.time.Clock
import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds
import kotlin.time.Instant

/** Who a phone number belongs to, for showing an incoming (or outgoing) call. */
data class CallerId(
    val number: String,
    val name: String,
    /** "Mobile", "Work", ...: which of the contact's numbers it is. */
    val phoneLabel: String,
    val contactId: String,
    val hasPhoto: Boolean,
    /** False if the contact has the number without its area code (could be someone else's). */
    val exact: Boolean,
)

/**
 * Caller names for the telephone code: [lookup] a number as the phone
 * reports it. Answers within [TIMEOUT] or not at all (the phone is ringing),
 * and remembers answers for [CACHE_TIME] (calls repeat, and phones report a
 * number several times per call).
 */
@ApplicationScope
class CallerIdLookup @Inject constructor(
    private val contactsRepository: ContactsRepository,
) {
    companion object {
        val TIMEOUT: Duration = 3.seconds
        val CACHE_TIME: Duration = 10.minutes
        const val CACHE_SIZE = 50
    }

    private data class Cached(val callerId: CallerId?, val at: Instant)

    private val cache = object : LinkedHashMap<String, Cached>(CACHE_SIZE, 0.75f, true) {
        override fun removeEldestEntry(eldest: MutableMap.MutableEntry<String, Cached>?) = size > CACHE_SIZE
    }

    /** The best match (exact first, then by name), or null if nobody has the number or there's no answer in time. */
    suspend fun lookup(number: String): CallerId? {
        val key = number.filter { it.isDigit() || it == '+' }
        if (key.none { it.isDigit() }) return null
        val now = Clock.System.now()
        synchronized(cache) {
            cache[key]?.let { if (now - it.at < CACHE_TIME) return it.callerId }
        }
        val matches = withTimeoutOrNull(TIMEOUT) { contactsRepository.findByPhoneNumber(number) } ?: return null
        // A failure (bridge not reachable) isn't cached: try again next time.
        val found = matches.getOrNull() ?: return null
        val callerId = found.firstOrNull()?.let {
            CallerId(
                number = number,
                name = it.contact.displayName,
                phoneLabel = it.phone.label.ifEmpty { it.phone.type.name.lowercase().replaceFirstChar { c -> c.uppercase() } },
                contactId = it.contact.id,
                hasPhoto = it.contact.hasPhoto,
                exact = it.exact,
            )
        }
        synchronized(cache) { cache[key] = Cached(callerId, now) }
        return callerId
    }

    /** After the contacts changed a lot (e.g. an account was added). */
    fun clearCache() = synchronized(cache) { cache.clear() }
}
