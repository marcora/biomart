package org.biomart.martRemote.martService;


import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


import org.biomart.common.general.constants.MyConstants;
import org.biomart.common.general.exceptions.FunctionalException;
import org.biomart.common.general.exceptions.TechnicalException;
import org.biomart.common.general.utils.MyUtils;
import org.biomart.martRemote.MartApi;
import org.biomart.martRemote.MartRemoteConstants;
import org.biomart.martRemote.MartRemoteUtils;
import org.biomart.martRemote.enums.MartRemoteEnum;
import org.biomart.martRemote.enums.MartServiceFormat;
import org.biomart.martRemote.objects.request.MartServiceRequest;
import org.biomart.martRemote.objects.request.QueryRequest;
import org.biomart.martRemote.objects.response.MartServiceResponse;
import org.jdom.JDOMException;



/**
		sudo cp /home/anthony/workspace/00MartConfigurator/xml/martservice.xsd /home/anthony/workspace/MartService/xml/
		sudo cp /home/anthony/workspace/00MartConfigurator/xml/web.xml /home/anthony/workspace/MartService/WEB-INF/
		sudo cp /home/anthony/workspace/00MartConfigurator/files/portal.serial /home/anthony/workspace/MartService/files/
		sudo cp /home/anthony/workspace/00MartConfigurator/web/index.jsp /home/anthony/workspace/MartService/
		sudo cp /home/anthony/workspace/00MartConfigurator/lib/* /home/anthony/workspace/MartService/WEB-INF/lib/
		cd /home/anthony/workspace/MartService;sudo rm -rf /var/lib/tomcat6/webapps/MartService.war /var/lib/tomcat6/webapps/MartService;sudo jar -cvf /var/lib/tomcat6/webapps/MartService.war .;sudo /etc/init.d/tomcat6 restart
		http://localhost:8082/MartService/martService?username=anonymous&password=p&format=xml&type=getDatasets&martName=ensembl&martVersion=-1
		
		sudo sh /home/anthony/workspace/00MartConfigurator/scripts/martServiceServer.sh
		firefox 'http://localhost:8082/MartService'
		firefox 'http://localhost:8082/MartService/martService?username=anonymous&password=p&format=xml&type=getDatasets&martName=ensembl&martVersion=-1'
		firefox 'http://localhost:8082/MartService/martService?username=anonymous&password=p&format=xml&type=query&query=<query formerVirtualSchema="default" processor="TSV" header="true" uniqueRows="false" count="false" datasetConfigVersion="0.7" limitStart="0" limitSize="100"><dataset name="hsapiens_gene_ensembl"><attribute name="ensembl_gene_id" /><attribute name="ensembl_transcript_id" /><filter name="chromosome_name" value="1" /></dataset></query>'
		
	sudo sh /home/anthony/workspace/00MartConfigurator/scripts/martServiceServer.sh
	sudo sh /home/anthony/workspace/00MartConfigurator/scripts/martServiceServer.sh;firefox 'http://localhost:8082/MartService';firefox 'http://localhost:8082/MartService/martService?username=anonymous&password=p&format=xml&type=getDatasets&martName=ensembl&martVersion=-1';firefox 'http://localhost:8082/MartService/martService?username=anonymous&password=p&format=xml&type=query&query=<query formerVirtualSchema="default" processor="TSV" header="true" uniqueRows="false" count="false" datasetConfigVersion="0.7" limitStart="0" limitSize="100"><dataset name="hsapiens_gene_ensembl"><attribute name="ensembl_gene_id" /><attribute name="ensembl_transcript_id" /><filter name="chromosome_name" value="1" /></dataset></query>'
		
		sudo cp /home/anthony/workspace/00MartConfigurator/xml/martservice.xsd /home/anthony/workspace/MartService/xml/;sudo cp /home/anthony/workspace/00MartConfigurator/xml/web.xml /home/anthony/workspace/MartService/WEB-INF/;sudo cp /home/anthony/workspace/00MartConfigurator/files/portal.serial /home/anthony/workspace/MartService/files/;sudo cp /home/anthony/workspace/00MartConfigurator/web/index.jsp /home/anthony/workspace/MartService/;sudo cp /home/anthony/workspace/00MartConfigurator/lib/* /home/anthony/workspace/MartService/WEB-INF/lib/;cd /home/anthony/workspace/MartService;sudo rm -rf /var/lib/tomcat6/webapps/MartService.war /var/lib/tomcat6/webapps/MartService;sudo jar -cvf /var/lib/tomcat6/webapps/MartService.war .;sudo /etc/init.d/tomcat6 restart;firefox 'http://localhost:8082/MartService';firefox 'http://localhost:8082/MartService/martService?username=anonymous&password=p&format=xml&type=getDatasets&martName=ensembl&martVersion=-1';firefox 'http://localhost:8082/MartService/martService?username=anonymous&password=p&format=xml&type=query&query=<query formerVirtualSchema="default" processor="TSV" header="true" uniqueRows="false" count="false" datasetConfigVersion="0.7" limitStart="0" limitSize="100"><dataset name="hsapiens_gene_ensembl"><attribute name="ensembl_gene_id" /><attribute name="ensembl_transcript_id" /><filter name="chromosome_name" value="1" /></dataset></query>'
		
		// Move to BM-TEST
		scp /var/lib/tomcat6/webapps/MartService.war acros@$BMTEST:/home/acros/
		sshb
		sudo -u tomcat55 bash
		rm -rf /var/lib/tomcat5.5/webapps/MartService.war /var/lib/tomcat5.5/webapps/MartService;cp /home/acros/MartService.war /var/lib/tomcat5.5/webapps;
		exit
		sudo /etc/init.d/tomcat5.5 stop
		sudo /etc/init.d/tomcat5.5 start
		firefox 'http://bm-test.res.oicr.on.ca:9180/MartService/martService?username=anonymous&password=p&format=xml&type=getDatasets&martName=ensembl&martVersion=-1'
		http://bm-test.res.oicr.on.ca:9180/MartService		
		http://bm-test.res.oicr.on.ca:9180/MartService/martService?username=anonymous&password=p&format=xml&type=getRegistry
		http://bm-test.res.oicr.on.ca:9180/MartService/martService?username=anonymous&password=p&format=xml&type=getDatasets&martName=ensembl&martVersion=-1
		http://bm-test.res.oicr.on.ca:9180/MartService/martService?username=anonymous&password=p&format=xml&type=query&query=<query formerVirtualSchema="default" processor="TSV" header="true" uniqueRows="false" count="false" datasetConfigVersion="0.7" limitStart="0" limitSize="100"><dataset name="hsapiens_gene_ensembl"><attribute name="ensembl_gene_id" /><attribute name="ensembl_transcript_id" /><filter name="chromosome_name" value="1" /></dataset></query>
 */

