package org.biomart.transformation;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.biomart.common.general.exceptions.FunctionalException;
import org.biomart.common.general.exceptions.TechnicalException;
import org.biomart.common.general.utils.MyUtils;
import org.biomart.common.general.utils.XmlUtils;
import org.biomart.objects.MartConfiguratorConstants;
import org.biomart.objects.MartConfiguratorUtils;
import org.biomart.objects.data.FilterData;
import org.biomart.objects.data.FilterDataRow;
import org.biomart.objects.data.TreeFilterData;
import org.biomart.objects.data.TreeFilterDataRow;
import org.biomart.objects.objects.Attribute;
import org.biomart.objects.objects.Config;
import org.biomart.objects.objects.Container;
import org.biomart.objects.objects.Filter;
import org.biomart.objects.objects.GroupFilter;
import org.biomart.objects.objects.Part;
import org.biomart.objects.objects.PartitionTable;
import org.biomart.objects.objects.Range;
import org.biomart.objects.objects.SimpleFilter;
import org.biomart.objects.objects.types.FilterDisplayType;
import org.biomart.transformation.helpers.DimensionPartition;
import org.biomart.transformation.helpers.ElementChildrenType;
import org.biomart.transformation.helpers.FilterOldDisplayType;
import org.biomart.transformation.helpers.FilterOldStyle;
import org.biomart.transformation.helpers.RelationalInfo;
import org.biomart.transformation.helpers.TransformationConstants;
import org.biomart.transformation.helpers.TransformationGeneralVariable;
import org.biomart.transformation.helpers.TransformationHelper;
import org.biomart.transformation.helpers.TransformationParameter;
import org.biomart.transformation.helpers.TransformationUtils;
import org.biomart.transformation.helpers.TransformationVariable;
import org.biomart.transformation.oldXmlObjects.OldAttribute;
import org.biomart.transformation.oldXmlObjects.OldEmptySpecificElementContent;
import org.biomart.transformation.oldXmlObjects.OldFilter;
import org.biomart.transformation.oldXmlObjects.OldFilterDescription;
import org.biomart.transformation.oldXmlObjects.OldOptionFilter;
import org.biomart.transformation.oldXmlObjects.OldOptionValue;
import org.biomart.transformation.oldXmlObjects.OldPushAction;
import org.biomart.transformation.oldXmlObjects.OldSpecificFilterContent;
import org.biomart.transformation.oldXmlObjects.OldSpecificOptionContent;


public class FilterTransformation extends ElementTransformation {

	private Map<String, SimpleFilter> pushActionMap = null;
	
	public FilterTransformation (TransformationGeneralVariable general, TransformationParameter params, TransformationVariable vars, 
			TransformationHelper help, PointerTransformation pointerTransformation) throws TechnicalException {	
		super(general, params, vars, help, pointerTransformation, false);
		
		this.pushActionMap = new HashMap<String, SimpleFilter>();
	}

	@Override
	protected Attribute transformAttribute(Container container, OldAttribute oldAttribute, Attribute nonSpecificTemplateAttribute, 
			Integer currentMainRow, boolean firstSpecific) throws FunctionalException, TechnicalException {
		MyUtils.errorProgram("Shouldn't be here");
		return null;
	}
	
