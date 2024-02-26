#!/bin/sh -ex

[ "$(id -nu)" != "root" ] && echo Script must run as root && exit 1

# Build and combine packages
equivs-build e39-deps.control
mkdir -p packages
cp *deb packages


# create image
unzip 2021-10-30-raspios-bullseye-arm64-lite.zip

mkdir -p mnt
mkdir -p mntboot

#Start = 532840, end= 3768319, sectors = 3235840
mount -o loop,offset=$((512*532480)) 2021-10-30-raspios-bullseye-arm64-lite.img mnt/

# Add assets
mkdir mnt/var/lib/e39
mkdir mnt/etc/e39

cp -a packages mnt/var/lib/e39
cp install-e39.sh mnt/var/lib/e39
cp *.jar mnt/var/lib/e39

cp jwmrc mnt/home/pi/.jwmrc
cp config.conf mnt/var/lib/e39
cp version.conf mnt/var/lib/e39
cp configure.sh mnt/var/lib/e39
cp firstboot.service mnt/lib/systemd/system/firstboot.service
ln -s /lib/systemd/system/firstboot.service mnt/etc/systemd/system/default.target.wants

umount mnt

#Start = 8192, end = 532479, sectors = 524288
mount -o loop,offset=$((512*8192)) 2021-10-30-raspios-bullseye-arm64-lite.img mntboot/

cp config.400x234.menu.ntsc.txt mntboot/config.txt
cp wpa_supplicant.conf mntboot/wpa_supplicant.conf
cp vga565.dtbo mntboot/overlays/vga565.dtbo

umount mntboot

# Copy image to output if it exists
[ -d /output ] && cp 2021-10-30-raspios-bullseye-arm64-lite.img /output
