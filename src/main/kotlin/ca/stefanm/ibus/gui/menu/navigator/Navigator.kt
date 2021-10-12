package ca.stefanm.ibus.gui.menu.navigator

import androidx.compose.runtime.*
import ca.stefanm.ibus.di.*
import ca.stefanm.ibus.gui.debug.hmiScreens.*
import ca.stefanm.ibus.gui.menu.BMWMainMenu
import ca.stefanm.ibus.gui.menu.EmptyMenu
import ca.stefanm.ibus.gui.menu.ComposeDebugMenu
import ca.stefanm.ibus.gui.bluetoothPairing.BluetoothPairingMenu
import ca.stefanm.ibus.gui.bluetoothPairing.ui.*
import ca.stefanm.ibus.gui.map.MapScreen
import ca.stefanm.ibus.gui.map.settings.MapTileDownloaderScreen
import ca.stefanm.ibus.gui.menu.SettingsRootMenu
import ca.stefanm.ibus.gui.menu.navigator.NavigationModule.Companion.ALL_NODES
import ca.stefanm.ibus.gui.menu.navigator.NavigationModule.Companion.ROOT_NODE
import ca.stefanm.ibus.lib.logging.Logger
import dagger.Module
import dagger.Provides
import dagger.multibindings.ElementsIntoSet
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.withContext
import kotlinx.coroutines.yield
import javax.inject.Inject
import javax.inject.Named
import javax.inject.Provider
import kotlin.collections.ArrayDeque


@Module
class NavigationModule {
    companion object {
        const val ALL_NODES = "all_nodes"
        const val ROOT_NODE = "root_node"
    }

    @Provides
    @Named(value = ROOT_NODE)
    @JvmSuppressWildcards(suppress = false)
    fun provideRootNode(
//        mainMenu: MainMenu
//        debugHmiRoot: DebugHmiRoot
        emptyMenu: EmptyMenu
    ) : NavigationNode<*> = emptyMenu

    @Provides
    @ElementsIntoSet
    @Named(ALL_NODES)
    fun provideAllNodes(
        bluetoothpairingmenu: BluetoothPairingMenu,
        bluetoothPinConfirmationScreen: BluetoothPinConfirmationScreen,
        btEmptyMenu: BtEmptyMenu,
        currentDeviceChooser: CurrentDeviceChooser,
        mainBtMenu: MainBtMenu,
        pairableDeviceChooser: PairableDeviceChooser,
        currentDeviceViewer: CurrentDeviceViewer,
        emptymenu: EmptyMenu,
        bmwmainmenu: BMWMainMenu,
        settingsRootMenu: SettingsRootMenu,
        mapTileDownloaderScreen: MapTileDownloaderScreen,
        composedebugmenu: ComposeDebugMenu,
        debughmimenutest: DebugHmiMenuTest,
        debughmikeyboard: DebugHmiKeyboard,
        debughmimenutesttwocolumn: DebugHmiMenuTestTwoColumn,
        debughmimenutestonecolumn: DebugHmiMenuTestOneColumn,
        debughmiroot: DebugHmiRoot,
        optionPromptTest: OptionPromptTest,
        mapScreen: MapScreen
    ) : Set<NavigationNode<*>> = setOf(
        bluetoothpairingmenu,
        bluetoothPinConfirmationScreen,
        pairableDeviceChooser,
        currentDeviceViewer,
        btEmptyMenu,
        currentDeviceChooser,
        settingsRootMenu,
        mapTileDownloaderScreen,
        mainBtMenu,
        emptymenu,
        bmwmainmenu,
        composedebugmenu,
        debughmimenutest,
        debughmikeyboard,
        debughmimenutesttwocolumn,
        debughmimenutestonecolumn,
        debughmiroot,
        optionPromptTest,
        mapScreen
    )
}

//@Module
//abstract class NavigationBindsModule {
//    @Binds
//    abstract fun bindAutoProvider(
//        provider: AutoDiscoveredNodesProviderImpl
//    ) : AutoDiscoveredNodesProvider
//}



