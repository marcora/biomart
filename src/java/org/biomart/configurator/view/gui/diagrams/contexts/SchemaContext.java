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
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JComponent;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JRadioButtonMenuItem;

import org.biomart.builder.model.Column;
import org.biomart.builder.model.ComponentStatus;
import org.biomart.builder.model.Key;
import org.biomart.builder.model.Mart;
import org.biomart.builder.model.Relation;
import org.biomart.builder.model.Schema;
import org.biomart.builder.model.Table;
import org.biomart.common.resources.Resources;
import org.biomart.configurator.utils.McUtils;
import org.biomart.configurator.utils.type.Cardinality;
import org.biomart.configurator.view.gui.diagrams.components.ColumnComponent;
import org.biomart.configurator.view.gui.diagrams.components.KeyComponent;
import org.biomart.configurator.view.gui.diagrams.components.RelationComponent;
import org.biomart.configurator.view.gui.diagrams.components.TableComponent;

/**
 * Provides the context menus and colour schemes to use when viewing a schema in
 * its plain vanilla form, ie. not a dataset schema, and not a window from a
 * dataset onto a set of masked relations.
 * 
 * @author Richard Holland <holland@ebi.ac.uk>
 * @version $Revision: 1.50 $, $Date: 2008/03/03 12:35:08 $, modified by
 *          $Author: rh4 $
 * @since 0.5
 */
public class SchemaContext implements DiagramContext {

	private Mart mart;

	/**
	 * Creates a new context which will pass any menu actions onto the given
	 * mart tab.
	 * 
	 * @param martTab
	 *            the mart tab which will receive any menu actions the user
	 *            selects.
	 */
	public SchemaContext(final Mart mart) {
		this.mart = mart;
	}

	/**
	 * Obtain the mart tab to pass menu events onto.
	 * 
	 * @return the mart tab this context is attached to.
	 */
	protected Mart getMart() {
		return this.mart;
	}

	public void customiseAppearance(final JComponent component,
			final Object object) {

		// This bit removes a restricted outline from any restricted tables.
		if (object instanceof Table) {
			final TableComponent tblcomp = (TableComponent) component;
			final Table table = (Table) object;
			tblcomp.setRestricted(false);

			// Fade out all ignored tables.
			if (this.isMasked(table))
				tblcomp.setBackground(TableComponent.IGNORE_COLOUR);

			// All others are normal.
			else
				tblcomp.setBackground(TableComponent.BACKGROUND_COLOUR);
		}

		// This bit removes a restricted outline from any restricted tables.
		else if (object instanceof Column) {
			final ColumnComponent colcomp = (ColumnComponent) component;
			final Column col = (Column) object;

			// Fade out all ignored tables.
			if (this.isMasked(col))
				colcomp.setBackground(ColumnComponent.MASKED_COLOUR);

			// All others are normal.
			else
				colcomp.setBackground(ColumnComponent.NORMAL_COLOUR);
		}

		// Relations get pretty colours if they are incorrect or handmade.
		else if (object instanceof Relation) {

			// What relation is this?
			final Relation relation = (Relation) object;
			final RelationComponent relcomp = (RelationComponent) component;

			// Is it restricted?
			relcomp.setRestricted(false);

			// Is it compounded?
			relcomp.setCompounded(false);

			// Is it loopback?
			relcomp.setLoopback(false);

			// Fade out all INFERRED_INCORRECT relations and those which
			// head to ignored tables.
			if (this.isMasked(relation))
				relcomp.setForeground(RelationComponent.INCORRECT_COLOUR);
			
			// Highlight all HANDMADE relations.
			else if (relation.getStatus().equals(ComponentStatus.HANDMADE))
				relcomp.setForeground(RelationComponent.HANDMADE_COLOUR);

			// Highlight MODIFIED relations.
			else if (relation.getStatus().equals(ComponentStatus.MODIFIED))
				relcomp.setForeground(RelationComponent.MODIFIED_COLOUR);

			// All others are normal.
			else
				relcomp.setForeground(RelationComponent.NORMAL_COLOUR);
		}

		// Keys also get pretty colours for being incorrect or handmade.
		else if (object instanceof Key) {

			// What key is this?
			final Key key = (Key) object;
			final KeyComponent keycomp = (KeyComponent) component;

			// Fade out all INFERRED_INCORRECT relations.
			if (key.getStatus().equals(ComponentStatus.INFERRED_INCORRECT))
				keycomp.setForeground(KeyComponent.INCORRECT_COLOUR);

			// Highlight all HANDMADE relations.
			else if (key.getStatus().equals(ComponentStatus.HANDMADE))
				keycomp.setForeground(KeyComponent.HANDMADE_COLOUR);

			// All others are normal.
			else
				keycomp.setForeground(KeyComponent.NORMAL_COLOUR);

			// Add drag-and-drop to all keys here.
			keycomp.setDraggable(true);
		}
	}

