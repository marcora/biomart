package org.biomart.test.linkIndicesTest.script;


import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.biomart.common.general.utils.Hack;
import org.biomart.common.general.utils.HackEnum;
import org.biomart.common.general.utils.IdGenerator;
import org.biomart.common.general.utils.MyUtils;
import org.biomart.common.general.utils.Range;
import org.biomart.test.linkIndicesTest.LinkIndexesUtils;


public class LinkIndicesScript {
	
	public static HackEnum hackEnum = null;
	public static Boolean debug = false;	/*if (!debug) {debug=Boolean.valueOf(InUtils.wrappedReadInput());}*/

	public static void main(String[] args) {}

	public static final String LOCALHOST = "msl";
	public static final String BMTEST = "msb";
	
	/**
	 * totalRowInMain has to be consistent with ranges 
	 */
	public String host = null;
	public Boolean createLinkIndexes = null;
	public Integer totalDataset = null;
	public Integer totalRowInMain = null;
	public Integer totalDimensionPerMain = null;	// up to 26 with no problems (letters)
	public Integer totalRowInDimensionPerRowInMain = null;
	public Hack[] hacks = null;		// accepts null as element
	
	public Range[] mainIdRanges = null;
	public Range[] subMainIdRanges = null;
	public Range[] dimensionIdRanges = null;
	
	public Integer mainIdRangesLength = null;
	public Integer subMainIdRangesLength = null;
	public Integer dimensionIdRangesLength = null;
	
	public Map<Integer, Integer> mapSharedMainID = null;
	public Map<Integer, Integer> mapSharedSubMainID = null;
	public Map<RowCombination, Integer> mapSharedDimensionID = null;

	public String description = null;
	public String name = null;
	public String rangeDescription1 = null;
	public String rangeDescription2 = null;
	
	public String databaseNameBase = null;
	public String outputFolderPathAndName = null;
	
	public LinkIndicesScript(String host, String description, boolean createLinkIndexes, 
			int totalDataset, int totalRowInMain, int totalDimensionPerMain, int totalRowInDimensionPerRowInMain, 
			Range[] mainIdRanges, Range[] subMainIdRanges, Range[] dimensionIdRanges, 
			String databaseNameBase, Hack[] hacks) {
		this.host = host;
		this.createLinkIndexes = createLinkIndexes;
		this.totalDataset = totalDataset;
		this.totalRowInMain = totalRowInMain;
		this.totalDimensionPerMain = totalDimensionPerMain;
		this.totalRowInDimensionPerRowInMain = totalRowInDimensionPerRowInMain;
		this.databaseNameBase = databaseNameBase;/*this.databaseNameBase = DATABASE_NAME_BASE + "_" + this.rangeDescription2;*/
		this.hacks = hacks;
		
		this.outputFolderPathAndName = LinkIndexesUtils.getOutputFolderPathAndName(host, this.databaseNameBase);
		
		this.mainIdRanges = mainIdRanges;
		this.subMainIdRanges = subMainIdRanges;
		this.dimensionIdRanges = dimensionIdRanges;
		
		this.mainIdRangesLength = Range.getTotalSize(this.mainIdRanges);
		this.subMainIdRangesLength = Range.getTotalSize(this.subMainIdRanges);
		this.dimensionIdRangesLength = Range.getTotalSize(this.dimensionIdRanges);
		
		hackEnum = HackEnum.MIDDLED;
		if (Hack.contains(this.hacks, hackEnum)) {
			Hack.getHack(this.hacks, hackEnum).subHack.integers[0] = 0;	// -> used as hackMiddledCount
			Hack.getHack(this.hacks, hackEnum).integers = buildHackRowTab(this.mainIdRanges, this.mainIdRangesLength);	// -> used as hackRowTab
		}
		
		boolean validMainIdRanges = true;
		if (null!=this.mainIdRanges) {
			for (int i = 0; i < this.mainIdRanges.length; i++) {
				validMainIdRanges&=this.mainIdRanges[i].isValidRangeFor(this.totalRowInMain);
			}
		}
		MyUtils.checkStatusProgram(validMainIdRanges, "validMainIdRanges");
		
		boolean validDimensionIdRanges = true;
		if (null!=this.dimensionIdRanges && this.totalDimensionPerMain>0) {
			for (int i = 0; i < this.dimensionIdRanges.length; i++) {
				validDimensionIdRanges&=this.dimensionIdRanges[i].isValidRangeFor(this.totalRowInDimensionPerRowInMain);
			}
		}
		MyUtils.checkStatusProgram(validDimensionIdRanges, "validDimensionIdRanges");
		
		hackEnum = HackEnum.HIDE_FULL_RANGES;
		this.rangeDescription1 = ConstructRangeDescriptionBracket();
		this.rangeDescription2 = ConstructRangeDescriptionUnderscore();		
		
		if (Hack.contains(this.hacks, hackEnum)) {
			this.rangeDescription1 = ConstructRangeDescriptionSize();
			this.rangeDescription2 = ConstructRangeDescriptionSize();	
		}
		
		this.name = "script_" + rangeDescription1;
		this.description = description;
		
		this.mapSharedMainID = new HashMap<Integer, Integer>();
		this.mapSharedSubMainID = new HashMap<Integer, Integer>();
		this.mapSharedDimensionID = new HashMap<RowCombination, Integer>();
	}
		
