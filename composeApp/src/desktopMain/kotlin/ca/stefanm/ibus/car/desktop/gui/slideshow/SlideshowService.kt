package ca.stefanm.ibus.car.desktop.gui.slideshow

import ca.stefanm.ibus.annotations.services.PlatformServiceInfo
import ca.stefanm.ibus.car.di.ConfiguredCarModule
import ca.stefanm.ibus.car.di.ConfiguredCarScope
import ca.stefanm.ibus.car.platform.LongRunningGuiServices
import ca.stefanm.ibus.car.platform.LongRunningService
import ca.stefanm.ibus.car.platform.Service
import ca.stefanm.ibus.gui.apps.actionRouter.ActionRouter
import ca.stefanm.ibus.gui.apps.fileManager.impl.fileType.FileType
import ca.stefanm.ibus.gui.menu.navigator.NavigationNodeTraverser
import ca.stefanm.ibus.lib.logging.Logger
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import java.io.File
import javax.inject.Inject
import javax.inject.Named
import kotlin.time.Duration

@PlatformServiceInfo(
    name = "SlideshowService",
    description = "A service that runs the slideshow. "
)
@LongRunningGuiServices
@ConfiguredCarScope
class SlideshowService @Inject constructor(
    private val logger: Logger,
    @Named(ConfiguredCarModule.SERVICE_COROUTINE_SCOPE) private val coroutineScope: CoroutineScope,
    @Named(ConfiguredCarModule.SERVICE_COROUTINE_DISPATCHER) parsingDispatcher: CoroutineDispatcher,
    private val navigationNodeTraverser: NavigationNodeTraverser,
    private val actionRouter: ActionRouter
) : LongRunningService(coroutineScope, parsingDispatcher) {

    data class SlideShowOptions(
        val fileList : List<Pair<File, FileType>>,
        val delayBetweenPictures : Duration
    )

    override suspend fun doWork() {

    }

    var options : SlideShowOptions? = null


    fun doStuff() {
        logger.d("SlideshowService", "Hello")
    }
}