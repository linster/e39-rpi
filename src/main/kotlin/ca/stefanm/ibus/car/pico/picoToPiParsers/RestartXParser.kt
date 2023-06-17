package ca.stefanm.ca.stefanm.ibus.car.pico.picoToPiParsers

import ca.stefanm.e39.proto.PicoToPiOuterClass
import ca.stefanm.ibus.annotations.services.PlatformServiceInfo
import ca.stefanm.ibus.car.bordmonitor.input.IBusDevice
import ca.stefanm.ibus.car.platform.LongRunningService
import ca.stefanm.ibus.car.platform.PicoToPiParserGroup
import ca.stefanm.ibus.di.ApplicationModule
import ca.stefanm.ibus.lib.logging.Logger
import ca.stefanm.ibus.lib.messages.IBusMessage
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.filter
import javax.inject.Inject
import javax.inject.Named


@PlatformServiceInfo(
    name = "RestartXParser",
    description = "Soft power request from Pico to restart the X server."
)
@PicoToPiParserGroup
class RestartXParser @Inject constructor(
    @Named(ApplicationModule.IBUS_MESSAGE_INGRESS) val incomingMessages : MutableSharedFlow<IBusMessage>,
    private val logger: Logger,
    coroutineScope: CoroutineScope,
    parsingDispatcher: CoroutineDispatcher
) : LongRunningService(coroutineScope, parsingDispatcher){

    override suspend fun doWork() {
        incomingMessages.filter {
            it.sourceDevice == IBusDevice.PICO && it.destinationDevice == IBusDevice.PI
        }.collect {

            val message = try {
                PicoToPiOuterClass.PicoToPi.parser().parseFrom(it.data.toByteArray())
            } catch (e : Throwable) {
                logger.e("LogMessageParser", "Could not parse protobuf", e)
                return@collect
            }

            if (message.messageType == PicoToPiOuterClass.PicoToPi.MessageType.PiSoftPowerRestartX) {
                restartX()
            }
        }
    }

    private fun restartX() {
        logger.w("RestartXParser", "TODO restart X")
    }

}