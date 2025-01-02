package ca.stefanm.ibus.annotations.services

@Target(AnnotationTarget.ANNOTATION_CLASS)
@Retention(AnnotationRetention.RUNTIME)
annotation class PlatformServiceGroup(
    val name : String,
    val description : String
)