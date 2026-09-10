package ca.stefanm.ca.stefanm.ibus.gui.apps.fileManager.impl.operation

import androidx.compose.runtime.Composable
import ca.stefanm.ibus.autoDiscover.AutoDiscover
import ca.stefanm.ibus.gui.menu.navigator.NavigationNode
import ca.stefanm.ibus.gui.menu.navigator.NavigationNodeTraverser
import ca.stefanm.ibus.gui.menu.navigator.Navigator
import ca.stefanm.ibus.lib.logging.Logger
import java.io.File
import javax.inject.Inject


@AutoDiscover
class DeleteFileScreen @Inject constructor(
    private val logger: Logger,
    private val navigationNodeTraverser: NavigationNodeTraverser
) : NavigationNode<Nothing> {

    companion object {
        const val TAG = "DeleteFileScreen"
        fun deleteFile(navigationNodeTraverser: NavigationNodeTraverser, file : File) {
            navigationNodeTraverser.navigateToNodeWithParameters(
                DeleteFileScreen::class.java,
                file
            )
        }
    }

    override val thisClass: Class<out NavigationNode<Nothing>>
        get() = DeleteFileScreen::class.java

    override fun provideMainContent(): @Composable ((incomingResult: Navigator.IncomingResult?) -> Unit) = content@ { params ->
        //TODO CHECK THE SETTINGS IF ALLOWED TOO.

        val file : File? = params?.requestParameters as? File

        if (file == null) {
            navigationNodeTraverser.goBack()
            return@content
        }

    }


    //Same idea as Rename file screen
}