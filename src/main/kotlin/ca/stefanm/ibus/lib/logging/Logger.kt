package ca.stefanm.ibus.lib.logging

import ca.stefanm.ibus.di.DaggerApplicationComponent

interface Logger {

    fun v(tag : String, msg : String)
    fun d(tag : String, msg : String)
    fun i(tag : String, msg : String)
    fun w(tag : String, msg : String)
    fun e(tag : String, msg : String)
    fun e(tag : String, msg : String, e : Throwable)

}

class StdOutLogger : Logger {
    override fun v(tag: String, msg: String) {
        println("VERBOSE : $tag / $msg")
    }

    override fun d(tag: String, msg: String) {
        println("DEBUG : $tag / $msg")
    }

    override fun i(tag: String, msg: String) {
        println("INFO : $tag / $msg")
    }

    override fun w(tag: String, msg: String) {
        println("WARN : $tag / $msg")
    }

    override fun e(tag: String, msg: String) {
        println("ERROR : $tag / $msg")
    }

    override fun e(tag: String, msg: String, e: Throwable) {
        e(tag, "$msg exception: ${e.printStackTrace()}")
    }

}

class StaticLogger(impl : Logger = DaggerApplicationComponent.create().logger()) : Logger by impl {
    companion object : Logger by StaticLogger()
}

fun foo(){
    StaticLogger.d("wat", "bar")
}