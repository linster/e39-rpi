package ca.stefanm.ibus.gui.chat.service

import ca.stefanm.ibus.configuration.ConfigurationStorage
import ca.stefanm.ibus.di.ApplicationScope
import dagger.Component
import dagger.Module
import dagger.Provides
import kotlinx.coroutines.CoroutineScope
import net.folivo.trixnity.client.MatrixClient
import net.folivo.trixnity.client.fromStore
import net.folivo.trixnity.client.media.MediaStore
import net.folivo.trixnity.client.media.createMediaModule
import net.folivo.trixnity.client.media.okio.OkioMediaStore
import net.folivo.trixnity.client.store.repository.exposed.createExposedRepositoriesModule
import okio.Path.Companion.toOkioPath
import org.jetbrains.exposed.sql.Database
import java.io.File
import javax.inject.Named

/// Put all the Trixnity dependencies in here

@Module
class MatrixChatModule {


}