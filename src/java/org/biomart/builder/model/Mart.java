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

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import org.biomart.builder.exceptions.ConstructorException;
import org.biomart.builder.exceptions.ValidationException;
import org.biomart.common.exceptions.DataModelException;
import org.biomart.common.resources.Log;
import org.biomart.common.resources.Resources;
import org.biomart.common.view.gui.SwingWorker;
import org.biomart.common.view.gui.dialogs.ProgressDialog;
import org.biomart.common.view.gui.dialogs.StackTrace;
import org.biomart.configurator.controller.MartConstructor.ConstructorRunnable;
import org.biomart.configurator.model.Location;
import org.biomart.configurator.utils.type.MartType;
import org.biomart.configurator.utils.type.CaseType;

/**
 * The mart contains the set of all schemas that are providing data to this
 * mart. It also has zero or more datasets based around these.
 * 
 * @author Richard Holland <holland@ebi.ac.uk>
 * @version $Revision: 1.90 $, $Date: 2008/03/03 12:16:15 $, modified by
 *          $Author: rh4 $
 * @since 0.5
 */
public class Mart {

	private Schemas schemasObj;
	private DataSets datasetsObj;
	//information of old MB
	private String outputDatabase = null;
	private String outputSchema = null;
	private String outputHost = null;
	private String outputPort = null;
	private String overrideHost = null;
	private String overridePort = null;
	private boolean hideMaskedDataSets = false;
	private boolean hideMaskedSchemas = false;
	//hack for marteditor, should be in the dataset
	private List<String> mainTableList;
	private CaseType nameCase = CaseType.MIXED;
	// For use in hash code and equals to prevent dups in prop change.
	private static int ID_SERIES = 0;
	private final int uniqueID = Mart.ID_SERIES++;
	private Location location;	
	private String name;
	private MartType martType; 



	/**
	 * Construct a new, empty, mart.
	 */
	public Mart(Location location, String name, MartType type) {
		Log.debug("Creating new mart");
		this.location = location;
		this.name = name;
		this.martType = type;
		this.datasetsObj = new DataSets(this);
		this.schemasObj = new Schemas(this);
		
	}
	
	public Schemas getSchemasObj() {
		return this.schemasObj;
	}
	
	public DataSets getDataSetObj() {
		return this.datasetsObj;
	}
	
	public DataSet getDataSet(String name) {
		return this.datasetsObj.getDataSet(name);
	}

	public Location getMartLocation() {
		return this.location;
	}
	
	public String getMartName() {
		return this.name;
	}
	
	public void setMartName(String value) {
		this.name = value;
	}
	/**
	 * Obtain the next unique ID to use for a schema.
	 * 
	 * @return the next ID.
	 */
	public int getNextUniqueId() {
		int x = 0;
		for (final Iterator i = this.schemasObj.getSchemas().values().iterator(); i.hasNext();)
			x = Math.max(x, ((Schema) i.next()).getUniqueId());
		return x + 1;
	}

	/**
	 * Obtain the unique series number for this mart.
	 * 
	 * @return the unique Id.
	 */
	public int getUniqueId() {
		return this.uniqueID;
	}

	public int hashCode() {
		return 0; // All marts go in one big bucket!
	}

	public boolean equals(final Object obj) {
		if (obj == this)
			return true;
		else if (obj == null)
			return false;
		else if (obj instanceof Mart)
			return this.uniqueID == ((Mart) obj).uniqueID;
		else
			return false;
	}

	/**
	 * Is this mart hiding masked datasets?
	 * 
	 * @param hideMaskedDataSets
	 *            true if it is.
	 */
	public void setHideMaskedDataSets(final boolean hideMaskedDataSets) {
		final boolean oldValue = this.hideMaskedDataSets;
		if (this.hideMaskedDataSets == hideMaskedDataSets)
			return;
		this.hideMaskedDataSets = hideMaskedDataSets;
	}

	/**
	 * Is this mart hiding masked datasets?
	 * 
	 * @return true if it is.
	 */
	public boolean isHideMaskedDataSets() {
		return this.hideMaskedDataSets;
	}

