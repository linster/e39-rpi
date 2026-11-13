package ca.stefanm.ibus.gui.map.guidance

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.Button
import androidx.compose.material.Checkbox
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.RadioButton
import androidx.compose.material.Slider
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.WindowScope
import ca.stefanm.gui.map.guidance.GuidanceSessionStorage
import ca.stefanm.ibus.configuration.ConfigurationStorage
import ca.stefanm.ibus.configuration.E39Config
import ca.stefanm.ibus.configuration.E39Config.GuidanceConfig.SidePanelInstructionEnablementState
import ca.stefanm.ibus.gui.debug.windows.NestingCard
import ca.stefanm.ibus.gui.debug.windows.NestingCardHeader
import ca.stefanm.ibus.gui.map.Kanata_path
import ca.stefanm.ibus.gui.map.places.MapPoiLabels
import ca.stefanm.ibus.gui.map.repo.RemoteRouteRepository
import ca.stefanm.ibus.gui.menu.navigator.WindowManager
import com.javadocmd.simplelatlng.LatLng
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Guidance debug window (Debug launchpad): pick a route (start and end),
 * compute it with the maps backend, guide along it, and feed fake GPS
 * positions: drive along the route, or jump to a maneuver's approach (its
 * trigger radius), the maneuver itself or just past it; dismiss instructions.
 * The instruction consumers (notification, side panel) react as they would
 * in the car; the event log says what the tracker decided.
 */
