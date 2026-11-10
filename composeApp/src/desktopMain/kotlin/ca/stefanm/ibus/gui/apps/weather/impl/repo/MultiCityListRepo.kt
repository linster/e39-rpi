package ca.stefanm.ibus.gui.apps.weather.impl.repo

import ca.stefanm.ibus.gui.apps.weather.impl.views.MultiCityListEditorScreen.CityItem
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

class MultiCityListRepo @Inject constructor() {

    data class City(
        val name : String
    ) : CityItem {
        override fun cityDisplayName(): String = name
        override fun cityDetail(): String = "WAT"
    }

    private val cityList = MutableStateFlow<List<City>>(listOf(City("Ottawa"), City("Calgary"), City("Edmonton")))

    fun getMultiCityList() : StateFlow<List<City>> = cityList
    fun updateCityList(new : List<City>) {
        cityList.value = new
    }
}