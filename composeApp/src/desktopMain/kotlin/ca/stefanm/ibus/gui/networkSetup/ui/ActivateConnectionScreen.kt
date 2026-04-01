package ca.stefanm.ca.stefanm.ibus.gui.networkSetup.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import ca.stefanm.ibus.annotations.screenflow.ScreenDoc
import ca.stefanm.ibus.autoDiscover.AutoDiscover
import ca.stefanm.ibus.gui.menu.Notification
import ca.stefanm.ibus.gui.menu.navigator.NavigationNode
import ca.stefanm.ibus.gui.menu.navigator.NavigationNodeTraverser
import ca.stefanm.ibus.gui.menu.navigator.Navigator
import ca.stefanm.ibus.gui.menu.notifications.NotificationHub
import ca.stefanm.ibus.gui.menu.widgets.BmwSingleLineHeader
import ca.stefanm.ibus.gui.menu.widgets.modalMenu.ModalMenuService
import ca.stefanm.ibus.gui.menu.widgets.screenMenu.FullScreenMenu
import ca.stefanm.ibus.gui.menu.widgets.screenMenu.TextMenuItem
import ca.stefanm.ibus.lib.logging.Logger
import org.jetbrains.compose.ui.tooling.preview.Preview
import javax.inject.Inject
import kotlin.time.Duration.Companion.seconds

@ScreenDoc(
    screenName = "ActivateConnectionScreen",
    description = "Allows a connection to be activated and deactivated."
)
@ScreenDoc.AllowsGoBack
@AutoDiscover
class ActivateConnectionScreen @Inject constructor(
    private val modalMenuService: ModalMenuService,
    private val logger : Logger,
    private val notificationHub: NotificationHub,
    private val navigationNodeTraverser: NavigationNodeTraverser
) : NavigationNode<Nothing> {
    override val thisClass: Class<out NavigationNode<Nothing>>
        get() = ActivateConnectionScreen::class.java

    override fun provideMainContent(): @Composable ((Navigator.IncomingResult?) -> Unit) = {

        Column(Modifier.fillMaxSize()) {
            BmwSingleLineHeader("Activate Connection")

            FullScreenMenu.OneColumn(
                listOf(
                    TextMenuItem(
                        title = "Go Back",
                        onClicked = {
                            navigationNodeTraverser.goBack()
                        }
                    ),
                    TextMenuItem(
                        title = "Show throbber",
                        onClicked = {
                            showConnectingThrobber()
                        }
                    ),
                )
            )
        }
    }

    fun showConnectingThrobber() {
        modalMenuService.showModalWaitDialog(
            image = Notification.NotificationImage.NONE,
            throbber = true,
            headerText = "Connecting...",
            bodyText = "Activating Connection",
            isCancellable = false,
            autoCloseTimeout = 3.seconds
        )
    }



}
