package ca.stefanm.ibus.gui.menu.bluetoothPairing.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.sp
import ca.stefanm.ibus.autoDiscover.AutoDiscover
import ca.stefanm.ibus.gui.menu.navigator.NavigationNode
import ca.stefanm.ibus.gui.menu.navigator.NavigationNodeTraverser
import ca.stefanm.ibus.gui.menu.navigator.Navigator
import ca.stefanm.ibus.gui.menu.widgets.ChipItemColors
import ca.stefanm.ibus.gui.menu.widgets.screenMenu.FullScreenPrompts
import javax.inject.Inject

fun NavigationNodeTraverser.requestPinConfirmation(params : BluetoothPinConfirmationScreen.PinConfirmationParameters) {
    this.navigateToNodeWithParameters(BluetoothPinConfirmationScreen::class.java, params)
}

@AutoDiscover
class BluetoothPinConfirmationScreen @Inject constructor(
    private val navigationNodeTraverser: NavigationNodeTraverser
) : NavigationNode<BluetoothPinConfirmationScreen.PinConfirmationResult> {

    data class PinConfirmationResult(
        val isApproved : Boolean
    ) : UiResult()

    data class PinConfirmationParameters(
        val phoneName : String,
        val pin : String
    )

    override val thisClass: Class<out NavigationNode<PinConfirmationResult>>
        get() = BluetoothPinConfirmationScreen::class.java

    override fun provideMainContent(): @Composable (incomingResult: Navigator.IncomingResult?) -> Unit = content@ {

        val parameters = it?.requestParameters as? PinConfirmationParameters ?: return@content

        FullScreenPrompts.OptionPrompt(
            header = "Pair with Device?",
            options = FullScreenPrompts.YesNoOptions(
                onYesSelected = {
                    navigationNodeTraverser.setResultAndGoBack(this,
                        PinConfirmationResult(true)
                    )
                },
                onNoSelected = {
                    navigationNodeTraverser.setResultAndGoBack(this,
                        PinConfirmationResult(false)
                    )
                }
            )
        ) {
            Column(
                Modifier.background(ChipItemColors.MenuBackground)
            ) {
                Text("Do you want to pair with this device?", color = Color.White, fontSize = 28.sp)
                Text("", color = Color.White, fontSize = 28.sp)
                Text("Name: ${parameters.phoneName}", color = Color.White, fontSize = 28.sp)

                Text(parameters.pin, color = Color.White, fontSize = 64.sp, textAlign = TextAlign.Center)
            }
        }
    }
}