	public String process() throws IOException {

		StringBuffer scriptStringBuffer = new StringBuffer();
		StringBuffer script2StringBuffer = new StringBuffer();
		StringBuffer consoleStringBuffer = new StringBuffer();
		BufferedWriter[] importBufferedWriterTab = new BufferedWriter[totalDataset];		
		String sqlScriptFolderAndPath = this.outputFolderPathAndName + this.databaseNameBase + ".sql";
		String[] mainTablenameTab = new String[totalDataset];
		String[] importScriptPathAndNameTab = new String[totalDataset];
		for (int datasetNumber = 0; datasetNumber < totalDataset; datasetNumber++) {
			mainTablenameTab[datasetNumber] = LinkIndexesUtils.getMainTableName(datasetNumber);
			importScriptPathAndNameTab[datasetNumber] = 
				this.outputFolderPathAndName + mainTablenameTab[datasetNumber] + ".txt";
		}
		
		hackEnum = HackEnum.ON_LOCAL;
		if (Hack.contains(this.hacks, hackEnum)) {
			consoleStringBuffer.append(host + " < " + sqlScriptFolderAndPath + ";");
			consoleStringBuffer.append("mysqldump -hlocalhost -uroot -proot --databases ");
			for (int i = 0; i < totalDataset; i++) {
				consoleStringBuffer.append(getDatabaseName(i) + " ");
			}
			consoleStringBuffer.append("> " + this.outputFolderPathAndName + this.databaseNameBase + ".dump;");
			consoleStringBuffer.append("mysql -h$BMTEST -umartadmin -pbiomart < " + this.outputFolderPathAndName + this.databaseNameBase + ".dump;");		
		} else {
			consoleStringBuffer.append(host + " < " + sqlScriptFolderAndPath + ";");
		}
		
		scriptStringBuffer.append("# " + consoleStringBuffer.toString() + MyUtils.LINE_SEPARATOR);
		scriptStringBuffer.append(MyUtils.LINE_SEPARATOR);
		scriptStringBuffer.append("# " + toString() + MyUtils.LINE_SEPARATOR);
		scriptStringBuffer.append(MyUtils.LINE_SEPARATOR);
		
		// Drop and creates
		scriptStringBuffer.append("drop database if exists " + this.databaseNameBase + ";" + MyUtils.LINE_SEPARATOR);
		for (int datasetNumber = 0; datasetNumber < totalDataset; datasetNumber++) {
			scriptStringBuffer.append("drop database if exists " + getDatabaseName(datasetNumber) + ";" + MyUtils.LINE_SEPARATOR);
			scriptStringBuffer.append("create database " + getDatabaseName(datasetNumber) + ";" + MyUtils.LINE_SEPARATOR);
			scriptStringBuffer.append(MyUtils.LINE_SEPARATOR);
		}
		
		for (int datasetNumber = 0; datasetNumber < totalDataset; datasetNumber++) {
			System.out.println("datasetNumber = " + datasetNumber);
		
			// Writer for mysqlimport files (can be big so need BufferedWriter)
			MyUtils.createParentFolders(importScriptPathAndNameTab[datasetNumber]);
			importBufferedWriterTab[datasetNumber] = new BufferedWriter(new FileWriter(new File(importScriptPathAndNameTab[datasetNumber])));
			
			scriptStringBuffer.append(LinkIndexesUtils.getUseDatabaseQuery(getDatabaseName(datasetNumber)) + MyUtils.LINE_SEPARATOR);
			
			// Creates
			scriptStringBuffer.append("create table " + LinkIndexesUtils.getMainTableName(datasetNumber) + 
					" (" + LinkIndexesUtils.getMainIdField(datasetNumber) + " int(1), " + LinkIndexesUtils.getMainDescField(datasetNumber) + " varchar(64))" + ";" + MyUtils.LINE_SEPARATOR);
			
			for (int dimensionNumber = 0; dimensionNumber < totalDimensionPerMain; dimensionNumber++) {
				Character dimensionLetter = LinkIndexesUtils.getDimensionLetter(dimensionNumber);
				
				scriptStringBuffer.append("create table " + LinkIndexesUtils.getDimensionTableName(dimensionLetter, datasetNumber) + 
						" (" + LinkIndexesUtils.getMainIdField(datasetNumber) + " int(1), " + LinkIndexesUtils.getDimensionIdField(dimensionLetter, datasetNumber) + " int(1), " + 
						LinkIndexesUtils.getDimensionDescField(dimensionLetter, datasetNumber) + " varchar(64))" + ";" + MyUtils.LINE_SEPARATOR);
			}
			scriptStringBuffer.append(MyUtils.LINE_SEPARATOR);
			
			// Inserts
			List<Integer> listMainID = new ArrayList<Integer>();
			for (int mainRow = 0; mainRow < totalRowInMain; mainRow++) {
				if ((mainRow%10000)==0) {
					System.out.println("\tmainRow = " + mainRow + "/" + totalRowInMain);
				}

				hackEnum = HackEnum.DATASET_NOT_IDENTICALS_1;
				if (Hack.contains(this.hacks, hackEnum)) {
					if (datasetNumber==1 && mainRow<Hack.getHack(this.hacks, hackEnum).integers[0]) {
						continue;
					}
				}
				Integer mainID = getMainID(datasetNumber, mainRow);
				listMainID.add(mainID);
				/*script2StringBuffer.append("insert into " + getMainTableName(datasetNumber) + " values " +
						"(" + mainID + ", " + getMainDesc(getMainDescField(datasetNumber), mainRow, mainID) + ")" + 
						";" + MyUtils.LINE_SEPARATOR);*/	// disabled for now
				importBufferedWriterTab[datasetNumber].write(mainID + MyUtils.TAB_SEPARATOR + 
						LinkIndexesUtils.getMainDesc(LinkIndexesUtils.getMainDescField(datasetNumber), mainRow, mainID) + MyUtils.LINE_SEPARATOR);
			}
			script2StringBuffer.append(MyUtils.LINE_SEPARATOR);
			
			for (int dimensionNumber = 0; dimensionNumber < totalDimensionPerMain; dimensionNumber++) {
				Character dimensionLetter = LinkIndexesUtils.getDimensionLetter(dimensionNumber);
				
				for (int mainRow = 0; mainRow < totalRowInMain; mainRow++) {
					Integer mainID = listMainID.get(mainRow);
					
					for (int dimensionRowForMainRow = 0; dimensionRowForMainRow < totalRowInDimensionPerRowInMain; 
					dimensionRowForMainRow++) {
						Integer dimensionID = getDimensionID(mainRow, dimensionRowForMainRow);
						script2StringBuffer.append("insert into " + LinkIndexesUtils.getDimensionTableName(dimensionLetter, datasetNumber) + " values (" +
								mainID + ", " + dimensionID + ", " + 
								LinkIndexesUtils.getDimensionDesc(LinkIndexesUtils.getDimensionDescField(dimensionLetter, datasetNumber), 
										mainRow, dimensionRowForMainRow, mainID, dimensionID) + ")" + ";" + MyUtils.LINE_SEPARATOR);
						/*importStringBuffer.append(mainID + MyUtils.TAB_S dimensionRowForMainRow, mainID, dimensionID) 
								+ MyUtils.LINE_SEPARATOR);*/	// not dim yet...
					}
					script2StringBuffer.append(MyUtils.LINE_SEPARATOR);
				}
				script2StringBuffer.append(MyUtils.LINE_SEPARATOR);
			}
			script2StringBuffer.append(MyUtils.LINE_SEPARATOR);
		}
			
		// Load data into tables & create db indexes
		for (int datasetNumber = 0; datasetNumber < totalDataset; datasetNumber++) {
			
			scriptStringBuffer.append(LinkIndexesUtils.getUseDatabaseQuery(getDatabaseName(datasetNumber)) + MyUtils.LINE_SEPARATOR);
			
			// Create load data local infile files
			scriptStringBuffer.append(LinkIndexesUtils.getLoadDataLocalInfileQuery(
					this.outputFolderPathAndName, mainTablenameTab[datasetNumber]) + MyUtils.LINE_SEPARATOR);
			scriptStringBuffer.append(MyUtils.LINE_SEPARATOR);
			
			// DB indexes
			String tableToDbIndex = LinkIndexesUtils.getMainTableName(datasetNumber);	//"ds0__main0__main";
			String fieldToDbIndex = LinkIndexesUtils.getMainIdField(datasetNumber);	//"main0_id_key";
			
			String dbIndexName = "dbIndex_" + tableToDbIndex + "_" + fieldToDbIndex;
			
			scriptStringBuffer.append(LinkIndexesUtils.getDropDbIndexIfExistsQuery(
					dbIndexName, tableToDbIndex, fieldToDbIndex) + MyUtils.LINE_SEPARATOR);	// fake
			scriptStringBuffer.append(LinkIndexesUtils.getCreateDbIndexQuery(
					dbIndexName, tableToDbIndex, fieldToDbIndex) + MyUtils.LINE_SEPARATOR);
			scriptStringBuffer.append(MyUtils.LINE_SEPARATOR);
			
		}
		
		// Create database for result table and link indexes tables
		scriptStringBuffer.append("create database if not exists " + this.databaseNameBase + ";" + MyUtils.LINE_SEPARATOR);
		scriptStringBuffer.append(LinkIndexesUtils.getUseDatabaseQuery(this.databaseNameBase) + MyUtils.LINE_SEPARATOR);
		scriptStringBuffer.append(MyUtils.LINE_SEPARATOR);
		
		// Result tables
		String withoutIndexResultTableName = this.databaseNameBase + "_result_without_index";
		String withIndexResultTableName = this.databaseNameBase + "_result_with_index";
				
		scriptStringBuffer.append(LinkIndexesUtils.getDropTableIfExistsQuery(withoutIndexResultTableName) + MyUtils.LINE_SEPARATOR);
		scriptStringBuffer.append(LinkIndexesUtils.getCreateResultTableQuery(withoutIndexResultTableName) + MyUtils.LINE_SEPARATOR);
		scriptStringBuffer.append(MyUtils.LINE_SEPARATOR);
		
		scriptStringBuffer.append(LinkIndexesUtils.getDropTableIfExistsQuery(withIndexResultTableName) + MyUtils.LINE_SEPARATOR);
		scriptStringBuffer.append(LinkIndexesUtils.getCreateResultTableQuery(withIndexResultTableName) + MyUtils.LINE_SEPARATOR);
		scriptStringBuffer.append(MyUtils.LINE_SEPARATOR);

		// Hacks
		System.out.println("Hacks");
		
		hackEnum = HackEnum.SAME_DATABASE;
		if (Hack.contains(this.hacks, hackEnum)) {
			String s0 = getDatabaseName(0);
			String s1 = getDatabaseName(1);
			scriptStringBuffer = MyUtils.stringBufferReplace(scriptStringBuffer, s1, s0);
			script2StringBuffer = MyUtils.stringBufferReplace(script2StringBuffer, s1, s0);
			consoleStringBuffer = MyUtils.stringBufferReplace(consoleStringBuffer, s1, s0);
		}
		
		hackEnum = HackEnum.FULLY_DESCRIPTIVE_DATABASE_NAMES;
		if (Hack.contains(this.hacks, hackEnum)) {
			for (int datasetNumber = 0; datasetNumber < totalDataset; datasetNumber++) {
				String s0 = getDatabaseName(datasetNumber);
				String s1 = Hack.getHack(this.hacks, hackEnum).strings[datasetNumber];
				scriptStringBuffer = MyUtils.stringBufferReplace(scriptStringBuffer, s0, s1);
				script2StringBuffer = MyUtils.stringBufferReplace(script2StringBuffer, s0, s1);
				consoleStringBuffer = MyUtils.stringBufferReplace(consoleStringBuffer, s0, s1);
			}

			/*String s3 = DATABASE_NAME_BASE;
			String s4 = Hack.getHack(this.hacks, hackEnum).strings[0];
			scriptStringBuffer = MyUtils.stringBufferReplace(scriptStringBuffer, s3, s4);
			script2StringBuffer = MyUtils.stringBufferReplace(script2StringBuffer, s3, s4);
			for (int i = 0; i < importStringBuffer.length; i++) {
				importStringBuffer[i] = MyUtils.stringBufferReplace(importStringBuffer[i], s3, s4);
			}
			consoleStringBuffer = MyUtils.stringBufferReplace(consoleStringBuffer, s3, s4);*/
		}
		
		hackEnum = HackEnum.FULLY_DESCRIPTIVE_SCRIPT_NAMES;
		if (Hack.contains(this.hacks, hackEnum)) {
			String s0 = null;
			String s1 = Hack.getHack(this.hacks, hackEnum).strings[0];
			
			s0 = getDatabaseName(0);
			scriptStringBuffer = MyUtils.stringBufferReplace(scriptStringBuffer, s0, s1);
			script2StringBuffer = MyUtils.stringBufferReplace(script2StringBuffer, s0, s1);
			consoleStringBuffer = MyUtils.stringBufferReplace(consoleStringBuffer, s0, s1);
			sqlScriptFolderAndPath = sqlScriptFolderAndPath.replace(s0, s1);		// also overwrite this variable!
			
			s0 = getDatabaseName(1);
			scriptStringBuffer = MyUtils.stringBufferReplace(scriptStringBuffer, s0, s1);
			script2StringBuffer = MyUtils.stringBufferReplace(script2StringBuffer, s0, s1);
			consoleStringBuffer = MyUtils.stringBufferReplace(consoleStringBuffer, s0, s1);
			sqlScriptFolderAndPath = sqlScriptFolderAndPath.replace(s0, s1);		// also overwrite this variable!
		}
		
		// Output (make sure all hacks have been applied on objects that are used here)
		new MyUtils.WriteFileClass().writeFile(sqlScriptFolderAndPath, scriptStringBuffer.toString());				
		for (int datasetNumber = 0; datasetNumber < totalDataset; datasetNumber++) {
			importBufferedWriterTab[datasetNumber].close();			
		}
		
		System.out.println(MyUtils.LINE_SEPARATOR + consoleStringBuffer);
		
		return consoleStringBuffer.toString();
	}
	
