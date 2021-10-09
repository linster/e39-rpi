package ca.stefanm.ibus.gui.debug.windows

import androidx.compose.desktop.Window
import androidx.compose.foundation.layout.Column
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.Composable

import ca.stefanm.ibus.gui.bluetoothPairing.stateMachine.dbus.BluetoothPairingManager
import javax.inject.Inject

class PairingDebug @Inject constructor(
    private val bluetoothPairingManager: BluetoothPairingManager
) {

    fun show() {
        Window(title = "Pairing Debug"
        ) {
            contents()
        }
    }

    @Composable
    fun contents() {
        Column {
            Button(
                onClick = { bluetoothPairingManager.startPairing()}
            ) { Text("Start Pairing")}

            Button(
                onClick = { bluetoothPairingManager.cleanup()}
            ) { Text("Cleanup")}
        }
    }
}