package ca.stefanm.ca.stefanm.ibus.car.audio.nowPlayingReader

import ca.stefanm.ibus.annotations.services.PlatformServiceGroup
import ca.stefanm.ibus.annotations.services.PlatformServiceInfo
import ca.stefanm.ibus.car.bluetooth.blueZdbus.TrackInfoPrinter
import ca.stefanm.ibus.car.di.ConfiguredCarScope
import ca.stefanm.ibus.car.platform.LongRunningService
import ca.stefanm.ibus.configuration.CarPlatformConfiguration
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject



@PlatformServiceGroup(
    name = "RadioListenerServiceGroup",
    description = "Services that listen to radio updates"
)
annotation class RadioListenerServiceGroup

@PlatformServiceGroup(
    name = "DBusTrackInfoNowPlayingServiceGroup",
    description = "A service group for services that listen to the DBus track info and print it out to NowPlaying"
)
annotation class DBusTrackInfoNowPlayingServiceGroup

data class RadioTextFields(
    val t0: String = "",
    val t1: String = "",
    val t2: String = "",
    val t3: String = "",
    val t4: String = "",
    val t5: String = "",
    val t6 : String = ""
)

object NowPlayingTextFieldFlows {
    val radioTextFieldsFlow = MutableStateFlow(RadioTextFields())
}

@PlatformServiceInfo(
    name = "RadioTextFieldReaderService",
    description = "Emits all the incoming radio text messages to a flow"
)
@RadioListenerServiceGroup
@ConfiguredCarScope
class RadioTextFieldReaderService @Inject constructor(
    private val deviceConfiguration : CarPlatformConfiguration,
    coroutineScope: CoroutineScope,
    parsingDispatcher: CoroutineDispatcher
) : LongRunningService(coroutineScope, parsingDispatcher) {

    override suspend fun doWork() {
        //TODO need to listen to the serial port and emit to

        //Also, trim the strings according to car config. See TrackInfoPrinter.kt for examples
        deviceConfiguration.trackInfoPrinter is CarPlatformConfiguration.TrackInfoPrinterType
    }
}