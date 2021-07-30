#!/bin/bash
mkdir -p ~/.gnupg
chown -R $(whoami) ~/.gnupg/
chmod 600 ~/.gnupg/*
chmod 700 ~/.gnupg
echo "cert-digest-algo SHA256" >> ~/.gnupg/gpg.conf
echo "digest-algo SHA256" >> ~/.gnupg/gpg.conf

KEYNAME="Hash-Token-United"
EMAIL=ds@htu.io
cat > $KEYNAME.batch <<EOF
 %echo Generating a standard key
 Key-Type: RSA
 Key-Length: 4096
 Subkey-Length: 4096
 Name-Real: ${KEYNAME}
 Name-Email: ${EMAIL}
 Expire-Date: 0
 Passphrase: ${GPG_PASS}
 %pubring ${KEYNAME}.pub
 %secring ${KEYNAME}.key
 # Do a commit here, so that we can later print "done" :-)
 %commit
 %echo done
EOF

gpg --batch --passphrase "HashZ1TokenZ2UnitedV3" --gen-key $KEYNAME.batch
gpg --no-default-keyring --secret-keyring ${KEYNAME}.key --keyring ${KEYNAME}.pub --list-secret-keys
gpg --import ${KEYNAME}.pub