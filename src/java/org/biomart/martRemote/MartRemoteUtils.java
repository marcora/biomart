package org.biomart.martRemote;

public class MartRemoteUtils {
	public static String buildRequestName(String identifier) {
		return identifier + MartRemoteConstants.REQUEST_SUFFIX;
	}
	public static String buildResponseName(String identifier) {
		return identifier + MartRemoteConstants.RESPONSE_SUFFIX;
	}
}
