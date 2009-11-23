package org.biomart.martRemote;


import java.io.IOException;
import java.io.Writer;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import net.sf.json.JSONObject;

import org.biomart.common.general.constants.MyConstants;
import org.biomart.common.general.exceptions.FunctionalException;
import org.biomart.common.general.exceptions.TechnicalException;
import org.biomart.common.general.utils.MyUtils;
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
import org.biomart.objects.lite.LiteListAttribute;
import org.biomart.objects.lite.LiteListDataset;
import org.biomart.objects.lite.LiteListFilter;
import org.biomart.objects.lite.LiteMartRegistry;
import org.biomart.objects.lite.LiteRootContainer;
import org.biomart.objects.lite.MartRemoteWrapper;
import org.biomart.objects.lite.QueryResult;
import org.biomart.objects.objects.MartRegistry;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.Namespace;
import org.jdom.input.SAXBuilder;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;


public class MartApi {

	/**
	 * registry containing all the info
	 */
	private MartRegistry martRegistry = null;
	
	/**
	 * whether xmls input/output should be validated
	 * For development only => to make sure XSD is always in sync with code
	 */
	private Boolean validateXml = null;
	
	/**
	 * if xml requires validation, parameters to do so
	 */
	private XmlParameters xmlParameters = null;
	
	/**
	 * debug mode (development only)
	 */
	private Boolean debug = null;
	
	public MartApi() throws FunctionalException, TechnicalException {
		this(MartRemoteConstants.BIOMART_JAVA_SERIALIZED_PORTAL_FILE);
	}
	public MartApi(String portalSerialFileUrl) 
				throws TechnicalException, FunctionalException {
		this(false, false, null, null, portalSerialFileUrl);
	}
	public MartApi(MartRegistry martRegistry) throws TechnicalException, FunctionalException {
		this(false, false, null, null, null, martRegistry);
	}
	/**
	 * Constructors for development
	 */
	public MartApi(boolean debug, boolean validateXml, 
			String xsdFilePath, String xsdFileUrl, String portalSerialFileUrl) throws TechnicalException, FunctionalException {
		this(debug, validateXml, xsdFilePath, xsdFileUrl, portalSerialFileUrl, null);
	}
	public MartApi(boolean debug, boolean validateXml, 
			String xsdFilePath, String xsdFileUrl, String portalSerialFileUrl, MartRegistry martRegistry) 
				throws TechnicalException, FunctionalException {
		
		this.debug = debug;
		this.validateXml = validateXml;
		
		Namespace martServiceNamespace = Namespace.getNamespace("ms", MartRemoteConstants.MART_SERVICE_NAMESPACE);
        Namespace xsiNamespace = Namespace.getNamespace("xsi", "http://www.w3.org/2001/XMLSchema-instance");
		if (this.validateXml) {
			SAXBuilder builder = new SAXBuilder();
			Document xsd = null;
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
			if (!martServiceNamespace.getURI().equals(xsdMartServiceNamespace.getURI())) {
				throw new FunctionalException("XSD namespace doesn't match the expected one");
			}
		}
        this.xmlParameters = new XmlParameters(this.validateXml, martServiceNamespace, xsiNamespace, xsdFilePath);
        
        this.martRegistry = null!=martRegistry ? martRegistry : loadSerializedMartRegistry(portalSerialFileUrl);
	}
	
	private MartRegistry loadSerializedMartRegistry(String portalSerialFileUrl) throws TechnicalException {
		URL portalSerialUrl = null;
		try {
			portalSerialFileUrl = portalSerialFileUrl.startsWith(MyConstants.FILE_SYSTEM_PROTOCOL) ? 
					portalSerialFileUrl : MyConstants.FILE_SYSTEM_PROTOCOL + portalSerialFileUrl;
			portalSerialUrl = new URL(portalSerialFileUrl);
		} catch (MalformedURLException e) {
			throw new TechnicalException(e.getMessage() + ": " + portalSerialFileUrl);
		}
		return (MartRegistry)MyUtils.readSerializedObject(portalSerialUrl);
	}

	public MartRegistry getMartRegistry() {
		return martRegistry;
	}
	