public class MartService extends HttpServlet {
	private static final long serialVersionUID = -6182537087217047287L;

	public static final String SERVER_PATH = "http://localhost:8082/MartService/";
	public static final String XSD_FILE_URL = 
		//SERVER_PATH + MartRemoteConstants.XML_FILES_FOLDER_NAME + MyUtils.FILE_SEPARATOR + MartRemoteConstants.XSD_FILE_NAME;
		MyConstants.FILE_SYSTEM_PROTOCOL + "/var/lib/tomcat5.5/webapps/MartService" + MyUtils.FILE_SEPARATOR + MartRemoteConstants.XML_FILES_FOLDER_NAME + MyUtils.FILE_SEPARATOR + MartRemoteConstants.XSD_FILE_NAME;
	
	public static final String PORTAL_SERIAL_FILE_URL = 
		//SERVER_PATH + MartRemoteConstants.ADDITIONAL_FILES_FOLDER_NAME + MyUtils.FILE_SEPARATOR + MartRemoteConstants.PORTAL_SERIAL_FILE_NAME;
		MyConstants.FILE_SYSTEM_PROTOCOL + "/var/lib/tomcat5.5/webapps/MartService" + MyUtils.FILE_SEPARATOR + MartRemoteConstants.ADDITIONAL_FILES_FOLDER_NAME + MyUtils.FILE_SEPARATOR + MartRemoteConstants.PORTAL_SERIAL_FILE_NAME;
	
	private MartApi martServiceApi = null;
	
	@Override
	public void init() throws ServletException {
		try {
			initialize();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			exception(e);
		} catch (JDOMException e) {
			e.printStackTrace();
			exception(e);
		} catch (IOException e) {
			e.printStackTrace();
			exception(e);
		} catch (TechnicalException e) {
			e.printStackTrace();
			exception(e);
		}
	}
	
