package ca.stefanm.ibus.car.di

import ca.stefanm.ibus.car.bluetooth.blueZdbus.CliTrackInfoPrinter
import ca.stefanm.ibus.car.bluetooth.blueZdbus.ScreenTrackInfoPrinter
import ca.stefanm.ibus.car.bluetooth.blueZdbus.TrackInfoPrinter
import ca.stefanm.ibus.car.bordmonitor.input.IBusInputMessageParser
import ca.stefanm.ibus.car.bordmonitor.input.InputEvent
import ca.stefanm.ibus.car.bordmonitor.menu.painter.Mk4NavTextLengthConstraints
import ca.stefanm.ibus.car.bordmonitor.menu.painter.TextLengthConstraints
import ca.stefanm.ibus.car.bordmonitor.menu.painter.TvModuleTextLengthConstraints
import ca.stefanm.ibus.car.platform.ConfigurablePlatform
import ca.stefanm.ibus.car.platform.ConfigurablePlatformServiceRunner
import ca.stefanm.ibus.car.platform.PlatformServiceRunner
import ca.stefanm.ibus.configuration.DeviceConfiguration
import ca.stefanm.ibus.di.ApplicationModule
import ca.stefanm.ibus.lib.hardwareDrivers.CliRelayReaderWriter
import ca.stefanm.ibus.lib.hardwareDrivers.RelayReaderWriter
import ca.stefanm.ibus.lib.hardwareDrivers.RpiRelayReaderWriter
import ca.stefanm.ibus.lib.hardwareDrivers.ibus.JSerialCommsAdapter
import ca.stefanm.ibus.lib.hardwareDrivers.ibus.SerialPortReader
import ca.stefanm.ibus.lib.hardwareDrivers.ibus.SerialPortWriter
import ca.stefanm.ibus.lib.messages.IBusMessage
import dagger.*
import io.ktor.utils.io.core.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import javax.inject.Named
import javax.inject.Scope

@Scope
@Retention
annotation class ConfiguredCarModuleScope


@Subcomponent(modules = [ConfiguredCarModule::class])
@ConfiguredCarModuleScope
interface ConfiguredCarComponent {

    fun legacyPlatformServiceRunner() : PlatformServiceRunner
    fun configurablePlatformServiceRunner() : ConfigurablePlatformServiceRunner
    fun ibusInputMessageParser() : IBusInputMessageParser

    @Subcomponent.Builder
    interface Builder {
        fun build() : ConfiguredCarComponent
        fun configuredCarModule(configuredCarModule: ConfiguredCarModule) : Builder
    }

}

@Module
class ConfiguredCarModule(
    private val deviceConfiguration: DeviceConfiguration
) {


    @Provides
//    @ConfiguredCarModuleScope
    fun provideDeviceConfiguration() : DeviceConfiguration = deviceConfiguration



    @ExperimentalCoroutinesApi
    @Provides
    @ConfiguredCarModuleScope
    fun provideSerialPortReader(jSerialCommsAdapter: JSerialCommsAdapter) : SerialPortReader = jSerialCommsAdapter

    @ExperimentalCoroutinesApi
    @Provides
    @ConfiguredCarModuleScope
    fun provideSerialPortWriter(jSerialCommsAdapter: JSerialCommsAdapter) : SerialPortWriter = jSerialCommsAdapter


    @Provides
    @ConfiguredCarModuleScope
    fun providePairedPhone(deviceConfiguration: DeviceConfiguration) : DeviceConfiguration.PairedPhone = deviceConfiguration.pairedPhone

    @Provides
    @ConfiguredCarModuleScope
    fun provideTrackPrinter(
        deviceConfiguration: DeviceConfiguration,
        screenTrackInfoPrinter: ScreenTrackInfoPrinter,
        cliTrackInfoPrinter: CliTrackInfoPrinter
    ) : TrackInfoPrinter {
        return when(deviceConfiguration.trackInfoPrinter) {
            DeviceConfiguration.TrackInfoPrinterType.CLI -> cliTrackInfoPrinter
            DeviceConfiguration.TrackInfoPrinterType.BMBT -> screenTrackInfoPrinter
        }
    }

    @Provides
    @ConfiguredCarModuleScope
    fun provideTextLengthConstraints(deviceConfiguration: DeviceConfiguration) : TextLengthConstraints {
        return if (deviceConfiguration.displayDriver == DeviceConfiguration.DisplayDriver.TV_MODULE) {
            TvModuleTextLengthConstraints
        } else {
            Mk4NavTextLengthConstraints
        }
    }

    @Provides
    @ConfiguredCarModuleScope
    fun provideRelayReaderWriter(
        deviceConfiguration: DeviceConfiguration,
        cliRelayReaderWriter: CliRelayReaderWriter,
        rpiRelayReaderWriter: RpiRelayReaderWriter
    ) : RelayReaderWriter {
        return if (deviceConfiguration.isPi) {
            rpiRelayReaderWriter
        } else {
            cliRelayReaderWriter
        }
    }

}