package ca.stefanm.ibus.gui.debug

import androidx.compose.desktop.Window
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.IntSize
import ca.stefanm.ibus.gui.menu.widgets.*
import ca.stefanm.ibus.lib.logging.Logger
import javax.inject.Inject


class MenuDebug @Inject constructor(
    private val logger: Logger,
    private val menuKnobListenerService: MenuKnobListenerService
) {

    fun show() {
        Window(
            title = "Menu Simulator",
            size = IntSize(800, 468)
        ) {
            BmwFullScreenMenu(
                header = {
//                    BmwSingleLineHeader()
                    BmwFullScreenTrackInfoHeader("T0", "T1", "T2", "T3", "T4", "T5")
                },
                optionalSubHeader = {
                    BmwSingleLineHeader("Settings -> BT")
                }
            ) {
                BmwChipMenu(
                    scrollListener = menuKnobListenerService.toScrollListener(9),
                    contentLeft = { listener ->

                        val currentSelected = listener.currentSelectedIndex.value

                        MenuItem(label = "On-Board Computer", chipOrientation = ItemChipOrientation.NW,
                            isSelected = currentSelected == 0,
                            scrollListenerOnClickListener = listener.ScrollListenerOnClick(0) { logger.d("MENU","Selected 0")})
                        MenuItem(label = "Telephone", chipOrientation = ItemChipOrientation.W,
                            isSelected = currentSelected == 1,
                            scrollListenerOnClickListener = listener.ScrollListenerOnClick(1) { logger.d("MENU", "Selected 1")})
                        MenuItem(label = "Code", labelColor = ChipItemColors.TEXT_BLUE_DARK, chipOrientation = ItemChipOrientation.W,
                            isSelected = currentSelected == 2,
                            scrollListenerOnClickListener = listener.ScrollListenerOnClick(2) { logger.d("MENU", "Selected 2")})
                        EmptyMenuItem()
                        MenuItem(label = "Emergency", labelColor = Color.Red, chipOrientation = ItemChipOrientation.W,
                            isSelected = currentSelected == 3,
                            scrollListenerOnClickListener = listener.ScrollListenerOnClick(3) { logger.d("MENU", "Selected 3")})
                        MenuItem(label = "Settings", chipOrientation = ItemChipOrientation.SW,
                            isSelected = currentSelected == 4,
                            scrollListenerOnClickListener = listener.ScrollListenerOnClick(4) { logger.d("MENU", "Selected 4")})
                    },
                    contentRight = { listener ->

                        val currentSelected = listener.currentSelectedIndex.value

                        MenuItem(label = "GPS-Navigation", chipOrientation = ItemChipOrientation.NE,
                            isSelected = currentSelected == 5,
                            scrollListenerOnClickListener = listener.ScrollListenerOnClick(5) { logger.d("MENU", "Selected 5")})
                        MenuItem(label = "Aux. Ventilation", chipOrientation = ItemChipOrientation.E,
                            isSelected = currentSelected == 6,
                            scrollListenerOnClickListener = listener.ScrollListenerOnClick(6) { logger.d("MENU", "Selected 6")})
                        MenuItem(label = "Aux. Ventilation", chipOrientation = ItemChipOrientation.E,
                            isSelected = currentSelected == 7,
                            scrollListenerOnClickListener = listener.ScrollListenerOnClick(7) { logger.d("MENU", "Selected 7")})
                        EmptyMenuItem()
                        EmptyMenuItem()
                        MenuItem(label = "Monitor Off", chipOrientation = ItemChipOrientation.SE,
                            isSelected = currentSelected == 8,
                            scrollListenerOnClickListener = listener.ScrollListenerOnClick(8) { logger.d("MENU", "Selected 9")})
                    }
                )
            }
        }
    }
}