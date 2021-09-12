package ca.stefanm.ibus.gui.menu.bluetoothPairing

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.sp
import ca.stefanm.ibus.autoDiscover.AutoDiscover
import ca.stefanm.ibus.gui.menu.bluetoothPairing.ui.BluetoothPinConfirmationScreen
import ca.stefanm.ibus.gui.menu.bluetoothPairing.ui.navigateToNodeWithParameters
import ca.stefanm.ibus.gui.menu.bluetoothPairing.ui.requestPinConfirmation
import ca.stefanm.ibus.gui.menu.navigator.NavigationNode
import ca.stefanm.ibus.gui.menu.navigator.NavigationNodeTraverser
import ca.stefanm.ibus.gui.menu.navigator.Navigator
import ca.stefanm.ibus.gui.menu.widgets.ChipItemColors
import ca.stefanm.ibus.gui.menu.widgets.screenMenu.FullScreenPrompts
import javax.inject.Inject

@AutoDiscover
class BluetoothPairingMenu @Inject constructor(
    private val navigationNodeTraverser: NavigationNodeTraverser
) : NavigationNode<BluetoothPairingMenu.PairingResult> {

    override val thisClass = this::class.java

    data class PairingResult(
        val isSuccess : Boolean
    )

    override fun provideMainContent(): @Composable (Navigator.IncomingResult?) -> Unit = {
        Column {

            val pairingResult = remember { mutableStateOf<BluetoothPinConfirmationScreen.PinConfirmationResult?>(null) }

            if (it?.resultFrom == BluetoothPinConfirmationScreen::class.java &&
                it.result is BluetoothPinConfirmationScreen.PinConfirmationResult
            ) {
                pairingResult.value = it.result
            }


            Text("Pairing result: ${pairingResult.value}")

            Text("Hello Bluetooth Menu")
            Button(onClick = {
                navigationNodeTraverser.setResultAndGoBack(
                    this@BluetoothPairingMenu,
                    PairingResult(true)
                )
            }) { Text("Go back (true)")}
            Button(onClick = {
                navigationNodeTraverser.setResultAndGoBack(
                    this@BluetoothPairingMenu,
                    PairingResult(false)
                )
            }) { Text("Go back (false)")}
            Button(onClick = {
                navigationNodeTraverser.requestPinConfirmation(BluetoothPinConfirmationScreen.PinConfirmationParameters(
                    phoneName = "Pixel 4a",
                    pin = "123456"
                ))
            }) { Text("PinConfirmation")}

            //Looks like we show the pairable devices, and while we're doing that, set our own agent.
            //That agent then listens to what's going on, and we listen to that to show the pin code.
        }
    }

}



