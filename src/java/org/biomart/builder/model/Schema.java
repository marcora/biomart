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
import java.sql.SQLException;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import org.biomart.common.exceptions.DataModelException;
import org.biomart.common.exceptions.TransactionException;
import org.biomart.common.resources.Log;
import org.biomart.common.resources.Resources;
import org.biomart.common.utils.McBeanCollection;
import org.biomart.common.utils.McBeanMap;
import org.biomart.common.utils.Transaction;
import org.biomart.common.utils.WeakPropertyChangeSupport;
import org.biomart.common.utils.Transaction.TransactionEvent;
import org.biomart.common.utils.Transaction.TransactionListener;




/**
 * A schema provides one or more table objects with unique names for the user to
 * use. It could be a relational database, or an XML document, or any other
 * source of potentially tabular information.
 * <p>
 * The generic implementation provided should suffice for most tasks involved in
 * keeping track of the tables a schema provides.
 * 
 * @author Richard Holland <holland@ebi.ac.uk>
 * @version $Revision: 1.67 $, $Date: 2008/02/20 11:47:30 $, modified by
 *          $Author: rh4 $
 * @since 0.5
 */
public class Schema implements Comparable<Schema>, DataLink, TransactionListener {

	/**
	 * Subclasses use this field to fire events of their own.
	 */
	protected final WeakPropertyChangeSupport pcs = new WeakPropertyChangeSupport(this);
	private final Mart mart;
	private int uniqueId;	
	private boolean isTarget = false;
	/**
	 * Subclasses can reference this to alter it - e.g. DataSet does this.
	 */
	protected String name;
	/**
	 * Subclasses can reference this to alter it - e.g. DataSet does this.
	 */
	protected String originalName;
	private boolean keyGuessing;
	private boolean masked;
	private String dataLinkSchema;
	private String dataLinkDatabase;
	private McBeanMap<String,Table> tables;
	private String partitionRegex;
	private String partitionNameExpression;
	private final Map<String,String> partitionCache = new TreeMap<String,String>();

	/**
	 * Subclasses use this to notify update requirements.
	 */
	protected boolean needsFullSync;
	private boolean hideMasked = false;
	private final McBeanCollection<Relation> relationCache;
	/**
	 * Subclasses use this to update synchronisation progress.
	 */
	protected double progress = 0.0;
	/*
	 * add/remove table (McBeanMap); add/remove relation (McBeanCollection) will trigger it.
	 */
	private final PropertyChangeListener relationCacheBuilder = new PropertyChangeListener() {
		public void propertyChange(final PropertyChangeEvent evt) {
			Schema.this.recalculateCaches(evt);
		}
	};


	/**
	 * The constructor creates a schema with the given name. Keyguessing is
	 * turned off.
	 * 
	 * @param mart
	 *            the mart this schema will belong to.
	 * @param name
	 *            the name for this new schema.
	 * @param dataLinkDatabase
	 *            the database name we are using.
	 * @param dataLinkSchema
	 *            the database schema name we are using.
	 * @param partitionRegex
	 *            partition stuff.
	 * @param partitionNameExpression
	 *            partition stuff.
	 */
	public Schema(final Mart mart, final String name,
			final String dataLinkDatabase, final String dataLinkSchema,
			final String partitionRegex, final String partitionNameExpression) {
		this(mart, name, false, dataLinkDatabase, dataLinkSchema,
				partitionRegex, partitionNameExpression);
	}

