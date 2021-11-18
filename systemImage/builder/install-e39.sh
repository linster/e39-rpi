#!/bin/bash -ex

echo "Installer stub script"
# Download e39 project

cd /var/lib/e39
git clone https://github.com/linster/e39-rpi.git
cd /var/lib/e39/e39-rpi

# Build e39 project
