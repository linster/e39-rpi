package ca.stefanm.ibus.gui.networkInfo

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import ca.stefanm.ibus.gui.networkSetup.NetworkSetupMenu
import ca.stefanm.ibus.annotations.screenflow.ScreenDoc
import ca.stefanm.ibus.autoDiscover.AutoDiscover
import ca.stefanm.ibus.gui.menu.navigator.NavigationNode
import ca.stefanm.ibus.gui.menu.navigator.NavigationNodeTraverser
import ca.stefanm.ibus.gui.menu.navigator.Navigator
import ca.stefanm.ibus.gui.menu.widgets.BmwSingleLineHeader
import ca.stefanm.ibus.gui.menu.widgets.screenMenu.FullScreenMenu
import ca.stefanm.ibus.gui.menu.widgets.screenMenu.TextMenuItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.Inet4Address
import java.net.NetworkInterface
import javax.inject.Inject

@ScreenDoc(
    screenName = "NetworkInfoScreen",
    description = "Shows the IP addresses of this machine so the user can SSH in",
    navigatesTo = [
        ScreenDoc.NavigateTo(NetworkSetupMenu::class)
    ]
)
@ScreenDoc.AllowsGoBack
@AutoDiscover
class NetworkInfoScreen @Inject constructor(
    private val navigationNodeTraverser: NavigationNodeTraverser,
) : NavigationNode<Nothing> {
    override val thisClass: Class<out NavigationNode<Nothing>>
        get() = NetworkInfoScreen::class.java

    override fun provideMainContent(): @Composable (incomingResult: Navigator.IncomingResult?) -> Unit = {


        val addresses = remember { mutableStateOf<List<String>?>(null) }

        LaunchedEffect(Unit) {
            addresses.value = withContext(Dispatchers.IO) { ipv4Addresses() }
        }

        Column(Modifier.fillMaxSize()) {
            BmwSingleLineHeader("Network Info")

            FullScreenMenu.OneColumn(
                addressLines(addresses.value).map {
                    TextMenuItem(
                        title = it,
                        isSelectable = false,
                        onClicked = {}
                    )
                } + listOf(
                    TextMenuItem(
                        title = "Network Setup",
                        onClicked = {
                            navigationNodeTraverser.navigateToNode(NetworkSetupMenu::class.java)
                        }
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

    private fun addressLines(addresses: List<String>?): List<String> = when {
        addresses == null -> listOf("IP Address: ...")
        addresses.isEmpty() -> listOf("IP Address: none (not connected)")
        else -> addresses.map { "IP Address: $it" }
    }

    /** "wlan0: 192.168.1.20", for every interface that's up, except loopback. */
    private fun ipv4Addresses(): List<String> =
        NetworkInterface.getNetworkInterfaces().toList()
            .filter { it.isUp && !it.isLoopback }
            .flatMap { nic ->
                nic.inetAddresses.toList()
                    .filterIsInstance<Inet4Address>()
                    .map { "${nic.name}: ${it.hostAddress}" }
            }
}
