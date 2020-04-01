#!/bin/bash

export SRPF_CONF_FILE=/opt/SRPF/DBUtils/dbUtils.properties
MAIN_CLASS=com.telespazio.csg.srpf.importTools.ManageAllocPlan
JAR=/opt/SRPF/DBUtils/dbUtils.jar

if [ "$#" -ne 1 ]; then
    echo "Usage: importSOE.sh filePath "
else
    java -cp $JAR  $MAIN_CLASS --import-soe $1
    #java -cp $JAR  $MAIN_CLASS import $1
fi

