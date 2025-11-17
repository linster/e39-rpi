package ca.stefanm.ibus.gui.pim.calendar.setup.views

import androidx.compose.runtime.Composable
import ca.stefanm.ibus.gui.menu.navigator.NavigationNode
import ca.stefanm.ibus.gui.menu.navigator.Navigator
import javax.inject.Inject

class CalendarSetupRootScreen @Inject constructor(

) : NavigationNode<Nothing>{
    override val thisClass: Class<out NavigationNode<Nothing>>
        get() = CalendarSetupRootScreen::class.java

    override fun provideMainContent(): @Composable
        (incomingResult: Navigator.IncomingResult?) -> Unit = { params ->

    }
}