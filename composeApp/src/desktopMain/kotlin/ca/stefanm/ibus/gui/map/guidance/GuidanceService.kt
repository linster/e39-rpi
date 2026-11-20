package ca.stefanm.ibus.gui.map.guidance

import ca.stefanm.gui.map.guidance.GuidanceSessionStorage
import ca.stefanm.ibus.gui.map.guidance.GuidanceSession
import ca.stefanm.ibus.di.ApplicationScope
import ca.stefanm.ibus.gui.menu.Notification
import ca.stefanm.ibus.gui.menu.notifications.NotificationHub
import ca.stefanm.ibus.gui.map.guidance.GuidanceInstructionTracker.Event
import ca.stefanm.ibus.gui.map.repo.RemoteRouteRepository
import ca.stefanm.ibus.lib.logging.Logger
import com.javadocmd.simplelatlng.LatLng
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import java.time.LocalTime
import java.time.temporal.ChronoUnit
import java.util.concurrent.Executors
import javax.inject.Inject

//Responsible for contacting HERE, getting waypoints between destinations,
//emitting directions UI (to side-pane) -- if enabled.
@OptIn(ExperimentalCoroutinesApi::class)
@ApplicationScope
class GuidanceService @Inject constructor(
    private val guidanceSessionStorage: GuidanceSessionStorage,
    private val routeRepository: RemoteRouteRepository,
    private val notificationHub: NotificationHub,
    private val notificationGuidanceInstructionConsumer: NotificationGuidanceInstructionConsumer,
    private val sideBarGuidanceInstructionConsumer: SideBarGuidanceInstructionConsumer,
    private val guidanceSetupScreenInstructionConsumer: GuidanceSetupScreenInstructionConsumer,
    private val coroutineScope: CoroutineScope,
    private val logger: Logger
) {

    companion object {
        const val TAG = "GuidanceService"
    }

    fun getSessionRoute() = guidanceSessionStorage
        .getCurrentSessionFlow()
        .map { it.route }

    fun getGuidanceSessionState() = guidanceSessionStorage
        .getCurrentSessionFlow()
        .map { it.sessionState }

    fun getInstantaneousGuidanceSessionState() = guidanceSessionStorage.currentSession.sessionState
    fun getCurrentSessionInstantaneous() = guidanceSessionStorage.currentSession
    fun getCurrentSession() = guidanceSessionStorage.getCurrentSessionFlow()

    fun clearCurrentSession() {
        guidanceSessionStorage.currentSession = GuidanceSession()
        guidanceSessionStorage.updateWithCurrent()
        coroutineScope.launch(guidanceThread) {
            _instructions.value = emptyList()
            tracker = GuidanceInstructionTracker(emptyList())
            publishTrackerState()
            _routeCalculation.value = RouteCalculation.Idle
        }
    }

    fun setStartPoint(start : LatLng) {
        guidanceSessionStorage.currentSession.startPoint = start
        guidanceSessionStorage.updateWithCurrent()
    }
    fun setEndPoint(end : LatLng) {
        guidanceSessionStorage.currentSession.endPoint = end
        guidanceSessionStorage.updateWithCurrent()
    }

    /** Where the last [calculateRoute] got to. */
    sealed interface RouteCalculation {
        data object Idle : RouteCalculation
        data object Calculating : RouteCalculation
        data class Calculated(val route: RemoteRouteRepository.ComputedRoute) : RouteCalculation
        data class Failed(val message: String) : RouteCalculation
    }

    val routeCalculation: StateFlow<RouteCalculation> get() = _routeCalculation.asStateFlow()
    private val _routeCalculation = MutableStateFlow<RouteCalculation>(RouteCalculation.Idle)

    private val routeCalculatorDispatcher = Executors.newSingleThreadExecutor().asCoroutineDispatcher()

    /**
     * Compute the route between the session's start and end points with the
     * maps backend (RouteService, Valhalla) and make it the session's route,
     * with its instructions. The session must be READY_TO_CALCULATE.
     */
    fun calculateRoute() {
        coroutineScope.launch(routeCalculatorDispatcher) {
            val session = guidanceSessionStorage.currentSession
            val start = session.startPoint
            val end = session.endPoint
            if (session.sessionState != GuidanceSession.SessionState.READY_TO_CALCULATE || start == null || end == null) {
                logger.w(TAG, "Not calculating a route: session is ${session.sessionState}")
                return@launch
            }

            _routeCalculation.value = RouteCalculation.Calculating
            notificationHub.postNotification(
                Notification(
                    Notification.NotificationImage.MAP_GENERAL,
                    "Calculating Route",
                    "Please wait."
                )
            )

            routeRepository.computeRoute(start, end).fold(
                onSuccess = { route ->
                    // The session was cleared or replaced while the backend worked.
                    if (guidanceSessionStorage.currentSession !== session ||
                        session.sessionState != GuidanceSession.SessionState.READY_TO_CALCULATE) {
                        logger.d(TAG, "Dropping the calculated route: the session changed meanwhile.")
                        _routeCalculation.value = RouteCalculation.Idle
                        return@fold
                    }
                    withContext(guidanceThread) { loadRoute(start, end, route) }
                    _routeCalculation.value = RouteCalculation.Calculated(route)
                    notificationHub.postNotification(Notification(
                        Notification.NotificationImage.MAP_GENERAL,
                        "Calculated Route",
                        "%.0f km, %d min. Start Navigation.".format(
                            route.lengthMeters / 1000, (route.durationSeconds / 60).toInt())
                    ))
                },
                onFailure = { error ->
                    logger.e(TAG, "Route calculation failed", error)
                    _routeCalculation.value = RouteCalculation.Failed(error.message ?: error.toString())
                    notificationHub.postNotification(Notification(
                        Notification.NotificationImage.ALERT_CIRCLE,
                        "Route Failed",
                        error.message ?: "Maps backend unavailable."
                    ))
                },
            )
        }
    }

    private val guidanceThread = Executors.newSingleThreadExecutor().asCoroutineDispatcher()
    private var guidanceJob : Job? = null
    fun startGuidance() {
        logger.d(TAG, "Ask to guidance.")

        if (guidanceSessionStorage.currentSession.sessionState == GuidanceSession.SessionState.IN_GUIDANCE) {
            logger.d(TAG, "Not starting guidance, already in guidance.")
            return
        }

        guidanceJob = coroutineScope.launch(guidanceThread) {
            logger.d(TAG, "Started guidance.")

            guidanceSessionStorage.currentSession.startGuidance()
            guidanceSessionStorage.updateWithCurrent()

            tracker = GuidanceInstructionTracker(_instructions.value)
            log("Guidance started (${_instructions.value.size} instructions)")
            _position.value?.let { dispatch(tracker.update(it)) } ?: publishTrackerState()
        }
    }

    fun stopGuidance() {

        logger.d(TAG, "Stopping guidance.")

        guidanceJob?.cancel()
        guidanceJob = null

        guidanceSessionStorage.currentSession.terminateGuidance()
        guidanceSessionStorage.updateWithCurrent()
        coroutineScope.launch(guidanceThread) {
            consumers.forEach { it.guidanceEnded() }
            log("Guidance stopped")
        }

        notificationHub.postNotificationBackground(Notification(
            Notification.NotificationImage.MAP_GENERAL,
            "Ended Guidance"
        ))
    }

    fun repeatLastDirection() {
        coroutineScope.launch(guidanceThread) {
            val shown = tracker.shown ?: tracker.next ?: return@launch
            val meters = _position.value?.let { GuidanceInstructionTracker.meters(it, shown.location) } ?: 0.0
            log("Repeat: ${shown.index} ${shown.kind}")
            consumers.forEach { it.notifyInstruction(shown, meters) }
        }
    }

    // --- Instructions (maneuvers) along the route -------------------------

    /** The route's turn-by-turn instructions (from the maps backend's RouteService). */
    val instructions: StateFlow<List<GuidanceInstruction>> get() = _instructions.asStateFlow()
    private val _instructions = MutableStateFlow<List<GuidanceInstruction>>(emptyList())

    /** Where the car is (GPS, or the guidance debug window's fake positions). */
    val position: StateFlow<LatLng?> get() = _position.asStateFlow()
    private val _position = MutableStateFlow<LatLng?>(null)

    data class TrackerState(val nextIndex: Int, val shownIndex: Int?, val distanceMeters: Double?)
    /** Which instruction is next / shown (positions in [instructions]), for the debug window. */
    val trackerState: StateFlow<TrackerState> get() = _trackerState.asStateFlow()
    private val _trackerState = MutableStateFlow(TrackerState(0, null, null))

    /** What happened lately (newest last), for the debug window. */
    val eventLog: StateFlow<List<String>> get() = _eventLog.asStateFlow()
    private val _eventLog = MutableStateFlow<List<String>>(emptyList())

    private var tracker = GuidanceInstructionTracker(emptyList())

    private val consumers: List<GuidanceInstructionConsumer>
        get() = listOf(
            notificationGuidanceInstructionConsumer,
            sideBarGuidanceInstructionConsumer,
            guidanceSetupScreenInstructionConsumer,
        )

    /** On [guidanceThread]: a new session with [route]'s path, its instructions and a fresh tracker. */
    private suspend fun loadRoute(start: LatLng, end: LatLng, route: RemoteRouteRepository.ComputedRoute) {
        if (guidanceSessionStorage.currentSession.sessionState == GuidanceSession.SessionState.IN_GUIDANCE) {
            consumers.forEach { it.guidanceEnded() }
        }
        guidanceSessionStorage.currentSession = GuidanceSession().apply {
            startPoint = start
            endPoint = end
            this.route = route.path
        }
        _instructions.value = GuidanceInstruction.fromRoute(route.maneuvers)
        tracker = GuidanceInstructionTracker(_instructions.value)
        publishTrackerState()
        log("Route: ${_instructions.value.size} instructions, ${(route.lengthMeters / 1000).toInt()} km")
    }

    //TODO this needs to be fed from gpsd.
    /** The car is at [position]: show, update or hide instructions (in guidance only). */
    fun updatePosition(position: LatLng) {
        coroutineScope.launch(guidanceThread) {
            _position.value = position
            if (guidanceSessionStorage.currentSession.sessionState != GuidanceSession.SessionState.IN_GUIDANCE) {
                return@launch
            }
            dispatch(tracker.update(position))
        }
    }

    /** Hide the shown (or next) instruction and go on to the one after. */
    fun dismissInstruction() {
        coroutineScope.launch(guidanceThread) {
            if (guidanceSessionStorage.currentSession.sessionState == GuidanceSession.SessionState.IN_GUIDANCE) {
                dispatch(tracker.dismiss())
            }
        }
    }

    private suspend fun dispatch(events: List<Event>) {
        for (event in events) {
            log(describe(event))
            when (event) {
                is Event.Approaching -> consumers.forEach { it.notifyInstruction(event.instruction, event.distanceMeters) }
                is Event.Distance -> consumers.forEach { it.updateDistance(event.instruction, event.distanceMeters) }
                is Event.Passed -> consumers.forEach { it.dismissInstruction(event.instruction) }
                is Event.Arrived -> {
                    consumers.forEach { it.dismissInstruction(event.instruction) }
                    notificationHub.postNotificationBackground(Notification(
                        Notification.NotificationImage.GUIDANCE_DESTINATION,
                        "You have arrived",
                        event.instruction.text,
                    ))
                }
            }
            _trackerState.value = TrackerState(
                tracker.nextIndex,
                tracker.shown?.let { tracker.instructions.indexOf(it) },
                (event as? Event.Distance)?.distanceMeters ?: (event as? Event.Approaching)?.distanceMeters,
            )
        }
        if (events.isEmpty()) publishTrackerState()
    }

    private fun publishTrackerState() {
        _trackerState.value = _trackerState.value.copy(
            nextIndex = tracker.nextIndex,
            shownIndex = tracker.shown?.let { tracker.instructions.indexOf(it) },
        )
    }

    private fun describe(event: Event): String = when (event) {
        is Event.Approaching -> "Approaching #${event.instruction.index} ${event.instruction.kind}, ${event.distanceMeters.toInt()} m: ${event.instruction.text}"
        is Event.Distance -> "  #${event.instruction.index}: ${event.distanceMeters.toInt()} m"
        is Event.Passed -> (if (event.dismissed) "Dismissed" else "Passed") + " #${event.instruction.index} ${event.instruction.kind}"
        is Event.Arrived -> "Arrived: ${event.instruction.text}"
    }

    private fun log(line: String) {
        logger.d(TAG, line)
        _eventLog.value = (_eventLog.value + "${LocalTime.now().truncatedTo(ChronoUnit.SECONDS)} $line").takeLast(200)
    }
}