	/**
	 * Is this mart hiding masked schemas?
	 * 
	 * @param hideMaskedSchemas
	 *            true if it is.
	 */
	public void setHideMaskedSchemas(final boolean hideMaskedSchemas) {
		final boolean oldValue = this.hideMaskedSchemas;
		if (this.hideMaskedSchemas == hideMaskedSchemas)
			return;
		this.hideMaskedSchemas = hideMaskedSchemas;
	}

	/**
	 * Is this mart hiding masked schemas?
	 * 
	 * @return true if it is.
	 */
	public boolean isHideMaskedSchemas() {
		return this.hideMaskedSchemas;
	}


	/**
	 * What case to use for table and column names? Mixed is default.
	 * 
	 * @return one of {@link #USE_LOWER_CASE}, {@link #USE_UPPER_CASE}, or
	 *         {@link #USE_MIXED_CASE}.
	 */
	public CaseType getCase() {
		return this.nameCase;
	}

	/**
	 * What case to use for table and column names? Mixed is default.
	 * 
	 * @param nameCase
	 *            one of {@link #USE_LOWER_CASE}, {@link #USE_UPPER_CASE}, or
	 *            {@link #USE_MIXED_CASE}.
	 */
	public void setCase(final CaseType nameCase) {
		Log.debug("Changing case for " + this + " to " + nameCase);
		final CaseType oldValue = this.nameCase;
		if (this.nameCase == nameCase)
			return;
		// Make the change.
		this.nameCase = nameCase;
	}

	/**
	 * Optional, sets the default target schema this mart will output dataset
	 * DDL to later.
	 * 
	 * @param outputSchema
	 *            the target schema.
	 */
	public void setOutputSchema(final String outputSchema) {
		Log.debug("Changing outputSchema for " + this + " to " + outputSchema);
		final String oldValue = this.outputSchema;
		if (this.outputSchema == outputSchema || this.outputSchema != null
				&& this.outputSchema.equals(outputSchema))
			return;
		// Make the change.
		this.outputSchema = outputSchema;
	}

	/**
	 * Optional, gets the default target schema this mart will output dataset
	 * DDL to later.
	 * 
	 * @return the target schema.
	 */
	public String getOutputSchema() {
		return this.outputSchema;
	}

	/**
	 * Optional, sets the default target database this mart will output dataset
	 * DDL to later.
	 * 
	 * @param outputDatabase
	 *            the target database.
	 */
	public void setOutputDatabase(final String outputDatabase) {
		Log.debug("Changing outputDatabase for " + this + " to "
				+ outputDatabase);
		final String oldValue = this.outputDatabase;
		if (this.outputDatabase == outputDatabase
				|| this.outputDatabase != null
				&& this.outputDatabase.equals(outputDatabase))
			return;
		// Make the change.
		this.outputDatabase = outputDatabase;
	}

	/**
	 * Optional, gets the default target database this mart will output dataset
	 * DDL to later.
	 * 
	 * @return the target schema.
	 */
	public String getOutputDatabase() {
		return this.outputDatabase;
	}

	/**
	 * Optional, sets the default target host this mart will output dataset DDL
	 * to later.
	 * 
	 * @param outputHost
	 *            the target host.
	 */
	public void setOutputHost(final String outputHost) {
		Log.debug("Changing outputHost for " + this + " to " + outputHost);
		final String oldValue = this.outputHost;
		if (this.outputHost == outputHost || this.outputHost != null
				&& this.outputHost.equals(outputHost))
			return;
		// Make the change.
		this.outputHost = outputHost;
	}

	/**
	 * Optional, gets the default target host this mart will output dataset DDL
	 * to later.
	 * 
	 * @return the target host.
	 */
	public String getOutputHost() {
		return this.outputHost;
	}

	/**
	 * Optional, sets the default target port this mart will output dataset DDL
	 * to later.
	 * 
	 * @param outputPort
	 *            the target port.
	 */
	public void setOutputPort(final String outputPort) {
		Log.debug("Changing outputPort for " + this + " to " + outputPort);
		if (this.outputPort == outputPort || this.outputPort != null
				&& this.outputPort.equals(outputPort))
			return;
		// Make the change.
		this.outputPort = outputPort;
	}

	/**
	 * Optional, gets the default target port this mart will output dataset DDL
	 * to later.
	 * 
	 * @return the target port.
	 */
	public String getOutputPort() {
		return this.outputPort;
	}

