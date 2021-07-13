#!/bin/bash
sw_vers
mkdir -p ~/private_keys
echo "$PRIV_KEY" > ~/private_keys/AuthKey_7QW2J46UP9.p8
cd src/osx
xcrun productbuild --component zzv.app /Applications --sign "$INSTALLER_CERT_NAME" zzv.pkg