	@Override
	protected Filter transformFilter(Container container, OldFilter oldFilter, Filter groupTemplateFilter, Filter nonSpecificTemplateFilter,
			Integer currentMainRowNumber, boolean firstSpecific,
			Boolean forcedVisibility	// null if not forced, otherwise whatever the value given is
			) throws FunctionalException, TechnicalException {
		
		System.out.println(XmlUtils.displayJdomElement(oldFilter.getJdomElement()) + ", " + firstSpecific);
		
		// Disregard elements with no match in the DB
		List<Integer> mainRowsList = null;
		if (oldFilter.getPointer() || oldFilter.isMain() || oldFilter.isDimension()) {
			mainRowsList = checkDatabase(oldFilter, currentMainRowNumber);
			if (mainRowsList.isEmpty()) {
				return null;
			}
		}
		System.out.println("\tmainRowsList = " + mainRowsList);
		
		// Look for dimension partitions (only consider partitions if dealing with a template)
		DimensionPartition dimensionPartition = getDimensionPartition(oldFilter);
		Filter dimensionPartitionTemplateFilter = (Filter)getDimensionPartitionTemplateElement(container, nonSpecificTemplateFilter, dimensionPartition);
		
		String filterName = allocateName(oldFilter, nonSpecificTemplateFilter);
	
		System.out.println("\toldFilter.getInternalName() = " + oldFilter.getInternalName() + ", filter = " + filterName);
		
		// Does template already exist? (from a pushAction for instance)
		Filter pushActionTemplateFilter = this.pushActionMap.get(filterName);
		if (pushActionTemplateFilter!=null) {
			MyUtils.checkStatusProgram(nonSpecificTemplateFilter==null && dimensionPartitionTemplateFilter==null);
		}
			
		// Tranform
		String nonSpecificFilterDisplayTypeValue = nonSpecificTemplateFilter!=null ? 
				(nonSpecificTemplateFilter instanceof SimpleFilter ? ((SimpleFilter)nonSpecificTemplateFilter).getDisplayType() : null) : null;
		FilterDisplayType nonSpecificFilterDisplayType = FilterDisplayType.fromValue(nonSpecificFilterDisplayTypeValue);

		Filter independentFilter = (Filter)transformElementIndependently(
				oldFilter, currentMainRowNumber, mainRowsList, dimensionPartition, filterName, forcedVisibility, 
				nonSpecificFilterDisplayType);
		if (null==independentFilter) {
			return null;
		}
		
		Filter filter = (Filter)updateTemplateElement(
				container, nonSpecificTemplateFilter, currentMainRowNumber, firstSpecific, dimensionPartitionTemplateFilter, 
				filterName, pushActionTemplateFilter, independentFilter);

		// Process with children
		if (oldFilter.hasChildren()) {
			List<SimpleFilter> simpleFilterList = new ArrayList<SimpleFilter>();
			
			List<OldSpecificFilterContent> oldSpecificFilterContentList = null;
			List<OldEmptySpecificElementContent> oldEmptySpecificFilterContentList = null;
			List<OldOptionFilter> oldOptionFilterList = null;
			List<OldSpecificOptionContent> oldSpecificOptionContentList = null;
			List<OldOptionValue> oldOptionValueList = null;
			
			if (oldFilter instanceof OldFilterDescription) {
				OldFilterDescription oldFilterDescription = (OldFilterDescription)oldFilter;
				
				oldSpecificFilterContentList = oldFilterDescription.getOldSpecificFilterContentList();
				oldEmptySpecificFilterContentList = oldFilterDescription.getOldEmptySpecificFilterContentList();
				oldOptionFilterList = oldFilterDescription.getOldOptionFilterList();
				oldOptionValueList = oldFilterDescription.getOldOptionValueList();
			} else if (oldFilter instanceof OldSpecificFilterContent) {
				OldSpecificFilterContent oldSpecificFilterContent = (OldSpecificFilterContent)oldFilter;
				
				oldOptionFilterList = oldSpecificFilterContent.getOldOptionFilterList();
				oldOptionValueList = oldSpecificFilterContent.getOldOptionValueList();
			}  else if (oldFilter instanceof OldOptionFilter) {
				OldOptionFilter oldOptionFilter = (OldOptionFilter)oldFilter;
				
				oldOptionValueList = oldOptionFilter.getOldOptionValueList();
				oldSpecificOptionContentList = oldOptionFilter.getOldSpecificOptionContentList();
			} else {
				throw new FunctionalException(FunctionalException.getErrorMessageUnhandledCaseOfElementChildrenType());
			}
			
			// Assumption: no pointers with children but range ones
			MyUtils.checkStatusProgram(!oldFilter.getPointer() || (oldFilter.getPointer() && 
					MyUtils.nullOrEmpty(oldSpecificFilterContentList) && MyUtils.nullOrEmpty(oldOptionFilterList) &&	// oldEmptySpecificFilterContentList may be empty or null 
					MyUtils.nullOrEmpty(oldSpecificOptionContentList) && MyUtils.nullOrEmpty(oldOptionValueList)), 
					XmlUtils.displayJdomElement(oldFilter.getJdomElement()) + ", " +
					filter.getClass().getSimpleName() + ", " + oldFilter.getClass().getSimpleName() + ", " +
					oldFilter.getPointer() + ", " + 
					(MyUtils.nullOrEmpty(oldSpecificFilterContentList)) + ", " + MyUtils.nullOrEmpty(oldEmptySpecificFilterContentList) + ", " + 
					MyUtils.nullOrEmpty(oldOptionFilterList) + ", " + MyUtils.nullOrEmpty(oldSpecificOptionContentList) + ", " + 
					MyUtils.nullOrEmpty(oldOptionValueList));
			
			// Simply add row to range
			if (oldEmptySpecificFilterContentList!=null && !oldEmptySpecificFilterContentList.isEmpty()) {
				// Erase main partition rows since they are specified one by one
				filter.getTargetRange().removePartition(super.mainPartitionTable);
				for (OldEmptySpecificElementContent oldEmptySpecificFilterContent : oldEmptySpecificFilterContentList) {
					String mainRowName = oldEmptySpecificFilterContent.getRangeInternalName();
					Integer newCurrentRow = super.mainPartitionTable.getRowNumber(mainRowName);
					MyUtils.checkStatusProgram(newCurrentRow!=null);
					
					if (filter.getPointer() || (vars.isTemplate() && 
							oldFilter.checkDatabase(params.getTemplateName(), general.getDatabaseCheck(), mainRowName))) {
																							// only for template (no access to DB otherwise)
						filter.getTargetRange().addRangePartitionRow(super.mainPartitionTable, newCurrentRow, true);
					}
				}
			}
				
			if (oldSpecificFilterContentList!=null && !oldSpecificFilterContentList.isEmpty()) {
				int specific = 0;
				for (OldSpecificFilterContent oldSpecificFilterContent : oldSpecificFilterContentList) {
					String mainRowName = oldSpecificFilterContent.getRangeInternalName();
					Integer newCurrentRow = super.mainPartitionTable.getRowNumber(mainRowName);
					MyUtils.checkStatusProgram(newCurrentRow!=null);
					Filter childFilter = transformFilter(container, oldSpecificFilterContent, null, filter, newCurrentRow, specific==0, null);
					if (childFilter!=null) {
						specific++;
					}
				}
			}
			
			if (oldSpecificOptionContentList!=null && !oldSpecificOptionContentList.isEmpty()) {
				int specific = 0;
				for (OldSpecificOptionContent oldSpecificOptionContent : oldSpecificOptionContentList) {
					String mainRowName = oldSpecificOptionContent.getRangeInternalName();
					Integer newCurrentRow = super.mainPartitionTable.getRowNumber(mainRowName);
					MyUtils.checkStatusProgram(newCurrentRow!=null);
					Filter childFilter = transformFilter(container, oldSpecificOptionContent, null, filter, newCurrentRow, specific==0, null);
					if (childFilter!=null) {
						specific++;
					}
				}
			}
			
			if (oldOptionFilterList!=null && !oldOptionFilterList.isEmpty()) {
				for (OldOptionFilter oldOptionFilter : oldOptionFilterList) {
					SimpleFilter childFilter = (SimpleFilter)transformFilter(	// Can't be a groupfilter
							container, oldOptionFilter, filter, null, currentMainRowNumber, false, false);//TODO false?
					if (childFilter!=null) {	// could be for some if no match in DB
						simpleFilterList.add(childFilter);
					}
				}
			}
			
			// Process option values
			if (oldOptionValueList!=null && !oldOptionValueList.isEmpty()) {
		
				/*if (filter instanceof BooleanFilter) {
					BooleanFilter booleanFilter = (BooleanFilter)filter;
					
					
					 * <Option displayName="Only" internalName="only" isSelectable="true" value="only" />
					 * <Option displayName="Excluded" internalName="excluded" isSelectable="true" value="excluded" />
					 
					MyUtils.checkStatusProgram(oldOptionValueList.size()==2);
					OldOptionValue tmp = oldOptionValueList.get(0);
					MyUtils.checkStatusProgram(tmp.getDisplayName().equals(TransformationConstants.BOOLEAN_FILTER_ONLY_DISPLAY_NAME) ||
							tmp.getDisplayName().equals(TransformationConstants.BOOLEAN_FILTER_EXCLUDED_DISPLAY_NAME));
					OldOptionValue oldOptionValue0 = null; 
					OldOptionValue oldOptionValue1 = null;	
					if (tmp.getDisplayName().equals(TransformationConstants.BOOLEAN_FILTER_ONLY_DISPLAY_NAME)) {
						oldOptionValue0 = tmp;
						oldOptionValue1 = oldOptionValueList.get(1);						
					} else {
						oldOptionValue0 = oldOptionValueList.get(1);
						oldOptionValue1 = tmp;
					}
					MyUtils.checkStatusProgram(oldOptionValue0.getDisplayName().equals(TransformationConstants.BOOLEAN_FILTER_ONLY_DISPLAY_NAME));
					MyUtils.checkStatusProgram(oldOptionValue0.getInternalName().equals(TransformationConstants.BOOLEAN_FILTER_ONLY_VALUE));
					MyUtils.checkStatusProgram(oldOptionValue0.getInternalName().equals(oldOptionValue0.getValue()));
					MyUtils.checkStatusProgram(oldOptionValue0.getIsSelectable());
					MyUtils.checkStatusProgram(oldOptionValue0.getOldPushActionList().isEmpty());
					MyUtils.checkStatusProgram(oldOptionValue1.getDisplayName().equals(TransformationConstants.BOOLEAN_FILTER_EXCLUDED_DISPLAY_NAME));
					MyUtils.checkStatusProgram(oldOptionValue1.getInternalName().equals(TransformationConstants.BOOLEAN_FILTER_EXCLUDED_VALUE));
					MyUtils.checkStatusProgram(oldOptionValue1.getInternalName().equals(oldOptionValue1.getValue()));
					MyUtils.checkStatusProgram(oldOptionValue1.getIsSelectable());
					MyUtils.checkStatusProgram(oldOptionValue1.getOldPushActionList().isEmpty());
					
					booleanFilter.setTrueValue(TransformationConstants.BOOLEAN_FILTER_ONLY_VALUE);
					booleanFilter.setFalseValue(TransformationConstants.BOOLEAN_FILTER_EXCLUDED_VALUE);
					booleanFilter.setTrueDisplay(TransformationConstants.BOOLEAN_FILTER_ONLY_DISPLAY_NAME);
					booleanFilter.setFalseDisplay(TransformationConstants.BOOLEAN_FILTER_EXCLUDED_DISPLAY_NAME);
					
				}*//* else if (filter instanceof ListFilter) {
					ListFilter listFilter = (ListFilter)filter;
					if (null==listFilter.getDataFolderPath()) {
						listFilter.setDataFolderPath(params.getDefaultDataFolderPath());
					}
					
					transformOptionValuesList(oldOptionValueList, listFilter, currentMainRowNumber);
				}  else if (filter instanceof TextfieldFilter) {
					TextfieldFilter textfieldFilter = (TextfieldFilter)filter;
					if (null==textfieldFilter.getDataFolderPath()) {
						textfieldFilter.setDataFolderPath(params.getDefaultDataFolderPath());
					}
					
					transformOptionValuesList(oldOptionValueList, textfieldFilter, currentMainRowNumber);
				} else if (filter instanceof GroupFilter) {
					GroupFilter groupFilter = (GroupFilter)filter;
					if (null==groupFilter.getDataFolderPath()) {
						groupFilter.setDataFolderPath(params.getDefaultDataFolderPath());
					}
					
					// c.f "chromosome_region" in "dmelanogaster_feature_set" dataset
					transformOptionValuesList(oldOptionValueList, groupFilter, currentMainRowNumber);
				} else if (filter instanceof TreeFilter) {
					TreeFilter treeFilter = (TreeFilter)filter;
					if (null==treeFilter.getDataFolderPath()) {
						treeFilter.setDataFolderPath(params.getDefaultDataFolderPath());
					}
					
					transformOptionValuesTree(oldOptionValueList, treeFilter, currentMainRowNumber);
				}*//* else {
					MyUtils.errorProgram(filter.getClass() + ", oldOptionValueList.size() = " + oldOptionValueList.size() + 
							", filter = " + filter.getName() + ", oldOptionValueList.get(0)=" + oldOptionValueList.get(0).getInternalName());
				}*/
				

				if (filter instanceof SimpleFilter) {
					SimpleFilter simpleFilter = (SimpleFilter)filter;
					
					// Treat boolean differently
					if (isBooleanDisplayType(simpleFilter)) {
						
						/*
						 * <Option displayName="Only" internalName="only" isSelectable="true" value="only" />
						 * <Option displayName="Excluded" internalName="excluded" isSelectable="true" value="excluded" />
						 */
						MyUtils.checkStatusProgram(oldOptionValueList.size()==2);
						OldOptionValue tmp = oldOptionValueList.get(0);
						MyUtils.checkStatusProgram(tmp.getDisplayName().equals(TransformationConstants.BOOLEAN_FILTER_ONLY_DISPLAY_NAME) ||
								tmp.getDisplayName().equals(TransformationConstants.BOOLEAN_FILTER_EXCLUDED_DISPLAY_NAME));
						OldOptionValue oldOptionValue0 = null; 
						OldOptionValue oldOptionValue1 = null;	
						if (tmp.getDisplayName().equals(TransformationConstants.BOOLEAN_FILTER_ONLY_DISPLAY_NAME)) {
							oldOptionValue0 = tmp;
							oldOptionValue1 = oldOptionValueList.get(1);						
						} else {
							oldOptionValue0 = oldOptionValueList.get(1);
							oldOptionValue1 = tmp;
						}
						MyUtils.checkStatusProgram(oldOptionValue0.getDisplayName().equals(TransformationConstants.BOOLEAN_FILTER_ONLY_DISPLAY_NAME));
						MyUtils.checkStatusProgram(oldOptionValue0.getInternalName().equals(TransformationConstants.BOOLEAN_FILTER_ONLY_VALUE)
								//|| oldOptionValue0.getInternalName().equals("spexponly")	// only for hsapiens_gene_ensembl in "Pancreatic_Expression"...
								);
						MyUtils.checkStatusProgram(oldOptionValue0.getInternalName().equals(oldOptionValue0.getValue())
								//|| oldOptionValue0.getInternalName().equals("spexponly")	// only for hsapiens_gene_ensembl in "Pancreatic_Expression"...
								);
						MyUtils.checkStatusProgram(oldOptionValue0.getIsSelectable());
						MyUtils.checkStatusProgram(oldOptionValue0.getOldPushActionList().isEmpty());
						MyUtils.checkStatusProgram(oldOptionValue1.getDisplayName().equals(TransformationConstants.BOOLEAN_FILTER_EXCLUDED_DISPLAY_NAME));
						MyUtils.checkStatusProgram(oldOptionValue1.getInternalName().equals(TransformationConstants.BOOLEAN_FILTER_EXCLUDED_VALUE)
								//|| oldOptionValue1.getInternalName().equals("spexpexcluded")	// only for hsapiens_gene_ensembl in "Pancreatic_Expression"...
								);
						MyUtils.checkStatusProgram(oldOptionValue1.getInternalName().equals(oldOptionValue1.getValue())
								//|| oldOptionValue1.getInternalName().equals("spexpexcluded")	// only for hsapiens_gene_ensembl in "Pancreatic_Expression"...
								);
						MyUtils.checkStatusProgram(oldOptionValue1.getIsSelectable());
						MyUtils.checkStatusProgram(oldOptionValue1.getOldPushActionList().isEmpty());
						
						simpleFilter.setTrueValue(TransformationConstants.BOOLEAN_FILTER_ONLY_VALUE);
						simpleFilter.setFalseValue(TransformationConstants.BOOLEAN_FILTER_EXCLUDED_VALUE);
						simpleFilter.setTrueDisplay(TransformationConstants.BOOLEAN_FILTER_ONLY_DISPLAY_NAME);
						simpleFilter.setFalseDisplay(TransformationConstants.BOOLEAN_FILTER_EXCLUDED_DISPLAY_NAME);
						
					} else if (!simpleFilter.getTree()) {
						transformOptionValuesList(oldOptionValueList, simpleFilter, currentMainRowNumber);
					} else {
						transformOptionValuesTree(oldOptionValueList, simpleFilter, currentMainRowNumber);
					}
				} else if (filter instanceof GroupFilter) {
					GroupFilter groupFilter = (GroupFilter)filter;
					
					// c.f "chromosome_region" in "dmelanogaster_feature_set" dataset
					transformOptionValuesList(oldOptionValueList, groupFilter, currentMainRowNumber);
				}
			}
						
			if (filter instanceof GroupFilter	// Must be last of the ifs
					&& !oldFilter.getHasFilterList()) {	// always the case here: 
											// the ones that specify a filterList don't have children and 
											// are treated differently when the new filter is created
				GroupFilter groupFilter = (GroupFilter)filter;
				if (simpleFilterList.isEmpty()) {	// Case where all the constituing filters are not valid (useless to keep the group)
					return null;
				}
				groupFilter.getElementList().addElements(simpleFilterList);
				setGroupFilterRange(simpleFilterList, groupFilter);
			}
		} 
		
		if (filter.getPointer()) {	// If pointer, prepare it's dataset transformation
			boolean valid = this.pointerTransformation.preparePointedDatasetTransformation(oldFilter, super.mainPartitionTable, filter);
			if (!valid) {	// If points to an unexisting dataset
				return null;
			}
		}

		if (oldFilter.getHasFilterList()) {	// If has a filter list then wait the end of the transformation to update the list+range
			MyUtils.checkStatusProgram(filter instanceof GroupFilter);
			GroupFilter groupFilter = (GroupFilter)filter;
			MyUtils.checkStatusProgram(vars.getFilterWithFilterList().get(groupFilter)==null);
			vars.getFilterWithFilterList().put(groupFilter, oldFilter.getFilterList());
		}
		
		return filter;
	}

