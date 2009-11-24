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

import java.awt.Dimension;
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
import java.util.Vector;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.table.DefaultTableModel;
import org.biomart.builder.model.ForeignKey;
import org.biomart.builder.model.PrimaryKey;
import org.biomart.builder.view.gui.diagrams.Diagram;
import org.biomart.builder.view.gui.diagrams.SchemaDiagram;
import org.biomart.builder.view.gui.dialogs.KeyDialog;
import org.biomart.common.resources.Resources;
import org.biomart.common.utils.Transaction;
import org.biomart.common.view.gui.LongProcess;
import org.biomart.common.view.gui.SwingWorker;
import org.biomart.common.view.gui.dialogs.ProgressDialog;
import org.biomart.common.view.gui.dialogs.StackTrace;
import org.biomart.configurator.utils.McUtils;
import org.biomart.configurator.utils.type.Cardinality;
import org.biomart.configurator.view.gui.diagrams.contexts.DiagramContext;
import org.biomart.configurator.view.gui.diagrams.contexts.SchemaContext;


/**
 * This tabset has one tab for the diagram which represents all schemas, and one
 * tab each for each schema in the mart. It provides methods for working with a
 * given schema, such as adding or removing them, or grouping them together. It
 * can update itself based on the schemas in the mart on request.
 * <p>
 * Like a diagram, it can have a {@link DiagramContext} associated with it.
 * Whenever this context changes, all {@link Diagram} instances represented in
 * the tabs have the same context applied.
 * 
 * @author Richard Holland <holland@ebi.ac.uk>
 * @version $Revision: 1.114 $, $Date: 2008/02/21 09:35:26 $, modified by
 *          $Author: rh4 $
 * @since 0.5
 */
public class Schemas {
	
	private DiagramContext diagramContext;
	private Mart mart;
	private final Map<String,Schema> schemasMap = new HashMap<String,Schema>();

	// Make a listener which knows how to handle masking and
	// renaming.
	public final PropertyChangeListener updateListener = new PropertyChangeListener() {
		public void propertyChange(PropertyChangeEvent evt) {
			final Schema sch = (Schema) evt.getSource();
			if (evt.getPropertyName().equals("name")) {
				// Rename in diagram set.
				Schemas.this.schemasMap.put((String)evt.getNewValue(),
						Schemas.this.schemasMap.remove(evt
								.getOldValue()));
			} else if (evt.getPropertyName().equals("masked")) {
				// For masks, if unmasking, add a tab, otherwise
				// remove the tab.
				final boolean masked = ((Boolean) evt.getNewValue())
						.booleanValue();
				if (masked)
					Schemas.this.removeSchemaTab(sch.getName(), true);
				else
					Schemas.this.addSchema(sch);
			}
		}
	};


	/**
	 * Creates a new set of tabs to represent the schemas in a mart. The mart is
	 * obtained by using methods on the mart tab passed in as a parameter. The
	 * mart tab is the parent tab that this schema tabset will appear inside the
	 * tabs of.
	 * 
	 * @param martTab
	 *            the parent tab this schema tabset will appear inside the tabs
	 *            of.
	 */
	public Schemas(final Mart mart) {
		super();

		// Remember the mart tabset we are shown inside.
		this.mart = mart;
		// Add the all-schemas overview tab. This tab displays a diagram
		// in which all schemas appear, linked where necessary by external
		// relations. This diagram could be quite large, so it is held inside
		// a scrollpane.
		// Populate the map to hold the relation between schemas and the
		// diagrams representing them.
		if(mart.getSchemas()!=null) {
			for (final Iterator i = mart.getSchemas().values()
					.iterator(); i.hasNext();) {
				final Schema sch = (Schema)i.next();
				// Don't add schemas which are initially masked.
				if (!sch.isMasked())
					this.addSchema(sch);
				sch.addPropertyChangeListener("masked", this.updateListener);
				sch.addPropertyChangeListener(Resources.get("PCNAME"), this.updateListener);
			}
		}

		//set DiagramContext
		final SchemaContext context = new SchemaContext(mart);
		this.setDiagramContext(context);

	}

