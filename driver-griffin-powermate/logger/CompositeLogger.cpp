//
// Created by stefan on 12/2/22.
//

#include "CompositeLogger.h"

namespace e39rpi::powermate::logger {
        CompositeLogger::CompositeLogger(std::vector<std::shared_ptr<BaseLogger>> loggerList) {
            this->loggerList = loggerList;
        }

        void CompositeLogger::d(std::string tag, std::string message) {
            for (const auto &item : loggerList) {
                item->d(tag, message);
            }
        }

        void CompositeLogger::i(std::string tag, std::string message) {
            for (const auto &item : loggerList) {
                item->i(tag, message);
            }
        }

        void CompositeLogger::w(std::string tag, std::string message) {
            for (const auto &item : loggerList) {
                item->w(tag, message);
            }
        }

        void CompositeLogger::e(std::string tag, std::string message) {
            for (const auto &item : loggerList) {
                item->e(tag, message);
            }
        }

        void CompositeLogger::wtf(std::string tag, std::string message) {
            for (const auto &item : loggerList) {
                item->wtf(tag, message);
            }
        }

        void CompositeLogger::print(BaseLogger::Level level, std::string tag, std::string message) {
            for (const auto &item : loggerList) {
                item->print(level, tag, message);
            }
        }
    } // logger