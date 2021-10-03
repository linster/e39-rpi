package ca.stefanm.ibus.gui.menu.bluetoothPairing.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import ca.stefanm.ibus.autoDiscover.AutoDiscover
import ca.stefanm.ibus.gui.menu.bluetoothPairing.stateMachine.PairingManager
import ca.stefanm.ibus.gui.menu.navigator.NavigationNode
import ca.stefanm.ibus.gui.menu.navigator.NavigationNodeTraverser
import ca.stefanm.ibus.gui.menu.navigator.Navigator
import ca.stefanm.ibus.gui.menu.widgets.BmwSingleLineHeader
import ca.stefanm.ibus.gui.menu.widgets.screenMenu.FullScreenMenu
import ca.stefanm.ibus.gui.menu.widgets.screenMenu.MenuItem
import javax.inject.Inject

@AutoDiscover
class BtEmptyMenu @Inject constructor(
    private val navigationNodeTraverser: NavigationNodeTraverser,
    private val pairingManager: PairingManager
) : NavigationNode<Nothing> {

    data class EmptyMenuParameters(
        //If true, we're loading this menu from the Main Menu.
        //if false, we're loading
        val isInitialLoad : Boolean = true
    )

    override val thisClass: Class<out NavigationNode<Nothing>>
        get() = BtEmptyMenu::class.java

    override fun provideMainContent(): @Composable (incomingResult: Navigator.IncomingResult?) -> Unit = { incomingResult ->

        val isInitialLoad =
            (incomingResult?.requestParameters as? EmptyMenuParameters)?.isInitialLoad ?: false

        if (!isInitialLoad) {
            navigationNodeTraverser.cleanupBackStackDescendentsOf(BtEmptyMenu::class.java)
        }

        if (incomingResult?.result is UiResult) {
            pairingManager.onUiResult(incomingResult.result)
        }

        Column {
            BmwSingleLineHeader("Bluetooth")
            FullScreenMenu.TwoColumnFillFromTop(
                leftItems = listOf(MenuItem.SPACER, MenuItem.SPACER),
                rightItems = listOf(MenuItem.SPACER, MenuItem.SPACER)
            )
            LaunchedEffect(true) {
                if (isInitialLoad) {
                    pairingManager.onLoadEmptyBtMenu()
                }
            }
        }
    }
}