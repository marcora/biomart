package org.biomart.martRemote;


import java.io.IOException;
import java.io.PrintWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import net.sf.json.JSONObject;

import org.biomart.common.general.exceptions.FunctionalException;
import org.biomart.common.general.exceptions.TechnicalException;
import org.biomart.common.general.utils.MyUtils;
import org.biomart.martRemote.enums.MartRemoteEnum;
import org.biomart.martRemote.enums.MartServiceFormat;
import org.biomart.martRemote.objects.request.GetDatasetsRequest;
import org.biomart.martRemote.objects.request.GetLinksRequest;
import org.biomart.martRemote.objects.request.GetRegistryRequest;
import org.biomart.martRemote.objects.request.GetRootContainerRequest;
import org.biomart.martRemote.objects.request.MartServiceRequest;
import org.biomart.martRemote.objects.request.QueryRequest;
import org.biomart.martRemote.objects.response.GetDatasetsResponse;
import org.biomart.martRemote.objects.response.GetLinksResponse;
import org.biomart.martRemote.objects.response.GetRegistryResponse;
import org.biomart.martRemote.objects.response.GetRootContainerResponse;
import org.biomart.martRemote.objects.response.MartServiceResponse;
import org.biomart.martRemote.objects.response.QueryResponse;
import org.biomart.objects.objects.MartRegistry;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.Namespace;
import org.jdom.input.SAXBuilder;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;


public class MartApi {

	@SuppressWarnings("all")
	public static void main(String[] args) throws Exception {
		
		PrintWriter printWriter = new PrintWriter(System.out);
		MartApi martServiceApi = new MartApi();
		System.out.println("Registry loaded");
		
		MartServiceRequest martServiceRequest = null;
		MartServiceResponse martServiceResult = null;
		
		String type = 
			//"getRegistry";
			"query";
		String username = "anonymous";
		String password = "";
		String martName = 
			"ensembl";
			//"uniprot_mart";
		String martVersionString = "-1";
		String datasetName = 
			"hsapiens_gene_ensembl";
			//"UNIPROT";
		String query = "query1";
		String filterPartitionString = "species.\"hsapiens_gene_ensembl,mmusculus_gene_ensembl\"";
		MartServiceFormat format = MartServiceFormat.JSON;
		
		MartRemoteEnum remoteAccessEnum = MartRemoteEnum.getEnumFromIdentifier(type);
		boolean valid = true;
		if (MartRemoteEnum.GET_REGISTRY.equals(remoteAccessEnum)) {
			martServiceRequest = martServiceApi.prepareGetRegistry(username, password, format);			
		} else if (MartRemoteEnum.GET_DATASETS.equals(remoteAccessEnum)) {
			martServiceRequest = martServiceApi.prepareGetDatasets(username, password, format, martName, martVersionString);			
		} else if (MartRemoteEnum.GET_ROOT_CONTAINER.equals(remoteAccessEnum)) {
			martServiceRequest = martServiceApi.prepareGetRootContainer(username, password, format, datasetName, filterPartitionString);
		}/*else if (MartRemoteEnum.GET_ATTRIBUTES.equals(remoteAccessEnum)) {
			//martServiceRequest = martServiceApi.prepareGetAttributes(username, password, format, datasetName, filterPartition);		
		} else if (MartRemoteEnum.GET_FILTERS.equals(remoteAccessEnum)) {
			//martServiceRequest = martServiceApi.prepareGetFilters(username, password, format, datasetName, filterPartition);		
		} */else if (MartRemoteEnum.GET_LINKS.equals(remoteAccessEnum)) {
			martServiceRequest = martServiceApi.prepareGetLinks(username, password, format, datasetName);			
		} else if (MartRemoteEnum.QUERY.equals(remoteAccessEnum)) {
			martServiceRequest = martServiceApi.prepareQuery(username, password, format, MartRemoteUtils.getProperty(query));			
		}
		
		if (!martServiceRequest.isValid()) {
			martServiceApi.writeError(martServiceRequest.getErrorMessage(), printWriter);
			valid = false;
		} else {
			if (MartRemoteEnum.GET_REGISTRY.equals(remoteAccessEnum)) {			
				martServiceResult = martServiceApi.executeGetRegistry(martServiceRequest);
			} else if (MartRemoteEnum.GET_DATASETS.equals(remoteAccessEnum)) {
				martServiceResult = martServiceApi.executeGetDatasets(martServiceRequest);			
			} else if (MartRemoteEnum.GET_ROOT_CONTAINER.equals(remoteAccessEnum)) {
				martServiceResult = martServiceApi.executeGetRootContainer(martServiceRequest);
			}/*else if (MartRemoteEnum.GET_ATTRIBUTES.equals(remoteAccessEnum)) {
				//martServiceResult = martServiceApi.executeGetAttributes(martServiceRequest);		
			} else if (MartRemoteEnum.GET_FILTERS.equals(remoteAccessEnum)) {
				//martServiceResult = martServiceApi.executeGetFilters(martServiceRequest);		
			}*/ else if (MartRemoteEnum.GET_LINKS.equals(remoteAccessEnum)) {
				martServiceResult = martServiceApi.executeGetLinks(martServiceRequest);			
			} else if (MartRemoteEnum.QUERY.equals(remoteAccessEnum)) {
				martServiceResult = martServiceApi.executeQuery(martServiceRequest);			
			}
		}
		
		if (valid) {
			martServiceApi.processMartServiceResult(martServiceResult, printWriter);
		}
		printWriter.flush();
	}
	
