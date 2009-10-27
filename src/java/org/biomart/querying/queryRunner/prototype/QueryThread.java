package org.biomart.querying.queryRunner.prototype;


import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.biomart.common.general.exceptions.FunctionalException;
import org.biomart.common.general.exceptions.TechnicalException;
import org.biomart.common.general.utils.Average;
import org.biomart.common.general.utils.MyUtils;
import org.biomart.common.general.utils.Timer;
import org.biomart.common.general.utils.WaitableBoolean;
import org.biomart.objects.helpers.DatabaseParameter;
import org.biomart.objects.helpers.Rdbs;


public class QueryThread extends Thread {

	enum Status {
		RESULTS, FIRST_DONE, DONE, 
		READY,
		CANCELLED,
	}
	
	private QueryThread previousQueryThread = null;
	private QueryThread nextQueryThread = null;
	
	private QueryRunnerIntermediaryResult currentResult = null;
	private WaitableBoolean done = null;
	private List<Status> statusList = null;
	private List<Status> readyList = null;
	
	private WaitableBoolean firstFinalResults = null;
	private WaitableBoolean someFinalResults = null;
	private WaitableBoolean cancelThread = null;
	private boolean unsynchronizedFirstFinalResults;	// non-synchronized version of the above boolean (optimization)
	private boolean unsynchronizedSomeFinalResults;
	private int checkCancelIndex;
	
	private Boolean first = null;
	private Boolean last = null;
	private Integer queryThreadIndex = null;
	private DatabaseParameter databaseParameter = null;
	private String databaseName = null;
	private String queryTemplate = null;
	private String tableName = null;
	
	private PreparedStatement preparedStatement = null;
	private ResultSet resultSet = null;
	
	private WaitableBoolean activeQuery = null;	// active = instantiated and not canceled
	
	private Boolean isMySql = null;
	private Boolean isOracle = null;
	private Boolean isPostgreSql = null;

	private Integer rows = null;	// Only relevant for last thread
	private Integer batchSize = null;
	private Boolean distinct = null;
	private Boolean isLinkIndex = null;
	private Boolean isSuperIndex = null;
	
	private List<String> leftJoinFields = null;
	private List<String> rightJoinFields = null;
	private List<String> resultFields = null;
	
	private List<String> leftJoinFieldFullNames = null;
	private List<String> rightJoinFieldFullNames = null;
	private List<String> resultFieldFullNames = null;
	
	private List<String> aliasedLeftJoinFieldFullNames = null;
	private List<String> aliasedRightJoinFieldFullNames = null;
	private List<String> aliasedResultFieldFullNames = null;
	
	private Integer totalFields = null;
	private List<String> headers = null;
	
	private List<Integer> joinFieldIndexesWithinResults = null;
	
	private Average launchQueryAverageTime = null;
	private Average fetchingQueryAverageTime = null;
	private Average fetchingBatchAverageTime = null;
	private Average intermediaryResultsProcessingAverageTime = null;
	
	private List<List<String>> currentBatchResults = null;
	private QueryRunnerIntermediaryResult batchResult = null;
	private String shortenedQuery = null;
	private Integer queryRow = null;
	
	public QueryThread() {}	// as simple placeholder for parameters
	
	public QueryThread(
			int totalThreads, int queryThreadIndex, Integer rows, int batchSize, boolean distinct, boolean isLinkIndex, boolean isSuperIndex,
			QueryThread parameters) throws TechnicalException {
		this(totalThreads, queryThreadIndex, rows, batchSize, distinct, isLinkIndex, isSuperIndex, 
				parameters.getDatabaseParameter(), parameters.getTableName(), 
				parameters.getLeftJoinFields(), parameters.getRightJoinFields(), parameters.getResultFields());
	}
	
