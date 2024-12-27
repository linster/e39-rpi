package ca.stefanm.ibus.annotations.services

import javax.inject.Qualifier

@Qualifier
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
annotation class PlatformServiceInfo(
    val name : String,
    val description : String
)

@Target(AnnotationTarget.ANNOTATION_CLASS)
@Retention(AnnotationRetention.RUNTIME)
annotation class PlatformServiceGroup(
    val name : String,
    val description : String
)