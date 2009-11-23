package org.biomart.objects.lite;

import java.io.Serializable;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;

import net.sf.json.JSONObject;

import org.biomart.common.general.exceptions.FunctionalException;
import org.biomart.common.general.exceptions.TechnicalException;
import org.biomart.martRemote.Jsoml;
import org.biomart.martRemote.objects.request.MartRemoteRequest;
import org.jdom.Document;

public class QueryResult extends MartRemoteObject implements Serializable {

	private static final long serialVersionUID = -5390698374744515564L;

	private static final String XML_ELEMENT_NAME = "query";
	
	private List<String> headers = null;
	private List<List<String>> data = null;
	
	public QueryResult(MartRemoteRequest martRemoteRequest) {
		super(martRemoteRequest, XML_ELEMENT_NAME);
		this.data = new ArrayList<List<String>>();
		this.headers = null;
	}

	public void setHeaders(List<String> headers) {
		this.headers = headers;
	}
	public void setData(List<List<String>> data) {
		this.data = data;
	}

	public List<String> getHeaders() {
		return new ArrayList<String>(headers);
	}
	public List<List<String>> getData() {
		List<List<String>> data = new ArrayList<List<String>>();
		for (List<String> list : this.data) {	// provide a copy of the list of list
			data.add(new ArrayList<String>(list));
		}
		return data;
	}

	@Override
	public JSONObject getJsonObject() throws TechnicalException,
			FunctionalException {
		return super.getJsonObject();
	}

	@Override
	public JSONObject getJsonObject(boolean debug, Writer printWriter)
			throws TechnicalException, FunctionalException {
		return super.getJsonObject(debug, printWriter);
	}

	@Override
	public Document getXmlDocument() throws TechnicalException,
			FunctionalException {
		return super.getXmlDocument();
	}

	@Override
	public Document getXmlDocument(boolean debug, Writer printWriter)
			throws TechnicalException, FunctionalException {
		return super.getXmlDocument(debug, printWriter);
	}
	
	protected Jsoml generateExchangeFormat(boolean xml, Jsoml root) throws FunctionalException {	// unless overriden
		if (this.headers!=null) {
			Jsoml jsomlHeaders = new Jsoml(xml, "headers");
			for (String header : this.headers) {
				Jsoml jsomlHeader = new Jsoml(xml, "header");
				jsomlHeader.setText(header);
				jsomlHeaders.addContent(jsomlHeader);
			}
			root.addContent(jsomlHeaders);
		}
		
		Jsoml jsomlRows = new Jsoml(xml, "rows");
		for (List<String> row : this.data) {
			
			Jsoml jsomlRow = new Jsoml(xml, "row");
			for (String value : row) {
				Jsoml jsomlValue = new Jsoml(xml, "value");
				jsomlValue.setText(value);
				jsomlRow.addContent(jsomlValue);
			}
			jsomlRows.addContent(jsomlRow);
		}
		root.addContent(jsomlRows);
		
		return root;
	}
}
