package org.biomart.objects.objects;


import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;


import org.biomart.common.general.exceptions.FunctionalException;
import org.biomart.common.general.exceptions.TechnicalException;
import org.biomart.common.general.utils.MyUtils;
import org.biomart.objects.MartConfiguratorConstants;
import org.biomart.transformation.helpers.PartitionTableAndRow;
import org.jdom.Element;


public class Range /*implements Comparable<Range>, Comparator<Range>*/implements Serializable {

	private static final long serialVersionUID = -5456292603227275941L;

	public static void main(String[] args) {}

	private Set<Part> partSet = null;
	private Set<PartitionTable> partitionTableSet = null;
	private Boolean target = null;	// To know whether to show visibility or not
	
	private PartitionTable mainPartitionTable = null;
	private Set<Integer> mainRowsSet = null;

	public Range(PartitionTable mainPartitionTable, Boolean target) {
		super();
		this.partSet = new TreeSet<Part>();
		this.partitionTableSet = new HashSet<PartitionTable>();
		this.target = target;
		
		this.mainPartitionTable = mainPartitionTable;
		this.mainRowsSet = new HashSet<Integer>();
	}
	
	public PartitionTable getMainPartitionTable() {
		return mainPartitionTable;
	}

	public Set<Integer> getMainRowsSet() {
		return this.mainRowsSet;
	}
	
	public int getTotalParts() {
		return this.partSet.size();
	}
	
	public int getTotalPartitionTables() {
		return this.partitionTableSet.size();
	}
	
	public boolean contains(PartitionTable partitionTable, int row) throws TechnicalException {
		if (!this.partitionTableSet.contains(partitionTable)) {
			return false;
		}
		for (Part part : this.partSet) {
			if (part.contains(partitionTable, row)) {
				return true;
			}
		}
		return false;
	}
	
	public void addRangePartitionRow(PartitionTable partitionTable, int row) throws TechnicalException {
		addRangePartitionRow(partitionTable, row, null);	// not updated if null
	}
	public void addRangePartitionRow(PartitionTable partitionTable, int row, Boolean visible) throws TechnicalException {
		try {
			if (!this.partitionTableSet.contains(partitionTable)) {
				this.partitionTableSet.add(partitionTable);
				
				if (this.partSet.isEmpty()) {
					Part newPart = new Part(this.target, (visible==null ? true : visible),	// visible by default 
							null, new PartitionTableAndRow(partitionTable, row));
					this.partSet.add(newPart);
				} else {
					for (Part part : this.partSet) {
						MyUtils.checkStatusProgram(!part.containsPartition(partitionTable), 
								"part = " + part.getXmlValue() + ", partitionTableSet = " + getPartitionTableSetToShortString() + 
								", partitionTable = " + partitionTable.getName() + ", row = " + row);
						part.addPartitionRow(partitionTable, row);
					}
				}
			} else {
				Set<Part> partSetTmp = new TreeSet<Part>();					
				for (Part part : this.partSet) {
					Part newPart = (Part)part.clone(); 	// clone existing one
					newPart.modifyPartitionRow(partitionTable, row);	// replace it's value
					if (visible!=null) {
						newPart.setVisible(visible);	// change visibility if specified
					}
					partSetTmp.add(newPart);				
				}
				this.partSet.addAll(partSetTmp);
			}
		} catch (CloneNotSupportedException e) {
			throw new TechnicalException(e);	// Impossible
		}
		if (partitionTable.getMain()) {
			this.mainRowsSet.add(row);
		}
	}
	
	/**
	 * Remove all references to a partition, ex: for Pox [PmR0:PoxR0:1][PmR0:PoxR1:1] become [PmR0]
	 * @param partitionTable
	 * @param row
	 * @param visible
	 * @throws TechnicalException
	 */
	public void removePartition(PartitionTable partitionTable) throws TechnicalException, FunctionalException {
		try {
			if (this.partitionTableSet.contains(partitionTable)) {
				Set<Part> partSetTmp = new TreeSet<Part>();					
				for (Part part : this.partSet) {
					Part newPart = (Part)part.clone(); 	// clone existing one
					int row = newPart.removePartition(partitionTable);
					if (partitionTable.getMain()) {
						this.mainRowsSet.remove(row);
					}
					partSetTmp.add(newPart);
				}
				this.partSet = partSetTmp;
				
				this.partitionTableSet.remove(partitionTable);

			} else {
				throw new FunctionalException("Can't remove");
			}
		} catch (CloneNotSupportedException e) {
			throw new TechnicalException(e);	// Impossible
		}
	}
	
