package org.biomart.configurator.utils;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.biomart.configurator.jdomUtils.JDomNodeAdapter;
import org.biomart.configurator.model.User;
import org.biomart.configurator.utils.type.IdwViewType;
import org.biomart.configurator.utils.type.McGuiType;
import org.biomart.configurator.utils.type.McViewType;
import org.biomart.configurator.view.idwViews.McViewSchema;
import org.biomart.configurator.view.idwViews.McViews;

/**
 * a singleton class
 * @author yliang
 *
 */
public enum McGuiUtils {
	INSTANCE;
	
	private McGuiType guiType;
	private McViewType mcViewType;
	private User currentUser;
	private String martRunnerHost;
	private String martRunnerPort;
	private Map<String,MRunnerInfoObject> locationRunnerMap = new HashMap<String,MRunnerInfoObject>(); 
	private Map<String, User> userMap = new HashMap<String, User>(); 
	//for synchronized user
	private List<ArrayList<String>> synUserList = new ArrayList<ArrayList<String>>();
	
	public void setGuiType(McGuiType guiType) {
		this.guiType = guiType;
	}
	
	public McGuiType getGuiType() {
		return guiType;
	}
	
	public void setMcViewType(McViewType type) {
		this.mcViewType = type;
	}
	
	public McViewType getMcViewType() {
		return this.mcViewType;
	}
	
	public void setCurrentUser(User currentUser) {
		this.currentUser = currentUser;
	}
	
	public User getCurrentUser() {
		return currentUser;
	}
	
	public void addUser(User newUser) {
		this.userMap.put(newUser.getUserName(), newUser);
	}
	
	public Map<String, User> getUserMap() {
		return this.userMap;
	}

	public static void refreshGui(JDomNodeAdapter treeNode) {
		McViewSchema schema = (McViewSchema)McViews.getInstance().getView(IdwViewType.SCHEMA);
		schema.showComponent(treeNode);
	}

	public void setMartRunnerHost(String host) {
		this.martRunnerHost = host;
	}
	
	public void setMartRunnerPort(String port) {
		this.martRunnerPort = port;
	}
	
	public String getMartRunnerHost() {
		return this.martRunnerHost;
	}
	
	public String getMartRunnerPort() {
		return this.martRunnerPort;
	}

	public Map<String,MRunnerInfoObject> getMRunnerInfoMap() {
		return this.locationRunnerMap;
	}

	public void setUserSynchronized(String oldUser, String newUser) {
		boolean isOldUserInSyn = false;
		for(List<String> synlist:this.synUserList) {
			if(synlist.contains(oldUser)) {
				synlist.add(newUser);
				isOldUserInSyn = true;
				break;
			}
		}
		
		if(!isOldUserInSyn) {
			ArrayList<String> list = new ArrayList<String>();
			list.add(oldUser);
			list.add(newUser);
			this.synUserList.add(list);
		}
	}
	
	public List<ArrayList<String>> getSynchronizedUserList() {
		return this.synUserList;
	}
	
	public List<String> getSynchronizedUserList(String user) {
		for(List<String> synlist: this.synUserList) {
			if(synlist.contains(user)) 
				return synlist;
		}
		return null;
	}
}