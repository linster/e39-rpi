//
// Created by stefan on 11/20/22.
//

#include "BaseLogger.h"

namespace e39rpi::powermate::logger {
        void BaseLogger::d(std::string tag, std::string message) {
            print(BaseLogger::Level::DEBUG, tag, message);
        }

        void BaseLogger::i(std::string tag, std::string message) {
            print(BaseLogger::Level::INFO, tag, message);
        }

        void BaseLogger::w(std::string tag, std::string message) {
            print(BaseLogger::Level::WARN, tag, message);
        }

        void BaseLogger::e(std::string tag, std::string message) {
            print(BaseLogger::Level::ERROR, tag, message);
        }

        void BaseLogger::wtf(std::string tag, std::string message) {
            print(BaseLogger::Level::WTF, tag, message);
        }
    } // logger