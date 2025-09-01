mkdir intermediate
mkdir -p intermediate/e39
mkdir -p intermediate/boot


cp version_template.conf ./intermediate/e39/version.conf
hocon -f ./intermediate/e39/version.conf set hmiVersion.sdHash `git log -1 --pretty=format:"%h"`
hocon -f ./intermediate/e39/version.conf set hmiVersion.hmiHash `git log -1 --pretty=format:"%h"`

export BUILD_PI="true"

cd ../../
./gradlew clean
./gradlew packageUberJarForCurrentOs

cd -
unset BUILD_PI

# Build up an intermediate/userconf.txt (to copy into /boot)
# that is username:password(hashed)
username=`hocon -f image.conf get imageConf.username`
rawpassword=`hocon -f image.conf get imageConf.password`
encpassword=`echo ${rawpassword} | openssl passwd -6 -stdin`
echo "${username}:${encpassword}" > intermediate/boot/userconf.txt

echo ${username} > intermediate/e39/username

# Next, we need to setup the wifi so we can ssh in
# This file needs to be copied into /boot
echo "ssh" > intermediate/boot/ssh

#https://www.zansara.dev/posts/2024-01-06-raspberrypi-headless-bookworm-wifi-config/
#We'll use this to setup the wifi command to run
#https://github.com/RPi-Distro/raspberrypi-sys-mods/blob/bookworm/usr/lib/raspberrypi-sys-mods/imager_custom

wifinetwork=`hocon -f image.conf get imageConf.wifinetwork`
wifipass=`hocon -f image.conf get imageConf.wifipass`
wificountry=`hocon -f image.conf get imageConf.wificountry`
cat <<- EOF > intermediate/wifisetup.sh
#!/bin/bash
/usr/lib/raspberrypi-sys-mods/imager_custom set_wlan ${wifinetwork} ${wifipass} ${wificountry}
EOF


chmod -R 0777 intermediate

#cp ../../build/compose/jars/e39Rpi-linux-x64-1.0.0.jar ./intermediate/e39/e39Rpi-linux-x64-1.0.0.jar
cp ../../build/compose/jars/e39Rpi-linux-x64-1.0.0.jar ./intermediate/e39/e39Rpi-linux-x64-1.0.0.jar

chmod -R 0777 intermediate

docker build . -t e39 && \
    docker run --rm --privileged --volume /dev:/dev --volume $PWD:/output e39
