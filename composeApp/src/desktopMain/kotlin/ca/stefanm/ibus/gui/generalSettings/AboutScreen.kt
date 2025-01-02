package ca.stefanm.ibus.gui.generalSettings

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.Text
import androidx.compose.runtime.*
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
import ca.stefanm.ibus.car.pico.messageFactory.PiToPicoMessageFactory
import ca.stefanm.ibus.annotations.screenflow.ScreenDoc
import ca.stefanm.ibus.gui.menu.widgets.themes.ThemeWrapper
import ca.stefanm.ibus.autoDiscover.AutoDiscover
import ca.stefanm.ibus.configuration.ConfigurationStorage
import ca.stefanm.ibus.configuration.HmiVersion
import ca.stefanm.ibus.di.ApplicationModule
import ca.stefanm.ibus.gui.menu.Notification
import ca.stefanm.ibus.gui.menu.navigator.NavigationNode
import ca.stefanm.ibus.gui.menu.navigator.NavigationNodeTraverser
import ca.stefanm.ibus.gui.menu.navigator.Navigator
import ca.stefanm.ibus.gui.menu.notifications.NotificationHub
import ca.stefanm.ibus.gui.menu.widgets.BmwSingleLineHeader
import ca.stefanm.ibus.gui.menu.widgets.ChipItemColors
import ca.stefanm.ibus.gui.menu.widgets.halveIfNotPixelDoubled
import ca.stefanm.ibus.gui.menu.widgets.screenMenu.HalfScreenMenu
import ca.stefanm.ibus.gui.menu.widgets.screenMenu.TextMenuItem
import ca.stefanm.ibus.lib.messages.IBusMessage
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Named
import ca.stefanm.ibus.resources.Res
import ca.stefanm.ibus.resources.tux
import org.jetbrains.compose.resources.painterResource

@ScreenDoc(
    screenName = "AboutScreen",
    description = "Shows author, and version number of e39-rpi system"
)
@ScreenDoc.AllowsGoBack
@AutoDiscover
class AboutScreen @Inject constructor(
    private val navigationNodeTraverser: NavigationNodeTraverser,
    private val configurationStorage: ConfigurationStorage,
    @Named(ApplicationModule.IBUS_MESSAGE_OUTPUT_CHANNEL) val outgoingMessages : Channel<IBusMessage>,
    private val piToPicoMessageFactory: PiToPicoMessageFactory,
    private val notificationHub: NotificationHub

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
            color = ThemeWrapper.ThemeHandle.current.colors.TEXT_WHITE,
            fontSize = if (ThemeWrapper.ThemeHandle.current.isPixelDoubled) 22.sp else 11.sp,
            fontWeight = weight
        )
    }

    @Composable
    private fun aboutInfo() {

        LaunchedEffect(Unit) {
            notificationHub.postNotification(Notification(
                Notification.NotificationImage.ALERT_OCTAGON,
                "Requesting firmware version from Pico"
            ))
            outgoingMessages.send(piToPicoMessageFactory.configStatusRequest())

        }


        Box(Modifier.padding(top = 30.dp.halveIfNotPixelDoubled())) {

            Column(Modifier
                .width(200.dp.halveIfNotPixelDoubled())
                .align(Alignment.TopEnd)
                .fillMaxHeight(),
                horizontalAlignment = Alignment.CenterHorizontally)
            {
                Image(
                    painter = painterResource(Res.drawable.tux),
                    contentDescription = "Tux",
                    contentScale = ContentScale.FillWidth,
                    modifier = Modifier.fillMaxWidth(0.8f)
                )
                Text("Powered by Linux", Modifier.padding(top = 10.dp.halveIfNotPixelDoubled()),
                    textAlign = TextAlign.Center, color = Color.White, fontSize = 16.sp.halveIfNotPixelDoubled())
            }


            Column(Modifier.fillMaxHeight(0.9F).fillMaxWidth()) {
                AboutLabel("Home-brew BMW IBus Navigation System")
                AboutLabel("https://github.com/linster/e39-rpi")
                //AboutLabel("")
                AboutLabel("Written by Stefan Martynkiw")
                AboutLabel("https://stefanm.ca")
                AboutLabel("")
                AboutLabel("Version 1.0.0")
                AboutLabel("      HMI: ${configurationStorage.versionConfig[HmiVersion.hmiHash]}")
                AboutLabel("      OS : ${configurationStorage.versionConfig[HmiVersion.sdHash]}")
                AboutLabel("Firmware : ${
                    configurationStorage.fwVersionAsFlow()
                        .collectAsState(initial = configurationStorage.versionConfig[HmiVersion.fwHash]).value
                }")
            }
        }
    }
}