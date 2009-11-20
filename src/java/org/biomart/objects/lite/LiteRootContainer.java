package org.biomart.objects.lite;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.biomart.common.general.exceptions.FunctionalException;
import org.biomart.martRemote.objects.request.MartRemoteRequest;

public class LiteRootContainer extends ListLiteMartConfiguratorObject implements Serializable {

	private static final long serialVersionUID = -1730316653773786788L;

	public LiteRootContainer(MartRemoteRequest martRemoteRequest) {
		super(martRemoteRequest);
	}
	
	/**
	 * Only used to populate the objects, should not be used once locked
	 */
	public void setLiteRootContainer(LiteContainer liteRootContainer) throws FunctionalException {
		super.checkLock();
		super.liteMartConfiguratorObjectList = new ArrayList<LiteSimpleMartConfiguratorObject>(
			Arrays.asList(new LiteSimpleMartConfiguratorObject[] {liteRootContainer}));
	}
	
	public LiteContainer getLiteRootContainer() {
		List<? extends LiteSimpleMartConfiguratorObject> liteRootContainer = 
			super.getLiteMartConfiguratorObjectList();	// can only have 0 or 1 element
		return liteRootContainer!=null && !liteRootContainer.isEmpty() ? (LiteContainer)liteRootContainer.get(0) : null;
	}

	@Override
	public String toString() {
		LiteContainer liteRootContainer = getLiteRootContainer();
		return "liteRootContainer = " + (liteRootContainer!=null ? 
				liteRootContainer.getName() : null);
	}
}
