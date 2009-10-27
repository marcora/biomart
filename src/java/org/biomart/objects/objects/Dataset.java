package org.biomart.objects.objects;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import net.sf.json.JSONObject;

import org.biomart.objects.MartConfiguratorConstants;
import org.biomart.objects.MartConfiguratorUtils;
import org.jdom.Element;
import org.jdom.Namespace;


public class Dataset extends MartConfiguratorObject implements Serializable {

	private static final long serialVersionUID = 1514366091760993846L;

	public static final String XML_ELEMENT_NAME = "dataset";
	
	public static void main(String[] args) {}

	private Boolean materialized = null;

	private List<Config> configList = null;
	private List<PartitionTable> partitionTableList = null;
	private List<Table> tableList = null;
	private List<Relation> relationList = null;
	private String centralTable = null;

	public Dataset(String name, String displayName, String description, Boolean visible, 
			Boolean materialized) {
		super(name, displayName, description, visible, XML_ELEMENT_NAME);
		this.materialized = materialized;
		
		this.configList = new ArrayList<Config>();
		this.partitionTableList = new ArrayList<PartitionTable>();
		this.tableList = new ArrayList<Table>();
		this.relationList = new ArrayList<Relation>();
	}
	
	public void addConfig(Config config) {
		this.configList.add(config);
	}
	
	public void addPartitionTable(PartitionTable partitionTable) {
		this.partitionTableList.add(partitionTable);
	}
	
	public void addTable(Table table) {
		this.tableList.add(table);
	}
	
	public void addRelation(Relation relation) {
		this.relationList.add(relation);
	}

	public Boolean getMaterialized() {
		return materialized;
	}

	public void setMaterialized(Boolean materialized) {
		this.materialized = materialized;
	}

	public List<Config> getConfigList() {
		return configList;
	}

	public List<PartitionTable> getPartitionTableList() {
		return partitionTableList;
	}

	public List<Table> getTableList() {
		return tableList;
	}

	public List<Relation> getRelationList() {
		return relationList;
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
			"centralTable = " + centralTable;
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
			(this.materialized==dataset.materialized || (this.materialized!=null && materialized.equals(dataset.materialized)))
		);
	}

	@Override
	public int hashCode() {
		int hash = MartConfiguratorConstants.HASH_SEED1;
		hash = MartConfiguratorConstants.HASH_SEED2 * hash + (null==materialized? 0 : materialized.hashCode());
		hash = MartConfiguratorConstants.HASH_SEED2 * hash + (null==centralTable? 0 : centralTable.hashCode());
		return hash;
	}
	
	public Element generateXml() {
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
	
	public Element generateXmlForWebService() {
		return generateXmlForWebService(null);
	}
	public Element generateXmlForWebService(Namespace namespace) {
		Element jdomObject = super.generateXmlForWebService(namespace);
		
		MartConfiguratorUtils.addAttribute(jdomObject, "materialized", this.materialized);
		MartConfiguratorUtils.addAttribute(jdomObject, "centralTable", this.centralTable);
		
		return jdomObject;
	}

	public JSONObject generateJsonForWebService() {
		JSONObject jsonObject = super.generateJsonForWebService();
		
		JSONObject object = (JSONObject)jsonObject.get(super.xmlElementName);
		object.put("materialized", this.materialized);
		object.put("centralTable", this.centralTable);
		
		jsonObject.put(super.xmlElementName, object);
		return jsonObject;
	}
}