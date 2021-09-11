package ca.stefanm.ibus.gui.menu

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.window.WindowScope
import androidx.compose.ui.window.WindowSize
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
import ca.stefanm.ibus.lib.logging.Logger
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.launch
import java.awt.event.InputEvent
import javax.inject.Inject


@ApplicationScope
@Stable
class MenuWindow @Inject constructor(
    private val navigator: Navigator,
    private val logger: Logger,
    private val notificationHub: NotificationHub,
    private val bottomBarClock: BottomBarClock,
    private val modalMenuService: ModalMenuService,
    private val realKnobListenerService: KnobListenerService
) : WindowManager.E39Window {

    companion object {
        val MenuWindowKnobListener = compositionLocalOf { DaggerApplicationComponent.create().knobListenerService() }
    }

    override val title: String
        get() = "BMW E39 Nav Menu"

    override val defaultPosition: WindowManager.E39Window.DefaultPosition
        get() = WindowManager.E39Window.DefaultPosition.OVER_MAIN

    override val size: WindowSize
        get() = WindowManager.DEFAULT_SIZE

    override fun content(): @Composable WindowScope.() -> Unit = {
        rootContent()
    }

    override val tag: Any
        get() = this

    @Composable
    private fun rootContent() {

        //TODO, a KnobListener needs to be a CompositionLocal passed down all the way through
        //TODO so we can avoid chains of passing it in as a screen parameter.
        //TODO this is the root node of the composition so it's a pretty good place to put it.
        //TODO https://developer.android.com/jetpack/compose/compositionlocal

        val dummyKnobListenerService = KnobListenerService(MutableSharedFlow())
        val providedKnobListenerService = remember { mutableStateOf(realKnobListenerService) }

        PaneManager(
            banner = null,
            sideSplit = null,
            sideSplitVisible = false,
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
                        MenuWindowKnobListener provides providedKnobListenerService.value
                    ) {
                        with (currentNode.value) {
                            node.provideMainContent().invoke(incomingResult)
                        }
                    }

                }
            },
            mainContentOverlay = {
                modalMenuService.modalMenuOverlay.collectAsState().value.let {
                    if (it != null) {
                        providedKnobListenerService.value = dummyKnobListenerService
                        it.invoke()
                    } else {
                        providedKnobListenerService.value = realKnobListenerService
                    }
                }
            }
        )
    }
}