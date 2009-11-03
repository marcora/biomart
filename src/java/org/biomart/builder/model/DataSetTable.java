package org.biomart.builder.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.biomart.builder.exceptions.ValidationException;
import org.biomart.builder.model.ForeignKey;

import org.biomart.builder.model.Relation.CompoundRelationDefinition;
import org.biomart.builder.model.TransformationUnit.JoinTable;
import org.biomart.builder.model.TransformationUnit.SelectFromTable;
import org.biomart.common.resources.Log;
import org.biomart.common.resources.Resources;
import org.biomart.configurator.utils.type.DataSetTableType;

	/**
	 * This special table represents the merge of one or more other tables by
	 * following a series of relations rooted in a similar series of keys. As
	 * such it has no real columns of its own, so every column is from another
	 * table and is given an alias. The tables don't last, as they are
	 * (re)created from scratch every time, and so they don't need to implement
	 * any kind of change control.
	 */
	public class DataSetTable extends Table {
		private static final long serialVersionUID = 1L;

		private final List<TransformationUnit> transformationUnits;

		private DataSetTableType type;

		private final Table focusTable;

		private final Relation focusRelation;

		private final int focusRelationIteration;

		 final Collection<Relation> includedRelations;

		 final Collection<Table> includedTables;

		 final Collection<Schema> includedSchemas;

		/**
		 * The constructor calls the parent table constructor. It uses a dataset
		 * as a parent schema for itself. You must also supply a type that
		 * describes this as a main table, dimension table, etc.
		 * 
		 * @param name
		 *            the table name.
		 * @param ds
		 *            the dataset to hold this table in.
		 * @param type
		 *            the type that best describes this table.
		 * @param focusTable
		 *            the schema table this dataset table starts from.
		 * @param focusRelation
		 *            the schema relation used to reach the focus table. Can be
		 *            <tt>null</tt>.
		 * @param focusRelationIteration
		 *            the schema relation iteration.
		 */
		public DataSetTable(final String name, final DataSet ds,
				final DataSetTableType type, final Table focusTable,
				final Relation focusRelation, final int focusRelationIteration) {
			// Super constructor first, using an alias to prevent duplicates.
			super(ds, name);
			Log.debug("Creating dataset table " + name);

			// Remember the other settings.
			this.type = type;
			this.focusTable = focusTable;
			this.focusRelation = focusRelation;
			this.focusRelationIteration = focusRelationIteration;
			this.transformationUnits = new ArrayList<TransformationUnit>();
			this.includedRelations = new LinkedHashSet<Relation>();
			this.includedTables = new LinkedHashSet<Table>();
			this.includedSchemas = new LinkedHashSet<Schema>();
			// Listen to own settings.
			this.addPropertyChangeListener("type", this.listener);
			this.addPropertyChangeListener("tableRename", this.listener);
			this.addPropertyChangeListener("dimensionMasked", this.listener);
			this.addPropertyChangeListener("distinctTable", this.listener);
			this.addPropertyChangeListener("noFinalLeftJoin", this.listener);
			this.addPropertyChangeListener("skipOptimiser", this.listener);
			this.addPropertyChangeListener("skipIndexOptimiser", this.listener);
			this.addPropertyChangeListener("partitionTableApplication",
					this.listener);
			this.addPropertyChangeListener("tableHideMasked", this.listener);

			// Make dataset rebuild when our columns change externally.
			this.getColumns().addPropertyChangeListener(
					this.getDataSet().rebuildListener);
		}


		/**
		 * Does this dataset table exist for the given partition?
		 * 
		 * @param schemaPrefix
		 *            the partition prefix.
		 * @return <tt>true</tt> if it does.
		 */
		public boolean existsForPartition(final String schemaPrefix) {
			return true;
		}

		/**
		 * Accept changes associated with columns from the target table. If the
		 * target table is null, all changes are accepted. All affected columns
		 * have their visible modified flag reset.
		 * 
		 * @param targetTable
		 *            the target table.
		 */
		public void acceptChanges(final Table targetTable) {
			this.acceptRejectChanges(targetTable, false);
		}

		/**
		 * Reject changes associated with columns from the target table. If the
		 * target table is null, all changes are rejected. Rejection means that
		 * if the relation was modified, the relation is masked. Otherwise, the
		 * affected columns are masked instead. All affected columns have their
		 * visible modified flag reset.
		 * 
		 * @param targetTable
		 *            the target table.
		 */
		public void rejectChanges(final Table targetTable) {
			this.acceptRejectChanges(targetTable, true);
		}

		private void acceptRejectChanges(final Table targetTable,
				final boolean reject) {
			// Reset all keys.
			for (final Iterator i = this.getKeys().iterator(); i.hasNext();)
				((Key) i.next()).transactionResetVisibleModified();
			// Locate the TU that provides the target table.
			for (final Iterator i = this.getTransformationUnits().iterator(); i
					.hasNext();) {
				final TransformationUnit tu = (TransformationUnit) i.next();
				if (tu instanceof SelectFromTable
						&& (targetTable == null || targetTable != null
								&& (((SelectFromTable) tu).getTable().equals(
										targetTable) || !this.getType().equals(
										DataSetTableType.MAIN)
										&& this.getFocusRelation().getOneKey()
												.getTable().equals(targetTable)))) {
					final SelectFromTable st = (SelectFromTable) tu;
					// Find all new columns from the TU.
					for (final Iterator j = st.getNewColumnNameMap().values()
							.iterator(); j.hasNext();) {
						final DataSetColumn dsCol = (DataSetColumn) j.next();
						// Is it new?
						if (!dsCol.isVisibleModified())
							continue;
						// Reset visible modified on all of them.
						dsCol.setVisibleModified(false);
						// Are we rejecting?
						if (reject)
							// Mask it.
							try {
								dsCol.setColumnMasked(true);
							} catch (final ValidationException ve) {
								// Ignore - if we can't mask it, it's because
								// it's important.
							}
					}
				}
			}
			// Only reset parent relation if has one and all columns
			// on this table are now not modified.
			if (this.getType() != DataSetTableType.MAIN) {
				for (final Iterator i = this.getColumns().values().iterator(); i
						.hasNext();)
					if (((Column) i.next()).isVisibleModified())
						return;
				// Find parent relation and reset that.
				Relation rel = null;
				for (final Iterator i = this.getForeignKeys().iterator(); i
						.hasNext()
						&& rel == null;)
					for (final Iterator j = ((Key) i.next()).getRelations()
							.iterator(); j.hasNext() && rel == null;)
						rel = (Relation) j.next();
				// Reset it.
				rel.setVisibleModified(false);
			}
			// Mask this table if it has no unmasked columns left.
			if (this.getType().equals(DataSetTableType.MAIN))
				return;
			for (final Iterator i = this.getColumns().values().iterator(); i
					.hasNext();) {
				final DataSetColumn dsCol = (DataSetColumn) i.next();
				if ( dsCol instanceof WrappedColumn
						&& !dsCol.isColumnMasked())
					return;
			}
			try {
				this.setDimensionMasked(true);
			} catch (final Throwable e) {
				// Don't care.
			}
		}

		/**
		 * Do any of the current visibly modified columns on this dataset table
		 * come from the specified source table?
		 * 
		 * @param table
		 *            the table to check.
		 * @return true if they do.
		 */
		public boolean hasVisibleModifiedFrom(final Table table) {
			for (final Iterator i = this.getColumns().values().iterator(); i
					.hasNext();) {
				final DataSetColumn dsCol = (DataSetColumn) i.next();
				if (!dsCol.isVisibleModified())
					continue;
				if (dsCol instanceof WrappedColumn
						&& ((WrappedColumn) dsCol).getWrappedColumn()
								.getTable().equals(table))
					return true;
			}
			return false;
		}

		/**
		 * Obtain the dataset this table belongs to.
		 * 
		 * @return the dataset it belongs to.
		 */
		public DataSet getDataSet() {
			return (DataSet) this.getSchema();
		}

		/**
		 * Get the named set of properties for this column.
		 * 
		 * @param property
		 *            the property to look up.
		 * @return the set of column names the property applies to.
		 */
		protected Map getMods(final String property) {
			return this.getDataSet().getMods(this.getName(), property);
		}

		/**
		 * Obtain all tables used by this dataset table in the order they were
		 * used.
		 * 
		 * @return all tables.
		 */
		public Collection getIncludedTables() {
			return this.includedTables;
		}

		/**
		 * Obtain all relations used by this dataset table in the order they
		 * were used.
		 * 
		 * @return all relations.
		 */
		public Collection getIncludedRelations() {
			return this.includedRelations;
		}

		/**
		 * Find out what schemas are used in this dataset table.
		 * 
		 * @return the set of schemas used.
		 */
		public Collection getIncludedSchemas() {
			return this.includedSchemas;
		}

		/**
		 * Return this modified name including any renames etc.
		 * 
		 * @return the modified name.
		 */
		public String getModifiedName() {
			String name = this.getTableRename();
			if (name == null)
				name = this.getName();
			// UC/LC/Mixed?
			switch (((DataSet) this.getSchema()).getMart().getCase()) {
			case LOWER:
				return name.toLowerCase();
			case UPPER:
				return name.toUpperCase();
			default:
				return name;
			}
		}

		/**
		 * Obtain the focus relation for this dataset table. The focus relation
		 * is the one which the transformation uses to reach the focus table.
		 * 
		 * @return the focus relation.
		 */
		public Relation getFocusRelation() {
			return this.focusRelation;
		}

		/**
		 * Obtain the focus relation iteration for this dataset table.
		 * 
		 * @return the focus relation iteration.
		 */
		public int getFocusRelationIteration() {
			return this.focusRelationIteration;
		}

		/**
		 * Obtain the focus table for this dataset table. The focus table is the
		 * one which the transformation starts from.
		 * 
		 * @return the focus table.
		 */
		public Table getFocusTable() {
			return this.focusTable;
		}

		/**
		 * Adds a transformation unit to the end of the chain.
		 * 
		 * @param tu
		 *            the unit to add.
		 */
		void addTransformationUnit(final TransformationUnit tu) {
			this.transformationUnits.add(tu);
		}

		/**
		 * Gets the ordered list of transformation units.
		 * 
		 * @return the list of units.
		 */
		public List<TransformationUnit> getTransformationUnits() {
			return this.transformationUnits;
		}

		/**
		 * Returns the type of this table specified at construction time.
		 * 
		 * @return the type of this table.
		 */
		public DataSetTableType getType() {
			return this.type;
		}

		/**
		 * Changes the type of this table specified at construction time.
		 * 
		 * @param type
		 *            the type of this table. Use with care.
		 */
		public void setType(final DataSetTableType type) {
			final DataSetTableType oldValue = this.getType();
			if (type == oldValue)
				return;
			this.type = type;
			this.pcs.firePropertyChange("type", oldValue, type);
			// Force a complete new rebuild.
			this.getColumns().clear();
		}

		/**
		 * Return the parent table, if any, or <tt>null</tt> if none.
		 * 
		 * @return the parent table.
		 */
		public DataSetTable getParent() {
			for (final Iterator i = this.getForeignKeys().iterator(); i
					.hasNext();) {
				final ForeignKey fk = (ForeignKey) i.next();
				if (fk.getRelations().size() > 0)
					return (DataSetTable) ((Relation) fk.getRelations()
							.iterator().next()).getOneKey().getTable();
			}
			return null;
		}

		/**
		 * Is this table masked in explain table dialog.
		 * 
		 * @return <tt>true</tt> if it is.
		 */
		public boolean isExplainHideMasked() {
			return this.getMods("explainHideMasked")
					.containsKey(this.getName());
		}

		/**
		 * Mask this table in explain table dialog.
		 * 
		 * @param explainHideMasked
		 *            <tt>true</tt> to mask.
		 */
		public void setExplainHideMasked(final boolean explainHideMasked) {
			final boolean oldValue = this.isExplainHideMasked();
			if (explainHideMasked == oldValue)
				return;
			if (explainHideMasked)
				this.getMods("explainHideMasked").put(this.getName().intern(),
						null);
			else
				this.getMods("explainHideMasked").remove(this.getName());
			this.pcs.firePropertyChange("explainHideMasked", oldValue,
					explainHideMasked);
		}

		/**
		 * Is this a masked table?
		 * 
		 * @return <tt>true</tt> if it is.
		 */
		public boolean isDimensionMasked() {
			return this.getMods("dimensionMasked").containsKey(this.getName());
		}

		/**
		 * Mask this table.
		 * 
		 * @param dimensionMasked
		 *            <tt>true</tt> to mask.
		 * @throws ValidationException
		 *             if masking is not possible.
		 */
		public void setDimensionMasked(final boolean dimensionMasked)
				throws ValidationException {
			final boolean oldValue = this.isDimensionMasked();
			if (dimensionMasked == oldValue)
				return;
			if (dimensionMasked
					&& !this.getType().equals(DataSetTableType.DIMENSION))
				throw new ValidationException(Resources
						.get("cannotMaskNonDimension"));
			if (dimensionMasked)
				this.getMods("dimensionMasked").put(this.getName().intern(),
						null);
			else
				this.getMods("dimensionMasked").remove(this.getName());
			this.pcs.firePropertyChange("dimensionMasked", oldValue,
					dimensionMasked);
		}

		/**
		 * Is this a no-optimiser table?
		 * 
		 * @return <tt>true</tt> if it is.
		 */
		public boolean isSkipOptimiser() {
			return this.getMods("skipOptimiser").containsKey(this.getName());
		}

		/**
		 * No-optimiser this table.
		 * 
		 * @param skipOptimiser
		 *            <tt>true</tt> to make no-optimiser.
		 */
		public void setSkipOptimiser(final boolean skipOptimiser) {
			final boolean oldValue = this.isSkipOptimiser();
			if (skipOptimiser == oldValue)
				return;
			if (skipOptimiser)
				this.getMods("skipOptimiser")
						.put(this.getName().intern(), null);
			else
				this.getMods("skipOptimiser").remove(this.getName());
			this.pcs.firePropertyChange("skipOptimiser", oldValue,
					skipOptimiser);
		}

		/**
		 * Is this a no-index-optimiser table?
		 * 
		 * @return <tt>true</tt> if it is.
		 */
		public boolean isSkipIndexOptimiser() {
			return this.getMods("skipIndexOptimiser").containsKey(
					this.getName());
		}

		/**
		 * No-index-optimiser this table.
		 * 
		 * @param skipIndexOptimiser
		 *            <tt>true</tt> to make no-index-optimiser.
		 */
		public void setSkipIndexOptimiser(final boolean skipIndexOptimiser) {
			final boolean oldValue = this.isSkipIndexOptimiser();
			if (skipIndexOptimiser == oldValue)
				return;
			if (skipIndexOptimiser)
				this.getMods("skipIndexOptimiser").put(this.getName().intern(),
						null);
			else
				this.getMods("skipIndexOptimiser").remove(this.getName());
			this.pcs.firePropertyChange("skipIndexOptimiser", oldValue,
					skipIndexOptimiser);
		}

		/**
		 * Is this a no-left-join table?
		 * 
		 * @return <tt>true</tt> if it is.
		 */
		public boolean isNoFinalLeftJoin() {
			return this.getMods("noFinalLeftJoin").containsKey(this.getName());
		}

		/**
		 * No-left-join this table.
		 * 
		 * @param noFinalLeftJoin
		 *            <tt>true</tt> to make no-left-join.
		 */
		public void setNoFinalLeftJoin(final boolean noFinalLeftJoin) {
			final boolean oldValue = this.isNoFinalLeftJoin();
			if (noFinalLeftJoin == oldValue)
				return;
			if (noFinalLeftJoin)
				this.getMods("noFinalLeftJoin").put(this.getName().intern(),
						null);
			else
				this.getMods("noFinalLeftJoin").remove(this.getName());
			this.pcs.firePropertyChange("noFinalLeftJoin", oldValue,
					noFinalLeftJoin);
		}

		/**
		 * Is this a hide-masked table?
		 * 
		 * @return <tt>true</tt> if it is.
		 */
		public boolean isTableHideMasked() {
			return this.getMods("tableHideMasked").containsKey(this.getName());
		}

		/**
		 * Hide-masked this table.
		 * 
		 * @param tableHideMasked
		 *            <tt>true</tt> to make hide-masked.
		 */
		public void setTableHideMasked(final boolean tableHideMasked) {
			final boolean oldValue = this.isTableHideMasked();
			if (tableHideMasked == oldValue)
				return;
			if (tableHideMasked)
				this.getMods("tableHideMasked").put(this.getName().intern(),
						null);
			else
				this.getMods("tableHideMasked").remove(this.getName());
			this.pcs.firePropertyChange("tableHideMasked", oldValue,
					tableHideMasked);
		}

		/**
		 * Is this a distinct table?
		 * 
		 * @return <tt>true</tt> if it is.
		 */
		public boolean isDistinctTable() {
			return this.getMods("distinctTable").containsKey(this.getName());
		}

		/**
		 * Distinct this table.
		 * 
		 * @param distinctTable
		 *            <tt>true</tt> to make distinct.
		 */
		public void setDistinctTable(final boolean distinctTable) {
			final boolean oldValue = this.isDistinctTable();
			if (distinctTable == oldValue)
				return;
			if (distinctTable)
				this.getMods("distinctTable")
						.put(this.getName().intern(), null);
			else
				this.getMods("distinctTable").remove(this.getName());
			this.pcs.firePropertyChange("distinctTable", oldValue,
					distinctTable);
		}


		/**
		 * Is this a renamed table?
		 * 
		 * @return <tt>null</tt> if it is not, otherwise return the new name.
		 */
		public String getTableRename() {
			return (String) this.getMods("tableRename").get(this.getName());
		}

		/**
		 * Rename this table.
		 * 
		 * @param tableRename
		 *            the new name, or <tt>null</tt> to undo it.
		 */
		public void setTableRename(String tableRename) {
			String oldValue = this.getTableRename();
			if (tableRename == oldValue || oldValue != null
					&& oldValue.equals(tableRename))
				return;
			if (oldValue == null)
				oldValue = this.getName();
			// Make the name unique if it has a parent.
			if (tableRename != null && this.getParent() != null) {
				final String baseName = tableRename;
				final Set entries = new HashSet();
				// Get renames of siblings.
				for (final Iterator i = this.getParent().getPrimaryKey()
						.getRelations().iterator(); i.hasNext();)
					entries.add(((DataSetTable) ((Relation) i.next())
							.getManyKey().getTable()).getModifiedName());
				entries.remove(oldValue);
				// Iterate over renamedTables entries.
				// If find an entry with same name, find ds table it refers to.
				// If entry ds table parent = table parent then increment and
				// restart search.
				int suffix = 1;
				while (entries.contains(tableRename))
					tableRename = baseName + "_" + suffix++;
			}
			// Check and change it.
			if (tableRename != null)
				this.getMods("tableRename").put(this.getName().intern(),
						tableRename);
			else
				this.getMods("tableRename").remove(this.getName().intern());
			this.pcs.firePropertyChange("tableRename", oldValue, tableRename);
		}

		/**
		 * What should the next expression column be called?
		 * 
		 * @return the name for it.
		 */
		public String getNextExpressionColumn() {
			final String prefix = Resources.get("expressionColumnPrefix");
			int suffix = 1;
			String name;
			do
				name = prefix + suffix++;
			while (this.getColumns().containsKey(name));
			return name;
		}
	}
