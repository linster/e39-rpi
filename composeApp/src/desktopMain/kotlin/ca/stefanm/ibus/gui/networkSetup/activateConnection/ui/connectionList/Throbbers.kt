package ca.stefanm.ibus.gui.networkSetup.activateConnection.ui.connectionList

import ca.stefanm.ibus.gui.menu.Notification
import ca.stefanm.ibus.gui.menu.widgets.modalMenu.ModalMenuService
import javax.inject.Inject
import kotlin.time.Duration.Companion.seconds

class Throbbers @Inject constructor(
    private val modalMenuService: ModalMenuService
) {

    fun showConnectingThrobber(
        onTimeout : () -> Unit
    ) {
        modalMenuService.showModalWaitDialog(
            image = Notification.NotificationImage.NONE,
            throbber = true,
            headerText = "Connecting...",
            bodyText = "Activating Connection",
            isCancellable = false,
            autoCloseTimeout = 3.seconds,
            onTimeout = onTimeout
        )
    }
}