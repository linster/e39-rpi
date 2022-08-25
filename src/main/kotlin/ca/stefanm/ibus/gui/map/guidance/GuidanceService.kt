package ca.stefanm.ibus.gui.map.guidance

import ca.stefanm.gui.map.guidance.GuidanceSessionStorage
import ca.stefanm.ibus.gui.map.guidance.GuidanceSession
import ca.stefanm.ibus.di.ApplicationScope
import ca.stefanm.ibus.gui.menu.Notification
import ca.stefanm.ibus.gui.menu.notifications.NotificationHub
import ca.stefanm.ibus.lib.logging.Logger
import com.javadocmd.simplelatlng.LatLng
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.map
import java.util.concurrent.Executors
import javax.inject.Inject

//Responsible for contacting HERE, getting waypoints between destinations,
//emitting directions UI (to side-pane) -- if enabled.
@OptIn(ExperimentalCoroutinesApi::class)
@ApplicationScope
class GuidanceService @Inject constructor(
    private val guidanceSessionStorage: GuidanceSessionStorage,
    private val routeCalculator: RouteCalculator,
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
    }

    fun setStartPoint(start : LatLng) {
        guidanceSessionStorage.currentSession.startPoint = start
        guidanceSessionStorage.updateWithCurrent()
    }
    fun setEndPoint(end : LatLng) {
        guidanceSessionStorage.currentSession.endPoint = end
        guidanceSessionStorage.updateWithCurrent()
    }

    private val routeCalculatorDispatcher = Executors.newSingleThreadExecutor().asCoroutineDispatcher()
    fun calculateRoute() {
        coroutineScope.launch(routeCalculatorDispatcher) {
            notificationHub.postNotification(
                Notification(
                    Notification.NotificationImage.MAP_GENERAL,
                    "Calculating Route",
                    "Please wait."
                )
            )

            guidanceSessionStorage.currentSession = routeCalculator
                .calculateRoute(guidanceSessionStorage.currentSession)

            guidanceSessionStorage.updateWithCurrent()

            notificationHub.postNotification(Notification(
                Notification.NotificationImage.MAP_GENERAL,
                "Calculated Route",
                "Start Navigation and have a safe trip."
            ))
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
        }
    }

    fun stopGuidance() {

        logger.d(TAG, "Stopping guidance.")

        guidanceJob?.cancel()
        guidanceJob = null

        guidanceSessionStorage.currentSession.terminateGuidance()
        guidanceSessionStorage.updateWithCurrent()

        notificationHub.postNotificationBackground(Notification(
            Notification.NotificationImage.MAP_GENERAL,
            "Ended Guidance"
        ))
    }

    fun repeatLastDirection() {

    }
}

