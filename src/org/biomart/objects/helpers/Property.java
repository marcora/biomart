package org.biomart.objects.helpers;

import org.biomart.common.general.utils.MyUtils;
import org.biomart.objects.MartConfiguratorConstants;



public class Property {	//TODO extends ancestor common with Range

	public static void main(String[] args) {}

	protected String property = null;
	protected Boolean generic = null;
	protected String partitionTableName = null;
	protected Integer partitionTableColumn = null;
	protected Boolean partSpecific = null;//TODO: authorize it to be part-specific
	protected Boolean partSuperSpecific = null;//TODO: authorize it to be part-specific
	// + part-specific name, ex: displayName--> dN=""

	// also a subtype MartConfiguratorObjectProperty (not a string, not an int, but a MCO)
	
	protected Property(String property, Boolean generic, String partitionTableName, Integer partitionTableColumn) {
		super();
		this.property = property;
		this.generic = generic;
		setGenericInfo(partitionTableName, partitionTableColumn);
	}
	
	public String getPartitionTableReference() {
		MyUtils.checkStatusProgram(this.generic);
		PartitionReference partitionReference = new PartitionReference(this.partitionTableName, partitionTableColumn);
		return partitionReference.toXmlString();
	}
	
	public String getStringValue() {
		MyUtils.errorProgram();
		return null;
	}
	
	public String getProperty() {
		return property;
	}

	public Boolean getGeneric() {
		return generic;
	}

	public Integer getPartitionTableColumn() {
		return partitionTableColumn;
	}

	public void setPartitionTableColumn(Integer partitionTableColumn) {
		this.partitionTableColumn = partitionTableColumn;
	}

	public String getPartitionTableName() {
		return partitionTableName;
	}

	public void setPartitionTableName(String partitionTableName) {
		this.partitionTableName = partitionTableName;
	}

	@Override
	public String toString() {
		return 
			super.toString() + ", " + 
			"property = " + property + ", " +
			"generic = " + generic + ", " +
			"partitionTableName = " + partitionTableName + ", " +
			"partitionTableColumn = " + partitionTableColumn;
	}

	@Override
	public boolean equals(Object object) {
		if (this==object) {
			return true;
		}
		if((object==null) || (object.getClass()!= this.getClass())) {
			return false;
		}
		Property property=(Property)object;
		return (
			(this.property==property.property || (this.property!=null && property.equals(property.property))) &&
			(this.generic==property.generic || (this.generic!=null && generic.equals(property.generic))) &&
			(this.partitionTableName==property.partitionTableName || (this.partitionTableName!=null && partitionTableName.equals(property.partitionTableName))) &&
			(this.partitionTableColumn==property.partitionTableColumn || (this.partitionTableColumn!=null && partitionTableColumn.equals(property.partitionTableColumn)))
		);
	}

	@Override
	public int hashCode() {
		int hash = MartConfiguratorConstants.HASH_SEED1;
		hash = MartConfiguratorConstants.HASH_SEED2 * hash + super.hashCode();
		hash = MartConfiguratorConstants.HASH_SEED2 * hash + (null==property? 0 : property.hashCode());
		hash = MartConfiguratorConstants.HASH_SEED2 * hash + (null==generic? 0 : generic.hashCode());
		hash = MartConfiguratorConstants.HASH_SEED2 * hash + (null==partitionTableName? 0 : partitionTableName.hashCode());
		hash = MartConfiguratorConstants.HASH_SEED2 * hash + (null==partitionTableColumn? 0 : partitionTableColumn.hashCode());
		return hash;
	}
	
	public boolean same(Property property) {
		MyUtils.errorProgram();
		return true;
	}

	public void setGenericInfo(String partitionTableName, Integer partitionTableColumn) {
		this.partitionTableName = partitionTableName;
		this.partitionTableColumn = partitionTableColumn;
		this.generic = partitionTableName!=null && partitionTableColumn!=null;
	}

	public String getXmlValue() {
		return this.generic ? this.getPartitionTableReference() : getStringValue();
	}
}
