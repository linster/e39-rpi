package ca.stefanm.ibus.gui.menu.widgets.bottombar

import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.text.font.FontWeight
import ca.stefanm.ibus.di.ApplicationScope
import ca.stefanm.ibus.gui.menu.widgets.bottombar.BmwFullScreenBottomBar
import ca.stefanm.ibus.gui.menu.widgets.bottombar.BottomBarClock
import ca.stefanm.ibus.gui.menu.widgets.themes.ThemeWrapper
import ca.stefanm.ibus.lib.logging.Logger
import kotlinx.coroutines.flow.Flow
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

    var bottomBarViewState = MutableStateFlow<BottomBarViewState>(BottomBarViewState.DateTime)

    sealed interface BottomBarViewState {
        object DateTime : BottomBarViewState
        data class SlideShow(val slideShowInfo : Flow<SlideShowInfo>) : BottomBarViewState {
            data class SlideShowInfo(
                val currentFileName : String,
                val currentFileNumber : Int,
                val totalFiles : Int
            )
        }
    }

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
            val state by bottomBarViewState.collectAsState(BottomBarViewState.DateTime)
            BmwFullScreenBottomBar(
                date = bottomBarClock.dateFlow.collectAsState().value,
                time = bottomBarClock.timeFlow.collectAsState().value,
            ) {
                logger.d(TAG, "bottomBarViewState: ${state}")
                when (state) {
                    BottomBarViewState.DateTime -> {}
                    is BottomBarViewState.SlideShow -> {
                        val slideShowInfo = (state as BottomBarViewState.SlideShow).slideShowInfo.collectAsState(
                            BottomBarViewState.SlideShow.SlideShowInfo("", 0,0 )
                        )
                        Text(
                            text = "Slideshow Running (${slideShowInfo.value.let {
                                "${it.currentFileName.takeLast(20)} : (${it.currentFileNumber}/${it.totalFiles})"
                            }})",
                            fontSize = ThemeWrapper.ThemeHandle.current.hmiHeaderFooter.fontSize,
                            fontWeight = FontWeight.Bold,
                            color = ThemeWrapper.ThemeHandle.current.hmiHeaderFooter.fontColor
                        )
                    }
                }
            }

        }
    }

}