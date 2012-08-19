#!/bin/bash --verbose

rm -rf *.class
rm -rf gnu

rm -rf MANIFEST.MF META-INF
rm -rf RXTXcomm.jar

cp ../RXTXcomm.jar .

if [ "$1" == "clean" ]
then
  rm -rf DrawbotGUI.app
  rm -rf build.xml
  rm -rf DrawbotGUI.jar
  rm -rf RXTXcomm.jar
  exit 0
fi



javac -g -sourcepath . -classpath ./classes Filter.java 
javac -g -sourcepath . -classpath ./classes Filter_BlackAndWhite.java 
javac -g -sourcepath . -classpath ./classes Filter_DitherFloydSteinberg.java 
javac -g -sourcepath . -classpath ./classes Filter_Resize.java 
javac -g -sourcepath . -classpath ./classes Point2D.java 
javac -g -sourcepath . -classpath ./classes  Point3D.java 
javac -g -sourcepath . -classpath ./RXTXcomm.jar:./ -extdirs rxtxnative/Mac_OS_X Filter_TSPCodeGenerator.java 

javac -g -sourcepath . -classpath ./classes StatusBar.java 
javac -g -sourcepath . -classpath ./classes DrawPanel.java 
javac -g -sourcepath . -classpath ./RXTXcomm.jar:./ -extdirs rxtxnative/Mac_OS_X DrawbotGUI.java 

echo "Manifest-version: 1.0" > MANIFEST.MF
echo "Main-Class: DrawbotGui" >> MANIFEST.MF

jar xvf RXTXcomm.jar
rm -rf META-INF
rm -rf RXTXcomm.jar

jar -cvfe DrawbotGUI.jar DrawbotGUI *.class rxtxnative gnu

export JAVA_HOME=`/usr/libexec/java_home`

rm -rf DrawbotGUI.app
rm -rf build.xml

if [ ! -f appbundler-1.0.jar ] 
then 
   echo "Java App Bundler not found."
   echo "Please download from: http://java.net/projects/appbundler/downloads"
   echo "Place appbundler-1.0.jar in this directory"
   exit 0
fi

echo "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" > build.xml
echo "<project name=\"DrawbotGUI\" default=\"default\" basedir=\".\">" >> build.xml
echo "<property environment=\"env\" />" >> build.xml
echo " <taskdef name=\"bundleapp\"" >> build.xml
echo "       classname=\"com.oracle.appbundler.AppBundlerTask\"" >> build.xml
echo "       classpath=\"./appbundler-1.0.jar\" />" >> build.xml
echo "<target name=\"bundle-DrawbotGUI\">" >> build.xml
echo "    <bundleapp outputdirectory=\".\"" >> build.xml
echo "        name=\"DrawbotGUI\"" >> build.xml
echo "	      icon=\"DrawbotGUI.icns\"" >> build.xml
echo "        displayname=\"DrawbotGUI\"" >> build.xml
echo "        identifier=\"DrawbotGUI\"" >> build.xml
echo "        shortversion=\"1.0\"" >> build.xml
echo "        applicationCategory=\"public.app-category.developer-tools\"" >> build.xml
echo "        mainclassname=\"DrawbotGUI\">" >> build.xml
echo "        <runtime dir=\"\${env.JAVA_HOME}\"/>" >> build.xml
echo "        <classpath file=\"DrawbotGUI.jar\"/>" >> build.xml
echo "	<option value=\"-Djava.library.path=\$APP_ROOT/Contents/MacOS/\"/>" >> build.xml
echo "  <option value=\"-Xdock:icon=\$APP_ROOT/Contents/Resources/DrawbotGUI.icns\"/>" >> build.xml
echo "    </bundleapp>" >> build.xml
echo "</target>" >> build.xml
echo "</project>" >> build.xml

ant bundle-DrawbotGUI
cp rxtxnative/Mac_OS_X/librxtxSerial.jnilib DrawbotGUI.app/Contents/MacOS/