	/**
	 * This constructor creates a schema with the given name, and with
	 * keyguessing set to the given value.
	 * 
	 * @param mart
	 *            the mart this schema will belong to.
	 * @param name
	 *            the name for the new schema.
	 * @param keyGuessing
	 *            <tt>true</tt>if you want keyguessing, <tt>false</tt> if
	 *            not.
	 * @param dataLinkDatabase
	 *            the database name we are using.
	 * @param dataLinkSchema
	 *            the database schema name we are using.
	 * @param partitionRegex
	 *            partition stuff.
	 * @param partitionNameExpression
	 *            partition stuff.
	 */
	public Schema(final Mart mart, final String name,
			final boolean keyGuessing, final String dataLinkDatabase,
			final String dataLinkSchema, final String partitionRegex,
			final String partitionNameExpression) {
		Log.debug("Creating schema " + name);
		this.mart = mart;
		//changed for creating schema before mart.
		if(this.mart==null)
			this.uniqueId = 1;
		else
			this.uniqueId = this.mart.getNextUniqueId();
		this.setName(name);
		this.setOriginalName(name);
		this.setKeyGuessing(keyGuessing);
		this.setDataLinkSchema(dataLinkSchema);
		this.setDataLinkDatabase(dataLinkDatabase);
		this.setMasked(false);
		// TreeMap keeps the partition cache in alphabetical order by name.
		this.tables = new McBeanMap<String,Table>(new HashMap<String,Table>());
		this.needsFullSync = false;

		Transaction.addTransactionListener(this);

		// Listen to own tables and update key+relation caches.
		this.relationCache = new McBeanCollection<Relation>(new HashSet<Relation>());
		//only listen for add/remove item
		this.tables.addPropertyChangeListener(McBeanMap.property_AddItem,this.relationCacheBuilder);
		this.tables.addPropertyChangeListener(McBeanMap.property_RemoveItem,this.relationCacheBuilder);
	}

	/**
	 * Do a 'select distinct' on the given column in the given schema.
	 * 
	 * @param schemaPrefix
	 *            the schema prefix identifier. Use a sensible default if null
	 *            given.
	 * @param column
	 *            the column to select.
	 * @return the values.
	 * @throws SQLException
	 *             if it goes wrong.
	 */
	public Collection getUniqueValues(final String schemaPrefix,
			final Column column) throws SQLException {
		return Collections.EMPTY_SET;
	}

	/**
	 * Change the unique ID for this schema.
	 * 
	 * @param uniqueId
	 *            the new one to use.
	 */
	public void setUniqueId(final int uniqueId) {
		this.uniqueId = uniqueId;
	}

	/**
	 * Get the unique ID for this schema.
	 * 
	 * @return the unique ID.
	 */
	public int getUniqueId() {
		return this.uniqueId;
	}

	/**
	 * Obtain the next unique ID to use for a table.
	 * 
	 * @return the next ID.
	 */
	public int getNextUniqueId() {
		int x = 0;
		for (final Iterator<Table> i = this.tables.values().iterator(); i.hasNext();)
			x = Math.max(x, (i.next()).getUniqueId());
		return x + 1;
	}

	/**
	 * Work out how far synchronising has got. If this returns a value greater
	 * than or equal to 100.0 then syncing is complete.
	 * 
	 * @return the progress so far on a scale of 0.0 to 100.0.
	 */
	public double getProgress() {
		return this.progress;
	}


	public void setDirectModified(final boolean modified) {
		// do nothing
	}

	public boolean isVisibleModified() {
		// If any table is visible modified, then we are too.
		for (final Iterator<Table> i = this.getTables().values().iterator(); i
				.hasNext();)
			if ((i.next()).isVisibleModified())
				return true;
		return false;
	}

	public void setVisibleModified(final boolean modified) {
		// If any table is visible modified, then we are too.
	}

	public void transactionResetVisibleModified() {
		// If any table is visible modified, then we are too.
	}

	public void transactionResetDirectModified() {
		// do nothing
	}

	public void transactionStarted(final TransactionEvent evt) {
		// Don't really care for now.
	}

	public void transactionEnded(final TransactionEvent evt)
			throws TransactionException {
		if (this.needsFullSync)
			try {
				this.synchronise();
			} catch (final Exception e) {
				throw new TransactionException(e);
			}
	}

	/**
	 * Indicate that a table has been dropped.
	 * 
	 * @param table
	 *            the table that has been dropped.
	 */
	protected void tableDropped(final Table table) {
		// Do nothing here.
	}

