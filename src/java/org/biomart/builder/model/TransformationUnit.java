/*
 Copyright (C) 2006 EBI
 
 This library is free software; you can redistribute it and/or
 modify it under the terms of the GNU Lesser General Public
 License as published by the Free Software Foundation; either
 version 2.1 of the License, or (at your option) any later version.
 
 This library is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the itmplied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 Lesser General Public License for more details.
 
 You should have received a copy of the GNU Lesser General Public
 License along with this library; if not, write to the Free Software
 Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
package org.biomart.builder.model;


import java.util.Arrays;

import java.util.HashMap;

import java.util.Iterator;
import java.util.List;
import java.util.Map;


/**
 * This interface defines a unit of transformation for mart construction.
 * 
 * @author Richard Holland <holland@ebi.ac.uk>
 * @version $Revision: 1.29 $, $Date: 2008/02/19 13:27:29 $, modified by
 *          $Author: rh4 $
 * @since 0.6
 */
public abstract class TransformationUnit {
	/**
	 * A map of source schema column names to dataset column objects.
	 */
	private final Map<Column,DataSetColumn> newColumnNameMap;

	private TransformationUnit previousUnit;

	/**
	 * Constructs a new transformation unit that follows on from a given
	 * previous unit (optional, can be <tt>null</tt>).
	 * 
	 * @param previousUnit
	 *            the unit this one comes after.
	 */
	public TransformationUnit(final TransformationUnit previousUnit) {
		this.newColumnNameMap = new HashMap<Column,DataSetColumn>();
		this.previousUnit = previousUnit;
	}

	/**
	 * Does this unit apply to the given schema prefix?
	 * 
	 * @param schemaPrefix
	 *            the prefix.
	 * @return <tt>true</tt> if it does.
	 */
	public boolean appliesToPartition(final String schemaPrefix) {
		return this.previousUnit == null ? true : this.previousUnit
				.appliesToPartition(schemaPrefix);
	}

	/**
	 * Find out what unit came before this one.
	 * 
	 * @return the previous unit. May be <tt>null</tt>.
	 */
	public TransformationUnit getPreviousUnit() {
		return this.previousUnit;
	}

	/**
	 * Change the previous unit to this one.
	 * 
	 * @param previousUnit
	 *            the new previous unit. <tt>null</tt> to remove it.
	 */
	public void setPreviousUnit(final TransformationUnit previousUnit) {
		this.previousUnit = previousUnit;
	}

	/**
	 * Obtain a map of columns defined in this unit. The keys are schema
	 * columns. The values are the dataset column names used for those columns
	 * after this unit has been applied.
	 * 
	 * @return the map of columns. Potentially empty but never <tt>null</tt>.
	 */
	public Map<Column,DataSetColumn> getNewColumnNameMap() {
		return this.newColumnNameMap;
	}

	/**
	 * Given a schema column, work out which dataset column in the
	 * transformation so far refers to it. If the column was not adopted in this
	 * particular unit it will go back until it finds the unit that adopted it,
	 * and interrogate that and return the results.
	 * 
	 * @param column
	 *            the column to look for.
	 * @return the matching dataset column. May be <tt>null</tt> if the column
	 *         is not in this dataset table at all.
	 */
	public abstract DataSetColumn getDataSetColumnFor(final Column column);

	/**
	 * This type of transformation selects columns from a single table.
	 */
	public static class SelectFromTable extends TransformationUnit {
		private static final long serialVersionUID = 1L;

		private final Table table;

		private SelectFromTable(final TransformationUnit previousUnit,
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
				for (final Iterator i = this.getNewColumnNameMap().values()
						.iterator(); i.hasNext() && candidate == null;) {
					candidate = (DataSetColumn) i.next();
					if (!this.columnMatches(column, candidate))
						candidate = null;
				}
			return candidate;
		}
	}

	/**
	 * This unit joins an existing dataset table to a schema table.
	 */
	public static class JoinTable extends SelectFromTable {
		private static final long serialVersionUID = 1L;

		private List<DataSetColumn> sourceDataSetColumns;

		private Key schemaSourceKey;

		private Relation schemaRelation;

