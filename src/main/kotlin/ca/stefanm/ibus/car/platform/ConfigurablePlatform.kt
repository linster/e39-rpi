package ca.stefanm.ibus.car.platform

import ca.stefanm.ibus.car.bordmonitor.input.InputEvent
import ca.stefanm.ibus.car.di.ConfiguredCarComponent
import ca.stefanm.ibus.car.di.ConfiguredCarModule
import ca.stefanm.ibus.car.di.ConfiguredCarScope
import ca.stefanm.ibus.configuration.CarPlatformConfiguration
import ca.stefanm.ibus.configuration.ConfigurationStorage
import ca.stefanm.ibus.configuration.E39Config
import ca.stefanm.ibus.configuration.LaptopDeviceConfiguration
import ca.stefanm.ibus.di.ApplicationModule
import ca.stefanm.ibus.di.ApplicationScope
import ca.stefanm.ibus.di.DaggerApplicationComponent
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import javax.inject.Inject
import javax.inject.Named
import javax.inject.Provider

@ExperimentalCoroutinesApi
@ApplicationScope
class ConfigurablePlatform @Inject constructor(
    private val configurationStorage: Provider<ConfigurationStorage>
) {

    private var runStatusViewer : ConfigurablePlatformServiceRunStatusViewer? = null
    private val _servicesRunning = MutableStateFlow<List<ConfigurablePlatformServiceRunStatusViewer.RunStatusRecordGroup>>(
        listOf()
    )
    val servicesRunning : StateFlow<List<ConfigurablePlatformServiceRunStatusViewer.RunStatusRecordGroup>>
        get() = _servicesRunning.asStateFlow()

    private companion object {
        var _configurablePlatformServiceRunner: ConfigurablePlatformServiceRunner? = null
        var _configuredCarComponent: ConfiguredCarComponent? = null
    }

    var configurablePlatformServiceRunner: ConfigurablePlatformServiceRunner?
        get() = _configurablePlatformServiceRunner
        set(value) { _configurablePlatformServiceRunner = value}
    var configuredCarComponent: ConfiguredCarComponent?
        get() = _configuredCarComponent
        set(value) { _configuredCarComponent = value }


    var currentConfiguration : CarPlatformConfiguration? = null
        private set(value) {
            field = value
            runBlocking {
                if (value != null) {
                    _currentConfigurationFlow.emit(value)
                }
            }
        }

    private val _currentConfigurationFlow = MutableSharedFlow<CarPlatformConfiguration>(replay = 1)
    val currentConfigurationFlow : SharedFlow<CarPlatformConfiguration> = _currentConfigurationFlow

    private var serviceListJob : Job? = null

    fun run() {
        val config = E39Config.CarPlatformConfigSpec
            .toCarPlatformConfiguration(config = configurationStorage.get().config)
        run(config)
    }

    fun run(initialConfiguration: CarPlatformConfiguration = LaptopDeviceConfiguration()) {
        onNewDeviceConfiguration(currentConfiguration ?: initialConfiguration)
    }

    fun stop() {
        configurablePlatformServiceRunner?.stopAll()
        configurablePlatformServiceRunner = null
        serviceListJob?.cancel()
        _servicesRunning.value = listOf()
    }


    fun onNewDeviceConfiguration(configuration: CarPlatformConfiguration) {
        //destroy and recreate the Platform.
        stop()

        configuredCarComponent = DaggerApplicationComponent.create()
            .configuredCarComponent(ConfiguredCarModule(configuration))

        configurablePlatformServiceRunner =
            configuredCarComponent?.configurablePlatformServiceRunner()

        val startupServiceList = configurationStorage.get().config[E39Config.CarPlatformConfigSpec._listOfServiceGroupsOnStartup]
        if (startupServiceList.isEmpty()) {
            configurablePlatformServiceRunner?.runAll()
        } else {
            configurablePlatformServiceRunner?.startGroups(startupServiceList)
        }

        currentConfiguration = configuration

        runStatusViewer = ConfigurablePlatformServiceRunStatusViewer(
            configuredCarComponent!!.platformServiceList()
        )

        runStatusViewer!!.onNewConfiguredCar()
        serviceListJob = GlobalScope.launch {
            runStatusViewer!!.records.collect { _servicesRunning.value = it }
        }
    }

    fun stopByName(serviceName : String) {
        configurablePlatformServiceRunner?.stopByName(serviceName)
    }

    fun startByName(serviceName: String) {
        configurablePlatformServiceRunner?.startByName(serviceName)
    }

    fun findServiceRecordByName(serviceName: String) : Flow<ConfigurablePlatformServiceRunStatusViewer.RunStatusRecordService> {
        return runStatusViewer!!.findService(serviceName)
    }
}

@ConfiguredCarScope
class ConfigurablePlatformServiceRunner @Inject constructor(
    private val list: PlatformServiceList
) {
    fun runAll() {
        list.list.forEach { group ->
            group.children.forEach { service ->
                service.onCreate()
            }
        }
    }

    fun startGroups(serviceGroupNames : List<String>) {
        list.list.filter { it.name in serviceGroupNames }.forEach { platformServiceGroup ->
            platformServiceGroup.children.forEach { platformService ->
                platformService.onCreate()
            }
        }
    }

    fun stopAll() {
        list.list.forEach { group ->
            group.children.forEach { service ->
                service.onShutdown()
            }
        }
    }

    fun stopByName(serviceName : String) {
        findService(serviceName).onShutdown()
    }

    fun startByName(serviceName: String) {
        findService(serviceName).onCreate()
    }

    fun findService(name: String) : PlatformService {
        return list.list.map { it.children }.flatten().first { it.name == name }
    }
}

class ConfigurablePlatformServiceRunStatusViewer internal constructor(
    private val platformServiceList: PlatformServiceList
){

    data class RunStatusRecordGroup(
        val name : String,
        val description: String,
        val children : List<RunStatusRecordService>
    )
    data class RunStatusRecordService(
        val name : String,
        val description : String,
        val runStatus : Flow<PlatformService.RunStatus>,
        val startService : () -> Unit,
        val stopService : () -> Unit
    )

    private val _records : MutableStateFlow<List<RunStatusRecordGroup>> = MutableStateFlow(listOf())
    val records : StateFlow<List<RunStatusRecordGroup>> get() = _records


    internal fun onNewConfiguredCar() {
        //We need to cancel all the flows?

        val newList = platformServiceList.list.map { group ->
            RunStatusRecordGroup(
                name = group.name,
                description = group.description,
                children = group.children.map { service ->
                    RunStatusRecordService(
                        name = service.name,
                        description = service.description,
                        runStatus = flow { service.runStatusFlow.collect { emit(it) } },
                        startService = { service.onCreate() },
                        stopService = { service.onShutdown() }
                    )
                }
            )
        }
        _records.value = newList
    }

    fun findService(name : String) : Flow<RunStatusRecordService> {
        return _records
            .map { groupList -> groupList.firstOrNull { service -> service.name == name } }
            .filterNotNull()
            .map { it.children }
            .map { it.firstOrNull() }
            .filterNotNull()
    }
}