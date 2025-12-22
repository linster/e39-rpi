package ca.stefanm.ibus.gui.menu.widgets.modalMenu

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ca.stefanm.ibus.di.ApplicationModule
import ca.stefanm.ibus.gui.map.poi.PoiRepository
import ca.stefanm.ibus.gui.menu.widgets.themes.ThemeWrapper
import ca.stefanm.ibus.di.ApplicationScope
import ca.stefanm.ibus.gui.map.Extents
import ca.stefanm.ibus.gui.map.MapViewer
import ca.stefanm.ibus.gui.map.OverlayProperties
import ca.stefanm.ibus.gui.map.PoiOverlay
import ca.stefanm.ibus.gui.map.widget.MapScale
import ca.stefanm.ibus.gui.menu.MenuWindow
import ca.stefanm.ibus.gui.menu.widgets.BmwSingleLineHeader
import ca.stefanm.ibus.gui.menu.widgets.ChipItemColors
import ca.stefanm.ibus.gui.menu.widgets.MenuItem
import ca.stefanm.ibus.gui.menu.widgets.halveIfNotPixelDoubled
import ca.stefanm.ibus.gui.menu.widgets.knobListener.KnobListenerService
import ca.stefanm.ibus.gui.menu.widgets.knobListener.dynamic.KnobObserverBuilder
import ca.stefanm.ibus.gui.menu.widgets.knobListener.dynamic.KnobObserverBuilderState
import ca.stefanm.ibus.gui.menu.widgets.modalMenu.keyboard.Keyboard
import ca.stefanm.ibus.gui.menu.widgets.modalMenu.keyboard.Keyboard.KeyboardType
import ca.stefanm.ibus.gui.menu.widgets.modalMenu.keyboard.KeyboardViews
import ca.stefanm.ibus.gui.menu.widgets.screenMenu.HalfScreenMenu
import ca.stefanm.ibus.gui.menu.widgets.screenMenu.MenuItem
import ca.stefanm.ibus.gui.menu.widgets.screenMenu.TextMenuItem
import ca.stefanm.ibus.gui.pim.calendar.views.CalendarDay
import ca.stefanm.ibus.lib.logging.Logger
import com.javadocmd.simplelatlng.LatLng
import com.kizitonwose.calendar.compose.HorizontalCalendar
import com.kizitonwose.calendar.compose.rememberCalendarState
import com.kizitonwose.calendar.core.daysOfWeek
import com.kizitonwose.calendar.core.minusMonths
import com.kizitonwose.calendar.core.now
import com.kizitonwose.calendar.core.plusMonths
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.datetime.LocalDate
import kotlinx.datetime.YearMonth
import kotlinx.datetime.toJavaLocalDate
import kotlinx.datetime.toLocalDateTime
import org.jxmapviewer.viewer.GeoPosition
import javax.inject.Inject
import javax.inject.Named
import kotlin.time.Clock

object SidePanelMenu {
    @Composable
    fun SidePanelMenu(
        title : String? = null,
        text : @Composable () -> Unit,
        buttons : List<MenuItem>
    ) {

        SidePanelMenu(title) {
            Column(Modifier.fillMaxHeight(), verticalArrangement = Arrangement.SpaceBetween) {
                Column(Modifier.padding(horizontal = 10.dp.halveIfNotPixelDoubled(), vertical = 10.dp.halveIfNotPixelDoubled())) {
                    text()
                }
                HalfScreenMenu.OneColumn(
                    alignment = Alignment.End,
                    fullWidth = true,
                    items = buttons
                )
            }
        }
    }

    @Composable
    fun SidePanelMenu(title : String? = null, contents : @Composable () -> Unit) {
        Column(
            Modifier
                .fillMaxSize()
                .background(ThemeWrapper.ThemeHandle.current.colors.menuBackground)
                .border(width = 4.dp.halveIfNotPixelDoubled(), color = ThemeWrapper.ThemeHandle.current.colors.sideMenuBorder)
                .shadow(4.dp.halveIfNotPixelDoubled(), RectangleShape)
        ) {
            if (title != null) {
                BmwSingleLineHeader(title)
            }
            contents()
        }
    }

    @Composable
    fun InfoLabel(text : String, weight : FontWeight = FontWeight.Normal) {
        Text(
            text = text,
            color = ThemeWrapper.ThemeHandle.current.colors.TEXT_WHITE,
            fontSize = if (ThemeWrapper.ThemeHandle.current.isPixelDoubled) 18.sp else 9.sp,
            fontWeight = weight
        )
    }