	private Boolean debug = null;
	
	private String xsdFile = null;
	
	private MartRegistry martRegistry = null;
	private SAXBuilder builder = null;
	private Namespace martServiceNamespace = null;
	private Namespace xsiNamespace = null;
	
	public MartApi() throws IOException, JDOMException, TechnicalException {
		this(false, MartRemoteConstants.XSD_FILE_FILE_PATH_AND_NAME, MartRemoteConstants.PORTAL_SERIAL_FILE_PATH_AND_NAME);
	}
	public MartApi(boolean webService, String xsdFile, String portalSerialFile) throws TechnicalException {
		this.debug = !webService;
		this.xsdFile = xsdFile;
			
		builder = new SAXBuilder();
        Document xsd = null;
        try {
			xsd = builder.build(new URL(xsdFile));
		} catch (MalformedURLException e) {
			throw new TechnicalException(e);
		} catch (JDOMException e) {
			throw new TechnicalException(e);
		} catch (IOException e) {
			throw new TechnicalException(e);
		}
        Element xsdRootElement = xsd.getRootElement();
        Namespace xsdMartServiceNamespace = xsdRootElement.getNamespace("tns");
        martServiceNamespace = Namespace.getNamespace("ms", xsdMartServiceNamespace.getURI());	//Namespace.getNamespace("ms", "http://www.mynamespace.com/mynamespace");
        xsiNamespace = Namespace.getNamespace("xsi", "http://www.w3.org/2001/XMLSchema-instance");
		URL portalSerialUrl = null;
		try {
			portalSerialUrl = new URL(portalSerialFile);
		} catch (MalformedURLException e) {
			throw new TechnicalException(e);
		}
		martRegistry = (MartRegistry)MyUtils.readSerializedObject(portalSerialUrl);
	}
	
	// Request preparation
	public GetRegistryRequest prepareGetRegistry(String username, String password, MartServiceFormat format) {
		GetRegistryRequest getRegistryRequest = new GetRegistryRequest(username, password, format);
		return getRegistryRequest;
	}
	public GetDatasetsRequest prepareGetDatasets(String username, String password, MartServiceFormat format, 
			String martName, String martVersionString) {
		Integer martVersion = Integer.valueOf(martVersionString);
		GetDatasetsRequest getDatasetsRequest = new GetDatasetsRequest(username, password, format, martName, martVersion);
		return getDatasetsRequest;
	}
	public GetRootContainerRequest prepareGetRootContainer(String username, String password, MartServiceFormat format, 
			String datasetName, String partitionFilterString) {
		String partitionFilter = null;
		List<String> partitionFilterValues = null;
		if (null!=partitionFilterString) {
			String[] split = partitionFilterString.split("[.]");
			partitionFilter = split[0];
			partitionFilterValues = new ArrayList<String>(Arrays.asList(split[1].replace("\"", "").split(",")));
		}
		GetRootContainerRequest getRootContainerRequest = new GetRootContainerRequest(username, password, format, 
				datasetName, partitionFilter, partitionFilterValues);
		return getRootContainerRequest;
	}
	public GetLinksRequest prepareGetLinks(String username, String password, MartServiceFormat format, String datasetName) {
		GetLinksRequest getLinksRequest = new GetLinksRequest(username, password, format, datasetName);
		return getLinksRequest;
	}
	