	private void setGroupFilterRange(List<SimpleFilter> simpleFilterList, GroupFilter groupFilter) throws FunctionalException, TechnicalException {
		// Compute range as the intersection of the main rows for each filter
		List<Range> rangeList = new ArrayList<Range>();
		for (SimpleFilter simpleFilter : simpleFilterList) {
			Range targetRange = simpleFilter.getTargetRange();
			rangeList.add(targetRange);
		}
		
		Range groupTargetRange = Range.mainRangesIntersection(super.mainPartitionTable, true, rangeList);
		groupFilter.setTargetRange(groupTargetRange);
	}

	private void transformOptionValuesList(List<OldOptionValue> oldOptionValueList, Filter filter, Integer currentMainRow) throws FunctionalException {
		
		for (OldOptionValue oldOptionValue : oldOptionValueList) {
			MyUtils.checkStatusProgram(oldOptionValue.getOldOptionValueList().isEmpty());
		}

		// When not partition specific: row=-1..?
		boolean template = currentMainRow==null;
		if (template) {
			currentMainRow = MartConfiguratorConstants.PARTITION_TABLE_ROW_WILDCARD_NUMBER;
		}
			
		// Sufficient for the transformation
		if (null==filter.getDataFolderPath()) {	// Add property here: only if we do have data
			filter.setDataFolderPath(params.getDefaultDataFolderPath());
		}
		FilterData filterData = filter.getFilterData();
		if (template) {
			filterData.setToTemplate();
		}
		
		Part currentPart = new Part(super.mainPartitionTable, currentMainRow);
		
		MyUtils.checkStatusProgram(filterData!=null && null==filterData.getPartValue(currentPart));
		filterData.addPart(currentPart);
		
		int rowNumber = 0;
		List<SimpleFilter> cascadeChildren = new ArrayList<SimpleFilter>();
		for (OldOptionValue oldOptionValue : oldOptionValueList) {
			
			FilterDataRow listFilterDataRow = getDataFileRow(filter, rowNumber, oldOptionValue);
			if (null==listFilterDataRow) {	// null if !isSelectable (just ignored like the hidden elements)
				continue;
			}
			
			// Add the row to data
			/*TransformationUtils.checkForWarning(filterData.getRowForPartValue(currentPart, listFilterDataRow)!=null, 
					vars., "The same filterDataRow " + listFilterDataRow + " appears more than once");*/
			filterData.addRowForPart(currentPart, listFilterDataRow);

			// Handle push actions
			List<OldPushAction> oldPushActionList = oldOptionValue.getOldPushActionList();
			if (!oldPushActionList.isEmpty()) {
				cascadeChildren.addAll(transformPushActions(filterData, currentPart, listFilterDataRow, oldPushActionList));
			}
			
			rowNumber++;
		}
		
		// Add cascade children
		if (!cascadeChildren.isEmpty()) {	// Cascade only possible for list filters, not group
			MyUtils.checkStatusProgram(filter instanceof SimpleFilter, "filter.getClass() = " + filter.getClass());
			SimpleFilter simpleFilter = (SimpleFilter)filter;
			simpleFilter.getElementList().addElements(cascadeChildren);
		}
	}
	
