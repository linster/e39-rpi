package ca.stefanm.ibus.gui.pim.email.repo

import ca.stefanm.ibus.configuration.ConfigurationStorage
import ca.stefanm.ibus.configuration.E39Config
import ca.stefanm.ibus.di.ApplicationScope
import ca.stefanm.ibus.gui.pim.email.repo.api.AccountMailSenders
import ca.stefanm.ibus.gui.pim.email.repo.api.FakeMailSenders
import ca.stefanm.ibus.gui.pim.email.repo.api.MailSenders
import ca.stefanm.ibus.gui.pim.contacts.repo.api.ContactRecipientSuggestions
import ca.stefanm.ibus.gui.pim.email.repo.api.RecipientSuggestions
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
        if (configurationStorage.config[E39Config.PimConfig.Mail.useFakeData]) sharedFake(fake) else remote.get()

    companion object {
        // The sample mail is one for the whole process: the car platform's
        // services (MailNotifierService) get their own Dagger component, and
        // must see the mail the screens send and get.
        private var fakeInstance: FakeMailRepository? = null

        private fun sharedFake(fake: Provider<FakeMailRepository>): FakeMailRepository =
            synchronized(this) { fakeInstance ?: fake.get().also { fakeInstance = it } }
    }

    @Provides
    @ApplicationScope
    fun provideMailSenders(
        configurationStorage: ConfigurationStorage,
        remote: Provider<AccountMailSenders>,
        fake: Provider<FakeMailSenders>,
    ): MailSenders =
        if (configurationStorage.config[E39Config.PimConfig.Mail.useFakeData]) fake.get() else remote.get()

    /** The contact picker's people: the address book and recent mail. */
    @Provides
    @ApplicationScope
    fun provideRecipientSuggestions(suggestions: ContactRecipientSuggestions): RecipientSuggestions = suggestions
}
