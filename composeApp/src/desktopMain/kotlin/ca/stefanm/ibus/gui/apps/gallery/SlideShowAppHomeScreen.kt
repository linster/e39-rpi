package ca.stefanm.ibus.gui.apps.gallery

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import ca.stefanm.ibus.autoDiscover.AutoDiscover
import ca.stefanm.ibus.car.desktop.gui.slideshow.SlideshowService
import ca.stefanm.ibus.car.platform.ConfigurablePlatform
import ca.stefanm.ibus.car.platform.PlatformService
import ca.stefanm.ibus.di.ApplicationModule
import ca.stefanm.ibus.gui.apps.fileManager.FileManagerScreen
import ca.stefanm.ibus.gui.apps.fileManager.FilerPickerParameters.Filter
import ca.stefanm.ibus.gui.apps.fileManager.impl.FileManagerScreenFolderSelectionResultHelper
import ca.stefanm.ibus.gui.apps.fileManager.impl.FileManagerScreenParamsParser
import ca.stefanm.ibus.gui.apps.fileManager.impl.fileType.FileType
import ca.stefanm.ibus.gui.apps.fileManager.impl.fileType.MimeTools
import ca.stefanm.ibus.gui.apps.fileManager.impl.views.parts.FileSidebar
import ca.stefanm.ibus.gui.apps.videoPlayer.VideoPlayerScreen
import ca.stefanm.ibus.gui.menu.Notification
import ca.stefanm.ibus.gui.menu.navigator.NavigationNode
import ca.stefanm.ibus.gui.menu.navigator.NavigationNodeTraverser
import ca.stefanm.ibus.gui.menu.navigator.Navigator
import ca.stefanm.ibus.gui.menu.notifications.NotificationHub
import ca.stefanm.ibus.gui.menu.widgets.ItemChipOrientation
import ca.stefanm.ibus.gui.menu.widgets.MenuItem
import ca.stefanm.ibus.gui.menu.widgets.bottombar.BottomBarController
import ca.stefanm.ibus.gui.menu.widgets.halveIfNotPixelDoubled
import ca.stefanm.ibus.gui.menu.widgets.knobListener.KnobListenerService
import ca.stefanm.ibus.gui.menu.widgets.knobListener.dynamic.KnobObserverBuilderScope
import ca.stefanm.ibus.gui.menu.widgets.knobListener.dynamic.toDynamicLambda
import ca.stefanm.ibus.gui.menu.widgets.modalMenu.ModalMenuService
import ca.stefanm.ibus.gui.menu.widgets.modalMenu.SidePanelMenu
import ca.stefanm.ibus.gui.menu.widgets.modalMenu.keyboard.Keyboard
import ca.stefanm.ibus.gui.menu.widgets.screenMenu.CheckBoxMenuItem
import ca.stefanm.ibus.gui.menu.widgets.screenMenu.FullScreenMenu
import ca.stefanm.ibus.gui.menu.widgets.screenMenu.FullScreenMenu.OneColumnSmoothScreenCustomViews
import ca.stefanm.ibus.gui.menu.widgets.screenMenu.SmoothScroll
import ca.stefanm.ibus.gui.menu.widgets.screenMenu.TextMenuItem
import ca.stefanm.ibus.lib.logging.Logger
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flattenConcat
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import org.apache.commons.io.FileUtils
import java.io.File
import javax.inject.Inject
import javax.inject.Named
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

