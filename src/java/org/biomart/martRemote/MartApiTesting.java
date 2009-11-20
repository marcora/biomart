package org.biomart.martRemote;

import java.util.List;

import net.sf.json.JSONObject;

import org.biomart.common.general.exceptions.FunctionalException;
import org.biomart.common.general.exceptions.TechnicalException;
import org.biomart.common.general.utils.MyUtils;
import org.biomart.common.general.utils.XmlUtils;
import org.biomart.objects.lite.LiteListAttribute;
import org.biomart.objects.lite.LiteListDataset;
import org.biomart.objects.lite.LiteListFilter;
import org.biomart.objects.lite.LiteMart;
import org.biomart.objects.lite.LiteMartRegistry;
import org.biomart.objects.lite.LiteRootContainer;
import org.biomart.objects.objects.MartRegistry;
import org.biomart.test.DummyPortal;
import org.jdom.Document;

public class MartApiTesting {

	/**
	 * quick test/example method
	 */
	public static void main(String[] args) {
		
		@SuppressWarnings("unused")
		MartRegistry dummyMartRegistry = DummyPortal.createDummyMartRegistry();
		
		/**
		 * To narrow down to just 3 partitions (takes a while if not)
		 */
		String partitionFilterArgument = "main_partition_filter.\"hsapiens_gene_ensembl,mmusculus_gene_ensembl,celegans_gene_ensembl\"";
		
		System.out.println("start.");
		
		try {
			
			// Instantiate API
			System.out.println("Loading registry...");
			MartApi martApi = 
				new MartApi();	// will use the serialized portal
				//new MartApi(dummyMartRegistry);	// will take any MartRegistry object
			System.out.println();
			
			//----------------------------------------------------------------
			// Obtain the registry (= list of light marts)
			System.out.println("Getting registry...");
			LiteMartRegistry liteMartRegistry = martApi.getRegistry(
					"anonymous", "", null);
			System.out.println();

			// Create the JSON object corresonding to the previous request
			displayJson(liteMartRegistry.getJsonObject());
			
			// Create the XML document corresonding to the previous request
			displayXml(liteMartRegistry.getXmlDocument());
	
			System.out.println(liteMartRegistry.getLiteMartList());
						// You can see what other methods you have access to on that object (get the list of light marts for instance here)
			
			List<LiteMart> liteMartList = liteMartRegistry.getLiteMartList();
			LiteMart liteMart = null;
			if (!liteMartList.isEmpty()) {
				liteMart = liteMartList.get(0);
				System.out.println(liteMart.getDisplayName());
						// You can see what methods you have access to on that object (get the display name for instance here)
			}
			
			//----------------------------------------------------------------
			// Obtain the list of datasets (= list of light datasets)
			System.out.println("Getting datasets...");
			LiteListDataset liteListDataset = martApi.getDatasets(
					"anonymous", "", null, "ensembl_mart_55", 55);
			System.out.println();

			// Create the JSON object corresonding to the previous request
			displayJson(liteListDataset.getJsonObject());
			
			// Create the XML document corresonding to the previous request
			displayXml(liteListDataset.getXmlDocument());
			
			
			//----------------------------------------------------------------
			// Obtain the root container (= tree structure of light objects)
			System.out.println("Getting root container...");
			LiteRootContainer liteRootContainer = martApi.getRootContainer(
					"anonymous", "", null, "gene_ensembl", partitionFilterArgument);
			System.out.println();
			
			// Create the JSON object corresonding to the previous request
			displayJson(liteRootContainer.getJsonObject());
			
			// Create the XML document corresonding to the previous request
			displayXml(liteRootContainer.getXmlDocument());


			//----------------------------------------------------------------
			// Obtain the list of attributes (= list of light attributes)
			System.out.println("Getting attributes...");
			LiteListAttribute liteListAttribute = martApi.getAttributes(
					"anonymous", "", null, "gene_ensembl", partitionFilterArgument);
			System.out.println();
			
			// Create the JSON object corresonding to the previous request
			displayJson(liteListAttribute.getJsonObject());
			
			// Create the XML document corresonding to the previous request
			displayXml(liteListAttribute.getXmlDocument());
			
			
			//----------------------------------------------------------------
			// Obtain the list of attributes (= list of light filters)
			System.out.println("Getting filters...");
			LiteListFilter liteListFilter = martApi.getFilters(
					"anonymous", "", null, "gene_ensembl", partitionFilterArgument);
			System.out.println();
			
			// Create the JSON object corresonding to the previous request
			displayJson(liteListFilter.getJsonObject());
			
			// Create the XML document corresonding to the previous request
			displayXml(liteListFilter.getXmlDocument());
			
			
			//----------------------------------------------------------------
			// Querying
			/*System.out.println("Querying...");
			System.out.println(MyUtils.capString(
					martApi.query("anonymous", "", null, 
					MyUtils.wrappedGetProperty(MartRemoteConstants.QUERY_TEST_PROPERTIES_FILE_PATH_AND_NAME, "query1")
					).getJsonObject().toString()));
			System.out.println();*/
			
			/*
			System.out.println(MyUtils.capString(
					martApi.getDatasets("anonymous", "", null, "ensembl_mart_55", 55).getJsonObject().toString()));
			*/
			
		} catch (FunctionalException e) {		// usually an error in arguments, or in the algorithm (message should describe the problem...)
			e.printStackTrace();
		} catch (TechnicalException e) {		// errors we don't control (connection lost, no more disk space...)
			e.printStackTrace();
		}
		
		System.out.println("done.");
	}
	
	private static void displayJson(JSONObject jsonObject) throws TechnicalException, FunctionalException {
		System.out.println("JSON:");
		System.out.println(jsonObject);
		System.out.println();
	}

	private static void displayXml(Document xmlDocument) throws TechnicalException {
		System.out.println("XML:");
		System.out.println(XmlUtils.getXmlDocumentString(xmlDocument));
		System.out.println();
	}
}