	public synchronized void addSchema(final Schema schema) {
		if(!(schema instanceof JDBCSchema))
			return;
		JDBCSchema jdbcSchema = (JDBCSchema)schema;
		// Create the diagram to represent this schema.
		final SchemaDiagram schemaDiagram = new SchemaDiagram(this.mart,
				schema);

		// Remember which diagram the schema is connected with.
		//TODO which name?
		this.schemasMap.put(schema.getName(), jdbcSchema);
		//this.schemasMap.put(schema.getOriginalName(), jdbcSchema);
		jdbcSchema.setSchemaDiagram(schemaDiagram);

		// Set the current context on the diagram to be the same as the
		// current context on this schema tabset.
		schemaDiagram.setDiagramContext(this.getDiagramContext());
	}

	private String askUserForSchemaName(final String defaultResponse) {
		// Ask user for a name, giving them the default suggestion.
		String name = (String) JOptionPane.showInputDialog(null, Resources
				.get("requestSchemaName"), Resources.get("questionTitle"),
				JOptionPane.QUESTION_MESSAGE, null, null, defaultResponse);

		// If they didn't select anything, return null.
		if (name == null)
			return null;

		// If they entered an empty string, ie. deleted the default
		// but didn't type anything else, make it as though
		// it had not been deleted.
		else if (name.trim().length() == 0)
			name = defaultResponse;

		// Return the response.
		return name;
	}

	private Key askUserForTargetKey(final Key from) {
		// Given a particular key, work out which other keys, in any schema,
		// this key may be linked to.

		// Start by making a list to contain the candidates.
		final List<Key> candidates = new ArrayList<Key>();

		// We want all keys that have the same number of columns.
		for (final Iterator i = this.mart.getSchemas().values()
				.iterator(); i.hasNext();)
			for (final Iterator j = ((Schema) i.next()).getTables().values()
					.iterator(); j.hasNext();) {
				final Table tbl = (Table) j.next();
				for (final Iterator k = tbl.getKeys().iterator(); k.hasNext();) {
					final Key key = (Key) k.next();
					if (key.getColumns().length == from.getColumns().length
							&& !key.equals(from))
						candidates.add(key);
				}
			}
		// Alphabetize.
		Collections.sort(candidates);

		// Put up a box asking which key to link this key to, based on the
		// list of candidates we just made. Return the key that the user
		// selects, or null if none was selected.
		return (Key) JOptionPane.showInputDialog(null, Resources
				.get("whichKeyToLinkRelationTo"), Resources
				.get("questionTitle"), JOptionPane.QUESTION_MESSAGE, null,
				candidates.toArray(), null);
	}

	public synchronized void removeSchemaTab(final String schemaName,
			final boolean select) {
		this.schemasMap.remove(schemaName);
	}


	/**
	 * Returns the diagram context currently being used by {@link Diagram}s in
	 * this schema tabset.
	 * 
	 * @return the diagram context currently being used.
	 */
	public DiagramContext getDiagramContext() {
		return this.diagramContext;
	}


	public Schema getSchema(String name) {
		return this.schemasMap.get(name);
	}
	
	public Map<String,Schema> getSchemas() {
		return this.schemasMap;
	}
	
	/**
	 * Update a key status.
	 * 
	 * @param key
	 *            the key to update the status of.
	 * @param status
	 *            the new status to give it.
	 */
	public void requestChangeKeyStatus(final Key key,
			final ComponentStatus status) {
		new LongProcess() {
			public void run() {
				Transaction.start(false);
				key.setStatus(status);
				Transaction.end();
			}
		}.start();
	}

	/**
	 * Update a relation cardinality.
	 * 
	 * @param relation
	 *            the relation to change cardinality of.
	 * @param cardinality
	 *            the new cardinality to give it.
	 */
	public void requestChangeRelationCardinality(final Relation relation,
			final Cardinality cardinality) {
		new LongProcess() {
			public void run() throws Exception {
				try {
					Transaction.start(true);
					relation.setCardinality(cardinality);
					if (!relation.getStatus().equals(ComponentStatus.HANDMADE))
						relation
								.setStatus(cardinality.equals(relation
										.getOriginalCardinality()) ? ComponentStatus.INFERRED
										: ComponentStatus.MODIFIED);
				} finally {
					Transaction.end();
				}
			}
		}.start();
	}


