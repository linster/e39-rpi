package ca.stefanm.ibus.gui.pim.contacts.debug

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Button
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.toComposeImageBitmap
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.WindowScope
import ca.stefanm.ibus.gui.debug.windows.CheckBoxWithLabel
import ca.stefanm.ibus.gui.debug.windows.NestingCard
import ca.stefanm.ibus.gui.debug.windows.NestingCardHeader
import ca.stefanm.ibus.gui.debug.windows.NumericTextViewWithSpinnerButtons
import ca.stefanm.ibus.gui.menu.navigator.WindowManager
import ca.stefanm.ibus.gui.pim.calendar.repo.backend.PimAccount
import ca.stefanm.ibus.gui.pim.calendar.repo.backend.PimBackendException
import ca.stefanm.ibus.gui.pim.calendar.repo.backend.PimBackendSettingsRepository
import ca.stefanm.ibus.gui.pim.calendar.repo.backend.RemoteAccountRepository
import ca.stefanm.ibus.gui.pim.contacts.repo.AddressBook
import ca.stefanm.ibus.gui.pim.contacts.repo.CallerId
import ca.stefanm.ibus.gui.pim.contacts.repo.CallerIdLookup
import ca.stefanm.ibus.gui.pim.contacts.repo.Contact
import ca.stefanm.ibus.gui.pim.contacts.repo.ContactField
import ca.stefanm.ibus.gui.pim.contacts.repo.ContactInput
import ca.stefanm.ibus.gui.pim.contacts.repo.ContactList
import ca.stefanm.ibus.gui.pim.contacts.repo.EmailAddress
import ca.stefanm.ibus.gui.pim.contacts.repo.PhoneNumber
import ca.stefanm.ibus.gui.pim.contacts.repo.ContactSummary
import ca.stefanm.ibus.gui.pim.contacts.repo.ContactsRepository
import ca.stefanm.ibus.gui.pim.contacts.repo.FakeContactsRepository
import ca.stefanm.ibus.gui.pim.contacts.repo.PhoneMatch
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Everything the contacts repositories can do: address books and the list
 * (live), a contact's details and picture, search, caller lookup (the
 * repository's matches and CallerIdLookup's answer), creating, editing and
 * deleting contacts, and turning contacts on and off for CalDAV accounts.
 */
