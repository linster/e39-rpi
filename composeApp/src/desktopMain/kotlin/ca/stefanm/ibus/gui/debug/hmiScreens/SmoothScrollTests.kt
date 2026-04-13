package ca.stefanm.ibus.gui.debug.hmiScreens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import ca.stefanm.ibus.gui.menu.widgets.screenMenu.SmoothScroll
import ca.stefanm.ibus.gui.networkSetup.activateConnection.ui.connectionList.ConnectionListItemViews
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
                        ConnectionListItemViews.ConnectionListDivider(
                            dividerHeader = "Wired",
                            modifier = Modifier,
                        )
                    },
                    { allocatedIndex, currentIndex ->
                        ConnectionListItemViews.Connection(
                            connectionName = "Wired Connection 1",
                            modifier = Modifier,
                            chipOrientation = ItemChipOrientation.W,
                            isSelected = allocatedIndex == currentIndex,
                            onClicked = CallWhen(currentIndexIs = allocatedIndex) {

                            }
                        )
                    },
                    { allocatedIndex, currentIndex ->
                        ConnectionListItemViews.Connection(
                            connectionName = "Wired Connection 2",
                            modifier = Modifier,
                            chipOrientation = ItemChipOrientation.W,
                            isSelected = allocatedIndex == currentIndex,
                            onClicked = CallWhen(currentIndexIs = allocatedIndex) {

                            }
                        )
                    },
                    { _, _ ->
                        ConnectionListItemViews.ConnectionListDivider(
                            dividerHeader = "Wi-Fi",
                            modifier = Modifier,
                        )
                    },
                    { allocatedIndex, currentIndex ->
                        ConnectionListItemViews.Connection(
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
                        ConnectionListItemViews.Connection(
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
                        ConnectionListItemViews.Connection(
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
                        ConnectionListItemViews.Connection(
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
                        ConnectionListItemViews.ConnectionListDivider(
                            dividerHeader = "Bridge (docker0)",
                            modifier = Modifier,
                        )
                    },
                    { allocatedIndex, currentIndex ->
                        ConnectionListItemViews.Connection(
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
                        ConnectionListItemViews.ConnectionListDivider(
                            dividerHeader = "Bluetooth",
                            modifier = Modifier,
                        )
                    },
                    { allocatedIndex, currentIndex ->
                        ConnectionListItemViews.Connection(
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