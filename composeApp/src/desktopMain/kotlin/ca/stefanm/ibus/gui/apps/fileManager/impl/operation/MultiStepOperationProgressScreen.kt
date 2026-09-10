package ca.stefanm.ca.stefanm.ibus.gui.apps.fileManager.impl.operation

import androidx.compose.runtime.Composable
import ca.stefanm.ibus.autoDiscover.AutoDiscover
import ca.stefanm.ibus.gui.menu.navigator.NavigationNode
import ca.stefanm.ibus.gui.menu.navigator.Navigator
import javax.inject.Inject

//After the multi step operation is built up with the builder,
//Navigate here to see it's progress
@AutoDiscover
class MultiStepOperationProgressScreen @Inject constructor(

) : NavigationNode<Nothing> {


    override val thisClass: Class<out NavigationNode<Nothing>>
        get() = MultiStepOperationProgressScreen::class.java

    override fun provideMainContent(): @Composable ((incomingResult: Navigator.IncomingResult?) -> Unit) = {

    }
}