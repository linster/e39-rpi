#!/bin/bash

export PI_IP="192.168.0.81"
export PI_USER="e39"
#export BUILD_PI="true"

#./gradlew clean
#./gradlew packageUberJarForCurrentOs

#unset BUILD_PI

#Make the JAR Runnable from the cli.
#zip -d `ls build/compose/jars/*.jar` 'META-INF/*.SF' 'META-INF/*.RSA' 'META-INF/*SF'

#ps a | awk '{print $1,$7}' | grep e39Rpi | head -1 | awk '{print $1}'

#Kill the HMI
#ssh `echo $PI_USER`@`echo $PI_IP` kill `ps -e | grep java | awk '{print $1}'`
sshpass -f ~/.ssh/pi_pass ssh -tt `echo $PI_USER`@`echo $PI_IP` stdbuf -oL tail -f /home/e39/hmi.log


#sudo systemctl restart display-manager
