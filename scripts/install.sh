#!/bin/bash

# colors
c_bold=
c_red=
c_yellow=
c_green=
if [ $TERM = 'xterm-256color' ]; then
  c_bold=`tput setaf 0`
  c_red=`tput setaf 1`
  c_yellow=`tput setaf 3`
  c_green=`tput setaf 2`
  c_end=`tput sgr0`
else
  c_bold='\e[1m'
  c_red='\e[1;31m'
  c_yellow='\e[1;36m'
  c_green='\e[1;32m'
  c_end=' \e[0m'
fi

[ ! -d "`pwd`/scripts" ] && cd ..
ROOT="`pwd`"
if [ ! -d "$ROOT/scripts" ]; then
  echo -e "${c_red}    Please run this script from the root directory of the repository (the directory that contains the 'src' folder). ${c_end}"
  exit 1
fi

# Java must be installed on the machine
JAVA=`which java`
if [ ! -f $JAVA ]; then
  echo -e "${c_red}    Java does not seem to be installed. Please install Java and re run this script. ${c_end}"
  exit 1
fi

# Check if we need to setup Apache UIMA
if [ -z "$UIMA_HOME" ]; then
  UIMA_HOME="`pwd`/apache-uima"
fi
if [ ! -d "$UIMA_HOME" ]; then
  echo -e "${c_yellow}Downloading Apache UIMA${c_end}"
  curl -o uimaj-2.4.0-bin.zip http://www.bizdirusa.com/mirrors/apache//uima//uimaj-2.4.0/uimaj-2.4.0-bin.zip
  unzip uimaj-2.4.0-bin.zip
  rm uimaj-2.4.0-bin.zip
  if [ ! -d "$UIMA_HOME" ]; then
    echo -e "${c_red}    Error downloading Apache UIMA Java Framework and SDK to $UIMA_HOME. Please download and install it from${c_end}"
    echo -e "${c_red}    http://uima.apache.org/downloads.cgi#Latest ${c_end}"
    echo -e "${c_red}    After installing, please create an environment variable UIMA_HOME that points to the installation.${c_end}"
    exit 1
  fi
fi

# If you want to use the latest cTakes version, uncomment this block.
# if [ ! -d "$ROOT/apache-ctakes-3.0.0-incubating" ]; then
#   echo -e "${c_yellow}Downloading cTAKES (please wait)${c_end}"
#   curl -o apache-ctakes-3.0.0-incubating-bin.zip http://www.globalish.com/am//incubator/ctakes/apache-ctakes-3.0.0-incubating-bin.zip
#   unzip apache-ctakes-3.0.0-incubating-bin.zip
#   rm apache-ctakes-3.0.0-incubating-bin.zip
#   mkdir tmp
#   cd tmp
#   curl -o ctakes-resources-3.0.1.zip http://sourceforge.net/projects/ctakesresources/files/ctakes-resources-3.0.1.zip
#   unzip ctakes-resources-3.0.1.zip
#   rm ctakes-resources-3.0.1.zip
#   cp -R ./resources/* ../apache-ctakes-3.0.0-incubating/resources
#   cd ..
#   # rm -rf ./resources
# fi

# Library files
if [ ! -d "$ROOT/lib" ]; then
  echo -e "${c_yellow}Downloading Library Jar files${c_end}"
  curl -o TextVect-lib.zip https://dl.dropboxusercontent.com/u/3091691/TextVect-lib/TextVect-lib.zip
  mkdir lib
  unzip TextVect-lib.zip
  cp TextVect-lib/* lib/
  rm TextVect-lib.zip
  rm -rf TextVect-lib
fi

# Sample data files
if [ ! -d "$ROOT/data" ]; then
  echo -e "${c_yellow}Downloading sample datasets (please request permission before using them)${c_end}"
  curl -o data.zip https://dl.dropboxusercontent.com/u/3091691/TextVect-lib/data.zip
  mkdir data
  unzip data.zip -d data
  rm data.zip
fi

# Update ".classpath" to point to correct directories
cp classpath-template.txt .classpath
sed -e "s#UIMA_HOME_LIB#$UIMA_HOME/lib#g" -i.bak .classpath
sed -e "s#UIMA_HOME#$UIMA_HOME#g" -i.bak .classpath
sed -e "s#ROOT#$ROOT#g" -i.bak .classpath

export UIMA_HOME
export ROOT
export JAVA

echo -e "${c_green}Install check successful!${c_end}"

