package org.biomart.objects;


import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.biomart.common.general.utils.MyUtils;
import org.biomart.objects.helpers.PartitionReference;
import org.biomart.objects.helpers.Property;
import org.biomart.objects.objects.Part;
import org.biomart.objects.objects.PartitionTable;
import org.biomart.transformation.helpers.PartitionTableAndRow;
import org.jdom.Comment;
import org.jdom.Element;

public class MartConfiguratorUtils {

	public static String getTabSpace(int tabLevel) {
		StringBuffer stringBuffer = new StringBuffer();
		for (int i = 0; i < tabLevel; i++) {
			stringBuffer.append(MyUtils.TAB_SEPARATOR);
		}
		return stringBuffer.toString();
	}

	public static void addText(Element element, String text) {
		if (text!=null && !MyUtils.isEmpty(text)) {
			element.setText(text);
		}
	}


	public static void addComment(Element element, Comment comment) {
		if (comment!=null) {
			element.addContent(comment);
		}
	}
	public static void addAttribute(Element element, Property property) {
		if (property!=null && !MyUtils.isEmpty(property.getXmlValue())) {
			element.setAttribute(property.getProperty(), property.getXmlValue());
		}
	}
	
	public static void addAttribute(Element element, String attributeName, File attributeValue) {
		if (attributeValue!=null) {
			element.setAttribute(attributeName, attributeValue.getAbsolutePath());
		}
	}
	
	public static void addAttribute(Element element, String attributeName, Object attributeValue) {
		if (attributeValue!=null) {
			element.setAttribute(attributeName, attributeValue.toString());
		}
	}
	
	public static void addAttribute(Element element, String attributeName, String attributeValue) {
		if (attributeValue!=null && !MyUtils.isEmpty(attributeValue)) {
			element.setAttribute(attributeName, attributeValue);
		}
	}

	public static void addAttribute(Element element, String attributeName, Boolean attributeValue) {
		if (attributeValue!=null) {
			element.setAttribute(attributeName, String.valueOf(attributeValue));
		}
	}

	public static void addAttribute(Element element, String attributeName, Integer attributeValue) {
		if (attributeValue!=null) {
			element.setAttribute(attributeName, String.valueOf(attributeValue));
		}
	}

	public static void addAttribute(Element element, String attributeName, Collection<String> attributeValues) {
		if (attributeValues!=null && !attributeValues.isEmpty()) {
			element.setAttribute(attributeName, 
					MartConfiguratorUtils.collectionToString(attributeValues, MartConfiguratorConstants.LIST_ELEMENT_SEPARATOR));
		}
	}

	public static boolean hasValue(String property) {
		return property!=null && !MyUtils.isEmpty(property);
	}
	
	public static String booleanToBinaryDigit(Boolean b) {
		return (b!=null && b ? "1" : "0");
	}
	public static Boolean binaryDigitToBoolean(String s) {
		return (s!=null ? (s.equals("1") ? true : false) : false);
	}

	public static<T> String collectionToCommaSeparatedString(Collection<? extends T> c) {
		return MartConfiguratorUtils.collectionToString(c, ",");
	}

	public static<T> String collectionToString(Collection<? extends T> c, String separator) {
		StringBuffer stringBuffer = new StringBuffer();
		int i=0;
		if (c!=null) {
			for (T t : c) {
				stringBuffer.append((i==0 ? "" : separator) + t);
				i++;
			}
		}
		return stringBuffer.toString();
	}

	public static boolean containsIgnoreCase(List<String> list, String value) {
		for (String valueTmp : list) {
			if (value.equalsIgnoreCase(valueTmp)) {
				return true;
			}
		}
		return false;
	}
	
	public static boolean isPartitionReference(String value) {
		if (null==value) {
			return false;
		}
		Matcher m = MartConfiguratorConstants.PARTITION_REFERENCE_PATTERN.matcher(value);
		return m.matches();
	}
	
	public static boolean containsPartitionReferences(String value) {
		if (null==value) {
			return false;
		}
		Matcher m = MartConfiguratorConstants.PARTITION_REFERENCE_PATTERN.matcher(value);
		return m.find();
	}
	
