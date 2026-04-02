package ca.stefanm.ibus.gui.debug.hmiScreens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import ca.stefanm.ca.stefanm.ibus.gui.menu.widgets.screenMenu.SmoothScroll
import ca.stefanm.ca.stefanm.ibus.gui.networkSetup.ui.connectionList.ConnectionListItems
import ca.stefanm.ibus.autoDiscover.AutoDiscover
import ca.stefanm.ibus.di.ApplicationComponent
import ca.stefanm.ibus.di.ApplicationModule
import ca.stefanm.ibus.gui.menu.navigator.NavigationNode
import ca.stefanm.ibus.gui.menu.navigator.NavigationNodeTraverser
import ca.stefanm.ibus.gui.menu.navigator.Navigator
import ca.stefanm.ibus.gui.menu.widgets.BmwSingleLineHeader
import ca.stefanm.ibus.gui.menu.widgets.ItemChipOrientation
import ca.stefanm.ibus.gui.menu.widgets.MenuItem
import ca.stefanm.ibus.gui.menu.widgets.knobListener.KnobListenerService
import ca.stefanm.ibus.gui.menu.widgets.knobListener.dynamic.KnobObserverBuilder
import ca.stefanm.ibus.gui.menu.widgets.knobListener.dynamic.KnobObserverBuilderState.Companion.setupListener
import ca.stefanm.ibus.gui.menu.widgets.screenMenu.MenuItem
import ca.stefanm.ibus.gui.menu.widgets.screenMenu.ScrollMenu
import ca.stefanm.ibus.gui.menu.widgets.screenMenu.TextMenuItem
import ca.stefanm.ibus.gui.menu.widgets.themes.ThemeWrapper
import ca.stefanm.ibus.lib.logging.Logger
import javax.inject.Inject
import javax.inject.Named

@AutoDiscover
class SmoothScrollTest @Inject constructor(
    private val navigationNodeTraverser: NavigationNodeTraverser,
    @Named(ApplicationModule.KNOB_LISTENER_MAIN)
    private val knobListenerService: KnobListenerService,
    private val logger: Logger
) : NavigationNode<Nothing> {

    override val thisClass: Class<out NavigationNode<Nothing>>
        get() = SmoothScrollTest::class.java

    override fun provideMainContent(): @Composable (incomingResult: Navigator.IncomingResult?) -> Unit = {

        Column(Modifier
            .background(ThemeWrapper.ThemeHandle.current.colors.menuBackground)
            .fillMaxSize()
        ) {
            BmwSingleLineHeader("Smooth Scroll Test")

            SmoothScroll.SmoothScroll(
                modifier = Modifier,
                knobListenerService,
                "Smooth Scroll Test",
                logger = logger,
                items = listOf(
                    { allocatedIndex, currentIndex ->
                        MenuItem(
                            label = "Go Back",
                            chipOrientation = ItemChipOrientation.W,
                            isSelected = allocatedIndex == currentIndex,
                            onClicked = CallWhen(currentIndexIs = allocatedIndex) {
                                navigationNodeTraverser.goBack()
                            }
                        )
                    },
                    { _, _ ->
                        ConnectionListItems.ConnectionListDivider(
                            dividerHeader = "Wired",
                            modifier = Modifier,
                        )
                    },
                    { allocatedIndex, currentIndex ->
                        ConnectionListItems.Connection(
                            connectionName = "Wired Connection 1",
                            modifier = Modifier,
                            chipOrientation = ItemChipOrientation.W,
                            isSelected = allocatedIndex == currentIndex,
                            onClicked = CallWhen(currentIndexIs = allocatedIndex) {

                            }
                        )
                    },
                    { allocatedIndex, currentIndex ->
                        ConnectionListItems.Connection(
                            connectionName = "Wired Connection 2",
                            modifier = Modifier,
                            chipOrientation = ItemChipOrientation.W,
                            isSelected = allocatedIndex == currentIndex,
                            onClicked = CallWhen(currentIndexIs = allocatedIndex) {

                            }
                        )
                    },
                    { _, _ ->
                        ConnectionListItems.ConnectionListDivider(
                            dividerHeader = "Wi-Fi",
                            modifier = Modifier,
                        )
                    },
                    { allocatedIndex, currentIndex ->
                        ConnectionListItems.Connection(
                            connectionName = "Smartynk-5G",
                            isConnected = true,
                            strength = 100,
                            modifier = Modifier,
                            chipOrientation = ItemChipOrientation.W,
                            isSelected = allocatedIndex == currentIndex,
                            onClicked = CallWhen(currentIndexIs = allocatedIndex) {

                            }
                        )
                    },
                    { allocatedIndex, currentIndex ->
                        ConnectionListItems.Connection(
                            connectionName = "RogersIgnite404",
                            strength = 75,
                            modifier = Modifier,
                            chipOrientation = ItemChipOrientation.W,
                            isSelected = allocatedIndex == currentIndex,
                            onClicked = CallWhen(currentIndexIs = allocatedIndex) {

                            }
                        )
                    },
                    { allocatedIndex, currentIndex ->
                        ConnectionListItems.Connection(
                            connectionName = "Kathy Network",
                            strength = 60,
                            modifier = Modifier,
                            chipOrientation = ItemChipOrientation.W,
                            isSelected = allocatedIndex == currentIndex,
                            onClicked = CallWhen(currentIndexIs = allocatedIndex) {

                            }
                        )
                    },
                    { allocatedIndex, currentIndex ->
                        ConnectionListItems.Connection(
                            connectionName = "grantnett5g",
                            strength = 6,
                            modifier = Modifier,
                            chipOrientation = ItemChipOrientation.W,
                            isSelected = allocatedIndex == currentIndex,
                            onClicked = CallWhen(currentIndexIs = allocatedIndex) {

                            }
                        )
                    },
                    { _, _ ->
                        ConnectionListItems.ConnectionListDivider(
                            dividerHeader = "Bridge (docker0)",
                            modifier = Modifier,
                        )
                    },
                    { allocatedIndex, currentIndex ->
                        ConnectionListItems.Connection(
                            connectionName = "docker0",
                            isConnected = true,
                            modifier = Modifier,
                            chipOrientation = ItemChipOrientation.W,
                            isSelected = allocatedIndex == currentIndex,
                            onClicked = CallWhen(currentIndexIs = allocatedIndex) {

                            }
                        )
                    },
                    { _, _ ->
                        ConnectionListItems.ConnectionListDivider(
                            dividerHeader = "Bluetooth",
                            modifier = Modifier,
                        )
                    },
                    { allocatedIndex, currentIndex ->
                        ConnectionListItems.Connection(
                            connectionName = "Stefan iPhone Network",
                            modifier = Modifier,
                            chipOrientation = ItemChipOrientation.W,
                            isSelected = allocatedIndex == currentIndex,
                            onClicked = CallWhen(currentIndexIs = allocatedIndex) {

                            }
                        )
                    }
                )
            )




        }


    }
}