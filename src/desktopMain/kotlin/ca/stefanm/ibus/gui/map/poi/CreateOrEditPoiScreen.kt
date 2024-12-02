package ca.stefanm.ibus.gui.map.poi

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import ca.stefanm.ibus.autoDiscover.AutoDiscover
import ca.stefanm.ibus.di.ApplicationScope
import ca.stefanm.ca.stefanm.ibus.gui.map.mapScreen.MapScreen
import ca.stefanm.ibus.gui.map.mapScreen.MapScreenResult
import ca.stefanm.ibus.gui.menu.Notification
import ca.stefanm.ibus.gui.menu.navigator.NavigationNode
import ca.stefanm.ibus.gui.menu.navigator.NavigationNodeTraverser
import ca.stefanm.ibus.gui.menu.navigator.Navigator
import ca.stefanm.ibus.gui.menu.notifications.NotificationHub
import ca.stefanm.ibus.gui.menu.widgets.BmwSingleLineHeader
import ca.stefanm.ibus.gui.menu.widgets.halveIfNotPixelDoubled
import ca.stefanm.ibus.gui.menu.widgets.modalMenu.ModalMenuService
import ca.stefanm.ibus.gui.menu.widgets.modalMenu.SidePanelMenu.InfoLabel
import ca.stefanm.ibus.gui.menu.widgets.modalMenu.SidePanelMenu.SidePanelMenu
import ca.stefanm.ibus.gui.menu.widgets.modalMenu.keyboard.Keyboard
import ca.stefanm.ibus.gui.menu.widgets.screenMenu.HalfScreenMenu
import ca.stefanm.ibus.gui.menu.widgets.screenMenu.ImageMenuItem
import ca.stefanm.ibus.gui.menu.widgets.screenMenu.TextMenuItem
import com.javadocmd.simplelatlng.LatLng
import javax.inject.Inject


