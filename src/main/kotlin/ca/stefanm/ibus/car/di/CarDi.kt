package ca.stefanm.ibus.car.di

import ca.stefanm.ca.stefanm.ibus.lib.hardwareDrivers.ibus.SerialListenerDebugService
import ca.stefanm.ca.stefanm.ibus.lib.hardwareDrivers.ibus.SerialWriterDebugService
import ca.stefanm.ca.stefanm.ibus.lib.hardwareDrivers.ibus.SyntheticIBusInputEventDebugLoggerService
import ca.stefanm.ibus.car.tvmodule.NavigationAnnounceService
import ca.stefanm.ibus.annotations.services.PlatformServiceInfo
import ca.stefanm.ibus.car.bluetooth.BluetoothService
import ca.stefanm.ibus.car.bluetooth.blueZdbus.CliTrackInfoPrinter
import ca.stefanm.ibus.car.bluetooth.blueZdbus.ScreenTrackInfoPrinter
import ca.stefanm.ibus.car.bluetooth.blueZdbus.TrackInfoPrinter
import ca.stefanm.ibus.car.bordmonitor.input.IBusInputMessageParser
import ca.stefanm.ibus.car.bordmonitor.input.InputEvent
import ca.stefanm.ibus.car.bordmonitor.menu.painter.Mk4NavTextLengthConstraints
import ca.stefanm.ibus.car.bordmonitor.menu.painter.TextLengthConstraints
import ca.stefanm.ibus.car.bordmonitor.menu.painter.TvModuleTextLengthConstraints
import ca.stefanm.ibus.car.platform.*
import ca.stefanm.ibus.configuration.CarPlatformConfiguration
import ca.stefanm.ibus.di.ApplicationComponent
import ca.stefanm.ibus.di.ApplicationModule
import ca.stefanm.ibus.di.ApplicationScope
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
import dagger.multibindings.ElementsIntoSet
import dagger.multibindings.IntoSet
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import java.lang.annotation.RetentionPolicy
import javax.inject.Named
import javax.inject.Provider
import javax.inject.Scope
import javax.inject.Singleton

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
    fun discoveredServiceSerialListenerDebugService() : SerialListenerDebugService
    fun discoveredServiceSerialWriterDebugService() : SerialWriterDebugService
    fun discoveredServiceSyntheticIBusInputEventDebugLoggerService() : SyntheticIBusInputEventDebugLoggerService
}

@Module
class ConfiguredCarModule(
    private val deviceConfiguration: CarPlatformConfiguration
) {


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
        screenTrackInfoPrinter: ScreenTrackInfoPrinter,
        cliTrackInfoPrinter: CliTrackInfoPrinter
    ) : TrackInfoPrinter {
        return when(deviceConfiguration.trackInfoPrinter) {
            CarPlatformConfiguration.TrackInfoPrinterType.CLI -> cliTrackInfoPrinter
            CarPlatformConfiguration.TrackInfoPrinterType.BMBT -> screenTrackInfoPrinter
        }
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
        return if (deviceConfiguration.isPi) {
            rpiRelayReaderWriter
        } else {
            cliRelayReaderWriter
        }
    }

//    @Provides
//    fun provideAllServices() : Set<DiscoveredServiceGroups.DiscoveredServiceInfo> {
//        return DiscoveredServiceGroups().getAllServiceInfos()
//    }

}
