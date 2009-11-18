package org.biomart.objects.objects;


import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.biomart.common.general.exceptions.FunctionalException;
import org.biomart.configurator.utils.type.McNodeType;
import org.biomart.objects.MartConfiguratorUtils;
import org.jdom.Element;


public class Mart extends MartConfiguratorObject implements Serializable {

	private static final long serialVersionUID = -5444938136316400493L;

	public static final String XML_ELEMENT_NAME = "mart";
	public static final McNodeType MC_NODE_TYPE = McNodeType.Mart;
	
	public static void main(String[] args) {}

	private Integer version = null;
	
	private List<Dataset> datasetList = null;
	
	public Mart(String name, String displayName, String description, Boolean visible, 
			Integer version) {
		super(name, displayName, description, visible, XML_ELEMENT_NAME);
		this.version = version;
		
		this.datasetList = new ArrayList<Dataset>();
	}
	
	public void addDataset(Dataset dataset) {
		this.datasetList.add(dataset);
	}
	public List<Dataset> getDatasetList() {
		return new ArrayList<Dataset>(this.datasetList);
	}
	public Dataset getDataset(String name) {
		return (Dataset)super.getMartConfiguratorObjectByName(this.datasetList, name);
	}

	public Integer getVersion() {
		return version;
	}

	public void setVersion(Integer version) {
		this.version = version;
	}

	@Override
	public String toString() {
		return 
			super.toString() + ", " + 
			"version = " + version + ", " + 
			"datasetList.size() = " + datasetList.size();
	}

	@Override
	public boolean equals(Object object) {
		if (this==object) {
			return true;
		}
		if((object==null) || (object.getClass()!= this.getClass())) {
			return false;
		}
		Mart mart=(Mart)object;
		return (
			super.equals(mart) &&
			(this.version==mart.version || (this.version!=null && version.equals(mart.version)))
		);
	}

	public void merge (Mart mart) {
		List<Dataset> datasetList1 = this.getDatasetList();
		List<Dataset> datasetList2 = mart.getDatasetList();
		for (Dataset dataset2 : datasetList2) {
			int index = datasetList1.indexOf(dataset2);
			if (index==-1) {
				this.addDataset(dataset2);
			} else {	// They are the same, but their content may be different
				Dataset dataset1 = datasetList1.get(index);
				dataset1.merge(dataset2);
			}
		}
	}
	
	public Element generateXml() throws FunctionalException {
		Element element = super.generateXml();
		MartConfiguratorUtils.addAttribute(element, "version", this.version);
		
		for (Dataset dataset : this.datasetList) {
			element.addContent(dataset.generateXml());
		}
		
		return element;
	}

	
	// ===================================== Should be a different class ============================================

	public Mart(Mart mart) throws CloneNotSupportedException {	// creates a light clone (temporary solution)
		this(mart.name, mart.displayName, null, mart.visible, mart.version);
	}
}
