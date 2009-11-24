package org.biomart.transformation.oldXmlObjects;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


import org.biomart.common.general.exceptions.FunctionalException;
import org.biomart.common.general.exceptions.TechnicalException;
import org.biomart.common.general.utils.MyUtils;
import org.biomart.common.general.utils.XmlUtils;
import org.biomart.objects.MartConfiguratorConstants;
import org.biomart.objects.MartConfiguratorUtils;
import org.biomart.transformation.helpers.DatabaseCheck;
import org.biomart.transformation.helpers.DimensionPartition;
import org.biomart.transformation.helpers.ElementChildrenType;
import org.biomart.transformation.helpers.NamingConventionTableName;
import org.biomart.transformation.helpers.TransformationUtils;
import org.jdom.Element;


public class OldElement extends OldElementPlaceHolder /*implements Comparable<OldElement>, Comparator<OldElement>*/ {

	public static void main(String[] args) {}

	public static final List<String> propertyList = new ArrayList<String>(Arrays.asList(new String[] {
			"internalName", "hidden", "hideDisplay", "description", "displayName", 
			"tableConstraint", "key", "field",
			"pointerDataset", "pointerInterface", "checkForNulls"
	}));

	// To make it easier (no need to do instanceof
	protected Boolean isAttribute = null;	// If not then is filter
	
	//protected Boolean hidden = null;
	protected Boolean hideDisplay = null;
	protected String description = null;
	protected String displayName = null;
	protected String tableConstraint = null;
	protected String key = null;
	protected String field = null;
	protected String pointerDataset = null;
	protected String pointerInterface = null;
	protected String pointerElement = null;
	protected Boolean checkForNulls = null;

	protected Boolean pointer = null;
	
	protected Boolean hasChildren = null;
	protected Boolean hasSubChildren = null;
	protected ElementChildrenType firstChildrenType = null;
	
	protected boolean main;
	protected boolean dimension;
	
	// Only for filters
	protected boolean filterGroup;
	protected boolean pushAction;
	protected boolean option;
	protected boolean tree;
	
	protected NamingConventionTableName nctm = null;
	protected DimensionPartition dimensionPartition = null;
	
	protected OldElement(boolean isAttribute, Element jdomElement, String pointerElement) throws FunctionalException {
		this(isAttribute, jdomElement, jdomElement.getValue(), 
				jdomElement.getChildren(),
				jdomElement.getAttributeValue("internalName"),
				jdomElement.getAttributeValue("hidden"),
				jdomElement.getAttributeValue("hideDisplay"),
				jdomElement.getAttributeValue("description"),
				jdomElement.getAttributeValue("displayName"),
				jdomElement.getAttributeValue("tableConstraint"),
				jdomElement.getAttributeValue("key"),
				jdomElement.getAttributeValue("field"),
				jdomElement.getAttributeValue("pointerDataset"),
				jdomElement.getAttributeValue("pointerInterface"),
				pointerElement,
				jdomElement.getAttributeValue("checkForNulls")
		);
	}
			
