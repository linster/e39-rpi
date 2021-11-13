#!/bin/sh -ex

[ "$(id -nu)" != "root" ] && echo Script must run as root && exit 1

# Build and combine packages
equivs-build e39-deps.control
mkdir -p packages
cp *deb packages

# create image
unzip 2021-10-30-raspios-bullseye-armhf-lite.zip
mkdir -p mnt
mount -o loop,offset=$((512*532480)) 2021-10-30-raspios-bullseye-armhf-lite.img mnt/

# Add assets
mkdir mnt/var/lib/e39
cp -a packages mnt/var/lib/e39

umount mnt

# Copy image to output if it exists
[ -d /output ] && cp 2021-10-30-raspios-bullseye-armhf-lite.img /output
