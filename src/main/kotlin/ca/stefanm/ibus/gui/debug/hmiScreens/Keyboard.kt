package ca.stefanm.ibus.gui.debug.hmiScreens

import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import ca.stefanm.ibus.gui.menu.navigator.NavigationNode
import ca.stefanm.ibus.gui.menu.navigator.NavigationNodeTraverser
import ca.stefanm.ibus.gui.menu.navigator.Navigator
import ca.stefanm.ibus.gui.menu.widgets.BmwSingleLineHeader
import ca.stefanm.ibus.gui.menu.widgets.modalMenu.keyboard.Keyboard
import ca.stefanm.ibus.gui.menu.widgets.modalMenu.ModalMenuService
import ca.stefanm.ibus.gui.menu.widgets.screenMenu.HalfScreenMenu
import ca.stefanm.ibus.gui.menu.widgets.screenMenu.TextMenuItem
import javax.inject.Inject

class DebugHmiKeyboard @Inject constructor(
    private val navigationNodeTraverser: NavigationNodeTraverser,
    private val modalMenuService: ModalMenuService
) : NavigationNode<Nothing> {

    override val thisClass: Class<out NavigationNode<Nothing>>
        get() = DebugHmiKeyboard::class.java

    override fun provideMainContent(): @Composable (incomingResult: Navigator.IncomingResult?) -> Unit = {
        Column {
            BmwSingleLineHeader("Debug -> Keyboard")

            val enteredText = remember { mutableStateOf("") }

            HalfScreenMenu.OneColumn(
                fullWidth = true,
                items = listOf(
                    TextMenuItem(
                        title = "Go Back",
                        onClicked = { navigationNodeTraverser.goBack() }
                    ),
                    TextMenuItem(
                        title = "Entered: ${enteredText.value}",
                        isSelectable = false,
                        onClicked = {}
                    )
                ) + Keyboard.KeyboardType.values().map {
                    TextMenuItem(
                        title = "Open Keyboard Type ${it.name}"
                    ) {
                        modalMenuService.showKeyboard(it) { text -> enteredText.value  =text }
                    }
                }
            )
        }
    }
}