package org.biomart.objects.lite;

import java.io.Serializable;
import java.io.Writer;

import net.sf.json.JSONObject;

import org.biomart.common.general.exceptions.FunctionalException;
import org.biomart.common.general.exceptions.TechnicalException;
import org.biomart.martRemote.objects.request.MartRemoteRequest;
import org.jdom.Document;

public abstract class MartRemoteObject implements Serializable {

	private static final long serialVersionUID = -7771063236020188948L;

	protected MartRemoteRequest martRemoteRequest = null;
	protected String xmlElementName = null;
	
	protected MartRemoteObject(MartRemoteRequest martRemoteRequest, String xmlElementName) {
		this.martRemoteRequest = martRemoteRequest;
		this.xmlElementName = xmlElementName;
	}
	
	protected abstract Document getXmlDocument() throws TechnicalException, FunctionalException;
	protected abstract Document getXmlDocument(boolean debug, Writer printWriter) throws TechnicalException, FunctionalException;
	protected abstract JSONObject getJsonObject() throws TechnicalException, FunctionalException;
	protected abstract JSONObject getJsonObject(boolean debug, Writer printWriter) throws TechnicalException, FunctionalException;
}
