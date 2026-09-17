package ca.stefanm.ibus.gui.apps.actionRouter

enum class FileAction {

    /** Open the file for viewing on the device */
    VIEW,

    /** Send the file to chat */
    SEND_CHAT,

    /** Send the file in an email */
    SEND_EMAIL,

    /** Attach to calendar event */
    ADD_TO_CALENDAR,

    /** Make a Todo item with the file in it */
    MAKE_TODO,

    /** Send the file to whatever can extract/compress zip/tar/bz2 */
    SEND_FILE_TO_EXTRACTOR_COMPRESSOR
}