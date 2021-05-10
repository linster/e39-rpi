package ca.stefanm.ibus.car.platform

import ca.stefanm.ibus.lib.logging.Logger
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

class PlatformService(
    private val name : String,
    private val description : String,
    val baseService : Service,
    private val watchdogCoroutineScope : CoroutineScope = GlobalScope,
    private val logger : Logger
) : JoinableService {

    enum class RunStatus {
        STOPPED,
        RUNNING,
        ZOMBIE //Hasn't watch-dogged in a while.
    }

    private var watchdogJob : Job? = null

    override var jobToJoin: Job? = null

    var runStatus : RunStatus = RunStatus.STOPPED
        set(value) {
            updateLifecycle(field, value)
            field = value
        }

    private val _runStatus = MutableStateFlow(RunStatus.STOPPED)
    val runStatusFlow : Flow<RunStatus> = _runStatus

    override fun onCreate() {
        try {
            if (baseService is JoinableService) {
                watchdogJob = watchdogCoroutineScope.launch {
                    while (true) {
                        if (baseService.jobToJoin == null) {
                            //We haven't started the service yet.
                        } else {
                            if (baseService.jobToJoin?.isActive != true) {
                                runStatus = RunStatus.ZOMBIE
                            }
                        }
                        yield()
                    }
                }

            }
            runStatus = RunStatus.STOPPED

            baseService.onCreate()
            if (baseService is JoinableService) {
                jobToJoin = baseService.jobToJoin
            }

            runStatus = RunStatus.RUNNING

        } catch (e : Throwable) {
            runStatus = RunStatus.ZOMBIE
            throw e
        }
    }

    override fun onShutdown() {
        //Cancel the watchdog job before we shutdown the service so we don't
        //spruiously zombie.
        watchdogJob?.cancel()
        jobToJoin?.cancel()
        runStatus = RunStatus.STOPPED
    }

    private fun updateLifecycle(old : RunStatus, new : RunStatus) {
        logger.v(
            "PlatformService",
            "Changing service $name from $old to $new"
        )

        _runStatus.value = new

        when (old) {
            RunStatus.STOPPED -> when (new) {
                RunStatus.STOPPED -> {}
                RunStatus.RUNNING -> onCreate()
                RunStatus.ZOMBIE -> {}
            }
            RunStatus.RUNNING -> when (new) {
                RunStatus.STOPPED -> onShutdown()
                RunStatus.RUNNING -> {}
                RunStatus.ZOMBIE ->  {}
            }
            RunStatus.ZOMBIE -> when (new) {
                RunStatus.STOPPED -> onShutdown()
                RunStatus.RUNNING -> onCreate()
                RunStatus.ZOMBIE -> {}
            }
        }
    }
}