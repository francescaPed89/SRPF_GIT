mvn install:install-file -Dfile=jarhdf-2.11.0.jar -DgroupId=srpfextdep.hdf -DartifactId=jhdf -Dversion=2.11 -Dpackaging=jar

mvn install:install-file -Dfile=jarh4obj.jar -DgroupId=srpfextdep.hdf -DartifactId=jhdf4obj -Dversion=2.11 -Dpackaging=jar

mvn install:install-file -Dfile=jarhdf5-2.11.0.jar -DgroupId=srpfextdep.hdf -DartifactId=jhdf5 -Dversion=2.11 -Dpackaging=jar

mvn install:install-file -Dfile=jarh5obj.jar -DgroupId=srpfextdep.hdf -DartifactId=jhdf5obj -Dversion=2.11 -Dpackaging=jar


mvn install:install-file -Dfile=jarhdfobj.jar -DgroupId=srpfextdep.hdf -DartifactId=jhdfobj -Dversion=2.11 -Dpackaging=jar
mvn install:install-file -Dfile=ojdbc6.jar -DgroupId=srpfextdep.oracle.jdbc -DartifactId=ojdbc6 -Dversion=11 -Dpackaging=jar



mvn install:install-file -Dfile=CMAPI-4.0.4.jar -DgroupId=com.telespazio.csg.cm -DartifactId=CMAPI -Dversion=4.0.4 -Dpackaging=jar
mvn install:install-file -Dfile=srpfBackendClient-2.2.13p.jar -DgroupId=com.telespazio.csg.srpf -DartifactId=srpfBackendClient -Dversion=2.2.13p -Dpackaging=jar
mvn install:install-file -Dfile=servlet-api.jar -DgroupId=javax -DartifactId=servlet-api -Dversion=3.1.FR -Dpackaging=jar

