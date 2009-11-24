package org.biomart.old.martService.restFulQueries.objects;

import java.io.Serializable;


public class Element implements Serializable {

	private static final long serialVersionUID = -8884865648230302348L;

	public String internalName = null;
	public Boolean hidden = null;
	/*public String tableConstraint = null;
	public String fieldName = null;
	public String keyName = null;*/
	public Field field = null;
	public Boolean externalPointer = null;
	/*public String pointerDatasetName = null;
	public String pointerElementName = null;
	public String pointerInterface = null;*/
	public PointerInfo pointerInfo = null;
	
	public void setPointer(String currentDatasetName, String pointerDatasetName, String pointerElementName, String pointerInterface) {
		this.externalPointer = !currentDatasetName.equals(pointerDatasetName);
		/*this.pointerDatasetName = pointerDatasetName;
		this.pointerElementName = pointerElementName;
		this.pointerInterface = pointerInterface;*/
		this.pointerInfo = new PointerInfo(pointerDatasetName, pointerElementName, pointerInterface);
	}
	
	@Override
	public boolean equals(Object element) {
		return 
		this.internalName.equals(((Element)element).internalName) && 
		this.field.equals(((Element)element).field);/*
		this.tableConstraint.equals(((Element)element).tableConstraint) &&
		this.fieldName.equals(((Element)element).fieldName) &&
		this.keyName.equals(((Element)element).keyName);*/
	}
	@Override
	public String toString() {
		return 
		"internalName = " + internalName + 
		"hidden = " + hidden + 
		", " + "field = " + field;
		/*", " + "tableConstraint = " + tableConstraint +
		", " + "fieldName = " + fieldName +
		", " + "keyName = " + keyName;*/
	}
	public Element(String name, String hidden,/*String tableName, String fieldName, String keyName*/ Field field) {
		super();
		this.internalName = name;
		this.hidden = Boolean.valueOf(hidden);	// in the form: "true" or "false", not "1" or "0"
		this.field = field;
		this.externalPointer = false;
		/*this.tableConstraint = tableName;
		this.fieldName = fieldName;
		this.keyName = keyName;*/
	}
	/*public boolean isCounterpart(Element element) {
		return this.tableConstraint.equals(element.tableConstraint) &&
		this.fieldName.equals(element.fieldName) &&
		this.keyName.equals(element.keyName);
	}*/
}