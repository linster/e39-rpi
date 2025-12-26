package ca.stefanm.ibus.gui.menu.widgets.modalMenu

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import ca.stefanm.ibus.gui.menu.Notification
import ca.stefanm.ibus.gui.menu.notifications.NotificationHub
import ca.stefanm.ibus.gui.menu.widgets.*
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
import ca.stefanm.ibus.gui.pim.calendar.views.parts.NorthButtonRow
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
import kotlinx.datetime.*
import kotlinx.datetime.format.Padding
import kotlinx.datetime.format.char
import org.jxmapviewer.viewer.GeoPosition
import java.time.DateTimeException
import java.time.format.DateTimeParseException
import java.time.format.TextStyle
import java.util.*
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
    private val logger: Logger,
    private val notificationHub: NotificationHub
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
        startMonthAt : YearMonth = YearMonth.now(),
        onDayPicked : (date : LocalDate) -> Unit
    ) {
        isKeyboardShowing.value = true
        _modalMenuOverlay.value = {

            val isPixelDoubled = ThemeWrapper.ThemeHandle.current.isPixelDoubled
            KeyboardViews.KeyboardPane(
                maxHeight =  0.7F
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


                val currentMonth = remember { mutableStateOf(startMonthAt) }
                val startMonth = remember(currentMonth) { currentMonth.value.minusMonths(20) }
                val endMonth = remember(currentMonth) { currentMonth.value.plusMonths(20) }

                val daysOfTheWeek = remember { daysOfWeek() }

                val state = rememberCalendarState(
                    startMonth = startMonth,
                    endMonth = endMonth,
                    firstVisibleMonth = currentMonth.value,
                    firstDayOfWeek = daysOfTheWeek.first(),

                    )

                Column(Modifier
                    .fillMaxHeight(),
                    //.aspectRatio(1F)
                    //.border(4.dp, Color.Red),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {

                    Row(Modifier.fillMaxHeight()) {

                        Column(Modifier.weight(0.5F)) {
                            Row(
                                modifier = Modifier
                                    .background(ThemeWrapper.ThemeHandle.current.colors.menuBackground)
                                    .fillMaxWidth()
                            ) {
                                for (day in daysOfTheWeek) {
                                    MenuItem(
                                        boxModifier = Modifier.weight(1f),
                                        label = day.toJavaDayOfWeek()
                                            .getDisplayName(TextStyle.NARROW, Locale.getDefault()),
                                        isSmallSize = true,
                                        onClicked = {}
                                    )
                                }
                            }
                            HorizontalCalendar(
                                modifier = Modifier.fillMaxWidth(),
                                state = state,
                                userScrollEnabled = true,
                                calendarScrollPaged = false,
                                dayContent = @Composable { day ->

                                    KnobObserverBuilder(knobState) { allocatedIndex: Int, currentIndex: Int ->
                                        val isSelected = allocatedIndex == currentIndex


                                        val isToday: Boolean = day.date.toJavaLocalDate().dayOfYear ==
                                                Clock.System.now()
                                                    .toLocalDateTime(kotlinx.datetime.TimeZone.currentSystemDefault())
                                                    .dayOfYear

                                        Box(
                                            modifier = Modifier
                                                .background(ThemeWrapper.ThemeHandle.current.colors.menuBackground)
                                                .fillMaxWidth()
                                                .border(
                                                    3.dp,
                                                    if (!isSelected) Color.White else ThemeWrapper.ThemeHandle.current.colors.selectedColor
                                                )
                                                .clickable {
                                                    isKeyboardShowing.value = false
                                                    _modalMenuOverlay.value = null
                                                    onDayPicked(day.date)
                                                }
                                                .wrapContentHeight(),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Row {

                                                Text(
                                                    text = day.date.dayOfMonth.toString(),
                                                    color = if (isSelected) {
                                                        ThemeWrapper.ThemeHandle.current.colors.selectedColor
                                                    } else if (isToday) {
                                                        Color.Red
                                                    } else {
                                                        ThemeWrapper.ThemeHandle.current.colors.TEXT_WHITE
                                                    },
                                                    fontSize = 18.sp,
                                                    modifier = Modifier
                                                        .padding(
                                                            top = 5.dp.halveIfNotPixelDoubled(),
                                                            bottom = 5.dp.halveIfNotPixelDoubled(),
                                                            start = 25.dp.halveIfNotPixelDoubled()
                                                        )
                                                )
                                            }
                                        }
                                    }
                                }
                            )
                        }
                        Column(Modifier.weight(0.5F)) {
                            MenuItem(
                                boxModifier = Modifier.fillMaxWidth(),
                                label = "${currentMonth.value.month.toJavaMonth().getDisplayName(TextStyle.SHORT, Locale.CANADA)} ${currentMonth.value.year}",
                                chipOrientation = ItemChipOrientation.NONE,
                                isSelected = false,
                                isSmallSize = true,
                                onClicked = {}
                            )
                            KnobObserverBuilder(knobState) { allocatedIndex, currentIndex ->
                                MenuItem(
                                    boxModifier = Modifier.fillMaxWidth(),
                                    label = "Scroll to prev month",
                                    chipOrientation = ItemChipOrientation.E,
                                    isSelected = currentIndex == allocatedIndex,
                                    isSmallSize = true,
                                    onClicked = CallWhen(currentIndexIs = allocatedIndex) {
                                        currentMonth.value = currentMonth.value.minusMonth()
                                    }
                                )
                            }
                            KnobObserverBuilder(knobState) { allocatedIndex, currentIndex ->
                                MenuItem(
                                    boxModifier = Modifier.fillMaxWidth(),
                                    label = "Scroll to next month",
                                    chipOrientation = ItemChipOrientation.E,
                                    isSelected = currentIndex == allocatedIndex,
                                    isSmallSize = true,
                                    onClicked = CallWhen(currentIndexIs = allocatedIndex) {
                                        currentMonth.value = currentMonth.value.plusMonth()
                                    }
                                )
                            }

                            KnobObserverBuilder(knobState) { allocatedIndex, currentIndex ->
                                MenuItem(
                                    boxModifier = Modifier.fillMaxWidth(),
                                    label = "Scroll to today's month",
                                    chipOrientation = ItemChipOrientation.E,
                                    isSelected = currentIndex == allocatedIndex,
                                    isSmallSize = true,
                                    onClicked = CallWhen(currentIndexIs = allocatedIndex) {
                                        currentMonth.value = YearMonth.now()
                                    }
                                )
                            }

                            KnobObserverBuilder(knobState) { allocatedIndex, currentIndex ->
                                MenuItem(
                                    boxModifier = Modifier.fillMaxWidth(),
                                    label = "Close without selection",
                                    chipOrientation = ItemChipOrientation.E,
                                    isSelected = currentIndex == allocatedIndex,
                                    isSmallSize = true,
                                    onClicked = CallWhen(currentIndexIs = allocatedIndex) {
                                        _modalMenuOverlay.value = null
                                    }
                                )
                            }
                        }

                    }
                }
            }
        }
    }

    fun showTimePicker(
        startTime : LocalTime,
        onLocalTimePicked : (LocalTime) -> Unit
    ) {

        //This thing should actually check that the format returned from the keyboard is okay
        //before returning it.

        val prefilledText = startTime.format(LocalTime.Format {
            hour() ; char(':') ; minute()
        })

        showKeyboard(
            type = KeyboardType.TIME_PICKER,
            prefilled = prefilledText,
            onTextEntered = {

                //If the text has "AM" or "PM" in it, parse it as an AM/PM time
                if (it.contains("AM") || it.contains("PM")) {
                    runCatching { LocalTime.parse(it, LocalTime.Format {
                        amPmHour(padding = Padding.NONE)
                        char(':')
                        minute()
                        char(' ')
                        amPmMarker("AM", "PM")
                    })}.fold(
                        onSuccess = { parsed -> onLocalTimePicked(parsed)},
                        onFailure = { throwable ->
                            if (throwable.cause is IllegalArgumentException && throwable?.cause?.cause is DateTimeException) {
                                logger.e("TimePicker", "AM/PM exception", throwable)
                                notificationHub.postNotificationBackground(Notification(
                                    Notification.NotificationImage.ALERT_TRIANGLE,
                                    "Invalid time",
                                    "Time should be HH:MM AM/PM"
                                ))
                            } else {
                                logger.e("TimePicker", "AM/PM other exception", throwable)
                                notificationHub.postNotificationBackground(Notification(
                                    Notification.NotificationImage.ALERT_TRIANGLE,
                                    "Format Exception",
                                    "Could not parse LocalTime: ${throwable.message}"
                                ))
                            }
                        }
                    )
                } else {
                    runCatching { LocalTime.parse(it, LocalTime.Format {
                        hour(padding = Padding.NONE)
                        char(':')
                        minute()
                    })}.fold(
                        onSuccess = { parsed -> onLocalTimePicked(parsed)},
                        onFailure = { throwable ->
                            if (throwable.cause is IllegalArgumentException && throwable?.cause?.cause is DateTimeException) {
                                logger.e("TimePicker", "parse exception", throwable)
                                notificationHub.postNotificationBackground(Notification(
                                    Notification.NotificationImage.ALERT_TRIANGLE,
                                    "Invalid time",
                                    "Time should be HH:MM"
                                ))
                            } else {
                                logger.e("TimePicker", "parse exception", throwable)
                                notificationHub.postNotificationBackground(Notification(
                                    Notification.NotificationImage.ALERT_TRIANGLE,
                                    "Format Exception",
                                    "Could not parse LocalTime: ${throwable.message}"
                                ))
                            }
                        }
                    )

                }
            }
        )
    }
}