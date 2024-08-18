package ca.stefanm.ca.stefanm.ibus.gui.audio

import androidx.compose.runtime.*
import ca.stefanm.ibus.car.platform.ConfigurablePlatform
import ca.stefanm.ibus.car.platform.ConfigurablePlatformServiceRunner
import ca.stefanm.ibus.car.platform.PlatformService
import ca.stefanm.ibus.gui.menu.widgets.modalMenu.ModalMenuService
import ca.stefanm.ibus.gui.menu.widgets.modalMenu.SidePanelMenu
import ca.stefanm.ibus.gui.menu.widgets.screenMenu.CheckBoxFlowMenuItem
import ca.stefanm.ibus.gui.menu.widgets.screenMenu.CheckBoxMenuItem
import ca.stefanm.ibus.gui.menu.widgets.screenMenu.TextMenuItem
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

class NowPlayingInfoSourcePane @Inject constructor(
    private val modalMenuService: ModalMenuService,
    private val configurablePlatform: ConfigurablePlatform
) {

    companion object {
        const val DBUS_SERVICE = "DbusTrackInfoPrinter"
        const val RADIO_SERVICE = "RadioTextFieldReaderService"
    }

    fun show() {

        modalMenuService.showSidePaneOverlay(darkenBackground = true) @Composable {

            val dbusServiceRunning = configurablePlatform
                    .findServiceRecordByName(DBUS_SERVICE)
                    .flatMapConcat { it.runStatus }
                    .map { it == PlatformService.RunStatus.RUNNING }
                .shareIn(rememberCoroutineScope(), SharingStarted.Eagerly, replay = 1)


            val radioServiceRunning = configurablePlatform
                    .findServiceRecordByName(RADIO_SERVICE)
                    .flatMapConcat { it.runStatus }
                    .map { it == PlatformService.RunStatus.RUNNING }
                    .shareIn(rememberCoroutineScope(), SharingStarted.Eagerly, replay = 1)

            //        val mprisServiceRunning = remember { mutableStateOf(false) }


            SidePanelMenu.SidePanelMenu(
                title = "Enable Information Sources",
                @Composable {
                    """ 
                        DBUS: Track info from paired BT telephone
                        Radio: Print messages from Radio / CD Changer
                    """.trimIndent().split('\n').forEach { SidePanelMenu.InfoLabel(it) }
                    //MPRIS : (Future) Virtual CD player
                },
                listOf(
                    CheckBoxFlowMenuItem(
                        "DBUS",
                        isChecked = dbusServiceRunning,
                        onClicked = {
                            GlobalScope.launch {
                                if (dbusServiceRunning.singleOrNull() == true) {
                                    stopService(DBUS_SERVICE)
                                } else {
                                    startService(DBUS_SERVICE)
                                }
                            }
                        }
                    ),
                    CheckBoxFlowMenuItem(
                        "Radio",
                        isChecked = radioServiceRunning,
                        onClicked = {
                            GlobalScope.launch {
                                if (dbusServiceRunning.singleOrNull() == true) {
                                    stopService(RADIO_SERVICE)
                                } else {
                                    startService(RADIO_SERVICE)
                                }
                            }
                        }
                    ),
//                    TextMenuItem(
//                        "MPRIS",
//                        onClicked = {
//                            GlobalScope.launch {
//                                if (dbusServiceRunning.value) {
//                                    stopService()
//                                } else {
//                                    startService()
//                                }
//                            }
//                        }
//                    ),
                    TextMenuItem("Go Back", onClicked = { modalMenuService.closeSidePaneOverlay(true)})
                )
            )
        }

    }

    private fun startService(name : String) {
        configurablePlatform.startByName(name)
    }

    private fun stopService(name : String) {
        configurablePlatform.stopByName(name)
    }
}