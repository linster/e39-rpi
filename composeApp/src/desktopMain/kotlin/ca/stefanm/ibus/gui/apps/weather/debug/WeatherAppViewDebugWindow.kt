package ca.stefanm.ca.stefanm.ibus.gui.apps.weather.debug

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.WindowScope
import ca.stefanm.ibus.gui.debug.windows.EnumColumn
import ca.stefanm.ibus.gui.debug.windows.NestingCard
import ca.stefanm.ibus.gui.debug.windows.NestingCardHeader
import ca.stefanm.ibus.gui.menu.navigator.WindowManager
import ca.stefanm.ibus.lib.logging.Logger
import javax.inject.Inject

class WeatherAppViewDebugWindow @Inject constructor(
    private val logger: Logger
) : WindowManager.E39Window {
    override val title: String = "Weather App View Debug Window"
    override val size: DpSize = DpSize(1800.dp, 1200.dp)
    override val tag: Any = this

    override val defaultPosition = WindowManager.E39Window.DefaultPosition.ANYWHERE

    override fun content(): @Composable (WindowScope.() -> Unit) = {
        // https://github.com/michaeldavie/env_canada
        // https://github.com/breezy-weather/breezy-weather-icon-packs/blob/main/README.md

        val model = remember { WeatherCardModel() }
        WeatherCard(
            "Today",
            model
        )
    }

    // Need to set the weather on the repo
    // Need to set the weather for a particular day.
    // Make a Weather Card Model

    @Stable
    class WeatherCardModel {
        var infographic by mutableStateOf(InfoGraphic.SUN)
        enum class InfoGraphic {
            SUN,
            CLOUD,
            MOON
        }
    }

    @Composable
    fun WeatherCard(
        title : String,
        model : WeatherCardModel
    ) {
        NestingCard {
            NestingCardHeader(title)
            EnumColumn(
                model.infographic,
                { model.infographic = it}
            )
        }
    }

}