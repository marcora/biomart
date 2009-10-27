package org.biomart.transformation.helpers;

import org.biomart.common.general.constants.MyConstants;
import org.biomart.common.general.utils.MyUtils;
import org.biomart.old.martService.objects.MartInVirtualSchema;


public class MartServiceIdentifier {

	private String host = null;
	private String port = null;
	private String path = null;
	public MartServiceIdentifier(String host, String port, String path) {
		super();
		this.host = host;
		this.port = port;
		this.path = path;
	}
	public MartServiceIdentifier(MartInVirtualSchema martInVirtualSchema) {
		super();
		this.host = martInVirtualSchema.getHost();
		this.port = martInVirtualSchema.getPort();
		this.path = martInVirtualSchema.getPath();
	}
	public String getServer() {
		return host + MyConstants.DOMAIN_PORT_SEPARATOR + port;
	}
	public String getHost() {
		return host;
	}
	public String getPort() {
		return port;
	}
	public String getPath() {
		return path;
	}
	public String generateIdentifier() {
		return MyUtils.trimHost(host).replace(".", "_") + MyUtils.INFO_SEPARATOR + 
		port + MyUtils.INFO_SEPARATOR + path.replace("/", "_");
	}
	@Override
	public String toString() {
		return "host = " + host + ", port = " + port + ", path = " + path;
	}
	public String formatMartServiceUrl() {
		String trimmedHost = MyUtils.trimHost(host);
		Integer portInteger = null;
		try {
			portInteger = Integer.valueOf(port);
		} catch (NumberFormatException e) {
			portInteger = MyConstants.DEFAULT_WEB_SERVICE_PORT;
		}
		String timmedPath = path.startsWith(MyUtils.FILE_SEPARATOR) ? path.substring(1) : path; 
		
		return MyConstants.HTTP_PROTOCOL + trimmedHost + MyConstants.DOMAIN_PORT_SEPARATOR + portInteger + 
		MyUtils.FILE_SEPARATOR + timmedPath;
	}
}
