package ca.stefanm.ibus.gui.debug

import androidx.compose.desktop.Window
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.IntSize
import ca.stefanm.ibus.gui.menu.*

class MenuDebug {

    fun show() {
        Window(
            title = "Menu Simulator",
            size = IntSize(800, 600)
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
                    contentLeft = {
                        MenuItem(label = "On-Board Computer", chipOrientation = ItemChipOrientation.NW)
                        MenuItem(label = "Telephone", chipOrientation = ItemChipOrientation.W)
                        MenuItem(label = "Code", labelColor = ChipItemColors.TEXT_BLUE_DARK, chipOrientation = ItemChipOrientation.W)
                        EmptyMenuItem()
                        MenuItem(label = "Emergency", labelColor = Color.Red, chipOrientation = ItemChipOrientation.W)
                        MenuItem(label = "Settings", chipOrientation = ItemChipOrientation.SW)
                    },
                    contentRight = {
                        MenuItem(label = "GPS-Navigation", chipOrientation = ItemChipOrientation.NE)
                        MenuItem(label = "Aux. Ventilation", chipOrientation = ItemChipOrientation.E)
                        MenuItem(label = "Aux. Ventilation", chipOrientation = ItemChipOrientation.E)
                        EmptyMenuItem()
                        EmptyMenuItem()
                        MenuItem(label = "Monitor Off", chipOrientation = ItemChipOrientation.SE)
                    }
                )
            }
        }
    }
}