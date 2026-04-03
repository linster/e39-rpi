package ca.stefanm.ca.stefanm.ibus.gui.networkSetup.activateConnection.dbus

import ca.stefanm.ca.stefanm.ibus.gui.networkSetup.activateConnection.dbus.types.NMDeviceType
import ca.stefanm.ca.stefanm.ibus.gui.networkSetup.activateConnection.dbus.types.NMDeviceType.Companion.toNMDeviceType
import org.freedesktop.networkmanager.Device
import javax.inject.Inject

class GetDisambiguatedDeviceNameUseCase @Inject constructor(

) {

    //Lib NM has a method to disambiguate device names based on
    //DeviceType. It's not exported via DBus, but I can reimplement it here.

    //From nmt-connect-connection-list.c:
    // Line 470: names       = nm_device_disambiguate_names((NMDevice **) devices->pdata, devices->len);

    // Goes to nm_device_disambiguate_names in nm-device.c, Line 2210

    fun getDisambiguatedNames(devices : List<Device>) : Map<Device, String> {
        /* Generic Device name */
        //Loop over the devices and for each one call
        //r = get_device_generic_type_name_with_iface()
        //If no duplicates, return a listOf(device to r).associate()
        return mapOf()
    }

    /* get_device_generic_type_name_with_iface(NMDevice *device)
     * https://github.com/NetworkManager/NetworkManager/blob/de91bd807096255e0f8a5be1ff40180e93bd31f9/src/libnm-client-impl/nm-device.c#L1916
     */
    private fun getDeviceGenericeTypeNameWithIFace(device: Device) : String {
        when (device.deviceType.toNMDeviceType()!!) {
            NMDeviceType.NM_DEVICE_TYPE_ETHERNET,
            NMDeviceType.NM_DEVICE_TYPE_INFINIBAND -> "Wired"
            else ->
        }
    }

    /*
     * https://github.com/NetworkManager/NetworkManager/blob/de91bd807096255e0f8a5be1ff40180e93bd31f9/src/libnm-client-impl/nm-device.c#L1900
     * get_device_type_name_with_iface(NMDevice *device)
     */
    private fun getDeviceTypeNameWithIFace(device : Device) : String {

    }


    //https://github.com/NetworkManager/NetworkManager/blob/main/src/libnm-core-public/nm-dbus-interface.h
    fun getDeviceTypeNameForIntDeviceType(type : Int) : String {

    }
}