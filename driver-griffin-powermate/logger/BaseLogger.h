//
// Created by stefan on 11/20/22.
//

#ifndef PICOTEMPLATE_BASELOGGER_H
#define PICOTEMPLATE_BASELOGGER_H

#include <string>

namespace e39rpi::powermate::logger {

        class BaseLogger {

        public:
            virtual void d(std::string tag, std::string message);
            virtual void i(std::string tag, std::string message);
            virtual void w(std::string tag, std::string message);
            virtual void e(std::string tag, std::string message);
            virtual void wtf(std::string tag, std::string message);

            //TODO have another level for logging about writing bytes to IBUS,
            //TODO so we don't get an infinite loop when we write one log statement to Ibus.
            enum class Level { DEBUG, INFO, WARN, ERROR, WTF};

            virtual void print(Level level, std::string tag, std::string message) = 0;
        };

    } // logger

#endif //PICOTEMPLATE_BASELOGGER_H
