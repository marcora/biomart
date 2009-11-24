package org.biomart.configurator.model.object;

import java.util.List;

public class Processor {
	private String processorName;
	
	private List<String> filterStrList;
	
	public Processor(String name) {
		
		this.processorName = name;
	}
	
	public void setFilterList(List<String> fl){
		this.filterStrList = fl;
	}
	
	public String getName() {
		return this.processorName;
	}
	
	public List<String> getFilterList() {
		return this.filterStrList;
	}
}
