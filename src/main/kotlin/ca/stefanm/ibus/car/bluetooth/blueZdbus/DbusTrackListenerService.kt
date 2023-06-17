package ca.stefanm.ibus.car.bluetooth.blueZdbus

import ca.stefanm.ibus.lib.logging.Logger
import ca.stefanm.ibus.car.platform.LongRunningService
import com.github.hypfvieh.bluetooth.wrapper.BluetoothDevice
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.channels.sendBlocking
import kotlinx.coroutines.channels.trySendBlocking
import kotlinx.coroutines.delay
import kotlinx.coroutines.yield
import org.bluez.MediaPlayer1
import org.freedesktop.DBus
import org.freedesktop.dbus.DBusMap
import org.freedesktop.dbus.connections.impl.DBusConnection
import org.freedesktop.dbus.interfaces.DBusSigHandler
import org.freedesktop.dbus.interfaces.Properties
import org.freedesktop.dbus.types.Variant
import javax.inject.Inject

class DbusTrackListenerService @Inject constructor(
    private val dbusConnector: DbusConnector,
    private val dbusReconnector: DbusReconnector,
    private val logger : Logger,
    private val trackInfoPrinter: TrackInfoPrinter,
    coroutineScope: CoroutineScope,
    parsingDispatcher: CoroutineDispatcher
) : LongRunningService(coroutineScope, parsingDispatcher) {

    val TAG = "Track Listener"

    var dbusConnection : DBusConnection? = null
    var btPhone : BluetoothDevice? = null
    var mediaPlayer1 : MediaPlayer1? = null

    private val bluzUniqueBusId : String?
        get() = dbusConnection?.getRemoteObject(
            "org.freedesktop.DBus", "/org/freedesktop/DBus", DBus::class.java
        )?.GetNameOwner("org.bluez")

    override fun onShutdown() {
        super.onShutdown()
        if (isConnectedToDBus()) {
            dbusConnection?.removeSigHandler(Properties.PropertiesChanged::class.java, bluzUniqueBusId!!, handler)
        }
    }

    private data class NewTrackInfo(val track : String, val artist : String, val album : String)

    private val newTracksChannel : Channel<NewTrackInfo> = Channel()


    override suspend fun doWork() {
        while (!isConnectedToDBus()) {
            delay(5 * 1000)
            yield()
        }

        logger.d(TAG, "Connected to DBus")
        //http://smartspacestuff.blogspot.com/2016/02/i-got-figurin-out-dbus-bluez.html
        dbusConnection?.addSigHandler(Properties.PropertiesChanged::class.java, bluzUniqueBusId!!, handler)

        logger.d(TAG, "Registered Signal handler.")

        newTracksChannel.consumeEach {
            logger.d(TAG, "Publishing new track $it")
            trackInfoPrinter.onNewTrackInfo(it.track, it.artist, it.album)
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

    private fun isConnectedToDBus() : Boolean = dbusConnection != null
            && btPhone != null
            && mediaPlayer1 != null
            && dbusConnection?.isConnected == true


    private fun getRawTrackInfo(dBusConnection: DBusConnection, mediaPlayer: MediaPlayer1) : Map<String, String>{
        return dBusConnection
            .getRemoteObject("org.bluez", mediaPlayer.objectPath, Properties::class.java)
            .Get<DBusMap<String, String>>("org.bluez.MediaPlayer1", "Track")
    }




}