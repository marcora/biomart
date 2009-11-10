package org.biomart.transformation;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.biomart.common.general.exceptions.FunctionalException;
import org.biomart.common.general.exceptions.TechnicalException;
import org.biomart.common.general.utils.CompareUtils;
import org.biomart.common.general.utils.MyUtils;
import org.biomart.objects.MartConfiguratorConstants;
import org.biomart.objects.MartConfiguratorUtils;
import org.biomart.objects.helpers.PartitionReference;
import org.biomart.objects.objects.Attribute;
import org.biomart.objects.objects.Containee;
import org.biomart.objects.objects.Container;
import org.biomart.objects.objects.Dataset;
import org.biomart.objects.objects.Element;
import org.biomart.objects.objects.Filter;
import org.biomart.objects.objects.FilterDisplayType;
import org.biomart.objects.objects.GroupFilter;
import org.biomart.objects.objects.Part;
import org.biomart.objects.objects.PartitionTable;
import org.biomart.objects.objects.Range;
import org.biomart.objects.objects.SimpleFilter;
import org.biomart.objects.objects.Table;
import org.biomart.objects.objects.TableType;
import org.biomart.transformation.helpers.ContainerPath;
import org.biomart.transformation.helpers.DimensionPartition;
import org.biomart.transformation.helpers.DimensionPartitionNameAndKeyAndValue;
import org.biomart.transformation.helpers.NamingConventionTableName;
import org.biomart.transformation.helpers.TableNameAndKeyName;
import org.biomart.transformation.helpers.TransformationConstants;
import org.biomart.transformation.helpers.TransformationGeneralVariable;
import org.biomart.transformation.helpers.TransformationHelper;
import org.biomart.transformation.helpers.TransformationParameter;
import org.biomart.transformation.helpers.TransformationUtils;
import org.biomart.transformation.helpers.TransformationVariable;
import org.biomart.transformation.oldXmlObjects.OldAttribute;
import org.biomart.transformation.oldXmlObjects.OldAttributeDescription;
import org.biomart.transformation.oldXmlObjects.OldElement;
import org.biomart.transformation.oldXmlObjects.OldFilter;
import org.biomart.transformation.oldXmlObjects.OldFilterDescription;
import org.biomart.transformation.oldXmlObjects.OldSpecificOptionContent;


public abstract class ElementTransformation {

	// For debug/error messages
	public static final String ATTRIBUTE_STRING = "attribute";
	public static final String FILTER_STRING = "filter";
	
	protected TransformationGeneralVariable general = null;
	protected TransformationParameter params = null;
	protected TransformationVariable vars = null;
	protected TransformationHelper help = null;
	protected PointerTransformation pointerTransformation = null;
		
	protected Boolean isAttribute = null;

	public ElementTransformation (TransformationGeneralVariable general, TransformationParameter params, TransformationVariable vars, 
			TransformationHelper help, PointerTransformation pointerTransformation, boolean isAttribute) throws TechnicalException {
		this.general = general;
		this.params = params;
		this.vars = vars;
		this.help = help;
		this.pointerTransformation = pointerTransformation;
		
		this.isAttribute = isAttribute;
	}
	
	public void transformElementsDescriptions(ElementTransformation crossElementTransformation, 
			Map<ContainerPath, List<OldElement>> oldElementDescriptionMap) 
	throws FunctionalException, TechnicalException {
		
		// For each Container, transform its composing attributes and add them to the container
		for (Iterator<ContainerPath> it = oldElementDescriptionMap.keySet().iterator(); it.hasNext();) {
			ContainerPath containerPath = it.next();
			
			List<OldElement> oldElementDescriptionListTmp = oldElementDescriptionMap.get(containerPath);
			MyUtils.checkStatusProgram(null!=oldElementDescriptionListTmp);	// shouldn't be null here
			
			// Transform each attribute for the container
			for (OldElement oldElementDescription : oldElementDescriptionListTmp) {
				
				MyUtils.checkStatusProgram(!help.containsAliases(oldElementDescription.getInternalName()), 
						"oldElementDescription.getInternalName() = " + oldElementDescription.getInternalName());
				MyUtils.checkStatusProgram(!help.containsAliases(oldElementDescription.getTableConstraint()));
				MyUtils.checkStatusProgram(!help.containsAliases(oldElementDescription.getKey()));
				MyUtils.checkStatusProgram(!help.containsAliases(oldElementDescription.getField()));				
				
				// Determine it here because of the few exceptions: pointerFilter within AttributeCollection...
				boolean isCrossElement = 
					(this.isAttribute && oldElementDescription instanceof OldFilterDescription) ||
					(!this.isAttribute && oldElementDescription instanceof OldAttributeDescription);
				
				Element element = null; 
				if (!isCrossElement && isAttribute) {
					OldAttributeDescription oldAttributeDescription = (OldAttributeDescription)oldElementDescription;
					element = this.transformAttribute(containerPath.getCollectionContainer(), oldAttributeDescription, null, 
							vars.isTemplate() ? null : MartConfiguratorConstants.DEFAULT_PARTITION_TABLE_ROW, false);
				} else if (!isCrossElement && !isAttribute) {
					OldFilterDescription oldFilterDescription = (OldFilterDescription)oldElementDescription;
					element = this.transformFilter(containerPath.getCollectionContainer(), oldFilterDescription, null, null,
							vars.isTemplate() ? null : MartConfiguratorConstants.DEFAULT_PARTITION_TABLE_ROW, false, null);
				} else if (isCrossElement && isAttribute) {
					OldFilterDescription oldFilterDescription = (OldFilterDescription)oldElementDescription;
					element = crossElementTransformation.transformFilter(containerPath.getCollectionContainer(), oldFilterDescription, null, null,
							vars.isTemplate() ? null : MartConfiguratorConstants.DEFAULT_PARTITION_TABLE_ROW, false, null);
				} else if (isCrossElement && !isAttribute) {
					OldAttributeDescription oldAttributeDescription = (OldAttributeDescription)oldElementDescription;
					element = crossElementTransformation.transformAttribute(containerPath.getCollectionContainer(), oldAttributeDescription, null, 
							vars.isTemplate() ? null : MartConfiguratorConstants.DEFAULT_PARTITION_TABLE_ROW, false);
				}
				
				// For dimension partition, always the internalName and displayName in the partition table no matter what
				postProcessDimensionPartitionElements(element);
			}
		}
	}

