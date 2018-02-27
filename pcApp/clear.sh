#!/bin/bash

echo "Deleting files:"
echo $(find -type f -regex .*/.*\.class)

find -type f -regex .*/.*\.class -delete
