package ca.stefanm.ibus.gui.networkSetup.activateConnection.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.onClick
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import ca.stefanm.ibus.gui.networkSetup.activateConnection.dbus.prereq.devices.convert.ConvertDeviceToWirelessUseCase
import ca.stefanm.ibus.gui.menu.widgets.screenMenu.SmoothScroll
import ca.stefanm.ibus.gui.networkSetup.activateConnection.dbus.ConnectNmtDeviceConnectionUseCase
import ca.stefanm.ibus.gui.networkSetup.activateConnection.dbus.DisconnectNmtDeviceConnectionUseCase
import ca.stefanm.ibus.gui.networkSetup.activateConnection.dbus.GetConnectionListUseCase
import ca.stefanm.ibus.gui.networkSetup.activateConnection.dbus.GetConnectionListUseCase.ConnectionListItem
import ca.stefanm.ibus.gui.networkSetup.activateConnection.dbus.types.NM80211Mode
import ca.stefanm.ibus.gui.networkSetup.activateConnection.dbus.types.Nmt
import ca.stefanm.ibus.gui.networkSetup.activateConnection.ui.connectionList.ConnectionListItemViews
import ca.stefanm.ibus.gui.networkSetup.activateConnection.ui.connectionList.Throbbers
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
import ca.stefanm.ibus.gui.networkSetup.activateConnection.dbus.types.NM80211ApSecurityFlags
import ca.stefanm.ibus.lib.logging.Logger
import androidx.compose.runtime.LaunchedEffect
import ca.stefanm.ibus.gui.networkSetup.activateConnection.dbus.types.WifiSecurity
import ca.stefanm.ibus.gui.networkSetup.editConnection.dbus.SavedConnectionsUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.withContext
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
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
    private val getConnectionListUseCase: GetConnectionListUseCase,
    private val convertDeviceToWirelessUseCase: ConvertDeviceToWirelessUseCase,
    private val connectNmtDeviceConnectionUseCase: ConnectNmtDeviceConnectionUseCase,
    private val disconnectNmtDeviceConnectionUseCase: DisconnectNmtDeviceConnectionUseCase,
    private val savedConnectionsUseCase: SavedConnectionsUseCase
) : NavigationNode<Nothing> {

    companion object {
        const val TAG = "ActivateConnectionScreen"
    }

    //The navigator replays the last result whenever this screen is shown again,
    //so remember which password result we've already acted on.
    private var lastHandledSecretResult : PromptSecretScreen.PromptSecretResult? = null

    override val thisClass: Class<out NavigationNode<Nothing>>
        get() = ActivateConnectionScreen::class.java

    override fun provideMainContent(): @Composable ((Navigator.IncomingResult?) -> Unit) = { incomingResult ->

        LaunchedEffect(incomingResult) {
            val result = incomingResult?.result
            if (result is PromptSecretScreen.PromptSecretResult.SecretEntered && result !== lastHandledSecretResult) {
                lastHandledSecretResult = result
                connectWithSecret(result.connection, result.secret)
            }
        }

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
                is ConnectionListItem.SectionHeader -> {
                    views.add { allocatedIndex, currentIndex ->
                        ConnectionListItemViews.ConnectionListDivider(
                            dividerHeader = item.name
                        )
                    }
                }
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
        //Look up the saved profile for the network first, it decides which actions we can offer.
        GlobalScope.launch(Dispatchers.IO) {
            val saved = try {
                savedConnectionsUseCase.findSavedWifiConnection(conn.ssid)
            } catch (e : Throwable) {
                logger.e(TAG, "Could not look up saved profile for ${conn.ssid}", e)
                null
            }
            showWifiSidebar(conn, saved)
        }
    }

    private fun showWifiSidebar(
        conn: ConnectionListItem.ConnectionListConnection.WifiAccessPoint,
        saved : SavedConnectionsUseCase.SavedConnection?
    ) {
        modalMenuService.showSidePaneOverlay(darkenBackground = true) {
            SidePanelMenu.SidePanelMenu(
                title = "Wifi Connection Info",
                @Composable {

                    InfoLabel("SSID: ${conn.ssid}")
                    InfoLabel("Saved: ${saved?.id ?: "No"}")
                    InfoLabel("Strength: \t (${conn.strength}) ${ConnectionListItemViews.toStrengthBars(conn.strength)}")

                    InfoLabel("AP Mode: ${conn.nmtConnectConnection.ap?.mode?.let { NM80211Mode.toSidebarList(it.toInt()) }}")

                    InfoLabel("AP Security: ${
                        conn.nmtConnectConnection
                            .ap
                            ?.let { ap ->
                                val rsnFlags = ap.rsnFlags
                                    .let { NM80211ApSecurityFlags.flagsFromInt(it.toInt()) }
                                    .map { NM80211ApSecurityFlags.prettyPrint(it) }
                                    .toSet()
                                val wpaFlags = ap.wpaFlags
                                    .let { NM80211ApSecurityFlags.flagsFromInt(it.toInt()) }
                                    .map { NM80211ApSecurityFlags.prettyPrint(it) }
                                    .toSet()
                                
                                rsnFlags.union(wpaFlags).toString()
                            } ?: "Unknown"
                            }")
                    InfoLabel("Object Path (Access Point): ${
                        conn.nmtConnectConnection.ap
                            ?.objectPath
                            ?.let { path -> path.split('/').takeLast(2).fold("", { acc, seg -> "$acc/$seg" })}
                    }")



                },
                listOfNotNull(
                    if (conn.isConnected) {
                        TextMenuItem("Disconnect", onClicked = { disconnect(conn.nmtConnectConnection)})
                    } else {
                        TextMenuItem("Connect", onClicked = { connect(conn.nmtConnectConnection)})
                    },
                    if (saved != null && saved.hasWifiPassword) {
                        TextMenuItem("Change Password", onClicked = {
                            modalMenuService.closeSidePaneOverlay(true)
                            navigationNodeTraverser.promptForWifiSecret(
                                PromptSecretScreen.PromptSecretParameters(
                                    networkName = conn.ssid,
                                    security = if (saved.keyMgmt == "sae") WifiSecurity.SAE else WifiSecurity.WPA_PSK,
                                    isChangingPassword = true,
                                    connection = conn.nmtConnectConnection
                                )
                            )
                        })
                    } else null,
                    if (saved != null) {
                        TextMenuItem("Forget Network", onClicked = { confirmForget(conn) })
                    } else null,
                    TextMenuItem("Go Back", onClicked = { modalMenuService.closeSidePaneOverlay(true)})
                )
            )
        }

    }


    private fun confirmForget(conn: ConnectionListItem.ConnectionListConnection.WifiAccessPoint) {
        modalMenuService.showSidePaneOverlay(darkenBackground = true) {
            SidePanelMenu.SidePanelMenu(
                title = "Forget Network?",
                @Composable {
                    InfoLabel("Forget ${conn.ssid}?")
                    InfoLabel("The saved password will be deleted.")
                },
                listOf(
                    TextMenuItem("Forget", onClicked = {
                        modalMenuService.closeSidePaneOverlay(true)
                        forget(conn.ssid)
                    }),
                    TextMenuItem("Cancel", onClicked = { modalMenuService.closeSidePaneOverlay(true) })
                )
            )
        }
    }

    fun openSidebarForConnection(conn: ConnectionListItem.ConnectionListConnection.OtherConnection) {
        modalMenuService.showSidePaneOverlay(darkenBackground = true) {
            SidePanelMenu.SidePanelMenu(
                title = "Connection Info",
                @Composable {
                    InfoLabel("Name: ${conn.name}")
                    InfoLabel("Object Path (Connection): ${conn.nmtConnectConnection.conn?.objectPath}")
                    InfoLabel("Has Active: ${conn.nmtConnectConnection.hasActiveConnection}")
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
            headerText = "",
            bodyText = device.device.objectPath?.split('/')?.takeLast(2).toString(),
            autoCloseTimeout = 5.seconds
        )
    }

    private fun connect(connectDevice: Nmt.NmtConnectConnection) {
        GlobalScope.launch {
            modalMenuService.closeSidePaneOverlay(true)
            val outcome = connectNmtDeviceConnectionUseCase.connect(connectDevice)
            if (outcome is ConnectNmtDeviceConnectionUseCase.ConnectOutcome.NeedsSecret) {
                withContext(Dispatchers.Main) {
                    navigationNodeTraverser.promptForWifiSecret(
                        PromptSecretScreen.PromptSecretParameters(
                            networkName = connectDevice.ssid ?: "Wi-Fi",
                            security = outcome.security,
                            isChangingPassword = false,
                            connection = connectDevice
                        )
                    )
                }
            }
        }
    }

    private fun connectWithSecret(connectDevice: Nmt.NmtConnectConnection, secret : String) {
        GlobalScope.launch {
            connectNmtDeviceConnectionUseCase.connectWifiWithSecret(connectDevice, secret)
        }
    }

    private fun disconnect(connectDevice: Nmt.NmtConnectConnection) {
        disconnectNmtDeviceConnectionUseCase.disconnect(connectDevice)
        modalMenuService.closeSidePaneOverlay(true)
    }

    private fun forget(ssid : String) {
        GlobalScope.launch(Dispatchers.IO) {
            try {
                val deleted = savedConnectionsUseCase.forgetWifi(ssid)
                notificationHub.postNotificationBackground(Notification(
                    topText = "Forgot network",
                    contentText = if (deleted > 1) "$ssid ($deleted saved profiles)" else ssid
                ))
            } catch (e : Throwable) {
                logger.e(TAG, "Could not forget $ssid", e)
                notificationHub.postNotificationBackground(Notification(
                    image = Notification.NotificationImage.ALERT_TRIANGLE,
                    topText = "Could not forget $ssid",
                    contentText = e.message ?: ""
                ))
            }
        }
    }

}