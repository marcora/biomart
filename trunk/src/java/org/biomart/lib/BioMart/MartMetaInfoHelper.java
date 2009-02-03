/**
 * 
 */
package org.biomart.lib.BioMart;

import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;



/**
 * @author junjun
 *
 */
public class MartMetaInfoHelper extends Root{

	// some settings. TODO: we should move this type of settings to a .properties file
	static final String metaTablePrefix = "meta_";
	static final String tableNameDivisionDelimiter = "__";
	static final String keyColumnSuffix = "_key";
	static final String booleanColumnSuffix = "_bool";
	static final boolean skipBooleanCol = true;
	static final Pattern partitionPatternDS = Pattern.compile("((P\\d+)(.+)(\\2))_", Pattern.CASE_INSENSITIVE);
	static final Pattern partitionPatternDM = Pattern.compile("_((P\\d+)(.+)(\\2))", Pattern.CASE_INSENSITIVE);

	// some private fields
	
	private String locationName = "";
	private String martName = "";
	
	private StringBuilder metaInfoXML = new StringBuilder();
	
	private ArrayList dbTable = new ArrayList();
	
	private TreeMap partitionTable = new TreeMap(); // partitionTable
	private LinkedHashMap dbDataset = new LinkedHashMap(); // {(P1C1)_egene=[[P1R1],[P1R2], (P1C1)_(P8C1)_homology=[[P1R1,P8R1],[P1R1,P8R2],, ],,,}
	private HashMap dbDatasetTable = new HashMap(); // {(P1C1)_egene={(P1C1)_egene__gene__main=[[P1R1],[P1R2],,],,},,}
	private HashMap dbTableColumn = new HashMap(); // {(P1C1)_egene={(P1C1)_egene__gene__main={id=[[P1R1],[P1R2],,],,}},,,}
	
	
	/**
	 *  empty constructor 
	 */
	public MartMetaInfoHelper() {
		
	}