@AutoDiscover
@ApplicationScope
class CreateOrEditPoiScreen @Inject constructor(
    private val navigationNodeTraverser: NavigationNodeTraverser,
    private val modalMenuService: ModalMenuService,
    private val notificationHub: NotificationHub,
    private val poiRepository: PoiRepository
) : NavigationNode<Nothing> {
    override val thisClass: Class<out NavigationNode<Nothing>>
        get() = CreateOrEditPoiScreen::class.java


    companion object {
        const val TAG = "CreateOrEditPoiScreen"

        fun openForPoiCreation(navigationNodeTraverser: NavigationNodeTraverser) {
            navigationNodeTraverser.navigateToNodeWithParameters(
                CreateOrEditPoiScreen::class.java,
                OpenMode.NewPoi
            )
        }

        fun newPoiAtLocation(navigationNodeTraverser: NavigationNodeTraverser, center: LatLng) {
            navigationNodeTraverser.navigateToNodeWithParameters(
                CreateOrEditPoiScreen::class.java,
                OpenMode.NewPoiAtLocation(center)
            )
        }

        fun editExistingPoi(navigationNodeTraverser: NavigationNodeTraverser, existingPoi: PoiRepository.Poi) {
            navigationNodeTraverser.navigateToNodeWithParameters(
                CreateOrEditPoiScreen::class.java,
                OpenMode.EditExistingPoi(existingPoi)
            )
        }
    }

    private sealed interface OpenMode {
        object NewPoi : OpenMode
        data class NewPoiAtLocation(val center : LatLng) : OpenMode
        data class EditExistingPoi(val poi : PoiRepository.Poi) : OpenMode
    }

    private var savedInProgressPoi : PoiRepository.Poi? = null
    //if we opened a POI to edit, this is the one we edited before we saved anything.
    private var openedExistingPoi : PoiRepository.Poi? = null

    override fun provideMainContent(): @Composable (incomingResult: Navigator.IncomingResult?) -> Unit = { params ->

        val existingPoi = (params?.requestParameters as? OpenMode)?.let {
            (it as? OpenMode.EditExistingPoi)?.poi
        }
        if (existingPoi != null) {
            openedExistingPoi = existingPoi
        }

        if (params?.requestParameters as? OpenMode == OpenMode.NewPoi) {
            savedInProgressPoi = null
            openedExistingPoi = null
        }


        val wipPoi = savedInProgressPoi

        val emptyName = existingPoi?.name ?: wipPoi?.name ?: "<no name>"
        val poiName = remember { mutableStateOf(emptyName) }

        val poiLocation = remember { mutableStateOf(existingPoi?.location ?: wipPoi?.location) }


        if ((params?.requestParameters as? OpenMode) is OpenMode.NewPoiAtLocation) {
            savedInProgressPoi = null
            openedExistingPoi = null

            poiLocation.value = (params.requestParameters as OpenMode.NewPoiAtLocation).center
        }

        val poiIcon = remember { mutableStateOf(
            existingPoi?.icon ?: wipPoi?.icon ?: PoiRepository.Poi.PoiIcon.NoIcon
        )}

        if (params?.resultFrom == MapScreen::class.java && params.result is MapScreenResult) {
            val chosenLocation = (params.result as? MapScreenResult.PointSelectedResult)?.point
            if (chosenLocation != null) {
                poiLocation.value = chosenLocation
            }
        }


        DisposableEffect(Unit) {
            onDispose {
                savedInProgressPoi = uiStateToPoi(
                    name = poiName.value,
                    icon = poiIcon.value,
                    position = poiLocation.value ?: LatLng(0.0, 0.0)
                )
            }
        }

        Column(Modifier.fillMaxSize()) {
            if (existingPoi == null) {
                BmwSingleLineHeader("Create POI")
            } else {
                BmwSingleLineHeader("Edit POI")
            }

            HalfScreenMenu.OneColumn(
                fullWidth = true,
                items = listOf(
                    TextMenuItem(
                        title = "Go Back",
                        onClicked = {
                            navigationNodeTraverser.goBack()
                        }
                    ),
                    TextMenuItem(
                        title = "POI Name: ${poiName.value}",
                        isSelectable = false,
                        onClicked = {}
                    ),
                    TextMenuItem(
                        title = "Set Name..."
                    ) {
                        modalMenuService.showKeyboard(
                            Keyboard.KeyboardType.FULL,
                            poiName.value.let { if (poiName.value == emptyName) "" else poiName.value }
                        ) {
                            poiName.value = it
                        }
                    },
                    TextMenuItem(
                        title = "Choose Location...: ${poiLocation.value?.let { "${it.latitude}, ${it.longitude}" } ?: ""}"
                    ) {
                        val currentLocation = poiLocation.value
                        if (currentLocation != null && currentLocation != LatLng(0.0, 0.0)) {
                            MapScreen.openForUserLocationSelection(navigationNodeTraverser, currentLocation)
                        } else {
                            MapScreen.openForUserLocationSelection(navigationNodeTraverser)
                        }
                    },
                    when (val icon = poiIcon.value) {
                        is PoiRepository.Poi.PoiIcon.ColoredCircle -> {
                            TextMenuItem(
                                title = "●",
                                labelColor = icon.color,
                                isSelectable = false,
                                onClicked = {}
                            )
                        }
                        is PoiRepository.Poi.PoiIcon.BundledIcon -> {
                            ImageMenuItem(
                                image = painterResource(icon.fileName),
                                tintColor = icon.tint,
                                imageModifier = Modifier.size(48.dp)
                                    .aspectRatio(1.0F),
                                isSelectable = false,
                                onClicked = {}
                            )
                        }
                        else -> TextMenuItem("", isSelectable = false, onClicked = {})
                    },
                    TextMenuItem(
                        title = "Set Icon...",
                    ) {
                        modalMenuService.showSidePaneOverlay(darkenBackground = true) {
                            IconSelectorMenu(poiIcon.value) {
                                poiIcon.value = it
                            }
                        }
                    },
                    TextMenuItem(
                        title = "Save POI"
                    ) {

                        val location = poiLocation.value

                        if (location == null) {
                            notificationHub.postNotificationBackground(
                                Notification(
                                    Notification.NotificationImage.ALERT_CIRCLE,
                                    topText = "Can't save POI.",
                                    contentText = "No location set."
                                )
                            )
                            return@TextMenuItem
                        }

                        savePoi(
                            //So we don't forget we're editing an existing when we really have just poorly recreated
                            //Android saved instance state problems. This screen shows some flaws in the framework.
                            existingPoi = openedExistingPoi ?: existingPoi,
                            uiStateToPoi(
                                name = poiName.value,
                                icon = poiIcon.value,
                                position = location
                            )
                        )
                        navigationNodeTraverser.goBack()
                    }
            ))
        }
    }

    @Composable
    fun IconSelectorMenu(
        currentIcon : PoiRepository.Poi.PoiIcon?,
        onIconSelected : (icon : PoiRepository.Poi.PoiIcon) -> Unit
    ) {

        SidePanelMenu(title = "Icon Selection") {
            Column(Modifier.padding(horizontal = 5.dp)) {
                InfoLabel("Select an Icon for this POI")


                //And now for the never-before-seen gridview.

                val colorList = listOf(Color.White, Color.Red, Color.Green, Color.Cyan, Color.Magenta)
                val iconList = listOf(
                    "poiIcons/briefcase.svg",
                    "poiIcons/home.svg",
                    "poiIcons/shopping-cart.svg",
                    "icons/map-pin.svg"
                )

                val windowedList : List<List<PoiRepository.Poi.PoiIcon>> = colorList.map { color ->
                    listOf(PoiRepository.Poi.PoiIcon.ColoredCircle(color)) + iconList.map { filename ->
                        PoiRepository.Poi.PoiIcon.BundledIcon(fileName = filename, tint = color)
                    }
                }

                HalfScreenMenu.GridMenu(
                    windowedList.map { row ->
                        row.map { icon ->
                            when (icon) {
                                is PoiRepository.Poi.PoiIcon.ColoredCircle -> TextMenuItem(
                                    title = "●",
                                    labelColor = icon.color,
                                    //isSelected = (currentIcon is PoiRepository.Poi.PoiIcon.ColoredCircle && currentIcon.color == it),
                                    onClicked = {
                                        onIconSelected(PoiRepository.Poi.PoiIcon.ColoredCircle(icon.color))
                                        modalMenuService.closeSidePaneOverlay(true)
                                    }
                                )
                                is PoiRepository.Poi.PoiIcon.BundledIcon -> ImageMenuItem(
                                    image = painterResource(icon.fileName),
                                    imageModifier = Modifier.size(48.dp.halveIfNotPixelDoubled())
                                        .aspectRatio(1.0F),
                                    tintColor = icon.tint,
                                    onClicked = {
                                        onIconSelected(PoiRepository.Poi.PoiIcon.BundledIcon(icon.fileName, icon.tint))
                                        modalMenuService.closeSidePaneOverlay(true)
                                    }
                                )
                                else -> TextMenuItem("", isSelectable = false, onClicked = {})
                            }
                        }
                    }
                )
            }
        }
    }

    private fun uiStateToPoi(
        name : String,
        icon: PoiRepository.Poi.PoiIcon,
        position : LatLng
    ) : PoiRepository.Poi {
        return PoiRepository.Poi(
            name,
            location = position,
            icon = icon,
            isVisible = false
        )
    }
    private fun savePoi(
        existingPoi: PoiRepository.Poi?,
        newPoi : PoiRepository.Poi
    ) {
        poiRepository.saveOrUpdatePoi(
            new = newPoi.copy(isVisible = existingPoi?.isVisible ?: false),
        )
    }
}

