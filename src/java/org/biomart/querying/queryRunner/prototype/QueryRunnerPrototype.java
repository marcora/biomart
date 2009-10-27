package org.biomart.querying.queryRunner.prototype;


import java.io.File;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.StringTokenizer;

import org.biomart.common.general.exceptions.FunctionalException;
import org.biomart.common.general.exceptions.TechnicalException;
import org.biomart.common.general.utils.MyUtils;
import org.biomart.common.general.utils.Timer;
import org.biomart.common.general.utils.WaitableBoolean;
import org.biomart.objects.helpers.DatabaseParameter;
import org.biomart.objects.helpers.Rdbs;


public class QueryRunnerPrototype {

	public static void main(String[] args) {	/// home/anthony/Desktop/QueryRunner/ /home/anthony/Desktop/config1
		try {			
			System.out.println("Main thread started.");
			
			String outputFolderPathAndName = args[0];
			String inputFilePathAndName = args[1];
			if (null==outputFolderPathAndName || null==inputFilePathAndName) {
				throw new FunctionalException("Invalid program parameters");
				
			}
			
			ThreadCommunication.initialize();
			QueryRunnerPrototype queryRunnerPrototype = new QueryRunnerPrototype(outputFolderPathAndName);
			queryRunnerPrototype.retrieveParameters(inputFilePathAndName);

			if (queryRunnerPrototype.getCreateSuperIndex()) {
				//queryRunnerPrototype.linkIndexCreation();	// TODO not supported anymore for now (will have to be)
			} else {
				/*if (queryRunnerPrototype.useLinkIndexes) {
					//queryRunnerPrototype.includeLinkIndexes();	// TODO not supported anymore for now, will have to??
				} else */if (queryRunnerPrototype.useSuperIndex) {
					queryRunnerPrototype.includeSuperIndex();
				} else {
					queryRunnerPrototype.noLinkIndex();
				}
				queryRunnerPrototype.runProgram(true);
			}
			System.out.println("Main thread ended.");
		} catch (TechnicalException e) {
			e.printStackTrace();
			System.exit(-1);
		} catch (FunctionalException e) {
			e.printStackTrace();
			System.exit(-1);
		}
	}

	private Boolean createSuperIndex = null;
	
	private String outputFolderPathAndName = null;
	private Integer rows = null;
	private Integer batchSize = null;
	private String outputFileName = null;
	
	/*private Boolean useLinkIndexes = null;
	private DatabaseParameter linkIndexesDatabaseParameter = null;
	private List<String> linkIndexesList = null;
	private List<List<String>> linkIndexesFieldsList = null;*/
	
	private Boolean useSuperIndex = null;
	private QueryThread superIndexParameter = null;
	
	private Integer totalThreads = null;
	private List<QueryThread> parameters = null;
	private List<QueryThread> queryThreads = null;
	
	public QueryRunnerPrototype(String outputFolderPathAndName) throws TechnicalException {
		this.outputFolderPathAndName = outputFolderPathAndName!=null && !outputFolderPathAndName.endsWith(MyUtils.FILE_SEPARATOR) ?
				outputFolderPathAndName + MyUtils.FILE_SEPARATOR : outputFolderPathAndName;
		File folder = new File(this.outputFolderPathAndName);
		if (!folder.exists()) {
			folder.mkdirs();
		}
		outln("this.outputFolderPathAndName = " + this.outputFolderPathAndName);
		
		this.parameters = new ArrayList<QueryThread>();
		this.queryThreads = new ArrayList<QueryThread>();
	}
	