	/*
	 * propertyName can be McBeanMap.* and McCollection.*
	 * source: table or relation
	 */
	private synchronized void recalculateCaches(PropertyChangeEvent pce) {
		String propertyName = pce.getPropertyName();
		//new table added
		if(propertyName.equals(McBeanMap.property_AddItem)) {
			Table table = (Table)pce.getNewValue();
			//only listen for add/remove relation
			table.getRelations().addPropertyChangeListener(McBeanCollection.property_AddItem,this.relationCacheBuilder);
			table.getRelations().addPropertyChangeListener(McBeanCollection.property_RemoveItem,this.relationCacheBuilder);
		} //table dropped
		else if(propertyName.equals(McBeanMap.property_RemoveItem)) {
			Table table = (Table) pce.getOldValue();
			this.tableDropped(table);
		}else if(propertyName.equals(McBeanCollection.property_AddItem)) {
			
		}else if(propertyName.equals(McBeanCollection.property_RemoveItem)) {
			
		}
		else {
			//TODO testing
			System.err.println("source = "+pce.getSource());
			System.err.println("property message not handled");
		}
		
		//TODO 
		final Collection<Relation> newRels = new HashSet<Relation>();
		for (final Iterator<Table> i = this.tables.values().iterator(); i.hasNext();) {
			final Table table = i.next();
			newRels.addAll(table.getRelations());
		}
		if (!newRels.equals(this.relationCache)) {
			this.relationCache.clear();
			this.relationCache.addAll(newRels);
		}
	}

	/**
	 * Is this schema hiding masked components?
	 * 
	 * @param hideMasked
	 *            true if it is.
	 */
	public void setHideMasked(final boolean hideMasked) {
		Log
				.debug("Setting hide masked schema on " + this + " to "
						+ hideMasked);
		final boolean oldValue = this.hideMasked;
		if (this.hideMasked == hideMasked)
			return;
		this.hideMasked = hideMasked;
		this.pcs.firePropertyChange("hideMasked", oldValue, hideMasked);
	}

	/**
	 * Is this schema hiding masked components?
	 * 
	 * @return true if it is.
	 */
	public boolean isHideMasked() {
		return this.hideMasked;
	}

