package ca.stefanm.ibus.gui.generalSettings

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.text.font.FontWeight
import ca.stefanm.ibus.autoDiscover.AutoDiscover
import ca.stefanm.ibus.car.platform.ConfigurablePlatform
import ca.stefanm.ibus.car.platform.ConfigurablePlatformServiceRunStatusViewer
import ca.stefanm.ibus.car.platform.PlatformService
import ca.stefanm.ibus.gui.menu.navigator.NavigationNode
import ca.stefanm.ibus.gui.menu.navigator.NavigationNodeTraverser
import ca.stefanm.ibus.gui.menu.navigator.Navigator
import ca.stefanm.ibus.gui.menu.widgets.BmwSingleLineHeader
import ca.stefanm.ibus.gui.menu.widgets.modalMenu.ModalMenuService
import ca.stefanm.ibus.gui.menu.widgets.modalMenu.SidePanelMenu.InfoLabel
import ca.stefanm.ibus.gui.menu.widgets.modalMenu.SidePanelMenu.SidePanelMenu
import ca.stefanm.ibus.gui.menu.widgets.screenMenu.ScrollMenu
import ca.stefanm.ibus.gui.menu.widgets.screenMenu.TextMenuItem
import javax.inject.Inject

@AutoDiscover
class CarServiceConfigScreen @Inject constructor(
    private val navigationNodeTraverser: NavigationNodeTraverser,
    private val modalMenuService: ModalMenuService,
    private val configurablePlatform: ConfigurablePlatform
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
        SidePanelMenu(
            "Service Group Info",
            text = @Composable {
                InfoLabel("${group.name}", FontWeight.Bold)
                InfoLabel("")
                InfoLabel("${group.description}")
            },
            buttons = listOf(
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
                    title = "Set as Default",
                    onClicked = {
                        TODO("CONFIG FOR SERVICE RUNSTATES")
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