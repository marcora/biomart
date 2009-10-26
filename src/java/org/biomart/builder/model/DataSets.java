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

package org.biomart.builder.model;


import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import javax.swing.JOptionPane;
import org.biomart.builder.view.gui.diagrams.DataSetDiagram;
import org.biomart.builder.view.gui.diagrams.Diagram;
import org.biomart.builder.view.gui.diagrams.contexts.DataSetContext;
import org.biomart.builder.view.gui.diagrams.contexts.DiagramContext;
import org.biomart.builder.view.gui.dialogs.SaveDDLDialog;
import org.biomart.builder.view.gui.dialogs.SuggestDataSetDialog;
import org.biomart.common.resources.Log;
import org.biomart.common.resources.Resources;
import org.biomart.common.utils.Transaction;
import org.biomart.common.view.gui.LongProcess;
import org.biomart.configurator.utils.McEventObject;
import org.biomart.configurator.utils.type.DataSetOptimiserType;
import org.biomart.configurator.utils.type.EventType;
import org.biomart.configurator.utils.type.IdwViewType;
import org.biomart.configurator.view.idwViews.McViews;


/**
 * This tabset contains most of the core functionality of the entire GUI. It handles all changes to any of the
 * datasets in the mart, and handles the assignment of {@link DiagramContext}s
 * to the various {@link Diagram}s inside it, including the schema tabset.
 * 
 * @author Richard Holland <holland@ebi.ac.uk>
 * @version $Revision: 1.170 $, $Date: 2008/03/20 11:13:08 $, modified by
 *          $Author: yliang $
 * @since 0.8
 */
public class DataSets {
	private static final long serialVersionUID = 1;

	private final Map<String, DataSet> datasetsMap = new HashMap<String, DataSet>();
	private Mart mart;

	// Make a listener which knows how to handle masking and
	// renaming.
	private final PropertyChangeListener renameListener = new PropertyChangeListener() {
		public void propertyChange(PropertyChangeEvent evt) {
			final DataSet ds = (DataSet) evt.getSource();
			if (evt.getPropertyName().equals("name")) {
				// Rename in diagram set.
				DataSets.this.datasetsMap.put((String)evt.getNewValue(),
						DataSets.this.datasetsMap.remove(evt
								.getOldValue()));
			} else if (evt.getPropertyName().equals("masked")) {
				// For masks, if unmasking, add a tab, otherwise
				// remove the tab.
				final boolean masked = ((Boolean) evt.getNewValue())
						.booleanValue();
				if (!masked)
					DataSets.this.addDataSet(ds);
			}
		}
	};

	// Listen to changes on tabs.
	private final PropertyChangeListener tabListener = new PropertyChangeListener() {
		public void propertyChange(final PropertyChangeEvent evt) {
			// Listen to masked schema and rename
			// schema events on each new schema added
			// regardless of tab presence.
			// Mass change. Copy to prevent concurrent mods.
			final Set<String> oldDSs = new HashSet<String>(DataSets.this.datasetsMap
					.keySet());
			for (final Iterator i = DataSets.this.mart
					.getDataSets().values().iterator(); i.hasNext();) {
				final DataSet ds = (DataSet) i.next();
				if (!oldDSs.remove(ds.getName())) {
					// Single-add.
					if (!ds.isMasked())
						DataSets.this.addDataSet(ds);
					ds.addPropertyChangeListener("masked",
							DataSets.this.renameListener);
					ds.addPropertyChangeListener("name",
							DataSets.this.renameListener);
				}
			}
		}
	};

	/**
	 * The constructor sets up a new set of tabs which represent all the
	 * datasets in the given mart, plus an overview tab to represent all the
	 * datasets in the mart.
	 * 
	 * @param martTab
	 *            the mart tab to represent the datasets for.
	 */
	public DataSets(final Mart mart) {
		super();

		// Add the datasets overview tab. This tab displays a diagram
		// in which all datasets appear. This diagram could be quite large,
		// so it is held inside a scrollpane.
		// Populate the map to hold the relation between schemas and the
		// diagrams representing them.
		for (final Iterator i = mart.getDataSets().values()
				.iterator(); i.hasNext();) {
			final DataSet ds = (DataSet) i.next();
			// Don't add schemas which are initially masked.
			if (!ds.isMasked())
				this.addDataSet(ds);
			ds.addPropertyChangeListener("masked", this.renameListener);
			ds.addPropertyChangeListener("name", this.renameListener);
		}
		this.mart = mart;
		// Listen to add/remove/mass change schema events.
		this.mart.getDataSets().addPropertyChangeListener(
				this.tabListener);
	}

