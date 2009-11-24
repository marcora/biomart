package org.biomart.transformation;


import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;


import org.biomart.common.general.exceptions.FunctionalException;
import org.biomart.common.general.exceptions.TechnicalException;
import org.biomart.common.general.utils.MyUtils;
import org.biomart.transformation.helpers.TransformationConstants;
import org.biomart.transformation.helpers.TransformationUtils;
import org.biomart.transformation.oldXmlObjects.OldAttributeCollection;
import org.biomart.transformation.oldXmlObjects.OldAttributeDescription;
import org.biomart.transformation.oldXmlObjects.OldAttributeGroup;
import org.biomart.transformation.oldXmlObjects.OldAttributePage;
import org.biomart.transformation.oldXmlObjects.OldDatasetConfig;
import org.biomart.transformation.oldXmlObjects.OldDynamicDataset;
import org.biomart.transformation.oldXmlObjects.OldElement;
import org.biomart.transformation.oldXmlObjects.OldEmptySpecificElementContent;
import org.biomart.transformation.oldXmlObjects.OldExportable;
import org.biomart.transformation.oldXmlObjects.OldFilterCollection;
import org.biomart.transformation.oldXmlObjects.OldFilterDescription;
import org.biomart.transformation.oldXmlObjects.OldFilterGroup;
import org.biomart.transformation.oldXmlObjects.OldFilterPage;
import org.biomart.transformation.oldXmlObjects.OldImportable;
import org.biomart.transformation.oldXmlObjects.OldNode;
import org.biomart.transformation.oldXmlObjects.OldOptionFilter;
import org.biomart.transformation.oldXmlObjects.OldOptionValue;
import org.biomart.transformation.oldXmlObjects.OldPushAction;
import org.biomart.transformation.oldXmlObjects.OldSpecificAttributeContent;
import org.biomart.transformation.oldXmlObjects.OldSpecificFilterContent;
import org.biomart.transformation.oldXmlObjects.OldSpecificOptionContent;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;

public class PopulateOldObjects {

	public static void main(String[] args) throws Exception {
		/*PopulateOldObjects populateOldObjects = new PopulateOldObjects(
				Transformation.INPUT_XML_FILE_PATH_AND_NAME, Transformation.TEMPLATE);
		populateOldObjects.populate();
		System.out.println("done.");*/
	}
	
	private Document xmlDocument = null;
	private Boolean template = null;
	
	public PopulateOldObjects(String inputXmlFilePathAndName, Boolean template) throws TechnicalException, MalformedURLException {
		this(new URL(inputXmlFilePathAndName), template);
	}
	public PopulateOldObjects(URL inputXmlUrl, Boolean template) throws TechnicalException {
		try {
			SAXBuilder builder = new SAXBuilder();
			if (null==this.xmlDocument) {
				this.xmlDocument = builder.build(inputXmlUrl);
			}
		} catch (JDOMException e) {
			throw new TechnicalException(e);
		} catch (IOException e) {
			throw new TechnicalException(e);
		}
		this.template = template;
	}
	public PopulateOldObjects(Document inputXmlDocument, Boolean template) throws TechnicalException {
		this.xmlDocument = inputXmlDocument;
		this.template = template;
	}
	private void checkNochildren(Element element) {
		MyUtils.checkStatusProgram(element.getChildren().isEmpty(), "", true);
	}
	private void checkHaschildren(Element element) {
		MyUtils.checkStatusProgram(!element.getChildren().isEmpty(), "", true);
	}
	
