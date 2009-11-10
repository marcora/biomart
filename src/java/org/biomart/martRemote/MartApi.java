package org.biomart.martRemote;


import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import net.sf.json.JSONObject;

import org.biomart.common.general.exceptions.FunctionalException;
import org.biomart.common.general.exceptions.TechnicalException;
import org.biomart.common.general.utils.MyUtils;
import org.biomart.common.general.utils.Timer;
import org.biomart.common.general.utils.XmlUtils;
import org.biomart.martRemote.enums.MartRemoteEnum;
import org.biomart.martRemote.enums.MartServiceFormat;
import org.biomart.martRemote.objects.request.GetAttributesRequest;
import org.biomart.martRemote.objects.request.GetContaineesRequest;
import org.biomart.martRemote.objects.request.GetDatasetsRequest;
import org.biomart.martRemote.objects.request.GetFiltersRequest;
import org.biomart.martRemote.objects.request.GetLinksRequest;
import org.biomart.martRemote.objects.request.GetRegistryRequest;
import org.biomart.martRemote.objects.request.GetRootContainerRequest;
import org.biomart.martRemote.objects.request.MartRemoteRequest;
import org.biomart.martRemote.objects.request.QueryRequest;
import org.biomart.martRemote.objects.response.GetAttributesResponse;
import org.biomart.martRemote.objects.response.GetContaineesResponse;
import org.biomart.martRemote.objects.response.GetDatasetsResponse;
import org.biomart.martRemote.objects.response.GetFiltersResponse;
import org.biomart.martRemote.objects.response.GetLinksResponse;
import org.biomart.martRemote.objects.response.GetRegistryResponse;
import org.biomart.martRemote.objects.response.GetRootContainerResponse;
import org.biomart.martRemote.objects.response.MartRemoteResponse;
import org.biomart.martRemote.objects.response.QueryResponse;
import org.biomart.objects.objects.MartRegistry;
import org.biomart.transformation.helpers.TransformationConstants;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.Namespace;
import org.jdom.input.SAXBuilder;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;


public class MartApi {

private static boolean COMPACT = false;

	@SuppressWarnings("all")
	public static void main(String[] args) throws Exception {

		StringWriter stringWriter = new StringWriter();
		MartApi martApi = new MartApi();
		System.out.println("Registry loaded");
		
		Timer timer = new Timer();
		timer.startTimer();
		
		MartRemoteRequest martServiceRequest = null;
		MartRemoteResponse martServiceResult = null;
		
		String type = 
			//"getRegistry";
			//"getDatasets";
			"getRootContainer";
			//"getAttributes";
			//"getFilters";
			//"query";
		String username = "anonymous";
		String password = "";
		String martName = MartRemoteConstants.WEB_PORTAL ? 
				"ensembl"
				//"uniprot_mart";
				:
				"ensembl_mart_55";
		Integer martVersion = MartRemoteConstants.WEB_PORTAL ? -1 : 55;
		String datasetName = 
			MartRemoteConstants.WEB_PORTAL ? "hsapiens_gene_ensembl" : "gene_ensembl";
			//"UNIPROT";
		String query = "query1";
		String filterPartitionString = TransformationConstants.MAIN_PARTITION_FILTER_NAME + 
			".\"hsapiens_gene_ensembl,mmusculus_gene_ensembl,celegans_gene_ensembl\"";
		MartServiceFormat format = MartServiceFormat.XML;
		
		MartRemoteEnum remoteAccessEnum = MartRemoteEnum.getEnumFromIdentifier(type);
		boolean valid = true;
		if (MartRemoteEnum.GET_REGISTRY.equals(remoteAccessEnum)) {
			martServiceRequest = martApi.prepareGetRegistry(username, password, format);			
		} else if (MartRemoteEnum.GET_DATASETS.equals(remoteAccessEnum)) {
			martServiceRequest = martApi.prepareGetDatasets(username, password, format, martName, martVersion);			
		} else if (MartRemoteEnum.GET_ROOT_CONTAINER.equals(remoteAccessEnum)) {
			martServiceRequest = martApi.prepareGetRootContainer(username, password, format, datasetName, filterPartitionString);
		}else if (MartRemoteEnum.GET_ATTRIBUTES.equals(remoteAccessEnum)) {
			martServiceRequest = martApi.prepareGetAttributes(username, password, format, datasetName, filterPartitionString);		
		} else if (MartRemoteEnum.GET_FILTERS.equals(remoteAccessEnum)) {
			martServiceRequest = martApi.prepareGetFilters(username, password, format, datasetName, filterPartitionString);		
		} else if (MartRemoteEnum.GET_LINKS.equals(remoteAccessEnum)) {
			martServiceRequest = martApi.prepareGetLinks(username, password, format, datasetName);			
		} else if (MartRemoteEnum.QUERY.equals(remoteAccessEnum)) {
			martServiceRequest = martApi.prepareQuery(username, password, format, MyUtils.getProperty(query));			
		}
		
		if (!martServiceRequest.isValid()) {
			martApi.writeError(martServiceRequest.getErrorMessage(), stringWriter);
			valid = false;
		} else {
			if (MartRemoteEnum.GET_REGISTRY.equals(remoteAccessEnum)) {			
				martServiceResult = martApi.executeGetRegistry(martServiceRequest);
			} else if (MartRemoteEnum.GET_DATASETS.equals(remoteAccessEnum)) {
				martServiceResult = martApi.executeGetDatasets(martServiceRequest);		
			} else if (MartRemoteEnum.GET_ROOT_CONTAINER.equals(remoteAccessEnum)) {
				martServiceResult = martApi.executeGetRootContainer(martServiceRequest);
			}else if (MartRemoteEnum.GET_ATTRIBUTES.equals(remoteAccessEnum)) {
				martServiceResult = martApi.executeGetAttributes(martServiceRequest);		
			} else if (MartRemoteEnum.GET_FILTERS.equals(remoteAccessEnum)) {
				martServiceResult = martApi.executeGetFilters(martServiceRequest);		
			} else if (MartRemoteEnum.GET_LINKS.equals(remoteAccessEnum)) {
				martServiceResult = martApi.executeGetLinks(martServiceRequest);			
			} else if (MartRemoteEnum.QUERY.equals(remoteAccessEnum)) {
				martServiceResult = martApi.executeQuery(martServiceRequest);			
			}
		}
		
		if (valid) {
			martApi.processMartServiceResult(martServiceResult, stringWriter);
		}
		stringWriter.flush();
		String string = stringWriter.toString();
		System.out.println(string);
		MyUtils.writeFile("/home/anthony/Desktop/MartApi", string);
		
		timer.stopTimer();
		System.out.println(timer);
	}
	
