package ca.stefanm.ibus.gui.map.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import ca.stefanm.ca.stefanm.ibus.gui.menu.widgets.themes.ThemeWrapper
import ca.stefanm.ibus.configuration.ConfigurationStorage
import ca.stefanm.ibus.configuration.E39Config
import ca.stefanm.ibus.gui.map.MapScreen
import ca.stefanm.ibus.gui.menu.navigator.NavigationNode
import ca.stefanm.ibus.gui.menu.navigator.NavigationNodeTraverser
import ca.stefanm.ibus.gui.menu.navigator.Navigator
import ca.stefanm.ibus.gui.menu.widgets.BmwSingleLineHeader
import ca.stefanm.ibus.gui.menu.widgets.ChipItemColors
import ca.stefanm.ibus.gui.menu.widgets.ItemChipOrientation
import ca.stefanm.ibus.gui.menu.widgets.screenMenu.FullScreenMenu
import ca.stefanm.ibus.gui.menu.widgets.screenMenu.HalfScreenMenu
import ca.stefanm.ibus.gui.menu.widgets.screenMenu.TextMenuItem
import com.javadocmd.simplelatlng.LatLng
import javax.inject.Inject

class MapCenterSettingsScreen @Inject constructor(
    private val navigationNodeTraverser: NavigationNodeTraverser,
    private val configurationStorage: ConfigurationStorage
) : NavigationNode<Nothing> {

    override val thisClass: Class<out NavigationNode<Nothing>>
        get() = MapCenterSettingsScreen::class.java

    override fun provideMainContent(): @Composable (incomingResult: Navigator.IncomingResult?) -> Unit = { params ->

        val chosenCenter = if (params?.resultFrom == MapScreen::class.java && params.result is MapScreen.MapScreenResult) {
            (params.result as? MapScreen.MapScreenResult.PointSelectedResult)?.point
        } else { null }

        if (chosenCenter != null) {
            configurationStorage.config[E39Config.MapConfig.defaultMapCenter] = Pair(
                chosenCenter.latitude,
                chosenCenter.longitude
            )
        }

        val center = remember(params) { mutableStateOf(
            LatLng(
                configurationStorage.config[E39Config.MapConfig.defaultMapCenter].first,
                configurationStorage.config[E39Config.MapConfig.defaultMapCenter].second
            )
        ) }

        Column {
            BmwSingleLineHeader("Set Default Map Center")

            Column(Modifier.background(ThemeWrapper.ThemeHandle.current.colors.menuBackground)) {
                TextMenuItem(
                    title = "Map Center: ${center.value.latitude}, ${center.value.longitude}",
                    isSelectable = false,
                    onClicked = {}
                ).toView(ItemChipOrientation.NONE)()

                Column(Modifier.fillMaxWidth().fillMaxHeight(), verticalArrangement = Arrangement.Bottom) {
                    HalfScreenMenu.OneColumn(
                        fullWidth = true,
                        items = listOf(
                            TextMenuItem(
                                title = "Go Back",
                                onClicked = {
                                    navigationNodeTraverser.goBack()
                                }
                            ),
                            TextMenuItem(
                                title = "Pick Default Center...",
                                onClicked = {
                                    MapScreen.openForUserLocationSelection(navigationNodeTraverser)
                                }
                            )
                        )
                    )
                }
            }
        }
    }
}