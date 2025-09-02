package ca.stefanm.ibus.gui.menu

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.WindowScope
import androidx.compose.ui.window.WindowSize
import ca.stefanm.ibus.gui.menu.widgets.themes.ThemeConfigurationStorage
import ca.stefanm.ibus.gui.menu.widgets.themes.ThemeWrapper
import ca.stefanm.ibus.configuration.ConfigurationStorage
import ca.stefanm.ibus.configuration.E39Config
import ca.stefanm.ibus.di.ApplicationModule
import ca.stefanm.ibus.di.ApplicationScope
import ca.stefanm.ibus.di.DaggerApplicationComponent
import ca.stefanm.ibus.gui.menu.navigator.Navigator
import ca.stefanm.ibus.gui.menu.navigator.WindowManager
import ca.stefanm.ibus.gui.menu.notifications.NotificationHub
import ca.stefanm.ibus.gui.menu.notifications.toView
import ca.stefanm.ibus.gui.menu.widgets.bottombar.BmwFullScreenBottomBar
import ca.stefanm.ibus.gui.menu.widgets.bottombar.BottomBarClock
import ca.stefanm.ibus.gui.menu.widgets.knobListener.KnobListenerService
import ca.stefanm.ibus.gui.menu.widgets.modalMenu.ModalMenuService
import ca.stefanm.ibus.gui.menu.widgets.modalMenu.keyboard.*
import ca.stefanm.ibus.lib.logging.Logger
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.launch
import java.awt.Window
import java.awt.event.*
import java.awt.event.KeyEvent.*
import javax.inject.Inject
import javax.inject.Named


@ApplicationScope
@Stable
class MenuWindow @Inject constructor(
    private val navigator: Navigator,
    private val logger: Logger,
    private val notificationHub: NotificationHub,
    private val bottomBarClock: BottomBarClock,
    private val modalMenuService: ModalMenuService,

    @Named(ApplicationModule.KNOB_LISTENER_MAIN)
    private val knobListenerServiceMain: KnobListenerService,

    @Named(ApplicationModule.KNOB_LISTENER_MODAL)
    private val knobListenerServiceModal: KnobListenerService,

    private val configurationStorage: ConfigurationStorage,
    private val themeConfigurationStorage: ThemeConfigurationStorage,
    @Named(ApplicationModule.INPUT_EVENTS_WRITER) val inputEventsWriter : MutableSharedFlow<ca.stefanm.ibus.car.bordmonitor.input.InputEvent>,
) : WindowManager.E39Window {

    companion object {
        val MenuWindowKnobListener = compositionLocalOf { DaggerApplicationComponent.create().knobListenerServiceMain() }
    }

    override val title: String
        get() = "BMW E39 Nav Menu"

    override val defaultPosition: WindowManager.E39Window.DefaultPosition
        get() = WindowManager.E39Window.DefaultPosition.OVER_MAIN

    override val size get() = configurationStorage.config[E39Config.WindowManagerConfig.hmiWindowSize].let {
        DpSize(it.first.dp, it.second.dp)
    }

    override fun content(): @Composable WindowScope.() -> Unit = {
        CompositionLocalProvider(KeyboardWindowProvider.Window provides window) {

            val isKeyboardShowingState = modalMenuService.isKeyboardShowing.collectAsState(false)


            rootContent()
        }
    }

    override val tag: Any
        get() = this

    @Composable
    private fun rootContent() {

        ThemeWrapper.ThemedUiWrapper(
            themeConfigurationStorage.getTheme().collectAsState(themeConfigurationStorage.getStoredTheme()).value
        ) {
            PaneManager(
                banner = null,
                sideSplit = @Composable {
                    modalMenuService.sidePaneOverlay.collectAsState().value.let {
                        CompositionLocalProvider(
                            MenuWindowKnobListener provides knobListenerServiceModal
                        ) {
                            it.ui?.invoke()
                        }
                    }
                },
                darkenBackgroundOnSideSplitDisplay = modalMenuService.sidePaneOverlay.collectAsState().value.darkenBackground,
                sideSplitVisible = modalMenuService.sidePaneOverlay.collectAsState().value.ui != null,
                bottomPanel = {
                    val scope = rememberCoroutineScope()
                    scope.launch {
                        bottomBarClock.updateValues()
                    }

                    BmwFullScreenBottomBar(
                        date = bottomBarClock.dateFlow.collectAsState().value,
                        time = bottomBarClock.timeFlow.collectAsState().value,
                    )
                },
                topPopIn = {
                    notificationHub.currentNotification.collectAsState().value?.toView()
                },
                topPopInVisible = notificationHub.currentNotificationIsVisible.collectAsState(false).value,
                mainContent = {
                    Box(
                        Modifier.fillMaxSize()
                    ) {
                        val currentNode = navigator.mainContentScreen.collectAsState()

                        logger.d("MenuWindow", currentNode.value.node.thisClass.canonicalName)
                        logger.d("MenuWindow", currentNode.value.incomingResult.toString())


                        CompositionLocalProvider(
                            MenuWindowKnobListener provides knobListenerServiceMain
                        ) {
                            with(currentNode.value) {
                                node.provideMainContent().invoke(incomingResult)
                            }
                        }

                    }
                },
                mainContentOverlay = {
                    modalMenuService.modalMenuOverlay.collectAsState().value.let {
                        it?.invoke()
                    }
                }
            )
        }
    }
}