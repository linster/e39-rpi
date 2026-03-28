FROM ubuntu:questing


# make sure maven is installed.
RUN apt-get update && apt-get -y install \
    maven \
    git \
    nano \
    openjdk-21-jdk

RUN mkdir /network-manager
WORKDIR /network-manager

ADD buildDbus.sh .

CMD [ "./buildDbus.sh" ]

#RUN mkdir nm-output
#
#RUN git clone https://github.com/hypfvieh/dbus-java.git
#RUN git clone https://github.com/NetworkManager/NetworkManager.git
#
#WORKDIR /network-manager/dbus-java
#RUN mvn clean install -DskipTests=true
#
## then, run the builder script
#WORKDIR /network-manager/dbus-java/dbus-java-utils
#
#RUN find /network-manager/NetworkManager/introspection/*.xml -type f -exec mvn exec:java \
#-Dexec.mainClass="org.freedesktop.dbus.utils.generator.InterfaceCodeGenerator" \
#-Dexec.executable="java" \
#-Dexec.args="--inputFile {} --outputDir /network-manager/outbind/nm-output" \;

#WORKDIR /network-manager
#RUN touch /network-manager/outbind/wat.txt

#RUN [-d /network-manager/outbind/] && cp -R /network-manager/nm-output /network-manager/outbind
#RUN cp -R /network-manager/nm-output /network-manager/outbind/