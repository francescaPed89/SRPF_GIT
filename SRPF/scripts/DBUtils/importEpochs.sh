#!/bin/bash
export SRPF_CONF_FILE=/opt/SRPF/DBUtils/dbUtils.properties
MAIN_CLASS=com.telespazio.csg.srpf.importTools.ImportEpochs
JAR=/opt/SRPF/DBUtils/dbUtils.jar

if [ "$#" -ne 1 ]; then
    echo "Usage: importEpochs.sh filePath / directorypath"
else
    java -cp $JAR  $MAIN_CLASS  $1
fi