	/**
	 */
	private synchronized void addDataSet(final DataSet dataset) {
		// Create the diagram to represent this dataset.
		final DataSetDiagram datasetDiagram = new DataSetDiagram(this.mart,dataset);

		// Remember which diagram the dataset is connected with.
		this.datasetsMap.put(dataset.getName(), dataset);
		dataset.setDataSetDiagram(datasetDiagram);
		// Set the current context on the diagram to be the same as the
		// current context on this dataset tabset.
		datasetDiagram.setDiagramContext(new DataSetContext(this.mart, dataset));
	}

	private String askUserForName(final String message,
			final String defaultResponse) {
		// Ask the user for a name. Use the default response
		// as the default value in the input field.
		String name = (String) JOptionPane.showInputDialog(null, message,
				Resources.get("questionTitle"), JOptionPane.QUESTION_MESSAGE,
				null, null, defaultResponse);

		// If they cancelled the request, return null.
		if (name == null)
			return null;

		// If they didn't enter anything, use the default response
		// as though they hadn't changed it.
		else if (name.trim().length() == 0)
			name = defaultResponse;

		// Return the response.
		return name;
	}

	public DataSetDiagram getDataSetDiagram(String name) {
		return this.datasetsMap.get(name).getDataSetDiagram();	
	}
	
	public DataSet getDataSet(String name) {
		return this.datasetsMap.get(name);
	}
	
	/**
	 * Request that the optimiser type used post-construction of a dataset be
	 * changed.
	 * 
	 * @param dataset
	 *            the dataset we are working with.
	 * @param type
	 *            the type of optimiser to use post-construction.
	 */
	public void requestChangeOptimiserType(final DataSet dataset,
			final DataSetOptimiserType type) {
		new LongProcess() {
			public void run() {
				Transaction.start(false);
				dataset.setDataSetOptimiserType(type);
				Transaction.end();
			}
		}.start();
	}

	/**
	 * Request that all changes on this dataset table associated with this
	 * target are accepted. See {@link DataSetTable#acceptChanges(Table)}.
	 * 
	 * @param dsTable
	 *            the dataset table to work with.
	 * @param targetTable
	 *            the (optional) target table to accept changes from.
	 */
	public void requestAcceptAll(final DataSetTable dsTable,
			final Table targetTable) {
		new LongProcess() {
			public void run() {
				Transaction.start(true);
				dsTable.acceptChanges(targetTable);
				Transaction.end();
			}
		}.start();
	}

	/**
	 * Request that all changes on this dataset table associated with this
	 * target are rejected. See {@link DataSetTable#rejectChanges(Table)}.
	 * 
	 * @param dsTable
	 *            the dataset table to work with.
	 * @param targetTable
	 *            the (optional) target table to reject changes from.
	 */
	public void requestRejectAll(final DataSetTable dsTable,
			final Table targetTable) {
		new LongProcess() {
			public void run() {
				Transaction.start(true);
				dsTable.rejectChanges(targetTable);
				Transaction.end();
			}
		}.start();
	}

	/**
	 * Request that all changes on this dataset associated with this target are
	 * accepted.
	 * 
	 * @param ds
	 *            the dataset to work with.
	 * @param targetTable
	 *            the (optional) target table to accept changes from.
	 */
	public void requestAcceptAll(final DataSet ds, final Table targetTable) {
		new LongProcess() {
			public void run() {
				Transaction.start(true);
				for (final Iterator i = ds.getTables().values().iterator(); i
						.hasNext();)
					((DataSetTable) i.next()).acceptChanges(targetTable);
				Transaction.end();
			}
		}.start();
	}

