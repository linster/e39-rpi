package ca.stefanm.ibus.gui.bluetoothPairing.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import ca.stefanm.ca.stefanm.ibus.gui.menu.widgets.screenMenu.SmoothScroll
import ca.stefanm.ibus.autoDiscover.AutoDiscover
import ca.stefanm.ibus.di.ApplicationModule
import ca.stefanm.ibus.gui.bluetoothPairing.stateMachine.PairingManager
import ca.stefanm.ibus.gui.menu.navigator.NavigationNode
import ca.stefanm.ibus.gui.menu.navigator.NavigationNodeTraverser
import ca.stefanm.ibus.gui.menu.navigator.Navigator
import ca.stefanm.ibus.gui.menu.widgets.BmwSingleLineHeader
import ca.stefanm.ibus.gui.menu.widgets.ItemChipOrientation
import ca.stefanm.ibus.gui.menu.widgets.MenuItem
import ca.stefanm.ibus.gui.menu.widgets.knobListener.KnobListenerService
import ca.stefanm.ibus.gui.menu.widgets.knobListener.dynamic.toDynamicLambdas
import ca.stefanm.ibus.gui.menu.widgets.screenMenu.ScrollMenu
import ca.stefanm.ibus.gui.menu.widgets.screenMenu.TextMenuItem
import ca.stefanm.ibus.gui.menu.widgets.screenMenu.TextMenuItem.Companion.toCheckBox
import ca.stefanm.ibus.gui.menu.widgets.themes.ThemeWrapper
import ca.stefanm.ibus.lib.logging.Logger
import kotlinx.coroutines.flow.SharedFlow
import javax.inject.Inject
import javax.inject.Named

//This is the screen that lets us pick devices to pair with

fun NavigationNodeTraverser.showPairableDevices(
    pairableDevices: SharedFlow<List<PairableDeviceChooser.PairableDevice>>
) {
    //TODO we're going to pass a flow to this screen so that when the agent
    //TODO updates the list of devices we can pair to, the UI updates
    //TODO live.
    this.navigateToNodeWithParameters(PairableDeviceChooser::class.java,
        PairableDeviceChooser.PairableDeviceChooserParameters(
            pairableDevices = pairableDevices
        )
    )
}

@AutoDiscover
class PairableDeviceChooser @Inject constructor(
    private val navigationNodeTraverser: NavigationNodeTraverser,
    private val pairingManager: PairingManager,

    @Named(ApplicationModule.KNOB_LISTENER_MAIN)
    private val knobListenerService: KnobListenerService,
    private val logger: Logger
) : NavigationNode<PairableDeviceChooser.PairableDeviceChooserResult> {

    data class PairableDevice(
        val address : String, //MAC Address as string
        val alias : String, //Name of device
        val isPaired : Boolean,
        val isConnected : Boolean
    ) {
        companion object {
            val EMPTY = PairableDevice(
                "<INVALID>",
                "<INVALID>",
                false,
                false
            )
        }
    }

    data class PairableDeviceChooserParameters(
        val pairableDevices : SharedFlow<List<PairableDevice>>
    )

    //This used to be sealed but something was funky with the Compose compiler.
    open class PairableDeviceChooserResult : UiResult() {
        object Cancelled : PairableDeviceChooserResult()
        data class RequestPairToDevice(val device: PairableDevice) : PairableDeviceChooserResult()
    }

    override val thisClass: Class<out NavigationNode<PairableDeviceChooserResult>>
        get() = PairableDeviceChooser::class.java

    override fun provideMainContent(): @Composable (incomingResult: Navigator.IncomingResult?) -> Unit = {
        Column(
            Modifier.background(ThemeWrapper.ThemeHandle.current.colors.menuBackground)
        ) {

            BmwSingleLineHeader("Select Device to Pair With")

            if (it == null) {
                pairingManager.onGoToBtMainMenu()
            }

            //Unpack the Flow<Devices> here and make it a UI state.
            val pairableDevices = (it?.requestParameters as PairableDeviceChooserParameters)
                .pairableDevices
                .collectAsState(listOf())

            SmoothScroll.SmoothScroll(
                modifier = Modifier,
                knobListenerService = knobListenerService,
                tag = "Pairable Device Chooser",
                logger = logger,
                items = pairableDevices.value.let {
                    listOf(TextMenuItem(
                        title = "Go Back",
                        onClicked = {
                            navigationNodeTraverser.setResultAndGoBack(this@PairableDeviceChooser, PairableDeviceChooserResult.Cancelled)
                        }
                    )) + it.map { device ->
                        TextMenuItem(
                            title = "${device.alias} ; isConnected ${device.isConnected.toCheckBox()} ; isPaired ${device.isPaired.toCheckBox()}",
                            onClicked = {
                                //TODO We probably end up cancelling the pairing because we complete the flow here.
                                //TODO because the screen goes away. We actually need to
                                navigationNodeTraverser.setResultAndGoBack(
                                    this@PairableDeviceChooser,
                                    PairableDeviceChooserResult.RequestPairToDevice(device)
                                )
                            }
                        )

                    }
                }.toDynamicLambdas()
            )
        }
    }
}