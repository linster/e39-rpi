package ca.stefanm.ibus.lib.bordmonitor.clock

import ca.stefanm.ibus.di.ApplicationModule
import ca.stefanm.ibus.lib.bordmonitor.input.IBusDevice
import ca.stefanm.ibus.lib.messages.IBusMessage
import ca.stefanm.ibus.lib.platform.LongRunningService
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.Channel
import javax.inject.Inject
import javax.inject.Named


//class ClockUpdateManager @Inject constructor(
//    @Named(ApplicationModule.IBUS_MESSAGE_OUTPUT_CHANNEL) private val messagesOut : Channel<IBusMessage>,
//    coroutineScope: CoroutineScope,
//    parsingDispatcher: CoroutineDispatcher
//) : LongRunningService(coroutineScope, parsingDispatcher) {
//
//
//    override suspend fun doWork() {
//        TODO("Not yet implemented")
//    }
//
//}
//
//
////https://github.com/tedsalmon/DroidIBus/blob/master/app/src/main/java/com/ibus/droidibus/ibus/systems/GFXNavigationSystem.java#L299
////TODO
////TODO the function above (and the one below it) need to be emulated so we can simulate
////TOD an IKE when the Mk4 drive tries to talk to us.
//class ClockUpdateMessage(
//    unixTimeStampSeconds : Long
//) : IBusMessage(
//    sourceDevice = IBusDevice.RADIO,
//    destinationDevice = IBusDevice.BROADCAST,
//    data = timeToMessage(unixTimeStampSeconds)
//) {
//
//    /**
//     * Send a new time setting to the IKE
//     * IBus message: 3B 06 80 40 01 <Hours> <Mins> <CRC>
//     * @param args Two ints MUST be provided
//     *  int hours, int minutes
//     * @return Byte array of composed message to send to IBus
//     */
//    private companion object {
//        fun timeToMessage(unixTimeStampSeconds: Long): ByteArray {
//            return arrayOf(0.toByte()).toByteArray()
//        }
//    }
//}
