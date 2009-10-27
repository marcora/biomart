package org.biomart.querying.queryRunner.prototype;


import java.util.ArrayList;
import java.util.List;

import org.biomart.common.general.utils.MyUtils;

public class QueryRunnerIntermediaryResult implements Cloneable {
	private Integer queryThreadIndex = null;
	private List<List<String>> values = null;
	private List<Integer> previousJoinFieldIndexesWithinResults = null;
	
	private Integer totalRows = null;
	private Integer totalColumns = null;
	
	public QueryRunnerIntermediaryResult(int queryThreadIndex) {
		this.queryThreadIndex = queryThreadIndex;
		this.previousJoinFieldIndexesWithinResults = null;	// N/A for the 1st one
		this.values = new ArrayList<List<String>>();
		this.totalRows = 0;
		this.totalColumns = 0;
	}
	public QueryRunnerIntermediaryResult(QueryRunnerIntermediaryResult queryRunnerIntermediaryResult, 
			List<Integer> previousJoinFieldIndexesWithinResults) {
		this.queryThreadIndex= queryRunnerIntermediaryResult.queryThreadIndex;
		this.previousJoinFieldIndexesWithinResults = previousJoinFieldIndexesWithinResults;
		this.values = new ArrayList<List<String>>();
		this.totalRows = 0;
		this.totalColumns = 0;
	}
	
	public boolean containsRow(List<String> row) {
		for (List<String> rowTmp : this.values) {
			if (QueryRunnerPrototypeUtils.stringListEquals(rowTmp, row)) {
				return true;
			}		
		}
		return false;
	}
	public void addValueRow(List<String> row) {
		this.values.add(row);
		this.totalRows++;
		this.totalColumns = row.size();
	}
	public List<String> getValueRow(int rowNumber) {
		return this.values.get(rowNumber);
	}
	
	public int getTotalRows() {
		return this.totalRows;
	}
	
	public int getTotalColumns() {
		return this.totalColumns;
	}

	/*@Override
	public QueryRunnerIntermediaryResult clone() throws CloneNotSupportedException {
		QueryRunnerIntermediaryResult queryRunnerIntermediaryResult = new QueryRunnerIntermediaryResult();
		queryRunnerIntermediaryResult.queryThreadIndex = this.queryThreadIndex;
		queryRunnerIntermediaryResult.previousJoinFieldIndexWithinResults = this.previousJoinFieldIndexWithinResults;
		queryRunnerIntermediaryResult.values = new ArrayList<List<String>>();
		for (List<String> row : this.values) {
			List<String> row2 = new ArrayList<String>(row);
			queryRunnerIntermediaryResult.values.add(row2);
		}
		return queryRunnerIntermediaryResult;
	}*/
	
	public List<List<String>> getTransposedValues() {	// By row instead of by field
		
		debug(values);
		
		MyUtils.pressKeyToContinue();
		
		List<List<String>> transposedValues = new ArrayList<List<String>>();
		if (values.size()>0) {
			int totalValues = values.get(0).size();
			for (int rowNumber = 0; rowNumber < totalValues; rowNumber++) {	// We know results are square: always same number of values for every columns
				List<String> newList = new ArrayList<String>();
				for (int fieldNumber = 0; fieldNumber < values.size(); fieldNumber++) {
					List<String> fieldValuesList = values.get(fieldNumber);					
MyUtils.checkStatusProgram(fieldValuesList.size()==totalValues, 
		"values.size() = " + values.size() + ", fieldNumber = " + fieldNumber + ", " +
		"fieldValuesList.size() = " + fieldValuesList.size() + ", totalValues = " + totalValues);				
					newList.add(fieldValuesList.get(rowNumber));
				}
				transposedValues.add(newList);
			}
		}
		return transposedValues;
	}
	public String display() {
		StringBuffer stringBuffer = new StringBuffer();
		stringBuffer.append(MyUtils.DASH_LINE + MyUtils.LINE_SEPARATOR);
		int row = 0;
		for (List<String> l : this.values) {
			stringBuffer.append("row " + row + "\t");
			for (String s : l) {
				stringBuffer.append(s + "\t");
			}
			stringBuffer.append(MyUtils.LINE_SEPARATOR);
			row++;
		}
		stringBuffer.append(MyUtils.DASH_LINE + MyUtils.LINE_SEPARATOR);
		return stringBuffer.toString();
	}

	public static void debug(List<List<String>> list) {
		StringBuffer stringBuffer = new StringBuffer();
		for (List<String> l : list) {
			for (String s : l) {
				stringBuffer.append(s + MyUtils.TAB_SEPARATOR);
			}
			stringBuffer.append(MyUtils.LINE_SEPARATOR);
		}
		MyUtils.writeFile("/home/anthony/Desktop/zed", stringBuffer.toString());
System.exit(0);		
	}
	public List<List<String>> getValues() {
		return values;
	}
	public List<Integer> getPreviousJoinFieldIndexesWithinResults() {
		return previousJoinFieldIndexesWithinResults;
	}
}
