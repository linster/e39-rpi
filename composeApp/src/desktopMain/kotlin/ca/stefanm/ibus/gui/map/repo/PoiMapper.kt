package ca.stefanm.ibus.gui.map.repo

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import ca.stefanm.e39maps.v1.PoiIcon
import ca.stefanm.e39maps.v1.PoiIconKt
import ca.stefanm.e39maps.v1.latLng
import ca.stefanm.e39maps.v1.poi
import ca.stefanm.e39maps.v1.poiIcon
import ca.stefanm.ibus.gui.map.poi.PoiRepository.Poi
import ca.stefanm.ibus.resources.Res
import ca.stefanm.ibus.resources.allDrawableResources
import com.javadocmd.simplelatlng.LatLng
import org.jetbrains.compose.resources.DrawableResource
import ca.stefanm.e39maps.v1.LatLng as PbLatLng
import ca.stefanm.e39maps.v1.Poi as PbPoi

/**
 * Conversion between the API's messages (ca.stefanm.e39maps.v1) and the HMI's
 * types (PoiRepository.Poi, simplelatlng's LatLng).
 *
 * Icons: a coloured circle's colour and a bundled icon's tint are 0xAARRGGBB
 * (Color.toArgb(); the proto's uint32 is a Java int with the same bits). A
 * bundled icon travels as its Compose resource name, the key in
 * Res.allDrawableResources ("poi_fuel"); a name this HMI doesn't have (an
 * icon removed, or a newer HMI wrote it) shows as no icon.
 */
object PoiMapper {

    private val drawableNames: Map<DrawableResource, String> by lazy {
        Res.allDrawableResources.entries.associate { (name, resource) -> resource to name }
    }

    fun LatLng.toPb(): PbLatLng = latLng {
        latitude = this@toPb.latitude
        longitude = this@toPb.longitude
    }

    fun PbLatLng.toLatLng(): LatLng = LatLng(latitude, longitude)

    fun Poi.toPb(): PbPoi {
        val source = this
        return poi {
            source.id?.let { id = it }
            name = source.name
            location = source.location.toPb()
            icon = source.icon.toPb()
            visible = source.isVisible
        }
    }

    fun PbPoi.toPoi(): Poi = Poi(
        id = id,
        name = name,
        location = location.toLatLng(),
        icon = icon.toPoiIcon(),
        isVisible = visible,
    )

    fun Poi.PoiIcon.toPb(): PoiIcon = when (this) {
        is Poi.PoiIcon.NoIcon -> poiIcon { none = PoiIcon.NoIcon.getDefaultInstance() }
        is Poi.PoiIcon.ColoredCircle -> poiIcon {
            coloredCircle = PoiIconKt.coloredCircle { argb = color.toArgb() }
        }
        is Poi.PoiIcon.BundledIcon -> {
            val name = drawableNames[drawableResource]
            if (name == null) {
                poiIcon { none = PoiIcon.NoIcon.getDefaultInstance() }
            } else {
                poiIcon {
                    bundled = PoiIconKt.bundledIcon {
                        fileName = name
                        tintArgb = tint.toArgb()
                    }
                }
            }
        }
    }

    fun PoiIcon.toPoiIcon(): Poi.PoiIcon = when (kindCase) {
        PoiIcon.KindCase.COLORED_CIRCLE -> Poi.PoiIcon.ColoredCircle(Color(coloredCircle.argb))
        PoiIcon.KindCase.BUNDLED -> {
            val resource = Res.allDrawableResources[bundled.fileName]
            if (resource == null) {
                Poi.PoiIcon.NoIcon
            } else {
                // 0 = unset: the default tint, white.
                val tint = if (bundled.tintArgb == 0) Color.White else Color(bundled.tintArgb)
                Poi.PoiIcon.BundledIcon(resource, tint)
            }
        }
        else -> Poi.PoiIcon.NoIcon
    }
}
