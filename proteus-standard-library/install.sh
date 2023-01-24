#!/bin/zsh

PROTEUS_HOME=~/.proteus
PROTEUS_LIBRARIES=$PROTEUS_HOME/libraries

mkdir -p $PROTEUS_LIBRARIES
# copy all files that end in .psl to the libraries directory
cp *.psl $PROTEUS_LIBRARIES