package org.biomart.test.cancelQueries;


import java.io.IOException;
import java.sql.SQLException;

import org.biomart.common.general.utils.MyUtils;
import org.biomart.common.general.utils.SqlUtils;


public class CancelQueriesThread extends Thread {

	private static final int TIME_OUT = 5000;
	private static final int CANCELATION = 2000;
	private static final String MY_SQL_MONITOR_COMMAND = 
		"mysql -h" + CancelQueriesTest.database.databaseHost + " -P" + CancelQueriesTest.database.databasePort + " -u" + 
		CancelQueriesTest.databaseUser + " -p" + CancelQueriesTest.databasePassword + " -e \"show processlist;\"";
	
	private static final String POSTGRESQL_MONITOR_COMMAND = // password is kept in ~/.pgpass as *:*:*:*:biomart
		"psql -h " + CancelQueriesTest.database.databaseHost + " -p" + CancelQueriesTest.database.databasePort + " -U" + 
		CancelQueriesTest.databaseUser + " -c \"select * from pg_stat_activity;\" " + CancelQueriesTest.database.databaseName;
	// psql -h bm-test.res.oicr.on.ca -U martadmin -c "select * from pg_stat_activity" ac_cancel

	private static final String SCRIPT_PATH_AND_NAME = "/home/anthony/workspace/00StandAlone/src/cancelQueries/oracle.sql";
	/*private static final String ORACLE_MONITOR_COMMAND = 
		"/usr/lib/oracle/xe/app/oracle/product/10.2.0/server/bin/sqlplus " + CancelQueriesTest.database.dbParam.databaseUser + "/" + CancelQueriesTest.database.dbParam.databasePassword + "@" + 
		CancelQueriesTest.databaseName + " @" + SCRIPT_PATH_AND_NAME;*/
	// sqlplus martadmin/biomart@XE @/home/anthony/workspace/00StandAlone/src/cancelQueries/oracle.sql

	@Override
	public void run() {
		try {
			
			if (CancelQueriesTest.database.rdbs.isOracle()) {
				String oracleMonitoringQuery = SqlUtils.ORACLE_MONITORING_QUERY_BASE + "AND SYS.V_$SQL.SQL_TEXT LIKE '%" + CancelQueriesTest.tableName + "%';" + 
				MyUtils.LINE_SEPARATOR + "exit";
				MyUtils.writeFile(SCRIPT_PATH_AND_NAME, oracleMonitoringQuery);
			}
			
			ThreadCommunication.println("Waiting");
			while (!CancelQueriesTest.isLaunched()) {
				Thread.sleep(500);
			}
			
			ThreadCommunication.println("About to cancel (in " + TIME_OUT + "  ms)");
			ThreadCommunication.print("Process list before cancel:");
			if (CancelQueriesTest.database.rdbs.isMySql()) {
				ThreadCommunication.println(MyUtils.LINE_SEPARATOR + MyUtils.runShCommand(MY_SQL_MONITOR_COMMAND).toString());
			} else if (CancelQueriesTest.database.rdbs.isPostgreSql()) {
				ThreadCommunication.println(MyUtils.LINE_SEPARATOR + MyUtils.runShCommand(POSTGRESQL_MONITOR_COMMAND).toString());
			} else if (CancelQueriesTest.database.rdbs.isOracle()) {
				/*String oracleMonitoringQuery = SqlUtils.ORACLE_MONITORING_QUERY_BASE + "AND SYS.V_$SQL.SQL_TEXT LIKE '%" + CancelQueriesTest.tableName + "%';" + 
				ThreadCommunication.prepareStatement(sql, oracleMonitoringQuery);
				ThreadCommunication.runExecuteQuery();
				ThreadCommunication.println(ThreadCommunication.getAllResult().toString());*/
				
				//ThreadCommunication.println(MyUtils.LINE_SEPARATOR + MyUtils.runCommand(ORACLE_MONITOR_COMMAND).toString());
			}
			
			Thread.sleep(TIME_OUT);
			ThreadCommunication.println("Cancelling");
			
			if (!CancelQueriesTest.database.rdbs.isOracle()) {
				CancelQueriesTest.cancelPreparedStatement();
			} else {
				CancelQueriesTest.closeResultSet();
			}

			
			Thread.sleep(CANCELATION);
			ThreadCommunication.print("Process list after cancel:");
			if (CancelQueriesTest.database.rdbs.isMySql()) {
				ThreadCommunication.println(MyUtils.LINE_SEPARATOR + MyUtils.runShCommand(MY_SQL_MONITOR_COMMAND).toString());
			} else if (CancelQueriesTest.database.rdbs.isPostgreSql()) {
				ThreadCommunication.println(MyUtils.LINE_SEPARATOR + MyUtils.runShCommand(POSTGRESQL_MONITOR_COMMAND).toString());
			} else if (CancelQueriesTest.database.rdbs.isOracle()) {
				//ThreadCommunication.println(MyUtils.LINE_SEPARATOR + MyUtils.runCommand(ORACLE_MONITOR_COMMAND).toString());
			}
		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (SQLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		ThreadCommunication.println("End of thread");
	}
}