	private void postProcessDimensionPartitionElements(Element element) {
		
		if (null!=element) {	// could be null if invalid element
			if (element instanceof GroupFilter) {
				GroupFilter groupFilter = (GroupFilter)element;
				for (SimpleFilter simpleFilter : groupFilter.getFilterList()) {	// May be none
					postProcessDimensionPartitionElement(simpleFilter);
				}
			}/* else if (element instanceof ListFilter) {
				ListFilter listFilter = (ListFilter)element;
				for (SimpleFilter cascadeChild : listFilter.getCascadeChildren()) {	// May be none
					postProcessDimensionPartitionElement(cascadeChild);
				}
			} else */{
				postProcessDimensionPartitionElement(element);
			}
		}
	}

	private void postProcessDimensionPartitionElement(Element element) {
		Range targetRange = element.getTargetRange();
		MyUtils.checkStatusProgram(targetRange!=null);
		if (targetRange.getPartitionTableSet().size()>1) {
			
			// Look for the dimension partition table
			PartitionTable dimensionPartitionTable = null;
			for (PartitionTable partitionTable : targetRange.getPartitionTableSet()) {
				if (!partitionTable.getMain()) {
					MyUtils.checkStatusProgram(dimensionPartitionTable==null);	// There can only be one dimension
					dimensionPartitionTable = partitionTable;
				}
			}
			MyUtils.checkStatusProgram(null!=dimensionPartitionTable);
			
			// Look for the row in the dimension partitiont table
			Integer dimensionRowNumber = null;
			for (Part part : targetRange.getPartSet()) {
				Integer dimensionRowNumberTmp = part.getRowNumber(dimensionPartitionTable);
				if (null==dimensionRowNumber) {
					dimensionRowNumber = dimensionRowNumberTmp;
				} else {
					MyUtils.checkStatusProgram(dimensionRowNumber.intValue()==dimensionRowNumberTmp.intValue());
																	// There can only be one dimension row (they are not merged)
				}
			}
			MyUtils.checkStatusProgram(null!=dimensionRowNumber);
			
			if (dimensionPartitionTable.getTotalColumns()==1) {
				int internalNameColumnNumber = dimensionPartitionTable.addColumn();
				int displayNameColumnNumber = dimensionPartitionTable.addColumn();
				MyUtils.checkStatusProgram(internalNameColumnNumber==TransformationConstants.DIMENSION_TABLE_INTERNAL_NAME_COLUMN_NUMBER);
				MyUtils.checkStatusProgram(displayNameColumnNumber==TransformationConstants.DIMENSION_TABLE_DISPLAY_NAME_COLUMN_NUMBER);
			}
			element.setName(updatePartitionTableAndReturnNewValue(
					dimensionPartitionTable, dimensionRowNumber, element.getName(), targetRange, TransformationConstants.DIMENSION_TABLE_INTERNAL_NAME_COLUMN_NUMBER));
			element.setDisplayName(updatePartitionTableAndReturnNewValue(
					dimensionPartitionTable, dimensionRowNumber, element.getDisplayName(), targetRange, TransformationConstants.DIMENSION_TABLE_DISPLAY_NAME_COLUMN_NUMBER));
		}
	}

