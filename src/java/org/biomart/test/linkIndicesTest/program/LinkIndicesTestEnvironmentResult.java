package org.biomart.test.linkIndicesTest.program;


import java.util.ArrayList;
import java.util.List;

import org.biomart.common.general.utils.Timer;



public class LinkIndicesTestEnvironmentResult {

	private Timer timer = null;
	private Integer resultSize = null;
	private List<String> resultList = null;
	
	public LinkIndicesTestEnvironmentResult(String timerComment) {
		super();
		this.timer = new Timer(timerComment);
		this.resultList = new ArrayList<String>();
		this.resultSize = 0;
	}

	/*public void addToResultList(String batchTargetValue) {
		this.resultList.add(batchTargetValue);
		this.resultSize++;
	}*/

	public void setResultSize(int resultSize) {
		this.resultSize = resultSize;
	}
	
	@Override
	public String toString() {
		return "timer = " + this.timer + 
		", resultSize = " + this.resultSize + 
		(resultList!=null && resultList.size()>0 ? 
				", resultList.get(first) = " + this.resultList.get(0) + ", " +
						"resultList.get(last) = " + this.resultList.get(this.resultList.size()-1) : "");		
	}
	
	public List<String> getResultList() {
		return resultList;
	}

	public Integer getResultSize() {
		return resultSize;
	}

	public Timer getTimer() {
		return timer;
	}
}
