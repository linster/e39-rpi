package ca.stefanm.ibus.gui.pim.contacts.repo

import ca.stefanm.ibus.configuration.ConfigurationStorage
import ca.stefanm.ibus.configuration.E39Config
import ca.stefanm.ibus.di.ApplicationScope
import dagger.Module
import dagger.Provides
import javax.inject.Provider

/**
 * [ContactsRepository]: the PIM bridge, or sample data
 * (E39Config.PimConfig.Contacts.useFakeData). Chosen once per run.
 */
@Module
class ContactsModule {

    @Provides
    @ApplicationScope
    fun provideContactsRepository(
        configurationStorage: ConfigurationStorage,
        remote: Provider<RemoteContactsRepository>,
        fake: Provider<FakeContactsRepository>,
    ): ContactsRepository =
        if (configurationStorage.config[E39Config.PimConfig.Contacts.useFakeData]) fake.get() else remote.get()
}
