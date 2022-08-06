package ca.stefanm.ca.stefanm.ibus.gui.map.poi

import androidx.compose.ui.graphics.Color
import ca.stefanm.ibus.configuration.ConfigurationStorage
import ca.stefanm.ibus.di.ApplicationScope
import com.javadocmd.simplelatlng.LatLng
import com.uchuhimo.konf.Config
import com.uchuhimo.konf.ConfigSpec
import com.uchuhimo.konf.source.hocon
import com.uchuhimo.konf.source.hocon.toHocon
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@ApplicationScope
class PoiRepository @Inject constructor(){

    companion object {
        private val poiFile = File(ConfigurationStorage.e39BaseFolder, "poi.conf")

        object PoiConfig : ConfigSpec() {
            val savedPois by optional(mutableSetOf<Poi>())

            val visiblePois by optional(mutableSetOf<Poi>())
        }
    }

    private val poiConfig = Config { addSpec(PoiConfig) }.from.hocon.file(poiFile, optional = true)

    init {
        if (!poiFile.exists()) {
            poiConfig.toHocon.toFile(poiFile)
        }

        poiConfig.afterSet { item, value ->
            poiConfig.toHocon.toFile(poiFile)
        }
    }

    class Poi(
        val name : String,
        val location : LatLng,
        val icon : PoiIcon,
    ) {
        sealed interface PoiIcon {
            object NoIcon : PoiIcon
            data class ColoredCircle(val color: Color) : PoiIcon
            data class BundledIcon(val fileName : String, val tint : Color = Color.White) : PoiIcon
        }
    }


    fun saveOrUpdatePoi(
        existing : Poi? = null,
        new : Poi
    ) {
        if (existing != null && poiConfig[PoiConfig.savedPois].contains(existing)) {
            //Update
            poiConfig[PoiConfig.savedPois].remove(existing)
            poiConfig[PoiConfig.savedPois].add(new)
        } else {
            poiConfig[PoiConfig.savedPois].add(new)
        }
    }

    fun getAllPois() : List<Poi> {
        return poiConfig[PoiConfig.savedPois].toList()
    }

    fun deletePoi(poi: Poi) {
        poiConfig[PoiConfig.savedPois].remove(poi)
    }

}