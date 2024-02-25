#!/bin/bash -ex

echo "Installer stub script"


cd /var/lib/e39
sudo chmod -R 0777 /var/lib/e39/

zip -d /var/lib/e39/e39Rpi-linux-x64-1.0.0.jar 'META-INF/*.SF' 'META-INF/*.RSA' 'META-INF/*SF'

mkdir /home/pi/.e39/
sudo chmod -R 0777 /home/pi/.e39
cp /var/lib/e39/config.conf /home/pi/.e39/config.conf
cp /var/lib/e39/version.conf /home/pi/.e39/version.conf

#Setup auto-login
sudo raspi-config nonint do_boot_behaviour B4

#Turn off screen blanking
sudo raspi-config nonint do_blanking 1

#Enable i2c for relays
sudo raspi-config nonint do_i2c 0


