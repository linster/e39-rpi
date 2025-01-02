package ca.stefanm.ibus.gui.map.mapScreen

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember


//https://developer.android.com/jetpack/compose/state#state-holder-source-of-truth

@Composable
fun rememberMapScreenStateHolder() {
    remember { MapScreenStateHolder() }
}

class MapScreenStateHolder {

}