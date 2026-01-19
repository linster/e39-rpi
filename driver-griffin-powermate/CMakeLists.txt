cmake_minimum_required(VERSION 3.24)
project(driver_griffin_powermate)

set(CMAKE_CXX_STANDARD 17)

find_package(sdbus-c++ REQUIRED)

add_executable(driver_griffin_powermate
        main.cpp
        device/listener/IDeviceEventListener.h device/listener/LoggingDeviceEventListener.cpp device/listener/LoggingDeviceEventListener.h device/listener/PulseAudioDeviceEventListener.cpp device/listener/PulseAudioDeviceEventListener.h device/ledManager/ILedManager.h)

target_include_directories(
        driver_griffin_powermate
        PUBLIC
        $<BUILD_INTERFACE:${CMAKE_CURRENT_SOURCE_DIR}/>
        $<BUILD_INTERFACE:${CMAKE_CURRENT_SOURCE_DIR}/logger>
)

include(FetchContent)

FetchContent_Declare(
        fmt
        GIT_REPOSITORY https://github.com/fmtlib/fmt
        GIT_TAG        e69e5f977d458f2650bb346dadf2ad30c5320281) # 10.2.1
FetchContent_MakeAvailable(fmt)


target_link_libraries(driver_griffin_powermate PRIVATE
        SDBusCpp::sdbus-c++
        fruit
        fmt::fmt
        )