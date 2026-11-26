package ca.stefanm.ibus.gui.menu.widgets.themes.designer

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.VerticalScrollbar
import androidx.compose.foundation.rememberScrollbarAdapter
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Slider
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.WindowScope
import ca.stefanm.ibus.gui.debug.windows.CheckBoxWithLabel
import ca.stefanm.ibus.gui.debug.windows.NestingCard
import ca.stefanm.ibus.gui.debug.windows.NestingCardHeader
import ca.stefanm.ibus.gui.menu.navigator.WindowManager
import ca.stefanm.ibus.gui.menu.widgets.themes.ThemeConfigurationStorage
import ca.stefanm.ibus.gui.menu.widgets.themes.Themes
import ca.stefanm.ibus.di.ApplicationScope
import javax.inject.Inject

/**
 * Build a theme's colours: generate them from one base colour with a preset
 * (BMW Blue's spacing for lighter themes, Goose Green's for darker ones),
 * then change any of them by hand, locking the ones a regenerate must keep.
 * The HMI window can show the theme live while it's edited.
 */
@ApplicationScope
class ThemeDesignerDebugWindow @Inject constructor(
    private val themeConfigurationStorage: ThemeConfigurationStorage,
) : WindowManager.E39Window {

    override val title: String = "Theme Designer"
    override val size = DpSize(1000.dp, 900.dp)
    override val tag = this
    override val defaultPosition = WindowManager.E39Window.DefaultPosition.ANYWHERE

    /** Kept while the app runs, so closing the window doesn't lose the work. */
    private val state = ThemeDesignerState(themeConfigurationStorage.getStoredTheme())
    private var previewOnHmi by mutableStateOf(false)

    override fun content(): @Composable WindowScope.() -> Unit = {
        LaunchedEffect(previewOnHmi, state.theme()) {
            themeConfigurationStorage.setPreviewTheme(if (previewOnHmi) state.theme() else null)
        }
        // Closing the window ends the preview.
        DisposableEffect(Unit) {
            onDispose { themeConfigurationStorage.setPreviewTheme(null) }
        }

        val scrollState = rememberScrollState()
        Box(Modifier.fillMaxSize()) {
            // Room on the right for the scrollbar.
            Column(Modifier.verticalScroll(scrollState).padding(start = 8.dp, top = 8.dp, bottom = 8.dp, end = 20.dp)) {
                Row {
                    Column(Modifier.weight(1F)) {
                        StartFromCard()
                        PreviewCard()
                    }
                    Column(Modifier.weight(1F)) {
                        ContrastCard()
                    }
                }
                GenerateCard()
                ColorsCard()
            }
            VerticalScrollbar(
                adapter = rememberScrollbarAdapter(scrollState),
                modifier = Modifier.align(Alignment.CenterEnd).fillMaxHeight(),
            )
        }
    }

    @Composable
    private fun StartFromCard() {
        NestingCard {
            NestingCardHeader("Start from a theme")
            Themes.availableThemes.filter { it.isPixelDoubled }.forEach { theme ->
                Button(onClick = { state.load(theme) }) {
                    Text(theme.friendlyName.removeSuffix(" (2X)"))
                }
            }
        }
    }

    @Composable
    private fun PreviewCard() {
        NestingCard {
            NestingCardHeader("Preview")
            CheckBoxWithLabel(previewOnHmi, { previewOnHmi = it }, "Show on the HMI window (not saved)")
            CheckBoxWithLabel(state.pixelDoubled, { state.pixelDoubled = it }, "2X sizes (800x468); off: 1X (400x234)")
        }
    }

    @Composable
    private fun ContrastCard() {
        NestingCard {
            NestingCardHeader("Contrast (WCAG, text needs ${ThemeColorMath.MIN_CONTRAST}:1)")
            state.contrasts().forEach { c ->
                Text(
                    text = "${if (c.isTooLow) "⚠ " else "✓ "}${c.foreground.label} on ${c.background.label}: " +
                            "%.1f:1".format(c.ratio),
                    color = if (c.isTooLow) Color(0xFFB00020) else Color.Unspecified,
                    fontWeight = if (c.isTooLow) FontWeight.Bold else FontWeight.Normal,
                )
            }
        }
    }

    @Composable
    private fun GenerateCard() {
        NestingCard(Modifier.fillMaxWidth()) {
            NestingCardHeader("Generate from a base colour (the menu background)")
            Text("Generating replaces every colour that isn't locked. Change any of them below afterwards.")
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Preset:", Modifier.padding(end = 8.dp))
                ThemeGenerator.presets.forEach { preset ->
                    val chosen = preset == state.preset
                    Button(
                        onClick = { state.changePreset(preset) },
                        modifier = Modifier.padding(end = 8.dp),
                        colors = if (chosen) ButtonDefaults.buttonColors() else ButtonDefaults.outlinedButtonColors(),
                    ) { Text(preset.name) }
                }
                Button(onClick = { state.generate() }) { Text("Generate") }
            }
            ColorEditor(
                label = "Base colour",
                color = state.base,
                onColorChanged = { state.changeBase(it) },
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Lightness shift: %+.2f".format(state.lightnessShift), Modifier.width(180.dp))
                Slider(
                    value = state.lightnessShift,
                    onValueChange = { state.changeLightnessShift(it) },
                    valueRange = -0.2F..0.2F,
                    modifier = Modifier.weight(1F),
                )
                Button(onClick = { state.changeLightnessShift(0F) }) { Text("0") }
            }
        }
    }

    @Composable
    private fun ColorsCard() {
        NestingCard(Modifier.fillMaxWidth()) {
            NestingCardHeader("Colours")
            ThemeColorRole.entries.forEach { role ->
                Row(verticalAlignment = Alignment.CenterVertically) {
                    CheckBoxWithLabel(
                        role in state.locked,
                        { state.setLocked(role, it) },
                        "Lock",
                    )
                    ColorEditor(
                        label = role.label,
                        description = role.description,
                        color = state.colors.getValue(role),
                        onColorChanged = { state.setColor(role, it) },
                    )
                }
            }
        }
    }

    /** A swatch, a hex box and H/S/L sliders, which follow each other. */
    @Composable
    private fun ColorEditor(
        label: String,
        color: Color,
        onColorChanged: (Color) -> Unit,
        description: String? = null,
    ) {
        val hsl = Hsl.of(color)
        Row(
            Modifier.padding(vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Column(Modifier.width(200.dp)) {
                Text(label, fontWeight = FontWeight.Bold)
                if (description != null) Text(description)
            }
            Box(Modifier.size(48.dp).background(color).border(1.dp, Color.Black))
            HexField(color, onColorChanged)
            Column(Modifier.weight(1F)) {
                HslSlider("H", hsl.h, 0F..360F, "%.0f") { onColorChanged(hsl.copy(h = it).toColor()) }
                HslSlider("S", hsl.s, 0F..1F, "%.2f") { onColorChanged(hsl.copy(s = it).toColor()) }
                HslSlider("L", hsl.l, 0F..1F, "%.2f") { onColorChanged(hsl.copy(l = it).toColor()) }
            }
        }
    }

    /** Takes a colour once the text is a whole hex colour; follows outside changes. */
    @Composable
    private fun HexField(color: Color, onColorChanged: (Color) -> Unit) {
        val hex = ThemeColorMath.toHex(color)
        var text by remember(hex) { mutableStateOf(hex) }
        OutlinedTextField(
            value = text,
            onValueChange = { new ->
                text = new
                ThemeColorMath.parseHex(new)?.let(onColorChanged)
            },
            isError = ThemeColorMath.parseHex(text) == null,
            singleLine = true,
            textStyle = androidx.compose.ui.text.TextStyle(fontFamily = FontFamily.Monospace),
            modifier = Modifier.width(150.dp),
        )
    }

    @Composable
    private fun HslSlider(name: String, value: Float, range: ClosedFloatingPointRange<Float>, format: String, onChange: (Float) -> Unit) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("$name ${format.format(value)}", Modifier.width(64.dp), fontFamily = FontFamily.Monospace)
            Slider(value = value, onValueChange = onChange, valueRange = range, modifier = Modifier.weight(1F))
        }
    }
}
