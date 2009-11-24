package org.biomart.transformation.helpers;

import java.util.HashMap;
import java.util.Map;

import org.biomart.objects.MartConfiguratorConstants;


public class TransformationConstants {
	
	public static final int DATASET_CONFIG_TAB_LEVEL = 1;
	public static final int ELEMENT_PAGE_TAB_LEVEL = 2;
	public static final int ELEMENT_GROUP_TAB_LEVEL = 3;
	public static final int ELEMENT_COLLECTION_TAB_LEVEL = 4;
	
	public static final int ELEMENT_LEVEL1_TAB_LEVEL = 5;
	public static final int ELEMENT_LEVEL2_TAB_LEVEL = 6;
	public static final int ELEMENT_LEVEL3_TAB_LEVEL = 7;
	public static final int ELEMENT_LEVEL4_TAB_LEVEL = 8;
	public static final int ELEMENT_LEVEL5_TAB_LEVEL = 9;
	
	public static final int OPTION_VALUE_TAB_LEVEL1 = ELEMENT_LEVEL2_TAB_LEVEL;
	public static final int OPTION_VALUE_TAB_LEVEL2 = ELEMENT_LEVEL3_TAB_LEVEL;
	public static final int OPTION_VALUE_TAB_LEVEL3 = ELEMENT_LEVEL4_TAB_LEVEL;
	public static final int OPTION_VALUE_TAB_LEVEL4 = ELEMENT_LEVEL5_TAB_LEVEL;
	
	public static final int PUSH_ACTION_TAB_LEVEL1 = OPTION_VALUE_TAB_LEVEL2;
	public static final int PUSH_ACTION_TAB_LEVEL2 = OPTION_VALUE_TAB_LEVEL3;
	
	public static final int OPTION_FILTER_TAB_LEVEL1 = ELEMENT_LEVEL2_TAB_LEVEL;
	public static final int OPTION_FILTER_TAB_LEVEL2 = ELEMENT_LEVEL3_TAB_LEVEL;
	
	public static final int SPECIFIC_FILTER_CONTENT_TAB_LEVEL = ELEMENT_LEVEL2_TAB_LEVEL;
	
	public static final int SPECIFIC_OPTION_CONTENT_TAB_LEVEL = ELEMENT_LEVEL3_TAB_LEVEL;
	
	public static final int PAGE_CONTAINER_LEVEL = 0;
	public static final int GROUP_CONTAINER_LEVEL = 1;
	public static final int COLLECTION_CONTAINER_LEVEL = 2;
	
	public static final Map<String, Boolean> DEFAULT_BOOLEAN_VALUES_MAP = new HashMap<String, Boolean>();
	static {
		DEFAULT_BOOLEAN_VALUES_MAP.put("hidden", false);
		DEFAULT_BOOLEAN_VALUES_MAP.put("hideDisplay", false);
		DEFAULT_BOOLEAN_VALUES_MAP.put("default", false);
		DEFAULT_BOOLEAN_VALUES_MAP.put("checkForNulls", false);
		DEFAULT_BOOLEAN_VALUES_MAP.put("multipleValues", null);	// see part-specific transformation
		DEFAULT_BOOLEAN_VALUES_MAP.put("pointer", false);
		DEFAULT_BOOLEAN_VALUES_MAP.put("isSelectable", true);
	}
	
	public static final Map<String, Integer> DEFAULT_INTEGER_VALUES_MAP = new HashMap<String, Integer>();
	static {
		DEFAULT_INTEGER_VALUES_MAP.put("maxLength", null);	// see part-specific transformation
		DEFAULT_INTEGER_VALUES_MAP.put("maxSelect", 0);
		DEFAULT_INTEGER_VALUES_MAP.put("datasetID", 0);
	}
	
	/*public static final String DEFAULT_CONFIG_NAME = "default";*/
	
	public static final String NAMING_CONVENTION_PARTITION_SEPARATOR = "_";
	public static final String NAMING_CONVENTION_ELEMENT_SEPARATOR_REGEX = "\\" + "_" + "\\" + "_";
	public static final String NAMING_CONVENTION_ELEMENT_SEPARATOR = "__";
	public static final String NAMING_CONVENTION_DIMENSION_TABLE_KEY_SUFFIX = "_key";
	public static final String NAMING_CONVENTION_MAIN_TABLE_CONSTRAINT = "main";
	public static final String NAMING_CONVENTION_META_TABLES_PREFIX = "meta_";
	
