package ca.stefanm.ibus.gui.generalSettings

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ca.stefanm.ibus.autoDiscover.AutoDiscover
import ca.stefanm.ibus.car.platform.ConfigurablePlatform
import ca.stefanm.ibus.car.platform.ConfigurablePlatformServiceRunStatusViewer
import ca.stefanm.ibus.car.platform.PlatformService
import ca.stefanm.ibus.gui.menu.navigator.NavigationNode
import ca.stefanm.ibus.gui.menu.navigator.NavigationNodeTraverser
import ca.stefanm.ibus.gui.menu.navigator.Navigator
import ca.stefanm.ibus.gui.menu.widgets.BmwSingleLineHeader
import ca.stefanm.ibus.gui.menu.widgets.ChipItemColors
import ca.stefanm.ibus.gui.menu.widgets.modalMenu.ModalMenuService
import ca.stefanm.ibus.gui.menu.widgets.screenMenu.HalfScreenMenu
import ca.stefanm.ibus.gui.menu.widgets.screenMenu.ScrollMenu
import ca.stefanm.ibus.gui.menu.widgets.screenMenu.TextMenuItem
import ca.stefanm.ibus.lib.logging.Logger
import javax.inject.Inject

@AutoDiscover
class CarServiceConfigScreen @Inject constructor(
    private val navigationNodeTraverser: NavigationNodeTraverser,
    private val modalMenuService: ModalMenuService,
    private val configurablePlatform: ConfigurablePlatform,
    private val logger: Logger
) : NavigationNode<Nothing> {

    override val thisClass: Class<out NavigationNode<Nothing>>
        get() = CarServiceConfigScreen::class.java

    override fun provideMainContent(): @Composable (incomingResult: Navigator.IncomingResult?) -> Unit = {

        val entries = configurablePlatform.servicesRunning.collectAsState()

        ScrollMenu.OneColumnScroll(
            displayOptions = ScrollMenu.ScrollListOptions(
                exitListItemLabel = "Go Back",
                isPageCountItemVisible = false,
                isExitItemOnEveryPage = false,
                showSpacerRow = false,
                itemsPerPage = 6
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

    private fun groupInfo(
        group : ConfigurablePlatformServiceRunStatusViewer.RunStatusRecordGroup
    ) : @Composable () -> Unit = {
        SidePanelServiceInfo(
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
        SidePanelServiceInfo(
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

    @Composable
    private fun SidePanelServiceInfo(
        title : String? = null,
        text : @Composable () -> Unit,
        buttons : List<TextMenuItem>
    ) {
        SidePanelServiceInfo(title) {
            Column(Modifier.fillMaxHeight(), verticalArrangement = Arrangement.SpaceBetween) {
                Column(Modifier.padding(horizontal = 10.dp, vertical = 15.dp)) {
                    text()
                }
                HalfScreenMenu.OneColumn(
                    alignment = Alignment.End,
                    fullWidth = true,
                    items = buttons
                )
            }
        }
    }

    @Composable
    private fun SidePanelServiceInfo(title : String? = null, contents : @Composable () -> Unit) {
        Column(
            Modifier
                .fillMaxSize()
                .background(ChipItemColors.MenuBackground)
                .border(width = 4.dp, color = Color(61, 112, 176, 255))
                .shadow(4.dp, RectangleShape)
        ) {
            if (title != null) {
                BmwSingleLineHeader(title)
            }
            contents()
        }
    }

    @Composable
    private fun InfoLabel(text : String, weight : FontWeight = FontWeight.Normal) {
        Text(
            text = text,
            color = ChipItemColors.TEXT_WHITE,
            fontSize = 18.sp,
            fontWeight = weight
        )
    }
}