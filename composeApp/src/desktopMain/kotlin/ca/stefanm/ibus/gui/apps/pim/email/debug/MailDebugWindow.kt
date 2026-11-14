package ca.stefanm.ibus.gui.pim.email.debug

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
import ca.stefanm.ibus.gui.pim.calendar.repo.backend.PimBackendException
import ca.stefanm.ibus.gui.pim.calendar.repo.backend.PimBackendSettingsRepository
import ca.stefanm.ibus.gui.pim.email.repo.FakeMailRepository
import ca.stefanm.ibus.gui.pim.email.repo.MailAddress
import ca.stefanm.ibus.gui.pim.email.repo.MailChangeStream
import ca.stefanm.ibus.gui.pim.email.repo.MailFolder
import ca.stefanm.ibus.gui.pim.email.repo.MailMessageHtml
import ca.stefanm.ibus.gui.pim.email.repo.MailRepository
import ca.stefanm.ibus.gui.pim.email.repo.MailSettingsRepository
import ca.stefanm.ibus.gui.pim.email.repo.MailSummary
import ca.stefanm.ibus.gui.pim.email.repo.MessageList
import ca.stefanm.ibus.gui.pim.email.repo.OutgoingMail
import kotlinx.coroutines.launch
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import javax.inject.Inject
import javax.inject.Provider
import kotlin.time.Clock

/**
 * Everything the mail repositories can do: folders and messages (live),
 * opening a message (text and HTML), flags, moving, trash, sending (new,
 * reply, forward, quick replies), search, events and raw notices, and the
 * mail settings.
 */
