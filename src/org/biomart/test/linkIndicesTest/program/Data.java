package org.biomart.test.linkIndicesTest.program;

import java.util.ArrayList;
import java.util.List;

public class Data {
	List<String> currentIdList = null;
	List<List<DatasetValues>> batchResultList = null;
	Integer currentDatasetMatchingRowsCount = null;
	public Data(List<String> currentIdList, List<List<DatasetValues>> batchResultList, Integer currentDatasetMatchingRowsCount) {
		super();
		this.currentIdList = new ArrayList<String>(currentIdList);
		this.batchResultList = new ArrayList<List<DatasetValues>>(batchResultList);
		this.currentDatasetMatchingRowsCount = currentDatasetMatchingRowsCount;
	}
}
