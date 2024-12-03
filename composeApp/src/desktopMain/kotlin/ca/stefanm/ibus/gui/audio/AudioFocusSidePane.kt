package ca.stefanm.ca.stefanm.ibus.gui.audio

import androidx.compose.runtime.Composable
import ca.stefanm.ca.stefanm.ibus.car.audio.focusWriter.AudioFocusWriter
import ca.stefanm.ibus.gui.menu.widgets.modalMenu.ModalMenuService
import ca.stefanm.ibus.gui.menu.widgets.modalMenu.SidePanelMenu
import ca.stefanm.ibus.gui.menu.widgets.modalMenu.SidePanelMenu.InfoLabel
import ca.stefanm.ca.stefanm.ibus.gui.menu.widgets.screenMenu.TextMenuItem
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import javax.inject.Inject

class AudioFocusSidePane @Inject constructor(
    private val modalMenuService: ModalMenuService,
    private val audioFocusWriter: AudioFocusWriter
) {

    fun show() {
        modalMenuService.showSidePaneOverlay(darkenBackground = true) {
            SidePanelMenu.SidePanelMenu(
                title = "Set Tuner Audio Focus",
                @Composable {
                    """
                        When the BM53 radio tuner is set to Nav/TV input,
                        the MODE button on the head unit will not switch
                        between audio sources. 
                    """.trimIndent().split('\n').forEach { InfoLabel(it) }
                },
                listOf(
                    TextMenuItem(
                        "Nav/Tv",
                        onClicked = {
                            GlobalScope.launch {
                                audioFocusWriter.navTv()
                            }
                        }
                    ),
                    TextMenuItem(
                        "Aux",
                        onClicked = {
                            GlobalScope.launch {
                                audioFocusWriter.aux()
                            }
                        }
                    ),
                    TextMenuItem("Go Back", onClicked = { modalMenuService.closeSidePaneOverlay(true)})
                )
            )
        }
    }


}