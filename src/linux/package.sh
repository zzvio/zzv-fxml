#!/bin/bash

KEYNAME="Hash-Token-United"
CWD=$(pwd)

# Make Deb
sudo dpkg-deb --build --root-owner-group zzv_1.0.1_amd64

# Make apt repo
mkdir -p zzv-view/amd64
mv zzv_1.0.1_amd64.deb zzv-view/amd64/
cd zzv-view
rm -f InRelease  KEY.gpg  Packages  Packages.gz  Release  Release.gpg

# Get Key ID
KEYID=$(gpg --show-keys $CWD/${KEYNAME}.pub | awk '/rsa4096/{getline; print}' | awk '{ gsub(/ /,""); print }')

# Package
gpg --output KEY.gpg --armor --export $KEYID
apt-ftparchive --arch amd64 packages amd64 > Packages
gzip -k -f Packages

# Release
apt-ftparchive release . > Release
gpg --pinentry-mode loopback  --passphrase $GPG_PASS --default-key ${KEYID} -abs -o Release.gpg Release
gpg --pinentry-mode loopback  --passphrase $GPG_PASS --default-key ${KEYID} --clearsign -o InRelease Release

cd $CWD
zip -r -qq zzv-view-apt-repo.zip zzv-view