	/**
	 * @param dmd 
	 * @param schemaName 
	 * @return String
	 * @throws Exception 
	 */
	public String makeMetaInfoXML(DatabaseMetaData dmd, String schemaName, String locationName, String martName) throws Exception {
		
		this.locationName = locationName;
		this.martName = martName;
		
		// construct dbTable so that main tables go first, then dm tables
		ResultSet tables = dmd.getTables(null, schemaName, "%main", null);
		while(tables.next()){
			dbTable.add(tables.getString(3));
		}
		tables.close();
		tables = dmd.getTables(null, schemaName, "%dm", null);
		while(tables.next()){
			dbTable.add(tables.getString(3));
		}
		tables.close();
		
		// processing tables (it's guaraunteed that main tables come before dm tables 
		Iterator tableIt = dbTable.iterator();
		while(tableIt.hasNext()){
			String tableName = (String) tableIt.next();
			
			// skipping meta tables
			if (tableName.startsWith(metaTablePrefix)) continue;
			
			if (tableName.contains("egene")) continue;  // TODO: remove this later. this is just for quickly testing a dataset without partition
			
			String [] tableNameDivisions = tableName.split(tableNameDivisionDelimiter);
			
			if (tableNameDivisions.length != 3 ||
					(!tableNameDivisions[2].equals("main") && !tableNameDivisions[2].equals("dm"))) {
				log.warning("table name '" + tableName + "' doesn't conform with naming convention, skipped.");
				continue;
			}

			log.info("processing table: " + tableName);

			String dataset = tableNameDivisions[0];
			ArrayList partitionRange = new ArrayList();

			HashMap partitionInfoDS = partitionParser(partitionPatternDS, dataset);
			
			// populating partitionTable and looking up partitionRow
			if (partitionInfoDS.containsKey("partition")) {
				dataset = (String) partitionInfoDS.get("partitionedName");
				if (tableNameDivisions[2].equals("main")) {  // main table
					partitionRange = partitionLookup((ArrayList) partitionInfoDS.get("partition"), false);
				} else {  // dm table
					partitionRange = partitionLookup((ArrayList) partitionInfoDS.get("partition"), true);
					if (partitionRange == null) {
						log.warning("dimenssion table '" + tableName + "' in dataset '" + dataset + "' does not have any associated main table with the same partition(s), skipped!");
						continue;
					}
				}
			}

			// populating dbDataset when it's main table
			if (tableNameDivisions[2].equals("main")) {
				if (!dbDataset.containsKey(dataset))  // if it's a new dataset
					dbDataset.put(dataset, new LinkedHashSet());  // add dataset
			
				((LinkedHashSet) dbDataset.get(dataset)).add(partitionRange);  // add the partitionRange
			} else { // dm table, validate it
				if (!dbDataset.containsKey(dataset)) { // if it's a non-exist dataset
					log.warning("dimenssion table '" + tableName + "' does not have any associated main table, skipping the dm table, skipping creating dataset '" + dataset + "'!");
					continue;
				}
			}
			
			// populate dbDatasetTable and dbTableColumn
			String content = tableNameDivisions[1];

			HashMap partitionInfoDM = partitionParser(partitionPatternDM, content);
			
			// populating partitionTable and looking up partitionRow
			if (partitionInfoDM.containsKey("partition")) {
				content = (String) partitionInfoDM.get("partitionedName");
				partitionRange.addAll(partitionLookup((ArrayList) partitionInfoDM.get("partition"), false));
			}

			String dbMartTableName = dataset + tableNameDivisionDelimiter + content + tableNameDivisionDelimiter + tableNameDivisions[2];
			if (!dbDatasetTable.containsKey(dataset)) {
				dbDatasetTable.put(dataset, new LinkedHashMap());  // add dataset to dbDatasetTable
				dbTableColumn.put(dataset, new LinkedHashMap());  // add dataset to dbTableColumn
			}
			
			if (!((LinkedHashMap) dbDatasetTable.get(dataset)).containsKey(dbMartTableName)) { // it's a new table
				((LinkedHashMap) dbDatasetTable.get(dataset)).put(dbMartTableName, new LinkedHashSet());  // add the new table with it's value being an empty LinkedHashSet
				((LinkedHashMap) dbTableColumn.get(dataset)).put(dbMartTableName, new LinkedHashMap());  // add the new table with it's value being an empty LinkedHashMap for columns
			}
			
			((LinkedHashSet)((LinkedHashMap) dbDatasetTable.get(dataset)).get(dbMartTableName)).add(partitionRange);

			// scan table columns and populate dbTableColumn
			ResultSet columns = dmd.getColumns(null, schemaName, tableName, null);
            while(columns.next()){
                    String columnName = columns.getString(4);
                    
                    if (skipBooleanCol && columnName.endsWith(booleanColumnSuffix)) continue;
                    
                    if (!((LinkedHashMap) ((LinkedHashMap) dbTableColumn.get(dataset)).get(dbMartTableName)).containsKey(columnName))
                    	((LinkedHashMap) ((LinkedHashMap) dbTableColumn.get(dataset)).get(dbMartTableName)).put(columnName, new LinkedHashSet());

                   	((LinkedHashSet) ((LinkedHashMap) ((LinkedHashMap) dbTableColumn.get(dataset))
                   			.get(dbMartTableName)).get(columnName)).add(partitionRange);
            }

		}


		// output partitions
		log.info("generating partition information ...");
		outputPartitionInfo();
		
		// output datasets
		log.info("generating dataset information ...");
		outputDatasetInfo();

		// print for debugging
		//System.out.println("partitionTable=" + partitionTable);
		//System.out.println("dbDataset=" + dbDataset);
		//System.out.println("dbDatasetTable=" + dbDatasetTable);
		//System.out.println("dbTableColumn=" + dbTableColumn);
		
		if (metaInfoXML.toString().length() == 0) return null;
		log.info("metaInfoXML generated");
		return metaInfoXML.toString();
	}


	private HashMap partitionParser (Pattern pattern, String fullname){
		HashMap partitionInfo = new HashMap();
		partitionInfo.put("name", fullname);
		
		ArrayList partition = new ArrayList();
		String partitionString = "";
		String partitionTemplate = "";
		
		Matcher m = pattern.matcher(fullname);
		while (m.find()) {
			if (m.group(2).equals(m.group(4))) {
				partition.add(new String [] {m.group(2), m.group(1)});
				if (partitionString.equals("")) {
					partitionString = m.group(1);
					partitionTemplate = "(" + m.group(2) + "C1)";
				} else {
					partitionString = partitionString + "_" + m.group(1);
					partitionTemplate = partitionTemplate + "_" + "(" + m.group(2) + "C1)";
				}
			}
		}
		
		if (partition.size() > 0) {
			String name = m.replaceAll(""); 
			if (fullname.startsWith(name) && fullname.equals(name + "_" + partitionString)) {
				partitionInfo.put("name", name);
				partitionInfo.put("partitionedName", name + "_" + partitionTemplate);
				partitionInfo.put("partition", partition);
			}else if (fullname.endsWith(name) && fullname.equals(partitionString + "_" + name)) {
				partitionInfo.put("name", name);
				partitionInfo.put("partitionedName", partitionTemplate + "_" + name);
				partitionInfo.put("partition", partition);
			}
		}
		
		return partitionInfo;
	}

	
	// this method does dual jobs: 1. populating partitionTable; 2. looking up partitionRow
	private ArrayList partitionLookup (ArrayList partitionInfo, boolean lookupOnly) {
		ArrayList partitionRange = new ArrayList();
		
		Iterator i = partitionInfo.iterator();
		while(i.hasNext()){
			String [] partitionData = (String []) i.next();
			if (!partitionTable.containsKey(partitionData[0])) { // if it's a new partitionName
				if (lookupOnly) return null;  // if it's lookupOnly, we can return null right away
				partitionTable.put(partitionData[0], new LinkedHashSet());  // add it
			}
			
			if (!((LinkedHashSet) partitionTable.get(partitionData[0])).contains(partitionData[1])) {  // new partitionRow
				if (lookupOnly) return null;  // if it's lookupOnly, we can return null right away
				((LinkedHashSet) partitionTable.get(partitionData[0])).add(partitionData[1]);  // add it
			}
			
			// now find out the partitionRow
			ArrayList partitionRows = new ArrayList((LinkedHashSet) partitionTable.get(partitionData[0]));
			
			partitionRange.add(partitionData[0] + "R" + (partitionRows.indexOf(partitionData[1]) + 1));
		}
		
		return partitionRange;
	}
	
