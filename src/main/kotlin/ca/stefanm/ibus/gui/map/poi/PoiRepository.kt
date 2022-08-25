package ca.stefanm.ibus.gui.map.poi

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import ca.stefanm.e39.navigation.db.NavigationDb
import ca.stefanm.ibus.configuration.ConfigurationStorage
import ca.stefanm.ibus.di.ApplicationScope
import ca.stefanm.ibus.lib.logging.Logger
import com.javadocmd.simplelatlng.LatLng
import com.squareup.sqldelight.db.SqlDriver
import com.squareup.sqldelight.sqlite.driver.JdbcSqliteDriver
import com.uchuhimo.konf.Config
import com.uchuhimo.konf.ConfigSpec
import com.uchuhimo.konf.source.hocon
import com.uchuhimo.konf.source.hocon.toHocon
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


    private val driver : SqlDriver = JdbcSqliteDriver(
        "jdbc:sqlite:" + ConfigurationStorage.e39BaseFolder.absolutePath + "/poi.sqlite"
    )

    init {
        NavigationDb.Schema.create(driver)
        val database = NavigationDb(driver)
//        PoiQueries.insertPoi()
        logger.d("DBWAT", database.poiQueries.selectAll().executeAsList().toString())


        database.poiQueries.insertPoi(
            PoiTable("foo", 1.0, 2.0, null, null, null)
        )


    }


    companion object {
        private val poiFile = File(ConfigurationStorage.e39BaseFolder, "poi.conf")

        object PoiConfig : ConfigSpec() {
            val savedPois by optional(mutableSetOf<SerializablePoi>())
        }
    }

    private val poiConfig = Config { addSpec(PoiConfig) }
        .from.hocon.file(poiFile, optional = true)

    init {
        if (!poiFile.exists()) {
            poiConfig.toHocon.toFile(poiFile)
        }

        poiConfig.afterSet { item, value ->
            logger.d("PoiRepository", "Settings $item to $value")
            poiConfig.toHocon.toFile(poiFile)
        }

    }

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


    fun saveOrUpdatePoi(
        existing : Poi? = null,
        new : Poi
    ) {

        if (poiConfig[PoiConfig.savedPois].any { it.location == new.toSerializablePoi().location }) {
            //Update
            poiConfig[PoiConfig.savedPois].removeAll(
                poiConfig[PoiConfig.savedPois].filter { it.location == new.toSerializablePoi().location }
            )
            poiConfig[PoiConfig.savedPois].add(new.toSerializablePoi())
        } else {
            poiConfig[PoiConfig.savedPois].add(new.toSerializablePoi())
        }
        poiConfig.toHocon.toFile(poiFile)
    }

    fun getAllPois() : List<Poi> {
        return poiConfig[PoiConfig.savedPois].map { it.toPoi() }.toList()
    }

    fun getAllPoisFlow() : Flow<List<Poi>> {
        return callbackFlow {

            val handler = PoiConfig.savedPois.onSet {
                runBlocking { send(it.map { it.toPoi() }.toList()) }
            }

            runBlocking { send(poiConfig[PoiConfig.savedPois].map { it.toPoi() }) }

            awaitClose {
                handler.cancel()
            }

        }
    }

    fun deletePoi(poi: Poi) {
        poiConfig[PoiConfig.savedPois].remove(poi.toSerializablePoi())
        poiConfig.toHocon.toFile(poiFile)
    }

    fun hideAllPois() {
        val allHidden = poiConfig[PoiConfig.savedPois].map { it.copy(isVisible = false) }
        with (poiConfig[PoiConfig.savedPois]) {
            clear()
            addAll(allHidden)
        }
        poiConfig.toHocon.toFile(poiFile)
    }

    fun showAllPois() {
        val allVisible = poiConfig[PoiConfig.savedPois].map { it.copy(isVisible = true) }
        with (poiConfig[PoiConfig.savedPois]) {
            clear()
            addAll(allVisible)
        }
        poiConfig.toHocon.toFile(poiFile)
    }

    fun toggleVisibilityForPoi(poi: Poi) {
        val existing = poiConfig[PoiConfig.savedPois].find { it.toPoi() == poi } ?: return
        val isCurrentlyVisible = existing.isVisible
        saveOrUpdatePoi(
            existing.toPoi(),
            new = existing.toPoi().copy(isVisible = !isCurrentlyVisible)
        )
        poiConfig.toHocon.toFile(poiFile)
    }

    fun getVisibilityForPoi(poi: Poi) : Flow<Boolean> {
        return getAllPoisFlow().map { list -> list.find { it == poi} }.map { it?.isVisible ?: false }
    }
}