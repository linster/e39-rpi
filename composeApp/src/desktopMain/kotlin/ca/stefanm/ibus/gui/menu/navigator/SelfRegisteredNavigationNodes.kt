package ca.stefanm.ca.stefanm.ibus.gui.menu.navigator

import ca.stefanm.ibus.di.ApplicationScope
import javax.inject.Inject

@ApplicationScope
class SelfRegisteredNavigationNodesHolder @Inject constructor() {
    private val _registeredNodes = mutableSetOf<NavigationNode<*>>()

    val registeredNodes : Set<NavigationNode<*>>
        get() = _registeredNodes

    fun registerNode(node : NavigationNode<*>) {
        _registeredNodes.add(node)
    }

    fun checkAllNodesRegistered(
        onMissingNode : (name : String) -> Unit = {}
    ) {

        val registeredClasses = _registeredNodes.map { it.thisClass }.toSet()

        //val foundClasses = setOf<>()
    }
}