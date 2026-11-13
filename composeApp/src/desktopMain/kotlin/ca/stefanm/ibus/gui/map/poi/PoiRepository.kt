package ca.stefanm.ibus.gui.map.poi

import androidx.compose.ui.graphics.Color
import ca.stefanm.ibus.di.ApplicationScope
import ca.stefanm.ibus.gui.map.repo.RemotePoiRepository
import ca.stefanm.ibus.gui.menu.Notification
import ca.stefanm.ibus.gui.menu.notifications.NotificationHub
import ca.stefanm.ibus.lib.logging.Logger
import com.javadocmd.simplelatlng.LatLng
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.DrawableResource
import javax.inject.Inject

/**
 * The POIs for the screens (POI manager, selector, editor, map overlay).
 *
 * The POIs used to be in a local SQLDelight database; now the maps backend
 * (e39-rpi-maps) stores them, and this class keeps the screens' simple API on
 * top of [RemotePoiRepository] (ca.stefanm.ibus.gui.map.repo). Reads come from
 * its cached list; changes are sent in the background, and a failure (e.g.
 * the backend isn't running) shows a notification.
 */
@ApplicationScope
class PoiRepository @Inject constructor(
    private val remotePoiRepository: RemotePoiRepository,
    private val notificationHub: NotificationHub,
    private val coroutineScope: CoroutineScope,
    private val logger: Logger,
) {

    companion object {
        const val TAG = "PoiRepository"
    }

    data class Poi(
        val name : String,
        val location : LatLng,
        val icon : PoiIcon,
        val isVisible : Boolean,
        /** The backend's id; null for a POI that isn't saved yet. */
        val id : Long? = null,
    ) {
        sealed class PoiIcon(val type : String) {
            object NoIcon : PoiIcon(type = "NoIcon")
            data class ColoredCircle(val color: Color) : PoiIcon("ColoredCircle")
            data class BundledIcon(val drawableResource : DrawableResource, val tint : Color = Color.White) : PoiIcon("BundledIcon")
        }
    }

    private fun inBackground(failureText: String, block: suspend () -> Result<*>) {
        coroutineScope.launch(Dispatchers.IO) {
            block().onFailure {
                notificationHub.postNotification(
                    Notification(
                        Notification.NotificationImage.ALERT_CIRCLE,
                        topText = failureText,
                        contentText = it.message ?: "Maps backend error"
                    )
                )
            }
        }
    }

    /** Creates [new] if it has no id yet, otherwise updates the POI with its id. */
    fun saveOrUpdatePoi(
        new : Poi
    ) {
        logger.d(TAG, "saveOrUpdatePoi: $new")
        inBackground("Can't save POI.") { remotePoiRepository.save(new) }
    }

    fun getAllPois() : List<Poi> = remotePoiRepository.pois.value

    fun getAllPoisFlow() : Flow<List<Poi>> = remotePoiRepository.pois

    fun getAllVisiblePoisFlow() : Flow<List<Poi>> =
        remotePoiRepository.pois.map { pois -> pois.filter { it.isVisible } }

    fun deletePoi(poi: Poi) {
        inBackground("Can't delete POI.") { remotePoiRepository.delete(poi) }
    }

    fun hideAllPois() {
        inBackground("Can't hide POIs.") { remotePoiRepository.setAllVisible(false) }
    }

    fun showAllPois() {
        inBackground("Can't show POIs.") { remotePoiRepository.setAllVisible(true) }
    }
}
