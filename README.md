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