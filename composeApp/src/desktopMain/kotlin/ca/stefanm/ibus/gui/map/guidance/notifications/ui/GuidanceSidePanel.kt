package ca.stefanm.ibus.gui.map.guidance.notifications.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ca.stefanm.ibus.gui.map.guidance.GuidanceIcons
import ca.stefanm.ibus.gui.map.guidance.GuidanceInstruction
import ca.stefanm.ibus.gui.map.guidance.GuidanceInstructionTracker
import ca.stefanm.ibus.gui.map.places.MapPoiLabels
import ca.stefanm.ibus.gui.menu.widgets.halveIfNotPixelDoubled
import ca.stefanm.ibus.gui.menu.widgets.modalMenu.SidePanelMenu
import ca.stefanm.ibus.gui.menu.widgets.themes.ThemeWrapper
import org.jetbrains.compose.resources.painterResource

/**
 * The side panel the SideBarGuidanceInstructionConsumer shows while a
 * guidance instruction is coming up:
 *
 *     | GUIDANCE         3/12 |
 *     |        [picture]      |
 *     |         350 m         |
 *     |      Take exit 2      |   (roundabouts)
 *     |      Main Street      |
 *     | Turn left onto Main   |
 *     | Street.               |
 */
object GuidanceSidePanel {

    /** "350 m", or "Now" once the car is at the maneuver. */
    fun distanceLabel(instruction: GuidanceInstruction, distanceMeters: Double): String = when {
        distanceMeters <= GuidanceInstructionTracker.AT_METERS -> if (instruction.isDestination) "Arriving" else "Now"
        else -> MapPoiLabels.distance(distanceMeters)
    }

    fun headerLabel(instruction: GuidanceInstruction): String =
        if (instruction.isDestination) "Destination" else "Guidance  ${instruction.index + 1}/${instruction.count}"

    @Composable
    fun Panel(instruction: GuidanceInstruction, distanceMeters: Double) {
        val theme = ThemeWrapper.ThemeHandle.current
        fun size(doubled: Int): TextUnit = if (theme.isPixelDoubled) doubled.sp else (doubled / 2).sp

        SidePanelMenu.SidePanelMenu(title = headerLabel(instruction)) {
            Column(
                Modifier.fillMaxSize().padding(10.dp.halveIfNotPixelDoubled()),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(6.dp.halveIfNotPixelDoubled()),
            ) {
                Image(
                    painter = painterResource(GuidanceIcons.drawable(instruction.kind)),
                    contentDescription = instruction.kind.name,
                    modifier = Modifier.fillMaxHeight(0.42F).aspectRatio(1F),
                )
                Text(
                    text = distanceLabel(instruction, distanceMeters),
                    color = theme.colors.TEXT_WHITE,
                    fontSize = size(44),
                    fontWeight = FontWeight.Bold,
                )
                if (instruction.roundaboutExit > 0) {
                    Text(
                        text = "Take exit ${instruction.roundaboutExit}",
                        color = theme.colors.TEXT_WHITE,
                        fontSize = size(26),
                        fontWeight = FontWeight.Bold,
                    )
                }
                if (instruction.streets.isNotEmpty()) {
                    Text(
                        text = instruction.streets,
                        color = theme.colors.TEXT_WHITE,
                        fontSize = size(26),
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                Text(
                    text = instruction.text,
                    color = theme.colors.TEXT_WHITE,
                    fontSize = size(22),
                    textAlign = TextAlign.Center,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}
