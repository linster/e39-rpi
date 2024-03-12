#!/bin/bash

echo "Running configure.sh"
whiptail --infobox "Running e39 setup (configure.sh)" 20 40

export DEBIAN_FRONTEND=noninteractive

#test -f /var/lib/e39/setup/wifisetup.sh
if [ -f /var/lib/e39/setup/wifisetup.sh ]; then
    whiptail --infobox "Calling wifisetup.sh" 20 40
    /var/lib/e39/setup/wifisetup.sh
    rm -f /var/lib/e39/setup/wifisetup.sh
    whiptail --infobox "Deleting wifisetup.sh" 20 40
    sleep 15
fi

#Resize the root fs
#raspi-config nonint get_can_expand
#test -f /var/lib/e39/setup/expandedRoot
if [ -f /var/lib/e39/setup/expandedRoot ]; then 
    echo "Root already expanded"
    whiptail --infobox "Root already expanded" 20 40
else
    touch /var/lib/e39/setup/expandedRoot
#    raspi-config nonint do_expand_rootfs
    raspi-config --expand-rootfs
    whiptail --infobox "Expanded root, rebooting." 20 40
    reboot
fi



apt-get update

whiptail --infobox "Ran apt-get update" 20 40

#TODO split out the audio, xserver, etc.

# Config reader
apt-get install -y --no-install-recommends git openjdk-17-jdk openjdk-17-jre zip ruby-hocon

whiptail --infobox "Installed config reader deps" 20 40

#X server
apt-get install -y --no-install-recommends xserver-xorg xinit \
  lightdm realvnc-vnc-server \
  x11-xserver-utils jwm xterm 
  
whiptail --infobox "Installed X server" 20 40
  
# Bluetooth
#apt-get install -y --no-install-recommends wireless-tools \
#  bluez libbluetooth3 gstreamer1.0-pulseaudio

# Multimedia
apt-get install -y install pipewire libspa-0.2-bluetooth pulseaudio-utils



# Old
#apt-get install -y --no-install-recommends xserver-xorg xinit git \
#  gstreamer1.0-x gstreamer1.0-omx gstreamer1.0-plugins-base gstreamer1.0-plugins-good \
#  gstreamer1.0-plugins-bad gstreamer1.0-alsa gstreamer1.0-libav \
#  alsa-utils realvnc-vnc-server openjdk-17-jdk openjdk-17-jre lightdm zip ruby-hocon \
#  x11-xserver-utils jwm xterm wireless-tools \
#  bluez libbluetooth3 gstreamer1.0-pulseaudio

whiptail --infobox "install-e39.sh starting..." 20 40

/var/lib/e39/install-e39.sh

whiptail --infobox "e39 platform configured!" 20 40
echo "e39 platform configured!"
