package ca.stefanm.ibus.di

import ca.stefanm.ibus.gui.menu.navigator.NavigationNode
import javax.inject.Inject
import javax.inject.Provider
import javax.inject.Singleton

@ApplicationScope
class AutoDiscoveredNodesHolder @Inject constructor() {
    val autoDiscoveredNodes = mutableSetOf<NavigationNode<*>>()
}