    @Composable
    fun LatLngDetailSidePanelMenu(
        title: String?,
        poi : PoiRepository.Poi,
        centerCrossHairsVisible : Boolean = true,
        mapScale : MapScale = MapScale.METERS_400,
        buttons : List<MenuItem>
    ) {
        SidePanelMenu(
            title = title,
            text = @Composable {
                Box(Modifier.fillMaxWidth(0.66F).aspectRatio(1F), contentAlignment = Alignment.TopCenter) {
                    MapViewer(
                        overlayProperties = OverlayProperties(
                            centerCrossHairsVisible = centerCrossHairsVisible,
                            mapScaleVisible = false,
                            gpsReceptionIconVisible = false,
                            route = null,
                            poiOverlay = PoiOverlay(listOf(
                                poi.let {
                                    //TODO this could be moved to a central spot.
                                    PoiOverlay.PoiOverlayItem(
                                        label = poi.name,
                                        position = poi.location,
                                        icon = { Box{
                                            when (poi.icon) {
                                                is PoiRepository.Poi.PoiIcon.ColoredCircle -> PoiOverlay.PoiOverlayItem.CIRCLE_COLOR.invoke(poi.icon.color)
                                                is PoiRepository.Poi.PoiIcon.BundledIcon -> PoiOverlay.PoiOverlayItem.ICON_FILE.invoke(poi.icon.drawableResource, poi.icon.tint)
                                                else -> {}
                                            }
                                        }}
                                    )
                                }
                            ))
                        ),
                        extents = Extents(
                            center = poi.location.let { GeoPosition(it.latitude, it.longitude) },
                            mapScale = mapScale
                        ),
                        onCenterPositionUpdated = {}
                    )
                }

            },
            buttons
        )
    }
}


