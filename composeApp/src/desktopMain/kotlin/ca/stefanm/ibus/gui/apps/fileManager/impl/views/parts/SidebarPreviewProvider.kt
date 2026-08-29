package ca.stefanm.ca.stefanm.ibus.gui.apps.fileManager.impl.views.parts

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ca.stefanm.ca.stefanm.ibus.gui.apps.fileManager.impl.fileType.FileType
import ca.stefanm.ca.stefanm.ibus.gui.apps.fileManager.impl.fileType.MimeTools
import ca.stefanm.ibus.gui.menu.widgets.halveIfNotPixelDoubled
import coil3.compose.AsyncImage
import org.apache.commons.io.FileUtils
import java.io.File
import javax.inject.Inject

//A class that helps draw icons for panes to show what the file is.
class SidebarPreviewProvider @Inject constructor(
){
    //For a PDF show a PDF page
    //For an image, draw it
    //For a movie, grab a picture of it

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
            Column(
                Modifier
                    .fillMaxWidth()
                    .height(130.dp.halveIfNotPixelDoubled()),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                when (type) {
                    FileType.PDF -> PdfFilePreview(file)
                    FileType.Picture -> PicturePreview(file)
                    FileType.TextFile -> TextPreview(file)
                    else -> {}
                }
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
                .aspectRatio(1F)
                .background(Color.Black)
        ) {}
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