#!/bin/bash
arp -a | grep -E --ignore-case 'b8:27:eb|dc:a6:32'
