#!/bin/bash
cd src/ios/
let "BUILD = $GITHUB_RUN_NUMBER"
sed -e 's/CFBundleVersionPlaceHolder/'$BUILD'/g' Default-Info.plist > Temp.plist
mv Temp.plist Default-Info.plist