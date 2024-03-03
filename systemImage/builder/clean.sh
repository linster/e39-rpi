#!/bin/bash
#run as root. This is so we can clean up the gradle build we had to make as root


export BUILD_PI="true"
cd ../../
./gradlew clean

cd -
unset BUILD_PI

# also, clean out the contents of intermediate
rm -rf intermediate
mkdir intermediate
mkdir intermediate/e39
