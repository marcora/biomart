package org.biomart.objects.objects;


import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.biomart.configurator.utils.type.McNodeType;
import org.biomart.objects.MartConfiguratorUtils;
import org.jdom.Element;


public class Config extends MartConfiguratorObject implements Serializable {

	private static final long serialVersionUID = -4824812826795490022L;

	public static final String XML_ELEMENT_NAME = "config";
	public static final McNodeType MC_NODE_TYPE = null;
	
	public static void main(String[] args) {}

	private String datasetName = null;
	private Container rootContainer = null;

	private List<Portable> importableList = null;
	private List<Portable> exportableList = null;

	public Config(String name,  
			String datasetName) {
		super(name, null, null, null, XML_ELEMENT_NAME);	// displayName, description & visible do not apply for that object
		this.datasetName = datasetName;
		
		this.importableList = new ArrayList<Portable>();
		this.exportableList = new ArrayList<Portable>();
		
		this.rootContainer = Container.createRootContainer();
	}
	
	
	public void addImportable(Portable importable) {
		this.importableList.add(importable);
	}
	public void addExportable(Portable exportable) {
		this.exportableList.add(exportable);
	}


	public List<Portable> getImportableList() {
		return new ArrayList<Portable>(this.importableList);
	}
	public List<Portable> getPortableList() {
		return new ArrayList<Portable>(this.exportableList);
	}
	
	
	public Portable getImportable(String name) {
		return (Portable)super.getMartConfiguratorObjectByName(this.importableList, name);
	}
	public Portable getExportable(String name) {
		return (Portable)super.getMartConfiguratorObjectByName(this.exportableList, name);
	}


	
	public Container getRootContainer() {
		return rootContainer;
	}
	
	public String getDatasetName() {
		return datasetName;
	}

	public void setDatasetName(String datasetName) {
		this.datasetName = datasetName;
	}

	@Override
	public String toString() {
		return 
			super.toString() + ", " +
			"datasetName = " + datasetName + ", " +
			"rootContainer = " + rootContainer + ", " +
			"importableList.size() = " + importableList.size() + ", " +
			"exportableList.size() = " + exportableList.size();
	}

	@Override
	public boolean equals(Object object) {
		if (this==object) {
			return true;
		}
		if((object==null) || (object.getClass()!= this.getClass())) {
			return false;
		}
		Config config=(Config)object;
		return (
			super.equals(config) &&
			(this.datasetName==config.datasetName || (this.datasetName!=null && datasetName.equals(config.datasetName)))	// check dataset name too
		);
	}

	/*@Override
	public int hashCode() {
		int hash = MartConfiguratorConstants.HASH_SEED1;
		hash = MartConfiguratorConstants.HASH_SEED2 * hash + super.hashCode();
		hash = MartConfiguratorConstants.HASH_SEED2 * hash + (null==datasetName? 0 : datasetName.hashCode());
		return hash;
	}*/

	/*public int compare(Config config1, Config config2) {
		if (config1==null && config2!=null) {
			return -1;
		} else if (config1!=null && config2==null) {
			return 1;
		}
		return CompareUtils.compareNull(config1.datasetName, config2.datasetName);
	}

	public int compareTo(Config config) {
		return compare(this, config);
	}*/
	
	public Element generateXml() {
		Element element = super.generateXml();
		MartConfiguratorUtils.addAttribute(element, "datasetName", this.datasetName);
		
		for (Portable importable : this.importableList) {
			element.addContent(importable.generateXml());
		}
		
		for (Portable exportable : this.exportableList) {
			element.addContent(exportable.generateXml());
		}

		element.addContent(this.rootContainer.generateXml());
		
		return element;
	}
}
