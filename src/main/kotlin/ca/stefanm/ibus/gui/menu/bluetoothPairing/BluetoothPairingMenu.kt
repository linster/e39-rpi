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
) : NavigationNode<Nothing> {

    override val thisClass = this::class.java


    //https://developer.android.com/jetpack/compose/side-effects#snapshotFlow
    //Use a snapshot flow to write out compose UI state to a box that is shared between
    //all the screens. This means that every UI event where we do stuff can be collected, in the collect{}
    //we can just save the current state.
    //If we don't need to collect or have a flow with filtering, we can use a SideEffect




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