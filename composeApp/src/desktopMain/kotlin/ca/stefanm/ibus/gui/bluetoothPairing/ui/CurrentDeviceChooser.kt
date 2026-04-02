package ca.stefanm.ibus.gui.bluetoothPairing.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import ca.stefanm.ca.stefanm.ibus.gui.menu.widgets.screenMenu.SmoothScroll
import ca.stefanm.ibus.autoDiscover.AutoDiscover
import ca.stefanm.ibus.di.ApplicationModule
import ca.stefanm.ibus.gui.bluetoothPairing.stateMachine.PairingManager
import ca.stefanm.ibus.gui.bluetoothPairing.ui.PairableDeviceChooser
import ca.stefanm.ibus.gui.bluetoothPairing.ui.PairableDeviceChooser.PairableDeviceChooserResult
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

fun NavigationNodeTraverser.showCurrentDeviceList(
    currentDevices : SharedFlow<List<PairableDeviceChooser.PairableDevice>>
) {
    //TODO we're going to pass a flow to this screen so that when the agent
    //TODO updates the list of devices we can pair to, the UI updates
    //TODO live.
    this.navigateToNodeWithParameters(CurrentDeviceChooser::class.java,
        CurrentDeviceChooser.CurrentDeviceChooserParameters(
            currentDevices = currentDevices
        )
    )
}

@AutoDiscover
class CurrentDeviceChooser @Inject constructor(
    private val pairingManager: PairingManager,


    @Named(ApplicationModule.KNOB_LISTENER_MAIN)
    private val knobListenerService: KnobListenerService,
    private val logger: Logger

) : NavigationNode<CurrentDeviceChooser.CurrentDeviceChooserResult> {

    data class CurrentDeviceChooserParameters(
        val currentDevices : SharedFlow<List<PairableDeviceChooser.PairableDevice>>
    )

    open class CurrentDeviceChooserResult : UiResult(){
        object Cancelled : CurrentDeviceChooserResult()
        data class RequestViewDevice(val device: PairableDeviceChooser.PairableDevice) : CurrentDeviceChooserResult()
    }

    override val thisClass: Class<out NavigationNode<CurrentDeviceChooserResult>>
        get() = CurrentDeviceChooser::class.java

    override fun provideMainContent(): @Composable (incomingResult: Navigator.IncomingResult?) -> Unit = {
        Column(
            Modifier.background(ThemeWrapper.ThemeHandle.current.colors.menuBackground)
        ) {

            BmwSingleLineHeader("Select Device to Inspect")

            //Unpack the Flow<Devices> here and make it a UI state.
            val currentDevices = (it?.requestParameters as CurrentDeviceChooserParameters)
                .currentDevices
                .collectAsState(listOf())

            SmoothScroll.SmoothScroll(
                modifier = Modifier,
                knobListenerService = knobListenerService,
                tag = "Current Device Chooser",
                logger = logger,
                items = currentDevices.value.let {
                    listOf(
                        TextMenuItem(
                            title = "Go Back",
                            onClicked = {
                                pairingManager.onUiResult(CurrentDeviceChooserResult.Cancelled)
                            }
                        )
                    ) + it.map { device ->
                            TextMenuItem(
                                title =
                                    "${device.alias} ; isConnected ${device.isConnected.toCheckBox()} ; isPaired ${device.isPaired.toCheckBox()}",
                                onClicked = {
                                    pairingManager.onUiResult(CurrentDeviceChooserResult.RequestViewDevice(device))
                                }
                            )
                    }
                }.toDynamicLambdas()

            )
            ScrollMenu.OneColumnScroll(
                items = currentDevices.value.map { device ->
                    TextMenuItem(
                        title =
                            "${device.alias} ; isConnected ${device.isConnected.toCheckBox()} ; isPaired ${device.isPaired.toCheckBox()}",
                        onClicked = {
                            pairingManager.onUiResult(CurrentDeviceChooserResult.RequestViewDevice(device))
                        }
                    )
                },
                onScrollListExitSelected = {
                    pairingManager.onUiResult(CurrentDeviceChooserResult.Cancelled)
                },
                displayOptions = ScrollMenu.ScrollListOptions(
                    itemsPerPage = 3,
                    isExitItemOnEveryPage = true,
                    isPageCountItemVisible = true,
                    showSpacerRow = false
                )
            )
        }
    }
}