	/**
	 * Optional, sets the default target JDBC host this mart will output dataset
	 * DDL to later.
	 * 
	 * @param overrideHost
	 *            the target host.
	 */
	public void setOverrideHost(final String overrideHost) {
		Log.debug("Changing overrideHost for " + this + " to " + overrideHost);
		if (this.overrideHost == overrideHost || this.overrideHost != null
				&& this.overrideHost.equals(overrideHost))
			return;
		// Make the change.
		this.overrideHost = overrideHost;
	}

	/**
	 * Optional, gets the default target JDBC host this mart will output dataset
	 * DDL to later.
	 * 
	 * @return the target host.
	 */
	public String getOverrideHost() {
		return this.overrideHost;
	}

	/**
	 * Optional, sets the default target JDBC port this mart will output dataset
	 * DDL to later.
	 * 
	 * @param overridePort
	 *            the target port.
	 */
	public void setOverridePort(final String overridePort) {
		Log.debug("Changing overridePort for " + this + " to " + overridePort);
		if (this.overridePort == overridePort || this.overridePort != null
				&& this.overridePort.equals(overridePort))
			return;
		// Make the change.
		this.overridePort = overridePort;
	}

	/**
	 * Optional, gets the default target JDBC port this mart will output dataset
	 * DDL to later.
	 * 
	 * @return the target port.
	 */
	public String getOverridePort() {
		return this.overridePort;
	}

	/**
	 * Returns the set of dataset objects which this mart includes. The set may
	 * be empty but it is never <tt>null</tt>.
	 * 
	 * @return a set of dataset objects. Keys are names, values are datasets.
	 */
	public Map<String,DataSet> getDataSets() {
		if(this.datasetsObj!=null)
			return this.datasetsObj.getDataSets();
		else
			return new HashMap<String,DataSet>();
	}


	/**
	 * Returns the set of schema objects which this mart includes. The set may
	 * be empty but it is never <tt>null</tt>.
	 * 
	 * @return a set of schema objects. Keys are names, values are actual
	 *         schemas.
	 */
	public Map<String,Schema> getSchemas() {
		if(this.schemasObj!=null)
			return this.schemasObj.getSchemas();
		else 
			return new HashMap<String,Schema>();
	}

