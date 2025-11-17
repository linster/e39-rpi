package ca.stefanm.ibus.gui.chat

import ca.stefanm.ibus.configuration.ConfigurationStorage
import ca.stefanm.ibus.configuration.E39Config
import ca.stefanm.ibus.lib.logging.Logger
import net.folivo.trixnity.core.model.RoomId
import javax.inject.Inject


// A utility class to save/load the most recently used room
// and DM.
class MostRecentStorage @Inject constructor(
    private val logger: Logger,
    private val configurationStorage: ConfigurationStorage
) {

    companion object {
        const val TAG = "MostRecentStorage"
    }

    fun saveMostRecentRoomId(mostRecentRoomId: RoomId) {
        logger.d(TAG, "saveMostRecentRoomId: $mostRecentRoomId" )
        configurationStorage.config[E39Config.MatrixMostRecentlyUsedConfig.mostRecentRoomId] = mostRecentRoomId.toString()
    }

    fun saveMostRecentDmId(dmId: RoomId) {
        logger.d(TAG, "saveMostRecentDmId: $dmId" )
        configurationStorage.config[E39Config.MatrixMostRecentlyUsedConfig.mostRecentDmId] = dmId.toString()
    }

    fun loadMostRecentRoomId(): RoomId? {
        //Not ideal that this re-implements the roomId serializer
        val full = configurationStorage.config[E39Config.MatrixMostRecentlyUsedConfig.mostRecentRoomId]
        return if (full != null) {
            RoomId(full)
        } else {
            null
        }
    }

    fun loadMostRecentDmId(): RoomId? {
        //Not ideal that this re-implements the roomId serializer
        val full = configurationStorage.config[E39Config.MatrixMostRecentlyUsedConfig.mostRecentDmId]
        return if (full != null) {
            RoomId(full)
        } else {
            null
        }
    }
}