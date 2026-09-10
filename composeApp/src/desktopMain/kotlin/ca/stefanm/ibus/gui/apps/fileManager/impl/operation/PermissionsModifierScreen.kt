package ca.stefanm.ca.stefanm.ibus.gui.apps.fileManager.impl.operation

import androidx.compose.runtime.Composable
import ca.stefanm.ibus.annotations.screenflow.ScreenDoc
import ca.stefanm.ibus.autoDiscover.AutoDiscover
import ca.stefanm.ibus.di.ApplicationModule
import ca.stefanm.ibus.gui.menu.navigator.NavigationNode
import ca.stefanm.ibus.gui.menu.navigator.NavigationNodeTraverser
import ca.stefanm.ibus.gui.menu.navigator.Navigator
import ca.stefanm.ibus.gui.menu.widgets.knobListener.KnobListenerService
import ca.stefanm.ibus.gui.menu.widgets.screenMenu.FullScreenMenu
import ca.stefanm.ibus.lib.logging.Logger
import java.io.File
import javax.inject.Inject
import javax.inject.Named

@ScreenDoc(
    screenName = "PermissionsModifierScreen",
    description = "A screen that shows the permissions for a file and lets the user modify them",
)
@ScreenDoc.AllowsGoBack
@AutoDiscover
class PermissionsModifierScreen @Inject constructor(
    @Named(ApplicationModule.KNOB_LISTENER_MAIN)
    private val knobListenerServiceMain: KnobListenerService,
    private val logger: Logger,
    private val navigationNodeTraverser: NavigationNodeTraverser
    ) : NavigationNode<Nothing> {
    companion object {
        const val TAG = "PermissionsModifierScreen"
        fun changePermissions(navigationNodeTraverser: NavigationNodeTraverser, file : File) {
            navigationNodeTraverser.navigateToNodeWithParameters(
                PermissionsModifierScreen::class.java,
                file
            )
        }
    }
    override val thisClass: Class<out NavigationNode<Nothing>>
        get() = PermissionsModifierScreen::class.java

    override fun provideMainContent(): @Composable ((incomingResult: Navigator.IncomingResult?) -> Unit) = content@ { params ->

        val file : File? = params?.requestParameters as? File

        if (file == null) {
            navigationNodeTraverser.goBack()
            return@content
        }

        FullScreenMenu.OneColumnSmoothScreen(
            header = "Change Permissions for ${file.name}",
            knobListenerService = knobListenerServiceMain,
            logger = logger,
            navigationNodeTraverser = navigationNodeTraverser,
            logTag = TAG,
            prependGoBackEntry = true,
            items = listOf(),
        )
    }

}