	private String updatePartitionTableAndReturnNewValue(
			PartitionTable dimensionPartitionTable, int dimensionRowNumber, String value, Range targetRange, int columnNumber) {
		
		// Update value in partitionTable
		dimensionPartitionTable.updateValue(dimensionRowNumber, columnNumber, value);
		
		// Return new value (a partition table reference)
		PartitionReference dimensionPartitionReference = new PartitionReference(dimensionPartitionTable, columnNumber);
		return dimensionPartitionReference.toXmlString();
	}
	
	abstract Attribute transformAttribute(Container container, OldAttribute oldAttribute, 
			Attribute nonSpecificTemplateAttribute, Integer currentMainRow, boolean firstSpecific) 
	throws FunctionalException, TechnicalException;

	abstract Filter transformFilter(Container container, OldFilter oldAttribute, Filter groupTemplateFilter, 
			Filter nonSpecificTemplateFilter, Integer currentMainRow, boolean firstSpecific, Boolean forcedVisibility) 
	throws FunctionalException, TechnicalException;
	
	protected DimensionPartition getDimensionPartition(OldElement oldElement) {
		
		DimensionPartition dimensionPartition = null;
		if (vars.isTemplate()) {
			dimensionPartition = oldElement.getDimensionPartition();
			dimensionPartition.lookForPatternMatches();
		}
		Boolean hasDimensionPartition = dimensionPartition!=null && dimensionPartition.getPartition();
		if (hasDimensionPartition) {
			DimensionPartitionNameAndKeyAndValue dimensionPartitionNameAndKeyAndValue = 
				dimensionPartition.getDimensionPartitionNameAndKeyAndValue();
			DimensionPartition dimensionPartition2 = vars.getDimensionPartitionsMap().get(dimensionPartitionNameAndKeyAndValue);
			MyUtils.checkStatusProgram(null!=dimensionPartition2);	// could be null if table+field doesn't exist in DB but would have been discarded already
			oldElement.setDimensionPartition(dimensionPartition2);	// Should use 2 objects really
			dimensionPartition = dimensionPartition2;
		}
		return dimensionPartition;
	}

	protected Element getDimensionPartitionTemplateElement(Container container, Element nonSpecificTemplateElement, DimensionPartition dimensionPartition) {
		Element dimensionPartitionTemplateElement = null;
		Boolean hasDimensionPartition = dimensionPartition!=null && dimensionPartition.getPartition();
		if (hasDimensionPartition) { 
				Containee containee = nonSpecificTemplateElement!=null ? nonSpecificTemplateElement : container;
			dimensionPartitionTemplateElement = isAttribute ?	// try to allocate a template (not garanteed, if 1st one for instance) 
					dimensionPartition.getAttributeForContainee(containee) : dimensionPartition.getFilterForContainee(containee);
		}
		return dimensionPartitionTemplateElement;
	}
	
	protected String allocateName(OldElement oldElement, Element nonSpecificTemplateElement) {
		String elementName = null;
		if (nonSpecificTemplateElement!=null) {
			elementName = nonSpecificTemplateElement.getName();
		} else {
			elementName = oldElement.getInternalName();
		}
		return elementName;
	}
	
	protected Element transformElementIndependently(OldElement oldElement, Integer currentMainRow, 
			List<Integer> mainRowsList, DimensionPartition dimensionPartition, String elementName, Boolean forcedVisibility,
			FilterDisplayType nonSpecificFilterDisplayType) throws FunctionalException, TechnicalException {
		
		Element newElement = isAttribute ? 
				createNewAttribute((OldAttribute)oldElement, currentMainRow, mainRowsList, dimensionPartition) : 
				createNewFilter((OldFilter)oldElement, currentMainRow, mainRowsList, dimensionPartition, forcedVisibility, nonSpecificFilterDisplayType);
		
		String warningMessage = "No combination of table+key+field exists in the database for " + 
		(isAttribute ? ATTRIBUTE_STRING : FILTER_STRING) + ": " + 
		elementName + " (" + oldElement.getTableConstraint() + ", " + oldElement.getKey() + ", " + oldElement.getField() + ")";		
		if (!TransformationUtils.checkForWarning(newElement==null, 	// elements with invalid table+field combination for instance 
				vars.getObsoleteElementList(), warningMessage)) {
			return null;
		}		
		return newElement;
	}
	
	abstract Attribute createNewAttribute(OldAttribute oldAttribute, Integer currentMainRowNumber, List<Integer> mainRowsList,
			DimensionPartition dimensionPartition) throws FunctionalException, TechnicalException;
	
	abstract Filter createNewFilter(OldFilter oldFilter, Integer currentMainRowNumber, List<Integer> mainRowsList,
			DimensionPartition dimensionPartition, Boolean forcedVisibility, FilterDisplayType nonSpecificFilterDisplayType) 
	throws FunctionalException, TechnicalException;
	
