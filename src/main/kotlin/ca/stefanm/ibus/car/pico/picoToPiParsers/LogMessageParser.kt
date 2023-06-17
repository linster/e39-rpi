package ca.stefanm.ca.stefanm.ibus.car.pico.picoToPiParsers

import ca.stefanm.e39.proto.PicoToPiOuterClass.PicoToPi
import ca.stefanm.ibus.annotations.services.PlatformServiceInfo
import ca.stefanm.ibus.car.bordmonitor.input.IBusDevice
import ca.stefanm.ibus.car.platform.LongRunningService
import ca.stefanm.ibus.car.platform.PicoToPiParserGroup
import ca.stefanm.ibus.di.ApplicationModule
import ca.stefanm.ibus.lib.logging.Logger
import ca.stefanm.ibus.lib.messages.IBusMessage
import com.google.common.annotations.VisibleForTesting
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Named

@PlatformServiceInfo(
    name = "LogMessageParser",
    description = "Forwards Pico log messages to the Pi logger"
)
@PicoToPiParserGroup
class LogMessageParser @Inject constructor(
    @Named(ApplicationModule.IBUS_MESSAGE_INGRESS) val incomingMessages : MutableSharedFlow<IBusMessage>,
    private val logger: Logger,
    coroutineScope: CoroutineScope,
    parsingDispatcher: CoroutineDispatcher
) : LongRunningService(coroutineScope, parsingDispatcher){

    override suspend fun doWork() {
        incomingMessages.filter {
            it.sourceDevice == IBusDevice.PICO && it.destinationDevice == IBusDevice.PI
        }.map { parseLogMessage(it) }
        .collect {
            if (it != null) {
                logger.i("PICO", it)
            }
        }
    }

    @VisibleForTesting
    fun parseLogMessage(raw: IBusMessage) : String? {
        val message = try {
            PicoToPi.parser().parseFrom(raw.data.toByteArray())
        } catch (e : Throwable) {
            logger.e("LogMessageParser", "Could not parse protobuf", e)
            return null
        }

        return if (message.messageType == PicoToPi.MessageType.LogStatement) {
            message.loggerStatement
        } else {
            null
        }
    }

}