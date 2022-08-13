package ca.stefanm.ibus.gui.map.guidance

import ca.stefanm.ca.stefanm.ibus.gui.map.guidance.GuidanceSession
import ca.stefanm.ibus.gui.map.Kanata_path
import javax.inject.Inject

interface RouteCalculator {
    suspend fun calculateRoute(guidanceSession: GuidanceSession) : GuidanceSession
}

class DummyRouteCalculator @Inject constructor(

): RouteCalculator {
    override suspend fun calculateRoute(guidanceSession: GuidanceSession): GuidanceSession {
        //This just returns the same route we can see in the MapDebugScreen
        return guidanceSession.apply {
            route = Kanata_path
        }
    }
}