package ca.stefanm.ibus.gui.debug.windows

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Column
import androidx.compose.material.Button
import androidx.compose.material.DropdownMenu
import androidx.compose.material.DropdownMenuItem
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.WindowScope
import androidx.compose.ui.window.WindowSize
import ca.stefanm.ibus.di.AutoDiscoveredNodesRegistry
import ca.stefanm.ibus.gui.menu.BMWMainMenu
import ca.stefanm.ibus.gui.menu.ComposeDebugMenu
import ca.stefanm.ibus.gui.menu.navigator.*
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Named
import javax.inject.Provider

class HmiNavigatorDebugWindow @Inject constructor(
    private val navigator: Provider<Navigator>,
    private val navigationNodeTraverser: Provider<NavigationNodeTraverser>,
    @Named(NavigationModule.ALL_NODES) private val allNodes : Provider<Set<NavigationNode<*>>>,
) : WindowManager.E39Window {

    override val tag: Any
        get() = this

    override val defaultPosition: WindowManager.E39Window.DefaultPosition
        get() = WindowManager.E39Window.DefaultPosition.ANYWHERE

    override val size = WindowSize(600.dp, 1200.dp)
    override val title = "HMI Navigator Debug"

    override fun content(): @Composable WindowScope.() -> Unit = {
        //We want info on the current backstack record
        //What's in the backstack
        //Which nodes are registered with the navigator, and which are missing
        //plus, the ability to navigate to an arbitrary node (no parameters)
        //

        NestingCard {
            NestingCardHeader("Back stack")
            NestingCard {
                NestingCardHeader("Registered Node")
                NavigateToNode()
                Button(
                    onClick = { navigationNodeTraverser.get().goBack() }
                ) { Text("Go Back")}
                Button(
                    onClick = { navigationNodeTraverser.get().navigateToRoot() }
                ) { Text("Go Root")}
                Button(
                    onClick = {
                        //This is here so we can navigate to ourselves
                        //so if we need to add the current screen as a back
                        //stack record, we can. (NavigateToNode() might not
                        //simulate backstack changes as a real action does)

                        val (node, incomingResult) = navigator.get()
                            .mainContentScreen.value.let { it.node to it.incomingResult }

                        if (incomingResult?.requestParameters != null) {
                            navigator.get().navigateToNodeWithParameters(node, incomingResult.requestParameters)
                        } else {
                            navigator.get().navigateToNode(node)
                        }
                    }
                ) { Text("Navigate to Self (register on back stack)")}
                NestingCard {
                    NestingCardHeader("Hot Keys")
                    ScreenHotKey(ComposeDebugMenu::class.java as Class<NavigationNode<*>>)
                    ScreenHotKey(BMWMainMenu::class.java as Class<NavigationNode<*>>)
                }
            }

            NestingCard {
                BackStackViewer()
            }
            NestingCard {
                CurrentBackstackEntry()
            }


            NestingCard {
                UnregisteredNodes()
            }
        }
    }


    @Composable
    private fun CurrentBackstackEntry() {
        NestingCardHeader("Current Entry")
        val currentBackStackRecord = navigator.get().mainContentScreen.collectAsState()
        Text("Current Class: ${currentBackStackRecord.value.node.thisClass.canonicalName}")

        NestingCard {
            NestingCardHeader("Incoming Result")
            currentBackStackRecord.value.incomingResult?.let { result ->
                Text("Request parameters: ${result.requestParameters.toString()}")
                Text("Result From: ${result.resultFrom?.canonicalName}")
                Text("Result: ${result.result.toString()}")
            }
        }
    }

    @Composable
    private fun NavigateToNode() {
        val isMenuOpen = remember { mutableStateOf(false) }
        Button(
            onClick = { isMenuOpen.value = true }
        ) {
            Text("Navigate to Registered Node V")
            DropdownMenu(
                expanded = isMenuOpen.value,
                onDismissRequest = { isMenuOpen.value = false }
            ) {
                allNodes.get().forEach {
                    DropdownMenuItem(
                        onClick = {
                            navigationNodeTraverser.get().navigateToNode(it.thisClass)
                            isMenuOpen.value = false
                        }
                    ) { Text(it.thisClass.canonicalName) }
                }
            }
        }
    }

    @Composable
    private fun ScreenHotKey(screen : Class<NavigationNode<*>>) {
        //Utility button to jump to a favorite screen
        Button(onClick = { navigationNodeTraverser.get().navigateToNode(screen) }
        ) { Text("Screen HotKey: ${screen.simpleName}")}
    }

    @Composable
    private fun UnregisteredNodes() {
        //List all nodes with AutoDiscover, but aren't manually put
        //into the allNodes list.
        NestingCardHeader("Unregistered Nodes")

        val registeredNodes = allNodes.get().map { it.thisClass }.toSet()
        val discoveredNodes = AutoDiscoveredNodesRegistry().getAllDiscoveredNodeClasses()

        val nonRegisteredNodes = registeredNodes - discoveredNodes

        if (nonRegisteredNodes.isEmpty()) {
            Text(
                "All nodes marked with @AutoDiscover are registered in Navigation DI Module",color = Color.Green)
        } else {
            Text("Nodes not registered in Navigation DI Module!!", color = Color.Red)
            nonRegisteredNodes.forEach {
                Text("Node: ${it.canonicalName}")
            }
        }
    }

    @Composable
    private fun BackStackViewer() {

        val currentScreen = remember { mutableStateOf<Navigator.BackStackRecord<*>?>(null) }
        val backStack : SnapshotStateList<Navigator.BackStackRecord<*>> = remember {
            mutableStateListOf()
        }

        rememberCoroutineScope().let {
            it.launch {
                navigator.get().getDebugClient().mutableMainContentScreen
                    .onEach {
                        backStack.clear()
                        backStack.addAll(navigator.get().getDebugClient().backStack)
                    }.collect { current ->
                        currentScreen.value = current
                    }
            }
        }

        NestingCardHeader("Stack Contents")
        Column {
            for (record in backStack) {
                Column(Modifier
                    .border(2.dp, Color.Black)
                    .then(
                        if (record == currentScreen.value) {
                            Modifier.background(Color.Green)
                        } else { Modifier }
                    )
                ) {
                    Text("Class: ${record.node.thisClass.simpleName}")
                    Text("Incoming Result: ${record.incomingResult.toString()}")
                }
            }
        }

        NestingCardHeader("Current")
        Column(Modifier
            .border(2.dp, Color.Black)
            .background(Color.Green)
        ) {
            Text("Class: ${currentScreen.value?.node?.thisClass?.simpleName}")
            Text("Incoming Result: ${currentScreen.value?.incomingResult.toString()}")
        }
    }
}