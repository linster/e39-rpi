package ca.stefanm.ca.stefanm.ibus.gui.bluetoothPairing

import androidx.compose.foundation.layout.Column
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import ca.stefanm.ibus.autoDiscover.AutoDiscover
import ca.stefanm.ibus.gui.bluetoothPairing.ui.*
import ca.stefanm.ibus.gui.menu.navigator.NavigationNode
import ca.stefanm.ibus.gui.menu.navigator.NavigationNodeTraverser
import ca.stefanm.ibus.gui.menu.navigator.Navigator
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.shareIn
import javax.inject.Inject

@AutoDiscover
class BluetoothPairingMenu @Inject constructor(
    private val navigationNodeTraverser: NavigationNodeTraverser
) : NavigationNode<Nothing> {

    override val thisClass = this::class.java

    override fun provideMainContent(): @Composable (Navigator.IncomingResult?) -> Unit = {

        LaunchedEffect(true) {
            navigationNodeTraverser
                .navigateToNodeWithParameters(
                    BtEmptyMenu::class.java,
                    BtEmptyMenu.EmptyMenuParameters(
                        isInitialLoad = true
                    )
                )
        }
    }

}



