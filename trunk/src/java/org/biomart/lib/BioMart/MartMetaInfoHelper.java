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
	
	private Map partitionTable = new TreeMap(); // partitionTable
	private Map dbDataset = new LinkedHashMap(); // {(P1C1)_egene=[[P1R1],[P1R2], (P1C1)_(P8C1)_homology=[[P1R1,P8R1],[P1R1,P8R2],, ],,,}
	private Map dbDatasetTable = new HashMap(); // {(P1C1)_egene={(P1C1)_egene__gene__main=[[P1R1],[P1R2],,],,},,}
	private Map dbTableColumn = new HashMap(); // {(P1C1)_egene={(P1C1)_egene__gene__main={id=[[P1R1],[P1R2],,],,}},,,}
	
	
	/**
	 *  empty constructor 
	 */
	public MartMetaInfoHelper() {
		
	}


	/**
	 * @param dmd 
	 * @param schemaName 
	 * @param locationName 
	 * @param martName 
	 * @return String
	 * @throws Exception
	 * TODO: to add validation for all necessary dataset components 
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
		
		// processing tables (it's guaranteed that main tables come before dm tables 
		Iterator tableIt = dbTable.iterator();
		while(tableIt.hasNext()){
			String tableName = (String) tableIt.next();
			
			// skipping meta tables
			if (tableName.startsWith(metaTablePrefix)) continue;
			
			//if (!tableName.contains("egene")) continue;  // TODO: remove this later. this is just for a quicker testing
			
			String [] tableNameDivisions = tableName.split(tableNameDivisionDelimiter);
			
			if (tableNameDivisions.length != 3 ||
					(!tableNameDivisions[2].equals("main") && !tableNameDivisions[2].equals("dm"))) {
				log.warning("table name '" + tableName + "' doesn't conform with naming convention, skipped.");
				continue;
			}

			log.info("processing table: " + tableName);

			String dataset = tableNameDivisions[0];
			ArrayList partitionRange = new ArrayList();

			Map partitionInfoDS = partitionParser(partitionPatternDS, dataset);
			
			// populating partitionTable and looking up partitionRow
			if (partitionInfoDS.containsKey("partition")) {
				dataset = (String) partitionInfoDS.get("partitionedName");
				if (tableNameDivisions[2].equals("main")) {  // main table, populate partitionTable
					partitionRange = partitionLookup((ArrayList) partitionInfoDS.get("partition"), false);
				} else {  // dm table, lookup in partitionTable to validate dm table
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
			
				((Set) dbDataset.get(dataset)).add(partitionRange);  // add the partitionRange
			} else { // dm table, validate it
				if (!dbDataset.containsKey(dataset)) { // if it's a non-exist dataset
					log.warning("dimenssion table '" + tableName + "' does not have any associated main table, skipping the dm table, skipping creating dataset '" + dataset + "'!");
					continue;
				}
			}
			
			// populate dbDatasetTable and dbTableColumn
			String content = tableNameDivisions[1];

			Map partitionInfoDM = partitionParser(partitionPatternDM, content);
			
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
			
			if (!((Map) dbDatasetTable.get(dataset)).containsKey(dbMartTableName)) { // it's a new table
				((Map) dbDatasetTable.get(dataset)).put(dbMartTableName, new LinkedHashSet());  // add the new table with it's value being an empty LinkedHashSet
				((Map) dbTableColumn.get(dataset)).put(dbMartTableName, new LinkedHashMap());  // add the new table with it's value being an empty LinkedHashMap for columns
			}
			
			((Set)((Map) dbDatasetTable.get(dataset)).get(dbMartTableName)).add(partitionRange);

			// scan table columns and populate dbTableColumn
			ResultSet columns = dmd.getColumns(null, schemaName, tableName, null);
			while(columns.next()){
				String columnName = columns.getString(4);

				if (skipBooleanCol && columnName.endsWith(booleanColumnSuffix)) continue;
                    
				if (!((Map) ((Map) dbTableColumn.get(dataset)).get(dbMartTableName)).containsKey(columnName))
					((Map) ((Map) dbTableColumn.get(dataset)).get(dbMartTableName)).put(columnName, new LinkedHashSet());

				((Set) ((Map) ((Map) dbTableColumn.get(dataset))
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


	private Map partitionParser (Pattern pattern, String fullname){
		Map partitionInfo = new HashMap();
		partitionInfo.put("name", fullname);
		
		ArrayList partition = new ArrayList();
		String partitionString = "";
		String partitionTemplate = "";
		
		Matcher m = pattern.matcher(fullname);
		while (m.find()) {
			if (m.group(2).equals(m.group(4))) {
				// partition is a ArrayList of a String array
				partition.add(new String [] {m.group(2), m.group(1)}); //for P1hsapP1 m.group(2)="P1", m.group(1)="hsap"
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
			String name = m.replaceAll(""); // remove partition tags from fullname
			if (fullname.startsWith(name) && fullname.equals(name + "_" + partitionString)) { // for dm partition; fullname can be reconstructed from partitionString
				partitionInfo.put("name", name);
				partitionInfo.put("partitionedName", name + "_" + partitionTemplate);
				partitionInfo.put("partition", partition);
			}else if (fullname.endsWith(name) && fullname.equals(partitionString + "_" + name)) { // for dataset partition; fullname can be reconstructed from partitionString
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
			
			if (!((Set) partitionTable.get(partitionData[0])).contains(partitionData[1])) {  // new partitionRow
				if (lookupOnly) return null;  // if it's lookupOnly, we can return null right away
				((Set) partitionTable.get(partitionData[0])).add(partitionData[1]);  // add it
			}
			
			// now find out the partitionRow
			ArrayList partitionRows = new ArrayList((Set) partitionTable.get(partitionData[0]));
			
			partitionRange.add(partitionData[0] + "R" + (partitionRows.indexOf(partitionData[1]) + 1));
		}
		
		return partitionRange;
	}
	
	private void outputPartitionInfo() {
		Iterator pI = partitionTable.keySet().iterator();
		while(pI.hasNext()){
			String partitionName = (String) pI.next();
			metaInfoXML.append("<partitionTable name=\"" + partitionName + "\">\n");
			Iterator prI = ((Set) partitionTable.get(partitionName)).iterator();
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
			Iterator pI = ((Set) dbDataset.get(datasetName)).iterator();
			String range = getPartitionRangeString(pI, false);

			metaInfoXML.append("<dataset name=\"" + datasetName 
								+ "\" range=\"" + range + "\">\n");
			// outputTable
			outputTableInfo((Map) dbDatasetTable.get(datasetName), datasetName);
			
			// outputConfig
			outputConfig(datasetName);
			
			metaInfoXML.append("</dataset>\n");
		}
	}

	private void outputConfig(String datasetName){
		// consolidate dbDataset and dbDatasetTable to build skeleton configuration
		Map skeletonConfig = skeletonConfigBuilder(datasetName);
		
		metaInfoXML.append("\t<config name=\"naive\" dataset=\"" + datasetName + "\">\n");
		// output templateConfig
		outputTemplateConfig(datasetName, skeletonConfig);
		// output userConfig
		outputUserConfig(datasetName, skeletonConfig);
		metaInfoXML.append("\t</config>\n");
	}

	
	private void outputTemplateConfig(String datasetName, Map skeletonConfig) {
		metaInfoXML.append("\t\t<templateConfig name=\"templateConfig\">\n");
		
		metaInfoXML.append(outputContainer(datasetName, skeletonConfig, 0, null));
		
		metaInfoXML.append("\t\t</templateConfig>\n");
	}

	private void outputUserConfig(String datasetName, Map skeletonConfig) {
		metaInfoXML.append("\t\t<userConfig name=\"userConfig\">\n");
		
		metaInfoXML.append(outputContainer(datasetName, skeletonConfig, 0, ""));
		
		metaInfoXML.append("\t\t</userConfig>\n");		
	}

	private String outputContainer(String datasetName, Map container, int level, String partitionPathNoRootLevel) {
		StringBuilder sbOuterCont = new StringBuilder();
		boolean noOuterOutput = true;
		Iterator cIt = container.keySet().iterator();
		while(cIt.hasNext()){
			String containerFullName = (String) cIt.next();
			
			String [] containerFullNameParts = containerFullName.split("\\.");
			
			String [] containerPath = containerFullNameParts[0].split(":");
			
			char [] tab = new char [level + 3];
			Arrays.fill(tab, '\t');
			String xmlLeadingTab = new String(tab);
			
			String outputContainerName = containerFullName;
			//String outputContainerName = containerPath[containerPath.length-1]; // we may choose this as output container name

			// Start: getting container partition range without
			Set partitionRange = new LinkedHashSet();
			if (level == 0 && containerFullNameParts.length == 1) { // root level container and non-partitioned dataset
				partitionRange.add("");
			} else if (level != 0 && containerFullNameParts.length == 1) { // this will never happen, this is kept for completeness
				// never happens
			} else { // up to here containerNameParts.length is always greater than 1, ie, we have partitions
				boolean isDatasetPartitioned = true;
				Iterator pI;
				if (!containerFullNameParts[1].contains(tableNameDivisionDelimiter)) { // dataset level partition container
					pI = ((Set) dbDataset.get(datasetName)).iterator();
				} else { // table level partition container
					// check if isDatasetPartitioned first?
					if (!datasetName.contains("(")) isDatasetPartitioned = false;
					pI = ((Set) ((Map) dbDatasetTable.get(datasetName)).get(containerFullNameParts[1])).iterator();
				}
				
				partitionRange = getPartionRangeByLevel (pI, level, partitionPathNoRootLevel, isDatasetPartitioned);
				
			}
			// End: getting container partition range

			//System.out.println(partitionRange);
			Iterator pRI = partitionRange.iterator();
			while(pRI.hasNext()){
				StringBuilder sbContainer = new StringBuilder();
				boolean noOutput = true;
				String range = (String) pRI.next();
				// START outputing partition containers
				sbContainer.append(xmlLeadingTab + "<container name=\"" + outputContainerName + "\" range=\"" + range + "\">\n");
			
				// START outputing tables
				String nameWithPartition = containerFullNameParts[containerFullNameParts.length - 1];
				List partition = getPartitionsByPartitionedName(nameWithPartition);
			
				// under the following two conditions we are ready to output non-partitioned tables
				if (containerFullNameParts.length == 1  // non-partitioned dataset
						|| (partition.size() == containerPath.length && !nameWithPartition.contains(tableNameDivisionDelimiter))) {  // this container is for the last level partition of a dataset

					Iterator tIt = ((Map) dbDatasetTable.get(datasetName)).keySet().iterator();
					while (tIt.hasNext()) {
						String tableName = (String) tIt.next();
						String [] nameParts = tableName.split(tableNameDivisionDelimiter);
						if (nameParts[1].contains("(")) continue;  //skip partitioned tables
			
						String tableString = outputTableContainer(xmlLeadingTab, datasetName, tableName, partitionPathNoRootLevel, range);
						if (!tableString.equals("")) {
							sbContainer.append(tableString);
							noOutput = false;
						}
					}
				
				// under the following condition we are ready to output partitioned tables
				}else if (partition.size() == containerPath.length) {  // this container is for the last level partition of a partitioned table
					String tableString = outputTableContainer(xmlLeadingTab, datasetName, nameWithPartition, partitionPathNoRootLevel, range);
					if (!tableString.equals("")) {
						sbContainer.append(tableString);
						noOutput = false;
					}
				}
				// END outputing tables
			
				if (((Map)container.get(containerFullName)).size() > 0) {
					if (partitionPathNoRootLevel != null && level > 0) { // we have to update partitionPathNoRootLevel if it's not null, we only do it when it's non-root level (ie, level > 0)
						String firstPR = range.split(":1\\]")[0];
						firstPR = firstPR.substring(1);
						if (!datasetName.contains("(")) { // dataset is not partitioned, don't need to remove the root level partition entry
							partitionPathNoRootLevel = firstPR;
						} else { // dataset is partitioned
							Pattern p = Pattern.compile("^P\\d+R\\d+:", Pattern.CASE_INSENSITIVE);
							Matcher m = p.matcher(firstPR);
							partitionPathNoRootLevel = m.replaceAll("");
						}
					}
					String containerString = outputContainer(datasetName, (Map)container.get(containerFullName), level+1, partitionPathNoRootLevel);
					if (!containerString.equals("")) {
						sbContainer.append(containerString);
						noOutput = false;
					}

				}
			
				sbContainer.append(xmlLeadingTab + "</container>\n");
				// END outputing partition containers
				if (!noOutput) {
					sbOuterCont.append(sbContainer.toString());
					noOuterOutput = false;
				}

			}
			
		}
		
		if (noOuterOutput) {
			return "";
		} else {
			return sbOuterCont.toString();
		}
		
	}

	private String outputTableContainer(String xmlLeadingTab, String datasetName, String tableName, String partitionPathNoRootLevel, String tableContainerRange) {
		StringBuilder sbTable = new StringBuilder();
		boolean noOutputColumn = true;
		sbTable.append(xmlLeadingTab + "\t<container name=\"" + tableName + "\" range=\"" + tableContainerRange + "\">\n");
		Iterator tcI = ((Map) ((Map) dbTableColumn.get(datasetName)).get(tableName)).keySet().iterator();
		while(tcI.hasNext()){
			String columnName = (String) tcI.next();
			Iterator tcpI = ((Set) ((Map) ((Map) dbTableColumn.get(datasetName)).get(tableName)).get(columnName)).iterator();
			
			String columnPartitionRange = getPartitionRangeString(tcpI, true);
			if (partitionPathNoRootLevel != null) { // userConfig, we have to filter column range by looking up to table range
				columnPartitionRange = getCommonRange(tableContainerRange, columnPartitionRange);
			}
			
			if (columnPartitionRange == null && !tableContainerRange.equals(""))
				continue;  // skip if container has range but this column is not in it's range (empty string)

			if (columnPartitionRange == null && tableContainerRange.equals(""))
				columnPartitionRange = "";  // skip if container has range but this column is not in it's range (empty string)
			
			sbTable.append(xmlLeadingTab + "\t\t<attributePointer name=\"" + columnName + "\"\n");
			sbTable.append(xmlLeadingTab + "\t\t\tpointer=\"false\" field=\"" + columnName + "\" location=\"" + locationName + "\"\n");
            sbTable.append(xmlLeadingTab + "\t\t\tmart=\"" + martName + "\" version=\"\" dataset=\"" + datasetName + "\" config=\"naive\"\n");
            sbTable.append(xmlLeadingTab + "\t\t\ttable=\"" + tableName + "\" sourceRange=\"\"\n");
			sbTable.append(xmlLeadingTab + "\t\t\trange=\"" + columnPartitionRange + "\">\n");
			sbTable.append(xmlLeadingTab + "\t\t\t<displayName value=\"" + columnName + " + SOMETHING\" />\n");
			sbTable.append(xmlLeadingTab + "\t\t</attributePointer>\n");
			noOutputColumn = false;
		}
		sbTable.append(xmlLeadingTab + "\t</container>\n");

		if (noOutputColumn) {
			return "";
		} else {
			return sbTable.toString();
		}
	}
	
	// method to find out common partition range
	// partitionRange format: [P1R1:P2R1:1][P1R2:P2R1:1]
	private String getCommonRange(String partitionRange1, String partitionRange2) {
		if (partitionRange1.equals("") && partitionRange2.equals(""))
			return "";
		
		if (partitionRange1 == null || partitionRange2 == null
				|| partitionRange1.equals("") || partitionRange2.equals(""))
			return null;
		
		partitionRange1 = partitionRange1.substring(1, partitionRange1.length()-1);	// first let's remove first "[" and last "]"
		String [] pRange1 = partitionRange1.split("\\]\\[");
		Set partition1 = new HashSet();
		for (int i=0; i<pRange1.length; i++){
			partition1.add(pRange1[i]);
		}

		partitionRange2 = partitionRange2.substring(1, partitionRange2.length()-1);	// first let's remove first "[" and last "]"
		String [] pRange2 = partitionRange2.split("\\]\\[");

		StringBuilder sb = new StringBuilder();
		for (int i=0; i<pRange2.length; i++){
			if (partition1.contains(pRange2[i]))
				sb.append("[" + pRange2[i] + "]");
		}
		
		if (sb.toString().equals("")){
			return null;
		} else {
			return sb.toString();
		}
	}
	
	// this method builds skeletonConfig with all level of containers, including partitions in dataset and table levels
	private Map skeletonConfigBuilder (String datasetName){
		Map skeletonConfig = new LinkedHashMap();
		
		// get partitions for dataset
		List datasetPartition = getPartitionsByPartitionedName(datasetName);
		Map currentNode = skeletonConfig;
		String containerPath = "";

		if (datasetPartition.size() == 0){ // non-partition dataset
			currentNode.put(datasetName, new LinkedHashMap());
			currentNode = (Map) currentNode.get(datasetName);
		}else{ // partitioned dataset
			for(int i=0; i<datasetPartition.size(); i++){
				if (containerPath.equals("")) {
					containerPath = (String) datasetPartition.get(i);
				}else{
					containerPath = containerPath + ":" + datasetPartition.get(i);
				}
				
				currentNode.put(containerPath + "." + datasetName, new LinkedHashMap());
				currentNode = (Map) currentNode.get(containerPath + "." + datasetName);
			}
		}
		
		// now continue to populate skeletonConfig with containers by partitioned tables 
		Iterator tIt = ((Map) dbDatasetTable.get(datasetName)).keySet().iterator();
		while(tIt.hasNext()){
			String tableName = (String) tIt.next();
			
			String [] tableNameDivisions = tableName.split(tableNameDivisionDelimiter);
			if(!tableNameDivisions[1].contains("(")) continue;  // skip non-partitioned table
			
			Map myCurrentNode = currentNode;
			String myContainerPath = containerPath;
			List tablePartition = getPartitionsByPartitionedName(tableNameDivisions[1]);
			
			for (int i=0; i<tablePartition.size(); i++) {
				if (myContainerPath.equals("")) {
					myContainerPath = (String) tablePartition.get(i);
				}else{
					myContainerPath = myContainerPath + ":" + tablePartition.get(i);
				}
				
				myCurrentNode.put(myContainerPath + "." + tableName, new LinkedHashMap());
				myCurrentNode = (Map) currentNode.get(myContainerPath + "." + tableName);
				
			}
		}
		
		//System.out.println("skeletonConfig{" + datasetName + "}="+ skeletonConfig);
		return skeletonConfig;
	}
	
	// this method parses name of a dataset or a table, and return partition names (if there are any) in an ArrayList
	private List getPartitionsByPartitionedName (String name) {
		List partition = new ArrayList();
		
		Pattern p = Pattern.compile("\\((p\\d+?)C1\\)_", Pattern.CASE_INSENSITIVE);
		Matcher m = p.matcher("_" + name + "_"); // adding flanking "_" to make sure pattern is matched when there is any partition(s)

		while (m.find()) {
		    partition.add(m.group(1));
		}
		
		return partition;
	}
	
	
	private void outputTableInfo(Map martTables, String datasetName) {
		Iterator tI = (martTables).keySet().iterator();
		while(tI.hasNext()){
			String tableName = (String) tI.next();
			Iterator pI = ((Set) martTables.get(tableName)).iterator();
			String range = getPartitionRangeString(pI, false);

			metaInfoXML.append("\t<table name=\"" + tableName 
					+ "\" range=\"" + range + "\">\n");
			outputColumnInfo((Map) ((Map) dbTableColumn.get(datasetName)).get(tableName));
			metaInfoXML.append("\t</table>\n");
		}
	}
	
	private void outputColumnInfo(Map martTableColumns) {
		Iterator tI = (martTableColumns).keySet().iterator();
		while(tI.hasNext()){
			String columnName = (String) tI.next();
			Iterator pI = ((Set) martTableColumns.get(columnName)).iterator();
			String range = getPartitionRangeString(pI, false);

			metaInfoXML.append("\t\t<attribute name=\"" + columnName 
					+ "\" range=\"" + range + "\"/>\n");
		}
	}
	
	private String getPartitionRangeString (Iterator partition, boolean extraTag) {
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
					range.append("[" + partitionRange + (extraTag ? ":1" : "") + "]");
		}
				
		return range.toString();
	}

	private Set getPartionRangeByLevel (Iterator partition, int level, String partitionPathNoRootLevel, boolean isDatasetPartitioned) {
		Set partitionRange = new LinkedHashSet ();
		
		if (level == 0 && !isDatasetPartitioned) {
			partitionRange.add("");
			return partitionRange;
		}
		
		int levelIndex = level;
		if (!isDatasetPartitioned) levelIndex = level - 1;
		
		if (level == 0 || partitionPathNoRootLevel == null) { // return collapsed range
			while(partition.hasNext()) {
				ArrayList partitionRow = (ArrayList) partition.next();

				String partitionString = "";
				for(int i=0; i <= levelIndex; i++){
					if(partitionString.equals("")){
						partitionString = (String) partitionRow.get(i);
					}else{
						partitionString = partitionString + ":" + (String) partitionRow.get(i);
					}
				}
				
				partitionRange.add("[" + partitionString + ":1]");
			}

			Iterator pRI = partitionRange.iterator();
			StringBuilder sb = new StringBuilder();
			while(pRI.hasNext()){
				sb.append((String) pRI.next());
			}
			partitionRange.clear();
			partitionRange.add(sb.toString());

		} else { // return partially expended range, root level is still collapsed
			Map myLevelPartition = new LinkedHashMap();
			while(partition.hasNext()) {
				ArrayList partitionRow = (ArrayList) partition.next();

				String partitionString = "";

				if (levelIndex == 0) { // partitioned dm table in a non-partitioned dataset
					partitionString = (level == 1 ? "" : partitionPathNoRootLevel + ":")
											+ (String) partitionRow.get(levelIndex);
				} else {
					partitionString = (String) partitionRow.get(0)
											+ (level == 1 ? "" : ":" + partitionPathNoRootLevel)
											+ ":" + (String) partitionRow.get(levelIndex);
				}

				if (!myLevelPartition.containsKey(partitionRow.get(levelIndex)))
						myLevelPartition.put(partitionRow.get(levelIndex), new LinkedHashSet());
					
				((Set) myLevelPartition.get(partitionRow.get(levelIndex))).add(partitionString);
					
			}
			
			Iterator myPI = myLevelPartition.keySet().iterator();
			while(myPI.hasNext()){
				String partitionString = "";
				Iterator myLevelsI =  ((Set) myLevelPartition.get((String) myPI.next())).iterator();
				while(myLevelsI.hasNext()){
					partitionString = partitionString + "[" + myLevelsI.next() + ":1]";
				}
				
				partitionRange.add(partitionString);
			}
						
		}
		
		return partitionRange;
	}

}