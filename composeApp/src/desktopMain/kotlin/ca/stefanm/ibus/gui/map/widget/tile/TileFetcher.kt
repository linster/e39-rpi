package ca.stefanm.ibus.gui.map.widget.tile

import ca.stefanm.ibus.configuration.E39Config
import ca.stefanm.ibus.gui.di.MapModule
import ca.stefanm.ibus.gui.map.widget.ExtentCalculator
import ca.stefanm.ibus.gui.map.widget.MapScale
import ca.stefanm.ibus.gui.menu.Notification
import ca.stefanm.ibus.gui.menu.notifications.NotificationHub
import ca.stefanm.ibus.lib.logging.Logger
import com.javadocmd.simplelatlng.LatLng
import com.javadocmd.simplelatlng.LatLngTool
import com.javadocmd.simplelatlng.util.LengthUnit
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.coroutines.flow.*
import org.apache.commons.io.FileUtils
import java.io.File
import java.lang.Exception
import javax.inject.Inject
import javax.inject.Named
import kotlin.coroutines.coroutineContext
import kotlin.math.min

class TileFetcher @Inject constructor(
    private val osmTileServerInfo: OSMTileServerInfo,
    private val tileServerImageCache: TileServerImageCache,
    @Named(MapModule.TILE_CLIENT) private val httpClient: HttpClient,
    private val logger: Logger,
    private val notificationHub: NotificationHub
) {

    /** The server has no such tile for this request (prefetching: not available offline). */
    class TileUnavailableException(message: String) : Exception(message)

    /** prefetch: a bulk download (see downloadTiles). */
    suspend fun getTile(x : Int, y : Int, zoom : Int, prefetch : Boolean = false) : File {
        val localTile = tileServerImageCache.retrieve(x, y, zoom)

        if (localTile != null) {
            return localTile
        }

        tileServerImageCache.saveTileToCache(x, y, zoom, getTileFromServer(x, y, zoom, prefetch))
        return tileServerImageCache.retrieve(x, y, zoom)!!
    }

    internal suspend fun getTileFromServer(x : Int, y : Int, zoom : Int, prefetch : Boolean = false) : ByteArray {
        val url = osmTileServerInfo.getTileUrl(x, y, zoom)
        val response : HttpResponse = httpClient.get(url) {
            // The maps backend then serves only what it has offline (its
            // MBTiles and cache) and never bulk-fetches from OpenStreetMap.
            if (prefetch) header("X-Tile-Prefetch", "1")
        }

        if (prefetch && response.status == HttpStatusCode.NotFound) {
            throw TileUnavailableException("$url: not available offline")
        }
        if (!response.status.isSuccess()) {
            error("$url: ${response.status}")
        }
        if (response.contentType() != ContentType.Image.PNG) {
            error("Content type was ${response.contentType()} and not PNG")
        }

        return response.body()
    }

    data class DownloadStatus(
        val tilesDownloaded : Int,
        val totalTilesToDownload : Int,
        /** Not available offline on the maps backend. */
        val tilesUnavailable : Int = 0,
    )

    /** Bulk downloads are only allowed from the maps backend (see downloadTiles). */
    fun bulkDownloadAllowed() : Boolean =
        osmTileServerInfo.current().source == E39Config.MapConfig.TileSource.MAPS_BACKEND

    /**
     * Pre-fetches every tile of an area. Only from the maps backend, with
     * X-Tile-Prefetch: it serves what it has offline and never fetches from
     * OpenStreetMap, whose tile usage policy forbids bulk downloading.
     */
    suspend fun downloadTiles(center : LatLng, radius : MapScale, closestZoom : MapScale) : Flow<DownloadStatus> {

        check(bulkDownloadAllowed()) {
            "Bulk downloads from tile.openstreetmap.org aren't allowed (OSM tile policy); use the maps backend"
        }

        if (radius.meters < closestZoom.meters) {
            return flowOf(DownloadStatus(0, 0))
        }

        val tilesToDownload = MapScale.values()
            .filter { it.meters > closestZoom.meters }
            .map { it.mapZoomLevel }
            .map { zoomLevel ->

            val left = LatLngTool.travel(
                center,
                LatLngTool.Bearing.WEST,
                radius.meters.toDouble(),
                LengthUnit.METER
            ).let { newLocation ->
                ExtentCalculator.getTileNumber(newLocation.latitude, newLocation.longitude, zoomLevel)
            }

            val right = LatLngTool.travel(
                center,
                LatLngTool.Bearing.EAST,
                radius.meters.toDouble(),
                LengthUnit.METER
            ).let { newLocation ->
                ExtentCalculator.getTileNumber(newLocation.latitude, newLocation.longitude, zoomLevel)
            }

            val top = LatLngTool.travel(
                center,
                LatLngTool.Bearing.NORTH,
                radius.meters.toDouble(),
                LengthUnit.METER
            ).let { newLocation ->
                ExtentCalculator.getTileNumber(newLocation.latitude, newLocation.longitude, zoomLevel)
            }


            val bottom = LatLngTool.travel(
                center,
                LatLngTool.Bearing.SOUTH,
                radius.meters.toDouble(),
                LengthUnit.METER
            ).let { newLocation ->
                ExtentCalculator.getTileNumber(newLocation.latitude, newLocation.longitude, zoomLevel)
            }


            val xRange = left.first .. right.first
            val yRange = top.second .. bottom.second

            xRange.map { x ->
                yRange.map { y ->
                    x to y
                }
            }.flatten().map {(x, y) -> Triple(x, y, zoomLevel) }
        }.flatten()

        logger.d("TileFetcher", "Tiles to download : (count) ${tilesToDownload.size}")

        val concurrentWorkers = min(24, tilesToDownload.size)

        val flowsByWorker = tilesToDownload.windowed(
            size = tilesToDownload.size / concurrentWorkers,
            step = tilesToDownload.size / concurrentWorkers,
            partialWindows = true
        ).map {
            flowOf(*it.toTypedArray()).map { tile ->
                try {
                    getTile(tile.first, tile.second, tile.third, prefetch = true)
                    true
                } catch (e : TileUnavailableException) {
                    null
                } catch (e : Exception) {
                    logger.e("TileFetcher", "Server response exception on tile ${tile}", e)
                    notificationHub.postNotificationBackground(Notification(
                        image = Notification.NotificationImage.ALERT_TRIANGLE,
                        topText = "Tile Download Error",
                        contentText = "Tile: ${tile}, error: ${e.message}",
                        duration = Notification.NotificationDuration.SHORT
                    ))
                    false
                }
            }
        }

        return merge(*flowsByWorker.toTypedArray())
            .scan(DownloadStatus(tilesDownloaded = 0, totalTilesToDownload = tilesToDownload.size)) { accumulator, tileLoadSuccess ->
                when (tileLoadSuccess) {
                    true -> accumulator.copy(tilesDownloaded = accumulator.tilesDownloaded + 1)
                    null -> accumulator.copy(tilesUnavailable = accumulator.tilesUnavailable + 1)
                    false -> accumulator
                }
            }
    }
}

