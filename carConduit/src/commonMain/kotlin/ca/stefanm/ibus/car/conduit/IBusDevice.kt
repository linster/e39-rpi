package ca.stefanm.ibus.car.conduit

//https://github.com/tedsalmon/DroidIBus/blob/master/app/src/main/java/com/ibus/droidibus/ibus/IBusSystem.java#L155
enum class IBusDevice(val deviceId: Int) {

    BODY_MODULE(0x00),
    BROADCAST(0xFF),

    /* Steering wheel buttons */
    MFL(0x50),

    /* BM54 Tuner */
    RADIO(0x68),

    MID(0xC0),

    /* Telephone Computer */
    TELEPHONE(0xC8),

    /* High Cluster */
    IKE(0x80),
    IKE_TEXTBAR(0xE7),

    TV_MODULE(0xED),

    NAV_VIDEOMODULE(0x3B),
    NAV_MENUSCREEN(0x43),
    /* GPS Feed from nav unit */
    NAV_LOCATION(0xD0),
    /* Board monitor buttons that aren't the radio buttons */
    BOARDMONITOR_BUTTONS(0xF0),

    DIS(0x3f)
}