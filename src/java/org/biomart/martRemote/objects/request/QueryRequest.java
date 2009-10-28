package org.biomart.martRemote.objects.request;

import java.io.IOException;
import java.io.StringReader;


import org.biomart.common.general.exceptions.TechnicalException;
import org.biomart.martRemote.enums.MartServiceFormat;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.Namespace;
import org.jdom.input.SAXBuilder;


public class QueryRequest extends MartServiceRequest {
	
	private String queryString = null;
	private Document queryDocument = null;
	private Query query = null;
	
	public QueryRequest(String requestName, Namespace martServiceNamespace, Namespace xsiNamespace, String xsdFile,
			String username, String password, MartServiceFormat format, String queryString) {
		super(requestName, martServiceNamespace, xsiNamespace, xsdFile, username, password, format);
		this.queryString = queryString;
	}
	
	public Query getQuery() {
		return query;
	}

	public Document getQueryDocument() {
		return queryDocument;
	}
	
	/**
	 * Always check that queryRequest is valid afterwards (if it validates)
	 */
	public boolean rebuildQueryDocument() throws TechnicalException {
		SAXBuilder builder = new SAXBuilder();
		Document queryDocumentTmp = null;
		try {
			queryDocumentTmp = builder.build(new StringReader(this.queryString));
		} catch (JDOMException e) {
			throw new TechnicalException(e);
		} catch (IOException e) {
			throw new TechnicalException(e);
		}
		Element cloneRoot = (Element)queryDocumentTmp.getRootElement().clone();
		this.queryDocument = createNewResponseXmlDocument(super.actionName);
		Element rootElement = this.queryDocument.getRootElement();
		rootElement.addContent(cloneRoot);
		return validateXml(this.queryDocument);	// Validation with XSD
	}
	
	@Override
	public void buildObjects() {
		this.query = Query.fromXml(this.queryDocument);
	}
}
