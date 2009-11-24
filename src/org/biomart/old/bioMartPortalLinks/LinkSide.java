package org.biomart.old.bioMartPortalLinks;


import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;

import org.biomart.common.general.utils.CompareUtils;
import org.biomart.common.general.utils.MyUtils;
import org.biomart.old.martService.MartServiceConstants;
import org.biomart.old.martService.objects.Exportable;
import org.biomart.old.martService.objects.Importable;
import org.biomart.old.martService.objects.Portable;
import org.biomart.old.martService.restFulQueries.RestFulQuery;
import org.biomart.old.martService.restFulQueries.RestFulQueryDataset;
import org.biomart.old.martService.restFulQueries.objects.Attribute;




public class LinkSide implements Serializable, Comparator<LinkSide>, Comparable<LinkSide> {
	
	private static final long serialVersionUID = 270201689451337785L;
	
	public String bioMartVersion = null;
	public String virtualSchemaName = null;
	public String martName = null;
	public String datasetName = null;
	public Importable importable = null;
	public Exportable exportable = null;
	private Boolean left = null;
	private PortableData portableData = null;
	
	public LinkableDataset dataset = null;	// Do not put in toString
	
	public LinkSide(LinkableDataset linkableDataset, Portable impExp) {
		super();
		
		this.bioMartVersion = linkableDataset.bioMartVersion;
		this.virtualSchemaName = linkableDataset.serverVirtualSchema;
		this.martName = linkableDataset.mart;
		this.datasetName = linkableDataset.datasetName;
		if (impExp instanceof Importable) {
			left = true;
			this.importable = (Importable)impExp;
		} else if (impExp instanceof Exportable) {
			left = false;
			this.exportable = (Exportable)impExp;
		}
		this.dataset = linkableDataset;
	}
	
	public boolean isLeft() {
		return this.left;
	}
	public boolean isRight() {
		return !this.left;
	}
	
	@Override
	public String toString() {
		return
			"bioMartVersion = " + bioMartVersion + ", " +
			"virtualSchemaName = " + virtualSchemaName + ", " +
			"martName = " + martName + ", " +
			"datasetName = " + datasetName + ", " +
			"importable = " + importable + ", " + 
			"exportable = " + exportable + ", " +
			"portableData = " + portableData; 
	}
	public String toShortString() {
		return bioMartVersion + MyUtils.TAB_SEPARATOR + virtualSchemaName + MyUtils.TAB_SEPARATOR + martName + 
		MyUtils.TAB_SEPARATOR + datasetName + 
		MyUtils.TAB_SEPARATOR + (importable!=null ? importable.toShortString() : null) + 
		MyUtils.TAB_SEPARATOR + (exportable!=null ? exportable.toShortString() : null);
	}
	public String toStatisticsString() {
		return virtualSchemaName + MyUtils.TAB_SEPARATOR + martName + MyUtils.TAB_SEPARATOR + datasetName + MyUtils.TAB_SEPARATOR + (isLeft() ? importable.linkName : exportable.linkName) + MyUtils.TAB_SEPARATOR + (portableData!=null ? portableData.toStatisticString() : null);
	}
	public String toNiceString() {
		return 
			"bioMartVersion = " + bioMartVersion + "," +
			MyUtils.TAB_SEPARATOR + "virtualSchemaName = " + virtualSchemaName + "," +
			MyUtils.TAB_SEPARATOR + "martName = " + martName + "," +
			MyUtils.TAB_SEPARATOR + "datasetName = " + datasetName + "," +
			MyUtils.TAB_SEPARATOR + "dataset.datasetType = " + dataset.datasetType + "," +
			MyUtils.TAB_SEPARATOR + "super.toString() = " + super.toString() + "," + MyUtils.LINE_SEPARATOR +
			MyUtils.TAB_SEPARATOR + MyUtils.TAB_SEPARATOR + MyUtils.TAB_SEPARATOR + "importable = " + importable + 
			MyUtils.TAB_SEPARATOR + "exportable = " + exportable + MyUtils.LINE_SEPARATOR +
			MyUtils.TAB_SEPARATOR + MyUtils.TAB_SEPARATOR + MyUtils.TAB_SEPARATOR + "portableData = " + (portableData!=null ? portableData.toNiceString() : null);
	}

