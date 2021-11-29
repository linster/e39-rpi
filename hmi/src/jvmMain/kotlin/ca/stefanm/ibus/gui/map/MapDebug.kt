package ca.stefanm.ibus.gui.map


import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
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

import ca.stefanm.ibus.logger.Logger
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

    override val size = DpSize(1480.dp, 1000.dp)
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


}