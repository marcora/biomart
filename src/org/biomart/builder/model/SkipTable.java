package org.biomart.builder.model;

import java.util.List;

/**
 * This unit joins an existing dataset table to a schema table, or at least
 * would do that if the join were ever to be made, which it won't.
 */
public class SkipTable extends JoinTable {
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
			final Table table, final List<DataSetColumn> sourceDataSetColumns,
			final Key schemaSourceKey, final Relation schemaRelation,
			final int schemaRelationIteration) {
		super(previousUnit, table, sourceDataSetColumns, schemaSourceKey,
				schemaRelation, schemaRelationIteration);
	}
}
