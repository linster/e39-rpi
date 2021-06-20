package ca.stefanm.ibus.gui.menu

import androidx.compose.desktop.AppManager
import androidx.compose.foundation.layout.Column
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import ca.stefanm.ibus.car.bordmonitor.input.IBusInputMessageParser
import ca.stefanm.ibus.car.bordmonitor.input.InputEvent
import ca.stefanm.ibus.di.ApplicationScope
import ca.stefanm.ibus.gui.menu.bluetoothPairing.BluetoothPairingMenu
import ca.stefanm.ibus.gui.menu.navigator.NavigationNode
import ca.stefanm.ibus.gui.menu.navigator.NavigationNodeTraverser
import ca.stefanm.ibus.gui.menu.widgets.ScrollListener
import ca.stefanm.ibus.gui.picker.TextEntry
import ca.stefanm.ibus.lib.logging.Logger
import ca.stefanm.ibus.lib.messages.IBusMessage
import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.value.MutableValue
import com.arkivanov.decompose.value.Value
import com.ginsberg.cirkle.circular
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import javax.inject.Inject

@ApplicationScope
class MainMenu @Inject constructor(
    private val navigationNodeTraverser: NavigationNodeTraverser,
    private val textEntry: TextEntry,
    private val logger: Logger
) : NavigationNode<Nothing> {
    override val thisClass = MainMenu::class.java

    override fun provideMainContent(): @Composable () -> Unit {
        return {
            val enteredText = remember { mutableStateOf("Initial") }

            Column {
                Text("Hello Main Menu")
                Button(onClick = {
                    navigationNodeTraverser.navigateToNode(BluetoothPairingMenu::class.java)
                }) { Text("Go to bt")}


                Button(
                    onClick = {
                        textEntry.enterText(
                            listOf(),
                            onComplete = {
                                enteredText.value = it
                            },
                            onCancel = {}
                        )
                    }
                ) { Text("Enter text")}
                Text(text = "Entered text: ${enteredText.value}")
            }
        }
    }

}
