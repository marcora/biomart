package org.biomart.configurator.view.gui.diagrams.explain;

import org.biomart.builder.model.Mart;
import org.biomart.configurator.utils.type.MartType;

/**
 * A fake mart does not really exist.
 */
public class FakeMart extends Mart {
	private static final long serialVersionUID = 1L;

	/**
	 * Construct a fake mart.
	 */
	public FakeMart() {
		super(null,null,MartType.FAKE);
	}
}