class ContactsDebugWindow @Inject constructor(
    private val contactsRepository: ContactsRepository,
    private val callerIdLookup: CallerIdLookup,
    private val accountRepository: RemoteAccountRepository,
    private val backendSettingsRepository: PimBackendSettingsRepository,
) : WindowManager.E39Window {

    override val tag: Any
        get() = this

    override val title: String = "Contacts (PIM Bridge) Debug"

    override val size: DpSize = DpSize(1400.dp, 950.dp)

    override val defaultPosition: WindowManager.E39Window.DefaultPosition
        get() = WindowManager.E39Window.DefaultPosition.ANYWHERE

    override fun content(): @Composable WindowScope.() -> Unit = {
        val selectedBook = remember { mutableStateOf<AddressBook?>(null) }
        val selectedContact = remember { mutableStateOf<ContactSummary?>(null) }

        Row(Modifier.fillMaxSize()) {
            Column(Modifier.weight(0.45f).verticalScroll(rememberScrollState())) {
                RepositoryPane()
                AccountsPane()
                AddressBooksPane(selectedBook.value) { selectedBook.value = if (selectedBook.value?.id == it.id) null else it }
                ContactsPane(selectedBook.value, selectedContact.value) { selectedContact.value = it }
            }
            Column(Modifier.weight(0.55f).verticalScroll(rememberScrollState())) {
                ContactPane(selectedContact.value)
                EditPane(selectedBook.value, selectedContact.value) { selectedContact.value = it }
                SearchPane(selectedBook.value) { selectedContact.value = it }
                PhonePane { selectedContact.value = it }
            }
        }
    }

    @Composable
    private fun RepositoryPane() {
        NestingCard {
            NestingCardHeader("ContactsRepository")
            Text("In use: ${contactsRepository::class.simpleName} (PimConfig.Contacts.useFakeData, at startup)")
            if (contactsRepository !is FakeContactsRepository) {
                Text("PIM bridge: ${backendSettingsRepository.settings.collectAsState().value.target}")
            }
        }
    }

    @Composable
    private fun AccountsPane() {
        val scope = rememberCoroutineScope()
        val accounts = remember { accountRepository.accounts }.collectAsState(null).value
        val result = remember { mutableStateOf("") }
        NestingCard {
            NestingCardHeader("Contacts per account (RemoteAccountRepository.setContactsEnabled)")
            accounts?.filter { it.type == PimAccount.Type.CALDAV || it.type == PimAccount.Type.GOOGLE }?.forEach { a ->
                CheckBoxWithLabel(a.contactsEnabled, { enabled ->
                    result.value = "Changing ${a.name}..."
                    scope.launch {
                        result.value = accountRepository.setContactsEnabled(a, enabled).fold(
                            { "${it.name}: contacts ${if (it.contactsEnabled) "on" else "off"}" },
                            { "Failed: ${it.describe()}" })
                    }
                }, "${a.name} (${a.type})")
            } ?: Text("Loading accounts...")
            if (result.value.isNotEmpty()) Text(result.value)
        }
    }

    @Composable
    private fun AddressBooksPane(selected: AddressBook?, onSelect: (AddressBook) -> Unit) {
        val books = remember { contactsRepository.addressBooks() }.collectAsState(null).value
        NestingCard {
            NestingCardHeader("addressBooks() (live; click to filter)")
            when {
                books == null -> Text("Loading...")
                books.isEmpty() -> Text("No address books (turn contacts on for an account)")
                else -> books.forEach { b ->
                    Text(
                        "${b.accountName} / ${b.name}  ${b.contactCount} contacts${if (b.readOnly) " RO" else ""}  id=${b.id}",
                        fontFamily = FontFamily.Monospace, fontSize = 12.sp,
                        fontWeight = if (b.id == selected?.id) FontWeight.Bold else FontWeight.Normal,
                        modifier = Modifier.fillMaxWidth().clickable { onSelect(b) }
                    )
                }
            }
        }
    }

    @Composable
    private fun ContactsPane(book: AddressBook?, selected: ContactSummary?, onSelect: (ContactSummary) -> Unit) {
        val limit = remember { mutableStateOf(100) }
        val list = remember(book?.id, limit.value) { contactsRepository.contacts(limit.value, book?.id) }.collectAsState(null).value
        NestingCard {
            NestingCardHeader("contacts(limit, addressBookId) (live; click for details)")
            NumericTextViewWithSpinnerButtons("limit", limit.value, stepOnButton = 50) {
                limit.value = it.coerceIn(1, ContactsRepository.MAX_CONTACTS)
            }
            ContactListView(list, selected, onSelect)
        }
    }

    @Composable
    private fun ContactListView(list: ContactList?, selected: ContactSummary?, onSelect: (ContactSummary) -> Unit) {
        if (list == null) {
            Text("Loading...")
            return
        }
        Text("${list.contacts.size} of ${list.totalCount}")
        var lastInitial: Char? = null
        list.contacts.forEach { c ->
            if (c.initial != lastInitial) {
                Text(c.initial.toString(), fontWeight = FontWeight.Bold, color = Color.Gray)
                lastInitial = c.initial
            }
            Text(
                "${if (c.hasPhoto) "▣" else " "} ${c.displayName.padEnd(28)} ${c.primaryPhone.padEnd(18)} ${c.primaryEmail}",
                fontFamily = FontFamily.Monospace, fontSize = 12.sp,
                fontWeight = if (c.id == selected?.id) FontWeight.Bold else FontWeight.Normal,
                modifier = Modifier.fillMaxWidth().clickable { onSelect(c) }
                    .background(if (c.id == selected?.id) Color(0x2200AAFF) else Color.Transparent)
            )
        }
    }

    @Composable
    private fun ContactPane(summary: ContactSummary?) {
        val contact = remember(summary?.id) { mutableStateOf<Result<Contact>?>(null) }
        val photo = remember(summary?.id) { mutableStateOf<ImageBitmap?>(null) }
        LaunchedEffect(summary?.id) {
            if (summary == null) return@LaunchedEffect
            contact.value = contactsRepository.getContact(summary.id)
            if (summary.hasPhoto) {
                contactsRepository.getPhoto(summary.id, 96).onSuccess {
                    photo.value = org.jetbrains.skia.Image.makeFromEncoded(it.jpeg).toComposeImageBitmap()
                }
            }
        }
        NestingCard {
            NestingCardHeader("getContact() / getPhoto()")
            if (summary == null) {
                Text("Select a contact")
                return@NestingCard
            }
            contact.value?.fold(
                onSuccess = { c ->
                    Row {
                        photo.value?.let { Image(it, "photo", Modifier.size(96.dp)); Spacer(Modifier.width(8.dp)) }
                        SelectionContainer {
                            Column {
                                Text(c.summary.displayName, fontWeight = FontWeight.Bold)
                                Text("sortKey ${c.summary.sortKey}  id ${c.summary.id}  book ${c.summary.addressBookId}", fontSize = 11.sp, color = Color.Gray)
                                if (c.nickname.isNotEmpty()) Text("Nickname: ${c.nickname}")
                                if (c.summary.organization.isNotEmpty() || c.title.isNotEmpty()) Text("${c.title} ${c.summary.organization}".trim())
                                c.phones.forEach { Text("☎ ${it.type} ${it.number}  (E.164 ${it.e164.ifEmpty { "?" }})${if (it.preferred) " pref" else ""}") }
                                c.emails.forEach { Text("✉ ${it.type} ${it.address}${if (it.preferred) " pref" else ""}") }
                                c.addresses.forEach { Text("⌂ ${it.type} ${it.formatted}") }
                                c.birthday?.let { Text("Birthday: $it") }
                                c.urls.forEach { Text("URL: $it") }
                                if (c.note.isNotEmpty()) Text("Note: ${c.note}")
                            }
                        }
                    }
                },
                onFailure = { Text("Failed: ${it.describe()}", color = Color.Red) }
            ) ?: Text("Loading...")
        }
    }

    @Composable
    private fun EditPane(book: AddressBook?, selected: ContactSummary?, onSaved: (ContactSummary?) -> Unit) {
        val scope = rememberCoroutineScope()
        // What the form was loaded from (null: a new contact).
        val loaded = remember { mutableStateOf<Contact?>(null) }
        val given = remember { mutableStateOf("") }
        val family = remember { mutableStateOf("") }
        val organization = remember { mutableStateOf("") }
        val nickname = remember { mutableStateOf("") }
        val phone = remember { mutableStateOf("") }
        val phoneType = remember { mutableStateOf(PhoneNumber.Type.MOBILE) }
        val email = remember { mutableStateOf("") }
        val note = remember { mutableStateOf("") }
        val result = remember { mutableStateOf("") }

        fun input() = ContactInput(
            givenName = given.value, familyName = family.value, organization = organization.value, nickname = nickname.value,
            phones = phone.value.split(',').map { it.trim() }.filter { it.isNotEmpty() }
                .mapIndexed { i, n -> PhoneNumber(n, "", phoneType.value, "", preferred = i == 0) },
            emails = email.value.split(',').map { it.trim() }.filter { it.isNotEmpty() }
                .mapIndexed { i, a -> EmailAddress(a, EmailAddress.Type.HOME, preferred = i == 0) },
            note = note.value,
        )

        fun fill(c: Contact?) {
            loaded.value = c
            given.value = c?.summary?.givenName ?: ""
            family.value = c?.summary?.familyName ?: ""
            organization.value = c?.summary?.organization ?: ""
            nickname.value = c?.nickname ?: ""
            phone.value = c?.phones?.joinToString(", ") { it.number } ?: ""
            phoneType.value = c?.phones?.firstOrNull()?.type ?: PhoneNumber.Type.MOBILE
            email.value = c?.emails?.joinToString(", ") { it.address } ?: ""
            note.value = c?.note ?: ""
        }

        // The fields that differ from the loaded contact: only these are sent.
        fun changed(c: Contact): Set<ContactField> {
            val before = ContactInput.from(c)
            val now = input()
            return buildSet {
                if (now.givenName != before.givenName) add(ContactField.GIVEN_NAME)
                if (now.familyName != before.familyName) add(ContactField.FAMILY_NAME)
                if (now.organization != before.organization) add(ContactField.ORGANIZATION)
                if (now.nickname != before.nickname) add(ContactField.NICKNAME)
                if (now.phones.map { it.number to it.type } != before.phones.map { it.number to it.type }) add(ContactField.PHONES)
                if (now.emails.map { it.address } != before.emails.map { it.address }) add(ContactField.EMAILS)
                if (now.note != before.note) add(ContactField.NOTE)
            }
        }

        NestingCard {
            NestingCardHeader("createContact() / updateContact() / deleteContact()")
            Text(loaded.value?.let { "Editing ${it.summary.displayName} (revision ${it.summary.revision})" } ?: "New contact in ${book?.name ?: "the first writable address book"}")
            Row {
                OutlinedTextField(given.value, { given.value = it }, label = { Text("Given name") }, singleLine = true, modifier = Modifier.weight(1f))
                OutlinedTextField(family.value, { family.value = it }, label = { Text("Family name") }, singleLine = true, modifier = Modifier.weight(1f))
            }
            Row {
                OutlinedTextField(organization.value, { organization.value = it }, label = { Text("Organization") }, singleLine = true, modifier = Modifier.weight(1f))
                OutlinedTextField(nickname.value, { nickname.value = it }, label = { Text("Nickname") }, singleLine = true, modifier = Modifier.weight(1f))
            }
            OutlinedTextField(phone.value, { phone.value = it }, label = { Text("Phone numbers (comma separated)") }, singleLine = true, modifier = Modifier.fillMaxWidth())
            Row(verticalAlignment = Alignment.CenterVertically) {
                listOf(PhoneNumber.Type.MOBILE, PhoneNumber.Type.HOME, PhoneNumber.Type.WORK, PhoneNumber.Type.OTHER).forEach { t ->
                    CheckBoxWithLabel(phoneType.value == t, { phoneType.value = t }, t.name)
                }
            }
            OutlinedTextField(email.value, { email.value = it }, label = { Text("E-mail addresses (comma separated)") }, singleLine = true, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(note.value, { note.value = it }, label = { Text("Note") }, modifier = Modifier.fillMaxWidth())
            Row {
                Button(onClick = {
                    scope.launch {
                        val id = selected?.id ?: return@launch
                        contactsRepository.getContact(id).onSuccess { fill(it) }
                    }
                }, enabled = selected != null) { Text("Load selected") }
                Spacer(Modifier.width(8.dp))
                Button(onClick = { fill(null) }) { Text("New") }
                Spacer(Modifier.width(8.dp))
                Button(onClick = {
                    scope.launch {
                        result.value = contactsRepository.createContact(input(), book?.id).fold(
                            { onSaved(it.summary); fill(it); "Created ${it.summary.displayName} (${it.summary.id})" },
                            { "Create failed: ${it.describe()}" })
                    }
                }) { Text("createContact()") }
                Spacer(Modifier.width(8.dp))
                Button(onClick = {
                    val c = loaded.value ?: return@Button
                    val fields = changed(c)
                    if (fields.isEmpty()) {
                        result.value = "Nothing changed"
                        return@Button
                    }
                    scope.launch {
                        result.value = contactsRepository.updateContact(c.summary, input(), fields).fold(
                            { onSaved(it.summary); fill(it); "Changed $fields; now revision ${it.summary.revision}" },
                            { "Update failed: ${it.describe()}" })
                    }
                }, enabled = loaded.value != null) { Text("updateContact() (changed fields)") }
                Spacer(Modifier.width(8.dp))
                Button(onClick = {
                    val c = loaded.value ?: return@Button
                    scope.launch {
                        result.value = contactsRepository.deleteContact(c.summary).fold(
                            { onSaved(null); fill(null); "Deleted ${c.summary.displayName}" },
                            { "Delete failed: ${it.describe()}" })
                    }
                }, enabled = loaded.value != null) { Text("deleteContact()") }
            }
            if (result.value.isNotEmpty()) Text(result.value)
        }
    }

    @Composable
    private fun SearchPane(book: AddressBook?, onSelect: (ContactSummary) -> Unit) {
        val scope = rememberCoroutineScope()
        val text = remember { mutableStateOf("") }
        val inBook = remember { mutableStateOf(false) }
        val result = remember { mutableStateOf<Result<ContactList>?>(null) }
        NestingCard {
            NestingCardHeader("search(text) (names, e-mail; digits for numbers)")
            Row(verticalAlignment = Alignment.CenterVertically) {
                OutlinedTextField(text.value, { text.value = it }, label = { Text("Text") }, singleLine = true, modifier = Modifier.width(260.dp))
                CheckBoxWithLabel(inBook.value, { inBook.value = it }, "Only in ${book?.name ?: "(no book selected)"}")
                Button(onClick = { scope.launch { result.value = contactsRepository.search(text.value, book?.id.takeIf { inBook.value }) } }) { Text("search()") }
            }
            result.value?.fold(
                onSuccess = { ContactListView(it, null, onSelect) },
                onFailure = { Text("Failed: ${it.describe()}", color = Color.Red) }
            )
        }
    }

    @Composable
    private fun PhonePane(onSelect: (ContactSummary) -> Unit) {
        val scope = rememberCoroutineScope()
        val number = remember { mutableStateOf("+1 306 555 0123") }
        val matches = remember { mutableStateOf<Result<List<PhoneMatch>>?>(null) }
        val callerId = remember { mutableStateOf<String?>(null) }
        NestingCard {
            NestingCardHeader("findByPhoneNumber() and CallerIdLookup.lookup()")
            Row(verticalAlignment = Alignment.CenterVertically) {
                OutlinedTextField(number.value, { number.value = it }, label = { Text("Number as the phone reports it") }, singleLine = true, modifier = Modifier.width(260.dp))
                Button(onClick = { scope.launch { matches.value = contactsRepository.findByPhoneNumber(number.value) } }) { Text("findByPhoneNumber()") }
                Spacer(Modifier.width(8.dp))
                Button(onClick = {
                    scope.launch {
                        val id: CallerId? = callerIdLookup.lookup(number.value)
                        callerId.value = id?.let { "${it.name} (${it.phoneLabel})${if (!it.exact) ", maybe: no area code in the contact" else ""}" }
                            ?: "Unknown caller (or no answer within ${CallerIdLookup.TIMEOUT})"
                    }
                }) { Text("lookup()") }
                Spacer(Modifier.width(8.dp))
                Button(onClick = { callerIdLookup.clearCache() }) { Text("Clear cache") }
                Spacer(Modifier.width(8.dp))
                Button(onClick = {
                    scope.launch {
                        callerId.value = contactsRepository.createContact(ContactInput.forNumber(number.value, givenName = "Saved caller")).fold(
                            { callerIdLookup.clearCache(); onSelect(it.summary); "Saved as ${it.summary.displayName}" },
                            { "Save failed: ${it.describe()}" })
                    }
                }) { Text("Save this caller") }
            }
            callerId.value?.let { Text("CallerIdLookup: $it", fontWeight = FontWeight.Bold) }
            matches.value?.fold(
                onSuccess = { list ->
                    if (list.isEmpty()) Text("Nobody has that number")
                    list.forEach { m ->
                        Text(
                            "${if (m.exact) "exact" else "weak "}  ${m.contact.displayName}  ${m.phone.type} ${m.phone.number}",
                            fontFamily = FontFamily.Monospace, fontSize = 12.sp,
                            modifier = Modifier.clickable { onSelect(m.contact) }
                        )
                    }
                },
                onFailure = { Text("Failed: ${it.describe()}", color = Color.Red) }
            )
        }
    }

    private fun Throwable.describe(): String = when (this) {
        is PimBackendException -> "${this::class.simpleName}: $message"
        else -> "${this::class.simpleName}: $message"
    }
}
