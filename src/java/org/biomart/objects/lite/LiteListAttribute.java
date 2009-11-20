package org.biomart.objects.lite;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.biomart.common.general.exceptions.FunctionalException;
import org.biomart.martRemote.objects.request.MartRemoteRequest;

public class LiteListAttribute extends ListLiteMartConfiguratorObject implements Serializable {

	private static final long serialVersionUID = 6245776134408584897L;

	public LiteListAttribute(MartRemoteRequest martRemoteRequest) {
		super(martRemoteRequest);
	}
	
	/**
	 * Only used to populate the objects, should not be used once locked
	 */
	public void addLiteAttribute(LiteAttribute liteAttribute) throws FunctionalException {
		super.checkLock();
		super.liteMartConfiguratorObjectList.add(liteAttribute);
	}
	
	@SuppressWarnings("unchecked")
	public List<LiteAttribute> getLiteAttributeList() {
		return new ArrayList<LiteAttribute>((List<LiteAttribute>)super.getLiteMartConfiguratorObjectList());
	}

	@Override
	public String toString() {
		return "liteAttributeList = " + super.toString();
	}
}
