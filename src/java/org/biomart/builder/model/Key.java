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
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import org.biomart.common.resources.Log;
import org.biomart.common.resources.Resources;
import org.biomart.common.utils.McBeanCollection;
import org.biomart.common.utils.McBeanMap;
import org.biomart.common.utils.Transaction;
import org.biomart.common.utils.WeakPropertyChangeSupport;
import org.biomart.common.utils.Transaction.TransactionEvent;
import org.biomart.common.utils.Transaction.TransactionListener;

/**
 * The key class is core to the way tables get associated. They are involved in
 * relations which link tables together in various ways, and provide information
 * about which columns at each end correspond.
 * <p>
 * The {@link Key} implementation provides the basis for the other types of
 * keys, eg. keeping track of relations etc.
 * <p>
 * Unless otherwise specified, all keys are created with a default status of
 * {@link ComponentStatus#INFERRED}.
 * 
 * @author Richard Holland <holland@ebi.ac.uk>
 * @version $Revision: 1.43 $, $Date: 2007/11/02 16:22:37 $, modified by
 *          $Author: rh4 $
 * @since 0.5
 */
public abstract class Key implements Comparable<Key>, TransactionListener {

	/**
	 * Subclasses use this field to fire events of their own.
	 */
	protected final WeakPropertyChangeSupport pcs = new WeakPropertyChangeSupport(
			this);

	private static final long serialVersionUID = 1L;

	private Column[] columns;

	private final McBeanCollection<Relation> relations;

	private ComponentStatus status;

	private boolean directModified = false;

	private final PropertyChangeListener listener = new PropertyChangeListener() {
		public void propertyChange(final PropertyChangeEvent evt) {
			Key.this.setDirectModified(true);
		}
	};

	/*
	 * triggered by a table drop McBeanMap.property_RemoveItem
	 */
	private final PropertyChangeListener dropListener = new PropertyChangeListener() {
		public void propertyChange(final PropertyChangeEvent evt) {
			if (!Key.this.getTable().getSchema().getTables().containsValue(
					Key.this.getTable())) {
				final List<Relation> relations = new ArrayList<Relation>(Key.this.getRelations());
				for (final Iterator<Relation> i = relations.iterator(); i.hasNext();) {
					final Relation rel = (Relation) i.next();
					rel.getFirstKey().getRelations().remove(rel);
					rel.getSecondKey().getRelations().remove(rel);
				}
			}
		}
	};
	/*
	 * triggered when a relation add/remove
	 */
	private final PropertyChangeListener relationCacheBuilder = new PropertyChangeListener() {
		public void propertyChange(final PropertyChangeEvent evt) {
			Key.this.setDirectModified(true);
			String pName = evt.getPropertyName();
			if(pName.equals(McBeanCollection.property_AddItem)) {
				Relation rel = (Relation)evt.getNewValue();
				rel.addPropertyChangeListener(Resources.get("PCDIRECTMODIFIED"),
						Key.this.listener);
			}else if(pName.equals(McBeanCollection.property_RemoveItem)) {
//TODO remove later
			}else {
				System.err.println("property message not handled "+pName);
			}			
		}
	};

	/**
	 * The constructor constructs a key over a set of columns. It doesn't check
	 * to make sure they all come from the same table, nor does it check to see
	 * if they are in a sensible order. The order they are specified in here is
	 * the order in which the key will refer to them in future. The key will
	 * have a status of {@link ComponentStatus#INFERRED}.
	 * 
	 * @param columns
	 *            the set of columns to form the key over.
	 */
	public Key(final Column[] columns) {
		Log.debug("Creating key over " + columns);
		this.status = ComponentStatus.INFERRED;
		this.relations = new McBeanCollection<Relation>(new HashSet<Relation>());
		this.setColumns(columns);

		Transaction.addTransactionListener(this);

		// All changes to us make us modified.
		this.addPropertyChangeListener(this.listener);
		// Check to see if our table goes AWOL.
		this.getTable().getSchema().getTables().addPropertyChangeListener(McBeanMap.property_RemoveItem,this.dropListener);

		//only add/remove
		this.relations.addPropertyChangeListener(McBeanCollection.property_AddItem,this.relationCacheBuilder);
		this.relations.addPropertyChangeListener(McBeanCollection.property_RemoveItem,this.relationCacheBuilder);
	}


	public void setDirectModified(final boolean modified) {
		if (modified == this.directModified)
			return;
		final boolean oldValue = this.directModified;
		this.directModified = modified;
		this.pcs.firePropertyChange(Resources.get("PCDIRECTMODIFIED"), oldValue, modified);
	}

