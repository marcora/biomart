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

package org.biomart.configurator.view.gui.dialogs;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.WindowConstants;
import org.biomart.builder.model.DataSet;
import org.biomart.builder.model.DataSetColumn;
import org.biomart.builder.model.DataSetTable;
import org.biomart.builder.model.Mart;
import org.biomart.builder.model.Table;
import org.biomart.builder.model.TransformationUnit;
import org.biomart.builder.model.JoinTable;
import org.biomart.builder.model.SelectFromTable;
import org.biomart.builder.model.SkipTable;
import org.biomart.builder.view.gui.diagrams.ExplainTransformationDiagram;
import org.biomart.common.exceptions.BioMartError;
import org.biomart.common.resources.Resources;
import org.biomart.common.resources.Settings;
import org.biomart.common.utils.Transaction;
import org.biomart.common.utils.Transaction.TransactionEvent;
import org.biomart.common.utils.Transaction.TransactionListener;
import org.biomart.configurator.utils.type.IdwViewType;
import org.biomart.configurator.view.gui.diagrams.components.TableComponent;
import org.biomart.configurator.view.gui.diagrams.contexts.ExplainContext;
import org.biomart.configurator.view.gui.diagrams.contexts.TransformationContext;
import org.biomart.configurator.view.idwViews.McViews;

/**
 * This simple dialog explains a table by drawing a series of diagrams of the
 * underlying tables and relations involved in it.
 * <p>
 * It has two tabs. In the first tab goes an overview diagram. In the second tab
 * goes a series of smaller diagrams, each one an instance of
 * {@link ExplainTransformationDiagram} which represents a single step in the
 * transformation process required to produce the table being explained.
 * 
 * @author Richard Holland <holland@ebi.ac.uk>
 * @version $Revision: 1.47 $, $Date: 2008/02/29 11:26:11 $, modified by
 *          $Author: rh4 $
 * @since 0.5
 */
public class ExplainTableDialog extends JDialog implements TransactionListener {
	private static final long serialVersionUID = 1;

	private static final int MAX_UNITS = Settings.getProperty("maxunits") == null ? 50
			: Integer.parseInt(Settings.getProperty("maxunits"));

	private JCheckBox maskedHidden;

	private boolean needsRebuild;

	/**
	 * Opens an explanation showing the underlying relations and tables behind a
	 * specific dataset table.
	 * 
	 * @param martTab
	 *            the mart tab which will handle menu events.
	 * @param table
	 *            the table to explain.
	 */
	public static void showTableExplanation(final Mart mart, final DataSetTable table) {
		new ExplainTableDialog(mart,table).setVisible(true);
	}

	private final DataSet ds;
	private final DataSetTable dsTable;
	private GridBagConstraints fieldConstraints;
	private GridBagConstraints fieldLastRowConstraints;
	private GridBagConstraints labelConstraints;
	private GridBagConstraints labelLastRowConstraints;
	private JPanel transformation;
	private final List<ExplainTransformationDiagram> transformationTableDiagrams = new ArrayList<ExplainTransformationDiagram>();
	private TransformationContext transformationContext;
	private final ExplainContext explainContext;
	private Mart mart;

	private final PropertyChangeListener listener = new PropertyChangeListener() {
		public void propertyChange(final PropertyChangeEvent evt) {
			ExplainTableDialog.this.needsRebuild = true;
		}
	};

	private final PropertyChangeListener recalcListener = new PropertyChangeListener() {
		public void propertyChange(final PropertyChangeEvent evt) {
			ExplainTableDialog.this.maskedHidden
					.setSelected(ExplainTableDialog.this.dsTable
							.isExplainHideMasked());
			ExplainTableDialog.this.recalculateTransformation();
		}
	};

	/**
	 * The background for the masked checkbox.
	 */
	public static final Color MASK_BG_COLOR = Color.WHITE;

