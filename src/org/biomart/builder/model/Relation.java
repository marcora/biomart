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
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.biomart.builder.exceptions.ValidationException;
import org.biomart.builder.model.ForeignKey;
import org.biomart.builder.model.PrimaryKey;
import org.biomart.common.exceptions.AssociationException;
import org.biomart.common.resources.Log;
import org.biomart.common.resources.Resources;
import org.biomart.common.utils.Transaction;
import org.biomart.common.utils.WeakPropertyChangeSupport;
import org.biomart.common.utils.Transaction.TransactionEvent;
import org.biomart.common.utils.Transaction.TransactionListener;
import org.biomart.configurator.utils.type.Cardinality;

/**
 * A relation represents the association between two keys. Relations between two
 * primary keys are always 1:1. Relations between two foreign keys are either
 * 1:1 or M:M. Relations between a foreign key and a primary key can either be
 * 1:1 or 1:M.
 * <p>
 * Both keys must have the same number of columns, and the related columns
 * should appear in the same order in both keys. If they do not, then results
 * may be unpredictable.
 * <p>
 * A {@link Relation} class forms the basic functionality outlined above.
 * 
 * @author Richard Holland <holland@ebi.ac.uk>
 * @version $Revision: 1.58 $, $Date: 2008/03/06 11:32:30 $, modified by
 *          $Author: rh4 $
 * @since 0.5
 */
public class Relation implements Comparable<Relation>, TransactionListener {

	private static final long serialVersionUID = 1L;

	private Cardinality cardinality;

	private Cardinality originalCardinality;

	private final Key firstKey;

	private final Key secondKey;

	private Key oneKey;

	private Key manyKey;

	private boolean oneToManyAAllowed;

	private boolean oneToManyBAllowed;

	private boolean oneToOne;

	private boolean oneToManyA;

	private boolean oneToManyB;

	private boolean external;

	private ComponentStatus status;

	private boolean visibleModified = Transaction.getCurrentTransaction() == null ? false
			: Transaction.getCurrentTransaction().isAllowVisModChange();

	private boolean directModified = false;

	private final Map<DataSet, Map<String, Map<String,Object>>> mods = new HashMap<DataSet, Map<String,Map<String,Object>>>();

	private static final String DATASET_WIDE = "__DATASET_WIDE__";

	/**
	 * Subclasses use this field to fire events of their own.
	 */
	protected final WeakPropertyChangeSupport pcs = new WeakPropertyChangeSupport(
			this);

	// All changes to us make us modified.
	private final PropertyChangeListener listener = new PropertyChangeListener() {
		public void propertyChange(final PropertyChangeEvent evt) {
			Relation.this.setDirectModified(true);
		}
	};

	// Add listeners to keys such that if the number of columns
	// no longer match, the relation will be removed.
	private final PropertyChangeListener keyColListener = new PropertyChangeListener() {
		public void propertyChange(final PropertyChangeEvent evt) {
			if (Relation.this.firstKey.getColumns().length != Relation.this.secondKey
					.getColumns().length) {
				Relation.this.firstKey.getRelations().remove(Relation.this);
				Relation.this.secondKey.getRelations().remove(Relation.this);
			}
		}
	};

