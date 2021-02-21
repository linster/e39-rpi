package ca.stefanm.ibus.di

import ca.stefanm.ibus.CliMain
import ca.stefanm.ibus.gui.GuiMain
import dagger.Component
import javax.inject.Singleton

@Component(modules = [ApplicationModule::class])
@Singleton
interface ApplicationComponent {

    fun inject(cliMain : CliMain)
    fun inject(guiMain: GuiMain)

}