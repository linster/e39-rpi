package ca.stefanm.ibus.lib.bluetooth.blueZdbus

//https://github.com/aguedes/bluez/blob/master/doc/media-api.txt
interface SmartynkMediaPlayer1 {

    sealed class ErrorResult(val details : Throwable) {
        class NotSupported(details: Throwable) : ErrorResult(details)
        class Failed(details: Throwable) : ErrorResult(details)
    }

    fun Play() : ErrorResult?
    fun Pause() : ErrorResult?
    fun Stop() : ErrorResult?
    fun Next() : ErrorResult?
    fun Previous() : ErrorResult?

    enum class PlayStatus(val raw : String) {
        PLAYING("playing"),
        STOPPED("stopped"),
        PAUSED("paused"),
        FORWARD_SEEK("forward-seek"),
        REVERSE_SEEK("reverse-seek"),
        ERROR("error")
    }

    val Status : PlayStatus

    @Retention(AnnotationRetention.RUNTIME)
    annotation class DBusDictKey(val key : String)

    data class TrackDict(
        @DBusDictKey("Track") val track : String,
        @DBusDictKey("Artist") val artist : String,
        @DBusDictKey("Album") val album : String
    )

    val Track : TrackDict
}

