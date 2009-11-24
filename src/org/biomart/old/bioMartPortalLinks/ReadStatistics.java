package org.biomart.old.bioMartPortalLinks;


import java.io.File;
import java.util.StringTokenizer;

import org.biomart.common.general.utils.MyUtils;

public class ReadStatistics {

	public static void main(String[] args) {
		ReadStatistics readStatistics = new ReadStatistics();
		readStatistics.process();
	}

	public static final String SERIAL_NAME_BASE = "Serial";
	public static final String STATISTICS_NAME = "Statistics";
	public static final String BROKEN_LINK_SIDE_DATA_NAME = "Broken";
	
	public static final String LINK_INDEX_CREATION_FOLDER = MyUtils.OUTPUT_FILES_PATH + "LinkIndexCreation" + MyUtils.FILE_SEPARATOR
	+ "untitledfolder/";
	//+ "untitledfolder2/";
	//;
	public static final String SERIAL_PATH_AND_NAME_BASE = LINK_INDEX_CREATION_FOLDER + SERIAL_NAME_BASE;
	public static final String SERIAL_NAME = LINK_INDEX_CREATION_FOLDER + "Serial_true";
	
	public static final String LINK_SIDE_DATA_FOLDER = LINK_INDEX_CREATION_FOLDER + "ExportableData" + MyUtils.FILE_SEPARATOR;
	public static final String LINK_SIDE_DATA_STATISTICS_FILE_PATH_AND_NAME = LINK_SIDE_DATA_FOLDER + STATISTICS_NAME;
	
	public static final String LINK_INDEXES_FOLDER = LINK_INDEX_CREATION_FOLDER + "LinkIndexes" + MyUtils.FILE_SEPARATOR;
	public static final String LINK_INDEXES_STATISTICS_FILE_PATH_AND_NAME = LINK_INDEXES_FOLDER + STATISTICS_NAME;
	
	public static final String BROKEN_LINK_SIDE_DATA_NAME_FILE_PATH_AND_NAME = LINK_INDEX_CREATION_FOLDER + BROKEN_LINK_SIDE_DATA_NAME;;
	
	public void process() {
		
		File exportableDataStatistics = new File (LINK_SIDE_DATA_STATISTICS_FILE_PATH_AND_NAME);
		//File brokenExportableDataStatistics = new File (BROKEN_LINK_SIDE_DATA_NAME_FILE_PATH_AND_NAME);
		//File linkIndexesStatistics = new File (LINK_INDEXES_STATISTICS_FILE_PATH_AND_NAME);
		
		String content = null;
		content = MyUtils.readFile(exportableDataStatistics);
		StringTokenizer st = new StringTokenizer(content, MyUtils.LINE_SEPARATOR);
		while (st.hasMoreTokens()) {
			String line = st.nextToken();
			
			String[] tokens = line.split(MyUtils.TAB_SEPARATOR);
			MyUtils.checkStatusProgram(tokens.length==11, "tokens.length = " + tokens.length, true);
			
		}
		
	}
}
