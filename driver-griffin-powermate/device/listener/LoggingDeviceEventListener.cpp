//
// Created by stefan on 1/1/26.
//

#include "LoggingDeviceEventListener.h"
namespace e39rpi::powermate::device::listener {

    void LoggingDeviceEventListener::onKnobLongPressed() {
        logger->d(TAG, "onKnobLongPressed()");
    }

    void LoggingDeviceEventListener::onKnobPressed(bool isPressed) {
        logger->d(TAG, fmt::format("onKnobPressed({})", isPressed));
    }

    void LoggingDeviceEventListener::onKnobTurnClockwise(bool isPressed) {
        logger->d(TAG, fmt::format("onKnobTurnClockwise({})", isPressed));
    }

    void LoggingDeviceEventListener::onKnobTurnCounterClockwise(bool isPressed) {
        logger->d(TAG, fmt::format("onKnobTurnCounterClockwise({})", isPressed));
    }


} // listener