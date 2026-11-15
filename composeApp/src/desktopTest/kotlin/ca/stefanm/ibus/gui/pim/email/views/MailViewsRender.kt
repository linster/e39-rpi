package ca.stefanm.ibus.gui.pim.email.views

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.ImageComposeScene
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Density
import androidx.compose.ui.use
import ca.stefanm.ibus.gui.menu.widgets.BmwSingleLineHeader
import ca.stefanm.ibus.gui.menu.widgets.knobListener.KnobListenerService
import ca.stefanm.ibus.gui.menu.widgets.knobListener.KnobListenerServiceIdentifier
import ca.stefanm.ibus.gui.menu.widgets.knobListener.dynamic.KnobObserverBuilderState
import ca.stefanm.ibus.gui.menu.widgets.themes.ThemeWrapper
import ca.stefanm.ibus.gui.menu.widgets.themes.Themes
import ca.stefanm.ibus.gui.pim.email.repo.FakeMailRepository
import ca.stefanm.ibus.gui.pim.email.repo.api.MailboxListRepo
import ca.stefanm.ibus.gui.pim.email.repo.api.MessageListRepo
import ca.stefanm.ibus.gui.pim.email.views.parts.FolderRowView
import ca.stefanm.ibus.gui.pim.email.views.parts.MailToolbar
import ca.stefanm.ibus.gui.pim.email.views.parts.MailToolbarButton
import ca.stefanm.ibus.gui.pim.email.views.parts.MessageBodyView
import ca.stefanm.ibus.gui.pim.email.views.parts.ComposeFieldRow
import ca.stefanm.ibus.gui.pim.email.views.parts.PersonRow
import ca.stefanm.ibus.gui.pim.email.repo.api.MailComposeRepo
import ca.stefanm.ibus.gui.pim.email.repo.api.FakeMailSenders
import ca.stefanm.ibus.gui.pim.email.repo.api.RecentCorrespondents
import ca.stefanm.ibus.gui.pim.email.repo.api.MailFormat
import ca.stefanm.ibus.gui.menu.MenuWindow
import ca.stefanm.ibus.gui.menu.widgets.modalMenu.SidePanelMenu
import ca.stefanm.ibus.gui.menu.widgets.screenMenu.TextMenuItem
import ca.stefanm.ibus.gui.menu.widgets.screenMenu.SmoothScroll
import ca.stefanm.ibus.gui.menu.widgets.knobListener.dynamic.toDynamicLambda
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.ui.text.font.FontWeight
import ca.stefanm.ibus.gui.pim.email.views.parts.MessageRowView
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.unit.dp
import ca.stefanm.ibus.lib.logging.StdOutLogger
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.jetbrains.skia.EncodedImageFormat
import org.junit.Assume.assumeTrue
import org.junit.Test
import java.io.File
import javax.inject.Provider

/**
 * Renders the mail list views (folder rows; message list with its toolbar)
 * from the fake data to PNG files, to look at them without a running HMI:
 *
 *   E39_RENDER_DIR=/tmp/mail ./gradlew :composeApp:desktopTest --tests '*MailViewsRender*'
 *
 * Skipped without E39_RENDER_DIR (the theme reads the HMI's settings).
 */
internal class MailViewsRender {