@Stable
@ApplicationScope
class Navigator @Inject constructor(
    @Named(ROOT_NODE) private val rootNode : NavigationNode<*>,
    private val logger: Logger
) {

    data class BackStackRecord<R>(
        val node : NavigationNode<R>,
        var incomingResult : IncomingResult?
    )

    data class IncomingResult(
        val requestParameters : Any?,
        val resultFrom : Class<out NavigationNode<*>>?,
        val result : Any?
    )

    private val _mainContentScreen = MutableStateFlow(BackStackRecord(rootNode, null))
    val mainContentScreen : StateFlow<BackStackRecord<*>>
        get() = _mainContentScreen

    private val backStack : ArrayDeque<BackStackRecord<*>> = ArrayDeque()
    init {
        backStack.addLast(BackStackRecord(rootNode, null))
    }

    fun navigateToNode(newNode : NavigationNode<*>) {
        backStack.addLast(mainContentScreen.value)
        _mainContentScreen.value = BackStackRecord(newNode, null)
    }

    fun navigateToNodeWithParameters(newNode: NavigationNode<*>, parameters : Any) {
        //Think of this as an Intent.putExtra()
        //This could be something like a prompt...

        //TODO put an entry on top of the back stack, and set it's incomingResult before displaying it.
        backStack.addLast(mainContentScreen.value)
        _mainContentScreen.value = BackStackRecord(newNode, IncomingResult(
            requestParameters = parameters, //Intent.putExtra()
            resultFrom = null, //Use instead of requestCode
            result = null
        ))
    }

    fun navigateToRoot() {
        backStack.clear()
        backStack.addLast(BackStackRecord(rootNode, null))
        _mainContentScreen.value = backStack.last()
    }

    fun goBack() {
        _mainContentScreen.value = backStack.removeLast()
        if (backStack.isEmpty()) {
            backStack.addLast(BackStackRecord(rootNode, null))
        }
    }

    fun <R> setResultForNodeAndGoBack(node: NavigationNode<R>, result : R) {
        //We're setting a result for the currently displayed node
        //TODO First assert that node is what's on top of the stack.
        //TODO Then, take the second item on the stack, and set result on it.
        //TODO also check that the node we're modifiyng isn't the root node.

        if (node != mainContentScreen.value.node) {
            logger.w("Navigator", "WARNING: Trying to navigate back from " +
                    "not the top node! Node: $node, ${mainContentScreen.value.node}")
        }

        backStack.last().incomingResult = IncomingResult(
            resultFrom = _mainContentScreen.value.node.thisClass,
            result = result,
            requestParameters = null
        )

        _mainContentScreen.value = backStack.removeLast() //TODO removeLast()?
    }

    fun <R> cleanupBackStackDescendentsOf(node: Class<out NavigationNode<R>>) {
        val indexNewTail = backStack
            .firstOrNull { it.node.thisClass == node }
            .let { backStack.indexOf(it) }

        val indicesToDelete = (indexNewTail+ 1 .. backStack.lastIndex)

        indicesToDelete.reversed().forEach { backStack.removeAt(it) }
        _mainContentScreen.value = backStack.last()
    }

    suspend fun <R> navigateToNodeAndAwaitResult(node: NavigationNode<R>, parameters: Any?) : R {
        val oldScreen = _mainContentScreen.value
        if (parameters == null) {
            navigateToNode(node)
        } else {
            navigateToNodeWithParameters(node, parameters)
        }

        //Wait while the user is doing stuff
        while (_mainContentScreen.value.node == node) {
            yield()
        }

        if (oldScreen.node.thisClass != _mainContentScreen.value.node.thisClass
            && _mainContentScreen.value.incomingResult?.resultFrom != node.thisClass) {
            error("User navigated away instead of returning a result. Incoming result: ${_mainContentScreen.value.incomingResult}")
        }

        return _mainContentScreen.value.incomingResult?.result as R ?: error("Result not expected! Got ")

    }

    //This is an interface returned to the HMINavigatorDebugWindow.
    //None of the objects in this interface should ever be modified
    //in normal operation.
    interface NavigatorDebugClient {
        //Don't change this value unless you know what you're doing.
        val mutableMainContentScreen : MutableStateFlow<BackStackRecord<*>>

        val backStack : ArrayDeque<BackStackRecord<*>>
    }

    fun getDebugClient() : NavigatorDebugClient {
        return object : NavigatorDebugClient {
            override val mutableMainContentScreen: MutableStateFlow<BackStackRecord<*>>
                get() = _mainContentScreen
            override val backStack: ArrayDeque<BackStackRecord<*>>
                get() = this@Navigator.backStack
        }
    }
}

interface NavigationNode<Result> {
    val thisClass : Class<out NavigationNode<Result>>
    fun provideMainContent() : @Composable (incomingResult : Navigator.IncomingResult?) -> Unit
}

class NavigationNodeTraverser @Inject constructor(
    private val navigator: Provider<Navigator>,
    @Named(ALL_NODES) private val allNodes : Provider<Set<NavigationNode<*>>>,
    private val logger: Logger
) {

    private fun findNode(node : Class<out NavigationNode<*>>) : NavigationNode<*>? {
        val newNode = allNodes.get().find { it.thisClass == node }
        if (newNode == null) {
            logger.e("NAVIGATOR", "No new node found. Requested ${node.toGenericString()}")
            //selfRegisteredNavigationNodesHolder.checkAllNodesRegistered()
            return null
        }
        return newNode
    }

    fun navigateToNode(node : Class<out NavigationNode<*>>) {
        findNode(node)?.let { newNode ->
            navigator.get().navigateToNode(newNode)
        }
    }

    fun navigateToNodeWithParameters(node : Class<out NavigationNode<*>>, parameters: Any) {
        findNode(node)?.let { newNode ->
            navigator.get().navigateToNodeWithParameters(newNode, parameters)
        }
    }

    fun <R> setResultAndGoBack(node : NavigationNode<R>, result : R) {
        //Nothing really preventing a bad child from getting a copy of ALL_NODES
        //and setting results on random things.
        navigator.get().setResultForNodeAndGoBack(node, result)
    }

    fun navigateToRoot() {
        navigator.get().navigateToRoot()
    }

    fun goBack() {
        navigator.get().goBack()
    }

    //Remove all items in the backstack below the most recent one that
    //has the given class. This can be used to implement a sort of task
    //affinity scheme.
    fun <R> cleanupBackStackDescendentsOf(node: Class<out NavigationNode<R>>) {
        navigator.get().cleanupBackStackDescendentsOf(node)
    }

    suspend fun <R> navigateToNodeAndAwaitResult(node: Class<out NavigationNode<*>>, parameters: Any?) : R {
        return findNode(node)!!.let {
            withContext(Dispatchers.Main) {
                navigator.get().navigateToNodeAndAwaitResult(it, parameters) as R
            }
        }
    }
}