	/**
	 * Request that all changes on this dataset associated with this target are
	 * rejected.
	 * 
	 * @param ds
	 *            the dataset to work with.
	 * @param targetTable
	 *            the (optional) target table to reject changes from.
	 */
	public void requestRejectAll(final DataSet ds, final Table targetTable) {
		new LongProcess() {
			public void run() {
				Transaction.start(true);
				for (final Iterator i = ds.getTables().values().iterator(); i
						.hasNext();)
					((DataSetTable) i.next()).rejectChanges(targetTable);
				Transaction.end();
			}
		}.start();
	}

	/**
	 * On a request to create DDL for the current dataset, open the DDL creation
	 * window with all this dataset selected.
	 * 
	 * @param dataset
	 *            the dataset to show the dialog for.
	 */
	public void requestCreateDDL(final DataSet dataset) {
		// If it is a partition table dataset, refuse.

			(new SaveDDLDialog(
					this.mart,
					Collections.singleton(dataset),
					null, // no partition for now
					SaveDDLDialog.VIEW_DDL)).setVisible(true);
	}


	/**
	 * Requests that a column be masked.
	 * 
	 * @param ds
	 *            the dataset we are working with.
	 * @param column
	 *            the column to mask.
	 * @param masked
	 *            whether to mask it.
	 */
	public void requestMaskColumn(final DataSet ds, final DataSetColumn column,
			final boolean masked) {
		this.requestMaskColumns(ds, Collections.singleton(column), masked);
	}

	/**
	 * Requests that a set of columns be masked.
	 * 
	 * @param ds
	 *            the dataset we are working with.
	 * @param columns
	 *            the columns to mask.
	 * @param masked
	 *            whether to mask it.
	 */
	public void requestMaskColumns(final DataSet ds, final Collection columns,
			final boolean masked) {
		new LongProcess() {
			public void run() throws Exception {
				try {
					Transaction.start(false);
					for (final Iterator i = columns.iterator(); i.hasNext();)
						((DataSetColumn) i.next()).setColumnMasked(masked);
				} finally {
					Transaction.end();
				}
			}
		}.start();
	}

	/**
	 * Requests that a column be indexed.
	 * 
	 * @param ds
	 *            the dataset we are working with.
	 * @param column
	 *            the column to index.
	 * @param index
	 *            whether to index it.
	 */
	public void requestIndexColumn(final DataSet ds,
			final DataSetColumn column, final boolean index) {
		new LongProcess() {
			public void run() {
				Transaction.start(false);
				column.setColumnIndexed(index);
				Transaction.end();
			}
		}.start();
	}



	/**
	 * Requests that a table be hide-masked.
	 * 
	 * @param ds
	 *            the dataset we are working with.
	 * @param dst
	 *            the table to make hide-masked.
	 * @param tableHideMasked
	 *            whether to do it.
	 */
	public void requestTableHideMasked(final DataSet ds,
			final DataSetTable dst, final boolean tableHideMasked) {
		new LongProcess() {
			public void run() {
				Transaction.start(false);
				dst.setTableHideMasked(tableHideMasked);
				Transaction.end();
			}
		}.start();
	}

	/**
	 * Requests that a table be not optimisered.
	 * 
	 * @param ds
	 *            the dataset we are working with.
	 * @param dst
	 *            the table to make not optimisered.
	 * @param skipOptimiser
	 *            whether to do it.
	 */
	public void requestSkipOptimiser(final DataSet ds, final DataSetTable dst,
			final boolean skipOptimiser) {
		new LongProcess() {
			public void run() {
				Transaction.start(false);
				dst.setSkipOptimiser(skipOptimiser);
				Transaction.end();
			}
		}.start();
	}

	/**
	 * Requests that a table be not index optimisered.
	 * 
	 * @param ds
	 *            the dataset we are working with.
	 * @param dst
	 *            the table to make not index optimisered.
	 * @param skipIndexOptimiser
	 *            whether to do it.
	 */
	public void requestSkipIndexOptimiser(final DataSet ds,
			final DataSetTable dst, final boolean skipIndexOptimiser) {
		new LongProcess() {
			public void run() {
				Transaction.start(false);
				dst.setSkipIndexOptimiser(skipIndexOptimiser);
				Transaction.end();
			}
		}.start();
	}


