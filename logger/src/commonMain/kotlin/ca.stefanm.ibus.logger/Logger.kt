package ca.stefanm.ibus.logger

interface Logger {
    fun v(tag : String, msg : String)
    fun d(tag : String, msg : String)
    fun i(tag : String, msg : String)
    fun w(tag : String, msg : String)
    fun e(tag : String, msg : String)
    fun e(tag : String, msg : String, e : Throwable)
}

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

    override fun e(tag: String, msg: String, e: Throwable) =
        loggers.forEach { it.e(tag, msg, e) }
}

class LogDistributionHub constructor() : Logger {

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
    override fun e(tag: String, msg: String, e: Throwable) {
        LogEvent(LogEvent.Level.E, tag, msg, e).notifyObservers()
    }
}

//class StaticLogger(impl : Logger = DaggerApplicationComponent.create().logger()) : Logger by impl {
//    companion object : Logger by StaticLogger()
//}
