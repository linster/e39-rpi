package ca.stefanm.ibus.gui.map


import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.WindowScope
import androidx.compose.ui.window.WindowSize
import ca.stefanm.ibus.di.DaggerApplicationComponent
import ca.stefanm.ibus.gui.debug.windows.NestingCard
import ca.stefanm.ibus.gui.debug.windows.NestingCardHeader
import ca.stefanm.ibus.gui.map.widget.ExtentCalculator
import ca.stefanm.ibus.gui.map.widget.MapScale
import ca.stefanm.ibus.gui.map.widget.MapScaleWidget
import ca.stefanm.ibus.gui.menu.navigator.WindowManager

import ca.stefanm.ibus.lib.logging.Logger
import com.ginsberg.cirkle.circular
import com.javadocmd.simplelatlng.LatLng
import com.javadocmd.simplelatlng.LatLngTool
import com.javadocmd.simplelatlng.util.LengthUnit
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.combine
import org.jxmapviewer.viewer.GeoPosition
import java.awt.Desktop
import java.net.URI
import javax.inject.Inject
import kotlin.math.roundToInt

class MapDebug @Inject constructor(
    private val logger: Logger
) : WindowManager.E39Window {

    override val tag: Any
        get() = this

    override val size = DpSize(1780.dp, 1200.dp)
    override val title = "Map Debug"
    override val defaultPosition: WindowManager.E39Window.DefaultPosition
        get() = WindowManager.E39Window.DefaultPosition.ANYWHERE

    override fun content(): @Composable WindowScope.() -> Unit = {
        windowContent()
    }

    @Composable
    fun windowContent() {

        val mapReportedCenter = remember { mutableStateOf(LatLng(0.0, 0.0))}

        val overlayProperties = remember { mutableStateOf(OverlayProperties(
            centerCrossHairsVisible = false,
            mapScaleVisible = false,
            gpsReceptionIconVisible = false
        )) }

        val extents = remember { mutableStateOf(Extents(
            center = GeoPosition(0.0, 0.0),
            mapScale = MapScale.KILOMETERS_25
        )) }

        Row {
            Box(modifier = Modifier.width(400.dp)) {
                manipulator(
                    onOverlayPropertiesChanged = {
                        overlayProperties.value = it
                    },
                    onExtentsChanged = {
                        extents.value = it
                    },
                    mapReportedCenter = mapReportedCenter.value
                )
            }

            Column {

                Box(
                    modifier = Modifier
                        .height(468.dp)
                        .width(800.dp)
                        .border(2.dp, Color.Red)
                ) {
                    MapViewer(
                        overlayProperties = overlayProperties.value,
                        extents = extents.value,
                        onCenterPositionUpdated = {
                            mapReportedCenter.value = LatLng(it.latitude, it.longitude)
                        }
                    )
                }

                NestingCard {
                    NestingCardHeader("Tile Image tester")

                    RawTileGrid(
                        2, 3,
                        4, 4,
                        0
                    )
                }
            }

            NestingCard {
                NestingCardHeader("POI")
                OverlayManipulator(
                    onPoisChanged = { new ->
                        overlayProperties.value = overlayProperties.value.copy(poiOverlay = new)
                    },
                    onRoutePathChanged = {
                        overlayProperties.value = overlayProperties.value.copy(route = it)
                    }
                )
            }
        }
    }

    @Composable
    fun manipulator(
        onOverlayPropertiesChanged : (new : OverlayProperties) -> Unit,
        onExtentsChanged : (new : Extents) -> Unit,

        //The center the map reports.
        mapReportedCenter : LatLng
    ) {

        val zoom = remember { mutableStateOf(MapScale.METERS_100) }
        val mapCenter = remember { mutableStateOf(GeoPosition(0.0, 0.0)) }

        LaunchedEffect(zoom, mapCenter) {
            combine(
                snapshotFlow { zoom.value },
                snapshotFlow { mapCenter.value }
            ) { zoom, center ->
                Extents(
                    center = center,
                    mapScale = zoom
                )
            }.collect {
                onExtentsChanged(it)
            }
        }


        Column {
            NestingCard {
                NestingCardHeader("Tile Cache")
                Button(onClick = {
                    DaggerApplicationComponent.create().tileCacheClearer().clearCache()
                }) { Text("Clear")}
            }
            NestingCard {
                NestingCardHeader("Map Control")

                NestingCard {
                    NestingCardHeader("Parsed Zoom")

                    Text("Zoom: ${zoom.value}")
                    Slider(
                        value = MapScale.values().indexOf(zoom.value).toFloat(),
                        onValueChange = {
                            logger.d("mapDebug", "newZoom: $it")
                            zoom.value = MapScale.values().toList().circular()[it.roundToInt()] },
                        valueRange = 0F.rangeTo(MapScale.values().size.toFloat()),
                        steps = MapScale.values().size
                    )
                }

                NestingCard {
                    NestingCardHeader("Raw lat-lon")

                    Row {
                        Button(
                            onClick = {mapCenter.value = GeoPosition(0.0, 0.0) }
                        ) { Text("0, 0")}

                        Button(
                            onClick = {
                                mapCenter.value = GeoPosition(45.3154699,-75.9194058)
                            }
                        ) { Text("Kanata")}

                        Button(
                            onClick = {
                                mapCenter.value = GeoPosition(51.031263,-114.072014)
                            }
                        ) { Text("Calgary")}
                    }
                }

                NestingCard {
                    NestingCardHeader("Pan")

                    Row {
                        Button(onClick = {
                            mapCenter.value = ExtentCalculator.newMapCenterOnPan(
                                mapCenter.value.let { LatLng(it.latitude, it.longitude) },
                                zoom.value,
                                LatLngTool.Bearing.WEST,
                            ).let { GeoPosition(it.latitude, it.longitude) }
                        }) { Text("<--")}
                        Button(onClick = {
                            mapCenter.value = ExtentCalculator.newMapCenterOnPan(
                                mapCenter.value.let { LatLng(it.latitude, it.longitude) },
                                zoom.value,
                                LatLngTool.Bearing.EAST,
                            ).let { GeoPosition(it.latitude, it.longitude) }
                        }) { Text("-->")}
                    }

                    Row {
                        Button(onClick = {
                            mapCenter.value = ExtentCalculator.newMapCenterOnPan(
                                mapCenter.value.let { LatLng(it.latitude, it.longitude) },
                                zoom.value,
                                LatLngTool.Bearing.NORTH,
                            ).let { GeoPosition(it.latitude, it.longitude) }
                        }) { Text("^")}
                        Button(onClick = {
                            mapCenter.value = ExtentCalculator.newMapCenterOnPan(
                                mapCenter.value.let { LatLng(it.latitude, it.longitude) },
                                zoom.value,
                                LatLngTool.Bearing.SOUTH,
                            ).let { GeoPosition(it.latitude, it.longitude) }
                        }) { Text("V")}
                    }

                }
            }

            NestingCard {
                NestingCardHeader("Overlays")

                val showGpsReceptionIndicator = remember { mutableStateOf(false)}
                val showMapScale = remember { mutableStateOf(false)}
                val showMapCrosshairs = remember { mutableStateOf(false)}

                LaunchedEffect(showGpsReceptionIndicator, showMapScale, showMapCrosshairs) {
                    combine(
                        snapshotFlow { showGpsReceptionIndicator.value },
                        snapshotFlow { showMapScale.value },
                        snapshotFlow { showMapCrosshairs.value }
                    ) { showGpsReceptionIndicator, mapScale, showMapCrosshairs ->
                        OverlayProperties(
                            centerCrossHairsVisible = showMapCrosshairs,
                            mapScaleVisible = mapScale,
                            gpsReceptionIconVisible = showGpsReceptionIndicator
                        )
                    }.collect {
                        onOverlayPropertiesChanged(it)
                    }
                }

                val mapScaleZoom = remember { mutableStateOf(MapScale.METERS_50) }


                Row {
                    Checkbox(
                        checked = showGpsReceptionIndicator.value,
                        onCheckedChange = { showGpsReceptionIndicator.value = it }
                    )
                    Text("GpsReception indicator")
                }

                Row {
                    Checkbox(
                        checked = showMapScale.value,
                        onCheckedChange = { showMapScale.value = it }
                    )
                    Text("Map Scale: ${mapScaleZoom.value.name}")
                }

                Row {
                    Button(onClick = {
                        val currentIndex = MapScale.values().indexOf(mapScaleZoom.value)
                        mapScaleZoom.value = MapScale.values().toList().circular()[currentIndex - 1]
                    }) { Text("Zoom out") }

                    Button(onClick = {
                        val currentIndex = MapScale.values().indexOf(mapScaleZoom.value)
                        mapScaleZoom.value = MapScale.values().toList().circular()[currentIndex + 1]
                    }) { Text("Zoom in") }
                }

                NestingCard {
                    MapScaleWidget(
                        mapScaleZoom.value,
                        GeoPosition(0.0, 0.0)
                    )
                }

                Row {
                    Checkbox(
                        checked = showMapCrosshairs.value,
                        onCheckedChange = { showMapCrosshairs.value = it }
                    )
                    Text("Picker Cross-hairs")

                    Spacer(Modifier.width(16.dp))

                    val center = remember { mutableStateOf(GeoPosition(0.0, 0.0)) }

                    Button(onClick = {

                    }) { Text("Query Center : (${center.value.latitude}, ${center.value.longitude})")}
                }
            }

            NestingCard {
                NestingCardHeader("Center error")

                Row {
                    Button(
                        onClick = {
                            Desktop.getDesktop().browse(
                                URI.create(
                                    "http://maps.google.com/maps?q=loc:${mapCenter.value.latitude},${mapCenter.value.longitude}"
                                )
                            )
                        }
                    ) {
                        Text("Requested Center")
                    }
                    Text("${mapCenter.value.latitude} ${mapCenter.value.longitude}")
                }
                Row {
                    Button(
                        onClick = {
                            Desktop.getDesktop().browse(
                                URI.create(
                                    "http://maps.google.com/maps?q=loc:${mapReportedCenter.latitude},${mapReportedCenter.longitude}"
                                )
                            )
                        }
                    ) {
                        Text("Map Reported Center")
                    }
                    Text("${mapReportedCenter.latitude} ${mapReportedCenter.longitude}")
                }
                Row {
                    Text(
                        "Distance (m): ${
                            LatLngTool.distance(
                                LatLng(mapCenter.value.latitude, mapCenter.value.longitude),
                                mapReportedCenter,
                                LengthUnit.METER
                            )
                        }")
                }

                Row {
                    Button(
                        onClick = {
                            mapCenter.value = GeoPosition(mapReportedCenter.latitude, mapReportedCenter.longitude)
                        }
                    ) { Text("Recenter on Reported Center")}
                }
            }
        }
    }


    @Composable
    fun OverlayManipulator(
        onPoisChanged : (new : PoiOverlay) -> Unit,
        onRoutePathChanged : (new : Route?) -> Unit,
    ) {

        val pois = remember { mutableStateOf(PoiOverlay(pois = emptyList())) }

        LaunchedEffect(pois.value) {
            onPoisChanged(pois.value)
        }

        NestingCard {
            NestingCardHeader("Fixed POI")

            NestingCard {
                pois.value.pois.forEach {
                    Column(Modifier
                        .padding(8.dp)
                        .border(2.dp, Color.Black)
                    ) {
                        Text("label ${it.label}")
                        Text("position ${it.position}")
                        Text("icon ${it.icon}")
                        Box(Modifier.size(16.dp)) {
                            it.icon()
                        }
                    }
                }
            }

            fun addPoiToList(poi : PoiOverlay.Poi) {
                val newList = pois.value.pois.toMutableList().also {
                    it.add(poi)
                }

                pois.value = pois.value.copy(pois = newList)
            }

            Row {
                Button(onClick = {
                    addPoiToList(
                        PoiOverlay.Poi(
                            label = "Kanata",
                            position = LatLng(45.315467999999996, -75.919399),
                            icon = PoiOverlay.Poi.ICON_BLUE_CIRCLE
                        )
                    )
                }) {
                    Text("Add Kanata Center")
                }
                Button(onClick = {
                    addPoiToList(
                        PoiOverlay.Poi(
                            label = "Canadian Tire",
                            position = LatLng(45.307368,-75.91812399999999),
                            icon = PoiOverlay.Poi.ICON_BLUE_CIRCLE
                        )
                    )
                }) {
                    Text("Add Canadian Tire")
                }
                Button(onClick = {
                    pois.value = PoiOverlay(emptyList())
                }) {
                    Text("Clear POI List")
                }
            }
        }

        NestingCard {
            NestingCardHeader("Route Path")

            NestingCard {
                Button(onClick = { onRoutePathChanged(null) }) { Text("Clear")}
                Button(onClick = {
                    onRoutePathChanged(
                        Route(
                            path = Kanata_path,
                            color = Color.Blue,
                            stroke = Stroke(width = 8F)
                    ))
                }) { Text("Add Kanata Loop (Blue)")}
                Button(onClick = {
                    onRoutePathChanged(
                        Route(
                            path = Kanata_path,
                            color = Color.Magenta,
                            stroke = Stroke(width = 8F)
                    ))
                }) { Text("Add Kanata Loop (Red)")}
            }
        }
    }
}

val Kanata_path = listOf(
    LatLng(45.31599199535223, -75.91989904925909),
    LatLng(45.31668722958348, -75.91999402998448),
    LatLng(45.31772513748084, -75.91970829239939),
    LatLng(45.3194084109557, -75.91874393299521),
    LatLng(45.32023138257964, -75.91860052070723),
    LatLng(45.320969173934, -75.91874702039361),
    LatLng(45.32173958116149, -75.91898520947589),
    LatLng(45.32227827257804, -75.91760220675201),
    LatLng(45.32245534122872, -75.91566843411272),
    LatLng(45.32303547637337, -75.91468806647295),
    LatLng(45.32385970494828, -75.91406512881512),
    LatLng(45.32446364373779, -75.9135762350286),
    LatLng(45.32504811347403, -75.91249361333639),
    LatLng(45.32514613838691, -75.91150636779972),
    LatLng(45.32479752451673, -75.91043493930178),
    LatLng(45.32346788409338, -75.90895443429137),
    LatLng(45.32261021800634, -75.9074955513106),
    LatLng(45.32180326616849, -75.90693159696134),
    LatLng(45.32126471215737, -75.90602673401212),
    LatLng(45.32034835430682, -75.90555126119544),
)