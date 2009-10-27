package org.biomart.martRemote;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.StringReader;
import java.util.Properties;

import org.jdom.Document;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;


public class MartRemoteUtils {

	public static String getXmlDocumentString(Document document) throws IOException {
		XMLOutputter prettyFormat = new XMLOutputter(Format.getPrettyFormat());
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		prettyFormat.output(document, baos);
		return baos.toString();
	}
	
	// Xml validation
	public static String validationXml(Document document) {
		SAXBuilder validator = new SAXBuilder("org.apache.xerces.parsers.SAXParser", true);
		validator.setFeature("http://apache.org/xml/features/validation/schema", true);

		String errorMessage = null;
		try {
			XMLOutputter fmt = new XMLOutputter(Format.getCompactFormat());
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			fmt.output(document, baos);
			StringReader stringReader = new StringReader(baos.toString());
			validator.build(stringReader);
			
			System.out.println("successful validation!");
			System.out.println();
		} catch (JDOMException e) {
			e.printStackTrace();
			errorMessage = e.getMessage();
		} catch (IOException e) {
			e.printStackTrace();
			errorMessage = e.getMessage();
		}
		return errorMessage;
	}

	public static String getProperty(String propertyName) throws FileNotFoundException, IOException {
		Properties properties = new Properties();
		properties.load(new FileInputStream(MartRemoteConstants.QUERY_TEST_PROPERTIES_FILE_PATH_AND_NAME));
		return properties.getProperty(propertyName);
	}
	
	public static String buildRequestName(String identifier) {
		return identifier + MartRemoteConstants.REQUEST_SUFFIX;
	}
	public static String buildResponseName(String identifier) {
		return identifier + MartRemoteConstants.RESPONSE_SUFFIX;
	}
}
