//
// Created by stefan on 12/2/22.
//

#ifndef PICOTEMPLATE_COMPOSITELOGGER_H
#define PICOTEMPLATE_COMPOSITELOGGER_H

#include <memory>
#include <vector>
#include "BaseLogger.h"

namespace e39rpi::powermate::logger {

        class CompositeLogger : public BaseLogger {

        private:
            std::vector<std::shared_ptr<BaseLogger>> loggerList;
        public:
            virtual void d(std::string tag, std::string message) override;
            virtual void i(std::string tag, std::string message) override;
            virtual void w(std::string tag, std::string message) override;
            virtual void e(std::string tag, std::string message) override;
            virtual void wtf(std::string tag, std::string message) override;
            CompositeLogger(
                std::vector<std::shared_ptr<BaseLogger>> loggerList
            );

            void print(Level level, std::string tag, std::string message) override;
        };

    } // pico
} // logger

#endif //PICOTEMPLATE_COMPOSITELOGGER_H