    @Test
    fun render() = runBlocking {
        val dir = System.getenv("E39_RENDER_DIR")
        assumeTrue("E39_RENDER_DIR not set", dir != null)
        val out = File(dir!!).apply { mkdirs() }

        val mail = FakeMailRepository()
        val mailbox = MailboxListRepo(mail)
        val accounts = mailbox.accounts().first()
        val inbox = accounts.first().inbox!!
        val inboxRows = MessageListRepo(mail, mailbox).pager(inbox.id).state.first()
        val listRows = MessageListRepo(mail, mailbox).pager(accounts.first().folders.first { it.name == "E39 list" }.id).state.first()

        val reader = ca.stefanm.ibus.gui.pim.email.repo.api.MessageReaderRepo(mail, mailbox)
        val sgi = reader.open(inboxRows.rows.first { it.hasAttachments }.id).getOrThrow()
        val trip = reader.open(inboxRows.rows.first().id).getOrThrow()

        val logger = StdOutLogger()
        lateinit var service: KnobListenerService
        val identifier = KnobListenerServiceIdentifier(Provider { service }, Provider { service }, Provider { service })
        service = KnobListenerService(MutableSharedFlow(), identifier, logger)

        for ((theme, w, h, suffix) in listOf(
            Quad(Themes.BmwBlueNormalSize, 400, 234, "normal"),
            Quad(Themes.BmwBlueDoubledPixels, 800, 468, "doubled"),
        )) {
            fun shot(name: String, content: @androidx.compose.runtime.Composable () -> Unit) {
                ImageComposeScene(w, h, Density(1F)) {
                    CompositionLocalProvider(ThemeWrapper.ThemeHandle provides theme, MenuWindow.MenuWindowKnobListener provides service) {
                        Column(Modifier.fillMaxSize().background(theme.colors.menuBackground)) { content() }
                    }
                }.use { scene ->
                    scene.render(0)
                    val image = scene.render(1_000_000_000)
                    File(out, "$suffix-$name.png").writeBytes(image.encodeToData(EncodedImageFormat.PNG)!!.bytes)
                }
            }

            shot("folders") {
                BmwSingleLineHeader("Mail · 7 unread")
                accounts.forEach { account ->
                    account.folders.forEachIndexed { i, folder ->
                        FolderRowView(folder, if (i == 0) account.accountName else null, account.unreadCount, isSelected = i == 1 && account == accounts.first(), onClicked = {})
                    }
                }
            }
            for ((name, content) in listOf("reader-sgi" to sgi, "reader-trip" to trip)) {
                shot(name) {
                    val knobState = KnobObserverBuilderState(service, logger)
                    BmwSingleLineHeader(content.subject)
                    MailToolbar(knobState, highlightNone = true, buttons = listOf(
                        MailToolbarButton("Back") {},
                        MailToolbarButton("↕ Press: menu", 1.8f) {},
                        MailToolbarButton("▲ Newer", 1.3f) {},
                        MailToolbarButton("▼ Older", 1.3f) {},
                        MailToolbarButton("↩ Reply", 1.3f) {},
                        MailToolbarButton("☰", 0.7f) {},
                    ))
                    Column(Modifier.padding(horizontal = 8.dp)) { MessageBodyView(content) }
                }
            }
            // The reader's actions, in a side pane over the right half.
            shot("reader-actions") {
                Row(Modifier.fillMaxSize()) {
                    Box(Modifier.weight(1f))
                    Box(Modifier.weight(1f).fillMaxHeight()) {
                        // As showMailSidePane lays it out.
                        SidePanelMenu.SidePanelMenu(title = sgi.subject) {
                            Column(Modifier.padding(10.dp)) { SidePanelMenu.InfoLabel(sgi.summary.from.display, FontWeight.Bold) }
                            SmoothScroll.SmoothScroll(
                                modifier = Modifier.fillMaxWidth(),
                                knobListenerService = service,
                                logger = logger,
                                items = listOf("Go Back", "Reply All", "Forward", "Mark Unread", "Remove Flag", "Move to Folder...", "Delete")
                                    .map { TextMenuItem(it, onClicked = {}).toDynamicLambda() },
                            )
                        }
                    }
                }
            }
            val compose = MailComposeRepo(mail, mailbox, FakeMailSenders(mail), MutableStateFlow(listOf("On my way.")), RecentCorrespondents(mail, FakeMailSenders(mail)))
            compose.startReply(trip.message, all = false)
            compose.setText("Sounds good, I'll bring the cooler and the maps. See you at 8!")
            val draft = compose.draft.value!!
            shot("compose") {
                BmwSingleLineHeader("Reply")
                listOf(
                    Triple("Cancel", "", false), Triple("From", "Home <${FakeMailRepository.ME_EMAIL}>", false),
                    Triple("To", MailFormat.addresses(draft.to), false), Triple("Cc", "Add...", true),
                    Triple("Subject", draft.subject, false), Triple("Message", draft.text, false),
                    Triple("Quick Reply...", "", false), Triple("Original Message", "Quoted below the text", false), Triple("Send", "", false),
                ).forEachIndexed { i, (label, value, dim) ->
                    ComposeFieldRow(label, value, isSelected = i == 5, onClicked = {}, valueLines = if (label == "Message") 3 else 1, dim = dim)
                }
            }
            val people = RecentCorrespondents(mail, FakeMailSenders(mail)).suggest()
            shot("picker") {
                BmwSingleLineHeader("To: Choose Recipient")
                ComposeFieldRow("Cancel", "", isSelected = false, onClicked = {})
                ComposeFieldRow("Search", "Type a name or address...", isSelected = false, onClicked = {}, dim = true)
                ComposeFieldRow("Type an Address", "e.g. anna@example.com", isSelected = false, onClicked = {}, dim = true)
                people.forEachIndexed { i, p -> PersonRow(p.name.ifBlank { p.email }, if (p.name.isBlank()) "" else p.email, isSelected = i == 0, onClicked = {}) }
            }
            // Settings screens: their MenuItems as OneColumnSmoothScreen shows them.
            fun menu(name: String, header: String, items: List<ca.stefanm.ibus.gui.menu.widgets.screenMenu.MenuItem>) = shot(name) {
                BmwSingleLineHeader(header)
                (listOf(TextMenuItem("Go Back", onClicked = {})) + items).forEachIndexed { i, item ->
                    item.copyAndSetIsSelected(i == 1).toView(chipOrientation = ca.stefanm.ibus.gui.menu.widgets.ItemChipOrientation.W)()
                }
            }
            menu("settings", "Mail Settings", listOf(
                ca.stefanm.ibus.gui.menu.widgets.screenMenu.CheckBoxMenuItem("Notify About New Mail", true, onCheckChanged = {}),
                ca.stefanm.ibus.gui.menu.widgets.screenMenu.CheckBoxMenuItem("Notify When Mail Is Sent or Fails", true, onCheckChanged = {}),
                TextMenuItem("Muted Accounts... (1)", onClicked = {}),
                TextMenuItem("Quick Replies... (6)", onClicked = {}),
                ca.stefanm.ibus.gui.menu.widgets.screenMenu.CheckBoxMenuItem("Sample Mail Instead of the Bridge (After Restart)", false, onCheckChanged = {}),
            ))
            menu("mute", "Notify About New Mail In", accounts.map {
                ca.stefanm.ibus.gui.menu.widgets.screenMenu.CheckBoxMenuItem(it.accountName, it.accountName == "Home", onCheckChanged = {})
            })
            menu("quick-replies", "Quick Replies", listOf(TextMenuItem("Add Quick Reply...", onClicked = {})) +
                listOf("I'm driving, I'll reply later.", "On my way.", "Running late, sorry.", "Yes.", "No.", "Thanks!").map { TextMenuItem(it, onClicked = {}) })
            for ((name, state) in listOf("inbox" to inboxRows, "list" to listRows)) {
                shot(name) {
                    val knobState = KnobObserverBuilderState(service, logger)
                    BmwSingleLineHeader("${state.folder!!.accountName}: ${state.folder!!.name} · ${state.rangeLabel}")
                    MailToolbar(knobState, listOf(
                        MailToolbarButton("Back", 1.5f) {},
                        MailToolbarButton(if (state.page.hasPrevious) "◀ Newer" else "◀", 1.5f) {},
                        MailToolbarButton(if (state.page.hasNext) "Older ▶" else "▶", 1.5f) {},
                        MailToolbarButton("☰") {},
                    ))
                    state.rows.take(8).forEachIndexed { i, row -> MessageRowView(row, isSelected = i == 1, onClicked = {}) }
                }
            }
        }
    }

    private data class Quad<A, B, C, D>(val a: A, val b: B, val c: C, val d: D)
}