	/**
	 * Update a relation status.
	 * 
	 * @param relation
	 *            the relation to change the status for.
	 * @param status
	 *            the new status to give it.
	 */
	public void requestChangeRelationStatus(final Relation relation,
			final ComponentStatus status) {
		new LongProcess() {
			public void run() throws Exception {
				try {
					Transaction.start(false);
					relation.setStatus(status);
				} finally {
					Transaction.end();
				}
			}
		}.start();
	}

	/**
	 * Ask the user to define a foreign key on a table, then create it.
	 * 
	 * @param table
	 *            the table to define the key on.
	 */
	public void requestCreateForeignKey(final Table table) {
		// Pop up a dialog to ask which columns to use.
		final KeyDialog dialog = new KeyDialog(table, Resources
				.get("newFKDialogTitle"), Resources.get("addButton"), null);
		dialog.setLocationRelativeTo(null);
		dialog.setVisible(true);
		final Column[] cols = dialog.getSelectedColumns();
		dialog.dispose();

		// If they chose some columns, create the key.
		if (cols.length > 0)
			this.requestCreateForeignKey(table, cols);
	}

	/**
	 * Given a set of columns, create a foreign key on the given table that
	 * contains those columns in the order they appear in the iterator.
	 * 
	 * @param table
	 *            the table to create the key over.
	 * @param columns
	 *            the columns to include the key.
	 */
	public void requestCreateForeignKey(final Table table,
			final Column[] columns) {
		new LongProcess() {
			public void run() {
				Transaction.start(false);
				final ForeignKey fk = new ForeignKey(columns);
				fk.setStatus(ComponentStatus.HANDMADE);
				table.getForeignKeys().add(fk);
				Transaction.end();
			}
		}.start();
	}

	/**
	 * Ask the user to define a primary key on a table, then create it.
	 * 
	 * @param table
	 *            the table to define the key on.
	 */
	public void requestCreatePrimaryKey(final Table table) {
		// Pop up a dialog to ask which columns to use.
		final KeyDialog dialog = new KeyDialog(table, Resources
				.get("newPKDialogTitle"), Resources.get("addButton"), null);
		dialog.setLocationRelativeTo(null);
		dialog.setVisible(true);
		final Column[] cols = dialog.getSelectedColumns();
		dialog.dispose();

		// If they chose some columns, create the key.
		if (cols.length > 0)
			this.requestCreatePrimaryKey(table, cols);
	}

	/**
	 * Given a set of columns, create a primary key on the given table that
	 * contains those columns in the order they appear in the iterator. This
	 * will replace any existing primary key on the table.
	 * 
	 * @param table
	 *            the table to create the key over.
	 * @param columns
	 *            the columns to include the key.
	 */
	public void requestCreatePrimaryKey(final Table table,
			final Column[] columns) {
		new LongProcess() {
			public void run() {
				Transaction.start(false);
				final PrimaryKey pk = new PrimaryKey(columns);
				pk.setStatus(ComponentStatus.HANDMADE);
				table.setPrimaryKey(pk);
				Transaction.end();
			}
		}.start();
	}

	/**
	 * Given a key, ask the user which other key they want to make a relation to
	 * from this key.
	 * 
	 * @param from
	 *            the key to make a relation from.
	 */
	public void requestCreateRelation(final Key from) {
		// Ask them which key they want to link to.
		final Key to = this.askUserForTargetKey(from);

		// If they selected something, create the relation to it.
		if (to != null)
			this.requestCreateRelation(from, to);
	}

	/**
	 * Given a pair of keys, establish a relation between them.
	 * 
	 * @param from
	 *            one end of the relation.
	 * @param to
	 *            the other end.
	 */
	public void requestCreateRelation(final Key from, final Key to) {
		// Create the relation in the background.
		new LongProcess() {
			public void run() throws Exception {
				try {
					Transaction.start(true);
					final Relation rel = new Relation(
							from,
							to,
							from instanceof PrimaryKey ? (to instanceof PrimaryKey ? Cardinality.ONE
									: Cardinality.MANY_A)
									: (to instanceof PrimaryKey ? Cardinality.MANY_B
											: Cardinality.MANY_A));
					rel.setStatus(ComponentStatus.HANDMADE);
					from.getRelations().add(rel);
					to.getRelations().add(rel);
				} finally {
					Transaction.end();
				}
			}
		}.start();
	}