class GuidanceDebugWindow @Inject constructor(
    private val configurationStorage: ConfigurationStorage,
    private val guidanceService: GuidanceService,
    private val guidanceSessionStorage: GuidanceSessionStorage,
    private val routeRepository: RemoteRouteRepository,
) : WindowManager.E39Window {

    override val tag: Any
        get() = this

    override val defaultPosition: WindowManager.E39Window.DefaultPosition
        get() = WindowManager.E39Window.DefaultPosition.ANYWHERE

    override val size = DpSize(1500.dp, 1000.dp)
    override val title = "Guidance Debug"

    private data class Preset(val name: String, val start: LatLng, val end: LatLng)

    private val presets = listOf(
        Preset("Parliament Hill -> Kanata Centrum", LatLng(45.4236, -75.7009), LatLng(45.3089, -75.9128)),
        Preset("Kanata (Kanata_path ends)", Kanata_path.first(), Kanata_path.last()),
        Preset("Ottawa -> Perth", LatLng(45.4215, -75.6972), LatLng(44.9007, -76.2482)),
        Preset("Westboro -> Rideau Centre", LatLng(45.3925, -75.7531), LatLng(45.4255, -75.6920)),
    )

    // The window's route: survives closing and reopening the window.
    private val start = mutableStateOf("45.4236, -75.7009")
    private val end = mutableStateOf("45.3089, -75.9128")
    private val route = mutableStateOf<RemoteRouteRepository.ComputedRoute?>(null)
    private val routeStatus = mutableStateOf("No route yet.")
    private val alongMeters = mutableStateOf(0.0)
    private val speedKmh = mutableStateOf("50")
    private val driving = mutableStateOf(false)

    override fun content(): @Composable WindowScope.() -> Unit = {
        Row(Modifier.fillMaxHeight()) {
            Column(Modifier.width(430.dp)) {
                RouteCard()
                SessionCard()
                ConsumersCard()
            }
            Column(Modifier.width(560.dp)) {
                ManeuversCard()
            }
            Column(Modifier.fillMaxWidth()) {
                FakeGpsCard()
                EventLogCard()
            }
        }
    }

    private fun parse(text: String): LatLng? = text.split(",").map { it.trim().toDoubleOrNull() }.let {
        if (it.size == 2 && it[0] != null && it[1] != null) LatLng(it[0]!!, it[1]!!) else null
    }

    private fun format(p: LatLng) = "%.5f, %.5f".format(p.latitude, p.longitude)

    @Composable
    private fun RouteCard() {
        val scope = rememberCoroutineScope()
        NestingCard {
            NestingCardHeader("Route")
            OutlinedTextField(start.value, { start.value = it }, label = { Text("Start (lat, lng)") })
            OutlinedTextField(end.value, { end.value = it }, label = { Text("End (lat, lng)") })
            presets.forEach { preset ->
                Button(onClick = {
                    start.value = format(preset.start)
                    end.value = format(preset.end)
                }) { Text(preset.name) }
            }
            Row {
                Button(onClick = { val s = start.value; start.value = end.value; end.value = s }) { Text("Swap") }
                Button(onClick = {
                    val from = parse(start.value)
                    val to = parse(end.value)
                    if (from == null || to == null) {
                        routeStatus.value = "Start and end must be \"lat, lng\"."
                        return@Button
                    }
                    routeStatus.value = "Computing..."
                    scope.launch {
                        routeRepository.computeRoute(from, to).fold(
                            onSuccess = {
                                route.value = it
                                alongMeters.value = 0.0
                                routeStatus.value = "%.1f km, %d min, %d maneuvers, %d points".format(
                                    it.lengthMeters / 1000, (it.durationSeconds / 60).toInt(),
                                    it.maneuvers.size, it.path.size)
                            },
                            onFailure = { routeStatus.value = "Failed: ${it.message}" },
                        )
                    }
                }) { Text("Compute route (maps backend)") }
            }
            Text(routeStatus.value)
        }
    }

    @Composable
    private fun SessionCard() {
        val state = guidanceService.getGuidanceSessionState().collectAsState(null).value
        NestingCard {
            NestingCardHeader("Guidance session: $state")
            Button(enabled = route.value != null, onClick = {
                val r = route.value ?: return@Button
                // startGuidance() returns at once if a session is running.
                if (guidanceService.getInstantaneousGuidanceSessionState() == GuidanceSession.SessionState.IN_GUIDANCE) {
                    guidanceService.stopGuidance()
                }
                guidanceService.useRoute(parse(start.value)!!, parse(end.value)!!, r)
                guidanceService.startGuidance()
                feed(alongMeters.value)
            }) { Text("Guide along this route (from the fake position)") }
            Row {
                Button(onClick = { guidanceService.startGuidance() }) { Text("Start") }
                Button(onClick = { guidanceService.stopGuidance() }) { Text("Stop") }
                Button(onClick = { guidanceService.clearCurrentSession() }) { Text("Clear session") }
                Button(onClick = { guidanceService.repeatLastDirection() }) { Text("Repeat") }
            }
        }
    }

    @Composable
    private fun ConsumersCard() {
        NestingCard {
            NestingCardHeader("Instruction consumers (Map Settings -> Guidance Instructions)")
            val notifications = remember {
                mutableStateOf(configurationStorage.config[E39Config.GuidanceConfig.notificationInstructionConsumerIsEnabled])
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(notifications.value, {
                    notifications.value = it
                    configurationStorage.config[E39Config.GuidanceConfig.notificationInstructionConsumerIsEnabled] = it
                })
                Text("Notifications")
            }
            val sidePanel = remember {
                mutableStateOf(configurationStorage.config[E39Config.GuidanceConfig.sidePanelScreenInstructionConsumerIsEnabled])
            }
            SidePanelInstructionEnablementState.entries.forEach { option ->
                Row(verticalAlignment = Alignment.CenterVertically) {
                    RadioButton(sidePanel.value == option, {
                        sidePanel.value = option
                        configurationStorage.config[E39Config.GuidanceConfig.sidePanelScreenInstructionConsumerIsEnabled] = option
                    })
                    Text("Side panel: $option")
                }
            }
        }
    }

    /** Meters along the route of the maneuver behind [instruction]. */
    private fun maneuverMeters(path: RoutePath, instruction: GuidanceInstruction): Double {
        val maneuver = route.value?.maneuvers?.getOrNull(instruction.index)
        return maneuver?.pathIndexRange?.first?.let { path.metersAt(it) }
            ?: path.distances[path.points.indices.minBy { GuidanceInstructionTracker.meters(path.points[it], instruction.location) }]
    }

    @Composable
    private fun ManeuversCard() {
        val instructions = guidanceService.instructions.collectAsState().value
        val tracker = guidanceService.trackerState.collectAsState().value
        val path = route.value?.let { RoutePath(it.path) }
        NestingCard(Modifier.fillMaxHeight()) {
            NestingCardHeader("Instructions (${instructions.size}) - the guided route's")
            LazyColumn {
                itemsIndexed(instructions) { i, instruction ->
                    val mark = when {
                        tracker.shownIndex == i -> "SHOWN"
                        i == tracker.nextIndex -> "next"
                        i < tracker.nextIndex -> "done"
                        else -> ""
                    }
                    Column(Modifier.padding(vertical = 4.dp)) {
                        Text("#${instruction.index} ${instruction.kind}  [$mark]  radius ${instruction.triggerRadiusMeters.toInt()} m",
                            fontWeight = if (mark == "SHOWN") FontWeight.Bold else FontWeight.Normal)
                        Text(instruction.text, fontSize = 12.sp)
                        if (path != null) {
                            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                val at = maneuverMeters(path, instruction)
                                Button(onClick = { jump(at - (instruction.triggerRadiusMeters - 5).coerceAtLeast(0.0)) }) { Text("Approach") }
                                Button(onClick = { jump(at) }) { Text("At") }
                                Button(onClick = { jump(at); jump(at + 50) }) { Text("Past") }
                            }
                        }
                    }
                }
            }
        }
    }

    private fun feed(meters: Double) {
        val path = route.value?.let { RoutePath(it.path) } ?: return
        guidanceService.updatePosition(path.at(meters))
    }

    private fun jump(meters: Double) {
        val path = route.value?.let { RoutePath(it.path) } ?: return
        alongMeters.value = meters.coerceIn(0.0, path.lengthMeters)
        feed(alongMeters.value)
    }

    @Composable
    private fun FakeGpsCard() {
        val position = guidanceService.position.collectAsState().value
        val tracker = guidanceService.trackerState.collectAsState().value
        val instructions = guidanceService.instructions.collectAsState().value
        val path = route.value?.let { remember(it) { RoutePath(it.path) } }
        NestingCard {
            NestingCardHeader("Fake GPS")
            Text("Position: ${position?.let(::format) ?: "none"}")
            Text("Next: #${instructions.getOrNull(tracker.nextIndex)?.index ?: "-"}, shown: " +
                    (tracker.shownIndex?.let { "#${instructions.getOrNull(it)?.index}" } ?: "none") +
                    (tracker.distanceMeters?.let { ", ${MapPoiLabels.distance(it)}" } ?: ""))
            if (path == null) {
                Text("Compute a route first.")
                return@NestingCard
            }
            Text("Along the route: ${MapPoiLabels.distance(alongMeters.value)} of ${MapPoiLabels.distance(path.lengthMeters)}")
            Slider(
                value = alongMeters.value.toFloat(),
                onValueChange = { alongMeters.value = it.toDouble(); feed(alongMeters.value) },
                valueRange = 0F..path.lengthMeters.toFloat(),
            )
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                listOf(-100, -10, 10, 100, 500).forEach { step ->
                    Button(onClick = { jump(alongMeters.value + step) }) { Text((if (step > 0) "+" else "") + "$step m") }
                }
            }
            val next = instructions.getOrNull(tracker.nextIndex)
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                Button(enabled = next != null, onClick = {
                    next?.let { jump(maneuverMeters(path, it) - (it.triggerRadiusMeters - 5).coerceAtLeast(0.0)) }
                }) { Text("Approach next") }
                Button(enabled = next != null, onClick = { next?.let { jump(maneuverMeters(path, it)) } }) { Text("At next") }
                Button(enabled = next != null, onClick = {
                    next?.let { val at = maneuverMeters(path, it); jump(at); jump(at + 50) }
                }) { Text("Past next") }
                Button(onClick = { guidanceService.dismissInstruction() }) { Text("Dismiss") }
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                OutlinedTextField(speedKmh.value, { speedKmh.value = it }, label = { Text("km/h") }, modifier = Modifier.width(100.dp))
                Button(onClick = { driving.value = !driving.value }) { Text(if (driving.value) "Stop driving" else "Drive") }
            }
            DriveLoop(path)
        }
    }

    /** While [driving]: move along the route at the given speed, a position every half second. */
    @Composable
    private fun DriveLoop(path: RoutePath) {
        LaunchedEffect(driving.value, path) {
            while (driving.value) {
                delay(500)
                val metersPerTick = (speedKmh.value.toDoubleOrNull() ?: 50.0) / 3.6 * 0.5
                jump(alongMeters.value + metersPerTick)
                if (alongMeters.value >= path.lengthMeters) driving.value = false
            }
        }
    }

    @Composable
    private fun EventLogCard() {
        val log = guidanceService.eventLog.collectAsState().value
        val listState = rememberLazyListState()
        LaunchedEffect(log.size) { if (log.isNotEmpty()) listState.scrollToItem(log.size - 1) }
        NestingCard(Modifier.fillMaxHeight()) {
            NestingCardHeader("Events")
            LazyColumn(state = listState) {
                items(log) { Text(it, fontSize = 12.sp) }
            }
        }
    }
}
