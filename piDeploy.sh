#!/bin/bash

export PI_IP="192.168.0.14"

./gradlew clean
./gradlew packageUberJarForCurrentOs

#ps a | awk '{print $1,$7}' | grep e39Rpi | head -1 | awk '{print $1}'

#zip -d e39Rpi-linux-x64-1.0.0.jar 'META-INF/*.SF' 'META-INF/*.RSA' 'META-INF/*SF'
