package ca.stefanm.ca.stefanm.ibus.gui.apps.fileManager.impl.operation

import androidx.compose.runtime.Composable
import ca.stefanm.ibus.annotations.screenflow.ScreenDoc
import ca.stefanm.ibus.autoDiscover.AutoDiscover
import ca.stefanm.ibus.gui.menu.navigator.NavigationNode
import ca.stefanm.ibus.gui.menu.navigator.NavigationNodeTraverser
import ca.stefanm.ibus.gui.menu.navigator.Navigator
import ca.stefanm.ibus.gui.menu.notifications.NotificationHub
import ca.stefanm.ibus.lib.logging.Logger
import java.io.File
import javax.inject.Inject

@ScreenDoc(
    screenName = "RenameFileScreen",
    description = "A screen that prompts the user to rename a file"
)
@AutoDiscover
class RenameFileScreen @Inject constructor(
    private val logger: Logger,
    private val navigationNodeTraverser: NavigationNodeTraverser,
    private val notificationHub: NotificationHub
) : NavigationNode<Nothing> {

    companion object {
        const val TAG = "RenameFileScreen"
        fun renameFile(navigationNodeTraverser: NavigationNodeTraverser, file : File) {
            navigationNodeTraverser.navigateToNodeWithParameters(
                RenameFileScreen::class.java,
                file
            )
        }
    }

    override val thisClass: Class<out NavigationNode<Nothing>>
        get() = RenameFileScreen::class.java

    override fun provideMainContent(): @Composable ((incomingResult: Navigator.IncomingResult?) -> Unit) = { params ->

        val file : File? = params?.requestParameters as? File


    }


}