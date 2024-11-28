package ca.stefanm.ca.stefanm.ibus.gui.di

import com.cosium.matrix_communication_client.MatrixResources
import dagger.Module
import dagger.Provides

@Module
class ChatModule {

    @Provides
    fun provideMatrixResources() : MatrixResources {
        return MatrixResources.factory()
            .builder()
            .https()
            .hostname("matrix.example.org")
            .defaultPort()
            .usernamePassword("jdoe", "secret")
            .build();
    }
}