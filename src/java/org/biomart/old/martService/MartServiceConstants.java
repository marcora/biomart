package org.biomart.old.martService;

import org.biomart.common.general.constants.MyConstants;

public class MartServiceConstants {
	
	public static final String INVALID_FILE_ERROR_MESSAGE = 
		"INVALID FILE";
		//"INVALID LINK SIDE DATA FILE";
	//public static final String INVALID_LINK_SIDE_DATA_FILE_ERROR_MESSAGE = 
	/*public static final String INVALID_LINK_INDEX_FILE_ERROR_MESSAGE = "INVALID LINK INDEX FILE";*/
	public static final String ERROR_MESSAGE = "Query ERROR: caught BioMart::Exception::";
		/*
			Query ERROR: caught BioMart::Exception::Database: Error during query execution: Table 'gene_test_41_v02_p80.cfamiliaris_gene_ensembl__xref_uniprot_swissprot__dm' doesn't exist
			Query ERROR: caught BioMart::Exception::Database: Could not connect to mysql database gene_test_41_v02_p80: DBI connect('database=gene_test_41_v02_p80;host=localhost;port=3306','biomart',...) failed: Too many connections at /home/biomart/server/biomart-perl/lib/BioMart/Configuration/DBLocation.pm line 98
			Query ERROR: caught BioMart::Exception::Usage: Attribute referencedatabase_ensembl_homo_sapiens_gene NOT FOUND
			Query ERROR: caught BioMart::Exception::Database: Error during query execution: You have an error in your SQL syntax; check the manual that corresponds to your MySQL server version for the right syntax to use near 'AND main.interaction_num_key=interaction__gene__dm.interaction_num_key LIMIT 21,' at line 1
		*/
	public static final String NO_LINK_ERROR_MESSAGE = "Query ERROR: caught BioMart::Exception: non-BioMart die(): Can't call method \"defaultLink\" on an undefined value";
		/*Query ERROR: caught BioMart::Exception: non-BioMart die(): Can't call method "defaultLink" on an undefined value at /home/acros/biomart-perl3/lib/BioMart/Query.pm line 1716.*/

	//Query ERROR: caught BioMart::Exception::Database: Could not connect to mysql database ensembl_mart_54: DBI connect('database=ensembl_mart_54;host=martdb.ensembl.org;port=5316','anonymous',...) failed: Lost connection to MySQL server at 'reading authorization packet', system error: 0 at /home/acros/biomart-perl3/lib/BioMart/Configuration/DBLocation.pm line 98

	public static final String EMPTY_IN_LIST_STRING = "%$01234_EMPTY_IN_LIST_56789$%";	// because if empty, then disregard filter instead of returning no results. we hope this string will never be an actual data
																				// no #: makes it crash when manually copy/paste URL
	public static final String DEFAULT_PATH_TO_MART_SERVICE = "/biomart/martservice";
	public static final String URL_QUERY_PARAMETER = "query";
	public static final String DATASET_CONFIGURATION_DOC_TYPE = "DatasetConfig";
	
	public static final String CENTRAL_PORTAL_MART_SERVICE_STRING_URL = MyConstants.HTTP_PROTOCOL + MyConstants.CENTRAL_PORTAL_SERVER + DEFAULT_PATH_TO_MART_SERVICE;
	public static final String BMTEST_MART_SERVICE_STRING_URL = MyConstants.HTTP_PROTOCOL + MyConstants.BMTEST_SERVER + DEFAULT_PATH_TO_MART_SERVICE;
	
	public static final String MART_SERVICE_REGISTRY_PARAMETER = "?type=registry";
	public static final String MART_SERVICE_DATASET_LIST_PARAMETER = "?type=datasets&mart=";
	public static final String MART_SERVICE_CONFIGURATION_PARAMETER = "?type=configuration&dataset=";
	public static final String MART_SERVICE_VIRTUAL_SCHEMA_PARAMETER = "&virtualSchema=";
	
	public static final String DEFAULT_FORMATTER = "TSV";
	public static final String DEFAULT_BIOMART_VERSION = "0.7";
		