	private void transformOptionValuesTree(List<OldOptionValue> oldOptionValueList, Filter filter, Integer currentMainRow) throws FunctionalException {
		
		for (OldOptionValue oldOptionValue : oldOptionValueList) {
			MyUtils.checkStatusProgram(oldOptionValue.getOldPushActionList().isEmpty());
		}

		// When not partition specific: row=-1..?
		boolean template = currentMainRow==null;
		if (template) {
			currentMainRow = MartConfiguratorConstants.PARTITION_TABLE_ROW_WILDCARD_NUMBER;
		}
		
		// Sufficient for the transformation
		if (null==filter.getDataFolderPath()) {	// Add property here: only if we do have data
			filter.setDataFolderPath(params.getDefaultDataFolderPath());
		}
		MyUtils.checkStatusProgram(filter instanceof SimpleFilter);
		SimpleFilter simpleFilter = (SimpleFilter)filter;
		TreeFilterData treeFilterData = simpleFilter.getTreeFilterData();
		if (template) {
			treeFilterData.setToTemplate();
		}
		
		Part currentPart = new Part(super.mainPartitionTable, currentMainRow);
		
		MyUtils.checkStatusProgram(null==treeFilterData.getPartValue(currentPart));
		ArrayList<TreeFilterDataRow> list = treeFilterData.addPart(currentPart);
		
		int rowNumber = 0;
		for (OldOptionValue oldOptionValue : oldOptionValueList) {				
			TreeFilterDataRow row = processOptionTreeNode(simpleFilter, oldOptionValue, rowNumber);
			if (null!=row) {
				list.add(row);
			}
			rowNumber++;
		}
		
	}
	
	private TreeFilterDataRow processOptionTreeNode (SimpleFilter simpleFilter, OldOptionValue oldOptionValue, int rowNumber){
		FilterDataRow listFilterDataRow = getDataFileRow(simpleFilter, rowNumber, oldOptionValue);
		if (null==listFilterDataRow) {	// null if !isSelectable (just ignored like the hidden elements)
			return null;
		}
		TreeFilterDataRow treeFilterDataRow = new TreeFilterDataRow(listFilterDataRow);

		// Process children
		int rowNumber2 = 0;
		List<OldOptionValue> oldOptionValueList = oldOptionValue.getOldOptionValueList();
		if (!oldOptionValueList.isEmpty()) {
			for (OldOptionValue oldOptionValue2 : oldOptionValueList) {			
				TreeFilterDataRow row = processOptionTreeNode(simpleFilter, oldOptionValue2, rowNumber2);
				if (null!=row) {
					treeFilterDataRow.addChild(row);
				}
			}
		}
		
		return treeFilterDataRow;
	}
	
	private HashSet<SimpleFilter> transformPushActions(FilterData listFilterData, Part currentPart, FilterDataRow listFilterDataRow, List<OldPushAction> oldPushActionList) throws FunctionalException {
		
		HashSet<SimpleFilter> cascadeChildrenTmp = new HashSet<SimpleFilter>();
		for (OldPushAction oldPushAction : oldPushActionList) {
			
			// Try to get existing push action filter or create new one if first time
			SimpleFilter pushActionFilter = this.pushActionMap.get(oldPushAction.getInternalName());
			if (null==pushActionFilter) {
				pushActionFilter = transformPushAction(null, oldPushAction);	// Container will be specified later
			}
			
			// Special case here (cf. hsap_encode), the cascadeChild appears twice and it seems only the latest is considered
			if (listFilterData.getCascadeChildForRowAndPartValue(currentPart, listFilterDataRow, pushActionFilter)!=null) {
				// Remove it
				listFilterData.removeCascadeChildForRowAndPart(currentPart, listFilterDataRow, pushActionFilter);
				
				// Send a warning
				TransformationUtils.sendWarning(vars.getIgnoredPushActions(), 
						"the cascade child " + pushActionFilter.getName() + 
						" appears more than once in the list of <PushAction>s, only the last one will be considered");
			}
			listFilterData.addCascadeChildForRowAndPart(currentPart, listFilterDataRow, pushActionFilter);
			
			// Gather child rows
			int subRowNumber = 0;
			List<OldOptionValue> subOldOptionValueList = oldPushAction.getOldOptionValueList();
			for (OldOptionValue subOldOptionValue : subOldOptionValueList) {
				FilterDataRow subListFilterDataRow = getDataFileRow(pushActionFilter, subRowNumber, subOldOptionValue);
				if (null!=subListFilterDataRow) {	// null if !isSelectable (just ignored like the hidden elements)		
					listFilterData.addRowForCascadeChildAndRowAndPart(
							currentPart, listFilterDataRow, pushActionFilter, subListFilterDataRow);
					subRowNumber++;
				}
			}
			
			// To keep track of the children of the filter
			cascadeChildrenTmp.add(pushActionFilter);
		}
		
		return cascadeChildrenTmp;
	}
	
	/**
		UNIPROT's "entry_type": <Option displayName="UniProtKB/Swiss-Prot (Reviewed)" internalName="Swiss-Prot" isSelectable="true" value="Swiss-Prot" />
			where        displayName!=internalName==value
		UNIPROT's "protein_evidence": <Option displayName="1: Evidence at protein level" internalName="1:_Evidence_at_protein_level" isSelectable="true" value="1: Evidence at protein level" />
			where value==displayName!=internalName
			
	 * @param listFilter
	 * @param rowNumber
	 * @param oldOptionValue
	 * @return
	 */
	private FilterDataRow getDataFileRow(Filter filter, int rowNumber, OldOptionValue oldOptionValue) {
							// TODO should use a superClass of only ListFilter and TreeFilter instead of SimpleFilter
		// Simply ignore such elements
		if (!oldOptionValue.getIsSelectable()) {
			/*MyUtils.errorProgram();		// haven't seen any yet*/
			return null;
		}
		
		// We ignore oldOptionValue.getInternalName();
		String optionValue = oldOptionValue.getValue();
		String optionDisplayName = oldOptionValue.getDisplayName();
		FilterDataRow dataRow = new FilterDataRow(
				filter, optionValue, optionDisplayName, rowNumber==0);	// first row is the default one
		return dataRow;
	}

