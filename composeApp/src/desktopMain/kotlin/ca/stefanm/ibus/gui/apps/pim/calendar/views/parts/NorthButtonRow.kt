package ca.stefanm.ibus.gui.pim.calendar.views.parts

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import ca.stefanm.ibus.gui.menu.widgets.ItemChipOrientation
import ca.stefanm.ibus.gui.menu.widgets.MenuItem
import ca.stefanm.ibus.gui.menu.widgets.halveIfNotPixelDoubled
import ca.stefanm.ibus.gui.menu.widgets.knobListener.dynamic.KnobObserverBuilder
import ca.stefanm.ibus.gui.menu.widgets.knobListener.dynamic.KnobObserverBuilderState
import ca.stefanm.ibus.gui.menu.widgets.themes.ThemeWrapper

@Composable
fun NorthButtonRow(
    knobState : KnobObserverBuilderState,
    previousButtonLabel : String = "⏴",
    nextButtonLabel : String = "⏵",
    menuButtonLabel : String = "Menu",
    timePeriodLabel : String,
    onPreviousClicked : () -> Unit,
    onNextClicked : () -> Unit,
    onMenuClicked : () -> Unit,
) {
    Row(
        modifier = Modifier
            .background(ThemeWrapper.ThemeHandle.current.colors.menuBackground)
            .fillMaxWidth()
    ) {

        KnobObserverBuilder(knobState) { allocatedIndex, currentIndex ->
            MenuItem(
                boxModifier = Modifier.weight(1F, true),
                label = previousButtonLabel,
                chipOrientation = ItemChipOrientation.N,
                isSelected = currentIndex == allocatedIndex,
                onClicked = CallWhen(currentIndexIs = allocatedIndex) {
                    onPreviousClicked()
                }
            )
        }
        val measurements = ThemeWrapper.ThemeHandle.current.bigItem

        Column(
            Modifier.weight(3F, fill = true).fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            MenuItem(
                boxModifier = Modifier
                    .padding(
                        top = measurements.chipWidth.dp.halveIfNotPixelDoubled(),
                        bottom = measurements.highlightWidth.dp.halveIfNotPixelDoubled()
                    ),
                label = timePeriodLabel,
                chipOrientation = ItemChipOrientation.NONE,
                labelColor = ThemeWrapper.ThemeHandle.current.colors.textMenuColorAccent,
                onClicked = {}
            )
        }

        Row(Modifier.weight(2F)) {
            KnobObserverBuilder(knobState) { allocatedIndex: Int, currentIndex: Int ->
                MenuItem(
                    boxModifier = Modifier.weight(1f, fill = true),
                    label = menuButtonLabel,
                    chipOrientation = ItemChipOrientation.N,
                    isSelected = allocatedIndex == currentIndex,
                    onClicked = CallWhen(currentIndexIs = allocatedIndex) {
                        onMenuClicked()
                    }
                )
            }
            KnobObserverBuilder(knobState) { allocatedIndex: Int, currentIndex: Int ->
                MenuItem(
                    boxModifier = Modifier.weight(1f, fill = true),
                    label = nextButtonLabel,
                    chipOrientation = ItemChipOrientation.N,
                    isSelected = allocatedIndex == currentIndex,
                    onClicked = CallWhen(currentIndexIs = allocatedIndex) {
                        onNextClicked()
                    }
                )
            }

        }

    }
}


@Composable
fun NorthButtonRowWithScroll(
    knobState : KnobObserverBuilderState,
    previousButtonLabel : String = "⏴",
    nextButtonLabel : String = "⏵",
    upButtonLabel : String = "⏶",
    downButtonLabel : String = "⏷",
    menuButtonLabel : String = "Menu",

    timePeriodLabel : String,

    onPreviousClicked : () -> Unit,
    onNextClicked : () -> Unit,

    onMenuClicked : () -> Unit,

    onUpClicked : () -> Unit,
    onDownClicked : () -> Unit,
) {
    Row(
        modifier = Modifier
            .background(ThemeWrapper.ThemeHandle.current.colors.menuBackground)
            .fillMaxWidth()
    ) {

        KnobObserverBuilder(knobState) { allocatedIndex, currentIndex ->
            MenuItem(
                boxModifier = Modifier.weight(0.5F, true),
                label = downButtonLabel,
                chipOrientation = ItemChipOrientation.N,
                isSelected = currentIndex == allocatedIndex,
                onClicked = CallWhen(currentIndexIs = allocatedIndex) {
                    onDownClicked()
                }
            )
        }

        KnobObserverBuilder(knobState) { allocatedIndex, currentIndex ->
            MenuItem(
                boxModifier = Modifier.weight(0.5F, true),
                label = upButtonLabel,
                chipOrientation = ItemChipOrientation.N,
                isSelected = currentIndex == allocatedIndex,
                onClicked = CallWhen(currentIndexIs = allocatedIndex) {
                    onUpClicked()
                }
            )
        }

        val measurements = ThemeWrapper.ThemeHandle.current.bigItem

        Column(
            Modifier.weight(3F, fill = true).fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            MenuItem(
                boxModifier = Modifier
                    .padding(
                        top = measurements.chipWidth.dp.halveIfNotPixelDoubled(),
                        bottom = measurements.highlightWidth.dp.halveIfNotPixelDoubled()
                    ),
                label = timePeriodLabel,
                chipOrientation = ItemChipOrientation.NONE,
                labelColor = ThemeWrapper.ThemeHandle.current.colors.textMenuColorAccent,
                onClicked = {}
            )
        }

        Row(Modifier.weight(2F)) {
            KnobObserverBuilder(knobState) { allocatedIndex: Int, currentIndex: Int ->
                MenuItem(
                    boxModifier = Modifier.weight(1.4f, fill = true),
                    label = menuButtonLabel,
                    chipOrientation = ItemChipOrientation.N,
                    isSelected = allocatedIndex == currentIndex,
                    onClicked = CallWhen(currentIndexIs = allocatedIndex) {
                        onMenuClicked()
                    }
                )
            }

            KnobObserverBuilder(knobState) { allocatedIndex, currentIndex ->
                MenuItem(
                    boxModifier = Modifier.weight(0.8F, true),
                    label = previousButtonLabel,
                    chipOrientation = ItemChipOrientation.N,
                    isSelected = currentIndex == allocatedIndex,
                    onClicked = CallWhen(currentIndexIs = allocatedIndex) {
                        onPreviousClicked()
                    }
                )
            }
            KnobObserverBuilder(knobState) { allocatedIndex: Int, currentIndex: Int ->
                MenuItem(
                    boxModifier = Modifier.weight(0.8f, fill = true),
                    label = nextButtonLabel,
                    chipOrientation = ItemChipOrientation.N,
                    isSelected = allocatedIndex == currentIndex,
                    onClicked = CallWhen(currentIndexIs = allocatedIndex) {
                        onNextClicked()
                    }
                )
            }

        }

    }
}