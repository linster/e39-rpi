#!/bin/sh -ex

[ "$(id -nu)" != "root" ] && echo Script must run as root && exit 1

# Build and combine packages
equivs-build e39-deps.control
mkdir -p packages
cp *deb packages

# create image
unzip 2021-10-30-raspios-bullseye-arm64-lite.zip
mkdir -p mnt
mount -o loop,offset=$((512*532480)) 2021-10-30-raspios-bullseye-arm64-lite.img mnt/

# Add assets
mkdir mnt/var/lib/e39
mkdir mnt/etc/e39
cp -a packages mnt/var/lib/e39
cp install-e39.sh mnt/var/lib/e39
cp configure.sh mnt/var/lib/e39
cp firstboot.service mnt/lib/systemd/system/firstboot.service
ln -s /lib/systemd/system/firstboot.service mnt/etc/systemd/system/default.target.wants

umount mnt

# Copy image to output if it exists
[ -d /output ] && cp 2021-10-30-raspios-bullseye-arm64-lite.img /output
