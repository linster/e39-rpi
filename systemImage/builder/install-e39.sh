#!/bin/bash -ex

echo "Installer stub script"
# Download e39 project

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

cat << EOT >> /boot/config.txt
overscan_left=48
overscan_right=-48
overscan_top=16
overscan_bottom=-64
sdtv_aspect=3
test_mode=0
EOT


#cd /var/lib/e39
#git clone https://github.com/linster/e39-rpi.git
#cd /var/lib/e39/e39-rpi

#git checkout -t origin/deploy/stefan

# Build e39 project
#./gradlew
#./gradlew packageUberJarForCurrentOs


