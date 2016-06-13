#!/usr/bin/env bash
if [ ! -f paths.txt ]; then
     mvn dependency:build-classpath | grep -v -e '\[.*\]' > paths.txt
fi
paths=`cat paths.txt`
file=target/dcframework-1.0-SNAPSHOT.jar
COUNTER=0
#while [ 1 ]; do
#    COUNTER=$[$COUNTER +1]
#    echo "Attempt $COUNTER"
    java -ea -cp ${paths}:${file} pl.adamborowski.zar.Main "$@"
#    if [ $? -ne 0 ]; then
#        break
#    fi
#done