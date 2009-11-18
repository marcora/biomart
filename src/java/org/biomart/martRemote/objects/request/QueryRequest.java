package org.biomart.martRemote.objects.request;

import java.io.IOException;
import java.io.StringReader;

import org.biomart.common.general.exceptions.TechnicalException;
import org.biomart.martRemote.MartRemoteUtils;
import org.biomart.martRemote.XmlParameters;
import org.biomart.martRemote.enums.MartRemoteEnum;
import org.biomart.martRemote.enums.MartServiceFormat;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;


public class QueryRequest extends MartRemoteRequest {
	
	private String queryString = null;
	private Document queryDocument = null;
	private Query query = null;
	
	public QueryRequest(XmlParameters xmlParameters, String username, String password, MartServiceFormat format, String queryString) {
		super(MartRemoteEnum.QUERY, xmlParameters, username, password, format);
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
	public void rebuildQueryDocument() throws TechnicalException {
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
		this.queryDocument = MartRemoteUtils.createNewMartRemoteXmlDocument(super.xmlParameters, type.getRequestName());
		Element rootElement = this.queryDocument.getRootElement();
		rootElement.addContent(cloneRoot);
		
		if (this.xmlParameters.getValidate()) {		// valide only if required
			MartRemoteUtils.validateXml(this.queryDocument, super.errorMessage);	// Validation with XSD
		}
	}
	
	@Override
	public void buildObjects() {
		this.query = Query.fromXml(this.queryDocument);
	}
}
