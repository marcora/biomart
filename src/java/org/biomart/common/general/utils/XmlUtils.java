package org.biomart.common.general.utils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;

import org.biomart.common.general.exceptions.TechnicalException;
import org.jdom.Document;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;

public class XmlUtils {

	public static String getXmlDocumentString(Document document) throws TechnicalException {
		XMLOutputter prettyFormat = new XMLOutputter(Format.getPrettyFormat());
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		String xmlDocumentString = null;
		try {
			prettyFormat.output(document, baos);
			xmlDocumentString = baos.toString("UTF-8");
		} catch (UnsupportedEncodingException e) {
			throw new TechnicalException(e);
		} catch (IOException e) {
			throw new TechnicalException(e);
		}
		return xmlDocumentString;
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

}