	/**
	 * Given a set of tables, produce the minimal set of datasets which include
	 * all the specified tables. Tables can be included in the same dataset if
	 * they are linked by 1:M relations (1:M, 1:M in a chain), or if the table
	 * is the last in the chain and is linked to the previous table by a pair of
	 * 1:M and M:1 relations via a third table, simulating a M:M relation.
	 * <p>
	 * If the chains of tables fork, then one dataset is generated for each
	 * branch of the fork.
	 * <p>
	 * Every suggested dataset is synchronised before being returned.
	 * <p>
	 * Datasets will be named after their central tables. If a dataset with that
	 * name already exists, a '_' and sequence number will be appended to make
	 * the new dataset name unique.
	 * <p>
	 * See also
	 * {@link #continueSubclassing(Collection, Collection, DataSet, Table)}.
	 * 
	 * @param includeTables
	 *            the tables that must appear in the final set of datasets.
	 * @return the collection of datasets generated.
	 * @throws SQLException
	 *             if there is any problem talking to the source database whilst
	 *             generating the dataset.
	 * @throws DataModelException
	 *             if synchronisation fails.
	 */
	public Collection<DataSet> suggestDataSets(final Collection<Table> includeTables)
			throws SQLException, DataModelException {
		Log.debug("Suggesting datasets for " + includeTables);
		// The root tables are all those which do not have a M:1 relation
		// to another one of the initial set of tables. This means that
		// extra datasets will be created for each table at the end of
		// 1:M:1 relation, so that any further tables past it will still
		// be included.
		Log.debug("Finding root tables");
		final Collection<Table> rootTables = new HashSet<Table>(includeTables);
		for (final Iterator<Table> i = includeTables.iterator(); i.hasNext();) {
			final Table candidate = i.next();
			for (final Iterator<Relation> j = candidate.getRelations().iterator(); j
					.hasNext();) {
				final Relation rel = (Relation) j.next();
				if (rel.getStatus().equals(ComponentStatus.INFERRED_INCORRECT))
					continue;
				if (!rel.isOneToMany())
					continue;
				if (!rel.getManyKey().getTable().equals(candidate))
					continue;
				if (includeTables.contains(rel.getOneKey().getTable()))
					rootTables.remove(candidate);
			}
		}
		// We construct one dataset per root table.
		final Set<DataSet> suggestedDataSets = new TreeSet<DataSet>();
		for (final Iterator<Table> i = rootTables.iterator(); i.hasNext();) {
			final Table rootTable = i.next();
			Log.debug("Constructing dataset for root table " + rootTable);
			final DataSet dataset;
			try {
				dataset = new DataSet(this, rootTable, rootTable.getName());
			} catch (final ValidationException e) {
				// Skip this one.
				continue;
			}
			//this.datasets.put(dataset.getOriginalName(), dataset);
			this.addDataSet(dataset);
			// Process it.
			final Collection<Table> tablesIncluded = new HashSet<Table>();
			tablesIncluded.add(rootTable);
			Log.debug("Attempting to find subclass datasets");
			suggestedDataSets.addAll(this.continueSubclassing(includeTables,
					tablesIncluded, dataset, rootTable));
		}

		// Synchronise them all.
		Log.debug("Synchronising constructed datasets");
		for (DataSet ds: suggestedDataSets)
			ds.synchronise();

		// Do any of the resulting datasets contain all the tables
		// exactly with subclass relations between each?
		// If so, just use that one dataset and forget the rest.
		Log.debug("Finding perfect candidate");
		DataSet perfectDS = null;
		for (final Iterator<DataSet> i = suggestedDataSets.iterator(); i.hasNext()
				&& perfectDS == null;) {
			final DataSet candidate = i.next();

			// A candidate is a perfect match if the set of tables
			// covered by the subclass relations is the same as the
			// original set of tables requested.
			final Collection<Table> scTables = new HashSet<Table>();
			for (final Iterator<Relation> j = candidate.getIncludedRelations().iterator(); j
					.hasNext();) {
				final Relation r = (Relation) j.next();
				if (!r.isSubclassRelation(candidate))
					continue;
				scTables.add(r.getFirstKey().getTable());
				scTables.add(r.getSecondKey().getTable());
			}
			// Finally perform the check to see if we have them all.
			if (scTables.containsAll(includeTables))
				perfectDS = candidate;
		}
		if (perfectDS != null) {
			Log.debug("Perfect candidate found - dropping others");
			// Drop the others.
			for (final Iterator<DataSet> i = suggestedDataSets.iterator(); i.hasNext();) {
				final DataSet candidate = i.next();
				if (!candidate.equals(perfectDS)) {
					this.removeDataSet(candidate);
					i.remove();
				}
			}
			// Rename it to lose any extension it may have gained.
			String newName = perfectDS.getCentralTable().getName();
			perfectDS.setName(newName);
		} else
			Log.debug("No perfect candidate found - retaining all");

		// Return the final set of suggested datasets.
		return suggestedDataSets;
	}

