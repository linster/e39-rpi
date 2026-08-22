package ca.stefanm.ca.stefanm.ibus.gui.apps.fileManager.impl.views

class FileManagerViewState {

    //Current directory
    //Are previews enabled
    // Is Up enabled?
    //View Mode { List, Grid, List With Preview }

    enum class ViewMode(val label : String) {
        /** Just a single column list of file names */
        List(label = "List"),
        /** A grid of files */
        Grid(label = "Grid"),
        /** A single column list of files with names and previews */
        //A separate view mode because the control is different, but
        //Selected by choosing list and enabling previews.
        ListWithPreviews(label = "List with Previews")
    }

    // Item height (if not List viewMode)

    //Think about how observable this will be with Compose?
    var previewItemHeightPx : Int = 100

}