	private String ConstructRangeDescriptionBracket() {
		StringBuffer stringBuffer = new StringBuffer();		
		stringBuffer.append(Range.toShortStringBracket(this.mainIdRanges));
		stringBuffer.append(Range.toShortStringBracket(this.subMainIdRanges));
		stringBuffer.append(Range.toShortStringBracket(this.dimensionIdRanges));
		return stringBuffer.toString();
	}

	private String ConstructRangeDescriptionUnderscore() {
		StringBuffer stringBuffer = new StringBuffer();
		stringBuffer.append(Range.toShortStringUnderscore(this.mainIdRanges));
		stringBuffer.append("_");
		stringBuffer.append(Range.toShortStringUnderscore(this.subMainIdRanges));
		stringBuffer.append("_");
		stringBuffer.append(Range.toShortStringUnderscore(this.dimensionIdRanges));
		return stringBuffer.toString();
	}
	private String ConstructRangeDescriptionSize() {
		StringBuffer stringBuffer = new StringBuffer();		
		stringBuffer.append(Range.toShortStringSize(this.mainIdRanges));
		stringBuffer.append("_");
		stringBuffer.append(Range.toShortStringSize(this.subMainIdRanges));
		stringBuffer.append("_");
		stringBuffer.append(Range.toShortStringSize(this.dimensionIdRanges));
		return stringBuffer.toString();
	}
	
