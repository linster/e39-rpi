package ca.stefanm.ca.stefanm.ibus.gui.networkSetup.activateConnection.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.onClick
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import ca.stefanm.ca.stefanm.ibus.gui.menu.widgets.screenMenu.SmoothScroll
import ca.stefanm.ca.stefanm.ibus.gui.networkSetup.activateConnection.dbus.GetConnectionListUseCase
import ca.stefanm.ca.stefanm.ibus.gui.networkSetup.activateConnection.dbus.GetConnectionListUseCase.ConnectionListItem
import ca.stefanm.ca.stefanm.ibus.gui.networkSetup.activateConnection.dbus.types.Nmt
import ca.stefanm.ca.stefanm.ibus.gui.networkSetup.activateConnection.ui.connectionList.ConnectionListItemViews
import ca.stefanm.ca.stefanm.ibus.gui.networkSetup.activateConnection.ui.connectionList.Throbbers
import ca.stefanm.ibus.annotations.screenflow.ScreenDoc
import ca.stefanm.ibus.autoDiscover.AutoDiscover
import ca.stefanm.ibus.di.ApplicationModule
import ca.stefanm.ibus.gui.menu.Notification
import ca.stefanm.ibus.gui.menu.navigator.NavigationNode
import ca.stefanm.ibus.gui.menu.navigator.NavigationNodeTraverser
import ca.stefanm.ibus.gui.menu.navigator.Navigator
import ca.stefanm.ibus.gui.menu.notifications.NotificationHub
import ca.stefanm.ibus.gui.menu.widgets.BmwSingleLineHeader
import ca.stefanm.ibus.gui.menu.widgets.ItemChipOrientation
import ca.stefanm.ibus.gui.menu.widgets.knobListener.KnobListenerService
import ca.stefanm.ibus.gui.menu.widgets.knobListener.dynamic.KnobObserverBuilderScope
import ca.stefanm.ibus.gui.menu.widgets.modalMenu.ModalMenuService
import ca.stefanm.ibus.gui.menu.widgets.modalMenu.SidePanelMenu
import ca.stefanm.ibus.gui.menu.widgets.modalMenu.SidePanelMenu.InfoLabel
import ca.stefanm.ibus.gui.menu.widgets.screenMenu.FullScreenMenu
import ca.stefanm.ibus.gui.menu.widgets.screenMenu.TextMenuItem
import ca.stefanm.ibus.gui.menu.widgets.themes.ThemeWrapper
import ca.stefanm.ibus.lib.logging.Logger
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Named
import kotlin.collections.component1
import kotlin.collections.component2
import kotlin.time.Duration.Companion.seconds

