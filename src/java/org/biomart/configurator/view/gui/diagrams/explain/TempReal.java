package org.biomart.configurator.view.gui.diagrams.explain;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.biomart.builder.model.Column;
import org.biomart.builder.model.DataSetColumn;
import org.biomart.builder.model.ForeignKey;
import org.biomart.builder.model.JoinTable;
import org.biomart.builder.model.Key;
import org.biomart.builder.model.Mart;
import org.biomart.builder.model.PrimaryKey;
import org.biomart.builder.model.Relation;
import org.biomart.builder.model.Table;
import org.biomart.builder.view.gui.diagrams.Diagram;
import org.biomart.builder.view.gui.diagrams.SchemaLayoutManager.SchemaLayoutConstraint;
import org.biomart.common.exceptions.AssociationException;
import org.biomart.common.exceptions.BioMartError;
import org.biomart.common.resources.Resources;
import org.biomart.configurator.view.gui.diagrams.components.RelationComponent;
import org.biomart.configurator.view.gui.diagrams.components.TableComponent;
import org.biomart.configurator.view.gui.diagrams.contexts.ExplainContext;

/**
 * This version of the class shows a temp table on the left and a real table
 * on the right.
 */
public class TempReal extends ExplainTransformationDiagram {
	private static final long serialVersionUID = 1;

	private final JoinTable ltu;

	private final Collection<Column> lIncludeCols;

	/**
	 * Creates a diagram showing the given pair of tables and a relation
	 * between them.
	 * 
	 * @param martTab
	 *            the mart tab to pass menu events onto.
	 * @param ltu
	 *            the transformation to explain.
	 * @param lIncludeCols
	 *            the columns to show in the temp table.
	 * @param step
	 *            the step of the transformation this diagram represents.
	 * @param explainContext
	 *            the context used to provide the relation contexts, which
	 *            are the same as those that appear in the explain diagram
	 *            in the other tab to the transform view.
	 * @param shownTables
	 *            name to state map for initial table states.
	 */
	public TempReal(final Mart mart, final JoinTable ltu,
			final List lIncludeCols, final int step,
			final ExplainContext explainContext, final Map<String,Object> shownTables) {
		super(mart, step, explainContext, shownTables);

		// Remember the columns, and calculate the diagram.
		this.ltu = ltu;
		this.lIncludeCols = new ArrayList(lIncludeCols);
		this.recalculateDiagram();
	}

	public void doRecalculateDiagram() {
		// Removes all existing components.
		super.doRecalculateDiagram();
		// Create a temp table called TEMP with the given columns
		// and given foreign key.
		final FakeSchema tempSourceSchema = new FakeSchema(Resources
				.get("dummyTempSchemaName"));
		final Table tempSource = new FakeTable(Resources
				.get("dummyTempTableName")
				+ " " + this.getStep(), tempSourceSchema);
		tempSourceSchema.getTables().put(tempSource.getName(), tempSource);
		for (final Iterator<Column> i = this.lIncludeCols.iterator(); i.hasNext();) {
			final Column col = (Column) i.next();
			tempSource.getColumns().put(col.getName(), col);
		}
		Key tempSourceKey;
		if (this.ltu.getSchemaSourceKey() instanceof ForeignKey) {
			tempSourceKey = new ForeignKey((Column[]) this.ltu
					.getSourceDataSetColumns().toArray(new Column[0]));
			tempSource.getForeignKeys().add((ForeignKey)tempSourceKey);
		} else {
			tempSourceKey = new PrimaryKey((Column[]) this.ltu
					.getSourceDataSetColumns().toArray(new Column[0]));
			tempSource.setPrimaryKey((PrimaryKey) tempSourceKey);
		}
		tempSourceKey.transactionResetVisibleModified();

		// Create a copy of the target table complete with target key.
		final Key realTargetKey = this.ltu.getSchemaRelation().getOtherKey(
				this.ltu.getSchemaSourceKey());
		final Table realTarget = this.ltu.getTable();
		final FakeSchema tempTargetSchema = new FakeSchema(realTarget
				.getSchema().getName());
		final Table tempTarget = new RealisedTable(realTarget.getName(),
				tempTargetSchema, realTarget, this.getExplainContext());
		tempTargetSchema.getTables().put(tempTarget.getName(), tempTarget);
		for (final Iterator i = this.ltu.getNewColumnNameMap().values()
				.iterator(); i.hasNext();) {
			final DataSetColumn col = (DataSetColumn) i.next();
			tempTarget.getColumns().put(col.getName(), col);
		}
		Key tempTargetKey;
		if (realTargetKey instanceof ForeignKey) {
			tempTargetKey = new ForeignKey(realTargetKey.getColumns());
			tempTarget.getForeignKeys().add((ForeignKey)tempTargetKey);
		} else {
			tempTargetKey = new PrimaryKey(realTargetKey.getColumns());
			tempTarget.setPrimaryKey((PrimaryKey) tempTargetKey);
		}
		tempTargetKey.transactionResetVisibleModified();

		// Create a copy of the relation but change to be between the
		// two fake keys.
		Relation tempRelation;
		try {
			tempRelation = new RealisedRelation(tempSourceKey,
					tempTargetKey, this.ltu.getSchemaRelation()
							.getCardinality(),
					this.ltu.getSchemaRelation(), this.ltu
							.getSchemaRelationIteration(), this
							.getExplainContext());
			// DON'T add to keys else it causes trouble with
			// the caching system!
		} catch (final AssociationException e) {
			// Really should never happen.
			throw new BioMartError(e);
		}

		// Add source and target tables.
		final TableComponent tc1 = new TableComponent(tempSource, this);
		this.add(tc1, new SchemaLayoutConstraint(1), Diagram.TABLE_LAYER);
		this.addTableComponent(tc1);
		final Object tc1State = this.getState(tc1);
		if (tc1State != null)
			tc1.setState(tc1State);
		final TableComponent tc2 = new TableComponent(tempTarget, this);
		this.add(tc2, new SchemaLayoutConstraint(1), Diagram.TABLE_LAYER);
		this.addTableComponent(tc2);
		final Object tc2State = this.getState(tc2);
		if (tc2State != null)
			tc2.setState(tc2State);
		// Add relation.
		final RelationComponent relationComponent = new RelationComponent(
				tempRelation, this);
		this.add(relationComponent, new SchemaLayoutConstraint(0),
				Diagram.RELATION_LAYER);
	}
}