	// Request exectution
	public GetRegistryResponse executeGetRegistry(MartServiceRequest martServiceRequest) {
		GetRegistryResponse getRegistryResponse = new GetRegistryResponse(
				MartRemoteEnum.GET_REGISTRY.getResponseName(), martServiceNamespace, xsiNamespace, xsdFile, 
				martRegistry, martServiceRequest);
		getRegistryResponse.populateObjects();
		return getRegistryResponse;
	}
	public GetDatasetsResponse executeGetDatasets(MartServiceRequest martServiceRequest) {
		GetDatasetsResponse getDatasetsResponse = new GetDatasetsResponse(
				MartRemoteEnum.GET_DATASETS.getResponseName(), martServiceNamespace, xsiNamespace, xsdFile, martRegistry, martServiceRequest);
		getDatasetsResponse.populateObjects();
		return getDatasetsResponse;
	}
	public GetRootContainerResponse executeGetRootContainer(MartServiceRequest martServiceRequest) {
		GetRootContainerResponse getRootContainerResponse = new GetRootContainerResponse(
				MartRemoteEnum.GET_ROOT_CONTAINER.getResponseName(), martServiceNamespace, xsiNamespace, xsdFile, martRegistry, martServiceRequest);
		getRootContainerResponse.populateObjects();
		return getRootContainerResponse;
	}
	/*public GetAttributesResponse getAttributes(String username, String password, String datasetName, String partitionFilter) {
		Dataset dataset = fetchDatasetByName(username, datasetName);
		List<Container> containerList = fetchContainerList(dataset);
		
		List<martConfigurator.objects.Attribute> attributeList = new ArrayList<martConfigurator.objects.Attribute>();
		recursivelyPopulateAttributes(containerList, attributeList);
		
		GetAttributesResponse getAttributesResponse = new GetAttributesResponse(MartRemoteEnum.GET_ATTRIBUTES.getResponseName(), martServiceNamespace, xsiNamespace, xsdFile, attributeList);
		return getAttributesResponse;
	}
	public GetFiltersResponse getFilters(String username, String password, String datasetName, String partitionFilter) {
		Dataset dataset = fetchDatasetByName(username, datasetName);
		List<Container> containerList = fetchContainerList(dataset);
		
		List<martConfigurator.objects.Filter> filterList = new ArrayList<martConfigurator.objects.Filter>();
		recursivelyFetchFilters(containerList, filterList);
		
		GetFiltersResponse getFiltersResponse = new GetFiltersResponse(MartRemoteEnum.GET_FILTERS.getResponseName(), martServiceNamespace, xsiNamespace, xsdFile, filterList);
		return getFiltersResponse;
	}
	private void recursivelyPopulateAttributes(List<Container> containerList,
			List<martConfigurator.objects.Attribute> attributeList) {
		for (Container container : containerList) {
			if (container.getVisible()) {
				ArrayList<martConfigurator.objects.Attribute> attributeListTmp = 
					new ArrayList<martConfigurator.objects.Attribute>(container.getAttributeList());
				for (martConfigurator.objects.Attribute attribute : attributeListTmp) {
					if (!attribute.getTargetRange().noVisiblePartInRange()) {
						attributeList.add(attribute);
					}
				}
				List<Container> subContainerList = container.getContainerList();
				recursivelyPopulateAttributes(subContainerList, attributeList);
			}
		}
	}
	private void recursivelyFetchFilters(List<Container> containerList,
			List<martConfigurator.objects.Filter> filterList) {
		for (Container container : containerList) {
			if (container.getVisible()) {
				ArrayList<martConfigurator.objects.Filter> filterListTmp = new ArrayList<martConfigurator.objects.Filter>(container.getFilterList());
				for (martConfigurator.objects.Filter filter : filterListTmp) {
					if (!filter.getTargetRange().noVisiblePartInRange()) {
						filterList.add(filter);
					}
				}
				
				List<Container> subContainerList = container.getContainerList();
				recursivelyFetchFilters(subContainerList, filterList);
			}
		}
	}*/
	public GetLinksResponse executeGetLinks(MartServiceRequest martServiceRequest) {
		GetLinksResponse getLinksResponse = new GetLinksResponse(
				MartRemoteEnum.GET_LINKS.getResponseName(), martServiceNamespace, xsiNamespace, xsdFile, martRegistry, martServiceRequest);
		getLinksResponse.populateObjects();
		return getLinksResponse;
	}
	
	public QueryRequest prepareQuery(String username, String password, MartServiceFormat format, String queryString) 
	throws JDOMException, IOException, TechnicalException, FunctionalException {
		QueryRequest queryRequest = new QueryRequest(MartRemoteEnum.QUERY.getRequestName(), martServiceNamespace, xsiNamespace, xsdFile, 
				username, password, format, queryString) ;
		queryRequest.rebuildQueryDocument();	// adding proper namespaces and wrapper
							// update errorMessage if not validation fails
		queryRequest.buildObjects();
		if (this.debug) System.out.println(MartRemoteUtils.getXmlDocumentString(queryRequest.getQueryDocument()));
		
		return queryRequest;
	}	
	public QueryResponse executeQuery(MartServiceRequest martServiceRequest) 
	throws JDOMException, IOException, TechnicalException, FunctionalException {			
		QueryResponse queryResponse = new QueryResponse(
				MartRemoteEnum.QUERY.getResponseName(), martServiceNamespace, xsiNamespace, xsdFile, martRegistry, martServiceRequest);
		queryResponse.populateObjects();
		
		return queryResponse;
	}
	
	/**
		<query processor="CSV" header="true" uniqueRows="false" count="false" datasetConfigVersion="0.8">
			<dataset name="hsapiens_gene_ensembl">
				<attribute name="ensembl_gene_id" />
				<attribute name="ensembl_transcript_id" />
				<filter name="chromosome_name" value="1" />
			</dataset>
		</query>
		
		<query processor="CSV" header="true" uniqueRows="false" count="false" datasetConfigVersion="0.8"><dataset name="hsapiens_gene_ensembl"><attribute name="ensembl_gene_id" /><attribute name="ensembl_transcript_id" /><filter name="chromosome_name" value="1" /></dataset></query>
	 */

	/*public QueryRequest prepareQuery(String username, String password, MartServiceFormat format, String queryString) throws JDOMException, IOException {
		
		QueryRequest queryRequest = new QueryRequest(
				MartRemoteEnum.QUERY.getRequestName(), martServiceNamespace, xsiNamespace, xsdFile, username, password, format, queryString);
		queryRequest.rebuildQueryDocument();	// adding proper namespaces and wrapper
					// update errorMessage if not validation fails
		if (this.debug) System.out.println(MartRemoteUtils.getXmlDocumentString(queryRequest.getQueryDocument()));
		return queryRequest;
	}
	public QueryResponse query(String username, String password, QueryRequest queryRequest) throws FunctionalException, TechnicalException {
		return query(username, password, null, queryRequest);
	}
	public QueryResponse query(String username, String password, String formerVirtualSchema, QueryRequest queryRequest) throws FunctionalException, TechnicalException {
		Query query = queryRequest.buildQueryObject();
        List<List<String>> data = fetchData(username, password, query);
		List<String> headers = fetchHeaders(query);
		QueryResponse queryResult = new QueryResponse(
				MartRemoteEnum.QUERY.getResponseName(), martServiceNamespace, xsiNamespace, xsdFile, martRegistry, queryRequest, data, headers);
		return queryResult;
	}*/
	