	protected List<Integer> checkDatabase(
			OldElement oldElement, Integer currentMainRowNumber, PartitionTable mainPartitionTable) throws TechnicalException, FunctionalException {
		
		List<Integer> mainRowsList = new ArrayList<Integer>();
		
		// Add main partition range (do it before dm partition so we can disregard invalid table+field combinations)
		if (currentMainRowNumber!=null) {
			String currentMainRowName = mainPartitionTable.getRowName(currentMainRowNumber);
			if (!vars.isTemplate() || oldElement.checkDatabase(
					params.getTemplateName(), general.getDatabaseCheck(), currentMainRowName)) {	// only for template (no access to DB otherwise)
				mainRowsList.add(currentMainRowNumber);
			}
		} else {
			MyUtils.checkStatusProgram(vars.isTemplate());
			
			Map<String, Integer> mapRowNameToRowNumber = mainPartitionTable.getRowNameToRowNumberMap();
			for (Iterator<String> it = mapRowNameToRowNumber.keySet().iterator(); it.hasNext();) {
				String mainRowName = it.next();
				Integer mainRowNumber = mapRowNameToRowNumber.get(mainRowName);
				
				if (!oldElement.getPointer()) {
					if (!oldElement.checkDatabase(params.getTemplateName(), general.getDatabaseCheck(), mainRowName)) {
						continue;
					}
				}
				mainRowsList.add(mainRowNumber);		
			}
		}
		return mainRowsList;
	}
	
	protected void updateRangeWithMainPartition(OldElement oldElement, Integer currentMainRowNumber, List<Integer> mainRowsList,
			PartitionTable mainPartitionTable, Element newElement, Boolean forcedVisibility) throws TechnicalException, FunctionalException {
		
		// At this point, can't be null or empty (or would have been disregarded before)
		MyUtils.checkStatusProgram(mainRowsList!=null && !mainRowsList.isEmpty());
		
		Boolean visible = !oldElement.getHideDisplay();	// get visibility
		
		// If visibility is forced, set it as specified (for filters among a group for instance, only the group is visible)
		if (null!=forcedVisibility) {
			visible = forcedVisibility;
		}
		
		// Add main partition range (do it before dm partition so we can disregard invalid table+field combinations)
		if (currentMainRowNumber!=null) {
			MyUtils.checkStatusProgram(mainRowsList.size()==1 && mainRowsList.get(0).intValue()==currentMainRowNumber);	//TODO get rid of currentMainRowNumber and only use the list
			newElement.getTargetRange().addRangePartitionRow(mainPartitionTable, currentMainRowNumber, visible);
		} else {
			MyUtils.checkStatusProgram(vars.isTemplate());
			Map<String, Integer> mapRowNameToRowNumber = mainPartitionTable.getRowNameToRowNumberMap();
			for (Iterator<String> it = mapRowNameToRowNumber.keySet().iterator(); it.hasNext();) {
				String mainRowName = it.next();
				Integer mainRowNumber = mapRowNameToRowNumber.get(mainRowName);
				if (mainRowsList.contains(mainRowNumber)) {
					newElement.getTargetRange().addRangePartitionRow(mainPartitionTable, mainRowNumber, visible);
				}
			}
		}
	}
	protected PartitionTable updateRangeWithDimensionPartition(DimensionPartition dimensionPartition, Element newElement) throws TechnicalException {
		return updateRangeWithDimensionPartition(dimensionPartition, newElement, null);
	}
	protected PartitionTable updateRangeWithDimensionPartition(DimensionPartition dimensionPartition, Element newElement,
			OldFilter oldFilter) throws TechnicalException {
		PartitionTable dimensionPartitionTable = null;
		Integer dimensionPartitionTableRowNumber = null;
		if (dimensionPartition!=null && dimensionPartition.getPartition()) {	// == has dimension partition
			dimensionPartitionTable = dimensionPartition.getDimensionPartitionTable();
			dimensionPartitionTableRowNumber = dimensionPartition.getDimensionTableRowNumber();
			MyUtils.checkStatusProgram(dimensionPartitionTable!=null && dimensionPartitionTableRowNumber!=null,
					dimensionPartitionTable + ", " + dimensionPartitionTableRowNumber + ", " + 
					newElement.getTargetRange().getXmlValue() + ", " + newElement.getName() + ", " + 
					(oldFilter instanceof OldSpecificOptionContent ? ((OldSpecificOptionContent)oldFilter).getRangeInternalName() : null));
			
			// Add associated dimension to the attribute (on top of the main partition)
			newElement.addOtherPartitionTable(dimensionPartitionTable);
			
			// Add parts accordingly
			newElement.getTargetRange().addRangePartitionRow(dimensionPartitionTable, dimensionPartitionTableRowNumber);
		}
		return dimensionPartitionTable;
	}

