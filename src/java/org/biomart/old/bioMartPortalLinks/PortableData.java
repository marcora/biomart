package org.biomart.old.bioMartPortalLinks;


import java.io.Serializable;
import java.util.List;

import org.biomart.common.general.utils.CompareUtils;
import org.biomart.common.general.utils.MyUtils;
import org.biomart.common.general.utils.Timer;
import org.biomart.common.general.utils.comparators.StringComparator;

public class PortableData implements Comparable<PortableData>, Serializable {

	private static final long serialVersionUID = 217652042966830862L;
	
	public static final String INTRA_LINK_SIDE_SEPARATOR = "_#_";
	public static final String INTER_LINK_SIDE_SEPARATOR = "_##_";

	private String virtualSchemaName = null;
	private String datasetName = null;
	private List<String> attributeNames = null;
	private String fileName = null;
	public String temporaryFileFolder = null;

	private String errorMessage = null;	// If any
	private Integer totalRows = null;
	private Long fileSize = null;
	private Timer timer = null;
	private String urlString = null;
	
	public static String createLinkIndexFileName(String portableDataLeft, String portableDataRight) {
		return portableDataLeft + INTER_LINK_SIDE_SEPARATOR + portableDataRight;
	}
	public PortableData(String virtualSchemaName, String datasetName, List<String> attributeNames, String temporaryFileFolder) {
		super();
		this.virtualSchemaName = virtualSchemaName;
		this.datasetName = datasetName;
		this.attributeNames = attributeNames;
		this.fileName = buildFileName();
		this.temporaryFileFolder = temporaryFileFolder;
	}
	public String buildFileName() {
		StringBuffer attributeNamesStringBuffer = new StringBuffer();
		for (int i = 0; i < this.attributeNames.size(); i++) {
			attributeNamesStringBuffer.append(this.attributeNames.get(i) + INTRA_LINK_SIDE_SEPARATOR);
		}
		String string = this.virtualSchemaName + INTRA_LINK_SIDE_SEPARATOR + this.datasetName + INTRA_LINK_SIDE_SEPARATOR + attributeNamesStringBuffer;
		return string;
	}
	public List<String> getAttributeNames() {
		return attributeNames;
	}
	public String getDatasetName() {
		return datasetName;
	}
	public String getVirtualSchemaName() {
		return virtualSchemaName;
	}
	@Override
	public boolean equals(Object object) {
		PortableData portableData = (PortableData)object;
		if (this.attributeNames.size()!=portableData.attributeNames.size()) {
			return false;
		}
		for (int i = 0; i < this.attributeNames.size(); i++) {
			if (!this.attributeNames.get(i).equals(portableData.attributeNames.get(i))) {
				return false;
			}
		}
		return this.virtualSchemaName.equals(portableData.virtualSchemaName) && this.datasetName.equals(portableData.datasetName);
	}
	@Override
	public int hashCode() {
		int hash = 7;
		hash = 31 * hash + (null == virtualSchemaName ? 0 : virtualSchemaName.hashCode());
		hash = 31 * hash + (null == datasetName ? 0 : datasetName.hashCode());
		hash = 31 * hash + (null == attributeNames ? 0 : attributeNames.hashCode());
		return hash;
	}
	@Override
	public String toString() {
		return 
		"virtualSchemaName = " + virtualSchemaName + 
		", datasetName = " + datasetName + 
		", attributeNames = " + attributeNames +
		
		", totalRows = " + totalRows + 
		", fileSize = " + fileSize + 
		", timer = " + timer + 
		", errorMessage = " + errorMessage +
		", urlString = " + urlString;
	}
	public String toNiceString() {
		return  toString();
	}
	public String toStatisticString() {
		return fileName + MyUtils.TAB_SEPARATOR + totalRows + MyUtils.TAB_SEPARATOR + fileSize + MyUtils.TAB_SEPARATOR + 
		(timer!=null ? timer.toStatisticString() : null) + MyUtils.TAB_SEPARATOR + errorMessage + MyUtils.TAB_SEPARATOR + urlString;
	}
	public String getFileName() {
		return this.fileName;
	}
	public int compareTo(PortableData portableData) {
		int compare = this.virtualSchemaName.compareTo(portableData.virtualSchemaName);
		if (compare!=0) {
			return compare;
		}
		compare = this.datasetName.compareTo(portableData.datasetName);
		if (compare!=0) {
			return compare;
		}
		return CompareUtils.compareList(this.attributeNames, portableData.attributeNames, new StringComparator());
	}
	
	public Long getFileSize() {
		return fileSize;
	}
	public void setFileSize(Long fileSize) {
		this.fileSize = fileSize;
	}
	public Integer getTotalRows() {
		return totalRows;
	}
	public void setTotalRows(Integer totalRows) {
		this.totalRows = totalRows;
	}
	public Timer getTimer() {
		return timer;
	}
	public void setTimer(Timer timer) {
		this.timer = timer;
	}
	public String getUrlString() {
		return urlString;
	}
	public void setUrlString(String urlString) {
		this.urlString = urlString;
	}
	public String getErrorMessage() {
		return errorMessage;
	}
	public void setErrorMessage(String errorMessage) {
		this.errorMessage = errorMessage;
	}
}
