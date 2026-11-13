package ca.stefanm.ibus.gui.pim.calendar.repo.backend

import ca.stefanm.e39pim.v1.BridgeStatus
import ca.stefanm.e39pim.v1.Calendar
import ca.stefanm.e39pim.v1.CalendarServiceGrpcKt
import ca.stefanm.e39pim.v1.ChangeNotice
import ca.stefanm.e39pim.v1.Event
import ca.stefanm.e39pim.v1.EventOccurrence
import ca.stefanm.e39pim.v1.StatusServiceGrpcKt
import ca.stefanm.e39pim.v1.createEventRequest
import ca.stefanm.e39pim.v1.deleteEventRequest
import ca.stefanm.e39pim.v1.getEventRequest
import ca.stefanm.e39pim.v1.updateEventRequest
import com.google.protobuf.fieldMask
import ca.stefanm.e39pim.v1.getStatusRequest
import ca.stefanm.e39pim.v1.listCalendarsRequest
import ca.stefanm.e39pim.v1.listEventsRequest
import ca.stefanm.e39pim.v1.watchChangesRequest
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.datetime.TimeZone
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import kotlin.time.Instant

/**
 * The transport layer for calendars: e39pim.v1 CalendarService and
 * StatusService calls, with protobuf messages in and out.
 * [RemoteCalendarRepository] builds on it with the HMI's types. Every call
 * throws [PimBackendException] on failure.
 */
class CalendarGrpcClient @Inject constructor(
    private val backendChannel: PimBackendChannel,
) {
    private fun calendarStub() = CalendarServiceGrpcKt.CalendarServiceCoroutineStub(backendChannel.channel())
    private fun statusStub() = StatusServiceGrpcKt.StatusServiceCoroutineStub(backendChannel.channel())

    private fun <S : io.grpc.kotlin.AbstractCoroutineStub<S>> S.withTimeout(): S =
        withDeadlineAfter(PimBackendChannel.CALL_TIMEOUT_SECONDS, TimeUnit.SECONDS)

    suspend fun getStatus(): BridgeStatus = pimCall {
        statusStub().withTimeout().getStatus(getStatusRequest { })
    }

    suspend fun listCalendars(): List<Calendar> = pimCall {
        calendarStub().withTimeout().listCalendars(listCalendarsRequest { }).calendarsList
    }

    /**
     * Occurrences overlapping [[start], [end]), repeating events expanded by
     * the bridge. [zone] places all-day events and floating times.
     */
    suspend fun listEvents(
        start: Instant,
        end: Instant,
        zone: TimeZone,
        calendarIds: List<String> = emptyList(),
    ): List<EventOccurrence> = pimCall {
        calendarStub().withTimeout().listEvents(listEventsRequest {
            this.start = start.toTimestamp()
            this.end = end.toTimestamp()
            timeZone = zone.id
            this.calendarIds.addAll(calendarIds)
        }).occurrencesList
    }

    suspend fun getEvent(eventId: String, recurrenceId: Instant?, zone: TimeZone): Event = pimCall {
        calendarStub().withTimeout().getEvent(getEventRequest {
            this.eventId = eventId
            if (recurrenceId != null) this.recurrenceId = recurrenceId.toTimestamp()
            timeZone = zone.id
        })
    }

    /** Creates [event] (server-set fields ignored) in calendar [calendarId]. */
    suspend fun createEvent(calendarId: String, event: Event): Event = pimCall {
        calendarStub().withTimeout().createEvent(createEventRequest {
            this.calendarId = calendarId
            this.event = event
        })
    }

    /**
     * Changes the [paths] of the event (occurrence, if event.recurrenceId is
     * set) named by event.eventId, based on event.revision. Empty [paths]:
     * all editable fields.
     */
    suspend fun updateEvent(event: Event, paths: Collection<String>): Event = pimCall {
        calendarStub().withTimeout().updateEvent(updateEventRequest {
            this.event = event
            if (paths.isNotEmpty()) updateMask = fieldMask { this.paths.addAll(paths) }
        })
    }

    suspend fun deleteEvent(eventId: String, recurrenceId: Instant?, revision: Long) {
        pimCall {
            calendarStub().withTimeout().deleteEvent(deleteEventRequest {
                this.eventId = eventId
                if (recurrenceId != null) this.recurrenceId = recurrenceId.toTimestamp()
                this.revision = revision
            })
        }
    }

    /**
     * The bridge's change notices: `ready` first, then what changed. Open
     * until the bridge goes away (then it throws [PimBackendException]) or
     * the collector stops.
     */
    fun watchChanges(): Flow<ChangeNotice> =
        calendarStub().watchChanges(watchChangesRequest { })
            .catch { throw PimBackendException.from(it) }
}