	/**
	 * This constructor tests that both ends of the relation have keys with the
	 * same number of columns. The default constructor sets the status to
	 * {@link ComponentStatus#INFERRED}.
	 * 
	 * @param firstKey
	 *            the first key.
	 * @param secondKey
	 *            the second key.
	 * @param cardinality
	 *            the cardinality of the foreign key end of this relation. If
	 *            both keys are primary keys, then this is ignored and defaults
	 *            to 1 (meaning 1:1). If they are a mixture, then this
	 *            differentiates between 1:1 and 1:M. If they are both foreign
	 *            keys, then this differentiates between 1:1 and M:M. See
	 *            {@link #setCardinality(Cardinality)}.
	 * @throws AssociationException
	 *             if the number of columns in the keys don't match, or if the
	 *             relation already exists.
	 */
	public Relation(Key firstKey, Key secondKey, final Cardinality cardinality)
			throws AssociationException {
		Log.debug("Creating relation between " + firstKey + " and " + secondKey
				+ " with cardinality " + cardinality);

		// Remember the keys etc.
		this.firstKey = firstKey;
		this.secondKey = secondKey;
		this.setOriginalCardinality(cardinality);
		this.setCardinality(cardinality);
		this.setStatus(ComponentStatus.INFERRED);

		// Check the keys have the same number of columns.
		if (firstKey.getColumns().length != secondKey.getColumns().length)
			throw new AssociationException(Resources
					.get("keyColumnCountMismatch"));
		// Check the relation doesn't already exist.
		if (firstKey.getRelations().contains(this))
			throw new AssociationException(Resources
					.get("relationAlreadyExists"));
		// Cannot place a relation on an FK to this table if it
		// already has relations.
		if (firstKey.getTable().equals(secondKey.getTable())
				&& (firstKey instanceof ForeignKey
						&& firstKey.getRelations().size() > 0 || secondKey instanceof ForeignKey
						&& secondKey.getRelations().size() > 0))
			throw new AssociationException(Resources
					.get("fkToThisOnceOrOthers"));
		// Cannot place a relation on an FK to another table if
		// it already has a relation to this table (it will have
		// only one due to previous check).
		if (!firstKey.getTable().equals(secondKey.getTable())
				&& ((firstKey instanceof ForeignKey
						&& firstKey.getRelations().size() == 1 && ((Relation) firstKey
						.getRelations().iterator().next())
						.getOtherKey(firstKey).getTable().equals(
								firstKey.getTable())) || (secondKey instanceof ForeignKey
						&& secondKey.getRelations().size() == 1 && ((Relation) secondKey
						.getRelations().iterator().next()).getOtherKey(
						secondKey).getTable().equals(secondKey.getTable()))))
			throw new AssociationException(Resources
					.get("fkToThisOnceOrOthers"));

		// Update flags.
		this.oneToManyAAllowed = this.secondKey instanceof ForeignKey;
		this.oneToManyBAllowed = this.firstKey instanceof ForeignKey;
		this.external = !this.firstKey.getTable().getSchema().equals(
				this.secondKey.getTable().getSchema());

		Transaction.addTransactionListener(this);

		this.firstKey.addPropertyChangeListener("columns", this.keyColListener);
		this.secondKey
				.addPropertyChangeListener("columns", this.keyColListener);

		this.addPropertyChangeListener("cardinality", this.listener);
		this.addPropertyChangeListener("originalCardinality", this.listener);
		this.addPropertyChangeListener("status", this.listener);
		this.addPropertyChangeListener("maskRelation", this.listener);
		this.addPropertyChangeListener("mergeRelation", this.listener);
		this.addPropertyChangeListener("subclassRelation", this.listener);
		this.addPropertyChangeListener("alternativeJoin", this.listener);
	}


	public void setDirectModified(final boolean modified) {
		if (modified == this.directModified)
			return;
		final boolean oldValue = this.directModified;
		this.directModified = modified;
		this.pcs.firePropertyChange(Resources.get("PCDIRECTMODIFIED"), oldValue, modified);
	}

	public boolean isVisibleModified() {
		return this.visibleModified;
	}

