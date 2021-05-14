package ca.stefanm.ibus.gui.menu.navigator

import androidx.compose.runtime.Composable
import ca.stefanm.ibus.di.ApplicationScope
import ca.stefanm.ibus.gui.menu.MainMenu
import ca.stefanm.ibus.gui.menu.bluetoothPairing.BluetoothPairingMenu
import ca.stefanm.ibus.gui.menu.navigator.NavigationModule.Companion.ALL_NODES
import ca.stefanm.ibus.gui.menu.navigator.NavigationModule.Companion.ROOT_NODE
import ca.stefanm.ibus.lib.logging.Logger
import dagger.Module
import dagger.Provides
import dagger.multibindings.ElementsIntoSet
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.util.*
import javax.inject.Inject
import javax.inject.Named
import javax.inject.Provider


@Module
class NavigationModule {
    companion object {
        const val ALL_NODES = "all_nodes"
        const val ROOT_NODE = "root_node"
    }

    @Provides
    @Named(ROOT_NODE)
    @JvmSuppressWildcards(suppress = false)
    fun provideRootNode(
        mainMenu: MainMenu
    ) : NavigationNode = mainMenu

    @Provides
    @ElementsIntoSet
    @Named(ALL_NODES)
    fun provideAllNodes(
        mainMenu: MainMenu,
        bluetoothPairingMenu: BluetoothPairingMenu
    ) : Set<NavigationNode> = setOf(
        mainMenu,
        bluetoothPairingMenu
    )

}

@ApplicationScope
class Navigator @Inject constructor(
    @Named(ROOT_NODE) private val rootNode : NavigationNode
) {

    private val _currentNode = MutableStateFlow(rootNode)
    val currentNode : StateFlow<NavigationNode>
        get() = _currentNode

    val backStack : Stack<NavigationNode> = Stack<NavigationNode>()
    init {
        backStack.push(rootNode)
    }

    fun navigateToNode(newNode : NavigationNode) {
        backStack.push(currentNode.value)
        _currentNode.value = newNode
    }

    fun goBack() {
        _currentNode.value = backStack.pop()
        if (backStack.empty()) {
            backStack.push(rootNode)
        }
    }
}

interface NavigationNode {
    val thisClass : Class<out NavigationNode>
    fun provideMainContent() : @Composable () -> Unit
}

class NavigationNodeTraverser @Inject constructor(
    private val navigator: Provider<Navigator>,
    @Named(ALL_NODES) private val allNodes : Provider<Set<NavigationNode>>,
    private val logger: Logger
) {
    fun navigateToNode(node : Class<out NavigationNode>) {
        val newNode = allNodes.get().find { it.thisClass == node }
        if (newNode == null) {
            logger.e("NAVIGATOR", "No new node found")
            return
        }
        navigator.get().navigateToNode(newNode)
    }
    fun goBack() {
        navigator.get().goBack()
    }

    suspend fun keyboardInput() {
        //TODO find that twitter post about s
        //https://www.reddit.com/r/androiddev/comments/898j9j/kotlin_coroutines_to_show_alert_dialog_yeah_why/


    }

}

