package ca.stefanm.ibus.gui.platformConfig

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.snapshots.SnapshotStateMap
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.WindowScope
import ca.stefanm.ibus.car.platform.DiscoveredPlatformServiceGroup
import ca.stefanm.ibus.car.platform.ConfigurablePlatform
import ca.stefanm.ibus.configuration.ConfigurationStorage
import ca.stefanm.ibus.configuration.E39Config
import ca.stefanm.ibus.di.ApplicationScope
import ca.stefanm.ibus.gui.debug.windows.CheckBoxWithLabel
import ca.stefanm.ibus.gui.debug.windows.NestingCard
import ca.stefanm.ibus.gui.debug.windows.NestingCardHeader
import ca.stefanm.ibus.gui.menu.navigator.WindowManager
import javax.inject.Inject
import ca.stefanm.ibus.car.platform.DiscoveredServiceGroups
import ca.stefanm.ibus.gui.debug.windows.ServiceStatusViewer
import kotlinx.coroutines.GlobalScope


@ApplicationScope
class PlatformConfigSetupWindow @Inject constructor(
    private val configurationStorage: ConfigurationStorage,
    private val configurablePlatform: ConfigurablePlatform
): WindowManager.E39Window {

    override val title = "Platform Config Setup"
    override val size: DpSize = WindowManager.DEFAULT_DEBUG_WINDOW_SIZE
    override val tag = this
    override val defaultPosition = WindowManager.E39Window.DefaultPosition.ANYWHERE

    override fun content(): @Composable WindowScope.() -> Unit = {

        Row(Modifier.fillMaxSize()) {
            val leftPaneScrollState = rememberScrollState()
            Box(Modifier.fillMaxWidth(0.45F).wrapContentHeight()) {
                this@Row.PlatformConfigPane()
                VerticalScrollbar(
                    modifier = Modifier.align(Alignment.CenterEnd)
                        .fillMaxHeight(),
                    adapter = rememberScrollbarAdapter(leftPaneScrollState)
                )
            }
            ServiceListViewer()
        }
    }

    @Composable
    private fun RowScope.PlatformConfigPane() {
        Column(Modifier) {
            Text("Platform Service Startup Config",
                fontWeight = FontWeight.Bold, fontSize = 18.sp, modifier = Modifier.padding(vertical = 8.dp)
            )
            NestingCard {
                Text(
                    "Set which platform services should be running " +
                            "when the car platform is started."
                )
                Text(
                    "Disable services that may conflict with other hardware fitted to your " +
                            "IBus car."
                )
                CardSpacer()
                Text(
                    """
                Platform configuration and feature flags is accomplished by enabling and disabling service groups to run at startup. A particular service may belong to one or more service groups.
                
                Set which groups should be enabled at startup, and save the configuration to a configuration file. On startup, e39-rpi will read the configuration file and automatically start selected services.
                
                Only platform groups' startup status may be configured. Disabling a service by itself is not a persistable option.
            """.trimIndent()
                )
            }


            val services = remember { mutableStateMapOf(
                *DiscoveredServiceGroups().getAllGroups().map { it.name to true }.toTypedArray()
            ) }

            fun loadStatusFromFile() {
                val allGroups = DiscoveredServiceGroups().getAllGroups()
                val fileEnabledGroups =
                    configurationStorage
                        .config[E39Config.CarPlatformConfigSpec._listOfServiceGroupsOnStartup]
                        .map { fileGroup -> allGroups.first { it.name == fileGroup } }
                        .associateWith { true }

                services.clear()
                if (fileEnabledGroups.isEmpty()) {
                    services.putAll(
                        allGroups.associate { it.name to true }
                    )
                } else {
                    services.putAll(
                        allGroups.associateWith {
                            fileEnabledGroups.getOrDefault(it, false)
                        }.mapKeys { it.key.name }
                    )
                }
            }

            fun saveStatusToFile() {
                configurationStorage.setServiceGroupsOnStartup(
                    services.filterValues { it }.keys.toList()
                )
            }

            LaunchedEffect(Unit) {
                loadStatusFromFile()
            }

            Row {
                Button(onClick = {
                    saveStatusToFile()
                    loadStatusFromFile()
                }) {
                    Text("Save Config to file")
                }
                Button(onClick = {
                    loadStatusFromFile()
                }) { Text("Reload Config from file") }
            }

            Row {
                Button(onClick = { configurablePlatform.stop() }) { Text("Stop Platform") }
                Button(onClick = {
                    configurablePlatform.run()
                }) { Text("Start Platform") }
            }

            PlatformGroupSelections(
                existingConfig = services,
                onPlatformGroupSelectionChanged = { group, enabled ->
                    services[group.name] = enabled
                }
            )
        }
    }

    @Composable
    private fun CardSpacer() {
        Spacer(Modifier.height(10.dp))
    }

    @Composable
    private fun PlatformGroupSelections(
        existingConfig: SnapshotStateMap<String, Boolean>,
        onPlatformGroupSelectionChanged: (group : DiscoveredPlatformServiceGroup, enabled : Boolean) -> Unit
    ) {
        NestingCard {
            NestingCardHeader("Platform Service Groups")
            DiscoveredServiceGroups().getAllGroups().map {
                Column(Modifier.background(Color.LightGray).fillMaxWidth().wrapContentHeight().padding(10.dp)) {
                    Text(it.name, fontWeight = FontWeight.Bold)
                    Text(it.description)
                    CardSpacer()
                    CheckBoxWithLabel(
                        isChecked = existingConfig.getOrDefault(it.name, false),
                        label = "Run at startup",
                        onCheckChanged = { new ->
                            onPlatformGroupSelectionChanged(it, new)
                        }
                    )
                }
                Spacer(Modifier.height(10.dp))
            }
        }
    }

    @Composable
    private fun ServiceListViewer() {
        ServiceStatusViewer.ScrollableStatusList(
            configurablePlatform.servicesRunning.collectAsState(rememberCoroutineScope().coroutineContext).value
        )
    }
}