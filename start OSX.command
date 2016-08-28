#!/usr/bin/env bash
name="Makelangelo*.jar"
minimumVersion="1.8"

#find Java
if type -p java; then
    echo found java executable in PATH
    _java=java
elif [[ -n "$JAVA_HOME" ]] && [[ -x "$JAVA_HOME/bin/java" ]];  then
    echo "found java executable in $JAVA_HOME"
    _java="$JAVA_HOME/bin/java"
else
    echo "Java $minimumVersion not found."
    exit 1
fi

#check the Java version
if [[ "$_java" ]]; then
    version=$("$_java" -version 2>&1 | awk -F '"' '/version/ {print $2}')
    echo "Java version $version"
    if [[ "$version" > "$minimumVersion" ]]; then
        echo "version is more than $minimumVersion"
    else
        echo "version is less than $minimumVersion"
        exit 2
    fi
fi

# search for the Makelangelo JAR file
JAR=($name)

if [ ${#JAR[@]} -gt 0 ]; then
    echo "launching $JAR ..."
    "$_java" -jar $JAR
else
    echo "Makelangelo JAR file not found."
    exit 3
fi
