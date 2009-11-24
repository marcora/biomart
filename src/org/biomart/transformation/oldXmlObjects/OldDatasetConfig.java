package org.biomart.transformation.oldXmlObjects;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.biomart.common.general.exceptions.FunctionalException;
import org.biomart.common.general.utils.MyUtils;
import org.biomart.objects.MartConfiguratorConstants;
import org.biomart.objects.MartConfiguratorUtils;
import org.biomart.objects.objects.Config;
import org.biomart.objects.objects.Dataset;
import org.biomart.old.martService.MartServiceConstants;
import org.biomart.transformation.helpers.TransformationConstants;
import org.biomart.transformation.helpers.TransformationHelper;
import org.biomart.transformation.helpers.TransformationUtils;
import org.jdom.Element;


public class OldDatasetConfig extends OldNode {

	public static void main(String[] args) {}

	private String dataset = null;
	private Integer datasetID = null;
	private String defaultDataset = null;
	private String displayName = null;
	private String description = null;
	private String entryLabel = null;
	private String interfaces = null;
	private String internalName = null;
	private String martUsers = null;
	private String modified = null;
	private String softwareVersion = null;
	private String template = null;
	private String type = null;
	private String version = null;
	private Boolean visible = null;
	
	private List<OldNode> oldMainTableList = null;
	private List<OldNode> oldKeyList = null;
	
	private List<OldDynamicDataset> oldDynamicDatasetList = null;
	private List<OldImportable> oldImportableList = null;
	private List<OldExportable> oldExportableList = null;
	private List<OldAttributePage> oldAttributePageList = null;
	private List<OldFilterPage> oldFilterPageList = null;

	public static final List<String> propertyList = new ArrayList<String>(Arrays.asList(new String[] {
			"dataset", "datasetID", "defaultDataset", "displayName", "description", "entryLabel", "interfaces", 
			"internalName", "martUsers", "modified", "softwareVersion", "template", "type", "version", "visible",
			
			/*"optional_parameters"*/// For GenomicSequence only it seems
			"hidden",	// for dataset "wormbase_anatomy_term"
			"hideDisplay",	// property ignored (dataset "wormbase_phenotype")
	}));
	
	public OldDatasetConfig(Element jdomElement) throws FunctionalException {
		
		this(jdomElement,
				jdomElement.getAttributeValue("dataset"),
				jdomElement.getAttributeValue("datasetID"),
				jdomElement.getAttributeValue("defaultDataset"),
				jdomElement.getAttributeValue("displayName"),
				jdomElement.getAttributeValue("description"),
				jdomElement.getAttributeValue("entryLabel"),
				jdomElement.getAttributeValue("interfaces"),
				jdomElement.getAttributeValue("internalName"),
				jdomElement.getAttributeValue("martUsers"),
				jdomElement.getAttributeValue("modified"),
				jdomElement.getAttributeValue("softwareVersion"),
				jdomElement.getAttributeValue("template"),
				jdomElement.getAttributeValue("type"),
				jdomElement.getAttributeValue("version"),
				MartConfiguratorUtils.binaryDigitToBoolean(jdomElement.getAttributeValue("visible"))
		);
	}
	
