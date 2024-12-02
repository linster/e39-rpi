package ca.stefanm.ibus.gui.networkInfo

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import ca.stefanm.ibus.annotations.screenflow.ScreenDoc
import ca.stefanm.ibus.autoDiscover.AutoDiscover
import ca.stefanm.ibus.configuration.ConfigurationStorage
import ca.stefanm.ibus.configuration.E39Config
import ca.stefanm.ibus.gui.menu.navigator.NavigationNode
import ca.stefanm.ibus.gui.menu.navigator.NavigationNodeTraverser
import ca.stefanm.ibus.gui.menu.navigator.Navigator
import ca.stefanm.ibus.gui.menu.widgets.BmwSingleLineHeader
import ca.stefanm.ibus.gui.menu.widgets.screenMenu.FullScreenMenu
import ca.stefanm.ibus.gui.menu.widgets.screenMenu.TextMenuItem
import com.pi4j.system.NetworkInfo
import com.pi4j.system.NetworkInterface
import com.pi4j.system.SystemInfo
import javax.inject.Inject

@ScreenDoc(
    screenName = "NetworkInfoScreen",
    description = "Shows the current IP Address of the Rpi so the user can SSH in"
)
@ScreenDoc.AllowsGoBack
@AutoDiscover
class NetworkInfoScreen @Inject constructor(
    private val navigationNodeTraverser: NavigationNodeTraverser,
    private val configurationStorage: ConfigurationStorage
) : NavigationNode<Nothing> {
    override val thisClass: Class<out NavigationNode<Nothing>>
        get() = NetworkInfoScreen::class.java

    override fun provideMainContent(): @Composable (incomingResult: Navigator.IncomingResult?) -> Unit = {


        val ipAddress = remember { mutableStateOf("") }

        LaunchedEffect(Unit) {
            if (configurationStorage.config[E39Config.CarPlatformConfigSpec._isPi]) {
                ipAddress.value = NetworkInfo.getIPAddresses()[0]
            }
        }

        Column(Modifier.fillMaxSize()) {
            BmwSingleLineHeader("Network Info")

            FullScreenMenu.OneColumn(
                listOf(
                    TextMenuItem(
                        title = "IP Address: ${ipAddress.value}",
                        isSelectable = false,
                        onClicked = {}
                    ),

                    TextMenuItem(
                        title = "Go Back",
                        onClicked = {
                            navigationNodeTraverser.goBack()
                        }
                    ),
                )
            )
        }
    }
}