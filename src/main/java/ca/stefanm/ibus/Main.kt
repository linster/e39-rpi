package ca.stefanm.ibus

import ca.stefanm.ibus.di.ApplicationModule
import ca.stefanm.ibus.di.DaggerApplicationComponent
import ca.stefanm.ibus.lib.platform.Platform
import javax.inject.Inject


fun main() {
    Main()
}

class Main {

    @Inject
    lateinit var platform: Platform

    init {


        DaggerApplicationComponent.builder().build().inject(this)

        println(platform.toString())
        platform.run()

        print("Hello World")
    }
}