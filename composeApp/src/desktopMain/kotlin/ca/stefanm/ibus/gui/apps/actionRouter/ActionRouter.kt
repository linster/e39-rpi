package ca.stefanm.ca.stefanm.ibus.gui.apps.actionRouter

import ca.stefanm.ca.stefanm.ibus.gui.apps.fileManager.impl.fileType.FileType
import ca.stefanm.ca.stefanm.ibus.gui.apps.fileManager.impl.fileType.MimeTools
import ca.stefanm.ca.stefanm.ibus.gui.apps.pdfViewer.PdfViewerScreen
import ca.stefanm.ca.stefanm.ibus.gui.apps.videoPlayer.VideoPlayerScreen
import ca.stefanm.ibus.gui.menu.Notification
import ca.stefanm.ibus.gui.menu.navigator.NavigationNodeTraverser
import ca.stefanm.ibus.gui.menu.notifications.NotificationHub
import java.io.File
import javax.inject.Inject

class ActionRouter @Inject constructor(
    private val mimeTools: MimeTools,
    private val notificationHub: NotificationHub,
    private val navigationNodeTraverser: NavigationNodeTraverser
) {

    fun handleFileAction(file : File, action: FileAction) {
        if (action == FileAction.VIEW) {
            viewFile(file, mimeTools.getFileTypeForFile(file))
        }
    }

    private fun viewFile(file: File, fileType: FileType) {
        when (fileType) {
            FileType.Archive -> notifyActionUnsupported()
            FileType.Audio -> notifyActionUnsupported()
            FileType.Movie -> viewMovie(file)
            FileType.Other -> notifyActionUnsupported()
            FileType.PDF -> viewPdf(file)
            FileType.Picture -> notifyActionUnsupported()
            FileType.TextFile -> notifyActionUnsupported()
            FileType.Unknown -> notifyActionUnsupported()
        }
    }
    private fun notifyActionUnsupported() {
        notificationHub.postNotificationBackground(Notification(
            Notification.NotificationImage.ALERT_TRIANGLE,
            "Action unsupported for filetype."
        ))
    }
    private fun viewPdf(file: File) {
        PdfViewerScreen.openWithFilename(navigationNodeTraverser, filename = file)
    }
    private fun viewMovie(file: File) {
        VideoPlayerScreen.openWithFile(navigationNodeTraverser, VideoPlayerScreen.VideoPlayerScreenParams(
            file, goBackOnPlaybackEnd = true
        ))
    }
}