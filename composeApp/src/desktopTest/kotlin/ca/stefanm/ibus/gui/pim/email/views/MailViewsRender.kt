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
import ca.stefanm.ibus.gui.pim.email.views.parts.MessageRowView
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.unit.dp
import ca.stefanm.ibus.lib.logging.StdOutLogger
import kotlinx.coroutines.flow.MutableSharedFlow
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
                    CompositionLocalProvider(ThemeWrapper.ThemeHandle provides theme) {
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
                        MailToolbarButton("↕ Press: menu", 2f) {},
                        MailToolbarButton("▲ Newer", 1.3f) {},
                        MailToolbarButton("▼ Older", 1.3f) {},
                        MailToolbarButton("☰") {},
                    ))
                    Column(Modifier.padding(horizontal = 8.dp)) { MessageBodyView(content) }
                }
            }
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
