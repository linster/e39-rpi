package ca.stefanm.ibus.gui.debug.windows

import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.ui.window.WindowScope
import ca.stefanm.ibus.gui.menu.navigator.WindowManager
import ca.stefanm.ibus.logger.Logger
import javax.inject.Inject

class MenuDebug @Inject constructor(
    private val logger: Logger,
) : WindowManager.E39Window {

    override val tag: Any
        get() = this

    override val title = "Menu Simulator"
    override val size = WindowManager.DEFAULT_SIZE
    override val defaultPosition: WindowManager.E39Window.DefaultPosition
        get() = WindowManager.E39Window.DefaultPosition.ANYWHERE

    override fun content(): @Composable WindowScope.() -> Unit = {

        Column {
            NestingCard {
                NestingCardHeader("")
            }

        }

    }
}