	public QueryThread(
			int totalThreads, int queryThreadIndex, Integer rows, int batchSize, boolean distinct, boolean isLinkIndex, boolean isSuperIndex,
			DatabaseParameter databaseParameter, String tableName, 
			List<String> leftJoinFields, List<String> rightJoinFields, List<String> resultFields) throws TechnicalException {
		super();
		this.queryThreadIndex = queryThreadIndex;
		this.first = queryThreadIndex==0;
		this.last = queryThreadIndex==totalThreads-1;
		
		this.rows = rows;
		this.batchSize = batchSize;
		this.distinct = distinct;
		this.isLinkIndex = isLinkIndex;
		this.isSuperIndex = isSuperIndex;
		this.databaseParameter = databaseParameter;
		this.databaseName = databaseParameter.getDatabaseName();

		this.isMySql = databaseParameter.getRdbs().isMySql();
		this.isOracle = databaseParameter.getRdbs().isOracle();
		this.isPostgreSql = databaseParameter.getRdbs().isPostgreSql();
		
		this.tableName = tableName;
		this.leftJoinFields = leftJoinFields;
		this.rightJoinFields = rightJoinFields;
		this.resultFields = resultFields;
		
		this.leftJoinFieldFullNames = new ArrayList<String>();
		this.aliasedLeftJoinFieldFullNames = new ArrayList<String>();
		for (int i = 0; i < leftJoinFields.size(); i++) {
			this.leftJoinFieldFullNames.add(this.databaseName + "." + this.tableName + "." + leftJoinFields.get(i));			
			this.aliasedLeftJoinFieldFullNames.add(QueryRunnerPrototypeConstants.TABLE_ALIAS + "." + leftJoinFields.get(i));			
		}
		this.rightJoinFieldFullNames = new ArrayList<String>();
		this.aliasedRightJoinFieldFullNames = new ArrayList<String>();
		for (int i = 0; i < rightJoinFields.size(); i++) {
			this.rightJoinFieldFullNames.add(this.databaseName + "." + this.tableName + "." + rightJoinFields.get(i));		
			this.aliasedRightJoinFieldFullNames.add(QueryRunnerPrototypeConstants.TABLE_ALIAS + "." + rightJoinFields.get(i));			
		}
		this.resultFieldFullNames = new ArrayList<String>();
		this.aliasedResultFieldFullNames = new ArrayList<String>();
		for (int i = 0; i < resultFields.size(); i++) {
			this.resultFieldFullNames.add(this.databaseName + "." + this.tableName + "." + resultFields.get(i));	
			this.aliasedResultFieldFullNames.add(QueryRunnerPrototypeConstants.TABLE_ALIAS + "." + resultFields.get(i));			
		}
		
		this.totalFields = 0;
		headers = new ArrayList<String>();
		StringBuffer queryTemplateSb = new StringBuffer();
		queryTemplateSb.append("select ");
		for (int i = 0; i < this.aliasedLeftJoinFieldFullNames.size(); i++) {
			queryTemplateSb.append((i==0 ? "" : ",") + this.aliasedLeftJoinFieldFullNames.get(i) 
					//+ " as " + (QueryRunnerPrototypeConstants.COLUMN_ALIAS_PREFIX + i)
					);
			headers.add(this.leftJoinFieldFullNames.get(i));
			this.totalFields++;
		}
		for (int i = 0; i < this.aliasedRightJoinFieldFullNames.size(); i++) {
			queryTemplateSb.append((i==0 ? (this.aliasedLeftJoinFieldFullNames.size()==0 ? "" : ",") : ",") + this.aliasedRightJoinFieldFullNames.get(i));
			headers.add(this.rightJoinFieldFullNames.get(i));
			this.totalFields++;
		}
		for (int i = 0; i < this.aliasedResultFieldFullNames.size(); i++) {
			String resultField = this.aliasedResultFieldFullNames.get(i);
			//if (!this.leftJoinFieldFullNames.contains(resultField) && !this.rightJoinFieldFullNames.contains(resultField)) {
				queryTemplateSb.append((i==0 ? (this.aliasedLeftJoinFieldFullNames.size()==0 && this.aliasedRightJoinFieldFullNames.size()==0 ? "" : ",") : ",") + resultField);
				headers.add(this.resultFieldFullNames.get(i));
				this.totalFields++;
			//}
		}
		
		queryTemplateSb.append(" from ");
		queryTemplateSb.append((this.isMySql ? databaseName + "." : "") +	// that doesn't seem to work with postgres 
				tableName + (!this.isOracle ? " as" : "") + " " + QueryRunnerPrototypeConstants.TABLE_ALIAS);
		
		this.queryTemplate = queryTemplateSb.toString();
		
		this.launchQueryAverageTime = new Average();
		this.fetchingQueryAverageTime = new Average();
		this.fetchingBatchAverageTime = new Average();
		this.intermediaryResultsProcessingAverageTime = new Average();
		
		this.statusList = new ArrayList<Status>();
		this.readyList = new ArrayList<Status>();
		
		this.done = new WaitableBoolean(false);
		this.firstFinalResults = new WaitableBoolean(false);	// always except for last that will provide them
		this.someFinalResults = new WaitableBoolean(false);		// always except for last that will provide them
		this.unsynchronizedFirstFinalResults = false;
		this.unsynchronizedSomeFinalResults = false;
		
		this.cancelThread = new WaitableBoolean(false);			// until told differently
		this.activeQuery = new WaitableBoolean(false);
		
		this.readyList.add(Status.READY);	// simulate a signal for all threads to notify they are ready
	}
	
	public void setSurroundingThreads(QueryThread previousQueryThread, QueryThread nextQueryThread) {
		this.previousQueryThread = previousQueryThread;
		this.nextQueryThread = nextQueryThread;
	}

	public void determineJoinFieldIndexesWithinResults(int globalTotalFields) throws TechnicalException {
		this.joinFieldIndexesWithinResults = new ArrayList<Integer>();
		int leftJoinfFieldsSize = this.leftJoinFields.size();		
		for (int i = 0; i < this.rightJoinFields.size(); i++) {
			this.joinFieldIndexesWithinResults.add(globalTotalFields + leftJoinfFieldsSize + i);
		}		
	}

	@Override
	public void run() {
		super.run();
		
		try {
			outln("Thread " + queryThreadIndex + " started.");
			process();
			outln("Thread " + queryThreadIndex + " ended.");
		} catch (TechnicalException e) {
			e.printStackTrace();
			System.exit(-1);
		} catch (FunctionalException e) {
			e.printStackTrace();
			System.exit(-1);
		}
	}
	
