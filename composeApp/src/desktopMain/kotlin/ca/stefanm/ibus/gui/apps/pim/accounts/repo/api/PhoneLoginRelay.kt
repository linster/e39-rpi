package ca.stefanm.ibus.gui.pim.accounts.repo.api

import ca.stefanm.ibus.configuration.ConfigurationStorage
import ca.stefanm.ibus.configuration.E39Config
import ca.stefanm.ibus.di.ApplicationScope
import ca.stefanm.ibus.gui.pim.calendar.repo.backend.AccountRepository
import ca.stefanm.ibus.gui.pim.calendar.repo.backend.PendingLogin
import ca.stefanm.ibus.gui.pim.calendar.repo.backend.PimBackendException
import ca.stefanm.ibus.lib.logging.Logger
import com.sun.net.httpserver.HttpExchange
import com.sun.net.httpserver.HttpServer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.net.Inet4Address
import java.net.InetSocketAddress
import java.net.NetworkInterface
import java.net.URLDecoder
import java.security.SecureRandom
import java.util.concurrent.Executors
import javax.inject.Inject

/**
 * Logging in to an account (Google) on a phone, as there's no browser in
 * the car. The HMI shows a QR code for a page it serves on the car's
 * network ([linkFor]); the page opens the login on the phone. Google's last
 * page goes to the bridge's 127.0.0.1 listener, which on the phone is the
 * phone, so it doesn't load: the user copies its address and pastes it on
 * this page, which hands it to [AccountRepository.finishLogin].
 *
 * Google's short-code login (google.com/device) can't be used instead: it
 * doesn't allow calendar, contacts or mail. Each login gets a random link,
 * valid while it waits; only that login's own 127.0.0.1 address is
 * accepted.
 */
