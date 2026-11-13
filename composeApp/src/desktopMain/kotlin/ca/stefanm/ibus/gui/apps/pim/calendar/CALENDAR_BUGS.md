# Calendar screens: known bugs (phase 3)

Found while testing the calendar against the PIM bridge (e39-rpi-pim) with
its test calendar (`scripts/run-dev.sh --test-calendar`, the week of
2026-11-16; see e39-rpi-pim's `testdata/README.md`). Data, repositories and
the bridge are fine; these are in the screens and agenda layout. Each code
site has a `TODO` pointing here. File/line references are as of commit
d3d6d79 on `pim-calendar`.

## 1. Agenda layout crashes on events that span several days

**Symptom.** Opening a week view that contains an event spanning more than
one day throws, and the HMI shows an "Unknown error" dialog:

```
Exception in thread "AWT-EventQueue-0" java.lang.NullPointerException
    at ...agenda.AgendaCalendarLayoutKt$AgendaCalendarLayout$4$8$1$1$1.invoke(AgendaCalendarLayout.kt:236)
    at androidx.constraintlayout.compose.ConstraintSetForInlineDsl.applyTo...
```

**Reproduce.** Run the bridge with the test calendar, then in the HMI
Calendar → Week (3 days) on a range including Fri 2026-11-13 (the
"Conference" runs Fri 11-13 09:00 to Mon 11-16 12:00 Phoenix). Any event
that isn't all on one day does it, including a timed event that ends at
midnight the next day (e.g. 22:00–00:00) and "Night drive" (11-18 22:00 to
11-19 02:00). All-day single-day events are fine: the mapper gives them
00:00 to 23:59:59.

**Cause.** In `views/parts/agenda/AgendaCalendarLayout.kt` (around lines
206–240):

```kotlin
events.forEach { event -> subdivisionCalculator.contributeEventToCalculation(event) }
val result = subdivisionCalculator.calculateAllSubdivisions()
events.forEach { event ->
    if (event.isVisibleOnCalendar(startDay, numberOfDays)) {
        ...
        start.linkTo(dayRefs[(dayNumber * subdivisionsPerDay) + result[event]!!.first]!!.start)   // line 236
```

`SubdivisionCalculator.calculateAllSubdivisions()`
(`views/parts/agenda/EventSplitter.kt`) first does
`rawEvents.map { it.splitToMultipleEvents() }.flatten()` and keys its result
map by those **single-day pieces**. The layout looks the result up with the
**unsplit** event, which isn't a key when the event was split, so
`result[event]!!` throws. (`AgendaCalendarEventData` is a data class, so
the pieces only equal the original when it wasn't split.)

**Fix idea.** Split before laying out, and use the pieces everywhere:

```kotlin
val pieces = events.flatMap { it.splitToMultipleEvents() }
pieces.forEach { subdivisionCalculator.contributeEventToCalculation(it) }
val result = subdivisionCalculator.calculateAllSubdivisions()
pieces.forEach { piece -> /* same as now, with piece; result[piece] exists */ }
```

Then each day's piece gets its own box (start/end hours from the piece), and
`isVisibleOnCalendar` can become a simple single-day check. Pieces keep the
event's `onClick` and `eventRef`, so clicking any piece opens the event.
Fix bug 3 (the splitter at month ends) at the same time: once the layout
splits everything, every multi-day event crossing a month boundary goes
through it.

Also worth replacing `!!` with a skip-and-log in the layout: one bad event
shouldn't take down the whole screen.

## 2. Week view arrows don't load the days they move to

**Symptom.** In Calendar → Week, the ◂/▸ arrows change the header (e.g.
"12 Nov - 15 Nov" → "15 Nov - 18 Nov") and the columns, but the events stay
the ones from the first range: the new days look empty.

**Cause.** Two separate "start day" states:

- `views/WeekScreen.kt` (around line 96) queries once, from today:
  ```kotlin
  val events = remember(today, numberOfDays) {
      weekViewRepo.getAgendaViewEvents(today, numberOfDays)
  }.collectAsState(emptyList()).value
  ```
- `views/parts/agenda/AgendaScreen.kt` keeps its own
  `val startDay = remember { mutableStateOf(<today>) }` and the arrows
  (`onPreviousClicked`/`onNextClicked`) only change that one. WeekScreen
  never hears about it, so it never queries the new range.

**Fix idea.** Hoist the start day into `WeekScreen` (or have `AgendaScreen`
take `startDay` and an `onStartDayChanged` callback) and key the query on
it:

```kotlin
val startDay = remember { mutableStateOf(today) }
val events = remember(startDay.value, numberOfDays) {
    weekViewRepo.getAgendaViewEvents(startDay.value, numberOfDays)
}.collectAsState(emptyList()).value
AgendaScreen(..., startDay = startDay.value, onStartDayChanged = { startDay.value = it })
```

Keep `remember(...)` around the repo call: each call starts its own query
and change stream. The flow keeps the previous list until the new range's
first answer arrives, which avoids a flash of empty columns.

The header's label also seems off by one day: `getLabelForDateRange` shows
`start` to `start + numberOfDays` (e.g. 3 days from the 12th shows
"12 Nov - 15 Nov"), but the last day shown is `start + numberOfDays - 1`.

## 3. Day splitter breaks at the end of a month

Already marked in `EventSplitter.kt` (`splitToMultipleEvents()`, a `TODO BUG`
comment). The next day's piece is built with
`LocalDateTime(startTime.year, startTime.month, startTime.day + 1, ...)`,
which throws `IllegalArgumentException` on the last day of a month (day 32,
or Feb 29/30) instead of rolling over. Fix: use
`startTime.date.plus(1, DateTimeUnit.DAY)` and build the next start from
that date at 00:00. With bug 1 fixed every multi-day event is split, so this
would be hit by any event crossing a month boundary.

## 4. Smaller things

- **Year boundaries in the agenda layout.**
  `AgendaCalendarEventData.isVisibleOnCalendar` and the layout's
  `dayNumber = (event.startTime.dayOfYear - startDay.dayOfYear)` compare
  `dayOfYear`, so a range crossing New Year (e.g. Dec 30 – Jan 2) misplaces
  or hides January's events. Use
  `startDay.daysUntil(event.startTime.date)` (kotlinx-datetime) instead.
  The layout also clamps `dayNumber` with `coerceIn(0..numberOfDays)`; the
  last valid day index is `numberOfDays - 1`.
- **Event editor preview draws the edited event twice.**
  `CalendarEventEditScreen` shows today's events from `WeekViewRepo` plus
  `eventToEdit.splitToMultipleEvents()`, and today's events already contain
  the event being edited, so it appears twice side by side (the narrow
  "W W" boxes). Leave out the event being edited from the repo's list, e.g.
  by matching `eventRef?.eventId` and `eventRef?.recurrenceId`.
- **Month view "today" ignores the year.** `CalendarDay` in
  `views/MonthScreen.kt` compares `dayOfYear` only, so Nov 12 of next or
  last year (visible when scrolling months) is also drawn as today. Compare
  the whole `LocalDate`.
- **`TwoWeekScreen` is empty.** Its `provideMainContent` has no content;
  Calendar's "Two Week" view shows a blank screen. It could be `WeekScreen`
  with 14 days, or removed from the view list.
- **No error states on the calendar screens.** When the bridge isn't
  reachable or Akonadi isn't running, the week/month/to-do screens just show
  no events. `RemoteCalendarRepository.connectionState` (and
  `checkConnection()` for Akonadi's state) say why; Calendar → Setup and the
  Calendar debug window already show it. A one-line banner on the screens
  would do.

## Shared widgets (found in phase 4, also not changed)

These affect other screens too, so they're listed here rather than fixed:

- **SmoothScroll resets the knob cursor to the first entry** whenever the
  list's data changes (e.g. after ticking a to-do). Its subcompose slot IDs
  are `index + maxAllocatedIndex` (`SmoothScroll.kt`), and
  `maxAllocatedIndex` grows with every allocation, so each recomposition
  gives every entry a new slot: all entries are disposed and re-created,
  each `KnobObserverBuilder` frees its index, and
  `AllocatedIdManager.removeId()` (`DynamicKnobListener.kt`) always moves the
  cursor to the minimum (`if (true)`). Fix ideas: slot IDs that don't change
  (the item index), and in `removeId` only move the cursor if the removed ID
  was the current one, to its neighbour.
- **`HalfScreenMenu.TwoColumn` keeps the items of its first composition**
  (`remember(knobListener) { mutableStateOf(circularList) }`), so changed
  labels and click handlers don't show. `TodoItemEditorScreen` works around
  it with `key(...)`.
- **`ScrollMenu` always adds the page counter row**: `ConjoinedScrollList`
  adds `pageIndicatorItem` regardless of
  `ScrollListOptions.isPageCountItemVisible` (while its item-count
  arithmetic does honour the flag).
