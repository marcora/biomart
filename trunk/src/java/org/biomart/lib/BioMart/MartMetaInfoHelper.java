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
	static final String patternPartiDataset = "^((P\\d+).+\\2)_(.+)";
	static final String patternPartiTable = "(.+)_((P\\d+).+\\3)";

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
		
		// some variables
		String metaInfoXML = null;
		
		ArrayList dbTable = new ArrayList();
		Map dbValidMartTable = new HashMap(); // {P1aaegyptiP1_egene__go_P7sequenceP7__dm={dataset=egene,content=go,type=dm,dsPt=P1aaegyptiP1,dtPt=P7sequenceP7},,,,}
		
		TreeSet dbDataset = new TreeSet(); // values are dataset name with partition name removed if any; scan only main table names {gene_ensembl, marker_start, ,,}
		LinkedHashMap dbDatasetToPartitionRow = new LinkedHashMap(); // {gene_ensembl={hsap=12, mmus=15,,,,}, marker_start={hsap=1,,,}}
		Hashtable dbDatasetToPartitionIndex = new Hashtable(); // {gene_ensembl=1, marker_start=2, ,,,}
		
		// the reason each of MainTable and DmTable needs one variable is that they may 
		// have almost the same name (except for the last part), eg, ensembl__gene__main and ensembl__gene__dm
		Map dbDatasetMainTable = new HashMap(); // {gene_ensembl={gene,transcript,translation}, marker_start={start},,, }
		Map dbDatasetDmTable = new HashMap(); // {gene_ensembl={go,ox,pfeat,,,}, , , }  # with partition portion (if any) removed
		Map dbDatasetTableToPartitionRow = new HashMap(); // {gene_ensembl__go={process=1,function=2,,,}, gene_ensembl__ox={refseq=1,,,},, evoc__ontology={pathology=1,,},}
		Map dbDatasetTableToPartitionIndex = new HashMap(); // {gene_ensembl__go=8, gene_ensembl__ox=9,,,, evoc__ontology=15,,,}
		
		Map dbDatasetTablePartitionRange = new HashMap(); // {gene_ensembl__gene=[P1R1,P1R2,P1R5,P1R8,,,],gene_ensembl__go=[P1R1:P8R1,P1R2:P8R1,,P1R1:P8R2,,,],,,}
		Map dbDatasetTableColumnPartitionRange = new HashMap(); // {gene_ensembl__gene={id=[P1R1,P1R2,P1R5,P1R8,,,],name=[,,,]},gene_ensembl__go{id=[,,,],,,}}
		
		ResultSet tables = dmd.getTables(null, schemaName, null, null);
		
		while(tables.next()){
			String tableName = tables.getString(3);
			
			// skipping meta tables
			if (tableName.startsWith(metaTablePrefix)) continue;
			
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
			
			// populate dbDatasetToPartitionRow when the dataset has partition
			if (datasetPartitionEntry != null && !datasetPartitionEntry.equals("")) {
				if (dbDatasetToPartitionRow.containsKey(dataset)) {
					if (!((Hashtable) dbDatasetToPartitionRow.get(dataset))
							.containsKey(datasetPartitionEntry)) { // new partitonEntry
						int nextPos = ((Hashtable) dbDatasetToPartitionRow
								.get(dataset)).size() + 1;
						((Hashtable) dbDatasetToPartitionRow.get(dataset))
								.put(datasetPartitionEntry, new Integer(nextPos));
					}
				} else { // new dataset, of course with the first partitionEntry if there is any
					dbDatasetToPartitionRow.put(dataset, new Hashtable());
					((Hashtable) dbDatasetToPartitionRow.get(dataset)).put(datasetPartitionEntry, new Integer(1));
				}
			}
			
			if (!dbDatasetMainTable.containsKey(dataset))
				dbDatasetMainTable.put(dataset, new TreeSet());
			
			((TreeSet) dbDatasetMainTable.get(dataset)).add(tableNameDivisions[1]);
			
			// populate dbValidMartTable
			dbValidMartTable.put(tableName, new HashMap());
			((HashMap)dbValidMartTable.get(tableName)).put("dataset", dataset);
			((HashMap)dbValidMartTable.get(tableName)).put("content", tableNameDivisions[1]);
			((HashMap)dbValidMartTable.get(tableName)).put("type", "main");
			if (datasetPartitionEntry != null && !datasetPartitionEntry.equals("")) 
					((HashMap)dbValidMartTable.get(tableName)).put("dsPt", datasetPartitionEntry);

		}
		tables.close();

		// populate dbDatasetToPartitionIndex
		Iterator iterator = dbDatasetToPartitionRow.keySet().iterator();
		int partitionIndex = 1;
		while(iterator.hasNext()){
			dbDatasetToPartitionIndex.put(iterator.next(), new Integer(partitionIndex));
			partitionIndex++;
		}

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

			if (!dbDataset.contains(dataset) ||
					(dbDatasetToPartitionRow.containsKey(dataset)) && !((Hashtable) dbDatasetToPartitionRow.get(dataset))
					.containsKey(datasetPartitionEntry)) {  // dm table that has no main table, should warn this error
				log.warning("dimension table (" + tableName + ") doesn't have any associated main table, skipped.");
				continue;
			}

			log.info("processing dm table: " + tableName);

			String tablePartitionEntry = null;
			String content = tableNameDivisions[1];
			pattern = Pattern.compile(patternPartiTable);
			matcher = pattern.matcher(content);
			
			if (matcher.find()) {  // check if this is a partitioned table
				// System.out.println("Matched: " + matcher.group(0) + " " + matcher.group(1) + " " + matcher.group(2) + " " + matcher.group(3));
				content = matcher.group(1);  // with partition part removed
				tablePartitionEntry = matcher.group(2);
			}
			
			if (!dbDatasetDmTable.containsKey(dataset))
				dbDatasetDmTable.put(dataset, new TreeSet());

			((TreeSet) dbDatasetDmTable.get(dataset)).add(content);

			// dbDatasetTableToPartitionRow: {gene_ensembl__go={process=1,function=2,,,}, gene_ensembl__ox={refseq=1,,,},, evoc__ontology={pathology=1,,},}
			if (tablePartitionEntry != null && !tablePartitionEntry.equals("")) {
				if (dbDatasetTableToPartitionRow.containsKey(dataset + tableNameDivisionDelimiter + content)){
					if (!((Hashtable) dbDatasetTableToPartitionRow.get(dataset + tableNameDivisionDelimiter + content))
							.containsKey(tablePartitionEntry)) { // new partitonEntry
						int nextPos = ((Hashtable) dbDatasetTableToPartitionRow
								.get(dataset + tableNameDivisionDelimiter + content)).size() + 1;
						((Hashtable) dbDatasetTableToPartitionRow.get(dataset + tableNameDivisionDelimiter + content))
								.put(tablePartitionEntry, new Integer(nextPos));
					}
				} else { // new dataset, of course with the first partitionEntry if there is any
					dbDatasetTableToPartitionRow.put(dataset + tableNameDivisionDelimiter + content, new Hashtable());
					((Hashtable) dbDatasetTableToPartitionRow.get(dataset + tableNameDivisionDelimiter + content))
							.put(tablePartitionEntry, new Integer(1));
				}
			}

			// populate dbValidMartTable
			// {P1aaegyptiP1_egene__go_P7sequenceP7__dm={dataset=egene,content=go,type=dm,dsPt=P1aaegyptiP1,dtPt=P7sequenceP7},,,,}
			dbValidMartTable.put(tableName, new HashMap());
			((HashMap)dbValidMartTable.get(tableName)).put("dataset", dataset);
			((HashMap)dbValidMartTable.get(tableName)).put("content", content);
			((HashMap)dbValidMartTable.get(tableName)).put("type", "dm");
			if (datasetPartitionEntry != null && !datasetPartitionEntry.equals("")) 
					((HashMap)dbValidMartTable.get(tableName)).put("dsPt", datasetPartitionEntry);
			if (tablePartitionEntry != null && !tablePartitionEntry.equals("")) 
				((HashMap)dbValidMartTable.get(tableName)).put("dtPt", tablePartitionEntry);

		}

		// dbDatasetTableToPartitionIndex: {gene_ensembl__go=8, gene_ensembl__ox=9,,,, evoc__ontology=15,,,}
		iterator = dbDatasetTableToPartitionRow.keySet().iterator();
		while(iterator.hasNext()){
			dbDatasetTableToPartitionIndex.put(iterator.next(), new Integer(partitionIndex));
			partitionIndex++;
		}
		
		// TODO: one more loop to populate dbDatasetTablePartitionRange and dbDatasetTableColumnPartitionRange
		iterator = dbValidMartTable.keySet().iterator();
		while(iterator.hasNext()){
			String dbTableName = (String) iterator.next();
			HashMap martTableInfo = (HashMap) dbValidMartTable.get(dbTableName);
			String dataset = (String) martTableInfo.get("dataset");
			String content = (String) martTableInfo.get("content");
			String type = (String) martTableInfo.get("type");
			String dsPt = (String) martTableInfo.get("dsPt");
			String dtPt = (String) martTableInfo.get("dtPt");
			String martTableName = dataset + tableNameDivisionDelimiter + content;
			
			//System.out.println(dataset + " " + content + " " + type + " " + dsPt + " " + dtPt);
			
			String partitionRange = null;
			
			if (dsPt != null && dtPt != null) {
				String dsP =  ((Integer) dbDatasetToPartitionIndex.get(dataset)).toString();
				String dsPC = ((Integer) ((Hashtable) dbDatasetToPartitionRow.get(dataset)).get(dsPt)).toString();
				String dtP =  ((Integer) dbDatasetTableToPartitionIndex.get(martTableName)).toString();
				String dtPC = ((Integer) ((Hashtable) dbDatasetTableToPartitionRow.get(martTableName)).get(dtPt)).toString();
				partitionRange = "P" + dsP + "R" + dsPC + ":" + "P" + dtP + "R" + dtPC;
			} else if (dsPt != null) {
				String dsP =  ((Integer) dbDatasetToPartitionIndex.get(dataset)).toString();
				String dsPC = ((Integer) ((Hashtable) dbDatasetToPartitionRow.get(dataset)).get(dsPt)).toString();
				partitionRange = "P" + dsP + "R" + dsPC;
			} else if (dtPt != null) {
				String dtP =  ((Integer) dbDatasetTableToPartitionIndex.get(martTableName)).toString();
				String dtPC = ((Integer) ((Hashtable) dbDatasetTableToPartitionRow.get(martTableName)).get(dtPt)).toString();
				partitionRange = "P" + dtP + "R" + dtPC;
			} else {
				continue;
			}
			
			// dbDatasetTablePartitionRange: {gene_ensembl__gene=[P1R1,P1R2,P1R5,P1R8,,,],gene_ensembl__go=[P1R1:P8R1,P1R2:P8R1,,P1R1:P8R2,,,],,,}
			if (!dbDatasetTablePartitionRange.containsKey(martTableName))
				dbDatasetTablePartitionRange.put(martTableName, new ArrayList());
			((ArrayList) dbDatasetTablePartitionRange.get(martTableName)).add(partitionRange);

			// dbDatasetTableColumnPartitionRange: {gene_ensembl__gene={id=[P1R1,P1R2,P1R5,P1R8,,,],name=[,,,]},gene_ensembl__go{id=[,,,],,,}}
			ResultSet columns = dmd.getColumns(null, schemaName, dbTableName, null);
			while(columns.next()){
				String columnName = columns.getString(4);
				//System.out.println(columnName);
				if (!dbDatasetTableColumnPartitionRange.containsKey(martTableName)) {  // new table, new column
					dbDatasetTableColumnPartitionRange.put(martTableName, new LinkedHashMap());
					((LinkedHashMap) dbDatasetTableColumnPartitionRange.get(martTableName))
								.put(columnName, new ArrayList());
					((ArrayList) ((LinkedHashMap) dbDatasetTableColumnPartitionRange.
								get(martTableName)).get(columnName)).add(partitionRange);
				} else {  // exist table, new column
					if (!((LinkedHashMap) dbDatasetTableColumnPartitionRange.get(martTableName)).containsKey(columnName)) {  // exist table, new column
						((LinkedHashMap) dbDatasetTableColumnPartitionRange.get(martTableName)).put(columnName, new ArrayList());
					}
					((ArrayList) ((LinkedHashMap) dbDatasetTableColumnPartitionRange.
							get(martTableName)).get(columnName)).add(partitionRange);
				}
			}

		}
		
		
		System.out.println("dbDataset=" + dbDataset);
		System.out.println("dbDatasetToPartitionIndex=" + dbDatasetToPartitionIndex);
		System.out.println("dbDatasetToPartitionRow=" + dbDatasetToPartitionRow);
		System.out.println("dbDatasetMainTable=" + dbDatasetMainTable);
		System.out.println("dbDatasetDmTable=" + dbDatasetDmTable);
		System.out.println("dbDatasetTableToPartitionRow=" + dbDatasetTableToPartitionRow);
		System.out.println("dbDatasetTableToPartitionIndex=" + dbDatasetTableToPartitionIndex);
		System.out.println("dbDatasetTablePartitionRange=" + dbDatasetTablePartitionRange);
		System.out.println("dbDatasetTableColumnPartitionRange=" + dbDatasetTableColumnPartitionRange);
		
		return metaInfoXML;
	}


}
