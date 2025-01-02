package ca.stefanm.ibus.annotations.services

@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
annotation class PlatformServiceInfo(
    val name : String,
    val description : String
)