	public void retrieveParameters(String inputFilePathAndName) throws FunctionalException, TechnicalException {
		
		String fileContent = MyUtils.readFile(inputFilePathAndName);
		StringTokenizer stringTokenizer = new StringTokenizer(fileContent, MyUtils.LINE_SEPARATOR);
		
		String rowsString = stringTokenizer.nextToken();
		if (rowsString==null) { 	// "null" is acceptable though
			throw new FunctionalException("Invalid parameters: invalid batch size specified");
		}
		this.rows = rowsString.equals("null") || rowsString.equals("-1") ? null : getIntegerFromString(rowsString);
		
		String batchSizeString = stringTokenizer.nextToken();
		this.batchSize = getIntegerFromString(batchSizeString);
		if (this.batchSize==null) {
			throw new FunctionalException("Invalid parameters: invalid batch size specified");
		}
		
		this.outputFileName = stringTokenizer.nextToken();
		if (this.outputFileName==null) {
			throw new FunctionalException("Invalid parameters: no output file name specified");
		}
		
		String superIndexParameters = stringTokenizer.nextToken();
		String[] superIndexParametersSplit = superIndexParameters.split(MyUtils.TAB_SEPARATOR);
		this.useSuperIndex = Boolean.valueOf(superIndexParametersSplit[0]);
		this.createSuperIndex = this.useSuperIndex && Boolean.valueOf(superIndexParametersSplit[1]);
		if (this.useSuperIndex) {
			DatabaseParameter superIndexDatabaseParameter = extractDatabaseParameter(superIndexParametersSplit[2]);
			
			String [] superIndexNameSplit = superIndexParametersSplit[3].split(":");
			String superIndexName = superIndexNameSplit[0];
			List<String> superIndexFields = MyUtils.isEmpty(superIndexNameSplit[1]) ? 
					new ArrayList<String>() : new ArrayList<String>(Arrays.asList(superIndexNameSplit[1].split(",")));
					
			this.superIndexParameter = new QueryThread();
			this.superIndexParameter.setDatabaseParameter(superIndexDatabaseParameter);
			this.superIndexParameter.setTableName(superIndexName);
			this.superIndexParameter.setLeftJoinFields(new ArrayList<String>());
			this.superIndexParameter.setRightJoinFields(superIndexFields);
			this.superIndexParameter.setResultFields(new ArrayList<String>());
		}
		outln("this.useSuperIndex = " + this.useSuperIndex + (!this.useSuperIndex ? "" : 
				", this.superIndexDatabaseParameter = " + this.superIndexParameter.getDatabaseParameter() +
				", this.superIndex = " + this.superIndexParameter.getTableName() +
				", this.superIndexFields = " + this.superIndexParameter.getRightJoinFields()));
		
		/*String linkIndexesParameters = stringTokenizer.nextToken();
		String[] linkIndexesParametersSplit = linkIndexesParameters.split(MyUtils.TAB_SEPARATOR);
		this.useLinkIndexes = Boolean.valueOf(linkIndexesParametersSplit[0]);
		if (this.useLinkIndexes) {
			this.linkIndexesDatabaseParameter = extractDatabaseParameter(linkIndexesParametersSplit[1]);
			
			String linkIndexesListString = linkIndexesParametersSplit[2];
			String[] linkIndexesSplit = linkIndexesListString.split(";");
			this.linkIndexesList = new ArrayList<String>();
			this.linkIndexesFieldsList = new ArrayList<List<String>>();
			for (int i = 0; i < linkIndexesSplit.length; i++) {
				String [] linkIndexesNameSplit = linkIndexesSplit[i].split(":");
				this.linkIndexesList.add(linkIndexesNameSplit[0]);
				List<String> linkIndexFieldList = MyUtils.isEmpty(linkIndexesNameSplit[1]) ? 
						new ArrayList<String>() : new ArrayList<String>(Arrays.asList(linkIndexesNameSplit[1].split(",")));
				this.linkIndexesFieldsList.add(linkIndexFieldList);
			}			
		}
		outln("this.useLinkIndexes = " + this.useLinkIndexes + 
				", this.linkIndexesDatabaseParameter = " + this.linkIndexesDatabaseParameter +
				", this.linkIndexesList = " + this.linkIndexesList +
				", this.linkIndexesFieldsList = " + this.linkIndexesFieldsList);*/
				
		if (stringTokenizer.countTokens()<=0) {
			throw new FunctionalException("Invalid parameters: " + stringTokenizer.countTokens() + " lines.");
		}
		/*if (this.useLinkIndexes && (stringTokenizer.countTokens()==linkIndexesList.size()-1)) {
			throw new FunctionalException("Invalid parameters: invalid combination of link indexes");
		}*/
		int totalThreads = stringTokenizer.countTokens();
		
		outln("this.batchSize = " + this.batchSize + ", this.outputFileName = " + this.outputFileName);
		
		Integer previousTotalJoinFields = null; 
		int threadNumber = 0;
		while (stringTokenizer.hasMoreTokens()) {
			String queryLine = stringTokenizer.nextToken();	// format is like: localhost:3306:root:root	ac_query_runner_0_1	table0	l0	r0	pk0
			String[] allParameterStringSplit = MyUtils.split(queryLine, '\t');
			if (allParameterStringSplit.length!=QueryRunnerPrototypeConstants.TOTAL_LINE_ITEMS) {
				throw new FunctionalException("Invalid parameters: " + allParameterStringSplit.length + 
						" items instead of " + QueryRunnerPrototypeConstants.TOTAL_LINE_ITEMS + " expected for the line " + threadNumber);
			}
			
			DatabaseParameter databaseParameter = extractDatabaseParameter(allParameterStringSplit[0]);
			
			String tableName = allParameterStringSplit[1];
			
			String leftJoinFieldListString = allParameterStringSplit[2];
			List<String> leftJoinFields = MyUtils.isEmpty(leftJoinFieldListString) ? 
					new ArrayList<String>() : new ArrayList<String>(Arrays.asList(leftJoinFieldListString.split(",")));
					
			String rightJoinFieldListString = allParameterStringSplit[3];
			List<String> rightJoinFields = MyUtils.isEmpty(rightJoinFieldListString) ? 
					new ArrayList<String>() : new ArrayList<String>(Arrays.asList(rightJoinFieldListString.split(",")));
				
			String resultFieldListString = allParameterStringSplit[4];
			List<String> resultFields = MyUtils.isEmpty(resultFieldListString) ? 
					new ArrayList<String>() : new ArrayList<String>(Arrays.asList(resultFieldListString.split(",")));
			if (tableName==null || leftJoinFields==null || rightJoinFields==null || resultFields==null) {
				throw new FunctionalException("Invalid parameters: null value");
			}
			if (MyUtils.isEmpty(tableName) || (threadNumber!=0 && leftJoinFields.isEmpty()) || 
					(threadNumber<totalThreads-1 && rightJoinFields.isEmpty())) {
				throw new FunctionalException("Invalid parameters: empty value");
			}
			if (threadNumber>0 && previousTotalJoinFields!=leftJoinFields.size()) {
				throw new FunctionalException("Invalid parameters: join on a different number of fields. " +
						"line " + (threadNumber-1) + "(" + previousTotalJoinFields + ")" + 
						" and line " + threadNumber + "(" + leftJoinFields.size() + ")");
			}
			previousTotalJoinFields = rightJoinFields.size();
			
			// Instanciate new thread
			QueryThread queryThread = new QueryThread();
			queryThread.setDatabaseParameter(databaseParameter);
			queryThread.setTableName(tableName);
			queryThread.setLeftJoinFields(leftJoinFields);
			queryThread.setRightJoinFields(rightJoinFields);
			queryThread.setResultFields(resultFields);
			this.parameters.add(queryThread);
			
			threadNumber++;
		}

		this.totalThreads = !this.useSuperIndex ? totalThreads : totalThreads+1;
		outln("this.totalThreads = " + this.totalThreads);
	}

