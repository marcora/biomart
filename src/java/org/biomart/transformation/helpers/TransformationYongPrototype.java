package org.biomart.transformation.helpers;
//package org.biomart.configurator.utils;


import java.io.File;
import java.util.Map;

import org.biomart.common.general.exceptions.FunctionalException;
import org.biomart.common.general.exceptions.TechnicalException;
import org.biomart.common.general.utils.MyUtils;
import org.biomart.objects.helpers.DatabaseParameter;
import org.biomart.objects.objects.MartRegistry;
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
		test();
	}

	public static void test() {
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
		
		/*MartRegistry martRegistry = wrappedRebuildCentralPortalRegistry();
		System.out.println(martRegistry.getLocationList().size());*/
	}
	public static Document wrappedTransform(MartServiceIdentifier initialHost, String martName, String datasetName)
	throws TechnicalException, FunctionalException {
		
		File transformationsGeneralOutputTemporaryFolder = createTmpFolders(initialHost.generateIdentifier());
		HostAndVirtualSchema hostAndVirtualSchema = TransformationMain.computeHostAndVirtualSchema(martName);
		Transformation transformation = TransformationMain.transform(true, initialHost, 
				hostAndVirtualSchema.getMartServiceIdentifier(), transformationsGeneralOutputTemporaryFolder.getAbsolutePath(), hostAndVirtualSchema.getVirtualSchema(), datasetName);
		boolean b = deleteDir(transformationsGeneralOutputTemporaryFolder);
		MyUtils.checkStatusProgram(b && !transformationsGeneralOutputTemporaryFolder.exists());
		return transformation.getTransformedDocument();
	}
	public static MartRegistry wrappedTransformObject(MartServiceIdentifier initialHost, String martName, String datasetName)
	throws TechnicalException, FunctionalException {
		
		File transformationsGeneralOutputTemporaryFolder = createTmpFolders(initialHost.generateIdentifier());
		HostAndVirtualSchema hostAndVirtualSchema = TransformationMain.computeHostAndVirtualSchema(martName);
		Transformation transformation = TransformationMain.transform(true, initialHost, 
				hostAndVirtualSchema.getMartServiceIdentifier(), transformationsGeneralOutputTemporaryFolder.getAbsolutePath(), hostAndVirtualSchema.getVirtualSchema(), datasetName);
		boolean b = deleteDir(transformationsGeneralOutputTemporaryFolder);
		MyUtils.checkStatusProgram(b && !transformationsGeneralOutputTemporaryFolder.exists());
		return transformation.getMartRegistry();
	}

	public static MartRegistry wrappedRebuildCentralPortalRegistry() throws TechnicalException, FunctionalException {
		File transformationsGeneralOutputTemporaryFolder = createTmpFolders("portal");
		MartRegistry martRegistry = TransformationMain.rebuildCentralPortalRegistry(transformationsGeneralOutputTemporaryFolder.getAbsolutePath(), false);
		boolean b = deleteDir(transformationsGeneralOutputTemporaryFolder);
		MyUtils.checkStatusProgram(b && !transformationsGeneralOutputTemporaryFolder.exists());
		return martRegistry;
	}
	public static File createTmpFolders(String identifier) {
		String fs = System.getProperty("file.separator");
		File file = new File("." + fs + identifier + fs);
		file.mkdirs();
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
