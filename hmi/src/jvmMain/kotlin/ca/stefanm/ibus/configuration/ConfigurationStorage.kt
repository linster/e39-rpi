package ca.stefanm.ibus.configuration

import ca.stefanm.ibus.car.conduit.configuration.CarPlatformConfiguration
import ca.stefanm.ibus.car.platform.ConfigurablePlatform
import ca.stefanm.ibus.di.ApplicationScope
import ca.stefanm.ibus.gui.bluetoothPairing.ui.CurrentDeviceViewer
import ca.stefanm.ibus.logger.Logger
import com.uchuhimo.konf.Config
import com.uchuhimo.konf.source.hocon
import com.uchuhimo.konf.source.hocon.toHocon
import java.io.File
import javax.inject.Inject

//https://github.com/russhwolf/multiplatform-settings

@ApplicationScope
class ConfigurationStorage @Inject constructor(
    private val configurablePlatform: ConfigurablePlatform,
    private val logger : Logger
) {

    private val TAG = "ConfigurationStorage"

    private val homeFolder = System.getProperty("user.home")
    private val e39BaseFolder = File(homeFolder, ".e39")

    private val configFile = File(e39BaseFolder, "config.conf")

    val config = Config { addSpec(E39Config) }
        .from.hocon.file(configFile)


    init {
        config.afterSet { item, value ->
            logger.d(TAG, "Setting $item to $value")
            config.toHocon.toFile(configFile)
        }
    }

    fun setBMBTPairedPhone(
        currentDevice: CurrentDeviceViewer.CurrentDevice
    ) {
        config[E39Config.CarPlatformConfigSpec._pairedPhone] = currentDevice.let {
            CarPlatformConfiguration.PairedPhone(
                friendlyName = it.alias,
                macAddress = it.address
                    .split(':')
                    .map { octet -> octet.toInt(16) }
            )
        }

        configurablePlatform.onNewDeviceConfiguration(
            E39Config.CarPlatformConfigSpec.toCarPlatformConfiguration(
                config
            )
        )
    }
}