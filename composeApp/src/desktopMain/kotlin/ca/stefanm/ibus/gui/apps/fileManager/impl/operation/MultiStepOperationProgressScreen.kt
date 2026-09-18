package ca.stefanm.ibus.gui.apps.fileManager.impl.operation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import ca.stefanm.ibus.autoDiscover.AutoDiscover
import ca.stefanm.ibus.di.ApplicationModule
import ca.stefanm.ibus.gui.apps.fileManager.FileManagerScreen
import ca.stefanm.ibus.gui.menu.widgets.screenMenu.FullScreenMenu.OneColumnSmoothScreenCustomViews
import ca.stefanm.ibus.gui.apps.fileManager.impl.FileManagerSettingsScreen
import ca.stefanm.ibus.gui.apps.fileManager.impl.operation.MultiStepOperationRunner
import ca.stefanm.ibus.gui.apps.fileManager.impl.operation.MultiStepOperationBuilder
import ca.stefanm.ibus.gui.apps.fileManager.impl.operation.MultiStepOperationBuilder.Operation
import ca.stefanm.ibus.gui.apps.fileManager.impl.operation.RenameFileScreen.Companion.TAG
import ca.stefanm.ibus.gui.apps.fileManager.impl.settings.FileManagerSettings
import ca.stefanm.ibus.gui.apps.fileManager.impl.settings.FileManagerSettingsOverridesRepo
import ca.stefanm.ibus.gui.menu.Notification
import ca.stefanm.ibus.gui.menu.navigator.NavigationNode
import ca.stefanm.ibus.gui.menu.navigator.NavigationNodeTraverser
import ca.stefanm.ibus.gui.menu.navigator.Navigator
import ca.stefanm.ibus.gui.menu.notifications.NotificationHub
import ca.stefanm.ibus.gui.menu.widgets.knobListener.KnobListenerService
import ca.stefanm.ibus.gui.menu.widgets.knobListener.dynamic.KnobObserverBuilderScope
import ca.stefanm.ibus.gui.menu.widgets.knobListener.dynamic.toDynamicLambdas
import ca.stefanm.ibus.gui.menu.widgets.screenMenu.FullScreenMenu
import ca.stefanm.ibus.gui.menu.widgets.screenMenu.FullScreenMenu.OneColumnSmoothScreen
import ca.stefanm.ibus.gui.menu.widgets.screenMenu.SmoothScroll
import ca.stefanm.ibus.gui.menu.widgets.screenMenu.SnapshotPair
import ca.stefanm.ibus.gui.menu.widgets.screenMenu.TextMenuItem
import ca.stefanm.ibus.lib.logging.Logger
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.apache.commons.io.FileUtils
import org.apache.commons.io.FilenameUtils
import javax.inject.Inject
import javax.inject.Named
import kotlin.collections.emptyList
import kotlin.time.Clock
import kotlin.time.Instant

