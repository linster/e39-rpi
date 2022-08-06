package ca.stefanm.ibus.gui.di

import dagger.Module
import dagger.Provides
import io.ktor.client.*
import io.ktor.client.features.logging.*
import javax.inject.Named


@Module
class MapModule {

    companion object {
        const val COROUTINE_SCOPE_TILE_CLIENT = "tile_client_scope"
        const val TILE_CLIENT = "tileClient"

        val httpClient = HttpClient {

            install(Logging) {
                logger = Logger.SIMPLE
                level = LogLevel.HEADERS
            }
        }
    }

    @Provides
    @Named(TILE_CLIENT)
    fun provideHttpClient() : HttpClient {
        //Lazy-init this since TileFetcher is eager.
        return httpClient
    }


}