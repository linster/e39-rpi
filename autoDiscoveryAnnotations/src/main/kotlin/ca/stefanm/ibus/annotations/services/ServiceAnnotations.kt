package ca.stefanm.ibus.annotations.services

import dagger.MapKey
import dagger.multibindings.IntoMap
import javax.inject.Qualifier
import kotlin.reflect.KClass

//https://dagger.dev/dev-guide/multibindings.html
//Maps whose keys are not known at compile time
//Make a provides method that calls the statically generated class. Loop over all
//the discovered services. Get the implementing classes.
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