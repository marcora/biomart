package org.biomart.martRemote;

import net.sf.json.JSONObject;

import org.biomart.common.general.utils.MyUtils;
import org.biomart.common.general.utils.XmlUtils;
import org.biomart.martRemote.objects.response.GetRegistryResponse;
import org.biomart.objects.objects.MartRegistry;
import org.biomart.test.DummyPortal;
import org.jdom.Document;

public class MartApiTesting {

	/**
	 * quick test/example method
	 */
	public static void main(String[] args) throws Exception {
		
		@SuppressWarnings("unused")
		MartRegistry dummyMartRegistry = DummyPortal.createDummyMartRegistry();
		
		// Instantiate API
		System.out.println("Loading registry...");
		MartApi martApi = 
			new MartApi();	// will use the serialized portal
			//new MartApi(dummyMartRegistry);	// will take any MartRegistry object
		System.out.println();
		
		// For instance obtain the registry (= list of mats)
		System.out.println("Getting registry...");
		GetRegistryResponse getRegistryResponse = martApi.getRegistry("anonymous", "", "xml");
		System.out.println();
		
		// Create the JSON object corresonding to the previous request
		JSONObject jsonObject = getRegistryResponse.getJsonObject();
		System.out.println("JSON:");
		System.out.println(jsonObject);
		System.out.println();
		
		// Create the XML document corresonding to the previous request
		Document xmlDocument = getRegistryResponse.getXmlDocument();
		System.out.println("XML:");
		System.out.println(XmlUtils.getXmlDocumentString(xmlDocument));
		System.out.println();
		
		// Other calls
		System.out.println("Getting datasets...");
		System.out.println(MyUtils.capString(
				martApi.getDatasets("anonymous", "", null, "ensembl_mart_55", 55).getJsonObject().toString()));
		System.out.println();
		
		/**
		 * To narrow down to just 3 partitions (takes a while if not)
		 */
		String partitionFilterArgument = "main_partition_filter.\"hsapiens_gene_ensembl,mmusculus_gene_ensembl,celegans_gene_ensembl\"";
		
		System.out.println("Getting root container...");
		System.out.println(MyUtils.capString(
				martApi.getRootContainer("anonymous", "", null, "gene_ensembl", partitionFilterArgument).getJsonObject().toString()));
		System.out.println();
		
		System.out.println("Getting attributes...");
		System.out.println(MyUtils.capString(
				martApi.getAttributes("anonymous", "", null, "gene_ensembl", partitionFilterArgument).getJsonObject().toString()));
		System.out.println();
		
		System.out.println("Getting filters...");
		System.out.println(MyUtils.capString(
				martApi.getFilters("anonymous", "", null, "gene_ensembl", partitionFilterArgument).getJsonObject().toString()));
		System.out.println();
		
		System.out.println("Querying...");
		System.out.println(MyUtils.capString(
				martApi.query("anonymous", "", null, 
				MyUtils.wrappedGetProperty(MartRemoteConstants.QUERY_TEST_PROPERTIES_FILE_PATH_AND_NAME, "query1")
				).getJsonObject().toString()));
		System.out.println();
		
		System.out.println("done.");
	}
}