	public Connection connect() throws TechnicalException {
		
		Connection connection = null;
		String connectionString = null;
		try {		
			if (databaseParameter.getRdbs().isMySql()) {
				DriverManager.registerDriver (new com.mysql.jdbc.Driver());
				connectionString = "jdbc:mysql://" + databaseParameter.getDatabaseHost() + ":" + 
				databaseParameter.getDatabasePort() + "/" + databaseParameter.getDatabaseName();
			} else if (databaseParameter.getRdbs().isPostgreSql()) {
				DriverManager.registerDriver (new org.postgresql.Driver());
				connectionString = "jdbc:postgresql://" + databaseParameter.getDatabaseHost() + ":" + 
				databaseParameter.getDatabasePort() + "/" + databaseParameter.getDatabaseName();
			} else if (databaseParameter.getRdbs().isOracle()) {
				DriverManager.registerDriver (new oracle.jdbc.driver.OracleDriver());
				connectionString = "jdbc:oracle:thin:@" + databaseParameter.getDatabaseHost() + ":" + 
				databaseParameter.getDatabasePort() + ":" + databaseParameter.getDatabaseName();
			}		
			
			connection = DriverManager.getConnection(
					connectionString, databaseParameter.getDatabaseUser(), databaseParameter.getDatabasePassword());
			
			if (databaseParameter.getRdbs().isPostgreSql()) {
				connection.setAutoCommit(false);	// Need to explicitely set auto-commit to false (for streaming results)
			}
		} catch (SQLException e) {
			throw new TechnicalException(connectionString, e);
		}
		return connection;
	}

	private void process() throws TechnicalException, FunctionalException {
		
		try {
			Connection connection = connect();
			
			while (true) {	// broken when done
				
				this.checkCancelIndex = 0;

				if (checkCancelThread()) {	// regularily check for cancel request and exit when encountered
					break;
				}
				
				QueryRunnerIntermediaryResult previousThreadResults = null;
				if (!first) {
					List<Status> tmp = fetchPreviousThreadNextStatus();
					
					if (checkCancelThread()) {	// see above similar line
						break;
					}
					
					previousThreadResults = processPreviousThreadDoneStatus(tmp);
					if (previousThreadResults==null) {	// means previous thread is done
						break;
					}
				}

				if (checkCancelThread()) {	// see above similar line
					break;
				}
				
				StringBuffer currentQuery = new StringBuffer(this.queryTemplate);
				boolean potentialResults = true;
				if (first) {
					previousThreadResults = new QueryRunnerIntermediaryResult(this.queryThreadIndex);
					outln("first: no input needed");
				} else {
					outln("Processing input");
					potentialResults = customizeQuery(previousThreadResults, currentQuery);
				}
				
				this.shortenedQuery = currentQuery.length()<QueryRunnerPrototypeConstants.MAX_DISPLAY_QUERY_LENGTH ? currentQuery.toString() :
						(currentQuery.substring(0, QueryRunnerPrototypeConstants.MAX_DISPLAY_QUERY_LENGTH2).toString() + " ... " + 
						currentQuery.substring(currentQuery.length()-QueryRunnerPrototypeConstants.MAX_DISPLAY_QUERY_LENGTH2, currentQuery.length()).toString());
				outln(potentialResults + ", " + "shortenedQuery = " + shortenedQuery);	
				writelnQueryFile(shortenedQuery);

				if (checkCancelThread()) {	// see above similar line
					break;
				}
				
				outln("preparing statement");
				synchronized (this.activeQuery) {	// always get the lock on that object to handle the query related-object: 2 threads can use them (main one and current one)
					lockln("lock acquired on activeQuery");
					if (this.preparedStatement==null) {
check(!this.activeQuery.getValue());
check(this.resultSet==null);	// not sure
						prepareStreamingStatement(connection, currentQuery);
					} else {
check(this.activeQuery.getValue());
check(this.resultSet!=null);	// not sure							
						this.resultSet.close();
						this.preparedStatement.close();
						prepareStreamingStatement(connection, currentQuery);
					}
					this.activeQuery.setTrue();		// set it to true: meaning cancel would have to actually cancel the query and close as much as it can
					lockln("lock released on activeQuery");
				}
				
				if (checkCancelThread()) {	// see above similar line
					break;
				}
				
				Timer queryTimer = new Timer();
				queryTimer.startTimer();
				
				Timer queryLaunchTimer = new Timer();
				queryLaunchTimer.startTimer();
				
				Integer columnCount = null;
				if (potentialResults) {
					outln("executing query");
					synchronized (this.activeQuery) {	// see above similar line 
						lockln("lock acquired on activeQuery");
						this.resultSet = this.preparedStatement.executeQuery(
								//currentQuery.toString());	// for Statement
								);	// for PreparedStatement
						outln("query prepared");
						columnCount = this.resultSet.getMetaData().getColumnCount();
						// activeQuery is already set to true here: it means we must always check whether resultSet is null or not before trying to close it
						lockln("lock released on activeQuery");
					}
				}
				
				queryLaunchTimer.stopTimer();
				this.launchQueryAverageTime.addToAverage(queryLaunchTimer.getTimeEllapsedMs());
				
				if (checkCancelThread()) {	// see above similar line
					break;
				}
				
				int batchNumber = 0;	// For the current query
				queryRow = 0;		// For the current query
				Boolean next = null;			
				boolean queryCancellation = false;		// so we can break the 2 embedded query while loops
				while (	// 1st query while loop: query one
						(potentialResults && 
						((next=resultSetNext(resultSet)) || batchNumber==0)) ||
						(!potentialResults && batchNumber==0)) {	// We want to go inside the first time even if no results	
					
					this.currentBatchResults = new ArrayList<List<String>>();
					int batchRow = 0;	// For the current batch for the current query
					outln("Fetching batch");
					
					Timer batchTimer = new Timer();
					batchTimer.startTimer();
					
					while (	// 2st query while loop: batch one
							batchRow<this.batchSize &&
							potentialResults && ((batchRow==0 && next) || (batchRow>0 && resultSetNext(resultSet)))) {
						
			            List<String> list = new ArrayList<String>();
			            
			        	// we also lock on that object when retrieving a row
						synchronized (this.activeQuery) {	// see above similar line 
							lockln("lock acquired on activeQuery");	
			            	if (this.activeQuery.getValue()) {
				            	for (int i = 0; i < columnCount; i++) {							
				            		String fieldValue = resultSet.getString(i+1);
				            		list.add(fieldValue);	// accepting null values
				            	}							
			            	} else {
			            		queryCancellation = true;
			            	}
			            	
			            	lockln("lock released on activeQuery");
			            	
			            	if (queryCancellation) {	// break 2nd query while loop: batch one (not the main one)
			            		break;
			            	}
				        }
			            currentBatchResults.add(list);
			            batchRow++;
			            queryRow++;
			        } // end of the 2nd query while loop (batch)

					batchTimer.stopTimer();
					this.fetchingBatchAverageTime.addToAverage(batchTimer.getTimeEllapsedMs());
					writelnQueryFile(MyUtils.TAB_SEPARATOR + MyUtils.TAB_SEPARATOR + batchRow + " rows in " + batchTimer.getTimeEllapsedMs() + " ms");
StringBuffer stringBuffer = new StringBuffer();/*stringBuffer.append("{");for (int i = 0; i < currentBatchResults.size(); i++) {stringBuffer.append(currentBatchResults.get(i).get(0) + ",");}stringBuffer.append("}");stringBuffer.toString();*/
					outln("currentBatchResults.size() = " + currentBatchResults.size()
							+ ", " + stringBuffer
					);

					if (queryCancellation) {	// break 1st query while loop: query one (not the main one)
						break;
					}					
					
					// if query produced results, process them
					if (!currentBatchResults.isEmpty()) {
if (first) outln(MyUtils.DASH_LINE + currentBatchResults.get(0));
						boolean threadCancellation = processBatchResults(queryThreadIndex, previousThreadResults);	// also check for cancelation
						
						if (threadCancellation) {	// break 1st query while loop (not the main one)
							break;
						}
					}
					
					batchNumber++;
				} // end of the 1st query while loop (query)s

				queryTimer.stopTimer();
				this.fetchingQueryAverageTime.addToAverage(queryTimer.getTimeEllapsedMs());
				MyUtils.checkStatusProgram(potentialResults || queryRow==0);
				writelnQueryFile(MyUtils.TAB_SEPARATOR + queryRow + " rows (sum) in " + queryTimer.getTimeEllapsedMs() + " ms");
				
				// First thread only has 1 query and is finished
				if (first) {
					outln("Setting thread to done");
					synchronized (this.done) {
						this.done.setTrue();
					}
					synchronized (this.statusList) {						
						this.statusList.add(Status.FIRST_DONE);
						this.statusList.notify();	// To notify second thread
					}
					break;
				}
			}	// end of the main while loop

			// close resources
			outln("trying to acquire query lock");
			synchronized (this.activeQuery) {	// see above similar line 
				lockln("lock acquired on activeQuery");
				if (this.activeQuery.getValue()) {
					if (null!=this.resultSet) {
						this.resultSet.close();
					}
check(this.preparedStatement!=null);
					this.preparedStatement.close();
					this.activeQuery.setFalse();
				}				
				lockln("lock released on activeQuery");
			}
			connection.close();
			
			// Update average times
			this.launchQueryAverageTime.update();
			this.fetchingQueryAverageTime.update();
			this.fetchingBatchAverageTime.update();
			this.intermediaryResultsProcessingAverageTime.update();
			
		} catch (SQLException e) {
			throw new TechnicalException(e);
		} catch (IOException e) {
			throw new TechnicalException(e);
		}
	}
	
