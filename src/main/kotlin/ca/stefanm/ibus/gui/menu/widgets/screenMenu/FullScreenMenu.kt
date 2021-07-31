package ca.stefanm.ibus.gui.menu.widgets.screenMenu

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import ca.stefanm.ibus.di.DaggerApplicationComponent
import ca.stefanm.ibus.gui.menu.widgets.ChipItemColors
import ca.stefanm.ibus.gui.menu.widgets.ItemChipOrientation
import ca.stefanm.ibus.gui.menu.widgets.MenuItem

//These are all non-scrollable full-screen menu types.
object FullScreenMenu {

    @Composable
    fun TwoColumnFillFromTop(
        leftItems : List<MenuItem>,
        rightItems : List<MenuItem>
    ) {
        Box(modifier = Modifier
            .background(ChipItemColors.MenuBackground)
            .fillMaxSize()
        ) {
            HalfScreenMenu.TwoColumn(leftItems, rightItems)
        }
    }

    @Composable
    fun TwoColumnFillFromCorners(
        nw : List<MenuItem>,
        ne : List<MenuItem>,
        sw : List<MenuItem>,
        se : List<MenuItem>
    ) {

    }
}