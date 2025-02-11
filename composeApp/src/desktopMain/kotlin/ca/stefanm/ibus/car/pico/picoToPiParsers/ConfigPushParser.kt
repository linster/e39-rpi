package ca.stefanm.ibus.car.pico.picoToPiParsers

import ca.stefanm.ibus.lib.hardwareDrivers.ibus.IbusCommsDebugMessage
import ca.stefanm.e39.proto.PicoToPiOuterClass
import ca.stefanm.e39.proto.PicoToPiOuterClass.PicoToPi
import ca.stefanm.e39.proto.configMessageOrNull
import ca.stefanm.ibus.annotations.services.PlatformServiceInfo
import ca.stefanm.ibus.car.bordmonitor.input.IBusDevice
import ca.stefanm.ibus.car.di.ConfiguredCarModule
import ca.stefanm.ibus.car.platform.LongRunningService
import ca.stefanm.ibus.car.platform.PicoToPiParserGroup
import ca.stefanm.ibus.configuration.ConfigurationStorage
import ca.stefanm.ibus.configuration.HmiVersion
import ca.stefanm.ibus.di.ApplicationModule
import ca.stefanm.ibus.lib.logging.Logger
import ca.stefanm.ibus.lib.messages.IBusMessage
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.filter
import java.time.Instant
import javax.inject.Inject
import javax.inject.Named

@PlatformServiceInfo(
    name = "ConfigPushParser",
    description = "Handles config push messages from the pico"
)
@PicoToPiParserGroup
class ConfigPushParser @Inject constructor(
    @Named(ApplicationModule.IBUS_MESSAGE_INGRESS) val incomingMessages : MutableSharedFlow<IBusMessage>,
    @Named(ApplicationModule.IBUS_COMMS_DEBUG_CHANNEL) private val commsDebugChannel : MutableSharedFlow<IbusCommsDebugMessage>,
    private val configurationStorage: ConfigurationStorage,
    private val logger: Logger,
    @Named(ConfiguredCarModule.SERVICE_COROUTINE_SCOPE) private val coroutineScope: CoroutineScope,
    @Named(ConfiguredCarModule.SERVICE_COROUTINE_DISPATCHER) parsingDispatcher: CoroutineDispatcher
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

            if (message.messageType == PicoToPi.MessageType.ConfigStatusResponse) {
                onConfigRecieved(it, message)
            }
        }
    }

    private suspend fun onConfigRecieved(raw : IBusMessage, parsed: PicoToPi) {
        logger.i("ConfigPushParser", "Got new config")

        val TAG = "ConfigPushParser"
        logger.i(TAG, "rpiFwGitCommitHash: ${parsed.configMessageOrNull?.rpiFwGitCommitHash}")
        logger.i(TAG, "isInitialized: ${parsed.configMessageOrNull?.isInitialized}")
        logger.i(TAG, "aspectRatio: ${parsed.configMessageOrNull?.aspectRatio}")
        logger.i(TAG, "alwaysTurnOnRpiOnStatup: ${parsed.configMessageOrNull?.alwaysTurnOnRpiOnStatup}")
        logger.i(TAG, "enabledMaxLogLevelForIbusLog: ${parsed.configMessageOrNull?.enabledMaxLogLevelForIbusLog}")
        logger.i(TAG, "enabledMaxLogLevelForPrintfLog: ${parsed.configMessageOrNull?.enabledMaxLogLevelForPrintfLog}")
        logger.i(TAG, "rpiFwGitCommitHash: ${parsed.configMessageOrNull?.rpiFwGitCommitHash}")
        logger.i(TAG, "scanProgramOnBoot: ${parsed.configMessageOrNull?.scanProgramOnBoot}")
        logger.i(TAG, "sendBMBTEncodingPacketOnBootup: ${parsed.configMessageOrNull?.sendBMBTEncodingPacketOnBootup}")
        logger.i(TAG, "videoSourceOnBoot: ${parsed.configMessageOrNull?.videoSourceOnBoot}")

//        logger.i("ConfigPushParser", parsed.configMessageOrNull)
        commsDebugChannel.emit(IbusCommsDebugMessage.IncomingMessage.PicoToPiMessage(
            Instant.now(),
            raw,
            parsed
        ))

        parsed.configMessageOrNull?.rpiFwGitCommitHash?.let {
            configurationStorage.versionConfig[HmiVersion.fwHash] = it
        }

    }

}