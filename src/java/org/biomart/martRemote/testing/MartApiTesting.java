package org.biomart.martRemote.testing;

import java.util.List;

import net.sf.json.JSONObject;

import org.biomart.common.general.exceptions.FunctionalException;
import org.biomart.common.general.exceptions.TechnicalException;
import org.biomart.common.general.utils.MyUtils;
import org.biomart.common.general.utils.XmlUtils;
import org.biomart.martRemote.MartApi;
import org.biomart.martRemote.MartRemoteConstants;
import org.biomart.objects.lite.LiteListAttribute;
import org.biomart.objects.lite.LiteListDataset;
import org.biomart.objects.lite.LiteListFilter;
import org.biomart.objects.lite.LiteMart;
import org.biomart.objects.lite.LiteMartRegistry;
import org.biomart.objects.lite.LiteRootContainer;
import org.biomart.objects.lite.QueryResult;
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
		
		outln("start.");
		
		try {
			
			// Instantiate API
			outln("Loading registry...");
			MartApi martApi = 
				new MartApi();	// will use the serialized portal
				//new MartApi(dummyMartRegistry);	// will take any MartRegistry object
			outln();
			
			//----------------------------------------------------------------
			// Obtain the registry (= list of light marts)
			outln("Getting registry...");
			LiteMartRegistry liteMartRegistry = martApi.getRegistry("anonymous", "", null);
			outln();

			// Create the JSON object corresonding to the previous request
			//displayJson(liteMartRegistry.getJsonObject());	NOW DEPRECATED
			
			// Create the XML document corresonding to the previous request
			displayXml(liteMartRegistry.getXmlDocument());
	
			// Manipulation of the objects
			
			outln(liteMartRegistry.getLiteMartList());
						// You can see what other methods you have access to on that object (get the list of light marts for instance here)
			
			List<LiteMart> liteMartList = liteMartRegistry.getLiteMartList();
			LiteMart liteMart = null;
			if (!liteMartList.isEmpty()) {
				liteMart = liteMartList.get(0);
				outln(liteMart.getDisplayName());
						// You can see what methods you have access to on that object (get the display name for instance here)
			}
			
			//----------------------------------------------------------------
			// Obtain the list of datasets (= list of light datasets)
			outln("Getting datasets...");
			LiteListDataset liteListDataset = martApi.getDatasets(
					"anonymous", "", null, "ensembl_mart_55", 55);
			outln();

			// Create the JSON object corresonding to the previous request
			//displayJson(liteListDataset.getJsonObject());	NOW DEPRECATED
			
			// Create the XML document corresonding to the previous request
			displayXml(liteListDataset.getXmlDocument());
			
			
			//----------------------------------------------------------------
			// Obtain the root container (= tree structure of light objects)
			outln("Getting root container...");
			LiteRootContainer liteRootContainer = martApi.getRootContainer(
					"anonymous", "", null, "gene_ensembl", partitionFilterArgument);
			outln();
			
			// Create the JSON object corresonding to the previous request
			//displayJson(liteRootContainer.getJsonObject());	NOW DEPRECATED
			
			// Create the XML document corresonding to the previous request
			displayXml(liteRootContainer.getXmlDocument());


			//----------------------------------------------------------------
			// Obtain the list of attributes (= list of light attributes)
			outln("Getting attributes...");
			LiteListAttribute liteListAttribute = martApi.getAttributes(
					"anonymous", "", null, "gene_ensembl", partitionFilterArgument);
			outln();
			
			// Create the JSON object corresonding to the previous request
			//displayJson(liteListAttribute.getJsonObject());	NOW DEPRECATED
			
			// Create the XML document corresonding to the previous request
			displayXml(liteListAttribute.getXmlDocument());
			
			
			//----------------------------------------------------------------
			// Obtain the list of attributes (= list of light filters)
			outln("Getting filters...");
			LiteListFilter liteListFilter = martApi.getFilters(
					"anonymous", "", null, "gene_ensembl", partitionFilterArgument);
			outln();
			
			// Create the JSON object corresonding to the previous request
			//displayJson(liteListFilter.getJsonObject());	NOW DEPRECATED
			
			// Create the XML document corresonding to the previous request
			displayXml(liteListFilter.getXmlDocument());
			
			//outln(MyUtils.capString(martApi.getDatasets("anonymous", "", null, "ensembl_mart_55", 55).getJsonObject().toString()));
			
			//----------------------------------------------------------------
			// Querying
			outln("Querying...");
			QueryResult queryResult = martApi.query("anonymous", "", null, 
					MyUtils.wrappedGetProperty(MartRemoteConstants.QUERY_TEST_PROPERTIES_FILE_PATH_AND_NAME, "query1"));
			outln();
			
			// Create the JSON object corresonding to the previous request
			//displayJson(queryResult.getJsonObject());	NOW DEPRECATED
			
			// Create the XML document corresonding to the previous request
			displayXml(queryResult.getXmlDocument());

			
		} catch (FunctionalException e) {		// usually an error in arguments, or in the algorithm (message should describe the problem...)
			e.printStackTrace();
		} catch (TechnicalException e) {		// errors we don't control (connection lost, no more disk space...)
			e.printStackTrace();
		}
		
		MyUtils.writeFile("./MartApiTesting.out", stringBuffer.toString());
		outln("done.");
	}
	
	@SuppressWarnings("unused")
	private static void displayJson(JSONObject jsonObject) throws TechnicalException, FunctionalException {
		outln("JSON:");
		outln(jsonObject);
		outln();
	}

	private static void displayXml(Document xmlDocument) throws TechnicalException {
		outln("XML:");
		outln(XmlUtils.getXmlDocumentString(xmlDocument));
		outln();
	}
	
	private static StringBuffer stringBuffer = new StringBuffer();
	private static void outln(Object object) {
		outln(object!=null ? object.toString() : null);
	}
	private static void outln() {
		outln("");
	}
	private static void outln(String message) {
		out(message + "\n");
	}
	private static void out(String message) {
		System.out.print(message);
		stringBuffer.append(message);
	}
}