	public void setVisibleModified(final boolean modified) {
		if (modified == this.visibleModified)
			return;
		final boolean oldValue = this.visibleModified;
		this.visibleModified = modified;
		this.pcs.firePropertyChange("visibleModified", oldValue, modified);
		this.setDirectModified(true);
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
		// Don't care for now.
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
	 * Returns the cardinality of the foreign key end of this relation, in a 1:M
	 * relation. In 1:1 relations this will always return 1, and in M:M
	 * relations it will always return M.
	 * 
	 * @return the cardinality of the foreign key end of this relation, in 1:M
	 *         relations only. Otherwise determined by the relation type.
	 */
	public Cardinality getCardinality() {
		return this.cardinality;
	}

	/**
	 * Returns the original cardinality of the foreign key end of this relation,
	 * in a 1:M relation. In 1:1 relations this will always return 1, and in M:M
	 * relations it will always return M.
	 * 
	 * @return the original cardinality of the foreign key end of this relation,
	 *         in 1:M relations only. Otherwise determined by the relation type.
	 */
	public Cardinality getOriginalCardinality() {
		return this.originalCardinality;
	}

	/**
	 * Returns the first key of this relation. The concept of which key is first
	 * and which is second depends merely on the order they were passed to the
	 * constructor.
	 * 
	 * @return the first key.
	 */
	public Key getFirstKey() {
		return this.firstKey;
	}

	/**
	 * In a 1:M relation, this will return the M end of the relation. In all
	 * other relation types, this will return <tt>null</tt>.
	 * 
	 * @return the key at the many end of the relation, or <tt>null</tt> if
	 *         this is not a 1:M relation.
	 */
	public Key getManyKey() {
		return this.manyKey;
	}

	/**
	 * In a 1:M relation, this will return the 1 end of the relation. In all
	 * other relation types, this will return <tt>null</tt>.
	 * 
	 * @return the key at the one end of the relation, or <tt>null</tt> if
	 *         this is not a 1:M relation.
	 */
	public Key getOneKey() {
		return this.oneKey;
	}

	/**
	 * Given a key that is in this relationship, return the other key.
	 * 
	 * @param key
	 *            the key we know is in this relationship.
	 * @return the other key in this relationship, or <tt>null</tt> if the key
	 *         specified is not in this relationship.
	 */
	public Key getOtherKey(final Key key) {
		return this.firstKey.equals(key) ? this.secondKey : this.firstKey;
	}

	/**
	 * Returns the second key of this relation. The concept of which key is
	 * first and which is second depends merely on the order they were passed to
	 * the constructor.
	 * 
	 * @return the second key.
	 */
	public Key getSecondKey() {
		return this.secondKey;
	}

	/**
	 * Returns the status of this relation. The default value, unless otherwise
	 * specified, is {@link ComponentStatus#INFERRED}.
	 * 
	 * @return the status of this relation.
	 */
	public ComponentStatus getStatus() {
		return this.status;
	}

	/**
	 * Returns <tt>true</tt> if this relation involves keys in two separate
	 * schemas. Those that do are external, those that don't are not.
	 * 
	 * @return <tt>true</tt> if this is external, <tt>false</tt> otherwise.
	 */
	public boolean isExternal() {
		return this.external;
	}

	/**
	 * Returns <tt>true</tt> if this is a 1:M(a) relation.
	 * 
	 * @return <tt>true</tt> if this is a 1:M(a) relation, <tt>false</tt>
	 *         otherwise.
	 */
	public boolean isOneToManyA() {
		return this.oneToManyA;
	}

	/**
	 * Returns <tt>true</tt> if this is a 1:M(b) relation.
	 * 
	 * @return <tt>true</tt> if this is a 1:M(b) relation, <tt>false</tt>
	 *         otherwise.
	 */
	public boolean isOneToManyB() {
		return this.oneToManyB;
	}

	/**
	 * Returns <tt>true</tt> if this is either kind of 1:M relation.
	 * 
	 * @return <tt>true</tt> if this is either kind of 1:M relation,
	 *         <tt>false</tt> otherwise.
	 */
	public boolean isOneToMany() {
		return this.oneToManyA || this.oneToManyB;
	}

	/**
	 * Can this relation be 1:M(a)? Returns <tt>true</tt> in all cases where
	 * the two keys are of different types.
	 * 
	 * @return <tt>true</tt> if this can be 1:M(a), <tt>false</tt> if not.
	 */
	public boolean isOneToManyAAllowed() {
		return this.oneToManyAAllowed;
	}

	/**
	 * Can this relation be 1:M(b)? Returns <tt>true</tt> in all cases where
	 * the two keys are of different types.
	 * 
	 * @return <tt>true</tt> if this can be 1:M(b), <tt>false</tt> if not.
	 */
	public boolean isOneToManyBAllowed() {
		return this.oneToManyBAllowed;
	}

	/**
	 * Returns <tt>true</tt> if this is a 1:1 relation.
	 * 
	 * @return <tt>true</tt> if this is a 1:1 relation, <tt>false</tt>
	 *         otherwise.
	 */
	public boolean isOneToOne() {
		return this.oneToOne;
	}

	/**
	 * Returns the key in this relation associated with the given table. If both
	 * keys are on that table, returns the one that is a PK, or the first one if
	 * both are FKs.
	 * 
	 * @param table
	 *            the table to get the key for.
	 * @return the key for that table. <tt>null</tt> if neither key is from
	 *         that table.
	 */
	public Key getKeyForTable(final Table table) {
		return this.firstKey.getTable().equals(table) ? this.firstKey
				: this.secondKey;
	}

	/**
	 * Returns the key in this relation associated with the given schema. If
	 * both keys are on tables in that schema, returns the first one.
	 * 
	 * @param schema
	 *            the schema to get the key for.
	 * @return the key for that schema. <tt>null</tt> if neither key is from
	 *         that schema.
	 */
	public Key getKeyForSchema(final Schema schema) {
		return this.firstKey.getTable().getSchema().equals(schema) ? this.firstKey
				: this.secondKey;
	}

	/**
	 * Sets the cardinality of the foreign key end of this relation, in a 1:M
	 * relation. If used on a 1:1 or M:M relation, then specifying M makes it
	 * M:M and specifying 1 makes it 1:1.
	 * 
	 * @param cardinality
	 *            the cardinality.
	 */
	public void setCardinality(Cardinality cardinality) {
		Log.debug("Changing cardinality of " + this + " to " + cardinality);
		if (this.firstKey instanceof PrimaryKey
				&& this.secondKey instanceof PrimaryKey) {
			Log.debug("Overriding cardinality change to ONE");
			cardinality = Cardinality.ONE;
		}

		// TODO This is a backwards-compatibility clause that needs to
		// stay in throughout the 0.7 release. It can be removed in 0.8.
		if (cardinality == Cardinality.MANY
				&& this.secondKey instanceof PrimaryKey) {
			cardinality = Cardinality.MANY_B;
		} else if (cardinality == Cardinality.MANY
				&& this.firstKey instanceof PrimaryKey) {
			cardinality = Cardinality.MANY_A;
		}
		// End fudge-mode.

		final Cardinality oldValue = this.cardinality;
		if (this.cardinality == cardinality || this.cardinality != null
				&& this.cardinality.equals(cardinality))
			return;
		this.cardinality = cardinality;

		if (this.cardinality.equals(Cardinality.ONE)) {
			this.oneToOne = true;
			this.oneToManyA = false;
			this.oneToManyB = false;
			this.oneKey = null;
			this.manyKey = null;
		} else if (this.cardinality.equals(Cardinality.MANY_A)) {
			this.oneToOne = false;
			this.oneToManyA = true;
			this.oneToManyB = false;
			this.oneKey = this.firstKey;
			this.manyKey = this.secondKey;
		} else if (this.cardinality.equals(Cardinality.MANY_B)) {
			this.oneToOne = false;
			this.oneToManyA = false;
			this.oneToManyB = true;
			this.oneKey = this.secondKey;
			this.manyKey = this.firstKey;
		} else {
			// TODO This is a backwards-compatibility clause that needs to
			// stay in throughout the 0.7 release. It can be removed in 0.8.
			this.oneToOne = false;
			this.oneToManyA = false;
			this.oneToManyB = false;
			this.oneKey = null;
			this.manyKey = null;
			// End fudge-mode.
		}
		if (Transaction.getCurrentTransaction() != null
				&& Transaction.getCurrentTransaction().isAllowVisModChange())
			this.setVisibleModified(true);
		this.pcs.firePropertyChange("cardinality", oldValue, cardinality);
	}

	/**
	 * Sets the original cardinality of the foreign key end of this relation, in
	 * a 1:M relation. If used on a 1:1 or M:M relation, then specifying M makes
	 * it M:M and specifying 1 makes it 1:1.
	 * 
	 * @param originalCardinality
	 *            the originalCardinality.
	 */
	public void setOriginalCardinality(Cardinality originalCardinality) {
		Log.debug("Changing original cardinality of " + this + " to "
				+ originalCardinality);
		final Cardinality oldValue = this.originalCardinality;
		if (this.originalCardinality == originalCardinality
				|| this.originalCardinality != null
				&& this.originalCardinality.equals(originalCardinality))
			return;
		this.originalCardinality = originalCardinality;

		// TODO This is a backwards-compatibility clause that needs to
		// stay in throughout the 0.7 release. It can be removed in 0.8.
		if (oldValue == Cardinality.MANY
				&& !(this.firstKey instanceof PrimaryKey || this.secondKey instanceof PrimaryKey))
			return;
		// End fudge-mode.

		this.pcs.firePropertyChange("originalCardinality", oldValue,
				originalCardinality);
	}

	/**
	 * Sets the status of this relation.
	 * 
	 * @param status
	 *            the new status of this relation.
	 * @throws AssociationException
	 *             if the keys at either end of the relation are incompatible
	 *             upon attempting to mark an
	 *             {@link ComponentStatus#INFERRED_INCORRECT} relation as
	 *             anything else.
	 */
	public void setStatus(final ComponentStatus status)
			throws AssociationException {
		Log.debug("Changing status of " + this + " to " + status);
		// If the new status is not incorrect, we need to make sure we
		// can legally do this, ie. the two keys have the same number of
		// columns each.
		if (!status.equals(ComponentStatus.INFERRED_INCORRECT))
			// Check both keys have same cardinality.
			if (this.firstKey.getColumns().length != this.secondKey
					.getColumns().length)
				throw new AssociationException(Resources
						.get("keyColumnCountMismatch"));
		final ComponentStatus oldValue = this.status;
		if (this.status == status || this.status != null
				&& this.status.equals(status))
			return;
		// Make the change.
		this.status = status;
		this.pcs.firePropertyChange("status", oldValue, status);
	}

	/**
	 * Drop modifications for the given dataset and optional table.
	 * 
	 * @param dataset
	 *            dataset
	 * @param tableKey
	 *            table key - <tt>null</tt> for all tables.
	 */
	public void dropMods(final DataSet dataset, final String tableKey) {
		// Drop all related mods.
		if (tableKey == null)
			this.mods.remove(dataset);
		else if (this.mods.containsKey(dataset))
			(this.mods.get(dataset)).remove(tableKey);
	}

	/**
	 * This contains the set of modifications to this schema that apply to a
	 * particular dataset and table (null table means all tables in dataset).
	 * 
	 * @param dataset
	 *            the dataset to lookup.
	 * @param tableKey
	 *            the table to lookup.
	 * @return the set of tables that the property currently applies to. This
	 *         set can be added to or removed from accordingly. The keys of the
	 *         map are names, the values are optional subsidiary objects.
	 */
	public Map<String,Object> getMods(final DataSet dataset, String tableKey) {
		if (tableKey == null)
			tableKey = Relation.DATASET_WIDE;
		if (!this.mods.containsKey(dataset))
			this.mods.put(dataset, new HashMap<String,Map<String,Object>>());
		final Map<String,Map<String,Object>> dsMap = (Map<String,Map<String,Object>>) this.mods.get(dataset);
		if (!dsMap.containsKey(tableKey))
			dsMap.put(tableKey.intern(), new HashMap<String,Object>());
		return (Map<String,Object>) dsMap.get(tableKey);
	}

	/**
	 * Is this relation subclassed?
	 * 
	 * @param dataset
	 *            the dataset to check for.
	 * @return <tt>true</tt> if it is.
	 */
	public boolean isSubclassRelation(final DataSet dataset) {
		return this.getMods(dataset, null).containsKey("subclassRelation");
	}

	/**
	 * Subclass this relation.
	 * 
	 * @param dataset
	 *            the dataset to set for.
	 * @param subclass
	 *            <tt>true</tt> to subclass it, <tt>false</tt> to not.
	 * @throws ValidationException
	 *             if it cannot make the change.
	 */
	public void setSubclassRelation(final DataSet dataset,
			final boolean subclass) throws ValidationException {
		final boolean oldValue = this.isSubclassRelation(dataset);
		if (subclass == oldValue)
			return;
		if (subclass) {
			// Work out the child end of the relation - the M end. The parent is
			// the 1 end.
			final Table parentTable = this.getOneKey().getTable();
			final Table childTable = this.getManyKey().getTable();
			if (parentTable.equals(childTable))
				throw new ValidationException(Resources
						.get("subclassNotBetweenTwoTables"));
			if (parentTable.getPrimaryKey() == null
					|| childTable.getPrimaryKey() == null)
				throw new ValidationException(Resources
						.get("subclassTargetNoPK"));

			// We need to test if the selected relation links to
			// a table which itself has subclass relations, or
			// is the central table, and has not got an
			// existing subclass relation in the direction we
			// are working in.
			boolean hasConflict = false;
			final Set<Relation> combinedRels = new HashSet<Relation>();
			combinedRels.addAll(parentTable.getRelations());
			combinedRels.addAll(childTable.getRelations());
			for (final Iterator<Relation> i = combinedRels.iterator(); i.hasNext()
					&& !hasConflict;) {
				final Relation rel =  i.next();
				if (!rel.isSubclassRelation(dataset))
					continue;
				else if (rel.getOneKey().getTable().equals(parentTable)
						|| rel.getManyKey().getTable().equals(childTable))
					hasConflict = true;
			}
			// If child has M:1 or parent has 1:M, we cannot do this.
			if (hasConflict)
				throw new ValidationException(Resources
						.get("mixedCardinalitySubclasses"));

			// Now do it.
			this.getMods(dataset, null).put("subclassRelation", null);
			this.pcs.firePropertyChange("subclassRelation", null, dataset);
		} else {
			// Break the chain first.
			final Key key = this.getManyKey();
			if (key != null) {
				final Table target = key.getTable();
				if (!target.equals(dataset.getCentralTable()))
					if (target.getPrimaryKey() != null)
						for (final Iterator<Relation> i = target.getPrimaryKey()
								.getRelations().iterator(); i.hasNext();) {
							final Relation rel = i.next();
							if (rel.isOneToMany())
								rel.setSubclassRelation(dataset, false);
						}
			}

			// Now do it.
			this.getMods(dataset, null).remove("subclassRelation");
			this.pcs.firePropertyChange("subclassRelation", dataset, null);
		}
	}

	/**
	 * Is this relation merged?
	 * 
	 * @param dataset
	 *            the dataset to check for.
	 * @return <tt>true</tt> if it is.
	 */
	public boolean isMergeRelation(final DataSet dataset) {
		return this.getMods(dataset, null).containsKey("mergeRelation");
	}

	/**
	 * Merge this relation.
	 * 
	 * @param dataset
	 *            the dataset to set for.
	 * @param merge
	 *            <tt>true</tt> to merge it, <tt>false</tt> to not.
	 */
	public void setMergeRelation(final DataSet dataset, final boolean merge) {
		final boolean oldValue = this.isMergeRelation(dataset);
		if (merge == oldValue)
			return;
		if (merge) {
			this.getMods(dataset, null).put("mergeRelation", null);
			this.pcs.firePropertyChange("mergeRelation", null, dataset);
		} else {
			this.getMods(dataset, null).remove("mergeRelation");
			this.pcs.firePropertyChange("mergeRelation", dataset, null);
		}
	}

	/**
	 * Is this relation masked?
	 * 
	 * @param dataset
	 *            the dataset to check for.
	 * @return <tt>true</tt> if it is.
	 */
	public boolean isMaskRelation(final DataSet dataset) {
		return this.getMods(dataset, null).containsKey("maskRelation");
	}

	/**
	 * Is this relation masked?
	 * 
	 * @param dataset
	 *            the dataset to check for.
	 * @param tableKey
	 *            the table to check for.
	 * @return <tt>true</tt> if it is.
	 */
	public boolean isMaskRelation(final DataSet dataset, final String tableKey) {
		return this.isMaskRelation(dataset)
				|| this.getMods(dataset, tableKey).containsKey("maskRelation");
	}

	/**
	 * Mask this relation.
	 * 
	 * @param dataset
	 *            the dataset to set for.
	 * @param mask
	 *            <tt>true</tt> to mask it, <tt>false</tt> to not.
	 */
	public void setMaskRelation(final DataSet dataset, final boolean mask) {
		final boolean oldValue = this.isMaskRelation(dataset);
		if (mask == oldValue)
			return;
		if (mask) {
			this.getMods(dataset, null).put("maskRelation", null);
			this.pcs.firePropertyChange("maskRelation", null, dataset);
		} else {
			this.getMods(dataset, null).remove("maskRelation");
			this.pcs.firePropertyChange("maskRelation", dataset, null);
		}
	}

	/**
	 * Mask this relation.
	 * 
	 * @param dataset
	 *            the dataset to set for.
	 * @param tableKey
	 *            the dataset table to set for.
	 * @param mask
	 *            <tt>true</tt> to mask it, <tt>false</tt> to not.
	 */
	public void setMaskRelation(final DataSet dataset, final String tableKey,
			final boolean mask) {
		final boolean oldValue = this.isMaskRelation(dataset, tableKey);
		if (mask == oldValue)
			return;
		if (mask) {
			this.getMods(dataset, tableKey).put("maskRelation", null);
			this.pcs.firePropertyChange("maskRelation", null, tableKey);
		} else {
			this.getMods(dataset, tableKey).remove("maskRelation");
			this.pcs.firePropertyChange("maskRelation", tableKey, null);
		}
	}

	/**
	 * Is this relation alternative-joined?
	 * 
	 * @param dataset
	 *            the dataset to check for.
	 * @param tableKey
	 *            the table to check for.
	 * @return <tt>true</tt> if it is.
	 */
	public boolean isAlternativeJoin(final DataSet dataset,
			final String tableKey) {
		return this.getMods(dataset, tableKey).containsKey("alternativeJoin");
	}

	public int compareTo(final Relation relation) throws ClassCastException {
		if (this.firstKey.equals(relation.firstKey))
			return this.secondKey.compareTo(relation.secondKey);
		else if (this.firstKey.equals(relation.secondKey))
			return this.secondKey.compareTo(relation.firstKey);
		else if (this.secondKey.equals(relation.firstKey))
			return this.firstKey.compareTo(relation.secondKey);
		else
			return this.firstKey.compareTo(relation.firstKey);
	}

	public int hashCode() {
		final int firstHash = this.firstKey.hashCode();
		final int secondHash = this.secondKey.hashCode();
		// So that two rels between same keys always match.
		return (Math.min(firstHash, secondHash) + "_" + Math.max(firstHash,
				secondHash)).hashCode();
	}

	public boolean equals(final Object o) {
		if (o == this)
			return true;
		else if (o == null)
			return false;
		else if (o instanceof Relation) {
			final Relation r = (Relation) o;
			// Check that the same keys are involved.
			return r.firstKey.equals(this.secondKey)
					&& r.secondKey.equals(this.firstKey)
					|| r.firstKey.equals(this.firstKey)
					&& r.secondKey.equals(this.secondKey);
		} else
			return false;
	}

	public String toString() {
		final StringBuffer sb = new StringBuffer();
		sb.append(this.firstKey == null ? "<undef>" : this.firstKey.toString());
		sb.append(" -> ");
		sb.append(this.secondKey == null ? "<undef>" : this.secondKey
				.toString());
		return sb.toString();
	}



}
