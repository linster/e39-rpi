package ca.stefanm.ibus.gui.pim.contacts.repo

import ca.stefanm.e39pim.v1.ContactNotice
import ca.stefanm.ibus.di.ApplicationScope
import ca.stefanm.ibus.gui.pim.calendar.repo.backend.PimBackendChannel
import ca.stefanm.ibus.gui.pim.calendar.repo.backend.PimBackendException
import ca.stefanm.ibus.gui.pim.calendar.repo.backend.PimBackendSettingsRepository
import ca.stefanm.ibus.lib.logging.Logger
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

/**
 * Contacts from the PIM bridge, through [ContactsGrpcClient]. Flows re-query
 * when the bridge reports changes ([ContactsChangeStream]) and retry every
 * [RETRY_INTERVAL] while it's unreachable, keeping what they last emitted.
 */
@ApplicationScope
class RemoteContactsRepository @Inject constructor(
    private val client: ContactsGrpcClient,
    private val backendChannel: PimBackendChannel,
    private val settingsRepository: PimBackendSettingsRepository,
    private val changeStream: ContactsChangeStream,
    private val logger: Logger,
) : ContactsRepository {

    companion object {
        const val TAG = "RemoteContactsRepository"
        val RETRY_INTERVAL: Duration = 10.seconds
    }

    override fun addressBooks(): Flow<List<AddressBook>> = reloading(
        what = "Address books",
        affects = { it.hasReady() || it.hasAddressBooksChanged() || it.hasContactsChanged() },
        load = { client.listAddressBooks().map { it.toAddressBook() } },
    )

    override fun contacts(limit: Int, addressBookId: String?): Flow<ContactList> {
        require(limit in 1..ContactsRepository.MAX_CONTACTS) { "limit $limit not in 1..${ContactsRepository.MAX_CONTACTS}" }
        return reloading(
            what = "Contacts",
            affects = {
                it.hasReady() || it.hasAddressBooksChanged() ||
                    (it.hasContactsChanged() && (addressBookId == null || it.contactsChanged.addressBookId == addressBookId))
            },
            load = { client.listContacts(addressBookId ?: "", limit).toContactList() },
            // The address book is gone: nothing to retry until they change.
            onNotFound = { ContactList(emptyList(), 0) },
        )
    }

    override suspend fun getContact(contactId: String): Result<Contact> = result {
        client.getContact(contactId).toContact()
    }

    override suspend fun search(text: String, addressBookId: String?, limit: Int): Result<ContactList> = result {
        require(text.isNotBlank()) { "Nothing to search for" }
        client.search(text.trim(), addressBookId ?: "", limit).toContactList()
    }

    override suspend fun findByPhoneNumber(number: String): Result<List<PhoneMatch>> = result {
        require(number.any { it.isDigit() }) { "Not a phone number: $number" }
        client.findByPhoneNumber(number.trim()).map { it.toPhoneMatch() }
    }

    override suspend fun getPhoto(contactId: String, maxSize: Int): Result<ContactPhoto> = result {
        require(maxSize in 16..1024) { "maxSize $maxSize not in 16..1024" }
        client.getPhoto(contactId, maxSize).toContactPhoto()
    }

    private fun <T> reloading(
        what: String,
        affects: (ContactNotice) -> Boolean,
        load: suspend () -> T,
        onNotFound: (() -> T)? = null,
    ): Flow<T> = channelFlow {
        val reloads = Channel<Unit>(Channel.CONFLATED)
        launch { changeStream.notices.collect { if (affects(it)) reloads.send(Unit) } }
        launch { settingsRepository.settings.drop(1).collect { reloads.send(Unit) } }
        reloads.send(Unit)

        var retry: Job? = null
        for (reload in reloads) {
            retry?.cancel()
            try {
                send(load())
            } catch (e: PimBackendException.NotFound) {
                if (onNotFound != null) {
                    send(onNotFound())
                } else {
                    logger.w(TAG, "$what: ${e.message}")
                }
            } catch (e: PimBackendException) {
                logger.w(TAG, "$what: ${e.message}")
                retry = launch {
                    delay(RETRY_INTERVAL)
                    backendChannel.resetConnectBackoff()
                    reloads.send(Unit)
                }
            }
        }
    }.flowOn(Dispatchers.IO)

    private suspend fun <T> result(block: suspend () -> T): Result<T> = try {
        Result.success(block())
    } catch (e: PimBackendException) {
        logger.w(TAG, "PIM bridge call failed: ${e.message}")
        Result.failure(e)
    } catch (e: IllegalArgumentException) {
        Result.failure(e)
    }
}
