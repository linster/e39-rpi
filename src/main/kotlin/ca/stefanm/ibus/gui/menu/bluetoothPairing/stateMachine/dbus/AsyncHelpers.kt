package ca.stefanm.ibus.gui.menu.bluetoothPairing.stateMachine.dbus

import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.yield
import org.freedesktop.dbus.DBusAsyncReply
import org.freedesktop.dbus.connections.impl.DBusConnection
import org.freedesktop.dbus.exceptions.DBusException
import org.freedesktop.dbus.interfaces.DBusInterface
import java.lang.Exception
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

//DBusInterface object, String m, Object... parameters
suspend fun <T> DBusConnection.convertAsyncCall(
    call : DBusConnection.() -> DBusAsyncReply<T>,
) = suspendCoroutine<T> {
    val reply: DBusAsyncReply<T> = try {
         this.call()
    } catch (e : Exception) {
        it.resumeWithException(e)
        return@suspendCoroutine
    }

    runBlocking {
        while (!reply.hasReply()) {
            yield()
        }
    }

    it.resume(reply.reply)
}