	private List<Status> fetchPreviousThreadNextStatus() throws TechnicalException {
		outln("waiting for previous thread to either provide data and/or be done");
		List<Status> previousStatusList = this.previousQueryThread.getChangedStatusList();
		List<Status> tmp = null; 
		Status firstStatus = null; 
		
		// Obtain lock on previous status
		synchronized (previousStatusList) {
			while (previousStatusList.isEmpty()) {
				try {
					previousStatusList.wait();
				} catch (InterruptedException e) {
					throw new TechnicalException(e);
				}
			}
			if (!previousStatusList.isEmpty()) {
				firstStatus = previousStatusList.remove(0);	// remove status from list, it will then be processed
			} else {
MyUtils.errorProgram("SPONTANEOUS NOTIFICATION!!!!!!!!");	// shouldn't happen really							
			}
if (QueryRunnerPrototypeConstants.CHECK) tmp = new ArrayList<Status>(previousStatusList);
		}
		outln("previous status to process: " + firstStatus);
		
		return tmp;
	}
	
	private QueryRunnerIntermediaryResult processPreviousThreadDoneStatus(List<Status> tmp) throws TechnicalException {
		
		QueryRunnerIntermediaryResult previousThreadResults = this.previousQueryThread.getCurrentResult();
		WaitableBoolean previousDone = this.previousQueryThread.getDone();
check(previousThreadResults!=null || (previousThreadResults==null && previousDone.getValue()),
(previousThreadResults==null) + ", " + previousDone);
		
		if (previousThreadResults==null) {	// means done (the opposite isn't true)
check(tmp==null ||	// can be null if CHECK is off 
		tmp.isEmpty(), checklog("previousChangedStatusList = " + tmp));	// There could have only been one signal sent if done and no previous results to process
check(previousDone.getValue());

			// If last thread and no results, notify that first results (=no results) have arrived
			outln("previous thread (" + (queryThreadIndex-1) + ") is done and did not provide data: " + "stopping current thread");
			if (this.last) {
				if (!this.unsynchronizedFirstFinalResults) {
					outln("notify first results (=no results)  have arrived");
					synchronized (this.firstFinalResults) {
						this.unsynchronizedFirstFinalResults = true;
						this.firstFinalResults.setTrue();
						this.firstFinalResults.notify();	// notify main thread
					}
				}
				if (!this.unsynchronizedSomeFinalResults) {
					outln("notify some results (=no results) have arrived");
					synchronized (this.someFinalResults) {
						this.unsynchronizedSomeFinalResults = true;
						this.someFinalResults.setTrue();
						this.someFinalResults.notify();	// notify main thread
					}
				}
			}
			
			// In any case set done to true and update status for next thread to be aware of
			synchronized (this.done) {
				this.done.setTrue();
			}
			synchronized (this.statusList) {							
				this.statusList.add(Status.DONE);
				this.statusList.notify();
			}
			
			// Leave the main while loop
			return null;
			
		} else {
			
			// Data has been provided, it will be processed
			outln("data provided by previous thread");
check(previousThreadResults!=null && previousThreadResults.getTotalRows()>0, "" + (previousThreadResults==null ? previousThreadResults : previousThreadResults.getTotalRows()));
			this.previousQueryThread.resetCurrentResult();	// intermediary results have already been stored here, 
															// we can clear previous thread of it so it can go on with the next batch 
															// TODO move to previous thread? after the following notification: it would know it can do it
			
			// update status for previous thread to know that its results are handled now and that it can fetch the next one
			synchronized (this.readyList) {	//TODO see above todo
				this.readyList.add(Status.READY);
				this.readyList.notify();
			}
		}
		
		return previousThreadResults;
	}

