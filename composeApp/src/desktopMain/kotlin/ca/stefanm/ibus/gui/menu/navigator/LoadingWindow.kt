package ca.stefanm.ibus.gui.menu.navigator

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.window.*
import ca.stefanm.ibus.gui.debug.windows.DebugLaunchpad
import ca.stefanm.ibus.gui.debug.windows.DeviceConfigurationViewerWindow
import ca.stefanm.ibus.gui.debug.windows.HmiNavigatorDebugWindow
import ca.stefanm.ibus.gui.debug.windows.KeyEventSimulator
import ca.stefanm.ibus.gui.debug.windows.commsDebug.PicoCommsDebugWindow
import ca.stefanm.ibus.gui.menu.widgets.themes.ThemeSelectorDebugWindow
import ca.stefanm.ibus.gui.platformConfig.PlatformConfigSetupWindow
import ca.stefanm.ibus.gui.platformConfig.WindowManagerConfigSetupWindow
import ca.stefanm.ibus.car.platform.ConfigurablePlatform
import ca.stefanm.ibus.configuration.ConfigurationStorage
import ca.stefanm.ibus.configuration.E39Config
import ca.stefanm.ibus.gui.debug.windows.*
import ca.stefanm.ibus.gui.menu.MenuWindow
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Provider
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalComposeUiApi::class)
@ExperimentalTime
class LoadingWindow @Inject constructor(
    private val configurationStorage: ConfigurationStorage,
    private val deviceConfigurationViewerWindow: DeviceConfigurationViewerWindow,
    private val configurablePlatform: ConfigurablePlatform,
    private val keyEventSimulator: KeyEventSimulator,
    private val debugLaunchpad: DebugLaunchpad,
    private val paneManagerDebug: PaneManagerDebug,
    private val hmiNavigatorDebugWindow: HmiNavigatorDebugWindow,
    private val platformConfigSetupWindow: PlatformConfigSetupWindow,
    private val windowManagerConfigSetupWindow: WindowManagerConfigSetupWindow,
    private val themeSelectorDebugWindow: ThemeSelectorDebugWindow,
    private val picoCommsDebugWindow: PicoCommsDebugWindow,
    private val windowManager : Provider<WindowManager>
) {

    private fun startPlatform() {
        configurablePlatform.run()
    }

    private fun restartPlatform() {
        configurablePlatform.stop()
        startPlatform()
    }

    fun contents() : @Composable FrameWindowScope.() -> Unit = {

        MenuBar {

            val isHidden = remember {
                mutableStateOf(configurationStorage.config[E39Config.LoadingWindowConfig.hideMenuBehindLoading])
            }

            if (isHidden.value) {
                Menu(
                    configurationStorage.config[E39Config.LoadingWindowConfig.hiddenMenuString]
                ) {
                    Item(
                        "Enable Debug Menu and Quit",
                        onClick = {
                            configurationStorage.config[E39Config.LoadingWindowConfig.hideMenuBehindLoading] = false
                            windowManager.get().exitApplication()
                        }
                    )
                    Item(
                        "Quit",
                        onClick = { windowManager.get().exitApplication()}
                    )
                }
            } else {
                Menu("Quit") {
                    Item(
                        "Quit",
                        onClick = { windowManager.get().exitApplication() },
                        //shortcut = KeyStroke(Key.Q)
                    )
                }

                Menu("Platform") {
                    Item(
                        "Start Platform",
                        onClick = { startPlatform() },
                        //shortcut = KeyStroke(Key.S)
                    )
                    Item(
                        "Stop Platform",
                        onClick = { configurablePlatform.stop() },
                        //shortcut = KeyStroke(Key.E)
                    )
                    Item(
                        "Restart Platform",
                        onClick = { restartPlatform() },
                        //shortcut = KeyStroke(Key.R)
                    )
                    Item(
                        "Configure Platform",
                        onClick = { windowManager.get().openDebugWindow(platformConfigSetupWindow) }
                    )
                }
                Menu("Debug") {
                    Item(
                        "Key Event Simulator",
                        onClick = { windowManager.get().openDebugWindow(keyEventSimulator) },
                        //shortcut = KeyStroke(Key.K)
                    )
                    Item(
                        "Debug Launchpad",
                        onClick = { windowManager.get().openDebugWindow(debugLaunchpad) },
                        //shortcut = KeyStroke(Key.D)
                    )
                    Item(
                        "Pane Manager Debugger",
                        onClick = { windowManager.get().openDebugWindow(paneManagerDebug) },
                        //shortcut = KeyStroke(Key.P)
                    )
                    Item(
                        "Navigation Hmi Debugger",
                        onClick = { windowManager.get().openDebugWindow(hmiNavigatorDebugWindow) },
                    )
                    Item(
                        "Theme Selector",
                        onClick = { windowManager.get().openDebugWindow(themeSelectorDebugWindow)}
                    )
                    Item(
                        "Pico Comms Debug",
                        onClick = { windowManager.get().openDebugWindow(picoCommsDebugWindow)}
                    )
                }
                Menu("Configuration") {
                    Item(
                        "Disable Debug Menu and Quit",
                        onClick = {
                            configurationStorage.config[E39Config.LoadingWindowConfig.hideMenuBehindLoading] = true
                            windowManager.get().exitApplication()
                        }
                    )
                    Item("View Device Configuration",
                        onClick = { windowManager.get().openDebugWindow(deviceConfigurationViewerWindow) }
                    )
                    Item(
                        "Configure Window Manager",
                        onClick = { windowManager.get().openDebugWindow(windowManagerConfigSetupWindow) }
                    )
                }
            }
        }

        Image(
            painter = painterResource("bmw_navigation.png"),
            contentDescription = null,
            modifier = Modifier.fillMaxSize()
                .background(Color.Black)
                .clickable {
                    openMenuWindow()
                }
        )

        if (configurationStorage.config[E39Config.LoadingWindowConfig.autoLaunchHmi]) {
            LaunchedEffect(true) {
                delay(configurationStorage.config[E39Config.LoadingWindowConfig.autoLaunchHmiDelay].toLong())
                openMenuWindow()
            }
        }

        if (configurationStorage.config[E39Config.LoadingWindowConfig.autoLaunchPlatformOnOpen]) {
            LaunchedEffect(true) {
                delay(100)
                startPlatform()
            }
        }
    }

    private fun openMenuWindow() {
        windowManager.get().openHmiMainWindow()
    }
}