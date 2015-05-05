#!/usr/bin/env bash

mvn install:install-file -Dfile=./ibm/websphere/com.ibm.mq.pcf.jar -DgroupId=com.ibm -DartifactId=mq-pcf -Dversion=7.5.0.2 -Dpackaging=jar
mvn install:install-file -Dfile=./ibm/websphere/com.ibm.mq.jar -DgroupId=com.ibm -DartifactId=mq -Dversion=7.5.0.2 -Dpackaging=jar
mvn install:install-file -Dfile=./ibm/websphere/com.ibm.mq.jmqi.jar -DgroupId=com.ibm -DartifactId=jmqi -Dversion=7.5.0.2 -Dpackaging=jar
mvn install:install-file -Dfile=./ibm/websphere/com.ibm.mq.headers.jar -DgroupId=com.ibm -DartifactId=mq-headers -Dversion=7.5.0.2 -Dpackaging=jar