	private void prepareStreamingStatement(Connection connection, StringBuffer currentQuery) throws SQLException {
		this.preparedStatement = connection.prepareStatement(
				currentQuery.toString()
				, java.sql.ResultSet.TYPE_FORWARD_ONLY, java.sql.ResultSet.CONCUR_READ_ONLY
		);					
		if (this.isMySql) {
			preparedStatement.setFetchSize(Integer.MIN_VALUE);	// MySql's way of telling server to stream (only way)
		} else if (this.isPostgreSql) {
			preparedStatement.setFetchSize(1);	// Postgres' way of telling server to stream (could be another value) + see autoCommit
		} // Nothing to do for Oracle: streaming is on by default
	}
	
	private boolean customizeQuery(QueryRunnerIntermediaryResult previousThreadResults, StringBuffer currentQuery) {
		boolean potentialResults = true;

		List<Integer> previousJoinFieldIndexesWithinResults = previousThreadResults.getPreviousJoinFieldIndexesWithinResults();
check(previousJoinFieldIndexesWithinResults.size()==this.leftJoinFieldFullNames.size());

		if (previousThreadResults.getTotalRows()>0) {
		
			// Use an IN list if only 1 field (more efficient)
			StringBuffer stringBuffer = new StringBuffer();
			boolean atLeastOne = false;
			if (this.leftJoinFieldFullNames.size()==1) {
				
				// Just checked only 1
				String column = this.aliasedLeftJoinFieldFullNames.get(0);
				int index = previousJoinFieldIndexesWithinResults.get(0);
				
				stringBuffer.append(" where " + column + " in (");
				Set<String> setValues = new HashSet<String>();
				for (int rowNumber = 0; rowNumber < previousThreadResults.getTotalRows(); rowNumber++) {
					List<String> row = previousThreadResults.getValueRow(rowNumber);
					String value = row.get(index);
					if (null!=value && !setValues.contains(value)) {	// Use of set to avoid doubles (useless in the IN list)
						setValues.add(value);	
						stringBuffer.append((rowNumber==0 ? "" : ",") + "'" + value + "'");
					}
				}
				stringBuffer.append(")");
				atLeastOne = !setValues.isEmpty();	// When only nulls
			} else {
check(this.leftJoinFieldFullNames.size()==previousJoinFieldIndexesWithinResults.size());							
				
				for (int rowNumber = 0; rowNumber < previousThreadResults.getTotalRows(); rowNumber++) {
					List<String> row = previousThreadResults.getValueRow(rowNumber);				
					
					StringBuffer stringBuffer2 = new StringBuffer();								
					for (int i = 0; i < this.aliasedLeftJoinFieldFullNames.size(); i++) {
						int index = previousJoinFieldIndexesWithinResults.get(i);	// because we know both list are the same size						
						String value = row.get(index);
						if (null!=value) { 
							stringBuffer2.append((i==0 ? "" : " and ") +
									this.aliasedLeftJoinFieldFullNames.get(i) +
									"=" + "'" + value + "'");
						} else {	// erase and break
							stringBuffer2 = null;
							break;
						}
					}
					if (null!=stringBuffer2) {
						stringBuffer.append((!atLeastOne ? " where " : " or ") +		// first one is where 
								"(" + stringBuffer2 + ")");
						atLeastOne = true;									
					}
				}
			}
			currentQuery.append(stringBuffer.toString()); 
			if (!atLeastOne) {
				potentialResults=false;
			}
		} else {
			potentialResults=false;	// if so, no results
		}
		return potentialResults;
	}
	private boolean resultSetNext(ResultSet resultSet) throws SQLException {

//outln("hitting next()");
//if (queryThreadIndex==0) outln("hitting next() - " + this.queryRow/* + " - " + this.shortenedQuery*/);
		boolean next;
		synchronized (this.activeQuery) {	// see above similar line 
			lockln("lock acquired on activeQuery");
			next = this.activeQuery.getValue() ? resultSet.next() : false;
			lockln("lock released on activeQuery");
		}
//outln("leaving next() - " + next);
//if (queryThreadIndex==0) outln("leaving next() - " + next);
		
		return next;
	}

