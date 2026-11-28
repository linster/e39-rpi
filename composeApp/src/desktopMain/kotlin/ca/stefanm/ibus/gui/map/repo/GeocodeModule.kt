package ca.stefanm.ibus.gui.map.repo

import ca.stefanm.ibus.configuration.ConfigurationStorage
import ca.stefanm.ibus.configuration.E39Config
import ca.stefanm.ibus.di.ApplicationScope
import dagger.Module
import dagger.Provides
import javax.inject.Provider

/**
 * [GeocodeRepository]: the maps backend, or sample places
 * (E39Config.MapConfig.Geocode.useFakeData). Chosen once per run.
 */
@Module
class GeocodeModule {

    @Provides
    @ApplicationScope
    fun provideGeocodeRepository(
        configurationStorage: ConfigurationStorage,
        remote: Provider<RemoteGeocodeRepository>,
        fake: Provider<FakeGeocodeRepository>,
    ): GeocodeRepository =
        if (configurationStorage.config[E39Config.MapConfig.Geocode.useFakeData]) fake.get() else remote.get()
}