	public boolean isMasked(final Object object) {

		if (object instanceof Schema) {
			final Schema schema = (Schema) object;

			if (schema.isMasked())
				return true;

			// Fade out if has external tables and all are inapplicable.
			final Set<Table> extTbls = new HashSet<Table>();
			for (final Iterator<Relation> i = schema.getRelations().iterator(); i
					.hasNext();) {
				final Relation r = i.next();
				if (r.isExternal())
					extTbls.add(r.getKeyForSchema(schema).getTable());
			}
			for (final Iterator<Table> i = extTbls.iterator(); i.hasNext();)
					return false;
			//TODO check
			return !extTbls.isEmpty();
		}

		// Incorrect and ignored stuff is 'masked'.
		else if (object instanceof Table) {
			final Table table = (Table) object;

			// Fade out all ignored and/or unreachable tables.
			if (table.isMasked())
				return true;
			else {
				for (final Iterator<Relation> i = table.getRelations().iterator(); i
						.hasNext();)
					if (!((Relation) i.next()).getStatus().equals(
							ComponentStatus.INFERRED_INCORRECT))
						return false;
				// If get here, it's unreachable.
				return true;
			}
		}

		// Relations get pretty colours if they are incorrect or handmade.
		else if (object instanceof Relation) {

			// What relation is this?
			final Relation relation = (Relation) object;

			// Fade out all INFERRED_INCORRECT relations and those which
			// head to ignored tables or masked schemas.
			if (relation.getStatus().equals(ComponentStatus.INFERRED_INCORRECT)
					|| this.isMasked(relation.getFirstKey().getTable())
					|| this.isMasked(relation.getSecondKey().getTable())
					|| this.isMasked(relation.getFirstKey().getTable()
							.getSchema())
					|| this.isMasked(relation.getSecondKey().getTable()
							.getSchema()))
				return true;

		}

		// Keys also get pretty colours for being incorrect or handmade.
		else if (object instanceof Key) {

			// What key is this?
			final Key key = (Key) object;

			// Fade out all INFERRED_INCORRECT relations.
			if (key.getStatus().equals(ComponentStatus.INFERRED_INCORRECT))
				return true;
		}

		// Columns get masked.
		else if (object instanceof Column) {
			return false;
		}

		return false;
	}

	public void populateMultiContextMenu(final JPopupMenu contextMenu,
			final Collection selectedItems, final Class clazz) {
		// Nothing to do here.
	}