@AutoDiscover
class SlideShowAppHomeScreen @Inject constructor(
    @Named(ApplicationModule.KNOB_LISTENER_MAIN)
    private val knobListenerServiceMain: KnobListenerService,

    private val logger: Logger,
    private val navigationNodeTraverser: NavigationNodeTraverser,
    private val notificationHub: NotificationHub,
    private val modalMenuService: ModalMenuService,

    private val folderSelectionResultHelper: FileManagerScreenFolderSelectionResultHelper,

    private val configurablePlatform: ConfigurablePlatform,
    private val mimeTools: MimeTools,
    private val bottomBarController: BottomBarController
) : NavigationNode<Nothing> {

    companion object {
        const val TAG = "SlideShowAppHomeScreen"

        fun openAfterSlideShowEnds(
            navigationNodeTraverser: NavigationNodeTraverser
        ) {
            //Just in case we need to re-set or reset some parameters?
            navigationNodeTraverser.navigateToRoot()
            navigationNodeTraverser.cleanupBackStackDescendentsOf(ImageViewerScreen::class.java)
            navigationNodeTraverser.cleanupBackStackDescendentsOf(VideoPlayerScreen::class.java)
            navigationNodeTraverser.navigateToNode(SlideShowAppHomeScreen::class.java)

        }
    }

    override val thisClass: Class<out NavigationNode<Nothing>>
        get() = SlideShowAppHomeScreen::class.java


    val context = object : SmoothScroll.SmoothScrollContext {
        override fun knobListenerService() = knobListenerServiceMain
        override fun logger() = logger
        override fun navigationNodeTraverser() = navigationNodeTraverser
        override fun tag() = TAG
    }

    private fun getSlideshowService() : SlideshowService? {
        return (slideShowServicePlatformService()?.baseService as? SlideshowService) ?: let {
            notificationHub.postNotificationBackground(Notification(
                Notification.NotificationImage.ALERT_TRIANGLE,
                "Could not get Slideshow Service",
                "Car Platform might not be running?"
            ))
            null
        }
    }

    private fun slideShowServicePlatformService() : PlatformService? {
        return configurablePlatform.findServiceByName("SlideshowService")
    }

    fun getSlideShowServiceIsRunning() : Flow<Boolean> {
        //TODO this will crash if the platform isn't running.
        //TODO the whole CarComponent thing needs a rework in Metro.
        return slideShowServicePlatformService()!!.runStatusFlow.map { it == PlatformService.RunStatus.RUNNING }
    }

    fun prepareSlideShowOptions(
        folderSelected : File,
        sortMode: SortMode,
        autoAdvance : Boolean,
        startIndex : Int,
        advanceTimeSeconds : Int
    ) : SlideshowService.SlideShowOptions {

        //TODO so many mimetools lookups :(

        val fileList : List<File> = folderSelected.listFiles {
            if (it.isDirectory) return@listFiles false
            val type = mimeTools.getFileTypeForFile(it)
            type == FileType.Movie || type == FileType.Picture
        }.toList().sortBySortMode(sortMode)

        return SlideshowService.SlideShowOptions(
            fileList = fileList.map { it to mimeTools.getFileTypeForFile(it) },
            startAtIndex = startIndex.coerceIn(0, fileList.lastIndex),
            delayBetweenPictures = if (autoAdvance) advanceTimeSeconds.seconds else Duration.INFINITE,
            modalMenuService = modalMenuService,
            navigationNodeTraverser = navigationNodeTraverser,
            bottomBarController = bottomBarController
        )
    }

    fun startSlideshow(options : SlideshowService.SlideShowOptions) {
        getSlideshowService()?.options = options
        configurablePlatform.startServiceByName("SlideshowService")
    }

    fun stopSlideShow() {
        configurablePlatform.stopServiceByName("SlideshowService")
    }

    enum class SortMode(val desc : String) {
        NO_SORT("File system entries remain in naive sortation order."),
        ALPHABETIC("Sort the entries by file name alphabetically."),
        ALPHABETIC_INV("Alphabetic, reversed"),
        MODIFIED_DATE("Sort Files by modified date"),
        MODIFIED_DATE_INV("Sort files by modified date, reversed"),
        DATE_TAKEN("Sort files by Exif taken date, falling back to modified date."),
        DATE_TAKEN_INV("Sort files by Exif taken date, falling back to modified date, reversed.")
    }

    fun List<File>.sortBySortMode(mode : SortMode) : List<File> {
        if (mode == SortMode.NO_SORT) return this
        return this.sortedBy {
            when (mode) {
                SortMode.ALPHABETIC,
                SortMode.ALPHABETIC_INV -> it.name
                SortMode.MODIFIED_DATE,
                SortMode.MODIFIED_DATE_INV -> it.lastModified().toString()
                SortMode.DATE_TAKEN,
                SortMode.DATE_TAKEN_INV -> mimeTools.getDateForSortation(it).epochSeconds.toString()
                else -> it.name
            }
        }.let {
            if (mode in listOf(SortMode.DATE_TAKEN_INV, SortMode.MODIFIED_DATE_INV, SortMode.ALPHABETIC_INV)) {
                it.reversed()
            } else {
                it
            }
        }
    }

    override fun provideMainContent(): @Composable ((incomingResult: Navigator.IncomingResult?) -> Unit) = { params ->

        val slideShowIsRunning = getSlideShowServiceIsRunning().collectAsState(false)

        val folderSelected : File? = folderSelectionResultHelper.parseSelectedFolder(params)

        val autoAdvance = remember { mutableStateOf(false) }
        val startIndex = remember { mutableStateOf(0) }
        val advanceTimeSeconds = remember { mutableStateOf(5)}

        val sortMode = remember { mutableStateOf(SortMode.ALPHABETIC) }

        with(context) {
            OneColumnSmoothScreenCustomViews(
                header = "Slideshow",
                prependGoBackEntry = false,
                items = buildList<@Composable KnobObserverBuilderScope.(allocatedIndex: Int, currentIndex: Int) -> Unit> {

                    if (!slideShowIsRunning.value) {
                        add(TextMenuItem(
                            "Go Back",
                            onClicked = {
                                navigationNodeTraverser.navigateToRoot()
                            }
                        ).toDynamicLambda())
                        add(
                            TextMenuItem(
                            "Select Folder",
                            onClicked = {
                                FileManagerScreen.openForFolderSelection(
                                    navigationNodeTraverser,
                                    filter = Filter.PicturesAndVideos //TODO allow Compound Filters, because I want to show Pictures AND movies.
                                )
                            }).toDynamicLambda(noChipWhenNotSelectable = true)
                        )
                    }
                    if (folderSelected != null) {
                        add(TextMenuItem(
                            "Folder: ${folderSelected.absolutePath}",
                            isSelectable = false,
                            onClicked = {}
                        ).toDynamicLambda(noChipWhenNotSelectable = true))
                        if (!slideShowIsRunning.value) {
                            add(
                                TextMenuItem(
                                title = "Options",
                                isSelectable = false,
                                onClicked = {}
                            ).toDynamicLambda(noChipWhenNotSelectable = true))
                            add { allocatedIndex, currentIndex ->
                                MenuItem(
                                    label = "SortMode: ${sortMode.value.name}...",
                                    chipOrientation = ItemChipOrientation.W,
                                    isSelected = allocatedIndex == currentIndex,
                                    onClicked = CallWhen(currentIndexIs = allocatedIndex) {
                                        modalMenuService.showSidePaneOverlayWithKnobListener(darkenBackground = true) { knobListenerServiceModal ->
                                            SidePanelMenu.SidePanelMenu("Set Sort Mode") {

                                                val highLightedMode = remember { mutableStateOf(SortMode.values().first()) }
                                                Column(Modifier.padding(horizontal = 10.dp.halveIfNotPixelDoubled())) {
                                                    SidePanelMenu.InfoLabel("Mode details:", FontWeight.Bold)
                                                    SidePanelMenu.InfoLabel(highLightedMode.value.desc)
                                                }

                                                SmoothScroll.SmoothScroll(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    knobListenerService = knobListenerServiceModal,
                                                    tag = TAG,
                                                    logger = logger,
                                                    prependGoBackEntry = false,
                                                    navigationNodeTraverser = navigationNodeTraverser,
                                                    items = buildList {
                                                        for (mode in SortMode.values()) {
                                                            add { allocatedIndex, currentIndex ->
                                                                LaunchedEffect(allocatedIndex, currentIndex) {
                                                                    if (allocatedIndex == currentIndex) {
                                                                        highLightedMode.value = mode
                                                                    }
                                                                }
                                                                MenuItem(
                                                                    label = mode.name,
                                                                    chipOrientation = ItemChipOrientation.E,
                                                                    isSelected = allocatedIndex == currentIndex,
                                                                    onClicked = CallWhen(currentIndexIs = allocatedIndex) {
                                                                        sortMode.value = mode
                                                                        modalMenuService.closeSidePaneOverlay(true)
                                                                    }
                                                                )

                                                            }
                                                        }
                                                    }
                                                )
                                            }
                                        }
                                    }
                                )
                            }

                            add(
                                CheckBoxMenuItem(
                                title = "Auto advance?",
                                isChecked = autoAdvance.value,
                                onCheckChanged = { autoAdvance.value = it }
                            ).toDynamicLambda())
                            if (autoAdvance.value) {
                                add { allocatedIndex, currentIndex ->
                                    MenuItem(
                                        label = "Set advance time: ${advanceTimeSeconds.value} s...",
                                        chipOrientation = ItemChipOrientation.W,
                                        isSelected = allocatedIndex == currentIndex,
                                        onClicked = CallWhen(currentIndexIs = allocatedIndex) {
                                            modalMenuService.showKeyboard(
                                                Keyboard.KeyboardType.NUMERIC,
                                                prefilled = advanceTimeSeconds.value.toString(),
                                                onTextEntered = { new ->
                                                    new.toIntOrNull()?.let {
                                                        advanceTimeSeconds.value = it
                                                    }
                                                }
                                            )
                                        }
                                    )
                                }
                            }
                            add { allocatedIndex, currentIndex ->
                                MenuItem(
                                    label = "Set startIndex: (${startIndex.value})...",
                                    chipOrientation = ItemChipOrientation.W,
                                    isSelected = allocatedIndex == currentIndex,
                                    onClicked = CallWhen(currentIndexIs = allocatedIndex) {
                                        modalMenuService.showKeyboard(
                                            Keyboard.KeyboardType.NUMERIC,
                                            prefilled = advanceTimeSeconds.value.toString(),
                                            onTextEntered = { new ->
                                                new.toIntOrNull()?.let {
                                                    startIndex.value = it
                                                }
                                            }
                                        )
                                    }
                                )
                            }
                            //TODO sort by filename, or sort by date taken? or sort by file system time?
                            //TODO set this in a sidepane explaining the options.
                            add(
                                TextMenuItem(
                                title = "Start slideshow",
                                onClicked = {
                                    startSlideshow(
                                        prepareSlideShowOptions(
                                            folderSelected = folderSelected!!,
                                            autoAdvance = autoAdvance.value,
                                            startIndex = startIndex.value,
                                            sortMode = sortMode.value,
                                            advanceTimeSeconds = advanceTimeSeconds.value
                                        )
                                    )
                                }
                            ).toDynamicLambda())
                        } else {
                            add(
                                TextMenuItem(
                                    title = "Stop slideshow",
                                    onClicked = {
                                        stopSlideShow()
                                    }
                                ).toDynamicLambda())
                        }
                    }
                }
            )
        }
    }



}