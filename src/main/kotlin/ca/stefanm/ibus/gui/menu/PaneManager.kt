package ca.stefanm.ibus.gui.menu

import androidx.compose.animation.*
import androidx.compose.animation.core.spring
import androidx.compose.foundation.layout.*
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun PaneManager(
    //Children should not use max size.
    banner: @Composable (() -> Unit)?,

    //Children can use fillMaxSize, and it just works.
    sideSplit: @Composable (() -> Unit)?,
    sideSplitVisible: Boolean,

    //Children should not use max size.
    bottomPanel: @Composable (() -> Unit)?,

    //This is for notifications
    //Children should use max size
    topPopIn: @Composable (() -> Unit)?,
    topPopInVisible: Boolean,

    //Children should use max size.
    mainContent: @Composable () -> Unit,

    mainContentOverlay : (@Composable () -> Unit)? = null
) {

    Box(Modifier.fillMaxSize()) {
        Column(Modifier.fillMaxWidth()) {
            val maxWeightBanner = 0.25F
            val maxWeightBottom = 0.15F

            val weightContent = 1 -
                    (if (banner == null) 0.0F else maxWeightBanner) -
                    if (bottomPanel == null) 0.0F else maxWeightBottom

            if (banner != null) {
                Box(
                    modifier = Modifier
                        .wrapContentHeight(Alignment.Top)
                ) {
                    banner()
                }
            }
            Box(Modifier.weight(weightContent)) {
                mainContent()
                mainContentOverlay?.invoke()
            }
            if (bottomPanel != null) {
                Box(
                    modifier = Modifier
                ) {
                    bottomPanel()
                }
            }
        }



        Box(
            modifier = Modifier.fillMaxWidth(0.4f)
                .align(Alignment.TopEnd)
        ) {
            AnimatedVisibility(
                modifier = Modifier.align(Alignment.TopEnd),
                visible = sideSplitVisible,
                enter = slideInHorizontally({ it }),
                exit = slideOutHorizontally({ it })
            ) {
                sideSplit?.invoke()
            }
        }

        Box(
            modifier = Modifier.fillMaxHeight(0.3f)
                .align(Alignment.TopCenter)
                .padding(horizontal = 64.dp)
        ) {
            AnimatedVisibility(
                modifier = Modifier.align(Alignment.TopEnd),
                visible = topPopInVisible,
                enter = slideInVertically({ -it }),
                exit = slideOutVertically({ -it })
            ) {
                topPopIn?.invoke()
            }
        }
    }
}