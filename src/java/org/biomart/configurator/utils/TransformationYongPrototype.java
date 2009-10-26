package org.biomart.configurator.utils;

import general.exceptions.FunctionalException;
import general.exceptions.TechnicalException;
import general.utils.MyUtils;

import java.io.File;
import java.util.Map;

import martConfigurator.helpers.DatabaseParameter;
import martConfigurator.transformation.Transformation;
import martConfigurator.transformation.TransformationMain;
import martConfigurator.transformation.helpers.HostAndVirtualSchema;
import martConfigurator.transformation.helpers.MartServiceIdentifier;
import martConfigurator.transformation.helpers.TransformationConstants;
import martService.Configuration;

import org.jdom.Document;

public class TransformationYongPrototype {

	// Constants
	public static final DatabaseParameter DATABASE_PARAMETER = null;
	public static final String[] DATABASE_NAMES = null;
	public static final String DEFAULT_DATA_FOLDER_NAME = "DataFolder";
	public static final String TRANSFORMATION_PROPERTY_FILE_NAME = "transformation.prop";
		
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

		TransformationMain.PROPERTIES_FILE_FOLDER_PATH_AND_NAME = file.getAbsolutePath() + fs;
		TransformationMain.TRANSFORMATIONS_GENERAL_OUTPUT = file.getAbsolutePath() + fs;

		StringBuffer stringBuffer = new StringBuffer();
		stringBuffer.append(TransformationConstants.PROPERTY_ERROR_FILE_PATH_AND_NAME + "=" +
				file.getAbsolutePath() + fs+"Errors" + MyUtils.LINE_SEPARATOR);
		stringBuffer.append(TransformationConstants.PROPERTY_SERVER + "=" + initialHost.getServer() + MyUtils.LINE_SEPARATOR);
		stringBuffer.append(TransformationConstants.PROPERTY_PATH_TO_MART_SERVICE + "=" + initialHost.getPath() + MyUtils.LINE_SEPARATOR);
		MyUtils.writeFile(file.getAbsolutePath() + fs + TRANSFORMATION_PROPERTY_FILE_NAME, stringBuffer.toString());
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