	protected String computeTableName(OldElement oldElement, DimensionPartition dimensionPartition, 
			String mainPartitionTableName, PartitionTable dimensionPartitionTable) throws FunctionalException, TechnicalException {
		
		// Table name: handling of dimension table partition
		if (help.containsAliases(oldElement.getTableConstraint())) {
			throw new FunctionalException("Unhandled");
		}
		
		String key = oldElement.getKey();
		String tableShortName = null;
		if (oldElement.isMain()) {
			MyUtils.checkStatusProgram(dimensionPartition==null || !dimensionPartition.getPartition());
			tableShortName = vars.getMainTableShortNameMap(key);
		} else if (vars.isTemplate() && dimensionPartition!=null && dimensionPartition.getPartition()) {	// = no dimension table partition
			tableShortName = TransformationUtils.generateTableShortNameWhenDimensionPartition(dimensionPartition, dimensionPartitionTable);
		} else {
			tableShortName = oldElement.getNctm().getTableShortName();
		}
		
		if (tableShortName==null) {
			return null;
		}//MyUtils.checkStatusProgram(null!=tableShortName, oldElement.getTableConstraint() + ", " + key + ", " + vars.getKeyNameToMainTableShortNameMap());
		
		String tableName = new NamingConventionTableName(
				vars.getDataset().getName(), tableShortName, oldElement.getNctm().getTableType()).generateTableName();

		if (!vars.isTemplate()) {
			updateTables(tableName, oldElement.isMain(), oldElement.getTableConstraint(), key, oldElement.getField());
		} else {
			// Make sure the table exist (it should here)
			MyUtils.checkStatusProgram(vars.getTableFromNameAndKey(new TableNameAndKeyName(tableName, key))!=null);
		}
		return tableName;
	}

	/**
	 * Create table if does not exist, add field if not already present
	 * @param field
	 * @param sourceDescription
	 * @throws Exception
	 */
	private void updateTables(String newTableName, boolean mainTable, 
			String tableConstraint, String keyName, String fieldName) throws TechnicalException {
		
		MyUtils.checkStatusProgram(tableConstraint!=null && keyName!=null && fieldName!=null);
		
		Dataset dataset = vars.getDataset();
		List<Table> tableList = dataset.getTableList();
		Table table = null;
		for (Table tableTmp : tableList) {
			if (newTableName.equalsIgnoreCase(tableTmp.getName()) && keyName.equalsIgnoreCase(tableTmp.getKey())) {
				table = tableTmp;
				break;
			}
		}
		
		// Create table if doesn't exist yet (can't be for a main)
		if (null==table) {
			MyUtils.checkStatusProgram(!mainTable, newTableName + ", " + tableConstraint + ", " + keyName + ", " + fieldName);
			table = new Table(newTableName, vars.getMainPartitionTable(), false, TableType.TARGET, 
					keyName, new HashSet<String>(Arrays.asList(new String[] {fieldName, keyName})));
			table.getRange().addRangePartitionRow(vars.getDefaultPT(), MartConfiguratorConstants.DEFAULT_PARTITION_TABLE_ROW);
			dataset.addTable(table);
		} else {
			HashSet<String> fields = table.getFields();
			fields.add(fieldName);
		}
	}

	protected void assignSimpleProperties(OldElement oldElement, Element newElement, String tableName) throws FunctionalException {
		
		Boolean pointer = oldElement.getPointer();
		newElement.setPointer(pointer);
		if (!pointer) {
			String displayName = help.replaceAliases(oldElement.getDisplayName());
			String description = help.replaceAliases(oldElement.getDescription());
		
			newElement.setDisplayName(displayName);
			newElement.setDescription(description);
			if (!(newElement instanceof GroupFilter)) {
				newElement.setVisible(null);	// not applicable for elements: in targetRange, except for groupfilter (doesn't have a range)
			}
			
			newElement.setLocationName(vars.getCurrentPath().getLocationName());
			newElement.setMartName(vars.getCurrentPath().getMartName());
			newElement.setVersion(vars.getCurrentPath().getMartVersion());
			newElement.setDatasetName(vars.getCurrentPath().getDatasetName());
			newElement.setConfigName(vars.getCurrentPath().getConfigName());
			
			String keyName = help.replaceAliases(oldElement.getKey());
			String fieldName = help.replaceAliases(oldElement.getField());
			
			newElement.setTableName(tableName);
			newElement.setKeyName(keyName);
			newElement.setFieldName(fieldName);
		}
	}
	
