package ca.stefanm.ibus.gui.pim.calendar.views.parts.agenda

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import ca.stefanm.ibus.gui.menu.widgets.themes.ThemeWrapper
import kotlinx.datetime.DateTimePeriod

object CalendarEventColors {


    val plum1 = Color(0xffba00ff)
    val green3 = Color(0xff009100)


}

data class CalendarEventBox(
    val header: String,
    val timespan : DateTimePeriod,
    val color: Color
)


@Composable
fun CalendarEventBox(
    modifier: Modifier = Modifier,
    header : String,
    body : String,
    isSelected : Boolean = false,
    baseColor : Color,
    onClick : () -> Unit
) {

    val theme = ThemeWrapper.ThemeHandle.current

    Column(
        modifier = Modifier
            .border(5.dp, if (isSelected) theme.colors.selectedColor else baseColor, shape= RoundedCornerShape(10.dp))
            .then(modifier),
        verticalArrangement = Arrangement.Top
    ) {
        Column(
            modifier = Modifier
                .padding(theme.smallItem.chipWidth.dp)
        ) {
            Text(
                header,
                color = theme.colors.textMenuColorAccent,
                fontWeight = FontWeight.Bold,
                fontSize = theme.smallItem.fontSize,
                maxLines = 1
            )
            Text(
                body,
                color = theme.colors.textMenuColorAccent,
                fontWeight = FontWeight.Light,
                fontSize = theme.smallItem.fontSize
            )
        }
    }

}