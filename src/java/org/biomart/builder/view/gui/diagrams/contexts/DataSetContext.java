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

package org.biomart.builder.view.gui.diagrams.contexts;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;

import javax.swing.ImageIcon;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JComponent;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;

import org.biomart.builder.model.DataSet;
import org.biomart.builder.model.DataSetColumn;
import org.biomart.builder.model.DataSetTable;

import org.biomart.builder.model.InheritedColumn;
import org.biomart.builder.model.Key;
import org.biomart.builder.model.Mart;
import org.biomart.builder.model.Relation;
import org.biomart.builder.model.WrappedColumn;
import org.biomart.builder.view.gui.diagrams.components.ColumnComponent;
import org.biomart.builder.view.gui.diagrams.components.KeyComponent;
import org.biomart.builder.view.gui.diagrams.components.RelationComponent;
import org.biomart.builder.view.gui.diagrams.components.TableComponent;
import org.biomart.common.resources.Resources;
import org.biomart.configurator.utils.type.DataSetTableType;

/**
 * This context adapts dataset diagrams to display different colours, and
 * provides the context menu for interacting with dataset diagrams.
 * 
 * @author Richard Holland <holland@ebi.ac.uk>
 * @version $Revision: 1.78 $, $Date: 2008/03/06 15:34:06 $, modified by
 *          $Author: rh4 $
 * @since 0.5
 */
public class DataSetContext extends SchemaContext {
	private DataSet dataset;

	/**
	 * Creates a new context that will adapt database objects according to the
	 * settings in the specified dataset.
	 * 
	 * @param martTab
	 *            the mart tab this context appears in.
	 * @param dataset
	 *            the dataset this context will use for customising menus and
	 *            colours.
	 */
	public DataSetContext(final Mart mart, final DataSet dataset) {
		super(mart);
		this.dataset = dataset;
	}

	/**
	 * Obtain the dataset that this context is linked with.
	 * 
	 * @return our dataset.
	 */
	protected DataSet getDataSet() {
		return this.dataset;
	}

	public void customiseAppearance(final JComponent component,
			final Object object) {

		// Is it a relation?
		if (object instanceof Relation) {

			// Which relation is it?
			final Relation relation = (Relation) object;
			final RelationComponent relcomp = (RelationComponent) component;

			// What tables does it link?
			final DataSetTable target = (DataSetTable) relation.getManyKey()
					.getTable();

			// Is it compounded?
			if (target.getFocusRelation() != null)
				relcomp.setCompounded(false);

			// Fade MASKED DIMENSION relations.
			if (target.isDimensionMasked() || this.getDataSet().isMasked())
				relcomp.setForeground(RelationComponent.MASKED_COLOUR);

			// Highlight SUBCLASS relations.
			else if (target.getType().equals(DataSetTableType.MAIN_SUBCLASS))
				relcomp.setForeground(RelationComponent.SUBCLASS_COLOUR);

			// All the rest are normal.
			else
				relcomp.setForeground(RelationComponent.NORMAL_COLOUR);
		}

		// Is it a table?
		else if (object instanceof DataSetTable) {

			// Which table is it?
			final TableComponent tblcomp = (TableComponent) component;
			final DataSetTable tbl = (DataSetTable) object;
			final DataSetTableType tableType = tbl.getType();

			if (this.isMasked(tbl))
				tblcomp.setBackground(TableComponent.MASKED_COLOUR);

			// Fade MASKED datasets.
			else if (this.getDataSet().isMasked())
				tblcomp.setBackground(TableComponent.MASKED_COLOUR);

			// Fade MERGED DIMENSION tables.
			else if (tbl.getFocusRelation() != null
					&& tbl.getFocusRelation()
							.isMergeRelation(this.getDataSet()))
				tblcomp.setBackground(TableComponent.MASKED_COLOUR);

			// Highlight DIMENSION tables.
			else if (tableType.equals(DataSetTableType.DIMENSION)) {
				// Is it compounded?
				tblcomp.setCompounded(false);
				tblcomp.setBackground(TableComponent.BACKGROUND_COLOUR);
			}

			else
				tblcomp.setBackground(TableComponent.BACKGROUND_COLOUR);

			// Update dotted line (partitioned).
			tblcomp.setRestricted(false);

			tblcomp.setRenameable(true);
			tblcomp.setSelectable(true);
		}

		// Columns.
		else if (object instanceof DataSetColumn) {

			// Which column is it?
			final DataSetColumn column = (DataSetColumn) object;
			final ColumnComponent colcomp = (ColumnComponent) component;

			// Fade out all MASKED columns.
			if (this.isMasked(column))
				colcomp.setBackground(ColumnComponent.MASKED_COLOUR);
			// Red INHERITED columns.
			else if (column instanceof InheritedColumn)
				colcomp.setBackground(ColumnComponent.INHERITED_COLOUR);
			// All others are normal.
			else
				colcomp.setBackground(ColumnComponent.NORMAL_COLOUR);

			// Indexed?
			if (column.isColumnIndexed())
				colcomp.setIndexed(true);
			else
				colcomp.setIndexed(false);

			colcomp.setRenameable(true);
			colcomp.setSelectable(true);
		}

		// Keys
		else if (object instanceof Key) {
			final KeyComponent keycomp = (KeyComponent) component;

			keycomp.setIndexed(true);

			// Remove drag-and-drop from the key as it does not apply in
			// the window context.
			keycomp.setDraggable(false);
		}
	}

