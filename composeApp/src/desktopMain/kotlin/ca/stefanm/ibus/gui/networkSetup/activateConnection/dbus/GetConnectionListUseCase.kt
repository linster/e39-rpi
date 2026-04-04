package ca.stefanm.ca.stefanm.ibus.gui.networkSetup.activateConnection.dbus

import ca.stefanm.ibus.lib.logging.Logger
import kotlinx.coroutines.flow.map
import org.freedesktop.networkmanager.Device
import org.freedesktop.networkmanager.settings.Connection
import javax.inject.Inject

class GetConnectionListUseCase @Inject constructor(
    private val getDevicesUseCase: GetDevicesUseCase,
    private val getDisambiguatedDeviceNameUseCase: GetDisambiguatedDeviceNameUseCase,
    private val getConnectionsForDeviceUseCase: GetConnectionsForDeviceUseCase,
    private val logger: Logger
) {

    data class CollatedDeviceInformation(
        val device: Device,
        val disambiguatedName: String? = null,
        val connectionList : List<Connection>? = null
    )

    fun getConnectionList() {
        getDevicesUseCase
            .getDevices()
            .map {
                getDisambiguatedDeviceNameUseCase.getDisambiguatedNames(devices = it)
            }
            .map {
                it.entries.map { (device, name) ->
                    CollatedDeviceInformation(
                        device = device,
                        disambiguatedName = name
                    )
                }
            }
            .map {
                //Get the sort order
            }
            .map {

            }
    }
}