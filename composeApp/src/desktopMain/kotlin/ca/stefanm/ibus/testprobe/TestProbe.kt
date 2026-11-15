package ca.stefanm.ibus.testprobe

import androidx.compose.runtime.snapshots.Snapshot
import ca.stefanm.e39hmi.test.v1.ClearNotificationRequest
import ca.stefanm.e39hmi.test.v1.ListScreensRequest
import ca.stefanm.e39hmi.test.v1.ListScreensResponse
import ca.stefanm.e39hmi.test.v1.backStackEntry
import ca.stefanm.e39hmi.test.v1.listScreensResponse
import ca.stefanm.e39hmi.test.v1.CloseOverlaysRequest
import ca.stefanm.e39hmi.test.v1.DeliverFakeMailRequest
import ca.stefanm.e39hmi.test.v1.DeliverFakeMailResponse
import ca.stefanm.e39hmi.test.v1.GetStateRequest
import ca.stefanm.e39hmi.test.v1.GetTextsRequest
import ca.stefanm.e39hmi.test.v1.GetTextsResponse
import ca.stefanm.e39hmi.test.v1.ScreenText
import ca.stefanm.e39hmi.test.v1.getTextsResponse
import ca.stefanm.e39hmi.test.v1.screenText
import ca.stefanm.e39hmi.test.v1.HmiState
import ca.stefanm.e39hmi.test.v1.HmiTestProbeGrpcKt
import ca.stefanm.e39hmi.test.v1.KnobTurn
import ca.stefanm.e39hmi.test.v1.NavigateRequest
import ca.stefanm.e39hmi.test.v1.ResetFakeMailRequest
import ca.stefanm.e39hmi.test.v1.ResetFakeMailResponse
import ca.stefanm.e39hmi.test.v1.ResetMailAppRequest
import ca.stefanm.e39hmi.test.v1.ResetContactsAppRequest
import ca.stefanm.e39hmi.test.v1.ResetAccountsRequest
import ca.stefanm.e39hmi.test.v1.ResetAccountsResponse
import ca.stefanm.e39hmi.test.v1.resetAccountsResponse
import ca.stefanm.e39hmi.test.v1.ResetContactsAppResponse
import ca.stefanm.e39hmi.test.v1.resetContactsAppResponse
import ca.stefanm.e39hmi.test.v1.ResetMailAppResponse
import ca.stefanm.e39hmi.test.v1.resetMailAppResponse
import ca.stefanm.e39hmi.test.v1.ScreenshotRequest
import ca.stefanm.e39hmi.test.v1.ScreenshotResponse
import ca.stefanm.e39hmi.test.v1.SendInputRequest
import ca.stefanm.e39hmi.test.v1.SendInputResponse
import ca.stefanm.e39hmi.test.v1.WaitForIdleRequest
import ca.stefanm.e39hmi.test.v1.deliverFakeMailResponse
import ca.stefanm.e39hmi.test.v1.hmiState
import ca.stefanm.e39hmi.test.v1.notification
import ca.stefanm.e39hmi.test.v1.resetFakeMailResponse
import ca.stefanm.e39hmi.test.v1.screenshotResponse
import ca.stefanm.e39hmi.test.v1.sendInputResponse
import ca.stefanm.ibus.car.bordmonitor.input.InputEvent
import ca.stefanm.ibus.di.ApplicationModule
import ca.stefanm.ibus.di.AutoDiscoveredNodesRegistry
import ca.stefanm.ibus.di.ApplicationScope
import ca.stefanm.ibus.gui.menu.navigator.NavigationModule
import ca.stefanm.ibus.gui.menu.navigator.NavigationNode
import ca.stefanm.ibus.gui.menu.navigator.NavigationNodeTraverser
import ca.stefanm.ibus.gui.menu.navigator.Navigator
import ca.stefanm.ibus.gui.menu.notifications.NotificationHub
import ca.stefanm.ibus.gui.menu.widgets.knobListener.KnobListenerService
import ca.stefanm.ibus.gui.menu.widgets.modalMenu.ModalMenuService
import ca.stefanm.ibus.gui.menu.widgets.themes.ThemeConfigurationStorage
import ca.stefanm.ibus.gui.pim.contacts.repo.ContactsRepository
import ca.stefanm.ibus.gui.pim.accounts.repo.api.AddMailAccountRepo
import ca.stefanm.ibus.gui.pim.calendar.repo.backend.AccountRepository
import ca.stefanm.ibus.gui.pim.calendar.repo.backend.FakeAccountRepository
import ca.stefanm.ibus.gui.pim.contacts.repo.FakeContactsRepository
import ca.stefanm.ibus.gui.pim.contacts.repo.api.ContactEditRepo
import ca.stefanm.ibus.gui.pim.contacts.repo.api.ContactListRepo
import ca.stefanm.ibus.gui.pim.email.repo.FakeMailRepository
import ca.stefanm.ibus.gui.pim.email.repo.MailAddress
import ca.stefanm.ibus.gui.pim.email.repo.MailRepository
import ca.stefanm.ibus.gui.pim.email.repo.MailSettingsRepository
import ca.stefanm.ibus.gui.pim.email.repo.api.MailComposeRepo
import ca.stefanm.ibus.gui.pim.email.repo.api.MessageListRepo
import ca.stefanm.ibus.lib.logging.Logger
import io.grpc.Server
import io.grpc.Status
import io.grpc.StatusException
import io.grpc.netty.shaded.io.grpc.netty.NettyServerBuilder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import java.awt.Frame
import java.awt.Rectangle
import java.awt.Robot
import java.awt.Window
import java.io.ByteArrayOutputStream
import java.net.InetSocketAddress
import java.util.concurrent.atomic.AtomicLong
import javax.imageio.ImageIO
import javax.inject.Inject
import javax.inject.Named
import javax.inject.Provider

