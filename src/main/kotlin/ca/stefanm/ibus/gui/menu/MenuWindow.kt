package ca.stefanm.ibus.gui.menu

import androidx.compose.desktop.*
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.window.WindowScope
import androidx.compose.ui.window.WindowSize
import ca.stefanm.ibus.gui.menu.navigator.Navigator
import ca.stefanm.ibus.gui.menu.navigator.WindowManager
import ca.stefanm.ibus.gui.menu.notifications.NotificationHub
import ca.stefanm.ibus.gui.menu.notifications.toView
import ca.stefanm.ibus.lib.logging.Logger
import javax.inject.Inject

class MenuWindow @Inject constructor(
    private val navigator: Navigator,
    private val logger: Logger,
    private val notificationHub: NotificationHub
) : WindowManager.E39Window {

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
        PaneManager(
            banner = null,
            sideSplit = null,
            sideSplitVisible = false,
            bottomPanel = null,
            topPopIn = {
                   notificationHub.currentNotification.collectAsState().value?.toView()
            },
            topPopInVisible = notificationHub.currentNotificationIsVisible.collectAsState(false).value,
            mainContent = {
                Box(
                    Modifier.fillMaxSize()
                ) {
                    val currentNode = navigator.mainContentScreen.collectAsState()

                    logger.d("MenuWindow", currentNode.value.thisClass.canonicalName)

                    currentNode.value.provideMainContent()()
                }
            }
        )
    }
}