	private DatabaseParameter extractDatabaseParameter(String databaseParameterString) throws FunctionalException {
		
		String[] databaseParameterStringSlit = databaseParameterString.split(":");
		if (databaseParameterStringSlit.length!=QueryRunnerPrototypeConstants.TOTAL_DB_PARAMS_ITEMS) {
			throw new FunctionalException("Invalid parameters: " + databaseParameterStringSlit.length + 
					" items instead of " + QueryRunnerPrototypeConstants.TOTAL_DB_PARAMS_ITEMS + " expected for the db params.");
		}
		
		String rdbsString = databaseParameterStringSlit[0];
		Rdbs rdbs = Rdbs.fromString(rdbsString);
		String host = databaseParameterStringSlit[1];
		String portString = databaseParameterStringSlit[2];
		Integer port = Integer.valueOf(portString);
		String userName = databaseParameterStringSlit[3];
		String password = databaseParameterStringSlit[4];
		String databaseName = databaseParameterStringSlit[5];
		if (host==null || port==null || userName==null || password==null || databaseName==null) {
			throw new FunctionalException("Invalid parameters: null value");
		}
		if (MyUtils.isEmpty(host) || port<0 || MyUtils.isEmpty(databaseName)) {
			throw new FunctionalException("Invalid parameters: empty value");
		}
		DatabaseParameter databaseParameter = new DatabaseParameter(rdbs, host, port, userName, password, databaseName);
		return databaseParameter;
	}

	/*private void linkIndexCreation() throws TechnicalException, FunctionalException {
		
		outln();
		outln(MyUtils.DASH_LINE);
		for (int i = 0; i < this.totalThreads-1; i++) {	// -1 only n-1 link indexes
			outln("Creating link index " + i + " / " + (i+1));
			ThreadCommunication.initialize();
			QueryRunnerPrototype queryRunnerPrototype = new QueryRunnerPrototype(this.outputFolderPathAndName);
			queryRunnerPrototype.initializeLinkIndexCreationThreads(
					QueryRunnerPrototypeConstants.LINK_INDEX_CREATION_BATCH_SIZE, "linkIndex_" + i + "_" + (i+1), 2,
					this.queryThreads.get(i), this.queryThreads.get(i+1));
			queryRunnerPrototype.runProgram(false);
			outln();
		}
	}
	
	private void initializeLinkIndexCreationThreads(int batchSize, String outputFileName, int totalThreads, 
			QueryThread leftQueryThread, QueryThread rightQueryThread ) throws TechnicalException {
		this.batchSize = batchSize;
		this.outputFileName = outputFileName;
		this.totalThreads =  totalThreads;
		QueryThread newLeftQueryThread = new QueryThread(totalThreads, 0, 
						batchSize, true, false, false, leftQueryThread.getDatabaseParameter(), leftQueryThread.getTableName(), 
						new ArrayList<String>(), leftQueryThread.getRightJoinFields(), new ArrayList<String>());
		this.queryThreads.add(newLeftQueryThread);
		QueryThread newRightQueryThread = new QueryThread(totalThreads, 1, 
						batchSize, true, false, false, rightQueryThread.getDatabaseParameter(), rightQueryThread.getTableName(), 
						rightQueryThread.getLeftJoinFields(), new ArrayList<String>(), new ArrayList<String>());
		this.queryThreads.add(newRightQueryThread);
	}*/
	
	/*private void includeLinkIndexes() throws TechnicalException {
		
		int newTotalThreads = this.totalThreads*2-1;
		
		List<QueryThread> queryThreads = new ArrayList<QueryThread>();
		int threadNumber = 0;
		int linkIndexNumber = 0;
		for (int i = 0; i < this.totalThreads; i++) {
			
			QueryThread originalQueryThread = this.queryThreads.get(i);
			
			if (i==0) {
				queryThreads.add(createLinkIndexQueryThread(newTotalThreads, threadNumber, linkIndexNumber));
				linkIndexNumber++;
				threadNumber++;
			}
				
			originalQueryThread.set(newTotalThreads, threadNumber);
			queryThreads.add(originalQueryThread);
			threadNumber++;
			
			if (i!=0 && i<this.totalThreads-1) {
				queryThreads.add(createLinkIndexQueryThread(newTotalThreads, threadNumber, linkIndexNumber));
				linkIndexNumber++;
				threadNumber++;
			}
		}

MyUtils.checkStatusProgram(threadNumber==newTotalThreads);
		this.totalThreads = threadNumber;	// account for link indexes
		this.queryThreads = queryThreads;
		outln("new this.totalThreads = " + this.totalThreads);
	}*/

	/*private QueryThread createLinkIndexQueryThread(int newTotalThreads, int threadNumber, int linkIndexNumber) throws TechnicalException {
		QueryThread linkIndexQueryThread = new QueryThread(newTotalThreads, threadNumber, 
								QueryRunnerPrototypeConstants.INDEXES_USE_BATCH_SIZE, true, true, false,
								this.linkIndexesDatabaseParameter, this.linkIndexesList.get(linkIndexNumber), 
								(threadNumber==0 ? new ArrayList<String>() : this.linkIndexesFieldsList.get(linkIndexNumber)), 
								this.linkIndexesFieldsList.get(linkIndexNumber), new ArrayList<String>());
		return linkIndexQueryThread;
	}*/
	
	private void includeSuperIndex() throws TechnicalException {
		
		QueryThread superIndexQueryThread = new QueryThread(this.totalThreads, 0, 
				this.rows, QueryRunnerPrototypeConstants.INDEXES_USE_BATCH_SIZE, true, false, true, this.superIndexParameter);
		this.queryThreads.add(superIndexQueryThread);

check(this.totalThreads==this.parameters.size()+1);
		for (int threadNumber = 0; threadNumber < this.parameters.size(); threadNumber++) {
			QueryThread parameterQueryThread = this.parameters.get(threadNumber);
			if (threadNumber==0) {
				parameterQueryThread.setLeftJoinFields(new ArrayList<String>(parameterQueryThread.getRightJoinFields()));
			}
			QueryThread queryThread = new QueryThread(this.totalThreads, threadNumber+1, 
					this.rows, this.batchSize, false, false, false, parameterQueryThread);
			this.queryThreads.add(queryThread);
		}
		setSurroundingThreads();
	}

