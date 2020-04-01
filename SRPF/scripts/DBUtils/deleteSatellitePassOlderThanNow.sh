#!/bin/bash
#delete the satellite pass older than the cirrent time
export SRPF_CONF_FILE=/opt/SRPF/DBUtils/dbUtils.properties
MAIN_CLASS=com.telespazio.csg.srpf.importTools.ManageAllocPlan
JAR=/opt/SRPF/DBUtils/dbUtils.jar

java -cp $JAR  $MAIN_CLASS -D

