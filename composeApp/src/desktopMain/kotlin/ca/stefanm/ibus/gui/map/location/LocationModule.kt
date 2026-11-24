package ca.stefanm.ibus.gui.map.location

import dagger.Binds
import dagger.Module

/** [LocationRepository]: gpsd, through the car platform's GpsdService. */
@Module
abstract class LocationModule {
    @Binds
    abstract fun bindLocationRepository(impl: GpsdLocationRepository): LocationRepository
}
