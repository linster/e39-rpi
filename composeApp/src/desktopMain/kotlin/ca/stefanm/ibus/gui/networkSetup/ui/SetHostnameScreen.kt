package ca.stefanm.ca.stefanm.ibus.gui.networkSetup.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import ca.stefanm.ibus.annotations.screenflow.ScreenDoc
import ca.stefanm.ibus.autoDiscover.AutoDiscover
import ca.stefanm.ibus.gui.menu.navigator.NavigationNode
import ca.stefanm.ibus.gui.menu.navigator.NavigationNodeTraverser
import ca.stefanm.ibus.gui.menu.navigator.Navigator
import ca.stefanm.ibus.gui.menu.notifications.NotificationHub
import ca.stefanm.ibus.gui.menu.widgets.BmwSingleLineHeader
import ca.stefanm.ibus.gui.menu.widgets.modalMenu.ModalMenuService
import ca.stefanm.ibus.gui.menu.widgets.modalMenu.keyboard.Keyboard
import ca.stefanm.ibus.gui.menu.widgets.screenMenu.FullScreenMenu
import ca.stefanm.ibus.gui.menu.widgets.screenMenu.TextMenuItem
import ca.stefanm.ibus.lib.logging.Logger
import javax.inject.Inject

@ScreenDoc(
    screenName = "SetHostnameScreen",
    description = "Set the system hostname"
)
@ScreenDoc.AllowsGoBack
@AutoDiscover
class SetHostnameScreen @Inject constructor(
    private val navigationNodeTraverser: NavigationNodeTraverser,
    private val modalMenuService: ModalMenuService,
    private val notificationHub: NotificationHub,
    private val logger: Logger
) : NavigationNode<Nothing> {

    override val thisClass: Class<out NavigationNode<Nothing>>
        get() = SetHostnameScreen::class.java

    override fun provideMainContent(): @Composable ((Navigator.IncomingResult?) -> Unit) = {

        val hostName = remember { mutableStateOf("") }

        LaunchedEffect(Unit) {

            // Populate the hostname field from settings...
        }

        Column(Modifier.fillMaxSize()) {
            BmwSingleLineHeader("Hostname")

            FullScreenMenu.OneColumn(
                listOf(
                    TextMenuItem(
                        title = "Hostname: ${hostName.value}",
                        isSelectable = false,
                        onClicked = {}
                    ),
                    TextMenuItem(
                        title = "Change Hostname...",
                        onClicked = {
                            modalMenuService.showKeyboard(
                                type = Keyboard.KeyboardType.FULL,
                                prefilled = hostName.value,
                                onTextEntered = { new ->

                                    try {

                                    } catch (t : Throwable) {

                                    } finally {
                                        modalMenuService.closeModalMenu()
                                    }

                                }
                            )
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
}