package org.biomart.configurator.model.object;

import org.biomart.builder.model.DataSet;
import org.biomart.builder.model.DataSetColumn;
import org.biomart.builder.model.Table;
import org.biomart.configurator.model.Location;
import org.biomart.configurator.utils.McGuiUtils;

public class McDsColumn extends McObject {
	private McDsTable mcDsTable;
	
	public McDsColumn(McDsTable table, String name) {
		this.mcDsTable = table;
		this.name = name;
	}
	
	public String toString() {
		return this.name;
	}
	
	public String getLocationName() {
		return this.mcDsTable.getLocationName();
	}
	
	public String getMartName() {
		return this.mcDsTable.getMartName();
	}
	
	public String getDataSetName() {
		return this.mcDsTable.getDataSetName();
	}
	
	public String getDataSetTableName() {
		return this.mcDsTable.getName();
	}
	
	public DataSet getDataSet() {
		return McGuiUtils.INSTANCE.getCurrentUser().getLocation(this.mcDsTable.getLocationName()).
			getMart(this.mcDsTable.getMartName()).getDataSet(this.mcDsTable.getDataSetName());
	}
	
	public Table getDsTable() {
		DataSet ds = this.getDataSet();
		return (Table)ds.getTables().get(this.mcDsTable.getName());
	}
	
	public DataSetColumn getDsColumn() {
		Table table = this.getDsTable();
		return (DataSetColumn)table.getColumns().get(this.name);
	}
	
	public Location getLocation() {
		return McGuiUtils.INSTANCE.getCurrentUser().getLocation(this.mcDsTable.getLocationName());
	}
}