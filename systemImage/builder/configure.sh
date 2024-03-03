#!/bin/sh -ex

export DEBIAN_FRONTEND=noninteractive

#WTF we need to add repos?



apt-get update

#TODO split out the audio, xserver, etc.

# Config reader
apt-get install -y --no-install-recommends git openjdk-17-jdk openjdk-17-jre zip ruby-hocon

#X server
apt-get install -y --no-install-recommends xserver-xorg xinit \
  lightdm realvnc-vnc-server \
  x11-xserver-utils jwm xterm 
  
# Bluetooth
#apt-get install -y --no-install-recommends wireless-tools \
#  bluez libbluetooth3 gstreamer1.0-pulseaudio

# Multimedia


# Old
#apt-get install -y --no-install-recommends xserver-xorg xinit git \
#  gstreamer1.0-x gstreamer1.0-omx gstreamer1.0-plugins-base gstreamer1.0-plugins-good \
#  gstreamer1.0-plugins-bad gstreamer1.0-alsa gstreamer1.0-libav \
#  alsa-utils realvnc-vnc-server openjdk-17-jdk openjdk-17-jre lightdm zip ruby-hocon \
#  x11-xserver-utils jwm xterm wireless-tools \
#  bluez libbluetooth3 gstreamer1.0-pulseaudio

/var/lib/e39/install-e39.sh

echo "e39 platform configured!"
