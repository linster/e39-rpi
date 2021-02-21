package ca.stefanm.ibus.car.bluetooth.blueZdbus

import ca.stefanm.ibus.di.ApplicationModule
import ca.stefanm.ibus.car.bordmonitor.input.IBusInputMessageParser
import ca.stefanm.ibus.car.bordmonitor.input.InputEvent
import ca.stefanm.ibus.car.bordmonitor.menu.painter.TextLengthConstraints
import ca.stefanm.ibus.car.bordmonitor.menu.painter.TitleNMessage
import ca.stefanm.ibus.car.bordmonitor.menu.painter.getAllowedLength
import ca.stefanm.ibus.lib.logging.Logger
import ca.stefanm.ibus.lib.messages.IBusMessage
import ca.stefanm.ibus.configuration.DeviceConfiguration
import ca.stefanm.ibus.car.platform.IBusInputEventListenerService
import ca.stefanm.ibus.car.platform.LongRunningService
import ca.stefanm.ibus.car.platform.Service
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.consumeAsFlow
import javax.inject.Inject
import javax.inject.Named


interface TrackInfoPrinter : Service {
    suspend fun onNewTrackInfo(track : String, artist : String, album : String)
}

class CliTrackInfoPrinter @Inject constructor(
    private val logger: Logger
) : TrackInfoPrinter {
    override fun onCreate() {}
    override fun onShutdown() {}

    override suspend fun onNewTrackInfo(track: String, artist: String, album: String) {
        logger.i("TrackInfo", "New track: $track, $artist, $album")
    }
}

class ScreenTrackInfoPrinter @Inject constructor(
    private val iBusInputMessageParser: IBusInputMessageParser,
    private val cliTrackInfoPrinter: CliTrackInfoPrinter,
    private val deviceConfiguration: DeviceConfiguration,
    private val textLengthConstraints: TextLengthConstraints,
    @Named(ApplicationModule.IBUS_MESSAGE_OUTPUT_CHANNEL) private val messagesOut : Channel<IBusMessage>,
    coroutineScope: CoroutineScope,
    parsingDispatcher: CoroutineDispatcher
) : LongRunningService(coroutineScope, parsingDispatcher), TrackInfoPrinter, IBusInputEventListenerService {

    private var currentTrack = ""
    private var currentArtist = ""
    private var currentAlbum = ""

    companion object {
        const val TRACK_FIELD = 6
        const val ARTIST_FIELD = 5
        const val ALBUM_FIELD = 3
    }

    override suspend fun onNewTrackInfo(track: String, artist: String, album: String) {
        cliTrackInfoPrinter.onNewTrackInfo(track, artist, album)

        currentTrack = track
        currentArtist = artist
        currentAlbum = album

        if (deviceConfiguration.displayDriver == DeviceConfiguration.DisplayDriver.MK4) {
            printMessage(track, 5)
            printMessage(artist, 6)
        }

        if (deviceConfiguration.displayDriver == DeviceConfiguration.DisplayDriver.TV_MODULE) {
            printMessage(track, 6)
            printMessage(artist, 7)
        }
    }

    private suspend fun printMessage(label : String, n : Int) {
        val clearMessage = TitleNMessage(
            label = "".padEnd(length = textLengthConstraints.getAllowedLength(n), padChar = ' '),
            n = n,
            lengthConstraints = textLengthConstraints
        )

        val writeMessage = TitleNMessage(
            label = label
                .take(textLengthConstraints.getAllowedLength(n))
                .padEnd(length = textLengthConstraints.getAllowedLength(n), padChar = ' '),
            n = n,
            lengthConstraints = textLengthConstraints
        )

        messagesOut.send(clearMessage)
        messagesOut.send(writeMessage)
    }

    override fun onCreate() {
        iBusInputMessageParser.addMailbox(this)
        super.onCreate()
    }

    override fun onShutdown() {
        super.onShutdown()
        iBusInputMessageParser.removeMailbox(this)
    }

    override val incomingIbusInputEvents: Channel<InputEvent> = Channel(capacity = Channel.UNLIMITED)

    override suspend fun doWork() {
        incomingIbusInputEvents.consumeAsFlow().collect {
            if (it == InputEvent.ShowRadioStatusScreen) {
                printMessage(currentTrack, TRACK_FIELD)
                printMessage(currentArtist, ARTIST_FIELD)
                printMessage(currentAlbum, ALBUM_FIELD)
            }
        }
    }
}