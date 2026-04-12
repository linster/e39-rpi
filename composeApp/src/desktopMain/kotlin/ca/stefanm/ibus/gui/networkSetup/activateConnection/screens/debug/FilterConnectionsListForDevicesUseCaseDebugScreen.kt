package ca.stefanm.ca.stefanm.ibus.gui.networkSetup.activateConnection.screens.debug

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import ca.stefanm.ca.stefanm.ibus.gui.menu.widgets.screenMenu.SmoothScroll
import ca.stefanm.ca.stefanm.ibus.gui.networkSetup.activateConnection.dbus.GetConnectionsForApsUseCase
import ca.stefanm.ca.stefanm.ibus.gui.networkSetup.activateConnection.dbus.GetConnectionsForDeviceUseCase
import ca.stefanm.ca.stefanm.ibus.gui.networkSetup.activateConnection.dbus.prereq.connections.get.all.GetConnectionsUseCase
import ca.stefanm.ca.stefanm.ibus.gui.networkSetup.activateConnection.dbus.prereq.devices.get.all.GetDevicesUseCase
import ca.stefanm.ca.stefanm.ibus.gui.networkSetup.activateConnection.dbus.types.Nmt
import ca.stefanm.ca.stefanm.ibus.gui.networkSetup.activateConnection.screens.debug.GetConnectionsForApsUseCaseDebugScreen.Companion.TAG
import ca.stefanm.ibus.autoDiscover.AutoDiscover
import ca.stefanm.ibus.di.ApplicationModule
import ca.stefanm.ibus.gui.menu.navigator.NavigationNode
import ca.stefanm.ibus.gui.menu.navigator.NavigationNodeTraverser
import ca.stefanm.ibus.gui.menu.navigator.Navigator
import ca.stefanm.ibus.gui.menu.widgets.BmwSingleLineHeader
import ca.stefanm.ibus.gui.menu.widgets.knobListener.KnobListenerService
import ca.stefanm.ibus.gui.menu.widgets.themes.ThemeWrapper
import ca.stefanm.ibus.lib.logging.Logger
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Named

@AutoDiscover
class FilterConnectionsListForDevicesUseCaseDebugScreen @Inject constructor(
    private val navigationNodeTraverser: NavigationNodeTraverser,
    @Named(ApplicationModule.KNOB_LISTENER_MAIN)
    private val knobListenerService: KnobListenerService,
    private val logger: Logger,

    private val getDevicesUseCase: GetDevicesUseCase,
    private val getConnectionsForDevicesUseCase: GetConnectionsForDeviceUseCase,
    private val getConnectionsForApsUseCase: GetConnectionsForApsUseCase,
    private val filterConnectionsUseCase: GetConnectionsUseCase
) : NavigationNode<Nothing> {
    companion object {
        const val TAG = "FilterConnectionsListForDevicesUseCaseDebugScreen"
    }

    override val thisClass: Class<out NavigationNode<Nothing>>
        get() = FilterConnectionsListForDevicesUseCaseDebugScreen::class.java

    override fun provideMainContent(): @Composable ((Navigator.IncomingResult?) -> Unit) = {
        Column(Modifier
            .background(ThemeWrapper.ThemeHandle.current.colors.menuBackground)
            .fillMaxSize()
        ) {
            BmwSingleLineHeader("Debug: $TAG.")

            val items = getDevicesUseCase
                .getDevices()
                .let {
                    getConnectionsForDevicesUseCase.getConnectionsForDevices(it)
                }
                .let {
                    getConnectionsForApsUseCase.fillInNmtConnectConnectionForWirelessOnly(it)
                }
                .collectAsState(emptyMap())


            SmoothScroll.SmoothScroll(
                modifier = Modifier,
                knobListenerService = knobListenerService,
                tag = "GetConnectionsUseCaseDebugScreen",
                logger = logger,
                prependGoBackEntry = true,
                navigationNodeTraverser = navigationNodeTraverser,
                items = items.value.mapToViews()
            )
        }
    }
}