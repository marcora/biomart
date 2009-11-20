package org.biomart.configurator.view.gui.diagrams.explain;

import org.biomart.builder.model.Schema;

/**
 * A fake schema does not really exist.
 */
public class FakeSchema extends Schema {
	private static final long serialVersionUID = 1L;

	/**
	 * Construct a fake schema with the given name.
	 * 
	 * @param name
	 *            the name.
	 */
	public FakeSchema(final String name) {
		super(new FakeMart(), name, name, name, null, null);
	}
}
