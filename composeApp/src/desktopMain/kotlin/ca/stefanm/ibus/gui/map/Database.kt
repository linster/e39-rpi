package ca.stefanm.ca.stefanm.ibus.gui.map

//import ca.stefanm.e39.navigation.db.NavigationDb
import ca.stefanm.ibus.configuration.ConfigurationStorage
import ca.stefanm.ibus.di.ApplicationScope
//import ca.stefanm.ibus.gui.map.poi.PoiQueries
//import ca.stefanm.ibus.gui.map.poi.PoiTable
//import com.squareup.sqldelight.db.SqlDriver
//import com.squareup.sqldelight.sqlite.driver.JdbcSqliteDriver
import dagger.Module
import dagger.Provides
import java.sql.SQLException


@Module
class MapDatabaseModule {

//
//    @Provides
//    fun provideDriver() : SqlDriver {
//        return JdbcSqliteDriver(
//            "jdbc:sqlite:" + ConfigurationStorage.e39BaseFolder.absolutePath + "/poi.sqlite"
//        )
//    }


//    @Provides
//    @ApplicationScope
//    fun provideDatabase(
//        driver: SqlDriver
//    ) : NavigationDb {
//        try {
//            NavigationDb.Schema.create(driver)
//        } catch (e : SQLException) {
//            //Do nothing, we're not doing migrations.
//
//        }
//        return NavigationDb(driver)
//    }
//
//    @Provides
//    @ApplicationScope
//    fun providePoiQueries(
//        navigationDb: NavigationDb
//    ) : PoiQueries {
//        return navigationDb.poiQueries
//    }
}