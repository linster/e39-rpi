package ca.stefanm.ibus.gui.menu.bluetoothPairing

import androidx.compose.foundation.layout.Column
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import ca.stefanm.ibus.gui.menu.navigator.NavigationNode
import ca.stefanm.ibus.gui.menu.navigator.NavigationNodeTraverser
import ca.stefanm.ibus.gui.menu.navigator.Navigator
import javax.inject.Inject

class BluetoothPairingMenu @Inject constructor(
    private val navigationNodeTraverser: NavigationNodeTraverser
) : NavigationNode<BluetoothPairingMenu.PairingResult> {

    override val thisClass = this::class.java

    data class PairingResult(
        val isSuccess : Boolean
    )


    override fun provideMainContent(): @Composable (Navigator.IncomingResult?) -> Unit {
        return {
            Column {
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
            }
        }
    }
}