package org.biomart.objects.objects;


import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.biomart.common.general.utils.MyUtils;
import org.biomart.objects.MartConfiguratorConstants;
import org.biomart.objects.MartConfiguratorUtils;



public class Portable extends MartConfiguratorObject implements Serializable /*implements Comparable<Portable>, Comparator<Portable> */{

	private static final long serialVersionUID = 4539635290976611423L;

	public static void main(String[] args) {}
	
	protected Range range = null;
	protected PartitionTable mainPartitionTable = null;
	protected List<PartitionTable> otherPartitionTableList = null;
	
	// For backward compatibility
	protected String formerLinkName = null;
	protected String formerLinkVersion = null;
		
	public Portable() {} 	// for Serialization
	protected Portable(PartitionTable mainPartitionTable, String name, String displayName, String description, Boolean visible, 
			String xmlElementName) {
		super(name, displayName, description, visible, xmlElementName);
		
		this.range = new Range(mainPartitionTable, false);

		this.mainPartitionTable = mainPartitionTable;
		this.otherPartitionTableList = new ArrayList<PartitionTable>();
	}
	
	public void addOtherPartitionTable(PartitionTable partitionTable) {
		MyUtils.checkStatusProgram(!this.otherPartitionTableList.contains(partitionTable));
		this.otherPartitionTableList.add(partitionTable);
	}

	@Override
	public String toString() {
		return 
			super.toString() + ", " + 
			"range = " + range + ", " + 
			"formerLinkName = " + formerLinkName + ", " + 
			"formerLinkVersion = " + formerLinkVersion;
	}

	@Override
	public boolean equals(Object object) {
		if (this==object) {
			return true;
		}
		if((object==null) || (object.getClass()!= this.getClass())) {
			return false;
		}
		Portable portable=(Portable)object;
		return (
			(this.range==portable.range || (this.range!=null && range.equals(portable.range))) &&
			(this.formerLinkName==portable.formerLinkName || (this.formerLinkName!=null && formerLinkName.equals(portable.formerLinkName))) &&
			(this.formerLinkVersion==portable.formerLinkVersion || (this.formerLinkVersion!=null && formerLinkVersion.equals(portable.formerLinkVersion)))
		);
	}

	@Override
	public int hashCode() {
		int hash = MartConfiguratorConstants.HASH_SEED1;
		hash = MartConfiguratorConstants.HASH_SEED2 * hash + super.hashCode();
		hash = MartConfiguratorConstants.HASH_SEED2 * hash + (null==range? 0 : range.hashCode());
		hash = MartConfiguratorConstants.HASH_SEED2 * hash + (null==formerLinkName? 0 : formerLinkName.hashCode());
		hash = MartConfiguratorConstants.HASH_SEED2 * hash + (null==formerLinkVersion? 0 : formerLinkVersion.hashCode());
		return hash;
	}

	public Range getRange() {
		return range;
	}

	public void setRange(Range range) {
		this.range = range;
	}

	public String getFormerLinkName() {
		return formerLinkName;
	}

	public void setFormerLinkName(String formerLinkName) {
		this.formerLinkName = formerLinkName;
	}

	public String getFormerLinkVersion() {
		return formerLinkVersion;
	}

	public void setFormerLinkVersion(String formerLinkVersion) {
		this.formerLinkVersion = formerLinkVersion;
	}

	/*@Override
	public int compare(Portable portable1, Portable portable2) {
		if (portable1==null && portable2!=null) {
			return -1;
		} else if (portable1!=null && portable2==null) {
			return 1;
		}
		return CompareUtils.compareNull(portable1.range, portable2.range);
	}

	@Override
	public int compareTo(Portable portable) {
		return compare(this, portable);
	}*/
	
	protected org.jdom.Element generateXml() {
		org.jdom.Element element = super.generateXml();
		MartConfiguratorUtils.addAttribute(element, "formerLinkName", this.formerLinkName);
		MartConfiguratorUtils.addAttribute(element, "formerLinkVersion", this.formerLinkVersion);
		this.range.addXmlAttribute(element, "range");
		
		return element;
	}

}
