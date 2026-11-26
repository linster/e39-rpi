package ca.stefanm.ibus.gui.menu.widgets.themes

import androidx.compose.ui.graphics.Color
import ca.stefanm.ibus.gui.menu.widgets.themes.designer.ThemeColorRole
import ca.stefanm.ibus.gui.menu.widgets.themes.designer.ThemeDesignerState
import ca.stefanm.ibus.gui.menu.widgets.themes.designer.ThemeGenerator
import ca.stefanm.ibus.lib.logging.Logger
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Test
import java.io.File
import java.nio.file.Files

class ThemeFilesTest {

    private val purple = Color(82, 41, 100, 255)
    private val jessPurple = ThemeScheme("JessPurple", "Jess Purple", ThemeGenerator.generate(purple, ThemeGenerator.LIGHT))

    private val logger = object : Logger {
        val errors = mutableListOf<String>()
        override fun v(tag: String, msg: String) {}
        override fun d(tag: String, msg: String) {}
        override fun i(tag: String, msg: String) {}
        override fun w(tag: String, msg: String) {}
        override fun e(tag: String, msg: String) { errors += msg }
        override fun e(tag: String, msg: String, e: Throwable?) { errors += msg }
    }

    private val folder: File = Files.createTempDirectory("themes").toFile()
    private val repositories = mutableListOf<ThemeRepository>()

    private fun repository(watch: Boolean = false) = ThemeRepository(folder, logger, watch).also { repositories += it }

    @After
    fun cleanUp() {
        repositories.forEach { it.close() }
        folder.deleteRecursively()
    }

    private fun badFile(text: String, why: String) {
        try {
            ThemeFiles.parse(text)
            fail("parsed: $text")
        } catch (e: ThemeFiles.BadThemeFile) {
            assertTrue("\"${e.message}\" says $why", e.message!!.contains(why))
        }
    }

    // --- The file format ---------------------------------------------------

    @Test
    fun writeThenParseGivesTheScheme() {
        val text = ThemeFiles.write(jessPurple)
        assertEquals(jessPurple, ThemeFiles.parse(text))
        // Every colour, in the designer's order, named by its key.
        val keys = Regex("""^ {2}(\w+) = "#[0-9A-F]{8}"$""", RegexOption.MULTILINE).findAll(text).map { it.groupValues[1] }.toList()
        assertEquals(ThemeColorRole.entries.map { it.key }, keys)
    }

    @Test
    fun builtInThemesRoundTrip() {
        Themes.builtInSchemes.forEach { assertEquals(it, ThemeFiles.parse(ThemeFiles.write(it))) }
    }

    @Test
    fun aFriendlyNameWithQuotesRoundTrips() {
        val scheme = jessPurple.copy(friendlyName = "Jess's \"Purple\" # 1")
        assertEquals(scheme, ThemeFiles.parse(ThemeFiles.write(scheme)))
    }

    @Test
    fun coloursLeftOutAreBmwBlues() {
        val scheme = ThemeFiles.parse("""
            name = "Minimal"
            friendlyName = "Minimal"
            colors { menuBackground = "#522964" }
        """.trimIndent())
        assertEquals(purple, scheme.colors.getValue(ThemeColorRole.MENU_BACKGROUND))
        assertEquals(Themes.BmwBlue.colors - ThemeColorRole.MENU_BACKGROUND, scheme.colors - ThemeColorRole.MENU_BACKGROUND)
    }

