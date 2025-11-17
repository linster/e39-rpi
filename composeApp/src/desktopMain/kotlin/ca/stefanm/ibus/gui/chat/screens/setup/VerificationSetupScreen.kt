package ca.stefanm.ibus.gui.chat.screens.setup

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import ca.stefanm.ibus.gui.chat.service.MatrixService
import ca.stefanm.ibus.annotations.screenflow.ScreenDoc
import ca.stefanm.ibus.autoDiscover.AutoDiscover
import ca.stefanm.ibus.gui.chat.screens.setup.ChatSetupMenuRoot
import ca.stefanm.ibus.gui.menu.navigator.NavigationNode
import ca.stefanm.ibus.gui.menu.navigator.NavigationNodeTraverser
import ca.stefanm.ibus.gui.menu.navigator.Navigator
import ca.stefanm.ibus.gui.menu.widgets.BmwSingleLineHeader
import ca.stefanm.ibus.gui.menu.widgets.screenMenu.FullScreenMenu
import ca.stefanm.ibus.gui.menu.widgets.screenMenu.TextMenuItem
import kotlinx.coroutines.launch
import net.folivo.trixnity.client.verification
import javax.inject.Inject

@ScreenDoc(
    screenName = "VerificationSetupScreen",
    description = "User can verify their matrix session with this device.",
    navigatesTo = [
    ]
)
@ScreenDoc.AllowsGoBack
@AutoDiscover
class VerificationSetupScreen @Inject constructor(
    private val navigationNodeTraverser: NavigationNodeTraverser,
    private val matrixService: MatrixService
) : NavigationNode<Nothing> {
    override val thisClass: Class<out NavigationNode<Nothing>>
    get() = VerificationSetupScreen::class.java

    override fun provideMainContent(): @Composable (incomingResult: Navigator.IncomingResult?) -> Unit = {
        Column(modifier = Modifier.fillMaxSize()) {

            BmwSingleLineHeader("Matrix Device Verification")

            val scope = rememberCoroutineScope()
            scope.launch {
                matrixService.getMatrixClient()?.verification?.getSelfVerificationMethods()?.collect {
                    it
                }
            }

            FullScreenMenu.OneColumn(listOf(
                TextMenuItem("Go Back", onClicked = { navigationNodeTraverser.goBack() }),
                TextMenuItem("", onClicked = {
                    navigationNodeTraverser.navigateToNode(LoginScreen::class.java)
                }),
                TextMenuItem("Matrix Service", onClicked = {

                }),
                TextMenuItem("Notifications", onClicked = {
                    navigationNodeTraverser.navigateToNode(NotificationPreferencesScreen::class.java)
                }),
                TextMenuItem("Verification", onClicked = {
                    navigationNodeTraverser.navigateToNode(VerificationSetupScreen::class.java)
                }),
            ))
        }
    }
}