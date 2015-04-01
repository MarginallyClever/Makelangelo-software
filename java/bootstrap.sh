#!/bin/bash
mvn org.apache.maven.plugins:maven-install-plugin:2.5.2:install-file \
                         -Dfile=lib/kabeja-0.4.jar -DgroupId=org.kabeja \
                         -DartifactId=kabeja -Dversion=0.4 \
                         -Dpackaging=jar -DlocalRepositoryPath=lib \
                         || { echo "Maven install kabeja-0.4.jar Unsuccessful"; exit 1; }

mvn org.apache.maven.plugins:maven-install-plugin:2.5.2:install-file \
                         -Dfile=lib/kabeja-0.4-sources.jar -DgroupId=org.kabeja \
                         -DartifactId=kabeja -Dversion=0.4 \
                         -Dpackaging=jar -DlocalRepositoryPath=lib \
                         -Dclassifier=sources \
                         || { echo "Maven install kabeja-0.4-sources.jar Unsuccessful"; exit 1; }

mvn org.apache.maven.plugins:maven-install-plugin:2.5.2:install-file \
                         -Dfile=lib/kabeja-0.4-javadoc.jar -DgroupId=org.kabeja \
                         -DartifactId=kabeja -Dversion=0.4 \
                         -Dpackaging=javadoc -DlocalRepositoryPath=lib \
                         -Dclassifier=javadoc \
                         || { echo "Maven install kabeja-0.4-javadoc.jar Unsuccessful"; exit 1; }