	private SimpleFilter transformPushAction(Container container, OldPushAction oldPushAction) throws FunctionalException {
		String pushActionName = oldPushAction.getInternalName();	// former 'ref'
		SimpleFilter pushActionFilter = new SimpleFilter(super.mainPartitionTable, pushActionName);
		pushActionFilter.setDataFolderPath(params.getDefaultDataFolderPath());	// We know for sure it has data by there
		this.pushActionMap.put(pushActionFilter.getName(), pushActionFilter);
		return pushActionFilter;
	}

	@Override
	Attribute createNewAttribute(OldAttribute oldAttribute, Integer currentMainRowNumber, List<Integer> mainRowsList,
			DimensionPartition dimensionPartition) throws FunctionalException, TechnicalException {
		MyUtils.errorProgram("Shouldn't be here");
		return null;
	}
	
	@Override
	Filter createNewFilter(OldFilter oldFilter, Integer currentMainRowNumber, List<Integer> mainRowsList,
			DimensionPartition dimensionPartition, Boolean forcedVisibility, FilterDisplayType nonSpecificFilterDisplayType) 
			throws FunctionalException, TechnicalException {
		
		String filterName = help.replaceAliases(oldFilter.getInternalName());
		Boolean pointer = oldFilter.getPointer();
		
		// For filters only
		FilterDisplayType filterType = null;
		if (!pointer) {
			filterType = FilterTransformation.getTransformationFilterDisplayType(oldFilter);
			if (null==filterType && nonSpecificFilterDisplayType!=null) {
				filterType = nonSpecificFilterDisplayType;
			}
		}
		
		// Unlike attribute, filter are divided in subtypes
		
		Filter newFilter = null;
		if (!oldFilter.getHasFilterList()) {
			if (pointer || null!=filterType) {
				boolean tree = FilterDisplayType.TREE.equals(filterType);
				newFilter = new SimpleFilter(super.mainPartitionTable, filterName, tree);
					
			} else {	// groupFilter
				MyUtils.checkStatusProgram(null==filterType);
				newFilter = new GroupFilter(super.mainPartitionTable, filterName);
			}
		} else {
			MyUtils.checkStatusProgram(FilterDisplayType.TEXTFIELD.equals(filterType) || FilterDisplayType.LIST.equals(filterType), 
					filterType + ", " + XmlUtils.displayJdomElement(oldFilter.getJdomElement()));		// case like "chromosome_region"
			newFilter = new GroupFilter(super.mainPartitionTable, filterName);
		}

		boolean filterGroup = oldFilter.isFilterGroup();
		
		// Add ranges
		PartitionTable dimensionPartitionTable = null;
		if (!filterGroup) {
			
			// DB has already been checked by then, merely updating the range
			updateRangeWithMainPartition(oldFilter, currentMainRowNumber, mainRowsList, newFilter, forcedVisibility);
						
			// Add dimensionTable partition range
			// Look for dimension partitions and create it if doesn't already exist
			dimensionPartitionTable = updateRangeWithDimensionPartition(dimensionPartition, newFilter, oldFilter);
		} else {
			Boolean visible = !oldFilter.getHideDisplay();	// get visibility
			newFilter.setVisible(visible);
		}
		
		String tableName = null;
		if (!pointer && !filterGroup) {
			tableName = computeTableName(oldFilter, dimensionPartition, dimensionPartitionTable);
			if (null==tableName) {	// No matching table for instance, ignore element
				return null;
			}
		}

		assignSimpleProperties(oldFilter, newFilter, tableName);
		
		// For filters only
		if (!pointer) {
			Boolean multipleValues = oldFilter.getMultipleValues();
			if (newFilter instanceof GroupFilter) {
				GroupFilter groupFilter = (GroupFilter)newFilter;
				
				MyUtils.checkStatusProgram(!vars.isTemplate() || currentMainRowNumber==null);	// Assume no part-specific cases like this
									
				// Discard provided value for these properties (make no sense)
				groupFilter.setVisible(null);
				
				// dataFolderPath is children dependent
				
				/*
				 * 1. <FilterDescription defaultValue="1:100:10000000:1" description="Limit to Genes within multiple comma separate Chromosomal regions (1:100:10000:-1,1:100000:2000000:1)" displayName="Chromosome Regions" displayType="text" field="seq_region_start_1020" filterList="chromosome_name,start,end,strand" hidden="false" internalName="chromosomal_region" key="gene_id_1020_key" legal_qualifiers="=" multipleValues="1" qualifier="=" tableConstraint="main" type="text" />
				 * 2. <FilterDescription displayName="Chromosome Name" displayType="list" field="name_1040", filterList="chromosome_name_regulatory,chromosome_name_annotated,chromosome_name_external", internalName="chromosome_region", key="feature_set_id_1023_key", legal_qualifiers="=", qualifier="=", style="menu", tableConstraint="dmelanogaster_feature_set__external_feature__dm", type="text">
				 * 3. <FilterDescription displayType="container" internalName="new_id_list_filters" type="boolean_list">
				          <Option displayName="with Custom platypus exon ID(s)" displayType="list" field="ox_custom_platypus_exon_bool" internalName="with_custom_platypus_exon" isSelectable="true" key="transcript_id_1064_key" legal_qualifiers="only,excluded" qualifier="only" style="radio" tableConstraint="main" type="boolean">
				            <Option displayName="Only" internalName="only" isSelectable="true" value="only" />
				            <Option displayName="Excluded" internalName="excluded" isSelectable="true" value="excluded" />
				          </Option>
				          <Option displayName="with Illumina MouseWG...
				 */
				String multipleFilter = null;
				//Boolean shareValue = null;
				String logicalOperator = null;
				if (oldFilter.getHasFilterList()) {
					if (multipleValues!=null && multipleValues) {	// 1.
						logicalOperator = MartConfiguratorConstants.FILTER_LOGICAL_OPERATOR_AND;
						//shareValue = false;
						multipleFilter = MartConfiguratorConstants.MULTIPLE_FILTER_VALUE_ALL;
					} else {	// 2.
						logicalOperator = MartConfiguratorConstants.FILTER_LOGICAL_OPERATOR_OR;
						//shareValue = true;
						multipleFilter = MartConfiguratorConstants.MULTIPLE_FILTER_VALUE_ALL;
					}
				} else {	// 3.				
					logicalOperator = MartConfiguratorConstants.FILTER_LOGICAL_OPERATOR_AND;
					//shareValue = false;
					if (multipleValues!=null && multipleValues) {
						multipleFilter = MartConfiguratorConstants.MULTIPLE_FILTER_VALUE_N;
					} else {
						multipleFilter = MartConfiguratorConstants.MULTIPLE_FILTER_VALUE_1;
					}
				}
				groupFilter.setMultipleFilter(multipleFilter);
				//groupFilter.setShareValue(shareValue);
				groupFilter.setLogicalOperator(logicalOperator);
			} else if (newFilter instanceof SimpleFilter) {
				SimpleFilter simpleFilter = (SimpleFilter)newFilter;
				
				// Relational info: store info in map to later associate filter with attribute
				RelationalInfo relationalInfo = new RelationalInfo(
						tableName, help.replaceAliases(oldFilter.getKey()), help.replaceAliases(oldFilter.getField()));
				vars.getSimpleFilterToRelationInfoMap().put(simpleFilter, relationalInfo);

				simpleFilter.setDisplayType(filterType.getValue());
				
				/*protected Filter cascadeParent = null;*/	// 	children dependent (its parent's one)
				simpleFilter.setQualifier(oldFilter.getQualifier());	// it's agreed that we disregard the legal_qualifiers
																// they may differ: qualifier = =, legal_qualifiers = =,in ->link_ensembl_gene_id in hsap
				simpleFilter.setCaseSensitive(true);
				simpleFilter.setOrderBy(null);	// not in old system
				
				/*if (newFilter instanceof BooleanFilter) {
			//		BooleanFilter booleanFilter = (BooleanFilter)newFilter;
					// The properties trueValue, trueDisplay, falseValue and falseDisplay are children dependent
				}*//* else if (newFilter instanceof TextfieldFilter) {
					TextfieldFilter textfieldFilter = (TextfieldFilter)newFilter;
					
					textfieldFilter.setMultiValue(multipleValues);
					
					// Old system: whenever there is multiple values for a textfield: you can upload
					textfieldFilter.setUpload(multipleValues);
					
				}*//* else if (newFilter instanceof ListFilter) {
					ListFilter listFilter = (ListFilter)newFilter;
					listFilter.setMultiValue(multipleValues);
					listFilter.setDataFolderPath(params.getDefaultDataFolderPath());	// Must have a data file, possibly with no rows though
				}*//* else if (newFilter instanceof TreeFilter) {
					TreeFilter treeFilter = (TreeFilter)newFilter;
					treeFilter.setMultiValue(multipleValues);
					treeFilter.setDataFolderPath(params.getDefaultDataFolderPath());	// Must have a data file, possibly with no rows though
					
					String buttonURL = oldFilter.getButtonURL();
					MyUtils.checkStatusProgram(null!=buttonURL);
					treeFilter.setButtonURL(buttonURL);
				}*/
								
				simpleFilter.setMultiValue(multipleValues);
				
				// filter data (list or tree) is children dependent
				//simpleFilter.setDataFolderPath(params.getDefaultDataFolderPath());	// Must have a data file, possibly with no rows though
				
				
				// For list
				
				// For tree
				/*if (simpleFilter.getTree()) {*/
				String buttonURL = oldFilter.getButtonURL();
				MyUtils.checkStatusProgram(!simpleFilter.getTree() || null!=buttonURL);
				simpleFilter.setButtonURL(buttonURL);
				
				// For textfields
				if (isTextfieldDisplayType(simpleFilter)) {
					simpleFilter.setUpload(multipleValues);	// Old system: whenever there is multiple values for a textfield: you can upload
				}
			}
		}

		return newFilter;
	}

