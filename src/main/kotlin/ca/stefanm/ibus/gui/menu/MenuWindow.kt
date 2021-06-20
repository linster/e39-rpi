package ca.stefanm.ibus.gui.menu

import androidx.compose.desktop.*
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import ca.stefanm.ibus.gui.menu.navigator.Navigator
import ca.stefanm.ibus.lib.logging.Logger
import javax.inject.Inject

class MenuWindow @Inject constructor(
    private val navigator: Navigator,
    private val logger: Logger
) {

    fun openWindow(x : Int, y : Int) {
        Window(
            title = "BMW E39 Nav Menu",
            size = IntSize(800, 468),
            undecorated = true,
            centered = false,
            location = IntOffset(x, y+30)
        ) {
            PaneManager(
                banner = null,
                sideSplit = null,
                sideSplitVisible = false,
                bottomPanel = null,
                topPopIn = null,
                topPopInVisible = false,
                mainContent = {
                    Box(
                        Modifier.fillMaxSize()
                    ) {
                        val currentNode = navigator.mainContentScreen.collectAsState()

                        logger.d("MenuWindow", currentNode.value.thisClass.canonicalName)

                        currentNode.value.provideMainContent()()
                    }
                }
            )
        }
    }
}