	private Boolean debug = null;
	
	private XmlParameters xmlParameters = null;
	//private String xsdFilePath = null;//private String xsdFileUrl = null;
	
	private Document xsd = null;
	private MartRegistry martRegistry = null;
	private SAXBuilder builder = null;
	
	public MartApi() throws IOException, JDOMException, TechnicalException {
		this(false, MartRemoteConstants.XSD_FILE_FILE_PATH_AND_NAME, MartRemoteConstants.XSD_FILE_FILE_PATH_AND_NAME, 
				MartRemoteConstants.PORTAL_SERIAL_FILE_PATH_AND_NAME, MartRemoteConstants.PORTAL_SERIAL_FILE_PATH_AND_NAME);
	}
	public MartApi(boolean webService, String xsdFilePath, String xsdFileUrl, String portalSerialPath, String portalSerialFileUrl) throws TechnicalException {
		this.debug = !webService;
		//String xsdFilePath = xsdFilePath;//this.xsdFileUrl = xsdFileUrl;
			
		builder = new SAXBuilder();
        xsd = null;
        try {
			xsd = builder.build(new URL(xsdFileUrl));
		} catch (MalformedURLException e) {
			throw new TechnicalException(e.getMessage() + ": " + xsdFileUrl);
		} catch (JDOMException e) {
			throw new TechnicalException(e);
		} catch (IOException e) {
			throw new TechnicalException(e);
		}
        Element xsdRootElement = xsd.getRootElement();
        Namespace xsdMartServiceNamespace = xsdRootElement.getNamespace("tns");
        
        Namespace martServiceNamespace = Namespace.getNamespace("ms", xsdMartServiceNamespace.getURI());	//Namespace.getNamespace("ms", "http://www.mynamespace.com/mynamespace");
        Namespace xsiNamespace = Namespace.getNamespace("xsi", "http://www.w3.org/2001/XMLSchema-instance");
        this.xmlParameters = new XmlParameters(martServiceNamespace, xsiNamespace, xsdFilePath);
        
		URL portalSerialUrl = null;
		try {
			portalSerialUrl = new URL(portalSerialFileUrl);
		} catch (MalformedURLException e) {
			throw new TechnicalException(e.getMessage() + ": " + portalSerialFileUrl);
		}
		martRegistry = (MartRegistry)MyUtils.readSerializedObject(portalSerialUrl);//martRegistry = TransformationYongPrototype.wrappedRebuildCentralPortalRegistry();		
	}
	
	public Document getXsd() {
		return xsd;
	}
	public MartRegistry getMartRegistry() {
		return martRegistry;
	}
	
