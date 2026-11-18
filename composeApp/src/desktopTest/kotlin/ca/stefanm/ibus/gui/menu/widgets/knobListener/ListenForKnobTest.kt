package ca.stefanm.ibus.gui.menu.widgets.knobListener

import androidx.compose.foundation.layout.Column
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.test.ComposeUiTest
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsNotSelected
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.runComposeUiTest
import ca.stefanm.ibus.car.bordmonitor.input.InputEvent
import ca.stefanm.ibus.car.bordmonitor.input.InputEvent.NavKnobTurned.Direction
import ca.stefanm.ibus.lib.logging.StdOutLogger
import kotlinx.coroutines.flow.MutableSharedFlow
import org.junit.Assert.assertEquals
import org.junit.Test
import javax.inject.Provider

/**
 * KnobListenerService.listenForKnob: the highlighted row is the row a knob
 * press acts on (#64).
 *
 * The rows are drawn as plain Text with the selected semantics, so the test
 * doesn't need a theme (ThemeWrapper reads the HMI's settings).
 */
@OptIn(ExperimentalTestApi::class)
internal class ListenForKnobTest {

    private data class Row(
        val label : String,
        val isSelectable : Boolean = true,
        val isSelected : Boolean = false
    )

    /** The Wi-Fi Password screen (PromptSecretScreen): three info rows, then the actions. */
    private val wifiPasswordRows = listOf(
        Row("Network: Home", isSelectable = false),
        Row("Security: WPA", isSelectable = false),
        Row("Password: <none>", isSelectable = false),
        Row("Enter Password..."),
        Row("Show Password"),
        Row("Connect"),
        Row("Cancel"),
    )

    private val events = MutableSharedFlow<InputEvent>(extraBufferCapacity = 16)
    private val clicked = mutableListOf<String>()

    private val service : KnobListenerService = run {
        lateinit var service : KnobListenerService
        val identifier = KnobListenerServiceIdentifier(Provider { service }, Provider { service }, Provider { service })
        service = KnobListenerService(events, identifier, StdOutLogger())
        service
    }

    @Composable
    private fun Menu(rows : List<Row>) {
        val shown = service.listenForKnob(
            listData = rows,
            onSelectAdapter = { row, isNowSelected -> row.copy(isSelected = isNowSelected) },
            isSelectableAdapter = { it.isSelectable },
            onItemClickAdapter = { clicked += it.label }
        ).value
        Column {
            for (row in shown) {
                Text(row.label, Modifier.semantics { selected = row.isSelected })
            }
        }
    }

    private fun ComposeUiTest.showMenu(rows : List<Row>) {
        setContent { Menu(rows) }
        // Knob events sent before the menu subscribes would be lost.
        waitUntil { events.subscriptionCount.value > 0 }
    }

    private fun ComposeUiTest.send(event : InputEvent) {
        check(events.tryEmit(event))
        waitForIdle()
    }

    private fun ComposeUiTest.turn(direction : Direction) = send(InputEvent.NavKnobTurned(1, direction))
    private fun ComposeUiTest.press() = send(InputEvent.NavKnobPressed)

    /** Exactly [label] is highlighted, out of [rows]. */
    private fun ComposeUiTest.assertOnlySelected(rows : List<Row>, label : String) {
        for (row in rows) {
            val node = onNodeWithText(row.label)
            if (row.label == label) node.assertIsSelected() else node.assertIsNotSelected()
        }
    }

    @Test
    fun infoRowsFirst_startsOnFirstSelectableRow() = runComposeUiTest {
        showMenu(wifiPasswordRows)
        assertOnlySelected(wifiPasswordRows, "Enter Password...")
    }

    @Test
    fun infoRowsFirst_pressRunsHighlightedRow() = runComposeUiTest {
        // The bug: this ran Cancel, which left the screen.
        showMenu(wifiPasswordRows)
        press()
        assertEquals(listOf("Enter Password..."), clicked)
        assertOnlySelected(wifiPasswordRows, "Enter Password...")
    }

    @Test
    fun infoRowsFirst_turnRightMovesToNextSelectableRow() = runComposeUiTest {
        showMenu(wifiPasswordRows)
        turn(Direction.RIGHT)
        assertOnlySelected(wifiPasswordRows, "Show Password")
        press()
        assertEquals(listOf("Show Password"), clicked)
    }

    @Test
    fun infoRowsFirst_turnLeftWrapsToLastSelectableRow() = runComposeUiTest {
        showMenu(wifiPasswordRows)
        turn(Direction.LEFT)
        assertOnlySelected(wifiPasswordRows, "Cancel")
        press()
        assertEquals(listOf("Cancel"), clicked)
    }

    @Test
    fun infoRowsFirst_turnRightPastEndWrapsToFirstSelectableRow() = runComposeUiTest {
        showMenu(wifiPasswordRows)
        repeat(4) { turn(Direction.RIGHT) }
        assertOnlySelected(wifiPasswordRows, "Enter Password...")
    }

    @Test
    fun firstRowSelectable_startsOnFirstRowAndPressRunsIt() = runComposeUiTest {
        val rows = listOf(Row("Go Back"), Row("Rename"), Row("Delete"))
        showMenu(rows)
        assertOnlySelected(rows, "Go Back")
        press()
        assertEquals(listOf("Go Back"), clicked)
    }

    @Test
    fun infoRowBetweenActions_isSkipped() = runComposeUiTest {
        val rows = listOf(Row("Connect"), Row("Signal: 80%", isSelectable = false), Row("Forget"))
        showMenu(rows)
        turn(Direction.RIGHT)
        assertOnlySelected(rows, "Forget")
        press()
        assertEquals(listOf("Forget"), clicked)
    }

    @Test
    fun noSelectableRows_knobIsIgnored() = runComposeUiTest {
        val rows = listOf(Row("Nothing here", isSelectable = false), Row("Still nothing", isSelectable = false))
        setContent { Menu(rows) }
        waitForIdle()
        // Used to throw on the first knob event (indexing an empty list).
        turn(Direction.RIGHT)
        press()
        assertEquals(emptyList<String>(), clicked)
        onNodeWithText("Nothing here").assertIsNotSelected()
        onNodeWithText("Still nothing").assertIsNotSelected()
    }
}
