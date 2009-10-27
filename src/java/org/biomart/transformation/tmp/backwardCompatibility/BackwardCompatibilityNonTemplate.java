package org.biomart.transformation.tmp.backwardCompatibility;


import org.biomart.old.martService.objects.DatasetInMart;
import org.biomart.old.martService.objects.MartInVirtualSchema;
import org.jdom.Document;

public class BackwardCompatibilityNonTemplate extends BackwardCompatibility {
	public BackwardCompatibilityNonTemplate(String virtualSchemaName, String datasetName, String xmlDocumentFilePathAndName)  throws Exception {
		super(virtualSchemaName, null, null, null, datasetName, xmlDocumentFilePathAndName);
	}
	
	public BackwardCompatibilityNonTemplate(String virtualSchemaName, MartInVirtualSchema martInVirtualSchema, 
			DatasetInMart datasetInMart, Document xmlDocument) throws Exception  {
		super(virtualSchemaName, martInVirtualSchema, datasetInMart, xmlDocument, datasetInMart.datasetName, null);
	}
}
