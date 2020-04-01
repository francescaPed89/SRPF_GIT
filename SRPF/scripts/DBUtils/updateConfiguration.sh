#!/bin/bash

export SRPF_CONF_FILE=/opt/SRPF/DBUtils/dbUtils.properties
MAIN_CLASS=com.telespazio.csg.srpf.importExportConfiguration.ImportExportConfiguration

JAR=/opt/SRPF/DBUtils/dbUtils.jar

if [ "$#" -ne 1 ]; then
    echo "Usage: importConfiguration.sh inputFilePath"
else
    echo "Warning the import of configuration will delete the following tables: sensor mode; beam and sat beams association. The other table will be unchanged: proceed y/n?" 
    read -r line
    
    if [ $line = "y" ]; then
        java -cp $JAR $MAIN_CLASS  update $1
    fi    

fi

