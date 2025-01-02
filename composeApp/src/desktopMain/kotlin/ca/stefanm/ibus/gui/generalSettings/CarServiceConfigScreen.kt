package ca.stefanm.ibus.gui.generalSettings

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.text.font.FontWeight
import ca.stefanm.ibus.gui.docs.CarPlatformScreenDocPartition
import ca.stefanm.ibus.annotations.screenflow.ScreenDoc
import ca.stefanm.ibus.autoDiscover.AutoDiscover
import ca.stefanm.ibus.car.platform.ConfigurablePlatform
import ca.stefanm.ibus.car.platform.ConfigurablePlatformServiceRunStatusViewer
import ca.stefanm.ibus.car.platform.PlatformService
import ca.stefanm.ibus.configuration.ConfigurationStorage
import ca.stefanm.ibus.configuration.E39Config
import ca.stefanm.ibus.gui.menu.Notification
import ca.stefanm.ibus.gui.menu.navigator.NavigationNode
import ca.stefanm.ibus.gui.menu.navigator.NavigationNodeTraverser
import ca.stefanm.ibus.gui.menu.navigator.Navigator
import ca.stefanm.ibus.gui.menu.notifications.NotificationHub
import ca.stefanm.ibus.gui.menu.widgets.BmwSingleLineHeader
import ca.stefanm.ibus.gui.menu.widgets.modalMenu.ModalMenuService
import ca.stefanm.ibus.gui.menu.widgets.modalMenu.SidePanelMenu.InfoLabel
import ca.stefanm.ibus.gui.menu.widgets.modalMenu.SidePanelMenu.SidePanelMenu
import ca.stefanm.ibus.gui.menu.widgets.screenMenu.ScrollMenu
import ca.stefanm.ibus.gui.menu.widgets.screenMenu.TextMenuItem
import kotlinx.coroutines.flow.asFlow
import javax.inject.Inject

@ScreenDoc(
    screenName = "CarServiceConfigScreen",
    description = "Enable, Disable, Restarts car platform services."
)
@ScreenDoc.AllowsGoBack
@CarPlatformScreenDocPartition
@AutoDiscover
class CarServiceConfigScreen @Inject constructor(
    private val navigationNodeTraverser: NavigationNodeTraverser,
    private val modalMenuService: ModalMenuService,
    private val configurablePlatform: ConfigurablePlatform,
    private val configurationStorage: ConfigurationStorage,
    private val notificationHub: NotificationHub
) : NavigationNode<Nothing> {

    override val thisClass: Class<out NavigationNode<Nothing>>
        get() = CarServiceConfigScreen::class.java

    override fun provideMainContent(): @Composable (incomingResult: Navigator.IncomingResult?) -> Unit = {
        Column {
            BmwSingleLineHeader("Car Service Configuration")

            val entries = configurablePlatform.servicesRunning.collectAsState()

            ScrollMenu.OneColumnScroll(
                displayOptions = ScrollMenu.ScrollListOptions(
                    exitListItemLabel = "Go Back",
                    isPageCountItemVisible = false,
                    isExitItemOnEveryPage = false,
                    showSpacerRow = false,
                    itemsPerPage = 5
                ),
                onScrollListExitSelected = {
                    modalMenuService.closeSidePaneOverlay(true)
                    navigationNodeTraverser.goBack()
                },
                items = entries.value.map { group ->
                    val groupItem = TextMenuItem(
                        title = "${group.name} Group",
                        onClicked = {
                            modalMenuService.showSidePaneOverlay(true, groupInfo(group))
                        }
                    )

                    val groupChildren = group.children.map { service ->
                        TextMenuItem(
                            title = "${service.name} ${service.runStatus.collectAsState(PlatformService.RunStatus.STOPPED).value.name}",
                            onClicked = {
                                modalMenuService.showSidePaneOverlay(true, serviceInfo(service))
                            }
                        )
                    }
                    listOf(groupItem, *groupChildren.toTypedArray())
                }.flatten()
            )
        }
    }

    private fun groupInfo(
        group : ConfigurablePlatformServiceRunStatusViewer.RunStatusRecordGroup
    ) : @Composable () -> Unit = {

        val isEnabled = produceState(true) {
            val enabledServicesList = configurationStorage.config[E39Config.CarPlatformConfigSpec._listOfServiceGroupsOnStartup]
            value = if (enabledServicesList.isEmpty()) {
                true
            } else {
                configurationStorage.config[E39Config.CarPlatformConfigSpec._listOfServiceGroupsOnStartup].any { it == group.name }
            }
        }

        fun enableServiceForStartup(groupName : String) {
            configurationStorage.config[E39Config.CarPlatformConfigSpec._listOfServiceGroupsOnStartup] =
                configurationStorage.config[E39Config.CarPlatformConfigSpec._listOfServiceGroupsOnStartup]
                    .toMutableSet()
                    .apply { add(groupName) }
                    .toList()
        }

        fun disableServiceForStartup(groupName : String) {
            configurationStorage.config[E39Config.CarPlatformConfigSpec._listOfServiceGroupsOnStartup] =
                configurationStorage.config[E39Config.CarPlatformConfigSpec._listOfServiceGroupsOnStartup]
                    .toMutableSet()
                    .apply { remove(groupName) }
                    .toList()
        }

        fun notifyChangeOnReboot() {
            notificationHub.postNotificationBackground(Notification(
                image = Notification.NotificationImage.ALERT_CIRCLE,
                topText = "",
                contentText = "Change will take effect on platform reboot."
            ))
        }

        SidePanelMenu(
            "Service Group Info",
            text = @Composable {
                InfoLabel(group.name, FontWeight.Bold)
                InfoLabel(if (isEnabled.value) {
                    "Enabled on Startup"
                } else { "Disabled on Startup" }, FontWeight.ExtraLight)
                InfoLabel(group.description)
            },
            buttons = listOf(
                TextMenuItem(
                    title = "Enable on Startup",
                    onClicked = {
                        enableServiceForStartup(group.name)
                        notifyChangeOnReboot()
                        modalMenuService.closeSidePaneOverlay(true)
                    }
                ),
                TextMenuItem(
                    title = "Disable on Startup",
                    onClicked = {
                        disableServiceForStartup(group.name)
                        notifyChangeOnReboot()
                        modalMenuService.closeSidePaneOverlay(true)
                    }
                ),
                TextMenuItem(
                    title = "Go Back",
                    onClicked = {
                        modalMenuService.closeSidePaneOverlay(true)
                    }
                )
            )
        )
    }

    private fun serviceInfo(
        service : ConfigurablePlatformServiceRunStatusViewer.RunStatusRecordService
    ) : @Composable () -> Unit = {
        SidePanelMenu(
            title = "Service Info",
            text = @Composable {
                InfoLabel(service.name, FontWeight.Bold)
                InfoLabel("${service.runStatus.collectAsState(rememberCoroutineScope()).value}", FontWeight.Light)
                InfoLabel("")
                InfoLabel(service.description)
            },
            buttons = listOf(
                TextMenuItem(
                    title = "Start Service",
                    onClicked = {
                        service.startService()
                    }
                ),
                TextMenuItem(
                    title = "Stop Service",
                    onClicked = {
                        service.stopService()
                    }
                ),
                TextMenuItem(
                    title = "Go Back",
                    onClicked = {
                        modalMenuService.closeSidePaneOverlay(true)
                    }
                )
            )
        )
    }
}