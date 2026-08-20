package ca.stefanm.ca.stefanm.ibus.gui.apps.fileManager.impl.views.parts

import androidx.compose.runtime.Composable
import ca.stefanm.ca.stefanm.ibus.gui.apps.fileManager.impl.views.FileManagerViewState
import ca.stefanm.ibus.gui.menu.widgets.BmwSingleLineHeader

object ToolbarViews {

    @Composable
    fun HeaderBar(
        viewState : FileManagerViewState
    ) {

        BmwSingleLineHeader()
    }

    @Composable
    fun Toolbar(
        viewState: FileManagerViewState,
        onViewStateChanged : (new : FileManagerViewState) -> Unit
    ) {


    }

}