package ca.stefanm.ca.stefanm.ibus.gui.networkSetup.activateConnection.screens.debug

import androidx.compose.runtime.Composable
import ca.stefanm.ibus.autoDiscover.AutoDiscover
import ca.stefanm.ibus.gui.menu.navigator.NavigationNode
import ca.stefanm.ibus.gui.menu.navigator.Navigator
import javax.inject.Inject

@AutoDiscover
class FilterConnectionsListForDevicesUseCaseDebugScreen @Inject constructor(

) : NavigationNode<Nothing> {

    override val thisClass: Class<out NavigationNode<Nothing>>
        get() = FilterConnectionsListForDevicesUseCaseDebugScreen::class.java

    override fun provideMainContent(): @Composable ((Navigator.IncomingResult?) -> Unit) = {

    }
}