	// Response writing
	public String processMartServiceResult(MartServiceResponse martServiceResponse, PrintWriter printWriter) throws TechnicalException {
		if (martServiceResponse.getMartServiceRequest().getFormat().isXml()) {
			Document document = martServiceResponse.getXmlRegistry(this.debug, printWriter);
			if (martServiceResponse.isValid()) {
				return writeXmlResponse(document, printWriter);					
			}
		} else if (martServiceResponse.getMartServiceRequest().getFormat().isJson()) {
			JSONObject jsonObject = martServiceResponse.getJsonRegistry();
			if (martServiceResponse.isValid()) {					
				return writeJsonResponse(jsonObject, printWriter);
			}
		}
		return writeError(martServiceResponse.getErrorMessage(), printWriter);	//	if (!martServiceResult.isValid())
	}
	public String writeXmlResponse(Document document, PrintWriter printWriter) throws TechnicalException {
		XMLOutputter compactFormat = new XMLOutputter(Format.getCompactFormat());
		try {
			compactFormat.output(document, printWriter);
		} catch (IOException e) {
			throw new TechnicalException(e);
		}
		return MartRemoteUtils.getXmlDocumentString(document);
	}
	public String writeJsonResponse(JSONObject root, PrintWriter printWriter) {
		printWriter.print(root);
		return root.toString();
	}
	public String writeError(StringBuffer errorMessage, PrintWriter printWriter) {
		String message = "ERROR" + MyUtils.LINE_SEPARATOR + errorMessage;
		printWriter.println(message);
		return message;
	}	
}



