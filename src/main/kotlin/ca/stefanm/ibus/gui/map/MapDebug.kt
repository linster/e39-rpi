package ca.stefanm.ibus.gui.map


import androidx.compose.desktop.Window
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import ca.stefanm.ibus.lib.logging.Logger
import com.ginsberg.cirkle.circular
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.combine
import org.jxmapviewer.viewer.GeoPosition
import javax.inject.Inject
import kotlin.math.roundToInt

class MapDebug @Inject constructor(
    private val logger: Logger
) {

    fun show() {
        Window(
            title = "BMW E39 Nav Loading",
            size = IntSize(1024, 800)
        ) {
            windowContent()
        }
    }

    @Composable
    fun windowContent() {

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
                    }
                )
            }

            Box(modifier = Modifier
                .height(468.dp)
                .width(800.dp)
                .border(2.dp, Color.Red)
            ) {
                MapViewer(
                    overlayProperties = overlayProperties.value,
                    extents = extents.value,
                    onCenterPositionUpdated = {}
                )
            }
        }
    }

    @Composable
    fun manipulator(
        onOverlayPropertiesChanged : (new : OverlayProperties) -> Unit,
        onExtentsChanged : (new : Extents) -> Unit
    ) {

        val zoom = remember { mutableStateOf(7) }
        val mapCenter = remember { mutableStateOf(GeoPosition(0.0, 0.0)) }

        LaunchedEffect(zoom, mapCenter) {
            combine(
                snapshotFlow { zoom.value },
                snapshotFlow { mapCenter.value }
            ) { zoom, center ->
                Extents(
                    center = center,
                    mapScale = zoom.toMapScale()
                )
            }.collect {
                onExtentsChanged(it)
            }
        }


        Column {
            NestingCard {
                NestingCardHeader("Download zoom 0")
                Button(onClick = {

                }) { Text("Download")}
            }
            NestingCard {
                NestingCardHeader("Map Control")

                NestingCard {
                    NestingCardHeader("Parsed Zoom")

                    Text("Zoom: ${zoom.value}")
                    Slider(
                        value = zoom.value.toFloat(),
                        onValueChange = {
                            logger.d("mapDebug", "newZoom: $it")
                            zoom.value = it.roundToInt() },
                        valueRange = 0F.rangeTo(10F),
                        steps = 11
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
        }
    }

    @Composable
    fun NestingCard(
        modifier: Modifier = Modifier,
        contents : @Composable () -> Unit
    ) {
        Card(
            modifier = Modifier
                .padding(8.dp)
                .then(modifier),
            elevation = 8.dp
        ) {
            Column {
                contents()
            }
        }
    }

    @Composable fun NestingCardHeader(text : String) {
        Text(
            modifier = Modifier.padding(vertical = 8.dp),
            text = text,
            style = MaterialTheme.typography.subtitle1
        )
    }
}