package org.biomart.old.bioMartPortalLinks;


import java.io.File;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.biomart.common.general.utils.MyUtils;
import org.biomart.common.general.utils.Timer;
import org.biomart.old.martService.MartServiceConstants;
import org.biomart.old.martService.objects.Exportable;
import org.biomart.old.martService.restFulQueries.RestFulQuery;
import org.biomart.old.martService.restFulQueries.RestFulQueryDataset;
import org.biomart.old.martService.restFulQueries.objects.Attribute;
import org.biomart.old.martService.restFulQueries.objects.Filter;




public class Link implements Serializable, Comparable<Link> {
	
	private static final long serialVersionUID = 2498122812383663392L;

	public static void main(String[] args) {}

	public String name = null;
	public String virtualSchema = null;
	public String bioMartVersion = null;
	//public Boolean biDirectional = null;
	public LinkSide left = null;
	public LinkSide right = null;
	//public Link otherDirection = null;	// Do not include in toString (or endless loop)
private Boolean indexableImportable = null;	
private Boolean validVisibility = null;
private Boolean noGenomicSequence = null;
private Integer hashProblem = null;
	
	private Integer totalRows = null;
	private Long fileSize = null;
	private Timer timer = null;	// Only to find the intersection
	private File file = null;
	private String filePathAndName = null;

	public Link(String name, String virtualSchema, String bioMartVersion, LinkSide left, LinkSide right) {
		super();
		this.name = name;
		this.virtualSchema = virtualSchema;
		this.bioMartVersion = bioMartVersion;
		
		this.left = left;
		this.right = right;
		
		//this.biDirectional = Boolean.FALSE;
		this.indexableImportable = left.importable.getCompleteAttributesList();
		this.noGenomicSequence = !this.left.dataset.datasetType.equals(MartServiceConstants.ATTRIBUTE_GENOMIC_SEQUENCE) && 
			!this.right.dataset.datasetType.equals(MartServiceConstants.ATTRIBUTE_GENOMIC_SEQUENCE);
		this.validVisibility = left.dataset.visible && right.dataset.visible;
		this.hashProblem = 0;
	}
	
	public boolean isIndexableLink() {
		return this.getValidVisibility() && this.getIndexableImportable() && this.getNoGenomicSequence();
	}
	
	/*public void setBiDirectional(Link otherDirectionLink) {
		this.biDirectional = Boolean.TRUE;
		this.left.setExportable(otherDirectionLink.right.exportable);
		this.right.setImportable(otherDirectionLink.left.importable);
		this.otherDirection = otherDirectionLink;
	}*/

	public Boolean getValidVisibility() {
		return validVisibility;
	}

	public Integer getHashProblem() {
		return hashProblem;
	}

	public void setHashProblem(Integer hashProblem) {
		this.hashProblem = hashProblem;
	}

