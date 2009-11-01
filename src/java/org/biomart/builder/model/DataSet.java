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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.biomart.builder.exceptions.ValidationException;
import org.biomart.builder.model.Key.ForeignKey;
import org.biomart.builder.model.Key.PrimaryKey;
import org.biomart.builder.model.Relation.CompoundRelationDefinition;
import org.biomart.builder.model.Relation.RestrictedRelationDefinition;
import org.biomart.builder.model.TransformationUnit.JoinTable;
import org.biomart.builder.model.TransformationUnit.SelectFromTable;
import org.biomart.builder.model.TransformationUnit.SkipTable;
import org.biomart.builder.model.TransformationUnit.UnrollTable;
import org.biomart.builder.view.gui.diagrams.DataSetDiagram;
import org.biomart.common.exceptions.BioMartError;
import org.biomart.common.exceptions.DataModelException;
import org.biomart.common.exceptions.TransactionException;
import org.biomart.common.resources.Log;
import org.biomart.common.resources.Resources;

import org.biomart.common.utils.Transaction.TransactionEvent;

import org.biomart.configurator.utils.McEventObject;
import org.biomart.configurator.utils.type.Cardinality;
import org.biomart.configurator.utils.type.DataSetOptimiserType;
import org.biomart.configurator.utils.type.DataSetTableType;
import org.biomart.configurator.utils.type.EventType;
import org.biomart.configurator.utils.type.IdwViewType;
import org.biomart.configurator.view.idwViews.McViews;

/**
 * A {@link DataSet} instance serves two purposes. First, it contains lists of
 * settings that are specific to this dataset and affect the way in which tables
 * and relations in the schemas it draws data from behave. Secondly, it is a
 * {@link Schema} itself, containing definitions of all the tables in the
 * dataset it represents and how they relate to each other.
 * <p>
 * The settings that customise the way in which schemas it uses behave include
 * masking of unwanted relations and columns, and flagging of relations as
 * concat-only or subclassed. These settings are specific to this dataset and do
 * not affect other datasets.
 * <p>
 * The central table of the dataset is a reference to a real table, from which
 * the main table of the dataset will be derived and all other transformations
 * in the dataset to produce dimensions and subclasses will begin.
 * 
 * @author Richard Holland <holland@ebi.ac.uk>
 * @version $Revision: 1.272 $, $Date: 2008/03/28 12:40:46 $, modified by
 *          $Author: rh4 $
 * @since 0.5
 */
public class DataSet extends Schema {
	private static final long serialVersionUID = 1L;

	final PropertyChangeListener rebuildListener = new PropertyChangeListener() {
		public void propertyChange(PropertyChangeEvent evt) {			
			final Object src = evt.getSource();
			Object val = evt.getNewValue();
			if (val == null)
				val = evt.getOldValue();
			if (src instanceof Relation || src instanceof Table) {
				if (val instanceof DataSet)
					DataSet.this.needsFullSync = val == DataSet.this;
				else if (val instanceof String)
					DataSet.this.needsFullSync = DataSet.this.getTables()
							.containsKey(val);
				else
					DataSet.this.needsFullSync = true;
			} else
				DataSet.this.needsFullSync = true;
		}
	};

	private final Table centralTable;

	private final Collection<Relation> includedRelations;

	private final Collection<Table> includedTables;

	private final Collection<Schema> includedSchemas;

	private boolean invisible;


	private DataSetOptimiserType optimiser;

	private boolean indexOptimiser;


	private boolean deadCheck = false;

	/**
	 * Use this key for dataset-wide operations.
	 */
	public static final String DATASET = "__DATASET_WIDE__";

	private final Map<String, Map<String, Map<String, Object>>> mods = new HashMap<String, Map<String, Map<String, Object>>>();
	/**
	 * one of the view of dataset model
	 * TODO to remove martpanel for now, should move to view later
	 */
	private DataSetDiagram datasetDiagram;
	
	/**
	 * TODO should generate the diagram when dataset finished
	 * @param diagram
	 */
	public void setDataSetDiagram(DataSetDiagram diagram) {
		this.datasetDiagram = diagram;
	}
	
	public DataSetDiagram getDataSetDiagram() {
		return this.datasetDiagram;
	}

	/**
	 * The constructor creates a dataset around one central table and gives the
	 * dataset a name. It adds itself to the specified mart automatically.
	 * <p>
	 * If the name already exists, an underscore and a sequence number will be
	 * appended until the name is unique, as per the constructor in
	 * {@link Schema}, which it inherits from.
	 * 
	 * @param mart
	 *            the mart this dataset will belong to.
	 * @param centralTable
	 *            the table to use as the central table for this dataset.
	 * @param name
	 *            the name to give this dataset.
	 * @throws ValidationException
	 *             if the central table does not have a primary key.
	 */
	public DataSet(final Mart mart, final Table centralTable, final String name)
			throws ValidationException {
		// Super first, to set the name.
		super(mart, name, name, name, null, null);

		// Remember the settings and make some defaults.
		this.invisible = false;
		this.centralTable = centralTable;
		this.optimiser = DataSetOptimiserType.NONE;
		this.includedRelations = new LinkedHashSet<Relation>();
		this.includedTables = new LinkedHashSet<Table>();
		this.includedSchemas = new LinkedHashSet<Schema>();


		// Always need syncing at end of creating transaction.
		this.needsFullSync = true;

		// All changes to us make us modified.

		// Recalculate completely if parent mart changes case.
	}

	protected void tableDropped(final Table table) {
		final DataSetTable dsTable = (DataSetTable) table;
		// Remove all mods.
		for (final Iterator<Schema> j = this.getMart().getSchemasObj().getSchemas().values().iterator(); j
				.hasNext();) {
			final Schema sch = j.next();
			for (final Iterator<Table> k = sch.getTables().values().iterator(); k
					.hasNext();)
				( k.next()).dropMods(dsTable.getDataSet(), dsTable
						.getName());
			for (final Iterator<Relation> k = sch.getRelations().iterator(); k.hasNext();)
				((Relation) k.next()).dropMods(dsTable.getDataSet(), dsTable
						.getName());
		}
	}

	public void transactionEnded(final TransactionEvent evt)
			throws TransactionException {
		try {
			if (this.deadCheck
					&& !this.getMart().getSchemasObj().getSchemas().containsKey(
							this.centralTable.getSchema().getOriginalName())
					|| !this.centralTable.getSchema().getTables().containsKey(
							this.centralTable.getName()))
				this.getMart().getDataSets().remove(this.getOriginalName());
			else
				super.transactionEnded(evt);
		} finally {
			this.deadCheck = false;
		}
	}


	/**
	 * Sets a new name for this dataset. It checks with the mart first, and
	 * renames it if is not unique.
	 * 
	 * @param name
	 *            the new name for the schema.
	 */
	public void setName(String name) {
		Log.debug("Renaming dataset " + this + " to " + name);
		final String oldValue = this.name;
		if (this.name == name || this.name != null && this.name.equals(name))
			return;
		// Work out all used names.
		final Set usedNames = new HashSet();
		for (final Iterator i = this.getMart().getDataSets().values()
				.iterator(); i.hasNext();)
			usedNames.add(((DataSet) i.next()).getName());
		// Make new name unique.
		final String baseName = name;
		for (int i = 1; usedNames.contains(name); name = baseName + "_" + i++)
			;
		this.name = name;
		this.pcs.firePropertyChange(Resources.get("PCNAME"), oldValue, name);
	}

	/**
	 * Sets a new original name for this dataset. It checks with the mart first,
	 * and renames it if is not unique.
	 * 
	 * @param name
	 *            the new original name for the dataset.
	 */
	protected void setOriginalName(String name) {
		Log.debug("Renaming original dataset " + this + " to " + name);
		// Work out all used names.
		final Set usedNames = new HashSet();
		for (final Iterator i = this.getMart().getDataSets().values()
				.iterator(); i.hasNext();)
			usedNames.add(((DataSet) i.next()).getOriginalName());
		// Make new name unique.
		final String baseName = name;
		for (int i = 1; usedNames.contains(name); name = baseName + "_" + i++)
			;
		this.originalName = name;
	}

