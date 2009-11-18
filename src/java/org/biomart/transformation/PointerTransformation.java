package org.biomart.transformation;


import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.biomart.common.general.exceptions.FunctionalException;
import org.biomart.common.general.exceptions.TechnicalException;
import org.biomart.common.general.utils.MyUtils;
import org.biomart.objects.MartConfiguratorUtils;
import org.biomart.objects.helpers.PartitionReference;
import org.biomart.objects.objects.Element;
import org.biomart.objects.objects.Part;
import org.biomart.objects.objects.PartitionTable;
import org.biomart.objects.objects.Range;
import org.biomart.old.martService.Configuration;
import org.biomart.old.martService.objects.DatasetInMart;
import org.biomart.old.martService.objects.MartInVirtualSchema;
import org.biomart.transformation.helpers.MartServiceIdentifier;
import org.biomart.transformation.helpers.PointerElementInfo;
import org.biomart.transformation.helpers.TransformationGeneralVariable;
import org.biomart.transformation.helpers.TransformationHelper;
import org.biomart.transformation.helpers.TransformationParameter;
import org.biomart.transformation.helpers.TransformationUtils;
import org.biomart.transformation.helpers.TransformationVariable;
import org.biomart.transformation.oldXmlObjects.OldAttribute;
import org.biomart.transformation.oldXmlObjects.OldElement;
import org.jdom.Document;

public class PointerTransformation {
	
	protected TransformationGeneralVariable general = null;
	protected TransformationParameter params = null;
	protected TransformationVariable vars = null;
	protected TransformationHelper help = null;

	public PointerTransformation(TransformationGeneralVariable general, TransformationParameter params, 
			TransformationVariable vars, TransformationHelper help) throws TechnicalException {
		this.general = general;
		this.params = params;
		this.vars = vars;
		this.help = help;
	}
	
	/**
	 * Prepare the pointers for processing after all the elements have been transformed
	 * @param oldElement
	 * @param mainPartitionTable
	 * @param newElement
	 * @return
	 * @throws FunctionalException
	 */
	protected boolean preparePointedDatasetTransformation(OldElement oldElement, PartitionTable mainPartitionTable, 
			Element newElement) throws FunctionalException {
		
		boolean isAttribute = oldElement instanceof OldAttribute;
		
		// Check that pointerElement doesn't not have aliases/partition reference
		if (help.containsAliases(oldElement.getPointerElement())) {
			throw new FunctionalException("Unhandled");
		}
		
		// Check that there are no dimension partition on the pointer (unhandled)
		if (oldElement.getDimensionPartition()!=null && oldElement.getDimensionPartition().getPartition()) {
			throw new FunctionalException("Unhandled");
		}
		
		String pointerDataset = oldElement.getPointerDataset();
		String unaliasedPointerDataset = help.replaceAliases(pointerDataset);
				
		// Populate the list of plain dataset names that need to be transformed in order to process the pointer fully
		// The list may be composed of 1 element only if there is no partition reference on the name
		// Each element of the list will not necessarily be transformed individually later on, the transformation of a template may transform all of them at a time 
		List<String> split = MartConfiguratorUtils.extractPartitionReferences(unaliasedPointerDataset);
		Set<String> plainPointerDatasetSet = new HashSet<String>();
		if (split.size()==1) {	// no partition references
			plainPointerDatasetSet.add(split.get(0));
		} else if (split.size()==3) {	// only 1 partition reference
			PartitionReference partitionReference = PartitionReference.fromString(split.get(1));
			MyUtils.checkStatusProgram(partitionReference.getPartitionTableName().equals(mainPartitionTable.getName()));
		
			// Go throw all the main rows for that element (already narrowed down to the relevant one by then), we checked there was no other partition (dimension) earlier
			Set<Integer> mainRowsSet = newElement.getTargetRange().getMainRowsSet();
			MyUtils.checkStatusProgram(!mainRowsSet.isEmpty());
			String plainPointerDataset = null;
			for (int mainRowNumber : mainRowsSet) {
				plainPointerDataset = split.get(0);
				plainPointerDataset+=mainPartitionTable.getValue(mainRowNumber, partitionReference.getColumn());
				plainPointerDataset += split.get(2);
				plainPointerDatasetSet.add(plainPointerDataset);			
			}
		} else {	// more partition reference are not handled
			throw new FunctionalException("Unhandled, " + split);
		}
		
		List<String> mainRowsList = mainPartitionTable.getRowNamesList();
		Boolean local = mainRowsList.containsAll(plainPointerDatasetSet);
									// if this is a local pointer: all plainPointerDataset belong to rows of the main table
		
		if(!local) {
			// Check that all the pointed dataset are accessible (if webservice, actually retrive and store it to save time)
			for (String plainPointerDataset : plainPointerDatasetSet) {
				boolean accessible = checkAccessibleDataset(plainPointerDataset);
				if (!TransformationUtils.checkForWarning(!accessible, vars.getUnexistingPointedDatasetWarningList(),
						(isAttribute ? ElementTransformation.ATTRIBUTE_STRING : ElementTransformation.FILTER_STRING) + " " + oldElement.getInternalName() + 
						" points to a dataset that cannot be found (may need to add the appropriate database as input): " + plainPointerDataset)) {					
					return false;	// return false because pointer is not valid (to be ignored)
				}
			}

			// Store the names for later processing (when all the elements are transformed)
			vars.getPointedDatasetPlainNamesSet().addAll(plainPointerDatasetSet);
			vars.getPointedDatasetGenericNamesSet().add(unaliasedPointerDataset);
		}
		
		// Create and store a PointerElementInfo to help process the pointers later
		PointerElementInfo pointerElementObject = null;
		if (local) {	// Local pointer	
			pointerElementObject = new PointerElementInfo(vars.getDataset().getName(), plainPointerDatasetSet, newElement, oldElement.getPointerElement(), true, isAttribute);			
		} else {
			pointerElementObject = new PointerElementInfo(unaliasedPointerDataset, plainPointerDatasetSet, newElement, oldElement.getPointerElement(), false, isAttribute);
		}
		vars.getPointerElementList().add(pointerElementObject);	// for pointer's info to be updated laters
		
		return true;	// return true to indicate pointer seems valid
	}

