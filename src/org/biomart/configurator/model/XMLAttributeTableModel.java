/*
	Copyright (C) 2003 EBI, GRL

	This library is free software; you can redistribute it and/or
	modify it under the terms of the GNU Lesser General Public
	License as published by the Free Software Foundation; either
	version 2.1 of the License, or (at your option) any later version.

	This library is distributed in the hope that it will be useful,
	but WITHOUT ANY WARRANTY; without even the implied warranty of
	MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
	Lesser General Public License for more details.

	You should have received a copy of the GNU Lesser General Public
	License along with this library; if not, write to the Free Software
	Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
*/

package org.biomart.configurator.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import javax.swing.JOptionPane;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.AbstractTableModel;

import org.biomart.common.resources.Resources;
import org.biomart.configurator.jdomUtils.JDomNodeAdapter;
import org.jdom.Attribute;



/**
 * Class DatasetConfigAttributeTableModel implementing TableModel.
 *
 * <p>This class is written for the attributes table to implement autoscroll
 * </p>
 *
 * @author <a href="mailto:katerina@ebi.ac.uk">Katerina Tzouvara</a>
 * //@see org.ensembl.mart.config.DatasetConfig
 */

public class XMLAttributeTableModel extends AbstractTableModel {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private String[] columnNames = { "Attribute", "Value" };
	private static final int COLUMN_COUNT = 2;
//	protected BaseConfigurationObject obj;
	private List<Attribute> dataObj;
	protected String objClass;
	protected String[] firstColumnData;
	protected int[] requiredFields;
	private JDomNodeAdapter treeNode;

/*	public XMLAttributeTableModel(List<Attribute> list) {
		this.dataObj = list;
	}
	*/
	public XMLAttributeTableModel(JDomNodeAdapter treeNode) {
		if(treeNode==null)
			return;
		this.treeNode = treeNode;
		this.dataObj = treeNode.getNode().getAttributes();
	}

	public void setDataOject(List<Attribute> list) {
		this.dataObj = list;
	}
	
	public Class getColumnClass(int columnIndex) {
		//Returns the most specific superclass for all the cell values in the column.
		try {
			return Class.forName("java.lang.String");
		} catch (Exception e) {
			return null;
		}
	}

	public int getColumnCount() {
		//Returns the number of columns in the model.
		return COLUMN_COUNT;
	}
	
	public int[] getRequiredFields() {
		return requiredFields;
	}

	public String getColumnName(int columnIndex) {
		//Returns the name of the column at columnIndex.
		return columnNames[columnIndex];
	}

	public int getRowCount() {
		//Returns the number of rows in the model.
		if(dataObj==null) return 0;
		return dataObj.size();
	}

	public Object getValueAt(int rowIndex, int columnIndex) {
		//Returns the value for the cell at columnIndex and rowIndex.
		if (columnIndex == 0) {
			return dataObj.get(rowIndex).getName();
		} else {
			return dataObj.get(rowIndex).getValue();
		}
	}

	public boolean isCellEditable(int rowIndex, int columnIndex) {
		//Returns true if the cell at rowIndex and columnIndex is editable.
		if (columnIndex == 0)
			return false;
		return true;
	}

	public void setValueAt(Object value, int row, int col) {
		if(!validate(value, row, col))
			return;
		dataObj.get(row).setValue(value.toString());
		fireTableCellUpdated(row,col);
	}
	
	private boolean validate(Object value, int row, int col) {
		boolean res = true;
		//check if it is partitioned
/*		String partitionTable = this.treeNode.getNode().getAttributeValue(Resources.get("PARTITIONTABLE"));
		if(partitionTable==null || partitionTable.equals(""))
			res = true;
		else {
			if(dataObj.get(row).getName().equals(Resources.get("NAME"))) {
				if(dataObj.get(row).getValue().indexOf("("+partitionTable+")")<0)
					return false;
			}
		}
		*/
		return res;
	}
}
