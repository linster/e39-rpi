package ca.stefanm.ibus.gui.pim.contacts.repo

import ca.stefanm.e39pim.v1.Contact as ProtoContact
import ca.stefanm.e39pim.v1.AddressBook as ProtoAddressBook
import ca.stefanm.e39pim.v1.ContactNotice
import ca.stefanm.e39pim.v1.ContactPhoto as ProtoPhoto
import ca.stefanm.e39pim.v1.ContactServiceGrpcKt
import ca.stefanm.e39pim.v1.ListContactsResponse
import ca.stefanm.e39pim.v1.PhoneMatch as ProtoPhoneMatch
import ca.stefanm.e39pim.v1.findByPhoneNumberRequest
import ca.stefanm.e39pim.v1.getContactPhotoRequest
import ca.stefanm.e39pim.v1.getContactRequest
import ca.stefanm.e39pim.v1.listAddressBooksRequest
import ca.stefanm.e39pim.v1.listContactsRequest
import ca.stefanm.e39pim.v1.searchContactsRequest
import ca.stefanm.e39pim.v1.watchContactsRequest
import ca.stefanm.ibus.gui.pim.calendar.repo.backend.PimBackendChannel
import ca.stefanm.ibus.gui.pim.calendar.repo.backend.PimBackendException
import ca.stefanm.ibus.gui.pim.calendar.repo.backend.pimCall
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import java.util.concurrent.TimeUnit
import javax.inject.Inject

/**
 * The transport layer for contacts: e39pim.v1 ContactService calls, with
 * protobuf messages in and out. [RemoteContactsRepository] builds on it.
 * Every call throws [PimBackendException] on failure.
 */
class ContactsGrpcClient @Inject constructor(
    private val backendChannel: PimBackendChannel,
) {
    companion object {
        /** Caller names must come quickly or not at all (the phone is ringing). */
        const val PHONE_LOOKUP_TIMEOUT_SECONDS = 3L
    }

    private fun stub(seconds: Long = PimBackendChannel.CALL_TIMEOUT_SECONDS) =
        ContactServiceGrpcKt.ContactServiceCoroutineStub(backendChannel.channel())
            .withDeadlineAfter(seconds, TimeUnit.SECONDS)

    suspend fun listAddressBooks(): List<ProtoAddressBook> = pimCall {
        stub().listAddressBooks(listAddressBooksRequest { }).addressBooksList
    }

    /** Sorted by name; [addressBookId] empty: all; [pageSize] 1..500. */
    suspend fun listContacts(addressBookId: String = "", pageSize: Int = 100, pageToken: String = ""): ListContactsResponse = pimCall {
        stub().listContacts(listContactsRequest {
            this.addressBookId = addressBookId
            this.pageSize = pageSize
            this.pageToken = pageToken
        })
    }

    suspend fun getContact(contactId: String): ProtoContact = pimCall {
        stub().getContact(getContactRequest { this.contactId = contactId })
    }

    suspend fun search(text: String, addressBookId: String = "", limit: Int = 50): ListContactsResponse = pimCall {
        stub().searchContacts(searchContactsRequest {
            this.text = text
            this.addressBookId = addressBookId
            this.limit = limit
        })
    }

    suspend fun findByPhoneNumber(number: String): List<ProtoPhoneMatch> = pimCall {
        stub(PHONE_LOOKUP_TIMEOUT_SECONDS).findByPhoneNumber(findByPhoneNumberRequest { this.number = number }).matchesList
    }

    /** [maxSize] 16..1024 (longer side). */
    suspend fun getPhoto(contactId: String, maxSize: Int): ProtoPhoto = pimCall {
        stub().getContactPhoto(getContactPhotoRequest {
            this.contactId = contactId
            this.maxSize = maxSize
        })
    }

    /** `ready` first, then changes. */
    fun watchContacts(): Flow<ContactNotice> =
        ContactServiceGrpcKt.ContactServiceCoroutineStub(backendChannel.channel())
            .watchContacts(watchContactsRequest { })
            .catch { throw PimBackendException.from(it) }
}
