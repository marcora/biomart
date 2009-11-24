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

package org.biomart.configurator.view.gui.diagrams.contexts;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashSet;
import java.util.Set;

import javax.swing.ImageIcon;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JComponent;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;

import org.biomart.builder.model.Column;
import org.biomart.builder.model.ComponentStatus;
import org.biomart.builder.model.DataSet;
import org.biomart.builder.model.DataSetTable;
import org.biomart.builder.model.Key;
import org.biomart.builder.model.Mart;
import org.biomart.builder.model.Relation;
import org.biomart.builder.model.Schema;
import org.biomart.builder.model.Table;
import org.biomart.common.resources.Resources;
import org.biomart.configurator.view.gui.diagrams.components.KeyComponent;
import org.biomart.configurator.view.gui.diagrams.components.RelationComponent;
import org.biomart.configurator.view.gui.diagrams.components.TableComponent;

/**
 * This context applies to the general schema view, as seen via a dataset tab.
 * It allows dataset-specific things such as masked relations to be set up,
 * where those things have to be defined against the source schema rather than
 * the dataset's generated schema.
 * 
 * @author Richard Holland <holland@ebi.ac.uk>
 * @version $Revision: 1.38 $, $Date: 2008/02/01 10:17:56 $, modified by
 *          $Author: rh4 $
 * @since 0.5
 */
public class ExplainContext extends SchemaContext {
	private DataSet dataset;

	private DataSetTable datasetTable;

	/**
	 * Creates a new context within a given set of tabs, which applies to a
	 * specific dataset table. All menu options will apply to this dataset, and
	 * operations working with these datasets will be delegated to the methods
	 * specified in the tabset.
	 * 
	 * @param martTab
	 *            the mart tab that the dataset tab appears within.
	 * @param dataset
	 *            the dataset the table is in.
	 * @param datasetTable
	 *            the dataset table we are attached to.
	 */
	public ExplainContext(final Mart mart, final DataSet dataset,
			final DataSetTable datasetTable) {
		super(mart);
		this.dataset = dataset;
		this.datasetTable = datasetTable;
	}

	/**
	 * Creates a new context within a given set of tabs, which applies to a
	 * specific dataset. All menu options will apply to this dataset, and
	 * operations working with these datasets will be delegated to the methods
	 * specified in the tabset.
	 * 
	 * @param martTab
	 *            the mart tab that the dataset tab appears within.
	 * @param dataset
	 *            the dataset we are attached to.
	 */
	public ExplainContext(final Mart mart, final DataSet dataset) {
		this(mart, dataset, null);
	}

	public void customiseAppearance(final JComponent component,
			final Object object) {

		// This section customises table objects.
		if (object instanceof Table) {
			final Table table = (Table) object;
			final TableComponent tblcomp = (TableComponent) component;

			// Fade out UNINCLUDED tables.
		
			tblcomp.setBackground(TableComponent.BACKGROUND_COLOUR);

		}

		// This section customises the appearance of key objects within
		// table objects in the diagram.
		else if (object instanceof Key) {
			final KeyComponent keycomp = (KeyComponent) component;

			// All are normal.
			keycomp.setForeground(KeyComponent.NORMAL_COLOUR);

			// Remove drag-and-drop from the key as it does not apply in
			// the window context.
			keycomp.setDraggable(false);
		}
	}

	public boolean isMasked(final Object object) {
		return false;
	}

	/**
	 * See {@link #customiseAppearance(JComponent, Object)} but this applies to
	 * a particular relation iteration.
	 * 
	 * @param component
	 *            See {@link #customiseAppearance(JComponent, Object)}.
	 * @param relation
	 *            the relation.
	 * @param iteration
	 *            the iteration of the relation, or
	 *            {@link RealisedRelation#NO_ITERATION} for all iterations.
	 */
	public void customiseRelationAppearance(final JComponent component,
			final Relation relation, final int iteration) {

		// Is it restricted?
			component.setForeground(RelationComponent.NORMAL_COLOUR);

	}

	/**
	 * Obtain the dataset that this context is linked with.
	 * 
	 * @return our dataset.
	 */
	protected DataSet getDataSet() {
		return this.dataset;
	}

	/**
	 * Obtain the dataset that this context is linked with.
	 * 
	 * @return our dataset.
	 */
	protected DataSetTable getDataSetTable() {
		return this.datasetTable;
	}

	public void populateContextMenu(final JPopupMenu contextMenu,
			final Object object) {

	}

	/**
	 * See {@link #populateContextMenu(JPopupMenu, Object)} but this applies to
	 * a particular relation iteration.
	 * 
	 * @param contextMenu
	 *            See {@link #populateContextMenu(JPopupMenu, Object)}.
	 * @param relation
	 *            the relation.
	 * @param iteration
	 *            the iteration of the relation, or
	 *            {@link RealisedRelation#NO_ITERATION} for all iterations.
	 */
	public void populateRelationContextMenu(final JPopupMenu contextMenu,
			final Relation relation, final int iteration) {
	}
}
