package ca.stefanm.ibus.car.desktop.input.griffinPowermate

import ca.stefanm.ibus.configuration.ConfigurationStorage
import ca.stefanm.ibus.configuration.E39Config
import javax.inject.Inject

class PowermateSensitivityConfig @Inject constructor(
    private val configurationStorage: ConfigurationStorage
) {

    companion object {
        const val DefaultSkip = 3
    }

    fun setNormalSkip(skip : Int) {
        configurationStorage.config[E39Config.GriffinPowermateSensitivity.normalSkip] = skip
    }

    fun setMapSkip(skip : Int) {
        configurationStorage.config[E39Config.GriffinPowermateSensitivity.mapSkip] = skip
    }

    fun getNormalSkip() : Int {
        return configurationStorage.config[E39Config.GriffinPowermateSensitivity.normalSkip]
    }

    fun getMapSkip() : Int {
        return configurationStorage.config[E39Config.GriffinPowermateSensitivity.mapSkip]
    }
}