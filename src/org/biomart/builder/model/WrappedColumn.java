package org.biomart.builder.model;

import org.biomart.builder.exceptions.ValidationException;
import org.biomart.common.exceptions.DataModelException;

/**
 * A column on a dataset table that wraps an existing column but is
 * otherwise identical to a normal column. It assigns itself an alias if
 * the original name is already used in the dataset table.
 */
public class WrappedColumn extends DataSetColumn {
	private static final long serialVersionUID = 1L;

	private final Column column;

	/**
	 * This constructor wraps an existing column. It also assigns an
	 * alias to the wrapped column if another one with the same name
	 * already exists on this table.
	 * 
	 * @param column
	 *            the column to wrap.
	 * @param colName
	 *            the name to give the wrapped column.
	 * @param dsTable
	 *            the dataset table to add the wrapped column to.
	 */
	public WrappedColumn(final Column column, final String colName,
			final DataSetTable dsTable) {
		// Call the parent which will use the alias generator for us.
		super(colName, dsTable);

		// Remember the wrapped column.
		this.column = column;
	}

	protected DataSetColumn doReplicate(final DataSetTable copyT)
			throws DataModelException, ValidationException {
		// Construct a new table.
		final WrappedColumn copyC = new WrappedColumn(this.column, this
				.getName(), copyT);
		return copyC;
	}

	/**
	 * Returns the wrapped column, the original column
	 * 
	 * @return the wrapped {@link Column}.
	 */
	public Column getWrappedColumn() {
		return this.column;
	}

	public boolean existsForPartition(final String schemaPrefix) {
		return true;
	}
}
