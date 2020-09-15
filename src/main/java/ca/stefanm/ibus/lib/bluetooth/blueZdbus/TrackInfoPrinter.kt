package ca.stefanm.ibus.lib.bluetooth.blueZdbus

import ca.stefanm.ibus.di.ApplicationModule
import ca.stefanm.ibus.lib.bordmonitor.input.InputEvent
import ca.stefanm.ibus.lib.bordmonitor.menu.painter.TextLengthConstraints
import ca.stefanm.ibus.lib.bordmonitor.menu.painter.TitleNMessage
import ca.stefanm.ibus.lib.bordmonitor.menu.painter.getAllowedLength
import ca.stefanm.ibus.lib.logging.Logger
import ca.stefanm.ibus.lib.messages.IBusMessage
import ca.stefanm.ibus.lib.platform.DeviceConfiguration
import ca.stefanm.ibus.lib.platform.LongRunningService
import ca.stefanm.ibus.lib.platform.Service
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
    private val cliTrackInfoPrinter: CliTrackInfoPrinter,
    private val deviceConfiguration: DeviceConfiguration,
    private val textLengthConstraints: TextLengthConstraints,
    @Named(ApplicationModule.IBUS_MESSAGE_OUTPUT_CHANNEL) private val messagesOut : Channel<IBusMessage>,
    @Named(ApplicationModule.CHANNEL_INPUT_EVENTS) private val inputEventChannel: Channel<InputEvent>,
    coroutineScope: CoroutineScope,
    parsingDispatcher: CoroutineDispatcher
) : LongRunningService(coroutineScope, parsingDispatcher), TrackInfoPrinter {

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

    override suspend fun doWork() {
        inputEventChannel.consumeAsFlow().collect {
            if (it == InputEvent.ShowRadioStatusScreen) {

                printMessage(currentTrack, TRACK_FIELD)
                printMessage(currentArtist, ARTIST_FIELD)
                printMessage(currentAlbum, ALBUM_FIELD)
            }
        }
    }
}