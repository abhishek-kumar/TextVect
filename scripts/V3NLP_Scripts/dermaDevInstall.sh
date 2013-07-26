#!/bin/sh
#
#  ------------------------------------------------------------------------
#  dermaDevInstall.sh
#
#  Created   2012.08.15
#  Modified  2012.12.07 
#  Modified  2012.12.27 Moved to the derma project
#                       Now invokes the derma multi-project pom to build
#  Modified  2013.01.08 Paths to the java and dependencies changed 
#  --- Copyright Notice: --------------------------------------------------
# 
#  Copyright 2012 United States Department of Veterans Affairs, 
#                 Health Services Research & Development Service
# 
#   Licensed under the Apache License, Version 2.0 (the "License");
#   you may not use this file except in compliance with the License.
#   You may obtain a copy of the License at
# 
#       http://www.apache.org/licenses/LICENSE-2.0
# 
#   Unless required by applicable law or agreed to in writing, software
#   distributed under the License is distributed on an "AS IS" BASIS,
#   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
#   See the License for the specific language governing permissions and
#   limitations under the License. 
#  
#  --- End Copyright Notice: ----------------------------------------------

# -------------------------------
# This is the master script that
# attempts to kick everything off
#
# 
#   This script assumes that you can run this in a bourne or bash shell.
# 
#   This script assumes that you've gotten this script from the
#   download site 
#     https://wiki.chpc.utah.edu/download/attachments/333643792/dermaDevInstall.sh
#   already.
#    
#   This script will prompt to create a proposed $DERMA_HOME directory 
#   to deposit the derma projects in. Change the location if you do
#   not like the proposed $DERMA_HOME directory.  
#  
#   This script will download and build all the derma projects into
#   the $DERMA_HOME directory.  

#   This script will keep a dermaDevInstall$DATE.log in the $DERMA_HOME directory. 
# 
#   If run via windows, this script
#   presumes that it's being run under
#   cygwin or similar shell, and that 
#   cp, wget, date, git, mvn 
#
#   When git pulls down files, it uses a setting core.autocrlf to determine
#   if the line endings should be crlf, or just lf.  The shell scripts run
#   in cygwin complain when the line endings are crlf.  For this reason,
#   this script will set this setting to coreautocrlf = false.  
#
#   The download uses git as it's mechanism.  Git looks for a %HOME%\_netrc
#   for authentication.  If not already done so, create this file
#   with the contents:
#   ---Begin File----------------------- 
#      machine inlp.bmi.utah.edu 
#      login someUserName 
#      password aPassword

#      machine v3nlp.bmi.utah.edu 
#      login someUserName 
#      password aPassword
#      
#      machine decipher.chpc.utah.edu
#      login someUserName 
#      password aPassword
#   ---End File-------------------------- 
#
#  Maven uses %HOME%/.m2/settings.xml to know what repositories
#  to pull from. If not already done so, create a setup.xml file
#  with the following entries: 
#
#  The server section is needed for when one creates releases into
#  the nexus repository. See one of us for the username and password.
#  These scripts do not create releases.
#  
#   ---Begin File----------------------- 
#   <?xml version="1.0"?>
#   <settings xmlns="http://maven.apache.org/SETTINGS/1.0.0"
#     xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
#     xsi:schemaLocation="http://maven.apache.org/SETTINGS/1.0.0
#                         http://maven.apache.org/xsd/settings-1.0.0.xsd">
#    <mirrors>
#      <mirror>
#      <!--This sends everything else to /public --> 
#         <id>nexus</id>
#         <mirrorOf>*</mirrorOf>
#         <url>http://inlp.bmi.utah.edu/nexus/content/groups/public</url> 
#      </mirror>
#    </mirrors>
#   
#     <servers>
#       <server>
#         <id>nexus</id>
#         <username>XXXXX</username>
#         <password>XXXXXXXXX</password>
#       </server>
#     </servers>
#   </settings>
#   ---End File-------------------------- 
#
# Last updated Aug 19, 2012
#
# -------------------------------
export STARTED=`date +%s` 
export DDATE=`date +%Y.%m.%d`
export PDERMA_HOME=derma_$DDATE

echo 
echo Setting the global crlf setting to false
echo 
echo 
git config --global core.autocrlf false

