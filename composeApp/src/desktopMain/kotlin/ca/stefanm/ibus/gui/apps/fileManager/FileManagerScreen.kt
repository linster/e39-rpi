package ca.stefanm.ibus.gui.apps.fileManager

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import ca.stefanm.ibus.gui.apps.actionRouter.ActionRouter
import ca.stefanm.ibus.gui.apps.actionRouter.FileAction
import ca.stefanm.ibus.gui.apps.fileManager.impl.FileManagerScreenFolderSelectionResultHelper
import ca.stefanm.ibus.gui.apps.fileManager.impl.FileManagerScreenOpenParameters
import ca.stefanm.ibus.gui.apps.fileManager.impl.FileManagerScreenOpener
import ca.stefanm.ibus.gui.apps.fileManager.impl.FileManagerScreenParamsParser
import ca.stefanm.ibus.gui.apps.fileManager.impl.FileManagerScreenResult
import ca.stefanm.ibus.gui.apps.fileManager.impl.FileManagerScreenSelfOpener
import ca.stefanm.ibus.gui.apps.fileManager.impl.operation.MultiStepOperationBuilder
import ca.stefanm.ibus.gui.apps.fileManager.impl.OpenMode
import ca.stefanm.ibus.gui.apps.fileManager.impl.operation.DeleteFileScreen
import ca.stefanm.ibus.gui.apps.fileManager.impl.operation.MultiStepOperationProgressScreen
import ca.stefanm.ibus.gui.apps.fileManager.impl.operation.PermissionsModifierScreen
import ca.stefanm.ibus.gui.apps.fileManager.impl.operation.RenameFileScreen
import ca.stefanm.ibus.gui.apps.fileManager.impl.repo.DirectoryRepo
import ca.stefanm.ibus.gui.apps.fileManager.impl.views.FileManagerViewState
import ca.stefanm.ibus.gui.apps.fileManager.impl.views.INavigationButtonVisibleProvider
import ca.stefanm.ibus.gui.apps.fileManager.impl.views.INewButtonVisibleProvider
import ca.stefanm.ibus.gui.apps.fileManager.impl.views.parts.FileSidebar
import ca.stefanm.ibus.gui.apps.fileManager.impl.views.parts.FolderSidebar
import ca.stefanm.ibus.gui.apps.fileManager.impl.views.parts.IconProvider
import ca.stefanm.ibus.gui.apps.fileManager.impl.views.parts.PreviewProvider
import ca.stefanm.ibus.gui.apps.fileManager.impl.views.parts.ToolbarViews
import ca.stefanm.ibus.annotations.screenflow.ScreenDoc
import ca.stefanm.ibus.autoDiscover.AutoDiscover
import ca.stefanm.ibus.di.ApplicationModule
import ca.stefanm.ibus.gui.apps.fileManager.impl.settings.FileManagerSettings
import ca.stefanm.ibus.gui.apps.fileManager.impl.settings.FileManagerSettingsOverridesRepo
import ca.stefanm.ibus.gui.menu.navigator.NavigationNode
import ca.stefanm.ibus.gui.menu.navigator.NavigationNodeTraverser
import ca.stefanm.ibus.gui.menu.navigator.Navigator
import ca.stefanm.ibus.gui.menu.notifications.NotificationHub
import ca.stefanm.ibus.gui.menu.widgets.ArbitraryContentsMenuItem
import ca.stefanm.ibus.gui.menu.widgets.ItemChipOrientation
import ca.stefanm.ibus.gui.menu.widgets.knobListener.KnobListenerService
import ca.stefanm.ibus.gui.menu.widgets.knobListener.dynamic.KnobObserverBuilderScope
import ca.stefanm.ibus.gui.menu.widgets.knobListener.dynamic.KnobObserverBuilderState
import ca.stefanm.ibus.gui.menu.widgets.knobListener.dynamic.toDynamicLambdas
import ca.stefanm.ibus.gui.menu.widgets.modalMenu.ModalMenuService
import ca.stefanm.ibus.gui.menu.widgets.screenMenu.SmoothScroll
import ca.stefanm.ibus.gui.menu.widgets.screenMenu.TextMenuItem
import ca.stefanm.ibus.gui.menu.widgets.themes.ThemeWrapper
import ca.stefanm.ibus.lib.logging.Logger
import java.io.File
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
    private val fileManagerScreenParameterParser: FileManagerScreenParamsParser,
    private val folderSelectionResultHelper: FileManagerScreenFolderSelectionResultHelper,
    private val previewProvider: PreviewProvider,
    private val iconProvider: IconProvider,
    private val fileSidebar: FileSidebar,
    private val folderSidebar: FolderSidebar,
    private val multiStepOperationBuilder: MultiStepOperationBuilder,
    private val actionRouter: ActionRouter
) : NavigationNode<Nothing> {

    companion object : FileManagerScreenOpener, FileManagerScreenSelfOpener {
        const val TAG = "FileManagerScreen"
    }

    override val thisClass: Class<out NavigationNode<Nothing>>
        get() = FileManagerScreen::class.java


    override fun provideMainContent(): @Composable ((incomingResult: Navigator.IncomingResult?) -> Unit) = { params ->

        val openParams = fileManagerScreenParameterParser.parseOpenParameters(params)

        val folderSelection = folderSelectionResultHelper.parseSelectedFolder(params)
        if (folderSelection != null && multiStepOperationBuilder.operationBuildingStarted()) {
            multiStepOperationBuilder.setDestinationFolder(folderSelection)
        }

        if (multiStepOperationBuilder.operationBuilt()) {
            navigationNodeTraverser.navigateToNode(MultiStepOperationProgressScreen::class.java)
        }

        val knobStateMain = KnobObserverBuilderState.setupListener(
            knobListenerService = knobListenerServiceMain,
            logger,
            TAG
        )

        val viewState = remember { FileManagerViewState() }

        LaunchedEffect(openParams) {
            directoryRepo.setBaseDirectory(openParams.baseDirectory)
            directoryRepo.requestNavigateToDirectory(openParams.openDirectory)
        }

        Column {

            ToolbarViews.HeaderBar(
                openParams.openMode,
                directoryRepo.getCurrentDirectoryPath().collectAsState("?").value
            )

            ToolbarViews.Toolbar(
                knobState = knobStateMain,
                modalMenuService = modalMenuService,
                viewState = viewState,
                navigationButtonVisibleProvider = object : INavigationButtonVisibleProvider {
                    override fun backVisible(): Boolean = false
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
                exitButtonText = when (openParams.openMode) {
                    OpenMode.BROWSE -> "Close"
                    OpenMode.SELECT_FILE,
                    OpenMode.SELECT_FOLDER_LOCATION-> "Cancel Select"
                    OpenMode.SELECT_COPY_TO_FOLDER -> "Cancel Copy"
                    OpenMode.SELECT_MOVE_TO_FOLDER -> "Cancel Move"
                },
                onExitButtonClicked = {
                    when (openParams.openMode) {
                        OpenMode.BROWSE -> {
                            navigationNodeTraverser.navigateToRoot()
                        }
                        OpenMode.SELECT_FILE -> {
                            navigationNodeTraverser.setResultAndGoBack(
                                FileManagerScreen::class.java,
                                FileManagerScreenResult.FileManagerScreenResultForSelectFile.NoFileSelected
                            )
                        }
                        OpenMode.SELECT_FOLDER_LOCATION -> {
                            navigationNodeTraverser.setResultAndGoBack(
                                FileManagerScreen::class.java,
                                FileManagerScreenResult.FileManagerScreenResultForSelectFolder.NoFolderSelected
                            )
                        }
                        OpenMode.SELECT_COPY_TO_FOLDER,
                        OpenMode.SELECT_MOVE_TO_FOLDER -> {
                            multiStepOperationBuilder.clear()
                            navigationNodeTraverser.setResultAndGoBack(
                                FileManagerScreen::class.java,
                                FileManagerScreenResult.FileManagerScreenResultForSelectFolder.NoFolderSelected
                            )
                        }
                    }
                }
            )

            val entries = directoryRepo.getDirectoryFlow(
                showFakeSelectThisDirectoryEntry = openParams.showSelectThisFolderEntries,
                filter = openParams.fileFilter
            ).collectAsState(emptyList())

            Column(Modifier
                .background(ThemeWrapper.ThemeHandle.current.colors.menuBackground)
                .fillMaxSize()
            ) {
                when (viewState.itemStyle) {
                    FileManagerViewState.ItemStyle.List -> ListView(entries.value) { entry -> onDirectoryEntrySelected(openParams, entry)}
                    FileManagerViewState.ItemStyle.Grid -> GridView(
                        havePreview = viewState.showPreview,
                        rowHeightFraction = viewState.getPreviewItemRowHeightFraction(),
                        entries = entries.value,
                        onEntrySelected = { entry -> onDirectoryEntrySelected(openParams, entry)}
                    )
                    FileManagerViewState.ItemStyle.ListWithPreviews -> ListViewWithPreviews(
                        rowHeightFraction = viewState.getPreviewItemRowHeightFraction(),
                        entries = entries.value,
                        onEntrySelected = { entry -> onDirectoryEntrySelected(openParams, entry)}
                    )
                }
            }
        }
    }

    fun allowModify() : Boolean =
        FileManagerSettingsOverridesRepo.config[FileManagerSettings.allowFilesystemModification]

    fun openFile(file : File) {
        actionRouter.handleFileAction(file, FileAction.VIEW)
    }

    fun onDirectoryEntrySelected(
        params : FileManagerScreenOpenParameters,
        entry : DirectoryRepo.DirectoryEntry
    ) {
        when (entry) {
            is DirectoryRepo.DirectoryEntry.Directory -> {
                if (params.openMode in listOf(OpenMode.SELECT_COPY_TO_FOLDER, OpenMode.SELECT_MOVE_TO_FOLDER, OpenMode.SELECT_FILE)) {
                    //Only browse lets the user faff around with folders.
                    directoryRepo.requestNavigateToDirectory(entry.path)
                    return
                }
                folderSidebar.openSidebarForFolder(
                    folder = entry.path,
                    allowModify = allowModify(),

                    allowCopy = params.openMode == OpenMode.BROWSE,
                    onCopyToSelected = {
                        multiStepOperationBuilder.setOperation(MultiStepOperationBuilder.Operation.COPY_FOLDER_TO_FOLDER)
                        multiStepOperationBuilder.setSource(it)
                        openForCopyTo(navigationNodeTraverser, baseDirectory = params.baseDirectory, currentDirectory = it)
                    },
                    onMoveToSelected = {
                        multiStepOperationBuilder.setOperation(MultiStepOperationBuilder.Operation.MOVE_FOLDER_TO_FOLDER)
                        multiStepOperationBuilder.setSource(it)
                        openForMoveTo(navigationNodeTraverser, baseDirectory = params.baseDirectory, currentDirectory = it)
                    },
                    onPermissionsActivityRequested = { PermissionsModifierScreen.changePermissions(navigationNodeTraverser, it) },
                    onRenameSelected = {
                        RenameFileScreen.renameFile(navigationNodeTraverser, it)
                    },
                    onDeleteSelected = {
                        DeleteFileScreen.deleteFile(navigationNodeTraverser, it)
                    },
                    onOpenSelected = {
                        directoryRepo.requestNavigateToDirectory(it)
                    }
                )
            }
            is DirectoryRepo.DirectoryEntry.DirectoryFile -> {
                fileSidebar.openSidebarForFile(
                    file = entry.file,
                    allowModify = allowModify(),
                    allowOpen = params.openMode == OpenMode.BROWSE,
                    allowSelect = params.openMode == OpenMode.SELECT_FILE,
                    allowCopy = params.openMode == OpenMode.BROWSE,
                    onCopyToSelected = {
                        multiStepOperationBuilder.setOperation(MultiStepOperationBuilder.Operation.COPY_FILE_TO_FOLDER)
                        multiStepOperationBuilder.setSource(it)
                        openForCopyTo(navigationNodeTraverser, baseDirectory = params.baseDirectory, currentDirectory = it.parentFile)
                    },
                    onMoveToSelected = {
                        multiStepOperationBuilder.setOperation(MultiStepOperationBuilder.Operation.MOVE_FILE_TO_FOLDER)
                        multiStepOperationBuilder.setSource(it)
                        openForMoveTo(navigationNodeTraverser, baseDirectory = params.baseDirectory, currentDirectory = it.parentFile)
                    },
                    onPermissionsActivityRequested = { PermissionsModifierScreen.changePermissions(navigationNodeTraverser, it) },
                    onRenameSelected = {
                        RenameFileScreen.renameFile(navigationNodeTraverser, it)
                    },
                    onOpenSelected = {
                        openFile(it)
                    },
                    onSelectFile = {
                        if (params.openMode != OpenMode.SELECT_FILE) {
                            logger.w(TAG, "The user selected 'Select this file' for $entry when the open mode was ${params.openMode}. Params were $params")
                            return@openSidebarForFile
                        }
                        navigationNodeTraverser.setResultAndGoBack(
                            thisClass,
                            FileManagerScreenResult.FileManagerScreenResultForSelectFile.FileSelected(it)
                        )
                    },
                    onDeleteSelected = {
                        DeleteFileScreen.deleteFile(navigationNodeTraverser, it)
                    }
                )
            }
            is DirectoryRepo.DirectoryEntry.SelectThisDirectory -> {
                //Check the open mode was correct for selection before making a result and returning it.
                if (params.openMode !in listOf(
                        OpenMode.SELECT_COPY_TO_FOLDER,
                        OpenMode.SELECT_MOVE_TO_FOLDER,
                        OpenMode.SELECT_FOLDER_LOCATION
                    )) {
                    logger.w(TAG, "The user selected 'Select this entry' for $entry when the open mode was ${params.openMode}. Params were $params")
                    return
                }
                navigationNodeTraverser.setResultAndGoBack(
                    thisClass,
                    FileManagerScreenResult.FileManagerScreenResultForSelectFolder.FolderSelected(directory = entry.path)
                )
            }
        }
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
                val name = when (it) {
                    is DirectoryRepo.DirectoryEntry.Directory -> "🖿 ${it.path.name}"
                    is DirectoryRepo.DirectoryEntry.SelectThisDirectory -> "🖿 Select this folder: ${it.path.name}"
                    is DirectoryRepo.DirectoryEntry.DirectoryFile -> it.path.name
                }
                TextMenuItem(
                    title = name,
                    onClicked = { onEntrySelected(it) }
                )
            }.toDynamicLambdas()
        )
    }

    @Composable
    fun GridView(
        havePreview : Boolean,
        rowHeightFraction : Float,
        entries : List<DirectoryRepo.DirectoryEntry>,
        onEntrySelected : (DirectoryRepo.DirectoryEntry) -> Unit
    ) {

        val items : List<@Composable KnobObserverBuilderScope.(allocatedIndex: Int, currentIndex: Int) -> Unit> = entries.map { entry ->
            { allocatedIndex, currentIndex ->
                ArbitraryContentsMenuItem(
                    chipOrientation = ItemChipOrientation.S,
                    isSelected = allocatedIndex == currentIndex,
                    onClicked = CallWhen(currentIndexIs = allocatedIndex) { onEntrySelected(entry) }) {

                    Column(
                        Modifier.aspectRatio(1F, matchHeightConstraintsFirst = true),
                    ) {
                        if (!havePreview) {
                            iconProvider.IconForEntry(entry)
                        } else {
                            previewProvider.FilePreview(entry.path)
                        }
                        //Label
                        val measurements = ThemeWrapper.ThemeHandle.current.bigItem
                        Text(
                            text = entry.path.name,
                            color = ThemeWrapper.ThemeHandle.current.colors.TEXT_WHITE,
                            fontSize = measurements.fontSize,
                            modifier = Modifier.clickable { onEntrySelected(entry) }
                        )
                    }
                }
            }
        }

        SmoothScroll.GridScroll(
            modifier = Modifier,
            knobListenerService = knobListenerServiceMain,
            tag = TAG,
            logger = logger,
            prependGoBackEntry = false,
            navigationNodeTraverser = navigationNodeTraverser,
            rowHeightFraction = rowHeightFraction,
            desiredItemAspectRatio = 1.5F,
            items = items
        )
    }

    @Composable
    fun ListViewWithPreviews(
        rowHeightFraction : Float,
        entries : List<DirectoryRepo.DirectoryEntry>,
        onEntrySelected : (DirectoryRepo.DirectoryEntry) -> Unit
    ) {
        BoxWithConstraints {
            val viewPortHeight = maxHeight
            val items: List<@Composable KnobObserverBuilderScope.(allocatedIndex: Int, currentIndex: Int) -> Unit> =
                entries.map { entry ->
                    { allocatedIndex, currentIndex ->
                        ArbitraryContentsMenuItem(
                            chipOrientation = ItemChipOrientation.W,
                            isSelected = allocatedIndex == currentIndex,
                            onClicked = CallWhen(currentIndexIs = allocatedIndex) {
                                onEntrySelected(entry)
                            }) {

                            Row(
                                Modifier.wrapContentHeight(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                //Preview
                                Box(
                                    Modifier
                                        .align(Alignment.CenterVertically)
                                        .height(viewPortHeight * rowHeightFraction)
                                        .aspectRatio(1F, true)
                                ) {
                                    previewProvider.FilePreview(entry.path)
                                }
                                //Label
                                val measurements = ThemeWrapper.ThemeHandle.current.bigItem
                                Text(
                                    text = entry.path.name,
                                    color = ThemeWrapper.ThemeHandle.current.colors.TEXT_WHITE,
                                    fontSize = measurements.fontSize,
                                    modifier = Modifier.clickable { onEntrySelected(entry) }
                                )

                            }
                        }
                    }
                }

            SmoothScroll.SmoothScroll(
                modifier = Modifier,
                knobListenerService = knobListenerServiceMain,
                tag = TAG,
                logger = logger,
                prependGoBackEntry = false,
                navigationNodeTraverser = navigationNodeTraverser,
                items = items
            )
        }

    }
}