	// Request preparation
	public GetRegistryRequest prepareGetRegistry(String username, String password, MartServiceFormat format) {
		GetRegistryRequest getRegistryRequest = new GetRegistryRequest(this.xmlParameters, username, password, format);
		return getRegistryRequest;
	}
	public GetDatasetsRequest prepareGetDatasets(String username, String password, MartServiceFormat format, 
			String martName, Integer martVersion) {
		GetDatasetsRequest getDatasetsRequest = new GetDatasetsRequest(this.xmlParameters, username, password, format, martName, martVersion);
		return getDatasetsRequest;
	}
	public GetContaineesRequest prepareGetContainees(String username, String password, MartServiceFormat format, 
			String datasetName, String partitionFilterString, MartRemoteEnum type) throws FunctionalException {
		String partitionFilter = null;
		List<String> partitionFilterValues = null;
		if (null!=partitionFilterString) {
			String[] split = partitionFilterString.split("[.]");
			if (split.length!=2) {
				throw new FunctionalException("Invalid partitionFilterString specified: " + partitionFilterString);
			}
			partitionFilter = split[0];
			partitionFilterValues = new ArrayList<String>(Arrays.asList(split[1].replace("\"", "").split(",")));
		}
		GetContaineesRequest getContaineesRequest = null;
		if (MartRemoteEnum.GET_ROOT_CONTAINER.equals(type)) {
			getContaineesRequest = new GetRootContainerRequest(this.xmlParameters, username, password, format, 
					datasetName, partitionFilter, partitionFilterValues);
		} else if (MartRemoteEnum.GET_ATTRIBUTES.equals(type)) {
			getContaineesRequest = new GetAttributesRequest(this.xmlParameters, username, password, format, 
					datasetName, partitionFilter, partitionFilterValues);
		} else if (MartRemoteEnum.GET_FILTERS.equals(type)) {
			getContaineesRequest = new GetFiltersRequest(this.xmlParameters, username, password, format, 
					datasetName, partitionFilter, partitionFilterValues);
		}
		return getContaineesRequest;
	}
	public GetRootContainerRequest prepareGetRootContainer(String username, String password, MartServiceFormat format, 
			String datasetName, String partitionFilterString) throws FunctionalException {
		return (GetRootContainerRequest)prepareGetContainees(
				username, password, format, datasetName, partitionFilterString, MartRemoteEnum.GET_ROOT_CONTAINER);
	}
	public GetAttributesRequest prepareGetAttributes(String username, String password, MartServiceFormat format, 
			String datasetName, String partitionFilterString) throws FunctionalException {
		return (GetAttributesRequest)prepareGetContainees(
				username, password, format, datasetName, partitionFilterString, MartRemoteEnum.GET_ATTRIBUTES);
	}
	public GetFiltersRequest prepareGetFilters(String username, String password, MartServiceFormat format, 
			String datasetName, String partitionFilterString) throws FunctionalException {
		return (GetFiltersRequest)prepareGetContainees(
				username, password, format, datasetName, partitionFilterString, MartRemoteEnum.GET_FILTERS);
	}
	public GetLinksRequest prepareGetLinks(String username, String password, MartServiceFormat format, String datasetName) {
		GetLinksRequest getLinksRequest = new GetLinksRequest(this.xmlParameters, username, password, format, datasetName);
		return getLinksRequest;
	}
	public QueryRequest prepareQuery(String username, String password, MartServiceFormat format, String queryString) throws TechnicalException, FunctionalException {
		QueryRequest queryRequest = new QueryRequest(this.xmlParameters, username, password, format, queryString) ;
		queryRequest.rebuildQueryDocument();	// adding proper namespaces and wrapper
							// update errorMessage if not validation fails
		queryRequest.buildObjects();
		if (this.debug) System.out.println(XmlUtils.getXmlDocumentString(queryRequest.getQueryDocument()));
		
		return queryRequest;
	}	
	
