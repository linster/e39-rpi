package ca.stefanm.ibus.car.audio.nowPlayingReader

import ca.stefanm.ibus.annotations.services.PlatformServiceGroup
import ca.stefanm.ibus.annotations.services.PlatformServiceInfo
import ca.stefanm.ibus.car.bordmonitor.menu.painter.TextLengthConstraints
import ca.stefanm.ibus.car.di.ConfiguredCarModule
import ca.stefanm.ibus.car.di.ConfiguredCarScope
import ca.stefanm.ibus.car.platform.LongRunningService
import ca.stefanm.ibus.configuration.CarPlatformConfiguration
import ca.stefanm.ibus.di.ApplicationModule
import ca.stefanm.ibus.lib.messages.IBusMessage
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.*
import javax.inject.Inject
import javax.inject.Named


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
    private val textLengthConstraints: TextLengthConstraints,
    @Named(ApplicationModule.IBUS_MESSAGE_INGRESS) val incomingMessages : MutableSharedFlow<IBusMessage>,
    @Named(ConfiguredCarModule.SERVICE_COROUTINE_SCOPE) coroutineScope: CoroutineScope,
    @Named(ConfiguredCarModule.SERVICE_COROUTINE_DISPATCHER) parsingDispatcher: CoroutineDispatcher
) : LongRunningService(coroutineScope, parsingDispatcher) {

    override suspend fun doWork() {
        getRadioTextFields()
            .map { it.trimToLength() }
            .collect {
                emitRadioTextFields(it)
            }
    }

    fun getRadioTextFields() : Flow<RadioTextFields> {
        // TODO need to replicate the behaviour where the sender needs to clear
        // the existing string... so we need a cachey thing
        return emptyFlow()
    }

    suspend fun emitRadioTextFields(fields : RadioTextFields) {
        NowPlayingTextFieldFlows.radioTextFieldsFlow.emit(
            fields
        )
    }

    fun RadioTextFields.trimToLength() : RadioTextFields {
        return this.copy(
            t0 = this.t0.subSequence(0, textLengthConstraints.AREA_0 - 1).toString(),
            t1 = this.t0.subSequence(0, textLengthConstraints.AREA_0 - 1).toString(),
            t2 = this.t0.subSequence(0, textLengthConstraints.AREA_0 - 1).toString(),
            t3 = this.t0.subSequence(0, textLengthConstraints.AREA_0 - 1).toString(),
            t4 = this.t0.subSequence(0, textLengthConstraints.AREA_0 - 1).toString(),
            t5 = this.t0.subSequence(0, textLengthConstraints.AREA_0 - 1).toString(),
            t6 = this.t0.subSequence(0, textLengthConstraints.AREA_0 - 1).toString(),
        )
    }
}