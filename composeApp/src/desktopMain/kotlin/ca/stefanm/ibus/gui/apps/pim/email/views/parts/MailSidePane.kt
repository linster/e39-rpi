package ca.stefanm.ibus.gui.pim.email.views.parts

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.unit.dp
import ca.stefanm.ibus.gui.menu.widgets.halveIfNotPixelDoubled
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import ca.stefanm.ibus.gui.menu.widgets.knobListener.dynamic.toDynamicLambda
import ca.stefanm.ibus.gui.menu.widgets.modalMenu.ModalMenuService
import ca.stefanm.ibus.gui.menu.widgets.modalMenu.SidePanelMenu
import ca.stefanm.ibus.gui.menu.widgets.modalMenu.SidePanelMenu.InfoLabel
import ca.stefanm.ibus.gui.menu.widgets.screenMenu.SmoothScroll
import ca.stefanm.ibus.gui.menu.widgets.screenMenu.TextMenuItem
import ca.stefanm.ibus.lib.logging.Logger

/** An entry of [showMailSidePane]; the pane closes before [onClicked] runs. */
data class SidePaneItem(val title: String, val onClicked: () -> Unit)

/**
 * A side pane with [title], [info] lines (the first bold), then "Go Back"
 * and [items] in a SmoothScroll on the modal knob listener: any number of
 * entries fits.
 */
fun ModalMenuService.showMailSidePane(
    logger: Logger,
    tag: String,
    title: String,
    info: List<String> = emptyList(),
    items: List<SidePaneItem>,
) {
    showSidePaneOverlayWithKnobListener(darkenBackground = true) { knobListenerServiceModal ->
        SidePanelMenu.SidePanelMenu(title = title) {
            if (info.isNotEmpty()) {
                // As SidePanelMenu pads its text.
                Column(Modifier.padding(horizontal = 10.dp.halveIfNotPixelDoubled(), vertical = 10.dp.halveIfNotPixelDoubled())) {
                    info.forEachIndexed { i, line -> InfoLabel(line, if (i == 0) FontWeight.Bold else FontWeight.Normal) }
                }
            }
            SmoothScroll.SmoothScroll(
                modifier = Modifier.fillMaxWidth(),
                knobListenerService = knobListenerServiceModal,
                tag = tag,
                logger = logger,
                items = (listOf(SidePaneItem("Go Back") {}) + items).map { item ->
                    TextMenuItem(item.title, onClicked = {
                        closeSidePaneOverlay(true)
                        item.onClicked()
                    }).toDynamicLambda()
                },
            )
        }
    }
}
