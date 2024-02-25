# e39 OS builder

Downloads an rPI base image and modifies it to run the e39 base image.

After successful installation and configuration of required resources, the rPI will reboot.

Build: `docker build . -t e39`
Run: `docker run --privileged --volume /dev:/dev --volume $PWD:/output e39`

To configure WiFi after flashing the image, follow the [headless networking documentation](https://www.raspberrypi.com/documentation/computers/configuration.html#setting-up-a-headless-raspberry-pi). A valid network connection is required before the firstboot service will complete.

# Wifi Setup, pre-build
Copy the `wpa_supplicant_dummy.conf` to `wpa_supplicant.conf` in this folder and change the ssid and password. Then, run `build.sh`.
(The way that this works has been removed in RPi OS Bookworm, but that also uses pipewire instead of pulse-audio, so we're not updating the distro any time soon).

https://learn.sparkfun.com/tutorials/headless-raspberry-pi-setup/wifi-with-dhcp

