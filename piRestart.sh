#!/bin/bash

export PI_IP="192.168.0.22"
export PI_USER="pi"

sshpass -f ~/.ssh/pi_pass ssh -t `echo $PI_USER`@`echo $PI_IP` sudo systemctl restart display-manager
