# run as root (sudo bash)
SERVER=http://localhost:8082
WEB_PROJECT_NAME=MartService
PROJECT_PATH=/home/anthony/workspace/biomart-java
WEB_PROJECT_PATH=/home/anthony/workspace/$WEB_PROJECT_NAME
WAR_NAME=$WEB_PROJECT_NAME
APPLICATION_PATH=$SERVER/$WAR_NAME
SERVLET_NAME=martService
SERVLET_PATH=$APPLICATION_PATH/$SERVLET_NAME
TOMCAT_WEBAPP_FOLDER=/var/lib/tomcat6/webapps

# copy files
cp $PROJECT_PATH/conf/xml/martservice.xsd $WEB_PROJECT_PATH/xml/
cp $PROJECT_PATH/conf/xml/web.xml $WEB_PROJECT_PATH/WEB-INF/
cp $PROJECT_PATH/conf/files/portal.serial $WEB_PROJECT_PATH/files/
cp $PROJECT_PATH/web/index.jsp $WEB_PROJECT_PATH/
cp $PROJECT_PATH/lib/* $WEB_PROJECT_PATH/WEB-INF/lib/
cd $WEB_PROJECT_PATH

# delete previous war
rm -rf $TOMCAT_WEBAPP_FOLDER/$WAR_NAME.war $TOMCAT_WEBAPP_FOLDER/$WAR_NAME

# create new war
jar -cvf $TOMCAT_WEBAPP_FOLDER/$WAR_NAME.war .

# restart tomcat
/etc/init.d/tomcat6 restart

# doesn't work
# test application (http://localhost:8082/MartService/martService?username=anonymous&password=p&format=xml&type=getDatasets&martName=ensembl&martVersion=-1)
#firefox '$APPLICATION_PATH' \
#'$SERVLET_PATH?username=anonymous&password=p&format=xml&type=getDatasets&martName=ensembl&martVersion=-1' \
#'$SERVLET_PATH?username=anonymous&password=p&format=xml&type=query&query=$QUERY'


