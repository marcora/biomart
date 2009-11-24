SELECT SYS.V_$SQL.SQL_ID, SYS.V_$SESSION.SQL_ID, SYS.V_$SQL.SQL_TEXT, SYS.V_$SQL.EXECUTIONS, SYS.V_$SQL.CHILD_ADDRESS, SYS.V_$SQL.ROWS_PROCESSED, SYS.V_$SQL.BUFFER_GETS, SYS.V_$SQL.MODULE, SYS.V_$SQL.LOADS, SYS.V_$SQL.FIRST_LOAD_TIME, SYS.V_$SQL.LAST_ACTIVE_TIME, SYS.V_$SESSION.SID, SYS.V_$SESSION.SADDR, SYS.V_$SESSION.SQL_CHILD_NUMBER, SYS.V_$SESSION.PREV_SQL_ADDR, SYS.V_$SESSION.LOGON_TIME, SYS.V_$SESSION.USER#, SYS.V_$SESSION.STATUS, SYS.V_$SESSION.PROCESS, SYS.V_$SESSION.PROGRAM, SYS.V_$SESSION.BLOCKING_SESSION_STATUS, SYS.V_$SESSION.EVENT  FROM SYS.V_$SQL, SYS.V_$SESSION WHERE SYS.V_$SQL.SQL_ID = SYS.V_$SESSION.SQL_ID(+) AND SYS.V_$SQL.SQL_TEXT LIKE '%long_query_test13%';
exit