	protected Element updateTemplateElement(Container container, Element nonSpecificTemplateElement, Integer currentMainRow, 
			boolean firstSpecific, Element dimensionPartitionTemplateAttribute, String elementName, 
			Element pushActionTemplateFilter, Element newElement) 
	throws FunctionalException, TechnicalException {
		
		// Element does not already exists
		if (null==nonSpecificTemplateElement && null==dimensionPartitionTemplateAttribute && pushActionTemplateFilter==null) {
			Element elementTmp = isAttribute ? vars.getAttributeMap().get(elementName) : vars.getFilterMap().get(elementName);
			TransformationUtils.checkForWarning(elementTmp!=null, vars.getNameConflictWarningList(),
					(isAttribute ? ATTRIBUTE_STRING : FILTER_STRING) + elementName + " appears more than once");	
			
			// keep newElement as is
		} else if (null!=pushActionTemplateFilter) {
			MyUtils.checkStatusProgram(nonSpecificTemplateElement==null && dimensionPartitionTemplateAttribute==null);
			MyUtils.checkStatusProgram(!vars.isTemplate() || currentMainRow==null);	// assumption: no part-specificity in such cases
			MyUtils.checkStatusProgram(pushActionTemplateFilter instanceof SimpleFilter && newElement instanceof SimpleFilter,
					pushActionTemplateFilter.getClass() + ", " + newElement.getClass());
			
			SimpleFilter newSimpleFilter = (SimpleFilter)newElement;
			SimpleFilter pushActionSimpleFilter = (SimpleFilter)pushActionTemplateFilter;
			MyUtils.checkStatusProgram(pushActionSimpleFilter.getFilterData()!=null);
			
			// Very little to do here, it's actually the newFilter, 
			// the only interesting information that the templateFilter has is the dataFolderPath
			newSimpleFilter.copyDataRelatedInformation(pushActionSimpleFilter);
			
			//newElement = updatePushActionTemplateFilter((ListFilter)pushActionTemplateFilter, (ListFilter)newElement);
		} else if (null!=nonSpecificTemplateElement) {
			MyUtils.checkStatusProgram(currentMainRow!=null);
			MyUtils.checkStatusProgram(pushActionTemplateFilter==null && dimensionPartitionTemplateAttribute==null);
			
			newElement = isAttribute ? 
					updateNonSpecificTemplateAttribute((Attribute)nonSpecificTemplateElement, (Attribute)newElement, currentMainRow, firstSpecific) :
					updateNonSpecificTemplateFilter((Filter)nonSpecificTemplateElement, (Filter)newElement, currentMainRow, firstSpecific);
		} else if (null!=dimensionPartitionTemplateAttribute) {
			MyUtils.errorProgram("Not handled: we don't bring together rows of the dimension table");
		} else {
			MyUtils.errorProgram();
		}
	
		// Add element to tree and map if new template or if updating a pushAction (in which case template is almost an empty shell and wasn't added before)
		if (null==nonSpecificTemplateElement && null==dimensionPartitionTemplateAttribute) {
			if (isAttribute) {
				container.addAttribute((Attribute)newElement);
				vars.getAttributeMap().put(elementName, (Attribute)newElement);
			} else {
				container.addFilter((Filter)newElement);
				vars.getFilterMap().put(elementName, (Filter)newElement);
			}
		}
		
		return newElement;
	}
	
	public void throwForbiddenSpecificityException(Element templateElement, Element newElement) throws FunctionalException {
		throw new FunctionalException("Forbidden specificity, " +
				"templateElement = " + MartConfiguratorUtils.displayJdomElement(templateElement.generateXml()) +
				", newElement = " + MartConfiguratorUtils.displayJdomElement(newElement.generateXml()));
	}
	
