#!/bin/bash
SOURCE="${BASH_SOURCE[0]}"
DIR="$( dirname "$SOURCE" )"
while [ -h "$SOURCE" ]
do
SOURCE="$(readlink "$SOURCE")"
  [[ $SOURCE != /* ]] && SOURCE="$DIR/$SOURCE"
  DIR="$( cd -P "$( dirname "$SOURCE" )" && pwd )"
done
DIR="$( cd -P "$( dirname "$SOURCE" )" && pwd )"

java -Djava.library.path="$DIR" -jar "$DIR/Makelangelo.jar"