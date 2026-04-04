package ca.stefanm.ca.stefanm.ibus.gui.networkSetup.activateConnection.screens.debug

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import ca.stefanm.ca.stefanm.ibus.gui.menu.widgets.screenMenu.SmoothScroll
import ca.stefanm.ca.stefanm.ibus.gui.networkSetup.activateConnection.dbus.GetConnectionListUseCase.CollatedDeviceInformation
import ca.stefanm.ca.stefanm.ibus.gui.networkSetup.activateConnection.dbus.GetDevicesUseCase
import ca.stefanm.ca.stefanm.ibus.gui.networkSetup.activateConnection.dbus.GetDisambiguatedDeviceNameUseCase
import ca.stefanm.ca.stefanm.ibus.gui.networkSetup.activateConnection.ui.connectionList.ConnectionListItems
import ca.stefanm.ibus.annotations.screenflow.ScreenDoc
import ca.stefanm.ibus.autoDiscover.AutoDiscover
import ca.stefanm.ibus.di.ApplicationModule
import ca.stefanm.ibus.gui.menu.navigator.NavigationNode
import ca.stefanm.ibus.gui.menu.navigator.NavigationNodeTraverser
import ca.stefanm.ibus.gui.menu.navigator.Navigator
import ca.stefanm.ibus.gui.menu.widgets.BmwSingleLineHeader
import ca.stefanm.ibus.gui.menu.widgets.ItemChipOrientation
import ca.stefanm.ibus.gui.menu.widgets.MenuItem
import ca.stefanm.ibus.gui.menu.widgets.knobListener.KnobListenerService
import ca.stefanm.ibus.gui.menu.widgets.knobListener.dynamic.KnobObserverBuilder
import ca.stefanm.ibus.gui.menu.widgets.knobListener.dynamic.KnobObserverBuilderScope
import ca.stefanm.ibus.gui.menu.widgets.knobListener.dynamic.KnobObserverBuilderState.Companion.setupListener
import ca.stefanm.ibus.gui.menu.widgets.themes.ThemeWrapper
import ca.stefanm.ibus.lib.logging.Logger
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Named
import kotlin.Int
import kotlin.Unit

@ScreenDoc(
    screenName = "DeviceListScreen",
    description = "Shows a list of devices picked up from NetworkManager " +
            "as an intermediate step towards a working ActivateConnection window.",
    navigatesTo = []
)
@ScreenDoc.AllowsGoBack
@AutoDiscover
class DeviceListScreen @Inject constructor(
    private val navigationNodeTraverser: NavigationNodeTraverser,
    @Named(ApplicationModule.KNOB_LISTENER_MAIN)
    private val knobListenerService: KnobListenerService,
    private val logger: Logger,
    private val getDevicesUseCase: GetDevicesUseCase,
    private val getDisambiguatedDeviceNameUseCase: GetDisambiguatedDeviceNameUseCase
) : NavigationNode<Nothing> {

    override val thisClass: Class<out NavigationNode<Nothing>>
        get() = DeviceListScreen::class.java

    override fun provideMainContent(): @Composable ((Navigator.IncomingResult?) -> Unit) = {

        Column(Modifier
            .background(ThemeWrapper.ThemeHandle.current.colors.menuBackground)
            .fillMaxSize()
        ) {
            BmwSingleLineHeader("Activate Connection")

            val devices = getDevicesUseCase.getDevices()
                .map {
                    getDisambiguatedDeviceNameUseCase.getDisambiguatedNames(it)
                }.map {
                    it.entries.map { (device, name) ->
                        CollatedDeviceInformation(
                            device = device,
                            disambiguatedName = name
                        )
                    }
                }.collectAsState(emptyList())


            SmoothScroll.SmoothScroll(
                modifier = Modifier,
                knobListenerService = knobListenerService,
                tag = "DeviceListScreen",
                logger = logger,
                prependGoBackEntry = true,
                items = devices.value.map { deviceInfo ->
                    { _, _ ->
                        ConnectionListItems.ConnectionListDivider(
                            dividerHeader = deviceInfo.disambiguatedName ?: "null device name",
                            modifier = Modifier,
                        )
                    }
                }
            )
        }

    }
}