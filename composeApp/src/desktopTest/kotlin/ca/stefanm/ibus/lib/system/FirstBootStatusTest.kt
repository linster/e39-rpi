package ca.stefanm.ibus.lib.system

import ca.stefanm.ibus.lib.system.FirstBootTask.NotificationKind
import ca.stefanm.ibus.lib.system.FirstBootTask.State
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

internal class FirstBootStatusTest {

    @get:Rule
    val folder = TemporaryFolder()

    // As e39-rpi-yocto's load-image writes it.
    private val running = """
        state=running
        title=First-boot setup
        text=Calendars & mail
        minutes=20
        started=1000
    """.trimIndent()

    @Test
    fun parsesLoadImagesStatusFile() {
        assertEquals(
            FirstBootTask("e39pim", State.RUNNING, "First-boot setup", "Calendars & mail", 20, 1000),
            FirstBootTask.parse("e39pim", running)
        )
        val done = FirstBootTask.parse("e39pim", "state=done\ntitle=Calendars & mail ready\n")!!
        assertEquals(State.DONE, done.state)
        assertEquals("Calendars & mail ready" to "", done.notificationText(NotificationKind.DONE, 0))
    }

    @Test
    fun rejectsUnknownStatesAndMissingTitle() {
        assertNull(FirstBootTask.parse("x", "state=sleeping\ntitle=Hi"))
        assertNull(FirstBootTask.parse("x", "state=running\ntext=no title"))
        assertNull(FirstBootTask.parse("x", ""))
    }

    @Test
    fun minutesLeftCountDownAndStopAtOne() {
        val task = FirstBootTask.parse("e39pim", running)!!
        assertEquals(20, task.minutesLeft(1000))
        assertEquals(15, task.minutesLeft(1000 + 5 * 60 + 30))
        assertEquals(1, task.minutesLeft(1000 + 60 * 60))
        assertEquals(
            "First-boot setup" to "Calendars & mail: about 15 min left",
            task.notificationText(NotificationKind.REMINDER, 1000 + 5 * 60)
        )
        assertNull(task.copy(minutes = null).minutesLeft(1000))
        assertEquals("First-boot setup" to "Calendars & mail", task.copy(minutes = null).notificationText(NotificationKind.STARTED, 1000))
    }

    @Test
    fun notifiesOnStartAfterAMinuteThenEveryFiveMinutesAndOnTheOutcome() {
        val task = FirstBootTask.parse("e39pim", running)!!
        assertEquals(NotificationKind.STARTED, FirstBootTask.notificationKind(null, task, null, 0))
        // The first reminder after a minute (the HMI's window may not have been up yet)...
        assertNull(FirstBootTask.notificationKind(State.RUNNING, task, 30, 1))
        assertEquals(NotificationKind.REMINDER, FirstBootTask.notificationKind(State.RUNNING, task, 60, 1))
        // ...then every five.
        assertNull(FirstBootTask.notificationKind(State.RUNNING, task, 60, 2))
        assertEquals(NotificationKind.REMINDER, FirstBootTask.notificationKind(State.RUNNING, task, 5 * 60, 2))

        val done = task.copy(state = State.DONE, title = "Calendars & mail ready", text = "")
        assertEquals(NotificationKind.DONE, FirstBootTask.notificationKind(State.RUNNING, done, 30))
        // Already done when the HMI started (an HMI restart): nothing to say.
        assertNull(FirstBootTask.notificationKind(null, done, null))
        assertNull(FirstBootTask.notificationKind(State.DONE, done, 30))

        val failed = task.copy(state = State.FAILED)
        assertEquals(NotificationKind.FAILED, FirstBootTask.notificationKind(State.RUNNING, failed, 30))
        assertEquals(NotificationKind.FAILED, FirstBootTask.notificationKind(null, failed, null))
        assertNull(FirstBootTask.notificationKind(State.FAILED, failed, 30))
    }

    @Test
    fun readsStatusFilesFromTheDirectory() {
        folder.newFile("e39pim.status").writeText(running)
        folder.newFile("notes.txt").writeText("state=running\ntitle=ignored")
        folder.newFile("broken.status").writeText("garbage")
        val tasks = FirstBootStatusReader(folder.root).read()
        assertEquals(listOf("e39pim"), tasks.map { it.name })
        assertEquals(emptyList<FirstBootTask>(), FirstBootStatusReader(folder.root.resolve("missing")).read())
    }
}