	/**
	 * This contains the 'real' version of all dataset modifications. It is
	 * accessed directly by MartBuilderXML, DataSetTable and DataSetColumn and
	 * is not for use anywhere else at all under any circumstances.
	 * 
	 * @param table
	 *            the table to apply the property to.
	 * @param property
	 *            the property to look up - e.g. maskedDimension, etc.
	 * @return the set of tables that the property currently applies to. This
	 *         set can be added to or removed from accordingly. The keys of the
	 *         map are names, the values are optional subsidiary objects.
	 */
	public Map<String, Object> getMods(final String table, final String property) {
		if (!this.mods.containsKey(table))
			this.mods.put(table.intern(), new HashMap<String, Map<String, Object>>());
		if (!(this.mods.get(table)).containsKey(property))
			(this.mods.get(table)).put(property.intern(), new HashMap<String, Object>());
		return  ( this.mods.get(table)).get(property);
	}



	/**
	 * Convert/Revert partition table status.
	 * 
	 * @param partitionTable
	 *            <tt>true</tt> if this dataset is to be a partition table.

	 *             if this dataset cannot be converted. You should use
	 *             to check first
	 *             before calling this if you want to avoid the exception.
	 */
	public void setPartitionTable(final boolean partitionTable)
{
		Log.debug("Setting partition table flag to " + partitionTable + " in "
				+ this);
		final boolean oldValue = false;
		if (partitionTable == oldValue)
			return;

			// Update ourselves to restore dimensions and subclasses.
			try {
				this.synchronise();
			} catch (final Exception e) {
				e.printStackTrace();
			}
	
		this.pcs.firePropertyChange("partitionTable", oldValue, partitionTable);
	}




	/**
	 * Obtain all relations used by this dataset, in the order in which they
	 * were included.
	 * 
	 * @return all relations.
	 */
	public Collection getIncludedRelations() {
		return this.includedRelations;
	}

