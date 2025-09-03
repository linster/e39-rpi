package ca.stefanm.ibus.car.di

import ca.stefanm.ibus.car.ExceptionHandler
import ca.stefanm.ibus.car.audio.nowPlayingReader.RadioTextFieldReaderService
import ca.stefanm.ibus.car.bluetooth.blueZdbus.DbusTrackListenerService
import ca.stefanm.ibus.car.bluetooth.blueZdbus.FlowDbusConnector
import ca.stefanm.ibus.car.pico.picoToPiParsers.*
import ca.stefanm.ibus.car.tvmodule.NavigationAnnounceService
import ca.stefanm.ibus.car.bluetooth.BluetoothEventDispatcherService
import ca.stefanm.ibus.car.bluetooth.BluetoothService
import ca.stefanm.ibus.car.bluetooth.blueZdbus.*
import ca.stefanm.ibus.car.bordmonitor.input.IBusInputMessageParser
import ca.stefanm.ibus.car.bordmonitor.menu.painter.Mk4NavTextLengthConstraints
import ca.stefanm.ibus.car.bordmonitor.menu.painter.TextLengthConstraints
import ca.stefanm.ibus.car.bordmonitor.menu.painter.TvModuleTextLengthConstraints
import ca.stefanm.ibus.car.platform.PlatformServiceList
import ca.stefanm.ibus.lib.hardwareDrivers.ibus.*
import ca.stefanm.ibus.car.platform.*
import ca.stefanm.ibus.configuration.CarPlatformConfiguration
import ca.stefanm.ibus.lib.hardwareDrivers.CliRelayReaderWriter
import ca.stefanm.ibus.lib.hardwareDrivers.CoolingFanController
import ca.stefanm.ibus.lib.hardwareDrivers.RelayReaderWriter
import ca.stefanm.ibus.lib.hardwareDrivers.RpiRelayReaderWriter
import ca.stefanm.ibus.lib.hardwareDrivers.ibus.*
import ca.stefanm.ibus.lib.logging.cli.debugPrinters.IbusInputEventCliPrinter
import ca.stefanm.ibus.lib.logging.cli.debugPrinters.IncomingIbusMessageCliPrinter
import ca.stefanm.ibus.lib.logging.cli.debugPrinters.PlatformMetronomeLogger
import ca.stefanm.ibus.stefane39.TelephoneButtonVideoSwitcherService
import dagger.*
import kotlinx.coroutines.*
import javax.inject.Named
import javax.inject.Scope
import kotlin.coroutines.CoroutineContext

@Scope
@Retention(AnnotationRetention.RUNTIME)
annotation class ConfiguredCarScope

@ConfiguredCarScope
@Subcomponent(modules = [ConfiguredCarModule::class])
interface ConfiguredCarComponent {

    fun inject(configurablePlatform: ConfigurablePlatform)
    fun platformServiceList(): PlatformServiceList
    fun legacyPlatformServiceRunner(): PlatformServiceRunner
    fun configurablePlatformServiceRunner(): ConfigurablePlatformServiceRunner
    fun ibusInputMessageParser(): IBusInputMessageParser
    fun relayReaderWriter(): RelayReaderWriter

    //I would've liked to auto-generate these. These should match ServicesAndServiceGroups.kt
    fun discoveredServiceNavigationAnnounceService() : NavigationAnnounceService
    fun discoveredServiceTelephoneButtonVideoSwitcherService() : TelephoneButtonVideoSwitcherService
    fun discoveredServiceCoolingFanController() : CoolingFanController
    fun discoveredServiceSerialListenerService() : SerialListenerService
    fun discoveredServiceSerialPublisherService() : SerialPublisherService
    fun discoveredServicePlatformMetronomeLogger() : PlatformMetronomeLogger
    fun discoveredServiceIbusInputEventCliPrinter() : IbusInputEventCliPrinter
    fun discoveredServiceIncomingIbusMessageCliPrinter() : IncomingIbusMessageCliPrinter
    fun discoveredServiceIbusInputMessageParser() : IBusInputMessageParser
    fun discoveredServiceBluetoothService() : BluetoothService
    fun discoveredServiceFlowDbusConnector() : FlowDbusConnector
    fun discoveredServiceDbusTrackListenerService() : DbusTrackListenerService
    fun discoveredServiceBluetoothEventDispatcherService() : BluetoothEventDispatcherService
    fun discoveredServiceScreenTrackInfoPrinter() : ScreenTrackInfoPrinter
    fun discoveredDbusTrackListenerService() : DbusTrackListenerService
    fun discoveredServiceCliTrackInfoPrinter() : CliTrackInfoPrinter
    fun discoveredServiceDbusTrackInfoPrinter() : DbusTrackInfoPrinter
    fun discoveredServiceSerialListenerDebugService() : SerialListenerDebugService
    fun discoveredServiceSerialWriterDebugService() : SerialWriterDebugService
    fun discoveredServiceSyntheticIBusInputEventDebugLoggerService() : SyntheticIBusInputEventDebugLoggerService

