package ca.stefanm.ca.stefanm.ibus.gui.debug.windows

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material.Button
import androidx.compose.material.Checkbox
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.WindowScope
import androidx.compose.ui.window.WindowSize
import ca.stefanm.ibus.gui.menu.Notification
import ca.stefanm.ca.stefanm.ibus.gui.menu.PaneManager
import ca.stefanm.ibus.gui.menu.navigator.WindowManager
import ca.stefanm.ibus.gui.menu.notifications.toView
import ca.stefanm.ibus.gui.menu.widgets.ItemChipOrientation
import ca.stefanm.ca.stefanm.ibus.gui.menu.widgets.modalMenu.ModalChipMenuWindowOverlay
import ca.stefanm.ca.stefanm.ibus.gui.menu.widgets.modalMenu.ModalMenu
import javax.inject.Inject
import kotlin.time.ExperimentalTime

@ExperimentalTime
class PaneManagerDebug @Inject constructor(
) : WindowManager.E39Window {

    override val title = "PaneManagerDebug"

    override val defaultPosition: WindowManager.E39Window.DefaultPosition
        get() = WindowManager.E39Window.DefaultPosition.ANYWHERE

    override val size = DpSize(1600.dp, 800.dp)
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
            NestingCard(
                Modifier.width(300.dp)
            ) {

                NestingCardHeader("Pane Debug")

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


            val mainContentOverlay = remember { mutableStateOf<(@Composable () -> Unit)?>(null) }

            NestingCard(
                Modifier.width(450.dp)
            ) {
                //TODO put in an overlay layer on MainContent above
                //TODO put this in on PaneManager.
                ModalChipMenuOverlayDebugPane { mainContentOverlay.value = it }
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
                    mainContentOverlay = mainContentOverlay.value
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

    @Composable
    private fun ModalChipMenuOverlayDebugPane(
        onNewOverlay : (newOverlay : (@Composable () -> Unit)?) -> Unit
    ) {

        NestingCardHeader("Chip Menu Overlays")

        Button(onClick = { onNewOverlay { null } }) { Text("Clear Overlay")}

        val menuTopLeft = remember { mutableStateOf(IntOffset.Zero) }
        val menuWidth = remember { mutableStateOf(584) }
        val chipOrientation = remember { mutableStateOf(ItemChipOrientation.E) }
        val data = remember { mutableStateOf(ModalMenu.EMPTY) }

        NestingCard {
            NestingCardHeader("Preset Positions")
            Row {
                Button(onClick = {
                    menuTopLeft.value = IntOffset(64, 64)
                }) { Text("NW")}
                Button(onClick = {
                    menuTopLeft.value = IntOffset(512, 256)
                }) { Text("NE")}
                Button(onClick = {
                    menuTopLeft.value = IntOffset(64, 64)
                }) { Text("SW")}
                Button(onClick = {
                    menuTopLeft.value = IntOffset(512, 256)
                }) { Text("SE")}
            }
        }

        NestingCard {
            NestingCardHeader("Chip Orientation")
            Row {
                for (orientation in ItemChipOrientation.values()) {
                    Button(onClick = {
                        chipOrientation.value = orientation
                        data.value = data.value.copy(chipOrientation = chipOrientation.value)
                    }) { Text(orientation.name)}
                }
            }
        }

        NumericTextViewWithSpinnerButtons(
            label = "Width",
            initialValue = menuWidth.value,
            stepOnButton = 32,
            onValueChanged = { menuWidth.value = it}
        )

        NestingCard {
            NestingCardHeader("Data")

            Button(onClick = {
                data.value = ModalMenu(
                    chipOrientation = chipOrientation.value,
                    onOpen = {},
                    onClose = {},
                    items = listOf(
                        ModalMenu.ModalMenuItem(
                            title = "Terminate Guidance",
                            onClicked = {}
                        ),
                        ModalMenu.ModalMenuItem(
                            title = "Instruction",
                            onClicked = {}
                        ),
                        ModalMenu.ModalMenuItem(
                            title = "New Route",
                            onClicked = {}
                        ),
                        ModalMenu.ModalMenuItem(
                            title = "Traffic Information",
                            onClicked = {}
                        ),
                        ModalMenu.ModalMenuItem(
                            title = "Route Preference",
                            onClicked = {}
                        ),
                    )
                )
            }) { Text("Navigation Typical")}

            Button(onClick = {
                data.value = ModalMenu(
                    chipOrientation = chipOrientation.value,
                    onOpen = {},
                    onClose = {},
                    items = (1..20).map {
                        ModalMenu.ModalMenuItem(
                            title = "Menu Item $it",
                            onClicked = {}
                        )
                    }
                )
            }) { Text("Tall")}
        }

        LaunchedEffect(
            menuTopLeft.value,
            menuWidth.value,
            chipOrientation.value,
            data.value
        ) {
            onNewOverlay {
                ModalChipMenuWindowOverlay(
                    menuTopLeft = menuTopLeft.value,
                    menuWidth = menuWidth.value,
                    menuData = data.value
                )
            }
        }
    }

}