	/**
	 * This internal method builds a dataset table based around a real table. It
	 * works out what dimensions and subclasses are required then recurses to
	 * create those too.
	 * 
	 * @param type
	 *            the type of table to build.
	 * @param parentDSTable
	 *            the table which this dataset table creates a foreign key to.
	 *            If this is to be a subclass table, it will inherit all columns
	 *            from this parent table.
	 * @param realTable
	 *            the real table in a schema from where the transformation to
	 *            create this dataset table will begin.
	 * @param skippedMainTables
	 *            the main tables to skip when building subclasses and
	 *            dimensions.
	 * @param sourceRelation
	 *            the real relation in a schema which was followed in order to
	 *            discover that this dataset table should be created. For
	 *            instance, it could be the 1:M relation between the realTable
	 *            parameter of this call, and the realTable parameter of the
	 *            main table call to this method.

	 */
	private void generateDataSetTable(final DataSetTableType type,
			final DataSetTable parentDSTable, final Table realTable,
			final Collection skippedMainTables, final List sourceDSCols,
			Relation sourceRelation, final Map subclassCount,
			final int relationIteration, final Collection unusedTables) {
		Log.debug("Creating dataset table for " + realTable
				+ " with parent relation " + sourceRelation + " as a " + type);
		// Create the empty dataset table. Use a unique prefix
		// to prevent naming clashes.
		String prefix = "";
		if (parentDSTable != null) {
			final String parts[] = parentDSTable.getName().split(
					Resources.get("tablenameSep"));
			prefix = parts[parts.length - 1] + Resources.get("tablenameSep");
		}
		String fullName = prefix + realTable.getName();
		if (relationIteration > 0)
			fullName = fullName + Resources.get("tablenameSubSep")
					+ relationIteration;
		// Loop over all tables with similar names to check for reuse.
		DataSetTable dsTable = null;
		for (final Iterator i = this.getTables().entrySet().iterator(); i
				.hasNext()
				&& dsTable == null;) {
			final Map.Entry entry = (Map.Entry) i.next();
			final String testName = (String) entry.getKey();
			final DataSetTable testTable = (DataSetTable) entry.getValue();
			// If find table starting with same letters, check to see
			// if can reuse, and update fullName to match it.
			if (testName.equals(fullName) || testName.startsWith(fullName))
				if (testTable.getFocusRelation() == null
						|| (testTable.getFocusRelation().equals(sourceRelation) && testTable
								.getFocusRelationIteration() == relationIteration)) {
					fullName = testName;
					dsTable = testTable;
					dsTable.setType(type); // Just to make sure.
					unusedTables.remove(dsTable);
					dsTable.getTransformationUnits().clear();
				} else
					dsTable = null;
		}
		// If still not found anything after all tables checked,
		// create new table.
		if (dsTable == null) {
			dsTable = new DataSetTable(fullName, this, type, realTable,
					sourceRelation, relationIteration);
			this.getTables().put(dsTable.getName(), dsTable);
			// Listen to this table to modify ourselves.
			// As it happens, nothing can happen to a dstable yet that
			// requires this.
		}

		// Prepare for action.
		dsTable.includedRelations.clear();
		dsTable.includedTables.clear();
		dsTable.includedSchemas.clear();

		// Identify secondStartTable and mergeTheseRelations.
		Table secondStartTable = null;
		final Set mergeTheseRelations = new HashSet();
		if (dsTable.getType().equals(DataSetTableType.DIMENSION)) {
			// A crude walk will tell us which relations we
			// would have included if we had done a normal transform.
			final List mergeTheseTables = new ArrayList();
			mergeTheseTables.add(realTable);
			for (int i = 0; i < mergeTheseTables.size(); i++) {
				final Table cand = (Table) mergeTheseTables.get(i);
				for (final Iterator j = cand.getRelations().iterator(); j
						.hasNext();) {
					final Relation candRel = (Relation) j.next();
					if (mergeTheseRelations.contains(candRel))
						continue;
					final Table newCand = candRel.getOtherKey(
							candRel.getKeyForTable(cand)).getTable();
					// Decide whether to include it or not.
					// Skip the source relation.
					if (candRel.equals(sourceRelation))
						continue;
					// Skip relations back to main tables.
					if (skippedMainTables.contains(newCand))
						continue;
					// Skip incorrect relations.
					if (candRel.getStatus().equals(
							ComponentStatus.INFERRED_INCORRECT))
						continue;
					// Skip masked relations.
					if (candRel.isMaskRelation(this, dsTable.getName()))
						continue;
					// Skip relations leading to masked schemas.
					if (newCand.getSchema().isMasked())
						continue;
					// Skip 1:M relations from the 1 end unless forced or
					// merged.
					if (candRel.isOneToMany()
							&& candRel.getOneKey().getTable().equals(cand)
							&& !(candRel.isForceRelation(this, dsTable
									.getName()) || candRel
									.isMergeRelation(this)))
						continue;
					// Add relation to set, and add table at other
					// end to queue.
					if (!mergeTheseTables.contains(newCand)) {
						mergeTheseTables.add(newCand);
						if (newCand.isTransformStart(this, dsTable.getName()))
							secondStartTable = newCand;
					}
					mergeTheseRelations.add(candRel);
				}
			}
			// At this stage we know if we have found an alternative
			// start point. If not, we can empty out the walked set.
			if (secondStartTable == null)
				mergeTheseRelations.clear();
		}

		// Create the three relation-table pair queues we will work with. The
		// normal queue holds pairs of relations and tables. The other two hold
		// a list of relations only, the tables being the FK ends of each
		// relation. The normal queue has a third object associated with each
		// entry, which specifies whether to treat the 1:M relations from
		// the merged table as dimensions or not.
		final List normalQ = new ArrayList();
		final List subclassQ = new ArrayList();
		final List dimensionQ = new ArrayList();

		// Set up a list to hold columns for this table's primary key.
		final List dsTablePKCols = new ArrayList();

		// Make a list of existing columns and foreign keys.
		final Collection unusedCols = new HashSet(dsTable.getColumns().values());
		final Collection unusedFKs = new HashSet(dsTable.getForeignKeys());

		// Make a map for unique column base names.
		final Map uniqueBases = new HashMap();

		// Skip this step if using a surrogate start table.
		// If the parent dataset table is not null, add columns from it
		// as appropriate. Dimension tables get just the PK, and an
		// FK linking them back. Subclass tables get all columns, plus
		// the PK with FK link, plus all the relations we followed to
		// get these columns.
		TransformationUnit parentTU = null;
		if (secondStartTable == null && parentDSTable != null) {
			parentTU = new SelectFromTable(parentDSTable);
			dsTable.addTransformationUnit(parentTU);

			// Make a list to hold the child table's FK cols.
			final List dsTableFKCols = new ArrayList();

			// Get the primary key of the parent DS table.
			final PrimaryKey parentDSTablePK = parentDSTable.getPrimaryKey();

			// Loop over each column in the parent table. If this is
			// a subclass table, add it. If it is a dimension table,
			// only add it if it is in the PK or is in the first underlying
			// key. In either case, if it is in the PK, add it both to the
			// child PK and the child FK. Also inherit if it is involved
			// in a restriction on the very first join.
			final RestrictedRelationDefinition restrictDef = sourceRelation
					.getRestrictRelation(this, dsTable.getName(), 0);
			for (final Iterator i = parentDSTable.getColumns().values()
					.iterator(); i.hasNext();) {
				final DataSetColumn parentDSCol = (DataSetColumn) i.next();
				boolean inRelationRestriction = false;
				// If this is not a subclass table, we need to filter columns.
				if (!type.equals(DataSetTableType.MAIN_SUBCLASS)) {
					// Skip columns that are not in the primary key.
					final boolean inPK = Arrays.asList(
							parentDSTablePK.getColumns()).contains(parentDSCol);
					final boolean inSourceKey = sourceDSCols
							.contains(parentDSCol);
					// If the column is in a restricted relation
					// on the source relation, we need to inherit it.
					if (restrictDef != null) {
						DataSetColumn inhCol = parentDSCol;
						while (inhCol instanceof InheritedColumn)
							inhCol = ((InheritedColumn) inhCol)
									.getInheritedColumn();
						if (inhCol instanceof WrappedColumn) {
							final Column wc = ((WrappedColumn) inhCol)
									.getWrappedColumn();
							inRelationRestriction = restrictDef
									.getLeftAliases().containsKey(wc)
									|| restrictDef.getRightAliases()
											.containsKey(wc);
						}
					}
					// Inherit it?
					if (!inPK && !inSourceKey && !inRelationRestriction)
						continue;
				}
				// Only unfiltered columns reach this point. Create a copy of
				// the column.
				final InheritedColumn dsCol;
				if (!dsTable.getColumns().containsKey(
						parentDSCol.getModifiedName())) {
					dsCol = new InheritedColumn(dsTable, parentDSCol);
					dsTable.getColumns().put(dsCol.getName(), dsCol);
					// If any other col has modified name same as
					// inherited col's modified name, then rename the
					// other column to avoid clash.
					for (final Iterator j = dsTable.getColumns().values()
							.iterator(); j.hasNext();) {
						final DataSetColumn cand = (DataSetColumn) j.next();
						if (!(cand instanceof InheritedColumn)
								&& cand.getModifiedName().equals(
										dsCol.getModifiedName()))
							try {
								final DataSetColumn renameCol = inRelationRestriction ? cand
										: dsCol;
								if (renameCol.getModifiedName().endsWith(
										Resources.get("keySuffix")))
									renameCol
											.setColumnRename(renameCol
													.getModifiedName()
													.substring(
															0,
															renameCol
																	.getModifiedName()
																	.indexOf(
																			Resources
																					.get("keySuffix")))
													+ "_clash"
													+ Resources
															.get("keySuffix"), true);
								else
									renameCol.setColumnRename(renameCol
											.getModifiedName()
											+ "_clash", true);
							} catch (final ValidationException ve) {
								// Ouch!
								throw new BioMartError(ve);
							}
					}
				} else
					dsCol = (InheritedColumn) dsTable.getColumns().get(
							parentDSCol.getModifiedName());
				unusedCols.remove(dsCol);
				parentTU.getNewColumnNameMap().put(parentDSCol, dsCol);
				dsCol.setTransformationUnit(parentTU);
				uniqueBases.put(parentDSCol.getModifiedName(), new Integer(0));
				// Add the column to the child's FK, but only if it was in
				// the parent PK.
				if (Arrays.asList(parentDSTablePK.getColumns()).contains(
						parentDSCol))
					dsTableFKCols.add(dsCol);
			}

			try {
				// Create the child FK.
				ForeignKey dsTableFK = new ForeignKey((Column[]) dsTableFKCols
						.toArray(new Column[0]));

				// Create only if not already exists.
				for (final Iterator i = dsTable.getForeignKeys().iterator(); i
						.hasNext();) {
					final ForeignKey cand = (ForeignKey) i.next();
					if (cand.equals(dsTableFK))
						dsTableFK = cand;
				}
				if (!dsTable.getForeignKeys().contains(dsTableFK)) {
					dsTable.getForeignKeys().add(dsTableFK);
					// Link the child FK to the parent PK.
					final Relation rel = new Relation(parentDSTablePK,
							dsTableFK, Cardinality.MANY_A);
					parentDSTablePK.getRelations().add(rel);
					dsTableFK.getRelations().add(rel);
				}
				unusedFKs.remove(dsTableFK);
			} catch (final Throwable t) {
				throw new BioMartError(t);
			}

			// Copy all parent FKs and add to child, but WITHOUT
			// relations. Subclasses only!
			if (type.equals(DataSetTableType.MAIN_SUBCLASS))
				for (final Iterator i = parentDSTable.getForeignKeys()
						.iterator(); i.hasNext();) {
					final ForeignKey parentFK = (ForeignKey) i.next();
					final List childFKCols = new ArrayList();
					for (int j = 0; j < parentFK.getColumns().length; j++)
						childFKCols.add(parentTU.getNewColumnNameMap().get(
								parentFK.getColumns()[j]));
					try {
						// Create the child FK.
						ForeignKey dsTableFK = new ForeignKey(
								(Column[]) childFKCols.toArray(new Column[0]));

						// Create only if not already exists.
						for (final Iterator j = dsTable.getForeignKeys()
								.iterator(); j.hasNext();) {
							final ForeignKey cand = (ForeignKey) j.next();
							if (cand.equals(dsTableFK))
								dsTableFK = cand;
						}
						if (dsTable.getForeignKeys().contains(dsTableFK))
							dsTable.getForeignKeys().add(dsTableFK);
						unusedFKs.remove(dsTableFK);
					} catch (final Throwable t) {
						throw new BioMartError(t);
					}
				}
		}

		// How many times are allowed to iterate over each relation?
		final Map relationCount = new HashMap();
		for (final Iterator<Schema> i = this.getMart().getSchemasObj().getSchemas().values().iterator(); i
				.hasNext();) {
			final Schema schema =  i.next();
			final Set relations = new HashSet();
			for (final Iterator j = schema.getTables().values().iterator(); j
					.hasNext();) {
				final Table tbl = (Table) j.next();
				if (tbl.getPrimaryKey() != null)
					relations.addAll(tbl.getPrimaryKey().getRelations());
				for (final Iterator k = tbl.getForeignKeys().iterator(); k
						.hasNext();)
					relations.addAll(((ForeignKey) k.next()).getRelations());
			}
			for (final Iterator j = relations.iterator(); j.hasNext();) {
				final Relation rel = (Relation) j.next();
				// Partition compounding is dealt with separately
				// and does not need to be included here.
				final CompoundRelationDefinition def = rel.getCompoundRelation(
						this, dsTable.getName());
				int compounded = def == null ? 1 : def.getN();
				// If loopback, increment count by one.
				if (rel.getLoopbackRelation(this, dsTable.getName()) != null)
					compounded++;
				relationCount.put(rel, new Integer(compounded));
			}
		}

		// How many times have we actually seen each table?
		final Map tableTracker = new HashMap();

		// Process the table. This operation will populate the initial
		// values in the normal, subclass and dimension queues. We only
		// want dimensions constructed if we are not already constructing
		// a dimension ourselves.
		this.processTable(parentTU, dsTable, dsTablePKCols,
				secondStartTable != null ? secondStartTable : realTable,
				normalQ, subclassQ, dimensionQ, sourceDSCols,
				secondStartTable != null ? null : sourceRelation,
				relationCount, subclassCount, 
						 !type.equals(DataSetTableType.DIMENSION),
				Collections.EMPTY_LIST, Collections.EMPTY_LIST,
				relationIteration, 0, unusedCols, uniqueBases,
				skippedMainTables, tableTracker, mergeTheseRelations);

		// Process the normal queue. This merges tables into the dataset
		// table using the relation specified in each pair in the queue.
		// The third value is the dataset parent table columns to link from.
		// The fourth value of each entry in the queue determines whether or
		// not to continue making dimensions off each table in the queue.
		// The fifth value is the counter of how many times this relation has
		// been seen before.
		// The sixth value is a map of relation counts used to reach this point.
		for (int i = 0; i < normalQ.size(); i++) {
			final Object[] tuple = (Object[]) normalQ.get(i);
			final Relation mergeSourceRelation = (Relation) tuple[0];
			final List newSourceDSCols = (List) tuple[1];
			final Table mergeTable = (Table) tuple[2];
			final TransformationUnit previousUnit = (TransformationUnit) tuple[3];
			final boolean makeDimensions = ((Boolean) tuple[4]).booleanValue();
			final int iteration = ((Integer) tuple[5]).intValue();
			final List nameCols = (List) tuple[6];
			final List nameColSuffixes = (List) tuple[7];
			final Map newRelationCounts = (Map) tuple[8];
			this.processTable(previousUnit, dsTable, dsTablePKCols, mergeTable,
					normalQ, subclassQ, dimensionQ, newSourceDSCols,
					mergeSourceRelation, newRelationCounts, subclassCount,
					makeDimensions, nameCols, nameColSuffixes, iteration,
					i + 1, unusedCols, uniqueBases, skippedMainTables,
					tableTracker, mergeTheseRelations);
		}

		// If using second select, need to do skip initial Select and
		// do a Join AFTER everything instead. That join must inherit
		// columns by doing a parent join to last temp table. If any columns
		// are involved in the join then add to where clause and DO NOT
		// inherit them.
		if (secondStartTable != null && parentDSTable != null) {
			// Work out the last stage in the transform so far.
			final JoinTable lastTU = (JoinTable) dsTable
					.getTransformationUnits().get(
							dsTable.getTransformationUnits().size() - 1);

			// Make it an inner join.
			sourceRelation.setAlternativeJoin(this, dsTable.getName(), false);

			// Locate the columns on our own table which form that join.
			// Note how we have to traverse forwards as this is a linked
			// list and getPreviousUnit may shortcut to the top.
			final List dsTabJoinCols = new ArrayList();
			for (int i = 0; i < sourceRelation.getManyKey().getColumns().length; i++) {
				boolean keepGoing = true;
				for (final Iterator j = dsTable.getTransformationUnits()
						.iterator(); keepGoing && j.hasNext();) {
					final TransformationUnit ptu = (TransformationUnit) j
							.next();
					final DataSetColumn candCol = ptu
							.getDataSetColumnFor(sourceRelation.getManyKey()
									.getColumns()[i]);
					if (candCol != null) {
						dsTabJoinCols.add(candCol);
						keepGoing = false;
					}
				}
			}

			// Set up a new join table. We join to parent
			// using sourceDSCols from dsTabJoinCols.
			final TransformationUnit parentJoin = new JoinTable(lastTU,
					parentDSTable, dsTabJoinCols, new ForeignKey(
							(DataSetColumn[]) sourceDSCols
									.toArray(new DataSetColumn[0])),
					sourceRelation, relationIteration);
			dsTable.addTransformationUnit(parentJoin);

			// Make a list to hold the child table's FK cols.
			final List dsTableFKCols = new ArrayList();

			// Get the primary key of the parent DS table.
			final PrimaryKey parentDSTablePK = parentDSTable.getPrimaryKey();

			// Loop over each column in the parent table. If this is
			// a subclass table, add it. If it is a dimension table,
			// only add it if it is in the PK or is in the first underlying
			// key. In either case, if it is in the PK, add it both to the
			// child PK and the child FK. Also inherit if it is involved
			// in a restriction on the very first join.
			final RestrictedRelationDefinition restrictDef = sourceRelation
					.getRestrictRelation(this, dsTable.getName(), 0);
			for (final Iterator i = parentDSTable.getColumns().values()
					.iterator(); i.hasNext();) {
				final DataSetColumn parentDSCol = (DataSetColumn) i.next();
				boolean inRelationRestriction = false;
				// As this is not a subclass table, we need to filter columns.
				// Skip columns that are not in the primary key.
				final boolean inPK = Arrays
						.asList(parentDSTablePK.getColumns()).contains(
								parentDSCol);
				// If the column is in a restricted relation
				// on the source relation, we need to inherit it.
				if (restrictDef != null) {
					DataSetColumn inhCol = parentDSCol;
					while (inhCol instanceof InheritedColumn)
						inhCol = ((InheritedColumn) inhCol)
								.getInheritedColumn();
					if (inhCol instanceof WrappedColumn) {
						final Column wc = ((WrappedColumn) inhCol)
								.getWrappedColumn();
						inRelationRestriction = restrictDef.getLeftAliases()
								.containsKey(wc)
								|| restrictDef.getRightAliases()
										.containsKey(wc);
					}
				}
				// Inherit it?
				if (!inPK && !inRelationRestriction)
					continue;
				// We don't inherit cols that we already have.
				if (sourceDSCols.contains(parentDSCol)) {
					// Add the column to the child's FK, but only if it was in
					// the parent PK.
					if (Arrays.asList(parentDSTablePK.getColumns()).contains(
							parentDSCol)) {
						final DataSetColumn existingFKCol = (DataSetColumn) dsTabJoinCols
								.get(Arrays
										.asList(parentDSTablePK.getColumns())
										.indexOf(parentDSCol));
						dsTableFKCols.add(existingFKCol);
						try {
							existingFKCol.setColumnRename(parentDSCol
									.getModifiedName(), false);
						} catch (final ValidationException ve) {
							// Should never happen.
							throw new BioMartError(ve);
						}
					}
					// On to the next one.
					continue;
				}
				// Only unfiltered columns reach this point. Create a copy of
				// the column.
				final InheritedColumn dsCol;
				if (!dsTable.getColumns().containsKey(
						parentDSCol.getModifiedName())) {
					dsCol = new InheritedColumn(dsTable, parentDSCol);
					dsTable.getColumns().put(dsCol.getName(), dsCol);
					// If any other col has modified name same as
					// inherited col's modified name, then rename the
					// other column to avoid clash.
					for (final Iterator j = dsTable.getColumns().values()
							.iterator(); j.hasNext();) {
						final DataSetColumn cand = (DataSetColumn) j.next();
						if (!(cand instanceof InheritedColumn)
								&& cand.getModifiedName().equals(
										dsCol.getModifiedName()))
							try {
								final DataSetColumn renameCol = inRelationRestriction ? cand
										: dsCol;
								if (renameCol.getModifiedName().endsWith(
										Resources.get("keySuffix")))
									renameCol
											.setColumnRename(renameCol
													.getModifiedName()
													.substring(
															0,
															renameCol
																	.getModifiedName()
																	.indexOf(
																			Resources
																					.get("keySuffix")))
													+ "_clash"
													+ Resources
															.get("keySuffix"), true);
								else
									renameCol.setColumnRename(renameCol
											.getModifiedName()
											+ "_clash", true);
							} catch (final ValidationException ve) {
								// Ouch!
								throw new BioMartError(ve);
							}
					}
				} else
					dsCol = (InheritedColumn) dsTable.getColumns().get(
							parentDSCol.getModifiedName());
				unusedCols.remove(dsCol);
				parentJoin.getNewColumnNameMap().put(parentDSCol, dsCol);
				dsCol.setTransformationUnit(parentJoin);
				uniqueBases.put(parentDSCol.getModifiedName(), new Integer(0));
				// Add the column to the child's FK, but only if it was in
				// the parent PK.
				if (Arrays.asList(parentDSTablePK.getColumns()).contains(
						parentDSCol))
					dsTableFKCols.add(dsCol);
			}

			try {
				// Create the child FK.
				ForeignKey dsTableFK = new ForeignKey((Column[]) dsTableFKCols
						.toArray(new Column[0]));
				// Create only if not already exists.
				for (final Iterator i = dsTable.getForeignKeys().iterator(); i
						.hasNext();) {
					final ForeignKey cand = (ForeignKey) i.next();
					if (cand.equals(dsTableFK))
						dsTableFK = cand;
				}
				if (!dsTable.getForeignKeys().contains(dsTableFK)) {
					dsTable.getForeignKeys().add(dsTableFK);
					// Link the child FK to the parent PK.
					final Relation rel = new Relation(parentDSTablePK,
							dsTableFK, Cardinality.MANY_A);
					parentDSTablePK.getRelations().add(rel);
					dsTableFK.getRelations().add(rel);
				}
				unusedFKs.remove(dsTableFK);
			} catch (final Throwable t) {
				throw new BioMartError(t);
			}
		} //end of if ...

		// Create the primary key on this table, but only if it has one.
		// Don't bother for dimensions.
		if (!dsTablePKCols.isEmpty()
				&& !dsTable.getType().equals(DataSetTableType.DIMENSION))
			// Create the key.
			dsTable.setPrimaryKey(new PrimaryKey((Column[]) dsTablePKCols
					.toArray(new Column[0])));
		else
			dsTable.setPrimaryKey(null);

		// Fish out any UnrollTable units and move to end of queue.
		final List units = dsTable.getTransformationUnits();
		for (int i = 1; i < units.size() - 1; i++) { // Skip very first+last.
			final TransformationUnit tu = (TransformationUnit) units.get(i);
			if (tu instanceof UnrollTable) {
				final TransformationUnit ptu = (TransformationUnit) units
						.get(i - 1);
				final TransformationUnit ntu = (TransformationUnit) units
						.get(i + 1);
				ntu.setPreviousUnit(ptu);
				final TransformationUnit ltu = (TransformationUnit) units
						.get(units.size() - 1);
				tu.setPreviousUnit(ltu);
				units.remove(i);
				units.add(tu);
				break;
			}
		}

		// Drop unused columns and foreign keys.
		for (final Iterator i = unusedFKs.iterator(); i.hasNext();) {
			final ForeignKey fk = (ForeignKey) i.next();
			for (final Iterator j = fk.getRelations().iterator(); j.hasNext();) {
				final Relation rel = (Relation) j.next();
				rel.getFirstKey().getRelations().remove(rel);
				rel.getSecondKey().getRelations().remove(rel);
			}
			dsTable.getForeignKeys().remove(fk);
		}
		for (final Iterator i = unusedCols.iterator(); i.hasNext();) {
			final Column deadCol = (Column) i.next();
			dsTable.getColumns().remove(deadCol.getName());
			// mods is Map{tablename -> Map{propertyName -> Map{...}} }
			for (final Iterator j = (this.mods.get(deadCol.getTable()
					.getName())).entrySet().iterator(); j.hasNext();) {
				final Map.Entry entry = (Map.Entry) j.next();
				// entry is propertyName -> Map{columnName, Object}}
				((Map) entry.getValue()).remove(deadCol.getName());
			}
		}

		// Rename columns in keys to have _key suffixes, and
		// remove that suffix from all others.
		//TODO is _key done in processTable?
		for (final Iterator i = dsTable.getColumns().values().iterator(); i
				.hasNext();) {
			final DataSetColumn dsCol = (DataSetColumn) i.next();
			try {
				if (dsCol.isKeyCol()
						&& !dsCol.getModifiedName().endsWith(
								Resources.get("keySuffix")))
					dsCol.setColumnRename(dsCol.getModifiedName()
							+ Resources.get("keySuffix"), false);
				else if (!dsCol.isKeyCol()
						&& dsCol.getModifiedName().endsWith(
								Resources.get("keySuffix")))
					dsCol.setColumnRename(dsCol.getModifiedName().substring(
							0,
							dsCol.getModifiedName().indexOf(
									Resources.get("keySuffix"))), false);
			} catch (final ValidationException ve) {
				// Should never happen!
				throw new BioMartError(ve);
			}
		}

		// Do a user-friendly rename.
		if (dsTable.getTableRename() == null)
			dsTable.setTableRename(realTable.getName());

		// Only dataset tables with primary keys can have subclasses
		// or dimensions.
		if (dsTable.getPrimaryKey() != null) {
			// Process the subclass relations of this table.
			for (int i = 0; i < subclassQ.size(); i++) {
				final Object[] triple = (Object[]) subclassQ.get(i);
				final List newSourceDSCols = (List) triple[0];
				final Relation subclassRelation = (Relation) triple[1];
				final int iteration = ((Integer) triple[2]).intValue();
				this.generateDataSetTable(DataSetTableType.MAIN_SUBCLASS,
						dsTable, subclassRelation.getManyKey().getTable(),
						skippedMainTables, newSourceDSCols, subclassRelation,
						subclassCount, iteration, unusedTables);
			}

			// Process the dimension relations of this table. For 1:M it's easy.
			// For M:M, we have to work out which end is connected to the real
			// table, then process the table at the other end of the relation.
			for (int i = 0; i < dimensionQ.size(); i++) {
				final Object[] triple = (Object[]) dimensionQ.get(i);
				final List newSourceDSCols = (List) triple[0];
				final Relation dimensionRelation = (Relation) triple[1];
				final int iteration = ((Integer) triple[2]).intValue();
				if (dimensionRelation.isOneToMany())
					this.generateDataSetTable(DataSetTableType.DIMENSION,
							dsTable, dimensionRelation.getManyKey().getTable(),
							skippedMainTables, newSourceDSCols,
							dimensionRelation, subclassCount, iteration,
							unusedTables);
				else
					this.generateDataSetTable(DataSetTableType.DIMENSION,
							dsTable, dimensionRelation.getFirstKey().getTable()
									.equals(realTable) ? dimensionRelation
									.getSecondKey().getTable()
									: dimensionRelation.getFirstKey()
											.getTable(), skippedMainTables,
							newSourceDSCols, dimensionRelation, subclassCount,
							iteration, unusedTables);
			}
		}
	}