	private ExplainTableDialog(final Mart mart, final DataSetTable dsTable) {
		// Create the blank dialog, and give it an appropriate title.
		super();
		this.setTitle(Resources.get("explainTableDialogTitle", dsTable
				.getModifiedName()));
		this.setModal(true);
		this.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		this.ds = dsTable.getDataSet();
		this.dsTable = dsTable;
		this.mart = mart;

		// Create a context.
		this.explainContext = new ExplainContext(this.mart, this.ds, dsTable);

		// Create the hide masked box.
		this.maskedHidden = new JCheckBox(Resources.get("hideMaskedTitle"));
		this.maskedHidden.setSelected(dsTable.isExplainHideMasked());
		this.maskedHidden.addActionListener(new ActionListener() {
			public void actionPerformed(final ActionEvent e) {
				dsTable
						.setExplainHideMasked(ExplainTableDialog.this.maskedHidden
								.isSelected());
			}
		});
		// It has a semi-transparent background with no border.
		this.maskedHidden.setOpaque(true);
		this.maskedHidden.setBackground(ExplainTableDialog.MASK_BG_COLOR);

		// Weak-listen to DataSetTable.explainHideMasked and recalc on change.
		dsTable.addPropertyChangeListener("explainHideMasked",
				this.recalcListener);

		// Make the content pane.
		final JPanel displayArea = new JPanel();


		// Create constraints for labels that are not in the last row.
		this.labelConstraints = new GridBagConstraints();
		this.labelConstraints.gridwidth = GridBagConstraints.RELATIVE;
		this.labelConstraints.fill = GridBagConstraints.HORIZONTAL;
		this.labelConstraints.anchor = GridBagConstraints.LINE_END;
		this.labelConstraints.insets = new Insets(0, 2, 0, 0);
		// Create constraints for fields that are not in the last row.
		this.fieldConstraints = new GridBagConstraints();
		this.fieldConstraints.gridwidth = GridBagConstraints.REMAINDER;
		this.fieldConstraints.fill = GridBagConstraints.NONE;
		this.fieldConstraints.anchor = GridBagConstraints.LINE_START;
		this.fieldConstraints.insets = new Insets(0, 1, 0, 2);
		// Create constraints for labels that are in the last row.
		this.labelLastRowConstraints = (GridBagConstraints) this.labelConstraints
				.clone();
		this.labelLastRowConstraints.gridheight = GridBagConstraints.REMAINDER;
		// Create constraints for fields that are in the last row.
		this.fieldLastRowConstraints = (GridBagConstraints) this.fieldConstraints
				.clone();
		this.fieldLastRowConstraints.gridheight = GridBagConstraints.REMAINDER;

		// Compute the transformation diagram.
		this.transformation = new JPanel(new GridBagLayout());
		displayArea.add(new JScrollPane(this.transformation));


		// Work out what size we want the diagram to be.
		final Dimension size = McViews.getInstance().getView(IdwViewType.SCHEMA).getPreferredSize();
		final Dimension maxSize = McViews.getInstance().getView(IdwViewType.SCHEMA).getMaximumSize();
		// The +20s in the following are to cater for scrollbar widths
		// and window borders.
		size.width = Math.max(100, Math
				.min(size.width + 20, maxSize.width - 20));
		size.height = Math.max(100, Math.min(size.height + 20,
				maxSize.height - 20));
		displayArea.setPreferredSize(size);

		// Make a context for our sub-diagrams.
		this.transformationContext = new TransformationContext(this.mart,this.ds);

		this.setContentPane(displayArea);

		// Add a listener to the dataset such that if any part of the dataset
		// changes, we recalculate ourselves entirely.
		this.needsRebuild = false;
		Transaction.addTransactionListener(this);

		this.ds.addPropertyChangeListener("directModified", this.listener);

		this.recalculateTransformation();
		// Pack the window.
		this.pack();

		// Move ourselves.
		this.setLocationRelativeTo(null);

	}

	public boolean isDirectModified() {
		return false;
	}

	public void setDirectModified(final boolean modified) {
		// Ignore, for now.
	}

	public boolean isVisibleModified() {
		return false;
	}

	public void setVisibleModified(final boolean modified) {
		// Ignore, for now.
	}

	public void transactionResetDirectModified() {
		// Ignore, for now.
	}

	public void transactionResetVisibleModified() {
		// Ignore, for now.
	}

	public void transactionStarted(final TransactionEvent evt) {
		// Ignore, for now.
	}

	public void transactionEnded(final TransactionEvent evt) {
		if (this.needsRebuild)
			this.recalculateTransformation();
	}

