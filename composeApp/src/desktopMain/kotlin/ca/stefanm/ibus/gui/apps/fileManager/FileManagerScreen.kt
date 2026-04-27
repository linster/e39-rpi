package ca.stefanm.ca.stefanm.ibus.gui.apps.fileManager

import androidx.compose.runtime.Composable
import ca.stefanm.ibus.gui.menu.navigator.NavigationNode
import ca.stefanm.ibus.gui.menu.navigator.Navigator
import javax.inject.Inject

class FileManagerScreen @Inject constructor(

) : NavigationNode<Nothing> {
    override val thisClass: Class<out NavigationNode<Nothing>>
        get() = FileManagerScreen::class.java

    override fun provideMainContent(): @Composable ((incomingResult: Navigator.IncomingResult?) -> Unit) = {

    }
}