	private String getDatabaseName(int datasetNumber) {
		return LinkIndexesUtils.getDatabaseName(this.databaseNameBase, datasetNumber);
	}
	
	private int getMainID(int datasetNumber, int mainRow) {	//datasetNumber is only for the hacks
		Boolean share = Boolean.FALSE;
		if (null!=mainIdRanges) {
			for (int i = 0; i < mainIdRanges.length; i++) {
				if (mainIdRanges[i].isInRange(mainRow)) {
					share = Boolean.TRUE;
					break;
				}
			}
		}
		
		hackEnum = HackEnum.MIDDLED;
		if (Hack.contains(this.hacks, hackEnum)) {
			if (datasetNumber==1) {
				int low = (int)((double)totalRowInMain/2)-(int)((double)mainIdRangesLength/2);
				int high = low+mainIdRangesLength-1;
								
				
				if (mainRow>=low && mainRow<=high) {			// inclusive
					share=true;									// reconsider it
					mainRow = Hack.getHack(this.hacks, hackEnum).integers[Hack.getHack(this.hacks, hackEnum).subHack.integers[0]];	// -> hackRowTab[hackMiddledCount]
																// redirect it
					Hack.getHack(this.hacks, hackEnum).subHack.integers[0]++;	// -> hackMiddledCount
				} else {
					share = false;								// reconsider it
				}
			}
		}
		
		hackEnum = HackEnum.DECREASING_OVERLAP;
		if (Hack.contains(this.hacks, hackEnum)) {
			if (datasetNumber!=0) {
				int newOverlapStart = this.totalRowInMain-Hack.getHack(this.hacks, hackEnum).integers[datasetNumber];
				if (mainRow<newOverlapStart) {
					share=false;						// reconsider it
				}
			}
		}

		if (!share) {
			return IdGenerator.getNextID();
		} else {
			Integer id = mapSharedMainID.get(mainRow);
			if (null==id) {
				id = IdGenerator.getNextID();
				mapSharedMainID.put(mainRow, id);
			} else {
				IdGenerator.addToSkip();
			}
			return id;
		}
	}
	public final Integer[] buildHackRowTab(Range[] ranges, int totalRangesSize) {
		Integer[] hackRowTab = null;
		if (null!=ranges) {
			hackRowTab = new Integer[totalRangesSize];
			int i = 0;		
			for (int j = 0; j < ranges.length; j++) {
				for (int k = 0; k < ranges[j].size; k++) {
					hackRowTab[i]=ranges[j].lower+k;
					i++;
				}
			}
		}
		return hackRowTab;
	}
	
