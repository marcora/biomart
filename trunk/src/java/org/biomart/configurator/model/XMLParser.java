package org.biomart.configurator.model;

import java.io.File;
import java.io.IOException;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.biomart.configurator.controller.Start;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import java.util.logging.Logger;


public class XMLParser {

    public Logger log = Logger.getLogger(Start.class.getName());

	public Document doc = null;

	public XMLParser(String FileName){
		try
		{
			doc = this.parserXML(new File(FileName));			
 		
		}
  		catch(Exception error)
 		{
 			error.printStackTrace();
		}
	}
	
	public Document parserXML(File file) throws SAXException, IOException, ParserConfigurationException
	{
		Document docObj = null;
		try {
			docObj = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(file);
	
		}catch(ParserConfigurationException pce) {
			pce.printStackTrace();
		}catch(SAXException se) {
			se.printStackTrace();
		}catch(IOException ioe) {
			ioe.printStackTrace();
		}
		
		return docObj; 
	}

	public Document getDoc() 
	{
		return this.doc;
	}
	public Element getDocRoot() 
	{
		return this.doc.getDocumentElement();
	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}
