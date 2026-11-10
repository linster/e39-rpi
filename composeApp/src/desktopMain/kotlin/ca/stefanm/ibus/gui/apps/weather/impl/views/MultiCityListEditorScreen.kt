package ca.stefanm.ibus.gui.apps.weather.impl.views

import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.runtime.snapshots.SnapshotStateMap
import androidx.compose.ui.util.fastFilterNotNull
import ca.stefanm.ibus.gui.apps.weather.impl.repo.MultiCityListRepo
import ca.stefanm.ibus.annotations.screenflow.ScreenDoc
import ca.stefanm.ibus.autoDiscover.AutoDiscover
import ca.stefanm.ibus.gui.menu.navigator.NavigationNode
import ca.stefanm.ibus.gui.menu.navigator.NavigationNodeTraverser
import ca.stefanm.ibus.gui.menu.navigator.Navigator
import ca.stefanm.ibus.gui.menu.widgets.ArbitraryContentsMenuItem
import ca.stefanm.ibus.gui.menu.widgets.ItemChipOrientation
import ca.stefanm.ibus.gui.menu.widgets.knobListener.dynamic.KnobObserverBuilderScope
import ca.stefanm.ibus.gui.menu.widgets.knobListener.dynamic.toDynamicLambda
import ca.stefanm.ibus.gui.menu.widgets.screenMenu.FullScreenMenu
import ca.stefanm.ibus.gui.menu.widgets.screenMenu.FullScreenMenu.OneColumnSmoothScreenCustomViews
import ca.stefanm.ibus.gui.menu.widgets.screenMenu.MenuItem
import ca.stefanm.ibus.gui.menu.widgets.screenMenu.SmoothScroll
import ca.stefanm.ibus.gui.menu.widgets.screenMenu.TextMenuItem
import ca.stefanm.ibus.lib.logging.Logger
import javax.inject.Inject

