//
// Created by stefan on 1/1/26.
//

#ifndef DRIVER_GRIFFIN_POWERMATE_PULSEAUDIODEVICEEVENTLISTENER_H
#define DRIVER_GRIFFIN_POWERMATE_PULSEAUDIODEVICEEVENTLISTENER_H

#include "IDeviceEventListener.h"
#include "logger/BaseLogger.h"
#include <memory>
#include "fmt/format.h"
namespace e39rpi::powermate::device::listener {

    /**
     * This is the part that is within
     * https://github.com/stefansundin/powermate-linux/blob/4bad63bce3236a792bba8e891d22aad0df0bfc25/main.c#L220
     *
     */
    class PulseAudioDeviceEventListener : public IDeviceEventListener {

    private:
        std::shared_ptr<logger::BaseLogger> logger;
        std::string TAG = "PulseAudioDeviceEventListener";
    public:
        PulseAudioDeviceEventListener(
            std::shared_ptr<logger::BaseLogger> logger
        );
        void onKnobLongPressed() override;
        void onKnobPressed(bool isPressed) override;
        void onKnobTurnClockwise(bool isPressed) override;
        void onKnobTurnCounterClockwise(bool isPressed) override;
    };

} // listener

#endif //DRIVER_GRIFFIN_POWERMATE_PULSEAUDIODEVICEEVENTLISTENER_H
