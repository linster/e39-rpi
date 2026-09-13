package ca.stefanm.ca.stefanm.ibus.gui.apps.videoPlayer

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import ca.stefanm.ca.stefanm.ibus.gui.apps.fileManager.FileManagerScreen
import ca.stefanm.ca.stefanm.ibus.gui.apps.fileManager.FilePickerParameterProvider
import ca.stefanm.ca.stefanm.ibus.gui.apps.fileManager.FilePickerScreen
import ca.stefanm.ca.stefanm.ibus.gui.apps.fileManager.FilerPickerParameters.Filter
import ca.stefanm.ca.stefanm.ibus.gui.apps.fileManager.impl.FileManagerScreenFileSelectionResultHelper
import ca.stefanm.ibus.autoDiscover.AutoDiscover
import ca.stefanm.ibus.gui.menu.navigator.NavigationNode
import ca.stefanm.ibus.gui.menu.navigator.NavigationNodeTraverser
import ca.stefanm.ibus.gui.menu.navigator.Navigator
import ca.stefanm.ibus.gui.menu.widgets.BmwSingleLineHeader
import ca.stefanm.ibus.gui.menu.widgets.screenMenu.HalfScreenMenu
import ca.stefanm.ibus.gui.menu.widgets.screenMenu.TextMenuItem
import ca.stefanm.ibus.gui.menu.widgets.themes.ThemeWrapper
import ca.stefanm.ibus.lib.logging.Logger
import java.io.File
import javax.inject.Inject

@AutoDiscover
class VideoPlayerAppHomeScreen @Inject constructor(
    private val navigationNodeTraverser: NavigationNodeTraverser,
    private val filePickerParameterProvider: FilePickerParameterProvider,
    private val logger : Logger,
    private val filePickerScreen: FilePickerScreen,
    private val fileSelectionResultHelper: FileManagerScreenFileSelectionResultHelper
) : NavigationNode<Nothing> {

    override val thisClass: Class<out NavigationNode<Nothing>>
        get() = VideoPlayerAppHomeScreen::class.java

    override fun provideMainContent(): @Composable ((incomingResult: Navigator.IncomingResult?) -> Unit) = { params ->

        val selectedFile = fileSelectionResultHelper.parseSelectedFile(params)

        if (selectedFile != null) {
            VideoPlayerScreen.openWithFile(
                navigationNodeTraverser,
                VideoPlayerScreen.VideoPlayerScreenParams(
                    file = selectedFile
                )
            )
        }

        Column(
            Modifier.fillMaxSize()
                .background(ThemeWrapper.ThemeHandle.current.colors.menuBackground)
        ) {

            BmwSingleLineHeader("Video Player")

            HalfScreenMenu.BottomHalfTwoColumn(
                modifier = Modifier.fillMaxHeight(),
                leftItems = listOf(
                    TextMenuItem(
                        "Go Back",
                        onClicked = { navigationNodeTraverser.goBack() }
                    )
                ),
                rightItems = listOf(
                    TextMenuItem(
                        "Open File Picker",
                        onClicked = {
                            FileManagerScreen.openForFileSelection(
                                navigationNodeTraverser,
                                baseDirectory = File("/home/stefan/BMW/fileManTest/"),
                                filter = Filter.Videos
                            )
                        }
                    )

                )
            )
        }



    }
}