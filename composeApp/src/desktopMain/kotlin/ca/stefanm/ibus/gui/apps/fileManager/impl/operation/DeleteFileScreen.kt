package ca.stefanm.ca.stefanm.ibus.gui.apps.fileManager.impl.operation

import androidx.compose.runtime.Composable
import ca.stefanm.ibus.gui.menu.navigator.NavigationNode
import ca.stefanm.ibus.gui.menu.navigator.NavigationNodeTraverser
import ca.stefanm.ibus.gui.menu.navigator.Navigator
import java.io.File
import javax.inject.Inject

class DeleteFileScreen @Inject constructor(

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

    override fun provideMainContent(): @Composable ((incomingResult: Navigator.IncomingResult?) -> Unit) {
        //TODO CHECK THE SETTINGS IF ALLOWED TOO.
        TODO("Not yet implemented")
    }


    //Same idea as Rename file screen
}