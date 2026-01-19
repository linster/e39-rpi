//
// Created by stefan on 1/1/26.
//

#ifndef DRIVER_GRIFFIN_POWERMATE_LOGGINGDEVICEEVENTLISTENER_H
#define DRIVER_GRIFFIN_POWERMATE_LOGGINGDEVICEEVENTLISTENER_H

#include "IDeviceEventListener.h"
#include "logger/BaseLogger.h"
#include <memory>
#include "fmt/format.h"
namespace e39rpi::powermate::device::listener {

    class LoggingDeviceEventListener : public IDeviceEventListener {

    private:
        std::shared_ptr<logger::BaseLogger> logger;
        std::string TAG = "LoggingDeviceEventListener";
    public:
        LoggingDeviceEventListener(
            std::shared_ptr<logger::BaseLogger> logger
        );
        void onKnobLongPressed() override;
        void onKnobPressed(bool isPressed) override;
        void onKnobTurnClockwise(bool isPressed) override;
        void onKnobTurnCounterClockwise(bool isPressed) override;
    };

} // listener

#endif //DRIVER_GRIFFIN_POWERMATE_LOGGINGDEVICEEVENTLISTENER_H
