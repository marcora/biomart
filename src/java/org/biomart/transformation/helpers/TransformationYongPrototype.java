package org.biomart.transformation.helpers;
//package org.biomart.configurator.utils;


import java.io.File;
import java.util.Map;


import org.biomart.common.general.exceptions.FunctionalException;
import org.biomart.common.general.exceptions.TechnicalException;
import org.biomart.common.general.utils.MyUtils;
import org.biomart.objects.helpers.DatabaseParameter;
import org.biomart.old.martService.Configuration;
import org.biomart.transformation.Transformation;
import org.biomart.transformation.TransformationMain;
import org.jdom.Document;

public class TransformationYongPrototype {

	// Constants
	public static final DatabaseParameter DATABASE_PARAMETER = null;
	public static final String[] DATABASE_NAMES = null;
	public static final String DEFAULT_DATA_FOLDER_NAME = "DataFolder";

	public static void main(String[] args) {

		MyUtils.CHECK = true;
		MyUtils.EXCEPTION = true;
		MyUtils.EXIT_PROGRAM = false;
		
		try {
			
			String server = "www.biomart.org";
			String port = "80";
			String pathToMartService = "/biomart/martservice";
			String martName = "ensembl";
			String datasetName = "hsapiens_gene_ensembl";
			Configuration configuration = 
				null;
				//TransformationMain.fetchConfiguration(new MartServiceIdentifier(server, port, pathToMartService).formatMartServiceUrl());
				
			MartServiceIdentifier initialHost = new MartServiceIdentifier(server, port, pathToMartService);	
			TransformationMain.fetchWebServiceConfigurationMap(initialHost, configuration);
			Map<String, Configuration> webServiceConfigurationMap = TransformationMain.getWebServiceConfigurationMap();
			Configuration initialConfiguration = TransformationMain.getInitialConfiguration();
			MyUtils.checkStatusProgram(null!=webServiceConfigurationMap && webServiceConfigurationMap.size()>=1 && initialConfiguration!=null);
			
			Document transformedDocument = wrappedTransform(initialHost, martName, datasetName);
			System.out.println(transformedDocument.getRootElement().getName());
			
		} catch (TechnicalException e) {
			e.printStackTrace();
		} catch (FunctionalException e) {
			e.printStackTrace();
		}
	}
		
	public static Document wrappedTransform(MartServiceIdentifier initialHost, String martName, String datasetName)
	throws TechnicalException, FunctionalException {
		
		File file = createTmpFolders(initialHost);
		HostAndVirtualSchema hostAndVirtualSchema = TransformationMain.computeHostAndVirtualSchema(martName);
		Transformation transformation = TransformationMain.transform(true, null, initialHost, 
				hostAndVirtualSchema.getMartServiceIdentifier(), hostAndVirtualSchema.getVirtualSchema(), datasetName);
		boolean b = deleteDir(file);
		MyUtils.checkStatusProgram(b && !file.exists());
		return transformation.getTransformedDocument();
	}
	public static File createTmpFolders(MartServiceIdentifier initialHost) {

		String identifier = initialHost.generateIdentifier();
		String fs = System.getProperty("file.separator");
		File file = new File("." + fs + identifier + fs);
		file.mkdirs();

		/*TransformationMain.PROPERTIES_FILE_FOLDER_PATH_AND_NAME = file.getAbsolutePath() + fs;
		TransformationMain.TRANSFORMATIONS_GENERAL_OUTPUT = file.getAbsolutePath() + fs;

		StringBuffer stringBuffer = new StringBuffer();
		stringBuffer.append(TransformationConstants.PROPERTY_ERROR_FILE_PATH_AND_NAME + "=" +
				file.getAbsolutePath() + fs+"Errors" + MyUtils.LINE_SEPARATOR);
		stringBuffer.append(TransformationConstants.PROPERTY_SERVER + "=" + initialHost.getServer() + MyUtils.LINE_SEPARATOR);
		stringBuffer.append(TransformationConstants.PROPERTY_PATH_TO_MART_SERVICE + "=" + initialHost.getPath() + MyUtils.LINE_SEPARATOR);
		MyUtils.writeFile(file.getAbsolutePath() + fs + TRANSFORMATION_PROPERTY_FILE_NAME, stringBuffer.toString());*/
		return file;

	}

    public static boolean deleteDir(File dir) {
        if (dir.isDirectory()) {
            String[] children = dir.list();
            for (int i=0; i<children.length; i++) {
                boolean success = deleteDir(new File(dir, children[i]));
                if (!success) {
                    return false;
                }
            }
        }
        // The directory is now empty so delete it
        return dir.delete();
    }
}
