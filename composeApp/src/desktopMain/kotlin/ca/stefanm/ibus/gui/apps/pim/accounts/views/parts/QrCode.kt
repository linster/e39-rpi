package ca.stefanm.ibus.gui.pim.accounts.views.parts

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.common.BitMatrix
import com.google.zxing.qrcode.QRCodeWriter
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel

/**
 * [text] as a QR code: black on white with the quiet zone phones need (4
 * modules), square, as big as [modifier] allows (e.g. fillMaxHeight()).
 */
@Composable
fun QrCode(text: String, modifier: Modifier = Modifier) {
    val matrix: BitMatrix = remember(text) {
        QRCodeWriter().encode(text, BarcodeFormat.QR_CODE, 0, 0, mapOf(
            EncodeHintType.MARGIN to 0,
            EncodeHintType.ERROR_CORRECTION to ErrorCorrectionLevel.M,
        ))
    }
    Canvas(modifier.aspectRatio(1f)) {
        drawRect(Color.White)
        val module = size.minDimension / (matrix.width + 8)
        val origin = 4 * module
        for (y in 0 until matrix.height) {
            for (x in 0 until matrix.width) {
                if (matrix[x, y]) {
                    // A hair bigger, so neighbours leave no seams.
                    drawRect(Color.Black, Offset(origin + x * module, origin + y * module), Size(module + 0.5f, module + 0.5f))
                }
            }
        }
    }
}
