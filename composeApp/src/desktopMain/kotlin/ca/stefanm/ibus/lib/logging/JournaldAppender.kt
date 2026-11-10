package ca.stefanm.ibus.lib.logging

import ch.qos.logback.classic.Level
import ch.qos.logback.classic.PatternLayout
import ch.qos.logback.classic.spi.ILoggingEvent
import ch.qos.logback.classic.spi.ThrowableProxyUtil
import ch.qos.logback.core.AppenderBase

/**
 * Logback appender for library logging (ktor, dbus-java, ...): writes to the
 * systemd journal when [Journald.isEnabled], with the fields LOGGER and THREAD,
 * otherwise prints [pattern] to stdout like a ConsoleAppender. Used from
 * logback.xml.
 */
class JournaldAppender : AppenderBase<ILoggingEvent>() {

    /** Console pattern used when not logging to the journal. */
    var pattern: String = "%d{HH:mm:ss.SSS} [%thread] %-5level %logger{36} - %msg%n"

    private var layout: PatternLayout? = null

    override fun start() {
        layout = PatternLayout().also {
            it.context = context
            it.pattern = pattern
            it.start()
        }
        super.start()
    }

    override fun stop() {
        layout?.stop()
        super.stop()
    }

    override fun append(event: ILoggingEvent) {
        if (Journald.isEnabled) {
            var message = event.formattedMessage ?: ""
            event.throwableProxy?.let { message += "\n" + ThrowableProxyUtil.asString(it) }
            val sent = Journald.send(
                priority(event.level),
                "${event.loggerName} - $message",
                mapOf("LOGGER" to event.loggerName, "THREAD" to (event.threadName ?: ""))
            )
            if (sent) return
        }
        layout?.let { print(it.doLayout(event)) }
    }

    private fun priority(level: Level): Int = when {
        level.isGreaterOrEqual(Level.ERROR) -> Journald.Priority.ERR
        level.isGreaterOrEqual(Level.WARN) -> Journald.Priority.WARNING
        level.isGreaterOrEqual(Level.INFO) -> Journald.Priority.INFO
        else -> Journald.Priority.DEBUG
    }
}