@ScreenDoc(
    screenName = "ActivateConnectionScreen",
    description = "Allows a connection to be activated and deactivated."
)
@ScreenDoc.AllowsGoBack
@AutoDiscover
class ActivateConnectionScreen @Inject constructor(
    @Named(ApplicationModule.KNOB_LISTENER_MAIN)
    private val knobListenerService: KnobListenerService,
    private val modalMenuService: ModalMenuService,
    private val logger : Logger,
    private val notificationHub: NotificationHub,
    private val navigationNodeTraverser: NavigationNodeTraverser,
    private val throbbers: Throbbers,
    private val getConnectionListUseCase: GetConnectionListUseCase
) : NavigationNode<Nothing> {

    companion object {
        const val TAG = "ActivateConnectionScreen"
    }

    override val thisClass: Class<out NavigationNode<Nothing>>
        get() = ActivateConnectionScreen::class.java

    override fun provideMainContent(): @Composable ((Navigator.IncomingResult?) -> Unit) = {

        Column(Modifier
            .background(ThemeWrapper.ThemeHandle.current.colors.menuBackground)
            .fillMaxSize()
        ) {
            BmwSingleLineHeader("Activate Connection")

            val entries = getConnectionListUseCase.getConnectionItems().collectAsState(emptyList())

            SmoothScroll.SmoothScroll(
                modifier = Modifier,
                knobListenerService = knobListenerService,
                tag = TAG,
                logger = logger,
                prependGoBackEntry = true,
                navigationNodeTraverser = navigationNodeTraverser,
                items = entries.value.mapToViews()
            )
        }
    }

    fun List<ConnectionListItem>.mapToViews() : List<@Composable KnobObserverBuilderScope.(Int, Int) -> Unit> {
        val views = mutableListOf<@Composable KnobObserverBuilderScope.(Int, Int) -> Unit>()
        this.forEach { item ->
            when (item) {
                is ConnectionListItem.DeviceHeader -> {
                    views.add { allocatedIndex, currentIndex ->
                        ConnectionListItemViews.ConnectionListDivider(
                            dividerHeader = item.name,
                            onClicked =  {
                                desktopClickOnDevice(item.nmtConnectDevice)
                            }
                        )
                    }
                }
                is ConnectionListItem.ConnectionListConnection.OtherConnection -> {
                    views.add { allocatedIndex, currentIndex ->
                        ConnectionListItemViews.Connection(
                            connectionName = item.name,
                            strength = null,
                            isConnected = item.isConnected,
                            modifier = Modifier,
                            chipOrientation = ItemChipOrientation.W,
                            isSelected = allocatedIndex == currentIndex,
                            onClicked = CallWhen(currentIndexIs = allocatedIndex) {
                                openSidebarForConnection(item)
                            }
                        )
                    }
                }
                is ConnectionListItem.ConnectionListConnection.WifiAccessPoint -> {
                    views.add { allocatedIndex, currentIndex ->
                        ConnectionListItemViews.Connection(
                            connectionName = item.ssid,
                            strength = item.strength,
                            isConnected = item.isConnected,
                            modifier = Modifier,
                            chipOrientation = ItemChipOrientation.W,
                            isSelected = allocatedIndex == currentIndex,
                            onClicked = CallWhen(currentIndexIs = allocatedIndex) {
                                openSidebarForConnection(item)
                            }
                        )
                    }

                }
            }
        }
        return views
    }


    fun openSidebarForConnection(conn: ConnectionListItem.ConnectionListConnection.WifiAccessPoint) {
        modalMenuService.showSidePaneOverlay(darkenBackground = true) {
            SidePanelMenu.SidePanelMenu(
                title = "Wifi Connection Info: ${conn.ssid}",
                @Composable {

                    InfoLabel("SSID: ${conn.ssid}")
                    InfoLabel("Strength: \t (${conn.strength}) ${ConnectionListItemViews.toStrengthBars(conn.strength)}")


                    InfoLabel("Object Path (Access Point): ${
                        conn.nmtConnectConnection.ap
                            ?.objectPath
                            ?.let { path -> path.split('/').takeLast(2).fold("", { acc, seg -> "$acc/$seg" })}
                    }")



                },
                listOf(
                    if (conn.isConnected) {
                        TextMenuItem("Disconnect", onClicked = { disconnect(conn.nmtConnectConnection)})
                    } else {
                        TextMenuItem("Connect", onClicked = { connect(conn.nmtConnectConnection)})
                    },
                    TextMenuItem("Go Back", onClicked = { modalMenuService.closeSidePaneOverlay(true)})
                )
            )
        }

    }


    fun openSidebarForConnection(conn: ConnectionListItem.ConnectionListConnection.OtherConnection) {
        modalMenuService.showSidePaneOverlay(darkenBackground = true) {
            SidePanelMenu.SidePanelMenu(
                title = "Connection Info: ${conn.name}",
                @Composable {

                    InfoLabel("Object Path (Connection): ${conn.nmtConnectConnection.conn?.objectPath}")
                    InfoLabel("Object Path (Active): ${conn.nmtConnectConnection.active?.objectPath}")
                },
                listOf(
                    if (conn.isConnected) {
                        TextMenuItem("Disconnect", onClicked = { disconnect(conn.nmtConnectConnection)})
                    } else {
                        TextMenuItem("Connect", onClicked = { connect(conn.nmtConnectConnection)})
                    },
                    TextMenuItem("Go Back", onClicked = { modalMenuService.closeSidePaneOverlay(true)})
                )
            )
        }
    }

    private fun desktopClickOnDevice(device : Nmt.NmtConnectDevice) {
        //Just for desktop debugging, not actually selectable with a scroll wheel.
        logger.d(TAG, "desktopClickOnDevice($device)")
        modalMenuService.showModalWaitDialog(
            image = Notification.NotificationImage.NONE,
            throbber = false,
            headerText = device.device.objectPath?.split('/')?.takeLast(2).toString(),
            bodyText = device.toString(),
            autoCloseTimeout = 5.seconds
        )
    }

    private fun connect(connectDevice: Nmt.NmtConnectConnection) {
        //Hide the sidebar
        //Show the throbber
        //start the usecase to make the connection
    }

    private fun disconnect(connectDevice: Nmt.NmtConnectConnection) {
        //Hide the sidebar
        //Show the throbber
        //start the usecase to do the disconnect
    }

}