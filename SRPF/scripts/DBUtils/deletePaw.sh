#!/bin/bash

export SRPF_CONF_FILE=/opt/SRPF/DBUtils/dbUtils.properties
MAIN_CLASS=com.telespazio.csg.srpf.importTools.ImportPlatformActivityWindow
JAR=/opt/SRPF/DBUtils/dbUtils.jar

echo "Warning the PAW older than now will be deleted. Do you want to proceed? y/n [n]"
read -r line

if [ $line = "y" ]; then
    java -cp $JAR  $MAIN_CLASS delete 
fi

