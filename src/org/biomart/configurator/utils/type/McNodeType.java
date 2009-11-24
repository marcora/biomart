package org.biomart.configurator.utils.type;

public enum McNodeType {
	MartRegistry (0),
	User (1),
	Location (1),
	Mart(2),
	DataSet(3),
	Container(4),
	PartitionTable(4),
	Filter(5),
	Attribute(5),
	AttributePointer(5);
	
	private int level;
	
	McNodeType(int level) {
		this.level = level;
	}
	
	public int getLevel() {
		return this.level;
	}
}