	@Override
	public String toString() {
		return 
			"name = " + name + ", " +
			"virtualSchema = " + virtualSchema + ", " +
			"bioMartVersion = " + bioMartVersion + ", " +
			//"biDirectional = " + biDirectional + ", " + 
			"validVisibility = " + validVisibility + ", " + 
			"hashProblem = " + hashProblem + ", " + 
			//"otherDirection = " + (otherDirection!=null ? otherDirection.hashCode() : null) + ", " + 
			"left = {" + left + "}" + ", " +
			"right = {" + right + "}";
	}
	public String toNiceString() {
		return toNiceStringPreCreation() + MyUtils.LINE_SEPARATOR + toNiceStringPostCreation();
	}
	public String toNiceStringPreCreation() {
		return 
			"name = " + name + "," + MyUtils.TAB_SEPARATOR + "virtualSchema = " + virtualSchema + "," + MyUtils.TAB_SEPARATOR + 
			"bioMartVersion = " + bioMartVersion + "," + MyUtils.TAB_SEPARATOR + 
			//"biDirectional = " + biDirectional + "," + MyUtils.TAB_SEPARATOR +
			"validVisibility = " + validVisibility + "," + MyUtils.TAB_SEPARATOR + "hashProblem = " + hashProblem + "," + MyUtils.TAB_SEPARATOR +
			//"otherDirection = " + (otherDirection!=null ? otherDirection.hashCode() : null) + "," + \
			MyUtils.LINE_SEPARATOR +
			MyUtils.TAB_SEPARATOR + "left = " + MyUtils.LINE_SEPARATOR + 
			MyUtils.TAB_SEPARATOR + MyUtils.TAB_SEPARATOR + left.toNiceString() + MyUtils.LINE_SEPARATOR +
			MyUtils.TAB_SEPARATOR + "right = " + MyUtils.LINE_SEPARATOR + 
			MyUtils.TAB_SEPARATOR + MyUtils.TAB_SEPARATOR + right.toNiceString();
	}
	public String toNiceStringPostCreation() {
		return 
			MyUtils.TAB_SEPARATOR + "totalRows = " + totalRows + ", fileSize = " + fileSize + ", fileName = " + filePathAndName + ", timer = " + timer;
	}
	public String toStatisticString() {
		return
			name + MyUtils.TAB_SEPARATOR + virtualSchema + MyUtils.TAB_SEPARATOR + bioMartVersion + MyUtils.TAB_SEPARATOR + 
			//biDirectional + MyUtils.TAB_SEPARATOR + 
			validVisibility + MyUtils.TAB_SEPARATOR + hashProblem + MyUtils.TAB_SEPARATOR + left.virtualSchemaName + MyUtils.TAB_SEPARATOR + 
			left.datasetName + MyUtils.TAB_SEPARATOR + right.datasetName + MyUtils.TAB_SEPARATOR +  
			totalRows + MyUtils.TAB_SEPARATOR + fileSize + MyUtils.TAB_SEPARATOR + filePathAndName + 
			MyUtils.TAB_SEPARATOR + (timer!=null ? timer.toStatisticString() : null);
	}
	public String toShortString() {
		return
			name + MyUtils.TAB_SEPARATOR + virtualSchema + MyUtils.TAB_SEPARATOR + bioMartVersion + MyUtils.TAB_SEPARATOR + 
			//biDirectional + MyUtils.TAB_SEPARATOR + 
			validVisibility + MyUtils.TAB_SEPARATOR + hashProblem + 
			MyUtils.TAB_SEPARATOR + "{" + left.toShortString() + "}" + MyUtils.TAB_SEPARATOR + "{" + right.toShortString() + "}" + 
			MyUtils.TAB_SEPARATOR + totalRows + MyUtils.TAB_SEPARATOR + fileSize + MyUtils.TAB_SEPARATOR + filePathAndName + MyUtils.TAB_SEPARATOR + 
			(timer!=null ? timer.toStatisticString() : null);
	}
	public String toShortString2() {
		return
			virtualSchema + MyUtils.TAB_SEPARATOR + left.datasetName + MyUtils.TAB_SEPARATOR + right.datasetName + MyUtils.TAB_SEPARATOR + name + 
			/*MyUtils.TAB_SEPARATOR + biDirectional + MyUtils.TAB_SEPARATOR + bothSideVisible + */MyUtils.TAB_SEPARATOR + hashProblem;
	}
	public String toQuickDescription() {
		return this.virtualSchema + MyUtils.TAB_SEPARATOR + 
		this.left.martName + MyUtils.TAB_SEPARATOR + this.left.datasetName + MyUtils.TAB_SEPARATOR + 
		this.right.martName + MyUtils.TAB_SEPARATOR + this.right.datasetName; 
	}
	public boolean isOtherDirectionOf(Link newLink) {
		return 
		this.name.equals(newLink.name) &&
		this.virtualSchema.equals(newLink.virtualSchema) && 
		this.left.importable.isOtherDirectionOf(newLink.right.exportable) && 
		newLink.left.importable.isOtherDirectionOf(this.right.exportable);
	}
	public boolean isOtherDirectionOf2(Link newLink) {
		return 
		/*this.name.equals(newLink.name) &&*/
		this.virtualSchema.equals(newLink.virtualSchema) && 
		this.left.importable.isOtherDirectionOf2(newLink.right.exportable) && 
		newLink.left.importable.isOtherDirectionOf2(this.right.exportable);
	}
	public static Link containsOtherDirection(List<Link> listLink, Link newLink) {
		for (Link link : listLink) {
			if (link.isOtherDirectionOf(newLink)) {
				return link;
			}
		}
		return null;
	}
	
