export LD_LIBRARY_PATH=$LD_LIBRARY_PTAH:/opt/SRPF/NativeLib
export SRPF_CONF_FILE=/opt/SRPF/BackEnd/srpfBackend.properties
#properties
#export CATALINA_OPTS="-Xms256m -Xmx8192m"
export CATALINA_OPTS="-Xms256m -Xmx12288m"
#export JAVA_OPTS="-Dcom.sun.management.jmxremote -Dcom.sun.management.jmxremote.port=9999  -Dcom.sun.management.jmxremote.ssl=false -Dcom.sun.management.jmxremote.authenticate=false"
 
ps -ef | grep java | grep -i catalina | grep -v grep > /dev/null 2> /dev/null
RETVAL=$?
if [ $RETVAL -eq 0 ]; then
   echo "Error an instance of frontend already runninng"
         exit 0
fi


TOMCAT_START_SCRIPT=/opt/SRPF/BackEnd/apache-tomcat-8.0.28/bin/startup.sh
$TOMCAT_START_SCRIPT
