package ca.stefanm.ca.stefanm.ibus.gui.apps.fileManager.impl.views



interface INewButtonVisibleProvider {
    fun isNewFileVisible() : Boolean
    fun isNewFolderVisible() : Boolean
}

interface INavigationButtonVisibleProvider {
    fun backVisible() : Boolean
    fun forwardVisible() : Boolean
    fun upVisible() : Boolean
}

interface IDirectoryNavigatorReader {
    fun canGoBack() : Boolean
    fun canGoForward() : Boolean
    fun canGoUp() : Boolean
}

interface IDirectoryStateRequestor {

    fun requestNavigateBack()
    fun requestNavigateUp()
    fun requestNavigateForward()
}