	/**
	 * This internal method takes a bunch of tables that the user would like to
	 * see as subclass or main tables in a single dataset, and attempts to find
	 * a subclass path between them. For each subclass path it can build, it
	 * produces one dataset based on that path. Each path contains as many
	 * tables as possible. The paths do not overlap. If there is a choice, the
	 * one chosen is arbitrary.
	 * 
	 * @param includeTables
	 *            the tables we want to include as main or subclass tables.
	 * @param tablesIncluded
	 *            the tables we have managed to include in a path so far.
	 * @param dataset
	 *            the dataset we started out from which contains just the main
	 *            table on its own with no subclassing.
	 * @param table
	 *            the real table we are looking at to see if there is a subclass
	 *            path between any of the include tables and any of the existing
	 *            subclassed or main tables via this real table.
	 * @return the datasets we have created - one per subclass path, or if there
	 *         were none, then a singleton collection containing the dataset
	 *         originally passed in.
	 */
	private Collection<DataSet> continueSubclassing(final Collection<Table> includeTables,
			final Collection<Table> tablesIncluded, final DataSet dataset,
			final Table table) {
		// Check table has a primary key.
		final Key pk = table.getPrimaryKey();

		// Make a unique set to hold all the resulting datasets. It
		// is initially empty.
		final Set<DataSet> suggestedDataSets = new HashSet<DataSet>();
		// Make a set to contain relations to subclass.
		final Set<Relation> subclassedRelations = new HashSet<Relation>();
		// Make a map to hold tables included for each relation.
		final Map<Relation,Set<Table>> relationTablesIncluded = new HashMap<Relation,Set<Table>>();
		// Make a list to hold all tables included at this level.
		final Set<Table> localTablesIncluded = new HashSet<Table>(tablesIncluded);

		// Find all 1:M relations starting from the given table that point
		// to another interesting table (includeTables).
		if (pk != null)
			for (final Iterator<Relation> i = pk.getRelations().iterator(); i.hasNext();) {
				final Relation r = (Relation) i.next();
				if (!r.isOneToMany())
					continue;
				else if (r.getStatus().equals(
						ComponentStatus.INFERRED_INCORRECT))
					continue;

				// For each relation, if it points to another included
				// table via 1:M we should subclass the relation.
				final Table target = r.getManyKey().getTable();
				if (includeTables.contains(target)
						&& !localTablesIncluded.contains(target)) {
					subclassedRelations.add(r);
					final Set<Table> newRelationTablesIncluded = new HashSet<Table>(
							tablesIncluded);
					relationTablesIncluded.put(r, newRelationTablesIncluded);
					newRelationTablesIncluded.add(target);
					localTablesIncluded.add(target);
				}
			}

		// Find all 1:M:1 relations starting from the given table that point
		// to another interesting table.
		if (pk != null)
			for (final Iterator<Relation> i = pk.getRelations().iterator(); i.hasNext();) {
				final Relation firstRel = (Relation) i.next();
				if (!firstRel.isOneToMany())
					continue;
				else if (firstRel.getStatus().equals(
						ComponentStatus.INFERRED_INCORRECT))
					continue;

				final Table intermediate = firstRel.getManyKey().getTable();
				for (final Iterator<ForeignKey> j = intermediate.getForeignKeys()
						.iterator(); j.hasNext();) {
					final Key fk = (Key) j.next();
					if (fk.getStatus().equals(
							ComponentStatus.INFERRED_INCORRECT))
						continue;
					for (final Iterator<Relation> k = fk.getRelations().iterator(); k
							.hasNext();) {
						final Relation secondRel = (Relation) k.next();
						if (secondRel.equals(firstRel))
							continue;
						else if (!secondRel.isOneToMany())
							continue;
						else if (secondRel.getStatus().equals(
								ComponentStatus.INFERRED_INCORRECT))
							continue;
						// For each relation, if it points to another included
						// table via M:1 we should subclass the relation, and this relation 
						// will replace the old one if created above.
						// But localTablesIncluded has the old table and new one
						final Table target = secondRel.getOneKey().getTable();
						if (includeTables.contains(target)
								&& !localTablesIncluded.contains(target)) {
							subclassedRelations.add(firstRel);
							final Set<Table> newRelationTablesIncluded = new HashSet<Table>(
									tablesIncluded);
							//if may replace the existing one
							relationTablesIncluded.put(firstRel,
									newRelationTablesIncluded);
							newRelationTablesIncluded.add(target);
							localTablesIncluded.add(target);
						}
					}
				}
			}

		// No subclassing? Return a singleton.
		if (subclassedRelations.isEmpty())
			return Collections.singleton(dataset);

		// Iterate through the relations we found and recurse.
		// If not the last one, we copy the original dataset and
		// work on the copy, otherwise we work on the original.
		for (final Iterator<Relation> i = subclassedRelations.iterator(); i.hasNext();) {
			final Relation r = (Relation) i.next();
			DataSet suggestedDataSet = dataset;
			try {
				if (i.hasNext()) {
					suggestedDataSet = new DataSet(this, dataset
							.getCentralTable(), dataset.getCentralTable()
							.getName());
				//	this.datasets.put(suggestedDataSet.getOriginalName(),
					//		suggestedDataSet);
					this.addDataSet(suggestedDataSet);
					// Copy subclassed relations from existing dataset.
					//TODO check, that is not all subclass relation????? it seems all empty
					for (final Iterator<Relation> j = dataset.getIncludedRelations()
							.iterator(); j.hasNext();)
						((Relation) j.next()).setSubclassRelation(
								suggestedDataSet, true);
				}
				r.setSubclassRelation(suggestedDataSet, true);
			} catch (final ValidationException e) {
				// Not valid? OK, ignore this one.
				continue;
			}
			suggestedDataSets.addAll(this.continueSubclassing(includeTables,
					(Set<Table>) relationTablesIncluded.get(r),
					suggestedDataSet, r.getManyKey().getTable()));
		}

		// Return the resulting datasets.
		return suggestedDataSets;
	}



