#VM OPTIONS TO DEBUG IN IDEA
updated
```
//to debug in intellij idea add VM OPTIONS to debug profile
p
/root/zzv/graalvm/javafx-sdk-11.0.2/lib
--add-exports=javafx.base/com.sun.javafx.reflect=ALL-UNNAMED
--add-exports=javafx.graphics/com.sun.javafx.util=ALL-UNNAMED
--module-path
/root/zzv/graalvm/javafx-sdk-11.0.2/lib/
--add-modules=javafx.controls,javafx.media,javafx.fxml

//test if you can run the application
mvn javafx:run

//generate configuration file for gaalvm 
mvn client:runagent

//compile applicaton into native
mvn client:build

//run native application ( failing at this moment on loading FXML files )
mvn client:run

# HelloFXML

A simple Hello World application with Java 11+, JavaFX 15+ (with FXML) and GraalVM.

## Documentation

Read about this sample [here](https://docs.gluonhq.com/#_hellofxml_sample)

## Quick Instructions

We use [Gluon Client](https://docs.gluonhq.com/) to build a native image for platforms including desktop, android and iOS.
Please follow the Gluon Client prerequisites as stated [here](https://docs.gluonhq.com/#_requirements).

### Desktop

Run the application using:

    mvn javafx:run

Build a native image using:

    mvn client:build

Run the native image app:

    mvn client:run

### Android

Build a native image for Android using:

    mvn client:build -Pandroid

Package the native image as an 'apk' file:

    mvn client:package -Pandroid

Install it on a connected android device:

    mvn client:install -Pandroid

Run the installed app on a connected android device:

    mvn client:run -Pandroid

### iOS

Build a native image for iOS using:

    mvn client:build -Pios

Install and run the native image on a connected iOS device:

    mvn client:run -Pios

Create an IPA file (for submission to TestFlight or App Store):

    mvn client:package -Pios