@ApplicationScope
class PhoneLoginRelay @Inject constructor(
    private val accounts: AccountRepository,
    private val configurationStorage: ConfigurationStorage,
    private val logger: Logger,
) {
    companion object {
        const val TAG = "PhoneLoginRelay"
        private const val PATH = "/login/"
        private val TOKEN_CHARS = "abcdefghjkmnpqrstuvwxyz23456789".toCharArray()

        /** This computer's address on the Wi-Fi/LAN (not containers, VPNs). */
        fun lanAddress(): String? = NetworkInterface.getNetworkInterfaces().toList()
            .filter { it.isUp && !it.isLoopback && !it.isVirtual && !it.isPointToPoint }
            .filterNot { n -> listOf("docker", "podman", "veth", "br-", "virbr", "cni", "tun", "tap", "wg", "lxc").any { n.name.startsWith(it) } }
            .sortedBy { n -> if (n.name.startsWith("wl")) 0 else 1 }
            .flatMap { n -> n.inetAddresses.toList() }
            .firstOrNull { it is Inet4Address && it.isSiteLocalAddress }
            ?.hostAddress

        private fun html(s: String) = s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace("\"", "&quot;")
    }

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private val random = SecureRandom()
    private val lock = Any()
    private var server: HttpServer? = null
    private var base: String = ""
    /** Link token → the login (and its link's token by login id). */
    private val logins = mutableMapOf<String, PendingLogin>()

    private val _shown = MutableStateFlow<PendingLogin?>(null)
    /** The login the QR screen shows. */
    val shown: StateFlow<PendingLogin?> = _shown.asStateFlow()

    fun show(login: PendingLogin) {
        _shown.value = login
    }

    init {
        // Ended logins' links stop working.
        scope.launch {
            accounts.pendingLogins.collect { pending ->
                val ids = pending.map { it.id }.toSet()
                synchronized(lock) { logins.entries.removeAll { it.value.id !in ids } }
            }
        }
    }

    /** The phone's link for [login] (starts the page if needed). Fails if it can't listen or there's no network. */
    fun linkFor(login: PendingLogin): Result<String> = synchronized(lock) {
        try {
            val token = logins.entries.firstOrNull { it.value.id == login.id }?.key
                ?: (1..10).map { TOKEN_CHARS[random.nextInt(TOKEN_CHARS.size)] }.joinToString("").also { logins[it] = login }
            Result.success(start() + PATH + token)
        } catch (e: Exception) {
            logger.w(TAG, "No login page: ${e.message}")
            Result.failure(e)
        }
    }

    private fun start(): String {
        server?.let { return base }
        val configured = configurationStorage.config[E39Config.PimConfig.LoginRelay.host].trim()
        val host = configured.ifEmpty { lanAddress() ?: throw IllegalStateException("Not on a network (no Wi-Fi/LAN address)") }
        val s = HttpServer.create(InetSocketAddress(configurationStorage.config[E39Config.PimConfig.LoginRelay.port]), 0)
        s.executor = Executors.newSingleThreadExecutor { r -> Thread(r, TAG).apply { isDaemon = true } }
        s.createContext(PATH) { ex -> try { handle(ex) } catch (e: Exception) { logger.w(TAG, "Page failed: ${e.message}") } finally { ex.close() } }
        s.start()
        server = s
        base = "http://$host:${s.address.port}"
        logger.i(TAG, "Login page at $base$PATH...")
        return base
    }

    private fun handle(ex: HttpExchange) {
        val token = ex.requestURI.path.removePrefix(PATH).trimEnd('/')
        val login = synchronized(lock) { logins[token] }
        if (login == null) {
            reply(ex, 404, page("Login ended", "<p>This login isn't waiting any more. Start it again in the car.</p>"))
            return
        }
        when (ex.requestMethod) {
            "GET" -> reply(ex, 200, loginPage(login, ""))
            "POST" -> {
                val pasted = String(ex.requestBody.readAllBytes()).split('&')
                    .map { it.split('=', limit = 2) }
                    .firstOrNull { it[0] == "url" }?.getOrNull(1)
                    ?.let { URLDecoder.decode(it, Charsets.UTF_8) }.orEmpty()
                val result = runBlocking { accounts.finishLogin(login, pasted) }
                result.fold(
                    onSuccess = { reply(ex, 200, page("Done", "<p>The car is finishing the login. You can close this tab.</p>")) },
                    onFailure = { e ->
                        val why = when (e) {
                            is PimBackendException.InvalidArgument ->
                                "That isn't the address of Google's last page. Copy the address of the page that didn't load (it starts with 127.0.0.1) and paste all of it."
                            else -> e.message ?: e.toString()
                        }
                        reply(ex, 200, loginPage(login, why))
                    },
                )
            }
            else -> reply(ex, 405, page("Not allowed", ""))
        }
    }

    private fun loginPage(login: PendingLogin, error: String) = page(login.title, """
        ${if (error.isNotEmpty()) "<p class=err>${html(error)}</p>" else ""}
        <ol>
        <li><a class=button href="${html(login.url)}" target="_blank" rel="noopener">Open the login</a><br>
            It opens in a new tab. Log in and allow access.</li>
        <li>Google's last page won't load (“can't connect to 127.0.0.1”): that's expected.
            Long-press its address bar, tap <b>Copy</b>, then come back to this tab.</li>
        <li><form method=post><textarea name=url rows=4 placeholder="Paste the address here"></textarea>
            <button type=submit class=button>Finish Login</button></form></li>
        </ol>
        <p class=small>If this page opened inside a camera or QR app, open it in your browser first.</p>
    """)

    private fun page(title: String, body: String) = """<!doctype html>
        <html><head><meta charset=utf-8><meta name=viewport content="width=device-width,initial-scale=1">
        <title>${html(title)}</title><style>
        body{font-family:sans-serif;font-size:18px;margin:1em;line-height:1.4}
        .button{display:inline-block;background:#1a5fb4;color:#fff;border:0;border-radius:8px;padding:.6em 1.2em;font-size:18px;text-decoration:none;margin:.3em 0}
        textarea{width:100%;font-size:16px;box-sizing:border-box}
        li{margin-bottom:1em}.err{color:#c01c28;font-weight:bold}.small{font-size:14px;color:#555}
        </style></head><body><h2>${html(title)}</h2>$body</body></html>"""

    private fun reply(ex: HttpExchange, code: Int, body: String) {
        val bytes = body.toByteArray()
        ex.responseHeaders.add("Content-Type", "text/html; charset=utf-8")
        ex.responseHeaders.add("Cache-Control", "no-store")
        ex.sendResponseHeaders(code, bytes.size.toLong())
        ex.responseBody.use { it.write(bytes) }
    }
}
