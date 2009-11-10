package org.biomart.transformation;


import java.util.List;
import java.util.Set;

import org.biomart.common.general.exceptions.FunctionalException;
import org.biomart.common.general.exceptions.TechnicalException;
import org.biomart.common.general.utils.MyUtils;
import org.biomart.objects.MartConfiguratorUtils;
import org.biomart.objects.objects.Attribute;
import org.biomart.objects.objects.Container;
import org.biomart.objects.objects.Filter;
import org.biomart.objects.objects.FilterDisplayType;
import org.biomart.objects.objects.PartitionTable;
import org.biomart.objects.objects.Range;
import org.biomart.transformation.helpers.DimensionPartition;
import org.biomart.transformation.helpers.TransformationGeneralVariable;
import org.biomart.transformation.helpers.TransformationHelper;
import org.biomart.transformation.helpers.TransformationParameter;
import org.biomart.transformation.helpers.TransformationUtils;
import org.biomart.transformation.helpers.TransformationVariable;
import org.biomart.transformation.oldXmlObjects.OldAttribute;
import org.biomart.transformation.oldXmlObjects.OldAttributeDescription;
import org.biomart.transformation.oldXmlObjects.OldEmptySpecificElementContent;
import org.biomart.transformation.oldXmlObjects.OldFilter;
import org.biomart.transformation.oldXmlObjects.OldSpecificAttributeContent;


public class AttributeTransformation extends ElementTransformation {

	public AttributeTransformation (TransformationGeneralVariable general, TransformationParameter params, TransformationVariable vars, 
			TransformationHelper help, PointerTransformation pointerTransformation) throws TechnicalException {	
		super(general, params, vars, help, pointerTransformation, true);
	}
	
	@Override
	protected Filter transformFilter(Container container, OldFilter oldFilter, Filter groupTemplateFilter, Filter nonSpecificTemplateFilter, 
			Integer currentMainRow, boolean firstSpecific, Boolean forcedVisibility) throws FunctionalException, TechnicalException {
		MyUtils.errorProgram("Shouldn't be here");
		return null;
	}
	
	@Override
	protected Attribute transformAttribute(Container container, OldAttribute oldAttribute, 
			Attribute nonSpecificTemplateAttribute, Integer currentMainRowNumber, boolean firstSpecific) 
	throws FunctionalException, TechnicalException {
		
		System.out.println(MartConfiguratorUtils.displayJdomElement(oldAttribute.getJdomElement()) + ", " + firstSpecific);
		
		PartitionTable mainPartitionTable = vars.getMainPartitionTable();
		
		// Disregard elements with no match in the DB
		List<Integer> mainRowsList = null;
		if (oldAttribute.getPointer() || oldAttribute.isMain() || oldAttribute.isDimension()) {
			mainRowsList = checkDatabase(oldAttribute, currentMainRowNumber, mainPartitionTable);
			if (mainRowsList.isEmpty()) {
				return null;
			}
		}
		System.out.println("\tmainRowsList = " + mainRowsList);
		
		// Look for dimension partitions (only consider partitions if dealing with a template)
		DimensionPartition dimensionPartition = getDimensionPartition(oldAttribute);
		Attribute dimensionPartitionTemplateAttribute = (Attribute)getDimensionPartitionTemplateElement(container, nonSpecificTemplateAttribute, dimensionPartition);
		
		String attributeName = allocateName(oldAttribute, nonSpecificTemplateAttribute);
		
		System.out.println("\toldAttribute.getInternalName() = " + oldAttribute.getInternalName() + ", attribute = " + attributeName);
		
		// No predefition of attribute like there is for filters (push actions), any template is specified as argument in the method
			
		// Tranform
		Attribute independentAttribute = (Attribute)transformElementIndependently(
				oldAttribute, currentMainRowNumber, mainRowsList, dimensionPartition, attributeName, null, null);
		if (null==independentAttribute) {
			return null;
		}
		
		Attribute attribute = (Attribute)updateTemplateElement(
				container, nonSpecificTemplateAttribute, currentMainRowNumber, firstSpecific, dimensionPartitionTemplateAttribute, 
				attributeName, null, independentAttribute);
		
		// Process with children
		if (oldAttribute.hasChildren()) {
			
			List<OldSpecificAttributeContent> oldSpecificAttributeContentList = null;
			List<OldEmptySpecificElementContent> oldEmptySpecificAttributeContentList = null;

			if (oldAttribute instanceof OldAttributeDescription) {
				OldAttributeDescription oldAttributeDescription = (OldAttributeDescription)oldAttribute;
				oldSpecificAttributeContentList = oldAttributeDescription.getOldSpecificAttributeContentList();
				oldEmptySpecificAttributeContentList = oldAttributeDescription.getOldEmptySpecificAttributeContentList();
			} else {
				throw new FunctionalException(FunctionalException.getErrorMessageUnhandledCaseOfElementChildrenType());
			}
			
			// Assumption: no pointers with children but range ones
			MyUtils.checkStatusProgram(!oldAttribute.getPointer() || (oldAttribute.getPointer() && 
					MyUtils.nullOrEmpty(oldSpecificAttributeContentList)), 
					MartConfiguratorUtils.displayJdomElement(oldAttribute.getJdomElement()) + ", " +
					attribute.getClass().getSimpleName() + ", " + oldAttribute.getClass().getSimpleName() + ", " + oldAttribute.getPointer() + ", " + 
					(MyUtils.nullOrEmpty(oldSpecificAttributeContentList)));
			
			// Simply add row to range
			if (oldEmptySpecificAttributeContentList!=null && !oldEmptySpecificAttributeContentList.isEmpty()) {
				
				// Erase main partition rows since they are specified one by one
				attribute.getTargetRange().removePartition(mainPartitionTable);
				for (OldEmptySpecificElementContent oldEmptySpecificAttributeContent : oldEmptySpecificAttributeContentList) {
					String mainRowName = oldEmptySpecificAttributeContent.getRangeInternalName();
					Integer newCurrentRow = mainPartitionTable.getRowNumber(mainRowName);
					MyUtils.checkStatusProgram(newCurrentRow!=null);
					
					if (attribute.getPointer() || (vars.isTemplate() && 
							oldAttribute.checkDatabase(params.getTemplateName(), general.getDatabaseCheck(), mainRowName))) {
																							// only for template (no access to DB otherwise)
						attribute.getTargetRange().addRangePartitionRow(mainPartitionTable, newCurrentRow, true);
					}
				}
			}
				
			if (oldSpecificAttributeContentList!=null && !oldSpecificAttributeContentList.isEmpty()) {
				int specific = 0;
				for (OldSpecificAttributeContent oldSpecificAttributeContent : oldSpecificAttributeContentList) {
					String mainRowName = oldSpecificAttributeContent.getRangeInternalName();
					Integer newCurrentRow = mainPartitionTable.getRowNumber(mainRowName);
					MyUtils.checkStatusProgram(newCurrentRow!=null);
					Attribute childAttribute = transformAttribute(container, oldSpecificAttributeContent, attribute, newCurrentRow, specific==0);
					if (childAttribute!=null) {
						specific++;
					}
				}
			} else {
				throw new FunctionalException(FunctionalException.getErrorMessageUnhandledCaseOfElementChildrenType());
			}
		}
		
		// If pointer, prepare it's dataset transformation
		if (attribute.getPointer()) {
			boolean valid = this.pointerTransformation.preparePointedDatasetTransformation(oldAttribute, mainPartitionTable, attribute);
			if (!valid) {
				return null;
			}
		}
		
		return attribute;
	}

