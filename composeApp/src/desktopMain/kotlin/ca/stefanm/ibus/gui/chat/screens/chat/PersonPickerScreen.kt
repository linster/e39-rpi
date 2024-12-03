package ca.stefanm.ca.stefanm.ibus.gui.chat.screens.chat

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import ca.stefanm.ibus.annotations.screenflow.ScreenDoc
import ca.stefanm.ibus.autoDiscover.AutoDiscover
import ca.stefanm.ibus.gui.menu.navigator.NavigationNode
import ca.stefanm.ibus.gui.menu.navigator.NavigationNodeTraverser
import ca.stefanm.ibus.gui.menu.navigator.Navigator
import ca.stefanm.ca.stefanm.ibus.gui.menu.widgets.BmwSingleLineHeader
import ca.stefanm.ca.stefanm.ibus.gui.menu.widgets.screenMenu.FullScreenMenu
import ca.stefanm.ibus.gui.menu.widgets.screenMenu.ScrollMenu
import ca.stefanm.ca.stefanm.ibus.gui.menu.widgets.screenMenu.TextMenuItem
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import javax.inject.Inject

@ScreenDoc(
    screenName = "PersonPickerScreen",
    description = "Let the user pick a person for use in Creating a Chat or adding to a room"
)
@ScreenDoc.AllowsGoBack
@AutoDiscover
class PersonPickerScreen @Inject constructor(
    private val navigationNodeTraverser: NavigationNodeTraverser
) : NavigationNode<PersonPickerScreen.PersonPickerResult>{

    companion object {
        const val TAG = "PersonPickerScreen"
    }

    override val thisClass: Class<out NavigationNode<PersonPickerResult>>
        get() = PersonPickerScreen::class.java

    sealed class PersonPickerResult {
        object NoChoiceMade : PersonPickerResult()
        data class ChosenPerson(
            val personId: String
        ) : PersonPickerResult()
    }
    data class PickablePerson(
        val name : String,
        val personId : String
    )

    override fun provideMainContent(): @Composable (incomingResult: Navigator.IncomingResult?) -> Unit = {
        Column(modifier = Modifier.fillMaxSize()) {
            BmwSingleLineHeader("Pick Person")

            ScrollMenu.OneColumnScroll(
                items = getPersonList().collectAsState(emptyList()).value.map {
                      TextMenuItem(it.name, onClicked = {
                          navigationNodeTraverser.setResultAndGoBack(
                              this@PersonPickerScreen,
                              PersonPickerResult.ChosenPerson(it.personId)
                          )
                      })
                },
                displayOptions = ScrollMenu.ScrollListOptions(
                    itemsPerPage = 5,
                    isPageCountItemVisible = true,
                    showSpacerRow = false
                ),
                onScrollListExitSelected = {
                    navigationNodeTraverser.setResultAndGoBack(this@PersonPickerScreen, PersonPickerResult.NoChoiceMade)
                }
            )
        }
    }

    private fun getPersonList() : Flow<List<PickablePerson>> {
        //TODO list all the people on the server
        return emptyFlow()
    }
}