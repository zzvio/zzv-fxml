#latest version should run on Linux, Windows, and MacOS
```
It should be tested with semux network ( test cases should be adapted into DEBUG view
```
#INSTALL
```
#download latest DEV graalvm 
https://github.com/graalvm/graalvm-ce-dev-builds/releases/tag/21.3.0-dev-20210715_2136


export JAVA_HOME=/<path to/graalvm-ce-java11-21.3.0-dev 
export GRAALVM_HOME=$JAVA_HOME
expoort PATH=GRAALVM_HOME/bin:$PATH

#download maven
https://mirror.cogentco.com/pub/apache/maven/maven-3/3.8.1/binaries/apache-maven-3.8.1-bin.zip

#download javafx lib for debuggin in IntelliJ IDEA
https://download2.gluonhq.com/openjfx/11.0.2/openjfx-11.0.2_linux-x64_bin-sdk.zip

#clone your project into ~/zzv/ directory
git clone https://zvvio@bitbucket.org/zvvio/zzv-fxml.git

#update your linux OS ubuntu
sudo apt-get update -y
sudo apt install build-essential
sudo apt-get install manpages-dev
sudo apt-get install -y binutils-common
sudo apt-get install pkg-config
sudo apt-get install libasound2-dev libavcodec-dev libavformat-dev libavutil-dev 
sudo apt-get install libfreetype6-dev libgl-dev libglib2.0-dev  libgtk-3-dev libpango1.0-dev libx11-dev libxtst-dev 

cd to ~/zzv/zzv-fxml

#clean the directory
mvn clean

#test/run project to update depenancies and see if project runs in java
mvn javafx:run -X -e 

//generate configuration file for gluonhq/graalvm 
mvn gluonfx:runagent -X -e

#compile project
mvn qluonfx:compile -X -e

#link project
mvn gluonfx:link -X -e

#copy configuration file into nativerun directory
cp -r config target/gluon*/x* .

#run project as a native executable
mvn gluonfx:nativerun -X -e

 
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

```

#PREPARE for GITHUB Actions 
```
#Windowsd computer 
#Open "x64 Native Tools Command Prompt for VS 2019
cd <ZZV HOME>\zzv\actions-runner
#run script 
run.cmd

#MacOS computer 
#to see if actions-runner is up
ps aux | grep run.sh

#run actions script
cd ~/zzv/actions-runner
./run.sh

```

#DEPLOY YOUR PROJECT: git add .: git commit -m"[COMPILE]": git push
```
you can commit to bitbucket with [COMPILE] tag in order to start Github Actions which will be triggered and 
synchronize bitbucket with github repositories and then will build the project for all operating systems
git add .
git commit -m"[COMPILE] test to trigger Github Actions"
git push

#monitor deployment URLs

//circleci for testing raw code and syncronization bitbucket repository with github repository
https://app.circleci.com/pipelines/bitbucket/zvvio  

//github action to monitor DevOps for Windows, Linux, MacOS
https://github.com/zzvio/zzv-fxml/actions

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