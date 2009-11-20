package org.biomart.configurator.view.gui.diagrams.explain;

import java.util.Iterator;
import java.util.Map;

import org.biomart.builder.model.DataSetColumn;
import org.biomart.builder.model.Mart;
import org.biomart.builder.model.Table;
import org.biomart.builder.model.TransformationUnit;
import org.biomart.builder.view.gui.diagrams.Diagram;
import org.biomart.builder.view.gui.diagrams.SchemaLayoutManager.SchemaLayoutConstraint;
import org.biomart.common.resources.Resources;
import org.biomart.configurator.view.gui.diagrams.components.TableComponent;
import org.biomart.configurator.view.gui.diagrams.contexts.ExplainContext;

/**
 * This version of the class shows a bunch of additional columns added in
 * the last transformation steps.
 */
public class AdditionalColumns extends ExplainTransformationDiagram {
	private static final long serialVersionUID = 1;

	private final TransformationUnit etu;

	/**
	 * Creates a diagram showing the given table.
	 * 
	 * @param martTab
	 *            the mart tab to pass menu events onto.
	 * @param etu
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
	public AdditionalColumns(final Mart mart,
			final TransformationUnit etu, final int step,
			final ExplainContext explainContext, final Map<String,Object> shownTables) {
		super(mart, step, explainContext, shownTables);

		// Remember the params, and calculate the diagram.
		this.etu = etu;
		this.recalculateDiagram();
	}

	public void doRecalculateDiagram() {
		// Removes all existing components.
		super.doRecalculateDiagram();
		// Replicate the table in an empty schema then add the columns
		// requested.
		final FakeSchema tempSourceSchema = new FakeSchema(Resources
				.get("dummyTempSchemaName"));
		final Table tempSource = new FakeTable(Resources
				.get("dummyTempTableName")
				+ " " + this.getStep(), tempSourceSchema);
		tempSourceSchema.getTables().put(tempSource.getName(), tempSource);
		for (final Iterator<DataSetColumn> i = this.etu.getNewColumnNameMap().values()
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