	/**
	 * Check that a pointed dataset is accessible (either via DB or webservice)
	 * @param plainPointerDataset
	 * @return
	 */
	private boolean checkAccessibleDataset(String plainPointerDataset) {
		boolean accessible = true;
		
		// Check that it is accessible via one of the DB provided
		if (!params.isWebservice()) {
			String templateName = general.getDatabaseCheck().getDatasetNameToTemplateName().get(plainPointerDataset);
			accessible = null!=templateName;
		}
		// Check that we can retrieve it over webservice (do so and store it to save time)
		else {
			try {
				if (general.getDatasetNameToWebServiceJdomDocument().get(plainPointerDataset)==null) {
					
					Configuration configuration = help.getConfiguration(params.getTrueHostIdentifier());
					
					Document xmlDocument = configuration.getXml(params.getVirtualSchema(), plainPointerDataset);
							/*TransformationUtils.fetchMartServiceXmlDocument(
							general.getServer(), general.getPathToMartService(), params.getVirtualSchema(), plainPointerDataset);*/
					
					DatasetInMart datasetInMart = help.getDatasetInMart(configuration, params.getVirtualSchema(), plainPointerDataset);
					
					MyUtils.checkStatusProgram(null!=datasetInMart, 
							configuration.getMartServiceUrl() + ", " + params.getVirtualSchema() + ", " + plainPointerDataset);
					MartInVirtualSchema martInVirtualSchema = datasetInMart.getMartInVirtualSchema();
					TransformationUtils.writeWebServiceXmlConfigurationFile(xmlDocument, general.getTransformationsGeneralOutput(), 
							params.isWebservice(), general.getDatabaseVersion(), 
							new MartServiceIdentifier(martInVirtualSchema), params.getVirtualSchema(), plainPointerDataset);
					
					// If no exception, add it to map so we don't use webservice again for that dataset
					general.getDatasetNameToWebServiceJdomDocument().put(plainPointerDataset, xmlDocument);
				}
			} catch (TechnicalException e) {
				accessible=false;
			}
		}
		return accessible;
	}

