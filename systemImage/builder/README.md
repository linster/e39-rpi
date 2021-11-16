# e39 OS builder

Downloads an rPI base image and modifies it to run the e39 base image.

After successful installation and configuration of required resources, the rPI will reboot.

Build: `docker build . -t e39`
Run: `docker run --privileged --volume /dev:/dev --volume $PWD:/output e39`

To configure WiFi after flashing the image, follow the [headless networking documentation](https://www.raspberrypi.com/documentation/computers/configuration.html#setting-up-a-headless-raspberry-pi). A valid network connection is required before the firstboot service will complete.
