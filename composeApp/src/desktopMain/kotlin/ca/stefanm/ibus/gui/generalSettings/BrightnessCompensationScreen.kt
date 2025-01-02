package ca.stefanm.ibus.gui.generalSettings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.colorspace.ColorSpaces
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ca.stefanm.ibus.annotations.screenflow.ScreenDoc
import ca.stefanm.ibus.gui.menu.widgets.themes.ThemeWrapper
import ca.stefanm.ibus.autoDiscover.AutoDiscover
import ca.stefanm.ibus.car.bordmonitor.input.InputEvent
import ca.stefanm.ibus.configuration.ConfigurationStorage
import ca.stefanm.ibus.configuration.E39Config
import ca.stefanm.ibus.gui.menu.navigator.NavigationNode
import ca.stefanm.ibus.gui.menu.navigator.NavigationNodeTraverser
import ca.stefanm.ibus.gui.menu.navigator.Navigator
import ca.stefanm.ibus.gui.menu.widgets.BmwSingleLineHeader
import ca.stefanm.ibus.gui.menu.widgets.ChipItemColors
import ca.stefanm.ibus.gui.menu.widgets.halveIfNotPixelDoubled
import ca.stefanm.ibus.gui.menu.widgets.knobListener.KnobListenerService
import ca.stefanm.ibus.gui.menu.widgets.screenMenu.HalfScreenMenu
import ca.stefanm.ibus.gui.menu.widgets.screenMenu.TextMenuItem
import ca.stefanm.ibus.lib.logging.Logger
import kotlinx.coroutines.ExperimentalCoroutinesApi
import javax.inject.Inject

@ScreenDoc(
    screenName = "BrightnessCompensationScreen",
    description = "A screen to configure a transparent black overlay over the HMI so that failing BMBT displays " +
            "don't show a green tint on brighter colors (and potentially lose sync)."
)
@ScreenDoc.AllowsGoBack
@ScreenDoc.OpensSubScreen("introductionScreen")
@ScreenDoc.OpensSubScreen("brightnessTestingScreen")
@AutoDiscover
class BrightnessCompensationScreen @Inject constructor(
    private val navigationNodeTraverser: NavigationNodeTraverser,
    private val configurationStorage: ConfigurationStorage,
    private val knobListenerService: KnobListenerService,
    private val logger : Logger,
) : NavigationNode<Nothing>{

    override val thisClass: Class<out NavigationNode<Nothing>>
        get() = BrightnessCompensationScreen::class.java

    enum class ViewState { INTRO, ADJUSTMENT }
    override fun provideMainContent(): @Composable (incomingResult: Navigator.IncomingResult?) -> Unit = {

        val viewState = remember { mutableStateOf(ViewState.INTRO)}

        when (viewState.value) {
            ViewState.INTRO -> introductionScreen { viewState.value = it }
            ViewState.ADJUSTMENT -> brightnessTestingScreen(
                onNewTint = {
                    configurationStorage.setBrightnessCompensation(it)
                },
                onViewStateChanged = {
                    viewState.value = it
                }
            )
        }
    }


    @Composable
    private fun InfoLabel(text : String, weight : FontWeight = FontWeight.Normal) {
        Text(
            text = text,
            color = ThemeWrapper.ThemeHandle.current.colors.TEXT_WHITE,
            fontSize = 22.sp.halveIfNotPixelDoubled(),
            fontWeight = weight,
            modifier = Modifier.padding(start = 10.dp.halveIfNotPixelDoubled())
        )
    }

    @ScreenDoc.SubScreen(
        screenName = "introductionScreen",
        paneDescription = "Shows an intro message for the screen adjust."
    )
    @ScreenDoc.SubScreen.AllowsCloseParent
    @ScreenDoc.SubScreen.NavigateToSubscreen("brightnessTestingScreen")
    @Composable
    fun introductionScreen(
        onViewStateChanged : (new : ViewState) -> Unit
    ) {
        Column(Modifier
            .background(ThemeWrapper.ThemeHandle.current.colors.menuBackground)
            .fillMaxSize()
        ) {
            BmwSingleLineHeader("Brightness Adjustment")

            """
                This lets you adjust the HMI brightness on your BMBT.
                
                Failing Bordmonitor circuitry causes a green-tint on the display
                in high-heat conditions. With the RPi VGA -> RGsB circuitry, this
                can cause a loss of video sync, making the display illegible.
                
                This screen allows you to darken the HMI output. Turn the dial 
                and click until the screen is no longer washed out.
            """.trimIndent().split('\n').forEach { InfoLabel(it) }

            HalfScreenMenu.OneColumn(listOf(
                TextMenuItem(
                    title = "Go back",
                    onClicked = { navigationNodeTraverser.goBack() }
                ),
                TextMenuItem(
                    title = "Calibrate Brightness",
                    onClicked = {
                        onViewStateChanged(ViewState.ADJUSTMENT)
                    }
                )
            ))

        }
    }

    @ScreenDoc.SubScreen(
        screenName = "brightnessTestingScreen",
        paneDescription = "Captures the knob rotations to change a blank white screen to a shade of grey."
    )
    @ScreenDoc.SubScreen.NavigateToSubscreen("introductionScreen")
    @OptIn(ExperimentalCoroutinesApi::class)
    @Composable
    fun brightnessTestingScreen(
        onNewTint : (new : Float) -> Unit,
        onViewStateChanged : (new : ViewState) -> Unit,
    ) {
        val alpha = remember { mutableStateOf(
            configurationStorage.config[E39Config.WindowManagerConfig.brightnessCompensation]
        ) }
        val tintBox = remember(alpha.value) {
            derivedStateOf { mutableStateOf(Color(0F, 0F, 0F, alpha.value, ColorSpaces.Srgb)) }
        }

        LaunchedEffect(alpha) {
            logger.d("BRIGHTNESS", "Current alpha is ${alpha.value}")
        }

        Column(Modifier.fillMaxSize().background(Color.White)) {
            Box(Modifier.fillMaxSize().background(tintBox.value.value)) {
                Text(text = "Current Tint: ${alpha.value}", color = Color.Black, fontSize = 24.sp.halveIfNotPixelDoubled())
            }

            LaunchedEffect(Unit) {
                knobListenerService.knobTurnEvents().collect { event ->
                    if (event is InputEvent.NavKnobTurned) {
                        val delta = 0.05F

                        val newTint = alpha.value + (event.clicks *
                                        delta *
                                        if (event.direction == InputEvent.NavKnobTurned.Direction.RIGHT) 1F else -1F
                                    )

                        alpha.value = newTint.coerceIn(0.0F, 1.0F)
                    }
                    if (event is InputEvent.NavKnobPressed) {
                        onNewTint(alpha.value)
                        onViewStateChanged(ViewState.INTRO)
                    }
                }
            }
        }
    }

}