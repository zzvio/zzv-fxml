#!/bin/bash
sw_vers
mkdir -p src/osx/zzv.app/Contents/MacOS/
cp target/gluonfx/x86_64-darwin/Zzv-view src/osx/zzv.app/Contents/MacOS/Zzv-view
cd src/osx

sed -e 's/CFBundleVersionPlaceHolder/63/g' zzv.app/Contents/Info.plist > zzv.app/Contents/Temp.plist
mv zzv.app/Contents/Temp.plist zzv.app/Contents/Info.plist
codesign -s "$APP_CERT_NAME" -f --entitlements entitlement.plist zzv.app