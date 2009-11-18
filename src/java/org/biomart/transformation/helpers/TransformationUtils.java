package org.biomart.transformation.helpers;


import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;


import org.biomart.common.general.constants.MyConstants;
import org.biomart.common.general.exceptions.FunctionalException;
import org.biomart.common.general.exceptions.TechnicalException;
import org.biomart.common.general.exceptions.WarningException;
import org.biomart.common.general.utils.CompareUtils;
import org.biomart.common.general.utils.MyUtils;
import org.biomart.objects.MartConfiguratorUtils;
import org.biomart.objects.helpers.PartitionReference;
import org.biomart.objects.objects.Container;
import org.biomart.objects.objects.PartitionTable;
import org.biomart.old.martService.MartServiceConstants;
import org.biomart.transformation.TransformationMain;
import org.biomart.transformation.oldXmlObjects.OldNode;
import org.jdom.Attribute;
import org.jdom.Document;
import org.jdom.Element;

public class TransformationUtils {
	
	
	public static Integer getIntegerEmptyValue(String propertyName) throws FunctionalException {
		Integer n = TransformationConstants.DEFAULT_INTEGER_VALUES_MAP.get(propertyName);
		/*if (n==null) {
			throw new FunctionalException("No default integer value for empty property: " + propertyName);
		}*/
		return n;
	}
	public static Boolean getBooleanEmptyValue(String propertyName) throws FunctionalException {
		Boolean b = TransformationConstants.DEFAULT_BOOLEAN_VALUES_MAP.get(propertyName);
		/*if (b==null) {
			throw new FunctionalException("No default boolean value for empty property: " + propertyName);
		}*/
		return b;
	}
	public static Integer getIntegerValueFromString(String propertyValue, String propertyName) throws FunctionalException {
		return (propertyValue==null || MyUtils.isEmpty(propertyValue) ? getIntegerEmptyValue(propertyName) : Integer.valueOf(propertyValue));
	}
	public static Boolean getBooleanValueFromString(String propertyValue, String propertyName) throws FunctionalException {
		if (propertyValue==null || MyUtils.isEmpty(propertyValue)) {
			return getBooleanEmptyValue(propertyName);
		} else {
			return Boolean.TRUE.toString().equals(propertyValue) || "1".equals(propertyValue);
		}
	}
	
	
	public static void throwUnhandledElementStructureException(Element element) throws FunctionalException {
		throw new FunctionalException("Unhandled element structure for element: " + MartConfiguratorUtils.displayJdomElement(element));
	}
	public static void throwUnknownElementException(Element element) throws FunctionalException {
		throw new FunctionalException("Unknown element type: " + MartConfiguratorUtils.displayJdomElement(element));
	}
	
	public static void checkJdomElementProperties(Element element, List<String>... propertyLists) throws FunctionalException {
		@SuppressWarnings("unchecked")
		List<Attribute> attributeList = element.getAttributes();
		for (Attribute attribute : attributeList) {
			String propertyName = attribute.getName();
			boolean missing = true;
			for (List<String> propertyList : propertyLists) {
				missing &= !propertyList.contains(propertyName);
			}
			if (missing && TransformationMain.ENABLE_PROPERTIES_CHECK) {
				throw new FunctionalException("Unknown property: " + propertyName + ", in: " + 
						element + "element: " + ",(" + element.getClass() + ")" + MartConfiguratorUtils.displayJdomElement(element));
			}
		}
	}
	
	public static StringBuffer displayNode(int tabLevel, String node) {
		StringBuffer sb = new StringBuffer();
		sb.append(node + MyUtils.LINE_SEPARATOR);
		return sb;
	}