	private boolean processBatchResults(int currentQueryThreadIndex, QueryRunnerIntermediaryResult previousThreadResults) 
	throws TechnicalException, FunctionalException {

		if (!first) {
			outln("Merging results");
		}
		
		Timer intermediaryResultsProcessingTimer = null;
		if (!this.first && !this.last) {
			intermediaryResultsProcessingTimer = new Timer();
			intermediaryResultsProcessingTimer.startTimer();
		}
		
		// Prepare results for join with the next query
		this.batchResult = prepareResults(previousThreadResults, currentBatchResults);

		if (!last) {

			// wait till next thread is start processing the latest results
			List<Status> nextReadyList = nextQueryThread.getReadyList();
			List<Status> tmp = null; 
			Status firstStatus = null;
			synchronized (nextReadyList) {
				outln("wait for next thread to be ready");
				while(nextReadyList.isEmpty()) {
					try {
						nextReadyList.wait();
					} catch (InterruptedException e) {
						throw new TechnicalException(e);
					}
				}
				if (!nextReadyList.isEmpty()) {
					firstStatus = nextReadyList.remove(0);
				} else {
MyUtils.errorProgram("SPONTANEOUS NOTIFICATION!!!!!!!!");	// shouldn't happen really							
				}
				tmp = new ArrayList<Status>(nextReadyList);
			}
			outln("next status to process: " + firstStatus);
			if (checkCancelThread()) {	// check here in case we sent a signal because of cancelling
				return true;
			}
check(tmp.isEmpty(), checklog("nextReadyList = " + nextReadyList));
check(null==this.currentResult);	// Must have been freed by next thread (call to resetCurrentResult())

			// Add the newest results for the next thread to process as soon as it's ready
			outln("Add input for next thread");
			this.currentResult = batchResult;
			synchronized (this.statusList) {				
				this.statusList.add(Status.RESULTS);
				this.statusList.notify();	// To notify next thread
			}
		} else {
			// Add final results
			outln("Add final results");
			
			List<Integer> ignoreList = ThreadCommunication.getIgnoreList();
			boolean cancelationInProcess = this.rows!=null && ThreadCommunication.getTotalRows()>=this.rows;
			if (!cancelationInProcess) {
				try {
					for (List<String> list : batchResult.getValues()) {
						boolean first = true;
						for (int i = 0; i < list.size(); i++) {
							if (!ignoreList.contains(i)) {
								ThreadCommunication.bufferedWriter.write((first ? "" : MyUtils.TAB_SEPARATOR) + list.get(i));
								if (!QueryRunnerPrototypeConstants.LOG) {
									System.out.print((first ? "" : MyUtils.TAB_SEPARATOR) + list.get(i));	
								}
								first = false;
							}
						}
						ThreadCommunication.bufferedWriter.write(MyUtils.LINE_SEPARATOR);
						if (!QueryRunnerPrototypeConstants.LOG) {
							System.out.println();	
						}
						int totalRows = ThreadCommunication.addRowToTotalRows();
						if (!this.unsynchronizedFirstFinalResults && totalRows==1) {	// if first row
							outln("notify first results have arrived");
							synchronized (this.firstFinalResults) {
								this.firstFinalResults.setTrue();
								this.firstFinalResults.notify();	// notify main thread
								this.unsynchronizedFirstFinalResults = true;
							}
						}
						if (!this.unsynchronizedSomeFinalResults && this.rows!=null && totalRows==this.rows.intValue()) {
							outln("notify some results have arrived");				
							synchronized (this.someFinalResults) {
								this.unsynchronizedSomeFinalResults = true;
								this.someFinalResults.setTrue();
								this.someFinalResults.notify();	// notify main thread
							}
							break;
						}
					}	
					if (QueryRunnerPrototypeConstants.LOG) {
						ThreadCommunication.bufferedWriter.flush();
					}
				} catch (IOException e) {
					throw new TechnicalException(e);
				}
			}
		}
		this.batchResult = null;
		
		if (!this.first && !this.last) {
			intermediaryResultsProcessingTimer.stopTimer();
			this.intermediaryResultsProcessingAverageTime.addToAverage(intermediaryResultsProcessingTimer.getTimeEllapsedMs());
		}
		
		return false;
	}
	
