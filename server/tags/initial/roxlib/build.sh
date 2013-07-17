#!/bin/sh
#
# This script extracts the necessary java.beans classes and dependencies for
# use in Android installations.

JAVA_HOME=/usr/lib/jvm/java-6-openjdk/

if [ ! -f javabeans.jar ]; then
  echo "Java Beans (javabeans.jar) not found, extracting from ${JAVA_HOME}."
  jar xf ${JAVA_HOME}/jre/lib/rt.jar java/beans
  jar cf javabeans.jar java/beans
  rm -rf ./java
fi

echo "Refactoring Java Beans."
java -jar jarjar-1.0.jar process rules.txt javabeans.jar javabeans-rf.jar

if [ ! -f rox.jar ]; then
  echo "ROX-XMLRPC (rox.jar) not found, proceeding without it."
else
  echo "Refactoring ROX-XMLRPC."
  java -jar jarjar-1.0.jar process rules.txt rox.jar rox-rf.jar
fi

