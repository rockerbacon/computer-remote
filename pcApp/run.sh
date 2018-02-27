#!/bin/bash

if [ "$(find -type f -wholename ./com/lab309/computerRemote/Main.class)" == "" ]; then
	echo Compiling program...
	javac com/lab309/computerRemote/Main.java
	echo Compilation finished
fi

echo Executing com/lab309/computerRemote/Main
java com/lab309/computerRemote/Main
