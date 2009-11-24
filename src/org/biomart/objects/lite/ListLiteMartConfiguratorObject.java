package org.biomart.objects.lite;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.biomart.common.general.exceptions.FunctionalException;
import org.biomart.martRemote.Jsoml;
import org.biomart.martRemote.objects.request.MartRemoteRequest;

public class ListLiteMartConfiguratorObject extends MartRemoteWrapper implements Serializable {

	private static final long serialVersionUID = -4022534573758673228L;
	
	protected List<LiteMartConfiguratorObject> liteMartConfiguratorObjectList = null;
	
	public ListLiteMartConfiguratorObject(MartRemoteRequest martRemoteRequest) {
		super(martRemoteRequest);
		
		this.liteMartConfiguratorObjectList = new ArrayList<LiteMartConfiguratorObject>();
	}
	
	
	protected List<? extends LiteMartConfiguratorObject> getLiteMartConfiguratorObjectList() {
		return (List<? extends LiteMartConfiguratorObject>)this.liteMartConfiguratorObjectList;
	}
	protected void addliteMartConfiguratorObject(LiteMartConfiguratorObject liteMartConfiguratorObject) throws FunctionalException {
		super.checkLock();
		this.liteMartConfiguratorObjectList.add(liteMartConfiguratorObject);
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