	public boolean isTextfieldDisplayType(SimpleFilter simpleFilter) {
		return FilterDisplayType.TEXTFIELD.equals(FilterDisplayType.fromValue(simpleFilter.getDisplayType()));
	}
	public boolean isBooleanDisplayType(SimpleFilter simpleFilter) {
		return FilterDisplayType.BOOLEAN.equals(FilterDisplayType.fromValue(simpleFilter.getDisplayType()));
	}
	public boolean isListDisplayType(SimpleFilter simpleFilter) {
		return FilterDisplayType.LIST.equals(FilterDisplayType.fromValue(simpleFilter.getDisplayType()));
	}
	public boolean isTreeDisplayType(SimpleFilter simpleFilter) {
		return FilterDisplayType.TREE.equals(FilterDisplayType.fromValue(simpleFilter.getDisplayType()));
	}

	/*public static FilterDisplayType getTransformationFilterDisplayType(OldFilter oldFilter) throws FunctionalException {
		
		FilterOldDisplayType filterOldDisplayType = oldFilter.getDisplayType();
		FilterOldType filterOldType = oldFilter.getType();
		
		if (oldFilter.isTree()) {
			return FilterDisplayType.TREE;
		}
		
		// "id_list_options" in "est" dataset is actually a groupFilter even though it says container/list...
		if (oldFilter.hasChildren() && ElementChildrenType.OPTION_FILTER.equals(oldFilter.getFirstChildrenType())) {
			return null;
		}
		
		// for regular TEXTFIELD
		// for some options in LIST_TEXTFIELD (filterOldType seems to be irrelevant here)
		if ((filterOldDisplayType.equals(FilterOldDisplayType.TEXT) && filterOldType.equals(FilterOldType.TEXT)) ||
			(filterOldDisplayType.equals(FilterOldDisplayType.TEXT) && filterOldType.equals(FilterOldType.LIST)) || 
			(filterOldDisplayType.equals(FilterOldDisplayType.TEXT) && filterOldType.equals(FilterOldType.NUMBER)) ||
			(filterOldDisplayType.equals(FilterOldDisplayType.TEXT) && filterOldType.equals(FilterOldType.ID_LIST)) ||
			(filterOldDisplayType.equals(FilterOldDisplayType.CONTAINER) && filterOldType.equals(FilterOldType.TEXT)) ||
									// see "gene_stable_id" from "rnorvegicus_expr_gene_ensembl_structure" (invisible, not sure that is indeed a textfield)
			(filterOldDisplayType.equals(FilterOldDisplayType.EMPTY) && filterOldType.equals(FilterOldType.TEXT)) ||	// for pointer
			(filterOldDisplayType.equals(FilterOldDisplayType.TEXT) && filterOldType.equals(FilterOldType.EMPTY))) {	// for pointer
			return FilterDisplayType.TEXTFIELD;
		}
		// for regular LIST
		// for cascade
		else if ((filterOldDisplayType.equals(FilterOldDisplayType.LIST) && filterOldType.equals(FilterOldType.LIST)) ||
				(filterOldDisplayType.equals(FilterOldDisplayType.CONTAINER) && filterOldType.equals(FilterOldType.LIST)) ||
				(filterOldDisplayType.equals(FilterOldDisplayType.LIST) && filterOldType.equals(FilterOldType.TEXT))) {
			return FilterDisplayType.LIST;
		}
		// for regular BOOLEAN
		// for options in LIST_BOOLEAN
		else if (filterOldDisplayType.equals(FilterOldDisplayType.LIST) && filterOldType.equals(FilterOldType.BOOLEAN) ||
				filterOldDisplayType.equals(FilterOldDisplayType.LIST) && filterOldType.equals(FilterOldType.BOOLEAN_NUM)) {
			return FilterDisplayType.BOOLEAN;
		}
		// for LIST_BOOLEAN
		else if (filterOldDisplayType.equals(FilterOldDisplayType.CONTAINER) && filterOldType.equals(FilterOldType.BOOLEAN_LIST)) {
			return null;
			//return TransformationFilterDisplayType.LIST_BOOLEAN;
		}
		// for LIST_TEXTFIELD
		else if (filterOldDisplayType.equals(FilterOldDisplayType.CONTAINER) && filterOldType.equals(FilterOldType.ID_LIST)) {
			return null;
			//return TransformationFilterDisplayType.LIST_TEXTFIELD;
		}
		// for cascade
		else if ((filterOldDisplayType.equals(FilterOldDisplayType.TEXT) && filterOldType.equals(FilterOldType.DROP_DOWN_BASIC_FILTER)) ||
				(filterOldDisplayType.equals(FilterOldDisplayType.LIST) && filterOldType.equals(FilterOldType.DROP_DOWN_BASIC_FILTER)) ||
				(filterOldDisplayType.equals(FilterOldDisplayType.EMPTY) && filterOldType.equals(FilterOldType.DROP_DOWN_BASIC_FILTER))) {
			return FilterDisplayType.LIST;
			//return TransformationFilterDisplayType.LIST_CASCADE;
		}
		// ?
		if ((filterOldDisplayType.equals(FilterOldDisplayType.CONTAINER) && filterOldType.equals(FilterOldType.EMPTY))) {	// filter_by in SSLP
			return null;
			//return TransformationFilterDisplayType.LIST_TEXTFIELD;
		}
		// for specific contents
		else if (filterOldDisplayType.equals(FilterOldDisplayType.EMPTY) && filterOldType.equals(FilterOldType.EMPTY)) {
			return null;
		}
		throw new FunctionalException("Unknown type combination: " + filterOldDisplayType + " and " + filterOldType + 
				", oldFilter = " + MartConfiguratorUtils.displayJdomElement(oldFilter.getJdomElement()));
	}*/

