package ca.stefanm.ibus.logger

class StdOutLogger constructor() : Logger {
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