package ca.stefanm
import ca.stefanm.ibus.gui.GuiMain
import kotlin.time.ExperimentalTime

class ComposeMain {
    companion object {
        @OptIn(ExperimentalTime::class)
        @JvmStatic fun main(args : Array<String>) {
            GuiMain().main()
        }
    }
}