	/**
	 * This method takes a real table and merges it into a dataset table. It
	 * does this by creating {@link WrappedColumn} instances for each new column
	 * it finds in the table.
	 * <p>
	 * If a source relation was specified, columns in the key in the table that
	 * is part of that source relation are ignored, else they'll get duplicated.
	 * 
	 * @param dsTable
	 *            the dataset table we are constructing and should merge the
	 *            columns into.
	 * @param dsTablePKCols
	 *            the primary key columns of that table. If we find we need to
	 *            add to these, we should add to this list directly.
	 * @param mergeTable
	 *            the real table we are about to merge columns from.
	 * @param normalQ
	 *            the queue to add further real tables into that we find need
	 *            merging into this same dataset table.
	 * @param subclassQ
	 *            the queue to add starting points for subclass tables that we
	 *            find.
	 * @param dimensionQ
	 *            the queue to add starting points for dimension tables we find.
	 * @param sourceRelation
	 *            the real relation we followed to reach this table.
	 * @param relationCount
	 *            how many times we have left to follow each relation, so that
	 *            we don't follow them too often.
	 * @param subclassCount
	 *            how many times we have followed a particular subclass
	 *            relation.
	 * @param makeDimensions
	 *            <tt>true</tt> if we should add potential dimension tables to
	 *            the dimension queue, <tt>false</tt> if we should just ignore
	 *            them. This is useful for preventing dimensions from gaining
	 *            dimensions of their own.
	 * @param nameCols
	 *            the list of partition columns to prefix the new dataset
	 *            columns with.
	 * @param queuePos
	 *            this position in the queue to insert the next steps at.
	 * @param skippedMainTables
	 *            the main tables to skip when processing subclasses and
	 *            dimensions.
	 * @param mergeTheseRelationsInstead
	 *            ignore all other rules and merge these ones, and don't fire
	 *            dimensions on any of these either.

	 */
	private void processTable(final TransformationUnit previousUnit,
			final DataSetTable dsTable, final List dsTablePKCols,
			final Table mergeTable, final List normalQ, final List subclassQ,
			final List dimensionQ, final List sourceDataSetCols,
			final Relation sourceRelation, final Map relationCount,
			final Map subclassCount, final boolean makeDimensions,
			final List nameCols, final List nameColSuffixes,
			final int relationIteration, int queuePos,
			final Collection unusedCols, final Map uniqueBases,
			final Collection skippedMainTables, final Map tableTracker,
			final Collection mergeTheseRelationsInstead) {
		Log.debug("Processing table " + mergeTable);

		// Remember the schema.
		this.includedSchemas.add(mergeTable.getSchema());

		// Don't ignore any keys by default.
		final Set ignoreCols = new HashSet();

		final TransformationUnit tu;

		int tableTrackerCount = 0;

		// Count the table.
		if (tableTracker.containsKey(mergeTable))
			tableTrackerCount = ((Integer) tableTracker.get(mergeTable))
					.intValue() + 1;
		tableTracker.put(mergeTable, new Integer(tableTrackerCount));

		// Did we get here via somewhere else?
		if (sourceRelation != null) {
			// Work out what key to ignore by working out at which end
			// of the relation we are.
			final Key ignoreKey = sourceRelation.getKeyForTable(mergeTable);
			ignoreCols.addAll(Arrays.asList(ignoreKey.getColumns()));
			final Key mergeKey = sourceRelation.getOtherKey(ignoreKey);

			// Add the relation and key to the list that the table depends on.
			// This list is what defines the path required to construct
			// the DDL for this table.
			tu = new JoinTable(previousUnit, mergeTable, sourceDataSetCols,
					mergeKey, sourceRelation, relationIteration);

			// Remember we've been here.
			this.includedRelations.add(sourceRelation);
			dsTable.includedRelations.add(sourceRelation);
		} else
			tu = new SelectFromTable(mergeTable);
		this.includedTables.add(mergeTable);
		dsTable.includedTables.add(mergeTable);
		dsTable.includedSchemas.add(mergeTable.getSchema());

		dsTable.addTransformationUnit(tu);

		// Work out the merge table's PK.
		final PrimaryKey mergeTablePK = mergeTable.getPrimaryKey();

		// We must merge only the first PK we come across, if this is
		// a main table, or the first PK we come across after the
		// inherited PK, if this is a subclass. Dimensions dont get
		// merged at all.
		boolean includeMergeTablePK = mergeTablePK != null
				&& !mergeTablePK.getStatus().equals(
						ComponentStatus.INFERRED_INCORRECT)
				&& !dsTable.getType().equals(DataSetTableType.DIMENSION);
		if (includeMergeTablePK && sourceRelation != null)
			// Only add further PK columns if the relation did NOT
			// involve our PK and was NOT 1:1.
			includeMergeTablePK = dsTablePKCols.isEmpty()
					&& !sourceRelation.isOneToOne()
					&& !sourceRelation.getFirstKey().equals(mergeTablePK)
					&& !sourceRelation.getSecondKey().equals(mergeTablePK);

		// Make a list of all columns involved in keys on the merge table.
		final Set colsUsedInKeys = new HashSet();
		for (final Iterator i = mergeTable.getKeys().iterator(); i.hasNext();)
			colsUsedInKeys.addAll(Arrays.asList(((Key) i.next()).getColumns()));

		// Add all columns from merge table to dataset table, except those in
		// the ignore key.
		for (final Iterator i = mergeTable.getColumns().values().iterator(); i
				.hasNext();) {
			final Column c = (Column) i.next();

			// Ignore those in the key used to get here.
			if (ignoreCols.contains(c))
				continue;

			// Create a name for this column.
			String internalColName = c.getName();
			String visibleColName = c.getName();
			// Add the unique suffix to the visible col name.
			visibleColName = visibleColName + "_"
					+ mergeTable.getSchema().getUniqueId() + "0"
					+ mergeTable.getUniqueId();
			// Expand to full-length by prefixing relation
			// info, and relation tracker info. Note we use
			// the tracker not the iteration as this gives us
			// a unique repetition number.
			internalColName = mergeTable.getSchema().getUniqueId() + "."
					+ mergeTable.getUniqueId() + "." + tableTrackerCount + "."
					+ internalColName;
			// Add partitioning prefixes.
			for (int k = 0; k < nameCols.size(); k++) {
				final String pcolName = (String) nameCols.get(k);
				final String suffix = "#" + (String) nameColSuffixes.get(k);
				internalColName = pcolName + suffix
						+ Resources.get("columnnameSep") + internalColName;
				visibleColName = pcolName + suffix
						+ Resources.get("columnnameSep") + visibleColName;
			}
			// Rename all PK columns to have the '_key' suffix.
			// otherwise, column will be added but not as _key
			if (includeMergeTablePK
					&& Arrays.asList(mergeTablePK.getColumns()).contains(c)
					&& !visibleColName.endsWith(Resources.get("keySuffix")))
				visibleColName = visibleColName + Resources.get("keySuffix");
			// Reuse or create new wrapped column?
			WrappedColumn wc = null;
			boolean reusedColumn = false;
			// Don't reuse cols that will be part of the PK.
			if (dsTable.getColumns().containsKey(internalColName)) {
				final DataSetColumn candDSCol = (DataSetColumn) dsTable
						.getColumns().get(internalColName);
				if (candDSCol instanceof WrappedColumn) {
					wc = (WrappedColumn) dsTable.getColumns().get(
							internalColName);
					reusedColumn = true;
				}
			}
						
			if (!reusedColumn) {
				// Create new column using unique name.
				wc = new WrappedColumn(c, internalColName, dsTable);
				dsTable.getColumns().put(wc.getName(), wc);
				// Insert column rename using origColName, but
				// only if one not already specified (e.g. from
				// XML file).
				
				try {
					
					if (wc.getColumnRename() == null)
						wc.setColumnRename(visibleColName, false);		
				} catch (final ValidationException ve) {
					// Should never happen!
					throw new BioMartError(ve);
				}
				
				// Listen to this column to modify ourselves.
				if (!dsTable.getType().equals(DataSetTableType.DIMENSION))
					wc.addPropertyChangeListener("columnRename",
							this.rebuildListener);
			}
			
			unusedCols.remove(wc);
			tu.getNewColumnNameMap().put(c, wc);
			wc.setTransformationUnit(tu);
			wc.setPartitionCols(nameCols);

			// If the column is in any key on this table then it is a
			// dependency for possible future linking, which must be
			// flagged.
			wc.setKeyDependency(colsUsedInKeys.contains(c));

			// If the column was in the merge table's PK, and we are
			// expecting to add the PK to the generated table's PK, then
			// add it to the generated table's PK.
			if (includeMergeTablePK
					&& Arrays.asList(mergeTablePK.getColumns()).contains(c))
				dsTablePKCols.add(wc);
		}

		// Update the three queues with relations that lead away from this
		// table.
		final List mergeRelations = new ArrayList(mergeTable.getRelations());
		Collections.sort(mergeRelations);
		for (int i = 0; i < mergeRelations.size(); i++) {
			final Relation r = (Relation) mergeRelations.get(i);

			// Allow to go back up sourceRelation if it is a loopback
			// 1:M relation and we have just merged the 1 end.
			final boolean isLoopback = r.getLoopbackRelation(this, dsTable
					.getName()) != null
					&& r.getOneKey().equals(r.getKeyForTable(mergeTable));
			final boolean isFirstLoopback = isLoopback
					&& !dsTable.includedRelations.contains(r);

			// Don't go back up same relation unless we are doing
			// loopback. If we are doing loopback, do source relation last
			// by adding it to the end of the queue and skipping it this
			// time round.
			if (r.equals(sourceRelation)) {
				if (!isLoopback)
					continue;
				if (i < mergeRelations.size() - 1) {
					mergeRelations.add(r);
					continue;
				}
			}

			// If just come down a 1:1, don't go back up another 1:1
			// to same table.
			if (sourceRelation != null && sourceRelation.isOneToOne()
					&& r.isOneToOne()) {
				final Set keys = new HashSet();
				keys.add(r.getFirstKey().getTable());
				keys.add(r.getSecondKey().getTable());
				keys.remove(sourceRelation.getFirstKey().getTable());
				keys.remove(sourceRelation.getSecondKey().getTable());
				if (keys.isEmpty())
					continue;
			}

			// Don't excessively repeat relations.
			if (((Integer) relationCount.get(r)).intValue() <= 0)
				continue;

			// Don't follow incorrect relations, or relations
			// between incorrect keys.
			if (r.getStatus().equals(ComponentStatus.INFERRED_INCORRECT)
					|| r.getFirstKey().getStatus().equals(
							ComponentStatus.INFERRED_INCORRECT)
					|| r.getSecondKey().getStatus().equals(
							ComponentStatus.INFERRED_INCORRECT))
				continue;

			// Don't follow relations to ignored tables.
			if (r.getOtherKey(r.getKeyForTable(mergeTable)).getTable()
					.isMasked())
				continue;

			// Don't follow relations back to skipped mains unless
			// they have been forced.
			if (skippedMainTables.contains(r.getOtherKey(
					r.getKeyForTable(mergeTable)).getTable())
					&& !(r.isForceRelation(this, dsTable.getName()) || r
							.isSubclassRelation(this))
					&& !mergeTheseRelationsInstead.contains(r))
				continue;

			// Don't follow relations to masked schemas.
			if (r.getOtherKey(r.getKeyForTable(mergeTable)).getTable()
					.getSchema().isMasked())
				continue;

			// Don't follow masked relations.
			// NB. This is last so that only masked relations show
			// up, not those skipped for other reasons.
			if (r.isMaskRelation(this, dsTable.getName())) {
				// Make a fake SKIP table unit to show what
				// might still be possible for the user.
				final Key skipKey = r.getKeyForTable(mergeTable);
				final List newSourceDSCols = new ArrayList();
				for (int j = 0; j < skipKey.getColumns().length; j++) {
					final DataSetColumn col = tu.getDataSetColumnFor(skipKey
							.getColumns()[j]);
					newSourceDSCols.add(col);
				}
				final SkipTable stu = new SkipTable(tu, skipKey.getTable(),
						newSourceDSCols, skipKey, r, ((Integer) relationCount
								.get(r)).intValue());
				dsTable.addTransformationUnit(stu);
				continue;
			}

			// Decrement the relation counter.
			relationCount.put(r, new Integer(((Integer) relationCount.get(r))
					.intValue() - 1));

			// Set up a holder to indicate whether or not to follow
			// the relation.
			boolean followRelation = false;
			boolean forceFollowRelation = false;

			// Are we at the 1 end of a 1:M?
			// If so, we may need to make a dimension, or a subclass.
			if (r.isOneToMany() && r.getOneKey().getTable().equals(mergeTable)) {

				// Subclass subclassed relations, if we are currently
				// not building a dimension table.
				if (!mergeTheseRelationsInstead.contains(r)
						&& r.isSubclassRelation(this)
						&& !dsTable.getType()
								.equals(DataSetTableType.DIMENSION)) {
					final List newSourceDSCols = new ArrayList();
					for (int j = 0; j < r.getOneKey().getColumns().length; j++)
						newSourceDSCols.add(tu.getDataSetColumnFor(r
								.getOneKey().getColumns()[j]));
					// Deal with recursive subclasses.
					final int nextSC = subclassCount.containsKey(r) ? ((Integer) subclassCount
							.get(r)).intValue() + 1
							: 0;
					subclassCount.put(r, new Integer(nextSC));
					// Only do this if the subclassCount is less than
					// the maximum allowed.
					final CompoundRelationDefinition def = r
							.getCompoundRelation(this, dsTable.getName());
					final int childCompounded = def == null ? 1 : def.getN();
					if (nextSC < childCompounded)
						subclassQ.add(new Object[] { newSourceDSCols, r,
								new Integer(nextSC) });
				}

				// Dimensionize dimension relations, which are all other 1:M
				// relations, if we are not constructing a dimension
				// table, and are currently intending to construct
				// dimensions.
				else if (!mergeTheseRelationsInstead.contains(r)
						&& makeDimensions
						&& !dsTable.getType()
								.equals(DataSetTableType.DIMENSION)) {
					final List newSourceDSCols = new ArrayList();
					for (int j = 0; j < r.getOneKey().getColumns().length; j++) {
						final DataSetColumn newCol = tu.getDataSetColumnFor(r
								.getOneKey().getColumns()[j]);
						newSourceDSCols.add(newCol);
					}
					int childCompounded = 1;
					final CompoundRelationDefinition def = r
							.getCompoundRelation(this, dsTable.getName());
					if (def != null && def.isParallel())
						childCompounded = def.getN();
					// Follow the relation.
					for (int k = 0; k < childCompounded; k++)
						dimensionQ.add(new Object[] { newSourceDSCols, r,
								new Integer(k) });
					if (r.isMergeRelation(this)
							|| r.getUnrolledRelation(this) != null)
						forceFollowRelation = true;
				}

				// Forcibly follow forced or loopback or unrolled relations.
				else if (mergeTheseRelationsInstead.contains(r)
						|| r.getLoopbackRelation(this, dsTable.getName()) != null
						|| r.isForceRelation(this, dsTable.getName()))
					forceFollowRelation = true;
			}

			// Follow all others. Don't follow relations that are
			// already in the subclass or dimension queues.
			else
				followRelation = mergeTheseRelationsInstead.isEmpty()
						|| mergeTheseRelationsInstead.contains(r);

			// If we follow a 1:1, and we are currently
			// including dimensions, include them from the 1:1 as well.
			// Otherwise, stop including dimensions on subsequent tables.
			if (followRelation || forceFollowRelation) {
				final List nextNameCols = new ArrayList(nameCols);
				final Map nextNameColSuffixes = new HashMap();
				nextNameColSuffixes.put("" + 0, new ArrayList(nameColSuffixes));

				final Key sourceKey = r.getKeyForTable(mergeTable);
				final Key targetKey = r.getOtherKey(sourceKey);
				final List newSourceDSCols = new ArrayList();
				for (int j = 0; j < sourceKey.getColumns().length; j++)
					newSourceDSCols.add(tu.getDataSetColumnFor(sourceKey
							.getColumns()[j]));
				// Repeat queueing of relation N times if compounded.
				int defaultChildCompounded = isFirstLoopback ? 2 : 1;
				int childCompounded = defaultChildCompounded;
				// Don't compound if loopback and we just processed the M end.
				final CompoundRelationDefinition def = r.getCompoundRelation(
						this, dsTable.getName());
				final boolean skipCompound = r.getLoopbackRelation(this,
						dsTable.getName()) != null
						&& r.getManyKey().equals(r.getKeyForTable(mergeTable));
				if (!skipCompound) {
					if (def != null && def.isParallel())
						childCompounded = def.getN();
					else {
						// Work out partition compounding. Table
						// applies within a dimension, where dataset does
						// not apply, but outside a dimension only dataset
						// applies.

							childCompounded = defaultChildCompounded;
					}
				}
				for (int k = 1; k <= childCompounded; k++) {
					final List nextList = new ArrayList(nameColSuffixes);
					nextList.add("" + k);
					nextNameColSuffixes.put("" + k, nextList);
				}
				// Insert first one at next position in queue
				// after current position. This creates multiple
				// top-down paths, rather than sideways-spanning trees of
				// actions. (If this queue were a graph, doing it this way
				// makes it depth-first as opposed to breadth-first).
				// The queue position is incremented so that they remain
				// in order - else they'd end up reversed.
				if (childCompounded > 1)
					for (int k = 0; k < childCompounded; k++)
						normalQ.add(queuePos++, new Object[] {
								r,
								newSourceDSCols,
								targetKey.getTable(),
								tu,
								Boolean.valueOf(makeDimensions
										&& r.isOneToOne()
										|| forceFollowRelation),
								new Integer(k), nextNameCols,
								nextNameColSuffixes.get("" + k),
								new HashMap(relationCount) });
				else
					normalQ.add(queuePos++, new Object[] {
							r,
							newSourceDSCols,
							targetKey.getTable(),
							tu,
							Boolean.valueOf(makeDimensions && r.isOneToOne()
									|| forceFollowRelation),
							new Integer(relationIteration), nextNameCols,
							nextNameColSuffixes.get("0"),
							new HashMap(relationCount) });
			}
		}
	}

