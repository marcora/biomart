package org.biomart.transformation.helpers;

public class HostAndVirtualSchema {

	
	private MartServiceIdentifier martServiceIdentifier = null;
	private String virtualSchema = null;
	public HostAndVirtualSchema(MartServiceIdentifier martServiceIdentifier, String virtualSchema) {
		super();
		this.martServiceIdentifier = martServiceIdentifier;
		this.virtualSchema = virtualSchema;
	}
	public MartServiceIdentifier getMartServiceIdentifier() {
		return martServiceIdentifier;
	}
	public String getVirtualSchema() {
		return virtualSchema;
	}
	@Override
	public String toString() {
		return "martServiceIdentifier = " + martServiceIdentifier + ", virtualSchema = " + virtualSchema;
	}
}
