#!/bin/bash
SOURCE="${BASH_SOURCE[0]}"
DIR="$( dirname "$SOURCE" )"
while [ -h "$SOURCE" ]
do 
  SOURCE="$(readlink "$SOURCE")"
  [[ $SOURCE != /* ]] && SOURCE="$DIR/$SOURCE"
  DIR="$( cd -P "$( dirname "$SOURCE"  )" && pwd )"
done
DIR="$( cd -P "$( dirname "$SOURCE" )" && pwd )"

java -classpath "$DIR/RXTXcomm.jar" -Djava.library.path="$DIR/classes/rxtxnative/Mac_OS_X" -jar "$DIR/DrawbotGUI.jar"