package ca.stefanm.ibus.gui.menu

import androidx.compose.desktop.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.imageFromResource
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.platform.Keyboard
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
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
                        val currentNode = navigator.currentNode.collectAsState()

                        logger.d("MenuWindow", currentNode.value.thisClass.canonicalName)

                        currentNode.value.provideMainContent()()
                    }
//                    Image(
//                        bitmap = imageFromResource("bmw_navigation.png"),
//                        contentDescription = null,
//                        modifier = Modifier.size(100.dp)
//                            .background(Color.Cyan)
//                            .clickable { AppManager.focusedWindow?.close() }
//                    )

                }
            )
        }
    }
}