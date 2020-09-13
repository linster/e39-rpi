package ca.stefanm.ibus.lib.bordmonitor.input

sealed class InputEvent {

    object PrevTrack : InputEvent()
    object NextTrack : InputEvent()

    object BMBTMenuPressed : InputEvent()
    object BMBTPhonePressed : InputEvent()

    object ShowRadioStatusScreen : InputEvent()

    object NavKnobPressed : InputEvent()
    data class NavKnobTurned(val clicks : Int, val direction : Direction) : InputEvent() {
        enum class Direction { LEFT, RIGHT }
    }

    //https://github.com/f1xpl/openauto/wiki/Keyboard-button-bindings

    //Menu item selected. [0..5]
    data class IndexSelectEvent(val selected : Int) : InputEvent()
}