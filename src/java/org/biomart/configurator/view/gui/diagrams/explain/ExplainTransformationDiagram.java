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

package org.biomart.configurator.view.gui.diagrams.explain;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.biomart.builder.model.Mart;
import org.biomart.builder.view.gui.diagrams.Diagram;
import org.biomart.builder.view.gui.diagrams.SchemaLayoutManager;
import org.biomart.configurator.view.gui.diagrams.components.TableComponent;
import org.biomart.configurator.view.gui.diagrams.contexts.ExplainContext;

/**
 * Displays a transformation step, depending on what is passed to the
 * constructor. The results is always a diagram containing only those components
 * which are involved in the current transformation.
 * <p>
 * Note how diagrams do not have contexts, in order to prevent user interaction
 * with them.
 * 
 * @author Richard Holland <holland@ebi.ac.uk>
 * @version $Revision: 1.46 $, $Date: 2008/02/01 10:17:56 $, modified by
 *          $Author: rh4 $
 * @since 0.5
 */
public abstract class ExplainTransformationDiagram extends Diagram {
	private static final long serialVersionUID = 1;

	private final List<TableComponent> tableComponents = new ArrayList<TableComponent>();

	private final int step;

	private final ExplainContext explainContext;

	private final Map<String,Object> shownTables;

	/**
	 * Creates an empty diagram, using the single-parameter constructor from
	 * {@link Diagram}.
	 * 
	 * @param martTab
	 *            the tabset to communicate with when (if) context menus are
	 *            selected.
	 * @param step
	 *            the step of the transformation this diagram represents.
	 * @param explainContext
	 *            the context used to provide the relation contexts, which are
	 *            the same as those that appear in the explain diagram in the
	 *            other tab to the transform view.
	 * @param shownTables
	 *            name to state map for initial table states.
	 */
	protected ExplainTransformationDiagram(final Mart mart,
			final int step, final ExplainContext explainContext,
			final Map<String,Object> shownTables) {
		super(new SchemaLayoutManager(), mart);
		this.step = step;
		this.explainContext = explainContext;
		this.shownTables = shownTables;

		// No listener required as diagram gets redone from
		// scratch if underlying tables change.
	}

	protected boolean isUseHideMasked() {
		return false;
	}

	/**
	 * Get which step this diagram is representing.
	 * 
	 * @return the step of the transformation.
	 */
	protected int getStep() {
		return this.step;
	}

	/**
	 * Get the state for a particular table component.
	 * 
	 * @param comp
	 *            the component.
	 * @return <tt>null</tt> for no state, an object otherwise.
	 */
	protected Object getState(final TableComponent comp) {
		return this.shownTables.get(comp.getTable().getName());
	}

	/**
	 * Find out what table components we have.
	 * 
	 * @return the list of components.
	 */
	public TableComponent[] getTableComponents() {
		final TableComponent[] comps = new TableComponent[this.tableComponents
				.size()];
		for (int i = 0; i < comps.length; i++)
			comps[i] = (TableComponent) this.tableComponents.get(i);
		return comps;
	}

	/**
	 * Add a table component to this diagram.
	 * 
	 * @param component
	 *            the component to add.
	 */
	protected void addTableComponent(final TableComponent component) {
		this.tableComponents.add(component);
	}

	/**
	 * Get the explain context which appears in the other tab, which is to be
	 * used for providing contexts for relations in this diagram.
	 * 
	 * @return the context.
	 */
	public ExplainContext getExplainContext() {
		return this.explainContext;
	}

	public void doRecalculateDiagram() {
		this.tableComponents.clear();
	}

}
