package ca.stefanm.ca.stefanm.ibus.gui.menu.widgets.bottombar

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.rememberCoroutineScope
import ca.stefanm.ibus.di.ApplicationScope
import ca.stefanm.ibus.gui.menu.widgets.bottombar.BmwFullScreenBottomBar
import ca.stefanm.ibus.gui.menu.widgets.bottombar.BottomBarClock
import ca.stefanm.ibus.lib.logging.Logger
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@ApplicationScope
class BottomBarController @Inject constructor(
    private val bottomBarClock: BottomBarClock,
    private val logger: Logger
) {

    private val bottomBarIsShowing = MutableStateFlow(true)

    companion object {
        const val TAG = "BottomBarController"
    }
    @Composable
    fun HideBottomPanelWhileInComposition() {
        DisposableEffect(Unit) {
            bottomBarIsShowing.value = false
            logger.d(TAG, "HideBottomPanelWhileInComposition entered")
            onDispose {
                bottomBarIsShowing.value = true
                logger.d(TAG, "HideBottomPanelWhileInComposition exited")
            }
        }
    }



    @Composable
    fun BottomPanelView() {
        val isShowing = bottomBarIsShowing.collectAsState(true)
        if (isShowing.value) {
            val scope = rememberCoroutineScope()
            scope.launch {
                bottomBarClock.updateValues()
            }

            BmwFullScreenBottomBar(
                date = bottomBarClock.dateFlow.collectAsState().value,
                time = bottomBarClock.timeFlow.collectAsState().value,
            )
        }
    }

}