	public static void displayChildren(List<? extends OldNode> oldNodeList, int tabLevel, StringBuffer sb, String listName) {
		if (oldNodeList.size()>0) {
			sb.append(MartConfiguratorUtils.getTabSpace(tabLevel) + listName + ".size() = " + oldNodeList.size() + MyUtils.LINE_SEPARATOR);	
			for (OldNode oldNode : oldNodeList) {
				sb.append(MartConfiguratorUtils.getTabSpace(tabLevel+1) + 
						MyUtils.firstLetterToLowerCase(oldNode.getClass().getSimpleName()) + " = {" + MyUtils.LINE_SEPARATOR);
				sb.append(MartConfiguratorUtils.getTabSpace(tabLevel+2) + oldNode + MyUtils.LINE_SEPARATOR);
				sb.append(MartConfiguratorUtils.getTabSpace(tabLevel+1) + "}" + MyUtils.LINE_SEPARATOR);
			}
		}
	}
	public static boolean isOptionValue(Element element) {
		return element.getAttribute("value")!=null && 
		!(element.getAttribute("tableConstraint")!=null && element.getAttribute("field")!=null && element.getAttribute("key")!=null);
	}
	public static boolean isEmptySpecificElementContent(Element element) {
		return element.getAttributes().size()==1 && element.getAttribute("internalName")!=null;
	}
	
	public static boolean checkValidSpecificityString(String templateValue, String newValue, boolean firstSpecific) {
		if (firstSpecific) {
			return templateValue==null || newValue==null || CompareUtils.same(templateValue, newValue);
		} else {
			return newValue==null || CompareUtils.same(templateValue, newValue);
		}
	}
	public static boolean checkValidSpecificityInteger(Integer templateValue, Integer newValue, boolean firstSpecific) {
		if (firstSpecific) {
			return templateValue==null || newValue==null || CompareUtils.same(templateValue, newValue);
		} else {
			return newValue==null || CompareUtils.same(templateValue, newValue);
		}
	}
	public static boolean checkValidSpecificityBoolean(Boolean templateValue, Boolean newValue, boolean firstSpecific) {
		if (firstSpecific) {
			return templateValue==null || newValue==null || CompareUtils.same(templateValue, newValue);
		} else {
			return newValue==null || CompareUtils.same(templateValue, newValue);
		}
	}
	public static boolean checkValidSpecificitySetString(Set<String> templateList, Set<String> newList, boolean firstSpecific) {
		return checkValidSpecificityListString(new ArrayList<String>(templateList), new ArrayList<String>(newList), firstSpecific);
	}
	public static boolean checkValidSpecificityListString(List<String> templateList, List<String> newList, boolean firstSpecific) {
		if (!checkValidSpecificityInteger(
				templateList.size(), newList.size(), firstSpecific)) {
			return false;
		}
		for (int i = 0; i < templateList.size(); i++) {	// they are the same size for sure by now
			String elementName1 = templateList.get(i);
			String elementName2 = newList.get(i);
			if (!checkValidSpecificityString(elementName1, elementName2, firstSpecific)) {
				return false;
			}
		}
		return true;
	}
	
	public static boolean isMain(String value) {
		return value.endsWith(TransformationConstants.NAMING_CONVENTION_MAIN_TABLE_CONSTRAINT);
	}
	public static boolean isKey(String value) {
		return value.endsWith(TransformationConstants.NAMING_CONVENTION_DIMENSION_TABLE_KEY_SUFFIX);
	}
	public static String generateTableShortNameWhenDimensionPartition(DimensionPartition dimensionPartition, PartitionTable dimensionPartitionTable) {
		String dimensionName = dimensionPartition.getDimensionPartitionNameAndKeyAndValue().getDimensionName();
		PartitionReference dimensionPartitionReference = new PartitionReference(dimensionPartitionTable);
		String tableShortName = dimensionName + TransformationConstants.NAMING_CONVENTION_PARTITION_SEPARATOR + 
		dimensionPartitionReference.toXmlString();
		return tableShortName;
	}
	public static boolean checkForWarning(boolean test, List<String> warningList, String warningMessage) {
		if (test) {
			sendWarning(warningList, warningMessage);
			return false;
		}
		return true;
	}
	public static void sendWarning(List<String> warningList, String warningMessage) {
		try {
			throw new WarningException(warningMessage);
		} catch (WarningException e) {
			System.err.println(e.getMessage());
		}
		warningList.add(warningMessage);
	}
	
	public static boolean isGenomicSequence(String datasetType) {
		return TransformationConstants.GENOMIC_SEQUENCE_DATASET_TYPE.equals(datasetType);
	}
	
