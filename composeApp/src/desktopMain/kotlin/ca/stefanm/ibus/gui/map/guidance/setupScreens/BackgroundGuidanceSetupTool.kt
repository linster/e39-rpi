package ca.stefanm.ca.stefanm.ibus.gui.map.guidance.setupScreens

import ca.stefanm.ibus.gui.map.guidance.GuidanceService
import ca.stefanm.ibus.gui.map.guidance.GuidanceSession
import ca.stefanm.ibus.gui.map.places.MapPoi
import ca.stefanm.ibus.gui.menu.notifications.NotificationHub
import ca.stefanm.ibus.lib.logging.Logger
import javax.inject.Inject

class BackgroundGuidanceSetupTool @Inject constructor(
    private val guidanceService: GuidanceService,
    private val logger: Logger,
    private val notificationHub: NotificationHub
) {

    /** Set the poi as a start for the guidance session.
     * Return true if there was a change, false otherwise.
     */
    fun setPoiAsStart(poi : MapPoi) : Boolean = callOnGuidanceSession {
        setStartPoint(poi.location)
    }

    /** Set the poi as a end for the guidance session.
     * Return true if there was a change, false otherwise.
     */
    fun setPoiAsEnd(poi : MapPoi) : Boolean = callOnGuidanceSession {
        setEndPoint(poi.location)
    }

    private fun callOnGuidanceSession(
        body : GuidanceService.() -> Unit
    ) : Boolean {
        guidanceService.stopGuidance()
        guidanceService.clearCurrentSession()
        body.invoke(guidanceService)
        return true
    }
}