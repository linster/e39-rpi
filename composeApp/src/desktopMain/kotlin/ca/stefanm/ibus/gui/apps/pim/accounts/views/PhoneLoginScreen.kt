package ca.stefanm.ibus.gui.pim.accounts.views

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ca.stefanm.ibus.annotations.screenflow.ScreenDoc
import ca.stefanm.ibus.autoDiscover.AutoDiscover
import ca.stefanm.ibus.gui.menu.Notification
import ca.stefanm.ibus.gui.menu.navigator.NavigationNode
import ca.stefanm.ibus.gui.menu.navigator.NavigationNodeTraverser
import ca.stefanm.ibus.gui.menu.navigator.Navigator
import ca.stefanm.ibus.gui.menu.notifications.NotificationHub
import ca.stefanm.ibus.gui.menu.widgets.BmwSingleLineHeader
import ca.stefanm.ibus.gui.menu.widgets.halveIfNotPixelDoubled
import ca.stefanm.ibus.gui.menu.widgets.screenMenu.HalfScreenMenu
import ca.stefanm.ibus.gui.menu.widgets.screenMenu.TextMenuItem
import ca.stefanm.ibus.gui.menu.widgets.themes.ThemeWrapper
import ca.stefanm.ibus.gui.pim.accounts.repo.api.AccountListRepo
import ca.stefanm.ibus.gui.pim.accounts.repo.api.PhoneLoginRelay
import ca.stefanm.ibus.gui.pim.accounts.views.parts.QrCode
import ca.stefanm.ibus.lib.logging.Logger
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * A login (Google) on the phone: a QR code for PhoneLoginRelay's page, the
 * steps, and the link as text. Goes back by itself when the login ends
 * (the accounts screen isn't listening meanwhile: this says how it ended).
 * PhoneLoginRelay.show(login) first, then open this.
 */
@ScreenDoc(
    screenName = "PhoneLoginScreen",
    description = "Accounts: a login (Google) finished on a phone through a QR code",
    navigatesTo = []
)
@ScreenDoc.AllowsGoBack
@AutoDiscover
class PhoneLoginScreen @Inject constructor(
    private val logger: Logger,
    private val navigationNodeTraverser: NavigationNodeTraverser,
    private val notificationHub: NotificationHub,
    private val relay: PhoneLoginRelay,
    private val accountListRepo: AccountListRepo,
) : NavigationNode<Nothing> {
    override val thisClass: Class<out NavigationNode<Nothing>> = this::class.java

    companion object {
        const val TAG = "PhoneLoginScreen"
    }

    override fun provideMainContent(): @Composable (incomingResult: Navigator.IncomingResult?) -> Unit = content@{
        val login = relay.shown.collectAsState().value
        if (login == null) {
            LaunchedEffect(Unit) { navigationNodeTraverser.goBack() }
            return@content
        }
        val link = remember(login.id) { relay.linkFor(login) }
        val scope = rememberCoroutineScope()

        // Ended (logged in, refused, cancelled): said so, back to the accounts.
        LaunchedEffect(login.id) {
            val result = accountListRepo.loginResults.first { it.id == login.id }
            notificationHub.postNotificationBackground(
                if (result.success) Notification(Notification.NotificationImage.MESSAGE_CIRCLE, "Logged in", "Syncing")
                else Notification(Notification.NotificationImage.ALERT_TRIANGLE, "Login not finished", result.message)
            )
            navigationNodeTraverser.goBack()
        }

        Column(
            Modifier.fillMaxSize().background(ThemeWrapper.ThemeHandle.current.colors.menuBackground)
        ) {
            BmwSingleLineHeader(login.title)
            Row(
                Modifier.fillMaxWidth().weight(1f).padding(12.dp.halveIfNotPixelDoubled()),
                horizontalArrangement = Arrangement.spacedBy(24.dp.halveIfNotPixelDoubled()),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                link.onSuccess { url -> QrCode(url, Modifier.fillMaxHeight()) }
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp.halveIfNotPixelDoubled())) {
                    link.fold(
                        onSuccess = { url ->
                            Label("Log in on your phone", FontWeight.Bold)
                            Label("1. Scan this with the phone (on the same Wi-Fi as the car).")
                            Label("2. Tap \"Open the login\" and log in.")
                            Label("3. Google's last page won't load: copy its address, go back to the first tab, paste it, tap Finish Login.")
                            Label("Or open: $url", size = 16)
                        },
                        onFailure = { e ->
                            Label("The phone page didn't start", FontWeight.Bold)
                            Label(e.message ?: e.toString())
                        },
                    )
                }
            }
            HalfScreenMenu.BottomHalfTwoColumn(
                leftItems = listOf(TextMenuItem("Go Back", onClicked = { navigationNodeTraverser.goBack() })),
                rightItems = listOf(TextMenuItem("Cancel Login", onClicked = {
                    scope.launch {
                        accountListRepo.cancelLogin(login).onFailure { e ->
                            logger.w(TAG, "Not cancelled: ${e.message}")
                            notificationHub.postNotificationBackground(Notification(
                                Notification.NotificationImage.ALERT_TRIANGLE, "Not cancelled", AccountListRepo.describe(e)))
                        }
                    }
                })),
            )
        }
    }

    @Composable
    private fun Label(text: String, weight: FontWeight = FontWeight.Normal, size: Int = 20) {
        Text(
            text = text,
            color = ThemeWrapper.ThemeHandle.current.colors.TEXT_WHITE,
            fontSize = size.sp.halveIfNotPixelDoubled(),
            fontWeight = weight,
        )
    }
}