	/**
	 * Returns the central table of this dataset.
	 * 
	 * @return the central table of this dataset.
	 */
	public Table getCentralTable() {
		return this.centralTable;
	}

	/**
	 * Follows subclassed relations to find where transformation should really
	 * start for this dataset.
	 * 
	 * @return the real central table.
	 */
	public Table getRealCentralTable() {
		Log.debug("Finding actual central table");
		// Identify main table.
		final Table realCentralTable = this.getCentralTable();
		Table centralTable = realCentralTable;
		// If central table has subclass relations and is at the M key
		// end, then follow them to the real central table.
		boolean found;
		do {
			found = false;
			for (final Iterator i = centralTable.getForeignKeys().iterator(); i
					.hasNext()
					&& !found;)
				for (final Iterator j = ((ForeignKey) i.next()).getRelations()
						.iterator(); j.hasNext() && !found;) {
					final Relation rel = (Relation) j.next();
					if (rel.isSubclassRelation(this)) {
						centralTable = rel.getOneKey().getTable();
						found = true;
					}
				}
		} while (found && centralTable != realCentralTable);
		Log.debug("Actual central table is " + centralTable);
		return centralTable;
	}

	/**
	 * Returns the central table of this dataset. If it currently doesn't have
	 * one it will return null.
	 * 
	 * @return the central table of this dataset.
	 */
	public DataSetTable getMainTable() {
		for (final Iterator i = this.getTables().values().iterator(); i
				.hasNext();) {
			final DataSetTable dst = (DataSetTable) i.next();
			if (dst.getType().equals(DataSetTableType.MAIN))
				return dst;
		}
		return null;
	}