	public static boolean isTableSet(String datasetType) {
		return TransformationConstants.TABLE_SET_DATASET_TYPE.equals(datasetType);
	}
	public static String getMartServiceDatasetStringUrl(String server, String virtualSchemaName, String datasetName) {
		return getMartServiceDatasetStringUrl(server, MartServiceConstants.DEFAULT_PATH_TO_MART_SERVICE, virtualSchemaName, datasetName);
	}
	@Deprecated
	public static Document fetchMartServiceXmlDocument(	// use the one in configuration instead
			String server, String pathToMartService, String virtualSchemaName, String datasetName) throws TechnicalException {
		Document xmlDocument = null;
		try {
			xmlDocument = getMartServiceXmlDocument(server, pathToMartService, null, datasetName);
		} catch (TechnicalException e) {
			System.out.println("VirtualSchema required");
			xmlDocument = getMartServiceXmlDocument(server, pathToMartService, virtualSchemaName, datasetName);
		}
		return xmlDocument;
	}
	
	private static Document getMartServiceXmlDocument(
			String server, String pathToMartService, String virtualSchemaName, String datasetName) throws TechnicalException {
		String inputXmlStringUrl = getMartServiceDatasetStringUrl(server, pathToMartService, virtualSchemaName, datasetName);
		Document xmlDocument = MyUtils.loadDocument(inputXmlStringUrl);
		return xmlDocument;
	}
	private static String getMartServiceDatasetStringUrl(String server, String pathToMartService, String virtualSchemaName, String datasetName) {
		return MyConstants.HTTP_PROTOCOL + server + pathToMartService + MartServiceConstants.MART_SERVICE_CONFIGURATION_PARAMETER +
		datasetName + (virtualSchemaName!=null ? MartServiceConstants.MART_SERVICE_VIRTUAL_SCHEMA_PARAMETER + virtualSchemaName : "");
	}
	public static String writeWebServiceXmlConfigurationFile(
			Document xmlDocument, String transformationsGeneralOutput, 
			boolean webService, String databaseVersion, MartServiceIdentifier martServiceIdentifier, String virtualSchema, String datasetName) throws TechnicalException {
		// Write file if it doesn't already exist);
		String xmlFilePathAndName = transformationsGeneralOutput + MyUtils.FILE_SEPARATOR +
		generateIdentifier(webService, databaseVersion, martServiceIdentifier, virtualSchema, datasetName) + ".xml";		
		if (!new File(xmlFilePathAndName).exists()) {
			MyUtils.writeXmlFile(xmlDocument, xmlFilePathAndName);
		}
		return xmlFilePathAndName;
	}
	public static String generateIdentifier(boolean webService, String databaseVersion,
			MartServiceIdentifier martServiceIdentifier, String virtualSchema, String datasetOrTemplateName) {
		return generateTransformationTypeFolderName(webService, databaseVersion) + MyUtils.INFO_SEPARATOR + MyUtils.INFO_SEPARATOR + 
						(martServiceIdentifier!=null ? martServiceIdentifier.generateIdentifier() : "") + MyUtils.INFO_SEPARATOR + MyUtils.INFO_SEPARATOR +  
						virtualSchema + MyUtils.INFO_SEPARATOR + MyUtils.INFO_SEPARATOR + datasetOrTemplateName;
	}
	public static String generateTransformationTypeFolderName(boolean webService, String databaseVersion) {
		return (webService ? "web" : "rdbms" + databaseVersion);
	}
	
	public static Container generateContainerForGeneratedAttributes() {
		return new Container(
				TransformationConstants.GENERATED_ATTRIBUTES_CONTAINER_NAME, TransformationConstants.GENERATED_ATTRIBUTES_CONTAINER_NAME, 
				TransformationConstants.GENERATED_ATTRIBUTES_CONTAINER_NAME, false, null);
	}
	public static String generateUniqueIdentiferForGeneratedAttribute(RelationalInfo relationalInfo) {
		return TransformationConstants.GENERATED_ATTRIBUTE_PREFIX + TransformationConstants.GENERATED_ATTRIBUTE_INFO_SEPARATOR + 
		relationalInfo.getTableName() + TransformationConstants.GENERATED_ATTRIBUTE_INFO_SEPARATOR + relationalInfo.getKeyName() + 
		TransformationConstants.GENERATED_ATTRIBUTE_INFO_SEPARATOR + relationalInfo.getColumnName();
	}
}
