package org.biomart.transformation.tmp.backwardCompatibility.objects;

import org.jdom.Element;

public class DimensionPartitionData {

	public String unpartitionedDimensionTableName = null;
	public String partitionValue = null;
	public Element table = null;
	public DimensionPartitionData(String unpartitionedDimensionTableName, String partitionValue) {
		super();
		this.unpartitionedDimensionTableName = unpartitionedDimensionTableName;
		this.partitionValue = partitionValue;
	}
}
