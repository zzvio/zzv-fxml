cp target/gluonfx/x86_64-windows/Zzv-view.exe src/win/pkg
cd src/win
((Get-Content -path ./pkg/AppxManifest.xml -Raw) -replace 'VersionPlaceholder',"1.0.$env:GITHUB_RUN_NUMBER.0") | Set-Content -Path ./pkg/AppxManifest.xml
& 'C:\Program Files (x86)\Windows Kits\10\bin\10.0.19041.0\x64\makeappx.exe' pack /d ./pkg /p ./Package.appx
& 'C:\Program Files (x86)\Windows Kits\10\bin\10.0.19041.0\x64\signtool.exe' sign /fd SHA256 /a /f $env:CERT_FILE /p $env:CERT_PASSWORD Package.appx