	public OldDatasetConfig populate() throws FunctionalException {
		return processDatasetConfig(this.xmlDocument.getRootElement());
	}
	public OldDatasetConfig processDatasetConfig(Element jdomDatasetConfig) throws FunctionalException {
		
		OldDatasetConfig oldDatasetConfig = new OldDatasetConfig(jdomDatasetConfig);
		if (!oldDatasetConfig.isTableSetDataset()) {
			return null;
		}
		
		@SuppressWarnings("unchecked")
		List<Element> elementList = jdomDatasetConfig.getChildren();
		for (Element element : elementList) {
			String elementName = element.getName();
			if (elementName.equals("DynamicDataset")) {	// Only for template
				MyUtils.checkStatusProgram(this.template);
				OldDynamicDataset dynamicDataset = processDynamicDataset(element);
				oldDatasetConfig.addOldDynamicDataset(dynamicDataset);
			} else if (elementName.equals("Importable")) {
				OldImportable importable = processImportable(element);
				if (importable.isValid()) {
					oldDatasetConfig.addOldImportable(importable);
				}
			} else if (elementName.equals("Exportable")) {
				OldExportable exportable = processExportable(element);
				if (exportable.isValid()) {
					oldDatasetConfig.addOldExportable(exportable);
				}
			} else if (elementName.equals("FilterPage")) {
				OldFilterPage filterPage = processFilterPage(element);
				if (filterPage.isValid()) {
					oldDatasetConfig.addOldFilterPage(filterPage);
				}
			} else if (elementName.equals("AttributePage")) {
				OldAttributePage attributePage = processAttributePage(element);
				if (attributePage.isValid()) {
					oldDatasetConfig.addOldAttributePage(attributePage);
				}
			} else if (elementName.equals("MainTable")) {	// Only relevant for non-template (kept for template to check consistency with DB though)
				OldNode oldMainTable = processNode(element);
				List<OldNode> oldMainTableList = oldDatasetConfig.getOldMainTableList();
				if (!oldMainTableList.contains(oldMainTable)) {	// some config repeat it...
					oldDatasetConfig.addOldMainTable(oldMainTable);
				}
			} else if (elementName.equals("Key")) {	// Only relevant for non-template (kept for template to check consistency with DB though)
				OldNode oldKey = processNode(element);
				List<OldNode> oldKeyList = oldDatasetConfig.getOldKeyList();
				if (!oldKeyList.contains(oldKey)) {	// some config repeat it...
					oldDatasetConfig.addOldKey(oldKey);
				}
			} else {
				TransformationUtils.throwUnknownElementException(element);
			}
		}
		MyUtils.checkStatusProgram(this.template || oldDatasetConfig.getOldMainTableList().size()==oldDatasetConfig.getOldKeyList().size(),
				oldDatasetConfig.getOldMainTableList().size() + ", " + oldDatasetConfig.getOldKeyList().size());
		
		return oldDatasetConfig;
	}

	private OldNode processNode(Element element) throws FunctionalException {
		OldNode oldNode = new OldNode(element, true);
		return oldNode;
	}
	
	private OldExportable processExportable(Element element) throws FunctionalException {
		OldExportable oldExportable = new OldExportable(element);
		checkNochildren(element);
		return oldExportable;
	}

	private OldImportable processImportable(Element element) throws FunctionalException {
		OldImportable oldImportable = new OldImportable(element);
		checkNochildren(element);
		return oldImportable;
	}

	private OldDynamicDataset processDynamicDataset(Element element) throws FunctionalException {
		OldDynamicDataset oldDynamicDataset = new OldDynamicDataset(element);
		checkNochildren(element);
		return oldDynamicDataset;
	}

