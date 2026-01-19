//
// Created by stefan on 1/9/26.
//

#ifndef DRIVER_GRIFFIN_POWERMATE_ILEDMANAGER_H
#define DRIVER_GRIFFIN_POWERMATE_ILEDMANAGER_H

// Same idea as https://github.com/stefansundin/powermate-linux/blob/4bad63bce3236a792bba8e891d22aad0df0bfc25/main.c#L53

namespace e39rpi::powermate::device::led {
    class ILedManager {

        //Wrap the set_led call so instead of just writing to stderr when
        //something went wrong, it returns an error.

        //also clamp the input to a 0-255 range

    };
}
#endif //DRIVER_GRIFFIN_POWERMATE_ILEDMANAGER_H