	private QueryRunnerIntermediaryResult prepareResults(QueryRunnerIntermediaryResult previousThreadResults, List<List<String>> currentBatchResults) 
	throws TechnicalException {
		QueryRunnerIntermediaryResult batchResult = new QueryRunnerIntermediaryResult(previousThreadResults, 
					this.joinFieldIndexesWithinResults);	// Tell next thread what field to use for the join
		
		int totalResultRows = 0;
		if (this.first) {
			for (List<String> list : currentBatchResults) {
check(this.isLinkIndex || list.size()==this.totalFields);
				
				List<String> row = new ArrayList<String>(list);
				if (this.distinct && batchResult.containsRow(row)) {
					continue;
				} 
				batchResult.addValueRow(row);
				totalResultRows++;
			}
		} else {
			List<Integer> previousJoinFieldIndexesWithinResults = previousThreadResults.getPreviousJoinFieldIndexesWithinResults();
			boolean oneFieldJoin = previousJoinFieldIndexesWithinResults.size()==1;
			
int max = -1;
if (QueryRunnerPrototypeConstants.CHECK) max = Collections.max(previousJoinFieldIndexesWithinResults);
			for (int rowNumber = 0; rowNumber < previousThreadResults.getTotalRows(); rowNumber++) {
				List<String> previousRow = previousThreadResults.getValueRow(rowNumber);
check(previousRow.size()>max);

				String previousJoinFieldValue = null;
				List<String> previousJoinFieldValues = null;
				if (oneFieldJoin) {
					int index = previousJoinFieldIndexesWithinResults.get(0);
					previousJoinFieldValue = previousRow.get(index);
				} else {
					previousJoinFieldValues = new ArrayList<String>();
					for (int index : previousJoinFieldIndexesWithinResults) {
						previousJoinFieldValues.add(previousRow.get(index));
					}
				}
				
				for (List<String> list : currentBatchResults) {
					String leftJoinFieldValue = null;
					List<String> leftJoinFieldValues = null;
					if (oneFieldJoin) {
						leftJoinFieldValue = list.get(0);
					} else {
						leftJoinFieldValues = new ArrayList<String>();
						for (int index = 0; index < this.leftJoinFieldFullNames.size(); index++) {	// Since these are the first ones
							leftJoinFieldValues.add(list.get(index));
						}
					}
					if ((oneFieldJoin && QueryRunnerPrototypeUtils.stringEquals(previousJoinFieldValue,leftJoinFieldValue)) || 
							(!oneFieldJoin && QueryRunnerPrototypeUtils.stringListEquals(previousJoinFieldValues, leftJoinFieldValues))) {	
																							// nulls are considered (not a match if any of the string is null)
																							// same size for the list is a input requirement (as well as order)
						List<String> row = new ArrayList<String>(previousRow);
						row.addAll(list);
						if (this.distinct && batchResult.containsRow(row)) {
							continue;
						} 
						batchResult.addValueRow(row);
						totalResultRows++;
					}
				}			
			}
		}
		
		return batchResult;
	}

	/**
	 * Stop everything if cancel requested
	 */
	private boolean checkCancelThread() {
		Boolean cancelThread = null;
		synchronized (this.cancelThread) {
			cancelThread = this.cancelThread.getValue();
		}
		
		// Must send every signals in order to unblock any wait() call (we immediately check for cancel signal after every wait())
		if (cancelThread) {
			outln("cancel request received: step " + this.checkCancelIndex);
			synchronized (this.done) {
				this.done.setTrue();
			}
			synchronized (this.statusList) {				
				this.statusList.add(Status.CANCELLED);
				this.statusList.notify();
			}
			synchronized (this.readyList) {
				this.readyList.add(Status.CANCELLED);
				this.readyList.notify();
			}
		}
		this.checkCancelIndex++;
		return cancelThread;
	}

	private void writelnQueryFile(String string) throws IOException {
		if (QueryRunnerPrototypeConstants.LOG) {
			ThreadCommunication.queryBw[queryThreadIndex].write(string + MyUtils.LINE_SEPARATOR);
		}
	}

	private synchronized void outln(String string) {
		if (QueryRunnerPrototypeConstants.LOG) {
			System.out.println(getLogPrefix() + string);
		}
	}

	private String getLogPrefix() {
		Integer size =this.currentBatchResults==null ? null : this.currentBatchResults.size();
		Integer totalRows = this.batchResult==null ? null : this.batchResult.getTotalRows();
		Integer totalColumns = this.batchResult==null ? null : this.batchResult.getTotalColumns();
		Integer totalFinalRows = ThreadCommunication.getTotalRows();
		String logPrefix = "[" + queryThreadIndex + "-" + size + "-" + 
										totalRows + "-" + totalColumns + "-" + totalFinalRows + "]" + 
										MyUtils.TAB_SEPARATOR;
		return logPrefix;
	}
	
	private synchronized void lockln(String string) {
		if (QueryRunnerPrototypeConstants.LOG && QueryRunnerPrototypeConstants.LOG_LOCKS) {
			System.out.println("[" + queryThreadIndex + "]" + MyUtils.TAB_SEPARATOR + string);
		}
	}
	
	private void check(boolean test) {
		check(test, "");
	}
	private void check(boolean test, String message) {
		if (QueryRunnerPrototypeConstants.CHECK) MyUtils.checkStatusProgram(test, checklog(message));
	}
	private String checklog(String string) {
		return getLogPrefix() + string;
	}

	public String toQuickDescriptiveString() {
		return this.queryThreadIndex + MyUtils.TAB_SEPARATOR + this.rows + MyUtils.TAB_SEPARATOR + this.batchSize + MyUtils.TAB_SEPARATOR + 
		this.first + MyUtils.TAB_SEPARATOR + this.last + 
		MyUtils.TAB_SEPARATOR + this.queryTemplate;
	}
	
	public Average getFetchingBatchAverageTime() {
		return fetchingBatchAverageTime;
	}

	public Average getFetchingQueryAverageTime() {
		return fetchingQueryAverageTime;
	}

	public Average getIntermediaryResultsProcessingAverageTime() {
		return intermediaryResultsProcessingAverageTime;
	}

	public Average getLaunchQueryAverageTime() {
		return launchQueryAverageTime;
	}

	public Boolean getFirst() {
		return first;
	}

	public Boolean getLast() {
		return last;
	}