	public void populateContextMenu(final JPopupMenu contextMenu,
			final Object object) {
		
		if (object instanceof Schema) {
			if (contextMenu.getComponentCount() > 0)
				contextMenu.addSeparator();

			final Schema sch = (Schema) object;

			// Accept/Reject changes - only enabled if dataset table
			// is visible modified.
			final JMenuItem accept = new JMenuItem(Resources
					.get("acceptChangesTitle"));
			accept
					.setMnemonic(Resources.get("acceptChangesMnemonic").charAt(
							0));
			accept.addActionListener(new ActionListener() {
				public void actionPerformed(final ActionEvent evt) {
					SchemaContext.this.mart.getSchemasObj()
							.requestAcceptAll(sch);
				}
			});
			accept.setEnabled(sch.isVisibleModified());
			contextMenu.add(accept);

			final JMenuItem reject = new JMenuItem(Resources
					.get("rejectChangesTitle"));
			reject
					.setMnemonic(Resources.get("rejectChangesMnemonic").charAt(
							0));
			reject.addActionListener(new ActionListener() {
				public void actionPerformed(final ActionEvent evt) {
					SchemaContext.this.mart.getSchemasObj()
							.requestRejectAll(sch);
				}
			});
			reject.setEnabled(sch.isVisibleModified());
			contextMenu.add(reject);
		}

		// Table objects have their own menus too.
		else if (object instanceof Table) {

			// Add a separator if the menu is not empty.
			if (contextMenu.getComponentCount() > 0)
				contextMenu.addSeparator();

			// Work out what table we are using.
			final Table table = (Table) object;

			// Menu option to suggest a bunch of datasets based around that
			// table.
			final JMenuItem suggest = new JMenuItem(Resources.get(
					"suggestDataSetsTableTitle", table.getName()));
			suggest.setMnemonic(Resources.get("suggestDataSetsTableMnemonic")
					.charAt(0));
			suggest.addActionListener(new ActionListener() {
				public void actionPerformed(final ActionEvent evt) {
					SchemaContext.this.mart.getDataSetObj()
							.requestSuggestDataSets(table);
				}
			});
			contextMenu.add(suggest);
			suggest.setEnabled(!table.getSchema().isMasked());


			// Separator.
			contextMenu.addSeparator();

			// Menu option to show first few rows.
			final JMenuItem showRows = new JMenuItem(Resources.get(
					"showRowsTitle", table.getName()));
			showRows.setMnemonic(Resources.get("showRowsMnemonic").charAt(0));
			showRows.addActionListener(new ActionListener() {
				public void actionPerformed(final ActionEvent evt) {
					SchemaContext.this.mart.getSchemasObj()
							.requestShowRows(table, 10);
				}
			});
			contextMenu.add(showRows);

			// Separator.
			contextMenu.addSeparator();

			// Menu item to create a primary key. If it already has one, disable
			// the option.
			final JMenuItem pk = new JMenuItem(Resources
					.get("createPrimaryKeyTitle"));
			pk.setMnemonic(Resources.get("createPrimaryKeyMnemonic").charAt(0));
			pk.addActionListener(new ActionListener() {
				public void actionPerformed(final ActionEvent evt) {
					SchemaContext.this.mart.getSchemasObj()
							.requestCreatePrimaryKey(table);
				}
			});
			if (table.getPrimaryKey() != null)
				pk.setEnabled(false);
			contextMenu.add(pk);

			// Menu item to create a foreign key.
			final JMenuItem fk = new JMenuItem(Resources
					.get("createForeignKeyTitle"));
			fk.setMnemonic(Resources.get("createForeignKeyMnemonic").charAt(0));
			fk.addActionListener(new ActionListener() {
				public void actionPerformed(final ActionEvent evt) {
					SchemaContext.this.mart.getSchemasObj()
							.requestCreateForeignKey(table);
				}
			});
			contextMenu.add(fk);

			// Separator.
			contextMenu.addSeparator();

		}

		// Relations have their own menus too.
		else if (object instanceof Relation) {

			// Add a separator if the menu is not empty.
			if (contextMenu.getComponentCount() > 0)
				contextMenu.addSeparator();

			// What relation is this? And is it correct?
			final Relation relation = (Relation) object;
			final boolean relationIncorrect = relation.getStatus().equals(
					ComponentStatus.INFERRED_INCORRECT);

			// Set up a radio group for the cardinality.
			final ButtonGroup cardGroup = new ButtonGroup();

			// Set the relation to be 1:1, but only if it is correct.
			final JRadioButtonMenuItem oneToOne = new JRadioButtonMenuItem(
					Resources.get("oneToOneTitle"));
			oneToOne.setMnemonic(Resources.get("oneToOneMnemonic").charAt(0));
			oneToOne.addActionListener(new ActionListener() {
				public void actionPerformed(final ActionEvent evt) {
					SchemaContext.this.mart.getSchemasObj()
							.requestChangeRelationCardinality(relation,
									Cardinality.ONE);
				}
			});
			cardGroup.add(oneToOne);
			contextMenu.add(oneToOne);
			if (relationIncorrect)
				oneToOne.setEnabled(false);
			if (relation.isOneToOne())
				oneToOne.setSelected(true);
			
			// Set the relation to be 1:M, but only if it is correct.
			final JRadioButtonMenuItem oneToManyA = new JRadioButtonMenuItem(
					Resources.get("oneToManyATitle", relation.getFirstKey().toString()));
			oneToManyA.setMnemonic(Resources.get("oneToManyAMnemonic").charAt(0));
			oneToManyA.addActionListener(new ActionListener() {
				public void actionPerformed(final ActionEvent evt) {
					SchemaContext.this.mart.getSchemasObj()
							.requestChangeRelationCardinality(relation,
									Cardinality.MANY_A);
				}
			});
			cardGroup.add(oneToManyA);
			contextMenu.add(oneToManyA);
			if (relationIncorrect || !relation.isOneToManyAAllowed())
				oneToManyA.setEnabled(false);
			if (relation.isOneToManyA())
				oneToManyA.setSelected(true);

			// Set the relation to be 1:M, but only if it is correct.
			final JRadioButtonMenuItem oneToManyB = new JRadioButtonMenuItem(
					Resources.get("oneToManyBTitle", relation.getFirstKey().toString()));
			oneToManyB.setMnemonic(Resources.get("oneToManyBMnemonic").charAt(0));
			oneToManyB.addActionListener(new ActionListener() {
				public void actionPerformed(final ActionEvent evt) {
					SchemaContext.this.mart.getSchemasObj()
							.requestChangeRelationCardinality(relation,
									Cardinality.MANY_B);
				}
			});
			cardGroup.add(oneToManyB);
			contextMenu.add(oneToManyB);
			if (relationIncorrect || !relation.isOneToManyBAllowed())
				oneToManyB.setEnabled(false);
			if (relation.isOneToManyB())
				oneToManyB.setSelected(true);

			// Separator.
			contextMenu.addSeparator();

			// Masked? (Incorrect?)
			// Mark relation as incorrect, but only if not handmade.
			final JCheckBoxMenuItem incorrect = new JCheckBoxMenuItem(Resources
					.get("incorrectRelationTitle"));
			incorrect.setMnemonic(Resources.get("incorrectRelationMnemonic")
					.charAt(0));
			incorrect.addActionListener(new ActionListener() {
				public void actionPerformed(final ActionEvent evt) {
					SchemaContext.this.mart.getSchemasObj()
							.requestChangeRelationStatus(
									relation,
									incorrect.isSelected() ? ComponentStatus.INFERRED_INCORRECT
											: ComponentStatus.INFERRED);
				}
			});
			contextMenu.add(incorrect);
			incorrect.setSelected(relationIncorrect);
			if (relation.getStatus().equals(ComponentStatus.MODIFIED) 
					|| relation.getStatus().equals(ComponentStatus.HANDMADE))
				incorrect.setEnabled(false);

			// Separator
			contextMenu.addSeparator();

			// Remove the relation from the schema, but only if handmade.
			final JMenuItem remove = new JMenuItem(Resources
					.get("removeRelationTitle"), McUtils.createImageIcon(Resources
					.get("CUTIMAGE")));
			remove.setMnemonic(Resources.get("removeRelationMnemonic")
					.charAt(0));
			remove.addActionListener(new ActionListener() {
				public void actionPerformed(final ActionEvent evt) {
					SchemaContext.this.mart.getSchemasObj()
							.requestRemoveRelation(relation);
				}
			});
			contextMenu.add(remove);
			if (!relation.getStatus().equals(ComponentStatus.HANDMADE))
				remove.setEnabled(false);
		}

		// Keys have menus too.
		else if (object instanceof Key) {
			// Then work out what key this is.
			final Key key = (Key) object;

			// Add a separator if the menu is not empty.
			if (contextMenu.getComponentCount() > 0)
				contextMenu.addSeparator();

			// Option to edit an existing key.
			final JMenuItem editkey = new JMenuItem(Resources
					.get("editKeyTitle"));
			editkey.setMnemonic(Resources.get("editKeyMnemonic").charAt(0));
			editkey.addActionListener(new ActionListener() {
				public void actionPerformed(final ActionEvent evt) {
					SchemaContext.this.mart.getSchemasObj()
							.requestEditKey(key);
				}
			});
			contextMenu.add(editkey);

			// Remove the key from the table, but only if handmade.
			final JMenuItem remove = new JMenuItem(Resources
					.get("removeKeyTitle"), McUtils.createImageIcon(Resources
					.get("CUTIMAGE")));
			remove.setMnemonic(Resources.get("removeKeyMnemonic").charAt(0));
			remove.addActionListener(new ActionListener() {
				public void actionPerformed(final ActionEvent evt) {
					SchemaContext.this.mart.getSchemasObj()
							.requestRemoveKey(key);
				}
			});
			contextMenu.add(remove);

			// Separator.
			contextMenu.addSeparator();

			// Incorrect = masked.
			// Mark the key as incorrect, but not if handmade.
			final JCheckBoxMenuItem incorrect = new JCheckBoxMenuItem(Resources
					.get("incorrectKeyTitle"));
			incorrect.setMnemonic(Resources.get("incorrectKeyMnemonic").charAt(
					0));
			incorrect.addActionListener(new ActionListener() {
				public void actionPerformed(final ActionEvent evt) {
					SchemaContext.this.mart.getSchemasObj()
							.requestChangeKeyStatus(
									key,
									incorrect.isSelected() ? ComponentStatus.INFERRED_INCORRECT
											: ComponentStatus.INFERRED);
				}
			});
			contextMenu.add(incorrect);
			incorrect.setSelected(key.getStatus().equals(
					ComponentStatus.INFERRED_INCORRECT));
			if (key.getStatus().equals(ComponentStatus.HANDMADE))
				incorrect.setEnabled(false);

			// Separator
			contextMenu.addSeparator();

			// Option to establish a relation between this key and another.
			final JMenuItem createrel = new JMenuItem(Resources
					.get("createRelationTitle"));
			createrel.setMnemonic(Resources.get("createRelationMnemonic")
					.charAt(0));
			createrel.addActionListener(new ActionListener() {
				public void actionPerformed(final ActionEvent evt) {
					SchemaContext.this.mart.getSchemasObj()
							.requestCreateRelation(key);
				}
			});
			contextMenu.add(createrel);
			if (key.getStatus().equals(ComponentStatus.INFERRED_INCORRECT))
				createrel.setEnabled(false);
		}
	}
}
