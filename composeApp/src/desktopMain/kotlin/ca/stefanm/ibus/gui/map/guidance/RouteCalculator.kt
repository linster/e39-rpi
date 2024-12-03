package ca.stefanm.ca.stefanm.ibus.gui.map.guidance

import ca.stefanm.ibus.gui.map.guidance.GuidanceSession
import ca.stefanm.ibus.gui.map.Kanata_path
import kotlinx.coroutines.delay
import javax.inject.Inject

interface RouteCalculator {
    suspend fun calculateRoute(guidanceSession: GuidanceSession) : GuidanceSession
}

class DummyRouteCalculator @Inject constructor(

): RouteCalculator {
    override suspend fun calculateRoute(guidanceSession: GuidanceSession): GuidanceSession {
        //This just returns the same route we can see in the MapDebugScreen

        delay(250)

        return guidanceSession.apply {
            route = Kanata_path
        }
    }
}