	private void noLinkIndex() throws TechnicalException {
check(this.totalThreads==this.parameters.size());		
		for (int threadNumber = 0; threadNumber < this.parameters.size(); threadNumber++) {
			QueryThread queryThread = new QueryThread(this.totalThreads, threadNumber, 
					this.rows, this.batchSize, false, false, false, this.parameters.get(threadNumber));
			this.queryThreads.add(queryThread);
		}
		setSurroundingThreads();
	}
	
	private void setSurroundingThreads() {
		for (int threadNumber = 0; threadNumber < this.queryThreads.size(); threadNumber++) {
			this.queryThreads.get(threadNumber).setSurroundingThreads(
					threadNumber==0 ? null : this.queryThreads.get(threadNumber-1), 
					threadNumber==this.queryThreads.size()-1 ? null : this.queryThreads.get(threadNumber+1));
		}
	}

	public void runProgram(boolean addHeaders) throws TechnicalException, FunctionalException {
		
		// Initialize output file
		ThreadCommunication.fileWriter = ThreadCommunication.prepareFileWriter(createFileName(null));
		ThreadCommunication.bufferedWriter = ThreadCommunication.prepareBufferedWriter(
				ThreadCommunication.fileWriter);		
		
		// Prepare output files (must be after totalThreads has been determined)
		if (QueryRunnerPrototypeConstants.LOG) {
			ThreadCommunication.prepareQueryFiles(this.outputFolderPathAndName, this.totalThreads);
		}

		// Initialization
		for (int threadIndex = 0; threadIndex < this.queryThreads.size(); threadIndex++) {
			QueryThread queryThread = this.queryThreads.get(threadIndex);
			outln(MyUtils.TAB_SEPARATOR + queryThread.toQuickDescriptiveString());
		}
		
		// Prepare timers
		Timer timerFirstResults = new Timer();
		Timer timerResults = new Timer();
		
boolean justLaunch = false;		
		// Launch threads
		for (int threadIndex = 0; threadIndex < this.queryThreads.size(); threadIndex++) {
			QueryThread queryThread = this.queryThreads.get(threadIndex);
			
			int totalGlobalFields = ThreadCommunication.getTotalFields();
			queryThread.determineJoinFieldIndexesWithinResults(totalGlobalFields);
			
			if (queryThread.isLinkIndex() || queryThread.isSuperIndex()) {
				for (int i = 0; i < queryThread.getTotalFields(); i++) {
					ThreadCommunication.addToIgnoreList(totalGlobalFields + i);
				}
			} else {
				if (addHeaders) {
					List<String> headers = queryThread.getHeaders();
					ThreadCommunication.addHeaderToResultFile(headers);
				}
				ThreadCommunication.addGlobalResultFields(queryThread.getTotalFields());
			}
			ThreadCommunication.addGlobalFields(queryThread.getTotalFields());				
			
			if (threadIndex==0) {
				timerFirstResults.startTimer();
				timerResults.startTimer();
				outln("START TIME: " + timerFirstResults.getStart() + MyUtils.LINE_SEPARATOR + MyUtils.LINE_SEPARATOR +
						MyUtils.EQUAL_LINE + MyUtils.LINE_SEPARATOR + MyUtils.LINE_SEPARATOR);
			}
			
if (!justLaunch)
			queryThread.start();
		}
if(justLaunch) System.exit(0);		
		
		// Wait for 1st final results
		outln("waiting for first results to arrive");
		QueryThread lastQueryThread = this.queryThreads.get(this.queryThreads.size()-1);
		WaitableBoolean firstFinalResults = lastQueryThread.getFirstFinalResults();
		synchronized(firstFinalResults) {
			while(!firstFinalResults.getValue()) {
				try {
					firstFinalResults.wait();
				} catch (InterruptedException e) {
					throw new TechnicalException(e);
				}
			}
			if (!firstFinalResults.getValue()) {
MyUtils.errorProgram("SPONTANEOUS NOTIFICATION!!!!!!!!");	// shouldn't happen really							
			}
		}
		
		timerFirstResults.stopTimer();
		String messageFirstResults = "timer 1st results: " + timerFirstResults.getTimeEllapsedMs() + " ms";
		if (QueryRunnerPrototypeConstants.LOG) {
			outln("STOP TIME (1st results): " + timerFirstResults.getStop());
			outln(messageFirstResults);
			outln();
		}
		MyUtils.writeFile(createFileName("firsts"), messageFirstResults);

		if (this.rows!=null) {	// means query may be cancelled before all results are here
			
			// Wait for some results (number specified in parameters)
			outln("waiting for some results to arrive");		
			WaitableBoolean someFinalResults = lastQueryThread.getSomeFinalResults();
			synchronized(someFinalResults) {
				while(!someFinalResults.getValue()) {
					try {
						someFinalResults.wait();
					} catch (InterruptedException e) {
						throw new TechnicalException(e);
					}
				}
				if (!someFinalResults.getValue()) {
MyUtils.errorProgram("SPONTANEOUS NOTIFICATION!!!!!!!!");	// shouldn't happen really							
				}
			}

			try {
				Thread.currentThread().setPriority(Thread.MAX_PRIORITY);
				for (int threadIndex = 0; threadIndex < this.queryThreads.size(); threadIndex++) {
					QueryThread queryThread = this.queryThreads.get(threadIndex);
					if (null!=queryThread) {	// TODO check thread not finished yet (need a lock)
						queryThread.setPriority(Thread.MIN_PRIORITY);
					}
				}
			} catch (IllegalArgumentException e) {
				e.printStackTrace();
MyUtils.errorProgram("JVM has different priorities");
			}
			if (QueryRunnerPrototypeConstants.LOG) {
				outln("some results have arrived, cancelling everything");
			}
			
			// Cancelling the threads
			for (int threadIndex = 0; threadIndex < this.queryThreads.size(); threadIndex++) {
				QueryThread queryThread = this.queryThreads.get(threadIndex);
				
				outln("cancelling thread " + threadIndex);
				WaitableBoolean cancelThread = queryThread.getCancelThread();
				synchronized (cancelThread) {
					cancelThread.setTrue();
				}
			}
			
			// Cancelling the queries
			for (int threadIndex = 0; threadIndex < this.queryThreads.size(); threadIndex++) {
				QueryThread queryThread = this.queryThreads.get(threadIndex);
				
				outln("cancelling query " + threadIndex);
				cancelQuery(queryThread);
			}

			if (QueryRunnerPrototypeConstants.LOG) {
				outln("cancelling requests sent");
			}
		} else {
			outln("waiting for all results to arrive");
		}
		
		// Wait for all threads to be finished
		outln("waiting for all threads to be finished");
		for (int threadIndex = 0; threadIndex < this.queryThreads.size(); threadIndex++) {
			try {
				this.queryThreads.get(threadIndex).join();
			} catch (InterruptedException e) {
				throw new TechnicalException(e);
			}
		}
check(ThreadCommunication.checkAllDead(this.queryThreads));
		
		timerResults.stopTimer();

		// Display results
		outln();
		outln("STOP TIME (1st results): " + timerFirstResults.getStop());
		outln("STOP TIME (" + (this.rows==null ? "all" : "some") + " results): " + timerResults.getStop());
		outln();

		outln("totalRows = " + ThreadCommunication.getTotalRows());
		outln();
		
		String messageAllResults = "timer all results: " + timerResults.getTimeEllapsedMs() + " ms";
		outln(messageFirstResults);
		outln(messageAllResults);
		
		// Write statistics
		writeStatistics(messageFirstResults, messageAllResults);
		
		ThreadCommunication.closeFile(ThreadCommunication.fileWriter, ThreadCommunication.bufferedWriter);
		if (QueryRunnerPrototypeConstants.LOG) {
			ThreadCommunication.closeQueryFiles(this.totalThreads);
		}
	}

