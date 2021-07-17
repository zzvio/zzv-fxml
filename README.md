#this version should run on Linux, Windows, and MacOS
```
It should be test with semux network
```
#INSTALL
```
#download graalvm 
https://github.com/graalvm/graalvm-ce-dev-builds/releases/tag/21.3.0-dev-20210715_2136
#download maven
https://mirror.cogentco.com/pub/apache/maven/maven-3/3.8.1/binaries/apache-maven-3.8.1-bin.zip
#download javafx lib for debuggin in IDEA
https://download2.gluonhq.com/openjfx/11.0.2/openjfx-11.0.2_linux-x64_bin-sdk.zip
#clone your project
git clone https://zvvio@bitbucket.org/zvvio/zzv-fxml.git

#update your linux OS ubuntu
sudo apt-get update -y
sudo apt install build-essential
sudo apt-get install manpages-dev
sudo apt-get install -y binutils-common
sudo apt-get install pkg-config
sudo apt-get install libasound2-dev libavcodec-dev libavformat-dev libavutil-dev 
sudo apt-get install libfreetype6-dev libgl-dev libglib2.0-dev  libgtk-3-dev libpango1.0-dev libx11-dev libxtst-dev 

cd to zzv-fxml

mvn clean
mvn qluonfx:compile -X -e
mvn gluonfx:link -X -e
mvn gluonfx:nativerun -X -e

cp -r config target/gluon*/x* .
 
```

#VM OPTIONS TO DEBUG IN IDEA
```
//to debug in intellij idea add VM OPTIONS to debug profile
//download JAVAFX from 
//https://gluonhq.com/products/javafx/  
--module-path
/<PATH_TO_JAVAFX>/javafx-sdk-11.0.2/lib/
--add-exports=javafx.base/com.sun.javafx.reflect=ALL-UNNAMED
--add-exports=javafx.graphics/com.sun.javafx.util=ALL-UNNAMED
--add-modules=javafx.controls,javafx.media,javafx.fxml

//test if you can run the application
mvn javafx:run -X -e

//generate configuration file for gaalvm 
mvn gluonfx:runagent -X -e

//compile applicaton into native
mvn gluonfx:compile -X -e

//run native application ( failing at this moment on loading FXML files )
mvn gluonfx:link -X -e

mvn gluonfx:nativerun -X -e

```
#COMPILE

```
you can commit to bitbucket with [COMPILE] tag in order to start Github Actions which will be triggered and 
synchronize bitbucket with github repositories and then will build the project for all operating systems
git add .
git commit -m"[COMPILE] test to trigger Github Actions"
git push
```

#DATABASE


```
MacOS
leveldbjni in 
java.library.path: 
[/Users/icteio/Library/Java/Extensions, /Library/Java/Extensions, /Network/Library/Java/Extensions, /System/Library/Java/Extensions, /usr/lib/java, .]

looking at the resource location ->   META-INF/native/osx64/libleveldbjni.jnilib
or
META-INF/native/osx/libleveldbjni.jnilib
-- if it finds
customPath - will be /var/folders/9k/jn6cdxzx1wsfjxfjj7mg4wpr0000gn/T/  ( temp path ) 
loaded target 
/var/folders/9k/jn6cdxzx1wsfjxfjj7mg4wpr0000gn/T/libleveldbjni-64-1-10357592336686326986.8

```