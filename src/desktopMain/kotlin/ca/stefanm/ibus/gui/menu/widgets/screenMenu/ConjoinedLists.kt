package ca.stefanm.ibus.gui.menu.widgets.screenMenu

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.snapshots.StateObject
import androidx.compose.runtime.snapshots.StateRecord

internal data class SnapshotPair<A, B>(
    val first : A,
    val second : B
) : StateObject {

    val backingList = mutableStateListOf(first, second)

    override val firstStateRecord: StateRecord
        get() = backingList.firstStateRecord

    override fun prependStateRecord(value: StateRecord) {
        backingList.prependStateRecord(value)
    }
}

internal open class SnapshotTriple<A, B, C>(
    val first : A,
    val second : B,
    val third : C
) : StateObject {
    val backingList = mutableStateListOf(first, second, third)
    override val firstStateRecord: StateRecord
        get() = backingList.firstStateRecord

    override fun prependStateRecord(value: StateRecord) {
        backingList.prependStateRecord(value)
    }

    operator fun component1() : A = first
    operator fun component2() : B = second
    operator fun component3() : C = third
    override fun toString() =
        "SnapshotTriple(first = $first, second = $second, third = $third)"
}

internal class ConjoinedListRecord<I, P>(
    val item : I,
    val sourcePlacementEnum : P, //LEFT or RIGHT, or a quadrant
    val originalItemPosition : Int //Original index in placement
) : SnapshotTriple<I, P, Int>(item, sourcePlacementEnum, originalItemPosition)