/** Inject this anywhere you want to show a modal menu */
@ExperimentalCoroutinesApi
@ApplicationScope
class ModalMenuService @Inject constructor(
    @Named(ApplicationModule.KNOB_LISTENER_MAIN)
    private val knobListenerServiceMain: KnobListenerService,
    @Named(ApplicationModule.KNOB_LISTENER_MODAL)
    private val knobListenerServiceModal: KnobListenerService,
    private val logger: Logger
) {
    private val _modalMenuOverlay = MutableStateFlow<(@Composable () -> Unit)?>(null)
    val modalMenuOverlay = _modalMenuOverlay.asStateFlow()

    data class SidePaneOverlay(
        val ui : (@Composable () -> Unit)? = null,
        val darkenBackground : Boolean = false
    )

    private val _sidePaneOverlay = MutableStateFlow(SidePaneOverlay())
    val sidePaneOverlay = _sidePaneOverlay.asStateFlow()

    val isKeyboardShowing = MutableStateFlow(false)

    interface ModalMenuDimensions {
        val menuTopLeft: IntOffset
        val menuWidth: Int
    }
    data class PixelDoubledModalMenuDimensions(
        override val menuTopLeft: IntOffset,
        override val menuWidth: Int
    ) : ModalMenuDimensions {
        fun toNormalModalMenuDimensions() = NormalModalMenuDimensions(
            menuTopLeft = menuTopLeft.let {
                IntOffset(it.x / 2, it.y / 2)
            },
            menuWidth = menuWidth / 2
        )
    }

    data class NormalModalMenuDimensions(
        override val menuTopLeft: IntOffset,
        override val menuWidth: Int
    ) : ModalMenuDimensions

    fun showModalMenu(
        dimensions: NormalModalMenuDimensions,
        menuData : ModalMenu,
        autoCloseOnSelect : Boolean = true
    ) {
        _modalMenuOverlay.value = @Composable {
            LaunchedEffect(Unit) {
                knobListenerServiceMain.disableListener()
            }
            DisposableEffect(Unit) {
                onDispose {
                    knobListenerServiceMain.enableListener()
                }
            }
            ModalChipMenuWindowOverlay(
                menuTopLeft = dimensions.menuTopLeft,
                menuWidth = dimensions.menuWidth,
                menuData = menuData.copy(
                    items = menuData.items
                        .reduceUpdateOnClick { existingOnClick ->
                            if (autoCloseOnSelect) {
                                closeModalMenu()
                            }
                            existingOnClick()
                        }.let {
                            knobListenerServiceModal
                                .listenForKnob(it,
                                    onSelectAdapter = { item, isNowSelected ->
                                        item.copy(isSelected = isNowSelected)
                                    },
                                    isSelectableAdapter = { item -> item.isSelectable },
                                    onItemClickAdapter = { item -> item.onClicked() }
                                ).value
                    }
                )
            )
        }
    }

    //This is a mix of `reduce` and `copy` that allows the caller to update
    //the onClick lambda in place.
    private fun List<ModalMenu.ModalMenuItem>.reduceUpdateOnClick(
        newOnclick : (existingOnClick : () -> Unit) -> Unit
    ) : List<ModalMenu.ModalMenuItem> {
        return this.map { item ->
            item.copy(onClicked = { newOnclick(item.onClicked) })
        }
    }

    fun closeModalMenu() {
        _modalMenuOverlay.value = null
        knobListenerServiceMain.enableListener()
    }

    fun showKeyboard(
        type : Keyboard.KeyboardType,
        prefilled : String = "",
        onCloseWithoutEntry : () -> Unit = this::closeModalMenu,
        onTextEntered : (entered : String) -> Unit,
    ) {
        isKeyboardShowing.value = true
        _modalMenuOverlay.value = {
            LaunchedEffect(Unit) {
                knobListenerServiceMain.disableListener()
            }
            DisposableEffect(Unit) {
                onDispose {
                    knobListenerServiceMain.enableListener()
                }
            }
            Keyboard.showKeyboard(
                type = type,
                prefilled = prefilled,
                knobListenerService = knobListenerServiceModal,
                onTextEntered = { isKeyboardShowing.value = false; onTextEntered(it); closeModalMenu() },
                closeWithoutEntry = { isKeyboardShowing.value = false; onCloseWithoutEntry() }
            )()
        }
    }

    fun showSidePaneOverlay(
        darkenBackground : Boolean = false,
        contents : @Composable () -> Unit
    ) {
        _sidePaneOverlay.value = SidePaneOverlay(
            ui = @Composable {
                LaunchedEffect(Unit) {
                    knobListenerServiceMain.disableListener()
                }
                DisposableEffect(Unit) {
                    onDispose {
                        knobListenerServiceMain.enableListener()
                    }
                }
                contents()
            },
            darkenBackground = darkenBackground
        )
    }

    fun closeSidePaneOverlay(clearDarkening : Boolean = false) {
        _sidePaneOverlay.value = _sidePaneOverlay.value.copy(
            ui = null,
            darkenBackground = !clearDarkening
        )
    }


    fun showDayPicker(
        startMonth : LocalDate,
        onDayPicked : (date : LocalDate) -> Unit
    ) {
        isKeyboardShowing.value = true
        _modalMenuOverlay.value = {

            val isPixelDoubled = ThemeWrapper.ThemeHandle.current.isPixelDoubled
            KeyboardViews.KeyboardPane(
                maxHeight =  0.8F
            ) {
                DisposableEffect(Unit) {
                    knobListenerServiceMain.disableListener()
                    onDispose {
                        knobListenerServiceMain.enableListener()
                    }
                }

                val knobState = remember(knobListenerServiceModal) { KnobObserverBuilderState(knobListenerServiceModal, logger) }
                val scope = rememberCoroutineScope()
                LaunchedEffect(Unit) {
                    knobState.subscribeEvents()
                }


                val currentMonth = remember { mutableStateOf(YearMonth.now()) }
                val startMonth = remember(currentMonth) { currentMonth.value.minusMonths(50) }
                val endMonth = remember(currentMonth) { currentMonth.value.plusMonths(50) }

                val daysOfTheWeek = remember { daysOfWeek() }

                val state = rememberCalendarState(
                    startMonth = startMonth,
                    endMonth = endMonth,
                    firstVisibleMonth = currentMonth.value,
                    firstDayOfWeek = daysOfTheWeek.first(),

                    )

                Column(Modifier
                    .fillMaxHeight()
                    //.aspectRatio(1F)
                    .border(4.dp, Color.Red),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {

                    Row(Modifier.fillMaxHeight()) {

                        HorizontalCalendar(
                            modifier = Modifier.aspectRatio(2F),
                            state = state,
                            userScrollEnabled = true,
                            calendarScrollPaged = false,
                            dayContent = @Composable { day ->

                                KnobObserverBuilder(knobState) { allocatedIndex: Int, currentIndex: Int ->
                                    CalendarDay(
                                        day = day,
                                        isSelected = allocatedIndex == currentIndex,
                                        onClicked = CallWhen(currentIndexIs = allocatedIndex) {}
                                    )

                                    val isToday : Boolean = day.date.toJavaLocalDate().dayOfYear ==
                                            Clock.System.now().toLocalDateTime(kotlinx.datetime.TimeZone.currentSystemDefault())
                                                .dayOfYear

                                    Box(
                                        modifier = Modifier
                                            .background(ThemeWrapper.ThemeHandle.current.colors.menuBackground)
                                            .fillMaxWidth()
                                            .border(3.dp, if (!isSelected) Color.White else ThemeWrapper.ThemeHandle.current.colors.selectedColor)
                                            .height(20.dp),
                                        //.aspectRatio(1f), // This is important for square sizing!
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Row {
                                            MenuItem(
                                                boxModifier = Modifier.weight(1f),
                                                label = day.date.dayOfMonth.toString(),
                                                labelColor = if (isSelected || isToday) {
                                                    ThemeWrapper.ThemeHandle.current.colors.selectedColor
                                                } else {
                                                    ThemeWrapper.ThemeHandle.current.colors.TEXT_WHITE
                                                },
                                                onClicked = { onClicked() }
                                            )

                                            Column {
                                                //Event chips
                                            }
                                        }
                                    }


                                }
                            }
                        )
                        Column {
                            Text("foo")
                        }

                    }
                }
            }
        }
    }
}