	/**
	 * Transform all the pointed dataset recursively, so we are able to update the pointers accordingly later (pointed dataset name, sourceRange, ...)
	 * @throws TechnicalException
	 * @throws FunctionalException
	 */
	public void transformPointedDatasets() throws TechnicalException, FunctionalException {
		
		// Create the list of either datasets or templates that need to be transformed (for all pointers of the current dataset/template)
		HashSet<String> datasetOrTemplateToTransformAsList = createDatasetOrTemplateToTransformSet();

		// Go through all of them and do the transformation
		for (String datasetOrTemplateName : datasetOrTemplateToTransformAsList) {			
			
			System.out.println(MyUtils.DASH_LINE + MyUtils.LINE_SEPARATOR + "Transforming pointedDataset = " + datasetOrTemplateName);

			// Prepare the transformation
			MartServiceIdentifier trueHostIdentifier = null;
			if (params.isWebservice()) {
				trueHostIdentifier = help.getNewTrueHostIdentifer(
						params.getTrueHostIdentifier(), params.getVirtualSchema(), datasetOrTemplateName);
			}
			
			TransformationParameter params = new TransformationParameter(this.params.isWebservice(), 
					trueHostIdentifier, this.params.getVirtualSchema(),
					this.params.isWebservice() ? datasetOrTemplateName : null, this.params.isWebservice() ? null : datasetOrTemplateName, 
					this.general, this.params.getDataFolderName());
			
			// Do the transformation
			Transformation transformation = new Transformation(this.general, params);
			transformation.transform();
			
			// Close warning/error file
			try {
				params.getErrorFileWriter().close();
			} catch (IOException e) {
				throw new TechnicalException(e);
			}
			
			System.out.println(MyUtils.LINE_SEPARATOR + "Transformed pointedDataset = " + datasetOrTemplateName + 
					MyUtils.LINE_SEPARATOR + MyUtils.DASH_LINE + MyUtils.LINE_SEPARATOR);

			// Update the transformation name mapping: used the transformed dataset name instead of the original dataset/template name
			String transformedDatasetName = transformation.getTransformedDatasetName();
			general.updateTransformedDatasetNameInPlainDatasetNameToTransformedDatasetName(
					datasetOrTemplateName, transformedDatasetName, params.isWebservice());
			
			// Add to the list of already transformed dataset/templates (so we don't redo it)
			if (!params.isWebservice()) {
				general.addTransformedTemplates(transformedDatasetName, transformation);
			} else {
				general.addTransformedDatasets(transformedDatasetName, transformation);
			}
	
			// If using web service, sleep a bit in between each transformation so we don't overload mart service
			if (params.isWebservice()) {
				try {
					Thread.sleep(100);
				} catch (InterruptedException e) {
					throw new TechnicalException(e);
				}
			}
		}
		
		System.out.println(MyUtils.DASH_LINE + MyUtils.LINE_SEPARATOR + "Resuming " + vars.getDataset().getName());
	}