	private OldElement(boolean isAttribute, Element jdomElement, String value, List<Element> childrenList,
			String internalName, String hidden, String hideDisplay, 
			String description, String displayName, String tableConstraint, String key, String field, 
			String pointerDataset, String pointerInterface, String pointerElement, String checkForNulls) throws FunctionalException {

		super(jdomElement, internalName);
			
		if (this.hidden) {	// assigned in OldNode
			this.valid = false;
			return;
		}
		
		this.isAttribute = isAttribute;

		this.hideDisplay = TransformationUtils.getBooleanValueFromString(hideDisplay, "hideDisplay");
		this.description = description;
		this.displayName = displayName;
		this.tableConstraint = tableConstraint;
		this.key = key;
		this.field = field;
		this.pointerDataset = pointerDataset;
		this.pointerInterface = pointerInterface;
		this.pointerElement = pointerElement;
		this.checkForNulls = TransformationUtils.getBooleanValueFromString(checkForNulls, "checkForNulls");
		
		this.hasChildren = childrenList!=null && childrenList.size()!=0;
		this.hasSubChildren = false;
		this.tree = false;
		if (this.hasChildren) {
			Element firstChild = childrenList.get(0);
			this.firstChildrenType = ElementChildrenType.getElementChildrenType(firstChild);
			
			// Will check for the 1st child that has subChildren (if any) and check the type of the subchild
			for(Element child : childrenList) {
				List<Element> subChildrenList = child.getChildren();
				this.hasSubChildren = subChildrenList!=null && subChildrenList.size()!=0;
				if (this.hasSubChildren) {
					ElementChildrenType subChildrenType = ElementChildrenType.getElementChildrenType(subChildrenList.get(0));
					this.tree = ElementChildrenType.OPTION_VALUE.equals(this.firstChildrenType) && 
					ElementChildrenType.OPTION_VALUE.equals(subChildrenType);
					break;
				}				
			}
		}
		
		this.pointer = this.pointerElement!=null && this.pointerInterface!=null && this.pointerDataset!=null;
		
		this.main = !this.pointer && tableConstraint!=null && TransformationUtils.isMain(tableConstraint);
		this.dimension = !this.pointer && tableConstraint!=null && !this.main;
	
		this.option = this instanceof OldOptionFilter;
		if (this.main || this.dimension) {
			this.nctm = new NamingConventionTableName(this.tableConstraint);
		} else {
			this.filterGroup = !pointer && !this.option; 
			MyUtils.checkStatusProgram(!this.filterGroup || hasChildren, XmlUtils.displayJdomElement(jdomElement));
		}
		
		this.dimensionPartition = new DimensionPartition(this.tableConstraint, this.key);		
		if (this.dimension) {
			this.dimensionPartition.searchForPotentialDimensionTablePartition();
		}
		
		if (!finalChecks()) {
//System.out.println(this.internalName + " ##########@@@@@@@@@@@@@######################*****************####################################");
System.err.println("######################### element has failed final checks" + this.internalName);
			this.valid = false;
		}
	}

	private boolean finalChecks() {
		if (!(this.filterGroup || this.pointer || 
				(MartConfiguratorUtils.hasValue(this.tableConstraint) && 
						MartConfiguratorUtils.hasValue(this.key) &&
						MartConfiguratorUtils.hasValue(this.field)))) {
			return false;
		}
		if (!(!this.pointer || 
				(MartConfiguratorUtils.hasValue(this.pointerElement) && 
						MartConfiguratorUtils.hasValue(this.pointerDataset) && 
						MartConfiguratorUtils.hasValue(this.pointerInterface)))) {
			return false;
		}
		return true;
	}

	public Boolean getHideDisplay() {
		return hideDisplay;
	}

	public String getDescription() {
		return description;
	}

	public String getDisplayName() {
		return displayName;
	}

	public String getTableConstraint() {
		return tableConstraint;
	}

	public String getKey() {
		return key;
	}

	public String getField() {
		return field;
	}

	public String getPointerDataset() {
		return pointerDataset;
	}

	public String getPointerElement() {
		return pointerElement;
	}

	public String getPointerInterface() {
		return pointerInterface;
	}

	public Boolean getCheckForNulls() {
		return checkForNulls;
	}

