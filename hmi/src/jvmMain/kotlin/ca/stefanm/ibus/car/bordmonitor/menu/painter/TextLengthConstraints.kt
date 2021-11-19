package ca.stefanm.ibus.car.bordmonitor.menu.painter


interface TextLengthConstraints {
    val AREA_0 : Int
    val AREA_1 : Int
    val AREA_2 : Int
    val AREA_3 : Int
    val AREA_4 : Int
    val AREA_5 : Int
    val AREA_6 : Int
    val AREA_7 : Int
    val INDEX_0_9 : Int
}

object TvModuleTextLengthConstraints : TextLengthConstraints {
    override val AREA_0 = 11
    override val AREA_1 = 5
    override val AREA_2 = 5
    override val AREA_3 = 5
    override val AREA_4 = 5
    override val AREA_5 = 7
    override val AREA_6 = 20
    override val AREA_7 = 20
    override val INDEX_0_9 = 14
}

object Mk4NavTextLengthConstraints : TextLengthConstraints {
    override val AREA_0 = 11
    override val AREA_1 = 4
    override val AREA_2 = 2
    override val AREA_3 = 4
    override val AREA_4 = 2
    override val AREA_5 = 7
    override val AREA_6 = 11
    override val AREA_7 = 0
    override val INDEX_0_9 = 23
}

object Mk4NavSplitScreenTextLengthConstraints : TextLengthConstraints {
    override val AREA_0 = 11
    override val AREA_1 = 4
    override val AREA_2 = 2
    override val AREA_3 = 4
    override val AREA_4 = 2
    override val AREA_5 = 7
    override val AREA_6 = 11
    override val AREA_7 = 0
    override val INDEX_0_9 = 23
}

fun TextLengthConstraints.getAllowedLength(area_n : Int) : Int = when(area_n) {
    1 -> AREA_1
    2 -> AREA_2
    3 -> AREA_3
    4 -> AREA_4
    5 -> AREA_5
    6 -> AREA_6
    7 -> AREA_7
    else -> throw IllegalArgumentException("Invalid area")
}.let { it - 1 }