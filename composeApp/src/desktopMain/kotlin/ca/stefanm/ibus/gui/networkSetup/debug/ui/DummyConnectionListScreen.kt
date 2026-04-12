package ca.stefanm.ca.stefanm.ibus.gui.networkSetup.debug.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import ca.stefanm.ca.stefanm.ibus.gui.networkSetup.activateConnection.ui.connectionList.ConnectionListItemViews
import ca.stefanm.ibus.annotations.screenflow.ScreenDoc
import ca.stefanm.ibus.autoDiscover.AutoDiscover
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
import ca.stefanm.ibus.gui.menu.widgets.themes.ThemeWrapper
import ca.stefanm.ibus.lib.logging.Logger
import javax.inject.Inject
import javax.inject.Named

@ScreenDoc(
    screenName = "DummyConnectionListScreen",
    description = "A dummy debug screen to show a fake list of connections"
)
@ScreenDoc.AllowsGoBack
@AutoDiscover
class DummyConnectionListScreen @Inject constructor(
    private val navigationNodeTraverser: NavigationNodeTraverser,
    @Named(ApplicationModule.KNOB_LISTENER_MAIN)
    private val knobListenerService: KnobListenerService,
    private val logger: Logger
) : NavigationNode<Nothing> {

    override val thisClass: Class<out NavigationNode<Nothing>>
        get() = DummyConnectionListScreen::class.java

    override fun provideMainContent(): @Composable ((Navigator.IncomingResult?) -> Unit) = {
        Column(Modifier
            .background(ThemeWrapper.ThemeHandle.current.colors.menuBackground)
            .fillMaxSize()
        ) {
            BmwSingleLineHeader("Activate Connection")

            val knobState = setupListener(
                knobListenerService,
                logger,
                "DummyConnectionListScreen"
            )

            KnobObserverBuilder(knobState) { allocatedIndex, currentIndex ->
                MenuItem(
                    label = "Go Back",
                    chipOrientation = ItemChipOrientation.W,
                    onClicked = CallWhen(currentIndexIs = allocatedIndex) {
                        navigationNodeTraverser.goBack()
                    }
                )
            }

            ConnectionListItemViews.ConnectionListDivider(
                dividerHeader = "Wired",
                modifier = Modifier,
            )

            KnobObserverBuilder(knobState) { allocatedIndex, currentIndex ->
                ConnectionListItemViews.Connection(
                    connectionName = "Wired Connection 1",
                    modifier = Modifier,
                    chipOrientation = ItemChipOrientation.W,
                    isSelected = allocatedIndex == currentIndex,
                    onClicked = CallWhen(currentIndexIs = allocatedIndex) {

                    }
                )
            }

            KnobObserverBuilder(knobState) { allocatedIndex, currentIndex ->
                ConnectionListItemViews.Connection(
                    connectionName = "Wired Connection 2",
                    modifier = Modifier,
                    chipOrientation = ItemChipOrientation.W,
                    isSelected = allocatedIndex == currentIndex,
                    onClicked = CallWhen(currentIndexIs = allocatedIndex) {

                    }
                )
            }

            ConnectionListItemViews.ConnectionListDivider(
                dividerHeader = "Wi-Fi",
                modifier = Modifier,
            )

            KnobObserverBuilder(knobState) { allocatedIndex, currentIndex ->
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
            }

            KnobObserverBuilder(knobState) { allocatedIndex, currentIndex ->
                ConnectionListItemViews.Connection(
                    connectionName = "RogersIgnite404",
                    strength = 75,
                    modifier = Modifier,
                    chipOrientation = ItemChipOrientation.W,
                    isSelected = allocatedIndex == currentIndex,
                    onClicked = CallWhen(currentIndexIs = allocatedIndex) {

                    }
                )
            }

            KnobObserverBuilder(knobState) { allocatedIndex, currentIndex ->
                ConnectionListItemViews.Connection(
                    connectionName = "Kathy Network",
                    strength = 60,
                    modifier = Modifier,
                    chipOrientation = ItemChipOrientation.W,
                    isSelected = allocatedIndex == currentIndex,
                    onClicked = CallWhen(currentIndexIs = allocatedIndex) {

                    }
                )
            }

            KnobObserverBuilder(knobState) { allocatedIndex, currentIndex ->
                ConnectionListItemViews.Connection(
                    connectionName = "grantnett5g",
                    strength = 6,
                    modifier = Modifier,
                    chipOrientation = ItemChipOrientation.W,
                    isSelected = allocatedIndex == currentIndex,
                    onClicked = CallWhen(currentIndexIs = allocatedIndex) {

                    }
                )
            }

            ConnectionListItemViews.ConnectionListDivider(
                dividerHeader = "Bridge (docker0)",
                modifier = Modifier,
            )

            KnobObserverBuilder(knobState) { allocatedIndex, currentIndex ->
                ConnectionListItemViews.Connection(
                    connectionName = "docker0",
                    isConnected = true,
                    modifier = Modifier,
                    chipOrientation = ItemChipOrientation.W,
                    isSelected = allocatedIndex == currentIndex,
                    onClicked = CallWhen(currentIndexIs = allocatedIndex) {

                    }
                )
            }

            ConnectionListItemViews.ConnectionListDivider(
                dividerHeader = "Bluetooth",
                modifier = Modifier,
            )

            KnobObserverBuilder(knobState) { allocatedIndex, currentIndex ->
                ConnectionListItemViews.Connection(
                    connectionName = "Stefan iPhone Network",
                    modifier = Modifier,
                    chipOrientation = ItemChipOrientation.W,
                    isSelected = allocatedIndex == currentIndex,
                    onClicked = CallWhen(currentIndexIs = allocatedIndex) {

                    }
                )
            }

        }
    }
}