	public static final String SPECIFIC_ATTRIBUTE_CONTENT_TYPE = "SpecificAttributeContent";
	public static final String SPECIFIC_FILTER_CONTENT_TYPE = "SpecificFilterContent";
	public static final String EMPTY_SPECIFIC_FILTER_CONTENT_TYPE = "SpecificFilterContent";
	public static final String SPECIFIC_OPTION_CONTENT_TYPE = "SpecificOptionContent";
	public static final String OPTION_FILTER_TYPE = "Option";
	public static final String OPTION_VALUE_TYPE = "Option";
	public static final String PUSH_ACTION_TYPE = "PushAction";

	public static final String DYNAMIC_DATASET_PARTITION_TABLE_NAME = MartConfiguratorConstants.MAIN_PARTITION_TABLE_DEFAULT_NAME;
	public static final int DYNAMIC_DATASET_PARTITION_TABLE_DATASET_NAME_VARIABLE_PART_COLUMN_NUMBER = 1;
	public static final String ALIAS_DELIMITER = "*";
	
	public static final String BOOLEAN_FILTER_EXCLUDED_VALUE = "excluded";
	public static final String BOOLEAN_FILTER_EXCLUDED_DISPLAY_NAME = "Excluded";
	public static final String BOOLEAN_FILTER_ONLY_VALUE = "only";
	public static final String BOOLEAN_FILTER_ONLY_DISPLAY_NAME = "Only";
	
	public static final String GENOMIC_SEQUENCE_DATASET_TYPE = "GenomicSequence";
	public static final String TABLE_SET_DATASET_TYPE = "TableSet";
	
	public static final String DYNAMIC_DATASET_INTERNAL_NAME_ELEMENT_SEPARATOR = "_";
 
	public static final String PORTABLE_TYPE_FORMATTER = "formatter";
	public static final String PORTABLE_TYPE_DAS_CHR = "dasChr";
	public static final String PORTABLE_TYPE_DAS_GENE = "dasGene";

	public static final String PROPERTY_ERROR_FILE_NAME = "errors";
	public static final String PROPERTY_SERVER = "server";
	public static final String PROPERTY_PATH_TO_MART_SERVICE = "martservice";
	
	public static final int DIMENSION_TABLE_INTERNAL_NAME_COLUMN_NUMBER = 1;
	public static final int DIMENSION_TABLE_DISPLAY_NAME_COLUMN_NUMBER = 2;
	
	public static final int WEBSERVICE_DEFAULT_PARTITION_TABLE_ROW = 0;

	/**
	 * Cross elements:
		"gene_ensembl": "upstream_flank", "downstream_flank" 
		"gene": "dna.filter.upstream_flank", "dna.filter.downstream_flank" 
		"ptroglodytes_snp": "reference_strain"
			ex: <AttributeDescription internalName="reference_strain", pointerDataset="ptroglodytes_snp", pointerFilter="reference_strain", pointerInterface="default"> 
	 */
	public static final String CROSS_ELEMENT_POINTER_FILTER = "pointerFilter";
	public static final String CROSS_ELEMENT_POINTER_ATTRIBUTE = "pointerAttribute";
	
	public static final String TRANSFORMATIONS_GENERAL_SERIAL_FOLDER_NAME = "GeneralSerial";
	public static final String SERIAL_FOLDER_NAME = "Serial";
	
	public static final String WEB_SERVICE_CONFIGURATION_SERIAL_FILE_NAME = "WebServiceConfiguration";
	
	public static final String DEFAULT_USER = "anonymous";
	
	public static final String PARTITION_FILTERS_CONTAINER_NAME = "partition_filters";
	public static final String PARTITION_FILTERS_CONTAINER_DISPLAY_NAME = "Partition Filters";
	public static final String MAIN_PARTITION_FILTER_NAME = "main_partition_filter";
	public static final String MAIN_PARTITION_FILTER_DISPLAY_NAME = "Main partition filter";
	
	// For attributes generated because a filter exists on the given relational info,
	// but either there are no counterpart attributes or there are more than one (with split range)
	public static final String GENERATED_ATTRIBUTES_CONTAINER_NAME = "TRANSFORMATION_GENERATED_ATTRIBUTES";
	public static final String GENERATED_ATTRIBUTE_PREFIX = "G";
	public static final String GENERATED_ATTRIBUTE_INFO_SEPARATOR = "___";
	
}