	private HashSet<String> createDatasetOrTemplateToTransformSet() throws FunctionalException {
		
		System.out.println("this.pointedDatasetPlainNamesSet = " + vars.getPointedDatasetPlainNamesSet());
		System.out.println("this.pointedDatasetGenericNamesSet = " + vars.getPointedDatasetGenericNamesSet());

		// Create set of dataset/template that need to be transformed in order to update the pointers accordingly (ignore GenomicSequence)
		HashSet<String> pointedDatasetPlainNamesSet = vars.getPointedDatasetPlainNamesSet();
		HashSet<String> datasetOrTemplateToTransformSet = new HashSet<String>();
		for (String pointedDatasetPlainName : pointedDatasetPlainNamesSet) {
			if (!isGenomicSequence(pointedDatasetPlainName)) {
				String datasetOrTemplateToTransform = null;
				
				// In case of DB transfo, get the template associated with the plain dataset name
				if (!params.isWebservice()) { 
					String templateName = general.getDatabaseCheck().getDatasetNameToTemplateName().get(pointedDatasetPlainName);
					if (null==templateName) {
						throw new FunctionalException("No corresponding template for dataset " + pointedDatasetPlainName);
					}
					datasetOrTemplateToTransform = templateName;
				}
				// Otherwise just use the provided name
				else {
					datasetOrTemplateToTransform = pointedDatasetPlainName;
				}
				datasetOrTemplateToTransformSet.add(datasetOrTemplateToTransform);
				general.addDatasetToPlainDatasetNameToTransformedDatasetName(pointedDatasetPlainName, datasetOrTemplateToTransform);
			}
		}
		
		// Use a list so we can go through it sequentially
		ArrayList<String> datasetOrTemplateToTransformAsList = new ArrayList<String>(datasetOrTemplateToTransformSet);
		
		// Remove ones that may have already been tranformed
		for (int i = datasetOrTemplateToTransformAsList.size()-1; i >= 0 ; i--) {	// To avoid concurrent access
			String datasetOrTemplateName = datasetOrTemplateToTransformAsList.get(i);
			
			if ((!params.isWebservice() && general.transformedDatasetsContains(datasetOrTemplateName) ||
					(params.isWebservice() && general.transformedTemplatesContains(datasetOrTemplateName)))) {
				MyUtils.checkStatusProgram(general.getFromTransformedDatasets(datasetOrTemplateName)!=null ||
						general.getFromTransformedTemplates(datasetOrTemplateName)!=null, datasetOrTemplateName);
				datasetOrTemplateToTransformAsList.remove(datasetOrTemplateName);
				general.removeDatasetToPlainDatasetNameToTransformedDatasetName(datasetOrTemplateName);
			}
		}
		
		// Back to a set to avoid doubles
		datasetOrTemplateToTransformSet = new HashSet<String>(datasetOrTemplateToTransformAsList);
		System.out.println("datasetOrTemplateToTransformSet = " + datasetOrTemplateToTransformSet);
		
		return datasetOrTemplateToTransformSet;
	}
	
	private boolean isGenomicSequence(String pointedDatasetPlainName) {
		Boolean genomicSequence = null;
		String datasetType = null;
		if (!params.isWebservice()) {
			datasetType = general.getDatabaseCheck().getDatasetNameToDatasetType().get(pointedDatasetPlainName);
		} else {
			//TODO use martservice's mart instead? would require loading entire config...
			Document xmlDocument = general.getDatasetNameToWebServiceJdomDocument().get(pointedDatasetPlainName);
			MyUtils.checkStatusProgram(null!=xmlDocument, pointedDatasetPlainName);
			datasetType = xmlDocument.getRootElement().getAttributeValue("type");
		}
		MyUtils.checkStatusProgram(null!=datasetType, pointedDatasetPlainName);		
		genomicSequence = TransformationUtils.isGenomicSequence(datasetType);
		if (!genomicSequence) {
			MyUtils.checkStatusProgram(TransformationUtils.isTableSet(datasetType));
		}
		return genomicSequence;
	}