@ScreenDoc(
    screenName = "MultiCityListEditorScreen",
    description = "Weather app settings screen "
)
@ScreenDoc.AllowsGoBack
@AutoDiscover
class MultiCityListEditorScreen @Inject constructor(
    private val logger: Logger,
    private val navigationNodeTraverser: NavigationNodeTraverser,
    private val multiCityListRepo: MultiCityListRepo
): NavigationNode<MultiCityListEditorScreen.Result> {

    companion object {
        const val TAG = "MultiCityListEditorScreen"
    }

    interface CityItem {
        /** For example: "Ottawa" or "Kanata" */
        fun cityDisplayName() : String

        /** For example: (Environment Canada: ON-118) */
        fun cityDetail() : String

    }


    /** Output parameters from the screen */
    //TODO this is kinda dumb. Maybe just import the repo here.
    sealed interface Result {
        // No changes were made to the input list.
        object NoChangesRequested : Result

        //The order changed
        object NewOrderRequested : Result

        /** The user wanted to add an item to the list, and not reorder.*/
        object InsertionRequested : Result
    }

    override val thisClass: Class<out NavigationNode<Result>>
        get() = MultiCityListEditorScreen::class.java

    override fun provideMainContent(): @Composable ((incomingResult: Navigator.IncomingResult?) -> Unit) = { params ->

        val context = SmoothScroll.MenuWindowSmoothScrollContext(TAG)

        // Get the items from the parameters
        val givenList = multiCityListRepo.getMultiCityList().value

        val mutableList = SnapshotStateList<MultiCityListRepo.City>().apply { addAll(givenList) }

        ReorderableListScreen(
            "Re-order Weather Cities",
            smoothScrollContext = context,
            inputItems = mutableList,
            onNewlyOrderedInputItems = {
                mutableList.clear()
                mutableList.addAll(it)
                multiCityListRepo.updateCityList(it)
            },
            inputItemViewFactory = { item ->
                // Quick and dirty, could also be a two line where the detail is much smaller.
                TextMenuItem(
                    title = "${item.cityDisplayName()} (${item.cityDetail()})",
                    onClicked = {}
                ).toDynamicLambda()
            },
            onBackSelected = { changesMade ->
                if (!changesMade) {
                    navigationNodeTraverser.setResultAndGoBack(
                        MultiCityListEditorScreen::class.java,
                        Result.NoChangesRequested
                    )
                } else {
                    //Need to take the indexOf everything in the list now
                    navigationNodeTraverser.setResultAndGoBack(
                        MultiCityListEditorScreen::class.java,
                        Result.NewOrderRequested
                    )
                }
            },
            newItemOptionProvider = {
                TextMenuItem ("Add city...", onClicked = {
                    navigationNodeTraverser.setResultAndGoBack(
                        MultiCityListEditorScreen::class.java, Result.InsertionRequested
                    )
                })
            }
        )
    }

    @Composable
    fun <T> ReorderableListScreen(
        header : String,
        smoothScrollContext: SmoothScroll.SmoothScrollContext,

        inputItems : List<T>,

        inputItemViewFactory : (T) -> @Composable KnobObserverBuilderScope.(allocatedIndex: Int, currentIndex: Int) -> Unit,

        onNewlyOrderedInputItems : (List<T>) -> Unit,

        // The user elected to exit the ReorderableListScreen
        onBackSelected : (changesMade : Boolean) -> Unit,

        // Provide a menu item that lets the user create a "new one" of
        // the things being reordered.
        newItemOptionProvider : (() -> TextMenuItem)? = null

    ) {

        var isSelectionMode by remember { mutableStateOf(false) }

        var wereChangesMade by remember { mutableStateOf(false) }

        val backItem = remember { TextMenuItem(title = "Go back", onClicked = { onBackSelected(wereChangesMade) }).toDynamicLambda() }
        val newItem = remember { newItemOptionProvider?.invoke().let { it?.toDynamicLambda() } }

        fun <S> selectThisSpotComposable(
            item : S,
            /* The index that this composable will notify what the "new index of the item" is.
             * (Ceil of the one before it).
             */
            index : Int,
            onPlaceSelected : (item : S, selectedIndex : Int) -> Unit,
        ) : @Composable KnobObserverBuilderScope.(allocatedIndex: Int, currentIndex: Int) -> Unit = { allocatedIndex, currentIndex ->
            ArbitraryContentsMenuItem(
                chipOrientation = ItemChipOrientation.W,
                isSelected = allocatedIndex == currentIndex,
                isSmallSize = true,
                onClicked = { onPlaceSelected(item, index) }
            ) {
                //TODO make this pretty small.
                Text("Move item to this position.")
            }
        }

        val items : SnapshotStateList<Pair<T?, @Composable KnobObserverBuilderScope.(allocatedIndex: Int, currentIndex: Int) -> Unit>> =
            SnapshotStateList()


        LaunchedEffect(Unit, isSelectionMode, items) {
            //TODO maybe move this in the key so there's no race condition on the index?
            logger.d(TAG, "Update items")
            items.clear()
            items.addAll(buildList {
                add(null to backItem)
                newItem?.let { add(null to it) }
                items.forEachIndexed { index, (item, menuItem) ->
                    if (isSelectionMode) {
                        add(null to selectThisSpotComposable(item, index) { selectedItem, selectedIndex ->
                            isSelectionMode = false
                            wereChangesMade = true
                            items.removeIf { it.first == selectedItem }
                            items.add (selectedIndex, selectedItem to inputItems.find { it == item }!!.let { inputItemViewFactory.invoke(it) })
                        })
                    }
                    add(item to menuItem)
                }
            })
            onNewlyOrderedInputItems(items.mapNotNull { it.first })
        }

        key (isSelectionMode, items) {
            smoothScrollContext.OneColumnSmoothScreenCustomViews(
                header = header,
                prependGoBackEntry = false,
                items = items.map { it.second }
            )
        }
    }
}