	/**
	 * Returns the post-creation optimiser type this dataset will use.
	 * 
	 * @return the optimiser type that will be used.
	 */
	public DataSetOptimiserType getDataSetOptimiserType() {
		return this.optimiser;
	}

	/**
	 * Sees if the optimiser will index its columns.
	 * 
	 * @return <tt>true</tt> if it will.
	 */
	public boolean isIndexOptimiser() {
		return this.indexOptimiser;
	}

	/**
	 * Test to see if this dataset is invisible.
	 * 
	 * @return <tt>true</tt> if it is invisible, <tt>false</tt> otherwise.
	 */
	public boolean isInvisible() {
		return this.invisible;
	}

	/**
	 * Sets the post-creation optimiser type this dataset will use.
	 * 
	 * @param optimiser
	 *            the optimiser type to use.
	 */
	public void setDataSetOptimiserType(final DataSetOptimiserType optimiser) {
		Log.debug("Setting optimiser to " + optimiser + " in " + this);
		final DataSetOptimiserType oldValue = this.optimiser;
		if (oldValue.equals(optimiser))
			return;
		this.optimiser = optimiser;
		this.pcs.firePropertyChange("optimiserType", oldValue, optimiser);
	}

	/**
	 * Sets the optimiser index type.
	 * 
	 * @param index
	 *            the optimiser index if <tt>true</tt>.
	 */
	public void setIndexOptimiser(final boolean index) {
		Log.debug("Setting optimiser index to " + index + " in " + this);
		final boolean oldValue = this.indexOptimiser;
		if (oldValue == index)
			return;
		this.indexOptimiser = index;
		this.pcs.firePropertyChange("indexOptimiser", oldValue, index);
	}

