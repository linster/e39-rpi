package ca.stefanm.ibus.gui.pim.calendar.repo.backend

import ca.stefanm.ibus.configuration.ConfigurationStorage
import ca.stefanm.ibus.configuration.E39Config
import ca.stefanm.ibus.di.ApplicationScope
import dagger.Module
import dagger.Provides
import javax.inject.Provider

/** [AccountRepository]: the PIM bridge, or sample accounts (E39Config.PimConfig.Accounts.useFakeData). Chosen once per run. */
@Module
class AccountModule {

    @Provides
    @ApplicationScope
    fun provideAccountRepository(
        configurationStorage: ConfigurationStorage,
        remote: Provider<RemoteAccountRepository>,
        fake: Provider<FakeAccountRepository>,
    ): AccountRepository =
        if (configurationStorage.config[E39Config.PimConfig.Accounts.useFakeData]) fake.get() else remote.get()
}
