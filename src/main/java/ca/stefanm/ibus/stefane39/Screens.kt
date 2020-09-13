package ca.stefanm.ibus.stefane39

import ca.stefanm.ibus.di.ApplicationModule
import ca.stefanm.ibus.lib.bordmonitor.input.InputEvent
import ca.stefanm.ibus.lib.bordmonitor.menu.Screen
import ca.stefanm.ibus.lib.bordmonitor.menu.ScreenManager
import ca.stefanm.ibus.lib.bordmonitor.menu.ScreenWidget
import ca.stefanm.ibus.lib.bordmonitor.menu.painter.ScreenPainter
import ca.stefanm.ibus.lib.hardwareDrivers.VideoEnableRelayManager
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Named
//
//class StefanE39Screens @Inject constructor(
//    @Named(ApplicationModule.CHANNEL_INPUT_EVENTS) inputEventChannel : Channel<InputEvent>,
//    private val videoEnableRelayManager: VideoEnableRelayManager,
//    private val screenManager: ScreenManager,
//    private val screenPainter: ScreenPainter,
//    private val coroutineScope: CoroutineScope,
//    private val dispatcher: CoroutineDispatcher = Dispatchers.IO
//) {
//
//    init {
//        coroutineScope.launch(dispatcher) {
//            while (true) {
//                inputEventChannel.receive().let {
//                    if (it == InputEvent.BMBTPhonePressed) {
//                        onPhoneButtonPress()
//                    }
//                }
//            }
//        }
//    }
//
//    var phoneMenuShowing = false
//    var androidAutoShowing = false
//
//
//    private fun onPhoneButtonPress() {
//        phoneMenuShowing = !phoneMenuShowing
//
//        if (phoneMenuShowing) {
//            screenManager.showScreen(mainScreen)
//        } else {
//            screenManager.clearBackStack()
//        }
//
//    }
//
//    private val mainScreen = object : Screen(
//        title = Title(
//
//        ),
//        indexWidgets = listOf(
//            ScreenWidget.Button("Launch Navit") {
//                androidAutoShowing = !androidAutoShowing
//                videoEnableRelayManager.rpiVideoEnabled = androidAutoShowing
//            }
//        ),
//        screenPainter = screenPainter
//    )
//}