	private int getDimensionID(int mainRow, int dimensionRowForMainRow) {
		Boolean share = Boolean.FALSE;
		if (null!=dimensionIdRanges) {
			for (int i = 0; i < dimensionIdRanges.length; i++) {
				if (dimensionIdRanges[i].isInRange(dimensionRowForMainRow)) {
					share = Boolean.TRUE;
					break;
				}
			}
		}
		if (!share) {
			return IdGenerator.getNextID();
		} else {
			RowCombination data = new RowCombination(mainRow, dimensionRowForMainRow);
			Integer id = mapSharedDimensionID.get(data);
			if (null==id) {
				id = IdGenerator.getNextID();
				mapSharedDimensionID.put(data, id);
			}
			return id;
		}
	}
	
	/* 2007/12/28-14:34 */
	@Override
	public String toString () {	
		// Can't happen but just in case
		if (null==this) {
			return "instance is null";
		}	
		
		StringBuilder stringBuilder = new StringBuilder ("");
		stringBuilder.append( "[instance of " +
				this.getClass().getSimpleName() + " : ");
		Object fieldObject = null;
		try {
			for (Field field : this.getClass().getDeclaredFields()) {
				fieldObject = field.get(this);
				if (null==fieldObject) {
					stringBuilder.append( "(" + field.getType().getSimpleName() + ")" + 
							field.getName() + " = null, ");
				} else {
					stringBuilder.append( "(" + fieldObject.getClass().getSimpleName() + ")" + 
							field.getName() + " = " + fieldObject + ", ");
				}
			}
		} catch (IllegalArgumentException e) {			
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		}
		stringBuilder.append( "]" );
		return stringBuilder.toString();
	}
}