# -------------------------------
echo 
echo 
echo 
echo 
echo 
echo 
echo ----------------------------------------------- 
echo The Derma Repository Download and Build Script.
echo ----------------------------------------------- 
echo 
echo 
echo 
echo 
echo The software about to be downloaded is distributed
echo under several license agreements.  
echo 
echo Intellectual property created by the University of Utah s
echo Biomedical Informatics Department, the Division of Epidemiology
echo and the VA Salt Lake City is governed by 
echo the Creative Commons Attribution 3.0 license.
echo When using this software, please attribute it to:
echo V3NLP, a Product of University of Utah, and the VA Salt Lake City
echo
echo Software that v3NLP is dependent upon will be downloaded
echo by you, through this script, from either the original distribution
echo sources or from authorized distribution sources.  The responsibility
echo for license adherence for each dependent third party source is
echo on you.  Due diligence has been taken to insure that any
echo dependent software used has been has an acceptable
echo open source agreement.  In each derma project, please see the file 
echo ThirdPartySoftwareUsed.txt for what dependent software is being 
echo employed within v3NLP.
echo
echo [Hook to put the specifics here ]

echo ------------------------------- 
read -p "Press [Enter] key to continue ..."
#
#
echo ------------------------------- 
echo Create the DERMA_HOME directory
echo ------------------------------- 
read -e -p "Place Derma in > $PDERMA_HOME: " DERMA_HOME
if [[ -z $DERMA_HOME && ${DERMA_HOME+x} ]] 
 then
  export DERMA_HOME=$PDERMA_HOME
fi
#
# ------------------------------------
# Check to see if the directory exists
#  If it exists, then write over it, because
#  the user explicitly named this directory 
# ------------------------------------
if [ ! -d $DERMA_HOME ]; then
   mkdir $DERMA_HOME
fi
#
#
#
echo ------------------------------------------------------------------ 
echo -- The output is now also going into the dermaDevInstall$DDATE.log 
echo ------------------------------------------------------------------ 
#
#
echo -------------------------------------------------  > dermaDevInstall$DDATE.log
echo -- Created the directory derma$DDATE           -- |tee -a dermaDevInstall$DDATE.log 1>&2
echo ------------------------------------------------- |tee -a dermaDevInstall$DDATE.log 1>&2

echo ------------------------------------------------- |tee -a dermaDevInstall$DDATE.log 1>&2 
echo  Downloading the latest build scripts          -- |tee -a dermaDevInstall$DDATE.log 1>&2
echo ------------------------------------------------- |tee -a dermaDevInstall$DDATE.log 1>&2 
git clone http://v3nlp.bmi.utah.edu/gitblit/git/derma/derma.git |tee -a dermaDevInstall$DDATE.log 1>&2 

echo ------------------------------------------------- |tee -a dermaDevInstall$DDATE.log 1>&2
echo  Finished downloading the build scripts        -- |tee -a dermaDevInstall$DDATE.log 1>&2
echo ------------------------------------------------- |tee -a dermaDevInstall$DDATE.log 1>&2

echo ------------------------------------------------- |tee -a dermaDevInstall$DDATE.log 1>&2
echo -- Change the directory to the $DERMA_HOME     -- |tee -a dermaDevInstall$DDATE.log 1>&2
echo ------------------------------------------------- |tee -a dermaDevInstall$DDATE.log 1>&2
cd $DERMA_HOME 

