package ca.stefanm.ibus.gui.crash

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.ApplicationScope
import androidx.compose.ui.window.LocalWindowExceptionHandlerFactory
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowPlacement
import androidx.compose.ui.window.WindowPosition
import androidx.compose.ui.window.rememberWindowState
import kotlinx.coroutines.delay

/**
 * The error screen (#78): dark, where the crashed window was, with the error,
 * counting down to [HmiCrashHandler.finish]. Not themed: the theme may be what
 * crashed.
 */
object ErrorScreenWindow {
    const val TITLE = "E39 HMI Error"

    @OptIn(ExperimentalComposeUiApi::class)
    @Composable
    fun ApplicationScope.ErrorScreen(handler: HmiCrashHandler, crash: HmiCrashHandler.Crash) {
        CompositionLocalProvider(LocalWindowExceptionHandlerFactory provides handler.errorScreenExceptionHandlerFactory) {
            Window(
                title = TITLE,
                state = rememberWindowState(
                    placement = WindowPlacement.Floating,
                    position = WindowPosition(crash.bounds.x.dp, crash.bounds.y.dp),
                    size = DpSize(crash.bounds.width.dp, crash.bounds.height.dp),
                ),
                undecorated = true,
                resizable = false,
                alwaysOnTop = true,
                focusable = false,
                onCloseRequest = {},
            ) {
                Contents(handler, crash)
            }
        }
    }

    @Composable
    private fun Contents(handler: HmiCrashHandler, crash: HmiCrashHandler.Crash) {
        var secondsLeft by remember(crash) { mutableStateOf(handler.countdownSeconds) }
        LaunchedEffect(crash) {
            while (secondsLeft > 0) {
                delay(1000)
                secondsLeft--
            }
            handler.finish()
        }

        Column(
            Modifier.fillMaxSize().background(Color(0xFF101014)).padding(10.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                if (crash.then == HmiCrashHandler.Then.EXIT && crash.window == HmiCrashHandler.CrashedWindow.HMI) {
                    "The HMI crashed again"
                } else {
                    "The HMI crashed"
                },
                color = Color(0xFFFF8A80), fontSize = 16.sp, fontWeight = FontWeight.Bold
            )
            Text(
                when (crash.then) {
                    HmiCrashHandler.Then.REOPEN_HMI -> "Back to the main menu in $secondsLeft s"
                    HmiCrashHandler.Then.EXIT -> "Restarting the HMI app in $secondsLeft s"
                },
                color = Color(0xFFE0E0E0), fontSize = 12.sp
            )
            Text(crash.throwable.toString(), color = Color(0xFFE0E0E0), fontSize = 10.sp)
            Text(
                crash.throwable.stackTrace.take(6).joinToString("\n") { "  at $it" },
                color = Color(0xFF9E9E9E), fontSize = 8.sp, fontFamily = FontFamily.Monospace
            )
        }
    }
}
