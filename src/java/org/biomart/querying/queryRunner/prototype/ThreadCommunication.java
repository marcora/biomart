package org.biomart.querying.queryRunner.prototype;


import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.biomart.common.general.exceptions.TechnicalException;
import org.biomart.common.general.utils.MyUtils;

public class ThreadCommunication {
	
	// Output file
	public static FileWriter fileWriter = null;
	public static BufferedWriter bufferedWriter = null;
	
	public static FileWriter[] queryFw = null;
	public static BufferedWriter[] queryBw = null;
	
	//private static Integer dotCount = null;
	private static Integer totalFields = null;
	private static Integer totalResultFields = null;
	private static Integer totalRows = null;	// access it only when all threads are finished (or synchronize it)
	private static List<Integer> ignore = null;
	
	public static void initialize() {
		fileWriter = null;
		bufferedWriter = null;
		queryFw = null;
		queryBw = null;
		//dotCount = 0;
		totalFields = 0;
		totalResultFields = 0;
		totalRows = 0;
		ignore = new ArrayList<Integer>();
	}
	
	// Display
	public static void prepareQueryFiles(String outputFolderPathAndName, int totalThreads) throws TechnicalException {	
		queryFw = new FileWriter[totalThreads];
		queryBw = new BufferedWriter[totalThreads];
		String QUERY_FILES = "Queries";
		String folderPathAndName = outputFolderPathAndName + MyUtils.FILE_SEPARATOR + QUERY_FILES + MyUtils.FILE_SEPARATOR;
		File folder = new File(folderPathAndName);
		if (!folder.exists()) {
			folder.mkdirs();
		}
		for (int i = 0; i < totalThreads; i++) {
			queryFw[i] = prepareFileWriter(folderPathAndName + i + MyUtils.INFO_SEPARATOR + QUERY_FILES);
			queryBw[i] = prepareBufferedWriter(queryFw[i]);
		}
	}
	public static void closeQueryFiles(int totalThreads) throws TechnicalException {	
		for (int i = 0; i < totalThreads; i++) {
			closeFile(queryFw[i], queryBw[i]);
		}
	}
	public static FileWriter prepareFileWriter(String outputFilePathAndName) throws TechnicalException {
		FileWriter fileWriter = null;
		try {
			File file = new File(outputFilePathAndName);
			fileWriter = new FileWriter(file);
		} catch (IOException e) {
			throw new TechnicalException(e);
		}
		return fileWriter;
	}
	public static BufferedWriter prepareBufferedWriter(FileWriter fileWriter) throws TechnicalException {
		return new BufferedWriter(fileWriter);
	}

	public static void closeFile(FileWriter fileWriter, BufferedWriter bufferedWriter) throws TechnicalException {
		try {			
			bufferedWriter.close();
			fileWriter.close();
		} catch (IOException e) {
			throw new TechnicalException(e);
		}
	}
	public static boolean checkAllDead(List<QueryThread> queryThreads) {
		for (QueryThread queryThread : queryThreads) {
			if (queryThread.isAlive()) {
				return false;
			}
		}
		return true;
	}
	public static synchronized int addRowToTotalRows() {
		totalRows++;
		return totalRows;
	}
	public static synchronized int getTotalRows() {
		return totalRows;
	}
	
	public synchronized static void addGlobalFields(int add) {
		totalFields+=add;
	}
	public synchronized static int getTotalFields() {
		return totalFields;
	}
	public synchronized static void addGlobalResultFields(int add) {
		totalResultFields+=add;
	}
	public synchronized static int getTotalResultFields() {
		return totalResultFields;
	}
	public synchronized static void addToIgnoreList(int i) {
		ignore.add(i);
	}
	public synchronized static List<Integer> getIgnoreList() {
		return ignore;
	}
	public synchronized static void addHeaderToResultFile(List<String> fieldFullNames) throws TechnicalException {
		try {
			int indexNumberTmp = getTotalResultFields();
			for (String fieldFullName : fieldFullNames) {	// Ordered
				for (int j = 0; j < indexNumberTmp; j++) {
					bufferedWriter.write(MyUtils.TAB_SEPARATOR);
				}
				bufferedWriter.write(fieldFullName + MyUtils.LINE_SEPARATOR);
				indexNumberTmp++;
			}
			bufferedWriter.flush();
		} catch (IOException e) {
			throw new TechnicalException(e);
		}
	}	
}














/*public static void addToFinalResults(QueryRunnerIntermediaryResult batchResult) {
	addToResultFile(batchResult.getValues(), ignore);
	if (false) {	// for now
		System.out.print(".");
		dotCount++;
		if (dotCount==200) {
			dotCount = 0;
			System.out.println();
		}
		if (dotCount==1000) {
			System.out.println();
		}
	}
}*/
/*public static void addToResultFile(List<List<String>> values, Integer maxRow) {
	try {
		for (List<String> list : values) {
			boolean first = true;
			for (int i = 0; i < list.size(); i++) {
				if (!ignore.contains(i)) {
					bufferedWriter.write((first ? "" : MyUtils.TAB_SEPARATOR) + list.get(i));
					if (!QueryRunnerPrototypeConstants.LOG) {
						System.out.print((first ? "" : MyUtils.TAB_SEPARATOR) + list.get(i));	
					}
					first = false;
				}
			}
			bufferedWriter.write(MyUtils.LINE_SEPARATOR);
			if (!QueryRunnerPrototypeConstants.LOG) {
				System.out.println();	
			}
			int totalRows = addRowToTotalRows();
			if (maxRow!=null && totalRows==maxRow.intValue()) {
				break;
			}
		}	
		if (QueryRunnerPrototypeConstants.DEBUG) {
			bufferedWriter.flush();
		}
	} catch (IOException e) {
		e.printStackTrace();
		System.exit(-1);
	}
}*/