    @Test
    fun badFiles() {
        badFile("this { isn't", "not HOCON")
        badFile("""friendlyName = "X", colors { menuBackground = "#000000" }""", "no name")
        badFile("""name = "Has Space", friendlyName = "X", colors { menuBackground = "#000000" }""", "isn't letters and digits")
        badFile("""name = "1st", friendlyName = "X", colors { menuBackground = "#000000" }""", "isn't letters and digits")
        badFile("""name = "X", colors { menuBackground = "#000000" }""", "no friendlyName")
        badFile("""name = "X", friendlyName = " ", colors { menuBackground = "#000000" }""", "friendlyName is empty")
        badFile("""name = "X", friendlyName = "X"""", "no colors")
        badFile("""name = "X", friendlyName = "X", colors { chipColor = "#000000" }""", "no colors.menuBackground")
        badFile("""name = "X", friendlyName = "X", colors { menuBackground = "purple" }""", "isn't #RRGGBB")
        badFile("""name = "X", friendlyName = "X", colors { menuBackground = 12 }""", "isn't a string")
        badFile("""name = "X", friendlyName = "X", colors { menuBackground = "#000000", menuBackgrond = "#000000" }""", "unknown colours: menuBackgrond")
    }

    // --- Names ---------------------------------------------------------------

    @Test
    fun namesFromFriendlyNames() {
        assertEquals("JessPurple", ThemeScheme.nameFor("Jess Purple"))
        assertEquals("FormulaBrown2", ThemeScheme.nameFor("  formula-brown 2! "))
        assertEquals("BmwBlue", ThemeScheme.nameFor("Bmw blue"))
        assertEquals("Blue", ThemeScheme.nameFor("2 Blue"))
        assertNull(ThemeScheme.nameFor(" !? "))
    }

    @Test
    fun aSchemeMakesBothSizes() {
        assertEquals("JessPurpleNormalSize" to "Jess Purple", jessPurple.normalSize.let { it.configFileName to it.friendlyName })
        assertEquals("JessPurpleDoubledPixels" to "Jess Purple (2X)", jessPurple.doubledPixels.let { it.configFileName to it.friendlyName })
        assertEquals(purple, jessPurple.normalSize.colors.menuBackground)
        assertEquals(Themes.BmwBlueNormalSize.bigItem, jessPurple.normalSize.bigItem)
        assertEquals(Themes.BmwBlueDoubledPixels.bigItem, jessPurple.doubledPixels.bigItem)
    }

    @Test
    fun theDefaultThemeNameIsATheme() {
        assertEquals(Themes.BmwBlueDoubledPixels.configFileName, Themes.DEFAULT_THEME_NAME)
    }

    // --- The repository ------------------------------------------------------

    @Test
    fun withoutAFolderOnlyBuiltInThemes() {
        folder.deleteRecursively()
        val repo = repository()
        assertEquals(Themes.builtInThemes, repo.themes.value)
        assertTrue(repo.problems.value.isEmpty())
    }

    @Test
    fun savedThemesAreAddedInBothSizes() {
        val repo = repository()
        val file = repo.save(jessPurple)
        assertEquals(File(folder, "JessPurple.conf"), file)
        assertEquals(jessPurple, ThemeFiles.parse(file.readText()))

        assertEquals(Themes.builtInSchemes + jessPurple, repo.schemes.value)
        assertEquals(jessPurple.doubledPixels, repo.find("JessPurpleDoubledPixels"))
        assertEquals(jessPurple.normalSize, repo.find("JessPurpleNormalSize"))
        // 2X themes first, as the lists show them.
        assertEquals(listOf(true, true, true, true, false, false, false, false), repo.themes.value.map { it.isPixelDoubled })

        // A new repository (the next start) reads it too.
        assertEquals(jessPurple.doubledPixels, repository().find("JessPurpleDoubledPixels"))
    }

    @Test
    fun aFileReplacesTheBuiltInThemeOfItsName() {
        val bluer = Themes.BmwBlue.copy(colors = Themes.BmwBlue.colors + (ThemeColorRole.MENU_BACKGROUND to Color(0, 0, 128, 255)))
        val repo = repository()
        repo.save(bluer)
        assertEquals(listOf("BmwBlue", "RevolutionOrange", "GooseGreen"), repo.schemes.value.map { it.name })
        assertEquals(Color(0, 0, 128, 255), repo.find("BmwBlueDoubledPixels")!!.colors.menuBackground)
        assertEquals(Color(0, 0, 128, 255), repo.find("BmwBlueNormalSize")!!.colors.menuBackground)
    }

    @Test
    fun badFilesAreSkippedAndListed() {
        File(folder, "broken.conf").writeText("this { isn't")
        File(folder, "notes.txt").writeText("not a theme file, not read")
        File(folder, "JessPurple.conf").writeText(ThemeFiles.write(jessPurple))
        val repo = repository()
        assertEquals(Themes.builtInSchemes + jessPurple, repo.schemes.value)
        assertEquals(listOf("broken.conf"), repo.problems.value.map { it.file })
        assertTrue(logger.errors.single().contains("broken.conf"))
    }

    @Test
    fun reloadPicksUpChangesAndRemovals() {
        val repo = repository()
        val file = File(folder, "JessPurple.conf").apply { writeText(ThemeFiles.write(jessPurple)) }
        assertNull(repo.find("JessPurpleNormalSize"))
        repo.reload()
        assertNotNull(repo.find("JessPurpleNormalSize"))
        file.delete()
        repo.reload()
        assertNull(repo.find("JessPurpleNormalSize"))
    }

    @Test
    fun theFolderIsWatched() {
        folder.mkdirs()
        val repo = repository(watch = true)
        File(folder, "JessPurple.conf").writeText(ThemeFiles.write(jessPurple))
        val deadline = System.currentTimeMillis() + 10_000
        while (repo.find("JessPurpleNormalSize") == null && System.currentTimeMillis() < deadline) Thread.sleep(50)
        assertNotNull("picked up without reload()", repo.find("JessPurpleNormalSize"))
    }

    // --- The designer's Save ---------------------------------------------------

    @Test
    fun theDesignerSavesWhatItShows() {
        val state = ThemeDesignerState(Themes.BmwBlueDoubledPixels)
        assertEquals("BMW Blue", state.friendlyName)
        state.changeBase(purple)
        state.friendlyName = "Jess Purple"
        val scheme = state.scheme()!!
        assertEquals("JessPurple", scheme.name)
        assertEquals(state.colors, scheme.colors)

        val repo = repository()
        repo.save(scheme)
        val loaded = ThemeDesignerState(Themes.BmwBlueDoubledPixels).apply { load(repo.schemes.value.last()) }
        assertEquals("Jess Purple", loaded.friendlyName)
        assertEquals(state.colors, loaded.colors)

        state.friendlyName = " ?! "
        assertNull(state.scheme())

        // An existing theme's friendly name saves as that theme (replacing it).
        state.friendlyName = "BMW Blue"
        assertEquals("BmwBlue", state.scheme()!!.name)
        state.friendlyName = "jess purple"
        assertEquals("JessPurple", state.scheme(repo.schemes.value)!!.name)
    }
}
