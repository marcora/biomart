package org.biomart.transformation.helpers;

import org.biomart.common.general.exceptions.FunctionalException;
import org.biomart.common.general.utils.MyUtils;
import org.biomart.objects.MartConfiguratorConstants;



public class NamingConventionTableName /*implements Comparable<NamingConventionTableName>, Comparator<NamingConventionTableName> */{

	public static void main(String[] args) {}

	private String datasetName = null;
	private String tableShortName = null;
	private String tableType = null;
	
	public NamingConventionTableName(String datasetName, String tableShortName, String tableType) {
		this.datasetName = datasetName;
		this.tableShortName = tableShortName;
		this.tableType = tableType;
	}
	public NamingConventionTableName(String originalTableName) {
		MyUtils.checkStatusProgram(null!=originalTableName);
		String[] tableNameSplit = originalTableName.split(TransformationConstants.NAMING_CONVENTION_ELEMENT_SEPARATOR_REGEX);
		MyUtils.checkStatusProgram(tableNameSplit.length>=1 || tableNameSplit.length<=3, "originalTableName = " + originalTableName);
		if (tableNameSplit.length==1) {	// if like "main"
			MyUtils.checkStatusProgram(tableNameSplit[0].equals(MartConfiguratorConstants.NAMING_CONVENTION_MAIN_TABLE_NAME));
			this.datasetName = null;
			this.tableShortName = null;
			this.tableType = MartConfiguratorConstants.NAMING_CONVENTION_MAIN_TABLE_NAME;
		} else if (tableNameSplit.length==2) {	// if like "ox_CCDS__dm"
			this.datasetName = null;
			this.tableShortName = tableNameSplit[0];
			this.tableType = tableNameSplit[1];
		} else if (tableNameSplit.length==3) {	// if like "hsapiens_gene_ensembl__ox_CCDS__dm"
			this.datasetName = tableNameSplit[0];
			this.tableShortName = tableNameSplit[1];
			this.tableType = tableNameSplit[2];			
		} 
	}
	
	public void setDatasetName(String datasetName) {
		this.datasetName = datasetName;
	}

	public String getDatasetName() {
		return datasetName;
	}

	public String getTableShortName() {
		return tableShortName;
	}

	public String getTableType() {
		return tableType;
	}

	@Override
	public String toString() {
		return
			"datasetName = " + datasetName + ", " +
			"tableShortName = " + tableShortName + ", " +
			"tableType = " + tableType;
	}

	@Override
	public boolean equals(Object object) {
		if (this==object) {
			return true;
		}
		if((object==null) || (object.getClass()!= this.getClass())) {
			return false;
		}
		NamingConventionTableName namingConventionTableName=(NamingConventionTableName)object;
		return (
			(this.datasetName==namingConventionTableName.datasetName || (this.datasetName!=null && datasetName.equals(namingConventionTableName.datasetName))) &&
			(this.tableShortName==namingConventionTableName.tableShortName || (this.tableShortName!=null && tableShortName.equals(namingConventionTableName.tableShortName))) &&
			(this.tableType==namingConventionTableName.tableType || (this.tableType!=null && tableType.equals(namingConventionTableName.tableType)))
		);
	}

	@Override
	public int hashCode() {
		int hash = MartConfiguratorConstants.HASH_SEED1;
		hash = MartConfiguratorConstants.HASH_SEED2 * hash + (null==datasetName? 0 : datasetName.hashCode());
		hash = MartConfiguratorConstants.HASH_SEED2 * hash + (null==tableShortName? 0 : tableShortName.hashCode());
		hash = MartConfiguratorConstants.HASH_SEED2 * hash + (null==tableType? 0 : tableType.hashCode());
		return hash;
	}

	public void setTableShortName(String tableShortName) {
		this.tableShortName = tableShortName;
	}

	/*@Override
	public int compare(NamingConventionTableName namingConventionTableName1, NamingConventionTableName namingConventionTableName2) {
		if (namingConventionTableName1==null && namingConventionTableName2!=null) {
			return -1;
		} else if (namingConventionTableName1!=null && namingConventionTableName2==null) {
			return 1;
		}
		int compare = CompareUtils.compareNull(namingConventionTableName1.datasetName, namingConventionTableName2.datasetName);
		if (compare!=0) {
			return compare;
		}
		compare = CompareUtils.compareNull(namingConventionTableName1.tableShortName, namingConventionTableName2.tableShortName);
		if (compare!=0) {
			return compare;
		}
		return CompareUtils.compareNull(namingConventionTableName1.tableType, namingConventionTableName2.tableType);
	}

	@Override
	public int compareTo(NamingConventionTableName namingConventionTableName) {
		return compare(this, namingConventionTableName);
	}*/

	public String generateTableName() throws FunctionalException {
		if (this.datasetName==null || this.tableShortName==null || this.tableType==null) {
			throw new FunctionalException("Cannot generate table name");
		}
		String newTableName = this.datasetName + 
		TransformationConstants.NAMING_CONVENTION_ELEMENT_SEPARATOR + this.tableShortName + 
		TransformationConstants.NAMING_CONVENTION_ELEMENT_SEPARATOR + this.tableType;
		return newTableName;
	}
}
