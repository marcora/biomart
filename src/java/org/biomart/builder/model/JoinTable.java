package org.biomart.builder.model;

import java.util.Arrays;
import java.util.List;


/**
 * This unit joins an existing dataset table to a schema table.
 */
public  class JoinTable extends SelectFromTable {
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
