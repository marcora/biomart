package org.biomart.old.martService.restFulQueries.objects;


import java.io.Serializable;
import java.util.Comparator;

import org.biomart.common.general.exceptions.FunctionalException;
import org.biomart.old.martService.MartServiceConstants;


public class Field implements Comparable<Field>, Comparator<Field>, Serializable {

	private static final long serialVersionUID = -3843113662842407859L;
	private static final String UNKNOWN_MAIN_TABLE_FULL_NAME_PREFIX = "??__";
	
	private String tableConstraint = null;
	private String keyName = null;
	private String fieldName = null;
	private Boolean main = null;
	private String tableName = null;
	public Field() {}	// for comparator only
	public Field(String tableConstraint, String keyName, String fieldName) {
		super();
		this.tableConstraint = tableConstraint;
		this.keyName = keyName;
		this.fieldName = fieldName;
		if (isValid()) {
			this.main = tableConstraint.equals(MartServiceConstants.MAIN_TABLE_CONSTRAINT); 
			this.tableName = (!this.main ? "" : UNKNOWN_MAIN_TABLE_FULL_NAME_PREFIX) + tableConstraint;
		}
	}
	public boolean isValid() {
		return this.tableConstraint!=null && this.fieldName!=null && this.keyName!=null;
	}
	public String getFieldName() {
		return fieldName;
	}
	public String getKeyName() {
		return keyName;
	}
	public String getTableConstraint() {
		return tableConstraint;
	}
	public boolean isMain() {
		return main;
	}
	
	@Deprecated
	public void setMainTableName(String mainTableName) throws FunctionalException {
		if (!this.main) {
			throw new FunctionalException("Not a Main Table, name is already set: " + this.tableName);
		}
		this.tableName = mainTableName;
	}
	@Deprecated
	public String getTableName() throws FunctionalException {
		if (this.main && this.tableName.startsWith(UNKNOWN_MAIN_TABLE_FULL_NAME_PREFIX)) {
			throw new FunctionalException("No name is set for the Main Table, name is still set to: " + this.tableName);
		}
		return this.tableName;
	}
	
	@Override
	public boolean equals(Object arg0) {
		Field field = (Field)arg0;
		return tableConstraint.equalsIgnoreCase(field.tableConstraint) &&
		fieldName.equalsIgnoreCase(field.fieldName) &&
		keyName.equalsIgnoreCase(field.keyName);
	}
	@Override
	public int hashCode() {
		int hash = 7;
		hash = 31 * hash + tableConstraint.hashCode();
		hash = 31 * hash + fieldName.hashCode();
		hash = 31 * hash + keyName.hashCode();
		return hash;
	}
	@Override
	public String toString() {
		return "tableConstraint = " + tableConstraint + ", keyName = " + keyName + ", fieldName = " + fieldName + ", tableName = " + tableName;
	}
	public int compareTo(Field field) {
		int compare = tableConstraint.compareToIgnoreCase(field.tableConstraint);
		if (compare!=0) {
			return compare;
		}
		compare = fieldName.compareToIgnoreCase(field.fieldName);
		if (compare!=0) {
			return compare;
		}
		return keyName.compareToIgnoreCase(field.keyName);
	}
	public int compare(Field arg0, Field arg1) {
		return arg0.compareTo(arg1);
	}
}
