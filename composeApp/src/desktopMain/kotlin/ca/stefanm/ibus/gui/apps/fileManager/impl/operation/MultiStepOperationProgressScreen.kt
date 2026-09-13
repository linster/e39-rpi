package ca.stefanm.ca.stefanm.ibus.gui.apps.fileManager.impl.operation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import ca.stefanm.ibus.autoDiscover.AutoDiscover
import ca.stefanm.ibus.di.ApplicationModule
import ca.stefanm.ibus.gui.apps.fileManager.impl.operation.MultiStepOperationRunner
import ca.stefanm.ibus.gui.menu.navigator.NavigationNode
import ca.stefanm.ibus.gui.menu.navigator.NavigationNodeTraverser
import ca.stefanm.ibus.gui.menu.navigator.Navigator
import ca.stefanm.ibus.gui.menu.widgets.knobListener.KnobListenerService
import ca.stefanm.ibus.lib.logging.Logger
import javax.inject.Inject
import javax.inject.Named

//After the multi step operation is built up with the builder,
//Navigate here to see it's progress
@AutoDiscover
class MultiStepOperationProgressScreen @Inject constructor(
    @Named(ApplicationModule.KNOB_LISTENER_MAIN)
    private val knobListenerServiceMain: KnobListenerService,

    private val logger: Logger,
    private val navigationNodeTraverser: NavigationNodeTraverser,

    private val multiStepOperationBuilder: MultiStepOperationBuilder,
    private val multiStepOperationRunner: MultiStepOperationRunner
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

        //If the source and dest folder is the same and it's file copy operation, then append "-copy(timestamp)" to the dest file name
        //If the source and dest folder are the same and it's a file move operation, open the file rename screen
        //If the source and dest folder are the same and it's a folder move or folder copy operation, show a notification and exit.
        

        //First get the user to check the operation with a smooth scroll of the operation, source, dest.
        //Then put a button to "Start Operation".

        //SmoothScroll and entries need to be added on from the runner if they come up.... Abort, Retry, Fail. Like a terminal but with list views.


    }
}