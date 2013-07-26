#!/bin/bash

[ ! -d "`pwd`/scripts" ] && cd ..
ROOT="`pwd`"
if [ ! -d "$ROOT/scripts" ]; then
  echo -e "\e[00;31m    Please run this script from the root directory of the repository (the directory that contains the 'src' folder). \e[00m"
  exit 1
fi

# Java must be installed on the machine
JAVA=`which java`
if [ ! -f $JAVA ]; then
  echo -e "\e[00;31m    Java does not seem to be installed. Please install Java and re run this script. \e[00m"
  exit 1
fi

# Check if we need to setup Apache UIMA
if [ -z "$UIMA_HOME" ]; then
  UIMA_HOME="`pwd`/apache-uima"
fi
if [ ! -d "$UIMA_HOME" ]; then
  echo -e "\e[1;33mDownloading Apache UIMA\e[0m"
  wget http://www.bizdirusa.com/mirrors/apache//uima//uimaj-2.4.0/uimaj-2.4.0-bin.zip
  unzip uimaj-2.4.0-bin.zip
  rm uimaj-2.4.0-bin.zip
  if [ ! -d "$UIMA_HOME" ]; then
    echo -e "\e[00;31m    Error downloading Apache UIMA Java Framework and SDK to $UIMA_HOME. Please download and install it from\e[00m"
    echo -e "\e[00;31m    http://uima.apache.org/downloads.cgi#Latest \e[00m"
    echo -e "\e[00;31m    After installing, please create an environment variable UIMA_HOME that points to the installation.\e[00m"
    exit 1
  fi
fi

# If you want to use the latest cTakes version, uncomment this block.
# if [ ! -d "$ROOT/apache-ctakes-3.0.0-incubating" ]; then
#   echo -e "\e[1;33mDownloading cTAKES (please wait)\e[0m"
#   wget http://www.globalish.com/am//incubator/ctakes/apache-ctakes-3.0.0-incubating-bin.zip
#   unzip apache-ctakes-3.0.0-incubating-bin.zip
#   rm apache-ctakes-3.0.0-incubating-bin.zip
#   mkdir tmp
#   cd tmp
#   wget http://sourceforge.net/projects/ctakesresources/files/ctakes-resources-3.0.1.zip
#   unzip ctakes-resources-3.0.1.zip
#   rm ctakes-resources-3.0.1.zip
#   cp -R ./resources/* ../apache-ctakes-3.0.0-incubating/resources
#   cd ..
#   # rm -rf ./resources
# fi

# Library files
if [ ! -d "$ROOT/lib" ]; then
  echo -e "\e[1;33mDownloading Library Jar files\e[0m"
  wget https://dl.dropboxusercontent.com/u/3091691/TextVect-lib/TextVect-lib.zip
  mkdir lib
  unzip TextVect-lib.zip
  cp TextVect-lib/* lib/
  rm TextVect-lib.zip
  rm -rf TextVect-lib
fi

# Update ".classpath" to point to correct directories
cp claspath-template.txt .classpath

sed -i "s#UIMA_HOME_LIB#$UIMA_HOME/lib#g" .classpath
sed -i "s#UIMA_HOME#$UIMA_HOME#g" .classpath
sed -i "s#ROOT#$ROOT#g" .classpath

echo -e "\e[1;32mInstall check successful!\e[0m"
exit 0

