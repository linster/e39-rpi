package ca.stefanm.ibus.lib.bordmonitor.menu

import ca.stefanm.ibus.lib.bordmonitor.menu.painter.ScreenPainter

abstract class Screen(
    val title : Title,
    val indexWidgets: List<ScreenWidget?> = listOf(),
    private val screenPainter: ScreenPainter
) : ScreenInputEventListener {

    data class Title(
        val t0 : String? = null,
        val t1 : String? = null,
        val t2 : String? = null,
        val t3 : String? = null,
        val t4 : String? = null,
        val t5 : String? = null,
        val t6 : String? = null,
        val t7 : String? = null
    ) : Collection<String?> {

        private val data = listOf(t0, t1, t2, t3, t4, t5, t6, t7)

        override val size: Int = data.size
        override fun contains(element: String?) = data.contains(element)
        override fun containsAll(elements: Collection<String?>) = data.containsAll(elements)
        override fun isEmpty() = data.all { it == null }
        override fun iterator(): Iterator<String?> = data.iterator()
    }

    override fun onIndexSelected(index: Int) {
        indexWidgets.getOrNull(index)?.onSelected()
    }
}

