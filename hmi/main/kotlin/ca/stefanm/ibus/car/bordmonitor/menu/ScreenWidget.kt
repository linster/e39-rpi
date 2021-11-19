package ca.stefanm.ibus.car.bordmonitor.menu

sealed class ScreenWidget {

    abstract fun onSelected()

    interface LabelWidget {
        val label : String
    }

    class Text(override val label : String) : ScreenWidget(), LabelWidget {
        override fun onSelected() {}
    }

    class Button(
        override val label : String,
        val onClicked : () -> Unit
    ) : ScreenWidget(), LabelWidget {
        override fun onSelected() { onClicked() }
    }

    class CheckBox(
        override val label : String,
        initialChecked : Boolean,
        val onCheckChanged : (newStatus : Boolean) -> Unit
    ) : ScreenWidget(), LabelWidget {

        var isChecked : Boolean = initialChecked
            set(value) {
                field = value
                onCheckChanged(field)
            }

        override fun onSelected() {
            isChecked = !isChecked
        }
    }

    //TODO figure out numeric select
    //https://www.youtube.com/watch?v=Ud7Mt30r3Ps
}