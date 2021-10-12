package ca.stefanm.ibus.configuration

import com.uchuhimo.konf.ConfigSpec

class WindowManagerConfig : ConfigSpec() {

    val mainWindowUndecorated by optional<Boolean>(
        false,
        "DEBUG_MAIN_DECORATION",
        "Open Main Window with titlebar chrome. False for debug."
    )



    val hmiShiftRight by optional<Boolean>(
        false,
        "DEBUG_HMI_SHIFT_RIGHT",
        "Open HMI window shifted to the right. True for debug.")


}