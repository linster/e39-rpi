package ca.stefanm.ibus.testprobe

import androidx.compose.runtime.snapshots.Snapshot
import ca.stefanm.e39hmi.test.v1.ClearNotificationRequest
import ca.stefanm.e39hmi.test.v1.ReopenHmiWindowRequest
import ca.stefanm.e39hmi.test.v1.OpenDebugWindowRequest
import ca.stefanm.e39hmi.test.v1.OpenDebugWindowResponse
import ca.stefanm.e39hmi.test.v1.openDebugWindowResponse
import ca.stefanm.e39hmi.test.v1.CloseDebugWindowRequest
import ca.stefanm.e39hmi.test.v1.CloseDebugWindowResponse
import ca.stefanm.e39hmi.test.v1.closeDebugWindowResponse
import ca.stefanm.e39hmi.test.v1.CrashHmiRequest
import ca.stefanm.e39hmi.test.v1.CrashHmiResponse
import ca.stefanm.e39hmi.test.v1.crashHmiResponse
import ca.stefanm.ibus.gui.crash.HmiCrashHandler
import ca.stefanm.e39hmi.test.v1.ClickTextRequest
import ca.stefanm.e39hmi.test.v1.ClickTextResponse
import ca.stefanm.e39hmi.test.v1.clickTextResponse
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
import ca.stefanm.ibus.car.platform.ConfigurablePlatform
import ca.stefanm.ibus.car.platform.PlatformService
import ca.stefanm.ibus.gui.map.location.LocationRepository
import ca.stefanm.ibus.lib.hardwareDrivers.gps.FakeLocationService
import ca.stefanm.ibus.di.ApplicationModule
import ca.stefanm.ibus.di.AutoDiscoveredNodesRegistry
import ca.stefanm.ibus.di.ApplicationScope
import ca.stefanm.ibus.gui.menu.navigator.NavigationModule
import ca.stefanm.ibus.gui.menu.navigator.NavigationNode
import ca.stefanm.ibus.gui.menu.navigator.NavigationNodeTraverser
import ca.stefanm.ibus.gui.menu.navigator.Navigator
import ca.stefanm.ibus.gui.menu.navigator.WindowManager
import ca.stefanm.ibus.gui.debug.windows.DebugLaunchpad
import ca.stefanm.ibus.gui.menu.widgets.themes.designer.ThemeColorMath
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
import ca.stefanm.e39hmi.test.v1.GetGuidanceRequest
import ca.stefanm.e39hmi.test.v1.GuidanceSessionInfo
import ca.stefanm.e39hmi.test.v1.GuidanceState
import ca.stefanm.e39hmi.test.v1.ResetGuidanceRequest
import ca.stefanm.e39hmi.test.v1.ResetGuidanceResponse
import ca.stefanm.e39hmi.test.v1.SetPositionRequest
import ca.stefanm.e39hmi.test.v1.geoPoint
import ca.stefanm.e39hmi.test.v1.guidanceInstructionInfo
import ca.stefanm.e39hmi.test.v1.guidanceSessionInfo
import ca.stefanm.e39hmi.test.v1.guidanceState
import ca.stefanm.e39hmi.test.v1.resetGuidanceResponse
import ca.stefanm.e39hmi.test.v1.SetFakeGeocodingRequest
import ca.stefanm.e39hmi.test.v1.SetFakeGeocodingResponse
import ca.stefanm.e39hmi.test.v1.setFakeGeocodingResponse
import ca.stefanm.ibus.gui.map.repo.FakeGeocodeRepository
import ca.stefanm.ibus.gui.map.repo.GeocodeRepository
import ca.stefanm.ibus.configuration.ConfigurationStorage
import ca.stefanm.ibus.configuration.E39Config
import ca.stefanm.ibus.gui.map.guidance.GuidanceService
import ca.stefanm.ibus.gui.map.guidance.GuidanceSession
import com.javadocmd.simplelatlng.LatLng
import io.grpc.Server
import io.grpc.Status
import io.grpc.StatusException
import io.grpc.netty.shaded.io.grpc.netty.NettyServerBuilder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
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
@OptIn(ExperimentalCoroutinesApi::class)
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
    private val guidanceService: Provider<GuidanceService>,
    private val configurablePlatform: Provider<ConfigurablePlatform>,
    private val locationRepository: Provider<LocationRepository>,
    private val configurationStorage: Provider<ConfigurationStorage>,
    private val windowManager: Provider<WindowManager>,
    private val debugLaunchpad: Provider<DebugLaunchpad>,
    private val crashHandler: Provider<HmiCrashHandler>,
    private val geocodeRepository: Provider<GeocodeRepository>,
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
            fakeGeocode = geocodeRepository.get() is FakeGeocodeRepository
            themeStorage.get().shownTheme().let { shown ->
                theme = shown.configFileName
                themeMenuBackground = ThemeColorMath.toHex(shown.colors.menuBackground)
            }
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
        // The content's corner, as capture() takes it: a window manager's
        // frame and title bar (insets; none on Xvfb) aren't in screenshots.
        val origin = window.locationOnScreen.let { java.awt.Point(it.x + window.insets.left, it.y + window.insets.top) }
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

    private fun hmiWindow(): Window? = shownFrame(HMI_WINDOW_TITLE)

    private fun shownFrame(title: String): Window? =
        Window.getWindows().firstOrNull { it.isShowing && it is Frame && it.title == title }

    /** One launchpad for the probe's life, so a window it opened is the one it closes. */
    private val launchpad by lazy { debugLaunchpad.get() }

    private fun debugWindowNamed(title: String): WindowManager.E39Window =
        launchpad.windows.firstOrNull { it.title == title }
            ?: throw StatusException(Status.NOT_FOUND.withDescription(
                "no debug window \"$title\"; there are: " + launchpad.windows.joinToString { "\"${it.title}\"" }
            ))

    /** On the main thread: the HMI window ([name] empty), or the open debug window titled [name]. */
    private fun windowNamed(name: String): Window =
        if (name.isEmpty()) {
            hmiWindow() ?: throw StatusException(Status.FAILED_PRECONDITION.withDescription("the HMI window isn't open"))
        } else {
            shownFrame(name) ?: throw StatusException(Status.FAILED_PRECONDITION.withDescription("no open window \"$name\""))
        }

    /**
     * The window's ([windowNamed]) contents, without a hovering pointer. It's
     * raised first: a screenshot is of the screen, and a debug window may be
     * over the HMI window (or the other way round).
     */
    private suspend fun capture(name: String = ""): java.awt.image.BufferedImage {
        withContext(Dispatchers.Main) {
            val window = windowNamed(name)
            window.toFront()
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
            val window = windowNamed(name)
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

    private fun sessionInfo(session: GuidanceSession): GuidanceSessionInfo {
        fun point(p: LatLng) = geoPoint { latitude = p.latitude; longitude = p.longitude }
        return guidanceSessionInfo {
            state = session.sessionState.name
            session.startPoint?.let { start = point(it) }
            session.endPoint?.let { end = point(it) }
            hasRoute = session.route != null
            session.route?.let { r -> route.addAll(r.map(::point)) }
        }
    }

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
            val image = capture(request.window)
            val png = ByteArrayOutputStream().also { ImageIO.write(image, "png", it) }.toByteArray()
            return screenshotResponse {
                this.png = com.google.protobuf.ByteString.copyFrom(png)
                width = image.width
                height = image.height
            }
        }

        override suspend fun getTexts(request: GetTextsRequest): GetTextsResponse {
            if (request.window.isNotEmpty()) {
                // A debug window: no knob, so nothing is selected.
                val texts = withContext(Dispatchers.Main) { screenTexts(windowNamed(request.window)) }
                return getTextsResponse { this.texts.addAll(texts) }
            }
            val shot = capture()
            val c = themeStorage.get().getTheme().first().colors
            val selected = listOf(c.selectedColor, c.chipColor, c.chipHighlights)
            val texts = withContext(Dispatchers.Main) {
                screenTexts(windowNamed(""))
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

        override suspend fun reopenHmiWindow(request: ReopenHmiWindowRequest): HmiState {
            suspend fun waitForWindow(open: Boolean, what: String) {
                withTimeoutOrNull(DEFAULT_TIMEOUT_MS.toLong()) {
                    while (withContext(Dispatchers.Main) { hmiWindow() != null } != open) delay(50)
                } ?: throw StatusException(Status.DEADLINE_EXCEEDED.withDescription("the HMI window didn't $what"))
            }
            withContext(Dispatchers.Main) { windowManager.get().closeHmiMainWindow() }
            waitForWindow(open = false, what = "close")
            withContext(Dispatchers.Main) { windowManager.get().openHmiMainWindow() }
            waitForWindow(open = true, what = "open again")
            return state()
        }

        override suspend fun openDebugWindow(request: OpenDebugWindowRequest): OpenDebugWindowResponse {
            val window = debugWindowNamed(request.title)
            withContext(Dispatchers.Main) { windowManager.get().openDebugWindow(window) }
            waitForFrame(request.title, showing = true)
            return openDebugWindowResponse { }
        }

        override suspend fun closeDebugWindow(request: CloseDebugWindowRequest): CloseDebugWindowResponse {
            val window = debugWindowNamed(request.title)
            withContext(Dispatchers.Main) { windowManager.get().closeDebugWindow(window) }
            waitForFrame(request.title, showing = false)
            return closeDebugWindowResponse { }
        }

        override suspend fun crashHmi(request: CrashHmiRequest): CrashHmiResponse {
            if (withContext(Dispatchers.Main) { hmiWindow() } == null) {
                throw StatusException(Status.FAILED_PRECONDITION.withDescription("the HMI window isn't open"))
            }
            crashHandler.get().requestCrash()
            // Thrown at the HMI window's next composition.
            withTimeoutOrNull(DEFAULT_TIMEOUT_MS.toLong()) {
                while (crashHandler.get().crashRequested.value) delay(20)
            } ?: throw StatusException(Status.DEADLINE_EXCEEDED.withDescription("the HMI window didn't crash"))
            return crashHmiResponse { }
        }

        override suspend fun clickText(request: ClickTextRequest): ClickTextResponse {
            if (request.window.isEmpty()) {
                throw StatusException(Status.INVALID_ARGUMENT.withDescription("ClickText is for debug windows; the HMI takes knob input"))
            }
            // Raised first, so the click lands on it and not on a window over it.
            withContext(Dispatchers.Main) { windowNamed(request.window).toFront() }
            delay(150)
            withContext(Dispatchers.Main) {
                val window = windowNamed(request.window)
                val matches = screenTexts(window).filter { it.text == request.text }
                val target = matches.getOrNull(request.index) ?: throw StatusException(Status.NOT_FOUND.withDescription(
                    "${matches.size} item(s) \"${request.text}\" in \"${request.window}\", no #${request.index}"
                ))
                // Screen texts are in content pixels, from the content's corner.
                val origin = window.locationOnScreen
                val x = origin.x + window.insets.left + target.x + target.width / 2
                val y = origin.y + window.insets.top + target.y + target.height / 2
                Robot().apply {
                    mouseMove(x, y)
                    mousePress(java.awt.event.InputEvent.BUTTON1_DOWN_MASK)
                    mouseRelease(java.awt.event.InputEvent.BUTTON1_DOWN_MASK)
                }
            }
            return clickTextResponse { }
        }

        private suspend fun waitForFrame(title: String, showing: Boolean) {
            withTimeoutOrNull(DEFAULT_TIMEOUT_MS.toLong()) {
                while (withContext(Dispatchers.Main) { shownFrame(title) != null } != showing) delay(50)
            } ?: throw StatusException(Status.DEADLINE_EXCEEDED.withDescription(
                "\"$title\" didn't " + if (showing) "open" else "close"
            ))
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

        override suspend fun setFakeGeocoding(request: SetFakeGeocodingRequest): SetFakeGeocodingResponse {
            val fake = geocodeRepository.get() as? FakeGeocodeRepository
                ?: throw StatusException(Status.FAILED_PRECONDITION.withDescription(
                    "addresses are looked up by the maps backend (mapConfig.geocode.useFakeData is off)"))
            fake.noGeocodingData = request.noGeocodingData
            return setFakeGeocodingResponse { }
        }

        override suspend fun resetGuidance(request: ResetGuidanceRequest): ResetGuidanceResponse {
            val service = guidanceService.get()
            if (service.getInstantaneousGuidanceSessionState() == GuidanceSession.SessionState.IN_GUIDANCE) {
                service.stopGuidance()
            }
            service.clearCurrentSession()
            // clearCurrentSession() resets the instructions and the route calculation on the guidance thread.
            withTimeoutOrNull(5_000) {
                while (service.instructions.value.isNotEmpty() ||
                    service.routeCalculation.value != GuidanceService.RouteCalculation.Idle) delay(20)
            }
            return resetGuidanceResponse { }
        }

        override suspend fun getGuidance(request: GetGuidanceRequest): GuidanceState {
            val service = guidanceService.get()
            val stored = GuidanceSession(configurationStorage.get().config[E39Config.GuidanceConfig.currentSession])
            val calculation = service.routeCalculation.value
            return guidanceState {
                session = sessionInfo(service.getCurrentSessionInstantaneous())
                this.stored = sessionInfo(stored)
                routeCalculation = calculation::class.simpleName ?: ""
                when (calculation) {
                    is GuidanceService.RouteCalculation.Failed -> routeCalculationError = calculation.message
                    is GuidanceService.RouteCalculation.Calculated -> {
                        routeLengthMeters = calculation.route.lengthMeters
                        routeDurationSeconds = calculation.route.durationSeconds
                    }
                    else -> {}
                }
                instructions = service.instructions.value.size
                instructionList.addAll(service.instructions.value.map { i ->
                    guidanceInstructionInfo {
                        index = i.index
                        kind = i.kind.name
                        text = i.text
                        location = geoPoint { latitude = i.location.latitude; longitude = i.location.longitude }
                        triggerRadiusMeters = i.triggerRadiusMeters
                        pathIndex = i.pathIndex ?: -1
                    }
                })
                service.trackerState.value.let {
                    nextIndex = it.nextIndex
                    shownIndex = it.shownIndex ?: -1
                }
            }
        }

        override suspend fun setPosition(request: SetPositionRequest): GuidanceState {
            val service = guidanceService.get()
            val fake = fakeLocationService()
            val position = LatLng(request.position.latitude, request.position.longitude)
            // As a location service would: into the LocationRepository, which GuidanceService follows.
            // A service just started starts on another thread: until then it ignores locations.
            withTimeoutOrNull(5_000) {
                while (!fake.setLocation(position)) delay(20)
            } ?: throw StatusException(Status.DEADLINE_EXCEEDED.withDescription(
                "${FakeLocationService.NAME} didn't start"))
            // The fix it reported (a new object each time, even at the same place).
            val fix = locationRepository.get().fix.value
            // Returns once guidance has handled that fix.
            withTimeoutOrNull(10_000) {
                service.lastHandledFix.first { it === fix }
            } ?: throw StatusException(Status.DEADLINE_EXCEEDED.withDescription(
                "GuidanceService didn't handle the position from the LocationRepository"))
            return getGuidance(GetGuidanceRequest.getDefaultInstance())
        }

        /**
         * The car platform's FakeLocationService (group GpsLocationProvidersDebug),
         * started if it isn't running. The runner's configuration doesn't start the
         * GpsLocationProviders group, so GpsdService stays off.
         */
        private fun fakeLocationService(): FakeLocationService {
            val platform = configurablePlatform.get()
            val platformService = try {
                platform.findServiceByName(FakeLocationService.NAME)
            } catch (e: NoSuchElementException) {
                null
            } ?: throw StatusException(Status.FAILED_PRECONDITION.withDescription(
                "no ${FakeLocationService.NAME}: is the car platform running?"))
            if (platformService.runStatus != PlatformService.RunStatus.RUNNING) {
                platform.startServiceByName(FakeLocationService.NAME)
            }
            return platformService.baseService as FakeLocationService
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
