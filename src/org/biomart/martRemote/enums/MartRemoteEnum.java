package org.biomart.martRemote.enums;

import org.biomart.martRemote.MartRemoteConstants;
import org.biomart.martRemote.MartRemoteUtils;



public enum MartRemoteEnum {
	GET_REGISTRY		(MartRemoteConstants.GET_REGISTRY_IDENTIFIER),
	GET_DATASETS		(MartRemoteConstants.GET_DATASETS_IDENTIFIER),
	GET_ROOT_CONTAINER	(MartRemoteConstants.GET_ROOT_CONTAINER_IDENTIFIER),
	GET_ATTRIBUTES		(MartRemoteConstants.GET_ATTRIBUTES_IDENTIFIER),
	GET_FILTERS			(MartRemoteConstants.GET_FILTERS_IDENTIFIER),
	GET_LINKS			(MartRemoteConstants.GET_LINKS_IDENTIFIER),
	QUERY				(MartRemoteConstants.QUERY_IDENTIFIER);
	
	private String identifier = null;
	private MartRemoteEnum(String identifier) {
		this.identifier = identifier;
	}
	public String getIdentifier() {
		return identifier;
	}
	public String getRequestName() {
		return MartRemoteUtils.buildRequestName(this.identifier);
	}
	public String getResponseName() {
		return MartRemoteUtils.buildResponseName(this.identifier);
	}
	public static MartRemoteEnum getEnumFromIdentifier(String value) {
		for (MartRemoteEnum remoteAccessEnum : MartRemoteEnum.values()) {
			if (remoteAccessEnum.getIdentifier().equals(value)) {
				return remoteAccessEnum;
			}
		}
		return null;
	}
}