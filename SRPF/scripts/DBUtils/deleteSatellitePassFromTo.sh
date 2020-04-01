#!/bin/bash

export SRPF_CONF_FILE=/opt/SRPF/DBUtils/dbUtils.properties
MAIN_CLASS=com.telespazio.csg.srpf.importTools.ManageAllocPlan
JAR=/opt/SRPF/DBUtils/dbUtils.jar

if [ "$#" -ne 3 ]; then
    echo "Usage: deleteSatellitePassFromTo.sh SATNAME startDate end Date. An example of date format is: 2016-12-21T02:12:00Z  "
else
    java -cp $JAR  $MAIN_CLASS -s $1 $2 $3
    #java -cp $JAR  $MAIN_CLASS import $1
fi

