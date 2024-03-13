#!/bin/bash

export PI_IP="192.168.0.81"
export PI_USER="e39"

sshpass -f ~/.ssh/pi_pass ssh -t `echo $PI_USER`@`echo $PI_IP` sudo systemctl restart display-manager
