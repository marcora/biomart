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

public class ListLiteMartConfiguratorObject extends LiteMartConfiguratorObject implements Serializable {

	private static final long serialVersionUID = -4022534573758673228L;

	protected String LOCK_VIOLATION_ERROR_MESSAGE = "Invalid operation: objects are already populated";
	
	protected List<LiteSimpleMartConfiguratorObject> liteMartConfiguratorObjectList = null;
	protected Boolean lock = null;
	
	public ListLiteMartConfiguratorObject(MartRemoteRequest martRemoteRequest) {
		super(martRemoteRequest);
		
		this.liteMartConfiguratorObjectList = new ArrayList<LiteSimpleMartConfiguratorObject>();
		this.lock = false;
	}
	
	/**
	 * In order to "lock" the object once populated (becomes read-only)
	 */
	public void lock() {
		this.lock = true;
	}
	protected void checkLock() throws FunctionalException {
		if (this.lock) {
			throw new FunctionalException(this.LOCK_VIOLATION_ERROR_MESSAGE);
		}
	}
	
	protected List<? extends LiteSimpleMartConfiguratorObject> getLiteMartConfiguratorObjectList() {
		return (List<? extends LiteSimpleMartConfiguratorObject>)this.liteMartConfiguratorObjectList;
	}

	@Override
	public String toString() {
		StringBuffer stringBuffer = new StringBuffer();
		for (int i = 0; i < liteMartConfiguratorObjectList.size(); i++) {
			stringBuffer.append((i==0 ? "" : ",") + liteMartConfiguratorObjectList.get(i));
		}
		return stringBuffer.toString();
	}
	
	
	@Override
	public Document getXmlDocument() throws TechnicalException, FunctionalException {
		return super.getXmlDocument();
	}
	@Override
	public Document getXmlDocument(boolean debug, Writer printWriter) throws TechnicalException, FunctionalException {
		return super.getXmlDocument(debug, printWriter);
	}
	@Override
	public JSONObject getJsonObject() throws TechnicalException, FunctionalException {
		return super.getJsonObject();
	}
	@Override
	public JSONObject getJsonObject(boolean debug, Writer printWriter) throws TechnicalException, FunctionalException {
		return super.getJsonObject(debug, printWriter);
	}
	
	@Override
	protected Jsoml generateExchangeFormat(boolean xml, Jsoml root) throws FunctionalException {	// unless overriden
		for (LiteSimpleMartConfiguratorObject liteSimpleMartConfiguratorObject : this.liteMartConfiguratorObjectList) {
			Jsoml jsoml = liteSimpleMartConfiguratorObject.generateExchangeFormat(xml);
			root.addContent(jsoml);
		}
		return root;
	}
}
