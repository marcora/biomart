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
	static final String patternPartiDataset = "^((P\\d+).+\\2)_(.+)";
	static final String patternPartiTable = "(.+)_((P\\d+).+\\3)";

	// some private fields
	private StringBuilder metaInfoXML = new StringBuilder();
	
	private ArrayList dbTable = new ArrayList();
	private Map dbValidMartTable = new LinkedHashMap(); // {P1aaegyptiP1_egene__go_P7sequenceP7__dm={dataset=egene,content=go,type=dm,dsPt=P1aaegyptiP1,dtPt=P7sequenceP7},,,,}
	
	private TreeSet dbDataset = new TreeSet(); // values are dataset name with partition name removed if any; scan only main table names {gene_ensembl, marker_start, ,,}
	private HashMap dbPartitionRow = new HashMap(); // {gene_ensembl={hsap=12, mmus=15,,,,}, marker_start={hsap=1,,,}, gene_ensembl__go__dm={process=1,function=2,,,}, gene_ensembl__ox__dm={refseq=1,,,},, evoc__ontology__dm={pathology=1,,},}
	private ArrayList dbPartitionIndex = new ArrayList();  // [gene_ensembl, marker_start, ,,, gene_ensembl__go__dm, gene_ensembl__ox__dm,,,,]
	
	private Map dbDatasetMartTable = new HashMap(); //{gene_ensembl={gene__main,transcript__main,,,go__dm,ox__dm,pfeat__dm,,,}, marker_start={start__main},,, } # with partition portion (if any) removed
	
	private Map dbDatasetTablePartitionRange = new HashMap(); // {gene_ensembl__gene__main=[P1R1,P1R2,P1R5,P1R8,,,],gene_ensembl__go__dm=[P1R1:P8R1,P1R2:P8R1,,P1R1:P8R2,,,],,,}
	private Map dbDatasetTableColumnPartitionRange = new HashMap(); // {gene_ensembl__gene__main={id=[P1R1,P1R2,P1R5,P1R8,,,],name=[,,,]},gene_ensembl__go__dm={id=[,,,],,,}}
	
	/**
	 * 
	 */
	public MartMetaInfoHelper() {
		
	}


	/**
	 * @param dmd 
	 * @param schemaName 
	 * @return String
	 * @throws Exception 
	 */
	public String makeMetaInfoXML(DatabaseMetaData dmd, String schemaName) throws Exception {
		
		ResultSet tables = dmd.getTables(null, schemaName, null, null);
		
		while(tables.next()){
			String tableName = tables.getString(3);
			
			// skipping meta tables
			if (tableName.startsWith(metaTablePrefix)) continue;
			
			//if (tableName.contains("egene")) continue;  // TODO: remove this later. this is just for quickly testing a dataset without partition
			
			String [] tableNameDivisions = tableName.split(tableNameDivisionDelimiter);
			
			if (tableNameDivisions.length != 3 ||
					(!tableNameDivisions[2].equals("main") && !tableNameDivisions[2].equals("dm"))) {
				log.warning("table name (" + tableName + ") doesn't conform with naming convention, skipped.");
				continue;
			}
			dbTable.add(tableName);

			if (tableNameDivisions[2].equals("dm")) continue;  // deals with dm tables later

			log.info("processing main table: " + tableName);

			String dataset = tableNameDivisions[0];
			String datasetPartitionEntry = null;

			Pattern pattern = Pattern.compile(patternPartiDataset);
			Matcher matcher = pattern.matcher(dataset);

			if (matcher.find()){  // check if this is a partitioned dataset
				// System.out.println("Matched: " + matcher.group(0) + " " + matcher.group(1) + " " + matcher.group(2) + " " + matcher.group(3));
				dataset = matcher.group(3);  // dataset name with partition portion removed
				datasetPartitionEntry = matcher.group(1);
			}

			dbDataset.add(dataset);  // add dataset
			
			// populate dbPartitionRow when the dataset has partition
			if (datasetPartitionEntry != null && !datasetPartitionEntry.equals("")) {
				if (dbPartitionRow.containsKey(dataset)) {
					if (!((LinkedHashMap) dbPartitionRow.get(dataset))
							.containsKey(datasetPartitionEntry)) { // new partitonEntry
						int nextPos = ((LinkedHashMap) dbPartitionRow
								.get(dataset)).size() + 1;
						((LinkedHashMap) dbPartitionRow.get(dataset))
								.put(datasetPartitionEntry, new Integer(nextPos));
					}
				} else { // new dataset, of course with the first partitionEntry if there is any
					dbPartitionRow.put(dataset, new LinkedHashMap());
					((LinkedHashMap) dbPartitionRow.get(dataset)).put(datasetPartitionEntry, new Integer(1));
					dbPartitionIndex.add(dataset);
				}
			}
			
			if (!dbDatasetMartTable.containsKey(dataset))
				dbDatasetMartTable.put(dataset, new LinkedHashSet());
			
			((LinkedHashSet) dbDatasetMartTable.get(dataset)).add(tableNameDivisions[1] + tableNameDivisionDelimiter + "main");
			
			// populate dbValidMartTable
			dbValidMartTable.put(tableName, new HashMap());
			((HashMap)dbValidMartTable.get(tableName)).put("dataset", dataset);
			((HashMap)dbValidMartTable.get(tableName)).put("content", tableNameDivisions[1]);
			((HashMap)dbValidMartTable.get(tableName)).put("type", "main");
			((HashMap)dbValidMartTable.get(tableName))
					.put("martTableName", dataset + tableNameDivisionDelimiter + tableNameDivisions[1] + tableNameDivisionDelimiter + "main");
			if (datasetPartitionEntry != null && !datasetPartitionEntry.equals("")) 
					((HashMap)dbValidMartTable.get(tableName)).put("dsPt", datasetPartitionEntry);

		}
		tables.close();

		// now process dm tables
		for (int i=0; i<dbTable.size(); i++) {
			String tableName = (String) dbTable.get(i);
			
			String [] tableNameDivisions = tableName.split(tableNameDivisionDelimiter);
			
			if (tableNameDivisions[2].equals("main")) continue; // skip main tables which have been processed earlier.
			
			String dataset = tableNameDivisions[0];
			String datasetPartitionEntry = null;

			Pattern pattern = Pattern.compile(patternPartiDataset);
			Matcher matcher = pattern.matcher(dataset);
			
			if (matcher.find()){  // check if this is a partitioned dataset
				// System.out.println("Matched: " + matcher.group(0) + " " + matcher.group(1) + " " + matcher.group(2) + " " + matcher.group(3));
				dataset = matcher.group(3);  // dataset name with partition portion removed
				datasetPartitionEntry = matcher.group(1);
			}

			if (
					!dbDataset.contains(dataset) ||	
					((dbPartitionRow.containsKey(dataset)) && !((LinkedHashMap) dbPartitionRow.get(dataset)).containsKey(datasetPartitionEntry))
						) {  // dm table that has no main table, should warn this error
				log.warning("dimension table (" + tableName + ") doesn't have any associated main table, skipped.");
				continue;
			}

			log.info("processing dm table: " + tableName);

			String tablePartitionEntry = null;
			String content = tableNameDivisions[1];
			pattern = Pattern.compile(patternPartiTable);
			matcher = pattern.matcher(content);
			
			if (matcher.find()) {  // check if this is a partitioned dm table
				// System.out.println("Matched: " + matcher.group(0) + " " + matcher.group(1) + " " + matcher.group(2) + " " + matcher.group(3));
				content = matcher.group(1);  // with partition part removed
				tablePartitionEntry = matcher.group(2);
			}
			
			if (!dbDatasetMartTable.containsKey(dataset))
				dbDatasetMartTable.put(dataset, new LinkedHashSet());

			((LinkedHashSet) dbDatasetMartTable.get(dataset)).add(content + tableNameDivisionDelimiter + "dm");

			// populating dbPartitionRow: {gene_ensembl__go__dm={process=1,function=2,,,}, gene_ensembl__ox__dm={refseq=1,,,},, evoc__ontology__dm={pathology=1,,},}
			String martTableName = dataset + tableNameDivisionDelimiter + content + tableNameDivisionDelimiter + "dm";
			if (tablePartitionEntry != null && !tablePartitionEntry.equals("")) {
				if (dbPartitionRow.containsKey(martTableName)){
					if (!((LinkedHashMap) dbPartitionRow.get(martTableName))
							.containsKey(tablePartitionEntry)) { // new partitonEntry
						int nextPos = ((LinkedHashMap) dbPartitionRow
								.get(martTableName)).size() + 1;
						((LinkedHashMap) dbPartitionRow.get(martTableName))
								.put(tablePartitionEntry, new Integer(nextPos));
					}
				} else { // new partitioned dm table, of course with the first partitionEntry if there is any
					dbPartitionRow.put(martTableName, new LinkedHashMap());
					((LinkedHashMap) dbPartitionRow.get(martTableName))
							.put(tablePartitionEntry, new Integer(1));
					dbPartitionIndex.add(martTableName);
				}
			}

			// populate dbValidMartTable
			// {P1aaegyptiP1_egene__go_P7sequenceP7__dm={dataset=egene,content=go,type=dm,dsPt=P1aaegyptiP1,dtPt=P7sequenceP7},,,,}
			dbValidMartTable.put(tableName, new HashMap());
			((HashMap)dbValidMartTable.get(tableName)).put("dataset", dataset);
			((HashMap)dbValidMartTable.get(tableName)).put("content", content);
			((HashMap)dbValidMartTable.get(tableName)).put("type", "dm");
			((HashMap)dbValidMartTable.get(tableName))
					.put("martTableName", dataset + tableNameDivisionDelimiter + content + tableNameDivisionDelimiter + "dm");
			if (datasetPartitionEntry != null && !datasetPartitionEntry.equals(""))
				((HashMap)dbValidMartTable.get(tableName)).put("dsPt", datasetPartitionEntry);
			if (tablePartitionEntry != null && !tablePartitionEntry.equals(""))
				((HashMap)dbValidMartTable.get(tableName)).put("dtPt", tablePartitionEntry);

		}

		// iterate through all mart tables to populate dbDatasetTablePartitionRange and dbDatasetTableColumnPartitionRange 
		log.info("processing table columns, this may take a few minutes ...");
		Iterator iterator = dbValidMartTable.keySet().iterator();
		while(iterator.hasNext()){
			String dbTableName = (String) iterator.next();
			HashMap martTableInfo = (HashMap) dbValidMartTable.get(dbTableName);
			String dataset = (String) martTableInfo.get("dataset");
			String content = (String) martTableInfo.get("content");
			String type = (String) martTableInfo.get("type");
			String dsPt = (String) martTableInfo.get("dsPt");
			String dtPt = (String) martTableInfo.get("dtPt");
			String martTableName = (String) martTableInfo.get("martTableName");
			
			//System.out.println(dataset + " " + content + " " + type + " " + dsPt + " " + dtPt);
			
			String partitionRange = null;
			
			if (dsPt != null && dtPt != null) {
				String dsP =  dbPartitionIndex.indexOf(dataset) + 1 + "";
				String dsPR = ((Integer) ((LinkedHashMap) dbPartitionRow.get(dataset)).get(dsPt)).toString();
				String dtP =  dbPartitionIndex.indexOf(martTableName) + 1 + "";
				String dtPR = ((Integer) ((LinkedHashMap) dbPartitionRow.get(martTableName)).get(dtPt)).toString();
				partitionRange = "P" + dsP + "R" + dsPR + ":" + "P" + dtP + "R" + dtPR;
			} else if (dsPt != null) {
				String dsP =  dbPartitionIndex.indexOf(dataset) + 1 + "";
				String dsPR = ((Integer) ((LinkedHashMap) dbPartitionRow.get(dataset)).get(dsPt)).toString();
				partitionRange = "P" + dsP + "R" + dsPR;
			} else if (dtPt != null) {
				String dtP =  dbPartitionIndex.indexOf(martTableName) + 1 + "";
				String dtPR = ((Integer) ((LinkedHashMap) dbPartitionRow.get(martTableName)).get(dtPt)).toString();
				partitionRange = "P" + dtP + "R" + dtPR;
			}
			
			// dbDatasetTablePartitionRange: {gene_ensembl__gene__main=[P1R1,P1R2,P1R5,P1R8,,,],gene_ensembl__go__dm=[P1R1:P8R1,P1R2:P8R1,,P1R1:P8R2,,,],,,}
			if (!dbDatasetTablePartitionRange.containsKey(martTableName))
					dbDatasetTablePartitionRange.put(martTableName, new ArrayList());
			if (partitionRange != null)
					((ArrayList) dbDatasetTablePartitionRange.get(martTableName)).add(partitionRange);
			
			///* temporarily comment out this time consuming process for faster debugging
			// dbDatasetTableColumnPartitionRange: {gene_ensembl__gene__main={id=[P1R1,P1R2,P1R5,P1R8,,,],name=[,,,]},gene_ensembl__go__dm={id=[,,,],,,}}
			ResultSet columns = dmd.getColumns(null, schemaName, dbTableName, null);
			while(columns.next()){
				String columnName = columns.getString(4);
				
				if (skipBooleanCol && columnName.endsWith(booleanColumnSuffix)) continue;
				
				if (!dbDatasetTableColumnPartitionRange.containsKey(martTableName)) {  // new table, new column
					dbDatasetTableColumnPartitionRange.put(martTableName, new LinkedHashMap());
					((LinkedHashMap) dbDatasetTableColumnPartitionRange.get(martTableName))
							.put(columnName, new ArrayList());
				} else {  // exist table, new column
					if (!((LinkedHashMap) dbDatasetTableColumnPartitionRange
							.get(martTableName)).containsKey(columnName)) {  // exist table, new column
						((LinkedHashMap) dbDatasetTableColumnPartitionRange.get(martTableName)).put(columnName, new ArrayList());
					}
				}
				if (partitionRange != null)
						((ArrayList) ((LinkedHashMap) dbDatasetTableColumnPartitionRange
							.get(martTableName)).get(columnName)).add(partitionRange);
			}

		}

		// output partitions
		log.info("generating partition information ...");
		outputPartitionInf();
		
		// output datasets
		log.info("generating dataset information ...");
		outputDatasetInf();

		// print for debugging
//		System.out.println("dbDataset=" + dbDataset);
//		System.out.println("dbPartitionIndex=" + dbPartitionIndex);
//		System.out.println("dbPartitionRow=" + dbPartitionRow);
//		System.out.println("dbDatasetMartTable=" + dbDatasetMartTable);
//		System.out.println("dbDatasetTablePartitionRange=" + dbDatasetTablePartitionRange);
//		System.out.println("dbDatasetTableColumnPartitionRange=" + dbDatasetTableColumnPartitionRange);
		
		if (metaInfoXML.toString().length() == 0) return null;
		log.info("metaInfoXML generated");
		return metaInfoXML.toString();
	}



	private void outputPartitionInf() {
		
		for (int i = 0; i < dbPartitionIndex.size(); i++) {
			metaInfoXML.append("<partitionTable name=\"P" + (i + 1) + "\" rows=\"\" cols=\"\">\n");
			Iterator it = ((LinkedHashMap) dbPartitionRow.get(dbPartitionIndex.get(i))).keySet().iterator();
			while(it.hasNext()) {
				String partitionEntry = (String) it.next();
				String rowNumber = ((LinkedHashMap) dbPartitionRow.get(dbPartitionIndex.get(i))).get(partitionEntry).toString();
				metaInfoXML.append("\t<cell row=\"" + rowNumber + "\" " + "col=\"1\">" + partitionEntry + "</cell>\n");
			}
			metaInfoXML.append("</partitionTable>\n");
		}

	}


	private void outputDatasetInf() {

		Iterator i = dbDataset.iterator();
		while (i.hasNext()) {
			String dataset = (String) i.next();
			
			metaInfoXML.append("<dataset name=\"" + dataset + "\">\n");

			// output tables
			outputTableInfo(dataset);
			
			// output config
			outputConfigInfo(dataset);
			
			metaInfoXML.append("</dataset>\n");
		}

		
	}


	private void outputTableInfo(String dataset) {

		String myDataset = dataset;

		if (dbPartitionIndex.indexOf(dataset) >= 0)   // check if this is a partitioned dataset
			myDataset = "(P" + (dbPartitionIndex.indexOf(dataset) + 1) + "C1)_"+ dataset;
		
		Iterator i = ((LinkedHashSet) dbDatasetMartTable.get(dataset)).iterator();
		while(i.hasNext()) {
			String [] tempStr = ((String) i.next()).split(tableNameDivisionDelimiter);;
			
			String table = tempStr[0];
			String tableType = tempStr[1];
			String martTableName = dataset + tableNameDivisionDelimiter + table + tableNameDivisionDelimiter + tableType;

			String myTable = table;
			if (tableType.equals("dm") && dbPartitionIndex.indexOf(martTableName) >= 0)  // check if this is a partitioned dm table
				myTable = table + "_(P" + (dbPartitionIndex.indexOf(martTableName) + 1) + "C1)";
			
			metaInfoXML.append("\t<table name=\"" + myDataset + tableNameDivisionDelimiter + myTable + tableNameDivisionDelimiter + tableType + "\"");
			metaInfoXML.append(" key=\"\"");
			String dtRange = "";
			if (((ArrayList)dbDatasetTablePartitionRange.get(martTableName)).size() > 0) {
				StringBuilder sb = new StringBuilder();
				for (int j = 0; j < ((ArrayList)dbDatasetTablePartitionRange.get(martTableName)).size(); j++) {
					sb.append("[" + ((ArrayList)dbDatasetTablePartitionRange.get(martTableName)).get(j) + "]");
				}
				dtRange = sb.toString();
			}
			metaInfoXML.append(" range=\"" + dtRange + "\"");
			metaInfoXML.append(">\n");
			
			// output attributes
			Iterator j = ((LinkedHashMap) dbDatasetTableColumnPartitionRange
					.get(martTableName)).keySet().iterator();
			while(j.hasNext()){
				String columnName = (String) j.next();
				metaInfoXML.append("\t\t<attribute>" + columnName + "</attribute>\n");
			}

			metaInfoXML.append("\t</table>\n");

		}
		
		
	}

	private void outputConfigInfo(String dataset) {
		metaInfoXML.append("\t<config name=\"naive\" dataset=\"" + dataset + "\">\n");
		
		// output template config
		outputConfInfo(dataset, "templateConfig");
		
		// output user config TODO: do we really need to generate user config for naive?
		//outputConfInfo(dataset, "userConfig");
		
		metaInfoXML.append("\t</config>\n");
	}

	private void outputConfInfo(String dataset, String confType) {
		
		// opening config
		metaInfoXML.append("\t\t<" + confType + " name=\"" + confType + "\">\n");
	
		// first dataset level container
		String partitionIndex = "P" + (dbPartitionIndex.indexOf(dataset) + 1);
		String dsRange = "";
		
		if (partitionIndex.equals("P0")) {  // not a partitioned dataset
			partitionIndex = dataset;
		} else {
			StringBuilder sb = new StringBuilder();
			for (int rnum = 1; rnum <= ((HashMap) dbPartitionRow.get(dataset)).size(); rnum++) {
				sb.append("[" + partitionIndex + "R" + rnum + ":1]");
			}
			dsRange = sb.toString();
		}
		
		// opening dataset level container
		metaInfoXML.append("\t\t\t<container name=\"" + partitionIndex + "\" queryRestriction=\"0\" range=\"" + dsRange + "\">\n");
		
		outputTableLevelContainer(dataset, confType);
		
		// closing dataset level container
		metaInfoXML.append("\t\t\t</container>\n");
		
		// closing config
		metaInfoXML.append("\t\t</" + confType + ">\n");
		
	}

	private void outputTableLevelContainer (String dataset, String confType) {
		String myDataset = dataset;
		String xmlLeadingTab = "\t\t\t\t";
		String extraTab = "";
		
		if (dbPartitionIndex.indexOf(dataset) >= 0)   // check if this is a partitioned dataset
			myDataset = "(P" + (dbPartitionIndex.indexOf(dataset) + 1) + "C1)_"+ dataset;
		
		Iterator i = ((LinkedHashSet) dbDatasetMartTable.get(dataset)).iterator();
		while(i.hasNext()) {
			String [] tempStr = ((String) i.next()).split(tableNameDivisionDelimiter);;
			
			String table = tempStr[0];
			String tableType = tempStr[1];
			String martTableName = dataset + tableNameDivisionDelimiter + table + tableNameDivisionDelimiter + tableType;

			String dtRange = "";
			if (((ArrayList)dbDatasetTablePartitionRange.get(martTableName)).size() > 0) {
				StringBuilder sb = new StringBuilder();
				for (int j = 0; j < ((ArrayList)dbDatasetTablePartitionRange.get(martTableName)).size(); j++) {
					sb.append("[" + ((ArrayList)dbDatasetTablePartitionRange.get(martTableName)).get(j) + ":1]");
				}
				dtRange = sb.toString();
			}

			String myTable = table;
			if (tableType.equals("dm") && dbPartitionIndex.indexOf(martTableName) >= 0) { // check if this is a partitioned dm table
				metaInfoXML.append(xmlLeadingTab + "<container name=\"P" + (dbPartitionIndex.indexOf(martTableName) + 1) + "\"");
				metaInfoXML.append(" queryRestriction=\"0\"");
				metaInfoXML.append(" range=\"" + dtRange + "\"");
				metaInfoXML.append(">\n");
				myTable = table + "_(P" + (dbPartitionIndex.indexOf(martTableName) + 1) + "C1)";
				extraTab = "\t";
			}

			// opening table level container
			metaInfoXML.append(xmlLeadingTab + extraTab + "<container name=\"" + myDataset + tableNameDivisionDelimiter + myTable + tableNameDivisionDelimiter + tableType + "\"");
			metaInfoXML.append(" queryRestriction=\"0\"");
			metaInfoXML.append(" range=\"" + dtRange + "\"");
			metaInfoXML.append(">\n");
			
			// output attributes
			Iterator j = ((LinkedHashMap) dbDatasetTableColumnPartitionRange
					.get(martTableName)).keySet().iterator();
			while(j.hasNext()){
				String columnName = (String) j.next();
				StringBuilder sb = new StringBuilder();
				for (int k=0; k < ((ArrayList) ((HashMap) dbDatasetTableColumnPartitionRange.get(martTableName)).get(columnName)).size(); k++) {
					sb.append("[" + ((ArrayList) ((HashMap) dbDatasetTableColumnPartitionRange.get(martTableName)).get(columnName)).get(k) + ":1]");
				}
				String tcRange = sb.toString();

				metaInfoXML.append(xmlLeadingTab + extraTab + "\t<attributePointer name=\"" + columnName + "\"\n");
				metaInfoXML.append(xmlLeadingTab + extraTab + "\t\tpoint=\"false\" field=\"" + columnName + "\" location=\"" + "\"\n");
				metaInfoXML.append(xmlLeadingTab + extraTab + "\t\tmart=\"\" version=\"\" dataset=\"" + dataset + "\" config=\"naive\"\n");
				metaInfoXML.append(xmlLeadingTab + extraTab + "\t\ttable=\"" + myDataset + tableNameDivisionDelimiter + myTable + tableNameDivisionDelimiter + tableType + "\" sourceRange=\"\"\n");
				metaInfoXML.append(xmlLeadingTab + extraTab + "\t\trange=\"" + tcRange + "\">\n");
				metaInfoXML.append(xmlLeadingTab + extraTab + "\t\t<displayName value=\"" + columnName + " + SOMETHING\" />\n");
				metaInfoXML.append(xmlLeadingTab + extraTab + "\t\t<default value=\"1\" />\n");
				
				metaInfoXML.append(xmlLeadingTab + extraTab + "\t</attributePointer>\n");
			}

			// closing table level container
			metaInfoXML.append(xmlLeadingTab + extraTab + "</container>\n");
			if (!myTable.equals(table)) { // this is a partitioned dm table 
				metaInfoXML.append(xmlLeadingTab + "</container>\n");
				extraTab = "";
			}
			
		}
	}
	
}
