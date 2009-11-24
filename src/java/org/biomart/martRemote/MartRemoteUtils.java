package org.biomart.martRemote;


import org.biomart.common.general.exceptions.TechnicalException;
import org.biomart.common.general.utils.MyUtils;
import org.biomart.common.general.utils.XmlUtils;
import org.jdom.Document;
import org.jdom.Element;

public class MartRemoteUtils {
	public static String buildRequestName(String identifier) {
		return identifier + MartRemoteConstants.REQUEST_SUFFIX;
	}
	public static String buildResponseName(String identifier) {
		return identifier + MartRemoteConstants.RESPONSE_SUFFIX;
	}
	public static Document createNewMartRemoteXmlDocument(XmlParameters xmlParameters, String rootName) {
		Element root = new org.jdom.Element(rootName, xmlParameters.getMartServiceNamespace());
		root.addNamespaceDeclaration(xmlParameters.getXsiNamespace());
		root.setAttribute("schemaLocation", 
				xmlParameters.getMartServiceNamespace().getURI() + " " + xmlParameters.getXsdFile(), xmlParameters.getXsiNamespace());
		return new Document(root);
	}	

	public static boolean validateXml(Document document, StringBuffer errorMessage) throws TechnicalException {
		String errorValidation = XmlUtils.validationXml(document);
		if (null!=errorValidation) {
			createErrorMessage(document, errorValidation, errorMessage);
			return false;
		}
		return true;
	}
	
	public static StringBuffer createErrorMessage(Document document, String errorValidation, StringBuffer errorMessage) throws TechnicalException {
		errorMessage.append(MyUtils.LINE_SEPARATOR + XmlUtils.getXmlDocumentString(document) + 
				MyUtils.LINE_SEPARATOR + MyUtils.LINE_SEPARATOR + errorValidation);
		return errorMessage;
	}
}
