package ca.stefanm.ibus.gui.apps.fileManager.impl.views.parts

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ca.stefanm.ibus.gui.apps.fileManager.impl.fileType.FileType
import ca.stefanm.ibus.gui.apps.fileManager.impl.fileType.MimeTools
import ca.stefanm.ibus.gui.apps.pdfViewer.impl.LoaderUtils
import ca.stefanm.ibus.gui.menu.widgets.halveIfNotPixelDoubled
import ca.stefanm.ibus.lib.logging.Logger
import coil3.compose.AsyncImage
import com.ginsberg.cirkle.circular
import dev.nucleusframework.pdfium.PdfPage
import dev.nucleusframework.pdfium.rememberPdfReaderState
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import org.apache.commons.io.FileUtils
import java.io.File
import javax.inject.Inject
import kotlin.math.min
import kotlin.time.Duration.Companion.seconds

//A class that helps draw icons for panes to show what the file is.
class PreviewProvider @Inject constructor(
    private val loaderUtils : LoaderUtils,
    private val logger : Logger,
    private val mimeTools: MimeTools
){
    //For a PDF show a PDF page
    //For an image, draw it
    //For a movie, grab a picture of it


    @Composable
    fun FilePreview(file: File) {
        FilePreview(file, mimeTools.getFileTypeForFile(file))
    }

    @Composable
    fun FilePreview(file: File, type : FileType) {
        val fileSize = FileUtils.sizeOf(file)
        if (fileSize > 25 * 1024 * 1024) {
            NoFilePreview()
            return
        }

        if (type in listOf(
                FileType.Archive,
                FileType.Audio,
                FileType.Other,
                FileType.Movie,
                FileType.Unknown
        )) {
            NoFilePreview()
        } else {
            when (type) {
                FileType.PDF -> PdfFilePreview(file)
                FileType.Picture -> PicturePreview(file)
                FileType.TextFile -> TextPreview(file)
                else -> {}
            }
        }
    }

    @Composable
    fun EmptyFilePreview(fileType: FileType) {
        //Draw a placeholder icon
        Box(
            Modifier
                .aspectRatio(1F)
                .background(Color.Black)
        ) {

        }
    }

    @Composable
    fun NoFilePreview() {}

    @Composable
    fun PdfFilePreview(file : File) {
        //Draw a placeholder icon
        Box(
            Modifier
                .aspectRatio(1F),
            contentAlignment = Alignment.TopCenter
        ) {

            val reader = rememberPdfReaderState()
            loaderUtils.loadPdfNoThrobber(reader, file)
            if (reader.pageCount > 0) {
//                var previewPage by remember { mutableStateOf(1) }
//                var previewIndex by remember { mutableStateOf(1) }
//                val pages = remember { (1 until min(reader.pageCount, 5)).toList().circular() }
//                LaunchedEffect(Unit) {
//                    while(isActive) {
//                        delay(3.seconds)
//                        previewPage = pages[previewIndex]
//                        previewIndex += 1
//                    }
//                }
//                LaunchedEffect(previewPage) {
//                    logger.d("PdfFilePreview", "Current preview page for ${file.name} is $previewPage ; total pages is ${reader.pageCount}")
//                }
//                key(previewPage) {
                    PdfPage(
                        state = reader,
                        pageIndex = 1,
                        contentScale = ContentScale.Fit,
                        modifier = Modifier
                    )
//                }
            } else {
                LaunchedEffect(reader.pageCount) {
                    logger.w("PdfFilePreview", "Reader for $file had zero pages")
                }
            }
        }
    }

    @Composable
    fun PicturePreview(file : File) {
        AsyncImage(
            model = file,
            contentDescription = file.absolutePath
        )
    }

    @Composable
    fun TextPreview(file : File) {
        Box(
            Modifier
                .aspectRatio(2F)
                .background(Color.White)
        ) {
            Text(
                modifier = Modifier
                    .padding(5.dp.halveIfNotPixelDoubled())
                    .fillMaxSize(),
                fontSize = 10.sp,
                softWrap = false,
                text = file.readLines().take(10).reduce { acc, string -> "$acc\n$string" }
            )
        }
    }
}