class TileServerImageCacheClearer @Inject constructor(
    private val tileServerImageCache: TileServerImageCache
) {
    fun clearCache() {
        tileServerImageCache.clearCache()
    }
}

class TileServerImageCache @Inject constructor(
    private val logger: Logger,
    private val tileServerInfo: OSMTileServerInfo,
) {
    private val TAG = "TileServerImageCache"

    private val homeFolder = System.getProperty("user.home")
    private val e39BaseFolder = File(homeFolder, ".e39")
    /** One folder per tile server (OSMTileServerInfo.Server.cacheFolder). */
    private val cacheRoot = File(e39BaseFolder, "mapTiles")

    /** The current server's cache: tiles from different servers differ. */
    private val cacheDir : File get() = File(cacheRoot, tileServerInfo.current().cacheFolder)

    init {
        moveLegacyCache()
        createCacheDir()
    }

    /**
     * Before there was a choice of servers, OpenStreetMap's tiles were
     * cached straight in mapTiles/<zoom>/...: move them to mapTiles/openstreetmap.
     */
    private fun moveLegacyCache() {
        val legacy = cacheRoot.listFiles { f -> f.isDirectory && f.name.all { it.isDigit() } } ?: return
        if (legacy.isEmpty()) return
        val osm = File(cacheRoot, "openstreetmap").apply { mkdirs() }
        legacy.forEach { zoomDir ->
            val target = File(osm, zoomDir.name)
            if (!target.exists() && zoomDir.renameTo(target)) return@forEach
            logger.w(TAG, "Couldn't move legacy tile cache ${zoomDir.absolutePath}")
        }
    }

    private fun createCacheDir() {
        val dir = cacheDir
        if (!dir.exists()) {
            dir.mkdirs().also { success ->
                if (!success) error("Could not make tile cache dir: ${dir.absolutePath}")
            }
        }
    }

    /** Clears the cache of every tile server. */
    fun clearCache() {
        cacheRoot.deleteRecursively()
        createCacheDir()
    }

    fun retrieve(x : Int, y : Int, zoom : Int) : File? {
        val cacheDir = cacheDir
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

    /** Of the current tile server's cache. */
    fun calculateCacheSize() : Long {
        val dir = cacheDir
        return if (dir.exists()) FileUtils.sizeOf(dir) else 0L
    }


}