	/**
	 * Pop up a dialog describing the key, and ask the user to modify it, before
	 * carrying out the modification.
	 * 
	 * @param key
	 *            the key to edit.
	 */
	public void requestEditKey(final Key key) {
		// Pop up the dialog which describes the key, and obtain the
		// list of columns they selected in response.
		final KeyDialog dialog = new KeyDialog(key.getTable(), Resources
				.get("editKeyDialogTitle"), Resources.get("modifyButton"), key
				.getColumns());
		dialog.setLocationRelativeTo(null);
		dialog.setVisible(true);
		final Column[] cols = dialog.getSelectedColumns();
		dialog.dispose();

		// If they selected any columns, modify the key.
		if (cols.length > 0)
			new LongProcess() {
				public void run() {
					Transaction.start(false);
					key.setColumns(cols);
					key.setStatus(ComponentStatus.HANDMADE);
					Transaction.end();
				}
			}.start();
	}

	/**
	 * Turn keyguessing on for a schema.
	 * 
	 * @param schema
	 *            the schema to turn keyguessing on for.
	 * @param keyGuessing
	 *            <tt>true</tt> to turn it on, not for off.
	 */
	public void requestKeyGuessing(final Schema schema,
			final boolean keyGuessing) {
		// Create a progress monitor.
		final ProgressDialog progressMonitor = new ProgressDialog(null, 0, 100,
				false);

		// Start the construction in a thread. It does not need to be
		// Swing-thread-safe because it will never access the GUI. All
		// GUI interaction is done through the Timer below.
		final SwingWorker worker = new SwingWorker() {
			public Object construct() {
				Transaction.start(true);
				try {
					schema.setKeyGuessing(keyGuessing);
				} catch (final Throwable t) {
					SwingUtilities.invokeLater(new Runnable() {
						public void run() {
							StackTrace.showStackTrace(t);
						}
					});
				}
				Transaction.end();
				return null;
			}

			public void finished() {
				// Close the progress dialog.
				progressMonitor.setVisible(false);
				progressMonitor.dispose();
				// This is to ensure that any modified flags get cleared.
				((SchemaDiagram) ((JDBCSchema)(Schemas.this.schemasMap.get(schema
						.getName()))).getSchemaDiagram()).repaintDiagram();
			}
		};

		// Create a timer thread that will update the progress dialog.
		// We use the Swing Timer to make it Swing-thread-safe. (1000 millis
		// equals 1 second.)
		final Timer timer = new Timer(300, null);
		timer.setInitialDelay(0); // Start immediately upon request.
		timer.setCoalesce(true); // Coalesce delayed events.
		timer.addActionListener(new ActionListener() {
			public void actionPerformed(final ActionEvent e) {
				SwingUtilities.invokeLater(new Runnable() {
					public void run() {
						final double progress = schema.getProgress();
						// Did the job complete yet?
						if (progress < 100.0 && progressMonitor.isVisible())
							// If not, update the progress report.
							progressMonitor.setProgress((int) progress);
						else {
							// If it completed, close the task and tidy up.
							// Stop the timer.
							timer.stop();
						}
					}
				});
			}
		});

		// Start the timer.
		timer.start();
		worker.start();
		progressMonitor.setVisible(true);
	}

	/**
	 * Pops up a dialog with details of the schema, which allows the user to
	 * modify them.
	 * 
	 * @param schema
	 *            the schema to modify.
	 */
	public void requestModifySchema(final Schema schema) {
//		if (SchemaConnectionDialog.modifySchema(schema))
//			this.requestSynchroniseSchema(schema, true);
	}

	/**
	 * Remove a key.
	 * 
	 * @param key
	 *            the key to remove.
	 */
	public void requestRemoveKey(final Key key) {
		new LongProcess() {
			public void run() {
				Transaction.start(false);
				if (key instanceof PrimaryKey)
					key.getTable().setPrimaryKey(null);
				else
					key.getTable().getForeignKeys().remove(key);
				Transaction.end();
			}
		}.start();
	}

