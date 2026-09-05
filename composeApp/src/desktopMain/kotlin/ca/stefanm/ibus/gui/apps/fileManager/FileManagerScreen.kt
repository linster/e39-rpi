package ca.stefanm.ca.stefanm.ibus.gui.apps.fileManager

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import ca.stefanm.ca.stefanm.ibus.gui.apps.fileManager.impl.FileManagerScreenOpenParameters
import ca.stefanm.ca.stefanm.ibus.gui.apps.fileManager.impl.FileManagerScreenOpener
import ca.stefanm.ca.stefanm.ibus.gui.apps.fileManager.impl.FileManagerScreenParamsParser
import ca.stefanm.ca.stefanm.ibus.gui.apps.fileManager.impl.repo.DirectoryRepo
import ca.stefanm.ca.stefanm.ibus.gui.apps.fileManager.impl.views.FileManagerViewState
import ca.stefanm.ca.stefanm.ibus.gui.apps.fileManager.impl.views.IDirectoryNavigatorReader
import ca.stefanm.ca.stefanm.ibus.gui.apps.fileManager.impl.views.IDirectoryStateRequestor
import ca.stefanm.ca.stefanm.ibus.gui.apps.fileManager.impl.views.INavigationButtonVisibleProvider
import ca.stefanm.ca.stefanm.ibus.gui.apps.fileManager.impl.views.INewButtonVisibleProvider
import ca.stefanm.ca.stefanm.ibus.gui.apps.fileManager.impl.views.parts.ToolbarViews
import ca.stefanm.ibus.annotations.screenflow.ScreenDoc
import ca.stefanm.ibus.autoDiscover.AutoDiscover
import ca.stefanm.ibus.di.ApplicationModule
import ca.stefanm.ibus.gui.menu.navigator.NavigationNode
import ca.stefanm.ibus.gui.menu.navigator.NavigationNodeTraverser
import ca.stefanm.ibus.gui.menu.navigator.Navigator
import ca.stefanm.ibus.gui.menu.notifications.NotificationHub
import ca.stefanm.ibus.gui.menu.widgets.knobListener.KnobListenerService
import ca.stefanm.ibus.gui.menu.widgets.knobListener.dynamic.KnobObserverBuilderState
import ca.stefanm.ibus.gui.menu.widgets.knobListener.dynamic.toDynamicLambdas
import ca.stefanm.ibus.gui.menu.widgets.modalMenu.ModalMenuService
import ca.stefanm.ibus.gui.menu.widgets.screenMenu.SmoothScroll
import ca.stefanm.ibus.gui.menu.widgets.screenMenu.TextMenuItem
import ca.stefanm.ibus.gui.menu.widgets.themes.ThemeWrapper
import ca.stefanm.ibus.lib.logging.Logger
import javax.inject.Inject
import javax.inject.Named

