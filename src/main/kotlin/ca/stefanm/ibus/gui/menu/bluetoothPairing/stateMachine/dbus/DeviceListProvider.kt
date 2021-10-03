package ca.stefanm.ibus.gui.menu.bluetoothPairing.stateMachine.dbus

import ca.stefanm.ibus.gui.menu.bluetoothPairing.stateMachine.DBusConnectionDependingComponent
import ca.stefanm.ibus.gui.menu.bluetoothPairing.stateMachine.DBusConnectionOwningComponent
import ca.stefanm.ibus.gui.menu.bluetoothPairing.ui.PairableDeviceChooser
import com.github.hypfvieh.bluetooth.wrapper.BluetoothAdapter
import com.github.hypfvieh.bluetooth.wrapper.BluetoothDevice
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.yield
import javax.inject.Inject

//This doesn't handle the actual pairing, it's just to draw the list of devices that are paired and connected.
//A selection from here will let us kick off the Pair -> Connect process.
class DeviceListProvider @Inject constructor(
    private val dBusConnectionOwningComponent: DBusConnectionOwner
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
                adapter.startDiscovery()
        }.onCompletion {
                adapter.stopDiscovery()
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
        adapter.startDiscovery()
    }.onCompletion {
        adapter.stopDiscovery()
    }
}