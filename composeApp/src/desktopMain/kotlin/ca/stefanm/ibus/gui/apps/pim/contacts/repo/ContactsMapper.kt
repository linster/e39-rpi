package ca.stefanm.ibus.gui.pim.contacts.repo

import ca.stefanm.e39pim.v1.AddressBook as ProtoAddressBook
import ca.stefanm.e39pim.v1.AddressType
import ca.stefanm.e39pim.v1.Contact as ProtoContact
import ca.stefanm.e39pim.v1.ContactPhoto as ProtoPhoto
import ca.stefanm.e39pim.v1.ContactSummary as ProtoSummary
import ca.stefanm.e39pim.v1.EmailType
import ca.stefanm.e39pim.v1.ListContactsResponse
import ca.stefanm.e39pim.v1.PhoneMatch as ProtoPhoneMatch
import ca.stefanm.e39pim.v1.PhoneNumber as ProtoPhone
import ca.stefanm.e39pim.v1.PhoneType
import ca.stefanm.ibus.gui.pim.calendar.repo.backend.toLocalDate

// e39pim.v1 contacts messages -> the HMI's contact models.

fun ProtoAddressBook.toAddressBook() = AddressBook(
    id = id,
    accountId = accountId,
    accountName = accountName,
    name = name,
    readOnly = readOnly,
    contactCount = contactCount,
)

fun ProtoSummary.toContactSummary() = ContactSummary(
    id = id,
    addressBookId = addressBookId,
    displayName = displayName,
    sortKey = sortKey,
    givenName = givenName,
    familyName = familyName,
    organization = organization,
    primaryPhone = primaryPhone,
    primaryEmail = primaryEmail,
    hasPhoto = hasPhoto,
)

fun ProtoPhone.toPhoneNumber() = PhoneNumber(
    number = number,
    e164 = e164,
    type = when (type) {
        PhoneType.PHONE_TYPE_MOBILE -> PhoneNumber.Type.MOBILE
        PhoneType.PHONE_TYPE_HOME -> PhoneNumber.Type.HOME
        PhoneType.PHONE_TYPE_WORK -> PhoneNumber.Type.WORK
        PhoneType.PHONE_TYPE_MAIN -> PhoneNumber.Type.MAIN
        PhoneType.PHONE_TYPE_FAX -> PhoneNumber.Type.FAX
        PhoneType.PHONE_TYPE_PAGER -> PhoneNumber.Type.PAGER
        PhoneType.PHONE_TYPE_CAR -> PhoneNumber.Type.CAR
        else -> PhoneNumber.Type.OTHER
    },
    label = label,
    preferred = preferred,
)

fun ProtoContact.toContact() = Contact(
    summary = summary.toContactSummary(),
    nickname = nickname,
    title = title,
    phones = phonesList.map { it.toPhoneNumber() },
    emails = emailsList.map {
        EmailAddress(
            address = it.address,
            type = when (it.type) {
                EmailType.EMAIL_TYPE_HOME -> EmailAddress.Type.HOME
                EmailType.EMAIL_TYPE_WORK -> EmailAddress.Type.WORK
                else -> EmailAddress.Type.OTHER
            },
            preferred = it.preferred,
        )
    },
    addresses = addressesList.map {
        PostalAddress(
            type = when (it.type) {
                AddressType.ADDRESS_TYPE_HOME -> PostalAddress.Type.HOME
                AddressType.ADDRESS_TYPE_WORK -> PostalAddress.Type.WORK
                else -> PostalAddress.Type.OTHER
            },
            street = it.street,
            locality = it.locality,
            region = it.region,
            postalCode = it.postalCode,
            country = it.country,
            formatted = it.formatted,
            label = it.label,
        )
    },
    birthday = if (hasBirthday() && birthday.month in 1..12) birthday.toLocalDate() else null,
    note = note,
    urls = urlsList,
)

fun ListContactsResponse.toContactList() = ContactList(
    contacts = contactsList.map { it.toContactSummary() },
    totalCount = totalCount,
)

fun ProtoPhoneMatch.toPhoneMatch() = PhoneMatch(
    contact = contact.toContactSummary(),
    phone = phone.toPhoneNumber(),
    exact = exact,
)

fun ProtoPhoto.toContactPhoto() = ContactPhoto(jpeg = jpeg.toByteArray(), width = width, height = height)
