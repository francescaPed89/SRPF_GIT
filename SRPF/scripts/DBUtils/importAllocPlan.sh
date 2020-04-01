#!/bin/bash

export SRPF_CONF_FILE=/opt/SRPF/DBUtils/dbUtils.properties
MAIN_CLASS=com.telespazio.csg.srpf.importTools.ManageAllocPlan
JAR=/opt/SRPF/DBUtils/dbUtils.jar

if [ "$#" -ne 1 ]; then
    echo "Usage: importAllocPlan.sh filePath "
else
    java -cp $JAR  $MAIN_CLASS --import-all-plan $1
    #java -cp $JAR  $MAIN_CLASS import $1
fi