	public void setMainTableList(List<String> list) {
		this.mainTableList = list;		
	}
	
	public List<String> getMartMTNameList() {
		return this.mainTableList;
	}

	public MartType getMartType() {
		return this.martType;
	}

	/**
	 * Runs the given {@link ConstructorRunnable} and monitors it's progress.
	 * 
	 * @param constructor
	 *            the constructor that will build a mart.
	 */
	public void requestMonitorConstructorRunnable(
			final ConstructorRunnable constructor) {
		// Create a progress monitor.
		final ProgressDialog progressMonitor = new ProgressDialog(null, 0, 100,
				true);

		// Start the construction in a thread. It does not need to be
		// Swing-thread-safe because it will never access the GUI. All
		// GUI interaction is done through the Timer below.
		final SwingWorker worker = new SwingWorker() {
			public Object construct() {
				constructor.run();
				return null;
			}
		};

		// Create a timer thread that will update the progress dialog.
		// We use the Swing Timer to make it Swing-thread-safe. (1000 millis
		// equals 1 second.)
		final Timer timer = new Timer(300, null);
		timer.setInitialDelay(300); // Start immediately upon request.
		timer.setCoalesce(true); // Coalesce delayed events.
		timer.addActionListener(new ActionListener() {
			public void actionPerformed(final ActionEvent e) {
				SwingUtilities.invokeLater(new Runnable() {
					public void run() {
						// Did the job complete yet?
						if (constructor.isAlive()) {
							if (progressMonitor.isCanceled())
								// Stop the thread if required.
								constructor.cancel();
							// If not, update the progress report.
							progressMonitor.setProgress(constructor
									.getPercentComplete());
						} else {
							// If it completed, close the task and tidy up.
							// Stop the timer.
							timer.stop();
							// Close the progress dialog.
							progressMonitor.setVisible(false);
							progressMonitor.dispose();
							// If it failed, show the exception.
							final Exception failure = constructor
									.getFailureException();
							// By singling out ConstructorException we can show
							// users useful messages straight away.
							if (failure != null)
								StackTrace
										.showStackTrace(failure instanceof ConstructorException ? failure
												: new ConstructorException(
														Resources
																.get("martConstructionFailed"),
														failure));
							// Inform user of success, if it succeeded.
							else
								JOptionPane.showMessageDialog(null, Resources
										.get("martConstructionComplete"),
										Resources.get("messageTitle"),
										JOptionPane.INFORMATION_MESSAGE);
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

	public void addSchema(Schema newSch) {
		final Set<String> oldSchs = new HashSet<String>(this.schemasObj.getSchemas()
				.keySet());
			if (!oldSchs.remove(newSch.getName())) {
				// Single-add.
				if (!newSch.isMasked())
					this.getSchemasObj().addSchema(newSch);
				newSch.addPropertyChangeListener("masked",
						this.getSchemasObj().updateListener);
				newSch.addPropertyChangeListener("name",
						this.getSchemasObj().updateListener);
			}
		
		for(String item: oldSchs) {
			this.getSchemasObj().removeSchemaTab(item, true);
		}
	}
	
	public void addDataSet(DataSet newDs) {
		if(this.datasetsObj.getDataSet(newDs.getName())==null) {
				// Single-add.
			if (!newDs.isMasked())
				this.datasetsObj.addDataSet(newDs);
			newDs.addPropertyChangeListener("masked",
					this.datasetsObj.renameListener);
			newDs.addPropertyChangeListener("name",
					this.datasetsObj.renameListener);
		}
	}
	
	public void removeDataSet(DataSet ds) {
		this.datasetsObj.removeDataSet(ds);
	}
	
}
