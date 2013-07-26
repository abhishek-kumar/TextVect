#!/bin/bash

# Ensure we're in the TextVect root directory
[ ! -d "`pwd`/scripts" ] && cd ..
if [ ! -f "`pwd`/scripts/install.sh" ]; then
  echo -e "\e[00;31m    Please run this script from the root directory of the repository (the directory that contains the 'src' folder). \e[00m"
  exit 1
fi

chmod u+x ./scripts/install.sh
. ./scripts/install.sh
if [ $? -ne 0 ]; then
  echo -e "\e[00;31m    Install check failed. Unable to run TextVect.\e[00m"
  exit 1
fi

mkdir -p output

$JAVA -Djava.util.logging.config.file=$UIMA_HOME/config/Logger.properties -DVNS_HOST=localhost -Dfile.encoding=US-ASCII -classpath $UIMA_HOME/examples/bin:$UIMA_HOME/lib/uima-core.jar:$UIMA_HOME/lib/uima-document-annotation.jar:$UIMA_HOME/lib/uima-cpe.jar:$UIMA_HOME/lib/uima-tools.jar:$UIMA_HOME/lib/uima-adapter-vinci.jar:$UIMA_HOME/lib/uima-adapter-soap.jar:$UIMA_HOME/lib/jVinci.jar:$UIMA_HOME/examples/resources:$UIMA_HOME:$ROOT/cTAKES/lib/*:$ROOT/cTAKES/resources:$ROOT/lib/*:$ROOT/bin/TextVect.jar org.apache.uima.tools.cpm.CpmFrame