	/**
	 * Remove a relation.
	 * 
	 * @param relation
	 *            the relation to remove.
	 */
	public void requestRemoveRelation(final Relation relation) {
		new LongProcess() {
			public void run() {
				Transaction.start(false);
				relation.getFirstKey().getRelations().remove(relation);
				relation.getSecondKey().getRelations().remove(relation);
				Transaction.end();
			}
		}.start();
	}


	/**
	 * Asks user for a new name, then renames a schema.
	 * 
	 * @param schema
	 *            the schema to rename.
	 */
	public void requestRenameSchema(final Schema schema) {
		// Ask for a new name, suggesting the schema's existing name
		// as the default response.
		this.requestRenameSchema(schema, this.askUserForSchemaName(schema
				.getName()));
	}

	/**
	 * Requests that the schema be given the new name, now, without further
	 * prompting
	 * 
	 * @param schema
	 *            the schema to rename.
	 * @param name
	 *            the new name to give it.
	 */
	public void requestRenameSchema(final Schema schema, final String name) {
		// Ask for a new name, suggesting the schema's existing name
		// as the default response.
		final String newName = name == null ? "" : name.trim();

		// If they cancelled or entered the same name, ignore the request.
		if (newName.length() == 0)
			return;

		new LongProcess() {
			public void run() {
				Transaction.start(false);
				schema.setName(newName);
				Transaction.end();
			}
		}.start();
	}

	/**
	 * Shows some rows of the table in a {@link JTable} in a popup dialog.
	 * 
	 * @param table
	 *            the table to show rows from.
	 * @param count
	 *            how many rows to show.
	 */
	public void requestShowRows(final Table table, final int count) {
		new LongProcess() {
			public void run() throws Exception {
				// Get the rows.
				final Collection rows = table.getSchema().getRows(
						null, table, count);
				// Convert to a nested vector.
				final Vector data = new Vector();
				for (final Iterator i = rows.iterator(); i.hasNext();)
					data.add(new Vector((List) i.next()));
				// Get the column names.
				final Vector colNames = new Vector(table.getColumns().keySet());
				// Construct a JTable.
				final JTable jtable = new JTable(new DefaultTableModel(data,
						colNames));
				final Dimension size = new Dimension();
				size.width = 0;
				size.height = jtable.getRowHeight() * count;
				for (int i = 0; i < jtable.getColumnCount(); i++)
					size.width += jtable.getColumnModel().getColumn(i)
							.getPreferredWidth();
				size.width = Math.min(size.width, 800); // Arbitrary.
				size.height = Math.min(size.height, 200); // Arbitrary.
				jtable.setPreferredScrollableViewportSize(size);
				// Display them.
				SwingUtilities.invokeLater(new Runnable() {
					public void run() {
						JOptionPane.showMessageDialog(null, new JScrollPane(
								jtable), Resources.get("showRowsDialogTitle",
								new String[] { "" + count, table.getName() }),
								JOptionPane.INFORMATION_MESSAGE);
					}
				});
			}
		}.start();
	}

	/**
	 * Syncs this schema against the database.
	 * 
	 * @param schema
	 *            the schema to synchronise.
	 * @param transactionMod
	 *            <tt>true</tt> if the transaction is allowed to show visible
	 *            modifications.
	 */
	public void requestSynchroniseSchema(final Schema schema,
			final boolean transactionMod) {
				Transaction.start(transactionMod);
				try {
					schema.synchronise();
				} catch (final Throwable t) {
					SwingUtilities.invokeLater(new Runnable() {
						public void run() {
							StackTrace.showStackTrace(t);
						}
					});
				}
				Transaction.end();
				// This is to ensure that any modified flags get cleared.
				JDBCSchema sch = (JDBCSchema) Schemas.this.schemasMap.get(schema.getName());
				((SchemaDiagram) sch.getSchemaDiagram()).repaintDiagram();
	}
	
