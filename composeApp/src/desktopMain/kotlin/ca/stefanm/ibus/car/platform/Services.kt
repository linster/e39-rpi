package ca.stefanm.ca.stefanm.ibus.car.platform


import ca.stefanm.ibus.lib.messages.IBusMessage
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel


interface Service {
    fun onCreate()
    fun onShutdown()
}

interface JoinableService : Service {
   var jobToJoin : Job?
}

abstract class LongRunningService constructor(
    private val coroutineScope: CoroutineScope,
    private val parsingDispatcher: CoroutineDispatcher
) : JoinableService {

    override var jobToJoin : Job? = null

    override fun onCreate() {
        jobToJoin = coroutineScope.launch(parsingDispatcher) {
            doWork()
        }
    }

    override fun onShutdown() {
        jobToJoin?.cancel(cause = ForegroundPlatform.PlatformShutdownCancellationException())
    }

    abstract suspend fun doWork()
}

abstract class LongRunningLoopingService constructor(
    private val coroutineScope: CoroutineScope,
    private val parsingDispatcher: CoroutineDispatcher
) : JoinableService {

    override var jobToJoin : Job? = null

    override fun onCreate() {
        jobToJoin = coroutineScope.launch(parsingDispatcher) {
            while (true) {
                doWork()
                yield()
            }
        }
    }

    override fun onShutdown() {
        jobToJoin?.cancel(cause = ForegroundPlatform.PlatformShutdownCancellationException())
    }

    abstract suspend fun doWork()
}

