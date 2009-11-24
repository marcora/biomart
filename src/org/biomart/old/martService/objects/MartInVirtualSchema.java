package org.biomart.old.martService.objects;


import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


import org.biomart.common.general.constants.MyConstants;
import org.biomart.common.general.utils.MyUtils;
import org.biomart.objects.MartConfiguratorUtils;
import org.biomart.old.martService.MartServiceConstants;
import org.jdom.Element;


public class MartInVirtualSchema implements Comparable<MartInVirtualSchema>, Serializable {

	private static final long serialVersionUID = 2123368976389964073L;
	
	private String virtualSchema = null;
	
	private Boolean local = null;
	private String serverVirtualSchema = null;
	public String type = null;
	public String martName = null;
	public String databaseType = null;
	public String displayName = null;
	public String host = null;
	public String port = null;
	public String user = null;
	public String password = null;
	public String databaseName = null;
	public String path = null;
	public Boolean visible = null;
	public List<String> includeDatasets = null;
	public Element martServiceLine = null;
	
	@Override
	public String toString() {
		return "(" + virtualSchema + ")" + MyUtils.TAB_SEPARATOR + "(" + local + ")" + MyUtils.TAB_SEPARATOR + type + MyUtils.TAB_SEPARATOR + martName + 
		MyUtils.TAB_SEPARATOR + databaseType + MyUtils.TAB_SEPARATOR + displayName + MyUtils.TAB_SEPARATOR + host + MyUtils.TAB_SEPARATOR + port + 
		MyUtils.TAB_SEPARATOR + user + MyUtils.TAB_SEPARATOR + password + MyUtils.TAB_SEPARATOR + databaseName + 
		MyUtils.TAB_SEPARATOR + visible + MyUtils.TAB_SEPARATOR + serverVirtualSchema + MyUtils.TAB_SEPARATOR + path + 
		MyUtils.TAB_SEPARATOR + includeDatasets;
	}
	public String toShortString() {
		return "(" + virtualSchema + ")" + MyUtils.TAB_SEPARATOR + "(" + local + ")" + MyUtils.TAB_SEPARATOR + type + MyUtils.TAB_SEPARATOR + martName + 
		MyUtils.TAB_SEPARATOR + databaseType + MyUtils.TAB_SEPARATOR + displayName + MyUtils.TAB_SEPARATOR + host + MyUtils.TAB_SEPARATOR + port + 
		MyUtils.TAB_SEPARATOR + user + MyUtils.TAB_SEPARATOR + password + MyUtils.TAB_SEPARATOR + databaseName + 
		MyUtils.TAB_SEPARATOR + visible + MyUtils.TAB_SEPARATOR + serverVirtualSchema + MyUtils.TAB_SEPARATOR + path + 
		MyUtils.TAB_SEPARATOR + includeDatasets;
	}
	public MartInVirtualSchema(String virtualSchema, String serverVirtualSchema, Boolean local, String type, String martName, String databaseType, String displayName, String host, String port, 
			String user, String password, String databaseName, String path, String visible, String includeDatasets, Element martServiceLine) {
		super();
		this.virtualSchema = virtualSchema;
		this.serverVirtualSchema = serverVirtualSchema;
		this.local = local;
		this.type = type;
		this.martName = martName;
		this.databaseType = databaseType;
		this.displayName = displayName;
		this.host = host;
		this.port = port;
		this.user = user;
		this.password = password;
		this.databaseName = databaseName;
		this.path = path;
		this.visible = MartConfiguratorUtils.binaryDigitToBoolean(visible);
		this.includeDatasets = new ArrayList<String>();
		if (includeDatasets.length()>0) {
			this.includeDatasets.addAll(new ArrayList<String>(Arrays.asList(includeDatasets.split(MartServiceConstants.INCLUDE_DATASETS_SEPARATOR))));
		}
		this.martServiceLine = martServiceLine;
	}
	public String getPathToRemoteMartService() {
		return MyConstants.HTTP_PROTOCOL + this.host + MyConstants.DOMAIN_PORT_SEPARATOR + this.port + this.path;
	}
	@Override
	public boolean equals(Object object) {
		MartInVirtualSchema martInVirtualSchema = (MartInVirtualSchema)object;
		return this.martName.equals(martInVirtualSchema.martName);
	}
	public int compareTo(MartInVirtualSchema martInVirtualSchema) {
		return this.martName.compareTo(martInVirtualSchema.martName); 
	}
	public String getVirtualSchema() {
		return virtualSchema;
	}
	public Boolean getLocal() {
		return local;
	}
	public String getServerVirtualSchema() {
		return serverVirtualSchema;
	}
	public String getType() {
		return type;
	}
	public String getMartName() {
		return martName;
	}
	public String getDatabaseType() {
		return databaseType;
	}
	public String getDisplayName() {
		return displayName;
	}
	public String getHost() {
		return host;
	}
	public String getPort() {
		return port;
	}
	public String getUser() {
		return user;
	}
	public String getPassword() {
		return password;
	}
	public String getDatabaseName() {
		return databaseName;
	}
	public String getPath() {
		return path;
	}
	public Boolean getVisible() {
		return visible;
	}
	public List<String> getIncludeDatasets() {
		return includeDatasets;
	}
	public Element getMartServiceLine() {
		return martServiceLine;
	}
}