	public static final String MART_TYPE_DB_LOCATION = "MartDBLocation";
	public static final String MART_TYPE_URL_LOCATION = "MartURLLocation";	
	
	public static final String ATTRIBUTE_TABLE_SET = "TableSet";
	public static final String ATTRIBUTE_GENOMIC_SEQUENCE = "GenomicSequence";
	public static final String ATTRIBUTE_TIMESTAMP = "MODIFIED_UNAVAILABLE";
	
	public static final String XML_IMPORTABLE = "Importable";
	public static final String XML_EXPORTABLE = "Exportable";
	public static final String XML_FILTER_PAGE = "FilterPage";
	public static final String XML_ATTRIBUTE_PAGE = "AttributePage";
	public static final String XML_MAIN_TABLE = "MainTable";
	public static final String XML_KEY = "Key";
	public static final String ATTRIBUTE_IN_EXPORTABLE_TO_IGNORE = "__";

	public static final String XML_ELEMENT_ATTRIBUTE_DESCRIPTION_INTERNAL_NAME = "internalName";
    
	public static final String XML_ATTRIBUTE_VIRTUAL_SCHEMA_NAME = "name";

	public static final String XML_ATTRIBUTE_SOFTWARE_VERSION = "softwareVersion";
	
	public static final String XML_ATTRIBUTE_VIRTUAL_SCHEMA = "serverVirtualSchema";	
	public static final String XML_ATTRIBUTE_MART_NAME = "name";
	public static final String XML_ATTRIBUTE_DISPLAY_NAME = "displayName";
	public static final String XML_ATTRIBUTE_HOST = "host";
	public static final String XML_ATTRIBUTE_PORT = "port";
	public static final String XML_ATTRIBUTE_PATH = "path";
	public static final String XML_ATTRIBUTE_VISIBLE = "visible";
	public static final String XML_ATTRIBUTE_DATABASE_TYPE = "databaseType";
	public static final String XML_ATTRIBUTE_USER = "user";
	public static final String XML_ATTRIBUTE_PASSWORD = "password";
	public static final String XML_ATTRIBUTE_DATABASE_NAME = "database";
	public static final String XML_ATTRIBUTE_INCLUDE_DATASETS = "includeDatasets";
	public static final String INCLUDE_DATASETS_SEPARATOR = ",";
	public static final String XML_ATTRIBUTE_DATABASE_VALUE_MYSQL = "mysql";
	
	public static final String XML_ATTRIBUTE_TYPE = "type";
	public static final String XML_ATTRIBUTE_FILTERS = "filters";	
	public static final String XML_ATTRIBUTE_ATTRIBUTES = "attributes";
	
	public static final String XML_ATTRIBUTE_LINK_NAME = "linkName";
	public static final String XML_ATTRIBUTE_LINK_VERSION = "linkVersion";
	public static final String XML_ATTRIBUTE_DEFAULT = "default";	
	
	public static final String XML_ATTRIBUTE_VALUE_LINK = "link";
	
	public static final int TAB_PAGE_MINIMUM_ATTRIBUTES = 2;
	public static final int TAB_PAGE_MAX_ATTRIBUTES = 9;
	public static final int TAB_PAGE_DATASET_TYPE_ATTRIBUTE_INDEX = 0;
	public static final int TAB_PAGE_VISIBILITY_ATTRIBUTE_INDEX = 3;
	public static final int TAB_PAGE_DATASET_NAME_ATTRIBUTE_INDEX = 1;
	public static final int TAB_PAGE_TIMESTAMP_ATTRIBUTE_INDEX = 8;
	
	// Meta tables
	public static final String DATASET_TABLE = "meta_conf__dataset__main";
	public static final String DATASET_MODIFIED_FIELD = "modified";
	public static final String DATASET_NAME_FIELD = "dataset";
	public static final String DATASET_TYPE_FIELD = "type";
	
	public static final String MAIN_TABLE_CONSTRAINT = "main";
	
	public static final String ELEMENT_SEPARATOR = ",";
}