	private String getPartitionTableSetToShortString() {
		StringBuffer stringBuffer = new StringBuffer();
		int i = 0;
		for (PartitionTable partitionTable : this.partitionTableSet) {
			stringBuffer.append((i==0 ? "" : ", ") + partitionTable.getName());	
			i++;
		}
		return stringBuffer.toString();
	}

	public Set<Part> getPartSet() {
		return partSet;
	}

	public Set<PartitionTable> getPartitionTableSet() {
		return partitionTableSet;
	}

	public void setPartSet(Set<Part> partSet) {
		this.partSet = partSet;
	}

	public void setPartitionTableSet(Set<PartitionTable> partitionTableSet) {
		this.partitionTableSet = partitionTableSet;
	}

	@Override
	public String toString() {
		return 
			/*super.toString() + ", " +*/ 
			"partSet = " + partSet + ", " +
			"partitionTableSet = " + partitionTableSet + ", " +
			"target = " + target;
	}

	@Override
	public boolean equals(Object object) {
		if (this==object) {
			return true;
		}
		if((object==null) || (object.getClass()!= this.getClass())) {
			return false;
		}
		Range range=(Range)object;
		return (
			/*super.equals();*/
			(this.partSet==range.partSet || (this.partSet!=null && partSet.equals(range.partSet))) &&
			(this.partitionTableSet==range.partitionTableSet || (this.partitionTableSet!=null && partitionTableSet.equals(range.partitionTableSet))) &&
			(this.target==range.target || (this.target!=null && target.equals(range.target)))
		);
	}

	@Override
	public int hashCode() {
		int hash = MartConfiguratorConstants.HASH_SEED1;
		hash = MartConfiguratorConstants.HASH_SEED2 * hash + (null==partSet? 0 : partSet.hashCode());
		hash = MartConfiguratorConstants.HASH_SEED2 * hash + (null==partitionTableSet? 0 : partitionTableSet.hashCode());
		hash = MartConfiguratorConstants.HASH_SEED2 * hash + (null==target? 0 : target.hashCode());
		return hash;
	}

	/*@Override
	public int compare(Range range1, Range range2) {
		if (range1==null && range2!=null) {
			return -1;
		} else if (range1!=null && range2==null) {
			return 1;
		}
		int compare = CompareUtils.compareNull(range1.partSet, range2.partSet);
		if (compare!=0) {
			return compare;
		}
		compare = CompareUtils.compareNull(range1.source, range2.source);
		if (compare!=0) {
			return compare;
		}
		return CompareUtils.compareNull(range1.partitionTableSet, range2.partitionTableSet);
	}

	@Override
	public int compareTo(Range range) {
		return compare(this, range);
	}*/
	
	public boolean isConsistent() {
		for (Part part : this.partSet) {
			for (PartitionTable partitionTable : this.partitionTableSet) {
				if (!part.containsPartition(partitionTable)) {
					return false;
				}
			}				
		}
		return true;
	}
	
	public String getXmlValue() {
		MyUtils.checkStatusProgram(this.isConsistent());
		StringBuffer stringBuffer = new StringBuffer();
		for (Part part : partSet) {
			stringBuffer.append(part.getXmlValue());
		}
		return stringBuffer.toString();
	}
	
	public void addXmlAttribute (Element element, String attributeName) {
		String xmlValue = getXmlValue();
//		MyUtils.checkStatusProgram(!MyUtils.isEmpty(xmlValue), MartConfiguratorUtils.displayJdomElement(element));
		element.setAttribute(attributeName, xmlValue);
	}
	
	public void addPart(Part part) {
		try {
			Part clone = (Part)part.clone();
			this.partSet.add(clone);
			this.partitionTableSet.addAll(clone.getMap().keySet());
			this.mainRowsSet.add(clone.getMainRowNumber());
		} catch (CloneNotSupportedException e) {
			e.printStackTrace();
		}
	}
	
