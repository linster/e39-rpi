#!/bin/bash -ex

echo "Installer stub script"


cd /var/lib/e39
sudo chmod -R 0777 /var/lib/e39/

zip -d /var/lib/e39/e39Rpi-linux-x64-1.0.0.jar 'META-INF/*.SF' 'META-INF/*.RSA' 'META-INF/*SF'

#TODO clean up the setup scripts with sensitive info.

username=`cat mnt/var/lib/e39/setup/username`
cp jwmrc mnt/var/lib/e39/setup/.jwmrc /home/${username}/.jwmrc

/var/lib/e39/setup/wifisetup.sh
rm /var/lib/e39/setup/wifisetup.sh

#Setup auto-login
sudo raspi-config nonint do_boot_behaviour B4

#Turn off screen blanking
sudo raspi-config nonint do_blanking 1

#Enable i2c for relays
sudo raspi-config nonint do_i2c 0


