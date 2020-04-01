#!/bin/bash

if [ "$#" -ne 2 ]; then
    echo "Usage: setSatelliteLookSide satelliteName lookSideId "
    echo "where: lookSideId=1 for right; lookSide=2 for left and lookSide=3 for both"
    exit
fi

SAR=$1
lookSide=$2

export SRPF_CONF_FILE=/opt/SRPF/DBUtils/dbUtils.properties
MAIN_CLASS=com.telespazio.csg.srpf.satellitetools.SatelliteTools
JAR=/opt/SRPF/DBUtils/dbUtils.jar

java -cp $JAR  $MAIN_CLASS  -L -S  $SAR -A $lookSide


