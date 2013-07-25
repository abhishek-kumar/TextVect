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

# cTakes
if [ ! -d "$ROOT/apache-ctakes-3.0.0-incubating" ]; then
  echo -e "\e[1;33mDownloading cTAKES (please wait)\e[0m"
  wget http://www.globalish.com/am//incubator/ctakes/apache-ctakes-3.0.0-incubating-bin.zip
  unzip apache-ctakes-3.0.0-incubating-bin.zip
  rm apache-ctakes-3.0.0-incubating-bin.zip
  mkdir tmp
  cd tmp
  wget http://sourceforge.net/projects/ctakesresources/files/ctakes-resources-3.0.1.zip
  unzip ctakes-resources-3.0.1.zip
  rm ctakes-resources-3.0.1.zip
  cp -R ./resources/* ../apache-ctakes-3.0.0-incubating/resources
  cd ..
  # rm -rf ./resources
fi

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

exit 0
#$JAVA -Djava.util.logging.config.file=$UIMA_HOME/config/Logger.properties -DVNS_HOST=localhost -Dfile.encoding=US-ASCII -classpath $UIMA_HOME/examples/bin:$UIMA_HOME/lib/uima-core.jar:$UIMA_HOME/lib/uima-document-annotation.jar:$UIMA_HOME/lib/uima-cpe.jar:$UIMA_HOME/lib/uima-tools.jar:$UIMA_HOME/lib/uima-adapter-vinci.jar:$UIMA_HOME/lib/uima-adapter-soap.jar:$UIMA_HOME/lib/jVinci.jar:$UIMA_HOME/examples/resources:/Users/abhishek/.m2/repository:$UIMA_HOME:$ROOT/V3NLP/derma_2013.01.31/common-type-system/target/classes:$ROOT/V3NLP/derma_2013.01.31/concept/target/classes:$ROOT/V3NLP/derma_2013.01.31/utils.metamap/target/test-classes:$ROOT/V3NLP/derma_2013.01.31/utils.metamap/target/classes:$ROOT/V3NLP/derma_2013.01.31/type.descriptor/target/classes:$ROOT/V3NLP/derma_2013.01.31/token/target/classes:$ROOT/V3NLP/derma_2013.01.31/utils.general/target/classes:$ROOT/V3NLP/derma_2013.01.31/utils.uima/target/classes:$ROOT/V3NLP/derma_2013.01.31/utils.framework.uima/target/classes:$ROOT/V3NLP/derma_2013.01.31/utils.acronym/target/classes:$ROOT/V3NLP/derma_2013.01.31/concept.local/target/classes:$ROOT/V3NLP/derma_2013.01.31/utils.umls/target/classes:$ROOT/V3NLP/derma_2013.01.31/corpusStats/target/classes:$ROOT/V3NLP/derma_2013.01.31/documentHeader/target/classes:$ROOT/V3NLP/derma_2013.01.31/utils.flap/target/classes:$ROOT/V3NLP/derma_2013.01.31/evaluate/target/config:$ROOT/V3NLP/derma_2013.01.31/evaluate/target/classes:$ROOT/V3NLP/derma_2013.01.31/exampleProject/target/classes:$ROOT/V3NLP/derma_2013.01.31/mastif/filename-printer-pear/target/classes:$ROOT/V3NLP/derma_2013.01.31/filter/target/classes:$ROOT/V3NLP/derma_2013.01.31/flapPipelines/target/config:$ROOT/V3NLP/derma_2013.01.31/flapPipelines/target/classes:$ROOT/V3NLP/derma_2013.01.31/utils.install/target/classes:$ROOT/V3NLP/derma_2013.01.31/slotValue/target/classes:$ROOT/V3NLP/derma_2013.01.31/sentence/target/classes:$ROOT/V3NLP/derma_2013.01.31/section/target/classes:$ROOT/V3NLP/derma_2013.01.31/term/target/classes:$ROOT/V3NLP/derma_2013.01.31/utils.term/target/classes:$ROOT/V3NLP/derma_2013.01.31/utils.db/target/classes:$ROOT/V3NLP/derma_2013.01.31/sophia/target/classes:$ROOT/V3NLP/derma_2013.01.31/utils.sophia/target/classes:$ROOT/V3NLP/derma_2013.01.31/pos/target/classes:$ROOT/V3NLP/derma_2013.01.31/phrase/target/classes:$ROOT/V3NLP/derma_2013.01.31/negation/target/classes:$ROOT/V3NLP/derma_2013.01.31/questionAnswer/target/classes:$ROOT/V3NLP/derma_2013.01.31/symptom/target/classes:$ROOT/V3NLP/derma_2013.01.31/utils.weka/target/classes:$ROOT/V3NLP/derma_2013.01.31/utils.mallet/target/classes:$ROOT/V3NLP/derma_2013.01.31/homelessness/target/classes:$ROOT/V3NLP/derma_2013.01.31/mrsaConcepts/target/classes:$ROOT/V3NLP/derma_2013.01.31/marshallers.commonModel/target/classes:$ROOT/V3NLP/derma_2013.01.31/marshallers.database/target/config:$ROOT/V3NLP/derma_2013.01.31/marshallers.database/target/classes:$ROOT/V3NLP/derma_2013.01.31/marshallers.knowtator/target/classes:$ROOT/V3NLP/derma_2013.01.31/mastifNegation/target/classes:$ROOT/V3NLP/derma_2013.01.31/marshallers.ctakes/target/classes:$ROOT/V3NLP/derma_2013.01.31/regex.uima/target/classes:$ROOT/V3NLP/derma_2013.01.31/utils.regex/target/test-classes:$ROOT/V3NLP/derma_2013.01.31/utils.regex/target/classes:$ROOT/V3NLP/derma_2013.01.31/preAnnotation/target/classes:$ROOT/V3NLP/derma_2013.01.31/wordContext/target/classes:$ROOT/V3NLP/derma_2013.01.31/index/target/classes:$ROOT/V3NLP/derma_2013.01.31/line/target/classes:$ROOT/V3NLP/derma_2013.01.31/relabel/target/classes:$ROOT/V3NLP/derma_2013.01.31/merge/target/classes:$ROOT/V3NLP/derma_2013.01.31/injectCopyright/target/classes:$ROOT/V3NLP/derma_2013.01.31/mastif/med-facts-i2b2/target/test-classes:$ROOT/V3NLP/derma_2013.01.31/mastif/med-facts-i2b2/target/classes:$ROOT/V3NLP/derma_2013.01.31/mastif/med-facts-zoner/target/test-classes:$ROOT/V3NLP/derma_2013.01.31/mastif/med-facts-zoner/target/classes:$ROOT/V3NLP/derma_2013.01.31/utils.gate/target/classes:$ROOT/V3NLP/derma_2013.01.31/word/target/classes:/Users/abhishek/.m2/repository/org/hsqldb/hsqldb/2.2.7/hsqldb-2.2.7.jar:$ROOT/V3NLP/derma_2013.01.31/framework-core/target/classes:/Users/abhishek/.m2/repository/log4j/log4j/1.2.17/log4j-1.2.17.jar:$ROOT/V3NLP/derma_2013.01.31/cTAKES/bin:$ROOT/V3NLP/derma_2013.01.31/cTAKES/cTAKESdesc:$ROOT/V3NLP/derma_2013.01.31/cTAKES/resources:$ROOT/V3NLP/derma_2013.01.31/cTAKES/lib/args4j-2.0.16.jar:$ROOT/V3NLP/derma_2013.01.31/cTAKES/lib/clearparser-0.33.jar:$ROOT/V3NLP/derma_2013.01.31/cTAKES/lib/cleartk-util-0.8.1.jar:$ROOT/V3NLP/derma_2013.01.31/cTAKES/lib/commons-cli-1.2.jar:$ROOT/V3NLP/derma_2013.01.31/cTAKES/lib/commons-io-2.0.1.jar:$ROOT/V3NLP/derma_2013.01.31/cTAKES/lib/commons-io-2.1.jar:$ROOT/V3NLP/derma_2013.01.31/cTAKES/lib/commons-lang-2.4.jar:$ROOT/V3NLP/derma_2013.01.31/cTAKES/lib/commons-lang-2.6.jar:$ROOT/V3NLP/derma_2013.01.31/cTAKES/lib/commons-lang3-3.0.1.jar:$ROOT/V3NLP/derma_2013.01.31/cTAKES/lib/commons-logging-1.1.1.jar:$ROOT/V3NLP/derma_2013.01.31/cTAKES/lib/FindStructAPI.jar:$ROOT/V3NLP/derma_2013.01.31/cTAKES/lib/hppc-0.3.1.jar:$ROOT/V3NLP/derma_2013.01.31/cTAKES/lib/jackson-core-asl-1.5.0.jar:$ROOT/V3NLP/derma_2013.01.31/cTAKES/lib/jackson-mapper-asl-1.5.0.jar:$ROOT/V3NLP/derma_2013.01.31/cTAKES/lib/Jama-1.0.2.jar:$ROOT/V3NLP/derma_2013.01.31/cTAKES/lib/jcarafe-core_2.9.1-0.9.8.3.RC4.jar:$ROOT/V3NLP/derma_2013.01.31/cTAKES/lib/jcarafe-ext_2.9.1-0.9.8.3.RC4.jar:$ROOT/V3NLP/derma_2013.01.31/cTAKES/lib/jdom.jar:$ROOT/V3NLP/derma_2013.01.31/cTAKES/lib/junit.jar:$ROOT/V3NLP/derma_2013.01.31/cTAKES/lib/jVinci.jar:$ROOT/V3NLP/derma_2013.01.31/cTAKES/lib/libsvm-2.91.jar:$ROOT/V3NLP/derma_2013.01.31/cTAKES/lib/log4j-1.2.15.jar:$ROOT/V3NLP/derma_2013.01.31/cTAKES/lib/lucene-core-3.0.2.jar:$ROOT/V3NLP/derma_2013.01.31/cTAKES/lib/lvg2010dist.jar:$ROOT/V3NLP/derma_2013.01.31/cTAKES/lib/med-facts-i2b2-1.2-SNAPSHOT.jar:$ROOT/V3NLP/derma_2013.01.31/cTAKES/lib/med-facts-zoner-1.1.jar:$ROOT/V3NLP/derma_2013.01.31/cTAKES/lib/OpenAI_FSM.jar:$ROOT/V3NLP/derma_2013.01.31/cTAKES/lib/opennlp-maxent-3.0.2-incubating.jar:$ROOT/V3NLP/derma_2013.01.31/cTAKES/lib/opennlp-tools-1.5.2-incubating.jar:$ROOT/V3NLP/derma_2013.01.31/cTAKES/lib/sbinary_2.9.0-0.4.0.jar:$ROOT/V3NLP/derma_2013.01.31/cTAKES/lib/scala-library-2.9.0.jar:$ROOT/V3NLP/derma_2013.01.31/cTAKES/lib/SQLWrapper.jar:$ROOT/V3NLP/derma_2013.01.31/cTAKES/lib/struct_mult.jar:$ROOT/V3NLP/derma_2013.01.31/cTAKES/lib/uima-adapter-soap.jar:$ROOT/V3NLP/derma_2013.01.31/cTAKES/lib/uima-adapter-vinci.jar:$ROOT/V3NLP/derma_2013.01.31/cTAKES/lib/uima-core.jar:$ROOT/V3NLP/derma_2013.01.31/cTAKES/lib/uima-cpe.jar:$ROOT/V3NLP/derma_2013.01.31/cTAKES/lib/uima-document-annotation.jar:$ROOT/V3NLP/derma_2013.01.31/cTAKES/lib/uima-examples.jar:$ROOT/V3NLP/derma_2013.01.31/cTAKES/lib/uima-tools.jar:$ROOT/V3NLP/derma_2013.01.31/cTAKES/lib/uimafit-1.2.0.jar:$ROOT/V3NLP/derma_2013.01.31/cTAKES/lib/uimaj-bootstrap.jar:$ROOT/V3NLP/derma_2013.01.31/cTAKES/lib/xercesImpl-2.6.2.jar:$ROOT/V3NLP/derma_2013.01.31/cTAKES/lib/xml-apis.jar:$ROOT/V3NLP/derma_2013.01.31/cTAKES/lib/xmlParserAPIs.jar:$ROOT/V3NLP/derma_2013.01.31/cTAKES/resources/coreresources:$ROOT/V3NLP/derma_2013.01.31/FeatureEncoder/bin:$ROOT/V3NLP/derma_2013.01.31/FeatureEncoder/lib/weka.jar:$ROOT/V3NLP/derma_2013.01.31/FeatureEncoder/desc org.apache.uima.tools.cpm.CpmFrame
$JAVA -Djava.util.logging.config.file=$UIMA_HOME/config/Logger.properties -DVNS_HOST=localhost -Dfile.encoding=US-ASCII -classpath $UIMA_HOME/examples/bin:$UIMA_HOME/lib/uima-core.jar:$UIMA_HOME/lib/uima-document-annotation.jar:$UIMA_HOME/lib/uima-cpe.jar:$UIMA_HOME/lib/uima-tools.jar:$UIMA_HOME/lib/uima-adapter-vinci.jar:$UIMA_HOME/lib/uima-adapter-soap.jar:$UIMA_HOME/lib/jVinci.jar:$UIMA_HOME/examples/resources:$UIMA_HOME:$ROOT/apache-ctakes-3.0.0-incubating/lib/*:$ROOT/apache-ctakes-3.0.0-incubating/resources org.apache.uima.tools.cpm.CpmFrame

