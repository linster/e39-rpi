package ca.stefanm.ibus.gui.map.guidance

import androidx.compose.runtime.collectAsState
import ca.stefanm.ibus.configuration.ConfigurationStorage
import ca.stefanm.ibus.configuration.E39Config
import ca.stefanm.ibus.configuration.E39Config.GuidanceConfig.SidePanelInstructionEnablementState
import ca.stefanm.ibus.di.ApplicationScope
import ca.stefanm.ibus.gui.map.guidance.notifications.ui.GuidanceSidePanel
import ca.stefanm.ibus.gui.map.mapScreen.MapScreen
import ca.stefanm.ibus.gui.menu.Notification
import ca.stefanm.ibus.gui.menu.navigator.Navigator
import ca.stefanm.ibus.gui.menu.notifications.NotificationHub
import ca.stefanm.ibus.gui.menu.widgets.modalMenu.ModalMenuService
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Provider

/**
 * Shows guidance instructions while a guidance session is active. The
 * GuidanceService calls every consumer, in order, from one coroutine:
 * [notifyInstruction] when the car approaches a maneuver, [updateDistance]
 * as it gets closer, [dismissInstruction] once it's passed (or dismissed, or
 * the destination is reached), and [guidanceEnded] when guidance stops.
 * Each consumer checks its own setting (E39Config.GuidanceConfig; Map
 * Settings -> Guidance Instructions).
 */
interface GuidanceInstructionConsumer {
    suspend fun notifyInstruction(instruction: GuidanceInstruction, distanceMeters: Double)
    suspend fun updateDistance(instruction: GuidanceInstruction, distanceMeters: Double) {}
    suspend fun dismissInstruction(instruction: GuidanceInstruction) {}
    suspend fun guidanceEnded() {}
}

/** A notification (the maneuver's picture, distance, instruction) per instruction. */
@ApplicationScope
class NotificationGuidanceInstructionConsumer @Inject constructor(
    private val notificationHub: NotificationHub,
    private val configurationStorage: ConfigurationStorage,
) : GuidanceInstructionConsumer {

    private var posted: Notification? = null

    private val enabled: Boolean
        get() = configurationStorage.config[E39Config.GuidanceConfig.notificationInstructionConsumerIsEnabled]

    override suspend fun notifyInstruction(instruction: GuidanceInstruction, distanceMeters: Double) {
        if (!enabled) return
        val notification = Notification(
            image = GuidanceIcons.notificationImage(instruction.kind),
            topText = GuidanceSidePanel.distanceLabel(instruction, distanceMeters).let {
                if (it == "Now" || it == "Arriving") it else "In $it"
            } + if (instruction.streets.isNotEmpty()) ": ${instruction.streets}" else "",
            contentText = instruction.text,
            duration = Notification.NotificationDuration.LONG,
        )
        posted = notification
        notificationHub.postNotificationBackground(notification)
    }

    override suspend fun dismissInstruction(instruction: GuidanceInstruction) = clearOurs()

    override suspend fun guidanceEnded() = clearOurs()

    /** Hide the notification if it's still ours (not one posted since). */
    private fun clearOurs() {
        val ours = posted ?: return
        if (notificationHub.currentNotification.value === ours) {
            notificationHub.clearNotification()
        }
        posted = null
    }
}

/**
 * A side panel (GuidanceSidePanel) that pops in with the coming instruction
 * and its distance, and goes when it's passed. Passive: the knob stays with
 * the screen behind it, and it doesn't replace a side pane the user opened.
 * Setting: never, only over the map, or always.
 */
@ApplicationScope
class SideBarGuidanceInstructionConsumer @Inject constructor(
    private val modalMenuService: ModalMenuService,
    private val configurationStorage: ConfigurationStorage,
    private val navigator: Provider<Navigator>,
) : GuidanceInstructionConsumer {

    private companion object {
        const val PANE_TAG = "SideBarGuidanceInstructionConsumer"
    }

    private val current = MutableStateFlow<Pair<GuidanceInstruction, Double>?>(null)

    private fun allowed(): Boolean = when (
        configurationStorage.config[E39Config.GuidanceConfig.sidePanelScreenInstructionConsumerIsEnabled]
    ) {
        SidePanelInstructionEnablementState.NEVER -> false
        SidePanelInstructionEnablementState.ALWAYS -> true
        SidePanelInstructionEnablementState.ONLY_OVER_MAP ->
            navigator.get().mainContentScreen.value.node is MapScreen
    }

    override suspend fun notifyInstruction(instruction: GuidanceInstruction, distanceMeters: Double) {
        current.value = instruction to distanceMeters
        showOrHide()
    }

    override suspend fun updateDistance(instruction: GuidanceInstruction, distanceMeters: Double) {
        current.value = instruction to distanceMeters
        // Also follows the setting and the screen (e.g. the user left the map).
        showOrHide()
    }

    override suspend fun dismissInstruction(instruction: GuidanceInstruction) {
        current.value = null
        modalMenuService.closeSidePaneOverlayIfTagged(PANE_TAG)
    }

    override suspend fun guidanceEnded() {
        current.value = null
        modalMenuService.closeSidePaneOverlayIfTagged(PANE_TAG)
    }

    private fun showOrHide() {
        if (current.value == null || !allowed()) {
            modalMenuService.closeSidePaneOverlayIfTagged(PANE_TAG)
            return
        }
        if (modalMenuService.isSidePaneOverlayTagged(PANE_TAG)) {
            return   // shown; it follows `current`
        }
        modalMenuService.showPassiveSidePaneOverlay(PANE_TAG) {
            current.collectAsState().value?.let { (instruction, meters) ->
                GuidanceSidePanel.Panel(instruction, meters)
            }
        }
    }
}

/** The guidance setup screens' view of the coming instruction (InGuidanceSubScreen). */
@ApplicationScope
class GuidanceSetupScreenInstructionConsumer @Inject constructor(
) : GuidanceInstructionConsumer {

    private val _current = MutableStateFlow<Pair<GuidanceInstruction, Double>?>(null)
    /** The instruction shown now and its distance; null between instructions. */
    val current: StateFlow<Pair<GuidanceInstruction, Double>?> = _current.asStateFlow()

    private val _instructionFlow = MutableSharedFlow<GuidanceInstruction>(replay = 1)
    /** Every instruction approached, as it's approached. */
    val instructionFlow: SharedFlow<GuidanceInstruction>
        get() = _instructionFlow

    override suspend fun notifyInstruction(instruction: GuidanceInstruction, distanceMeters: Double) {
        _current.value = instruction to distanceMeters
        _instructionFlow.emit(instruction)
    }

    override suspend fun updateDistance(instruction: GuidanceInstruction, distanceMeters: Double) {
        _current.value = instruction to distanceMeters
    }

    override suspend fun dismissInstruction(instruction: GuidanceInstruction) {
        _current.value = null
    }

    override suspend fun guidanceEnded() {
        _current.value = null
    }
}
