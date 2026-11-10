package ca.stefanm.ibus.lib.logging

import ca.stefanm.ibus.di.ApplicationScope
import ca.stefanm.ibus.di.DaggerApplicationComponent
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import javax.inject.Inject

interface Logger {
    fun v(tag : String, msg : String)
    fun d(tag : String, msg : String)
    fun i(tag : String, msg : String)
    fun w(tag : String, msg : String)
    fun e(tag : String, msg : String)
    fun e(tag : String, msg : String, e : Throwable?)
}

@ApplicationScope
class StdOutLogger @Inject constructor() : Logger {
    override fun v(tag: String, msg: String) {
        println("VERBOSE : $tag / $msg")
    }

    override fun d(tag: String, msg: String) {
        println("DEBUG : $tag / $msg")
    }

    override fun i(tag: String, msg: String) {
        println("INFO : $tag / $msg")
    }

    override fun w(tag: String, msg: String) {
        println("WARN : $tag / $msg")
    }

    override fun e(tag: String, msg: String) {
        println("ERROR : $tag / $msg")
    }

    override fun e(tag: String, msg: String, e: Throwable?) {
        if (e != null) {
            e(tag, "$msg exception: ${e.stackTraceToString()}")
        } else {
            e(tag, msg)
        }
    }
}

/**
 * Logs to the systemd journal (see [Journald]): priority from the level, plus
 * the fields HMI_TAG (the tag) and HMI_LEVEL (V/D/I/W/E; V and D are both
 * journal priority "debug"). E.g. `journalctl -t e39rpi-hmi HMI_TAG=Bluetooth`.
 * Falls back to stdout if an entry can't be written.
 */
@ApplicationScope
class JournaldLogger @Inject constructor(
    private val fallback: StdOutLogger
) : Logger {

    private fun send(level: String, priority: Int, tag: String, msg: String, orElse: () -> Unit) {
        val sent = Journald.send(priority, "$tag / $msg", mapOf("HMI_TAG" to tag, "HMI_LEVEL" to level))
        if (!sent) orElse()
    }

    override fun v(tag: String, msg: String) =
        send("V", Journald.Priority.DEBUG, tag, msg) { fallback.v(tag, msg) }

    override fun d(tag: String, msg: String) =
        send("D", Journald.Priority.DEBUG, tag, msg) { fallback.d(tag, msg) }

    override fun i(tag: String, msg: String) =
        send("I", Journald.Priority.INFO, tag, msg) { fallback.i(tag, msg) }

    override fun w(tag: String, msg: String) =
        send("W", Journald.Priority.WARNING, tag, msg) { fallback.w(tag, msg) }

    override fun e(tag: String, msg: String) =
        send("E", Journald.Priority.ERR, tag, msg) { fallback.e(tag, msg) }

    override fun e(tag: String, msg: String, e: Throwable?) {
        if (e != null) {
            e(tag, "$msg exception: ${e.stackTraceToString()}")
        } else {
            e(tag, msg)
        }
    }
}

@ApplicationScope
class CompositeLogger(private vararg var loggers : Logger) : Logger {
    override fun v(tag: String, msg: String) =
        loggers.forEach { it.v(tag, msg) }

    override fun d(tag: String, msg: String) =
        loggers.forEach { it.d(tag, msg) }

    override fun i(tag: String, msg: String) =
        loggers.forEach { it.i(tag, msg) }

    override fun w(tag: String, msg: String) =
        loggers.forEach { it.w(tag, msg) }

    override fun e(tag: String, msg: String) =
        loggers.forEach { it.e(tag, msg) }

    override fun e(tag: String, msg: String, e: Throwable?) =
        loggers.forEach { it.e(tag, msg, e) }
}

@ApplicationScope
class LogDistributionHub @Inject constructor() : Logger {

    //This is a hack. I don't know why Dagger gave everyone a separate
    //instance of the hub. There should be enough scope sprinkled everywhere.
    private companion object {
        private val observers = mutableListOf<(LogEvent) -> Unit>()
    }

    fun registerObserver(observer : (LogEvent) -> Unit) {
        //println("${this.hashCode()} Registering observer: ${observer.hashCode()}")
        observers.add(observer)
    }

    fun unregisterObserver(observer: (LogEvent) -> Unit) {
        //println("${this.hashCode()} Unregistering observer ${observer.hashCode()}")
        observers.remove(observer)
    }

    data class LogEvent(
        val level : Level,
        val tag : String,
        val message : String,
        val exception : Throwable?
    ) {
        enum class Level { V, D, I, W, E }

        companion object {
            val EMPTY = LogEvent(
                Level.D, "", "", null
            )
        }
    }

    private fun LogEvent.notifyObservers() {
        //println("${this.hashCode()} Notifying observers ${observers.size}")
        observers.forEach { it.invoke(this) }
    }

    override fun v(tag: String, msg: String) {
        LogEvent(LogEvent.Level.V, tag, msg, null).notifyObservers()
    }

    override fun d(tag: String, msg: String) {
        LogEvent(LogEvent.Level.D, tag, msg, null).notifyObservers()
    }

    override fun i(tag: String, msg: String) {
        LogEvent(LogEvent.Level.I, tag, msg, null).notifyObservers()
    }
    override fun w(tag: String, msg: String) {
        LogEvent(LogEvent.Level.W, tag, msg, null).notifyObservers()
    }
    override fun e(tag: String, msg: String) {
        LogEvent(LogEvent.Level.E, tag, msg, null).notifyObservers()
    }
    override fun e(tag: String, msg: String, e: Throwable?) {
        LogEvent(LogEvent.Level.E, tag, msg, e).notifyObservers()
    }
}

class StaticLogger(impl : Logger = DaggerApplicationComponent.create().logger()) : Logger by impl {
    companion object : Logger by StaticLogger()
}
