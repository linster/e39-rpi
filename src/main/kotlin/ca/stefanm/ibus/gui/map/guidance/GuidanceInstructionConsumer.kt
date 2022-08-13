package ca.stefanm.ibus.gui.map.guidance

import ca.stefanm.ibus.gui.menu.notifications.NotificationHub
import ca.stefanm.ibus.gui.menu.widgets.modalMenu.ModalMenuService
import com.javadocmd.simplelatlng.LatLng
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import javax.inject.Inject

interface GuidanceInstructionConsumer {
    data class GuidanceInstruction(
        //When is the user supposed to make the turn?
        val locationOfInstruction : LatLng,
        //What's the circle at which we need to tell the user to make the turn?
        val notificationRadius : LatLng,

        )

    suspend fun notifyInstruction(instruction: GuidanceInstruction)
}

class NotificationGuidanceInstructionConsumer @Inject constructor(
    private val notificationHub: NotificationHub
): GuidanceInstructionConsumer {
    override suspend fun notifyInstruction(instruction: GuidanceInstructionConsumer.GuidanceInstruction) {

    }
}

class SideBarGuidanceInstructionConsumer @Inject constructor(
    private val modalMenuService: ModalMenuService
) : GuidanceInstructionConsumer {


    override suspend fun notifyInstruction(instruction: GuidanceInstructionConsumer.GuidanceInstruction) {

        //Check config.
        //If map_only, then check if map is visible state.
    }


}

class GuidanceSetupScreenInstructionConsumer @Inject constructor(
) : GuidanceInstructionConsumer {
    override suspend fun notifyInstruction(instruction: GuidanceInstructionConsumer.GuidanceInstruction) {
        _instructionFlow.emit(instruction)
    }

    val instructionFlow : SharedFlow<GuidanceInstructionConsumer.GuidanceInstruction>
        get() = _instructionFlow
    private val _instructionFlow = MutableSharedFlow<GuidanceInstructionConsumer.GuidanceInstruction>()
}