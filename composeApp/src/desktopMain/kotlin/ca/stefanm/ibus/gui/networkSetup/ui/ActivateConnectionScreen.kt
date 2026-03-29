package ca.stefanm.ca.stefanm.ibus.gui.networkSetup.ui

import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import ca.stefanm.ibus.annotations.screenflow.ScreenDoc
import ca.stefanm.ibus.autoDiscover.AutoDiscover
import ca.stefanm.ibus.gui.menu.navigator.NavigationNode
import ca.stefanm.ibus.gui.menu.navigator.Navigator
import ca.stefanm.ibus.gui.menu.notifications.NotificationHub
import ca.stefanm.ibus.gui.menu.widgets.modalMenu.ModalMenuService
import ca.stefanm.ibus.lib.logging.Logger
import org.jetbrains.compose.ui.tooling.preview.Preview
import javax.inject.Inject

@ScreenDoc(
    screenName = "ActivateConnectionScreen",
    description = "Allows a connection to be activated and deactivated."
)
@ScreenDoc.AllowsGoBack
@AutoDiscover
class ActivateConnectionScreen @Inject constructor(
    private val modalMenuService: ModalMenuService,
    private val logger : Logger,
    private val notificationHub: NotificationHub
) : NavigationNode<Nothing> {
    override val thisClass: Class<out NavigationNode<Nothing>>
        get() = ActivateConnectionScreen::class.java

    override fun provideMainContent(): @Composable ((Navigator.IncomingResult?) -> Unit) = {

    }

}
