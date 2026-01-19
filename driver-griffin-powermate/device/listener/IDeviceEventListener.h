//
// Created by stefan on 1/1/26.
//

#ifndef DRIVER_GRIFFIN_POWERMATE_IDEVICEEVENTLISTENER_H
#define DRIVER_GRIFFIN_POWERMATE_IDEVICEEVENTLISTENER_H

namespace e39rpi::powermate::device::listener {

    /** An interface to listen to events coming out of the knob. */
    class IDeviceEventListener {

        /**
         * Called when the knob is turned CCW
         * @param isPressed is the knob pressed down while the turn is happening?
         */
        virtual void onKnobTurnCounterClockwise(bool isPressed) = 0;
        virtual void onKnobTurnClockwise(bool isPressed) = 0;

        /** Called when the knob is clicked briefly */
        virtual void onKnobPressed(bool isPressed) = 0;

        virtual void onKnobLongPressed() = 0;



    };


};
#endif //DRIVER_GRIFFIN_POWERMATE_IDEVICEEVENTLISTENER_H