	@Override
	Filter createNewFilter(OldFilter oldFilter, Integer currentMainRowNumber, List<Integer> mainRowsList,
			DimensionPartition dimensionPartition, Boolean forcedVisibility,
			FilterDisplayType nonSpecificFilterDisplayType) throws FunctionalException, TechnicalException {
		MyUtils.errorProgram("Shouldn't be here");
		return null;
	}
	
	@Override
	public Attribute createNewAttribute(OldAttribute oldAttribute, Integer currentMainRowNumber, List<Integer> mainRowsList,
			DimensionPartition dimensionPartition) throws FunctionalException, TechnicalException {
		
		PartitionTable mainPartitionTable = vars.getMainPartitionTable();	// Used thoroughly here		
		String attributeName = help.replaceAliases(oldAttribute.getInternalName());
		Boolean pointer = oldAttribute.getPointer();
		
		Attribute newAttribute = new Attribute(mainPartitionTable, attributeName);
				
		// Add ranges
		updateRangeWithMainPartition(oldAttribute, currentMainRowNumber, mainRowsList, mainPartitionTable, newAttribute, null);
		
		// Add dimensionTable partition range
		// Look for dimension partitions and create it if doesn't already exist
		PartitionTable dimensionPartitionTable = updateRangeWithDimensionPartition(dimensionPartition, newAttribute);
		
		String tableName = null;
		if (!pointer) {
			tableName = computeTableName(oldAttribute, dimensionPartition, mainPartitionTable.getName(), dimensionPartitionTable);
			if (null==tableName) {	// No matching table for instance, ignore element
				return null;
			}
		}
		
		assignSimpleProperties(oldAttribute, newAttribute, tableName);
		
		// For attributes only
		Boolean selectedByDefault = oldAttribute.getDefault_();
		newAttribute.setSelectedByDefault(selectedByDefault);
		if (!pointer) {
			String linkURL = help.replaceAliases(oldAttribute.getLinkoutURL());
			Integer maxLength = oldAttribute.getMaxLength();
			MyUtils.checkStatusProgram(!help.containsAliases(String.valueOf(maxLength)));
			
			newAttribute.setMaxLength(maxLength);
			newAttribute.setLinkURL(linkURL);
		}
		
		return newAttribute;
	}
	
	@Override
	Filter updateNonSpecificTemplateFilter(Filter templateFilter, Filter newFilter, Integer currentMainRow, boolean firstSpecific) throws FunctionalException, TechnicalException {
		MyUtils.errorProgram("Shouldn't be here");
		return null;
	}
	
	protected Attribute updateNonSpecificTemplateAttribute(Attribute templateAttribute, 
			Attribute newAttribute, Integer currentMainRow, boolean firstSpecific) throws FunctionalException, TechnicalException {
		
		updateNonSpecificTemplateElement(templateAttribute, newAttribute, currentMainRow, firstSpecific);
		
		// Check that all these properties are the same, exception made for the first specific which can define the 1st values for some properties
		if (!TransformationUtils.checkValidSpecificityInteger(templateAttribute.getMaxLength(), newAttribute.getMaxLength(), firstSpecific)) {
			throwForbiddenSpecificityException(templateAttribute, newAttribute);
		}
		
		// Update properties that are allowed to be part specific
		Range targetRange = templateAttribute.getTargetRange();
		Set<Integer> mainRowsSet = targetRange.getMainRowsSet();
		
		templateAttribute.setLinkURL(updateSpecificProperty(
				currentMainRow, templateAttribute.getLinkURL(), newAttribute.getLinkURL(), firstSpecific, mainRowsSet));
		
		return templateAttribute;
	}
}
