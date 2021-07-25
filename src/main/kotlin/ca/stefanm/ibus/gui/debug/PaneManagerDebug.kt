package ca.stefanm.ibus.gui.debug

import androidx.compose.desktop.Window
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material.Button
import androidx.compose.material.Checkbox
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.WindowScope
import androidx.compose.ui.window.WindowSize
import ca.stefanm.ibus.gui.menu.Notification
import ca.stefanm.ibus.gui.menu.PaneManager
import ca.stefanm.ibus.gui.menu.navigator.WindowManager
import ca.stefanm.ibus.gui.menu.notifications.toView
import javax.inject.Inject
import kotlin.time.ExperimentalTime

@ExperimentalTime
class PaneManagerDebug @Inject constructor(
) : WindowManager.E39Window {

    override val title = "PaneManagerDebug"

    override val defaultPosition: WindowManager.E39Window.DefaultPosition
        get() = WindowManager.E39Window.DefaultPosition.ANYWHERE

    override val size = WindowSize(1300.dp, 800.dp)
    override val tag: Any
        get() = this

    override fun content(): @Composable WindowScope.() -> Unit  = {
        val mainContentEnabled = remember{ mutableStateOf(true) }
        val bannerEnabled = remember{ mutableStateOf(false) }
        val topPopInEnabled = remember{ mutableStateOf(true) }
        val sideSplitEnabled = remember{ mutableStateOf(true) }
        val keyboardOverlayEnabled = remember{ mutableStateOf(true) }
        val bottomPanelEnabled = remember{ mutableStateOf(false) }

        //Should each box grow to maximum size?
        val mainContentFillSize = remember{ mutableStateOf(true) }
        val bannerFillSize = remember{ mutableStateOf(false) }
        val topPopInFillSize = remember{ mutableStateOf(true) }
        val sideSplitFillSize = remember{ mutableStateOf(true) }
        val keyboardOverlayFillSize = remember{ mutableStateOf(true) }
        val bottomPanelFillSize = remember{ mutableStateOf(false) }

        val sideSplitVisible = remember { mutableStateOf(false)}
        val topPopInVisible = remember { mutableStateOf(false)}


        @Composable fun MutableState<Boolean>.toToggleCheckbox(label : String) {
            Row {
                Text(label)
                Spacer(Modifier.width(10.dp))
                Checkbox(
                    checked = value,
                    onCheckedChange = { value = !value }
                )
            }
        }

        val topPopInComposableDefault = getFiller(Color.Magenta, topPopInEnabled.value, topPopInFillSize.value)
        val topPopInComposable = remember { mutableStateOf<(@Composable () -> Unit)?>(topPopInComposableDefault) }
        val topPopInComposableNotification = Notification(
            topText = "Example Notification",
            contentText = "Content Text",
            image = Notification.NotificationImage.MAP_WAYPOINT
        )

        Row {
            Column(
                Modifier.width(300.dp)
            ) {
                mainContentEnabled.toToggleCheckbox(label = "MainContent Enabled")
                bannerEnabled.toToggleCheckbox(label = "Banner Enabled")
                topPopInEnabled.toToggleCheckbox(label = "Top Pop-In Enabled")
                sideSplitEnabled.toToggleCheckbox(label = "Side-split Enabled")
                bottomPanelEnabled.toToggleCheckbox(label = "Bottom Panel Enabled")
                keyboardOverlayEnabled.toToggleCheckbox("Keyboard Overlay enabled")
                Spacer(Modifier.height(10.dp))
                mainContentFillSize.toToggleCheckbox(label = "MainContent Fill size?")
                bannerFillSize.toToggleCheckbox(label = "Banner Fill size?")
                topPopInFillSize.toToggleCheckbox(label = "Top Pop-In Fill size?")
                sideSplitFillSize.toToggleCheckbox(label = "Side-split Fill size?")
                bottomPanelFillSize.toToggleCheckbox(label = "Bottom Panel Fill size?")
                keyboardOverlayFillSize.toToggleCheckbox(label = "Keyboard overlay Fill size?")
                Spacer(Modifier.height(30.dp))
                sideSplitVisible.toToggleCheckbox("Side-split visible")
                topPopInVisible.toToggleCheckbox("Top Pop-in visible")
                Spacer(Modifier.height(30.dp))

                Button(
                    onClick = { topPopInComposable.value = topPopInComposableDefault}
                ) { Text("Top-Pop in to default")}

                Button(
                    onClick = { topPopInComposable.value =  {topPopInComposableNotification.toView()} }
                ) { Text("Show Notification")}

            }

            Column(
                Modifier
                    .width(800.dp)
                    .height(468.dp)
                    .border(2.dp, Color.Red)
            ) {
                PaneManager(
                    banner = getFiller(Color.Red, bannerEnabled.value, bannerFillSize.value),
                    sideSplit = getFiller(Color.Green, sideSplitEnabled.value, sideSplitFillSize.value),
                    bottomPanel = getFiller(Color.Yellow, bottomPanelEnabled.value, bottomPanelFillSize.value),
                    topPopIn = topPopInComposable.value,
                    mainContent = getContentFiller(),
                    sideSplitVisible = sideSplitVisible.value,
                    topPopInVisible = topPopInVisible.value,
                )
            }
        }
    }

    private fun getContentFiller() : @Composable () -> Unit = {
        Canvas(
            modifier = Modifier.fillMaxHeight().fillMaxWidth()
                .background(Brush
                    .verticalGradient(
                        0.0F to Color.Cyan,
                        0.3F to Color.Red,
                        0.7F to Color.Blue,
                        1F to Color.Green
                    )),
            onDraw = {}
        )
    }

    private fun getFiller(
        color: Color,
        isEnabled : Boolean,
        fillSize : Boolean
    ) : @Composable (() -> Unit)? {
        return if (!isEnabled) {
            null
        } else {
            if (fillSize) {
                {
                    Canvas(
                    Modifier.background(color = color).fillMaxSize(),
                    onDraw = {})
                }
            } else {
                {
                    Canvas(
                        Modifier.background(color = color).width(40.dp).height(40.dp),
                        onDraw = {}
                    )
                }
            }
        }
    }

}