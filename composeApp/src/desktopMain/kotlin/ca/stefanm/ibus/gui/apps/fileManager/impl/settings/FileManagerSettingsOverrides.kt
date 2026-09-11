package ca.stefanm.ca.stefanm.ibus.gui.apps.fileManager.impl.settings

import ca.stefanm.ibus.configuration.ConfigurationStorage.Companion.e39BaseFolder
import ca.stefanm.ibus.configuration.HmiVersion
import ca.stefanm.ibus.di.ApplicationScope
import com.uchuhimo.konf.Config
import com.uchuhimo.konf.Config.Companion.invoke
import com.uchuhimo.konf.ConfigSpec
import com.uchuhimo.konf.optional
import com.uchuhimo.konf.source.hocon
import com.uchuhimo.konf.source.hocon.toHocon
import java.io.File
import javax.inject.Inject


object FileManagerSettings : ConfigSpec() {

    /** When false, no operation that can break anything actually runs */
    val allowFilesystemModification by optional(
        default = false,
        name = "allowFilesystemModification",
        description = "When false, no operation that can break anything actually runs "
    )

    val defaultBrowseFolder by optional(
        "/home/stefan/BMW/fileManTest",
        name = "defaultBrowseFolder"
    )

    val showHiddenFiles by optional(
        default =false,
        name = "showHiddenFiles"
    )
}

object FileManagerSettingsOverridesRepo {

    private val fileManagerConfigFile = File(e39BaseFolder, "fileMan.conf")

    val config = Config { addSpec(FileManagerSettings) }
        .from.hocon.file(fileManagerConfigFile, optional = true)

    init {
        if (!fileManagerConfigFile.exists()) {
            config.toHocon.toFile(fileManagerConfigFile)
        }
        config.afterSet { item, value ->
            config.toHocon.toFile(fileManagerConfigFile)
        }
    }
}