#!/bin/zsh

PROTEUS_HOME=~/.proteus
PROTEUS_LIBRARIES=$PROTEUS_HOME/libraries
PROTEUS_STANDARD_LIB_PATH=$PROTEUS_LIBRARIES/std

rm -rf $PROTEUS_STANDARD_LIB_PATH
mkdir -p $PROTEUS_STANDARD_LIB_PATH
# copy all files that end in .psl to the libraries directory
cp *.psl $PROTEUS_STANDARD_LIB_PATH