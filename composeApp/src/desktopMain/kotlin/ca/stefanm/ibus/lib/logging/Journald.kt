package ca.stefanm.ibus.lib.logging

import com.sun.jna.Library
import com.sun.jna.Native

/**
 * Writes log entries to the systemd journal with sd_journal_send(), so they get
 * real priorities and structured fields (e.g. HMI_TAG) instead of being plain
 * stdout lines.
 *
 * Only used when the HMI runs under systemd with stdout connected to the
 * journal (systemd sets $JOURNAL_STREAM then), e.g. as the e39rpi-hmi user
 * service on the Pi. Run from IntelliJ, it stays on stdout.
 *
 * Override with E39_LOG_TARGET=journal or E39_LOG_TARGET=stdout.
 */
object Journald {

    const val SYSLOG_IDENTIFIER = "e39rpi-hmi"

    /** syslog priorities, as used by the journal's PRIORITY field. */
    object Priority {
        const val ERR = 3
        const val WARNING = 4
        const val INFO = 6
        const val DEBUG = 7
    }

    @Suppress("FunctionName")
    private interface SystemdLibrary : Library {
        /** int sd_journal_send(const char *format, ...); the argument list ends with NULL. */
        fun sd_journal_send(format: String, vararg args: Any?): Int
    }

    private val library: SystemdLibrary? by lazy {
        try {
            Native.load("libsystemd.so.0", SystemdLibrary::class.java)
        } catch (t: Throwable) {
            System.err.println("Journald: libsystemd not available, logging to stdout ($t)")
            null
        }
    }

    /** True if log entries should go to the journal. */
    val isEnabled: Boolean by lazy {
        val wanted = when (System.getenv("E39_LOG_TARGET")?.lowercase()) {
            "journal" -> true
            "stdout" -> false
            else -> System.getenv("JOURNAL_STREAM") != null
        }
        wanted && library != null
    }

    /**
     * Send one entry. [fields] are extra journal fields: upper-case names
     * (letters, digits, underscores), e.g. "HMI_TAG" to "Bluetooth".
     * Returns false if the entry couldn't be written (caller may fall back).
     */
    fun send(priority: Int, message: String, fields: Map<String, String> = emptyMap()): Boolean {
        val lib = library ?: return false
        val args = ArrayList<Any?>(4 + fields.size * 2)
        // Values are passed as "%s" arguments, so '%' in a message is safe.
        args.add(message)
        args.add("PRIORITY=%d"); args.add(priority)
        args.add("SYSLOG_IDENTIFIER=%s"); args.add(SYSLOG_IDENTIFIER)
        for ((name, value) in fields) {
            args.add("$name=%s"); args.add(value)
        }
        args.add(null)
        return try {
            lib.sd_journal_send("MESSAGE=%s", *args.toTypedArray()) >= 0
        } catch (t: Throwable) {
            false
        }
    }

    /** Journal field names allow only A-Z, 0-9 and '_' and can't start with '_'. */
    fun fieldName(raw: String): String =
        raw.uppercase().replace(Regex("[^A-Z0-9_]"), "_").trimStart('_').ifEmpty { "FIELD" }
}
