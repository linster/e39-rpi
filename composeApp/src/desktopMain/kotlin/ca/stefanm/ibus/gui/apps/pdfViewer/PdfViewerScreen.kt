package ca.stefanm.ca.stefanm.ibus.gui.apps.pdfViewer

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import ca.stefanm.ca.stefanm.ibus.gui.apps.fileManager.FilePickerScreen
import ca.stefanm.ca.stefanm.ibus.gui.apps.pdfViewer.PdfPageSelectorScreen.PageSelectorResult
import ca.stefanm.ca.stefanm.ibus.gui.apps.pdfViewer.impl.LoaderUtils
import ca.stefanm.ibus.di.ApplicationModule
import ca.stefanm.ibus.gui.menu.Notification
import ca.stefanm.ibus.gui.menu.navigator.NavigationNode
import ca.stefanm.ibus.gui.menu.navigator.NavigationNodeTraverser
import ca.stefanm.ibus.gui.menu.navigator.Navigator
import ca.stefanm.ibus.gui.menu.notifications.NotificationHub
import ca.stefanm.ibus.gui.menu.widgets.BmwSingleLineHeader
import ca.stefanm.ibus.gui.menu.widgets.CenterGradientWithEdgeHighlight
import ca.stefanm.ibus.gui.menu.widgets.ItemChipOrientation
import ca.stefanm.ibus.gui.menu.widgets.MenuItem
import ca.stefanm.ibus.gui.menu.widgets.knobListener.KnobListenerService
import ca.stefanm.ibus.gui.menu.widgets.knobListener.dynamic.KnobObserverBuilder
import ca.stefanm.ibus.gui.menu.widgets.knobListener.dynamic.KnobObserverBuilderState
import ca.stefanm.ibus.gui.menu.widgets.modalMenu.ModalMenuService
import ca.stefanm.ibus.gui.menu.widgets.screenMenu.TextMenuItem
import ca.stefanm.ibus.gui.menu.widgets.themes.ThemeWrapper
import ca.stefanm.ibus.lib.logging.Logger
import dev.nucleusframework.pdfium.PdfReaderState
import dev.nucleusframework.pdfium.rememberPdfReaderState
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject
import javax.inject.Named
import kotlin.math.log
import kotlin.time.Duration.Companion.seconds

class PdfViewerScreen @Inject constructor(
    @Named(ApplicationModule.KNOB_LISTENER_MAIN)
    private val knobListenerService: KnobListenerService,

    private val modalMenuService: ModalMenuService,
    private val logger: Logger,
    private val navigationNodeTraverser: NavigationNodeTraverser,
    private val notificationHub: NotificationHub,
    private val loaderUtils: LoaderUtils
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
            logger.d(TAG, "params were null")
            InstructionalPage()
            return@content
        }


        val fileName =
            if (params.resultFrom == FilePickerScreen::class.java && params.result is FilePickerScreen.Companion.FilePickerResult) {
                when (val incoming = params.result) {
                    is FilePickerScreen.Companion.FilePickerResult.FileChosen -> incoming.file
                    FilePickerScreen.Companion.FilePickerResult.NoFileChosen -> null
                    else -> null
                }
            } else if (params.resultFrom == PdfPageSelectorScreen::class.java && params.result is PdfPageSelectorScreen.PageSelectorResult) {
                params.result.fileName
            } else {
                if (params.requestParameters is OpenParameters.FileName) {
                    params.requestParameters.filename
                } else {
                    null
                }
            }

        val bytesFromParameters = if (params.requestParameters is OpenParameters.Bytes) {
            params.requestParameters.bytes
        } else if (params.resultFrom == PdfPageSelectorScreen::class.java && params.result is PdfPageSelectorScreen.PageSelectorResult) {
            params.result.fileBytes
        } else {
            null
        }

        if (fileName == null && bytesFromParameters == null) {
            InstructionalPage()
            return@content
        }


        val fileBytes = loaderUtils.loadFileBytes(fileName, bytesFromParameters)

        val reader = rememberPdfReaderState()
        fileBytes.value?.let {
            loaderUtils.loadPdf(reader, it)
        }

        PdfViewer(
            reader = reader,
            fileName = fileName,
            pageSelectorResult = if (
                params.resultFrom == PdfPageSelectorScreen::class.java &&
                params.result is PdfPageSelectorScreen.PageSelectorResult
                ) {
                params.result
            } else {
                null
            },
            requestSelectPage = {
                PdfPageSelectorScreen.openWithByteArray(
                    navigationNodeTraverser,
                    fileName,
                    fileBytes.value!!
                )
            })

