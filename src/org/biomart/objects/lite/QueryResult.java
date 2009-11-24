package org.biomart.objects.lite;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.biomart.common.general.exceptions.FunctionalException;
import org.biomart.martRemote.Jsoml;
import org.biomart.martRemote.objects.request.MartRemoteRequest;

public class QueryResult extends MartRemoteWrapper implements Serializable {

	private static final long serialVersionUID = -5390698374744515564L;
	
	private List<String> headers = null;
	private List<List<String>> data = null;
	
	public QueryResult(MartRemoteRequest martRemoteRequest) {
		super(martRemoteRequest);
		this.data = new ArrayList<List<String>>();
		this.headers = null;
	}

	public void setHeaders(List<String> headers) throws FunctionalException {
		super.checkLock();
		this.headers = headers;
	}
	public void setData(List<List<String>> data) throws FunctionalException {
		super.checkLock();
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
