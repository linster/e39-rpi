package ca.stefanm.ibus.annotations.services


@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.SOURCE)
annotation class PlatformServiceInfo(
    val name : String,
    val description : String
)

@Target(AnnotationTarget.ANNOTATION_CLASS)
@Retention(AnnotationRetention.SOURCE)
annotation class PlatformServiceGroup(
    val name : String,
    val description : String
)

