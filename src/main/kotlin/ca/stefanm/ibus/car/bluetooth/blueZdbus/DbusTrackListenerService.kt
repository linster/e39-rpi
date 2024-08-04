package ca.stefanm.ibus.car.bluetooth.blueZdbus

import ca.stefanm.ca.stefanm.ibus.car.bluetooth.blueZdbus.FlowDbusConnector
import ca.stefanm.ibus.annotations.services.PlatformServiceInfo
import ca.stefanm.ibus.car.di.ConfiguredCarScope
import ca.stefanm.ibus.car.platform.BluetoothServiceGroup
import ca.stefanm.ibus.lib.logging.Logger
import ca.stefanm.ibus.car.platform.LongRunningService
import com.github.hypfvieh.bluetooth.wrapper.BluetoothDevice
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.channels.sendBlocking
import kotlinx.coroutines.channels.trySendBlocking
import kotlinx.coroutines.flow.*
import org.bluez.MediaPlayer1
import org.freedesktop.DBus
import org.freedesktop.dbus.DBusMap
import org.freedesktop.dbus.connections.impl.DBusConnection
import org.freedesktop.dbus.interfaces.DBusSigHandler
import org.freedesktop.dbus.interfaces.Properties
import org.freedesktop.dbus.types.Variant
import javax.inject.Inject

@PlatformServiceInfo(
    name = "DbusTrackListenerService",
    description = "A service that listens to DBus for track info changes and sends that info to a TrackInfoPrinter"
)
@BluetoothServiceGroup
@ConfiguredCarScope
class DbusTrackListenerService @Inject constructor(
    private val flowDbusConnector: FlowDbusConnector,

    private val logger : Logger,
    private val trackInfoPrinter: TrackInfoPrinter,
    private val coroutineScope: CoroutineScope,
    parsingDispatcher: CoroutineDispatcher
) : LongRunningService(coroutineScope, parsingDispatcher) {

    val TAG = "Track Listener"


//
//    private val bluzUniqueBusId : String?
//        get() = dbusConnection?.getRemoteObject(
//            "org.freedesktop.DBus", "/org/freedesktop/DBus", DBus::class.java
//        )?.GetNameOwner("org.bluez")

    private fun getbluzUniqueBusId(connection: DBusConnection) : String? {
        return connection.getRemoteObject(
                "org.freedesktop.DBus", "/org/freedesktop/DBus", DBus::class.java
            )?.GetNameOwner("org.bluez")
    }

    override fun onShutdown() {
        super.onShutdown()
        //TODO does this actually matter? Do I need this?
//        flowDbusConnector.connection.value?.removeSigHandler(Properties.PropertiesChanged::class.java, bluzUniqueBusId!!, handler)
    }

    private data class NewTrackInfo(val track : String, val artist : String, val album : String)

    private val newTracksChannel : Channel<NewTrackInfo> = Channel(capacity = 128)


    override suspend fun doWork() {
        //http://smartspacestuff.blogspot.com/2016/02/i-got-figurin-out-dbus-bluez.html

        coroutineScope.launch {
            newTracksChannel.consumeEach {
                logger.d(TAG, "Publishing new track $it")
                trackInfoPrinter.onNewTrackInfo(it.track, it.artist, it.album)
            }
        }

        flowDbusConnector.connection
            .filter { it != null }
            .map { it as DBusConnection }
            .map { it to getbluzUniqueBusId(it) }
            .filter { it.second != null }
            .collect { (connection, busId) ->

                logger.d(TAG, "Got a new DBusConnection")
                connection.addSigHandler(Properties.PropertiesChanged::class.java, busId, handler)
                logger.d(TAG, "Registered Signal handler.")
        }
    }

    private val handler = object : DBusSigHandler<Properties.PropertiesChanged> {
        override fun handle(s: Properties.PropertiesChanged?) {
            s?.propertiesChanged?.getOrDefault("Track", null)?.let { newTrack ->
                val newTrackMap = newTrack.value as? DBusMap<String, Variant<*>>
                if (newTrackMap != null) {
                    val track = newTrackMap.getTrackField("Title")
                    val artist = newTrackMap.getTrackField("Artist")
                    val album = newTrackMap.getTrackField("Album")
                    newTracksChannel.trySendBlocking(NewTrackInfo(track, artist, album))
                }
            }
        }
    }

    private fun DBusMap<String, Variant<*>>.getTrackField(field : String) : String {
        return get(field)?.value as? String ?: let { logger.d(TAG, "No $field name") ; "" }
    }


    private fun getRawTrackInfo(dBusConnection: DBusConnection, mediaPlayer: MediaPlayer1) : Map<String, String>{
        return dBusConnection
            .getRemoteObject("org.bluez", mediaPlayer.objectPath, Properties::class.java)
            .Get<DBusMap<String, String>>("org.bluez.MediaPlayer1", "Track")
    }




}