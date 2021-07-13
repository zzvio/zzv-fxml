#!/bin/bash
echo "deb http://htu.io/zzv/ /" > /etc/apt/sources.list.d/zzv.list
wget -q -O - http://htu.io/zzv/KEY.gpg | apt-key add -
apt update
apt install zzv-view