//                LaunchedEffect(fileBytes) {
//                    fileBytes.value?.let {
//                        if (params.requestParameters is OpenParameters) {
//                            PdfPageSelectorScreen.openWithByteArray(
//                                navigationNodeTraverser,
//                                fileName,
//                                it,
//                                replayParams = params.requestParameters
//                            )
//                        }
//                    }
//                }


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



    @Composable
    fun PdfViewer(
        reader : PdfReaderState,
        fileName : File?,
        pageSelectorResult: PdfPageSelectorScreen.PageSelectorResult? = null,
        requestSelectPage: () -> Unit
    ) {
        val knobState = KnobObserverBuilderState.setupListener(
            knobListenerService = knobListenerService,
            logger,
            TAG
        )
        Column(
            Modifier
                .fillMaxSize()
                .background(ThemeWrapper.ThemeHandle.current.colors.menuBackground)
        ) {

            HeaderBar(
                reader, fileName, 3,
            )


            TopMenuBar(reader = reader,
                knobState = knobState,
                requestSelectPage = { requestSelectPage() }
            )


        }
    }

    @Composable
    fun HeaderBar(
        reader: PdfReaderState,
        fileName : File?,
        currentPage : Int?,
    ) {
        CenterGradientWithEdgeHighlight {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "PDF Viewer : ${fileName?.absolutePath}".uppercase(),
                    fontSize = ThemeWrapper.ThemeHandle.current.hmiHeaderFooter.fontSize,
                    fontWeight = FontWeight.Bold,
                    color = ThemeWrapper.ThemeHandle.current.hmiHeaderFooter.fontColor
                )

                if (currentPage != null) {
                    Text(
                        text = "Page $currentPage / ${reader.pageCount}".uppercase(),
                        fontSize = ThemeWrapper.ThemeHandle.current.hmiHeaderFooter.fontSize,
                        fontWeight = FontWeight.Bold,
                        color = ThemeWrapper.ThemeHandle.current.hmiHeaderFooter.fontColor
                    )
                }
            }
        }

    }

    @Composable
    fun TopMenuBar(
        reader: PdfReaderState,
        knobState : KnobObserverBuilderState,
        requestSelectPage : () -> Unit,
    ) {
        // {Close} {Select Page} { Fit : { Width } { Height } { Page } } { Zoom }
        Row(
            modifier = Modifier
                .background(ThemeWrapper.ThemeHandle.current.colors.menuBackground)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {

            KnobObserverBuilder(knobState) { allocatedIndex, currentIndex ->
                MenuItem(
                    boxModifier = Modifier.weight(1F, true),
                    label = "Close",
                    isSmallSize = true,
                    chipOrientation = ItemChipOrientation.N,
                    isSelected = currentIndex == allocatedIndex,
                    onClicked = CallWhen(currentIndexIs = allocatedIndex) {
                        navigationNodeTraverser.navigateToRoot()
                    }
                )
            }

            KnobObserverBuilder(knobState) { allocatedIndex, currentIndex ->
                MenuItem(
                    boxModifier = Modifier.weight(1F, true),
                    label = "Select Page...",
                    isSmallSize = true,
                    chipOrientation = ItemChipOrientation.N,
                    isSelected = currentIndex == allocatedIndex,
                    onClicked = CallWhen(currentIndexIs = allocatedIndex) {
                        requestSelectPage()
                    }
                )
            }

            val measurements = ThemeWrapper.ThemeHandle.current.bigItem

//            Row(Modifier.weight(2F)) {
//                KnobObserverBuilder(knobState) { allocatedIndex: Int, currentIndex: Int ->
//                    MenuItem(
//                        boxModifier = Modifier.weight(1f, fill = true),
//                        label = "Zoom (${"%.2f".format(rowHeight)}) ...",
//                        isSmallSize = true,
//                        chipOrientation = ItemChipOrientation.N,
//                        isSelected = allocatedIndex == currentIndex,
//                        onClicked = CallWhen(currentIndexIs = allocatedIndex) {
//                            modalMenuService.showFloatSlider(
//                                currentValue = flowOf(rowHeight),
//                                initialValue = rowHeight,
//                                validItems = 0.3F .. 1.1F,
//                                step = 0.1F,
//                                onCurrentValueChanged = {
//                                    onChangeRowHeightFraction(it)
//                                },
//                                hintText = "Zoom"
//                            )
//                        }
//                    )
//                }
//                KnobObserverBuilder(knobState) { allocatedIndex: Int, currentIndex: Int ->
//                    MenuItem(
//                        boxModifier = Modifier.weight(1f, fill = true),
//                        label = "Aspect (${"%.2f".format(aspectRatio)}) ...",
//                        isSmallSize = true,
//                        chipOrientation = ItemChipOrientation.N,
//                        isSelected = allocatedIndex == currentIndex,
//                        onClicked = CallWhen(currentIndexIs = allocatedIndex) {
//                            modalMenuService.showFloatSlider(
//                                hintText = "Aspect",
//                                initialValue = aspectRatio,
//                                validItems = 0.5F .. 2.0F,
//                                step = 0.1F,
//                                onCurrentValueChanged = {
//                                    onChangeDesiredItemAspectRatio(it)
//                                },
//                                currentValue = flowOf(aspectRatio)
//                            )
//                        }
//                    )
//                }
//
//            }

        }

    }
}