	private void outputPartitionInfo() {
		Iterator pI = partitionTable.keySet().iterator();
		while(pI.hasNext()){
			String partitionName = (String) pI.next();
			metaInfoXML.append("<partitionTable name=\"" + partitionName + "\">\n");
			Iterator prI = ((LinkedHashSet) partitionTable.get(partitionName)).iterator();
			int prow = 1;
			while(prI.hasNext()){
				metaInfoXML.append("\t<cell row=\"" + prow + "\" col=\"1\">" + prI.next() + "</cell>\n");
				prow++;
			}
			metaInfoXML.append("</partitionTable>\n");
		}
	}
	
	private void outputDatasetInfo() {
		Iterator dI = dbDataset.keySet().iterator();
		while(dI.hasNext()){
			String datasetName = (String) dI.next(); 
			Iterator pI = ((LinkedHashSet) dbDataset.get(datasetName)).iterator();
			String range = getPartitionRange(pI);

			metaInfoXML.append("<dataset name=\"" + datasetName 
								+ "\" range=\"" + range + "\">\n");
			// outputTable
			outputTableInfo((LinkedHashMap) dbDatasetTable.get(datasetName), datasetName);
			
			// outputConfig
			outputConfig(datasetName);
			
			metaInfoXML.append("</dataset>\n");
		}
	}

	private void outputConfig(String datasetName){
		metaInfoXML.append("\t<config name=\"naive\" dataset=\"" + datasetName + "\">\n");
		// output templateConfig
		outputConfigInfo(datasetName, true);
		// output userConfig
		outputConfigInfo(datasetName, false);
		metaInfoXML.append("\t</config>\n");
	}

	private void outputConfigInfo(String datasetName, boolean isTemplate) {
		String xmlLeadingTab = "\t\t";

		String configName = isTemplate ? "templateConfig" : "userConfig";
		metaInfoXML.append(xmlLeadingTab + "<" + configName + " name=\""+ configName + "\">\n");

		outputDatasetContainer(datasetName, null, 0, isTemplate);
		
		metaInfoXML.append(xmlLeadingTab + "</" + configName + ">\n");
	}

	private void outputDatasetContainer(String datasetName, String rangeWithNoRootPartition, int levelIndex, boolean isTemplate) {
		char [] tab = new char [levelIndex + 3];
		Arrays.fill(tab, '\t');
		String xmlLeadingTab = new String(tab);
		
		Pattern rvRowTag = Pattern.compile("R\\d+$");
		
		ArrayList partitionTree = new ArrayList();
		partitionTree.addAll( (LinkedHashSet) dbDataset.get(datasetName) );
		
		int partitionLevels = ((ArrayList) (partitionTree.get(0))).size();
		
		if (partitionLevels == 0) {  // non-partitioned dataset
			// outputTableConfig
			metaInfoXML.append(xmlLeadingTab + "<container name=\"" + datasetName + "\" rang=\"\">\n");
			metaInfoXML.append(xmlLeadingTab + "\tTODO: outputTableConfig: " + isTemplate + "\n");
			metaInfoXML.append(xmlLeadingTab + "</container>\n");
			return;
		}
		
		if (isTemplate || levelIndex == 0) {  // template or root level
			Iterator pI = partitionTree.iterator();
		
			String range = getPartialPartitionRange(pI, levelIndex, "");
		
			String partitionName = rvRowTag.matcher((String) ((ArrayList) partitionTree.get(0)).get(levelIndex)).replaceAll("");
			metaInfoXML.append(xmlLeadingTab + "<container name=\"" + 
					partitionName + "\" range=\"" + range + "\">\n");

			if (levelIndex == partitionLevels - 1) {
				metaInfoXML.append(xmlLeadingTab + "\tTODO: outputTableConfig: " + isTemplate + "\n");
			}
		
			// recursively call itself
			if (levelIndex < partitionLevels - 1) outputDatasetContainer(datasetName, "", levelIndex + 1, isTemplate);
		
			metaInfoXML.append(xmlLeadingTab + "</container>\n");
		} else {  // userConfig
			// iterate partitionRow for this level
			LinkedHashSet partitionRow = new LinkedHashSet();
			for (int prow = 0; prow < partitionTree.size(); prow++) {
				partitionRow.add((String)((ArrayList) partitionTree.get(prow)).get(levelIndex));
			}
			
			Iterator prI = partitionRow.iterator();
			while(prI.hasNext()) {
				
				String pRow = (String) prI.next();
				
				String myRangeWithNoRootPartition = rangeWithNoRootPartition.equals("") ? pRow : rangeWithNoRootPartition + ":" + pRow;

				Iterator pI = partitionTree.iterator();

				String range = getPartialPartitionRange(pI, levelIndex, myRangeWithNoRootPartition);
		
				metaInfoXML.append(xmlLeadingTab + "<container name=\"" + 
							pRow + "\" range=\"" + range + "\">\n");

				if (levelIndex == partitionLevels - 1) {
					metaInfoXML.append(xmlLeadingTab + "\tTODO: outputTableConfig: " + isTemplate + "\n");
				}
		
				// recursively call itself
				if (levelIndex < partitionLevels - 1) outputDatasetContainer(datasetName, myRangeWithNoRootPartition, levelIndex + 1, isTemplate);
		
				metaInfoXML.append(xmlLeadingTab + "</container>\n");
			}
		}

	}
	