	private void cancelQuery(QueryThread queryThread) throws TechnicalException {
		try {
			int threadIndex = queryThread.getQueryThreadIndex();
			Rdbs rdbs = queryThread.getRdbs();
			PreparedStatement preparedStatement = queryThread.getPreparedStatement();
			ResultSet resultSet = queryThread.getResultSet();
			
			WaitableBoolean activeQuery = queryThread.getActiveQuery();
			synchronized (activeQuery) {	// always get the lock on that object to handle the query related-object: 2 threads can use them (main one and current one)
				lockln("lock acquired on activeQuery");
				if (activeQuery.getValue()) {
check(preparedStatement!=null);		
					if (rdbs.isMySql()) {
			        	preparedStatement.cancel();
			        	outln("Cancelled statement for thread " + threadIndex + " (" + rdbs + ")");
//System.out.println("trying to close the preparedStatement...");
			        	preparedStatement.close();
			        	outln("Closed statement for thread " + threadIndex + " (" + rdbs + ")");	
//System.out.println("trying to close the resultSet...");
			            if (resultSet!=null) {
							resultSet.close();
							outln("Closed resultSet for thread " + threadIndex + " (" + rdbs + ")");
			            }
						// Need to get a new connection for MySql
						/* connection.close();
						connection = DriverManager.getConnection(connectionString, USERNAME, PASSWORD);*/
					} else if (rdbs.isPostgreSql()) {
				        	preparedStatement.cancel();
				        	outln("Cancelled statement for thread " + threadIndex + " (" + rdbs + ")");
				            preparedStatement.close();
				            outln("Closed statement for thread " + threadIndex + " (" + rdbs + ")");
				            if (resultSet!=null) {
								resultSet.close();
								outln("Closed resultSet for thread " + threadIndex + " (" + rdbs + ")");
				            }
			    	} else if (rdbs.isOracle()) {
			    		if (resultSet!=null) {
							resultSet.close();
				        	outln("Closed resultSet for thread " + threadIndex + " (" + rdbs + ")");
			    		}
			            preparedStatement.close();
			            outln("Closed statement for thread " + threadIndex + " (" + rdbs + ")");
				        // Statement must not be cancelled with Oracle, otherwise get error: ORA-01013: user requested cancel of current operation
			    	}
					activeQuery.setFalse();
				} else {
					outln("Query is not active anymore for thread " + threadIndex + " (" + rdbs + ")");
				}
		    }
		} catch (SQLException e) {
			throw new TechnicalException(e);
		}
	}

	private void writeStatistics(String messageFirstResults, String messageAllResults) {
			
			StringBuffer stringBuffer = new StringBuffer();
			for (QueryThread queryThread : this.queryThreads) {
				stringBuffer.append("Thread " + queryThread.getQueryThreadIndex() + MyUtils.LINE_SEPARATOR);
				stringBuffer.append("launchQueryAverageTime = " + queryThread.getLaunchQueryAverageTime() + MyUtils.LINE_SEPARATOR);
				stringBuffer.append("fetchingQueryAverageTime = " + queryThread.getFetchingQueryAverageTime() + MyUtils.LINE_SEPARATOR);
				stringBuffer.append("fetchingBatchAverageTime = " + queryThread.getFetchingBatchAverageTime() + MyUtils.LINE_SEPARATOR);
				if (!queryThread.getFirst() && !queryThread.getLast()) {
					stringBuffer.append("intermediaryResultsProcessingAverageTime = " + 
							queryThread.getIntermediaryResultsProcessingAverageTime() + MyUtils.LINE_SEPARATOR);
				}		
				stringBuffer.append(MyUtils.LINE_SEPARATOR);
			}

			stringBuffer.append(MyUtils.LINE_SEPARATOR);
			for (QueryThread queryThread : this.queryThreads) {
				stringBuffer.append(queryThread.getLaunchQueryAverageTime().average + MyUtils.TAB_SEPARATOR);
			}
			stringBuffer.append(MyUtils.TAB_SEPARATOR);
			for (QueryThread queryThread : this.queryThreads) {
				stringBuffer.append(queryThread.getFetchingQueryAverageTime().average + MyUtils.TAB_SEPARATOR);
			}
			stringBuffer.append(MyUtils.TAB_SEPARATOR);
			for (QueryThread queryThread : this.queryThreads) {
				stringBuffer.append(queryThread.getFetchingBatchAverageTime().average + MyUtils.TAB_SEPARATOR);
			}
			stringBuffer.append(MyUtils.TAB_SEPARATOR);
			for (QueryThread queryThread : this.queryThreads) {
				stringBuffer.append((!queryThread.getFirst() && !queryThread.getLast() ? 
						queryThread.getIntermediaryResultsProcessingAverageTime().average : "n/a") + MyUtils.TAB_SEPARATOR);
			}
			stringBuffer.append(MyUtils.LINE_SEPARATOR);
			
			stringBuffer.append(MyUtils.LINE_SEPARATOR);
			stringBuffer.append("totalRows = " + ThreadCommunication.getTotalRows() + MyUtils.LINE_SEPARATOR);
			stringBuffer.append(MyUtils.LINE_SEPARATOR);
			stringBuffer.append(messageFirstResults + MyUtils.LINE_SEPARATOR);
			stringBuffer.append(messageAllResults + MyUtils.LINE_SEPARATOR);
			
			MyUtils.writeFile(createFileName("stats"), stringBuffer.toString());
		}
		
