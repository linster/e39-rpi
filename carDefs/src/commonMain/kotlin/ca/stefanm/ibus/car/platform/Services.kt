package ca.stefanm.ibus.car.platform


import kotlinx.coroutines.*


interface Service {
    fun onCreate()
    fun onShutdown()
}

interface JoinableService : Service {
   var jobToJoin : Job?

   class ServiceShutdownException : CancellationException()
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
        jobToJoin?.cancel(cause = JoinableService.ServiceShutdownException())
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
        jobToJoin?.cancel(cause = JoinableService.ServiceShutdownException())
    }

    abstract suspend fun doWork()
}

