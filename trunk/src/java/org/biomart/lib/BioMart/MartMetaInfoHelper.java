/**
 * 
 */
package org.biomart.lib.BioMart;

import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.TreeSet;
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
		
		TreeSet dbDataset = new TreeSet(); // values are dataset name with partition name removed if any; scan only main table names
		LinkedHashMap dbDatasetToPartitionMap = new LinkedHashMap(); // key: gene_ensembl, value: {hsap=12, mmus=15,,,,}
		Hashtable dbDatasetToPartitionIndex = new Hashtable(); // {gene_ensembl=1, marker_start=2, ,,,}
		
		Map dbDatasetMainTable = new HashMap(); // {gene_ensembl=[gene, transcript, translation], marker_start=[start],,, }
		Map dbDatasetDmTable = new HashMap(); // {gene_ensembl=[go, ox, pfeat,,,], , , }  # with partition portion removed
		Map dbDatasetTablePartition = new HashMap(); // key: gene_ensembl__go, value: [process,function,,,]
		Map dbDatasetTableColumn = new HashMap(); // key: gene_ensembl__go:process, value: [id,primary_acc,,,,]
		
		Map dbDatasetTableToPartitionIndex = new HashMap(); // key: gene_ensembl__go, value: P8; gene_ensembl__pfeat, value: P9,
		Map dbDatasetTableColumnPartitionRange = new HashMap(); // key: gene_ensembl__go:process:id, value: [P1R1:P7R3,,,]
		
		
		
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

			log.info("processing table: " + tableName);

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
			
			// populate datasetToPartitionMap when the dataset has partition
			if (datasetPartitionEntry != null && !datasetPartitionEntry.equals("")) {
				if (dbDatasetToPartitionMap.containsKey(dataset)) {
					if (!((Hashtable) dbDatasetToPartitionMap.get(dataset))
							.containsKey(datasetPartitionEntry)) { // new partitonEntry
						int nextPos = ((Hashtable) dbDatasetToPartitionMap
								.get(dataset)).size() + 1;
						((Hashtable) dbDatasetToPartitionMap.get(dataset))
								.put(datasetPartitionEntry, new Integer(nextPos));
					}
				} else { // new dataset, of course with the first partitionEntry if there is any
					dbDatasetToPartitionMap.put(dataset, new Hashtable());
					((Hashtable) dbDatasetToPartitionMap.get(dataset)).put(datasetPartitionEntry, new Integer(1));
				}
			}
			
			// new process tables
			
		
		}

		tables.close();

		Iterator dsIterator = dbDatasetToPartitionMap.keySet().iterator();
		int position = 1;
		while(dsIterator.hasNext()){
			dbDatasetToPartitionIndex.put(dsIterator.next(), new Integer(position));
			position++;
		}
		
		 
		
		//System.out.println(dbDataset);
		//System.out.println(dbDatasetToPartitionIndex);
		//System.out.println(dbDatasetToPartitionMap);
		
		return metaInfoXML;
	}


}
