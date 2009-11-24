package org.biomart.configurator.model;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.biomart.configurator.utils.McGuiUtils;
import org.biomart.configurator.utils.type.McNewUserType;

public class User {
	private Map<String, Location> locMap;
	private String userName;
	private String password;
	//TODO the business logic should't in the object
	private McNewUserType type; 
	private String synUser;

	
	public User(String userName, String password) {
		this.userName = userName;
		this.password = password;
		locMap = new HashMap<String, Location>();
		McGuiUtils.INSTANCE.addUser(this);
	}

	public String getUserName() {
		return userName;
	}

	public String getPassword() {
		return password;
	}
	
	public Map<String, Location> getLocationMap() {
		return this.locMap;
	}
	
	public String toString() {
		return this.userName;
	}
	public McNewUserType getType() {
		return this.type;
	}
	
	public void setType(McNewUserType userType) {
		this.type = userType;
	}
	
	public String getSynchronizedUser() {
		return this.synUser;
	}
	
	public void setSynchronizedUser(String user) {
		this.synUser = user;
	}

	public void addLocation(Location loc) {
		this.locMap.put(loc.getName(), loc);
	}
	
	public Location getLocation(String locName) {
		Location loc = this.locMap.get(locName);
		if(loc==null) {
			List<String> users = McGuiUtils.INSTANCE.getSynchronizedUserList(this.userName);
			if(users==null || users.size()==0)
				return null;
			for(String user:users) {
				loc = McGuiUtils.INSTANCE.getUserMap().get(user).getLocationMap().get(locName);
				if(loc!=null)
					return loc;
			}
		} else
			return loc;
		return null;
	}
	
	
}