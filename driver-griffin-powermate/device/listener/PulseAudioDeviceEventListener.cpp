//
// Created by stefan on 1/1/26.
//

#include "PulseAudioDeviceEventListener.h"

namespace e39rpi::powermate::device::listener {

    void PulseAudioDeviceEventListener::onKnobLongPressed() {
        logger->d(TAG, "onKnobLongPressed()");
    }

    void PulseAudioDeviceEventListener::onKnobPressed(bool isPressed) {
        logger->d(TAG, fmt::format("onKnobPressed({})", isPressed));
    }

    void PulseAudioDeviceEventListener::onKnobTurnClockwise(bool isPressed) {
        logger->d(TAG, fmt::format("onKnobTurnClockwise({})", isPressed));
    }

    void PulseAudioDeviceEventListener::onKnobTurnCounterClockwise(bool isPressed) {
        logger->d(TAG, fmt::format("onKnobTurnCounterClockwise({})", isPressed));
    }
} // listener