@ScreenDoc(
    screenName = "FileManagerScreen",
    description = "The main screen for the file manager app."
)
@AutoDiscover
class FileManagerScreen @Inject constructor(
    @Named(ApplicationModule.KNOB_LISTENER_MAIN)
    private val knobListenerServiceMain: KnobListenerService,

    @Named(ApplicationModule.KNOB_LISTENER_MAIN_AUX)
    private val knobListenerServiceMainAux: KnobListenerService,

    private val modalMenuService: ModalMenuService,
    private val logger: Logger,
    private val navigationNodeTraverser: NavigationNodeTraverser,
    private val notificationHub: NotificationHub,
    private val directoryRepo: DirectoryRepo,
    private val fileManagerScreenParameterParser: FileManagerScreenParamsParser
) : NavigationNode<Nothing> {

    companion object : FileManagerScreenOpener {
        const val TAG = "FileManagerScreen"
    }

    override val thisClass: Class<out NavigationNode<Nothing>>
        get() = FileManagerScreen::class.java


    override fun provideMainContent(): @Composable ((incomingResult: Navigator.IncomingResult?) -> Unit) = { params ->

        val params = fileManagerScreenParameterParser.parse(params)

        val knobStateMain = KnobObserverBuilderState.setupListener(
            knobListenerService = knobListenerServiceMain,
            logger,
            TAG
        )

        val viewState = remember { FileManagerViewState() }
        val directoryRepo = remember { directoryRepo }

        LaunchedEffect(params) {
            directoryRepo.setBaseDirectory(params.baseDirectory)
            directoryRepo.requestNavigateToDirectory(params.baseDirectory)
        }

        Column {

            ToolbarViews.HeaderBar(directoryRepo.getCurrentDirectoryPath().collectAsState("?").value)

            ToolbarViews.Toolbar(
                knobState = knobStateMain,
                modalMenuService = modalMenuService,
                viewState = viewState,
                navigationButtonVisibleProvider = object : INavigationButtonVisibleProvider {
                    override fun backVisible(): Boolean = true
                    override fun forwardVisible(): Boolean = false
                    override fun upVisible(): Boolean = true
                },
                newButtonVisibleProvider = object : INewButtonVisibleProvider {
                    override fun isNewFileVisible() = true
                    override fun isNewFolderVisible() = true
                },
                directoryNavigatorReader = directoryRepo,
                directoryStateRequestor = directoryRepo,
                onNewFileClicked = { },
                onNewFolderClicked = { },
                exitButtonText = "Cancel",
                onExitButtonClicked = { }
            )

            val entries = directoryRepo.getDirectoryFlow(
                showFakeSelectThisDirectoryEntry = params.showSelectThisFolderEntries,
                filter = params.fileFilter
            ).collectAsState(emptyList())

            Column(Modifier
                .background(ThemeWrapper.ThemeHandle.current.colors.menuBackground)
                .fillMaxSize()
            ) {
                when (viewState.itemStyle) {
                    FileManagerViewState.ItemStyle.List -> ListView(entries.value) {}
                    FileManagerViewState.ItemStyle.Grid -> GridView(
                        rowHeightFraction = viewState.getPreviewItemRowHeightFraction(),
                        entries = entries.value,
                        onEntrySelected = { entry -> onDirectoryEntrySelected(params, entry)}
                    )
                    FileManagerViewState.ItemStyle.ListWithPreviews -> ListViewWithPreviews(
                        rowHeightFraction = viewState.getPreviewItemRowHeightFraction(),
                        entries = entries.value,
                        onEntrySelected = { entry -> onDirectoryEntrySelected(params, entry)}
                    )
                }
            }
        }
    }

    fun onDirectoryEntrySelected(
        params : FileManagerScreenOpenParameters,
        entry : DirectoryRepo.DirectoryEntry
    ) {
        //TODO this needs the open mode from the composable to know what entries to enable.
    }

    @Composable
    fun ListView(entries : List<DirectoryRepo.DirectoryEntry>, onEntrySelected : (DirectoryRepo.DirectoryEntry) -> Unit) {
        SmoothScroll.SmoothScroll(
            modifier = Modifier,
            knobListenerService = knobListenerServiceMain,
            tag = TAG,
            logger = logger,
            prependGoBackEntry = false,
            navigationNodeTraverser = navigationNodeTraverser,
            items = entries.map {
                TextMenuItem(
                    title = it.path.name,
                    onClicked = { onEntrySelected(it) }
                )
            }.toDynamicLambdas()
        )
    }

    @Composable
    fun GridView(
        rowHeightFraction : Float,
        entries : List<DirectoryRepo.DirectoryEntry>,
        onEntrySelected : (DirectoryRepo.DirectoryEntry) -> Unit
    ) {
        SmoothScroll.GridScroll(
            modifier = Modifier,
            knobListenerService = knobListenerServiceMain,
            tag = TAG,
            logger = logger,
            prependGoBackEntry = false,
            navigationNodeTraverser = navigationNodeTraverser,
            rowHeightFraction = rowHeightFraction,
            desiredItemAspectRatio = 1F,
            items = listOf()
        )
    }

    @Composable
    fun ListViewWithPreviews(
        rowHeightFraction : Float,
        entries : List<DirectoryRepo.DirectoryEntry>,
        onEntrySelected : (DirectoryRepo.DirectoryEntry) -> Unit
    ) {


        SmoothScroll.SmoothScroll(
            modifier = Modifier,
            knobListenerService = knobListenerServiceMain,
            tag = TAG,
            logger = logger,
            prependGoBackEntry = false,
            navigationNodeTraverser = navigationNodeTraverser,
            items = entries.map {
                TextMenuItem(
                    title = it.path.name,
                    onClicked = { onEntrySelected(it) }
                )
            }.toDynamicLambdas()
        )

    }
}