/*private void populateWithDummyData(List<List<String>> data) {
	data.add(new ArrayList<String>(Arrays.asList(new String[] {"ENSG00000233084", "ENST00000430973"})));
	data.add(new ArrayList<String>(Arrays.asList(new String[] {"ENSG00000233084", "ENST00000437585"})));
	data.add(new ArrayList<String>(Arrays.asList(new String[] {"ENSG00000227237", "ENST00000417047"})));
	data.add(new ArrayList<String>(Arrays.asList(new String[] {"ENSG00000229663", "ENST00000427780"})));
	data.add(new ArrayList<String>(Arrays.asList(new String[] {"ENSG00000242529", "ENST00000425108"})));
	data.add(new ArrayList<String>(Arrays.asList(new String[] {"ENSG00000227102", "ENST00000427827"})));
	data.add(new ArrayList<String>(Arrays.asList(new String[] {"ENSG00000229703", "ENST00000437830"})));
	data.add(new ArrayList<String>(Arrays.asList(new String[] {"ENSG00000232694", "ENST00000436484"})));
	data.add(new ArrayList<String>(Arrays.asList(new String[] {"ENSG00000224521", "ENST00000438623"})));
	data.add(new ArrayList<String>(Arrays.asList(new String[] {"ENSG00000224521", "ENST00000436515"})));
	data.add(new ArrayList<String>(Arrays.asList(new String[] {"ENSG00000227152", "ENST00000460972"})));
	data.add(new ArrayList<String>(Arrays.asList(new String[] {"ENSG00000227152", "ENST00000452956"})));
	data.add(new ArrayList<String>(Arrays.asList(new String[] {"ENSG00000229255", "ENST00000450847"})));
	data.add(new ArrayList<String>(Arrays.asList(new String[] {"ENSG00000177233", "ENST00000318104"})));
	data.add(new ArrayList<String>(Arrays.asList(new String[] {"ENSG00000232215", "ENST00000416377"})));
	data.add(new ArrayList<String>(Arrays.asList(new String[] {"ENSG00000197067", "ENST00000403423"})));
	data.add(new ArrayList<String>(Arrays.asList(new String[] {"ENSG00000197067", "ENST00000356294"})));
	data.add(new ArrayList<String>(Arrays.asList(new String[] {"ENSG00000226191", "ENST00000427566"})));
	data.add(new ArrayList<String>(Arrays.asList(new String[] {"ENSG00000226191", "ENST00000436052"})));
	data.add(new ArrayList<String>(Arrays.asList(new String[] {"ENSG00000224830", "ENST00000421144"})));
	data.add(new ArrayList<String>(Arrays.asList(new String[] {"ENSG00000237492", "ENST00000444456"})));
	data.add(new ArrayList<String>(Arrays.asList(new String[] {"ENSG00000224227", "ENST00000422727"})));
	data.add(new ArrayList<String>(Arrays.asList(new String[] {"ENSG00000232908", "ENST00000412419"})));
	data.add(new ArrayList<String>(Arrays.asList(new String[] {"ENSG00000232908", "ENST00000435783"})));
	data.add(new ArrayList<String>(Arrays.asList(new String[] {"ENSG00000230576", "ENST00000431838"})));
	data.add(new ArrayList<String>(Arrays.asList(new String[] {"ENSG00000235749", "ENST00000449298"})));
	data.add(new ArrayList<String>(Arrays.asList(new String[] {"ENSG00000230411", "ENST00000438288"})));
	data.add(new ArrayList<String>(Arrays.asList(new String[] {"ENSG00000236817", "ENST00000435333"})));
	data.add(new ArrayList<String>(Arrays.asList(new String[] {"ENSG00000236817", "ENST00000446347"})));
	data.add(new ArrayList<String>(Arrays.asList(new String[] {"ENSG00000198452", "ENST00000419087"})));
	data.add(new ArrayList<String>(Arrays.asList(new String[] {"ENSG00000198452", "ENST00000366486"})));
	data.add(new ArrayList<String>(Arrays.asList(new String[] {"ENSG00000203664", "ENST00000366492"})));
	data.add(new ArrayList<String>(Arrays.asList(new String[] {"ENSG00000203664", "ENST00000447418"})));
	data.add(new ArrayList<String>(Arrays.asList(new String[] {"ENSG00000227135", "ENST00000420469"})));
	data.add(new ArrayList<String>(Arrays.asList(new String[] {"ENSG00000214144", "ENST00000397642"})));
	data.add(new ArrayList<String>(Arrays.asList(new String[] {"ENSG00000235818", "ENST00000472048"})));
	data.add(new ArrayList<String>(Arrays.asList(new String[] {"ENSG00000235818", "ENST00000415799"})));
	data.add(new ArrayList<String>(Arrays.asList(new String[] {"ENSG00000197617", "ENST00000472952"})));
	data.add(new ArrayList<String>(Arrays.asList(new String[] {"ENSG00000197617", "ENST00000357135"})));
	data.add(new ArrayList<String>(Arrays.asList(new String[] {"ENSG00000232347", "ENST00000428687"})));
	data.add(new ArrayList<String>(Arrays.asList(new String[] {"ENSG00000224014", "ENST00000452704"})));
	data.add(new ArrayList<String>(Arrays.asList(new String[] {"ENSG00000215795", "ENST00000400933"})));
	data.add(new ArrayList<String>(Arrays.asList(new String[] {"ENSG00000226164", "ENST00000457012"})));
	data.add(new ArrayList<String>(Arrays.asList(new String[] {"ENSG00000227953", "ENST00000421003"})));
	data.add(new ArrayList<String>(Arrays.asList(new String[] {"ENSG00000227953", "ENST00000426089"})));
	data.add(new ArrayList<String>(Arrays.asList(new String[] {"ENSG00000227953", "ENST00000419361"})));
	data.add(new ArrayList<String>(Arrays.asList(new String[] {"ENSG00000227953", "ENST00000438701"})));
	data.add(new ArrayList<String>(Arrays.asList(new String[] {"ENSG00000236775", "ENST00000431287"})));
	data.add(new ArrayList<String>(Arrays.asList(new String[] {"ENSG00000215796", "ENST00000439523"})));
	data.add(new ArrayList<String>(Arrays.asList(new String[] {"ENSG00000215796", "ENST00000400934"})));
	data.add(new ArrayList<String>(Arrays.asList(new String[] {"ENSG00000228879", "ENST00000413092"})));
	data.add(new ArrayList<String>(Arrays.asList(new String[] {"ENSG00000225300", "ENST00000417786"})));
	data.add(new ArrayList<String>(Arrays.asList(new String[] {"ENSG00000228955", "ENST00000449978"})));
	data.add(new ArrayList<String>(Arrays.asList(new String[] {"ENSG00000235021", "ENST00000442712"})));
	data.add(new ArrayList<String>(Arrays.asList(new String[] {"ENSG00000225222", "ENST00000436684"})));
	data.add(new ArrayList<String>(Arrays.asList(new String[] {"ENSG00000230184", "ENST00000426929"})));
	data.add(new ArrayList<String>(Arrays.asList(new String[] {"ENSG00000227728", "ENST00000398739"})));
	data.add(new ArrayList<String>(Arrays.asList(new String[] {"ENSG00000235096", "ENST00000426386"})));
	data.add(new ArrayList<String>(Arrays.asList(new String[] {"ENSG00000226876", "ENST00000443763"})));
	data.add(new ArrayList<String>(Arrays.asList(new String[] {"ENSG00000226876", "ENST00000451926"})));
	data.add(new ArrayList<String>(Arrays.asList(new String[] {"ENSG00000231612", "ENST00000414565"})));
	data.add(new ArrayList<String>(Arrays.asList(new String[] {"ENSG00000232793", "ENST00000452887"})));
	data.add(new ArrayList<String>(Arrays.asList(new String[] {"ENSG00000238224", "ENST00000418402"})));
	data.add(new ArrayList<String>(Arrays.asList(new String[] {"ENSG00000179576", "ENST00000323801"})));
	data.add(new ArrayList<String>(Arrays.asList(new String[] {"ENSG00000223353", "ENST00000412257"})));
	data.add(new ArrayList<String>(Arrays.asList(new String[] {"ENSG00000232192", "ENST00000446321"})));
	data.add(new ArrayList<String>(Arrays.asList(new String[] {"ENSG00000227735", "ENST00000415043"})));
	data.add(new ArrayList<String>(Arrays.asList(new String[] {"ENSG00000232059", "ENST00000416174"})));
	data.add(new ArrayList<String>(Arrays.asList(new String[] {"ENSG00000225401", "ENST00000435390"})));
	data.add(new ArrayList<String>(Arrays.asList(new String[] {"ENSG00000240963", "ENST00000417765"})));
	data.add(new ArrayList<String>(Arrays.asList(new String[] {"ENSG00000237759", "ENST00000440148"})));
	data.add(new ArrayList<String>(Arrays.asList(new String[] {"ENSG00000229960", "ENST00000441338"})));
	data.add(new ArrayList<String>(Arrays.asList(new String[] {"ENSG00000228939", "ENST00000417120"})));
	data.add(new ArrayList<String>(Arrays.asList(new String[] {"ENSG00000226828", "ENST00000440494"})));
	data.add(new ArrayList<String>(Arrays.asList(new String[] {"ENSG00000232184", "ENST00000453572"})));
	data.add(new ArrayList<String>(Arrays.asList(new String[] {"ENSG00000226766", "ENST00000424004"})));
	data.add(new ArrayList<String>(Arrays.asList(new String[] {"ENSG00000236031", "ENST00000439849"})));
	data.add(new ArrayList<String>(Arrays.asList(new String[] {"ENSG00000224727", "ENST00000451185"})));
	data.add(new ArrayList<String>(Arrays.asList(new String[] {"ENSG00000214837", "ENST00000435840"})));
	data.add(new ArrayList<String>(Arrays.asList(new String[] {"ENSG00000214837", "ENST00000399073"})));
	data.add(new ArrayList<String>(Arrays.asList(new String[] {"ENSG00000214837", "ENST00000424800"})));
	data.add(new ArrayList<String>(Arrays.asList(new String[] {"ENSG00000214837", "ENST00000434639"})));
	data.add(new ArrayList<String>(Arrays.asList(new String[] {"ENSG00000214837", "ENST00000418377"})));
	data.add(new ArrayList<String>(Arrays.asList(new String[] {"ENSG00000214837", "ENST00000417964"})));
	data.add(new ArrayList<String>(Arrays.asList(new String[] {"ENSG00000214837", "ENST00000435793"})));
	data.add(new ArrayList<String>(Arrays.asList(new String[] {"ENSG00000214837", "ENST00000411733"})));
	data.add(new ArrayList<String>(Arrays.asList(new String[] {"ENSG00000214837", "ENST00000433986"})));
	data.add(new ArrayList<String>(Arrays.asList(new String[] {"ENSG00000214837", "ENST00000431528"})));
	data.add(new ArrayList<String>(Arrays.asList(new String[] {"ENSG00000214837", "ENST00000415989"})));
	data.add(new ArrayList<String>(Arrays.asList(new String[] {"ENSG00000214837", "ENST00000427210"})));
	data.add(new ArrayList<String>(Arrays.asList(new String[] {"ENSG00000214837", "ENST00000438255"})));
	data.add(new ArrayList<String>(Arrays.asList(new String[] {"ENSG00000214837", "ENST00000413762"})));
	data.add(new ArrayList<String>(Arrays.asList(new String[] {"ENSG00000227230", "ENST00000439562"})));
	data.add(new ArrayList<String>(Arrays.asList(new String[] {"ENSG00000232085", "ENST00000437499"})));
	data.add(new ArrayList<String>(Arrays.asList(new String[] {"ENSG00000232085", "ENST00000422938"})));
	data.add(new ArrayList<String>(Arrays.asList(new String[] {"ENSG00000230199", "ENST00000438481"})));
	data.add(new ArrayList<String>(Arrays.asList(new String[] {"ENSG00000234116", "ENST00000427176"})));
	data.add(new ArrayList<String>(Arrays.asList(new String[] {"ENSG00000231512", "ENST00000436756"})));
	data.add(new ArrayList<String>(Arrays.asList(new String[] {"ENSG00000231512", "ENST00000420830"})));
	data.add(new ArrayList<String>(Arrays.asList(new String[] {"ENSG00000231512", "ENST00000450226"})));
	data.add(new ArrayList<String>(Arrays.asList(new String[] {"ENSG00000231512", "ENST00000437691"})));
	data.add(new ArrayList<String>(Arrays.asList(new String[] {"ENSG00000215800", "ENST00000400938"})));
	data.add(new ArrayList<String>(Arrays.asList(new String[] {"ENSG00000213690", "ENST00000395679"})));
	data.add(new ArrayList<String>(Arrays.asList(new String[] {"ENSG00000235467", "ENST00000427498"})));
	data.add(new ArrayList<String>(Arrays.asList(new String[] {"ENSG00000224525", "ENST00000421878"})));
	data.add(new ArrayList<String>(Arrays.asList(new String[] {"ENSG00000224348", "ENST00000424448"})));
	data.add(new ArrayList<String>(Arrays.asList(new String[] {"ENSG00000225554", "ENST00000444330"})));
	data.add(new ArrayList<String>(Arrays.asList(new String[] {"ENSG00000225554", "ENST00000413994"})));
	data.add(new ArrayList<String>(Arrays.asList(new String[] {"ENSG00000229022", "ENST00000414898"})));
	data.add(new ArrayList<String>(Arrays.asList(new String[] {"ENSG00000235990", "ENST00000447965"})));
	data.add(new ArrayList<String>(Arrays.asList(new String[] {"ENSG00000196289", "ENST00000419583"})));
	data.add(new ArrayList<String>(Arrays.asList(new String[] {"ENSG00000196289", "ENST00000356052"})));
	data.add(new ArrayList<String>(Arrays.asList(new String[] {"ENSG00000213026", "ENST00000451536"})));
	data.add(new ArrayList<String>(Arrays.asList(new String[] {"ENSG00000213026", "ENST00000391847"})));
	data.add(new ArrayList<String>(Arrays.asList(new String[] {"ENSG00000224625", "ENST00000428444"})));
	data.add(new ArrayList<String>(Arrays.asList(new String[] {"ENSG00000224625", "ENST00000436895"})));
	data.add(new ArrayList<String>(Arrays.asList(new String[] {"ENSG00000232689", "ENST00000425514"})));
	data.add(new ArrayList<String>(Arrays.asList(new String[] {"ENSG00000233615", "ENST00000458678"})));
	data.add(new ArrayList<String>(Arrays.asList(new String[] {"ENSG00000238085", "ENST00000437248"})));
	data.add(new ArrayList<String>(Arrays.asList(new String[] {"ENSG00000230015", "ENST00000431139"})));
	data.add(new ArrayList<String>(Arrays.asList(new String[] {"ENSG00000226919", "ENST00000413801"})));
	data.add(new ArrayList<String>(Arrays.asList(new String[] {"ENSG00000223963", "ENST00000447914"})));
	data.add(new ArrayList<String>(Arrays.asList(new String[] {"ENSG00000215802", "ENST00000400940"})));
	data.add(new ArrayList<String>(Arrays.asList(new String[] {"ENSG00000234872", "ENST00000442611"})));
	data.add(new ArrayList<String>(Arrays.asList(new String[] {"ENSG00000228844", "ENST00000420757"})));
	data.add(new ArrayList<String>(Arrays.asList(new String[] {"ENSG00000231440", "ENST00000428891"})));
	data.add(new ArrayList<String>(Arrays.asList(new String[] {"ENSG00000224359", "ENST00000457477"})));
	data.add(new ArrayList<String>(Arrays.asList(new String[] {"ENSG00000226014", "ENST00000425769"})));
	data.add(new ArrayList<String>(Arrays.asList(new String[] {"ENSG00000223694", "ENST00000433842"})));
	data.add(new ArrayList<String>(Arrays.asList(new String[] {"ENSG00000233735", "ENST00000412311"})));
	data.add(new ArrayList<String>(Arrays.asList(new String[] {"ENSG00000233735", "ENST00000444308"})));
	data.add(new ArrayList<String>(Arrays.asList(new String[] {"ENSG00000233519", "ENST00000455599"})));
	data.add(new ArrayList<String>(Arrays.asList(new String[] {"ENSG00000228818", "ENST00000451892"})));
	data.add(new ArrayList<String>(Arrays.asList(new String[] {"ENSG00000215805", "ENST00000400943"})));
	data.add(new ArrayList<String>(Arrays.asList(new String[] {"ENSG00000217327", "ENST00000441482"})));
	data.add(new ArrayList<String>(Arrays.asList(new String[] {"ENSG00000217327", "ENST00000405182"})));
	data.add(new ArrayList<String>(Arrays.asList(new String[] {"ENSG00000233355", "ENST00000427859"})));
	data.add(new ArrayList<String>(Arrays.asList(new String[] {"ENSG00000233355", "ENST00000428176"})));
	data.add(new ArrayList<String>(Arrays.asList(new String[] {"ENSG00000233355", "ENST00000458325"})));
	data.add(new ArrayList<String>(Arrays.asList(new String[] {"ENSG00000234601", "ENST00000444721"})));
	data.add(new ArrayList<String>(Arrays.asList(new String[] {"ENSG00000231979", "ENST00000430542"})));
	data.add(new ArrayList<String>(Arrays.asList(new String[] {"ENSG00000227854", "ENST00000422215"})));
	data.add(new ArrayList<String>(Arrays.asList(new String[] {"ENSG00000227185", "ENST00000442410"})));
	data.add(new ArrayList<String>(Arrays.asList(new String[] {"ENSG00000215807", "ENST00000424695"})));
	data.add(new ArrayList<String>(Arrays.asList(new String[] {"ENSG00000215807", "ENST00000400945"})));
	data.add(new ArrayList<String>(Arrays.asList(new String[] {"ENSG00000224783", "ENST00000422560"})));
	data.add(new ArrayList<String>(Arrays.asList(new String[] {"ENSG00000215808", "ENST00000400946"})));
	data.add(new ArrayList<String>(Arrays.asList(new String[] {"ENSG00000231877", "ENST00000442726"})));
	data.add(new ArrayList<String>(Arrays.asList(new String[] {"ENSG00000232989", "ENST00000456364"})));
	data.add(new ArrayList<String>(Arrays.asList(new String[] {"ENSG00000234464", "ENST00000422844"})));
	data.add(new ArrayList<String>(Arrays.asList(new String[] {"ENSG00000227860", "ENST00000445624"})));
	data.add(new ArrayList<String>(Arrays.asList(new String[] {"ENSG00000230019", "ENST00000445891"})));
	data.add(new ArrayList<String>(Arrays.asList(new String[] {"ENSG00000225723", "ENST00000441157"})));
	data.add(new ArrayList<String>(Arrays.asList(new String[] {"ENSG00000232154", "ENST00000433123"})));
	data.add(new ArrayList<String>(Arrays.asList(new String[] {"ENSG00000237250", "ENST00000450451"})));
	data.add(new ArrayList<String>(Arrays.asList(new String[] {"ENSG00000237250", "ENST00000446627"})));
	data.add(new ArrayList<String>(Arrays.asList(new String[] {"ENSG00000243781", "ENST00000450208"})));
	data.add(new ArrayList<String>(Arrays.asList(new String[] {"ENSG00000223776", "ENST00000487567"})));
	data.add(new ArrayList<String>(Arrays.asList(new String[] {"ENSG00000223776", "ENST00000448554"})));
	data.add(new ArrayList<String>(Arrays.asList(new String[] {"ENSG00000230325", "ENST00000433131"})));
	data.add(new ArrayList<String>(Arrays.asList(new String[] {"ENSG00000226498", "ENST00000414293"})));
	data.add(new ArrayList<String>(Arrays.asList(new String[] {"ENSG00000237991", "ENST00000450819"})));
	data.add(new ArrayList<String>(Arrays.asList(new String[] {"ENSG00000244457", "ENST00000366587"})));
	data.add(new ArrayList<String>(Arrays.asList(new String[] {"ENSG00000237922", "ENST00000423792"})));
	data.add(new ArrayList<String>(Arrays.asList(new String[] {"ENSG00000229463", "ENST00000412098"})));
	data.add(new ArrayList<String>(Arrays.asList(new String[] {"ENSG00000237845", "ENST00000438371"})));
	data.add(new ArrayList<String>(Arrays.asList(new String[] {"ENSG00000229291", "ENST00000443207"})));
	data.add(new ArrayList<String>(Arrays.asList(new String[] {"ENSG00000235371", "ENST00000446607"})));
	data.add(new ArrayList<String>(Arrays.asList(new String[] {"ENSG00000235674", "ENST00000437325"})));
	data.add(new ArrayList<String>(Arrays.asList(new String[] {"ENSG00000226135", "ENST00000446945"})));
	data.add(new ArrayList<String>(Arrays.asList(new String[] {"ENSG00000232714", "ENST00000412909"})));
	data.add(new ArrayList<String>(Arrays.asList(new String[] {"ENSG00000235761", "ENST00000425787"})));
	data.add(new ArrayList<String>(Arrays.asList(new String[] {"ENSG00000227236", "ENST00000424229"})));
	data.add(new ArrayList<String>(Arrays.asList(new String[] {"ENSG00000231199", "ENST00000424872"})));
	data.add(new ArrayList<String>(Arrays.asList(new String[] {"ENSG00000226663", "ENST00000446199"})));
	data.add(new ArrayList<String>(Arrays.asList(new String[] {"ENSG00000231582", "ENST00000415303"})));
	data.add(new ArrayList<String>(Arrays.asList(new String[] {"ENSG00000229795", "ENST00000434479"})));
	data.add(new ArrayList<String>(Arrays.asList(new String[] {"ENSG00000238236", "ENST00000457471"})));
	data.add(new ArrayList<String>(Arrays.asList(new String[] {"ENSG00000235638", "ENST00000433044"})));
	data.add(new ArrayList<String>(Arrays.asList(new String[] {"ENSG00000230026", "ENST00000421884"})));
	data.add(new ArrayList<String>(Arrays.asList(new String[] {"ENSG00000243533", "ENST00000430613"})));
	data.add(new ArrayList<String>(Arrays.asList(new String[] {"ENSG00000236863", "ENST00000452590"})));
	data.add(new ArrayList<String>(Arrays.asList(new String[] {"ENSG00000227962", "ENST00000254724"})));
	data.add(new ArrayList<String>(Arrays.asList(new String[] {"ENSG00000232686", "ENST00000357671"})));
	data.add(new ArrayList<String>(Arrays.asList(new String[] {"ENSG00000233018", "ENST00000426692"})));
	data.add(new ArrayList<String>(Arrays.asList(new String[] {"ENSG00000238005", "ENST00000418557"})));
	data.add(new ArrayList<String>(Arrays.asList(new String[] {"ENSG00000238005", "ENST00000423988"})));
	data.add(new ArrayList<String>(Arrays.asList(new String[] {"ENSG00000238005", "ENST00000423175"})));
	data.add(new ArrayList<String>(Arrays.asList(new String[] {"ENSG00000238005", "ENST00000458044"})));
	data.add(new ArrayList<String>(Arrays.asList(new String[] {"ENSG00000238005", "ENST00000451766"})));
	data.add(new ArrayList<String>(Arrays.asList(new String[] {"ENSG00000227630", "ENST00000437601"})));
	data.add(new ArrayList<String>(Arrays.asList(new String[] {"ENSG00000237520", "ENST00000423963"})));
	data.add(new ArrayList<String>(Arrays.asList(new String[] {"ENSG00000224037", "ENST00000457051"})));
	data.add(new ArrayList<String>(Arrays.asList(new String[] {"ENSG00000224037", "ENST00000422414"})));
	data.add(new ArrayList<String>(Arrays.asList(new String[] {"ENSG00000241475", "ENST00000441360"})));
	data.add(new ArrayList<String>(Arrays.asList(new String[] {"ENSG00000230628", "ENST00000442382"})));
	data.add(new ArrayList<String>(Arrays.asList(new String[] {"ENSG00000228044", "ENST00000417805"})));
	data.add(new ArrayList<String>(Arrays.asList(new String[] {"ENSG00000228044", "ENST00000449012"})));
	data.add(new ArrayList<String>(Arrays.asList(new String[] {"ENSG00000228044", "ENST00000453568"})));
	data.add(new ArrayList<String>(Arrays.asList(new String[] {"ENSG00000224939", "ENST00000429269"})));
}*/