	public static FilterDisplayType getTransformationFilterDisplayType(OldFilter oldFilter) throws FunctionalException {
		
		FilterOldDisplayType filterOldDisplayType = oldFilter.getDisplayType();
		FilterOldStyle filterOldStyle = oldFilter.getStyle();
		
		if (oldFilter.isTree()) {
			return FilterDisplayType.TREE;
		}
		
		// "id_list_options" in "est" dataset is actually a groupFilter even though it says container/list...
		if (oldFilter.hasChildren() && ElementChildrenType.OPTION_FILTER.equals(oldFilter.getFirstChildrenType())) {
			return null;
		}
		
		if (filterOldDisplayType.equals(FilterOldDisplayType.TEXT) && 
				(filterOldStyle.equals(FilterOldStyle.EMPTY)/* ||
						filterOldStyle.equals(FilterOldStyle.TEXT) ||
						filterOldStyle.equals(FilterOldStyle.LIST) ||
						filterOldStyle.equals(FilterOldStyle.BOOLEAN)*/)) {
			return FilterDisplayType.TEXTFIELD;
		} else if (filterOldDisplayType.equals(FilterOldDisplayType.LIST) && filterOldStyle.equals(FilterOldStyle.MENU)) {
			return FilterDisplayType.LIST;
		} else if (filterOldDisplayType.equals(FilterOldDisplayType.LIST) && filterOldStyle.equals(FilterOldStyle.RADIO)) {
			return FilterDisplayType.BOOLEAN;
		} else if (filterOldDisplayType.equals(FilterOldDisplayType.CONTAINER)) {
			return null;	// Containers
		}
		throw new FunctionalException("Unknown type combination: " + filterOldDisplayType + " and " + filterOldStyle + " and " + oldFilter.getType() + 
				", oldFilter = " + XmlUtils.displayJdomElement(oldFilter.getJdomElement()));
	}

	@Override
	Attribute updateNonSpecificTemplateAttribute(Attribute templateAttribute, Attribute newAttribute, Integer currentMainRow, boolean firstSpecific) throws FunctionalException, TechnicalException {
		MyUtils.errorProgram("Shouldn't be here");
		return null;
	}

	@Override
	protected Filter updateNonSpecificTemplateFilter(Filter templateFilter, Filter newFilter, Integer currentMainRow, boolean firstSpecific) throws FunctionalException, TechnicalException {
		
		updateNonSpecificTemplateElement(templateFilter, newFilter, currentMainRow, firstSpecific);
		
		// Check that all these properties are the same, exception made for the first specific which can define the 1st values for some properties
		//boolean doThrow = false;
		if (!TransformationUtils.checkValidSpecificityBoolean(templateFilter.getCaseSensitive(), newFilter.getCaseSensitive(), firstSpecific)/* ||
				!TransformationUtils.checkValidSpecificityString(templateFilter.getQualifier(), newFilter.getQualifier(), firstSpecific)*/	// Allow qualifier
				) {
			throwForbiddenSpecificityException(templateFilter, newFilter);
		}
		if (templateFilter instanceof GroupFilter) {
			MyUtils.checkStatusProgram(newFilter instanceof GroupFilter);
			
			GroupFilter templateGroupFilter = (GroupFilter)templateFilter;
			GroupFilter newGroupFilter = (GroupFilter)newFilter;
			
			if (!TransformationUtils.checkValidSpecificityListString(
					templateGroupFilter.getElementList().getElementNames(), newGroupFilter.getElementList().getElementNames(), firstSpecific) ||
					!TransformationUtils.checkValidSpecificityString(templateGroupFilter.getLogicalOperator(), newGroupFilter.getLogicalOperator(), firstSpecific) ||
					//!TransformationUtils.checkValidSpecificityBoolean(templateGroupFilter.getShareValue(), newGroupFilter.getShareValue(), firstSpecific) ||		
					!TransformationUtils.checkValidSpecificityString(templateGroupFilter.getMultipleFilter(), newGroupFilter.getMultipleFilter(), firstSpecific)) {
				throwForbiddenSpecificityException(templateFilter, newFilter);
			}
		} else if (templateFilter instanceof SimpleFilter) {
			MyUtils.checkStatusProgram(newFilter instanceof SimpleFilter);
			
			SimpleFilter templateSimpleFilter = (SimpleFilter)templateFilter;
			SimpleFilter newSimpleFilter = (SimpleFilter)newFilter;
			
			/*if (!TransformationUtils.checkValidSpecificityString(
					templateSimpleFilter.getDisplayType()!=null ? templateSimpleFilter.getDisplayType().getValue() : null, 
							newSimpleFilter.getDisplayType()!=null ? newSimpleFilter.getDisplayType().getValue() : null, firstSpecific) ||
					!TransformationUtils.checkValidSpecificityString(templateSimpleFilter.getOrderBy(), newSimpleFilter.getOrderBy(), firstSpecific)) {
				throwForbiddenSpecificityException(templateFilter, newFilter);
			}*/
			Range targetRange = templateSimpleFilter.getTargetRange();
			Set<Integer> mainRowsSet = targetRange.getMainRowsSet();
			templateSimpleFilter.setDisplayType(updateSpecificProperty(
					currentMainRow, templateSimpleFilter.getDisplayType(), newSimpleFilter.getDisplayType(), firstSpecific, mainRowsSet));
			
			/*if (templateFilter instanceof BooleanFilter) {
				MyUtils.checkStatusProgram(newFilter instanceof BooleanFilter);
				
				BooleanFilter templateBooleanFilter = (BooleanFilter)templateFilter;
				BooleanFilter newBooleanFilter = (BooleanFilter)newFilter;
				
				if (!TransformationUtils.checkValidSpecificityString(templateBooleanFilter.getTrueValue(), newBooleanFilter.getTrueValue(), firstSpecific) ||
						!TransformationUtils.checkValidSpecificityString(templateBooleanFilter.getTrueDisplay(), newBooleanFilter.getTrueDisplay(), firstSpecific) ||
						!TransformationUtils.checkValidSpecificityString(templateBooleanFilter.getFalseValue(), newBooleanFilter.getFalseValue(), firstSpecific) ||
						!TransformationUtils.checkValidSpecificityString(templateBooleanFilter.getFalseDisplay(), newBooleanFilter.getFalseDisplay(), firstSpecific)) {
					throwForbiddenSpecificityException(templateFilter, newFilter);
				}
			}*//* else if (templateFilter instanceof TextfieldFilter) {
				MyUtils.checkStatusProgram(newFilter instanceof TextfieldFilter ||
						newFilter instanceof ListFilter, 
						templateFilter.getName() + ", " + MartConfiguratorUtils.displayJdomElement(newFilter.generateXml()) + 
						vars.get###$().getRowName(12) + ", " + newFilter.getClass());
				
				TextfieldFilter templateTextfieldFilter = (TextfieldFilter)templateFilter;
				TextfieldFilter newTextfieldFilter = (newFilter instanceof TextfieldFilter) ? 
						(TextfieldFilter)newFilter : (TextfieldFilter)newFilter;
				
				Boolean multiValueTemplate = templateTextfieldFilter.getMultiValue();
				Boolean multiValueNew = (newFilter instanceof TextfieldFilter) ? 
						((TextfieldFilter)newFilter).getMultiValue() : templateSimpleFilter.getMultiValue();
				Boolean uploadTemplate = templateTextfieldFilter.getUpload();
				Boolean uploadNew = templateSimpleFilter.getUpload();
				if (!TransformationUtils.checkValidSpecificityBoolean(multiValueTemplate, multiValueNew, firstSpecific) ||
						!TransformationUtils.checkValidSpecificityBoolean(uploadTemplate, uploadNew, firstSpecific)) {
					throwForbiddenSpecificityException(templateFilter, newFilter);
				}
			}*//* else if (templateFilter instanceof ListFilter) {
				MyUtils.checkStatusProgram(newFilter instanceof ListFilter);
				
				ListFilter templateListFilter = (ListFilter)templateFilter;
				ListFilter newListFilter = (ListFilter)newFilter;

				
			}*//* else if (templateFilter instanceof TreeFilter) {
				MyUtils.errorProgram();
			}*/
			MyUtils.checkStatusProgram(templateFilter instanceof SimpleFilter && !((SimpleFilter)templateFilter).getTree());
			 // Assumption that there are no part-specific tree in current configs
			if (!TransformationUtils.checkValidSpecificityBoolean(templateSimpleFilter.getMultiValue(), newSimpleFilter.getMultiValue(), firstSpecific) ||
					!TransformationUtils.checkValidSpecificityString(
							templateSimpleFilter.getDataFolderPath()!=null ? templateSimpleFilter.getDataFolderPath().getAbsolutePath() : null, 
							newSimpleFilter.getDataFolderPath()!=null ? newSimpleFilter.getDataFolderPath().getAbsolutePath() : null, firstSpecific)) {
				throwForbiddenSpecificityException(templateFilter, newFilter);
			}
			if (!TransformationUtils.checkValidSpecificityString(templateSimpleFilter.getTrueValue(), newSimpleFilter.getTrueValue(), firstSpecific) ||
					!TransformationUtils.checkValidSpecificityString(templateSimpleFilter.getTrueDisplay(), newSimpleFilter.getTrueDisplay(), firstSpecific) ||
					!TransformationUtils.checkValidSpecificityString(templateSimpleFilter.getFalseValue(), newSimpleFilter.getFalseValue(), firstSpecific) ||
					!TransformationUtils.checkValidSpecificityString(templateSimpleFilter.getFalseDisplay(), newSimpleFilter.getFalseDisplay(), firstSpecific)) {
				throwForbiddenSpecificityException(templateFilter, newFilter);
			}
			if (!TransformationUtils.checkValidSpecificityBoolean(templateSimpleFilter.getUpload(), newSimpleFilter.getUpload(), firstSpecific)) {
				throwForbiddenSpecificityException(templateFilter, newFilter);
			}
		}
		
		// Update properties that are allowed to be part specific
		Range targetRange = templateFilter.getTargetRange();
		Set<Integer> mainRowsSet = targetRange.getMainRowsSet();
		templateFilter.setQualifier(updateSpecificProperty(
				currentMainRow, templateFilter.getQualifier(), newFilter.getQualifier(), firstSpecific, mainRowsSet));
		
		
		//!TransformationUtils.checkValidSpecificitySetString(templateListFilter.getCascadeChildrenNamesList(), newListFilter.getCascadeChildrenNamesList(), firstSpecific) ||
		
		return templateFilter;
	}

