package org.biomart.builder.model;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import org.biomart.builder.exceptions.ValidationException;
import org.biomart.common.exceptions.DataModelException;

/**
 * A column on a dataset table that is inherited from a parent dataset
 * table.
 */
public  class InheritedColumn extends DataSetColumn {
	private static final long serialVersionUID = 1L;

	private final DataSetColumn dsColumn;

	private final PropertyChangeListener listener = new PropertyChangeListener() {
		public void propertyChange(final PropertyChangeEvent e) {
			InheritedColumn.this.pcs.firePropertyChange(e
					.getPropertyName(), e.getOldValue(), e
					.getNewValue());
		}
	};

	/**
	 * This constructor gives the column a name. The underlying relation
	 * is not required here. The name is inherited from the column too.
	 * 
	 * @param dsTable
	 *            the dataset table to add the wrapped column to.
	 * @param dsColumn
	 *            the column to inherit.
	 */
	public InheritedColumn(final DataSetTable dsTable,
			DataSetColumn dsColumn) {
		// The super constructor will make the alias for us.
		super(dsColumn.getModifiedName(), dsTable);
		// Remember the inherited column.
		this.dsColumn = dsColumn;
		this.visibleModified = dsColumn.visibleModified;

		dsColumn.addPropertyChangeListener("columnMasked",
				this.listener);
		dsColumn.addPropertyChangeListener("columnRename",
				this.listener);
	}

	protected DataSetColumn doReplicate(final DataSetTable copyT)
			throws DataModelException, ValidationException {
		// These should be reconstructed automatically. ????
		return null;
	}

	/**
	 * Returns the column that has been inherited by this column.
	 * 
	 * @return the inherited column.
	 */
	public DataSetColumn getInheritedColumn() {
		return this.dsColumn;
	}

	public String getModifiedName() {
		return this.dsColumn.getModifiedName();
	}

	public boolean isColumnMasked() {
		return this.dsColumn.isColumnMasked();
	}

	public void setColumnMasked(final boolean columnMasked)
			throws ValidationException {
		this.dsColumn.setColumnMasked(columnMasked);
	}

	public void setColumnRename(final String columnRename, final boolean userRequest)
			throws ValidationException {
		this.dsColumn.setColumnRename(columnRename, userRequest);
	}

	public boolean existsForPartition(final String schemaPrefix) {
		return this.dsColumn.existsForPartition(schemaPrefix);
	}
}
