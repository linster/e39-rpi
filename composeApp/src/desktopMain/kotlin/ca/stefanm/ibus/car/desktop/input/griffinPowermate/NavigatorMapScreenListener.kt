package ca.stefanm.ca.stefanm.ibus.car.desktop.input.griffinPowermate

import ca.stefanm.ibus.gui.map.mapScreen.MapScreen
import ca.stefanm.ibus.gui.menu.navigator.Navigator
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Provider

class NavigatorMapScreenListener @Inject constructor(
    private val navigator: Provider<Navigator>
) {

    fun isOnMapScreenFlow() : Flow<Boolean> {
        return navigator.get().mainContentScreen.map {
            it.node.thisClass.isAssignableFrom(MapScreen::class.java)
        }
    }

    fun isOnMapScreen() : Boolean {
        return navigator.get().mainContentScreen.value.let {
            it.node.thisClass.isAssignableFrom(MapScreen::class.java)
        }
    }

}