package org.biomart.builder.model;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;


import org.biomart.builder.exceptions.ValidationException;

import org.biomart.common.exceptions.DataModelException;
import org.biomart.common.resources.Log;
import org.biomart.common.resources.Resources;
import org.biomart.configurator.utils.McEventObject;
import org.biomart.configurator.utils.type.EventType;
import org.biomart.configurator.utils.type.IdwViewType;
import org.biomart.configurator.view.idwViews.McViews;

	/**
	 * A column on a dataset table has to be one of the types of dataset column
	 * available from this class.
	 * 
	 * DataSetColumns don't change, and so they don't provide any change
	 * listeners of any kind. They are always (re)created from scratch.
	 */
	public class DataSetColumn extends Column {
		private static final long serialVersionUID = 1L;

		private boolean keyDependency;

		private boolean expressionDependency;
		private final List partitionCols = new ArrayList();

		private TransformationUnit tu;

		private final PropertyChangeListener listener = new PropertyChangeListener() {
			public void propertyChange(final PropertyChangeEvent e) {
				DataSetColumn.this.setDirectModified(true);
			}
		};

		/**
		 * This constructor gives the column a name.
		 * 
		 * @param name
		 *            the name to give this column.
		 * @param dsTable
		 *            the parent dataset table.
		 */
		public DataSetColumn(final String name, final DataSetTable dsTable) {
			// Call the super constructor using the alias generator to
			// ensure we have a unique name.
			super(dsTable, name);

			Log.debug("Creating dataset column " + name + " of type "
					+ this.getClass().getName());

			// Set up default mask/partition values.
			this.keyDependency = false;
			this.expressionDependency = false;

			// Listen to own settings.
			this.addPropertyChangeListener("columnMasked", this.listener);
			this.addPropertyChangeListener("columnRename", this.listener);
			this.addPropertyChangeListener("columnIndexed", this.listener);
			this.addPropertyChangeListener("splitOptimiserColumn",
					this.listener);
		}


		/**
		 * Construct a new instance of ourselves and return a copy.
		 * 
		 * @param copyT
		 *            the copy dataset table to copy into.
		 * @return the copy of ourselves.
		 * @throws ValidationException
		 *             if we could not do it.
		 * @throws DataModelException
		 *             if we could not do it.
		 */
		protected DataSetColumn doReplicate(final DataSetTable copyT)
				throws DataModelException, ValidationException {
			// Construct a new table.
			final DataSetColumn copyC = new DataSetColumn(this.getName(), copyT);
			return copyC;
		}

		/**
		 * Set the transformation unit causing this column to exist.
		 * 
		 * @param tu
		 *            the unit.
		 */
		public void setTransformationUnit(final TransformationUnit tu) {
			this.tu = tu;
		}

		/**
		 * Get the transformation unit causing this column to exist.
		 * 
		 * @return the unit.
		 */
		public TransformationUnit getTransformationUnit() {
			return this.tu;
		}

		/**
		 * Obtain the dataset this column belongs to.
		 * 
		 * @return the dataset it belongs to.
		 */
		public DataSet getDataSet() {
			return (DataSet) this.getTable().getSchema();
		}

		/**
		 * Obtain the dataset table this column belongs to.
		 * 
		 * @return the dataset table it belongs to.
		 */
		public DataSetTable getDataSetTable() {
			return (DataSetTable) this.getTable();
		}

		/**
		 * Get the named set of properties for this column.
		 * 
		 * @param property
		 *            the property to look up.
		 * @return the set of column names the property applies to.
		 */
		protected Map getMods(final String property) {
			return this.getDataSet().getMods(this.getDataSetTable().getName(),
					property);
		}

		/**
		 * Update the partition column list on this column to use the names.
		 * 
		 * @param partCols
		 *            the new list of partition column names that apply.
		 */
		public void setPartitionCols(final List partCols) {
			this.partitionCols.clear();
			this.partitionCols.addAll(partCols);
		}

		/**
		 * Get the name of this column after partitioning has been applied. Must
		 * call {@link #} first
		 * else it will delegate to {@link #getModifiedName()}.
		 * 
		 * @return the partitioned name.
		 */
		public String getPartitionedName() {
			return this.getModifiedName();
		}

		/**
		 * Test to see if this column exists for the given partition.
		 * 
		 * @param schemaPrefix
		 *            the schema prefix to test for.
		 * @return <tt>true</tt> if it is.
		 */
		public boolean existsForPartition(final String schemaPrefix) {
			return this.tu == null || this.tu.appliesToPartition(schemaPrefix);
		}

		/**
		 * Test to see if this column is required during intermediate
		 * construction phases.
		 * 
		 * @return <tt>true</tt> if it is.
		 */
		public boolean isRequiredInterim() {
			return this.keyDependency || this.expressionDependency
					|| !this.isColumnMasked();
		}

		/**
		 * Test to see if this column is required in the final completed dataset
		 * table.
		 * 
		 * @return <tt>true</tt> if it is.
		 */
		public boolean isRequiredFinal() {
			// Masked columns are not final.
			if (this.isColumnMasked())
				return false;
			// By default if we reach here, we are final.
			return true;
		}

		/**
		 * Return this modified name including any renames etc.
		 * 
		 * @return the modified name.
		 */
		public String getModifiedName() {
			String name = this.getColumnRename();
			if (name == null)
				name = this.getName();
			// UC/LC/Mixed?
			switch (this.getDataSet().getMart().getCase()) {
			case LOWER:
				return name.toLowerCase();
			case UPPER:
				return name.toUpperCase();
			default:
				return name;
			}
		}

		/**
		 * Changes the dependency flag on this column.
		 * 
		 * @param dependency
		 *            the new dependency flag. <tt>true</tt> indicates that
		 *            this column is required for the fundamental structure of
		 *            the dataset table to exist. The column will get selected
		 *            regardless of it's masking flag. However, if it is masked,
		 *            it will be removed again after the dependency is
		 *            satisified.
		 */
		public void setKeyDependency(final boolean dependency) {
			this.keyDependency = dependency;
		}

		/**
		 * Changes the dependency flag on this column.
		 * 
		 * @param dependency
		 *            the new dependency flag. <tt>true</tt> indicates that
		 *            this column is required for the fundamental structure of
		 *            the dataset table to exist. The column will get selected
		 *            regardless of it's masking flag. However, if it is masked,
		 *            it will be removed again after the dependency is
		 *            satisified.
		 */
		public void setExpressionDependency(final boolean dependency) {
			this.expressionDependency = dependency;
		}

		/**
		 * Is this column required as a dependency?
		 * 
		 * @return <tt>true</tt> if it is.
		 */
		public boolean isKeyDependency() {
			return this.keyDependency;
		}

		/**
		 * Is this column required as a dependency?
		 * 
		 * @return <tt>true</tt> if it is.
		 */
		public boolean isExpressionDependency() {
			return this.expressionDependency;
		}

		boolean isKeyCol() {
			// Are we in our table's PK or FK?
			final Set cols = new HashSet();
			for (final Iterator i = this.getDataSetTable().getKeys().iterator(); i
					.hasNext();)
				cols.addAll(Arrays.asList(((Key) i.next()).getColumns()));
			return cols.contains(this);
		}

		/**
		 * Is this a masked column?
		 * 
		 * @return <tt>true</tt> if it is.
		 */
		public boolean isColumnMasked() {
			return this.getMods("columnMasked").containsKey(this.getName());
		}

		/**
		 * Mask this column.
		 * 
		 * @param columnMasked
		 *            <tt>true</tt> to mask.
		 * @throws ValidationException
		 *             if masking is not possible.
		 */
		public void setColumnMasked(final boolean columnMasked)
				throws ValidationException {
			final boolean oldValue = this.isColumnMasked();
			if (columnMasked == oldValue)
				return;
			if (columnMasked && this.isKeyCol())
				throw new ValidationException(Resources
						.get("cannotMaskNecessaryColumn"));
			if (columnMasked)
				this.getMods("columnMasked").put(this.getName().intern(), null);
			else
				this.getMods("columnMasked").remove(this.getName());
			this.pcs.firePropertyChange("columnMasked", oldValue, columnMasked);
			//notify mctree
			McEventObject obj = new McEventObject(EventType.Update_DSColumnMasked, this);
			McViews.getInstance().getView(IdwViewType.SCHEMA).getController().processV2Cupdate(obj);
		}

		/**
		 * Is this an indexed column?
		 * 
		 * @return <tt>true</tt> if it is.
		 */
		public boolean isColumnIndexed() {
			return this.getMods("columnIndexed").containsKey(this.getName());
		}

		/**
		 * Index this column.
		 * 
		 * @param columnIndexed
		 *            <tt>true</tt> to index.
		 */
		public void setColumnIndexed(final boolean columnIndexed) {
			final boolean oldValue = this.isColumnIndexed();
			if (columnIndexed == oldValue)
				return;
			if (columnIndexed)
				this.getMods("columnIndexed")
						.put(this.getName().intern(), null);
			else
				this.getMods("columnIndexed").remove(this.getName());
			this.pcs.firePropertyChange("columnIndexed", oldValue,
					columnIndexed);
		}

		/**
		 * Is this an split optimiser column?
		 * 
		 * @return the definition if it is.
		 */
		public SplitOptimiserColumnDef getSplitOptimiserColumn() {
			return (SplitOptimiserColumnDef) this.getMods(
					"splitOptimiserColumn").get(this.getName());
		}

		/**
		 * Is this a renamed column?
		 * 
		 * @return <tt>null</tt> if it is not, otherwise return the new name.
		 */
		public String getColumnRename() {
			return (String) this.getMods("columnRename").get(this.getName());
		}

		/**
		 * Rename this column.
		 * 
		 * @param columnRename
		 *            the new name, or <tt>null</tt> to undo it.
		 * @param userRequest
		 *            <tt>true</tt> if this is a user request, <tt>false</tt> if not.
		 * @throws ValidationException
		 *             if it could not be done.
		 */
		public void setColumnRename(String columnRename, final boolean userRequest)
				throws ValidationException {
			String oldValue = this.getColumnRename();
			if (oldValue == null)
				oldValue = this.getModifiedName();
			if (oldValue.equals(columnRename))
				return;
			// Make the name unique.
			if (columnRename != null) {
				final Set entries = new HashSet();
				// Get renames of siblings.
				for (final Iterator i = this.getTable().getColumns().values()
						.iterator(); i.hasNext();)
					entries.add(((DataSetColumn) i.next()).getModifiedName());
				entries.remove(oldValue);
				// First we need to find out the base name, ie. the bit
				// we append numbers to make it unique, but before any
				// key suffix. If we appended numbers after the key
				// suffix then it would confuse MartEditor.
				String keySuffix = Resources.get("keySuffix");
				String baseName = columnRename;
				if (columnRename.endsWith(keySuffix)) {
					baseName = columnRename.substring(0, columnRename
							.indexOf(keySuffix));
				}
				if (!this.isKeyCol())
					keySuffix = "";
				columnRename = baseName + keySuffix;
				// Now, if the old name has a partition prefix, and the
				// new one doesn't, reinstate or replace it.
				if (this.getName().indexOf("__") >= 0) {
					if (columnRename.indexOf("__") >= 0)
						columnRename = columnRename.substring(columnRename
								.lastIndexOf("__") + 2);
					columnRename = this.getName().substring(0,
							this.getName().lastIndexOf("__") + 2)
							+ columnRename;
				}
				// Remove numbered prefix if its snuck back in.
				if (columnRename.indexOf(".")>=0) 
					columnRename = columnRename.substring(columnRename
							.lastIndexOf(".") + 1);
				// Now simply check to see if the name is used, and
				// then add an incrementing number to it until it is unique.
				int suffix = 1;
				while (entries.contains(columnRename))
					columnRename = baseName + '_' + (userRequest?'c':'r') + suffix++ + keySuffix;
			}
			// Check and change it.
			if (oldValue.equals(columnRename))
				return;
			if (columnRename != null)
				this.getMods("columnRename").put(this.getName().intern(),
						columnRename);
			else
				this.getMods("columnRename").remove(this.getName());
			this.pcs.firePropertyChange("columnRename", oldValue, columnRename);
		}
	}