	/**
	 * Always gives a list that starts with a string (empty or not) and then alternates partition reference / string (empty or not)
	 * @param pattern
	 * @param value
	 * @return
	 */
	public static List<String> extractPartitionReferences(String value) {
		/*String pattern = "\\(" + "P" + "\\w" + "C" + "\\d" + "\\)";
		String s = "asdfaf (P0C1) asdf (P3C4)adfaf";*/
		List<String> list = new ArrayList<String>();
		if (null!=value) {
			Pattern pattern = MartConfiguratorConstants.PARTITION_REFERENCE_PATTERN;
			Matcher m = pattern.matcher(value);
			while (m.find()) {
				int start = m.start();
				int end = m.end();
				list.add(value.substring(0, start));
				list.add(value.substring(start, end));
				value = value.substring(end);
				m = pattern.matcher(value);
			}
			list.add(value);
		} else {
			list.add("");	// null becomes an empty value
		}
		return list;
	}
	public static String extractPartitionTableNameFromPartitionReferenceString(String partitionReferenceString) {
		String partitionTableName = extractDataFromString(
				MartConfiguratorConstants.PARTITION_REFERENCE_PARTITION_TABLE_NAME_PREFIX_PATTERN, 
				MartConfiguratorConstants.PARTITION_REFERENCE_PARTITION_TABLE_NAME_SUFIX_PATTERN, partitionReferenceString);		
		MyUtils.checkStatusProgram(partitionTableName!=null && !MyUtils.isEmpty(partitionTableName));
		return partitionTableName;
	}
	public static int extractColumnNumberFromPartitionReferenceString(String partitionReferenceString) {
		String columnString = extractDataFromString(
				MartConfiguratorConstants.PARTITION_REFERENCE_COLUMN_NUMBER_PREFIX_PATTERN, 
				MartConfiguratorConstants.PARTITION_REFERENCE_COLUMN_NUMBER_SUFIX_PATTERN, partitionReferenceString);		
		MyUtils.checkStatusProgram(columnString!=null && !MyUtils.isEmpty(columnString));
		return Integer.valueOf(columnString);
	}
	public static String extractDataFromString(Pattern patternPrefix, Pattern patternSuffix, String value) {
		String data = null;
		Matcher m1 = patternPrefix.matcher(value);
		Matcher m2 = patternSuffix.matcher(value);
		if (m1.find() && m2.find()) {
			int end1 = m1.end();
			int start2 = m2.start();
			if (end1<start2) {
				data = value.substring(end1, start2);
			}
		}
		return data;
	}
	@Deprecated
	public static String replaceMainPartitionReferenceByValue(PartitionTable mainPartitionTable, int mainRowNumber, String value) {
		if (MartConfiguratorUtils.containsPartitionReferences(value)) {
			StringBuffer stringBuffer = new StringBuffer();
			List<String> partitionReferences = MartConfiguratorUtils.extractPartitionReferences(value);
			for (int i = 1; i < partitionReferences.size(); i+=2) {
				String token = partitionReferences.get(i);
				if ((i+1)%2==0) {	// see definition of extractPartitionReferences
					PartitionReference partitionReference = PartitionReference.fromString(token);
					if (partitionReference.getPartitionTableName().equals(mainPartitionTable.getName())) {
						String actualValue = mainPartitionTable.getValue(mainRowNumber, partitionReference.getColumn()); 
						stringBuffer.append(actualValue);
					} else {
						stringBuffer.append(token);
					}
				} else {
					stringBuffer.append(token);
				}
			}
			value = stringBuffer.toString();
		}
		return value; 
	}
	public static String replacePartitionReferencesByValues(String value, Part part) {
		if (MartConfiguratorUtils.containsPartitionReferences(value) && part!=null) {
			StringBuffer stringBuffer = new StringBuffer();
			List<String> partitionReferences = MartConfiguratorUtils.extractPartitionReferences(value);
			for (int i = 1; i < partitionReferences.size(); i+=2) {
				String token = partitionReferences.get(i);
				if ((i+1)%2==0) {	// see definition of extractPartitionReferences
					PartitionReference partitionReference = PartitionReference.fromString(token);
					PartitionTable partitionTable = part.getPartitionTableByName(partitionReference.getPartitionTableName());
					if (null!=partitionTable) {
						String actualValue = partitionTable.getValue(part.getRowNumber(partitionTable), partitionReference.getColumn()); 
						stringBuffer.append(actualValue);
					} else {
						stringBuffer.append(token);	// not a real partition table reference: shouldn't be allowed though (GUI validation)
					}
				} else {
					stringBuffer.append(token);
				}
			}
			value = stringBuffer.toString();
		}
		return value; 
	}

	public static Part createGenericPart(PartitionTable mainPartitionTable) {
		return new Part(false, null, null, 
				new PartitionTableAndRow(mainPartitionTable, MartConfiguratorConstants.PARTITION_TABLE_ROW_WILDCARD_NUMBER));
	}
}
