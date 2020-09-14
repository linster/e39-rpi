package ca.stefanm.ibus.lib.bluetooth

import ca.stefanm.ibus.di.ApplicationModule
import ca.stefanm.ibus.lib.bluetooth.blueZdbus.DbusConnector
import ca.stefanm.ibus.lib.bordmonitor.input.InputEvent
import ca.stefanm.ibus.lib.bordmonitor.menu.painter.TextLengthConstraints
import ca.stefanm.ibus.lib.bordmonitor.menu.painter.TitleNMessage
import ca.stefanm.ibus.lib.bordmonitor.menu.painter.getAllowedLength
import ca.stefanm.ibus.lib.logging.Logger
import ca.stefanm.ibus.lib.messages.IBusMessage
import ca.stefanm.ibus.lib.platform.LongRunningService
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.consumeAsFlow
import kotlinx.coroutines.flow.receiveAsFlow
import org.bluez.MediaControl1
import org.bluez.MediaPlayer1
import javax.inject.Inject
import javax.inject.Named


//https://github.com/aguedes/bluez/blob/master/doc/media-api.txt

class BluetoothService @Inject constructor(
    @Named(ApplicationModule.CHANNEL_INPUT_EVENTS) private val inputEventChannel : Channel<InputEvent>,
    private val onScreenSetupManager: BluetoothOnScreenSetupManager,
    private val dbusConnector: DbusConnector,
    private val logger: Logger,
    coroutineScope: CoroutineScope,
    parsingDispatcher: CoroutineDispatcher
) : LongRunningService(coroutineScope, parsingDispatcher) {

    override fun onCreate() {
        dbusConnector.onCreate()
        super.onCreate()
    }

    override fun onShutdown() {
        dbusConnector.onShutdown()
        super.onShutdown()
    }

    override suspend fun doWork() {

        val pairedPhone = with(onScreenSetupManager) {
            if (!isPhonePaired()) {
                requestBluetoothSetup()
            }
            getPairedPhone()
        }

        val btPhone = dbusConnector.getDevice(pairedPhone.macAddress)
        logger.d("BT", "have phone.")

        val player = btPhone?.dbusConnection?.getRemoteObject("org.bluez", btPhone?.dbusPath + "/player0", MediaPlayer1::class.java)

        player?.Next()
        player?.Next()
        player?.Play()
    }

    private fun dispatchInputEvent(event: InputEvent) {
        when (event) {
            InputEvent.PrevTrack -> TODO()
            InputEvent.NextTrack -> TODO()
        }
    }
}




internal class BluetoothEventDispatcherService @Inject constructor(
    @Named(ApplicationModule.CHANNEL_INPUT_EVENTS) private val inputEventChannel : Channel<InputEvent>,
    coroutineScope: CoroutineScope,
    parsingDispatcher: CoroutineDispatcher
) : LongRunningService(coroutineScope, parsingDispatcher) {

    var mediaPlayer1 : MediaPlayer1? = null

    override suspend fun doWork() {
        inputEventChannel.receiveAsFlow().collect {
            dispatchInputEvent(it)
        }
    }

    private fun dispatchInputEvent(event: InputEvent) {
        when (event) {
            InputEvent.PrevTrack -> mediaPlayer1?.Previous()
            InputEvent.NextTrack -> mediaPlayer1?.Next()
        }
    }
}





interface TrackInfoPrinter {
    suspend fun onNewTrackInfo(track : String, artist : String, album : String)
}

class CliTrackInfoPrinter @Inject constructor(
    private val logger: Logger
) : TrackInfoPrinter {
    override suspend fun onNewTrackInfo(track: String, artist: String, album: String) {
        logger.i("TrackInfo", "New track: $track, $artist, $album")
    }
}

class ScreenTrackInfoPrinter @Inject constructor(
    private val cliTrackInfoPrinter: CliTrackInfoPrinter,
    private val textLengthConstraints: TextLengthConstraints,
    @Named(ApplicationModule.IBUS_MESSAGE_OUTPUT_CHANNEL) private val messagesOut : Channel<IBusMessage>
) : TrackInfoPrinter {
    override suspend fun onNewTrackInfo(track: String, artist: String, album: String) {
        cliTrackInfoPrinter.onNewTrackInfo(track, artist, album)

        printMessage(track, 3)
        printMessage(artist, 4)
        printMessage(album, 5)
    }

    private suspend fun printMessage(label : String, n : Int) {
        val clearMessage = TitleNMessage(
            label = "".padEnd(length = textLengthConstraints.getAllowedLength(n), padChar = ' '),
            n = n,
            lengthConstraints = textLengthConstraints
        )

        val writeMessage = TitleNMessage(
            label = label.padEnd(length = textLengthConstraints.getAllowedLength(n), padChar = ' '),
            n = n,
            lengthConstraints = textLengthConstraints
        )

        messagesOut.send(clearMessage)
        messagesOut.send(writeMessage)
    }
}