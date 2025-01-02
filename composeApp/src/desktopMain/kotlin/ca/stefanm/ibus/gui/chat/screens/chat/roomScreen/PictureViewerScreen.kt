package ca.stefanm.ibus.gui.chat.screens.chat.roomScreen

import androidx.compose.foundation.layout.Column
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import ca.stefanm.ibus.autoDiscover.AutoDiscover
import ca.stefanm.ibus.gui.menu.navigator.NavigationNode
import ca.stefanm.ibus.gui.menu.navigator.NavigationNodeTraverser
import ca.stefanm.ibus.gui.menu.navigator.Navigator
import ca.stefanm.ibus.gui.menu.widgets.BmwSingleLineHeader
import ca.stefanm.ibus.gui.menu.widgets.screenMenu.HalfScreenMenu
import ca.stefanm.ibus.gui.menu.widgets.screenMenu.TextMenuItem
import java.io.File
import javax.inject.Inject


// Load a picture full-screen from a random source for bigger viewing
@AutoDiscover
class PictureViewerScreen @Inject constructor(
    private val navigationNodeTraverser: NavigationNodeTraverser
) : NavigationNode<Nothing> {

    override val thisClass: Class<out NavigationNode<Nothing>>
        get() = PictureViewerScreen::class.java

    companion object {
        const val TAG = "PictureViewerScreen"

        fun openForFile(navigationNodeTraverser: NavigationNodeTraverser, file : File) {
            navigationNodeTraverser.navigateToNodeWithParameters(PictureViewerScreen::class.java,
                PictureViewerScreenParameters(file))
        }
    }

    data class PictureViewerScreenParameters(
        val file : File
    )

    override fun provideMainContent(): @Composable (incomingResult: Navigator.IncomingResult?) -> Unit = {

        val chosenFile : File? = (it?.requestParameters as? PictureViewerScreenParameters?)?.file

        Column {
            if (chosenFile == null) {
                BmwSingleLineHeader("No Picture Chosen")
                HalfScreenMenu.OneColumn(listOf(
                    TextMenuItem("Go Back", onClicked = { navigationNodeTraverser.goBack()})
                ))
            } else {
                HalfScreenMenu.OneColumn(listOf(
                    TextMenuItem("Go Back", onClicked = { navigationNodeTraverser.goBack()})
                ))
                // TODO picture load here.
            }
        }
    }
}