package org.biomart.configurator.view.gui.diagrams.explain;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import org.biomart.builder.model.Table;
import org.biomart.configurator.view.gui.diagrams.contexts.ExplainContext;
import org.biomart.configurator.view.gui.diagrams.explain.FakeSchema;

/**
 * Realised tables are copies of those found in real schemas.
 */
public class RealisedTable extends Table {
	private static final long serialVersionUID = 1L;

	private final Table table;

	private final ExplainContext explainContext;

	private final PropertyChangeListener listener = new PropertyChangeListener() {
		public void propertyChange(final PropertyChangeEvent e) {
			final PropertyChangeEvent ours = new PropertyChangeEvent(
					RealisedTable.this, e.getPropertyName(), e
							.getOldValue(), e.getNewValue());
			ours.setPropagationId(e.getPropagationId());
			RealisedTable.this.pcs.firePropertyChange(ours);
		}
	};

	/**
	 * Creates a realised table.
	 * 
	 * @param name
	 *            the name to give the realised table.
	 * @param schema
	 *            the schema to put it in.
	 * @param table
	 *            the actual table we are referring to.
	 * @param explainContext
	 *            the context for displaying it.
	 */
	public RealisedTable(final String name, final FakeSchema schema,
			final Table table, final ExplainContext explainContext) {
		super(schema, name);
		this.table = table;
		this.explainContext = explainContext;
		table.addPropertyChangeListener(this.listener);
	}

	/**
	 * @return the explainContext
	 */
	public ExplainContext getExplainContext() {
		return this.explainContext;
	}

	/**
	 * @return the table
	 */
	public Table getTable() {
		return this.table;
	}
}