	/*protected void updatePushActionTemplateElement(Element templateElement, Element newElement, 
			Integer currentMainRow, boolean firstSpecific) throws FunctionalException, TechnicalException {
		
		PartitionTable mainPartitionTable = vars.getMainPartitionTable();
		
		if (firstSpecific) {
			// Erase main partition rows since they are specified one by one
			templateElement.getTargetRange().removePartition(mainPartitionTable);
		}
		
		// Add row
		Range targetRange = templateElement.getTargetRange();
		MyUtils.checkStatusProgram(!targetRange.contains(mainPartitionTable, currentMainRow));
		targetRange.addRangePartitionRow(mainPartitionTable, currentMainRow);
		
		// Check that all these properties are the same, exception made for the first specific which can define the 1st values for some properties
		if (templateElement.getTableName()==null || 
				!templateElement.getTableName().equals(newElement.getTableName())) {	// Can't even be null
			throw new FunctionalException("Unhandled");
		}	//TODO what if pointer?
		if (!TransformationUtils.checkValidSpecificityString(templateElement.getLocationName(), newElement.getLocationName(), firstSpecific) ||
				!TransformationUtils.checkValidSpecificityString(templateElement.getMartName(), newElement.getMartName(), firstSpecific) ||
				!TransformationUtils.checkValidSpecificityInteger(templateElement.getVersion(), newElement.getVersion(), firstSpecific) ||
				!TransformationUtils.checkValidSpecificityString(templateElement.getDatasetName(), newElement.getDatasetName(), firstSpecific) ||
				!TransformationUtils.checkValidSpecificityString(templateElement.getConfigName(), newElement.getConfigName(), firstSpecific) ||
				!TransformationUtils.checkValidSpecificityBoolean(templateElement.getPointer(), newElement.getPointer(), firstSpecific) ||
				!TransformationUtils.checkValidSpecificityBoolean(templateElement.getSelectedByDefault(), newElement.getSelectedByDefault(), firstSpecific) ||
				!TransformationUtils.checkValidSpecificityString(templateElement.getPointedElementName(), newElement.getPointedElementName(), firstSpecific) ||
				!TransformationUtils.checkValidSpecificityBoolean(templateElement.getVisible(), newElement.getVisible(), firstSpecific) ||
				!TransformationUtils.checkValidSpecificityBoolean(templateElement.getCheckForNulls(), newElement.getCheckForNulls(), firstSpecific)) {
			throw new FunctionalException("Forbidden specificity, " +
					"templateAttribute = " + MartConfiguratorUtils.displayJdomElement(templateElement.generateXml()) +
					", newAttribute = " + MartConfiguratorUtils.displayJdomElement(newElement.generateXml()));
		}
		
		// Update properties that are allowed to be part specific
		Set<Integer> mainRowsSet = targetRange.getMainRowsSet();
		templateElement.setName(updateSpecificProperty(
				currentMainRow, templateElement.getName(), newElement.getName(), firstSpecific, mainRowsSet));
		templateElement.setKeyName(updateSpecificProperty(
				currentMainRow, templateElement.getKeyName(), newElement.getKeyName(), firstSpecific, mainRowsSet));
		templateElement.setDescription(updateSpecificProperty(
				currentMainRow, templateElement.getDescription(), newElement.getDescription(), firstSpecific, mainRowsSet));
		templateElement.setFieldName(updateSpecificProperty(
				currentMainRow, templateElement.getFieldName(), newElement.getFieldName(), firstSpecific, mainRowsSet));
		templateElement.setDisplayName(updateSpecificProperty(
				currentMainRow, templateElement.getDisplayName(), newElement.getDisplayName(), firstSpecific, mainRowsSet));
	}*/
	
	protected void updateNonSpecificTemplateElement(Element templateElement, Element newElement, 
			Integer currentMainRow, boolean firstSpecific) throws FunctionalException, TechnicalException {
		
		PartitionTable mainPartitionTable = vars.getMainPartitionTable();
		
		if (firstSpecific) {
			// Erase main partition rows since they are specified one by one
			templateElement.getTargetRange().removePartition(mainPartitionTable);
		}
		
		// Add row
		Range targetRange = templateElement.getTargetRange();
		MyUtils.checkStatusProgram(!targetRange.contains(mainPartitionTable, currentMainRow));
		targetRange.addRangePartitionRow(mainPartitionTable, currentMainRow);
		
		// Check that all these properties are the same, exception made for the first specific which can define the 1st values for some properties
		if (templateElement.getTableName()==null || 
				!templateElement.getTableName().equals(newElement.getTableName())) {	// Can't even be null
			throw new FunctionalException("Unhandled");
		}	//TODO what if pointer?
		if (!TransformationUtils.checkValidSpecificityString(templateElement.getLocationName(), newElement.getLocationName(), firstSpecific) ||
				!TransformationUtils.checkValidSpecificityString(templateElement.getMartName(), newElement.getMartName(), firstSpecific) ||
				!TransformationUtils.checkValidSpecificityInteger(templateElement.getVersion(), newElement.getVersion(), firstSpecific) ||
				!TransformationUtils.checkValidSpecificityString(templateElement.getDatasetName(), newElement.getDatasetName(), firstSpecific) ||
				!TransformationUtils.checkValidSpecificityString(templateElement.getConfigName(), newElement.getConfigName(), firstSpecific) ||
				!TransformationUtils.checkValidSpecificityBoolean(templateElement.getPointer(), newElement.getPointer(), firstSpecific) ||
				!TransformationUtils.checkValidSpecificityBoolean(templateElement.getSelectedByDefault(), newElement.getSelectedByDefault(), firstSpecific) ||
				!TransformationUtils.checkValidSpecificityString(templateElement.getPointedElementName(), newElement.getPointedElementName(), firstSpecific) ||
				!TransformationUtils.checkValidSpecificityBoolean(templateElement.getVisible(), newElement.getVisible(), firstSpecific) ||
				!TransformationUtils.checkValidSpecificityBoolean(templateElement.getCheckForNulls(), newElement.getCheckForNulls(), firstSpecific)) {
			throw new FunctionalException("Forbidden specificity, " +
					"templateAttribute = " + MartConfiguratorUtils.displayJdomElement(templateElement.generateXml()) +
					", newAttribute = " + MartConfiguratorUtils.displayJdomElement(newElement.generateXml()));
		}
		
		// Update properties that are allowed to be part specific
		Set<Integer> mainRowsSet = targetRange.getMainRowsSet();
		templateElement.setName(updateSpecificProperty(
				currentMainRow, templateElement.getName(), newElement.getName(), firstSpecific, mainRowsSet));
		templateElement.setDisplayName(updateSpecificProperty(
				currentMainRow, templateElement.getDisplayName(), newElement.getDisplayName(), firstSpecific, mainRowsSet));
		templateElement.setKeyName(updateSpecificProperty(
				currentMainRow, templateElement.getKeyName(), newElement.getKeyName(), firstSpecific, mainRowsSet));
		templateElement.setDescription(updateSpecificProperty(
				currentMainRow, templateElement.getDescription(), newElement.getDescription(), firstSpecific, mainRowsSet));
		templateElement.setFieldName(updateSpecificProperty(
				currentMainRow, templateElement.getFieldName(), newElement.getFieldName(), firstSpecific, mainRowsSet));
	}

