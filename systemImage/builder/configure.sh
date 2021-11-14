#!/bin/sh -ex

export DEBIAN_FRONTEND=noninteractive
apt-get update
apt-get install -y --no-install-recommends xserver-xorg xinit fvwm

echo "e39 platform configured!"
