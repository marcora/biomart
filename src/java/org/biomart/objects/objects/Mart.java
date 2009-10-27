package org.biomart.objects.objects;


import java.io.Serializable;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;


import org.biomart.common.general.utils.CompareUtils;
import org.biomart.objects.MartConfiguratorConstants;
import org.biomart.objects.MartConfiguratorUtils;
import org.jdom.Element;


public class Mart extends MartConfiguratorObject implements Comparable<Mart>, Comparator<Mart>, Serializable {

	private static final long serialVersionUID = -5444938136316400493L;

	public static final String XML_ELEMENT_NAME = "mart";
	
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

	public Integer getVersion() {
		return version;
	}

	public void setVersion(Integer version) {
		this.version = version;
	}
	
	public List<Dataset> getDatasetList() {
		return datasetList;
	}

	@Override
	public String toString() {
		return 
			super.toString() + ", " + 
			"version = " + version;
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

	@Override
	public int hashCode() {
		int hash = MartConfiguratorConstants.HASH_SEED1;
		hash = MartConfiguratorConstants.HASH_SEED2 * hash + super.hashCode();
		hash = MartConfiguratorConstants.HASH_SEED2 * hash + (null==version? 0 : version.hashCode());
		return hash;
	}

	public int compare(Mart mart1, Mart mart2) {
		if (mart1==null && mart2!=null) {
			return -1;
		} else if (mart1!=null && mart2==null) {
			return 1;
		}
		return CompareUtils.compareNull(mart1.version, mart2.version);
	}

	public int compareTo(Mart mart) {
		return compare(this, mart);
	}
	
	/**
	 * Only for the node, children are treated separately
	 */
	public Element generateXml() {
		Element element = super.generateXml();
		MartConfiguratorUtils.addAttribute(element, "version", this.version);
		
		for (Dataset dataset : this.datasetList) {
			element.addContent(dataset.generateXml());
		}
		
		return element;
	}

	
	/*public Element generateXmlForWebService() {
		return generateXmlForWebService(null);
	}
	public Element generateXmlForWebService(Namespace namespace) {
		Element jdomObject = super.generateXmlForWebService(namespace);
		
		MartConfiguratorUtils.addAttribute(jdomObject, "version", this.version);
		
		return jdomObject;
	}
	public JSONObject generateJsonForWebService() {
		JSONObject jsonObject = super.generateJsonForWebService();
		
		JSONObject object = (JSONObject)jsonObject.get(super.xmlElementName);
		object.put("version", this.version);
		
		jsonObject.put(super.xmlElementName, object);
		return jsonObject;
	}*/
}