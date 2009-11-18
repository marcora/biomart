package org.biomart.martRemote;

import java.io.StringWriter;

import org.biomart.common.general.utils.MyUtils;
import org.biomart.common.general.utils.Timer;
import org.biomart.martRemote.enums.MartRemoteEnum;
import org.biomart.martRemote.enums.MartServiceFormat;
import org.biomart.martRemote.objects.request.MartRemoteRequest;
import org.biomart.martRemote.objects.response.MartRemoteResponse;
import org.biomart.transformation.helpers.TransformationConstants;

public class MartApiDevelopment {

	public static final String type = 
		//"getRegistry";
		//"getDatasets";
		"getRootContainer";
		//"getAttributes";
		//"getFilters";
		//"query";
	public static final String username = "anonymous";
	public static final String password = "";
	public static final String martName = MartRemoteConstants.WEB_PORTAL ? 
			"ensembl"
			//"uniprot_mart";
			:
			"ensembl_mart_55";
	public static final Integer martVersion = MartRemoteConstants.WEB_PORTAL ? -1 : 55;
	public static final String datasetName = 
		MartRemoteConstants.WEB_PORTAL ? "hsapiens_gene_ensembl" : "gene_ensembl";
		//"UNIPROT";
	public static final String query = MyUtils.wrappedGetProperty(
			MartRemoteConstants.QUERY_TEST_PROPERTIES_FILE_PATH_AND_NAME, "query1");
	public static final String filterPartitionString = TransformationConstants.MAIN_PARTITION_FILTER_NAME + 
		".\"hsapiens_gene_ensembl,mmusculus_gene_ensembl,celegans_gene_ensembl\"";
	public static final MartServiceFormat format = MartServiceFormat.XML;

	@SuppressWarnings("all")
	public static void main(String[] args) throws Exception {

		StringWriter stringWriter = new StringWriter();
		MartApi martApi = new MartApi(true, true, 
				MartRemoteConstants.XSD_FILE_FILE_PATH_AND_NAME, MartRemoteConstants.XSD_FILE_FILE_PATH_AND_NAME, 
				MartRemoteConstants.PORTAL_SERIAL_FILE_PATH_AND_NAME);
		System.out.println("Registry loaded");
		
		Timer timer = new Timer();
		timer.startTimer();
		
		MartRemoteRequest martServiceRequest = null;
		MartRemoteResponse martServiceResult = null;
		
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
			martServiceRequest = martApi.prepareQuery(username, password, format, query);			
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
		
		/*MartRegistry martRegistry = martApi.getMartRegistry();
		Location location = martRegistry.getLocation("ensembl_mart_55");
		Mart mart = location.getMart("ensembl_mart_55");
		Dataset dataset = mart.getDataset("gene_ensembl");
		Config config = dataset.getConfig("gene_ensembl_template");
		Container rootContainer = config.getRootContainer();
		MyUtils.writeXmlFile(martRegistry.generateXmlDocument(), "/home/anthony/Desktop/zzzzz.xml");*/
	}
}