	public Integer getQueryThreadIndex() {
		return queryThreadIndex;
	}

	public DatabaseParameter getDatabaseParameter() {
		return databaseParameter;
	}

	public List<String> getLeftJoinFields() {
		return leftJoinFields;
	}

	public List<String> getResultFields() {
		return resultFields;
	}

	public List<String> getRightJoinFields() {
		return rightJoinFields;
	}

	public String getTableName() {
		return tableName;
	}
	
	public List<String> getHeaders() {
		return headers;
	}

	public Boolean getDistinct() {
		return distinct;
	}

	public String getQueryTemplate() {
		return queryTemplate;
	}

	public Integer getTotalFields() {
		return totalFields;
	}

	public boolean isLinkIndex() {
		return isLinkIndex;
	}

	public boolean isSuperIndex() {
		return isSuperIndex;
	}

	public void setDatabaseParameter(DatabaseParameter databaseParameter) {
		this.databaseParameter = databaseParameter;
	}

	public void setLeftJoinFields(List<String> leftJoinFields) {
		this.leftJoinFields = leftJoinFields;
	}

	public void setResultFields(List<String> resultFields) {
		this.resultFields = resultFields;
	}

	public void setRightJoinFields(List<String> rightJoinFields) {
		this.rightJoinFields = rightJoinFields;
	}

	public void setTableName(String tableName) {
		this.tableName = tableName;
	}

	public synchronized QueryRunnerIntermediaryResult getCurrentResult() {
		return currentResult;
	}
	public void resetCurrentResult() {
		this.currentResult = null;
	}
	
	public WaitableBoolean getDone() {
		return this.done;
	}

	public WaitableBoolean getFirstFinalResults() {
		return firstFinalResults;
	}

	public List<Status> getReadyList() {
		return readyList;
	}

	public List<Status> getChangedStatusList() {
		return statusList;
	}

	public WaitableBoolean getCancelThread() {
		return cancelThread;
	}

	public WaitableBoolean getSomeFinalResults() {
		return someFinalResults;
	}

	public PreparedStatement getPreparedStatement() {
		return preparedStatement;
	}

	public ResultSet getResultSet() {
		return resultSet;
	}
	
	public Rdbs getRdbs() {
		return (databaseParameter==null ? null : databaseParameter.getRdbs());
	}

	public WaitableBoolean getActiveQuery() {
		return this.activeQuery;	// requires to synchronize on it first? I don't think so but I'm not sure...
	}
}











/*
	[8-99-null-null]	executing query
		general.exceptions.TechnicalException: com.mysql.jdbc.PacketTooBigException: Packet for query is too large (88629796 > 16777216). You can change this value on the server by setting the max_allowed_packet' variable.
			at queryRunner.prototype.QueryThread.process(QueryThread.java:382)
			at queryRunner.prototype.QueryThread.run(QueryThread.java:161)
		Caused by: com.mysql.jdbc.PacketTooBigException: Packet for query is too large (88629796 > 16777216). You can change this value on the server by setting the max_allowed_packet' variable.
			at com.mysql.jdbc.MysqlIO.send(MysqlIO.java:3202)
			at com.mysql.jdbc.MysqlIO.sendCommand(MysqlIO.java:1932)
			at com.mysql.jdbc.MysqlIO.sqlQueryDirect(MysqlIO.java:2101)
			at com.mysql.jdbc.ConnectionImpl.execSQL(ConnectionImpl.java:2548)
			at com.mysql.jdbc.ConnectionImpl.execSQL(ConnectionImpl.java:2477)
			at com.mysql.jdbc.StatementImpl.executeQuery(StatementImpl.java:1422)
			at queryRunner.prototype.QueryThread.process(QueryThread.java:304)
			... 1 more
*/

/*List<Integer> previousJoinFieldIndexesWithinResults = previousThreadResults.getPreviousJoinFieldIndexesWithinResults();

int batchSize2 = 100;
int rowNumber = 0;
while (rowNumber < previousThreadResults.getTotalRows()) {
	
	QueryRunnerIntermediaryResult batchResult = new QueryRunnerIntermediaryResult(previousThreadResults, 
			this.joinFieldIndexesWithinResults);	// Tell next thread what field to use for the join

	
	int totalResultRows = 0;

	int batch2RowNumber = 0;
	while (batch2RowNumber<batchSize2 && rowNumber < previousThreadResults.getTotalRows()) {
		
		List<String> previousRow = previousThreadResults.getValueRow(rowNumber);
		if (QueryRunnerPrototypeConstants.DEBUG) MyUtils.checkStatusProgram(previousRow.size()>max);

		List<String> previousJoinFieldValues = new ArrayList<String>();
		for (int index : previousJoinFieldIndexesWithinResults) {
			previousJoinFieldValues.add(previousRow.get(index));
		}
		
		for (List<String> list : currentBatchResults) {
			List<String> leftJoinFieldValues = new ArrayList<String>();
			for (int index = 0; index < this.leftJoinFieldFullNames.size(); index++) {	// Since these are the first ones
				leftJoinFieldValues.add(list.get(index));
			}
			if (stringListEquals(previousJoinFieldValues, leftJoinFieldValues)) {	// null aren't handled because they aren't really 'null', there only just a string "null"
																					// same size for the list is a input requirement (as well as order)
				List<String> row = new ArrayList<String>(previousRow);
				row.addAll(list);
				batchResult.addValueRow(row);
				totalResultRows++;
			}
		}	
						
		batch2RowNumber++;
		rowNumber++;
	}
}*/