//After the multi step operation is built up with the builder,
//Navigate here to see it's progress
@AutoDiscover
class MultiStepOperationProgressScreen @Inject constructor(
    @Named(ApplicationModule.KNOB_LISTENER_MAIN)
    private val knobListenerServiceMain: KnobListenerService,

    private val logger: Logger,
    private val navigationNodeTraverser: NavigationNodeTraverser,

    private val builder: MultiStepOperationBuilder,
    private val runner: MultiStepOperationRunner,
    private val notificationHub: NotificationHub
) : NavigationNode<Nothing> {

    companion object {
        const val TAG = "MultiStepOperationProgressScreen"
    }

    private val context = object : SmoothScroll.SmoothScrollContext {
        override fun knobListenerService() = knobListenerServiceMain
        override fun tag() = TAG
        override fun logger() = logger
        override fun navigationNodeTraverser() = navigationNodeTraverser
    }

    override val thisClass: Class<out NavigationNode<Nothing>>
        get() = MultiStepOperationProgressScreen::class.java

    override fun provideMainContent(): @Composable ((incomingResult: Navigator.IncomingResult?) -> Unit) = content@ {

        //Bail if we can't modify filesystem
        if (!FileManagerSettingsOverridesRepo.config[FileManagerSettings.allowFilesystemModification]) {
            logger.d(TAG, "Filesystem modification is not allowed, returning")
            notificationHub.postNotificationBackground(Notification(
                Notification.NotificationImage.ALERT_TRIANGLE,
                "Rename File Screen",
                "Filesystem modification is disabled."
            ))
            navigationNodeTraverser.goBack()
            return@content
        }

        DisposableEffect(Unit) {
            onDispose {
                //Clear the builder regardless of whether we go back from a meu entry, or the nav HMI debugger.
                builder.clear()
            }
        }

        //If the source and dest folder is the same and it's file copy operation, then append "-copy(timestamp)" to the dest file name
        if (builder.getOperation() == Operation.COPY_FILE_TO_FOLDER) {
            if (builder.sourceAndDestInSameFolder()) {
                builder.getSource()?.let { sourceFile ->
                    navigationNodeTraverser.cleanupBackStackDescendentsOf(thisClass)
                    RenameFileScreen.silentRename(
                        navigationNodeTraverser,
                        sourceFile,
                        sourceFile.name.let {
                            sourceFile.nameWithoutExtension + Clock.System.now().epochSeconds.toString() + sourceFile.extension.let { if (it.isNotEmpty()) ".$it" }
                        }
                    )
                    return@content
                }
            }
        }
        //If the source and dest folder are the same and it's a file move operation, open the file rename screen
        if (builder.getOperation() == Operation.MOVE_FILE_TO_FOLDER) {
            if (builder.sourceAndDestInSameFolder()) {
                navigationNodeTraverser.cleanupBackStackDescendentsOf(thisClass)
                builder.getSource()?.let { file -> RenameFileScreen.renameFile(navigationNodeTraverser, file) }
                return@content
            }
        }
        //If the source and dest folder are the same and it's a folder move or folder copy operation, show a notification and exit.
        if (builder.getOperation() in listOf(Operation.MOVE_FOLDER_TO_FOLDER, Operation.COPY_FOLDER_TO_FOLDER)) {
            if (builder.sourceAndDestInSameFolder()) {
                notificationHub.postNotificationBackground(Notification(
                    Notification.NotificationImage.ALERT_CIRCLE,
                    "Source and destination are the same folder"
                ))
                GlobalScope.launch {
                    delay(10L)
                    navigationNodeTraverser.goBack()
                }
                //return@content
            }
        }

        //First get the user to check the operation with a smooth scroll of the operation, source, dest.
        //Then put a button to "Start Operation".

        //SmoothScroll and entries need to be added on from the runner if they come up.... Abort, Retry, Fail. Like a terminal but with list views.

        val scope = rememberCoroutineScope()
        val isRunning = remember { mutableStateOf(false)}
        val runningState = remember { mutableStateOf(RunningState.CHECKING)}
        val entries = remember {
            mutableStateListOf<SnapshotPair<ListEntrySource, TextMenuItem>>()
        }

        LaunchedEffect(runningState.value) {
            if (entries.isEmpty() && runningState.value == RunningState.CHECKING) {
                //Prepend the entries to check the builder
                entries.addAll(getCheckerEntries().map { SnapshotPair(ListEntrySource.CHECKING_PARAMETERS, it) })
            }
            //Append the action buttons for the checker. The filter will remove them when the
            entries.add(
                SnapshotPair(ListEntrySource.CHECKING_PARAMETERS_ACTION_BUTTONS, TextMenuItem(
                    title = "Go Back",
                    onClicked = {
                        navigationNodeTraverser.cleanupBackStackDescendentsOf(FileManagerScreen::class.java)
                        navigationNodeTraverser.cleanupBackStackDescendentsOf(MultiStepOperationProgressScreen::class.java)
                        FileManagerScreen.openForBrowsing(navigationNodeTraverser)
                    }
                ))
            )
            if (builder.operationBuilt()) {
                entries.add(
                    SnapshotPair(
                        ListEntrySource.CHECKING_PARAMETERS_ACTION_BUTTONS, TextMenuItem(
                        title = "Do operation",
                        onClicked = {

                        }
                    ))
                )
            }
            //runningState changes.
        }

        with(context) {
            OneColumnSmoothScreenCustomViews(
                header  = when (runningState.value) {
                    RunningState.CHECKING -> "Check Operation Before Beginning"
                    else -> "Operation Progress"
                },
                prependGoBackEntry = false,
                items = entries.filter {
                    return@filter when (runningState.value) {
                        RunningState.CHECKING -> it.first in listOf(
                            ListEntrySource.CHECKING_PARAMETERS,
                            ListEntrySource.CHECKING_PARAMETERS_ACTION_BUTTONS
                        )
                        RunningState.RUNNING -> it.first == ListEntrySource.RUNNING
                        RunningState.RUNNING_ARF -> it.first == ListEntrySource.RUNNING_ARF
                    }
                }.map { it.second }.toDynamicLambdas()
            )
        }
    }

    private fun getCheckerEntries() : List<TextMenuItem> {
        val returnedList = mutableListOf<String>()
        if (!builder.operationBuilt()) {
            returnedList.add("Operation not built.")
        } else {
            val source = builder.getSource()!!
            val destination = builder.getDestinationFolder()!!
            val operation = builder.getOperation()

            returnedList.add("Source")
            returnedList.add("   ${source.name}")
            returnedList.add("   ${source.absolutePath}")

            returnedList.add("Operation: $operation")

            returnedList.add("Destination")
            returnedList.add("   ${destination.name}")
            returnedList.add("   ${destination.absolutePath}")
        }
        return returnedList.map {
            TextMenuItem(
                title = it,
                isSelectable = false,
                onClicked = {}
            )
        }
    }

    enum class RunningState {
        CHECKING,
        RUNNING,
        RUNNING_ARF
    }
}

enum class ListEntrySource {
    // Entries added as part of the "Check Parameters" phase
    CHECKING_PARAMETERS,
    // Action buttons added as part of the "Check Parameters"
    CHECKING_PARAMETERS_ACTION_BUTTONS,
    // Action buttons added as part of the running operation
    RUNNING,
    // Action buttons added as part of an Abort, Retry, Fail prompt
    RUNNING_ARF

}