	public void initialize() throws FileNotFoundException, JDOMException, IOException, TechnicalException {
		this.martServiceApi = new MartApi(true, XSD_FILE_URL, PORTAL_SERIAL_FILE_URL);
	}

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		this.doGet(req, resp);
	}

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {		

		try {
			String username = req.getParameter("username");
			String password = req.getParameter("password");
			if (null==username || "".equals(username)) {
				username = "anonymous";
				if (null==password || "".equals(password)) {
					password = "";
				}
			}
			String formatString = req.getParameter("format");
			MartServiceFormat format = MartServiceFormat.getFormat(formatString);
			String type = req.getParameter("type");
			String martName = req.getParameter("martName");
			String martVersionString = req.getParameter("martVersion");		
			//Integer martVersion = martVersionString!=null ? Integer.valueOf(martVersionString) : null;
			String datasetName = req.getParameter("datasetName");
			String query = req.getParameter("query");
			String partitionFilter = req.getParameter("partitionFilter");
			
			// Run appropriate method
			MartServiceRequest martServiceRequest = null;
			MartServiceResponse martServiceResult = null;
			PrintWriter printWriter = resp.getWriter();
			StringBuffer errorMessage = new StringBuffer();
			MartRemoteEnum remoteAccessEnum = MartRemoteEnum.getEnumFromIdentifier(type);
			if (MartRemoteEnum.GET_REGISTRY.equals(remoteAccessEnum)) {
				martServiceRequest = this.martServiceApi.prepareGetRegistry(username, password, format);
				martServiceResult = this.martServiceApi.executeGetRegistry(martServiceRequest);
			} else if (MartRemoteEnum.GET_DATASETS.equals(remoteAccessEnum)) {
				martServiceRequest = this.martServiceApi.prepareGetDatasets(username, password, format, martName, martVersionString);
				martServiceResult = this.martServiceApi.executeGetDatasets(martServiceRequest);
			} else if (MartRemoteEnum.GET_ROOT_CONTAINER.equals(remoteAccessEnum)) {
				martServiceRequest = this.martServiceApi.prepareGetRootContainer(username, password, format, datasetName, partitionFilter);
				martServiceResult = this.martServiceApi.executeGetRootContainer(martServiceRequest);
			}/*else if (MartRemoteEnum.GET_ATTRIBUTES.equals(remoteAccessEnum)) {
				martServiceRequest = this.martServiceApi.prepareGetAttributes(username, password, format, datasetName, partitionFilter);
				martServiceResult = this.martServiceApi.executeGetAttributes(martServiceRequest);
			} else if (MartRemoteEnum.GET_FILTERS.equals(remoteAccessEnum)) {
				martServiceRequest = this.martServiceApi.prepareGetFilters(username, password, format, datasetName, partitionFilter);
				martServiceResult = this.martServiceApi.executeGetFilters(martServiceRequest);
			} */else if (MartRemoteEnum.GET_LINKS.equals(remoteAccessEnum)) {
				martServiceRequest = this.martServiceApi.prepareGetLinks(username, password, format, datasetName);
				martServiceResult = this.martServiceApi.executeGetLinks(martServiceRequest);
			} else if (MartRemoteEnum.QUERY.equals(remoteAccessEnum)) {
				QueryRequest queryRequest = this.martServiceApi.prepareQuery(username, password, format, query);
				if (!queryRequest.isValid()) {
					errorMessage.append("Invalid query request " + 
							MartRemoteUtils.getXmlDocumentString(queryRequest.getQueryDocument()) + MyUtils.LINE_SEPARATOR);
					this.martServiceApi.writeError(errorMessage, printWriter);
					return;
				}
				martServiceResult = this.martServiceApi.executeQuery(queryRequest);
			} else {
				errorMessage.append("Unknown type " + type + MyUtils.LINE_SEPARATOR);
				this.martServiceApi.writeError(errorMessage, printWriter);
				return;
			}
			this.martServiceApi.processMartServiceResult(martServiceResult, printWriter);
		} catch (NumberFormatException e) {
			e.printStackTrace();
			exception(e);
		} catch (JDOMException e) {
			e.printStackTrace();
			exception(e);
		} catch (IOException e) {
			e.printStackTrace();
			exception(e);
		} catch (TechnicalException e) {
			e.printStackTrace();
			exception(e);
		} catch (FunctionalException e) {
			e.printStackTrace();
			exception(e);
		}
	}

	// Miscellaneous
	private void exception(Exception e) {
		exception(e.getMessage());
	}
	private void exception(String message) {
		throw new NullPointerException(message);
	}
}