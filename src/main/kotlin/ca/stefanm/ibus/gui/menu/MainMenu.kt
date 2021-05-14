package ca.stefanm.ibus.gui.menu

import androidx.compose.desktop.AppManager
import ca.stefanm.ibus.car.bordmonitor.input.IBusInputMessageParser
import ca.stefanm.ibus.car.bordmonitor.input.InputEvent
import ca.stefanm.ibus.gui.menu.widgets.ScrollListener
import ca.stefanm.ibus.lib.logging.Logger
import ca.stefanm.ibus.lib.messages.IBusMessage
import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.value.MutableValue
import com.arkivanov.decompose.value.Value
import com.ginsberg.cirkle.circular
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import javax.inject.Inject

class MainMenu @Inject constructor() {

}

class MainMenuComponent(
    componentContext: ComponentContext
) : ComponentContext by componentContext {

}

class BmwMenuParent(componentContext: ComponentContext) : ComponentContext by componentContext {


}