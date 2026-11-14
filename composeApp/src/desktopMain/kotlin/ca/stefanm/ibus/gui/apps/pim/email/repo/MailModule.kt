package ca.stefanm.ibus.gui.pim.email.repo

import ca.stefanm.ibus.configuration.ConfigurationStorage
import ca.stefanm.ibus.configuration.E39Config
import ca.stefanm.ibus.di.ApplicationScope
import dagger.Module
import dagger.Provides
import javax.inject.Provider

/**
 * [MailRepository]: the PIM bridge, or sample data
 * (E39Config.PimConfig.Mail.useFakeData). Chosen once per run, so screens
 * and the notifier agree.
 */
@Module
class MailModule {

    @Provides
    @ApplicationScope
    fun provideMailRepository(
        configurationStorage: ConfigurationStorage,
        remote: Provider<RemoteMailRepository>,
        fake: Provider<FakeMailRepository>,
    ): MailRepository =
        if (configurationStorage.config[E39Config.PimConfig.Mail.useFakeData]) fake.get() else remote.get()
}