	// Request preparation
	private GetRegistryRequest prepareGetRegistry(String username, String password, MartServiceFormat format) {
		GetRegistryRequest getRegistryRequest = new GetRegistryRequest(this.xmlParameters, username, password, format);
		return getRegistryRequest;
	}
	private GetDatasetsRequest prepareGetDatasets(String username, String password, MartServiceFormat format, 
			String martName, Integer martVersion) {
		GetDatasetsRequest getDatasetsRequest = new GetDatasetsRequest(this.xmlParameters, username, password, format, martName, martVersion);
		return getDatasetsRequest;
	}
	private GetContaineesRequest prepareGetContainees(String username, String password, MartServiceFormat format, 
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
	private GetRootContainerRequest prepareGetRootContainer(String username, String password, MartServiceFormat format, 
			String datasetName, String partitionFilterString) throws FunctionalException {
		return (GetRootContainerRequest)prepareGetContainees(
				username, password, format, datasetName, partitionFilterString, MartRemoteEnum.GET_ROOT_CONTAINER);
	}
	private GetAttributesRequest prepareGetAttributes(String username, String password, MartServiceFormat format, 
			String datasetName, String partitionFilterString) throws FunctionalException {
		return (GetAttributesRequest)prepareGetContainees(
				username, password, format, datasetName, partitionFilterString, MartRemoteEnum.GET_ATTRIBUTES);
	}
	private GetFiltersRequest prepareGetFilters(String username, String password, MartServiceFormat format, 
			String datasetName, String partitionFilterString) throws FunctionalException {
		return (GetFiltersRequest)prepareGetContainees(
				username, password, format, datasetName, partitionFilterString, MartRemoteEnum.GET_FILTERS);
	}
	@SuppressWarnings("unused")
	private GetLinksRequest prepareGetLinks(String username, String password, MartServiceFormat format, String datasetName) {
		GetLinksRequest getLinksRequest = new GetLinksRequest(this.xmlParameters, username, password, format, datasetName);
		return getLinksRequest;
	}
	private QueryRequest prepareQuery(String username, String password, MartServiceFormat format, String queryString) throws TechnicalException, FunctionalException {
		QueryRequest queryRequest = new QueryRequest(this.xmlParameters, username, password, format, queryString) ;
		queryRequest.rebuildQueryDocument();	// adding proper namespaces and wrapper
							// update errorMessage if not validation fails
		queryRequest.buildObjects();
		if (this.debug) {
			System.out.println("query request xml: " + XmlUtils.getXmlDocumentString(queryRequest.getQueryDocument()));
		}
		
		return queryRequest;
	}	
	
	// Request execution
	private GetRegistryResponse executeGetRegistry(MartRemoteRequest martServiceRequest) throws FunctionalException {
		GetRegistryResponse getRegistryResponse = new GetRegistryResponse(martRegistry, martServiceRequest);
		getRegistryResponse.populateObjects();
		return getRegistryResponse;
	}
	private GetDatasetsResponse executeGetDatasets(MartRemoteRequest martServiceRequest) throws FunctionalException {
		GetDatasetsResponse getDatasetsResponse = new GetDatasetsResponse(martRegistry, martServiceRequest);
		getDatasetsResponse.populateObjects();
		return getDatasetsResponse;
	}
	private GetContaineesResponse executeGetContainees(MartRemoteRequest martServiceRequest, MartRemoteEnum type) throws FunctionalException{
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
	private GetRootContainerResponse executeGetRootContainer(MartRemoteRequest martServiceRequest) throws FunctionalException{
		return (GetRootContainerResponse)executeGetContainees(martServiceRequest, MartRemoteEnum.GET_ROOT_CONTAINER);
	}
	private GetAttributesResponse executeGetAttributes(MartRemoteRequest martServiceRequest) throws FunctionalException{
		return (GetAttributesResponse)executeGetContainees(martServiceRequest, MartRemoteEnum.GET_ATTRIBUTES);
	}
	private GetFiltersResponse executeGetFilters(MartRemoteRequest martServiceRequest) throws FunctionalException{
		return (GetFiltersResponse)executeGetContainees(martServiceRequest, MartRemoteEnum.GET_FILTERS);
	}
	@SuppressWarnings("unused")
	private GetLinksResponse executeGetLinks(MartRemoteRequest martServiceRequest) {
		GetLinksResponse getLinksResponse = new GetLinksResponse(martRegistry, martServiceRequest);
		getLinksResponse.populateObjects();
		return getLinksResponse;
	}
	private QueryResponse executeQuery(MartRemoteRequest martServiceRequest) throws TechnicalException, FunctionalException {			
		QueryResponse queryResponse = new QueryResponse(martRegistry, martServiceRequest);
		queryResponse.populateObjects();
		
		return queryResponse;
	}
	
	public LiteMartRegistry getRegistry(String username, String password, String format) throws FunctionalException {
		GetRegistryResponse getRegistryResponse = getRegistryResponse(username, password, format);
		return getRegistryResponse!=null ? getRegistryResponse.getLiteMartRegistry() : null;
	}
	public LiteListDataset getDatasets(String username, String password,
			String format, String mart, Integer version) throws FunctionalException {
		GetDatasetsResponse getDatasetsResponse = getDatasetsResponse(username, password, format, mart, version);
		return getDatasetsResponse!=null ? getDatasetsResponse.getLiteListDataset() : null;		
	}
	public LiteRootContainer getRootContainer(String username, String password, String format, 
			String dataset, String partitionFilter) throws FunctionalException, TechnicalException {
		GetRootContainerResponse getRootContainerResponse = getRootContainerResponse(
				username, password, format, dataset, partitionFilter);
		return getRootContainerResponse!=null ? getRootContainerResponse.getLiteRootContainer() : null;
	}
	public LiteListAttribute getAttributes(String username, String password, String format, 
			String dataset, String partitionFilter) throws FunctionalException, TechnicalException {
		GetAttributesResponse getAttributesResponse = getAttributesResponse(
				username, password, format, dataset, partitionFilter);
		return getAttributesResponse!=null ? getAttributesResponse.getLiteListAttribute() : null;
	}
	public LiteListFilter getFilters(String username, String password, String format, 
			String dataset, String partitionFilter) throws FunctionalException, TechnicalException {
		GetFiltersResponse getFiltersResponse = getFiltersResponse(
				username, password, format, dataset, partitionFilter);
		return getFiltersResponse!=null ? getFiltersResponse.getLiteListFilter() : null;
	}
	

	protected GetRegistryResponse getRegistryResponse(String username, String password, String format) throws FunctionalException {
		MartRemoteRequest martRemoteRequest = this.prepareGetRegistry(
				username, password, MartServiceFormat.getFormat(format));
		return martRemoteRequest.isValid() ?
				(GetRegistryResponse)executeRequest(martRemoteRequest) : null;
	}
	protected GetDatasetsResponse getDatasetsResponse(String username, String password,
			String format, String mart, Integer version) throws FunctionalException {
		MartRemoteRequest martRemoteRequest = this.prepareGetDatasets(
				username, password, MartServiceFormat.getFormat(format), mart, version);
		return martRemoteRequest.isValid() ?
				(GetDatasetsResponse)executeRequest(martRemoteRequest) : null;
	}
	protected GetRootContainerResponse getRootContainerResponse(String username, String password, String format, 
			String dataset, String partitionFilter) throws FunctionalException, TechnicalException {
		return (GetRootContainerResponse)getContainees(
				MartRemoteEnum.GET_ROOT_CONTAINER, username, password, format, dataset, partitionFilter);
	}
	protected GetAttributesResponse getAttributesResponse(String username, String password, String format, 
			String dataset, String partitionFilter) throws FunctionalException, TechnicalException {
		return (GetAttributesResponse)getContainees(
				MartRemoteEnum.GET_ATTRIBUTES, username, password, format, dataset, partitionFilter);
	}
	protected GetFiltersResponse getFiltersResponse(String username, String password, String format, 
			String dataset, String partitionFilter) throws FunctionalException, TechnicalException {
		return (GetFiltersResponse)getContainees(
				MartRemoteEnum.GET_FILTERS, username, password, format, dataset, partitionFilter);
	}
	private GetContaineesResponse getContainees(MartRemoteEnum type, String username, String password, String format, 
			String dataset, String partitionFilter) throws FunctionalException, TechnicalException {
		MartRemoteRequest martRemoteRequest = null;
		if (MartRemoteEnum.GET_ROOT_CONTAINER.equals(type)) {
			martRemoteRequest = this.prepareGetRootContainer(
					username, password, MartServiceFormat.getFormat(format), dataset, partitionFilter);
		} else if (MartRemoteEnum.GET_ATTRIBUTES.equals(type)) {
			martRemoteRequest = this.prepareGetAttributes(
					username, password, MartServiceFormat.getFormat(format), dataset, partitionFilter);
		}if (MartRemoteEnum.GET_FILTERS.equals(type)) {
			martRemoteRequest = this.prepareGetFilters(
					username, password, MartServiceFormat.getFormat(format), dataset, partitionFilter);
		}
		return martRemoteRequest.isValid() ?
				(GetContaineesResponse)executeRequest(martRemoteRequest) : null;
	}
	protected QueryResponse queryResponse(String username, String password, String format,
			String query) throws FunctionalException {
		MartRemoteRequest martRemoteRequest = null;
		try {
			martRemoteRequest = this.prepareQuery(
					username, password, MartServiceFormat.getFormat(format), query);
		} catch (TechnicalException e) {
			e.printStackTrace();
			return null;
		} catch (FunctionalException e) {
			e.printStackTrace();
			return null;
		}
		return martRemoteRequest.isValid() ?
			(QueryResponse)executeRequest(martRemoteRequest) : null;
	}
	public QueryResult query(String username, String password, String format,
			String query) throws FunctionalException {
		QueryResponse queryResponse = queryResponse(username, password, format, query);
		return queryResponse!=null ? queryResponse.getQueryResult() : null;
	}
	
	private MartRemoteResponse executeRequest(MartRemoteRequest martRemoteRequest) throws FunctionalException {
		
		MartRemoteResponse martRemoteResponse = null;
		try {
			if (martRemoteRequest instanceof GetRegistryRequest) {
				martRemoteResponse = this.executeGetRegistry(martRemoteRequest);
			} else if (martRemoteRequest instanceof GetDatasetsRequest) {
				martRemoteResponse = this.executeGetDatasets(martRemoteRequest);		
			} else if (martRemoteRequest instanceof GetRootContainerRequest) {
				martRemoteResponse = this.executeGetRootContainer(martRemoteRequest);
			} else if (martRemoteRequest instanceof GetAttributesRequest) {
				martRemoteResponse = this.executeGetAttributes(martRemoteRequest);
			} else if (martRemoteRequest instanceof GetFiltersRequest) {
				martRemoteResponse = this.executeGetFilters(martRemoteRequest);
			} else if (martRemoteRequest instanceof QueryRequest) {
				martRemoteResponse = this.executeQuery(martRemoteRequest);		
			} else {
				return null;
			}
		} catch (TechnicalException e) {
			e.printStackTrace();
			return null;
		} catch (FunctionalException e) {
			e.printStackTrace();
			return null;
		}
		return martRemoteResponse;
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
	protected String processMartServiceResult(MartRemoteResponse martRemoteResponse, Writer writer) throws TechnicalException, FunctionalException {
		MartRemoteWrapper martRemoteWrapper = martRemoteResponse.getMartRemoteWrapper();
		if (null!=martRemoteWrapper && martRemoteResponse.getMartServiceRequest().getFormat().isXml()) {
			Document document = martRemoteWrapper.getXmlDocument(this.debug, writer);
			if (martRemoteResponse.isValid()) {
				return writeXmlResponse(document, writer);					
			}
		} else if (null!=martRemoteWrapper && martRemoteResponse.getMartServiceRequest().getFormat().isJson()) {
			@SuppressWarnings("deprecation")	// for now
			JSONObject jSONObject = martRemoteWrapper.getJsonObject(this.debug, writer);
			if (martRemoteResponse.isValid()) {					
				return writeJsonResponse(jSONObject, writer);
			}
		}
		return writeError(martRemoteResponse.getErrorMessage(), writer);	//	if (!martServiceResult.isValid())
	}
	
	private String writeXmlResponse(Document document, Writer writer) throws TechnicalException {
		if (null!=writer && this.debug) {
			try {
				XMLOutputter compactFormat = new XMLOutputter(Format.getCompactFormat());
				compactFormat.output(document, writer);
			} catch (IOException e) {
				throw new TechnicalException(e);
			}
		}
		return XmlUtils.getXmlDocumentString(document);
	}
	private String writeJsonResponse(JSONObject jSONObject, Writer writer) throws TechnicalException {
		if (null!=writer && this.debug) {
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
	
	private String writeError(StringBuffer errorMessage, Writer writer) throws TechnicalException {
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
