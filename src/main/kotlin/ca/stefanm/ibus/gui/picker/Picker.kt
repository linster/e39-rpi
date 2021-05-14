package ca.stefanm.ibus.gui.picker

import androidx.compose.runtime.Composable
import ca.stefanm.ibus.gui.menu.navigator.NavigationNode
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

//https://store.bimmernav.com/blogs/installation-bmw/bmw-mkiv-navigation-computer-features-and-benefits

interface PickList


class Picker @Inject constructor(
    private val autoCompleteSet : Flow<String>
) : NavigationNode {

    override val thisClass = Picker::class.java

    override fun provideMainContent(): @Composable () -> Unit {
        return { Picker() }
    }

    @Composable
    fun Picker(

    ) {

    }
}



