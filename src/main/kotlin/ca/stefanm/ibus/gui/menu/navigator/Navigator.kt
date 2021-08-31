package ca.stefanm.ibus.gui.menu.navigator

import androidx.compose.runtime.*
import ca.stefanm.ibus.di.ApplicationScope
import ca.stefanm.ibus.gui.debug.hmiScreens.*
import ca.stefanm.ibus.gui.menu.BMWMainMenu
import ca.stefanm.ibus.gui.menu.EmptyMenu
import ca.stefanm.ibus.gui.menu.ComposeDebugMenu
import ca.stefanm.ibus.gui.menu.bluetoothPairing.BluetoothPairingMenu
import ca.stefanm.ibus.gui.menu.navigator.NavigationModule.Companion.ALL_NODES
import ca.stefanm.ibus.gui.menu.navigator.NavigationModule.Companion.ROOT_NODE
import ca.stefanm.ibus.lib.logging.Logger
import dagger.Module
import dagger.Provides
import dagger.multibindings.ElementsIntoSet
import kotlinx.coroutines.flow.*
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

//    @Provides
//    @ElementsIntoSet
//    @Named(ALL_NODES)
//    fun provideAllNodes(
//        composeDebugMenu: ComposeDebugMenu,
//        bluetoothPairingMenu: BluetoothPairingMenu,
//        debugHmiRoot: DebugHmiRoot,
//        debugHmiMenuTestTwoColumn: DebugHmiMenuTestTwoColumn,
//        debugHmiMenuTestOneColumn: DebugHmiMenuTestOneColumn,
//        debugHmiKeyboard: DebugHmiKeyboard,
//        emptyMenu: EmptyMenu,
//        bmwMainMenu: BMWMainMenu
//    ) : Set<NavigationNode<*>> = setOf<NavigationNode<*>>(
//        composeDebugMenu,
//        bluetoothPairingMenu,
//        debugHmiRoot,
//        debugHmiMenuTestTwoColumn,
//        debugHmiMenuTestOneColumn,
//        debugHmiKeyboard,
//        emptyMenu,
//        bmwMainMenu
//    )

}

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

        val displayedNodeWithResult = backStack.removeLast()

        backStack.last().incomingResult = IncomingResult(
            resultFrom = displayedNodeWithResult.node.thisClass,
            result = result,
            requestParameters = null
        )

        _mainContentScreen.value = backStack.last()
    }
}

interface NavigationNode<Result> {
    val thisClass : Class<out NavigationNode<Result>>
    fun provideMainContent() : @Composable (incomingResult : Navigator.IncomingResult?) -> Unit
}

class NavigationNodeTraverser @Inject constructor(
    private val navigator: Provider<Navigator>,

    //TODO
    //TODO what we could do here is instead of depending on a provider of a set
    //TODO that dagger builds, we could depend on a class that contains a set
    //TODO ...and that class has a get() method that returns the set we're querying.
    //TODO That way, the code we generate from kotlin poet doesn't need dagger junk
    //TODO in it, and we then don't need two annotation passes (unnsuported by KAPT,
    //TODO ... dunno if KSP and KAPT play nice together. Does dagger work on KSP?)

    @Named("all_nodes") private val allNodes : Provider<Set<NavigationNode<*>>>,
    private val logger: Logger
) {

    private fun findNode(node : Class<out NavigationNode<*>>) : NavigationNode<*>? {
        val newNode = allNodes.get().find { it.thisClass == node }
        if (newNode == null) {
            logger.e("NAVIGATOR", "No new node found")
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

    fun goBack() {
        navigator.get().goBack()
    }
}
