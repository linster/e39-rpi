package ca.stefanm.ibus.car.di

import javax.inject.Scope

@Scope
@Retention(AnnotationRetention.RUNTIME)
annotation class UnConfiguredCarScope

@Scope
@Retention(AnnotationRetention.RUNTIME)
annotation class ConfiguredCarScope