package ca.stefanm.ca.stefanm.ibus.gui.apps.fileManager.impl.views

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ca.stefanm.ca.stefanm.ibus.gui.apps.fileManager.FilePickerScreen.Companion.FilePickerResult
import ca.stefanm.ca.stefanm.ibus.gui.apps.fileManager.FilePickerScreen.Companion.FilerPickerParameters
import ca.stefanm.ca.stefanm.ibus.gui.apps.fileManager.impl.repo.MostRecentlyUsedRepo
import ca.stefanm.ibus.di.DaggerApplicationComponent
import ca.stefanm.ibus.gui.menu.navigator.NavigationNodeTraverser
import ca.stefanm.ibus.gui.menu.widgets.ArbitraryContentsMenuItem
import ca.stefanm.ibus.gui.menu.widgets.CenterGradientWithEdgeHighlight
import ca.stefanm.ibus.gui.menu.widgets.ItemChipOrientation
import ca.stefanm.ibus.gui.menu.widgets.MenuItem
import ca.stefanm.ibus.gui.menu.widgets.halveIfNotPixelDoubled
import ca.stefanm.ibus.gui.menu.widgets.modalMenu.ModalMenuService
import ca.stefanm.ibus.gui.menu.widgets.screenMenu.SmoothScroll
import ca.stefanm.ibus.gui.menu.widgets.themes.ThemeWrapper
import ca.stefanm.ibus.lib.logging.Logger
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.format
import kotlinx.datetime.toLocalDateTime
import javax.inject.Inject

class FilePickerMruPane @Inject constructor(
    private val modalMenuService: ModalMenuService,
    private val logger: Logger,
    private val navigationNodeTraverser: NavigationNodeTraverser,
    private val mostRecentlyUsedRepo: MostRecentlyUsedRepo
) {

    companion object {
        const val TAG = "FilePickerMruPane"
    }

    fun showMruPane(
        parameters: FilerPickerParameters,
        onQuickFileSelect : (FilePickerResult) -> Unit
    ) {
        modalMenuService.showSidePaneOverlayWithKnobListener(darkenBackground = true) { knobListenerServiceModal ->
            Column(
                Modifier
                    .fillMaxSize()
                    .background(ThemeWrapper.ThemeHandle.current.colors.menuBackground)
                    .border(
                        width = 4.dp.halveIfNotPixelDoubled(),
                        color = ThemeWrapper.ThemeHandle.current.colors.sideMenuBorder
                    )
                    .shadow(4.dp.halveIfNotPixelDoubled(), RectangleShape),
                verticalArrangement = Arrangement.SpaceBetween
            ) {

                CenterGradientWithEdgeHighlight {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        Text(
                            text = "Select File".uppercase(),
                            fontSize = ThemeWrapper.ThemeHandle.current.hmiHeaderFooter.fontSize,
                            fontWeight = FontWeight.Bold,
                            color = ThemeWrapper.ThemeHandle.current.hmiHeaderFooter.fontColor,
                        )
                    }
                }

                val entries = mostRecentlyUsedRepo.getEntriesForType(parameters.filter).collectAsState(emptyList())

                SmoothScroll.SmoothScroll(
                    modifier = Modifier.fillMaxWidth(),
                    knobListenerService = knobListenerServiceModal,
                    tag = TAG,
                    logger = logger,
                    navigationNodeTraverser = navigationNodeTraverser,
                    prependGoBackEntry = false,
                    items = buildList {
                        add(@Composable { allocatedIndex, currentIndex ->
                            MenuItem(
                                label = "Go Back",
                                chipOrientation = ItemChipOrientation.W,
                                isSelected = allocatedIndex == currentIndex,
                                onClicked = CallWhen(currentIndexIs = allocatedIndex) {
                                    modalMenuService.closeSidePaneOverlay(true)
                                    onQuickFileSelect(FilePickerResult.NoFileChosen)
                                }
                            )
                        })
                        add(@Composable { allocatedIndex, currentIndex ->
                            MenuItem(
                                label = "Clear Recents for file type",
                                chipOrientation = ItemChipOrientation.W,
                                isSelected = allocatedIndex == currentIndex,
                                onClicked = CallWhen(currentIndexIs = allocatedIndex) {
                                    modalMenuService.closeSidePaneOverlay(true)
                                    mostRecentlyUsedRepo.clearType(parameters.filter)
                                }
                            )
                        })

                        entries.value.forEach { entry ->
                            add(@Composable { allocatedIndex, currentIndex ->
                                ArbitraryContentsMenuItem(
                                    isSelected = allocatedIndex == currentIndex,
                                    chipOrientation = ItemChipOrientation.W,
                                    isSmallSize = true,
                                    onClicked = CallWhen(currentIndexIs = allocatedIndex) {
                                        modalMenuService.closeSidePaneOverlay(true)
                                        onQuickFileSelect(FilePickerResult.FileChosen(entry.file))
                                    }
                                ) {
                                    Column {

                                        val fontSize = if (ThemeWrapper.ThemeHandle.current.isPixelDoubled) 18.sp else 9.sp

                                        Text(
                                            text = entry.file.name,
                                            color = ThemeWrapper.ThemeHandle.current.colors.TEXT_WHITE,
                                            fontSize = fontSize,
                                            fontWeight = FontWeight.Normal
                                        )
                                        Text(
                                            text = entry.file.canonicalPath,
                                            color = ThemeWrapper.ThemeHandle.current.colors.textMenuColorAccent,
                                            fontSize = fontSize,
                                            fontWeight = FontWeight.Normal
                                        )
                                    }
                                }
                            })
                        }


                    }
                )

            }
        }
    }



}