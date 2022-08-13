package ca.stefanm.ca.stefanm.ibus.gui.map.guidance

import ca.stefanm.ca.stefanm.ibus.gui.map.poi.PoiRepository
import ca.stefanm.ibus.configuration.ConfigurationStorage
import ca.stefanm.ibus.configuration.E39Config
import ca.stefanm.ibus.di.ApplicationScope
import ca.stefanm.ibus.gui.map.Route
import ca.stefanm.ibus.lib.logging.Logger
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.runBlocking
import javax.inject.Inject

@ExperimentalCoroutinesApi
@ApplicationScope
class GuidanceSessionStorage @Inject constructor(
    private val configurationStorage: ConfigurationStorage,
    private val logger: Logger
) {

    private val TAG = "GuidanceSessionStorage"

    fun getCurrentSessionFlow() : Flow<GuidanceSession> {
        return callbackFlow {


            val handler = E39Config.GuidanceConfig.currentSession.onSet {
                runBlocking { send(GuidanceSession(it)) }
            }

            runBlocking { send(currentSession) }

            awaitClose {
                handler.cancel()
            }

        }
    }


    var currentSession: GuidanceSession = getCurrentSessionFromStorage()
        set(value) {
            field = value
            saveCurrentSession(value)
        }

    fun updateWithCurrent() {
        logger.d(TAG, "updateWithCurrent")
        saveCurrentSession(currentSession)
    }
    private fun getCurrentSessionFromStorage(): GuidanceSession {
        return configurationStorage
            .config[E39Config.GuidanceConfig.currentSession]
            .let { GuidanceSession(it) }
    }

    private fun saveCurrentSession(session: GuidanceSession) {
        configurationStorage.config[E39Config.GuidanceConfig.currentSession] =
            PersistedGuidanceSession.fromSession(session)
    }
}

@ApplicationScope
class BrowsableRouteStorage @Inject constructor() {

    fun suggestRouteName() : String = ""

    fun saveRoute(name : String, route: Route) {

    }
}