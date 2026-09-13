package ca.stefanm.ca.stefanm.ibus.gui.apps.fileManager.impl.operation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import ca.stefanm.ibus.autoDiscover.AutoDiscover
import ca.stefanm.ibus.gui.menu.navigator.NavigationNode
import ca.stefanm.ibus.gui.menu.navigator.Navigator
import javax.inject.Inject

//After the multi step operation is built up with the builder,
//Navigate here to see it's progress
@AutoDiscover
class MultiStepOperationProgressScreen @Inject constructor(
    private val multiStepOperationBuilder: MultiStepOperationBuilder
) : NavigationNode<Nothing> {


    override val thisClass: Class<out NavigationNode<Nothing>>
        get() = MultiStepOperationProgressScreen::class.java

    override fun provideMainContent(): @Composable ((incomingResult: Navigator.IncomingResult?) -> Unit) = {

        DisposableEffect(Unit) {
            onDispose {
                //Clear the builder regardless of whether we go back from a meu entry, or the nav HMI debugger.
                multiStepOperationBuilder.clear()
            }
        }

        //SmoothScroll and entries need to be added on from the runner if they come up.... Abort, Retry, Fail. Like a terminal but with list views.
    }
}