echo ------------------------------------------------- |tee -a dermaDevInstall$DDATE.log 1>&2
echo  Copy the build scripts to the derma directory    |tee -a dermaDevInstall$DDATE.log 1>&2
echo ------------------------------------------------- |tee -a dermaDevInstall$DDATE.log 1>&2
cp ../derma/*.* .

echo ------------------------------------------------- |tee -a dermaDevInstall$DDATE.log 1>&2
echo  Finished copy the build scripts to the derma directory |tee -a dermaDevInstall$DDATE.log 1>&2 
echo ------------------------------------------------- |tee -a dermaDevInstall$DDATE.log 1>&2


echo ------------------------------------------------- |tee -a dermaDevInstall$DDATE.log 1>&2
echo  Copy the multi-module pom to the derma directory |tee -a dermaDevInstall$DDATE.log 1>&2
cp ../derma/derma.pom/pom.xml .

echo ------------------------------------------------- |tee -a dermaDevInstall$DDATE.log 1>&2
echo  Finished copying over the multi-module pom to the derma directory |tee -a dermaDevInstall$DDATE.log 1>&2
echo ------------------------------------------------- |tee -a dermaDevInstall$DDATE.log 1>&2


echo --------------------------------------------------|tee -a dermaDevInstall$DDATE.log 1>&2
echo -- The derma download and build log file       -- |tee -a dermaDevInstall$DDATE.log 1>&2
echo ------------------------------------------------- |tee -a dermaDevInstall$DDATE.log 1>&2
echo ------------------------------------------------- |tee -a dermaDevInstall$DDATE.log 1>&2
echo -- Kicked off ...... `date` |tee -a dermaDevInstall$DDATE.log 1>&2
echo ------------------------------------------------- |tee -a dermaDevInstall$DDATE.log 1>&2
#
#
#
echo  Derma  |tee -a dermaDevInstall$DDATE.log 1>&2
echo ------------------------------ |tee -a dermaDevInstall$DDATE.log 1>&2
date |tee -a dermaDevInstall$DDATE.log
echo ------------------------------ |tee -a dermaDevInstall$DDATE.log 1>&2


echo ------------------------------ |tee -a dermaDevInstall$DDATE.log 1>&2
echo  Download all the repositories |tee -a dermaDevInstall$DDATE.log 1>&2
echo ------------------------------ |tee -a dermaDevInstall$DDATE.log 1>&2
sh -xv downloadDermaRelease.sh |tee -a dermaDevInstall$DDATE.log 2>&1 
echo  Finished Download all the repositories |tee -a dermaDevInstall$DDATE.log 1>&2
echo ------------------------------ |tee -a dermaDevInstall$DDATE.log 1>&2

echo ------------------------------ |tee -a dermaDevInstall$DDATE.log 1>&2
echo  Install the parent java pom   |tee -a dermaDevInstall$DDATE.log 1>&2
echo ------------------------------ |tee -a dermaDevInstall$DDATE.log 1>&2
cd framework.java.pom
mvn install |tee -a dermaDevInstall$DDATE.log 2>&1
cd ..

echo -------------------------------------- |tee -a dermaDevInstall$DDATE.log 1>&2
echo  Install the parent dependencies pom   |tee -a dermaDevInstall$DDATE.log 1>&2
echo -------------------------------------- |tee -a dermaDevInstall$DDATE.log 1>&2
cd framework.dependencies.pom
mvn install |tee -a dermaDevInstall$DDATE.log 2>&1
cd ..

echo -------------------------------------- |tee -a dermaDevInstall$DDATE.log 1>&2
echo  Install the parent pom                |tee -a dermaDevInstall$DDATE.log 1>&2
echo -------------------------------------- |tee -a dermaDevInstall$DDATE.log 1>&2
cd framework.parent.pom
mvn install |tee -a dermaDevInstall$DDATE.log 2>&1
cd ..

echo ------------------------------ |tee -a dermaDevInstall$DDATE.log 1>&2
echo  build each repository |tee -a dermaDevInstall$DDATE.log 1>&2
echo ------------------------------ |tee -a dermaDevInstall$DDATE.log 1>&2
mvn install |tee -a dermaDevInstall$DDATE.log 2>&1
date |tee -a dermaDevInstall$DDATE.log 1>&2
echo  Finished build each repository |tee -a dermaDevInstall$DDATE.log 1>&2
echo ------------------------------ |tee -a dermaDevInstall$DDATE.log 1>&2

echo ------------------------------ |tee -a dermaDevInstall$DDATE.log 1>&2
echo  Unjar the resources that are too big for git |tee -a dermaDevInstall$DDATE.log 1>&2
echo ------------------------------ |tee -a dermaDevInstall$DDATE.log 1>&2
jar xvf sophiaIndexes.jar
jar xvf sophiaHash.jar
cd cTAKES
jar xvf ../ctakesResources.jar
cd ..

export ENDED=`date +%s` 
echo $STARTED  $ENDED
diff=$(((($ENDED - $STARTED ) /60) + 1 ))
echo ------------------------------ |tee -a dermaDevInstall$DDATE.log 1>&2
echo -- Total Time taken to build ... $diff minutes |tee -a dermaDevInstall$DDATE.log 1>&2
echo ------------------------------ |tee -a dermaDevInstall$DDATE.log 1>&2

echo ------------------------------ 
echo ------------------------------ 
echo          FINISHED
echo ------------------------------ 
echo ------------------------------ 

echo ------------------------------ 
echo ------------------------------ 
echo  Look at the $DERMA_HOME/dermaDevInstall$DDATE.log for issues
echo ------------------------------ 
echo ------------------------------ 