	public OldDatasetConfig(Element jdomDatasetConfig, String dataset, String datasetID, String defaultDataset, 
			String displayName, String description, String entryLabel, String interfaces, String internalName, String martUsers, 
			String modified, String softwareVersion, String template, String type, String version, Boolean visible) throws FunctionalException {
		
		super(jdomDatasetConfig);
		
		this.dataset = dataset;
		this.datasetID = TransformationUtils.getIntegerValueFromString(datasetID, "datasetID");
		this.defaultDataset = defaultDataset;
		this.displayName = displayName;
		this.description = description;
		this.entryLabel = entryLabel;
		this.interfaces = interfaces;
		this.internalName = internalName;
		this.martUsers = martUsers;
		this.modified = modified;
		this.softwareVersion = softwareVersion;
		this.template = template;
		this.type = type;
		this.version = version;
		this.visible = visible;
		
		this.oldMainTableList = new ArrayList<OldNode>();
		this.oldKeyList = new ArrayList<OldNode>();
		
		this.oldDynamicDatasetList = new ArrayList<OldDynamicDataset>();
		this.oldImportableList = new ArrayList<OldImportable>();
		this.oldExportableList = new ArrayList<OldExportable>();
		this.oldAttributePageList = new ArrayList<OldAttributePage>();
		this.oldFilterPageList = new ArrayList<OldFilterPage>();
		
		if (isTableSetDataset()) { 	// Don't bother checking if not a TableSet (probably not right)
			TransformationUtils.checkJdomElementProperties(jdomElement, OldDatasetConfig.propertyList);
		}
	}
	public boolean isTableSetDataset() {
		return MartServiceConstants.ATTRIBUTE_TABLE_SET.equals(this.type);
	}
	public void addOldMainTable (OldNode oldMainTable) {
		this.oldMainTableList.add(oldMainTable);		
	}
	public void addOldKey (OldNode oldKey) {
		this.oldKeyList.add(oldKey);		
	}
	public void addOldDynamicDataset (OldDynamicDataset oldDynamicDataset) {
		this.oldDynamicDatasetList.add(oldDynamicDataset);		
	}
	public void addOldImportable (OldImportable oldImportable) {
		this.oldImportableList.add(oldImportable);		
	}
	public void addOldExportable (OldExportable oldExportable) {
		this.oldExportableList.add(oldExportable);		
	}
	public void addOldAttributePage (OldAttributePage oldAttributePage) {
		this.oldAttributePageList.add(oldAttributePage);		
	}
	public void addOldFilterPage (OldFilterPage oldFilterPage) {
		this.oldFilterPageList.add(oldFilterPage);		
	}

	public String getDataset() {
		return dataset;
	}

	public Integer getDatasetID() {
		return datasetID;
	}

	public String getDefaultDataset() {
		return defaultDataset;
	}

	public String getDisplayName() {
		return displayName;
	}

	public String getDescription() {
		return description;
	}

	public String getEntryLabel() {
		return entryLabel;
	}

	public String getInterfaces() {
		return interfaces;
	}

	public String getInternalName() {
		return internalName;
	}

	public String getMartUsers() {
		return martUsers;
	}

	public String getModified() {
		return modified;
	}

	public String getSoftwareVersion() {
		return softwareVersion;
	}

	public String getTemplate() {
		return template;
	}

	public String getType() {
		return type;
	}

	public String getVersion() {
		return version;
	}

	public Boolean getVisible() {
		return visible;
	}

	public List<OldNode> getOldKeyList() {
		return oldKeyList;
	}

	public void setOldKeyList(List<OldNode> oldKeyList) {
		this.oldKeyList = oldKeyList;
	}

	public List<OldNode> getOldMainTableList() {
		return oldMainTableList;
	}

	public void setOldMainTableList(List<OldNode> oldMainTableList) {
		this.oldMainTableList = oldMainTableList;
	}

	public List<OldDynamicDataset> getOldDynamicDatasetList() {
		return oldDynamicDatasetList;
	}

	public List<OldImportable> getOldImportableList() {
		return oldImportableList;
	}

	public List<OldExportable> getOldExportableList() {
		return oldExportableList;
	}

	public List<OldAttributePage> getOldAttributePageList() {
		return oldAttributePageList;
	}

	public List<OldFilterPage> getOldFilterPageList() {
		return oldFilterPageList;
	}

	public void setDataset(String dataset) {
		this.dataset = dataset;
	}

	public void setDatasetID(Integer datasetID) {
		this.datasetID = datasetID;
	}

	public void setDefaultDataset(String defaultDataset) {
		this.defaultDataset = defaultDataset;
	}

