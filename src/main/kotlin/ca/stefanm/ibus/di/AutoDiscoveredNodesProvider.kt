package ca.stefanm.ibus.di

import ca.stefanm.ibus.gui.menu.navigator.NavigationNode

interface AutoDiscoveredNodesProviderFoo {
    fun getAllNodes(): Set<NavigationNode<*>>
}