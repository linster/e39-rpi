#!/bin/bash

## I have a surface-rt running postmarketOS edge (with MATE)
## that is on the side of the fridge.

export PI_IP="192.168.0.193"
export PI_USER="user"

# The surface-rt is also armhf, so this variable is fine.
export BUILD_PI="true"

./gradlew --stop
#./gradlew clean
./gradlew packageUberJarForCurrentOs

unset BUILD_PI

#Make the JAR Runnable from the cli.
zip -d `ls composeApp/build/compose/jars/*.jar` 'META-INF/*.SF' 'META-INF/*.RSA' 'META-INF/*SF'

#ps a | awk '{print $1,$7}' | grep e39Rpi | head -1 | awk '{print $1}'

#Kill the HMI
#ssh `echo $PI_USER`@`echo $PI_IP` kill `ps -e | grep java | awk '{print $1}'`
#sshpass -f ~/.ssh/pi_pass ssh `echo $PI_USER`@`echo $PI_IP` killall java

echo "Backing up previous JAR file on device"
sshpass -f ~/.ssh/pi_pass ssh -t `echo $PI_USER`@`echo $PI_IP` sudo cp /var/lib/e39/e39Rpi-linux-x64-1.0.0.jar /var/lib/e39/e39Rpi-linux-x64-1.0.0-`date --iso-8601=seconds`.jar

echo "Copying new JAR file"
sshpass -f ~/.ssh/pi_pass scp `ls composeApp/build/compose/jars/*.jar` `echo $PI_USER`@`echo $PI_IP`:/var/lib/e39/e39Rpi-linux-x64-1.0.0.jar

echo "Setting hmiVersion hash in /var/lib/e39/version.conf"
sshpass -f ~/.ssh/pi_pass ssh `echo $PI_USER`@`echo $PI_IP` hocon -f /var/lib/e39/version.conf set hmiVersion.hmiHash `git log -1 --pretty=format:"%h"`

sshpass -f ~/.ssh/pi_pass ssh `echo $PI_USER`@`echo $PI_IP` killall java

echo "Restarting display manager"
#sshpass -f ~/.ssh/pi_pass ssh -t `echo $PI_USER`@`echo $PI_IP` sudo systemctl restart display-manager

echo "Showing log file"
#sshpass -f ~/.ssh/pi_pass ssh -tt `echo $PI_USER`@`echo $PI_IP` stdbuf -oL tail -f /home/e39/hmi.log


#sudo systemctl restart display-manager
