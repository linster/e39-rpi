#!/bin/bash

# Meant to be run inside the docker container

git clone -b dbus-java-parent-5.2.0 https://github.com/hypfvieh/dbus-java.git
git clone https://github.com/NetworkManager/NetworkManager.git

cd /network-manager/dbus-java || exit
mvn clean install -DskipTests=true

# then, run the builder script
cd /network-manager/dbus-java/dbus-java-utils || exit

find /network-manager/NetworkManager/introspection/*.xml -type f -exec mvn exec:java \
-Dexec.mainClass="org.freedesktop.dbus.utils.generator.InterfaceCodeGenerator" \
-Dexec.executable="java" \
-Dexec.args="--inputFile {} --outputDir /network-manager/outbind/nm-output" \;
