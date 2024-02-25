#!/bin/sh -ex

export DEBIAN_FRONTEND=noninteractive

apt-get update
apt-get install -y --no-install-recommends xserver-xorg xinit git \
  gstreamer1.0-x gstreamer1.0-omx gstreamer1.0-plugins-base gstreamer1.0-plugins-good \
  gstreamer1.0-plugins-bad gstreamer1.0-alsa gstreamer1.0-libav \
  alsa-utils realvnc-vnc-server openjdk-17-jdk openjdk-17-jre lightdm zip ruby-hocon \
  x11-xserver-utils jwm xterm pulseaudio-module-bluetooth wireless-tools

cp /var/lib/e39/config.txt /boot/config.txt

/var/lib/e39/install-e39.sh

echo "e39 platform configured!"