	/**
	 * Syncs this schema against the database.
	 * 
	 * @param schema
	 *            the schema to synchronise.
	 * @param transactionMod
	 *            <tt>true</tt> if the transaction is allowed to show visible
	 *            modifications.
	 */
	public void requestInitSchema(final Schema schema,
			final boolean transactionMod, final List<String> tables) {
		long t1 = McUtils.getCurrentTime();
				Transaction.start(transactionMod);
		long t2 = McUtils.getCurrentTime();
				try {
					schema.init(tables);
				} catch (final Throwable t) {
					SwingUtilities.invokeLater(new Runnable() {
						public void run() {
							StackTrace.showStackTrace(t);
						}
					});
				}
		long t3 = McUtils.getCurrentTime();
				Transaction.end();
		long t4 = McUtils.getCurrentTime();
		System.err.println("transaction start "+(t2-t1));
		System.err.println("schema init "+(t3-t2));
		System.err.println("transaction end "+(t4-t3));
				// This is to ensure that any modified flags get cleared.
//				JDBCSchema sch = (JDBCSchema) Schemas.this.schemasMap.get(schema.getName());
	//			((SchemaDiagram) sch.getSchemaDiagram()).repaintDiagram();
	}


	/**
	 * Request that all changes on this schema are accepted.
	 * 
	 * @param sch
	 *            the target schema.
	 */
	public void requestAcceptAll(final Schema sch) {
		new LongProcess() {
			public void run() {
				final List modTbls = new ArrayList();
				Transaction.start(true);
				for (final Iterator i = sch.getTables().values().iterator(); i
						.hasNext();) {
					final Table tbl = (Table) i.next();
					if (tbl.isVisibleModified()) {
						modTbls.add(tbl);
						for (final Iterator k = tbl.getColumns().values().iterator(); k.hasNext(); )
							((Column)k.next()).setVisibleModified(false);
						for (final Iterator k = tbl.getRelations().iterator(); k.hasNext(); )
							((Relation)k.next()).setVisibleModified(false);
					}
				}
				Transaction.end();
				for (final Iterator i = sch.getMart().getDataSets().values()
						.iterator(); i.hasNext();) {
					final DataSet ds = (DataSet) i.next();
					if (!ds.isVisibleModified())
						continue;
					for (final Iterator j = modTbls.iterator(); j.hasNext();) {
						final Table modTbl = (Table)j.next();
						Schemas.this.mart.getDataSetObj()
								.requestAcceptAll(ds, modTbl);
					}
				}
			}
		}.start();
	}

	/**
	 * Request that all changes on this schema are rejected.
	 * 
	 * @param sch
	 *            the target schema.
	 */
	public void requestRejectAll(final Schema sch) {
		new LongProcess() {
			public void run() {
				final List modTbls = new ArrayList();
				Transaction.start(true);
				for (final Iterator i = sch.getTables().values().iterator(); i
						.hasNext();) {
					final Table tbl = (Table) i.next();
					if (tbl.isVisibleModified()) {
						modTbls.add(tbl);
						for (final Iterator k = tbl.getColumns().values().iterator(); k.hasNext(); )
							((Column)k.next()).setVisibleModified(false);
						for (final Iterator k = tbl.getRelations().iterator(); k.hasNext(); )
							((Relation)k.next()).setVisibleModified(false);
					}
				}
				Transaction.end();
				for (final Iterator i = sch.getMart().getDataSets().values()
						.iterator(); i.hasNext();) {
					final DataSet ds = (DataSet) i.next();
					if (!ds.isVisibleModified())
						continue;
					for (final Iterator j = modTbls.iterator(); j.hasNext();) {
						final Table modTbl = (Table)j.next();
						Schemas.this.mart.getDataSetObj()
								.requestRejectAll(ds, modTbl);
					}
				}
			}
		}.start();
	}

	/**
	 * Sets the diagram context to use for all {@link Diagram}s inside this
	 * schema tabset. Once set,
	 * {@link Diagram#setDiagramContext(DiagramContext)} is called on each
	 * diagram in the tabset in turn so that they are all working with the same
	 * context.
	 * 
	 * @param diagramContext
	 *            the context to use for all {@link Diagram}s in this schema
	 *            tabset.
	 */
	public void setDiagramContext(final DiagramContext diagramContext) {
		this.diagramContext = diagramContext;
		for (final Iterator<Schema> i = this.schemasMap.values().iterator(); i
				.hasNext();)
			((JDBCSchema)i.next()).getSchemaDiagram().setDiagramContext(diagramContext);
	}

}
