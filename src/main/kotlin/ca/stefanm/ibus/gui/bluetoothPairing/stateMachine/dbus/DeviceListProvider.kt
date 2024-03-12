package ca.stefanm.ibus.gui.bluetoothPairing.stateMachine.dbus

import ca.stefanm.ibus.gui.bluetoothPairing.stateMachine.DBusConnectionDependingComponent
import ca.stefanm.ibus.lib.logging.Logger
import com.github.hypfvieh.bluetooth.wrapper.BluetoothAdapter
import com.github.hypfvieh.bluetooth.wrapper.BluetoothDevice
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.yield
import javax.inject.Inject

//This doesn't handle the actual pairing, it's just to draw the list of devices that are paired and connected.
//A selection from here will let us kick off the Pair -> Connect process.
class DeviceListProvider @Inject constructor(
    private val dBusConnectionOwningComponent: DBusConnectionOwner,
    private val logger: Logger
) : DBusConnectionDependingComponent {

    private lateinit var adapter : BluetoothAdapter

    override fun onSetup() {
        with (dBusConnectionOwningComponent.getDeviceManager()) {
            this@DeviceListProvider.adapter = getAdapter() ?: error("No adapter")
        }
    }

    override fun onCleanup() {
        if (adapter.isDiscovering) {
            adapter.stopDiscovery()
        }
    }

    fun getDeviceForMac(macAddress: String) : BluetoothDevice {
        return dBusConnectionOwningComponent.getDeviceManager()
            .devices
            .first { it.address == macAddress }
    }

    fun getDevices() : Flow<List<BluetoothDevice>> {
        return flow {
            //Using a ticker flow while we have a listener isn't ideal.
            while (true) {
                emit(Unit)
                kotlinx.coroutines.delay(3000)
                yield()
            }
        }.map {
            dBusConnectionOwningComponent.getDeviceManager().findBtDevicesByIntrospection(adapter)
            dBusConnectionOwningComponent.getDeviceManager().devices.toList()
        }.onStart {
                logger.d("DeviceListProvider", "getDevices() onStart")
                adapter.startDiscovery()
        }.onCompletion {
            logger.d("DeviceListProvider", "getDevices() onCompletion")
        }
    }

    fun getLiveDevice(macAddress : String) : Flow<BluetoothDevice> = flow {
        //Using a ticker flow while we have a listener isn't ideal.
        while (true) {
            emit(Unit)
            kotlinx.coroutines.delay(3000)
            yield()
        }
    }.mapNotNull {
        dBusConnectionOwningComponent.getDeviceManager().devices.firstOrNull { it.address == macAddress }
    }.onStart {
        logger.d("DeviceListProvider", "getLiveDevice() onStart")
        adapter.startDiscovery()
    }.onCompletion {
        logger.d("DeviceListProvider", "getLiveDevice() onCompletion")
    }
}