	public boolean isVisibleModified() {
		return false;
	}

	public void setVisibleModified(final boolean modified) {
		// Ignore.
	}

	public void transactionResetVisibleModified() {
		this.setVisibleModified(false);
	}

	public void transactionResetDirectModified() {
		this.directModified = false;
	}

	public void transactionStarted(final TransactionEvent evt) {
		// Don't really care for now.
	}

	public void transactionEnded(final TransactionEvent evt) {
		// Don't really care for now.
	}

	/**
	 * Adds a property change listener.
	 * 
	 * @param listener
	 *            the listener to add.
	 */
	public void addPropertyChangeListener(final PropertyChangeListener listener) {
		this.pcs.addPropertyChangeListener(listener);
	}

	/**
	 * Adds a property change listener.
	 * 
	 * @param property
	 *            the property to listen to.
	 * @param listener
	 *            the listener to add.
	 */
	public void addPropertyChangeListener(final String property,
			final PropertyChangeListener listener) {
		this.pcs.addPropertyChangeListener(property, listener);
	}

	/**
	 * The constructor constructs a key over a single column. Otherwise, it is
	 * identical to the multi-column constructor.
	 * 
	 * @param column
	 *            the column to form the key over.
	 */
	public Key(final Column column) {
		this(new Column[] { column });
	}

	/**
	 * Returns the list of columns this key is formed over. It may return an
	 * empty set.
	 * 
	 * @return the list of columns this key involves.
	 */
	public Column[] getColumns() {
		return this.columns;
	}

	/**
	 * Returns all relations this key is involved in. The set may be empty but
	 * it will never be <tt>null</tt>.
	 * 
	 * @return the set of all relations this key is involved in.
	 */
	public McBeanCollection<Relation> getRelations() {
		return this.relations;
	}

	/**
	 * Returns the status of this key. The default value, unless otherwise
	 * specified, is {@link ComponentStatus#INFERRED}.
	 * 
	 * @return the status of this key.
	 */
	public ComponentStatus getStatus() {
		return this.status;
	}

	/**
	 * Returns the table this key is formed over.
	 * 
	 * @return the table this key involves.
	 */
	public Table getTable() {
		return this.columns[0].getTable();
	}

	/**
	 * Replaces the set of columns this key is formed over with a new set.
	 * 
	 * @param columns
	 *            the replacement columns, in order.
	 */
	public void setColumns(final Column[] columns) {
		Log.debug("Creating key over " + columns);
		final Column[] oldValue = this.columns;
		if (this.columns == columns || this.columns != null
				&& this.columns.equals(columns))
			return;
		this.columns = columns;
		this.pcs.firePropertyChange("columns", oldValue, columns);
	}

	/**
	 * Sets the status of this key.
	 * 
	 * @param status
	 *            the new status of this key.
	 */
	public void setStatus(final ComponentStatus status) {
		Log.debug("Changing status for " + this + " to " + status);
		final ComponentStatus oldValue = this.status;
		if (this.status == status || this.status != null
				&& this.status.equals(status))
			return;
		this.status = status;
		this.pcs.firePropertyChange("status", oldValue, status);
	}

	private String getName() {
		final StringBuffer sb = new StringBuffer();
		sb.append(this.getTable() == null ? "<undef>" : this.getTable()
				.toString());
		sb.append(" [");
		for (int i = 0; i < this.columns.length; i++) {
			if (i > 0)
				sb.append(',');
			sb
					.append(this.columns[i] instanceof DataSetColumn ? ((DataSetColumn) this.columns[i])
							.getModifiedName()
							: this.columns[i].getName());
		}
		sb.append(']');
		return sb.toString();
	}

	public int compareTo(final Key o) throws ClassCastException {
		return (this.getTable().getSchema().getMart().getUniqueId() + "_" + this
				.toString()).compareTo(o.getTable().getSchema().getMart()
				.getUniqueId()
				+ "_" + o.toString());
	}

	public boolean equals(final Object o) {
		if (o == this)
			return true;
		else if (o == null)
			return false;
		else if (o instanceof Key) {
			final Key k = (Key) o;
			return k.getClass().equals(this.getClass())
					&& (k.getTable().getSchema().getMart().getUniqueId() + "_" + k
							.toString()).equals(this.getTable().getSchema()
							.getMart().getUniqueId()
							+ "_" + this.toString());
		} else
			return false;
	}

	public int hashCode() {
		// The hash code is only against the table itself, in
		// case our column names change. This is to ensure
		// that we stay in the same hash buckets.
		return this.getTable().hashCode();
	}

	public String toString() {
		return this.getName();
	}


}
