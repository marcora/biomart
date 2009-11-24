package org.biomart.configurator.model.object;

import org.biomart.builder.model.PartitionTable;

/**
 * for now, assume only one level partition
 * @author yliang
 *
 */
public class McFilterObject {
	
	private PartitionTable partitionTable; 
	
	public void setPartitionTable(PartitionTable pt) {
		this.partitionTable = pt;
	}
	
	public McFilterObject () {
		
	}
	
	public PartitionTable getPartitionTable() {
		return this.partitionTable;
	}
}