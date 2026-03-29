package ca.stefanm.ca.stefanm.ibus.gui.networkSetup

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import ca.stefanm.ca.stefanm.ibus.gui.networkSetup.ui.SetHostnameScreen
import ca.stefanm.ibus.annotations.screenflow.ScreenDoc
import ca.stefanm.ibus.autoDiscover.AutoDiscover
import ca.stefanm.ibus.gui.menu.navigator.NavigationNode
import ca.stefanm.ibus.gui.menu.navigator.NavigationNodeTraverser
import ca.stefanm.ibus.gui.menu.navigator.Navigator
import ca.stefanm.ibus.gui.menu.widgets.BmwSingleLineHeader
import ca.stefanm.ibus.gui.menu.widgets.screenMenu.FullScreenMenu
import ca.stefanm.ibus.gui.menu.widgets.screenMenu.TextMenuItem
import ca.stefanm.ibus.lib.logging.Logger
import javax.inject.Inject

@ScreenDoc(
    screenName = "NetworkSetupMenu",
    description = "The root menu for the network-manager based setup"
)
@ScreenDoc.AllowsGoRoot
@AutoDiscover
class NetworkSetupMenu @Inject constructor(
    private val navigationNodeTraverser: NavigationNodeTraverser,
    private val logger: Logger
) : NavigationNode<Nothing> {

    override val thisClass: Class<out NavigationNode<Nothing>>
        get() = NetworkSetupMenu::class.java

    override fun provideMainContent(): @Composable ((Navigator.IncomingResult?) -> Unit) = {
        Column(Modifier.fillMaxSize()) {
            BmwSingleLineHeader("Network Setup (Network Manager)")

            FullScreenMenu.TwoColumnFillFromCorners(
                nw = listOf(

                ),
                ne = listOf(
                    TextMenuItem(
                        "Edit a connection",
                        onClicked = {}
                    ),
                    TextMenuItem(
                        "Activate a connection",
                        onClicked = {}
                    ),
                    TextMenuItem(
                        "Set system hostname",
                        onClicked = { navigationNodeTraverser.navigateToNode(SetHostnameScreen::class.java)}
                    )
                    ),
                sw = listOf(
                    TextMenuItem(
                        "Go Back",
                        onClicked = {
                            navigationNodeTraverser.navigateToRoot()
                        }
                    )
                ),
                se = listOf()
            )

        }
    }
}