	public Range cloneRange() throws TechnicalException {	// can't use clone() because of exception thrown...
		Range range = new Range(
				this.mainPartitionTable, this.target);	// the main partition table must not be "cloned" here
		TreeSet<Part> partSet = new TreeSet<Part>();
		try {
			for (Part part : this.partSet) {
				Part clone = (Part)part.clone();
				partSet.add(clone);
			}
		} catch (CloneNotSupportedException e) {
			throw new TechnicalException(e);
		}
		range.setPartSet(partSet);
		range.setPartitionTableSet(new HashSet<PartitionTable>(this.partitionTableSet));
		range.setMainRowsSet(new HashSet<Integer>(this.mainRowsSet));
		return range;
	}
	
	public Range cloneRangeAsNotTarget() throws TechnicalException {	// can't use clone() because of exception thrown...
		Range range = cloneRange();
		range.setTarget(false);
		return range;
	}

	public void setMainRowsSet(Set<Integer> mainRowsSet) {
		this.mainRowsSet = mainRowsSet;
	}

	public void setTarget(Boolean target) {
		this.target = target;
		for (Part part : this.partSet) {
			part.setTarget(false);
		}
	}
	
	public static Range mainRangesIntersection(
			PartitionTable mainPartitionTable , Boolean target, List<Range> rangeList) throws FunctionalException, TechnicalException {
		MyUtils.checkStatusProgram(rangeList.size()>=1);
		Range newRange = new Range(mainPartitionTable, false);
		List<HashSet<Integer>> allMainRows = new ArrayList<HashSet<Integer>>();
		
		// Create set of all the sets 
		for (int i = 0; i < rangeList.size(); i++) {
			Range rangeTmp = rangeList.get(i);
			HashSet<Integer> mainRows = new HashSet<Integer>();
			for (Part part : rangeTmp.getPartSet()) {
				mainRows.add(part.getMainRowNumber());
			}
			if (!allMainRows.contains(mainRows)) {	// No need to take consider doubles for intersection
				allMainRows.add(mainRows);
			}
		}
		HashSet<Integer> firstMainRows = allMainRows.get(0);	// take 1st one (at least one)
		for (int i = 1; i < allMainRows.size(); i++) {
			HashSet<Integer> mainRows = allMainRows.get(i);
			firstMainRows.retainAll(mainRows);
		}
		for (Integer mainRow : firstMainRows) {
			newRange.addRangePartitionRow(mainPartitionTable, mainRow);			
		}
		return newRange;
	}
	
	public static Range rangesIntersection(Boolean target, List<Range> rangeList) throws FunctionalException, TechnicalException {
		MyUtils.checkStatusProgram(rangeList.size()>=1);
		Range newRange = null;
		newRange = rangeList.get(0).cloneRange();
		if (rangeList.size()>1 && !allEqual(rangeList)) {
			
	System.out.println(MyUtils.EQUAL_LINE);
	for (int i = 0; i < rangeList.size(); i++) {
		Range range = rangeList.get(i);
		System.out.println(range.getXmlValue());
	}
	System.out.println(MyUtils.EQUAL_LINE);
	System.out.println(allEqual(rangeList));
			
			for (int i = 1; i < rangeList.size(); i++) {
				Range range = rangeList.get(i);
				
				Set<PartitionTable> partitionTableSet1 = newRange.getPartitionTableSet();
				Set<PartitionTable> partitionTableSet2 = range.getPartitionTableSet();
				
				Set<PartitionTable> intersection = new HashSet<PartitionTable>(partitionTableSet1);
				intersection.retainAll(partitionTableSet2);
				
				//TODO....
				// Will require a remove(partitionTable, row) that will also remove the pT if no more rows of it in the range
				/*for (Part part1 : newRange.getPartSet()) {
					boolean inCommon = false;
					for (Part part2 : range.getPartSet()) {
						for (PartitionTable partitionTable : intersection) {
							
						}
					}
				}*/
				
				MyUtils.errorProgram("unhandled...");
			}			
		}
		newRange.setTarget(target);
		return newRange;
	}
	public static boolean allEqual(List<Range> rangeList) {
		for (int i = 1; i < rangeList.size(); i++) {
			if (!rangeList.get(i-1).equals(rangeList.get(i))) {
				return false;
			}
		}	
		return true;
	}
	
	public void changeAllVisibility(boolean allVisible) {
		for (Part part : this.partSet) {
			part.setVisible(allVisible);				
		}
	}
	
	/**
	 * Check if all parts are set to invisible (invisible element)
	 */
	public boolean noVisiblePartInRange() {
		for (Part part : this.partSet) {
			if (part.getVisible()) {
				return false;
			}
		}
		return true;
	}
}
