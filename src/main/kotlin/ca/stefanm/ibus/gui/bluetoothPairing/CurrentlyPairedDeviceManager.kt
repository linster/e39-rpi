package ca.stefanm.ibus.gui.bluetoothPairing

//This is the screen that shows us the devices that
//BlueZ is paired to, and which ones are connected to.

// DeviceName [Connected/NotConnected -- *** Forget ***]
// ...

//https://github.com/luetzel/bluez/blob/master/doc/device-api.txt#L157
//Paired and Connected

//...plus we need to register a property changed listener that goes to ProduceState
//...so that we can keep the UI up to current.
class CurrentlyPairedDeviceManager {

    //The screen that launches all the others has this.
    interface IncomingEventProducer {
        fun onPairingResult()
        fun onRequestToPairResult()
    }
}