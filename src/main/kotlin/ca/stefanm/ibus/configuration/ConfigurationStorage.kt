package ca.stefanm.ibus.configuration

import ca.stefanm.ibus.car.platform.ConfigurablePlatform
import ca.stefanm.ibus.di.ApplicationScope
import ca.stefanm.ibus.gui.bluetoothPairing.ui.CurrentDeviceViewer
import ca.stefanm.ibus.gui.menu.navigator.WindowManager
import ca.stefanm.ibus.lib.logging.Logger
import com.uchuhimo.konf.Config
import com.uchuhimo.konf.ConfigSpec
import com.uchuhimo.konf.source.hocon
import com.uchuhimo.konf.source.hocon.toHocon
import java.io.File
import javax.inject.Inject
import javax.inject.Provider

//https://github.com/russhwolf/multiplatform-settings

@ApplicationScope
class ConfigurationStorage @Inject constructor(
    private val configurablePlatform: ConfigurablePlatform,
    private val logger : Logger
) {

    private val TAG = "ConfigurationStorage"

    companion object {
        val homeFolder = System.getProperty("user.home")
        val e39BaseFolder = File(homeFolder, ".e39")
    }

    private val configFile = File(e39BaseFolder, "config.conf")
    private val versionFile = File(e39BaseFolder, "version.conf")

    val config = Config { addSpec(E39Config) }
        .from.hocon.file(configFile, optional = true)

    val versionConfig = Config { addSpec(HmiVersion) }
        .from.hocon.file(versionFile, optional = true)

    init {
        if (!configFile.exists()) {
            config.toHocon.toFile(configFile)
        }
        config.afterSet { item, value ->
            logger.d(TAG, "Setting $item to $value")
            config.toHocon.toFile(configFile)
        }

        if (!versionFile.exists()) {
            versionConfig.toHocon.toFile(versionFile)
        }
    }

    fun saveConfigAsFile(filename : String) {
        config.toHocon.toFile(File(e39BaseFolder, filename))
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

    fun setServiceGroupsOnStartup(
        groupNames : List<String>
    ) {
        logger.d("configurationStorage", "Saving groups to run on startup: $groupNames")
        config[E39Config.CarPlatformConfigSpec._listOfServiceGroupsOnStartup] = groupNames
    }

    fun setBrightnessCompensation(tint : Float) {
        config[E39Config.WindowManagerConfig.brightnessCompensation] = tint
    }
}