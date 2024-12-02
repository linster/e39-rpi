package ca.stefanm.ca.stefanm.ibus.gui.audio

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import ca.stefanm.ca.stefanm.ibus.car.audio.nowPlayingReader.NowPlayingTextFieldFlows
import ca.stefanm.ca.stefanm.ibus.car.audio.nowPlayingReader.RadioTextFieldReaderService
import ca.stefanm.ca.stefanm.ibus.car.audio.nowPlayingReader.RadioTextFields
import ca.stefanm.ibus.annotations.screenflow.ScreenDoc
import ca.stefanm.ibus.autoDiscover.AutoDiscover
import ca.stefanm.ibus.gui.menu.navigator.NavigationNode
import ca.stefanm.ibus.gui.menu.navigator.NavigationNodeTraverser
import ca.stefanm.ibus.gui.menu.navigator.Navigator
import ca.stefanm.ibus.gui.menu.widgets.BmwFullScreenTrackInfoHeader
import ca.stefanm.ibus.gui.menu.widgets.BmwSingleLineHeader
import ca.stefanm.ibus.gui.menu.widgets.screenMenu.FullScreenMenu
import ca.stefanm.ibus.gui.menu.widgets.screenMenu.HalfScreenMenu
import ca.stefanm.ibus.gui.menu.widgets.screenMenu.TextMenuItem
import ca.stefanm.ibus.gui.menu.widgets.themes.ThemeWrapper
import javax.inject.Inject

@ScreenDoc(
    screenName = "NowPlayingMenu",
    description = "Show radio status of what is now playing"
)
@ScreenDoc.AllowsGoBack
@AutoDiscover
class NowPlayingMenu @Inject constructor(
    private val navigationNodeTraverser: NavigationNodeTraverser,
    private val audioFocusSidePane: AudioFocusSidePane,
//    private val radioTextFieldReaderService: RadioTextFieldReaderService
) : NavigationNode<Nothing> {

    override val thisClass: Class<out NavigationNode<Nothing>>
        get() = NowPlayingMenu::class.java

    override fun provideMainContent(): @Composable (incomingResult: Navigator.IncomingResult?) -> Unit = {
        Column(modifier = Modifier.fillMaxSize()
            .background(ThemeWrapper.ThemeHandle.current.colors.menuBackground),
            verticalArrangement = Arrangement.SpaceBetween) {

            Column {
                BmwSingleLineHeader("Now Playing")

                val header: State<RadioTextFields> =
                    NowPlayingTextFieldFlows.radioTextFieldsFlow.collectAsState(RadioTextFields())

                BmwFullScreenTrackInfoHeader(
                    t0 = header.value.t0,
                    t1 = header.value.t1,
                    t2 = header.value.t2,
                    t3 = header.value.t3,
                    t4 = header.value.t4,
                    t5 = header.value.t5,
                    t6 = header.value.t6,
                )
            }

            HalfScreenMenu.BottomHalfTwoColumn(
                leftItems = listOf(
                    TextMenuItem(
                        "Go Back",
                        onClicked = { navigationNodeTraverser.goBack() }
                    )
                ),
                rightItems = listOf(
                    TextMenuItem(
                        "Set Audio Focus",
                        onClicked = { audioFocusSidePane.show() }
                    )
                )
            )
        }
    }

}