class MailDebugWindow @Inject constructor(
    private val mailRepository: MailRepository,
    private val settingsRepository: MailSettingsRepository,
    private val backendSettingsRepository: PimBackendSettingsRepository,
    // Only collected when watching raw notices (it opens a stream).
    private val changeStream: Provider<MailChangeStream>,
) : WindowManager.E39Window {

    companion object {
        const val MAX_LOG_LINES = 100
    }

    override val tag: Any
        get() = this

    override val title: String = "Mail (PIM Bridge) Debug"

    override val size: DpSize = DpSize(1500.dp, 1000.dp)

    override val defaultPosition: WindowManager.E39Window.DefaultPosition
        get() = WindowManager.E39Window.DefaultPosition.ANYWHERE

    override fun content(): @Composable WindowScope.() -> Unit = {
        val selectedFolder = remember { mutableStateOf<MailFolder?>(null) }
        val selectedMessage = remember { mutableStateOf<MailSummary?>(null) }
        val folders = remember { mailRepository.folders() }.collectAsState(null).value

        Row(Modifier.fillMaxSize()) {
            Column(Modifier.weight(0.4f).verticalScroll(rememberScrollState())) {
                RepositoryPane()
                FoldersPane(folders, selectedFolder.value) { selectedFolder.value = it }
                SettingsPane(selectedFolder.value)
                EventsPane()
            }
            Column(Modifier.weight(0.6f).verticalScroll(rememberScrollState())) {
                MessagesPane(selectedFolder.value, selectedMessage.value) { selectedMessage.value = it }
                SelectedMessagePane(selectedMessage.value, folders.orEmpty())
                SendPane(selectedMessage.value, folders.orEmpty())
                SearchPane(selectedFolder.value) { selectedMessage.value = it }
            }
        }
    }

    // --- Which repository ------------------------------------------------

    @Composable
    private fun RepositoryPane() {
        val unread = remember { mailRepository.unreadInboxCount() }.collectAsState(null).value
        val fakeSetting = remember { mutableStateOf(settingsRepository.useFakeData) }

        NestingCard {
            NestingCardHeader("MailRepository")
            Text("In use: ${mailRepository::class.simpleName}")
            if (mailRepository !is FakeMailRepository) {
                Text("PIM bridge: ${backendSettingsRepository.settings.collectAsState().value.target} (change it in the Calendar debug window)")
            }
            Text("unreadInboxCount(): ${unread ?: "..."}")
            CheckBoxWithLabel(fakeSetting.value, {
                settingsRepository.setUseFakeData(it)
                fakeSetting.value = it
            }, "useFakeData (after a restart)")
            if (mailRepository is FakeMailRepository) {
                Button(onClick = { mailRepository.simulateNewMail() }) { Text("simulateNewMail()") }
            }
        }
    }

    // --- Folders ---------------------------------------------------------

    @Composable
    private fun FoldersPane(folders: List<MailFolder>?, selected: MailFolder?, onSelect: (MailFolder) -> Unit) {
        NestingCard {
            NestingCardHeader("folders() (live; click to select)")
            when {
                folders == null -> Text("Loading (retrying while the bridge is unreachable)...")
                folders.isEmpty() -> Text("No mail folders (no mail account)")
                else -> folders.forEach { f ->
                    Text(
                        "${"    ".repeat(f.depth)}${f.accountName} / ${f.path.joinToString("/")}  [${f.role}]  " +
                                "${f.unreadCount}/${f.totalCount}${if (f.readOnly) " RO" else ""}${if (!f.holdsMessages) " (no messages)" else ""}  id=${f.id}",
                        fontFamily = FontFamily.Monospace,
                        fontSize = 12.sp,
                        fontWeight = if (f.id == selected?.id) FontWeight.Bold else FontWeight.Normal,
                        modifier = Modifier.fillMaxWidth().clickable { onSelect(f) }
                            .background(if (f.id == selected?.id) Color(0x2200AAFF) else Color.Transparent)
                    )
                }
            }
        }
    }

    // --- Messages --------------------------------------------------------

    @Composable
    private fun MessagesPane(folder: MailFolder?, selected: MailSummary?, onSelect: (MailSummary) -> Unit) {
        val limit = remember { mutableStateOf(25) }
        val unreadOnly = remember { mutableStateOf(false) }

        NestingCard {
            NestingCardHeader("messages(folderId, limit, unreadOnly) (live; click to select)")
            if (folder == null) {
                Text("Select a folder")
                return@NestingCard
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                NumericTextViewWithSpinnerButtons("limit", limit.value, stepOnButton = 25) {
                    limit.value = it.coerceIn(1, MailRepository.MAX_MESSAGES)
                }
                CheckBoxWithLabel(unreadOnly.value, { unreadOnly.value = it }, "unreadOnly")
            }
            val list = remember(folder.id, limit.value, unreadOnly.value) {
                mailRepository.messages(folder.id, limit.value, unreadOnly.value)
            }.collectAsState(null).value
            MessageListView(list, selected, onSelect)
            if (list?.hasMore == true) {
                Button(onClick = { limit.value = (limit.value + 25).coerceAtMost(MailRepository.MAX_MESSAGES) }) {
                    Text("More (limit + 25)")
                }
            }
        }
    }

    @Composable
    private fun MessageListView(list: MessageList?, selected: MailSummary?, onSelect: (MailSummary) -> Unit) {
        if (list == null) {
            Text("Loading...")
            return
        }
        Text("${list.messages.size} of ${list.totalCount}")
        list.messages.forEach { m ->
            val marks = buildString {
                append(if (m.seen) ' ' else '*')
                append(if (m.flagged) '!' else ' ')
                append(if (m.answered) 'R' else ' ')
                append(if (m.forwarded) 'F' else ' ')
                append(if (m.hasAttachments) '@' else ' ')
            }
            Text(
                "$marks ${m.date.local()}  ${m.from.display.take(24).padEnd(24)}  ${m.subject}",
                fontFamily = FontFamily.Monospace,
                fontSize = 12.sp,
                fontWeight = if (m.id == selected?.id) FontWeight.Bold else FontWeight.Normal,
                modifier = Modifier.fillMaxWidth().clickable { onSelect(m) }
                    .background(if (m.id == selected?.id) Color(0x2200AAFF) else Color.Transparent)
            )
        }
        Text("* unread  ! flagged  R answered  F forwarded  @ attachments", fontSize = 11.sp, color = Color.Gray)
    }

    // --- One message -----------------------------------------------------

    @Composable
    private fun SelectedMessagePane(message: MailSummary?, folders: List<MailFolder>) {
        val scope = rememberCoroutineScope()
        val markSeen = remember { mutableStateOf(true) }
        val opened = remember(message?.id) { mutableStateOf<Result<MailMessageHtml>?>(null) }
        val withHtml = remember(message?.id) { mutableStateOf(false) }
        val lastResult = remember(message?.id) { mutableStateOf("") }

        fun report(what: String, result: Result<*>) {
            lastResult.value = result.fold({ "$what: OK" }, { "$what failed: ${it.describe()}" })
        }

        NestingCard {
            NestingCardHeader("Selected message")
            if (message == null) {
                Text("Select a message (list or search)")
                return@NestingCard
            }
            Text("id=${message.id} folder=${message.folderId}  ${message.subject}")
            Row(verticalAlignment = Alignment.CenterVertically) {
                CheckBoxWithLabel(markSeen.value, { markSeen.value = it }, "markSeen")
                Button(onClick = {
                    scope.launch {
                        withHtml.value = false
                        opened.value = mailRepository.getMessage(message.id, markSeen.value).map { MailMessageHtml(it, "") }
                    }
                }) { Text("getMessage()") }
                Spacer(Modifier.width(8.dp))
                Button(onClick = {
                    scope.launch {
                        withHtml.value = true
                        opened.value = mailRepository.getMessageHtml(message.id, markSeen.value)
                    }
                }) { Text("getMessageHtml()") }
            }
            Row {
                Button(onClick = { scope.launch { report("setSeen", mailRepository.setSeen(listOf(message.id), !message.seen)) } }) {
                    Text(if (message.seen) "setSeen(false)" else "setSeen(true)")
                }
                Spacer(Modifier.width(8.dp))
                Button(onClick = { scope.launch { report("setFlagged", mailRepository.setFlagged(listOf(message.id), !message.flagged)) } }) {
                    Text(if (message.flagged) "setFlagged(false)" else "setFlagged(true)")
                }
                Spacer(Modifier.width(8.dp))
                Button(onClick = { scope.launch { report("trash", mailRepository.trash(listOf(message.id))) } }) { Text("trash()") }
            }
            val accountId = folders.firstOrNull { it.id == message.folderId }?.accountId
            val targets = folders.filter { it.accountId == accountId && it.id != message.folderId && it.holdsMessages }
            if (targets.isNotEmpty()) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("move() to: ")
                    targets.forEach { f ->
                        Button(onClick = { scope.launch { report("move to ${f.name}", mailRepository.move(listOf(message.id), f.id)) } }) {
                            Text(f.path.joinToString("/"))
                        }
                        Spacer(Modifier.width(4.dp))
                    }
                }
            }
            if (lastResult.value.isNotEmpty()) Text(lastResult.value)

            opened.value?.fold(
                onSuccess = { (m, html) ->
                    SelectionContainer {
                        Column {
                            Text("From: ${m.summary.from}")
                            Text("To: ${m.summary.to.joinToString()}")
                            if (m.cc.isNotEmpty()) Text("Cc: ${m.cc.joinToString()}")
                            if (m.replyTo.isNotEmpty()) Text("Reply-To: ${m.replyTo.joinToString()}")
                            Text("Date: ${m.summary.date.local()}   Message-ID: ${m.messageId}")
                            Text("Subject: ${m.summary.subject}")
                            m.attachments.forEach { Text("Attachment ${it.index}: ${it.filename} (${it.mimeType}, ${it.sizeBytes} B)") }
                            Text("--- text${if (m.textWasHtml) " (from HTML)" else ""} ---", color = Color.Gray)
                            Text(m.text, fontFamily = FontFamily.Monospace, fontSize = 12.sp)
                            if (withHtml.value) {
                                Text("--- html (${html.length} chars) ---", color = Color.Gray)
                                Text(html.ifEmpty { "(no HTML part)" }, fontFamily = FontFamily.Monospace, fontSize = 11.sp)
                            }
                        }
                    }
                },
                onFailure = { Text("Failed: ${it.describe()}", color = Color.Red) }
            )
        }
    }

    // --- Sending ---------------------------------------------------------

    private enum class SendKind { NEW, REPLY, FORWARD }

    @Composable
    private fun SendPane(selected: MailSummary?, folders: List<MailFolder>) {
        val scope = rememberCoroutineScope()
        val kind = remember { mutableStateOf(SendKind.NEW) }
        val to = remember { mutableStateOf("") }
        val cc = remember { mutableStateOf("") }
        val bcc = remember { mutableStateOf("") }
        val subject = remember { mutableStateOf("") }
        val text = remember { mutableStateOf("") }
        val accountId = remember { mutableStateOf("") }
        val result = remember { mutableStateOf("") }
        val quickReplies = settingsRepository.quickReplies.collectAsState().value

        NestingCard {
            NestingCardHeader("send()")
            Row(verticalAlignment = Alignment.CenterVertically) {
                SendKind.entries.forEach { k ->
                    CheckBoxWithLabel(kind.value == k, {
                        kind.value = k
                        if (selected != null && k != SendKind.NEW) {
                            val account = folders.firstOrNull { it.id == selected.folderId }?.accountId.orEmpty()
                            accountId.value = account
                            if (k == SendKind.REPLY) {
                                to.value = selected.from.toString()
                                subject.value = if (selected.subject.startsWith("Re:", true)) selected.subject else "Re: ${selected.subject}"
                            } else {
                                subject.value = "Fwd: ${selected.subject}"
                            }
                        }
                    }, k.name)
                }
            }
            if (kind.value != SendKind.NEW) {
                Text(if (selected == null) "Select a message to ${kind.value.name.lowercase()}" else "${kind.value.name.lowercase()} of: ${selected.subject}")
            }
            OutlinedTextField(to.value, { to.value = it }, label = { Text("To (comma separated, Name <a@b>)") }, modifier = Modifier.fillMaxWidth())
            Row {
                OutlinedTextField(cc.value, { cc.value = it }, label = { Text("Cc") }, modifier = Modifier.weight(1f))
                OutlinedTextField(bcc.value, { bcc.value = it }, label = { Text("Bcc") }, modifier = Modifier.weight(1f))
            }
            Row {
                OutlinedTextField(subject.value, { subject.value = it }, label = { Text("Subject") }, modifier = Modifier.weight(2f))
                OutlinedTextField(accountId.value, { accountId.value = it }, label = { Text("Account ID (empty: default)") }, modifier = Modifier.weight(1f))
            }
            OutlinedTextField(text.value, { text.value = it }, label = { Text("Text") }, modifier = Modifier.fillMaxWidth().height(120.dp))
            Text("Quick replies (MailSettingsRepository):")
            Row {
                quickReplies.forEach { reply ->
                    Button(onClick = { text.value = reply }) { Text(reply, fontSize = 11.sp) }
                    Spacer(Modifier.width(4.dp))
                }
            }
            Button(onClick = {
                val mail = OutgoingMail(
                    to = parseAddresses(to.value),
                    cc = parseAddresses(cc.value),
                    bcc = parseAddresses(bcc.value),
                    subject = subject.value,
                    text = text.value,
                    accountId = accountId.value.trim().ifEmpty { null },
                    inReplyTo = selected.takeIf { kind.value == SendKind.REPLY },
                    forwardOf = selected.takeIf { kind.value == SendKind.FORWARD },
                )
                result.value = "Sending..."
                scope.launch {
                    result.value = mailRepository.send(mail).fold(
                        { "Queued: outbox message $it (see events)" },
                        { "Failed: ${it.describe()}" }
                    )
                }
            }) { Text("send()") }
            if (result.value.isNotEmpty()) Text(result.value)
        }
    }

    // --- Search ----------------------------------------------------------

    @Composable
    private fun SearchPane(folder: MailFolder?, onSelect: (MailSummary) -> Unit) {
        val scope = rememberCoroutineScope()
        val query = remember { mutableStateOf("") }
        val inFolder = remember { mutableStateOf(false) }
        val result = remember { mutableStateOf<Result<MessageList>?>(null) }

        NestingCard {
            NestingCardHeader("search(text, folderId) (headers only)")
            Row(verticalAlignment = Alignment.CenterVertically) {
                OutlinedTextField(query.value, { query.value = it }, label = { Text("Text") }, singleLine = true, modifier = Modifier.width(300.dp))
                CheckBoxWithLabel(inFolder.value, { inFolder.value = it }, "Only in ${folder?.name ?: "(no folder selected)"}")
                Button(onClick = {
                    scope.launch {
                        result.value = mailRepository.search(query.value, folder?.id.takeIf { inFolder.value })
                    }
                }) { Text("search()") }
            }
            result.value?.fold(
                onSuccess = { MessageListView(it, null, onSelect) },
                onFailure = { Text("Failed: ${it.describe()}", color = Color.Red) }
            )
        }
    }

    // --- Settings --------------------------------------------------------

    @Composable
    private fun SettingsPane(folder: MailFolder?) {
        val notifications = settingsRepository.notifications.collectAsState().value
        val quickReplies = settingsRepository.quickReplies.collectAsState().value
        val newReply = remember { mutableStateOf("") }

        NestingCard {
            NestingCardHeader("MailSettingsRepository")
            CheckBoxWithLabel(notifications.newMail, { settingsRepository.setNewMailNotifications(it) }, "New-mail notifications")
            CheckBoxWithLabel(notifications.sent, { settingsRepository.setSentNotifications(it) }, "Sent / not sent notifications")
            Text("Muted accounts: ${notifications.mutedAccountIds.ifEmpty { setOf("(none)") }.joinToString()}")
            Text("Muted folders: ${notifications.mutedFolderIds.ifEmpty { setOf("(none)") }.joinToString()}")
            if (folder != null) {
                CheckBoxWithLabel(folder.accountId in notifications.mutedAccountIds,
                    { settingsRepository.setAccountMuted(folder.accountId, it) }, "Mute account ${folder.accountName}")
                CheckBoxWithLabel(folder.id in notifications.mutedFolderIds,
                    { settingsRepository.setFolderMuted(folder.id, it) }, "Mute folder ${folder.name}")
            }
            Text("Notifications need the MailNotifierService (platform service group \"Pim\").", fontSize = 11.sp, color = Color.Gray)

            Text("Quick replies:")
            quickReplies.forEachIndexed { i, reply ->
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(reply, modifier = Modifier.weight(1f))
                    Button(onClick = { settingsRepository.moveQuickReply(i, i - 1) }, enabled = i > 0) { Text("Up") }
                    Spacer(Modifier.width(4.dp))
                    Button(onClick = { settingsRepository.removeQuickReply(reply) }) { Text("Remove") }
                }
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                OutlinedTextField(newReply.value, { newReply.value = it }, label = { Text("New quick reply") }, singleLine = true, modifier = Modifier.weight(1f))
                Button(onClick = {
                    if (newReply.value.isNotBlank()) {
                        settingsRepository.addQuickReply(newReply.value)
                        newReply.value = ""
                    }
                }) { Text("Add") }
            }
        }
    }

    // --- Events and raw notices ------------------------------------------

    @Composable
    private fun EventsPane() {
        val watchingRaw = remember { mutableStateOf(false) }
        val log = remember { mutableStateListOf<String>() }

        fun append(line: String) {
            log.add(0, "${Clock.System.now().local()} $line")
            while (log.size > MAX_LOG_LINES) log.removeAt(log.lastIndex)
        }

        LaunchedEffect(Unit) {
            mailRepository.events.collect { append("event: $it") }
        }
        LaunchedEffect(watchingRaw.value) {
            if (!watchingRaw.value || mailRepository is FakeMailRepository) return@LaunchedEffect
            changeStream.get().notices.collect { notice ->
                append("notice: " + notice.toString().trim().replace(Regex("\\s+"), " "))
            }
        }

        NestingCard {
            NestingCardHeader("events (and raw MailChangeStream notices)")
            Row(verticalAlignment = Alignment.CenterVertically) {
                CheckBoxWithLabel(watchingRaw.value, { watchingRaw.value = it }, "Raw notices too (bridge only)")
                Spacer(Modifier.width(8.dp))
                Button(onClick = { log.clear() }) { Text("Clear") }
            }
            SelectionContainer {
                Column {
                    log.forEach { Text(it, fontFamily = FontFamily.Monospace, fontSize = 12.sp) }
                }
            }
        }
    }

    // --- Helpers ---------------------------------------------------------

    /** "Name <a@b>, c@d" -> addresses. */
    private fun parseAddresses(text: String): List<MailAddress> =
        text.split(',').map { it.trim() }.filter { it.isNotEmpty() }.map { part ->
            val match = Regex("^(.*)<([^>]+)>$").find(part)
            if (match != null) {
                MailAddress(match.groupValues[1].trim().trim('"'), match.groupValues[2].trim())
            } else {
                MailAddress("", part)
            }
        }

    private fun kotlin.time.Instant.local() =
        toLocalDateTime(TimeZone.currentSystemDefault()).let { "${it.date} ${it.time.toString().take(5)}" }

    private fun Throwable.describe(): String = when (this) {
        is PimBackendException -> "${this::class.simpleName}: $message"
        else -> "${this::class.simpleName}: $message"
    }
}
