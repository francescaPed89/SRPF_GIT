#!/bin/bash

export SRPF_CONF_FILE=/opt/SRPF/DBUtils/dbUtils.properties
MAIN_CLASS=com.telespazio.csg.srpf.importExportConfiguration.ImportExportConfiguration

JAR=/opt/SRPF/DBUtils/dbUtils.jar

if [ "$#" -ne 1 ]; then
    echo "Usage: importConfiguration.sh inputFilePath"
else
    echo "Warning the import of configuration will delete orbital data and PAW on db. Do you want proceed? y/n [n]" 
    read -r line
    
    if [ $line = "y" ]; then
        java -cp $JAR $MAIN_CLASS  import $1
    fi    

fi

