package ca.stefanm.ibus.gui.menu.widgets.modalMenu

import androidx.compose.foundation.Image
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ca.stefanm.ibus.di.ApplicationModule
import ca.stefanm.ibus.gui.map.poi.PoiRepository
import ca.stefanm.ibus.gui.menu.widgets.themes.ThemeWrapper
import ca.stefanm.ibus.di.ApplicationScope
import ca.stefanm.ibus.gui.chat.screens.setup.LoginScreen.LoadingPictures
import ca.stefanm.ibus.gui.map.Extents
import ca.stefanm.ibus.gui.map.MapViewer
import ca.stefanm.ibus.gui.map.OverlayProperties
import ca.stefanm.ibus.gui.map.PoiOverlay
import ca.stefanm.ibus.gui.map.widget.MapScale
import ca.stefanm.ibus.gui.menu.MenuWindow
import ca.stefanm.ibus.gui.menu.Notification
import ca.stefanm.ibus.gui.menu.notifications.NotificationHub
import ca.stefanm.ibus.gui.menu.notifications.toView
import ca.stefanm.ibus.gui.menu.widgets.*
import ca.stefanm.ibus.gui.menu.widgets.knobListener.KnobListenerService
import ca.stefanm.ibus.gui.menu.widgets.knobListener.dynamic.KnobObserverBuilder
import ca.stefanm.ibus.gui.menu.widgets.knobListener.dynamic.KnobObserverBuilderState
import ca.stefanm.ibus.gui.menu.widgets.knobListener.dynamic.KnobObserverBuilderState.Companion.setupListener
import ca.stefanm.ibus.gui.menu.widgets.modalMenu.keyboard.Keyboard
import ca.stefanm.ibus.gui.menu.widgets.modalMenu.keyboard.Keyboard.KeyboardType
import ca.stefanm.ibus.gui.menu.widgets.modalMenu.keyboard.KeyboardViews
import ca.stefanm.ibus.gui.menu.widgets.screenMenu.HalfScreenMenu
import ca.stefanm.ibus.gui.menu.widgets.screenMenu.MenuItem
import ca.stefanm.ibus.gui.menu.widgets.screenMenu.TextMenuItem
import ca.stefanm.ibus.gui.pim.calendar.views.CalendarDay
import ca.stefanm.ibus.gui.pim.calendar.views.parts.NorthButtonRow
import ca.stefanm.ibus.lib.logging.Logger
import ca.stefanm.ibus.resources.Res
import ca.stefanm.ibus.resources.notification_alert_circle
import ca.stefanm.ibus.resources.notification_alert_octagon
import ca.stefanm.ibus.resources.notification_alert_triangle
import ca.stefanm.ibus.resources.notification_bluetooth
import ca.stefanm.ibus.resources.notification_map
import ca.stefanm.ibus.resources.notification_map_pin
import ca.stefanm.ibus.resources.notification_message_circle
import ca.stefanm.ibus.resources.notification_message_square
import ca.stefanm.ibus.resources.notification_music
import ca.stefanm.ibus.resources.notification_navigation
import ca.stefanm.ibus.resources.notification_phone
import ca.stefanm.ibus.resources.notification_phone_incoming
import ca.stefanm.ibus.resources.notification_phone_missed
import ca.stefanm.ibus.resources.notification_voicemail
import com.ginsberg.cirkle.circular
import com.javadocmd.simplelatlng.LatLng
import com.kizitonwose.calendar.compose.HorizontalCalendar
import com.kizitonwose.calendar.compose.rememberCalendarState
import com.kizitonwose.calendar.core.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.datetime.*
import kotlinx.datetime.format.Padding
import kotlinx.datetime.format.char
import org.jetbrains.compose.resources.painterResource
import org.jxmapviewer.viewer.GeoPosition
import java.time.DateTimeException
import java.time.format.DateTimeParseException
import java.time.format.TextStyle
import java.util.*
import javax.inject.Inject
import javax.inject.Named
import kotlin.time.Clock
import kotlin.time.Duration

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
                            center = poi.location,
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
        _modalMenuOverlay.value = @Composable {

            LaunchedEffect(Unit) {
                logger.d("DayPicker", "Disabling main listener.")
                knobListenerServiceMain.disableListener()
            }
            DisposableEffect(Unit) {
                onDispose {
                    logger.d("DayPicker", "Re-enabling main listener.")
                    knobListenerServiceMain.enableListener()
                }
            }

            val isPixelDoubled = ThemeWrapper.ThemeHandle.current.isPixelDoubled
            KeyboardViews.KeyboardPane(
                maxHeight =  0.7F
            ) {

                val knobState = setupListener(
                    knobListenerServiceModal,
                    logger,
                    "dayPicker"
                )

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

                                        CallWhen(currentIndexIs = allocatedIndex) {
                                            closeModalMenu()
                                            onDayPicked(day.date)
                                        }

                                        Box(
                                            modifier = Modifier
                                                .background(ThemeWrapper.ThemeHandle.current.colors.menuBackground)
                                                .fillMaxWidth()
                                                .border(
                                                    3.dp,
                                                    if (!isSelected) Color.White else ThemeWrapper.ThemeHandle.current.colors.selectedColor
                                                )
                                                .clickable {
                                                    closeModalMenu()
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
                                        closeModalMenu()
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

    fun showModalWaitDialog(
        image : Notification.NotificationImage = Notification.NotificationImage.NONE,
        throbber : Boolean = false,
        headerText : String,
        bodyText : String = "",
        titleText : String? = null,
        autoCloseTimeout : Duration?,
        isCancellable : Boolean = false,
        onCancel : () -> Unit = {}
    ) {
        _modalMenuOverlay.value = @Composable {
            val isPixelDoubled = ThemeWrapper.ThemeHandle.current.isPixelDoubled

            LaunchedEffect(Unit) {
                logger.d("ModalWaitDialog", "Disabling main listener.")
                knobListenerServiceMain.disableListener()
            }
            DisposableEffect(Unit) {
                onDispose {
                    logger.d("ModalWaitDialog", "Re-enabling main listener.")
                    knobListenerServiceMain.enableListener()
                }
            }

            val scope = rememberCoroutineScope()
            if (autoCloseTimeout != null) {
                scope.launch {
                    delay(autoCloseTimeout)
                    closeModalMenu()
                }
            }
            Box(Modifier
                .fillMaxSize()
                //.background(Color.Yellow)
                , contentAlignment = Alignment.Center) {
                Box(Modifier
                    .border(
                        width = if (isPixelDoubled) 4.dp else 2.dp,
                        color = ThemeWrapper.ThemeHandle.current.colors.menuBackground
                    )
                    .shadow(
                        20.dp, spotColor = Color.White
                    )
                    .background(
                        Brush.horizontalGradient(
                            ThemeWrapper.ThemeHandle.current.centerGradientWithEdgeHighlight.backgroundGradientColorList
                        )
                    )
                    //.wrapContentHeight()
                    .fillMaxWidth(0.75F)
                    .aspectRatio(3F, matchHeightConstraintsFirst = false)

                ) {
                    Column(Modifier
                        .fillMaxHeight()
                        .fillMaxWidth()
                        .border(4.dp, Color.Magenta)
                        ,
                        verticalArrangement = Arrangement.Center
                    ) {
                        if (titleText != null) {
                            BmwSingleLineHeader(titleText)
                        }

                        Row(
                            modifier = Modifier.padding(8.dp)
                        ) {
                            Box(
                                Modifier
                                    .fillMaxHeight(0.7F)
                                    .aspectRatio(1F)
                                //    .background(Color.Green)
                                ,
                                contentAlignment = Alignment.Center
                            ) {

                                if (!throbber) {
                                    if (image == Notification.NotificationImage.NONE) {
                                        Spacer(
                                            Modifier
                                                .fillMaxSize(0.9F)
                                                //.fillMaxSize(0.75F)
                                                //.border(2.dp, Color.Red)
                                                .aspectRatio(1.0F)
                                        )
                                    } else {
                                        val resource = painterResource(
                                            when (image) {
                                                Notification.NotificationImage.NONE -> error("Invalid")
                                                Notification.NotificationImage.ALERT_CIRCLE -> Res.drawable.notification_alert_circle
                                                Notification.NotificationImage.ALERT_OCTAGON -> Res.drawable.notification_alert_octagon
                                                Notification.NotificationImage.ALERT_TRIANGLE -> Res.drawable.notification_alert_triangle
                                                Notification.NotificationImage.BLUETOOTH -> Res.drawable.notification_bluetooth
                                                Notification.NotificationImage.MESSAGE_CIRCLE -> Res.drawable.notification_message_circle
                                                Notification.NotificationImage.MESSAGE_SQUARE -> Res.drawable.notification_message_square
                                                Notification.NotificationImage.MUSIC -> Res.drawable.notification_music
                                                Notification.NotificationImage.PHONE -> Res.drawable.notification_phone
                                                Notification.NotificationImage.PHONE_INCOMING -> Res.drawable.notification_phone_incoming
                                                Notification.NotificationImage.PHONE_MISSED -> Res.drawable.notification_phone_missed
                                                Notification.NotificationImage.VOICE_MAIL -> Res.drawable.notification_voicemail
                                                Notification.NotificationImage.MAP_GENERAL -> Res.drawable.notification_map
                                                Notification.NotificationImage.MAP_INSTRUCTION -> Res.drawable.notification_navigation
                                                Notification.NotificationImage.MAP_WAYPOINT -> Res.drawable.notification_map_pin
                                            }
                                        )
                                        Image(
                                            painter = resource,
                                            contentDescription = image.toString(),
                                            modifier = Modifier
                                                .fillMaxSize(0.9F)
                                                //.fillMaxSize(0.75F)
                                                //.border(2.dp, Color.Red)
                                                .aspectRatio(1.0F)
                                        )
                                    }
                                } else {
                                    val pictureScope = rememberCoroutineScope()
                                    val pictureList = LoadingPictures.values().asList().circular()
                                    val pictureIndex = remember { mutableStateOf(0) }
                                    LaunchedEffect(Unit) {
                                        while (isActive) {
                                            delay(1500)
                                            pictureIndex.value += 1
                                        }
                                    }
                                    key(pictureIndex.value) {
                                        Image(
                                            painter = painterResource(pictureList[pictureIndex.value].drawableResource),
                                            contentDescription = pictureList[pictureIndex.value].name,
                                            contentScale = ContentScale.FillHeight,
                                            modifier = Modifier.fillMaxSize(0.9F).aspectRatio(1F)
                                        )
                                    }
                                }
                            }

                            Column(
                                modifier = Modifier.weight(3F, true).align(Alignment.CenterVertically).padding(8.dp),
                                verticalArrangement = Arrangement.Center
                            ) {
                                if (headerText.isNotBlank()) {
                                    Text(
                                        text = headerText,
                                        fontSize = if (isPixelDoubled) 32.sp else 16.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                }
                                if (bodyText.isNotBlank()) {
                                    Text(
                                        text = bodyText,
                                        fontSize = if (isPixelDoubled) 26.sp else 13.sp,
                                        color = Color.White
                                    )
                                }
                            }
                        }

                        if (isCancellable) {
                            val knobState = setupListener(
                                knobListenerServiceModal,
                                logger,
                                "ModalWaitDialog"
                            )



                            KnobObserverBuilder(knobState) { allocatedIndex, currentIndex ->
                                MenuItem(
                                    boxModifier = Modifier
                                        .align(Alignment.End)
                                        .fillMaxWidth(0.5F)
                                    ,
                                    label = "Cancel",
                                    chipOrientation = ItemChipOrientation.E,
                                    isSelected = currentIndex == allocatedIndex,
                                    isSmallSize = true,
                                    onClicked = CallWhen(currentIndexIs = allocatedIndex) {
                                        onCancel()
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }

    }
}