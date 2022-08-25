package ca.stefanm.ibus.gui.generalSettings

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.loadImageBitmap
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ca.stefanm.ibus.gui.menu.widgets.themes.ThemeWrapper
import ca.stefanm.ibus.autoDiscover.AutoDiscover
import ca.stefanm.ibus.configuration.ConfigurationStorage
import ca.stefanm.ibus.configuration.HmiVersion
import ca.stefanm.ibus.gui.menu.navigator.NavigationNode
import ca.stefanm.ibus.gui.menu.navigator.NavigationNodeTraverser
import ca.stefanm.ibus.gui.menu.navigator.Navigator
import ca.stefanm.ibus.gui.menu.widgets.BmwSingleLineHeader
import ca.stefanm.ibus.gui.menu.widgets.ChipItemColors
import ca.stefanm.ibus.gui.menu.widgets.halveIfNotPixelDoubled
import ca.stefanm.ibus.gui.menu.widgets.screenMenu.HalfScreenMenu
import ca.stefanm.ibus.gui.menu.widgets.screenMenu.TextMenuItem
import javax.inject.Inject

@AutoDiscover
class AboutScreen @Inject constructor(
    private val navigationNodeTraverser: NavigationNodeTraverser,
    private val configurationStorage: ConfigurationStorage
) : NavigationNode<Nothing> {

    override val thisClass: Class<out NavigationNode<Nothing>>
        get() = AboutScreen::class.java

    override fun provideMainContent(): @Composable (incomingResult: Navigator.IncomingResult?) -> Unit = {

        Column(
            Modifier.background(ThemeWrapper.ThemeHandle.current.colors.menuBackground),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            BmwSingleLineHeader("About E39-rpi")

            Box(
                Modifier.wrapContentHeight()
                    .align(Alignment.CenterHorizontally)
                    .weight(0.9F)
                    .fillMaxWidth(0.9F)
            ) {
                aboutInfo()
            }

            HalfScreenMenu.OneColumn(
                fullWidth = false,
                items = listOf(
                    TextMenuItem(
                        title = "Go Back",
                        onClicked = {navigationNodeTraverser.goBack()}
                    )
                )
            )
        }


    }

    @Composable
    private fun AboutLabel(text : String, weight : FontWeight = FontWeight.Normal) {
        Text(
            text = text,
            color = ChipItemColors.TEXT_WHITE,
            fontSize = if (ThemeWrapper.ThemeHandle.current.isPixelDoubled) 22.sp else 11.sp,
            fontWeight = weight
        )
    }

    @Composable
    private fun aboutInfo() {
        Box(Modifier.padding(top = 30.dp.halveIfNotPixelDoubled())) {

            Column(Modifier
                .width(200.dp.halveIfNotPixelDoubled())
                .align(Alignment.TopEnd)
                .fillMaxHeight(),
                horizontalAlignment = Alignment.CenterHorizontally)
            {
                Image(
                    painter = painterResource("tux.png"),
                    contentDescription = "Tux",
                    contentScale = ContentScale.FillWidth,
                    modifier = Modifier.fillMaxWidth(0.8f)
                )
                Text("Powered by Linux", Modifier.padding(top = 10.dp.halveIfNotPixelDoubled()), textAlign = TextAlign.Center, color = Color.White)
            }


            Column(Modifier.fillMaxHeight(0.9F).fillMaxWidth()) {
                AboutLabel("Home-brew BMW IBus Navigation System")
                AboutLabel("https://github.com/linster/e39-rpi")
                //AboutLabel("")
                AboutLabel("Written by Stefan Martynkiw")
                AboutLabel("https://stefanm.ca")
                AboutLabel("")
                AboutLabel("Version 1.0.0")
                AboutLabel("     HMI: ${configurationStorage.versionConfig[HmiVersion.hmiHash]}")
                AboutLabel("     OS : ${configurationStorage.versionConfig[HmiVersion.sdHash]}")
            }
        }
    }
}