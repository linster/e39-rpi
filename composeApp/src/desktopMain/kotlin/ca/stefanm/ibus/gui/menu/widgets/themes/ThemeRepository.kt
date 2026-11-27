package ca.stefanm.ibus.gui.menu.widgets.themes

import ca.stefanm.ibus.configuration.ConfigurationStorage
import ca.stefanm.ibus.di.ApplicationScope
import ca.stefanm.ibus.lib.logging.Logger
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.io.File
import java.nio.file.ClosedWatchServiceException
import java.nio.file.FileSystems
import java.nio.file.StandardWatchEventKinds
import java.nio.file.WatchService
import javax.inject.Inject

/**
 * The themes to choose from: the built-in ones ([Themes]) and the theme files
 * ([ThemeFiles]) in [folder] (themes/ under the configuration folder). A file
 * with a built-in theme's name replaces it. The folder is optional.
 *
 * A file that isn't a theme is logged, listed in [problems] and skipped. The
 * folder is watched: a file saved, changed or removed reloads the themes
 * ([reload] does it by hand).
 */
@ApplicationScope
class ThemeRepository internal constructor(
    private val folder: File,
    private val logger: Logger,
    watch: Boolean,
) {
    @Inject constructor(logger: Logger) : this(File(ConfigurationStorage.e39BaseFolder, FOLDER), logger, watch = true)

    companion object {
        const val TAG = "ThemeRepository"
        const val FOLDER = "themes"
    }

    data class Problem(val file: String, val message: String)

    private val _schemes = MutableStateFlow(Themes.builtInSchemes)
    /** The colour schemes: built in, replaced or added to by files. */
    val schemes: StateFlow<List<ThemeScheme>> = _schemes.asStateFlow()

    private val _themes = MutableStateFlow(Themes.builtInThemes)
    /** Every scheme's 2X themes, then their 1X ones. */
    val themes: StateFlow<List<Theme>> = _themes.asStateFlow()

    private val _problems = MutableStateFlow<List<Problem>>(emptyList())
    /** The theme files skipped at the last load, and why. */
    val problems: StateFlow<List<Problem>> = _problems.asStateFlow()

    private val lock = Any()
    private var watcher: WatchService? = null

    init {
        reload()
        if (watch) startWatching()
    }

    /** The theme with this configFileName, or null. */
    fun find(configFileName: String): Theme? = themes.value.firstOrNull { it.configFileName == configFileName }

    /** Reads the theme files again. */
    fun reload() = synchronized(lock) {
        val problems = mutableListOf<Problem>()
        val fromFiles = linkedMapOf<String, ThemeScheme>()
        val files = folder.listFiles { f -> f.isFile && f.extension == ThemeFiles.EXTENSION }?.sortedBy { it.name } ?: emptyList()
        for (file in files) {
            try {
                val scheme = ThemeFiles.parse(file.readText())
                fromFiles[scheme.name]?.let {
                    problems += Problem(file.name, "same name (${scheme.name}) as an earlier file; this one is used")
                }
                fromFiles[scheme.name] = scheme
            } catch (e: ThemeFiles.BadThemeFile) {
                problems += Problem(file.name, e.message ?: "not a theme")
            } catch (e: Exception) {
                problems += Problem(file.name, e.toString())
            }
        }
        problems.forEach { logger.e(TAG, "Skipping theme file ${it.file}: ${it.message}") }

        val replaced = Themes.builtInSchemes.map { builtIn -> fromFiles[builtIn.name] ?: builtIn }
        val added = fromFiles.values.filter { scheme -> Themes.builtInSchemes.none { it.name == scheme.name } }
        val schemes = replaced + added
        _schemes.value = schemes
        _themes.value = Themes.themesOf(schemes)
        _problems.value = problems
        logger.d(TAG, "Themes: ${schemes.joinToString { it.name }} (${fromFiles.size} from $folder)")
    }

    /**
     * Writes [scheme] to its theme file ([ThemeFiles.fileFor]), replacing one
     * of the same name, and reloads. Returns the file.
     */
    fun save(scheme: ThemeScheme): File {
        folder.mkdirs()
        val file = ThemeFiles.fileFor(folder, scheme)
        // Written whole, then moved into place: the watcher never reads half a file.
        val partial = File(folder, ".${file.name}.partial")
        partial.writeText(ThemeFiles.write(scheme))
        java.nio.file.Files.move(
            partial.toPath(), file.toPath(),
            java.nio.file.StandardCopyOption.REPLACE_EXISTING, java.nio.file.StandardCopyOption.ATOMIC_MOVE
        )
        logger.i(TAG, "Saved theme ${scheme.name} to $file")
        reload()
        startWatching()
        return file
    }

    /**
     * Watches [folder] and reloads on any change. It's made if it isn't
     * there, so theme files copied in later are seen too.
     */
    private fun startWatching() = synchronized(lock) {
        if (watcher != null) return@synchronized
        if (!folder.isDirectory && !folder.mkdirs()) {
            logger.w(TAG, "Can't make $folder; theme files there won't be seen until Reload themes")
            return@synchronized
        }
        val service = try {
            FileSystems.getDefault().newWatchService().also {
                folder.toPath().register(
                    it,
                    StandardWatchEventKinds.ENTRY_CREATE,
                    StandardWatchEventKinds.ENTRY_MODIFY,
                    StandardWatchEventKinds.ENTRY_DELETE,
                )
            }
        } catch (e: Exception) {
            logger.e(TAG, "Can't watch $folder; use Reload themes", e)
            return@synchronized
        }
        watcher = service
        Thread({
            try {
                while (true) {
                    val key = service.take()
                    // An editor's save can be several events: let them land, then reload once.
                    Thread.sleep(200)
                    key.pollEvents()
                    key.reset()
                    reload()
                }
            } catch (_: InterruptedException) {
            } catch (_: ClosedWatchServiceException) {
            }
        }, "ThemeRepository watcher").apply { isDaemon = true }.start()
    }

    /** Stops watching (tests). */
    internal fun close() {
        watcher?.close()
    }
}
