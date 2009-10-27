package org.biomart.transformation.tmp.backwardCompatibility;

public class BackwardCompatibilityTemplate extends BackwardCompatibility {

	public BackwardCompatibilityTemplate(String virtualSchemaName, String datasetName, String xmlDocumentFilePathAndName) throws Exception {
		super(virtualSchemaName, null, null, null, datasetName, xmlDocumentFilePathAndName);
	}
}
