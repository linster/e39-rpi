#!/bin/bash

export PI_IP="192.168.0.22"
export PI_USER="pi"
export BUILD_PI="true"

./gradlew clean
./gradlew packageUberJarForCurrentOs

unset BUILD_PI

#Make the JAR Runnable from the cli.
zip -d `ls build/compose/jars/*.jar` 'META-INF/*.SF' 'META-INF/*.RSA' 'META-INF/*SF'

#ps a | awk '{print $1,$7}' | grep e39Rpi | head -1 | awk '{print $1}'

#Kill the HMI
#ssh `echo $PI_USER`@`echo $PI_IP` kill `ps -e | grep java | awk '{print $1}'`
sshpass -f ~/.ssh/pi_pass ssh `echo $PI_USER`@`echo $PI_IP` killall java

sshpass -f ~/.ssh/pi_pass ssh -t `echo $PI_USER`@`echo $PI_IP` sudo cp /var/lib/e39/e39Rpi-linux-x64-1.0.0.jar /var/lib/e39/e39Rpi-linux-x64-1.0.0-`date --iso-8601=seconds`.jar
sshpass -f ~/.ssh/pi_pass scp `ls build/compose/jars/*.jar` `echo $PI_USER`@`echo $PI_IP`:/var/lib/e39/e39Rpi-linux-x64-1.0.0.jar

sshpass -f ~/.ssh/pi_pass ssh `echo $PI_USER`@`echo $PI_IP` hocon -f /home/pi/.e39/version.conf set hmiVersion.hmiHash `git log -1 --pretty=format:"%h"`

sshpass -f ~/.ssh/pi_pass ssh -t `echo $PI_USER`@`echo $PI_IP` sudo systemctl restart display-manager

sshpass -f ~/.ssh/pi_pass ssh -tt `echo $PI_USER`@`echo $PI_IP` stdbuf -oL tail -f /home/pi/hmi.log


#sudo systemctl restart display-manager
