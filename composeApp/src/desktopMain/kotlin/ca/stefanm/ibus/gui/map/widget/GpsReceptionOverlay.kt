package ca.stefanm.ca.stefanm.ibus.gui.map.widget

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.Stable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ca.stefanm.ibus.gui.menu.widgets.themes.ThemeWrapper
import ca.stefanm.ibus.lib.hardwareDrivers.gps.GpsSatellites
import ca.stefanm.ibus.lib.hardwareDrivers.gps.GpsState
import ca.stefanm.ibus.resources.Res
import ca.stefanm.ibus.resources.gps_fix
import ca.stefanm.ibus.resources.gps_no_gpsd
import ca.stefanm.ibus.resources.gps_no_receiver
import ca.stefanm.ibus.resources.gps_searching
import ca.stefanm.ibus.resources.gps_stale
import coil3.compose.AsyncImage
import org.jetbrains.compose.resources.painterResource

@Stable
class GpsReceptionOverlayState(
    val satellites: State<GpsSatellites?> = mutableStateOf(null),
    val state: State<GpsState> = mutableStateOf(GpsState.NO_GPSD)
)

@Composable
fun GpsReceptionIndicator(
    state : GpsReceptionOverlayState
) {
    Box(modifier = Modifier
        .width(220.dp)
        .height(48.dp)
        .border(
            2.dp,
            Color.DarkGray
        )
        .background(Color.LightGray)
    ) {
        Row(Modifier.fillMaxSize()) {
            Column(
                Modifier.weight(0.3F).fillMaxHeight()
            ) {
                //Image

                val imageRes = when (state.state.value) {
                    GpsState.NO_GPSD -> Res.drawable.gps_no_gpsd
                    GpsState.NO_RECEIVER -> Res.drawable.gps_no_receiver
                    GpsState.SEARCHING -> Res.drawable.gps_searching
                    GpsState.FIX -> Res.drawable.gps_fix
                    GpsState.STALE -> Res.drawable.gps_stale
                }

                Image(
                    modifier = Modifier.padding(8.dp).fillMaxSize(),
                    painter = painterResource(imageRes),
                    contentDescription = state.state.value.name
                )

            }
            Column(
                Modifier.weight(0.7F).fillMaxHeight(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                //Satellites count if we have, otherwise label the picture

                val text = when (state.state.value) {
                    GpsState.NO_GPSD -> "No GPSd"
                    GpsState.NO_RECEIVER -> "No GPS Receiver"
                    GpsState.SEARCHING -> "Searching..."
                    GpsState.FIX -> {
                        val satelites = state.satellites.value
                        if (satelites != null) {
                            val using = satelites.used
                            val visible = satelites.visible
                            "Fix ($using / $visible)"
                        } else {
                            "Fix"
                        }
                    }
                    GpsState.STALE -> "GPS Fix Stale"
                }

                Text(
                    text = text,
                    style = TextStyle(
                        color = Color.Black,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    ),
                    overflow = TextOverflow.Clip
                )
            }
        }
    }
}