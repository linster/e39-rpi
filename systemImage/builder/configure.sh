#!/bin/sh -ex

export DEBIAN_FRONTEND=noninteractive
apt-get update
apt-get install -y --no-install-recommends xserver-xorg xinit fvwm git \
  gstreamer1.0-x gstreamer1.0-omx gstreamer1.0-plugins-base gstreamer1.0-plugins-good \
  gstreamer1.0-plugins-bad gstreamer1.0-alsa gstreamer1.0-libav \
  alsa-utils realvnc-vnc-server

/var/lib/e39/install-e39.sh

echo "e39 platform configured!"
