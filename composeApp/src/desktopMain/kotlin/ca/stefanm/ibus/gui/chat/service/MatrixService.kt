package ca.stefanm.ca.stefanm.ibus.gui.chat.service

import ca.stefanm.ibus.configuration.ConfigurationStorage
import ca.stefanm.ibus.di.ApplicationScope
import ca.stefanm.ibus.gui.menu.Notification
import ca.stefanm.ibus.gui.menu.notifications.NotificationHub
import ca.stefanm.ibus.lib.logging.Logger
import io.ktor.http.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import net.folivo.trixnity.client.MatrixClient
import net.folivo.trixnity.client.fromStore
import net.folivo.trixnity.client.login
import net.folivo.trixnity.client.media.MediaStore
import net.folivo.trixnity.client.media.okio.OkioMediaStore
import net.folivo.trixnity.client.store.repository.exposed.createExposedRepositoriesModule
import net.folivo.trixnity.clientserverapi.model.authentication.IdentifierType
import okio.Path.Companion.toOkioPath
import org.jetbrains.exposed.sql.Database
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@ApplicationScope
class MatrixService @Inject constructor(
    private val logger : Logger,
    private val notificationHub: NotificationHub
) {

    private companion object Dependencies {

        const val TAG = "MatrixService"
        /// Put the singleton MatrixClient stuff here.

        val mediaStore : MediaStore get() {
            if (_mediaStore == null) {
                _mediaStore = makeMediaStore()
            }
            return _mediaStore!!
        }
        var _mediaStore : MediaStore? = null
        fun makeMediaStore() : MediaStore {

            val matrixFolder = File(ConfigurationStorage.e39BaseFolder, "matrix")
            if (!matrixFolder.exists()) {
                matrixFolder.mkdirs()
            }

            return OkioMediaStore(
                basePath = matrixFolder.toOkioPath()
            )
        }

        fun provideMatrixDatabase() : Database {
            // The database is unencrypted with no password. Matrix server secrets
            // are stored in the db. Good luck, have fun!
            val matrixFolder = File(ConfigurationStorage.e39BaseFolder, "matrix")
            if (!matrixFolder.exists()) {
                matrixFolder.mkdirs()
            }
            val matrixDbFile = File(matrixFolder, "db.db")
            return Database.connect("jdbc:sqlite:${matrixDbFile.absolutePath}", "org.sqlite.JDBC")
        }

        suspend fun provideMatrixDataStore(
            database : Database = provideMatrixDatabase()
        ) : org.koin.core.module.Module {
            return createExposedRepositoriesModule(database)
        }


        var matrixClient : MatrixClient? = null

        val matrixCoroutineScope = CoroutineScope(SupervisorJob())

    }

    fun start() {
        matrixCoroutineScope.launch {
            if (matrixClient == null) {
                matrixClient = MatrixClient.fromStore(
                    repositoriesModule = provideMatrixDataStore(provideMatrixDatabase()),
                    mediaStore = mediaStore
                ).getOrNull()
            } else {
                logger.d(TAG, "using stored matrix client")
            }

            if (matrixClient == null) {
                logger.w(TAG, "Stored matrix client is null for some reason")
            }

            matrixClient?.startSync()
        }
    }

    fun stop() {
        matrixCoroutineScope.launch {
            matrixClient?.stop(wait = true)
            logger.d(TAG, "matrix service stopped")
        }

    }

    fun login(
        server : String,
        username : String,
        password : String
    ) {
        matrixCoroutineScope.launch {
            matrixClient?.logout()
            logger.d(TAG, "Login: Matrix client logged out")
            matrixClient?.stop()
            logger.d(TAG, "Login: Matrix client stopped")

            matrixClient = null

            matrixClient = MatrixClient.login(
                baseUrl = Url(server),
                identifier = IdentifierType.User(username),
                password = password,
                repositoriesModule = provideMatrixDataStore(provideMatrixDatabase()),
                mediaStore = mediaStore
            ).fold(
                onSuccess = { it },
                onFailure = {
                    logger.e(TAG, "Failed to log in: $it", it)
                    notificationHub.postNotification(
                        Notification(
                            Notification.NotificationImage.ALERT_TRIANGLE,
                            "Matrix failed to log in",
                            "Exception: ${it.message}"
                        )
                    )
                    null
                }
            )

            if (matrixClient == null) {
                logger.w(TAG, "login() just set the client to null")
            }

            start()
        }

    }

    fun getServiceState() : Flow<Boolean> {
        return flowOf(false)
    }


    fun getMatrixClient() : MatrixClient? {
        return matrixClient
    }

    //Exposed for the settings page
    suspend fun logout() {
        matrixClient?.logout()
    }
    suspend fun clearCache() {
        matrixClient?.clearCache()
    }
    suspend fun clearMediaCache() {
        matrixClient?.clearMediaCache()
    }
}