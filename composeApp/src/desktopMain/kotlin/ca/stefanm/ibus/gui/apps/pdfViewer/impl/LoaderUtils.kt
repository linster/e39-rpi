package ca.stefanm.ca.stefanm.ibus.gui.apps.pdfViewer.impl

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import ca.stefanm.ibus.gui.menu.Notification
import ca.stefanm.ibus.gui.menu.navigator.NavigationNodeTraverser
import ca.stefanm.ibus.gui.menu.notifications.NotificationHub
import ca.stefanm.ibus.gui.menu.widgets.modalMenu.ModalMenuService
import ca.stefanm.ibus.gui.menu.widgets.modalMenu.keyboard.Keyboard
import ca.stefanm.ibus.lib.logging.Logger
import dev.nucleusframework.pdfium.PdfError
import dev.nucleusframework.pdfium.PdfReaderState
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject
import kotlin.time.Duration.Companion.seconds

class LoaderUtils @Inject constructor(
    private val logger: Logger,
    private val notificationHub: NotificationHub,
    private val navigationNodeTraverser: NavigationNodeTraverser,
    private val modalMenuService: ModalMenuService
) {
    companion object {
        const val TAG = "PdfLoaderUtils"
    }

    //TODO use the PDF library to load the file


    @Composable
    fun loadFileBytes(fileName : File?, fileBytes: ByteArray?) : State<ByteArray?>{
        return produceState(null, fileName, fileBytes) {
            if (fileBytes != null) {
                value = fileBytes
            } else {
                runCatching {
                    fileName!!.readBytes()
                }.fold(
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
                        value = null
                    }
                )
            }
        }

    }

    @Composable
    fun loadPdf(reader : PdfReaderState, fileBytes : ByteArray) {

        val scope = rememberCoroutineScope()

        var pending by remember { mutableStateOf<ByteArray?>(null) }
        LaunchedEffect(fileBytes) { pending = fileBytes ; reader.open(fileBytes) }

        if (reader.isLoading) {
            modalMenuService.showModalWaitDialog(
                image = Notification.NotificationImage.NONE,
                throbber = true,
                headerText = "Loading Pdf",
                isCancellable = true,
                autoCloseTimeout = null,
                onCancel = {
                    navigationNodeTraverser.goBack()
                }
            )
        } else {
            modalMenuService.closeModalMenu()
        }

        when (val error = reader.error) {
            is PdfError.InvalidFormat,
            is PdfError.Io,
            is PdfError.NativeFailure -> {
                notificationHub.postNotificationBackground(
                    Notification(
                        image = Notification.NotificationImage.ALERT_TRIANGLE,
                        topText = "PDF error",
                        contentText = error.message
                    )
                )
            }
            is PdfError.PasswordRequired -> {
                if (pending != null) {
                    notificationHub.postNotificationBackground(
                        Notification(
                            topText = "PDF requires password",
                            contentText = "Enter it below",
                            duration = Notification.NotificationDuration.INDEFINITE
                        )
                    )
                    modalMenuService.showKeyboard(
                        Keyboard.KeyboardType.FULL,
                        onCloseWithoutEntry = {
                            notificationHub.clearNotification()
                            modalMenuService.closeModalMenu()
                        },
                        onTextEntered = { password ->
                            scope.launch {
                                reader.open(fileBytes, password)
                            }
                        }
                    )
                }
            }
            null -> {}
        }
    }
}