	/**
	 * Requests that a dimension be masked.
	 * 
	 * @param ds
	 *            the dataset we are working with.
	 * @param dim
	 *            the dimension to mask.
	 * @param masked
	 *            whether to mask it.
	 */
	public void requestMaskDimension(final DataSet ds, final DataSetTable dim,
			final boolean masked) {
		new LongProcess() {
			public void run() throws Exception {
				try {
					Transaction.start(false);
					dim.setDimensionMasked(masked);
				} finally {
					Transaction.end();
					//notify controller
					McEventObject mcObject = new McEventObject(EventType.Synchronize_Dataset, dim);
					McViews.getInstance().getView(IdwViewType.SCHEMA).getController().processV2Cupdate(mcObject);
				}
			}
		}.start();
	}


	/**
	 * Asks that a dimension be merged.
	 * 
	 * @param ds
	 *            the dataset we are working with.
	 * @param dst
	 *            the dimension to merge.
	 * @param merged
	 *            whether to merge it.
	 */
	public void requestMergeDimension(final DataSet ds, final DataSetTable dst,
			final boolean merged) {
		new LongProcess() {
			public void run() {
				Transaction.start(false);
				dst.getFocusRelation().setMergeRelation(ds, merged);
				Transaction.end();
			}
		}.start();
	}



	/**
	 * Asks that a relation be masked.
	 * 
	 * @param ds
	 *            the dataset we are working with.
	 * @param dst
	 *            the table to work with.
	 * @param relation
	 *            the schema relation to mask.
	 * @param masked
	 *            whether to mask it.
	 */
	public void requestMaskRelation(final DataSet ds, final DataSetTable dst,
			final Relation relation, final boolean masked) {
		new LongProcess() {
			public void run() {
				Transaction.start(false);
				if (dst != null)
					relation.setMaskRelation(ds, dst.getName(), masked);
				else
					relation.setMaskRelation(ds, masked);
				Transaction.end();
			}
		}.start();
	}



	/**
	 * Asks that a dataset be (un)masked.
	 * 
	 * @param ds
	 *            the dataset we are working with.
	 * @param masked
	 *            mask it?
	 */
	public void requestMaskDataSet(final DataSet ds, final boolean masked) {
		new LongProcess() {
			public void run() {
				Transaction.start(false);
				ds.setMasked(masked);
				Transaction.end();
			}
		}.start();
	}







	/**
	 * Asks the user if they are sure they want to remove the dataset, then
	 * removes it from the mart (and the tabs) if they agree.
	 * 
	 * @param dataset
	 *            the dataset to remove.
	 */
	public void requestRemoveDataSet(final DataSet dataset) {
		// Confirm the decision first.
		final int choice = JOptionPane.showConfirmDialog(null, Resources
				.get("confirmDelDataset"), Resources.get("questionTitle"),
				JOptionPane.YES_NO_OPTION);

		// Refuse to do it if they said no.
		if (choice != JOptionPane.YES_OPTION)
			return;

		new LongProcess() {
			public void run() {
				Transaction.start(false);
				DataSets.this.mart.getDataSets().remove(
						dataset.getOriginalName());
				Transaction.end();
			}
		}.start();
	}


	/**
	 * Renames a dataset, then renames the tab too.
	 * 
	 * @param dataset
	 *            the dataset to rename.
	 */
	public void requestRenameDataSet(final DataSet dataset) {
		// Ask user for the new name.
		this.requestRenameDataSet(dataset, this.askUserForName(Resources
				.get("requestDataSetName"), dataset.getName()));
	}

	/**
	 * Renames a dataset, then renames the tab too.
	 * 
	 * @param dataset
	 *            the dataset to rename.
	 * @param name
	 *            the new name to give it.
	 */
	public void requestRenameDataSet(final DataSet dataset, final String name) {
		// If the new name is null (user cancelled), or has
		// not changed, don't rename it.
		final String newName = name == null ? "" : name.trim();
		if (newName.length() == 0)
			return;

		new LongProcess() {
			public void run() {
				Transaction.start(false);
				dataset.setName(newName);
				Transaction.end();
			}
		}.start();
	}