/**
 * The UI test probe (proto/e39hmi_test/v1/probe.proto): a gRPC server on
 * 127.0.0.1 for uitest/'s runner, started by GuiMain when E39_TEST_PROBE
 * names a port. It reads the HMI's state (navigator, notifications,
 * overlays, knob listeners), feeds the input stream like the IBus parser,
 * and screenshots the HMI window. Nothing in the UI knows about it.
 */
@ApplicationScope
class TestProbe @Inject constructor(
    private val navigator: Provider<Navigator>,
    private val navigationNodeTraverser: NavigationNodeTraverser,
    @Named(NavigationModule.ALL_NODES) private val allNodes: Provider<Set<NavigationNode<*>>>,
    private val modalMenuService: Provider<ModalMenuService>,
    private val notificationHub: NotificationHub,
    @Named(ApplicationModule.KNOB_LISTENER_MAIN) private val knobMain: KnobListenerService,
    @Named(ApplicationModule.KNOB_LISTENER_MAIN_AUX) private val knobMainAux: KnobListenerService,
    @Named(ApplicationModule.KNOB_LISTENER_MODAL) private val knobModal: KnobListenerService,
    @Named(ApplicationModule.INPUT_EVENTS_WRITER) private val inputEvents: MutableSharedFlow<InputEvent>,
    private val mailRepository: Provider<MailRepository>,
    private val themeStorage: Provider<ThemeConfigurationStorage>,
    private val mailSettings: Provider<MailSettingsRepository>,
    private val mailCompose: Provider<MailComposeRepo>,
    private val messageLists: Provider<MessageListRepo>,
    private val contactsRepository: Provider<ContactsRepository>,
    private val contactLists: Provider<ContactListRepo>,
    private val contactEdit: Provider<ContactEditRepo>,
    private val accountRepository: Provider<AccountRepository>,
    private val addMailAccount: Provider<AddMailAccountRepo>,
    private val logger: Logger,
) {
    companion object {
        const val TAG = "TestProbe"
        const val ENV = "E39_TEST_PROBE"
        const val HMI_WINDOW_TITLE = "E39 Menu"
        const val DEFAULT_QUIET_MS = 300
        const val DEFAULT_TIMEOUT_MS = 10_000

        /** The port from E39_TEST_PROBE, or null (no probe). */
        fun portFromEnvironment(): Int? = System.getenv(ENV)?.trim()?.takeIf { it.isNotEmpty() }?.let {
            it.toIntOrNull()?.takeIf { p -> p in 1..65535 } ?: error("$ENV=$it isn't a port")
        }
    }

    private val uiChanges = AtomicLong(0)
    private val lastChangeAt = AtomicLong(System.currentTimeMillis())
    private val droppedInputs = AtomicLong(0)
    private var server: Server? = null

    fun start(port: Int) {
        // Every applied Compose state change: "the UI is still changing".
        Snapshot.registerApplyObserver { _, _ ->
            uiChanges.incrementAndGet()
            lastChangeAt.set(System.currentTimeMillis())
        }
        server = NettyServerBuilder.forAddress(InetSocketAddress("127.0.0.1", port))
            .addService(Service())
            .build()
            .start()
        logger.i(TAG, "UI test probe listening on 127.0.0.1:$port")
    }

    fun stop() {
        server?.shutdownNow()
    }

    private fun state(): HmiState {
        val nav = navigator.get()
        val current = nav.mainContentScreen.value
        val stack = nav.getDebugClient().backStack.toList()
        val modal = modalMenuService.get()
        val n = notificationHub.currentNotification.value
        return hmiState {
            screen = current.node.thisClass.simpleName
            screenClass = current.node.thisClass.name
            screenParameters = current.incomingResult?.let { r ->
                listOfNotNull(r.requestParameters?.let { "parameters=$it" }, r.result?.let { "result=$it" }).joinToString(", ")
            } ?: ""
            backStack.addAll(stack.asReversed().map { it.node.thisClass.simpleName })
            backStackEntries.addAll(stack.asReversed().map { record ->
                backStackEntry {
                    screen = record.node.thisClass.simpleName
                    parameters = record.incomingResult?.requestParameters?.toString() ?: ""
                    result = record.incomingResult?.result?.toString() ?: ""
                }
            })
            notification = notification {
                visible = notificationHub.currentNotificationIsVisible.value
                topText = n?.topText ?: ""
                contentText = n?.contentText ?: ""
                image = n?.image?.name ?: ""
            }
            sidePaneOpen = modal.sidePaneOverlay.value.ui != null
            modalMenuOpen = modal.modalMenuOverlay.value != null
            keyboardOpen = modal.isKeyboardShowing.value
            mainKnobListener = knobMain.listenerEnabled.value
            mainAuxKnobListener = knobMainAux.listenerEnabled.value
            modalKnobListener = knobModal.listenerEnabled.value
            inputSubscribers = inputEvents.subscriptionCount.value
            uiChanges = this@TestProbe.uiChanges.get()
            quietMs = System.currentTimeMillis() - lastChangeAt.get()
            droppedInputs = this@TestProbe.droppedInputs.get()
            val window = hmiWindow()
            hmiWindowOpen = window != null
            window?.let { w -> runCatching { w.locationOnScreen }.getOrNull() }?.let { p ->
                windowX = p.x
                windowY = p.y
            }
            fakeMail = mailRepository.get() is FakeMailRepository
            fakeContacts = contactsRepository.get() is FakeContactsRepository
            fakeAccounts = accountRepository.get() is FakeAccountRepository
        }
    }

    /**
     * Settled: no UI change for [quietMs] (counted from [since] at the
     * earliest: the HMI reacts to an input a frame or more after it's sent)
     * and someone listening for input.
     */
    private suspend fun waitForIdle(quietMs: Int, timeoutMs: Int, since: Long = 0): HmiState {
        val quiet = if (quietMs > 0) quietMs else DEFAULT_QUIET_MS
        val timeout = if (timeoutMs > 0) timeoutMs else DEFAULT_TIMEOUT_MS
        return withTimeoutOrNull(timeout.toLong()) {
            while (true) {
                val sinceChange = System.currentTimeMillis() - maxOf(lastChangeAt.get(), since)
                if (sinceChange >= quiet && inputEvents.subscriptionCount.value > 0) break
                delay(maxOf(20L, quiet - sinceChange).coerceAtMost(100L))
            }
            state()
        } ?: throw StatusException(Status.DEADLINE_EXCEEDED.withDescription(
            "not idle after $timeout ms: ${state().let { "quiet ${it.quietMs} ms, ${it.inputSubscribers} input subscribers, on ${it.screen}" }}"))
    }

    /**
     * Every named accessible below [window] (Compose: one per semantics node
     * with text, rows merged), and clickable ones without a name, top to
     * bottom. layer: the branch of the tree it's in, below the part all of
     * them share (the screen, then overlays drawn on top of it).
     */
    private fun screenTexts(window: Window): List<ScreenText> {
        val origin = window.locationOnScreen
        val found = mutableListOf<Pair<List<Int>, ScreenText>>()
        fun walk(a: javax.accessibility.Accessible?, path: List<Int>) {
            if (a == null || path.size > 60) return
            val ctx = a.accessibleContext ?: return
            val name = ctx.accessibleName ?: ""
            val comp = ctx.accessibleComponent
            val clickable = (ctx.accessibleAction?.accessibleActionCount ?: 0) > 0
            if (name.isNotBlank() || (clickable && path.isNotEmpty())) {
                val loc = runCatching { comp?.locationOnScreen }.getOrNull()
                val size = runCatching { comp?.size }.getOrNull()
                found += path to screenText {
                    text = name.trim()
                    x = (loc?.x ?: origin.x) - origin.x
                    y = (loc?.y ?: origin.y) - origin.y
                    width = size?.width ?: 0
                    height = size?.height ?: 0
                    role = ctx.accessibleRole?.toDisplayString() ?: ""
                    this.clickable = clickable
                }
            }
            for (i in 0 until ctx.accessibleChildrenCount) walk(ctx.getAccessibleChild(i), path + i)
        }
        walk(window as javax.accessibility.Accessible, emptyList())
        // Below the window itself: where the paths part.
        val paths = found.map { it.first }.filter { it.isNotEmpty() }
        var shared = 0
        while (paths.isNotEmpty() && paths.all { it.size > shared && it[shared] == paths[0][shared] }) shared++
        return found.map { (path, t) -> t.toBuilder().setLayer(path.getOrElse(shared) { 0 }).build() }
            .sortedWith(compareBy({ it.y }, { it.x }))
    }

    private fun hmiWindow(): Window? =
        Window.getWindows().firstOrNull { it.isShowing && it is Frame && it.title == HMI_WINDOW_TITLE }

    /** The HMI window's contents, without a hovering pointer. */
    private suspend fun capture(): java.awt.image.BufferedImage {
        withContext(Dispatchers.Main) {
            val window = hmiWindow() ?: throw StatusException(Status.FAILED_PRECONDITION.withDescription("the HMI window isn't open"))
            val origin = window.locationOnScreen
            // A pointer over the window shows as a hovered row.
            java.awt.MouseInfo.getPointerInfo()?.location?.let { p ->
                if (Rectangle(origin, window.size).contains(p)) {
                    Robot().mouseMove(origin.x + window.width + 5, origin.y + window.height + 5)
                }
            }
        }
        delay(150)
        return withContext(Dispatchers.Main) {
            val window = hmiWindow() ?: throw StatusException(Status.FAILED_PRECONDITION.withDescription("the HMI window isn't open"))
            val origin = window.locationOnScreen
            val insets = window.insets
            Robot().createScreenCapture(Rectangle(
                origin.x + insets.left, origin.y + insets.top,
                window.width - insets.left - insets.right, window.height - insets.top - insets.bottom,
            ))
        }
    }

    /**
     * Whether [t] is drawn as the knob's item: menu items get a frame (the
     * selection colour, or the chip colours for ArbitraryContentsMenuItem),
     * so a line of [colors] across most of the box's width in its top part
     * and another in its bottom part. A neighbour's frame only reaches into
     * one side; an unselected item's chip is a narrow vertical strip.
     */
    private fun isHighlighted(image: java.awt.image.BufferedImage, t: ScreenText, colors: List<androidx.compose.ui.graphics.Color>): Boolean {
        val rgb = colors.map { Triple((it.red * 255).toInt(), (it.green * 255).toInt(), (it.blue * 255).toInt()) }
        val x0 = t.x.coerceIn(0, image.width)
        val y0 = t.y.coerceIn(0, image.height)
        val x1 = (t.x + t.width).coerceIn(0, image.width)
        val y1 = (t.y + t.height).coerceIn(0, image.height)
        val h = y1 - y0
        if (x1 - x0 < 8 || h < 8) return false
        fun matches(p: Int) = rgb.any { (r, g, b) ->
            kotlin.math.abs(((p shr 16) and 0xFF) - r) < 24 && kotlin.math.abs(((p shr 8) and 0xFF) - g) < 24 &&
                kotlin.math.abs((p and 0xFF) - b) < 24
        }
        fun isLine(y: Int) = (x0 until x1).count { matches(image.getRGB(it, y)) } * 10 >= (x1 - x0) * 7
        val band = maxOf(2, h * 4 / 10)
        // A box at or past the window's edge: the frame (drawn a little
        // outside the box) shows on one side only.
        val edge = 12
        val cutTop = t.y < edge
        val cutBottom = t.y + t.height > image.height - edge
        val top = cutTop || (y0 until y0 + band).any(::isLine)
        val bottom = cutBottom || (y1 - band until y1).any(::isLine)
        if (top && bottom && !(cutTop && cutBottom)) return true
        // Its bottom hidden (e.g. behind the footer, drawn over the list):
        // the frame's top line and its right edge (an unselected row's chip
        // is on the left; a neighbour's frame only reaches in from above or
        // below).
        val reachesFooter = t.y + t.height > image.height - 64
        if (reachesFooter && !cutTop && (y0 until y0 + band).any(::isLine)) {
            val rightEdge = (maxOf(x0, x1 - 24) until x1)
            val rows = (y0 until y1).count { y -> rightEdge.any { matches(image.getRGB(it, y)) } }
            if (rows * 10 >= (y1 - y0) * 5) return true
        }
        return false
    }

    private fun toInputEvents(e: ca.stefanm.e39hmi.test.v1.InputEvent): List<InputEvent> = when (e.eventCase) {
        ca.stefanm.e39hmi.test.v1.InputEvent.EventCase.KNOB_TURN -> {
            val direction = when (e.knobTurn.direction) {
                KnobTurn.Direction.LEFT -> InputEvent.NavKnobTurned.Direction.LEFT
                KnobTurn.Direction.RIGHT -> InputEvent.NavKnobTurned.Direction.RIGHT
                else -> throw StatusException(Status.INVALID_ARGUMENT.withDescription("knob_turn needs a direction"))
            }
            val clicks = if (e.knobTurn.clicks == 0) 1 else e.knobTurn.clicks
            if (clicks !in 1..9) throw StatusException(Status.INVALID_ARGUMENT.withDescription("clicks $clicks not in 1..9"))
            // Single clicks, like a knob turned slowly: each can be waited on.
            List(clicks) { InputEvent.NavKnobTurned(1, direction) }
        }
        ca.stefanm.e39hmi.test.v1.InputEvent.EventCase.KNOB_PRESS -> listOf(InputEvent.NavKnobPressed)
        ca.stefanm.e39hmi.test.v1.InputEvent.EventCase.NEXT_TRACK -> listOf(InputEvent.NextTrack)
        ca.stefanm.e39hmi.test.v1.InputEvent.EventCase.PREV_TRACK -> listOf(InputEvent.PrevTrack)
        ca.stefanm.e39hmi.test.v1.InputEvent.EventCase.MENU -> listOf(InputEvent.BMBTMenuPressed)
        ca.stefanm.e39hmi.test.v1.InputEvent.EventCase.PHONE -> listOf(InputEvent.BMBTPhonePressed)
        ca.stefanm.e39hmi.test.v1.InputEvent.EventCase.RT -> listOf(InputEvent.RTButton)
        ca.stefanm.e39hmi.test.v1.InputEvent.EventCase.INDEX_SELECT -> listOf(InputEvent.IndexSelectEvent(e.indexSelect))
        else -> throw StatusException(Status.INVALID_ARGUMENT.withDescription("empty input event"))
    }

    private fun fakeMail(): FakeMailRepository = mailRepository.get() as? FakeMailRepository
        ?: throw StatusException(Status.FAILED_PRECONDITION.withDescription("the HMI uses the PIM bridge's mail, not sample mail (useFakeData)"))

    private inner class Service : HmiTestProbeGrpcKt.HmiTestProbeCoroutineImplBase() {

        override suspend fun getState(request: GetStateRequest): HmiState = state()

        override suspend fun waitForIdle(request: WaitForIdleRequest): HmiState =
            waitForIdle(request.quietMs, request.timeoutMs, since = if (request.fromNow) System.currentTimeMillis() else 0)

        override suspend fun sendInput(request: SendInputRequest): SendInputResponse {
            val events = request.eventsList.flatMap { toInputEvents(it) }
            var dropped = 0
            if (request.waitForIdle) waitForIdle(request.quietMs, request.timeoutMs)
            for (event in events) {
                if (inputEvents.subscriptionCount.value == 0) {
                    dropped++
                    droppedInputs.incrementAndGet()
                    logger.w(TAG, "Input $event lost: nothing listening (on ${state().screen})")
                }
                val sentAt = System.currentTimeMillis()
                inputEvents.emit(event)
                if (request.waitForIdle) waitForIdle(request.quietMs, request.timeoutMs, since = sentAt)
            }
            val after = state()
            return sendInputResponse {
                this.dropped = dropped
                state = after
            }
        }

        override suspend fun screenshot(request: ScreenshotRequest): ScreenshotResponse {
            val image = capture()
            val png = ByteArrayOutputStream().also { ImageIO.write(image, "png", it) }.toByteArray()
            return screenshotResponse {
                this.png = com.google.protobuf.ByteString.copyFrom(png)
                width = image.width
                height = image.height
            }
        }

        override suspend fun getTexts(request: GetTextsRequest): GetTextsResponse {
            val shot = capture()
            val c = themeStorage.get().getTheme().first().colors
            val selected = listOf(c.selectedColor, c.chipColor, c.chipHighlights)
            val texts = withContext(Dispatchers.Main) {
                val window = hmiWindow() ?: throw StatusException(Status.FAILED_PRECONDITION.withDescription("the HMI window isn't open"))
                screenTexts(window)
            }.map { t ->
                if (t.clickable && isHighlighted(shot, t, selected)) t.toBuilder().setSelected(true).build() else t
            }
            return getTextsResponse { this.texts.addAll(texts) }
        }

        override suspend fun listScreens(request: ListScreensRequest): ListScreensResponse {
            val registered = allNodes.get().map { it.thisClass }.toSet()
            val discovered = AutoDiscoveredNodesRegistry().getAllDiscoveredNodeClasses()
            return listScreensResponse {
                this.registered.addAll(registered.map { it.name }.sorted())
                unregistered.addAll((discovered - registered).map { it.name }.sorted())
            }
        }

        override suspend fun navigate(request: NavigateRequest): HmiState {
            withContext(Dispatchers.Main) {
                when (request.toCase) {
                    NavigateRequest.ToCase.ROOT -> navigationNodeTraverser.navigateToRoot()
                    NavigateRequest.ToCase.BACK -> navigationNodeTraverser.goBack()
                    NavigateRequest.ToCase.SCREEN -> {
                        val name = request.screen
                        val node = allNodes.get().firstOrNull { it.thisClass.name == name || it.thisClass.simpleName == name }
                            ?: throw StatusException(Status.NOT_FOUND.withDescription("no registered screen $name"))
                        navigationNodeTraverser.navigateToNode(node.thisClass)
                    }
                    else -> throw StatusException(Status.INVALID_ARGUMENT.withDescription("navigate where?"))
                }
            }
            return state()
        }

        override suspend fun closeOverlays(request: CloseOverlaysRequest): HmiState {
            withContext(Dispatchers.Main) {
                val modal = modalMenuService.get()
                modal.closeSidePaneOverlay(true)
                modal.closeModalMenu()
                modal.isKeyboardShowing.value = false
                notificationHub.clearNotification()
            }
            return state()
        }

        override suspend fun clearNotification(request: ClearNotificationRequest): HmiState {
            notificationHub.clearNotification()
            return state()
        }

        override suspend fun resetMailApp(request: ResetMailAppRequest): ResetMailAppResponse {
            withContext(Dispatchers.Main) {
                mailSettings.get().resetToDefaults()
                mailCompose.get().discard()
                messageLists.get().clearPositions()
                (mailRepository.get() as? FakeMailRepository)?.reset()
            }
            return resetMailAppResponse { }
        }

        override suspend fun resetContactsApp(request: ResetContactsAppRequest): ResetContactsAppResponse {
            withContext(Dispatchers.Main) {
                contactLists.get().clearPositions()
                contactEdit.get().discard()
                (contactsRepository.get() as? FakeContactsRepository)?.reset()
            }
            return resetContactsAppResponse { }
        }

        override suspend fun resetAccounts(request: ResetAccountsRequest): ResetAccountsResponse {
            withContext(Dispatchers.Main) {
                addMailAccount.get().discard()
                (accountRepository.get() as? FakeAccountRepository)?.reset()
            }
            return resetAccountsResponse { }
        }

        override suspend fun resetFakeMail(request: ResetFakeMailRequest): ResetFakeMailResponse {
            fakeMail().reset()
            return resetFakeMailResponse { }
        }

        override suspend fun deliverFakeMail(request: DeliverFakeMailRequest): DeliverFakeMailResponse {
            val fake = fakeMail()
            val from = if (request.fromEmail.isBlank()) MailAddress("Anna Kowalski", "anna@example.com")
            else MailAddress(request.fromName, request.fromEmail)
            fake.simulateNewMail(
                from = from,
                subject = request.subject.ifBlank { "Are you close?" },
                text = request.text.ifBlank { "We're at the restaurant already. Table by the window." },
            )
            return deliverFakeMailResponse { }
        }
    }
}
