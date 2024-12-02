package ca.stefanm.ibus.gui.map.poi

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
//import ca.stefanm.e39.navigation.db.NavigationDb
import ca.stefanm.ibus.configuration.ConfigurationStorage
import ca.stefanm.ibus.di.ApplicationScope
import ca.stefanm.ibus.lib.logging.Logger
import com.javadocmd.simplelatlng.LatLng
//import com.squareup.sqldelight.db.SqlDriver
//import com.squareup.sqldelight.runtime.coroutines.asFlow
//import com.squareup.sqldelight.runtime.coroutines.mapToList
//import com.squareup.sqldelight.sqlite.driver.JdbcSqliteDriver
import com.uchuhimo.konf.Config
import com.uchuhimo.konf.ConfigSpec
import com.uchuhimo.konf.source.hocon
import com.uchuhimo.konf.source.hocon.toHocon
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.runBlocking
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@ApplicationScope
class PoiRepository @Inject constructor(
    private val logger : Logger
){

    data class Poi(
        val name : String,
        val location : LatLng,
        val icon : PoiIcon,
        val isVisible : Boolean
    ) {
        sealed class PoiIcon(val type : String) {
            object NoIcon : PoiIcon(type = "NoIcon")
            data class ColoredCircle(val color: Color) : PoiIcon("ColoredCircle")
            data class BundledIcon(val fileName : String, val tint : Color = Color.White) : PoiIcon("BundledIcon")
        }

        fun toSerializablePoi() : SerializablePoi {
            return SerializablePoi(
                name = name,
                location = location.latitude to location.longitude,
                iconType = icon.type,
                iconColor = when (icon) {
                    is PoiIcon.ColoredCircle -> icon.color
                    is PoiIcon.BundledIcon -> icon.tint
                    else -> null
                }.let { it?.toArgb() },
                iconFileName = when (icon) {
                    is PoiIcon.BundledIcon -> icon.fileName
                    else -> null
                },
                isVisible = isVisible
            )
        }
    }

    data class SerializablePoi(
        val name : String,
        val location : Pair<Double, Double>,
        val iconType : String,
        val iconColor : Int?,
        val iconFileName : String?,
        val isVisible : Boolean
    ) {
        fun toPoi() : Poi {
            return Poi(
                name = name,
                location = LatLng(location.first, location.second),
                icon = when (iconType) {
                    "NoIcon" -> Poi.PoiIcon.NoIcon
                    "ColoredCircle" -> Poi.PoiIcon.ColoredCircle(Color(iconColor!!))
                    "BundledIcon" -> Poi.PoiIcon.BundledIcon(fileName = iconFileName!!, Color(iconColor!!))
                    else -> Poi.PoiIcon.NoIcon
                },
                isVisible = isVisible
            )
        }
    }
//    fun PoiTable.toSerializablePoi() : SerializablePoi {
//        return SerializablePoi(
//            name = this.name_string ?: "",
//            location = Pair(latitude_long ?: 0.0,  longitude_long ?: 0.0),
//            iconType = iconType_string ?: "NoIcon",
//            iconColor = iconColor_int,
//            iconFileName = iconFileName_string,
//            isVisible = isVisible ?: false
//        )
//    }
//
//    fun SerializablePoi.toPoiTable() : PoiTable {
//        return PoiTable(
//            name_string = name,
//            latitude_long = location.first,
//            longitude_long = location.second,
//            iconType_string = iconType,
//            iconColor_int = iconColor,
//            iconFileName_string = iconFileName,
//            isVisible = isVisible
//        )
//    }


    fun saveOrUpdatePoi(
        new : Poi
    ) {
//        poiQueries.transaction {
//            val matching = poiQueries.poiExists(name_string = new.name, latitude = new.location.latitude, longitude = new.location.longitude).executeAsOne()
//            logger.d("PoiRepository", "SaveOrUpdatePoi. Matching is $matching")
//            if (matching == 0L) {
//                poiQueries.insertPoi(new.toSerializablePoi().toPoiTable())
//            } else {
//                poiQueries.deletePoi(name_string = new.name, latitude = new.location.latitude, longitude = new.location.longitude)
//                poiQueries.insertPoi(new.toSerializablePoi().toPoiTable())
//            }
//        }
    }

    fun getAllPois() : List<Poi> {
        return emptyList()
//        return poiQueries.selectAll().executeAsList().map { it.toSerializablePoi().toPoi() }
    }

    fun getAllPoisFlow() : Flow<List<Poi>> {
        return flowOf(emptyList())
//        return poiQueries.selectAll()
//            .asFlow().mapToList().map { it.map { it.toSerializablePoi().toPoi() } }.flowOn(Dispatchers.IO)
    }

    fun getAllVisiblePoisFlow() : Flow<List<Poi>> {
        return flowOf(emptyList())
//        return poiQueries.selectAllVisible()
//            .asFlow().mapToList().map { it.map { it.toSerializablePoi().toPoi() } }.flowOn(Dispatchers.IO)
    }

    fun deletePoi(poi: Poi) {
//        poiQueries.deleteByName(name_string = poi.name)
    }

    fun hideAllPois() {
//        poiQueries.hideAllPois()
    }

    fun showAllPois() {
//        poiQueries.showAllPois()
    }

}