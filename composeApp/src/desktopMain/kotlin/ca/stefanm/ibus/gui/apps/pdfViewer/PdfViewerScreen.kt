package ca.stefanm.ca.stefanm.ibus.gui.apps.pdfViewer

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.produceState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import ca.stefanm.ca.stefanm.ibus.gui.apps.fileManager.FilePickerScreen
import ca.stefanm.ibus.gui.menu.Notification
import ca.stefanm.ibus.gui.menu.navigator.NavigationNode
import ca.stefanm.ibus.gui.menu.navigator.NavigationNodeTraverser
import ca.stefanm.ibus.gui.menu.navigator.Navigator
import ca.stefanm.ibus.gui.menu.notifications.NotificationHub
import ca.stefanm.ibus.gui.menu.widgets.BmwSingleLineHeader
import ca.stefanm.ibus.gui.menu.widgets.modalMenu.ModalMenuService
import ca.stefanm.ibus.gui.menu.widgets.themes.ThemeWrapper
import ca.stefanm.ibus.lib.logging.Logger
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject
import kotlin.math.log
import kotlin.time.Duration.Companion.seconds

class PdfViewerScreen @Inject constructor(
    private val modalMenuService: ModalMenuService,
    private val logger: Logger,
    private val navigationNodeTraverser: NavigationNodeTraverser,
    private val notificationHub: NotificationHub
) : NavigationNode<Nothing> {

    companion object {

        const val TAG = "PdfViewerScreen"

        fun openWithFilename(navigationNodeTraverser: NavigationNodeTraverser, filename : File) {
            navigationNodeTraverser.navigateToNodeWithParameters(
                PdfViewerScreen::class.java,
                OpenParameters.FileName(filename)
            )
        }

        fun openWithByteArray(navigationNodeTraverser: NavigationNodeTraverser, fileBytes : ByteArray) {
            navigationNodeTraverser.navigateToNodeWithParameters(
                PdfViewerScreen::class.java,
                OpenParameters.Bytes(fileBytes)
            )
        }
    }

    sealed interface OpenParameters {
        data class FileName(val filename : File) : OpenParameters
        data class Bytes(val bytes : ByteArray) : OpenParameters {
            override fun equals(other: Any?): Boolean {
                if (this === other) return true
                if (javaClass != other?.javaClass) return false

                other as Bytes

                if (!bytes.contentEquals(other.bytes)) return false

                return true
            }

            override fun hashCode(): Int {
                return bytes.contentHashCode()
            }
        }
    }

    override val thisClass: Class<out NavigationNode<Nothing>>
        get() = PdfViewerScreen::class.java

    override fun provideMainContent(): @Composable ((Navigator.IncomingResult?) -> Unit) = content@ { params ->


            if (params == null) {
                InstructionalPage()
                return@content
            } else {
                val fileName =
                    if (params.resultFrom == FilePickerScreen::class.java && params.result is FilePickerScreen.Companion.FilePickerResult) {
                        when (val incoming = params.result) {
                            is FilePickerScreen.Companion.FilePickerResult.FileChosen -> incoming.file
                            FilePickerScreen.Companion.FilePickerResult.NoFileChosen -> null
                            else -> null
                        }
                    } else {
                        if (params.requestParameters is OpenParameters.FileName) {
                            params.requestParameters.filename
                        } else {
                            null
                        }
                    }

                val bytesFromParameters = if (params.requestParameters is OpenParameters.Bytes) {
                    params.requestParameters.bytes
                } else {
                    null
                }

                if (bytesFromParameters != null) {
                    PdfViewer(bytesFromParameters)
                    return@content
                }

                if (fileName == null) {
                    InstructionalPage()
                    return@content
                } else {

                    val filebytes: State<ByteArray?> = produceState(null, fileName) {
                        loadFileBytes(fileName).fold(
                            onSuccess = {
                                value = it
                            },
                            onFailure = {
                                logger.e(TAG, "Could not open file $fileName", it)
                                notificationHub.postNotificationBackground(
                                    Notification(
                                        topText = "Could not open ${fileName}",
                                        contentText = "${it.message}"
                                    )
                                )
                                delay(5.seconds)
                                navigationNodeTraverser.goBack()
                            }
                        )
                    }
                    filebytes.value?.let { PdfViewer(it) }
                }

            }

    }

    @Composable
    fun InstructionalPage() {
        Column(
            Modifier.background(ThemeWrapper.ThemeHandle.current.colors.menuBackground)
                .fillMaxSize()
        ){
            BmwSingleLineHeader("PDF Viewer -- Instructions")

        }
    }

    @Composable
    fun PromptToSelectFile() {
        Column(
            Modifier.background(ThemeWrapper.ThemeHandle.current.colors.menuBackground)
                .fillMaxSize()
        ) {
            BmwSingleLineHeader("PDF Viewer -- Instructions")

        }
    }

    suspend fun loadFileBytes(
        filename: File
    ) : Result<ByteArray> {
        return runCatching {
            filename.readBytes()
        }
    }

    @Composable
    fun PdfViewer(contents : ByteArray) {
        Column(
            Modifier.background(ThemeWrapper.ThemeHandle.current.colors.menuBackground)
        ) {

        }
    }
}