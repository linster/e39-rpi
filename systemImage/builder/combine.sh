#!/bin/sh -ex

[ "$(id -nu)" != "root" ] && echo Script must run as root && exit 1

# Build and combine packages
equivs-build e39-deps.control
mkdir -p packages
cp *deb packages


# create image
xz --decompress 2023-12-11-raspios-bookworm-arm64-lite.img.xz

#2023-12-11-raspios-bookworm-arm64-lite.img.xz

mkdir -p mnt
mkdir -p mntboot

mount -o loop,offset=$((512*1056768)) 2023-12-11-raspios-bookworm-arm64-lite.img mnt/

# Add assets
mkdir mnt/var/lib/e39
mkdir mnt/etc/e39
mkdir mnt/var/lib/e39/setup

cp -a packages mnt/var/lib/e39
cp install-e39.sh mnt/var/lib/e39
cp *.jar mnt/var/lib/e39

cp wifisetup.sh mnt/var/lib/e39/setup/wifisetup.sh
cp username mnt/var/lib/e39/setup/username

#TODO how are we figuring out where to put the user data?
#TODO we need to copy this after we run usersetup.sh into the right home folder.
cp jwmrc mnt/var/lib/e39/setup/.jwmrc



cp config.conf mnt/var/lib/e39
cp version.conf mnt/var/lib/e39
cp configure.sh mnt/var/lib/e39


cp e39firstboot.service mnt/lib/systemd/system/e39firstboot.service
ln -s /lib/systemd/system/firstboot.service mnt/etc/systemd/system/default.target.wants

umount mnt

mount -o loop,offset=$((512*8192)) 2023-12-11-raspios-bookworm-arm64-lite.img mntboot/

#TODO is boot still boot, or are we on /boot/firmware? (mountpoint vs partition?)
cp config.400x234.menu.ntsc.shorter.txt mntboot/config.txt

cp vga565.dtbo mntboot/overlays/vga565.dtbo
cp ssh mntboot/ssh
cp userconf.txt mntboot/userconf.txt


umount mntboot

# Copy image to output if it exists
[ -d /output ] && cp 2023-12-11-raspios-bookworm-arm64-lite.img /output
