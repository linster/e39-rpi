#!/bin/bash

protoc -I=src/main/kotlin/ca/stefanm/ibus/car/pico --java_out=src/main/java/ --kotlin_out=src/main/java/ src/main/kotlin/ca/stefanm/ibus/car/pico/ConfigProto.proto 
protoc -I=src/main/kotlin/ca/stefanm/ibus/car/pico --java_out=src/main/java/ --kotlin_out=src/main/java/ src/main/kotlin/ca/stefanm/ibus/car/pico/PiToPico.proto 
protoc -I=src/main/kotlin/ca/stefanm/ibus/car/pico --java_out=src/main/java/ --kotlin_out=src/main/java/ src/main/kotlin/ca/stefanm/ibus/car/pico/PicoToPi.proto 
