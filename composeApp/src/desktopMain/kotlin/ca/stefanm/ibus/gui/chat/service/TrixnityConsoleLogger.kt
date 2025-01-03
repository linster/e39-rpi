package ca.stefanm.ca.stefanm.ibus.gui.chat.service

import ca.stefanm.ibus.lib.logging.Logger
import javax.inject.Inject


///https://github.com/oshai/kotlin-logging

/// Used to hook into Trixnity's logger and log to our logger.
class TrixnityConsoleLogger @Inject constructor(
    private val logger: Logger
){

    //These need to be done with slf4j config.

}