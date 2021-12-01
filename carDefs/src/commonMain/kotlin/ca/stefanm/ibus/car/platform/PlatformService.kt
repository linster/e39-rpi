package ca.stefanm.ibus.car.platform

import ca.stefanm.ibus.logger.Logger
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

class PlatformService(
    val name : String,
    val description : String,
    val baseService : Service,
    private val watchdogCoroutineScope : CoroutineScope = GlobalScope,
    private val logger : Logger
) : JoinableService {

    enum class RunStatus {
        STOPPED,
        RUNNING,
        ZOMBIE //Hasn't watch-dogged in a while.
    }

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
            runStatus = RunStatus.RUNNING
        } catch (e : Throwable) {
            runStatus = RunStatus.ZOMBIE
            logger.v("PlatformService: $name", e.stackTraceToString())
        }
    }

    override fun onShutdown() {
        //Cancel the watchdog job before we shutdown the service so we don't
        //spruiously zombie.
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
                RunStatus.RUNNING -> startBaseService()
                RunStatus.ZOMBIE -> {}
            }
            RunStatus.RUNNING -> when (new) {
                RunStatus.STOPPED -> stopBaseService()
                RunStatus.RUNNING -> {}
                RunStatus.ZOMBIE ->  {}
            }
            RunStatus.ZOMBIE -> when (new) {
                RunStatus.STOPPED -> stopBaseService()
                RunStatus.RUNNING -> startBaseService()
                RunStatus.ZOMBIE -> {}
            }
        }
    }

    private fun startBaseService() {
        GlobalScope.launch {
            baseService.onCreate()
            if (baseService is JoinableService) {
                baseService.jobToJoin?.join()
            }
        }
    }

    private fun stopBaseService() {
        if (baseService is JoinableService) {
            baseService.jobToJoin?.cancel()
        }
        baseService.onShutdown()
    }
}