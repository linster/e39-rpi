package ca.stefanm.ibus.stefane39

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