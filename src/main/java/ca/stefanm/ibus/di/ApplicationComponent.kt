package ca.stefanm.ibus.di

import ca.stefanm.ibus.Main
import dagger.Component
import javax.inject.Singleton

@Component(modules = [ApplicationModule::class])
@Singleton
interface ApplicationComponent {

    fun inject(main : Main)

}