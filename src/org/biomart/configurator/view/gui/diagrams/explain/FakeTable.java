package org.biomart.configurator.view.gui.diagrams.explain;

import org.biomart.builder.model.Table;

/**
 * A fake table does not really exist.
 */
public class FakeTable extends Table {
	private static final long serialVersionUID = 1L;

	/**
	 * Construct a fake table with the given name in the given schema.
	 * 
	 * @param name
	 *            the name.
	 * @param schema
	 *            the schema.
	 */
	public FakeTable(final String name, final FakeSchema schema) {
		super(schema, name);
	}
}