	private OldAttributePage processAttributePage(Element jdomAttributePage) throws FunctionalException {
		OldAttributePage oldAttributePage = new OldAttributePage(jdomAttributePage);
		
		@SuppressWarnings("unchecked")
		List<Element> attributeGroupList = jdomAttributePage.getChildren();
		for (Element jdomAttributeGroup : attributeGroupList) {
			OldAttributeGroup attributeGroup = processAttributeGroup(jdomAttributeGroup);
			if (attributeGroup.isValid()) {
				oldAttributePage.addOldAttributeGroup(attributeGroup);
			}
		}
		
		return oldAttributePage;
	}
	private OldAttributeGroup processAttributeGroup(Element jdomAttributeGroup) throws FunctionalException {
		OldAttributeGroup oldAttributeGroup = new OldAttributeGroup(jdomAttributeGroup);
		
		@SuppressWarnings("unchecked")
		List<Element> attributeCollectionList = jdomAttributeGroup.getChildren();
		for (Element jdomAttributeCollection : attributeCollectionList) {
			OldAttributeCollection attributeCollection = processAttributeCollection(jdomAttributeCollection);
			if (attributeCollection.isValid()) {
				oldAttributeGroup.addOldAttributeCollection(attributeCollection);
			}
		}
		
		return oldAttributeGroup;
	}
	private OldAttributeCollection processAttributeCollection(Element jdomAttributeCollection) throws FunctionalException {
		OldAttributeCollection oldAttributeCollection = new OldAttributeCollection(jdomAttributeCollection);
		
		@SuppressWarnings("unchecked")
		List<Element> attributeDescriptionList = jdomAttributeCollection.getChildren();
		for (Element jdomAttributeDescription : attributeDescriptionList) {

			OldElement oldElementDescription = jdomAttributeDescription.getAttributeValue(TransformationConstants.CROSS_ELEMENT_POINTER_FILTER)==null ?
					processAttributeDescription(jdomAttributeDescription) :
					processFilterDescription(jdomAttributeDescription);	// Exceptions of filters defined among attributes (cross element)
			if (oldElementDescription.isValid()) {
				oldAttributeCollection.addOldElementDescription(oldElementDescription);
			}
		}
		
		return oldAttributeCollection;
	}
	private OldAttributeDescription processAttributeDescription(Element jdomAttributeDescription) throws FunctionalException {
		OldAttributeDescription oldAttributeDescription = new OldAttributeDescription(jdomAttributeDescription);
		
		@SuppressWarnings("unchecked")
		List<Element> specificContentAttributeList = jdomAttributeDescription.getChildren();
		for (Element element : specificContentAttributeList) {
			
			String elementName = element.getName();
			if ((elementName.equals("SpecificAttributeContent") || elementName.equals("SpecificFilterContent")) && 
					!TransformationUtils.isEmptySpecificElementContent(element)) {
				OldSpecificAttributeContent specificContentAttribute = processSpecificContentAttribute(element);
				if (specificContentAttribute.isValid()) {
					oldAttributeDescription.addOldSpecificAttributeContent(specificContentAttribute);
				}
			} else if ((elementName.equals("SpecificAttributeContent") || elementName.equals("SpecificFilterContent")) && 
					TransformationUtils.isEmptySpecificElementContent(element)) {
				OldEmptySpecificElementContent emptySpecificFilterContent = processEmptySpecificElementContent(element);
				oldAttributeDescription.addOldEmptySpecificAttributeContent(emptySpecificFilterContent);
			} else {
				TransformationUtils.throwUnknownElementException(element);
			}
		}
		
		return oldAttributeDescription;
	}
	private OldSpecificAttributeContent processSpecificContentAttribute(Element jdomSpecificAttributeContent) throws FunctionalException {
		OldSpecificAttributeContent oldSpecificAttributeContent = new OldSpecificAttributeContent(jdomSpecificAttributeContent);
		checkNochildren(jdomSpecificAttributeContent);
		return oldSpecificAttributeContent;
	}

