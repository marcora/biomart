package org.biomart.objects.objects;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.biomart.common.general.exceptions.FunctionalException;
import org.biomart.configurator.utils.type.McNodeType;
import org.biomart.objects.MartConfiguratorUtils;
import org.jdom.Element;


public class Dataset extends MartConfiguratorObject implements Serializable {

	private static final long serialVersionUID = 1514366091760993846L;

	public static final String XML_ELEMENT_NAME = "dataset";
//	public static final McNodeType MC_NODE_TYPE = McNodeType.DataSet;
	
	public static void main(String[] args) {}

	private Boolean materialized = null;
	private String centralTable = null;

	private List<Config> configList = null;
	private List<PartitionTable> partitionTableList = null;
	private List<Table> tableList = null;
	private List<Relation> relationList = null;

	// For internal use only
	private PartitionTable mainPartitionTable = null;

	public Dataset(String name, String displayName, String description, Boolean visible, 
			Boolean materialized) {
		super(name, displayName, description, visible, XML_ELEMENT_NAME);
		this.materialized = materialized;
		
		this.configList = new ArrayList<Config>();
		this.partitionTableList = new ArrayList<PartitionTable>();
		this.tableList = new ArrayList<Table>();
		this.relationList = new ArrayList<Relation>();
	}

	
	public void addTable(Table table) {
		this.tableList.add(table);
	}
	public void addRelation(Relation relation) {
		this.relationList.add(relation);
	}
	public void addConfig(Config config) {
		this.configList.add(config);
	}
	public void addPartitionTable(PartitionTable partitionTable) {
		if (partitionTable.getMain()) {
			this.mainPartitionTable = partitionTable;
		}
		this.partitionTableList.add(partitionTable);
	}

	public List<Config> getConfigList() {
		return new ArrayList<Config>(this.configList);
	}
	public List<PartitionTable> getPartitionTableList() {
		return new ArrayList<PartitionTable>(partitionTableList);
	}
	public List<Table> getTableList() {
		return new ArrayList<Table>(tableList);
	}
	public List<Relation> getRelationList() {
		return new ArrayList<Relation>(relationList);
	}

	public Config getConfig(String name) {
		return (Config)super.getMartConfiguratorObjectByName(this.configList, name);
	}
	public PartitionTable getPartitionTable(String name) {
		return (PartitionTable)super.getMartConfiguratorObjectByName(this.partitionTableList, name);
	}
	public Table getTable(String name) {
		return (Table)super.getMartConfiguratorObjectByName(this.tableList, name);
	}
	public Relation getRelation(String name) {
		return (Relation)super.getMartConfiguratorObjectByName(this.relationList, name);
	}
	
	public PartitionTable getMainPartitionTable() {
		return mainPartitionTable;
	}

	public Boolean getMaterialized() {
		return materialized;
	}

	public void setMaterialized(Boolean materialized) {
		this.materialized = materialized;
	}

	public String getCentralTable() {
		return centralTable;
	}

	public void setCentralTable(String centralTable) {
		this.centralTable = centralTable;
	}

	@Override
	public String toString() {
		return 
			super.toString() + ", " + 
			"materialized = " + materialized + ", " + 
			"centralTable = " + centralTable + ", " + 
			"configList.size() = " + configList.size() + ", " + 
			"partitionTableList() = " + partitionTableList.size() + ", " + 
			"tableList() = " + tableList.size() + ", " + 
			"relationList() = " + relationList.size();
	}

	@Override
	public boolean equals(Object object) {
		if (this==object) {
			return true;
		}
		if((object==null) || (object.getClass()!= this.getClass())) {
			return false;
		}
		Dataset dataset=(Dataset)object;
		return (
			super.equals(dataset) &&
			(this.configList==dataset.configList || (this.configList!=null && this.configList.equals(dataset.configList)))
		);
	}

	public void merge (Dataset dataset) {
		List<Config> configList1 = this.getConfigList();
		List<Config> configList2 = dataset.getConfigList();
		for (Config config2 : configList2) {
			int index = configList1.indexOf(config2);
			if (index==-1) {
				this.addConfig(config2);
			}	// else: nothing
		}
	}
	
	public Element generateXml() throws FunctionalException {
		Element element = super.generateXml();
		MartConfiguratorUtils.addAttribute(element, "materialized", this.materialized);
		MartConfiguratorUtils.addAttribute(element, "centralTable", this.centralTable);
		
		for (PartitionTable partitionTable : this.partitionTableList) {
			element.addContent(partitionTable.generateXml());
		}
		
		for (Table table : this.tableList) {
			element.addContent(table.generateXml());
		}
		
		for (Relation relation : this.relationList) {
			element.addContent(relation.generateXml());
		}

		for (Config config : this.configList) {
			element.addContent(config.generateXml());
		}
		
		return element;
	}
}