	private void outputTableInfo(LinkedHashMap martTables, String datasetName) {
		Iterator tI = (martTables).keySet().iterator();
		while(tI.hasNext()){
			String tableName = (String) tI.next();
			Iterator pI = ((LinkedHashSet) martTables.get(tableName)).iterator();
			String range = getPartitionRange(pI);

			metaInfoXML.append("\t<table name=\"" + tableName 
					+ "\" range=\"" + range + "\">\n");
			outputColumnInfo((LinkedHashMap) ((LinkedHashMap) dbTableColumn.get(datasetName)).get(tableName));
			metaInfoXML.append("\t</table>\n");
		}
	}
	
	private void outputColumnInfo(LinkedHashMap martTableColumns) {
		Iterator tI = (martTableColumns).keySet().iterator();
		while(tI.hasNext()){
			String columnName = (String) tI.next();
			Iterator pI = ((LinkedHashSet) martTableColumns.get(columnName)).iterator();
			String range = getPartitionRange(pI);

			metaInfoXML.append("\t\t<attribute name=\"" + columnName 
					+ "\" range=\"" + range + "\"/>\n");
			//outputColumnInfo((LinkedHashMap) ((LinkedHashMap) dbTableColumn.get(datasetName)).get(tableName));
		}
	}
	
	private String getPartitionRange (Iterator partition) {
		StringBuilder range = new StringBuilder();
		
		while(partition.hasNext()){
			ArrayList partitionRow = (ArrayList) partition.next();
			
			String partitionRange = "";
			for(int i=0; i<partitionRow.size(); i++){
				if(i==0){
					partitionRange = (String) partitionRow.get(0);
				}else{
					partitionRange = partitionRange + ":" + partitionRow.get(i);
				}
			}
			if (!partitionRange.equals(""))
					range.append("[" + partitionRange + "]");
		}
				
		return range.toString();
	}

	// arguments levelIndex and nodeWithoutRootPartition are mutually exclusive
	private String getPartialPartitionRange (Iterator partition, int levelIndex, String rangeWithNoRootPartition) {
		StringBuilder range = new StringBuilder();
		HashSet uniqueChecker = new HashSet(); 
		
		while(partition.hasNext()){
			ArrayList partitionRow = (ArrayList) partition.next();

			String partitionRange = "";
			int totalPartitionLevels = partitionRow.size();
			int partitionLevel = levelIndex < 0 ? totalPartitionLevels : 
									(levelIndex >= totalPartitionLevels ? totalPartitionLevels : levelIndex + 1);
			
			for(int i=0; i<partitionLevel; i++){
				if(i==0){
					partitionRange = (String) partitionRow.get(0);
					if (!rangeWithNoRootPartition.equals("")) {
						partitionRange = partitionRange + ":" + rangeWithNoRootPartition;
						break;
					}
				}else{
					partitionRange = partitionRange + ":" + partitionRow.get(i);
				}
			}
			
			if (!partitionRange.equals("")) {
				String toAdd = "[" + partitionRange + ":1]";
				if (!uniqueChecker.contains(toAdd)) {
					range.append(toAdd);
					uniqueChecker.add(toAdd);
				}
			}
		}
				
		return range.toString();
	}
}