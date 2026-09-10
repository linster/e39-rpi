package ca.stefanm.ca.stefanm.ibus.gui.apps.fileManager.impl.fileType


//maybe we need an action router, and an annotation for it that can go on a method
//that method can then take the file and do stuff with it?

//Rebuilding all of Android intents is a bit pointless. The idea here is to have a way for the
//file manager to open a file with either a built-in thing, or delegate to xdg-open

sealed interface FileType {
    /** File types that cannot be determined */
    object Unknown : FileType
    /** File types we don't care about */
    object Other : FileType

    /** A zip file, bz2, etc. Supporting extraction would be handy */
    object Archive : FileType

    /** Music we can play without having to watch it. */
    object Audio : FileType

    object Movie : FileType
    object Picture : FileType
    object PDF : FileType
    object TextFile : FileType
}

