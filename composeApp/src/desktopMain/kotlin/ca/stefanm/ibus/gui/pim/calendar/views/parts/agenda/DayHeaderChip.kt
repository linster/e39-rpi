package ca.stefanm.ca.stefanm.ibus.gui.pim.calendar.views.parts.agenda

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ca.stefanm.ibus.gui.menu.widgets.themes.ThemeWrapper

@Composable
fun DayHeaderChip(
    modifier: Modifier = Modifier,
    label : String
) {

    val theme = ThemeWrapper.ThemeHandle.current
    val strokeWidth = theme.bigItem.highlightWidth.let { it / 2F }

    Row(modifier = Modifier
        .background(theme.colors.sideMenuBorder)
        .border(5.dp, theme.colors.chipHighlights, RoundedCornerShape(5.dp) )
        .shadow(5.dp)
        .then(modifier)
    ) {
        Spacer(Modifier.width(10.dp))
        Text(
            modifier = Modifier.padding(top = 5.dp, bottom = 5.dp),
            text = label, color = theme.colors.TEXT_WHITE,
            maxLines = 1, fontSize = (theme.smallItem.fontSize.value * 0.75).sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.width(10.dp))
    }
}