		private int schemaRelationIteration;

		/**
		 * Construct a new join unit.
		 * 
		 * @param previousUnit
		 *            the unit that precedes this one.
		 * @param table
		 *            the table we are joining to.
		 * @param sourceDataSetColumns
		 *            the columns in the existing dataset table that are used to
		 *            make the join.
		 * @param schemaSourceKey
		 *            the key in the schema table that we are joining to.
		 * @param schemaRelation
		 *            the relation we are following to make the join.
		 * @param schemaRelationIteration
		 *            the number of the compound relation, if it is compound.
		 *            Use 0 if it is not.
		 */
		public JoinTable(final TransformationUnit previousUnit,
				final Table table, final List<DataSetColumn> sourceDataSetColumns,
				final Key schemaSourceKey, final Relation schemaRelation,
				final int schemaRelationIteration) {
			super(previousUnit, table);
			this.sourceDataSetColumns = sourceDataSetColumns;
			this.schemaSourceKey = schemaSourceKey;
			this.schemaRelation = schemaRelation;
			this.schemaRelationIteration = schemaRelationIteration;
		}

		public boolean appliesToPartition(final String schemaPrefix) {
			return super.appliesToPartition(schemaPrefix);
		}

		/**
		 * Get the dataset columns this transformation starts from.
		 * 
		 * @return the columns.
		 */
		public List<DataSetColumn> getSourceDataSetColumns() {
			return this.sourceDataSetColumns;
		}

		/**
		 * Get the schema table key this transformation joins to.
		 * 
		 * @return the key we are joining to.
		 */
		public Key getSchemaSourceKey() {
			return this.schemaSourceKey;
		}

		/**
		 * Get the schema relation used to make the join.
		 * 
		 * @return the relation.
		 */
		public Relation getSchemaRelation() {
			return this.schemaRelation;
		}

		/**
		 * Get the number of the compound relation used, or 0 if it is not
		 * compound.
		 * 
		 * @return the compound relation number.
		 */
		public int getSchemaRelationIteration() {
			return this.schemaRelationIteration;
		}

		public DataSetColumn getDataSetColumnFor(final Column column) {
			DataSetColumn candidate = (DataSetColumn) this
					.getNewColumnNameMap().get(column);
			if (candidate == null && this.getPreviousUnit() != null) {
				final Key ourKey = Arrays.asList(
						this.schemaRelation.getFirstKey().getColumns())
						.contains(column) ? this.schemaRelation.getFirstKey()
						: this.schemaRelation.getSecondKey();
				final Key parentKey = this.schemaRelation.getOtherKey(ourKey);
				final int pos = Arrays.asList(ourKey.getColumns()).indexOf(
						column);
				if (pos >= 0)
					candidate = this.getPreviousUnit().getDataSetColumnFor(
							parentKey.getColumns()[pos]);
				if (candidate == null)
					candidate = this.getPreviousUnit().getDataSetColumnFor(
							column);
			}
			return candidate;
		}
	}


	/**
	 * This unit joins an existing dataset table to a schema table, or at least
	 * would do that if the join were ever to be made, which it won't.
	 */
	public static class SkipTable extends JoinTable {
		private static final long serialVersionUID = 1L;

		/**
		 * Construct a new join unit.
		 * 
		 * @param previousUnit
		 *            the unit that precedes this one.
		 * @param table
		 *            the table we are joining to.
		 * @param sourceDataSetColumns
		 *            the columns in the existing dataset table that are used to
		 *            make the join.
		 * @param schemaSourceKey
		 *            the key in the schema table that we are joining to.
		 * @param schemaRelation
		 *            the relation we are following to make the join.
		 * @param schemaRelationIteration
		 *            the number of the compound relation, if it is compound.
		 *            Use 0 if it is not.
		 */
		public SkipTable(final TransformationUnit previousUnit,
				final Table table, final List sourceDataSetColumns,
				final Key schemaSourceKey, final Relation schemaRelation,
				final int schemaRelationIteration) {
			super(previousUnit, table, sourceDataSetColumns, schemaSourceKey,
					schemaRelation, schemaRelationIteration);
		}
	}

}
