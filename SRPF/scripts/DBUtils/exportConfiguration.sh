#!/bin/bash

export SRPF_CONF_FILE=/opt/SRPF/DBUtils/dbUtils.properties
MAIN_CLASS=com.telespazio.csg.srpf.importExportConfiguration.ImportExportConfiguration

JAR=/opt/SRPF/DBUtils/dbUtils.jar

if [ "$#" -ne 1 ]; then
    echo "Usage: exportConfiguration.sh outputFilePath"
else
    java -cp $JAR $MAIN_CLASS  export $1
fi

