package ca.stefanm.ibus.gui.debug.windows

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import kotlin.enums.enumEntries

@Composable
fun NestingCard(
    modifier: Modifier = Modifier,
    contents : @Composable () -> Unit
) {
    Card(
        modifier = Modifier
            .padding(3.dp)
            .then(modifier),
        elevation = 3.dp
    ) {
        Column {
            contents()
        }
    }
}

@Composable
fun NestingCardHeader(text : String) {
    Text(
        modifier = Modifier.padding(vertical = 8.dp),
        text = text,
        style = MaterialTheme.typography.subtitle1
    )
}

@Composable
fun NumericTextViewWithSpinnerButtons(
    label : String = "",
    initialValue : Int = 0,
    stepOnButton : Int = 1,
    plusButtonEnabled : Boolean = true,
    minusButtonEnabled : Boolean = true,
    onValueChanged : (newValue : Int) -> Unit
) {

    val textFieldInt = remember { mutableStateOf(initialValue) }

    Row {

        Text(label)
        Spacer(Modifier.width(32.dp))

        TextField(
            value = textFieldInt.value.toString(),
            onValueChange = { newStr ->
                textFieldInt.value = newStr.toIntOrNull() ?: initialValue
                onValueChanged(textFieldInt.value)
            },
            modifier = Modifier.width(128.dp),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            singleLine = true
        )

        if (minusButtonEnabled) {
            Button(onClick = {
                textFieldInt.value = textFieldInt.value - stepOnButton
                onValueChanged(textFieldInt.value)
            }) { Text(" - ") }
        }

        if (plusButtonEnabled) {
            Button(onClick = {
                textFieldInt.value = textFieldInt.value + stepOnButton
                onValueChanged(textFieldInt.value)
            }) { Text(" + ") }
        }
    }
}

@Composable
fun CheckBoxWithLabel(isChecked : Boolean, onCheckChanged : (new : Boolean) -> Unit, label : String) {
    Row(Modifier.wrapContentHeight(), verticalAlignment = Alignment.CenterVertically) {
        Checkbox(
            checked = isChecked,
            onCheckedChange = { onCheckChanged(it)}
        )
        Text(label)
    }
}

@Composable
inline fun <reified T : Enum<T>> EnumColumn(
    currentEntry : T,
    crossinline onEntryChanged : (T) -> Unit,
    modifier: Modifier = Modifier
) {
    val radioOptions = enumEntries<T>()

    Column(
        modifier = modifier.selectableGroup()
    ) {
        radioOptions.forEach {
            Row(
                Modifier.selectable(
                    selected = it == currentEntry,
                    onClick = { onEntryChanged(it) },
                    role = Role.RadioButton
                )
            ) {
                RadioButton(
                    selected = it == currentEntry,
                    onClick = null
                )
                Text(
                    text = it.name
                )
            }
        }
    }
}