	public void updatePointers() throws FunctionalException, TechnicalException {
		
		// Display list
		System.out.println("this.pointerList.size() = " + vars.getPointerElementList().size());
		for (PointerElementInfo pointerElement : vars.getPointerElementList()) {
			Element pointingElement = pointerElement.getPointingElement();
			System.out.println(pointingElement.getName() + MyUtils.TAB_SEPARATOR + pointingElement.getPointedElementName());
		}
		System.out.println();
		
		// For all the pointers to update in the current dataset/template, proceed with update
		for (PointerElementInfo pointerElementInfo : vars.getPointerElementList()) {
			System.out.println(pointerElementInfo.toString());
			
			Element pointedElement = null;
			String transformedDatasetName = null;
			Transformation transformation = null;
			
			String pointedDatasetName = pointerElementInfo.getPointedDatasetName();
			MyUtils.checkStatusProgram(null!=pointedDatasetName);
			String pointedElementName = pointerElementInfo.getPointedElementName();
			MyUtils.checkStatusProgram(null!=pointedElementName);
			
			Element pointingElement = pointerElementInfo.getPointingElement();
			Boolean isAttributePointer = pointerElementInfo.getAttribute();
			
			// If local pointer than simply get the pointed attribute from the appropriate map, and the current name of the dataset
			if (pointerElementInfo.getLocal()) {
				//MyUtils.checkStatusProgram(!vars.isTemplate() || MartConfiguratorUtils.containsPartitionReferences(pointedDatasetName));
				pointedElement = isAttributePointer ? vars.getAttributeMap().get(pointedElementName) : vars.getFilterMap().get(pointedElementName);
				transformedDatasetName = vars.getDataset().getName();
			}
			
			// Otherwise try to find the appropriate transformation (the remote dataset has already been transformed, we need to find where)
			else {
				
				// Get the list of pointer dataset plain names
				List<String> plainPointerDatasetList = new ArrayList<String>(pointerElementInfo.getPlainPointerDatasetSet());
				
				// If genomic sequence, disregard
				if (!handleGenomicSequence(plainPointerDatasetList, pointedDatasetName, pointingElement)) {
					continue;
				}
				
				// Fetch the transformed dataset name
				transformedDatasetName = fetchTransformedDatasetName(plainPointerDatasetList);
				MyUtils.checkStatusProgram(transformedDatasetName!=null);
				
				// Get the actual transformed dataset for the pointed dataset, thanks to its name
				if (!params.isWebservice()) {			
					transformation = general.getFromTransformedTemplates(transformedDatasetName);
				} else {
					transformation = general.getFromTransformedDatasets(transformedDatasetName);					
				}
				MyUtils.checkStatusProgram(null!=transformation, "transformedDatasetName = " + transformedDatasetName);
				
				// Finally get the element itself
				pointedElement = transformation.getElementFromTransformation(
						params.getVirtualSchema(), transformedDatasetName, pointedElementName, isAttributePointer);
			}
			
			// Make sure a matching transformed dataset has been found (illegal)
			MyUtils.checkStatusProgram(transformedDatasetName!=null);
			
			// If no matching element has been found, send a warning
			if (!TransformationUtils.checkForWarning(null==pointedElement, vars.getUnexistingPointedElementWarningList(),
					pointingElement.getName() + " points to unexisting " + pointerElementInfo.getPointedElementName() + " in " + pointerElementInfo.getPointedDatasetName())) {
				continue;
			}
			
			
	if (1>0) {	//TODO sort this out
			MyUtils.checkStatusProgram(pointingElement.getClass().equals(pointedElement.getClass()),
					pointingElement.getName() + ", " + 
					pointingElement.getClass().getSimpleName() + ", " + pointedElement.getClass().getSimpleName());
	}

			// Update pointer
			PartitionTable remoteMainPartitionTable = pointerElementInfo.getLocal() ? 
					vars.getMainPartitionTable() : transformation.getMainPartitionTable();
			updatePointer(pointedDatasetName, transformedDatasetName, pointingElement, pointedElement, remoteMainPartitionTable);
		}
	}
	
	private boolean handleGenomicSequence(List<String> plainPointerDatasetList, String pointedDatasetName, Element pointingElement) {
		boolean genomicSequence = false;
		for (int i = 0; i < plainPointerDatasetList.size(); i++) {
			String pointedDatasetPlainName = plainPointerDatasetList.get(i);
			
			// Make sure it's consistent: remote are all genomic sequences (ignored) or not at all
			if (isGenomicSequence(pointedDatasetPlainName)) {
				if (i==0) {
					genomicSequence = true;
				} else {
					MyUtils.checkStatusProgram(genomicSequence);
				}
			}
		}

		// If not valid (GenomicSequence), disregard pointer
		if (genomicSequence) {
			pointingElement.setDatasetName("!!!! GENOMIC SEQUENCE !!!");	//TODO better
			
			TransformationUtils.sendWarning(vars.getPointingToGenomicSequenceWarningList(), 
					pointingElement.getName() + " points to GenomicSequence(s) (" + pointedDatasetName + ")");
			return false;
		}
		
		return true;
	}
	