	/**
	 * Obtain all relations on this schema.
	 * 
	 * @return the unmodifiable collection of relations.
	 */
	public McBeanCollection<Relation> getRelations() {
		return this.relationCache;
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
	 * Obtain the tables in this schema.
	 * 
	 * @return the tables. The keys of the map are the names as returned by
	 *         {@link Table#getName()}. The values are the table objects
	 *         themselves.
	 */
	public McBeanMap<String,Table> getTables() {
		return this.tables;
	}

	/**
	 * Gets the mart for this schema.
	 * 
	 * @return the mart for this schema.
	 */
	public Mart getMart() {
		return this.mart;
	}

	/**
	 * Gets the name of this schema.
	 * 
	 * @return the name of this schema.
	 */
	public String getName() {
		return this.name;
	}

	/**
	 * Gets the original name of this schema.
	 * 
	 * @return the original name of this schema.
	 */
	public String getOriginalName() {
		return this.originalName;
	}

	public String getDataLinkDatabase() {
		return this.dataLinkDatabase;
	}

	public void setDataLinkDatabase(final String dataLinkDatabase) {
		Log.debug("Setting data link database on " + this + " to "
				+ dataLinkDatabase);
		final String oldValue = this.dataLinkDatabase;
		if (this.dataLinkDatabase == dataLinkDatabase
				|| this.dataLinkDatabase != null
				&& this.dataLinkDatabase.equals(dataLinkDatabase))
			return;
		this.dataLinkDatabase = dataLinkDatabase;
		this.needsFullSync = true;
		this.pcs.firePropertyChange("dataLinkDatabase", oldValue,
				dataLinkDatabase);
	}

	public String getDataLinkSchema() {
		return this.dataLinkSchema;
	}

	public void setDataLinkSchema(final String dataLinkSchema) {
		Log.debug("Setting data link schema on " + this + " to "
				+ dataLinkSchema);
		final String oldValue = this.dataLinkSchema;
		if (this.dataLinkSchema == dataLinkSchema
				|| this.dataLinkSchema != null
				&& this.dataLinkSchema.equals(dataLinkSchema))
			return;
		this.dataLinkSchema = dataLinkSchema;
		this.needsFullSync = true;
		this.pcs.firePropertyChange("dataLinkSchema", oldValue, dataLinkSchema);
	}

	/**
	 * Checks whether this schema is masked or not.
	 * 
	 * @return <tt>true</tt> if it is, <tt>false</tt> if it isn't.
	 */
	public boolean isMasked() {
		return this.masked;
	}

	/**
	 * Sets a new name for this schema. It checks with the mart first, and
	 * renames it if is not unique.
	 * 
	 * @param name
	 *            the new name for the schema.
	 */
	public void setName(String name) {
		Log.debug("Renaming schema " + this + " to " + name);
		final String oldValue = this.name;
		if (this.name == name || this.name != null && this.name.equals(name))
			return;
		// Work out all used names.
		final Set<String> usedNames = new HashSet<String>();
		for (final Iterator i = this.mart.getSchemas().values().iterator(); i
				.hasNext();)
			usedNames.add(((Schema) i.next()).getName());
		// Make new name unique.
		final String baseName = name;
		for (int i = 1; usedNames.contains(name); name = baseName + "_" + i++);
		this.name = name;
		this.pcs.firePropertyChange(Resources.get("PCNAME"), oldValue, name);
	}

	/**
	 * Sets a new original name for this schema. It checks with the mart first,
	 * and renames it if is not unique.
	 * TODO: check to see if the originalName is the same as name
	 * 
	 * @param name
	 *            the new original name for the schema.
	 */
	protected void setOriginalName(String name) {
		Log.debug("Renaming original schema " + this + " to " + name);
		// Work out all used names.
		final Set<String> usedNames = new HashSet<String>();
		for (final Iterator i = this.mart.getSchemas().values().iterator(); i
				.hasNext();)
			usedNames.add(((Schema) i.next()).getOriginalName());
		// Make new name unique.
		final String baseName = name;
		for (int i = 1; usedNames.contains(name); name = baseName + "_" + i++)
			;
		this.originalName = name;
	}

	/**
	 * Sets whether this schema is masked or not.
	 * 
	 * @param masked
	 *            <tt>true</tt> if it is, <tt>false</tt> if it isn't.
	 */
	public void setMasked(final boolean masked) {
		Log.debug("Setting masked schema on " + this + " to " + masked);
		final boolean oldValue = this.masked;
		if (this.masked == masked)
			return;
		this.masked = masked;
		this.pcs.firePropertyChange("masked", oldValue, masked);
	}

	/**
	 * Checks whether this schema uses key-guessing or not.
	 * 
	 * @return <tt>true</tt> if it does, <tt>false</tt> if it doesn't.
	 */
	public boolean isKeyGuessing() {
		return this.keyGuessing;
	}

	/**
	 * Sets whether this schema uses key-guessing or not.
	 * 
	 * @param keyGuessing
	 *            <tt>true</tt> if it does, <tt>false</tt> if it doesn't.
	 */
	public void setKeyGuessing(final boolean keyGuessing) {
		Log.debug("Setting key guessing on " + this + " to " + keyGuessing);
		final boolean oldValue = this.keyGuessing;
		if (this.keyGuessing == keyGuessing)
			return;
		this.keyGuessing = keyGuessing;
		this.needsFullSync = true;
		this.pcs.firePropertyChange("keyGuessing", oldValue, keyGuessing);
	}

	public void setIsMart(boolean b) {
		this.isTarget = b;
	}
	
	public boolean isMart() {
		return this.isTarget;
	}
	
	public int hashCode() {
		return 0; // Because Schemas can be used as keys in maps.
	}

	public boolean equals(final Object o) {
		if (o == this)
			return true;
		else if (o == null)
			return false;
		else if (o instanceof Schema) {
			final Schema t = (Schema) o;
			return (this.mart.getUniqueId() + "_" + this.originalName)
					.equals(t.mart.getUniqueId() + "_" + t.originalName);
		} else
			return false;
	}

	public int compareTo(final Schema obj) {
		return (this.mart.getUniqueId() + "_" + this.originalName)
				.compareTo(obj.mart.getUniqueId() + "_" + obj.originalName);
	}

	public String toString() {
		return this.name;
	}

	/**
	 * Synchronise this schema with the data source that is providing its
	 * tables. Synchronisation means checking the list of tables available and
	 * drop/add any that have changed, then check each column. and key and
	 * relation and update those too.
	 * <p>
	 * This method should set {@link #progress} to 0.0 and update it
	 * periodically until syncing is complete, when {@link #progress} should be
	 * greater than or equal to 100.0.
	 * 
	 * @throws SQLException
	 *             if there was a problem connecting to the data source.
	 * @throws DataModelException
	 *             if there was any other kind of logical problem.
	 */
	public void synchronise() throws SQLException, DataModelException {
		this.clearPartitionCache();
		this.needsFullSync = false;
		this.progress = 0.0;
		// Extend as required.
	}


	public boolean canCohabit(final DataLink dataLink) {
		// We're not connected to anything, so we can never cohabit.
		return false;
	}

	public boolean test() throws SQLException {
		// We're not connected to anything, so we always work.
		return true;
	}

	/**
	 * If this schema is identical across multiple source schemas, and the user
	 * wants to process each of those sequentially using the same schema
	 * settings, then the map returned by this call should be used to set up
	 * those partitions.
	 * <p>
	 * Note that the schema itself does not necessarily have to appear in the
	 * partition map - it is only a template by which each partition will be
	 * created.
	 * <p>
	 * The keys of the maps are strings - they can mean different things
	 * according to whether this is a JDBC schema, an XML schema, etc. The
	 * values are the prefix to stick on table names in datasets generated from
	 * this schema.
	 * <p>
	 * The entries in the map are the result of applying a combination of
	 *  to
	 * the list of available schemas in the database, as determined by the
	 * appropriate database driver.
	 * <p>
	 * This is NOT a bean. The contents are NOT mutable. If you want to change
	 * them, use  and
	 * to alter the matching and
	 * transformation regexes to change the content of the map.
	 * 
	 * @return the map of partitions. If empty, then partitioning is not
	 *         required. It will never be <tt>null</tt>.
	 * @throws SQLException
	 *             if the partitions could not be retrieved.
	 */
	public Map getPartitions() throws SQLException {
		if (this.partitionCache.isEmpty() && (this.partitionRegex != null && !this.partitionRegex.equals(""))
				&& (this.partitionNameExpression != null && !this.partitionNameExpression.equals("")))
			this.populatePartitionCache(this.partitionCache);
		return Collections.unmodifiableMap(this.partitionCache);
	}

	/**
	 * This method is for subclasses to use  and
	 * , both of which are guaranteed to
	 * be non-null when this method is called, to recalculate the set of
	 * partition values available. The map should have keys which are actual
	 * database schema names, and the values should be prefixes to use for those
	 * schemas when modifying table names to be unique to each partition.
	 * 
	 * @param partitionCache
	 *            the cache to populate. It will already be empty.
	 * @throws SQLException
	 *             if the population went wrong.
	 */
	protected void populatePartitionCache(final Map<String, String> partitionCache)
			throws SQLException {
		// Do nothing.
	}

	/**
	 * Return the first n rows for a table.
	 * 
	 * @param schemaPrefix
	 *            the schema to use.
	 * @param table
	 *            the table to get rows from.
	 * @param count
	 *            the number of rows to select.
	 * @return the rows. The list will be empty if the operation is not
	 *         possible.
	 * @throws SQLException
	 *             if anything goes wrong.
	 */
	public List getRows(final String schemaPrefix, final Table table,
			final int count) throws SQLException {
		// Default implementation does nothing.
		return Collections.EMPTY_LIST;
	}

	/**
	 * Retrieve the regex used to work out schema partitions. If this regex is
	 * <tt>null</tt> then no partitioning will be done.
	 * 
	 * @return the regex used. Groups from this regex will be used to populate
	 *         values in the name expression. See
	 *         .
	 */
	public String getPartitionRegex() {
		return this.partitionRegex;
	}

	/**
	 * Retrieve the expression used to reformat groups from the partition regex
	 * into schema partition names.
	 * 
	 * @return the expression used. .
	 */
	public String getPartitionNameExpression() {
		return this.partitionNameExpression;
	}


	/**
	 * Clears the partition cache.
	 */
	public void clearPartitionCache() {
		this.partitionCache.clear();
	}

	/**
	 * replace synchronise if the schema is first time created. To create a schema fast. 
	 * @throws SQLException 
	 * @throws DataModelException 
	 */
	public void init(List<String> tablesInDb) throws DataModelException, SQLException {
		
}
	
}
