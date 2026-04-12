package ca.stefanm.ca.stefanm.ibus.gui.networkSetup.activateConnection.screens.debug

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import ca.stefanm.ca.stefanm.ibus.gui.menu.widgets.screenMenu.SmoothScroll
import ca.stefanm.ca.stefanm.ibus.gui.networkSetup.activateConnection.dbus.GetConnectionsForApsUseCase
import ca.stefanm.ca.stefanm.ibus.gui.networkSetup.activateConnection.dbus.prereq.devices.get.all.GetDevicesUseCase
import ca.stefanm.ca.stefanm.ibus.gui.networkSetup.activateConnection.dbus.types.Nmt
import ca.stefanm.ca.stefanm.ibus.gui.networkSetup.activateConnection.ui.connectionList.ConnectionListItems
import ca.stefanm.ibus.autoDiscover.AutoDiscover
import ca.stefanm.ibus.di.ApplicationModule
import ca.stefanm.ibus.gui.menu.navigator.NavigationNode
import ca.stefanm.ibus.gui.menu.navigator.NavigationNodeTraverser
import ca.stefanm.ibus.gui.menu.navigator.Navigator
import ca.stefanm.ibus.gui.menu.widgets.BmwSingleLineHeader
import ca.stefanm.ibus.gui.menu.widgets.ItemChipOrientation
import ca.stefanm.ibus.gui.menu.widgets.MenuItem
import ca.stefanm.ibus.gui.menu.widgets.knobListener.KnobListenerService
import ca.stefanm.ibus.gui.menu.widgets.knobListener.dynamic.KnobObserverBuilderScope
import ca.stefanm.ibus.gui.menu.widgets.themes.ThemeWrapper
import ca.stefanm.ibus.lib.logging.Logger
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import org.freedesktop.networkmanager.Device
import javax.inject.Inject
import javax.inject.Named

@AutoDiscover
class GetConnectionsForApsUseCaseDebugScreen @Inject constructor(
    private val navigationNodeTraverser: NavigationNodeTraverser,
    @Named(ApplicationModule.KNOB_LISTENER_MAIN)
    private val knobListenerService: KnobListenerService,
    private val logger: Logger,
    private val getDevicesUseCase: GetDevicesUseCase,
    private val getConnectionsForApsUseCase: GetConnectionsForApsUseCase
) : NavigationNode<Nothing> {
    companion object {
        const val TAG = "GetConnectionsForApsUseCaseDebugScreen"
    }

    override val thisClass: Class<out NavigationNode<Nothing>>
        get() = GetConnectionsForApsUseCaseDebugScreen::class.java

    override fun provideMainContent(): @Composable ((Navigator.IncomingResult?) -> Unit) = {
        Column(Modifier
            .background(ThemeWrapper.ThemeHandle.current.colors.menuBackground)
            .fillMaxSize()
        ) {
            BmwSingleLineHeader("Debug: $TAG.")

            val scope = rememberCoroutineScope()

            //No Updates
//            val items = getDevicesUseCase
//                .getDevices()
//                .map {
//                    getConnectionsForApsUseCase.fillInNmtConnectConnectionForWirelessOnly(
//                        it.associate { it to emptyList() }
//                    )
//                }
//                .mapToViews()
//                .collectAsState(emptyList())

            val items = getDevicesUseCase
                .getDevices()
                .map {
                    it.associate {
                        it to emptyList<Nmt.NmtConnectConnection>()
                    }
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