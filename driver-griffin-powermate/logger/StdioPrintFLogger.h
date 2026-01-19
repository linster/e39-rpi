//
// Created by stefan on 11/20/22.
//

#ifndef PICOTEMPLATE_STDIOPRINTFLOGGER_H
#define PICOTEMPLATE_STDIOPRINTFLOGGER_H

#include "BaseLogger.h"

namespace e39rpi::powermate::logger {
        class StdioPrintFLogger : public BaseLogger {

        public:
            void print(e39rpi::powermate::logger::BaseLogger::Level level, std::string tag,
                                          std::string message) override;
        };
}

#endif //PICOTEMPLATE_STDIOPRINTFLOGGER_H