	public void setHideDisplay(Boolean hideDisplay) {
		this.hideDisplay = hideDisplay;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public void setDisplayName(String displayName) {
		this.displayName = displayName;
	}

	public void setTableConstraint(String tableConstraint) {
		this.tableConstraint = tableConstraint;
	}

	public void setKey(String key) {
		this.key = key;
	}

	public void setField(String field) {
		this.field = field;
	}

	public void setPointerInterface(String pointerInterface) {
		this.pointerInterface = pointerInterface;
	}

	public void setCheckForNulls(Boolean checkForNulls) {
		this.checkForNulls = checkForNulls;
	}


	public boolean hasChildren() {
		return hasChildren;
	}
	
	public boolean isMain() {
		return main;
	}

	public boolean isDimension() {
		return dimension;
	}

	public Boolean getPointer() {
		return pointer;
	}

	/*public String getDimensionTableIdentifier() {
		return dimensionTableIdentifier;
	}

	public List<DimensionNameAndKeyAndValue> getDimensionPartitionInfoCandidateList() {
		return dimensionPartitionInfoCandidateList;
	}*/

	@Override
	public String toString() {
		StringBuffer sb = new StringBuffer(
				super.toString() + ", " +
					"hideDisplay = " + hideDisplay + ", " +
					"description = " + description + ", " +
					"displayName = " + displayName + ", " +
					"tableConstraint = " + tableConstraint + ", " +
					"key = " + key + ", " +
					"field = " + field + ", " +
					"pointerDataset = " + pointerDataset + ", " +
					"pointerInterface = " + pointerInterface + ", " +
					"pointerElement = " + pointerElement + ", " +
					"checkForNulls = " + checkForNulls);
		sb.append(
				", " + "pointer = " + pointer + ", " +
				"main = " + main + ", " +
				"dimension = " + dimension + ", " +
				"filterGroup = " + filterGroup + ", " +
				
				"filterGroup = " + filterGroup + ", " +
				"pushAction = " + pushAction + ", " +
				"option = " + option + ", " +
				"tree = " + tree + ", " +
				
				"dimensionPartition = " + dimensionPartition + ", " +
				"hasChildren = " + hasChildren + ", " +
				"hasSubChildren = " + hasSubChildren);
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
		OldElement oldElement=(OldElement)object;
		return (
			(this.hideDisplay==oldElement.hideDisplay || (this.hideDisplay!=null && hideDisplay.equals(oldElement.hideDisplay))) &&
			(this.description==oldElement.description || (this.description!=null && description.equals(oldElement.description))) &&
			(this.displayName==oldElement.displayName || (this.displayName!=null && displayName.equals(oldElement.displayName))) &&
			(this.tableConstraint==oldElement.tableConstraint || (this.tableConstraint!=null && tableConstraint.equals(oldElement.tableConstraint))) &&
			(this.key==oldElement.key || (this.key!=null && key.equals(oldElement.key))) &&
			(this.field==oldElement.field || (this.field!=null && field.equals(oldElement.field))) &&
			(this.pointerDataset==oldElement.pointerDataset || (this.pointerDataset!=null && pointerDataset.equals(oldElement.pointerDataset))) &&
			(this.pointerInterface==oldElement.pointerInterface || (this.pointerInterface!=null && pointerInterface.equals(oldElement.pointerInterface))) &&
			(this.pointerElement==oldElement.pointerElement || (this.pointerElement!=null && pointerElement.equals(oldElement.pointerElement))) &&
			(this.checkForNulls==oldElement.checkForNulls || (this.checkForNulls!=null && checkForNulls.equals(oldElement.checkForNulls)))
		);
	}

	@Override
	public int hashCode() {
		int hash = MartConfiguratorConstants.HASH_SEED1;
		hash = MartConfiguratorConstants.HASH_SEED2 * hash + (null==hideDisplay? 0 : hideDisplay.hashCode());
		hash = MartConfiguratorConstants.HASH_SEED2 * hash + (null==description? 0 : description.hashCode());
		hash = MartConfiguratorConstants.HASH_SEED2 * hash + (null==displayName? 0 : displayName.hashCode());
		hash = MartConfiguratorConstants.HASH_SEED2 * hash + (null==tableConstraint? 0 : tableConstraint.hashCode());
		hash = MartConfiguratorConstants.HASH_SEED2 * hash + (null==key? 0 : key.hashCode());
		hash = MartConfiguratorConstants.HASH_SEED2 * hash + (null==field? 0 : field.hashCode());
		hash = MartConfiguratorConstants.HASH_SEED2 * hash + (null==pointerDataset? 0 : pointerDataset.hashCode());
		hash = MartConfiguratorConstants.HASH_SEED2 * hash + (null==pointerInterface? 0 : pointerInterface.hashCode());
		hash = MartConfiguratorConstants.HASH_SEED2 * hash + (null==pointerElement? 0 : pointerElement.hashCode());
		hash = MartConfiguratorConstants.HASH_SEED2 * hash + (null==checkForNulls? 0 : checkForNulls.hashCode());
		return hash;
	}

	/*@Override
	public int compare(OldElement oldElement1, OldElement oldElement2) {
		if (oldElement1==null && oldElement2!=null) {
			return -1;
		} else if (oldElement1!=null && oldElement2==null) {
			return 1;
		}
		int compare = CompareUtils.compareNull(oldElement1.internalName, oldElement2.internalName);
		if (compare!=0) {
			return compare;
		}
		compare = CompareUtils.compareNull(oldElement1.hidden, oldElement2.hidden);
		if (compare!=0) {
			return compare;
		}
		compare = CompareUtils.compareNull(oldElement1.hideDisplay, oldElement2.hideDisplay);
		if (compare!=0) {
			return compare;
		}
		compare = CompareUtils.compareNull(oldElement1.description, oldElement2.description);
		if (compare!=0) {
			return compare;
		}
		compare = CompareUtils.compareNull(oldElement1.displayName, oldElement2.displayName);
		if (compare!=0) {
			return compare;
		}
		compare = CompareUtils.compareNull(oldElement1.tableConstraint, oldElement2.tableConstraint);
		if (compare!=0) {
			return compare;
		}
		compare = CompareUtils.compareNull(oldElement1.key, oldElement2.key);
		if (compare!=0) {
			return compare;
		}
		compare = CompareUtils.compareNull(oldElement1.field, oldElement2.field);
		if (compare!=0) {
			return compare;
		}
		compare = CompareUtils.compareNull(oldElement1.pointerElement, oldElement2.pointerElement);
		if (compare!=0) {
			return compare;
		}
		compare = CompareUtils.compareNull(oldElement1.pointerDataset, oldElement2.pointerDataset);
		if (compare!=0) {
			return compare;
		}
		compare = CompareUtils.compareNull(oldElement1.pointerInterface, oldElement2.pointerInterface);
		if (compare!=0) {
			return compare;
		}
		return CompareUtils.compareNull(oldElement1.checkForNulls, oldElement2.checkForNulls);
	}

	@Override
	public int compareTo(OldElement oldElement) {
		return compare(this, oldElement);
	}*/

	private String toDebugString(String internalName) {
		return super.jdomElement.getName()+ ", " +internalName;
	}
	
	public boolean checkDatabase(String templateName, DatabaseCheck databaseCheck, String rangeName) throws TechnicalException, FunctionalException {
		 
		String datasetNameFromTableName = this.nctm.getDatasetName();
		if (null!=datasetNameFromTableName && !MyUtils.isEmpty(datasetNameFromTableName) && !datasetNameFromTableName.equals(rangeName)) {
			throw new FunctionalException("Mismatching dataset identifiers: datasetNameFromTableName = " + datasetNameFromTableName + 
					", rangeName = " + rangeName);
		}
		
		if (this.getTableConstraint()==null || this.getField()==null) {
			return false;
		}
		
		String tableName = null;
		if (this.main) {
			return true;
		} else {
			tableName = datasetNameFromTableName==null ? rangeName + "__" + this.getTableConstraint() : this.getTableConstraint();
			MyUtils.checkStatusProgram(tableName.split("__").length==3, "tableName = " + tableName, true);
		}
		return databaseCheck.checkDatabase(templateName, tableName, this.getField()) && 
		databaseCheck.checkDatabase(templateName, tableName, this.getKey());
	}

	public DimensionPartition getDimensionPartition() {
		return dimensionPartition;
	}

	public void setDimensionPartition(DimensionPartition dimensionPartition) {
		this.dimensionPartition = dimensionPartition;
	}

	public NamingConventionTableName getNctm() {
		return nctm;
	}

	public boolean isFilterGroup() {
		return filterGroup;
	}

	public boolean isOption() {
		return option;
	}

	public boolean isPushAction() {
		return pushAction;
	}

	public boolean isTree() {
		return tree;
	}

	public ElementChildrenType getFirstChildrenType() {
		return firstChildrenType;
	}
	
	/*public martConfigurator.objects.Element transformElement(
			CurrentPath currentPath, String xmlElementName, Boolean selectedByDefault) {
		
		martConfigurator.objects.Element element = new martConfigurator.objects.Element(
				this.internalName, this.displayName, this.description, !this.hideDisplay, xmlElementName,
				this.pointer ? null : currentPath.getLocationName(), this.pointer ? null : currentPath.getMartName(), 
				this.pointer ? null : currentPath.getMartVersion(), this.pointer ? null : currentPath.getDatasetName(), 
				this.pointer ? null : currentPath.getConfigName(), 
				this.pointer ? null : this.tableConstraint, this.pointer ? null : this.field, 
				null, selectedByDefault, this.pointer, this.pointer ? this.pointerElement : null, null);

		return element;
	}
	
	public void updateElement(Attribute attribute) {
		
	}*/
}