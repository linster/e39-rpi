package ca.stefanm.ca.stefanm.ibus.gui.networkSetup.debug.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import ca.stefanm.ca.stefanm.ibus.gui.menu.widgets.screenMenu.SmoothScroll
import ca.stefanm.ca.stefanm.ibus.gui.networkSetup.activateConnection.dbus.GetConnectionListUseCase
import ca.stefanm.ca.stefanm.ibus.gui.networkSetup.activateConnection.screens.debug.FilterConnectionsListForDevicesUseCaseDebugScreen
import ca.stefanm.ca.stefanm.ibus.gui.networkSetup.activateConnection.screens.debug.GetActiveConnectionsUseCaseDebugScreen
import ca.stefanm.ca.stefanm.ibus.gui.networkSetup.activateConnection.screens.debug.GetActiveConnectionsUseCaseDebugScreen.Companion.TAG
import ca.stefanm.ca.stefanm.ibus.gui.networkSetup.activateConnection.screens.debug.GetConnectionListUseCaseDebugScreen
import ca.stefanm.ca.stefanm.ibus.gui.networkSetup.activateConnection.screens.debug.GetConnectionsForApsUseCaseDebugScreen
import ca.stefanm.ca.stefanm.ibus.gui.networkSetup.activateConnection.screens.debug.GetConnectionsUseCaseDebugScreen
import ca.stefanm.ca.stefanm.ibus.gui.networkSetup.activateConnection.screens.debug.GetDevicesUseCaseDebugScreen
import ca.stefanm.ibus.autoDiscover.AutoDiscover
import ca.stefanm.ibus.di.ApplicationModule
import ca.stefanm.ibus.gui.menu.navigator.NavigationNode
import ca.stefanm.ibus.gui.menu.navigator.NavigationNodeTraverser
import ca.stefanm.ibus.gui.menu.navigator.Navigator
import ca.stefanm.ibus.gui.menu.widgets.BmwSingleLineHeader
import ca.stefanm.ibus.gui.menu.widgets.ItemChipOrientation
import ca.stefanm.ibus.gui.menu.widgets.MenuItem
import ca.stefanm.ibus.gui.menu.widgets.knobListener.KnobListenerService
import ca.stefanm.ibus.gui.menu.widgets.themes.ThemeWrapper
import ca.stefanm.ibus.lib.logging.Logger
import javax.inject.Inject
import javax.inject.Named

@AutoDiscover
class NetworkManagerDebugLaunchpad @Inject constructor(
    private val navigationNodeTraverser: NavigationNodeTraverser,
    @Named(ApplicationModule.KNOB_LISTENER_MAIN)
    private val knobListenerService: KnobListenerService,
    private val logger: Logger,
) : NavigationNode<Nothing> {
    companion object {
        const val TAG = "NetworkManagerDebugLaunchpad"
    }
    override val thisClass: Class<out NavigationNode<Nothing>>
        get() = NetworkManagerDebugLaunchpad::class.java

    override fun provideMainContent(): @Composable ((Navigator.IncomingResult?) -> Unit) = {
        Column(Modifier
            .background(ThemeWrapper.ThemeHandle.current.colors.menuBackground)
            .fillMaxSize()
        ) {
            BmwSingleLineHeader("Debug: $TAG")

            SmoothScroll.SmoothScroll(
                modifier = Modifier,
                knobListenerService = knobListenerService,
                tag = TAG,
                logger = logger,
                prependGoBackEntry = true,
                navigationNodeTraverser = navigationNodeTraverser,
                items = listOf(
                    { allocatedIndex, currentIndex ->
                        MenuItem(
                            label = "GetActiveConnectionsUseCase Debug Screen",
                            chipOrientation = ItemChipOrientation.W,
                            isSelected = allocatedIndex == currentIndex,
                            onClicked = CallWhen(currentIndexIs = allocatedIndex) {
                                navigationNodeTraverser.navigateToNode(
                                    GetActiveConnectionsUseCaseDebugScreen::class.java
                                )
                            }
                        )
                    },
                    { allocatedIndex, currentIndex ->
                        MenuItem(
                            label = "GetConnectionsUeCase Debug Screen",
                            chipOrientation = ItemChipOrientation.W,
                            isSelected = allocatedIndex == currentIndex,
                            onClicked = CallWhen(currentIndexIs = allocatedIndex) {
                                navigationNodeTraverser.navigateToNode(
                                    GetConnectionsUseCaseDebugScreen::class.java
                                )
                            }
                        )
                    },
                    { allocatedIndex, currentIndex ->
                        MenuItem(
                            label = "GetDevicesUseCase Debug Screen",
                            chipOrientation = ItemChipOrientation.W,
                            isSelected = allocatedIndex == currentIndex,
                            onClicked = CallWhen(currentIndexIs = allocatedIndex) {
                                navigationNodeTraverser.navigateToNode(
                                    GetDevicesUseCaseDebugScreen::class.java
                                )
                            }
                        )
                    },
                    { allocatedIndex, currentIndex ->
                        MenuItem(
                            label = "Dummy Connection List",
                            chipOrientation = ItemChipOrientation.W,
                            isSelected = allocatedIndex == currentIndex,
                            onClicked = CallWhen(currentIndexIs = allocatedIndex) {
                                navigationNodeTraverser.navigateToNode(
                                    DummyConnectionListScreen::class.java
                                )
                            }
                        )
                    },
                    { allocatedIndex, currentIndex ->
                        MenuItem(
                            label = "GetConnectionsListUseCase Debug Screen",
                            chipOrientation = ItemChipOrientation.W,
                            isSelected = allocatedIndex == currentIndex,
                            onClicked = CallWhen(currentIndexIs = allocatedIndex) {
                                navigationNodeTraverser.navigateToNode(
                                    GetConnectionListUseCaseDebugScreen::class.java
                                )
                            }
                        )
                    },
                    { allocatedIndex, currentIndex ->
                        MenuItem(
                            label = "GetConnectionsForApsUseCase Debug Screen",
                            chipOrientation = ItemChipOrientation.W,
                            isSelected = allocatedIndex == currentIndex,
                            onClicked = CallWhen(currentIndexIs = allocatedIndex) {
                                navigationNodeTraverser.navigateToNode(
                                    GetConnectionsForApsUseCaseDebugScreen::class.java
                                )
                            }
                        )
                    },
                    { allocatedIndex, currentIndex ->
                        MenuItem(
                            label = "FilterConnectionsListForDevicesUseCaseDebugScreen Debug Screen",
                            chipOrientation = ItemChipOrientation.W,
                            isSelected = allocatedIndex == currentIndex,
                            onClicked = CallWhen(currentIndexIs = allocatedIndex) {
                                navigationNodeTraverser.navigateToNode(
                                    FilterConnectionsListForDevicesUseCaseDebugScreen::class.java
                                )
                            }
                        )
                    },
                )
            )
        }

    }
}