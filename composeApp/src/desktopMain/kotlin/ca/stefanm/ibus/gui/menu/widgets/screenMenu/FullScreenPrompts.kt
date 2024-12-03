package ca.stefanm.ca.stefanm.ibus.gui.menu.widgets.screenMenu

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import ca.stefanm.ca.stefanm.ibus.gui.menu.widgets.themes.ThemeWrapper
import ca.stefanm.ca.stefanm.ibus.gui.menu.widgets.BmwSingleLineHeader
import ca.stefanm.ibus.gui.menu.widgets.ChipItemColors
import ca.stefanm.ibus.gui.menu.widgets.ItemChipOrientation

object FullScreenPrompts {

    fun YesNoOptions(
        onYesSelected : () -> Unit,
        onNoSelected : () -> Unit
    ) : List<MenuItem> = listOf(
        TextMenuItem(
            title = "Yes",
            onClicked = onYesSelected
        ),
        TextMenuItem(
            title = "No",
            onClicked = onNoSelected
        )
    )

    @Composable
    fun OptionPrompt(
        header : String,
        options : List<MenuItem>,
        centerContents : @Composable () -> Unit
    ) {
        //Full screen Yes, no
        Column(
            Modifier.background(ThemeWrapper.ThemeHandle.current.colors.menuBackground),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            BmwSingleLineHeader(header)

            //Contents
            Box(
                Modifier.wrapContentHeight()
                    .align(Alignment.CenterHorizontally)
                    .weight(0.8F)
                    .fillMaxWidth(0.8F)
            ) {
                centerContents()
            }

            HalfScreenMenu.OneColumn(
                items = options,
                alignment = Alignment.End,
                fullWidth = false
            )
        }
    }
}