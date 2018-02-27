#!/bin/bash

filePath=$(find -type f -regex .*/$1)

echo Compiling $filePath
javac $filePath
