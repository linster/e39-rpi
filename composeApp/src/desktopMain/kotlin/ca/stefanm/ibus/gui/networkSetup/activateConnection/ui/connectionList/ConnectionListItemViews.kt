package ca.stefanm.ca.stefanm.ibus.gui.networkSetup.activateConnection.ui.connectionList

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import ca.stefanm.ibus.gui.menu.widgets.ArbitraryContentsMenuItem
import ca.stefanm.ibus.gui.menu.widgets.ItemChipOrientation
import ca.stefanm.ibus.gui.menu.widgets.MenuItem
import ca.stefanm.ibus.gui.menu.widgets.themes.ThemeWrapper

object ConnectionListItemViews {
    

    @Composable
    fun Connection(
        connectionName : String,
        strength: Int? = null,
        isConnected : Boolean = false,

        modifier: Modifier,
        chipOrientation: ItemChipOrientation,
        isSelected : Boolean,
        onClicked : () -> Unit
    ) {
        val isSmallSize = false
        val measurements = if (isSmallSize)
            ThemeWrapper.ThemeHandle.current.smallItem
        else
            ThemeWrapper.ThemeHandle.current.bigItem

        ArbitraryContentsMenuItem(
            boxModifier = modifier,
            labelColor = ThemeWrapper.ThemeHandle.current.colors.TEXT_WHITE,
            chipOrientation = chipOrientation,
            isSmallSize = false,
            isSelected = isSelected,
            onClicked = onClicked
        ) {
            Row(
                Modifier
                    .padding(start = 8.dp, end = 8.dp)
                    .clickable { onClicked() },
            ) {
                if (isConnected) {
                    Text(
                        modifier = Modifier.wrapContentWidth(),
                        text = "\uD83D\uDDA7 ",
                        color = ThemeWrapper.ThemeHandle.current.colors.TEXT_WHITE,
                        fontSize = measurements.fontSize,
                    )
                } else {
                    Spacer(Modifier.width(32.dp))
                }
                Text(
                    modifier = Modifier,
                    text = connectionName,
                    color = ThemeWrapper.ThemeHandle.current.colors.TEXT_WHITE,
                    fontSize = measurements.fontSize,
                )
                Spacer(Modifier.weight(2F, true))

                if (strength != null) {
                    Text(
                        modifier = Modifier,
                        text = toStrengthBars(strength),
                        color = ThemeWrapper.ThemeHandle.current.colors.TEXT_WHITE,
                        fontSize = measurements.fontSize,
                    )
                }
            }
        }
    }

    @Composable
    fun ConnectionListDivider(
        dividerHeader : String,
        modifier: Modifier = Modifier,
    ) {
        MenuItem(
            boxModifier = modifier,
            label = dividerHeader,
            labelColor = ThemeWrapper.ThemeHandle.current.colors.TEXT_WHITE,
            chipOrientation = ItemChipOrientation.NONE,
            isSelected = false,
            onClicked = { }
        )
    }



    private fun toStrengthBars(
        strength : Int /* 0..100 */
    ) : String{
        //Wifi Strength Bars
        //https://github.com/NetworkManager/NetworkManager/blob/fb1104d27889dec326ef0863bf894f6de9e88991/src/libnmc-base/nm-client-utils.c#L655
        return if (strength > 80)
             "▂▄▆█"
        else if (strength > 55)
             "▂▄▆_"
        else if (strength > 30)
             "▂▄__"
        else if (strength > 5)
             "▂___"
        else
             "____"
    }
}