    fun discoveredServicePicoHeartbeatRequestParser() : HeartbeatRequestParser
    fun discoveredServiceLogMessageParser() : LogMessageParser
    fun discoveredServiceRestartPiParser() : RestartPiParser
    fun discoveredServiceRestartXParser() : RestartXParser
    fun discoveredServiceShutdownPiParser() : ShutdownPiParser
    fun discoveredServiceConfigPushParser() : ConfigPushParser
    fun discoveredServiceRadioTextFieldReaderService() : RadioTextFieldReaderService
    fun discoveredServicePicoHeartbeatResponseParser() : HeartbeatResponseParser
}

@Module
class ConfiguredCarModule(
    private val deviceConfiguration: CarPlatformConfiguration
) {

    companion object {
        const val SERVICE_COROUTINE_SCOPE = "ServiceCoroutineScope"
        const val SERVICE_COROUTINE_CONTEXT = "ServiceCoroutineContext"
        const val SERVICE_COROUTINE_DISPATCHER = "ServiceCoroutineDispatcher"
    }

    @Provides
    @Named(SERVICE_COROUTINE_SCOPE)
    @ConfiguredCarScope
    fun provideServiceCoroutineScope(
        exceptionHandler: ExceptionHandler
    ) : CoroutineScope {
        return CoroutineScope(GlobalScope.coroutineContext + exceptionHandler.handler)
    }

    @Provides
    @Named(SERVICE_COROUTINE_CONTEXT)
    @ConfiguredCarScope
    fun provideServiceCoroutineContext(
        @Named(SERVICE_COROUTINE_SCOPE) scope: CoroutineScope
    ) : CoroutineContext = scope.coroutineContext

    @Provides
    @Named(SERVICE_COROUTINE_DISPATCHER)
    @ConfiguredCarScope
    fun provideServiceCoroutineDispatcher(
        @Named(SERVICE_COROUTINE_SCOPE) scope: CoroutineScope
    ) : CoroutineDispatcher = Dispatchers.IO

    @Provides
    @ConfiguredCarScope
    fun provideDeviceConfiguration() : CarPlatformConfiguration = deviceConfiguration

    @ExperimentalCoroutinesApi
    @Provides
    @ConfiguredCarScope
    fun provideSerialPortReader(jSerialCommsAdapter: JSerialCommsAdapter) : SerialPortReader = jSerialCommsAdapter

    @ExperimentalCoroutinesApi
    @Provides
    @ConfiguredCarScope
    fun provideSerialPortWriter(jSerialCommsAdapter: JSerialCommsAdapter) : SerialPortWriter = jSerialCommsAdapter


    @Provides
    @ConfiguredCarScope
    fun providePairedPhone(deviceConfiguration: CarPlatformConfiguration) : CarPlatformConfiguration.PairedPhone? = deviceConfiguration.pairedPhone

    @Provides
    @ConfiguredCarScope
    fun provideTrackPrinter(
        deviceConfiguration: CarPlatformConfiguration,
        dbusTrackInfoPrinter: DbusTrackInfoPrinter,
        screenTrackInfoPrinter: ScreenTrackInfoPrinter,
        cliTrackInfoPrinter: CliTrackInfoPrinter
    ) : TrackInfoPrinter {
        val right = when(deviceConfiguration.trackInfoPrinter) {
            CarPlatformConfiguration.TrackInfoPrinterType.CLI -> cliTrackInfoPrinter
            CarPlatformConfiguration.TrackInfoPrinterType.BMBT -> screenTrackInfoPrinter
        }
        return CompositeTrackInfoPrinter(listOf(dbusTrackInfoPrinter, right))
    }

    @Provides
    @ConfiguredCarScope
    fun provideTextLengthConstraints(deviceConfiguration: CarPlatformConfiguration) : TextLengthConstraints {
        return if (deviceConfiguration.displayDriver == CarPlatformConfiguration.DisplayDriver.TV_MODULE) {
            TvModuleTextLengthConstraints
        } else {
            Mk4NavTextLengthConstraints
        }
    }

    @Provides
    @ConfiguredCarScope
    fun provideRelayReaderWriter(
        deviceConfiguration: CarPlatformConfiguration,
        cliRelayReaderWriter: CliRelayReaderWriter,
        rpiRelayReaderWriter: RpiRelayReaderWriter
    ) : RelayReaderWriter {
// STEFAN in 2025: I wrote this thinking I'd one day have an i2c relay board
// STEFAN in 2025: on the RPi, and it would turn on a cooling fan. Cooling never
// STEFAN in 2025: turned out to be a problem IRL, and I never did install the i2c pins
// STEFAN in 2025: on the HAT I made, so this doesn't work anyways. The Pi4j has a link error
// STEFAN in 2025: at runtime anyways:
// e39@raspberrypi:~ $ cat hmi.log | grep -a exception
//ERROR : CarServiceUncaughtExceptionHandler / java.lang.UnsatisfiedLinkError:
// 'int com.pi4j.jni.I2C.i2cOpen(java.lang.String)' exception: {java.lang.UnsatisfiedLinkError:
// 'int com.pi4j.jni.I2C.i2cOpen(java.lang.String)'} 'int com.pi4j.jni.I2C.i2cOpen(java.lang.String)' kotlin.Unit
//        return if (deviceConfiguration.isPi) {
//            rpiRelayReaderWriter
//        } else {
//            cliRelayReaderWriter
//        }

        return cliRelayReaderWriter
    }

}
