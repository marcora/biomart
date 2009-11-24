package org.biomart.configurator.component;

import java.util.ArrayList;
import java.util.List;

import javax.swing.table.AbstractTableModel;

import org.biomart.configurator.utils.McEventObject;
import org.biomart.configurator.utils.type.EventType;
import org.biomart.configurator.utils.type.IdwViewType;
import org.biomart.configurator.view.idwViews.McViews;

public class PtModel extends AbstractTableModel {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private final String col = "col";
	private List<ArrayList<String>> data;
	private List<String> columns;
	private String partitionTableName;
	
	public PtModel() {
		
	}
	
	public PtModel(List<ArrayList<String>> data, String tableName) {
		this.data = data;
		this.partitionTableName = tableName;
		this.recalculateColumns();
	}
	
	public int getColumnCount() {
		return columns.size();
	}

	public int getRowCount() {
		return data.size();
	}

	public Object getValueAt(int x, int y) {
		return data.get(x).get(y);
	}
	
	public String getColumnName(int col) {
		return columns.get(col);
	}
	
	private void recalculateColumns() {
		columns = new ArrayList<String>();
		if(data.size()>0) {
			int colSize = data.get(0).size();
			for(int i=1; i<=colSize; i++) {
				columns.add(this.col+i);
			}
		}
	}
	
	public List<String> getColumn(int col) {
		List<String> colList = new ArrayList<String>();
		for(ArrayList<String> row:data) {
			colList.add(row.get(col));
		}
		return colList;
	}
	
	public void addColumn(List<String> colList) {
		for(int i=0; i<this.getRowCount(); i++) {
			data.get(i).add(colList.get(i));
		}
		this.recalculateColumns();
		this.fireTableStructureChanged();
	}
	
	public void cloneColumn(int sourceCol) {
		for(ArrayList<String> row: data) {
			row.add(row.get(sourceCol));
		}
		this.recalculateColumns();
		this.fireTableStructureChanged();
		//update mctree
		List<String> newCol = this.getColumn(this.getColumnCount()-1);
		McEventObject obj = new McEventObject(EventType.Update_PartitionTable,newCol);
		obj.SetContextString(this.partitionTableName);
		McViews.getInstance().getView(IdwViewType.MCTREE).getController().processV2Cupdate(obj);
	}

	public boolean isCellEditable(int rowIndex, int columnIndex) {
		//Returns true if the cell at rowIndex and columnIndex is editable.
		if (columnIndex == 0)
			return false;
		return true;
	}
	
	public void setValueAt(Object value, int row, int col) {
		data.get(row).set(col, (String)value);
		fireTableCellUpdated(row,col);
	}
	
	public String getPartitionTableName() {
		return this.partitionTableName;
	}

}