	private OldFilterPage processFilterPage(Element jdomFilterPage) throws FunctionalException {
		OldFilterPage oldFilterPage = new OldFilterPage(jdomFilterPage);
		
		@SuppressWarnings("unchecked")
		List<Element> filterGroupList = jdomFilterPage.getChildren();
		for (Element jdomFilterGroup : filterGroupList) {
			OldFilterGroup filterGroup = processFilterGroup(jdomFilterGroup);
			if (filterGroup.isValid()) {
				oldFilterPage.addOldFilterGroup(filterGroup);
			}
		}
		
		return oldFilterPage;
	}
	private OldFilterGroup processFilterGroup(Element jdomFilterGroup) throws FunctionalException {
		OldFilterGroup oldFilterGroup = new OldFilterGroup(jdomFilterGroup);
		
		@SuppressWarnings("unchecked")
		List<Element> filterCollectionList = jdomFilterGroup.getChildren();
		for (Element jdomFilterCollection : filterCollectionList) {
			OldFilterCollection filterCollection = processFilterCollection(jdomFilterCollection);
			if (filterCollection.isValid()) {
				oldFilterGroup.addOldFilterCollection(filterCollection);
			}
		}
		
		return oldFilterGroup;
	}
	private OldFilterCollection processFilterCollection(Element jdomFilterCollection) throws FunctionalException {
		OldFilterCollection oldFilterCollection = new OldFilterCollection(jdomFilterCollection);
		
		@SuppressWarnings("unchecked")
		List<Element> filterDescriptionList = jdomFilterCollection.getChildren();
		for (Element jdomFilterDescription : filterDescriptionList) {
			
			OldElement oldElementDescription = jdomFilterDescription.getAttributeValue(TransformationConstants.CROSS_ELEMENT_POINTER_ATTRIBUTE)==null ?
					processFilterDescription(jdomFilterDescription) :
					processAttributeDescription(jdomFilterDescription);	// Exceptions of attributes defined among filter (cross element)
			if (oldElementDescription.isValid()) {
				oldFilterCollection.addOldElementDescription(oldElementDescription);
			}
		}
		
		return oldFilterCollection;
	}
	private OldFilterDescription processFilterDescription(Element jdomElement) throws FunctionalException {		
		OldFilterDescription oldFilterDescription = new OldFilterDescription(jdomElement);

		@SuppressWarnings("unchecked")
		List<Element> elementList = jdomElement.getChildren();
		for (Element element : elementList) {
			String elementName = element.getName();
			if (elementName.equals("Option") && !TransformationUtils.isOptionValue(element)) {
				OldOptionFilter optionFilter = processOptionFilter(element);
				if (optionFilter.isValid()) {
					oldFilterDescription.addOldOptionFilter(optionFilter);
				}
			} else if (elementName.equals("Option") && TransformationUtils.isOptionValue(element)) {
				OldOptionValue optionValue = processOptionValue(element);
				oldFilterDescription.addOldOptionValue(optionValue);
			} else if ((elementName.equals("SpecificFilterContent") || elementName.equals("SpecificAttributeContent")) && 
					!TransformationUtils.isEmptySpecificElementContent(element)) {
				OldSpecificFilterContent specificFilterContent = processSpecificFilterContent(element);
				if (specificFilterContent.isValid()) {
					oldFilterDescription.addOldSpecificFilterContent(specificFilterContent);
				}
			} else if ((elementName.equals("SpecificFilterContent") || elementName.equals("SpecificAttributeContent")) && 
					TransformationUtils.isEmptySpecificElementContent(element)) {
				OldEmptySpecificElementContent emptySpecificFilterContent = processEmptySpecificElementContent(element);
				oldFilterDescription.addOldEmptySpecificFilterContent(emptySpecificFilterContent);
			} else {
				TransformationUtils.throwUnknownElementException(element);
			}
		}
		
		return oldFilterDescription;
	}
	private OldOptionFilter processOptionFilter(Element jdomElement) throws FunctionalException {
		OldOptionFilter oldOptionFilter = new OldOptionFilter(jdomElement);
		
		@SuppressWarnings("unchecked")
		List<Element> elementList = jdomElement.getChildren();
		for (Element element : elementList) {
			String elementName = element.getName();
			if (elementName.equals("Option") && !TransformationUtils.isOptionValue(element)) {
				TransformationUtils.throwUnhandledElementStructureException(element);
			} else if (elementName.equals("Option") && TransformationUtils.isOptionValue(element)) {
				OldOptionValue optionValue = processOptionValue(element);
				oldOptionFilter.addOldOptionValue(optionValue);
			} else if (elementName.equals("SpecificOptionContent")) {
				OldSpecificOptionContent specificOptionContent = processSpecificOptionContent(element);
				if (specificOptionContent.isValid()) {
					oldOptionFilter.addOldSpecificOptionContent(specificOptionContent);
				}
			} else {
				TransformationUtils.throwUnknownElementException(element);
			}
		}
		
		return oldOptionFilter;
	}
	private OldOptionValue processOptionValue(Element jdomElement) throws FunctionalException {
		OldOptionValue oldOptionValue = new OldOptionValue(jdomElement);
		
		@SuppressWarnings("unchecked")
		List<Element> elementList = jdomElement.getChildren();
		for (Element element : elementList) {
			String elementName = element.getName();
			if (elementName.equals("Option")) {
				OldOptionValue optionValue = processOptionValue(element);
				oldOptionValue.addOldOptionValue(optionValue);
			} else if (elementName.equals("PushAction")) {
				OldPushAction pushAction = processPushAction(element);
				oldOptionValue.addOldPushAction(pushAction);
			} else {
				TransformationUtils.throwUnknownElementException(element);
			}
		}
		
		return oldOptionValue;
	}
	
	
	private OldPushAction processPushAction(Element jdomElement) throws FunctionalException {
		OldPushAction oldPushAction = new OldPushAction(jdomElement);
		
		checkHaschildren(jdomElement);
		
		@SuppressWarnings("unchecked")
		List<Element> elementList = jdomElement.getChildren();
		for (Element element : elementList) {
			String elementName = element.getName();
			if (elementName.equals("Option") && !TransformationUtils.isOptionValue(element)) {
				TransformationUtils.throwUnhandledElementStructureException(element);
			} else if (elementName.equals("Option") && TransformationUtils.isOptionValue(element)) {
				OldOptionValue optionValue = processOptionValue(element);
				oldPushAction.addOldOptionValue(optionValue);
			} else {
				TransformationUtils.throwUnknownElementException(element);
			}
		}
		
		return oldPushAction;
	}
	private OldSpecificOptionContent processSpecificOptionContent(Element jdomElement) throws FunctionalException {
		OldSpecificOptionContent oldSpecificOptionContent = new OldSpecificOptionContent(jdomElement);
		
		@SuppressWarnings("unchecked")
		List<Element> elementList = jdomElement.getChildren();
		for (Element element : elementList) {
			String elementName = element.getName();
			if (elementName.equals("Option") && !TransformationUtils.isOptionValue(element)) {
				TransformationUtils.throwUnhandledElementStructureException(element);
			} else if (elementName.equals("Option") && TransformationUtils.isOptionValue(element)) {
				OldOptionValue optionValue = processOptionValue(element);
				oldSpecificOptionContent.addOldOptionValue(optionValue);
			}else {
				TransformationUtils.throwUnknownElementException(element);
			}
		}
		
		return oldSpecificOptionContent;
	}
	private OldSpecificFilterContent processSpecificFilterContent(Element jdomElement) throws FunctionalException {
		OldSpecificFilterContent oldSpecificFilterContent = new OldSpecificFilterContent(jdomElement);
		
		@SuppressWarnings("unchecked")
		List<Element> elementList = jdomElement.getChildren();
		for (Element element : elementList) {
			String elementName = element.getName();
			if (elementName.equals("Option") && !TransformationUtils.isOptionValue(element)) {
				OldOptionFilter optionFilter = processOptionFilter(element);
				if (optionFilter.isValid()) {
					oldSpecificFilterContent.addOldOptionFilter(optionFilter);
				}
			} else if (elementName.equals("Option") && TransformationUtils.isOptionValue(element)) {
				OldOptionValue optionValue = processOptionValue(element);
				oldSpecificFilterContent.addOldOptionValue(optionValue);
			}else {
				TransformationUtils.throwUnknownElementException(element);
			}
		}
		
		return oldSpecificFilterContent;
	}
	private OldEmptySpecificElementContent processEmptySpecificElementContent(Element jdomElement) throws FunctionalException {
		OldEmptySpecificElementContent oldEmptySpecificFilterContent = new OldEmptySpecificElementContent(jdomElement);
		checkNochildren(jdomElement);
		return oldEmptySpecificFilterContent;
	}
}