	@Override
	public boolean equals(Object arg0) {
		LinkSide linkSide = (LinkSide)arg0;
		return 
		this.bioMartVersion.equals(linkSide.bioMartVersion) &&
		this.virtualSchemaName.equals(linkSide.virtualSchemaName) &&
		this.martName.equals(linkSide.martName) &&
		this.datasetName.equals(linkSide.datasetName) &&
		((this.importable==null && linkSide.importable==null) ||
		(this.importable!=null && linkSide.importable!=null && this.importable.equals(linkSide.importable))) &&
		((this.exportable==null && linkSide.exportable==null) ||
		(this.exportable!=null && linkSide.exportable!=null && this.exportable.equals(linkSide.exportable)));
	}

	public boolean isSameSide(LinkSide linkSide) {
		return
		this.bioMartVersion.equals(linkSide.bioMartVersion) &&
		this.virtualSchemaName.equals(linkSide.virtualSchemaName) &&
		this.martName.equals(linkSide.martName) &&
		this.datasetName.equals(linkSide.datasetName);
	}
	
	public int compareTo(LinkSide link) {
		return compare(this, link);
	}
	public int compare(LinkSide link1, LinkSide link2) {
    	int compare = link1.bioMartVersion.compareTo(link2.bioMartVersion);
		if (compare!=0) {
			return compare;
		}
		compare = link1.virtualSchemaName.compareTo(link2.virtualSchemaName);
		if (compare!=0) {
			return compare;
		}
		compare = link1.martName.compareTo(link2.martName);
		if (compare!=0) {
			return compare;
		}
		compare = link1.datasetName.compareTo(link2.datasetName);
		if (compare!=0) {
			return compare;
		}
		compare = CompareUtils.compareNull(link1.importable, link2.importable);
		if (compare!=0) {
			return compare;
		}
		compare = link1.importable.compareTo(link2.importable);
		if (compare!=0) {
			return compare;
		}
		compare = CompareUtils.compareNull(link1.exportable, link2.exportable);
		if (compare!=0) {
			return compare;
		}
		return link1.exportable.compareTo(link2.exportable);
    }

	@Override
	public int hashCode() {
		int hash = 7;
		hash = 31 * hash + virtualSchemaName.hashCode();
		hash = 31 * hash + datasetName.hashCode();
		return hash;
	}
	public void setExportable(Exportable exportable) {
		this.exportable = exportable;
	}
	public void setImportable(Importable importable) {
		this.importable = importable;
	}

	public void setPortableData(PortableData portableData) {
		this.portableData = portableData;
	}

	public PortableData getPortableData() {
		return portableData;
	}

	public RestFulQuery createMartServiceRestFulQuery(String martServiceUrl) throws UnsupportedEncodingException {
		return createMartServiceRestFulQuery(martServiceUrl, false);
	}
	public RestFulQuery createMartServiceRestFulQuery(String martServiceUrl, boolean unique) throws UnsupportedEncodingException {
		RestFulQuery restFulQuery = null;
		if (isLeft()) {
			restFulQuery = new RestFulQuery(martServiceUrl, this.virtualSchemaName, this.bioMartVersion, 
					MartServiceConstants.DEFAULT_FORMATTER, false, false, unique, null, null,		// unique?
					new RestFulQueryDataset(this.datasetName, 
							(this.importable.getCompleteAttributesList() ? 
									this.importable.getAttibutesList() :
									new ArrayList<Attribute>(Arrays.asList(new Attribute[] {new Attribute(this.importable.getFirstAttributeName())}))
							//new ArrayList<Attribute>(Arrays.asList(new Attribute[] {new Attribute(linkSide.importable.getFirstAttributeName())}))
							//,((Importable)linkSide.importable).getFilterList()
							)));			
		} else if (isRight()) {
			restFulQuery = new RestFulQuery(martServiceUrl, this.virtualSchemaName, this.bioMartVersion, 
					MartServiceConstants.DEFAULT_FORMATTER, false, false, unique, null, null,		// unique?
					new RestFulQueryDataset(this.datasetName, ((Exportable)this.exportable).getAttributesList(), null));	
		} else {
			MyUtils.errorProgram("this.importable==null = " + (this.importable==null) + 
					", this.exportable==null = " + (this.exportable==null), true);
		}
		return restFulQuery;
	}
}