	private String fetchTransformedDatasetName(List<String> plainPointerDatasetList) throws FunctionalException {
		
		String transformedDatasetName = null;
		
		// Find the transformed dataset name, all elements in the above created list must provide the same transformed dataset name
		// (technically one the first would be do then, but we want to make sure they all give the same)
		for (int i = 0; i < plainPointerDatasetList.size(); i++) {
			String pointedDatasetPlainName = plainPointerDatasetList.get(i);
			
			// Assign appropriate name (1 and only 1 match)
			// Get the transformed dataset NAME thanks to the map (old name->transformed name)
			String transformedDatasetNameTmp = general.getTransformedDatasetName(pointedDatasetPlainName);
			
			// Check that there is indeed a match: must have one
			if (transformedDatasetNameTmp==null) {
				throw new FunctionalException("No corresponding pointed dataset name (plain name) for : " + pointedDatasetPlainName);
			}
			
			// Assign the transformed dataset 
			if (i==0) {
				transformedDatasetName = transformedDatasetNameTmp;
			}
			// Check that only transformed dataset is a match
			else if (!transformedDatasetNameTmp.equals(transformedDatasetName)) {
				throw new FunctionalException("Ambiguous pointed dataset name (plain name): " + pointedDatasetPlainName +
						", transformedDatasetName = " + transformedDatasetName + ", transformedDatasetNameTmp = " + transformedDatasetNameTmp);
			}
			
		}
		
		return transformedDatasetName;
	}

	/**
	 * Actually update the pointer
	 * @param pointedDatasetName
	 * @param transformedDatasetName
	 * @param pointingElement
	 * @param pointedElement
	 * @param remoteMainPartitionTable
	 * @throws FunctionalException
	 */
	private void updatePointer(String pointedDatasetName, String transformedDatasetName, 
			Element pointingElement, Element pointedElement, PartitionTable remoteMainPartitionTable) throws FunctionalException {

		// Now we have the pointed element and the name of the pointed dataset, we can update the pointer with the appropriate values
		// Update path (except dataset that can be part-specific)
		pointingElement.setLocationName(pointedElement.getLocationName());
		pointingElement.setMartName(pointedElement.getMartName());
		pointingElement.setVersion(pointedElement.getVersion());
		pointingElement.setConfigName(pointedElement.getConfigName());
		
		// Update dataset
		pointingElement.setDatasetName(transformedDatasetName);
		
		// Update sourceRange
		updateSourceRange(pointedDatasetName, pointingElement, pointedElement, remoteMainPartitionTable);		

		// Update pointedElement
		pointingElement.setPointedElement(pointedElement);	// Also set the name
	}
	
	/**
	 * Update the source range
	 * @param pointedDatasetName
	 * @param pointingElement
	 * @param pointedElement
	 * @param remoteMainPartitionTable
	 * @throws FunctionalException
	 */
	private void updateSourceRange(String pointedDatasetName, Element pointingElement, Element pointedElement, PartitionTable remoteMainPartitionTable) throws FunctionalException {
		
		Range pointedTargetRange = pointedElement.getTargetRange();
		Range pointingTargetRange = pointingElement.getTargetRange();
		Range pointingSourceRange = pointingElement.getSourceRange();
		
		// Go through all the main rows for the pointer (determined earlier when doing the pre-transformation)
		for (Integer mainRowNumber : pointingTargetRange.getMainRowsSet()) {	
			
			// Get the row name (we can only match the names of the rows, not the number (order in each dataset is not guaranteed to be the same)
			String unreferencedRemoteDatasetName = help.unreference(pointedDatasetName, mainRowNumber);
			
			// Find the matching row number in the remote dataset
			int remoteRowNumber = remoteMainPartitionTable.getRowNamesList().indexOf(unreferencedRemoteDatasetName);
			MyUtils.checkStatusProgram(remoteRowNumber!=-1, unreferencedRemoteDatasetName + ", " + remoteMainPartitionTable.getRowNamesList());
			
			// Find the one match
			Part matchingRemotePart = null;
			for (Part remotePart : pointedTargetRange.getPartSet()) {
				int remoteRowNumberTmp = remotePart.getMainRowNumber();
				if (remoteRowNumber==remoteRowNumberTmp) {
					MyUtils.checkStatusProgram(matchingRemotePart==null, ""+matchingRemotePart);	// Check that there's only one
					matchingRemotePart = remotePart;
				}
			}
			MyUtils.checkStatusProgram(matchingRemotePart!=null, "remoteRowNumber = " + remoteRowNumber + 
					", unreferencedRemoteDatasetName = " + unreferencedRemoteDatasetName + ", pointedElement = " +
					MartConfiguratorUtils.displayJdomElement(pointedElement.generateXml()));
			
			// Assign part to range
			pointingSourceRange.addPart(matchingRemotePart);
		}
		System.out.println("\t" + pointingElement.getSourceRange().toString());
	}
}
