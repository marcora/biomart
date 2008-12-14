package org.biomart.lib.BioMart;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.biomart.lib.utils.DBConnectionUtils;

public class Mart extends Root{
	
	public String name = null;
	public String version = null;
	public Collection partitionTables;
	public Location location;
	private String databaseName;
	private String schemaName;

	public Mart(String name, String version, String databaseName,
			String schemaName, Location location) {

		log.info("creating Mart Object: " + name);
		partitionTables = new LinkedList();
		
		this.name = name;
		this.version = version;
		this.databaseName = databaseName;
		this.schemaName = schemaName;
		this.location = location;
	}

	public void addPartitionTable(PartitionTable partitionTableObj) {
		log.info("adding PartitionTable object to Mart");
		this.partitionTables.add(partitionTableObj);		
	}
	
	public String getMetaInfoXML() throws Exception, SQLException {
		String metaInfoXML = null;
		
		// TODO: check if location.getType() is "martservice", if yes, get metaInfoXML through martservice
		if (location.getType().equals("martservice")) {
			log.info("getting metaInfoXML from martservice at: " + location.getHost());
			// code for getting XML through martservice here
			return metaInfoXML;
		}
		
		Connection conn;
		
		log.info("connecting to DB at: " + location.getHost());
		conn = DBConnectionUtils.getConnection(location.getType(), location.getHost(),
					location.getPort(), databaseName, location.getUserName(), location.getPassword());
		
		DatabaseMetaData dmd;
		try {
			dmd = conn.getMetaData();
		} catch (Exception e) {
			conn.close();
			throw e;
		}
		
		// TODO: try to get the XML from metatable in the DB, or create one if it's necessary
		/* pseudocode here
		If NOT metatables with XML
		{
		    check_if_you_are_admin_for_writeBack;
		    if YES
		    {
		        readSchema;
		        makeXML;
		        dumpTheXMLBackToMart;
		        $xml = XMLJustCreated;
		    }
		    NOT Admin
		    {
		       sorryMate;
		       exit();
		    }
		}
		else
		{
		    $xml = XML_FROM_MetaTables;
		}
		*/
		
		// go directly to make metaInfoXML from DB schema for now
		metaInfoXML = makeMetaInfoXML(dmd);
		
		conn.close();
		return metaInfoXML;
	}

	// TODO: should we move this method to a utility class?
	//       we probably should, this method is going to be complex and 
	//       very likely will need some helper functions
	private String makeMetaInfoXML(DatabaseMetaData dmd) throws Exception {
		
		// some settings. TODO: we should move this type of settings to a .properties file
		String metaTablePrefix = "meta_";
		String tableNameDivisionDelimiter = "__";
		String keyColumnSuffix = "_key";
		String booleanColumnSuffix = "_bool";
		String patternPartiDataset = "^((P\\d+).+\\2)_(.+)";
		String patternPartiTable = "(.+)_((P\\d+).+\\3)";
		
		// some variables
		String metaInfoXML = null;
		
		TreeSet dbDataset = new TreeSet(); // values are dataset name with partition name removed if any; scan only main table names
		// keys are dataset names, key position will be the partition index,
		// values are datasetPartition values
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

			// log.info("processing table: " + tableName);

			String dataset = tableNameDivisions[0];
			String datasetPartitionEntry = null;

			Pattern pattern = Pattern.compile(patternPartiDataset);
			Matcher matcher = pattern.matcher(dataset);

			if (matcher.find()){  // check if this is a partitioned dataset
				System.out.println("Matched: " + matcher.group(0) + " " + matcher.group(1) + " " + matcher.group(2) + " " + matcher.group(3));
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
		
		}

		tables.close();

		Iterator dsIterator = dbDatasetToPartitionMap.keySet().iterator();
		int position = 1;
		while(dsIterator.hasNext()){
			dbDatasetToPartitionIndex.put(dsIterator.next(), new Integer(position));
			position++;
		}
		
		System.out.println(dbDataset);
		System.out.println(dbDatasetToPartitionIndex);
		System.out.println(dbDatasetToPartitionMap);
		
		return metaInfoXML;
	}

	/**
	 * main for testing
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		Location myLocation = new Location("ensembl", "mysql", "localhost", "3306", "martadmin", "biomart");
		Mart myMart = new Mart("ensembl", "51", "jz_ensembl_mart_51_08", "", myLocation);
		String xml = null;
		try {
			xml = myMart.getMetaInfoXML();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println(xml);
	}

	
	public String getDatabaseName() {
		return databaseName;
	}

	public String getSchemaName() {
		return schemaName;
	}

}