/*
	private int getMainID2(int mainRow) {
		Boolean share = Boolean.FALSE;
		if (null!=mainIdRanges) {
			for (int i = 0; i < mainIdRanges.length; i++) {
				if (mainIdRanges[i].isInRange(mainRow)) {
					share = Boolean.TRUE;
					break;
				}
			}
		}
		if (!share) {
			return IdGenerator.getNextID();
		} else {
			Integer id = mapSharedMainID.get(mainRow);
			if (null==id) {
				id = IdGenerator.getNextID();
				mapSharedMainID.put(mainRow, id);
			} else {
				IdGenerator.addToSkip();
			}
			return id;
		}
	}
*/
/*if (createLinkIndexes) {	//TODO broken (now that in different databases)
	for (int datasetNumber = 1; datasetNumber < totalDataset; datasetNumber++) {
		// Link indexes
		String index_dataset_combination = "_" + (datasetNumber-1) + "_" + datasetNumber;
		String indexTableName = "index_" + this.databaseNameBase + index_dataset_combination;
		String indexTableKey = "main_id_key" + index_dataset_combination;
		String tableToIndex1 = LinkIndicesUtils.getMainTableName(datasetNumber-1);
		String tableToIndex2 = LinkIndicesUtils.getMainTableName(datasetNumber);
		String fieldToIndex1 = LinkIndicesUtils.getMainIdField(datasetNumber-1);
		String fieldToIndex2 = LinkIndicesUtils.getMainIdField(datasetNumber);
		
		scriptStringBuffer.append(LinkIndicesUtils.getDropTableIfExistsQuery(indexTableName) + MyUtils.LINE_SEPARATOR);
		scriptStringBuffer.append(LinkIndicesUtils.getCreateIndexQuery(
				indexTableName, indexTableKey, tableToIndex1, tableToIndex2, fieldToIndex1, fieldToIndex2) + MyUtils.LINE_SEPARATOR);
		scriptStringBuffer.append(MyUtils.LINE_SEPARATOR);
	}
}*/