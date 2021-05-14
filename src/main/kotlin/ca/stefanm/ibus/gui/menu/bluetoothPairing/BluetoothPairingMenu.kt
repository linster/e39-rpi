package ca.stefanm.ibus.gui.menu.bluetoothPairing

import androidx.compose.foundation.layout.Column
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import ca.stefanm.ibus.gui.menu.navigator.NavigationNode
import ca.stefanm.ibus.gui.menu.navigator.NavigationNodeTraverser
import javax.inject.Inject

class BluetoothPairingMenu @Inject constructor(
    private val navigationNodeTraverser: NavigationNodeTraverser
) : NavigationNode {

    override val thisClass = this::class.java

    override fun provideMainContent(): @Composable () -> Unit {
        return {
            Column {
                Text("Hello Bluetooth Menu")
                Button(onClick = {
                    navigationNodeTraverser.goBack()
                }) { Text("Go back")}
            }
        }
    }
}