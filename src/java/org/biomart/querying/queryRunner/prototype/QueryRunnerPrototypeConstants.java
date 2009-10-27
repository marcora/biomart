package org.biomart.querying.queryRunner.prototype;


public class QueryRunnerPrototypeConstants {

	public static final Boolean FULL_SPEED = false;
	
	public static final Boolean CHECK = FULL_SPEED ? false : 
		true;
	public static final Boolean LOG = FULL_SPEED ? false:
		true;
	public static final Boolean LOG_LOCKS = FULL_SPEED ? false :
		false;

	public static int MAX_DISPLAY_QUERY_LENGTH = 1000;
	public static int MAX_DISPLAY_QUERY_LENGTH2 = (int)(QueryRunnerPrototypeConstants.MAX_DISPLAY_QUERY_LENGTH/(double)2);

	public static final String TABLE_ALIAS = "a";
	public static final String COLUMN_ALIAS_PREFIX = "A";	// alias are like A0, A1, ...
	
	public static final int TOTAL_LINE_ITEMS = 5;	// mysql:bm-test.res.oicr.on.ca:3306:martadmin:biomart:ac_query_runner_50_1_100000_bottom	table0	l0	r0	pk0
	public static final int TOTAL_DB_PARAMS_ITEMS = 6;	//mysql:localhost:3306:root:root:link_indexes
	
	public static final int LINK_INDEX_CREATION_BATCH_SIZE = 10000;
	public static final int INDEXES_USE_BATCH_SIZE = 1000;
}
