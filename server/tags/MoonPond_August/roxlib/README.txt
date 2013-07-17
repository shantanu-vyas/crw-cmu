This directory is used to build refactored versions of Rox, the XMLRPC library
used by the airboat server.  

Rox depends on the JavaBeans Introspector to do class reflection, and it is 
not available on Android.  To fix this, we use JarJar, a bytecode refactoring
library, to repackage the JavaBeans component of Sun Java as a standalone
library in the edu.cmu.ri.airboat.beans package.  Then, we refactor Rox to use
this package instead of java.beans.  The result is a pair of libraries:

javabeans-rf.jar
rox-rf.jar

which run on Android to provide limited JavaBeans and full Rox support.
