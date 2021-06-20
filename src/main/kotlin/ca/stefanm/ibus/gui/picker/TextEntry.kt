package ca.stefanm.ibus.gui.picker

import androidx.compose.desktop.AppManager
import androidx.compose.desktop.Window
import androidx.compose.foundation.layout.Row
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import ca.stefanm.ibus.di.ApplicationScope
import ca.stefanm.ibus.gui.menu.navigator.NavigationNode
import ca.stefanm.ibus.gui.menu.navigator.NavigationNodeTraverser
import ca.stefanm.ibus.gui.menu.navigator.Navigator
import ca.stefanm.ibus.lib.logging.Logger
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.sync.Mutex
import java.util.concurrent.CountDownLatch
import javax.inject.Inject
import javax.inject.Provider
import kotlin.coroutines.Continuation
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

//https://store.bimmernav.com/blogs/installation-bmw/bmw-mkiv-navigation-computer-features-and-benefits

interface PickList


@ApplicationScope
class TextEntry @Inject constructor(
    private val logger : Logger
)  {

    fun enterText(
        autoCompleteSet: List<String>,
        onComplete: (String) -> Unit,
        onCancel: () -> Unit
    ) {
        val (x, y) = AppManager.focusedWindow?.let { it.x to it.y } ?: 0 to 0
        val (width, height) = AppManager.focusedWindow?.let { it.width to it.height } ?: 800 to 468
        Window(
            size = IntSize(width, height),
            undecorated = true,
            location = IntOffset(x, y)
        ) {
            TextEntry(
                autoCompleteSet,
                onComplete = { entered ->
                    onComplete(entered)
                    AppManager.focusedWindow?.close()
                },
                onCancel = { onCancel() ; AppManager.focusedWindow?.close() }
            )
        }
    }

    @Composable
    fun TextEntry(
        autoCompleteSet: List<String>,
        onComplete: (String) -> Unit,
        onCancel: () -> Unit
    ) {

        Row {
            Button(
                onClick = {
                    logger.d("TextEntry", "wat")

                    onComplete("Jeff")

                }
            ) { Text("Ok") }

//            val someStateReader = remember { someState.collectAsState() }
//            Text("SomeState: ${someState.collectAsState(3).value}")

            Button(onClick = { onCancel() }) { Text("Cancel") }
        }
    }
}