	public void setDisplayName(String displayName) {
		this.displayName = displayName;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public void setEntryLabel(String entryLabel) {
		this.entryLabel = entryLabel;
	}

	public void setInterfaces(String interfaces) {
		this.interfaces = interfaces;
	}

	public void setInternalName(String internalName) {
		this.internalName = internalName;
	}

	public void setMartUsers(String martUsers) {
		this.martUsers = martUsers;
	}

	public void setModified(String modified) {
		this.modified = modified;
	}

	public void setSoftwareVersion(String softwareVersion) {
		this.softwareVersion = softwareVersion;
	}

	public void setTemplate(String template) {
		this.template = template;
	}

	public void setType(String type) {
		this.type = type;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	public void setVisible(Boolean visible) {
		this.visible = visible;
	}

	public void setOldDynamicDatasetList(List<OldDynamicDataset> oldDynamicDatasetList) {
		this.oldDynamicDatasetList = oldDynamicDatasetList;
	}

	public void setOldImportableList(List<OldImportable> oldImportableList) {
		this.oldImportableList = oldImportableList;
	}

	public void setOldExportableList(List<OldExportable> oldExportableList) {
		this.oldExportableList = oldExportableList;
	}

	public void setOldAttributePageList(List<OldAttributePage> oldAttributePageList) {
		this.oldAttributePageList = oldAttributePageList;
	}

	public void setOldFilterPageList(List<OldFilterPage> oldFilterPageList) {
		this.oldFilterPageList = oldFilterPageList;
	}

	@Override
	public String toString() {
		
		int tabLevel = TransformationConstants.DATASET_CONFIG_TAB_LEVEL;
		StringBuffer sb = new StringBuffer();
		 
		sb.append(MartConfiguratorUtils.getTabSpace(tabLevel) + super.toString() + ", ");
		sb.append("dataset = " + dataset + ", " +
					"datasetID = " + datasetID + ", " +
					"defaultDataset = " + defaultDataset + ", " +
					"displayName = " + displayName + ", " +
					"description = " + description + ", " +
					"entryLabel = " + entryLabel + ", " +
					"interfaces = " + interfaces + ", " +
					"internalName = " + internalName + ", " +
					"martUsers = " + martUsers + ", " +
					"modified = " + modified + ", " +
					"softwareVersion = " + softwareVersion + ", " +
					"template = " + template + ", " +
					"type = " + type + ", " +
					"version = " + version + ", " +
					"visible = " + visible + MyUtils.LINE_SEPARATOR);

		sb.append(MartConfiguratorUtils.getTabSpace(tabLevel) + "oldMainTableList.size() = " + oldMainTableList.size() + MyUtils.LINE_SEPARATOR);
		for (OldNode oldMainTable : this.oldMainTableList) {
			sb.append(MartConfiguratorUtils.getTabSpace(tabLevel+1) + "oldMainTable = {" + oldMainTable);
			sb.append(MartConfiguratorUtils.getTabSpace(tabLevel+1) + "}" + MyUtils.LINE_SEPARATOR);
		}
		sb.append(MartConfiguratorUtils.getTabSpace(tabLevel) + "oldKeyList.size() = " + oldKeyList.size() + MyUtils.LINE_SEPARATOR);
		for (OldNode oldKey : this.oldKeyList) {
			sb.append(MartConfiguratorUtils.getTabSpace(tabLevel+1) + "oldKey = {" + oldKey);
			sb.append(MartConfiguratorUtils.getTabSpace(tabLevel+1) + "}" + MyUtils.LINE_SEPARATOR);
		}
		sb.append(MartConfiguratorUtils.getTabSpace(tabLevel) + "oldDynamicDatasetList.size() = " + oldDynamicDatasetList.size() + MyUtils.LINE_SEPARATOR);
		for (OldDynamicDataset oldDynamicDataset : this.oldDynamicDatasetList) {
			sb.append(MartConfiguratorUtils.getTabSpace(tabLevel+1) + "oldDynamicDataset = {" + oldDynamicDataset);
			sb.append(MartConfiguratorUtils.getTabSpace(tabLevel+1) + "}" + MyUtils.LINE_SEPARATOR);
		}
		sb.append(MartConfiguratorUtils.getTabSpace(tabLevel) + "oldImportableList.size() = " + oldImportableList.size() + MyUtils.LINE_SEPARATOR);	
		for (OldImportable oldImportable : this.oldImportableList) {
			sb.append(MartConfiguratorUtils.getTabSpace(tabLevel+1) + "oldImportable = {" + oldImportable);
			sb.append(MartConfiguratorUtils.getTabSpace(tabLevel+1) + "}" + MyUtils.LINE_SEPARATOR);
		}
		sb.append(MartConfiguratorUtils.getTabSpace(tabLevel) + "oldExportableList.size() = " + oldExportableList.size() + MyUtils.LINE_SEPARATOR);	
		for (OldExportable oldExportable : this.oldExportableList) {
			sb.append(MartConfiguratorUtils.getTabSpace(tabLevel+1) + "oldExportable = {" + oldExportable);
			sb.append(MartConfiguratorUtils.getTabSpace(tabLevel+1) + "}" + MyUtils.LINE_SEPARATOR);
		}
		sb.append(MartConfiguratorUtils.getTabSpace(tabLevel) + "oldFilterPageList.size() = " + oldFilterPageList.size() + MyUtils.LINE_SEPARATOR);	
		for (OldFilterPage oldFilterPage : this.oldFilterPageList) {
			sb.append(MartConfiguratorUtils.getTabSpace(tabLevel+1) + "oldFilterPage = {" + oldFilterPage);
			sb.append(MartConfiguratorUtils.getTabSpace(tabLevel+1) + "}" + MyUtils.LINE_SEPARATOR);
		}
		sb.append(MartConfiguratorUtils.getTabSpace(tabLevel) + "oldAttributePageList.size() = " + oldAttributePageList.size() + MyUtils.LINE_SEPARATOR);	
		for (OldAttributePage oldAttributePage : this.oldAttributePageList) {
			sb.append(MartConfiguratorUtils.getTabSpace(tabLevel+1) + "oldAttributePage = {" + oldAttributePage);
			sb.append(MartConfiguratorUtils.getTabSpace(tabLevel+1) + "}" + MyUtils.LINE_SEPARATOR);
		}
		
		return sb.toString();
	}

	@Override
	public boolean equals(Object object) {
		if (this==object) {
			return true;
		}
		if((object==null) || (object.getClass()!= this.getClass())) {
			return false;
		}
		OldDatasetConfig oldDatasetConfig=(OldDatasetConfig)object;
		return (
			(this.dataset==oldDatasetConfig.dataset || (this.dataset!=null && dataset.equals(oldDatasetConfig.dataset))) &&
			(this.datasetID==oldDatasetConfig.datasetID || (this.datasetID!=null && datasetID.equals(oldDatasetConfig.datasetID))) &&
			(this.defaultDataset==oldDatasetConfig.defaultDataset || (this.defaultDataset!=null && defaultDataset.equals(oldDatasetConfig.defaultDataset))) &&
			(this.displayName==oldDatasetConfig.displayName || (this.displayName!=null && displayName.equals(oldDatasetConfig.displayName))) &&
			(this.description==oldDatasetConfig.description || (this.description!=null && description.equals(oldDatasetConfig.description))) &&
			(this.entryLabel==oldDatasetConfig.entryLabel || (this.entryLabel!=null && entryLabel.equals(oldDatasetConfig.entryLabel))) &&
			(this.interfaces==oldDatasetConfig.interfaces || (this.interfaces!=null && interfaces.equals(oldDatasetConfig.interfaces))) &&
			(this.internalName==oldDatasetConfig.internalName || (this.internalName!=null && internalName.equals(oldDatasetConfig.internalName))) &&
			(this.martUsers==oldDatasetConfig.martUsers || (this.martUsers!=null && martUsers.equals(oldDatasetConfig.martUsers))) &&
			(this.modified==oldDatasetConfig.modified || (this.modified!=null && modified.equals(oldDatasetConfig.modified))) &&
			(this.softwareVersion==oldDatasetConfig.softwareVersion || (this.softwareVersion!=null && softwareVersion.equals(oldDatasetConfig.softwareVersion))) &&
			(this.template==oldDatasetConfig.template || (this.template!=null && template.equals(oldDatasetConfig.template))) &&
			(this.type==oldDatasetConfig.type || (this.type!=null && type.equals(oldDatasetConfig.type))) &&
			(this.version==oldDatasetConfig.version || (this.version!=null && version.equals(oldDatasetConfig.version))) &&
			(this.visible==oldDatasetConfig.visible || (this.visible!=null && visible.equals(oldDatasetConfig.visible))) &&
			(this.oldMainTableList==oldDatasetConfig.oldMainTableList || (this.oldMainTableList!=null && oldMainTableList.equals(oldDatasetConfig.oldMainTableList))) &&
			(this.oldKeyList==oldDatasetConfig.oldKeyList || (this.oldKeyList!=null && oldKeyList.equals(oldDatasetConfig.oldKeyList))) &&
			(this.oldDynamicDatasetList==oldDatasetConfig.oldDynamicDatasetList || (this.oldDynamicDatasetList!=null && oldDynamicDatasetList.equals(oldDatasetConfig.oldDynamicDatasetList))) &&
			(this.oldImportableList==oldDatasetConfig.oldImportableList || (this.oldImportableList!=null && oldImportableList.equals(oldDatasetConfig.oldImportableList))) &&
			(this.oldExportableList==oldDatasetConfig.oldExportableList || (this.oldExportableList!=null && oldExportableList.equals(oldDatasetConfig.oldExportableList))) &&
			(this.oldAttributePageList==oldDatasetConfig.oldAttributePageList || (this.oldAttributePageList!=null && oldAttributePageList.equals(oldDatasetConfig.oldAttributePageList))) &&
			(this.oldFilterPageList==oldDatasetConfig.oldFilterPageList || (this.oldFilterPageList!=null && oldFilterPageList.equals(oldDatasetConfig.oldFilterPageList)))
		);
	}

	@Override
	public int hashCode() {
		int hash = MartConfiguratorConstants.HASH_SEED1;
		hash = MartConfiguratorConstants.HASH_SEED2 * hash + (null==dataset? 0 : dataset.hashCode());
		hash = MartConfiguratorConstants.HASH_SEED2 * hash + (null==datasetID? 0 : datasetID.hashCode());
		hash = MartConfiguratorConstants.HASH_SEED2 * hash + (null==defaultDataset? 0 : defaultDataset.hashCode());
		hash = MartConfiguratorConstants.HASH_SEED2 * hash + (null==displayName? 0 : displayName.hashCode());
		hash = MartConfiguratorConstants.HASH_SEED2 * hash + (null==description? 0 : description.hashCode());
		hash = MartConfiguratorConstants.HASH_SEED2 * hash + (null==entryLabel? 0 : entryLabel.hashCode());
		hash = MartConfiguratorConstants.HASH_SEED2 * hash + (null==interfaces? 0 : interfaces.hashCode());
		hash = MartConfiguratorConstants.HASH_SEED2 * hash + (null==internalName? 0 : internalName.hashCode());
		hash = MartConfiguratorConstants.HASH_SEED2 * hash + (null==martUsers? 0 : martUsers.hashCode());
		hash = MartConfiguratorConstants.HASH_SEED2 * hash + (null==modified? 0 : modified.hashCode());
		hash = MartConfiguratorConstants.HASH_SEED2 * hash + (null==softwareVersion? 0 : softwareVersion.hashCode());
		hash = MartConfiguratorConstants.HASH_SEED2 * hash + (null==template? 0 : template.hashCode());
		hash = MartConfiguratorConstants.HASH_SEED2 * hash + (null==type? 0 : type.hashCode());
		hash = MartConfiguratorConstants.HASH_SEED2 * hash + (null==version? 0 : version.hashCode());
		hash = MartConfiguratorConstants.HASH_SEED2 * hash + (null==visible? 0 : visible.hashCode());
		hash = MartConfiguratorConstants.HASH_SEED2 * hash + (null==oldMainTableList? 0 : oldMainTableList.hashCode());
		hash = MartConfiguratorConstants.HASH_SEED2 * hash + (null==oldKeyList? 0 : oldKeyList.hashCode());
		hash = MartConfiguratorConstants.HASH_SEED2 * hash + (null==oldDynamicDatasetList? 0 : oldDynamicDatasetList.hashCode());
		hash = MartConfiguratorConstants.HASH_SEED2 * hash + (null==oldImportableList? 0 : oldImportableList.hashCode());
		hash = MartConfiguratorConstants.HASH_SEED2 * hash + (null==oldExportableList? 0 : oldExportableList.hashCode());
		hash = MartConfiguratorConstants.HASH_SEED2 * hash + (null==oldAttributePageList? 0 : oldAttributePageList.hashCode());
		hash = MartConfiguratorConstants.HASH_SEED2 * hash + (null==oldFilterPageList? 0 : oldFilterPageList.hashCode());
		return hash;
	}
	
	public Dataset transformToDataset(String newDatasetName, TransformationHelper help) throws FunctionalException {
		String displayName = help.replaceAliases(this.displayName);
		String description = help.replaceAliases(this.description);
		Dataset dataset = new Dataset(newDatasetName, displayName, description, this.visible, true);
																	// always materialized (old system doesn't handled non-materialized)
		return dataset;
	}
	
	public Config transformToConfig() {
		Config config = new Config(this.dataset, this.dataset);	// Default name for the config is the dataset name (since there's only one config per dataset when transforming)
		return config;
	}
}