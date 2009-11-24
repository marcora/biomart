package org.biomart.builder.model;

import java.util.Iterator;

/**
 * This type of transformation selects columns from a single table.
 */
public  class SelectFromTable extends TransformationUnit {
	private static final long serialVersionUID = 1L;

	//it is a source table
	private final Table table;

	protected SelectFromTable(final TransformationUnit previousUnit,
			final Table table) {
		super(previousUnit);
		this.table = table;
	}

	/**
	 * Instantiate a unit that selects from the given schema table.
	 * 
	 * @param table
	 *            the table this unit selects from.
	 */
	public SelectFromTable(final Table table) {
		super(null);
		this.table = table;
	}

	public boolean appliesToPartition(final String schemaPrefix) {
		return super.appliesToPartition(schemaPrefix);
	}

	/**
	 * Find out which schema table this unit selects from.
	 * 
	 * @return the schema table this unit selects from.
	 */
	public Table getTable() {
		return this.table;
	}

	private boolean columnMatches(final Column column, DataSetColumn dsCol) {
		if (dsCol == null)
			return false;
		while (dsCol instanceof InheritedColumn)
			dsCol = ((InheritedColumn) dsCol).getInheritedColumn();
		if (dsCol instanceof WrappedColumn)
			return ((WrappedColumn) dsCol).getWrappedColumn()
					.equals(column);
		return false;
	}

	public DataSetColumn getDataSetColumnFor(final Column column) {
		DataSetColumn candidate = (DataSetColumn) this
				.getNewColumnNameMap().get(column);
		if (candidate == null)
			// We need to check each of our columns to see if they
			// are dataset columns, and if so, if they point to
			// the appropriate real column.
			for (final Iterator<DataSetColumn> i = this.getNewColumnNameMap().values()
					.iterator(); i.hasNext() && candidate == null;) {
				candidate = (DataSetColumn) i.next();
				if (!this.columnMatches(column, candidate))
					candidate = null;
			}
		return candidate;
	}
}