	/**
	 * Sets the inivisibility of this dataset.
	 * 
	 * @param invisible
	 *            <tt>true</tt> if it is invisible, <tt>false</tt>
	 *            otherwise.
	 */
	public void setInvisible(final boolean invisible) {
		Log.debug("Setting invisible flag to " + invisible + " in " + this);
		final boolean oldValue = this.invisible;
		if (oldValue == invisible)
			return;
		this.invisible = invisible;
		this.pcs.firePropertyChange("invisible", oldValue, invisible);
	}

	/**
	 * Synchronise this dataset with the schema that is providing its tables.
	 * Synchronisation means checking the columns and relations and removing any
	 * that have disappeared. The dataset is then regenerated. After
	 * regeneration, any customisations to the dataset such as partitioning are
	 * reapplied to columns which match the original names of the columns from
	 * before regeneration.
	 * 
	 * @throws SQLException
	 *             never thrown - this is inherited from {@link Schema} but does
	 *             not apply here because we are not doing any database
	 *             communications.
	 * @throws DataModelException
	 *             never thrown - this is inherited from {@link Schema} but does
	 *             not apply here because we are not attempting any new logic
	 *             with the schema.
	 */
	public void synchronise() throws SQLException, DataModelException {
		Log.debug("Regenerating dataset " + this.getName());
		super.synchronise();

		// Empty out used rels and schs.
		this.includedRelations.clear();
		this.includedSchemas.clear();
		this.includedTables.clear();

		// Get the real main table.
		final Table realCentralTable = this.getRealCentralTable();

		// Work out the main tables to skip whilst transforming.
		final List skippedTables = new ArrayList();
		skippedTables.add(realCentralTable);
		// the size may changed
		for (int i = 0; i < skippedTables.size(); i++) {
			final Table cand = (Table) skippedTables.get(i);
			if (cand.getPrimaryKey() != null)
				for (final Iterator j = cand.getPrimaryKey().getRelations()
						.iterator(); j.hasNext();) {
					final Relation rel = (Relation) j.next();
					if (rel.isSubclassRelation(this))
						skippedTables.add(rel.getManyKey().getTable());
				}
		}

		// Make a list of all table names.
		final Collection unusedTables = new HashSet(this.getTables().values());
		try {
			// Generate the main table. It will recursively generate all the
			// others.
			this.generateDataSetTable(DataSetTableType.MAIN, null,
					realCentralTable, skippedTables, Collections.EMPTY_LIST,
					null, new HashMap(), 0, unusedTables);
		} catch (final Exception pe) {
			throw new DataModelException(pe);
		}

		// Drop any rels from tables still in list, then drop tables too.
		for (final Iterator i = unusedTables.iterator(); i.hasNext();) {
			final Table deadTbl = (Table) i.next();
			for (final Iterator j = deadTbl.getKeys().iterator(); j.hasNext();) {
				final Key key = (Key) j.next();
				for (final Iterator r = key.getRelations().iterator(); r
						.hasNext();) {
					final Relation rel = (Relation) r.next();
					rel.getFirstKey().getRelations().remove(rel);
					rel.getSecondKey().getRelations().remove(rel);
				}
			}
			deadTbl.setPrimaryKey(null);
			deadTbl.getForeignKeys().clear();
			this.getTables().remove(deadTbl.getName());
			this.mods.remove(deadTbl.getName());
		}

		// Add us as a listener to all included rels and schs, replacing
		// ourselves if we are already listening to them.
		for (final Iterator i = this.includedSchemas.iterator(); i.hasNext();) {
			final Schema sch = (Schema) i.next();
			sch.addPropertyChangeListener("masked", this.rebuildListener);
		}
		// Gather up the tables we have used and those linked to them.
		final Set listeningTables = new HashSet();
		for (final Iterator i = this.includedRelations.iterator(); i.hasNext();) {
			final Relation rel = (Relation) i.next();
			// Don't bother listening to tables at end of incorrect
			// relations - but those that are excluded because of
			// non-relation-related reasons can be listened to.
			if (!rel.getStatus().equals(ComponentStatus.INFERRED_INCORRECT)) {
				listeningTables.add(rel.getFirstKey().getTable());
				listeningTables.add(rel.getSecondKey().getTable());
			}
		}
		// Listen to the tables and their children.
		final Set listeningRels = new HashSet();
		for (final Iterator i = listeningTables.iterator(); i.hasNext();) {
			final Table tbl = (Table) i.next();
			listeningRels.addAll(tbl.getRelations());
			// Listen only to useful things.
			tbl.addPropertyChangeListener("masked", this.rebuildListener);
			tbl
					.addPropertyChangeListener("restrictTable",
							this.rebuildListener);
			tbl.addPropertyChangeListener("transformStart",
					this.rebuildListener);
			tbl.getColumns().addPropertyChangeListener(this.rebuildListener);
			tbl.getRelations().addPropertyChangeListener(this.rebuildListener);
		}
		// Listen to useful bits of the relation.
		for (final Iterator i = listeningRels.iterator(); i.hasNext();) {
			final Relation rel = (Relation) i.next();
			rel.addPropertyChangeListener("cardinality", this.rebuildListener);
			rel.addPropertyChangeListener("status", this.rebuildListener);
			rel.addPropertyChangeListener("compoundRelation",
					this.rebuildListener);
			rel.addPropertyChangeListener("unrolledRelation",
					this.rebuildListener);
			rel
					.addPropertyChangeListener("forceRelation",
							this.rebuildListener);
			rel.addPropertyChangeListener("loopbackRelation",
					this.rebuildListener);
			rel.addPropertyChangeListener("maskRelation", this.rebuildListener);
			rel
					.addPropertyChangeListener("mergeRelation",
							this.rebuildListener);
			rel.addPropertyChangeListener("restrictRelation",
					this.rebuildListener);
			rel.addPropertyChangeListener("subclassRelation",
					this.rebuildListener);
		}

		// Check all visibleModified type/key pairs for
		// all vismod relations, keys, and columns. Update, then remove.
		for (final Iterator i = this.getRelations().iterator(); i.hasNext();) {
			final Relation rel = (Relation) i.next();
			final String key = rel.toString();
			if (this.getMods(key, "visibleModified").containsKey(key))
				rel.setVisibleModified(true);
			this.mods.remove(key);
		}
		for (final Iterator i = this.getTables().values().iterator(); i
				.hasNext();) {
			final Table tbl = (Table) i.next();
			for (final Iterator j = tbl.getKeys().iterator(); j.hasNext();) {
				final Key k = (Key) j.next();
				final String key = k.toString();
				if (this.getMods(key, "visibleModified").containsKey(key))
					k.setVisibleModified(true);
				this.mods.remove(key);
			}
			for (final Iterator j = tbl.getColumns().values().iterator(); j
					.hasNext();) {
				final Column col = (Column) j.next();
				final String key = col.toString();
				if (this.getMods(key, "visibleModified").containsKey(key))
					col.setVisibleModified(true);
				this.mods.remove(key);
			}
		}
		Log.info("done synchronize");
		//forward message to controller
		McEventObject mcObject = new McEventObject(EventType.Synchronize_Dataset, this);
		McViews.getInstance().getView(IdwViewType.SCHEMA).getController().processV2Cupdate(mcObject);
	}

	public boolean isMasked() {
		// Is the table or schema ignored?
		if(this.centralTable==null)
			return true;
		if (this.centralTable.isMasked()
				|| this.centralTable.getSchema().isMasked())
			return true;
		else
			return super.isMasked();
	}

	/**
	 * Find out what schemas are used in this dataset, in the order they were
	 * used.
	 * 
	 * @return the set of schemas used.
	 */
	public Collection getIncludedSchemas() {
		return this.includedSchemas;
	}

}