	public void updateFiltersWithFilterList() {
		Map<GroupFilter, List<String>> filterWithFilterList = vars.getFilterWithFilterList();
		
		// Process old filters that have a filterLists
		for (Iterator<GroupFilter> it = filterWithFilterList.keySet().iterator(); it.hasNext();) {
			GroupFilter groupFilter = it.next();
			List<String> filterNameList = filterWithFilterList.get(groupFilter);
			for (String childFilterName : filterNameList) {
				Filter filter = vars.getFilterFromFilterMap(childFilterName);
				MyUtils.checkStatusProgram(null!=filter);	// Assumes it refers to a filter already defined
				MyUtils.checkStatusProgram(filter instanceof SimpleFilter);	// Assumes it refers to a simple filter
				SimpleFilter simpleFilter = (SimpleFilter)filter;
				groupFilter.getElementList().addElement(simpleFilter);
			}
		}
	}

	public void createMainPartitionFilter(Config config) throws FunctionalException {

		Container partitionFilterContainer = new Container(
				TransformationConstants.PARTITION_FILTERS_CONTAINER_NAME, TransformationConstants.PARTITION_FILTERS_CONTAINER_DISPLAY_NAME,
				null, true, null);
		SimpleFilter mainPartitionFilter = new SimpleFilter(
				super.mainPartitionTable, TransformationConstants.MAIN_PARTITION_FILTER_NAME);
		mainPartitionFilter.setDisplayName(TransformationConstants.MAIN_PARTITION_FILTER_DISPLAY_NAME);
		mainPartitionFilter.setVisible(true);
		mainPartitionFilter.setSelectedByDefault(false);
		mainPartitionFilter.setPointer(false);
		mainPartitionFilter.setPartition(true);
		
		mainPartitionFilter.setDataFolderPath(params.getDefaultDataFolderPath());
		FilterData filterData = mainPartitionFilter.getFilterData();
		filterData.setToTemplate();
		
		Part part = MartConfiguratorUtils.createGenericPart(super.mainPartitionTable);
		filterData.addPart(part);
		
		for (int rowNumber = 0; rowNumber < super.mainPartitionTable.getRowNamesList().size(); rowNumber++) {
			String rowName = super.mainPartitionTable.getRowNamesList().get(rowNumber);
			FilterDataRow dataRow = new FilterDataRow(
					mainPartitionFilter, rowName, rowName, rowNumber==0);	// first row is the default one
			filterData.addRowForPart(part, dataRow);			
		}
		
		partitionFilterContainer.addFilter(mainPartitionFilter);
		vars.addFilterToMaps(mainPartitionFilter);
		
		Container rootContainer = config.getRootContainer();
		rootContainer.addContainer(partitionFilterContainer);
	}
	
	public void updateFilters(AttributeTransformation attributeTransformation, Container rootContainer) 
	throws FunctionalException, TechnicalException {
		for (Filter filter : vars.getFiltersFromFilterMap()) {
			if (filter instanceof SimpleFilter && !filter.getPointer()) {	// treat pointers differently (later)
				SimpleFilter simpleFilter = (SimpleFilter)filter;
				if (!simpleFilter.getPartition()) {	// no need for an attribute when a partition filter
					RelationalInfo relationalInfo = vars.getSimpleFilterToRelationInfoMap().get(simpleFilter);
					MyUtils.checkStatusProgram(null!=relationalInfo, "simpleFilter = " + simpleFilter);
					List<Attribute> attributeListForRelationInfo = vars.getAttributeListFromRelationalInfoToAttributeListMap(relationalInfo);
					
					/*
					 * Either there is only one, in which case use it, otherwise generate an attribute for it
					 */
					String attributeName = null;
					if (null!=attributeListForRelationInfo && attributeListForRelationInfo.size()==1) {
						/*displayMatchingList(relationalInfo, attributeListForRelationInfo);*/
						Attribute attribute = attributeListForRelationInfo!=null ? 
								attributeListForRelationInfo.get(0) : null;
						attributeName = attribute.getName();
					} else {
						
						// Must create an attribute for this relational info
						Attribute generatedAttribute = attributeTransformation.createNewAttribute(relationalInfo, rootContainer);
						attributeName = generatedAttribute.getName();
					}
					simpleFilter.setAttributeName(attributeName);
				}
			}
		}
	}

	/**
	 * Displays attributes that match the relational info for that pointer
	 * @param relationalInfo
	 * @param attributeListForRelationInfo
	 */
	@SuppressWarnings("unused")	// TODO to keep for now
	private static void displayMatchingList(RelationalInfo relationalInfo,
			List<Attribute> attributeListForRelationInfo) {
		if (attributeListForRelationInfo!=null && attributeListForRelationInfo.size()>1) {
			System.out.println("more than one for " + relationalInfo);
			for (Attribute attribute : attributeListForRelationInfo) {
				System.out.println("\t" + attribute.getName() + " - " + attribute.getTargetRange().getXmlValue());
			}
			System.out.println();
			MyUtils.pressKeyToContinue();
		}
	}
}

