package ca.stefanm.ca.stefanm.ibus.gui.apps.fileManager.impl.fileType

import ca.stefanm.ibus.di.ApplicationScope
import ca.stefanm.ibus.lib.logging.Logger
import com.drew.imaging.ImageMetadataReader
import com.drew.metadata.Metadata
import com.drew.metadata.exif.ExifSubIFDDescriptor
import com.drew.metadata.exif.ExifSubIFDDirectory
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.format
import kotlinx.datetime.format.DateTimeFormat
import kotlinx.datetime.toLocalDateTime
import org.overviewproject.mime_types.MimeTypeDetector
import java.io.File
import java.io.IOException
import javax.inject.Inject
import kotlin.time.toKotlinInstant


typealias DrewFileType = com.drew.imaging.FileType
@ApplicationScope
class MimeTools @Inject constructor(
    private val logger: Logger,
    private val mimeTypeDetector: MimeTypeDetector
)
{
    companion object {
        const val TAG = "MimeTools"
    }

    //Get FileType from File()

    fun getFileTypeForFile(file : File) : FileType {
        return matchMimeStringToFileType(file, getMimeString(file)).first
    }

    fun getFileTypeAndMetaDataForFile(file: File, allData : Boolean) : Pair<FileType, Map<String, String>> {
        val (fileType, hasMetaData) = matchMimeStringToFileType(file, getMimeString(file))
        return if (!hasMetaData) {
            fileType to emptyMap()
        } else {
            fileType to getMetadataMapForFile(file, fileType, allData)
        }

    }

    private fun getMimeString(file: File) : String? {
        if (!file.canRead()) {
            logger.d(TAG, "Cannot read file $file")
            return null
        }
        runCatching {
            mimeTypeDetector.detectMimeType(file)
        }.fold(
            onSuccess = { type -> return type },
            onFailure = { e ->
                logger.e(TAG, "Could not read mimetype", e)
                return null
            }
        )
    }

    /**
     * What file type is it, and can we get more metadata out of it?
     */
    private fun matchMimeStringToFileType(file : File, mimeString: String?) : Pair<FileType, Boolean> {
        if (mimeString == null || !file.canRead()) {
            return FileType.Unknown to false
        }

        // If it's a file that Drew Noakes' library knows about, the bool in the pair
        // is true. Early return for picture, PDF, or video, since we'll know based on his
        // short list of supported types.
        val drewGuess : Pair<FileType, Boolean>? = file.inputStream().use {
            val drewFileType : DrewFileType? = try {
                com.drew.imaging.FileTypeDetector.detectFileType(it)
            } catch (e : IOException) {
                logger.e(TAG, "Stream didn't support mark/reset on file $file", e)
                // Just because the file is weird doesn't mean we still can't make a guess
                // about it's type.
                null
            }
            return@use when (drewFileType) {
                com.drew.imaging.FileType.Unknown -> {
                    //Drew's library doesn't know about it, but can keep guessing
                    null
                }
                null -> {
                    // Null means we couldn't open the file above. Maybe try guessing before
                    // giving up.
                    null
                }
                /** Pictures that are easy to open */
                com.drew.imaging.FileType.Jpeg,
                com.drew.imaging.FileType.Tiff,
                com.drew.imaging.FileType.Png,
                com.drew.imaging.FileType.Bmp,
                com.drew.imaging.FileType.Gif,
                com.drew.imaging.FileType.Ico,
                com.drew.imaging.FileType.Pcx,
                com.drew.imaging.FileType.Riff,
                com.drew.imaging.FileType.Heif,
                com.drew.imaging.FileType.WebP -> {
                    FileType.Picture to true
                }

                /** PDFs and things that can be opened in the PDF reader */
                com.drew.imaging.FileType.Eps,
                com.drew.imaging.FileType.Pdf -> {
                    FileType.PDF to true
                }
                /** Audio that's easy to play */
                com.drew.imaging.FileType.Wav,
                com.drew.imaging.FileType.Mp3 -> {
                    FileType.Audio to true
                }
                com.drew.imaging.FileType.Ram -> {
                    FileType.Audio to false
                }


                /** Movies we can play, and show metadata about */
                com.drew.imaging.FileType.Avi,
                com.drew.imaging.FileType.QuickTime,
                com.drew.imaging.FileType.Mp4 -> {
                    FileType.Movie to true
                }
                /** Movies we can play, but don't have extra metadata about */
                com.drew.imaging.FileType.Aac,
                com.drew.imaging.FileType.Asf,
                com.drew.imaging.FileType.Flv,
                com.drew.imaging.FileType.Vob -> {
                    FileType.Movie to false
                }

                com.drew.imaging.FileType.Sit,
                com.drew.imaging.FileType.Sitx,
                com.drew.imaging.FileType.Zip -> {
                    //Drew's lib knows what type it is but can't fetch metadata on it.
                    FileType.Archive to false
                }

                /** Fancy / weird / old desktop formats from 2001 that this device won't support */
                com.drew.imaging.FileType.Swf,
                com.drew.imaging.FileType.Qxp,
                com.drew.imaging.FileType.Mxf,
                com.drew.imaging.FileType.Indd,
                com.drew.imaging.FileType.Cfbf,
                com.drew.imaging.FileType.Psd,
                com.drew.imaging.FileType.Rtf -> {
                    FileType.Other to false
                }

                /** Weird Camera RAW formats we don't care about */
                com.drew.imaging.FileType.Arw,
                com.drew.imaging.FileType.Crw,
                com.drew.imaging.FileType.Cr2,
                com.drew.imaging.FileType.Nef,
                com.drew.imaging.FileType.Orf,
                com.drew.imaging.FileType.Raf,
                com.drew.imaging.FileType.Rw2,
                com.drew.imaging.FileType.Crx -> {
                    FileType.Other to false
                }
            }
        }
        if (drewGuess != null) {
            return drewGuess
        }

        // Make a guess from the mime database.
        // Considerations include what ComposeMediaPlayer supports.

        // We want to support all formats that GStreamer supports,
        // because that's what ComposeMediaPlayer supports

        // https://gstreamer.freedesktop.org/documentation/plugin-development/advanced/media-types.html?gi-language=c#list-of-defined-types


        // Or, what if we just guessed.
        if (mimeString.startsWith("audio/")) {
            return FileType.Audio to false
        }
        if (mimeString.startsWith("video/")) {
            return FileType.Movie to false
        }
        return FileType.Other to false
    }

    // If all is false, only print common tags (EXIF for images)
    private fun getMetadataMapForFile(file : File, fileType: FileType, all : Boolean = false) : Map<String, String> {
        //https://github.com/drewnoakes/metadata-extractor/wiki/Getting-Started-(Java)

        if (fileType is FileType.Picture) {
            val metaDataResult = runCatching {
                ImageMetadataReader.readMetadata(file)
            }
            if (metaDataResult.isFailure) {
                logger.e(TAG, "Could not read metadata for $file", metaDataResult.exceptionOrNull())
            }
            val metadata = metaDataResult.getOrNull()!!

            if (all) {
                //Dump out everything
                //https://github.com/drewnoakes/metadata-extractor/wiki/Getting-Started-(Java)#print-out-all-tag-values

            }
            val returnedMap = mutableMapOf<String, String>()

            //TODO I also want the date taken, camera model

            val directory : ExifSubIFDDirectory? = metadata.getFirstDirectoryOfType(ExifSubIFDDirectory::class.java)
            if (directory == null) {
                return emptyMap()
            }

            returnedMap["Date Taken"] = directory.dateOriginal.toInstant().toKotlinInstant()
                .toLocalDateTime(TimeZone.currentSystemDefault())
                .format(LocalDateTime.Formats.ISO)

            returnedMap["Date Modified"] = directory.dateModified.toInstant().toKotlinInstant()
                .toLocalDateTime(TimeZone.currentSystemDefault())
                .format(LocalDateTime.Formats.ISO)


            val descriptor = ExifSubIFDDescriptor(directory)

            with (descriptor) {
                returnedMap["exposureTime"] = exposureTimeDescription
                returnedMap["apertureValue"] = apertureValueDescription
                returnedMap["exposureMode"] = exposureModeDescription
                returnedMap["whiteBalanceMode"] = whiteBalanceModeDescription
                returnedMap["whiteBalance"] = whiteBalanceDescription
                returnedMap["meteringMode"] = meteringModeDescription
                returnedMap["get35mmFilmEquivFocalLength"] = get35mmFilmEquivFocalLengthDescription()
                returnedMap["digitalZoomRatio"] = digitalZoomRatioDescription
                returnedMap["flash"] = flashDescription
                returnedMap["imageHeight"] = imageHeightDescription
                returnedMap["imageWidth"] = imageWidthDescription
                returnedMap.put("userComment" , userCommentDescription)
            }

            // I shoot Canon, so that's what we're supporting :D
            //https://github.com/drewnoakes/metadata-extractor/blob/main/Source/com/drew/metadata/exif/makernotes/CanonMakernoteDescriptor.java


            // Add GPS if we got it. TODO would be cool if the picture viewer can open the map!
            //https://github.com/drewnoakes/metadata-extractor/blob/main/Source/com/drew/metadata/exif/GpsDescriptor.java


        }

        if (fileType is FileType.Movie) {

        }
        return emptyMap()

    }

}