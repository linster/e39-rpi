package ca.stefanm.ibus.gui.map.widget.tile

import ca.stefanm.ibus.gui.di.MapModule
import ca.stefanm.ibus.lib.logging.Logger
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import java.io.File
import javax.inject.Inject
import javax.inject.Named
import kotlin.coroutines.coroutineContext

class TileFetcher @Inject constructor(
    private val osmTileServerInfo: OSMTileServerInfo,
    private val tileServerImageCache: TileServerImageCache,
    @Named(MapModule.TILE_CLIENT) private val httpClient: HttpClient
) {

    suspend fun getTile(x : Int, y : Int, zoom : Int) : File {
        val localTile = tileServerImageCache.retrieve(x, y, zoom)

        if (localTile != null) {
            return localTile
        }

        tileServerImageCache.saveTileToCache(x, y, zoom, getTileFromServer(x, y, zoom))
        return tileServerImageCache.retrieve(x, y, zoom)!!
    }

    internal suspend fun getTileFromServer(x : Int, y : Int, zoom : Int) : ByteArray {
        val response : HttpResponse = httpClient.get(osmTileServerInfo.getTileUrl(x, y, zoom))

        if (!response.status.isSuccess()) {
            error("Response was a failure: ${response.headers}")
        }
        if (response.contentType() != ContentType.Image.PNG) {
            error("Content type was ${response.contentType()} and not PNG")
        }

        return response.receive()
    }
}

class TileServerImageCache @Inject constructor(
    private val logger: Logger
) {
    private val TAG = "TileServerImageCache"

    private val homeFolder = System.getProperty("user.home")
    private val e39BaseFolder = File(homeFolder, ".e39")
    private val cacheDir = File(e39BaseFolder, "mapTiles")

    init {
        createCacheDir()
    }

    private fun createCacheDir() {
        if (!cacheDir.exists()) {
            cacheDir.mkdirs().also { success ->
                if (!success) error("Could not make tile cache dir: ${cacheDir.absolutePath}")
            }
        }
    }

    fun clearCache() {
        cacheDir.deleteRecursively()
        createCacheDir()
    }

    fun retrieve(x : Int, y : Int, zoom : Int) : File? {
        //"http://tile.openstreetmap.org/${OSMTileServerInfo.MAX_ZOOM - zoom}/$x/$y.png"

        if (!File(cacheDir, zoom.toString()).exists()){
            logger.d(TAG, "Zoom folder doesn't exist: $zoom")
            return null
        }

        val xFolder = File(File(cacheDir, zoom.toString()), x.toString())

        if (!xFolder.exists()) {
            logger.d(TAG, "xFolder: ${xFolder.absolutePath} doesn't exist")
            return null
        }

        val tileFile = File(xFolder, "${y}.png")

        if (!tileFile.exists()) {
            logger.d(TAG, "TileFile doesn't exist: ${tileFile.absolutePath}")
            return null
        }

        //TODO someday, delete the local cache if it's old enough
        //TODO put that deletion check here.

        return tileFile
    }

    suspend fun saveTileToCache(
        x : Int, y : Int, zoom : Int,
        imageDataPng : ByteArray,
    ) {
        val tileFolder = File(File(cacheDir, zoom.toString()), x.toString())
        tileFolder.mkdirs()

        val tile = File(tileFolder, "${y}.png")
        tile.createNewFile()

        tile.writeBytes(imageDataPng)
    }
}