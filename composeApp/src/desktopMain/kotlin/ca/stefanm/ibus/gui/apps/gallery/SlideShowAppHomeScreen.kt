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
import ca.stefanm.ibus.gui.apps.fileManager.impl.FileManagerScreenFolderSelectionResultHelper
import ca.stefanm.ibus.gui.apps.fileManager.impl.FileManagerScreenParamsParser
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
import java.io.File
import javax.inject.Inject
import javax.inject.Named
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

    private val configurablePlatform: ConfigurablePlatform
) : NavigationNode<Nothing> {

    companion object {
        const val TAG = "SlideShowAppHomeScreen"
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
        return flowOf(false)
    }

    fun prepareSlideShowOptions(
        folderSelected : File,
        autoAdvance : Boolean,
        advanceTimeMs : Int
    ) : SlideshowService.SlideShowOptions {
TODO()
        //TODO make the list of all the files to show.
    }

    fun startSlideshow(options : SlideshowService.SlideShowOptions) {

    }

    fun stopSlideShow() {

    }

    override fun provideMainContent(): @Composable ((incomingResult: Navigator.IncomingResult?) -> Unit) = { params ->

        val slideShowIsRunning = getSlideShowServiceIsRunning().collectAsState(false)

        //TODO might have to curry these params like crazy?
        val folderSelected : File? = folderSelectionResultHelper.parseSelectedFolder(params)

        val autoAdvance = remember { mutableStateOf(false) }
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
                                    navigationNodeTraverser
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
                            add(
                                TextMenuItem(
                                title = "Start slideshow",
                                onClicked = {
                                    startSlideshow(
                                        prepareSlideShowOptions(
                                            folderSelected = folderSelected!!,
                                            autoAdvance = autoAdvance.value,
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