	public int compareTo(Link link) {
    	int compare = this.name.compareTo(link.name);
		if (compare!=0) {
			return compare;
		}
		compare = this.virtualSchema.compareTo(link.virtualSchema);
		if (compare!=0) {
			return compare;
		}
		compare = this.left.compare(this.left, link.left);
		if (compare!=0) {
			return compare;
		}
		compare = this.right.compare(this.right, link.right);
		if (compare!=0) {
			return compare;
		}
		/*compare = this.biDirectional.compareTo(link.biDirectional);
		if (compare!=0) {
			return compare;
		}*/
		compare = this.validVisibility.compareTo(link.validVisibility);
		if (compare!=0) {
			return compare;
		}
		return this.hashProblem.compareTo(link.hashProblem);
	}

	@Override
	public boolean equals(Object arg0) {
		Link link = (Link)arg0;
		return this.name.equals(link.name) &&
		this.virtualSchema.equals(link.virtualSchema) &&
		//this.biDirectional.equals(link.biDirectional) &&
		this.validVisibility.equals(link.validVisibility) &&
		this.hashProblem.equals(link.hashProblem) &&
		this.left.equals(link.left) &&
		this.right.equals(link.right);
	}

	public Timer getTimer() {
		return timer;
	}

	public void setTimer(Timer timer) {
		this.timer = timer;
	}

	public void setTotalRows(Integer totalRow) {
		this.totalRows = totalRow;
	}
	
	public Integer getTotalRows() {
		return totalRows;
	}

	public Long getFileSize() {
		return fileSize;
	}

	public void setFileSize(Long fileSize) {
		this.fileSize = fileSize;
	}

	public File getFile() {
		return file;
	}

	public void setFile(File file) {
		this.file = file;
	}
	
	public String getFilePathAndName() {
		return filePathAndName;
	}

	public void setFilePathAndName(String filePathAndName) {
		this.filePathAndName = filePathAndName;
	}

	public String createLinkIndexFilePathAndName(String linkIndexesFolder) {
		return linkIndexesFolder + PortableData.createLinkIndexFileName(
				this.left.getPortableData().getFileName(), this.right.getPortableData().getFileName());
	}

	/**
	 * The left side is the "master" one (bioMartVersion-wise)
	 * @param link
	 * @return
	 * @throws UnsupportedEncodingException
	 */
	public RestFulQuery createMartServiceRestFulQuery(String martServiceUrl) throws UnsupportedEncodingException {
		LinkSide left = this.left;
		LinkSide right = this.right;
		RestFulQueryDataset leftDataset = new RestFulQueryDataset(left.datasetName, 
				new ArrayList<Attribute>(Arrays.asList(new Attribute[] {new Attribute(left.importable.getFirstAttributeName())}))
				//, ((Importable)left.importable).getFilterList()
				);
		RestFulQueryDataset rightDataset = new RestFulQueryDataset(right.datasetName, 
				((Exportable)right.exportable).getAttributesList(), null);
		RestFulQuery restFulQuery = 
			new RestFulQuery(martServiceUrl, left.virtualSchemaName, left.bioMartVersion, 
					MartServiceConstants.DEFAULT_FORMATTER, false, false, false,		// unique? 
					null, 1,	// limitSize = 1 to speed it up	
					leftDataset, rightDataset);
		return restFulQuery;
	}
	
	public RestFulQuery createPointerMartServiceRestFulQuery(String martServiceUrl) throws UnsupportedEncodingException {
		RestFulQuery restFulQuery = null;
		LinkSide left = this.left;
		LinkSide right = this.right;
		Filter filterPointer = left.dataset.mapRemoteDatasetToExternalFilterPointer.get(right.datasetName);
		if (null!=filterPointer && !filterPointer.hidden) {
			Filter remoteFilter = new Filter(filterPointer.pointerInfo.getPointerElementName(), null);
			RestFulQueryDataset leftDataset = new RestFulQueryDataset(left.datasetName, 
					new ArrayList<Attribute>(Arrays.asList(new Attribute[] {new Attribute(left.importable.getFirstAttributeName())})),
					new ArrayList<Filter>(Arrays.asList(new Filter[] {remoteFilter}))
					);
			restFulQuery = 
				new RestFulQuery(martServiceUrl, left.virtualSchemaName, left.bioMartVersion, 
						MartServiceConstants.DEFAULT_FORMATTER, false, false, false,		// unique? 
						null, 1,	// limitSize = 1 to speed it up	
						leftDataset);
		}
		return restFulQuery;
	}

	public Boolean getIndexableImportable() {
		return indexableImportable;
	}

	public Boolean getNoGenomicSequence() {
		return noGenomicSequence;
	}
}

