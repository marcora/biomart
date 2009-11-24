package org.biomart.transformation.oldXmlObjects;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.biomart.common.general.exceptions.FunctionalException;
import org.jdom.Element;


public class OldElementPlaceHolder extends OldNode {

	public static void main(String[] args) {}
	
	public static final List<String> propertyList = new ArrayList<String>(Arrays.asList(new String[] {
	}));
	
	protected String internalName = null;
		
	protected OldElementPlaceHolder(Element jdomElement, String internalName) throws FunctionalException {
		super(jdomElement);
		
		this.internalName = internalName;
	}

	public String getInternalName() {
		return internalName;
	}

	@Override
	public String toString() {
		return 
			super.toString() + ", " +
			"internalName = " + internalName;
	}
}