	/**
	 * Renames a column, after prompting the user to enter a new name. By
	 * default, the existing name is used. If the name entered is blank or
	 * matches the existing name, no change is made.
	 * 
	 * @param dsColumn
	 *            the column to rename.
	 */
	public void requestRenameDataSetColumn(final DataSetColumn dsColumn) {
		// Ask user for the new name.
		this.requestRenameDataSetColumn(dsColumn, this.askUserForName(Resources
				.get("requestDataSetColumnName"), dsColumn.getModifiedName()));
	}

	/**
	 * Renames a dataset column to have the given name.
	 * 
	 * @param dsColumn
	 *            the column to rename.
	 * @param name
	 *            the new name to give it.
	 */
	public void requestRenameDataSetColumn(final DataSetColumn dsColumn,
			final String name) {
		// Ask user for the new name.
		final String newName = name == null ? "" : name.trim();

		// If the new name is null (user cancelled), or has
		// not changed, don't rename it.
		if (newName.length() == 0)
			return;

		new LongProcess() {
			public void run() throws Exception {
				try {
					Transaction.start(false);
					dsColumn.setColumnRename(newName, true);
				} finally {
					Transaction.end();
				}
			}
		}.start();
	}

	/**
	 * Renames a table, after prompting the user to enter a new name. By
	 * default, the existing name is used. If the name entered is blank or
	 * matches the existing name, no change is made.
	 * 
	 * @param dsTable
	 *            the table to rename.
	 */
	public void requestRenameDataSetTable(final DataSetTable dsTable) {
		// Ask user for the new name.
		this.requestRenameDataSetTable(dsTable, this.askUserForName(Resources
				.get("requestDataSetTableName"), dsTable.getModifiedName()));
	}

	/**
	 * Renames a table.
	 * 
	 * @param dsTable
	 *            the table to rename.
	 * @param name
	 *            the new name to give it.
	 */
	public void requestRenameDataSetTable(final DataSetTable dsTable,
			final String name) {
		// If the new name is null (user cancelled), or has
		// not changed, don't rename it.
		final String newName = name == null ? "" : name.trim();
		if (newName.length() == 0)
			return;

		new LongProcess() {
			public void run() {
				Transaction.start(false);
				dsTable.setTableRename(newName);
				Transaction.end();
			}
		}.start();
	}

	/**
	 * Requests that a relation be flagged as a subclass relation.
	 * 
	 * @param ds
	 *            the dataset we are working with.
	 * @param relation
	 *            the relation to subclass.
	 * @param subclassed
	 *            whether to do it.
	 */
	public void requestSubclassRelation(final DataSet ds,
			final Relation relation, final boolean subclassed) {
		new LongProcess() {
			public void run() throws Exception {
				try {
					Transaction.start(false);
					relation.setSubclassRelation(ds, subclassed);
				} finally {
					Transaction.end();
				}
			}
		}.start();
	}



	/**
	 * Given a table, suggest a series of synchronised datasets that may be
	 * possible for that table.
	 * 
	 * @param table
	 *            the table to suggest datasets for. If <tt>null</tt>, no
	 *            default table is used.
	 */
	public void requestSuggestDataSets(final Table table) {
		// Ask the user what tables they want to work with and what
		// mode they want.
		final SuggestDataSetDialog dialog = new SuggestDataSetDialog(
				this.mart.getSchemas().values(), table);
		dialog.setVisible(true);

		// If they cancelled it, return without doing anything.
		if (dialog.getSelectedTables().isEmpty())
			return;

		new LongProcess() {
			public void run() throws Exception {
				try {
					Transaction.start(false);
					DataSets.this.mart.suggestDataSets(
							dialog.getSelectedTables());
					Log.info("update view");
				} finally {
					dialog.dispose();
					Transaction.end();
				}
			}
		}.start();

	}


	/**
	 * Requests that the dataset be index optimised.
	 * 
	 * @param dataset
	 *            the dataset to do this to.
	 * @param indexOptimiser
	 *            whether to do it.
	 */
	public void requestIndexOptimiser(final DataSet dataset,
			final boolean indexOptimiser) {
		new LongProcess() {
			public void run() {
				Transaction.start(false);
				dataset.setIndexOptimiser(indexOptimiser);
				Transaction.end();
			}
		}.start();
	}
}