	abstract Attribute updateNonSpecificTemplateAttribute(Attribute templateAttribute, Attribute newAttribute, 
			Integer currentMainRow, boolean firstSpecific) throws FunctionalException, TechnicalException;
	
	abstract Filter updateNonSpecificTemplateFilter(Filter templateFilter, Filter newFilter, 
			Integer currentMainRow, boolean firstSpecific) throws FunctionalException, TechnicalException;
	
	protected String updateSpecificProperty(Integer currentMainRow, String templateProperty, String newProperty, 
			boolean firstSpecific, Set<Integer> mainRowsSet) throws FunctionalException {
		
		PartitionTable mainPartitionTable = vars.getMainPartitionTable();
		
		String property = null;
		if (templateProperty==null && newProperty==null) {
			property = null;
		} else if (templateProperty!=null && newProperty==null) {
			property = templateProperty;
		} else if (templateProperty==null && newProperty!=null) {
			property = newProperty;
		} else if (templateProperty!=null && newProperty!=null) {
			
			boolean templateIsReference = MartConfiguratorUtils.isPartitionReference(templateProperty);
			boolean newIsReference = MartConfiguratorUtils.isPartitionReference(newProperty);
			
			PartitionReference templatePartitionReference = templateIsReference ? PartitionReference.fromString(templateProperty) : null;
			PartitionReference newPartitionReference = newIsReference ? PartitionReference.fromString(newProperty) : null;
			
			boolean referencesInTemplate = MartConfiguratorUtils.containsPartitionReferences(templateProperty);
			boolean referencesInNew = MartConfiguratorUtils.containsPartitionReferences(newProperty);
			
			
			if ((!templateIsReference && referencesInTemplate) || (!newIsReference && referencesInNew)) {
				throw new FunctionalException("Unhandled, " + templateProperty + ", " + newProperty);
			}
			
			if (templateIsReference && newIsReference) {
				if (!templatePartitionReference.equals(newPartitionReference)) {
					throw new FunctionalException("Unhandled");
				} else {
					property = templateProperty; // either, they're both the same
				}
			} else if (!templateIsReference && newIsReference) {
				throw new FunctionalException("Unhandled");
			} else if (templateIsReference && !newIsReference) {
				
				if (firstSpecific) {
					throw new FunctionalException("Unhandled");
				}
				
				MyUtils.checkStatusProgram(templatePartitionReference!=null);
				int existingColumn = templatePartitionReference.getColumn();
								
				// Update partition table with new value: the new one (make sure empty slot)
				mainPartitionTable.updateValue(currentMainRow, existingColumn, newProperty);
				
				property = templateProperty;	// becomes a reference
			} else if (!templateIsReference && !newIsReference) {
				
				if (CompareUtils.same(templateProperty, newProperty)) {
					property = templateProperty; // either, they're both the same
				} else {
					
					/*System.out.println("adding..." + templateProperty + ", " + newProperty);
					//MyUtils.pressKeyToContinue();
					MyUtils.errorProgram();*/
					
					int newColumn = mainPartitionTable.addColumn();
					
					PartitionReference mainPartitionReference = new PartitionReference(mainPartitionTable, newColumn);
					
					// Update the partition table: all previous rows get templateProperty, the new one gets newProperty
					for (int mainRow : mainRowsSet) {						
						if (mainRow!=currentMainRow) {	// It's already part of the rows
							mainPartitionTable.updateValue(mainRow, newColumn, templateProperty);
						}
					}
					mainPartitionTable.updateValue(currentMainRow, newColumn, newProperty);
					
					property = mainPartitionReference.toXmlString();
				}
			}
		}
		
		return property;
	}
}
