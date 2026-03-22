package ca.stefanm.ca.stefanm.ibus.gui.generalSettings

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import ca.stefanm.ca.stefanm.ibus.car.desktop.input.PowermateSensitivityConfig
import ca.stefanm.ibus.annotations.screenflow.ScreenDoc
import ca.stefanm.ibus.autoDiscover.AutoDiscover
import ca.stefanm.ibus.configuration.E39Config
import ca.stefanm.ibus.gui.menu.navigator.NavigationNode
import ca.stefanm.ibus.gui.menu.navigator.NavigationNodeTraverser
import ca.stefanm.ibus.gui.menu.navigator.Navigator
import ca.stefanm.ibus.gui.menu.widgets.BmwSingleLineHeader
import ca.stefanm.ibus.gui.menu.widgets.modalMenu.ModalMenuService
import ca.stefanm.ibus.gui.menu.widgets.modalMenu.SidePanelMenu
import ca.stefanm.ibus.gui.menu.widgets.modalMenu.keyboard.Keyboard
import ca.stefanm.ibus.gui.menu.widgets.screenMenu.FullScreenMenu
import ca.stefanm.ibus.gui.menu.widgets.screenMenu.TextMenuItem
import ca.stefanm.ibus.lib.logging.Logger
import javax.inject.Inject

@ScreenDoc(
    screenName = "GriffinPowermateConfigScreen",
    description = "Set the sensititivy of the Griffin Powermate (on Desktop)"
)
@ScreenDoc.AllowsGoBack
@AutoDiscover
class GriffinPowermateConfigScreen @Inject constructor(
    private val navigationNodeTraverser: NavigationNodeTraverser,
    private val logger: Logger,
    private val modalMenuService: ModalMenuService,
    private val powermateSensitivityConfig: PowermateSensitivityConfig,
) : NavigationNode<Nothing> {

    override val thisClass: Class<out NavigationNode<Nothing>>
        get() = GriffinPowermateConfigScreen::class.java

    override fun provideMainContent(): @Composable (incomingResult: Navigator.IncomingResult?) -> Unit = { params ->
        Column(Modifier.fillMaxSize()) {
            BmwSingleLineHeader("Griffin Powermate Sensitivity")

            FullScreenMenu.OneColumn(
                listOf(
                    TextMenuItem(
                        title = "Go Back",
                        onClicked = {
                            navigationNodeTraverser.navigateToRoot()
                        }
                    ),
                    TextMenuItem(
                        title = "Set Normal Menu Skip...",
                        onClicked = { setNormalMenuSkip() }
                    ),
                    TextMenuItem(
                        title = "Set Maps Scroll Skip...",
                        onClicked = { setMapSkip() }
                    )
                )
            )
        }
    }

    private fun setNormalMenuSkip() {
        modalMenuService.showSidePaneOverlay(true) {
            SidePanelMenu.SidePanelMenu(
                "Griffin PowerMate Knob Sensitivity",
                text = {
                    SidePanelMenu.InfoLabel("Set the sensitivity for the Normal menu.", FontWeight.Bold)

                },
                buttons =  listOf(
                    TextMenuItem(
                        title = "Skip: ${powermateSensitivityConfig.getNormalSkip()}",
                        onClicked = {
                            modalMenuService.closeSidePaneOverlay(false)
                            modalMenuService.showKeyboard(
                                Keyboard.KeyboardType.NUMERIC,
                                prefilled = powermateSensitivityConfig.getNormalSkip().toString(),
                                onTextEntered = { enteredString ->
                                    enteredString.toIntOrNull()?.let {
                                        powermateSensitivityConfig.setNormalSkip(it)
                                    }
                                    setNormalMenuSkip()
                                }
                            )
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

    private fun setMapSkip() {
        modalMenuService.showSidePaneOverlay(true) {
            SidePanelMenu.SidePanelMenu(
                "Griffin PowerMate Knob Sensitivity",
                text = {
                    SidePanelMenu.InfoLabel("Set the sensitivity for the Map menu.", FontWeight.Bold)

                },
                buttons =  listOf(
                    TextMenuItem(
                        title = "Skip: ${powermateSensitivityConfig.getMapSkip()}",
                        onClicked = {
                            modalMenuService.closeSidePaneOverlay(false)
                            modalMenuService.showKeyboard(
                                Keyboard.KeyboardType.NUMERIC,
                                prefilled = powermateSensitivityConfig.getMapSkip().toString(),
                                onTextEntered = { enteredString ->
                                    enteredString.toIntOrNull()?.let {
                                        powermateSensitivityConfig.setMapSkip(it)
                                    }
                                    setMapSkip()
                                }
                            )
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

}