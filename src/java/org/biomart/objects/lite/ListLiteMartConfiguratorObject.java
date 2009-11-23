package org.biomart.objects.lite;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.biomart.common.general.exceptions.FunctionalException;
import org.biomart.martRemote.Jsoml;
import org.biomart.martRemote.objects.request.MartRemoteRequest;

public class ListLiteMartConfiguratorObject extends MartRemoteWrapper implements Serializable {

	private static final long serialVersionUID = -4022534573758673228L;

	protected String LOCK_VIOLATION_ERROR_MESSAGE = "Invalid operation: objects are already populated";
	
	protected List<LiteMartConfiguratorObject> liteMartConfiguratorObjectList = null;
	protected Boolean lock = null;
	
	public ListLiteMartConfiguratorObject(MartRemoteRequest martRemoteRequest) {
		super(martRemoteRequest);
		
		this.liteMartConfiguratorObjectList = new ArrayList<LiteMartConfiguratorObject>();
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
	
	protected List<? extends LiteMartConfiguratorObject> getLiteMartConfiguratorObjectList() {
		return (List<? extends LiteMartConfiguratorObject>)this.liteMartConfiguratorObjectList;
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
	protected Jsoml generateExchangeFormat(boolean xml, Jsoml root) throws FunctionalException {	// unless overriden
		for (LiteMartConfiguratorObject liteMartConfiguratorObject : this.liteMartConfiguratorObjectList) {
			Jsoml jsoml = liteMartConfiguratorObject.generateExchangeFormat(xml);
			root.addContent(jsoml);
		}
		return root;
	}
}
