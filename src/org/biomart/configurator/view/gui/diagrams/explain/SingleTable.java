package org.biomart.configurator.view.gui.diagrams.explain;

import java.util.Iterator;
import java.util.Map;

import org.biomart.builder.model.DataSetColumn;
import org.biomart.builder.model.DataSetTable;
import org.biomart.builder.model.Mart;
import org.biomart.builder.model.SelectFromTable;
import org.biomart.builder.model.Table;
import org.biomart.builder.view.gui.diagrams.Diagram;
import org.biomart.builder.view.gui.diagrams.SchemaLayoutManager.SchemaLayoutConstraint;
import org.biomart.configurator.view.gui.diagrams.components.TableComponent;
import org.biomart.configurator.view.gui.diagrams.contexts.ExplainContext;

/**
 * This version of the class shows a single table.
 */
public class SingleTable extends ExplainTransformationDiagram {
	private static final long serialVersionUID = 1;

	private final SelectFromTable stu;

	/**
	 * Creates a diagram showing the given table.
	 * 
	 * @param mart
	 *            the mart to pass menu events onto.
	 * @param stu
	 *            the transformation unit to show.
	 * @param step
	 *            the step of the transformation this diagram represents.
	 * @param explainContext
	 *            the context used to provide the relation contexts, which
	 *            are the same as those that appear in the explain diagram
	 *            in the other tab to the transform view.
	 * @param shownTables
	 *            name to state map for initial table states.
	 */
	public SingleTable(final Mart mart, final SelectFromTable stu,
			final int step, final ExplainContext explainContext,
			final Map<String,Object> shownTables) {
		super(mart, step, explainContext, shownTables);

		// Remember the params, and calculate the diagram.
		this.stu = stu;
		this.recalculateDiagram();
	}

	public void doRecalculateDiagram() {
		// Removes all existing components.
		super.doRecalculateDiagram();
		// Replicate the table in an empty schema then add the columns
		// requested.
		final FakeSchema tempSourceSchema = new FakeSchema(this.stu
				.getTable().getSchema().getName());
		final Table tempSource = new RealisedTable(
				this.stu.getTable() instanceof DataSetTable ? ((DataSetTable) this.stu
						.getTable()).getModifiedName()
						: this.stu.getTable().getName(), tempSourceSchema,
				this.stu.getTable(), this.getExplainContext());
		tempSourceSchema.getTables().put(tempSource.getName(), tempSource);
		for (final Iterator<DataSetColumn> i = this.stu.getNewColumnNameMap().values()
				.iterator(); i.hasNext();) {
			final DataSetColumn col = (DataSetColumn) i.next();
			tempSource.getColumns().put(col.getName(), col);
		}
		final TableComponent tc = new TableComponent(tempSource, this);
		this.add(tc, new SchemaLayoutConstraint(0), Diagram.TABLE_LAYER);
		this.addTableComponent(tc);
		final Object tcState = this.getState(tc);
		if (tcState != null)
			tc.setState(tcState);
	}
}
