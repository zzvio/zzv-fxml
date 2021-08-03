#!/bin/bash
sw_vers
mkdir -p src/osx/zzv.app/Contents/MacOS/
cp target/gluonfx/x86_64-darwin/semux-core src/osx/zzv.app/Contents/MacOS/Zzv-view
cd src/osx
let "BUILD = $GITHUB_RUN_NUMBER + 30"
sed -e 's/CFBundleVersionPlaceHolder/'$BUILD'/g' zzv.app/Contents/Info.plist > zzv.app/Contents/Temp.plist
mv zzv.app/Contents/Temp.plist zzv.app/Contents/Info.plist
codesign -s "$APP_CERT_NAME" -f --entitlements entitlement.plist zzv.app