	// Request execution
	public GetRegistryResponse executeGetRegistry(MartRemoteRequest martServiceRequest) throws FunctionalException {
		GetRegistryResponse getRegistryResponse = new GetRegistryResponse(martRegistry, martServiceRequest);
		getRegistryResponse.populateObjects();
		return getRegistryResponse;
	}
	public GetDatasetsResponse executeGetDatasets(MartRemoteRequest martServiceRequest) throws FunctionalException {
		GetDatasetsResponse getDatasetsResponse = new GetDatasetsResponse(martRegistry, martServiceRequest);
		getDatasetsResponse.populateObjects();
		return getDatasetsResponse;
	}
	public GetContaineesResponse executeGetContainees(MartRemoteRequest martServiceRequest, MartRemoteEnum type) throws FunctionalException{
		GetContaineesResponse getContaineesResponse = null;
		if (MartRemoteEnum.GET_ROOT_CONTAINER.equals(type)) {
			getContaineesResponse = new GetRootContainerResponse(martRegistry, martServiceRequest);
		} else if (MartRemoteEnum.GET_ATTRIBUTES.equals(type)) {
			getContaineesResponse = new GetAttributesResponse(martRegistry, martServiceRequest);
		} else if (MartRemoteEnum.GET_FILTERS.equals(type)) {
			getContaineesResponse = new GetFiltersResponse(martRegistry, martServiceRequest);
		} 
		getContaineesResponse.populateObjects();
		return getContaineesResponse;
	}
	public GetRootContainerResponse executeGetRootContainer(MartRemoteRequest martServiceRequest) throws FunctionalException{
		return (GetRootContainerResponse)executeGetContainees(martServiceRequest, MartRemoteEnum.GET_ROOT_CONTAINER);
	}
	public GetAttributesResponse executeGetAttributes(MartRemoteRequest martServiceRequest) throws FunctionalException{
		return (GetAttributesResponse)executeGetContainees(martServiceRequest, MartRemoteEnum.GET_ATTRIBUTES);
	}
	public GetFiltersResponse executeGetFilters(MartRemoteRequest martServiceRequest) throws FunctionalException{
		return (GetFiltersResponse)executeGetContainees(martServiceRequest, MartRemoteEnum.GET_FILTERS);
	}
	public GetLinksResponse executeGetLinks(MartRemoteRequest martServiceRequest) {
		GetLinksResponse getLinksResponse = new GetLinksResponse(martRegistry, martServiceRequest);
		getLinksResponse.populateObjects();
		return getLinksResponse;
	}
	public QueryResponse executeQuery(MartRemoteRequest martServiceRequest) throws TechnicalException, FunctionalException {			
		QueryResponse queryResponse = new QueryResponse(martRegistry, martServiceRequest);
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
	// Response writing
	public String processMartServiceResult(MartRemoteResponse martServiceResponse, Writer writer) throws TechnicalException, FunctionalException {
		if (martServiceResponse.getMartServiceRemote().getFormat().isXml()) {
			Document document = martServiceResponse.getXmlDocument(this.debug, writer);
			if (martServiceResponse.isValid()) {
				return writeXmlResponse(document, writer);					
			}
		} else if (martServiceResponse.getMartServiceRemote().getFormat().isJson()) {
			JSONObject jSONObject = martServiceResponse.getJsonObject(this.debug, writer);
			if (martServiceResponse.isValid()) {					
				return writeJsonResponse(jSONObject, writer);
			}
		}
		return writeError(martServiceResponse.getErrorMessage(), writer);	//	if (!martServiceResult.isValid())
	}
	public String writeXmlResponse(Document document, Writer writer) throws TechnicalException {
		if (null!=writer && COMPACT) {
			try {
				XMLOutputter compactFormat = new XMLOutputter(Format.getCompactFormat());
				compactFormat.output(document, writer);
			} catch (IOException e) {
				throw new TechnicalException(e);
			}
		}
		return XmlUtils.getXmlDocumentString(document);
	}
	public String writeJsonResponse(JSONObject jSONObject, Writer writer) throws TechnicalException {
		if (null!=writer && COMPACT) {
			try {
				writer.append(
						jSONObject
						//JsonUtils.getJSONObjectNiceString(jSONObject)
						+ MyUtils.LINE_SEPARATOR);
			} catch (IOException e) {
				throw new TechnicalException(e);
			}
		}
		return jSONObject.toString();
	}
	/*public String writeJsonResponse(JSON json, Writer writer) throws TechnicalException {
		if (null!=writer && COMPACT) {
			try {
				writer.append(json.toString().substring(0, 1000) + MyUtils.LINE_SEPARATOR);
			} catch (IOException e) {
				throw new TechnicalException(e);
			}
		}
		return json.toString().substring(0, 1000);
	}
	@Deprecated
	public String writeJsonResponse(org.json.JSONObject root, Writer writer) throws TechnicalException {
		if (null!=writer && COMPACT) {
			try {
				writer.append(JsonUtils.getJSONObjectNiceString(root) + MyUtils.LINE_SEPARATOR);
			} catch (IOException e) {
				throw new TechnicalException(e);
			}
		}
		return root.toString();
	}*/
	
	public String writeError(StringBuffer errorMessage, Writer writer) throws TechnicalException {
		String message = "ERROR" + MyUtils.LINE_SEPARATOR + errorMessage;
		if (null!=writer) {
			try {
				writer.append(message + MyUtils.LINE_SEPARATOR);
			} catch (IOException e) {
				throw new TechnicalException(e);
			}
		}
		return message;
	}	
}