	private void recalculateTransformation() {
		this.needsRebuild = false;

		// Keep a note of shown tables.
		final Map<String,Map<String,Object>> shownTables = new HashMap<String,Map<String,Object>>();
		for (int i = 1; i <= ExplainTableDialog.this.transformationTableDiagrams.size(); i++) {
			final TableComponent[] comps = ((ExplainTransformationDiagram) ExplainTableDialog.this.transformationTableDiagrams
					.get(i - 1)).getTableComponents();
			final Map<String,Object> map = new HashMap<String,Object>();
			shownTables.put("" + i, map);
			for (int j = 0; j < comps.length; j++)
				map.put(((Table) comps[j].getObject()).getName(),
						comps[j].getState());
		}

		// Clear the transformation box.
		ExplainTableDialog.this.transformation.removeAll();
		ExplainTableDialog.this.transformationTableDiagrams.clear();

		// Keep track of columns counted so far.
		final List<DataSetColumn> columnsSoFar = new ArrayList<DataSetColumn>();

		// Count our steps.
		int stepNumber = 1;

		// If more than a set limit of units, we hit memory
		// and performance issues. Refuse to do the display and
		// instead put up a helpful message. Limit should be
		// configurable from a properties file.
		final Collection<TransformationUnit> units = new ArrayList<TransformationUnit>(
				ExplainTableDialog.this.dsTable
						.getTransformationUnits()); // To prevent
													// concmod.
		if (units.size() > ExplainTableDialog.MAX_UNITS)
			ExplainTableDialog.this.transformation.add(new JLabel(
					Resources.get("tooManyUnits", ""
							+ ExplainTableDialog.MAX_UNITS)),
					ExplainTableDialog.this.fieldLastRowConstraints);
		else {
			// Insert show/hide hidden steps button.
			ExplainTableDialog.this.transformation.add(new JLabel(),
					ExplainTableDialog.this.labelConstraints);
			JPanel field = new JPanel();
			field.add(ExplainTableDialog.this.maskedHidden);
			ExplainTableDialog.this.transformation.add(field,
					ExplainTableDialog.this.fieldConstraints);

			// Iterate over transformation units.
			for (final Iterator<TransformationUnit> i = units.iterator(); i.hasNext();) {
				final TransformationUnit tu = (TransformationUnit) i.next();
				// Holders for our stuff.
				final JLabel label;
				final ExplainTransformationDiagram diagram;
				Map<String,Object> map = shownTables.get("" + stepNumber);
				if (map==null)
					map = Collections.emptyMap();
				// Draw the unit.
				if (tu instanceof SkipTable) {
					// Don't show these if we're hiding masked things.
					if (ExplainTableDialog.this.dsTable
							.isExplainHideMasked())
						continue;
					// Temp table to schema table join.
					label = new JLabel(
							Resources
									.get(
											"stepTableLabel",
											new String[] {
													"" + stepNumber,
													Resources
															.get("explainSkipLabel") }));
					diagram = new ExplainTransformationDiagram.SkipTempReal(
							ExplainTableDialog.this.mart,
							(SkipTable) tu, columnsSoFar, stepNumber,
							ExplainTableDialog.this.explainContext,
							map);
				}  else if (tu instanceof JoinTable) {
					// Temp table to schema table join.
					label = new JLabel(
							Resources
									.get(
											"stepTableLabel",
											new String[] {
													"" + stepNumber,
													Resources
															.get("explainMergeLabel") }));
					diagram = new ExplainTransformationDiagram.TempReal(
							ExplainTableDialog.this.mart,
							(JoinTable) tu, columnsSoFar, stepNumber,
							ExplainTableDialog.this.explainContext,
							map);
				} else if (tu instanceof SelectFromTable) {
					// Do a single-step select.
					label = new JLabel(
							Resources
									.get(
											"stepTableLabel",
											new String[] {
													"" + stepNumber,
													Resources
															.get("explainSelectLabel") }));
					diagram = new ExplainTransformationDiagram.SingleTable(
							ExplainTableDialog.this.mart,
							(SelectFromTable) tu, stepNumber,
							ExplainTableDialog.this.explainContext,
							map);
				} else
					throw new BioMartError();
				// Display the diagram.
				ExplainTableDialog.this.transformation
						.add(
								label,
								i.hasNext() ? ExplainTableDialog.this.labelConstraints
										: ExplainTableDialog.this.labelLastRowConstraints);
				diagram
						.setDiagramContext(ExplainTableDialog.this.transformationContext);
				field = new JPanel();
				field.add(diagram);
				ExplainTableDialog.this.transformation
						.add(
								field,
								i.hasNext() ? ExplainTableDialog.this.fieldConstraints
										: ExplainTableDialog.this.fieldLastRowConstraints);
				// Add columns from this unit to the transformed table.
				columnsSoFar.addAll(tu.getNewColumnNameMap().values());
				stepNumber++;
				// Remember what tables we just added.
				ExplainTableDialog.this.transformationTableDiagrams
						.add(diagram);
			}
		}

		// Resize the diagram to fit the components.
		ExplainTableDialog.this.transformation.revalidate();
		ExplainTableDialog.this.transformation.repaint();
	}
}
