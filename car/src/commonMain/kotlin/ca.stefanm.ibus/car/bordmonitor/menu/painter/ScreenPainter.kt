package ca.stefanm.ibus.car.bordmonitor.menu.painter

import ca.stefanm.ibus.di.ApplicationModule
import ca.stefanm.ibus.car.bordmonitor.menu.Screen
import ca.stefanm.ibus.car.bordmonitor.menu.ScreenWidget
import ca.stefanm.ibus.lib.messages.IBusMessage
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import okio.Buffer
import javax.inject.Inject
import javax.inject.Named
import kotlin.text.toByteArray

fun Buffer.appendString(string : String) : Buffer {
    string.toByteArray(Charsets.US_ASCII).map { it.toInt() }.forEach { this.writeByte(it) }
    return this
}

/** Sends mesages to paint a screen. Each menu item is painted by drawing index areas */
class ScreenPainter @Inject constructor(
    @Named(ApplicationModule.IBUS_MESSAGE_OUTPUT_CHANNEL) private val messagesOut : Channel<IBusMessage>,
    private val coroutineScope: CoroutineScope,
    private val painterDispatcher : CoroutineDispatcher = Dispatchers.IO,
    private val textLengthConstraints: TextLengthConstraints
) {

    //TODO looks like the isChccked field involves writing a magic value to the end of index - 2

    enum class TitleTextAttribute(val data : Int) {
        NORMAL(data = 0x30),
        BLINKING(data = 0xFF)
    }

    //http://www.translatetheweb.com/?from=de&to=en&refd=xoutpost.com&dl=en&rr=DC&a=http%3a%2f%2fwww.alextronic.de%2fbmw%2fprojects_bmw_info_ibus.html%23_3

    private fun String.padSpaces(length : Int = this.length) : String {
        return this.padEnd(length = length, padChar = ' ')
    }

    val EmptyIndexWidget = ScreenWidget.Text(label = "".padSpaces(textLengthConstraints.INDEX_0_9))



    fun paint(screen: Screen) {
        coroutineScope.launch(painterDispatcher) {
            screen.title.forEachIndexed{ n, label ->
                if (label != null) {
                    messagesOut.send(TitleNMessage(label, n, textLengthConstraints))
                }
            }

            screen.indexWidgets.forEachIndexed { n, widget ->
                if (widget == null) {
                    messagesOut.send(
                        IndexMessage(label = "".padSpaces(textLengthConstraints.INDEX_0_9),
                        n = n,
                        lengthConstraints = textLengthConstraints
                    )
                    )
                    return@forEachIndexed
                }

                when (widget) {
                    is ScreenWidget.LabelWidget -> {
                        messagesOut.send(
                            IndexMessage(
                                label = widget.label,
                                n = n,
                                lengthConstraints = textLengthConstraints
                            )
                        )
                    }
                }
            }

            messagesOut.send(IndexRefreshMessage)
        }
    }







}