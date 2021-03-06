#!/bin/sh

# Note: /bin/sh doesn't work on Alphas (need to use bash thexre) but
# works everywhere else.

# Starts the MartRunner CLI application.

# Usage:
#
# prompt> bin/martrunner.sh <PORT>

TMP_ROOT=`dirname $0`/..
 
TMP_CLASSPATH=${TMP_ROOT}
TMP_CLASSPATH=${TMP_CLASSPATH}:${TMP_ROOT}/build/classes
TMP_CLASSPATH=${TMP_CLASSPATH}:${TMP_ROOT}/lib/mysql-connector-java-3.1.14-bin.jar
TMP_CLASSPATH=${TMP_CLASSPATH}:${TMP_ROOT}/lib/log4j-1.2.15.jar
TMP_CLASSPATH=${TMP_CLASSPATH}:${TMP_ROOT}/lib/jython.jar
TMP_CLASSPATH=${TMP_CLASSPATH}:${TMP_ROOT}/lib/ensj-util.jar
TMP_CLASSPATH=${TMP_CLASSPATH}:${TMP_ROOT}/lib/jdom.jar
TMP_CLASSPATH=${TMP_CLASSPATH}:${TMP_ROOT}/lib/martj.jar 
TMP_CLASSPATH=${TMP_CLASSPATH}:${TMP_ROOT}/lib/ojdbc14.jar
TMP_CLASSPATH=${TMP_CLASSPATH}:${TMP_ROOT}/lib/ecp1_0beta.jar
TMP_CLASSPATH=${TMP_CLASSPATH}:${TMP_ROOT}/lib/postgresql-8.3-604.jdbc3.jar
TMP_CLASSPATH=${TMP_CLASSPATH}:${TMP_ROOT}/lib/activation.jar
TMP_CLASSPATH=${TMP_CLASSPATH}:${TMP_ROOT}/lib/dsn.jar
TMP_CLASSPATH=${TMP_CLASSPATH}:${TMP_ROOT}/lib/imap.jar
TMP_CLASSPATH=${TMP_CLASSPATH}:${TMP_ROOT}/lib/mailapi.jar
TMP_CLASSPATH=${TMP_CLASSPATH}:${TMP_ROOT}/lib/pop3.jar
TMP_CLASSPATH=${TMP_CLASSPATH}:${TMP_ROOT}/lib/smtp.jar
TMP_CLASSPATH=${TMP_CLASSPATH}:${CLASSPATH}

TMP_JYTHON_LIB=${TMP_ROOT}/lib

echo "Starting MartRunner please wait .... " 

# Note: If you get Java "Out of memory" errors, try increasing the numbers
# in the -Xmx and -Xms parameters in the java command below. For performance
# sake it is best if they are both the same value.
java -Xmx256m -Xms256m -ea -cp $TMP_CLASSPATH org.biomart.runner.view.cli.MartRunner $@