	public void populateMultiContextMenu(final JPopupMenu contextMenu,
			final Collection selectedItems, final Class clazz) {

		// Menu for multiple table selection.
		if (DataSetTable.class.isAssignableFrom(clazz)) {
			// If all are dimensions...
			boolean allDimensions = true;
			for (final Iterator i = selectedItems.iterator(); i.hasNext();)
				allDimensions &= ((DataSetTable) i.next()).getType().equals(
						DataSetTableType.DIMENSION);
			if (allDimensions) {
				// The dimension can be removed by using this option. This
				// simply masks the relation that caused the dimension to exist.
				final JMenuItem removeDM = new JMenuItem(Resources
						.get("maskGroupDimensionTitle"));
				removeDM.setMnemonic(Resources
						.get("maskGroupDimensionMnemonic").charAt(0));
				removeDM.addActionListener(new ActionListener() {
					public void actionPerformed(final ActionEvent evt) {
						for (final Iterator i = selectedItems.iterator(); i
								.hasNext();) {
							final DataSetTable table = (DataSetTable) i.next();
							final boolean isMasked = table.isDimensionMasked();
							final boolean isMerged = table.getFocusRelation()
									.isMergeRelation(
											DataSetContext.this.getDataSet());
							contextMenu.add(removeDM);
							if (!isMerged && !isMasked)
								DataSetContext.this.getMart().getDataSetObj()
										.requestMaskDimension(
												DataSetContext.this
														.getDataSet(), table,
												true);
						}
					}
				});
				contextMenu.add(removeDM);

				// Unmask the dimensions.
				final JMenuItem reinstateDM = new JMenuItem(Resources
						.get("unmaskGroupDimensionTitle"));
				reinstateDM.setMnemonic(Resources.get(
						"unmaskGroupDimensionMnemonic").charAt(0));
				reinstateDM.addActionListener(new ActionListener() {
					public void actionPerformed(final ActionEvent evt) {
						for (final Iterator i = selectedItems.iterator(); i
								.hasNext();) {
							final DataSetTable table = (DataSetTable) i.next();
							final boolean isMasked = table.isDimensionMasked();
							contextMenu.add(removeDM);
							if (isMasked)
								DataSetContext.this.getMart().getDataSetObj()
										.requestMaskDimension(
												DataSetContext.this
														.getDataSet(), table,
												false);
						}
					}
				});
				contextMenu.add(reinstateDM);
			} else
				JOptionPane.showMessageDialog(null, Resources.get("multiTableDimOnly"),
						Resources.get("questionTitle"),
						JOptionPane.INFORMATION_MESSAGE);
		}

		// Menu for multiple column selection.
		else if (DataSetColumn.class.isAssignableFrom(clazz)) {

			// The dimension can be removed by using this option. This
			// simply masks the relation that caused the dimension to exist.
			final JMenuItem mask = new JMenuItem(Resources
					.get("maskGroupColumnTitle"));
			mask
					.setMnemonic(Resources.get("maskGroupColumnMnemonic")
							.charAt(0));
			mask.addActionListener(new ActionListener() {
				public void actionPerformed(final ActionEvent evt) {
					final Collection columns = new HashSet();
					for (final Iterator i = selectedItems.iterator(); i
							.hasNext();) {
						final DataSetColumn column = (DataSetColumn) i.next();
						final boolean isMasked = column.isColumnMasked();
						if (!isMasked)
							columns.add(column);
					}
					DataSetContext.this.getMart().getDataSetObj()
							.requestMaskColumns(
									DataSetContext.this.getDataSet(), columns,
									true);
				}
			});
			contextMenu.add(mask);

			// Unmask the columns.
			final JMenuItem unmask = new JMenuItem(Resources
					.get("unmaskGroupColumnTitle"));
			unmask.setMnemonic(Resources.get("unmaskGroupColumnMnemonic")
					.charAt(0));
			unmask.addActionListener(new ActionListener() {
				public void actionPerformed(final ActionEvent evt) {
					final Collection columns = new HashSet();
					for (final Iterator i = selectedItems.iterator(); i
							.hasNext();) {
						final DataSetColumn column = (DataSetColumn) i.next();
						final boolean isMasked = column.isColumnMasked();
						if (isMasked)
							columns.add(column);
					}
					DataSetContext.this.getMart().getDataSetObj()
							.requestMaskColumns(
									DataSetContext.this.getDataSet(), columns,
									false);
				}
			});
			contextMenu.add(unmask);

			contextMenu.addSeparator();

			// Index cols.
			final JMenuItem index = new JMenuItem(Resources
					.get("indexGroupColumnTitle"));
			index.setMnemonic(Resources.get("indexGroupColumnMnemonic").charAt(
					0));
			index.addActionListener(new ActionListener() {
				public void actionPerformed(final ActionEvent evt) {
					for (final Iterator i = selectedItems.iterator(); i
							.hasNext();) {
						final DataSetColumn column = (DataSetColumn) i.next();
						DataSetContext.this.getMart().getDataSetObj()
								.requestIndexColumn(
										DataSetContext.this.getDataSet(),
										column, true);
					}
				}
			});
			contextMenu.add(index);

			// Un-index cols.
			final JMenuItem unindex = new JMenuItem(Resources
					.get("unindexGroupColumnTitle"));
			unindex.setMnemonic(Resources.get("unindexGroupColumnMnemonic")
					.charAt(0));
			unindex.addActionListener(new ActionListener() {
				public void actionPerformed(final ActionEvent evt) {
					for (final Iterator i = selectedItems.iterator(); i
							.hasNext();) {
						final DataSetColumn column = (DataSetColumn) i.next();
						DataSetContext.this.getMart().getDataSetObj()
								.requestIndexColumn(
										DataSetContext.this.getDataSet(),
										column, false);
					}
				}
			});
			contextMenu.add(unindex);
		}
	}