		private String createFileName(String suffix) {
			return this.outputFolderPathAndName + MyUtils.FILE_SEPARATOR + this.outputFileName + 
			((null==suffix || MyUtils.isEmpty(suffix)) ? "" : (MyUtils.INFO_SEPARATOR + suffix));
		}
		
		private Integer getIntegerFromString(String batchSizeString) {
			try {
				return Integer.valueOf(batchSizeString);
			} catch (NumberFormatException e) {
				return null;
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
	
	private void outln() {
		outln(MyUtils.LINE_SEPARATOR);
	}
	private void outln(String string) {
		if (QueryRunnerPrototypeConstants.LOG) {
			System.out.println(getLogPrefix() + string);
		}
	}

	private String getLogPrefix() {
		return "[" + "main" + "-" + ThreadCommunication.getTotalRows() + "]" + MyUtils.TAB_SEPARATOR;
	}
	private void lockln(String string) {
		if (QueryRunnerPrototypeConstants.LOG && QueryRunnerPrototypeConstants.LOG_LOCKS) {
			System.out.println(getLogPrefix() + string);
		}
	}

	public Boolean getCreateSuperIndex() {
		return createSuperIndex;
	}
}


/*
null
1000
ac_query_runner_x
false	false	mysql:localhost:3306:root:root:super_indexes	super_index_50_1_100000_bottom:f0_1
mysql:bm-test.res.oicr.on.ca:3306:martadmin:biomart:ac_query_runner_x	table0	l0,m0	r0,s0	pk0
postgresql:bm-test.res.oicr.on.ca:5432:martadmin:biomart:ac_query_runner_x	table1	l1,m1	r1,s1	pk1
oracle:localhost:1521:martadmin:biomart:xe	ac_query_runner_x__table2	l2,m2	r2,s2	pk2

null
1000
ac_query_runner_x
false	false	mysql:localhost:3306:root:root:super_indexes	super_index_50_1_100000_bottom:f0_1
mysql:bm-test.res.oicr.on.ca:3306:martadmin:biomart:ac_query_runner_x	table0	l0,m0	r0,s0	pk0
mysql:bm-test.res.oicr.on.ca:3306:martadmin:biomart:ac_query_runner_x	table1	l1,m1	r1,s1	pk1
mysql:bm-test.res.oicr.on.ca:3306:martadmin:biomart:ac_query_runner_x	table2	l2,m2	r2,s2	pk2

null
1000
ac_query_runner_x
false	false	mysql:localhost:3306:root:root:super_indexes	super_index_50_1_100000_bottom:f0_1
postgresql:bm-test.res.oicr.on.ca:5432:martadmin:biomart:ac_query_runner_x	table0	l0,m0	r0,s0	pk0
postgresql:bm-test.res.oicr.on.ca:5432:martadmin:biomart:ac_query_runner_x	table1	l1,m1	r1,s1	pk1
postgresql:bm-test.res.oicr.on.ca:5432:martadmin:biomart:ac_query_runner_x	table2	l2,m2	r2,s2	pk2

null
1000
ac_query_runner_x
false	false	mysql:localhost:3306:root:root:super_indexes	super_index_50_1_100000_bottom:f0_1
oracle:localhost:1521:martadmin:biomart:xe	ac_query_runner_x__table0	l0,m0	r0,s0	pk0
oracle:localhost:1521:martadmin:biomart:xe	ac_query_runner_x__table1	l1,m1	r1,s1	pk1
oracle:localhost:1521:martadmin:biomart:xe	ac_query_runner_x__table2	l2,m2	r2,s2	pk2

null
100
ac_query_runner_10_1_100000_bottom
false	false	mysql:localhost:3306:root:root:super_indexes	super_index_10_1_100000_bottom:f0_1
oracle:localhost:1521:martadmin:biomart:xe	ac_qr_10_1_100000_bottom__t0	l0	r0	pk0
oracle:localhost:1521:martadmin:biomart:xe	ac_qr_10_1_100000_bottom__t1	l1	r1	pk1
oracle:localhost:1521:martadmin:biomart:xe	ac_qr_10_1_100000_bottom__t2	l2	r2	pk2
oracle:localhost:1521:martadmin:biomart:xe	ac_qr_10_1_100000_bottom__t3	l3	r3	pk3
oracle:localhost:1521:martadmin:biomart:xe	ac_qr_10_1_100000_bottom__t4	l4	r4	pk4

null
100
ac_query_runner_10_1_100000_bottom
false	false	mysql:localhost:3306:root:root:super_indexes	super_index_10_1_100000_bottom:f0_1
postgresql:bm-test.res.oicr.on.ca:5432:martadmin:biomart:ac_query_runner_10_1_100000_bottom	table0	l0	r0	pk0
postgresql:bm-test.res.oicr.on.ca:5432:martadmin:biomart:ac_query_runner_10_1_100000_bottom	table1	l1	r1	pk1
postgresql:bm-test.res.oicr.on.ca:5432:martadmin:biomart:ac_query_runner_10_1_100000_bottom	table2	l2	r2	pk2
postgresql:bm-test.res.oicr.on.ca:5432:martadmin:biomart:ac_query_runner_10_1_100000_bottom	table3	l3	r3	pk3
postgresql:bm-test.res.oicr.on.ca:5432:martadmin:biomart:ac_query_runner_10_1_100000_bottom	table4	l4	r4	pk4

null
100
ac_query_runner_50_1_100000_bottom
false	false	mysql:localhost:3306:root:root:super_indexes	super_index_50_1_100000_bottom:f0_1
mysql:bm-test.res.oicr.on.ca:3306:martadmin:biomart:ac_query_runner_50_1_100000_bottom	table0	l0	r0	pk0
mysql:bm-test.res.oicr.on.ca:3306:martadmin:biomart:ac_query_runner_50_1_100000_bottom	table1	l1	r1	pk1
mysql:bm-test.res.oicr.on.ca:3306:martadmin:biomart:ac_query_runner_50_1_100000_bottom	table2	l2	r2	pk2
mysql:bm-test.res.oicr.on.ca:3306:martadmin:biomart:ac_query_runner_50_1_100000_bottom	table3	l3	r3	pk3
mysql:bm-test.res.oicr.on.ca:3306:martadmin:biomart:ac_query_runner_50_1_100000_bottom	table4	l4	r4	pk4


null
100
ac_query_runner_10_1_1000000_bottom
true	false	mysql:localhost:3306:root:root:super_indexes	super_index_10_1_1000000_bottom:f0_1
mysql:bm-test.res.oicr.on.ca:3306:martadmin:biomart:ac_query_runner_10_1_1000000_bottom	table0	l0	r0	pk0
mysql:bm-test.res.oicr.on.ca:3306:martadmin:biomart:ac_query_runner_10_1_1000000_bottom	table1	l1	r1	pk1
mysql:bm-test.res.oicr.on.ca:3306:martadmin:biomart:ac_query_runner_10_1_1000000_bottom	table2	l2	r2	pk2
mysql:bm-test.res.oicr.on.ca:3306:martadmin:biomart:ac_query_runner_10_1_1000000_bottom	table3	l3	r3	pk3
mysql:bm-test.res.oicr.on.ca:3306:martadmin:biomart:ac_query_runner_10_1_1000000_bottom	table4	l4	r4	pk4
mysql:bm-test.res.oicr.on.ca:3306:martadmin:biomart:ac_query_runner_10_1_1000000_bottom	table5	l5	r5	pk5
mysql:bm-test.res.oicr.on.ca:3306:martadmin:biomart:ac_query_runner_10_1_1000000_bottom	table6	l6	r6	pk6
mysql:bm-test.res.oicr.on.ca:3306:martadmin:biomart:ac_query_runner_10_1_1000000_bottom	table7	l7	r7	pk7
mysql:bm-test.res.oicr.on.ca:3306:martadmin:biomart:ac_query_runner_10_1_1000000_bottom	table8	l8	r8	pk8
mysql:bm-test.res.oicr.on.ca:3306:martadmin:biomart:ac_query_runner_10_1_1000000_bottom	table9	l9	r9	pk9

null
100
ac_query_runner_10_1_1000000_bottom
true	false	postgresql:bm-test.res.oicr.on.ca:5432:martadmin:biomart:super_indexes	super_index_10_1_1000000_bottom:f0_1
postgresql:bm-test.res.oicr.on.ca:5432:martadmin:biomart:ac_query_runner_10_1_1000000_bottom	table0	l0	r0	pk0
postgresql:bm-test.res.oicr.on.ca:5432:martadmin:biomart:ac_query_runner_10_1_1000000_bottom	table1	l1	r1	pk1
postgresql:bm-test.res.oicr.on.ca:5432:martadmin:biomart:ac_query_runner_10_1_1000000_bottom	table2	l2	r2	pk2
postgresql:bm-test.res.oicr.on.ca:5432:martadmin:biomart:ac_query_runner_10_1_1000000_bottom	table3	l3	r3	pk3
postgresql:bm-test.res.oicr.on.ca:5432:martadmin:biomart:ac_query_runner_10_1_1000000_bottom	table4	l4	r4	pk4
postgresql:bm-test.res.oicr.on.ca:5432:martadmin:biomart:ac_query_runner_10_1_1000000_bottom	table5	l5	r5	pk5
postgresql:bm-test.res.oicr.on.ca:5432:martadmin:biomart:ac_query_runner_10_1_1000000_bottom	table6	l6	r6	pk6
postgresql:bm-test.res.oicr.on.ca:5432:martadmin:biomart:ac_query_runner_10_1_1000000_bottom	table7	l7	r7	pk7
postgresql:bm-test.res.oicr.on.ca:5432:martadmin:biomart:ac_query_runner_10_1_1000000_bottom	table8	l8	r8	pk8
postgresql:bm-test.res.oicr.on.ca:5432:martadmin:biomart:ac_query_runner_10_1_1000000_bottom	table9	l9	r9	pk9

null
100
ac_query_runner_10_1_1000000_bottom
true	false	oracle:localhost:1521:martadmin:biomart:xe	si_10_1_1000000_b:f0_1
oracle:localhost:1521:martadmin:biomart:xe	ac_qr_10_1_1000000_b__t0	l0	r0	pk0
oracle:localhost:1521:martadmin:biomart:xe	ac_qr_10_1_1000000_b__t1	l1	r1	pk1
oracle:localhost:1521:martadmin:biomart:xe	ac_qr_10_1_1000000_b__t2	l2	r2	pk2
oracle:localhost:1521:martadmin:biomart:xe	ac_qr_10_1_1000000_b__t3	l3	r3	pk3
oracle:localhost:1521:martadmin:biomart:xe	ac_qr_10_1_1000000_b__t4	l4	r4	pk4
oracle:localhost:1521:martadmin:biomart:xe	ac_qr_10_1_1000000_b__t5	l5	r5	pk5
oracle:localhost:1521:martadmin:biomart:xe	ac_qr_10_1_1000000_b__t6	l6	r6	pk6
oracle:localhost:1521:martadmin:biomart:xe	ac_qr_10_1_1000000_b__t7	l7	r7	pk7
oracle:localhost:1521:martadmin:biomart:xe	ac_qr_10_1_1000000_b__t8	l8	r8	pk8
oracle:localhost:1521:martadmin:biomart:xe	ac_qr_10_1_1000000_b__t9	l9	r9	pk9

false	mysql:localhost:3306:root:root:link_indexes	link_index_50_1_100000_bottom_0_1:f0_1;link_index_50_1_100000_bottom_1_2:f1_2
false	mysql:localhost:3306:root:root:link_indexes	link_index_50_1_100000_bottom_0_1:f0_1;link_index_50_1_100000_bottom_1_2:f1_2;link_index_50_1_100000_bottom_2_3:f2_3;link_index_50_1_100000_bottom_3_4:f3_4
false	mysql:localhost:3306:root:root:link_indexes	link_index_10_1_1000000_bottom_0_1:f0_1;link_index_10_1_1000000_bottom_1_2:f1_2;link_index_10_1_1000000_bottom_2_3:f2_3;link_index_10_1_1000000_bottom_3_4:f3_4;link_index_10_1_1000000_bottom_4_5:f4_5;link_index_10_1_1000000_bottom_5_6:f5_6;link_index_10_1_1000000_bottom_6_7:f6_7;link_index_10_1_1000000_bottom_7_8:f7_8;link_index_10_1_1000000_bottom_8_9:f8_9
*/






/*public static final String DATABASE_PASSWORD2 = "root";

public static final String[] DATABASE_NAMES = new String[] {
	"ensembl_mart_48", "ensembl_mart_49",
	"ensembl_mart_50", "ensembl_mart_51", "ensembl_mart_52", 
	"ensembl_mart_53", "ensembl_mart_54", "ensembl_mart_55"
};
public static final String JOIN_FIELD = 
	//"transcript_id_1064_key";
	"gene_id_1020_key";
public static final String RESULT_FIELD = 
	//"name_106";
	"description_4014";
public static final String TABLE =
	//"hsapiens_gene_ensembl__transcript__main";
	//"hsapiens_gene_ensembl__exon_transcript__dm";
	null;
public static String[] TABLES = TABLE!=null ? null : new String[] {
	
	"hsapiens_gene_ensembl__gene__main",
	"hsapiens_gene_ensembl__homolog_Mmus__dm",
	"hsapiens_gene_ensembl__homolog_Btau__dm",
	"hsapiens_gene_ensembl__homolog_Ggor__dm",
	
	"hsapiens_gene_ensembl__homolog_Mmus__dm",
	"hsapiens_gene_ensembl__homolog_Btau__dm",
	"hsapiens_gene_ensembl__homolog_Ggor__dm",
	"hsapiens_gene_ensembl__homolog_Mmus__dm",
	"hsapiens_gene_ensembl__homolog_Btau__dm",
	"hsapiens_gene_ensembl__homolog_Ggor__dm"
	
	"hsapiens_gene_ensembl__exp_est_AnatomicalSystem__dm",
	"hsapiens_gene_ensembl__exp_est_AssociatedWith__dm",  
	"hsapiens_gene_ensembl__exp_est_CellType__dm", 
	"hsapiens_gene_ensembl__exp_est_DevelopmentStage__dm",
	"hsapiens_gene_ensembl__exp_est_ExperimentalTechnique__dm",
	"hsapiens_gene_ensembl__exp_est_MicroarrayPlatform__dm",
	"hsapiens_gene_ensembl__exp_est_Pathology__dm",
	"hsapiens_gene_ensembl__exp_est_Pooling__dm",
	"hsapiens_gene_ensembl__exp_est_TissuePreparation__dm",
	"hsapiens_gene_ensembl__exp_est_Treatment__dm"
	
	"hsapiens_gene_ensembl__exp_est_AnatomicalSystem__dm",
	"hsapiens_gene_ensembl__exp_est_AssociatedWith__dm",  
	"hsapiens_gene_ensembl__exp_est_CellType__dm", 
	"hsapiens_gene_ensembl__exp_est_DevelopmentStage__dm",
	"hsapiens_gene_ensembl__exp_est_ExperimentalTechnique__dm",
	"hsapiens_gene_ensembl__exp_est_MicroarrayPlatform__dm",
	"hsapiens_gene_ensembl__exp_est_Pathology__dm",
	"hsapiens_gene_ensembl__exp_est_Pooling__dm",
	"hsapiens_gene_ensembl__exp_est_TissuePreparation__dm",
	"hsapiens_gene_ensembl__exp_est_Treatment__dm",
	"hsapiens_gene_ensembl__exp_gnf_AnatomicalSystem__dm",
	"hsapiens_gene_ensembl__exp_gnf_CellType__dm", 
	"hsapiens_gene_ensembl__exp_gnf_DevelopmentStage__dm",
	"hsapiens_gene_ensembl__exp_gnf_Pathology__dm",
	"hsapiens_gene_ensembl__exp_gnf_Pooling__dm",
			
	"hsapiens_gene_ensembl__transcript__main", 
	"hsapiens_gene_ensembl__exon_transcript__dm", 
	"hsapiens_gene_ensembl__transcript_variation__dm"
};*/
