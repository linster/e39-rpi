package ca.stefanm.ibus.gui.menu.navigator

import androidx.compose.runtime.*
import ca.stefanm.ibus.di.ApplicationScope
import ca.stefanm.ibus.gui.menu.MainMenu
import ca.stefanm.ibus.gui.menu.bluetoothPairing.BluetoothPairingMenu
import ca.stefanm.ibus.gui.menu.navigator.NavigationModule.Companion.ALL_NODES
import ca.stefanm.ibus.gui.menu.navigator.NavigationModule.Companion.ROOT_NODE
import ca.stefanm.ibus.gui.picker.TextEntry
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
    @Named(ROOT_NODE)
    @JvmSuppressWildcards(suppress = false)
    fun provideRootNode(
        mainMenu: MainMenu
    ) : NavigationNode<*> = mainMenu

    @Provides
    @ElementsIntoSet
    @Named(ALL_NODES)
    fun provideAllNodes(
        mainMenu: MainMenu,
        bluetoothPairingMenu: BluetoothPairingMenu,
    ) : Set<NavigationNode<*>> = setOf<NavigationNode<*>>(
        mainMenu,
        bluetoothPairingMenu,
    )

}

@ApplicationScope
class Navigator @Inject constructor(
    @Named(ROOT_NODE) private val rootNode : NavigationNode<*>,
    private val textEntry: Provider<TextEntry>
) {

    private val _mainContentScreen = MutableStateFlow(rootNode)
    val mainContentScreen : StateFlow<NavigationNode<*>>
        get() = _mainContentScreen

    private val backStack : ArrayDeque<NavigationNode<*>> = ArrayDeque()
    init {
        backStack.addLast(rootNode)
    }

    fun navigateToNode(newNode : NavigationNode<*>) {
        backStack.addLast(mainContentScreen.value)
        _mainContentScreen.value = newNode
    }

    fun goBack() {
        _mainContentScreen.value = backStack.removeLast()
        if (backStack.isEmpty()) {
            backStack.addLast(rootNode)
        }
    }
}

interface NavigationNode<R> {
    val thisClass : Class<out NavigationNode<R>>
    fun provideMainContent() : @Composable () -> Unit
}

class NavigationNodeTraverser @Inject constructor(
    private val navigator: Provider<Navigator>,
    @Named(ALL_NODES) private val allNodes : Provider<Set<NavigationNode<*>>>,
    private val logger: Logger
) {
    fun navigateToNode(node : Class<out NavigationNode<*>>) {
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
}

