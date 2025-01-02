package ca.stefanm.ibus.car.bordmonitor.menu

interface ScreenInputEventListener {
    fun onKnobLeft(clicks : Int)
    fun onKnobRight(clicks: Int)

    fun onIndexSelected(index : Int)

    fun onKnobClick()
}