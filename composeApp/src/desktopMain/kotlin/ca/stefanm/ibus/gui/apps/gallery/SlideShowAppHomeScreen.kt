package ca.stefanm.ibus.gui.apps.gallery

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
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
import ca.stefanm.ibus.gui.menu.Notification
import ca.stefanm.ibus.gui.menu.navigator.NavigationNode
import ca.stefanm.ibus.gui.menu.navigator.NavigationNodeTraverser
import ca.stefanm.ibus.gui.menu.navigator.Navigator
import ca.stefanm.ibus.gui.menu.notifications.NotificationHub
import ca.stefanm.ibus.gui.menu.widgets.ItemChipOrientation
import ca.stefanm.ibus.gui.menu.widgets.MenuItem
import ca.stefanm.ibus.gui.menu.widgets.knobListener.KnobListenerService
import ca.stefanm.ibus.gui.menu.widgets.knobListener.dynamic.KnobObserverBuilderScope
import ca.stefanm.ibus.gui.menu.widgets.knobListener.dynamic.toDynamicLambda
import ca.stefanm.ibus.gui.menu.widgets.modalMenu.ModalMenuService
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
    private val mimeTools: MimeTools
) : NavigationNode<Nothing> {

    companion object {
        const val TAG = "SlideShowAppHomeScreen"

        fun openAfterSlideShowEnds(
            navigationNodeTraverser: NavigationNodeTraverser
        ) {
            //Just in case we need to re-set or reset some parameters?
            navigationNodeTraverser.navigateToRoot()
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
        autoAdvance : Boolean,
        startIndex : Int,
        advanceTimeMs : Int
    ) : SlideshowService.SlideShowOptions {

        //TODO so many mimetools lookups :(

        val fileList : List<File> = folderSelected.listFiles {
            if (it.isDirectory) return@listFiles false
            val type = mimeTools.getFileTypeForFile(it)
            type == FileType.Movie || type == FileType.Picture
        }.toList()

        return SlideshowService.SlideShowOptions(
            fileList = fileList.map { it to mimeTools.getFileTypeForFile(it) },
            startAtIndex = startIndex.coerceIn(0, fileList.lastIndex),
            delayBetweenPictures = if (autoAdvance) Duration.INFINITE else advanceTimeMs.milliseconds
        )
    }

    fun startSlideshow(options : SlideshowService.SlideShowOptions) {
        getSlideshowService()?.options = options
        configurablePlatform.startServiceByName("SlideshowService")
    }

    fun stopSlideShow() {
        configurablePlatform.stopServiceByName("SlideshowService")
    }

    override fun provideMainContent(): @Composable ((incomingResult: Navigator.IncomingResult?) -> Unit) = { params ->

        val slideShowIsRunning = getSlideShowServiceIsRunning().collectAsState(false)

        val folderSelected : File? = folderSelectionResultHelper.parseSelectedFolder(params)

        val autoAdvance = remember { mutableStateOf(false) }
        val startIndex = remember { mutableStateOf(0) }
        val advanceTimeMs = remember { mutableStateOf(5)}

        with(context) {
            OneColumnSmoothScreenCustomViews(
                header = "Slideshow",
                prependGoBackEntry = false,
                items = buildList<@Composable KnobObserverBuilderScope.(allocatedIndex: Int, currentIndex: Int) -> Unit> {

                    if (!slideShowIsRunning.value) {
                        add(TextMenuItem(
                            "Go Back",
                            onClicked = {
                                navigationNodeTraverser.goBack()
                            }
                        ).toDynamicLambda())
                        add(
                            TextMenuItem(
                            "Select Folder",
                            onClicked = {
                                FileManagerScreen.openForFolderSelection(
                                    navigationNodeTraverser,
                                    filter = Filter.Pictures
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
                            add(
                                CheckBoxMenuItem(
                                title = "Auto advance?",
                                isChecked = autoAdvance.value,
                                onCheckChanged = { autoAdvance.value = it }
                            ).toDynamicLambda())
                            if (autoAdvance.value) {
                                add { allocatedIndex, currentIndex ->
                                    MenuItem(
                                        label = "Set advance time: ${advanceTimeMs.value} ms...",
                                        chipOrientation = ItemChipOrientation.W,
                                        isSelected = allocatedIndex == currentIndex,
                                        onClicked = CallWhen(currentIndexIs = allocatedIndex) {
                                            modalMenuService.showKeyboard(
                                                Keyboard.KeyboardType.NUMERIC,
                                                prefilled = advanceTimeMs.value.toString(),
                                                onTextEntered = { new ->
                                                    new.toIntOrNull()?.let {
                                                        advanceTimeMs.value = it
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
                                            prefilled = advanceTimeMs.value.toString(),
                                            onTextEntered = { new ->
                                                new.toIntOrNull()?.let {
                                                    startIndex.value = it
                                                }
                                            }
                                        )
                                    }
                                )
                            }
                            add(
                                TextMenuItem(
                                title = "Start slideshow",
                                onClicked = {
                                    logger.d(TAG, "navigationNodeTraverser: ${navigationNodeTraverser.hashCode()}")
                                    startSlideshow(
                                        prepareSlideShowOptions(
                                            folderSelected = folderSelected!!,
                                            autoAdvance = autoAdvance.value,
                                            startIndex = startIndex.value,
                                            advanceTimeMs = advanceTimeMs.value
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