	public boolean isMasked(final Object object) {

		final String schemaPrefix = null;

		// Is it a relation?
		if (object instanceof Relation) {
			// Which relation is it?
			final Relation relation = (Relation) object;

			// What tables does it link?
			final DataSetTable target = (DataSetTable) relation.getManyKey()
					.getTable();

			// Fade MASKED DIMENSION relations.
			if (this.isMasked(target))
				return true;
		}

		// Is it a table?
		else if (object instanceof DataSetTable) {
			final DataSetTable dsTable = (DataSetTable) object;
			// Fade MASKED DIMENSION relations.
			if (dsTable.isDimensionMasked())
				return true;
		}

		// Is it a column?
		else if (object instanceof DataSetColumn) {
			final DataSetColumn dsCol = (DataSetColumn) object;
			if (dsCol.isColumnMasked())
				return true;
		}

		return false;
	}

	public void populateContextMenu(final JPopupMenu contextMenu,
			final Object object) {
		// Did the user click on a dataset table?
		if (object instanceof DataSetTable) {

			// Add a separator if the menu is not empty.
			if (contextMenu.getComponentCount() > 0)
				contextMenu.addSeparator();

			// Work out which table we are dealing with, and what type it is.
			final DataSetTable table = (DataSetTable) object;
			final DataSetTableType tableType = table.getType();

			final boolean isMasked = table.isDimensionMasked();
			final boolean isMerged = table.getFocusRelation() != null
					&& table.getFocusRelation().isMergeRelation(
							this.getDataSet());
			final boolean isUnrolled = false;
			final boolean isCompound = false;

			contextMenu.addSeparator();

			// Rename the table.
			final JMenuItem rename = new JMenuItem(Resources
					.get("renameTableTitle"));
			rename.setMnemonic(Resources.get("renameTableMnemonic").charAt(0));
			rename.addActionListener(new ActionListener() {
				public void actionPerformed(final ActionEvent evt) {
					DataSetContext.this.getMart().getDataSetObj()
							.requestRenameDataSetTable(table);
				}
			});
			contextMenu.add(rename);


			contextMenu.addSeparator();


			// Subclasses and dimensions have optimiser columns.
			if (!tableType.equals(DataSetTableType.MAIN)) {
				// The table can be no-optimised by using this option.
				final JCheckBoxMenuItem skipOptimiser = new JCheckBoxMenuItem(
						Resources.get("skipOptimiserTitle"));
				skipOptimiser.setMnemonic(Resources
						.get("skipOptimiserMnemonic").charAt(0));
				skipOptimiser.addActionListener(new ActionListener() {
					public void actionPerformed(final ActionEvent evt) {
						DataSetContext.this.getMart().getDataSetObj()
								.requestSkipOptimiser(
										DataSetContext.this.getDataSet(),
										table, skipOptimiser.isSelected());
					}
				});
				contextMenu.add(skipOptimiser);
				skipOptimiser.setSelected(table.isSkipOptimiser());

				// The table can be no-index-optimised by using this option.
				final JCheckBoxMenuItem skipIndexOptimiser = new JCheckBoxMenuItem(
						Resources.get("skipIndexOptimiserTitle"));
				skipIndexOptimiser.setMnemonic(Resources.get(
						"skipIndexOptimiserMnemonic").charAt(0));
				skipIndexOptimiser.addActionListener(new ActionListener() {
					public void actionPerformed(final ActionEvent evt) {
						DataSetContext.this.getMart().getDataSetObj()
								.requestSkipIndexOptimiser(
										DataSetContext.this.getDataSet(),
										table, skipIndexOptimiser.isSelected());
					}
				});
				contextMenu.add(skipIndexOptimiser);
				skipIndexOptimiser.setEnabled(!table.isSkipOptimiser());
				skipIndexOptimiser.setSelected(table.isSkipIndexOptimiser());
			}

			// Dimension tables have their own options.
			if (tableType.equals(DataSetTableType.DIMENSION)) {


				// The dimension can be merged by using this option. This
				// affects all dimensions based on this relation.
				final JCheckBoxMenuItem mergeDM = new JCheckBoxMenuItem(
						Resources.get("mergeDimensionTitle"));
				mergeDM.setMnemonic(Resources.get("mergeDimensionMnemonic")
						.charAt(0));
				mergeDM.addActionListener(new ActionListener() {
					public void actionPerformed(final ActionEvent evt) {
						DataSetContext.this.getMart().getDataSetObj()
								.requestMergeDimension(
										DataSetContext.this.getDataSet(),
										table, mergeDM.isSelected());
					}
				});
				contextMenu.add(mergeDM);
				mergeDM.setSelected(isMerged);
				if (isCompound || isUnrolled)
					mergeDM.setEnabled(false);

				// The dimension can be removed by using this option. This
				// simply masks the relation that caused the dimension to exist.
				final JCheckBoxMenuItem removeDM = new JCheckBoxMenuItem(
						Resources.get("maskDimensionTitle"));
				removeDM.setMnemonic(Resources.get("maskDimensionMnemonic")
						.charAt(0));
				removeDM.addActionListener(new ActionListener() {
					public void actionPerformed(final ActionEvent evt) {
						DataSetContext.this.getMart().getDataSetObj()
								.requestMaskDimension(
										DataSetContext.this.getDataSet(),
										table, removeDM.isSelected());
					}
				});
				contextMenu.add(removeDM);
				if (isMerged || isCompound || isUnrolled)
					removeDM.setEnabled(false);
				if (isMasked)
					removeDM.setSelected(true);


				contextMenu.addSeparator();

				// The dim table can be subclassed by using this option. This
				// simply subclasses the relation that caused the dim to exist.
				final JMenuItem subclass = new JMenuItem(Resources
						.get("dimToSubclassTitle"));
				subclass.setMnemonic(Resources.get("dimToSubclassMnemonic")
						.charAt(0));
				subclass.addActionListener(new ActionListener() {
					public void actionPerformed(final ActionEvent evt) {
						DataSetContext.this.getMart().getDataSetObj()
								.requestSubclassRelation(
										DataSetContext.this.getDataSet(),
										table.getFocusRelation(), true);
					}
				});
				if (isMerged || isMasked || isCompound || isUnrolled)
					subclass.setEnabled(false);
				contextMenu.add(subclass);

				contextMenu.addSeparator();

			}

			// Subclass tables have their own options too.
			if (tableType.equals(DataSetTableType.MAIN_SUBCLASS)) {

				contextMenu.addSeparator();

				// The subclass table can be removed by using this option. This
				// simply masks the relation that caused the subclass to exist.
				final JMenuItem unsubclass = new JMenuItem(Resources
						.get("removeSubclassTitle"));
				unsubclass.setMnemonic(Resources.get("removeSubclassMnemonic")
						.charAt(0));
				unsubclass.addActionListener(new ActionListener() {
					public void actionPerformed(final ActionEvent evt) {
						DataSetContext.this.getMart().getDataSetObj()
								.requestSubclassRelation(
										DataSetContext.this.getDataSet(),
										table.getFocusRelation(), false);
					}
				});
				contextMenu.add(unsubclass);

			}

			contextMenu.addSeparator();

			// Accept/Reject changes - only enabled if dataset table
			// is visible modified.
			final JMenuItem accept = new JMenuItem(Resources
					.get("acceptChangesTitle"));
			accept
					.setMnemonic(Resources.get("acceptChangesMnemonic").charAt(
							0));
			accept.addActionListener(new ActionListener() {
				public void actionPerformed(final ActionEvent evt) {
					DataSetContext.this.getMart().getDataSetObj()
							.requestAcceptAll(table, null);
				}
			});
			accept.setEnabled(table.isVisibleModified());
			contextMenu.add(accept);

			final JMenuItem reject = new JMenuItem(Resources
					.get("rejectChangesTitle"));
			reject
					.setMnemonic(Resources.get("rejectChangesMnemonic").charAt(
							0));
			reject.addActionListener(new ActionListener() {
				public void actionPerformed(final ActionEvent evt) {
					DataSetContext.this.getMart().getDataSetObj()
							.requestRejectAll(table, null);
				}
			});
			reject.setEnabled(table.isVisibleModified());
			contextMenu.add(reject);
		}

		// Column menu goes here.
		else if (object instanceof DataSetColumn) {
			// Add separator if the menu is not empty.
			if (contextMenu.getComponentCount() > 0)
				contextMenu.addSeparator();

			// Work out which column has been clicked.
			final DataSetColumn column = (DataSetColumn) object;

			// Rename the column.
			final JMenuItem rename = new JMenuItem(Resources
					.get("renameColumnTitle"));
			rename.setMnemonic(Resources.get("renameColumnMnemonic").charAt(0));
			rename.addActionListener(new ActionListener() {
				public void actionPerformed(final ActionEvent evt) {
					DataSetContext.this.getMart().getDataSetObj()
							.requestRenameDataSetColumn(column);
				}
			});
			contextMenu.add(rename);

			contextMenu.addSeparator();

			// Mask the column.
			final boolean isMasked = column.isColumnMasked();
			final JCheckBoxMenuItem mask = new JCheckBoxMenuItem(Resources
					.get("maskColumnTitle"));
			mask.setMnemonic(Resources.get("maskColumnMnemonic").charAt(0));
			mask.addActionListener(new ActionListener() {
				public void actionPerformed(final ActionEvent evt) {
					DataSetContext.this.getMart().getDataSetObj()
							.requestMaskColumn(
									DataSetContext.this.getDataSet(), column,
									mask.isSelected());
				}
			});
			contextMenu.add(mask);
			mask.setSelected(isMasked);

			// Index the column.
			final boolean isIndexed = column.isColumnIndexed();
			final JCheckBoxMenuItem index = new JCheckBoxMenuItem(Resources
					.get("indexColumnTitle"));
			index.setMnemonic(Resources.get("indexColumnMnemonic").charAt(0));
			index.addActionListener(new ActionListener() {
				public void actionPerformed(final ActionEvent evt) {
					DataSetContext.this.getMart().getDataSetObj()
							.requestIndexColumn(
									DataSetContext.this.getDataSet(), column,
									index.isSelected());
				}
			});
			contextMenu.add(index);
			index.setSelected(isIndexed);

		}
	}
}
