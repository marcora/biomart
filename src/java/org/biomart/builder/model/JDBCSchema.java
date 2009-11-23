package org.biomart.builder.model;

import java.awt.Frame;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.TreeMap;
import javax.swing.JOptionPane;
import org.biomart.builder.model.DataLink.JDBCDataLink;
import org.biomart.builder.model.ForeignKey;
import org.biomart.builder.model.PrimaryKey;
import org.biomart.builder.view.gui.diagrams.SchemaDiagram;
import org.biomart.builder.view.gui.dialogs.SaveOrphanKeyDialog;
import org.biomart.common.exceptions.AssociationException;
import org.biomart.common.exceptions.BioMartError;
import org.biomart.common.exceptions.DataModelException;
import org.biomart.common.resources.Log;
import org.biomart.common.resources.Resources;
import org.biomart.common.resources.Settings;
import org.biomart.common.view.gui.dialogs.ProgressDialog2;
import org.biomart.configurator.controller.dialects.DatabaseDialect;
import org.biomart.configurator.utils.ConnectionPool;
import org.biomart.configurator.utils.DbConnectionInfoObject;
import org.biomart.configurator.utils.McUtils;
import org.biomart.configurator.utils.type.Cardinality;
import org.biomart.configurator.utils.type.JdbcType;


	/**
	 * This implementation of the {@link Schema} interface connects to a JDBC
	 * data source and loads tables, keys and relations using database metadata.
	 * <p>
	 * If key-guessing is enabled, foreign keys are guessed instead of being
	 * read from the database. Guessing works by iterating through known primary
	 * keys, where the first column of the key matches the name of the table
	 * (optionally with '_id' appended), then iterating through all other tables
	 * looking for sets of columns with identical names, or names that have had
	 * '_key' appended. If it finds a matching set, then it assumes that it has
	 * found a foreign key, and establishes a relation between the two.
	 * <p>
	 * When using keyguessing, primary keys are read from database metadata, but
	 * if this method returns no results, then each table is searched for a
	 * column with the same name as the table, optionally with '_id' appended.
	 * If one is found, then it is assumed that that column is the primary key
	 * for the table.
	 * <p>
	 * This implementation is very careful not to override any hand-made
	 * relations or keys, or to reinstate any that have previously been marked
	 * as incorrect.
	 */
public class JDBCSchema extends Schema implements JDBCDataLink{
		private static final long serialVersionUID = 1L;
		private Connection connection;
		private DbConnectionInfoObject conObj;
		private String realSchemaName;
		private SchemaDiagram schemaDiagram;
		
		//TODO need clean up
		private Map<String,List<String>> tblColMap = new HashMap<String,List<String>>();
		private Map<String,List<String>> tblPkMap = new HashMap<String,List<String>>();
//		private Map<String,Map<String,List<String>>> tblFkMap = new HashMap<String,Map<String,List<String>>>();
		
		public SchemaDiagram getSchemaDiagram() {
			return this.schemaDiagram;
		}
		
		public void setSchemaDiagram(SchemaDiagram digram) {
			this.schemaDiagram = digram;
		}

		/**
		 * <p>
		 * Establishes a JDBC connection from the information provided, and
		 * remembers it. Nothing is read yet - if you want to read the schema
		 * data, you must use the {@link #synchronise()} method to do so.
		 * 
		 * @param mart
		 *            the mart we belong to.
		 * @param driverClassName
		 *            the class name of the JDBC driver, eg.
		 *            <tt>com.mysql.jdbc.Driver</tt>.
		 * @param url
		 *            the JDBC URL of the database server to connect to.
		 * @param dataLinkDatabase
		 *            the database to read tables from.
		 * @param dataLinkSchema
		 *            the database schema name to read tables from. In MySQL
		 *            this should be the same as the database name specified in
		 *            the JDBC URL. In Oracle and PostgreSQL, it is a distinct
		 *            entity.
		 * @param username
		 *            the username to connect as.
		 * @param password
		 *            the password to connect as. Defaults to no password if the
		 *            empty string is passed in.
		 * @param name
		 *            the name to give this schema after it has been created.
		 * @param keyGuessing
		 *            <tt>true</tt> if you want keyguessing enabled,
		 *            <tt>false</tt> otherwise.
		 * @param partitionRegex
		 *            partition stuff.
		 * @param partitionNameExpression
		 *            partition stuff.
		 */
		public JDBCSchema(final Mart mart, DbConnectionInfoObject conObject,
				final String dataLinkSchema, final String name,
				final boolean keyGuessing, final String partitionRegex,
				final String partitionNameExpression) {
			// Call the Schema constructor first, to set up our name,
			// and set up keyguessing.
			super(mart, name, keyGuessing, conObject.getDatabaseName(), dataLinkSchema,
					partitionRegex, partitionNameExpression);
			Log.debug("Creating JDBC schema");
			// Remember the settings.
			this.conObj = conObject;
		}

		protected void finalize() throws Throwable {
			try {
				this.closeConnection();
			} finally {
				super.finalize();
			}
		}

		public Collection<String> getUniqueValues(final String schemaPrefix,
				final Column column) throws SQLException {
			// Do the select.
			final List<String> results = new ArrayList<String>();
			final String schemaName = this
					.getDataLinkSchema();
			final Connection conn = this.getConnection(null);
			final String sql = DatabaseDialect.getDialect(this)
					.getUniqueValuesSQL(schemaName, column);
			Log.debug("About to run query: " + sql);
			final ResultSet rs = conn.prepareStatement(sql).executeQuery();
			while (rs.next())
				results.add(rs.getString(1));
			rs.close();

			// Return the results.
			return results;
		}

		public List<Object> getRows(final String schemaPrefix, final Table table,
				final int count) throws SQLException {
			// Do the select.
			final List<Object> results = new ArrayList<Object>();
			final String schemaName = this.getDataLinkSchema();
			final Connection conn = this.getConnection(null);
			final String sql = DatabaseDialect.getDialect(this)
					.getSimpleRowsSQL(schemaName, table);
			Log.debug("About to run query: " + sql);
			final ResultSet rs = conn.prepareStatement(sql).executeQuery();
			int rowCount = 0;
			while (rs.next() && rowCount++ < count) {
				final List<Object> values = new ArrayList<Object>();
				for (int i = 1; i <= rs.getMetaData().getColumnCount(); i++)
					values.add(rs.getObject(i));
				results.add(values);
			}
			rs.close();

			// Return the results.
			return results;
		}


		/**
		 * {@inheritDoc}
		 * <p>
		 * In our case, cohabitation means that the partner link is also a
		 * {@link JDBCDataLink} and that its connection is connected to the same
		 * database server listening on the same port and connected with the
		 * same username.
		 */
		public boolean canCohabit(final DataLink partner) {
			Log.debug("Testing " + this + " against " + partner
					+ " for cohabitation");
			// We can't cohabit with non-JDBCDataLink partners.
			if (!(partner instanceof JDBCDataLink))
				return false;
			final JDBCDataLink partnerLink = (JDBCDataLink) partner;

			// Work out the partner's catalogs and schemas.
			final Collection<String> partnerSchemas = new HashSet<String>();
			try {
				final DatabaseMetaData dmd = partnerLink.getConnection(null)
						.getMetaData();
				// We need to compare by catalog only.
				final ResultSet catalogs = dmd.getCatalogs();
				while (catalogs.next())
					partnerSchemas.add(catalogs.getString("TABLE_CAT"));
				return partnerSchemas.contains(this.getConnection(null)
						.getCatalog());
			} catch (final Throwable t) {
				// If get an error, assume can't find anything, thus assume
				// incompatible.
				return false;
			}
		}

		public Connection getConnection(final String overrideDataLinkSchema)
				throws SQLException {
			// If we are already connected, test to see if we are
			// still connected. If not, reset our connection.
			if (this.connection != null && this.connection.isClosed())
				try {
					Log.debug("Closing dead JDBC connection");
					this.connection.close();
				} catch (final SQLException e) {
					// We don't care. Ignore it.
				} finally {
					this.connection = null;
				}

			// If we are not connected, we should attempt to (re)connect now.
			if (this.connection == null) {
				Log.debug("Establishing JDBC connection");
				// Start out with no driver at all.
				Class loadedDriverClass = null;

				// Try the system class loader instead.
				try {
					loadedDriverClass = Class.forName(this.conObj.getJdbcType().getDriverClassName());
				} catch (final ClassNotFoundException e) {
					final SQLException e2 = new SQLException();
					e2.initCause(e);
					throw e2;
				}

				// Check it really is an instance of Driver.
				if (!Driver.class.isAssignableFrom(loadedDriverClass))
					throw new ClassCastException(Resources
							.get("driverClassNotJDBCDriver"));

				// Connect!
				final Properties properties = new Properties();
				properties.setProperty("user", this.conObj.getUserName());
				if (!this.conObj.getPassword().equals(""))
					properties.setProperty("password", this.conObj.getPassword());
				properties.setProperty("nullCatalogMeansCurrent", "false");
				this.connection = DriverManager.getConnection(
						overrideDataLinkSchema == null ? this.conObj.getJdbcUrl()+this.conObj.getDatabaseName(): 
							(this.conObj.getJdbcUrl()+this.conObj.getDatabaseName())
								.replaceAll(this.getDataLinkSchema(),
										overrideDataLinkSchema), properties);

				// Check the schema name.
				final DatabaseMetaData dmd = this.connection.getMetaData();
				final String catalog = this.connection.getCatalog();
				this.realSchemaName = this.getDataLinkSchema();
				ResultSet rs = dmd.getTables(catalog, this.realSchemaName, "%",
						null);
				if (!rs.isBeforeFirst()) {
					rs = dmd.getTables(catalog, this.realSchemaName
							.toUpperCase(), "%", null);
					if (rs.isBeforeFirst())
						this.realSchemaName = this.realSchemaName.toUpperCase();
				}
				if (!rs.isBeforeFirst()) {
					rs = dmd.getTables(catalog, this.realSchemaName
							.toLowerCase(), "%", null);
					if (rs.isBeforeFirst())
						this.realSchemaName = this.realSchemaName.toLowerCase();
				}
				rs.close();
			}

			// Return the connection.
			return this.connection;
		}

		public void setDataLinkDatabase(final String databaseName) {
			super.setDataLinkDatabase(databaseName);
			// Reset the cached database connection.
			try {
				this.closeConnection();
			} catch (final SQLException e) {
				// We don't care.
			}
		}

		public void setDataLinkSchema(final String schemaName) {
			super.setDataLinkSchema(schemaName);
			// Reset the cached database connection.
			try {
				this.closeConnection();
			} catch (final SQLException e) {
				// We don't care.
			}
		}

		private void closeConnection() throws SQLException {
			Log.debug("Closing JDBC connection");
			if (this.connection != null)
				try {
					this.connection.close();
				} finally {
					this.connection = null;
				}
		}

		public DbConnectionInfoObject getConnectionObject() {
			return this.conObj;
		}

		public void setConnectionObject(DbConnectionInfoObject object) {
			this.conObj = object;
		}

		public void storeInHistory() {
			// Store the schema settings in the history file.
			final Properties history = new Properties();
			history.setProperty("driverClass", this.conObj.getJdbcType().getDriverClassName());
			history.setProperty("jdbcURL", this.conObj.getJdbcUrl());
			history.setProperty("username", this.conObj.getUserName());
			history.setProperty("password", this.conObj.getPassword().equals("")? ""
					: this.conObj.getPassword());
			history.setProperty("schema", this.getDataLinkSchema());
			history.setProperty("partitionRegex", "");
			history.setProperty("partitionNameExpression",  "" );
			history.setProperty("keyguessing", "" + this.isKeyGuessing());
			Settings.saveHistoryProperties(JDBCSchema.class, this.getName(),
					history);
		}

		public boolean test() throws SQLException {
			Log.debug("Testing connection for " + this);
			// Establish the JDBC connection. May throw an exception of its own,
			// which is fine, just let it go.
			final Connection connection = this.getConnection(null);
			// If we have no connection, we can't test it!
			if (connection == null)
				return false;

			// Get the metadata.
			final DatabaseMetaData dmd = connection.getMetaData();

			// By opening, executing, then closing a DMD query we will test
			// the connection fully without actually having to read anything
			// from it. 
			// modified by yong liang for checking the lowercase/uppercase schema
			final String catalog = connection.getCatalog();
//			ResultSet rs = dmd.getTables(
//					"".equals(dmd.getSchemaTerm()) ? this.realSchemaName
//							: catalog, this.realSchemaName, "%", null);
			//FIXME: It should use the same format as getConnection and synchronize in the future
			ResultSet rs = dmd.getTables(catalog, this.realSchemaName, "%", null);
			
			final boolean worked = rs.isBeforeFirst();
			rs.close();

			// If we get here, it worked.
			return worked;
		}

		private void createPkFks(DatabaseMetaData dmd, final String catalog, double stepSize) throws SQLException, DataModelException {
			// Get and create primary keys.
			// Work out a list of all foreign keys currently existing.
			// Any remaining in this list later will be dropped.
			//do the pk for the main table first if it is mart
			List<String> mainTableList = this.getMart().getMartMTNameList();
			Table[] orderedTables = null;
			Map<Table, ArrayList<Column>> mtColsMap = new HashMap<Table, ArrayList<Column>>();
			if(mainTableList!=null) {
				orderedTables = new Table[mainTableList.size()];
				for(String tableStr:mainTableList) {
					Table table = (Table)this.getTables().get(tableStr);
					//find the column numbers
					int count = 0;
					ArrayList<Column> colList = new ArrayList<Column>();
					for(final Iterator<Column> i = table.getColumns().values().iterator(); i.hasNext();){
						final Column column = (Column)i.next();
						if(column.getName().indexOf(Resources.get("martPKSuffix"))>=0) {
							colList.add(column);
							count++;
						}
					}
					if(count-1>orderedTables.length)
						return; //error
					orderedTables[count-1]=table;
					mtColsMap.put(table, colList);
				}
				//check 
				for(Table t:orderedTables) {
					if(t==null)
						return;
				}
			}
			final Set<ForeignKey> fksToBeDropped = new HashSet<ForeignKey>();
			for (final Iterator<Table> i = this.getTables().values().iterator(); i.hasNext();) {
				final Table t = (Table) i.next();
				fksToBeDropped.addAll(t.getForeignKeys());

				// Obtain the primary key from the database. Even in databases
				// without referential integrity, the primary key is still
				// defined and can be obtained from the metadata.
				Log.debug("Loading table primary keys");
				String searchCatalog = catalog;
				String searchSchema = this.realSchemaName;

				final ResultSet dbTblPKCols = dmd.getPrimaryKeys(searchCatalog,
						searchSchema, t.getName());

				// Load the primary key columns into a map keyed by column
				// position.
				// In other words, the first column in the key has a map key of
				// 1, and so on. We do this because we can't guarantee we'll
				// read the key columns from the database in the correct order.
				// We keep the map sorted, so that when we iterate over it later
				// we get back the columns in the correct order.
				final Map<Short, Column> pkCols = new TreeMap<Short, Column>();
				while (dbTblPKCols.next()) {
					final String pkColName = dbTblPKCols
							.getString("COLUMN_NAME");
					final Short pkColPosition = new Short(dbTblPKCols
							.getShort("KEY_SEQ"));
					pkCols.put(pkColPosition, (Column)t.getColumns().get(pkColName));
				}
				dbTblPKCols.close();

				// Did DMD find a PK? If not, which is really unusual but
				// potentially may happen, attempt to find one by looking for a
				// single column with the same name as the table or with '_id'
				// appended if it is source. For the mart, pk is the columns with '_key'.
				// Only do this if we are using key-guessing.
				if (pkCols.isEmpty() && this.isKeyGuessing()) {
					Log.debug("Found no primary key, so attempting to guess one");
					if(!this.isMart()) {
						// Plain version first.
						Column candidateCol = (Column) t.getColumns().get(
								t.getName());
						// Try with '_id' appended if plain version turned up
						// nothing.
						if (candidateCol == null)
							candidateCol = (Column) t
									.getColumns()
									.get(
											t.getName()
													+ Resources
															.get("primaryKeySuffix"));
						// Found something? Add it to the primary key columns map,
						// with a dummy key of 1. (Use Short for the key because
						// that
						// is what DMD would have used had it found anything
						// itself).
						if (candidateCol != null)
							pkCols.put(Short.valueOf("1"), candidateCol);
					}else {
						Short colPosition = 1;
						if(mtColsMap.containsKey(t)) {
							List<Column> currentKeysList = mtColsMap.get(t);
							int keySize = currentKeysList.size();
							if(keySize == 1) //central main
								pkCols.put(Short.valueOf("1"),mtColsMap.get(t).get(0));
							else {
								//get parent table
								Table parentTable = orderedTables[keySize-2];
								//parent key list
								List<Column> pkeysList = mtColsMap.get(parentTable);
								for(Column col:currentKeysList) {
									boolean found = false;
									for(Column pcol:pkeysList) {
										if(pcol.getName().equals(col.getName())) {
											found = true;
											break;
										}
									}
									if(found == false)
										pkCols.put(Short.valueOf("1"),col);
								}
							}
						}else {
							for(Iterator ci = t.getColumns().values().iterator();ci.hasNext();){
								Column candidateCol = (Column)ci.next();
								if(candidateCol.getName().indexOf(Resources.get("martPKSuffix"))>=0) {
									pkCols.put(colPosition, candidateCol);
									colPosition++;
								}
							}
						}
					}
				}

				// Obtain the existing primary key on the table, if the table
				// previously existed and even had one in the first place.
				final PrimaryKey existingPK = t.getPrimaryKey();

				// Did we find a PK on the database copy of the table?
				if (!pkCols.isEmpty()) {

					// Yes, we found a PK on the database copy of the table. So,
					// create a new key based around the columns we identified.
					PrimaryKey candidatePK;
					try {
						candidatePK = new PrimaryKey((Column[]) pkCols.values()
								.toArray(new Column[0]));
					} catch (final Throwable th) {
						throw new BioMartError(th);
					}

					// If the existing table has no PK, or has a PK which
					// matches and is not incorrect, or has a PK which does not
					// match
					// and is not handmade, replace that PK with the one we
					// found.
					// This way we preserve any existing handmade PKs, and don't
					// override any marked as incorrect.
					try {
						if (existingPK == null)
							t.setPrimaryKey(candidatePK);
						else if (existingPK.equals(candidatePK)
								&& existingPK.getStatus().equals(
										ComponentStatus.HANDMADE))
							existingPK.setStatus(ComponentStatus.INFERRED);
						else if (!existingPK.equals(candidatePK)
								&& !existingPK.getStatus().equals(
										ComponentStatus.HANDMADE))
							t.setPrimaryKey(candidatePK);
					} catch (final Throwable th) {
						throw new BioMartError(th);
					}
				} else // No, we did not find a PK on the database copy of the
				// table, so that table should not have a PK at all. So if the
				// existing table has a PK which is not handmade, remove it.
				// the orphan PK is already cleaned by clearOrphanKey();
				if (existingPK != null
						&& !existingPK.getStatus().equals(
								ComponentStatus.HANDMADE))
					try {
						t.setPrimaryKey(null);
					} catch (final Throwable th) {
						throw new BioMartError(th);
					}
			} //end of for (final Iterator i = this.getTables().values().iterator(); i.hasNext();)

			// Are we key-guessing? Key guess the foreign keys, passing in a
			// reference to the list of existing foreign keys. After this call
			// has completed, the list will contain all those foreign keys which
			// no longer exist, and can safely be dropped.
			if (this.isKeyGuessing()) {
				if(this.isMart())					
					this.synchroniseKeysUsingMartKeyGuessing(fksToBeDropped, stepSize, orderedTables);
				else
					this.synchroniseKeysUsingKeyGuessing(fksToBeDropped, stepSize);
			// Otherwise, use DMD to do the same, also passing in the list of
			// existing foreign keys to be updated as the call progresses. Also
			// pass in the DMD details so it doesn't have to work them out for
			// itself.
			}
			else
				this.synchroniseKeysUsingDMD(fksToBeDropped, dmd,
						this.realSchemaName, catalog);

			// Drop any foreign keys that are left over (but not handmade ones).
			// the orphan FK is already cleaned by clearOrphanKey();
			for (final Iterator i = fksToBeDropped.iterator(); i.hasNext();) {
				final Key k = (Key) i.next();
				if (k.getStatus().equals(ComponentStatus.HANDMADE))
					continue;
				Log.debug("Dropping redundant foreign key " + k);
				for (final Iterator r = k.getRelations().iterator(); r
						.hasNext();) {
					final Relation rel = (Relation) r.next();
					rel.getFirstKey().getRelations().remove(rel);
					rel.getSecondKey().getRelations().remove(rel);
				}
				k.getTable().getForeignKeys().remove(k);
			}			
		}
		
		public void synchronise() throws SQLException, DataModelException {
			Log.info("Synchronising " + this);
			ProgressDialog2.getInstance().setStatus("Synchronising "+this);
			super.synchronise();
			// Get database metadata, catalog, and schema details.
			final DatabaseMetaData dmd = this.getConnection(null).getMetaData();
			final String catalog = this.getConnection(null).getCatalog();
			
			// List of objects storing orphan key column and its table name
			// The list may have duplicated key
			List orphanFKList = new ArrayList();
			StringBuffer orphanSearch = new StringBuffer();
			boolean orphanBool = false;
		
			try {
				orphanBool = findOrphanKeysFromDB(orphanFKList, orphanSearch);

				if (orphanBool) {
					Frame frame = new Frame();

					Object[] options = {Resources.get("detailButton"), Resources.get("cancelButton")};
					int n = JOptionPane
							.showOptionDialog(
									frame, Resources.get("orphanRelationWarningMessage"),
									Resources.get("orphanRelationWarningTitle"),
									JOptionPane.YES_NO_OPTION,
									JOptionPane.WARNING_MESSAGE, null, // do not use a custom Icon
									options, // the titles of buttons
									options[1]); // default button title
					if (n != JOptionPane.YES_OPTION) {
						//CorruptSchemaTextDialog.displayText("Orphan Foreign Key", orphanSearch);
						return;						
					}
					else{
						SaveOrphanKeyDialog sokd = new SaveOrphanKeyDialog(Resources.get("orphanKeyDialogTitle"), orphanSearch.toString());
						sokd.setVisible(true);
						//user abort it
						if(!sokd.checkSaved())
							return;
					}					
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
			
			// Now that user decides to sync GUI model to DB schema, remove orphan key
			if(orphanBool)
				clearOrphanKey(orphanFKList);

			// Create a list of existing tables. During this method, we remove
			// from this list all tables that still exist in the database. At
			// the end of the method, the list contains only those tables
			// which no longer exist, so they will be dropped.
			final Collection tablesToBeDropped = new HashSet(this.getTables()
					.values());

			// Load tables and views from database, then loop over them.
			ResultSet dbTables;
			
				dbTables = dmd.getTables(catalog, this.realSchemaName, "%",
						new String[] { "TABLE", "VIEW", "ALIAS", "SYNONYM" });

			// Do the loop.
			final Collection tablesToBeKept = new HashSet();
			while (dbTables.next()) {
				// Check schema and catalog.
				final String catalogName = dbTables.getString("TABLE_CAT");
				final String schemaName = dbTables.getString("TABLE_SCHEM");
				String schemaPrefix = null;
				
				
				// What is the table called?
				final String dbTableName = dbTables.getString("TABLE_NAME");
				Log.debug("Processing table " + dbTableName);

				//this is hardcode for oracle, check if this table is from recyclebin
				if(this.conObj.getJdbcType().equals(JdbcType.Oracle) && dbTableName.indexOf("BIN$")==0)
					continue;

				// Look to see if we already have a table by this name defined.
				// If we do, reuse it. If not, create a new table.
				Table dbTable = (Table) this.getTables().get(dbTableName);
				if (dbTable == null)
					try {
						dbTable = new Table(this, dbTableName);
						this.getTables().put(dbTableName, dbTable);
					} catch (final Throwable t) {
						throw new BioMartError(t);
					}

				// Table exists, so remove it from our list of tables to be
				// dropped at the end of the method.
				tablesToBeDropped.remove(dbTable);
				tablesToBeKept.add(dbTable);
			}
			dbTables.close();

			// Work out progress increment step size.
			double stepSize = 100.0 / (double) tablesToBeKept.size();
			// Divide by 2 - columns then relations.
			stepSize /= 2.0;

			// Loop over all columns.
			for (final Iterator i = tablesToBeKept.iterator(); i.hasNext();) {
				final Table dbTable = (Table) i.next();
				final String dbTableName = dbTable.getName();
				// Make a list of all the columns in the table. Any columns
				// remaining in this list by the end of the loop will be
				// dropped.
				final Collection colsToBeDropped = new HashSet(dbTable
						.getColumns().values());


				// Load the table columns from the database, then loop over
				// them.
				Log.debug("Loading table column list for " + dbTableName);
				ResultSet dbTblCols;
				
					dbTblCols = dmd.getColumns(catalog, this.realSchemaName,
							dbTableName, "%");
				// FIXME: When using Oracle, if the table is a synonym then the
				// above call returns no results.
				while (dbTblCols.next()) {
					// Check schema and catalog.
					final String catalogName = dbTblCols.getString("TABLE_CAT");
					final String schemaName = dbTblCols
							.getString("TABLE_SCHEM");
					String schemaPrefix = null;
					

					// What is the column called, and is it nullable?
					final String dbTblColName = dbTblCols
							.getString("COLUMN_NAME");
					Log.debug("Processing column " + dbTblColName);

					// Look to see if the column already exists on this table.
					// If it does, reuse it. Else, create it.
					Column dbTblCol = (Column) dbTable.getColumns().get(
							dbTblColName);
					if (dbTblCol == null)
						try {
							dbTblCol = new Column(dbTable, dbTblColName);
							dbTable.getColumns().put(dbTblCol.getName(),
									dbTblCol);
						} catch (final Throwable t) {
							throw new BioMartError(t);
						}

					// Column exists, so remove it from our list of columns to
					// be dropped at the end of the loop.
					colsToBeDropped.remove(dbTblCol);

				}
				dbTblCols.close();

				// Drop all columns that are left in the list, as they no longer
				// exist in the database.
				for (final Iterator j = colsToBeDropped.iterator(); j.hasNext();) {
					final Column column = (Column) j.next();
					Log.debug("Dropping redundant column " + column.getName());
					dbTable.getColumns().remove(column.getName());
				}

				// Update progress;
				this.progress += stepSize;
			}

			// Remove from schema all tables not found in the database, using
			// the list we constructed above.
			for (final Iterator i = tablesToBeDropped.iterator(); i.hasNext();) {
				final Table existingTable = (Table) i.next();
				Log.debug("Dropping redundant table " + existingTable);
				final String tableName = existingTable.getName();
				// By clearing its keys we will also clear its relations.
				for (final Iterator j = existingTable.getKeys().iterator(); j
						.hasNext();) {
					// Deref to prevent concurrent mods.
					final Collection rels = new ArrayList(((Key) j.next())
							.getRelations());
					for (final Iterator r = rels.iterator(); r.hasNext();) {
						final Relation rel = (Relation) r.next();
						rel.getFirstKey().getRelations().remove(rel);
						rel.getSecondKey().getRelations().remove(rel);
					}
				}
				existingTable.setPrimaryKey(null);
				existingTable.getForeignKeys().clear();
				this.getTables().remove(tableName);
			}
			// Get and create primary keys.
			// Work out a list of all foreign keys currently existing.
			// Any remaining in this list later will be dropped.
			this.createPkFks(dmd, catalog, stepSize);
			Log.info("Done synchronising");
			Log.info("forward message to controller");
			//forward message to controller
		}

		
		private ResultSet getTablesFromDB() throws SQLException {

			// Get database metadata, catalog, and schema details.
			final DatabaseMetaData dmd = this.getConnection(null).getMetaData();
			final String catalog = this.getConnection(null).getCatalog();

			// Load tables and views from database, then loop over them.
			ResultSet dbTables = null;
			dbTables = dmd.getTables(catalog, this.realSchemaName, "%",
						new String[] { "TABLE", "VIEW", "ALIAS", "SYNONYM" });

			return dbTables;

		}

		private ResultSet getTableColumnsFromDB(String dbTableName)
				throws SQLException {

			// Get database metadata, catalog, and schema details.
			final DatabaseMetaData dmd = this.getConnection(null).getMetaData();
			final String catalog = this.getConnection(null).getCatalog();

			ResultSet dbTblCols;
			dbTblCols = dmd.getColumns(catalog, this.realSchemaName,
						dbTableName, "%");

			return dbTblCols;

		}

		private HashMap getDBTableColumnCollection(ResultSet dbTableSet)
				throws SQLException {
			ResultSet dbTableColSet;
			HashMap tableColMap = new HashMap();
			while (dbTableSet.next()) {
				String tableName = dbTableSet.getString("TABLE_NAME");
				dbTableColSet = getTableColumnsFromDB(tableName);

				HashSet cols = new HashSet();
				while (dbTableColSet.next()) {
					cols.add(dbTableColSet.getString("COLUMN_NAME"));
				}
				dbTableColSet.close();
				tableColMap.put(tableName, cols);
			}

			return tableColMap;

		}

		 
		/** 
		 * Pass in a list object to hold table and column with orphan FK and PK
		 * Get PK, FK and the corresponding relations if they have. (Some PK, FK may not have relations)
		 *  
		 * modified by @author yliang
		 */
		private boolean findOrphanKeysFromDB(List orphanKeyList, StringBuffer orphanSearch) throws Exception {

			HashSet dbcols;
			boolean foundOrphanKey = false;
			StringBuffer result = orphanSearch;

			//List missTableList = new ArrayList();
			

			ResultSet dbTableSet = getTablesFromDB();
			HashMap tableColMap = getDBTableColumnCollection(dbTableSet);
			dbTableSet.close();

			// Loop through each key in the GUI model tables
			for (final Iterator i = this.getTables().values().iterator(); i
					.hasNext();) {

				final Table t = (Table) i.next();
				// Find the hashset of columns in corresponding DB table
				dbcols = (HashSet) tableColMap.get(t.getName());
				// Tables dropped or renamed is handled inside sync process
/*				if (dbcols == null) {
					//missTableList.add(t.getName());
					
					
					boolean foundRel = addTableKeysToOrphanList(t, orphanFK);
					if (foundRel){
						foundOrphanFK = true;
					}
					continue;
				
				}
*/
				//handle both PK and FK
				for (final Iterator j = t.getKeys().iterator(); j.hasNext();) {
					final Key k = (Key) j.next();
					for (int kcl = 0; kcl < k.getColumns().length; kcl++)

						// If there is no matching column in the DB table, the key is orphan
						// If dbcols is null, all columns are dropped and the key is orphan
						if (dbcols==null || !dbcols.contains(k.getColumns()[kcl].getName())) {

							foundOrphanKey = true;
							orphanKeyList.add(k);
							
							String msg = Resources.get("orphanFound")+" "+k+"; "+Resources.get("columnMissed")+" "+k.getColumns()[kcl].getName();
							if(k.getRelations()!=null && k.getRelations().size()>0)
								msg = msg + "; " + Resources.get("incorrectRelations")+ " "+ k.getRelations().toString() + "\n";

							result.append(msg);
							Log.warn(msg);
						}
				}
				
			}
			return foundOrphanKey;
		}

		/**
		 * clear Orphan Key
		 * @param orphanFKList
		 * @author yliang
		 */
		private void clearOrphanKey(List orphanFKList){
			
			
			for (final Iterator i = orphanFKList.iterator(); i.hasNext();) {
				final Key k = (Key) i.next();

				// Remove the relations for this key, it may happen that both PK and FK are orphen keys
				while(k.getRelations().size()>0)
				{
					final Relation rel = (Relation)k.getRelations().iterator().next();
					rel.getFirstKey().getRelations().remove(rel);
					rel.getSecondKey().getRelations().remove(rel);
				}
				// Remove the key from the table
				if(k instanceof PrimaryKey)
					k.getTable().setPrimaryKey(null);
				else
					k.getTable().getForeignKeys().remove(k);
				
				k.getTable().getKeys().remove(k);
			}
		}

	

		/**
		 * Establish foreign keys based purely on database metadata.
		 * 
		 * @param fksToBeDropped
		 *            the list of foreign keys to update as we go along. By the
		 *            end of the method, the only keys left in this list should
		 *            be ones that no longer exist in the database and may be
		 *            dropped.
		 * @param dmd
		 *            the database metadata to obtain the foreign keys from.
		 * @param schema
		 *            the database schema to read metadata from.
		 * @param catalog
		 *            the database catalog to read metadata from.
		 * @param stepSize
		 *            the progress step size to increment by.
		 * @throws SQLException
		 *             if there was a problem talking to the database.
		 * @throws DataModelException
		 *             if there was a logical problem during construction of the
		 *             set of foreign keys.
		 */
		/**
		 * @param fksToBeDropped
		 * @param dmd
		 * @param schema
		 * @param catalog
		 * @param stepSize
		 * @throws SQLException
		 * @throws DataModelException
		 */
		private void synchroniseKeysUsingDMD(final Set<ForeignKey> fksToBeDropped,
				final DatabaseMetaData dmd, final String schema,
				final String catalog)
				throws SQLException, DataModelException {
			Log.debug("Running DMD key synchronisation");
			// Loop through all the tables in the database, which is the same
			// as looping through all the primary keys.
			Log.debug("Finding tables");
			for (final Iterator<Table> i = this.getTables().values().iterator(); i
					.hasNext();) {

				// Obtain the table and its primary key.
				final Table pkTable = (Table) i.next();
				final PrimaryKey pk = pkTable.getPrimaryKey();
				// Skip all tables which have no primary key.
				if (pk == null)
					continue;

				Log.debug("Processing primary key " + pk);

				// Make a list of relations that already exist in this schema,
				// from some previous run. Any relations that are left in this
				// list by the end of the loop for this table no longer exist in
				// the database, and will be dropped.
				final Collection<Relation> relationsToBeDropped = new HashSet<Relation>(pk
						.getRelations());

				// Identify all foreign keys in the database metadata that refer
				// to the current primary key.
				Log.debug("Finding referring foreign keys");
				String searchCatalog = catalog;
				String searchSchema = this.realSchemaName;
				final ResultSet dbTblFKCols = dmd.getExportedKeys(
						searchCatalog, searchSchema, pkTable.getName());

				// Loop through the results. There will be one result row per
				// column per key, so we need to build up a set of key columns
				// in a map.
				// The map keys represent the column position within a key. Each
				// map value is a list of columns. In essence the map is a 2-D
				// representation of the foreign keys which refer to this PK,
				// with the keys of the map (Y-axis) representing the column
				// position in the FK, and the values of the map (X-axis)
				// representing each individual FK. In all cases, FK columns are
				// assumed to be in the same order as the PK columns. The map is
				// sorted by key column position.
				// An assumption is made that the query will return columns from
				// the FK in the same order as all other FKs, ie. all column 1s
				// will be returned before any 2s, and then all 2s will be
				// returned
				// in the same order as the 1s they are associated with, etc.
				final TreeMap dbFKs = new TreeMap();
				while (dbTblFKCols.next()) {
					final String fkTblName = dbTblFKCols
							.getString("FKTABLE_NAME");
					final String fkColName = dbTblFKCols
							.getString("FKCOLUMN_NAME");
					final Short fkColSeq = new Short(dbTblFKCols
							.getShort("KEY_SEQ"));
					// Note the column.
					if (!dbFKs.containsKey(fkColSeq))
						dbFKs.put(fkColSeq, new ArrayList());
					// In some dbs, FKs can be invalid, so we need to check
					// them.
					final Table fkTbl = (Table) this.getTables().get(fkTblName);
					if (fkTbl != null) {
						final Column fkCol = (Column) fkTbl.getColumns().get(
								fkColName);
						if (fkCol != null)
							((List) dbFKs.get(fkColSeq)).add(fkCol);
					}
				}
				dbTblFKCols.close();

				// Only construct FKs if we actually found any.
				if (!dbFKs.isEmpty()) {
					// Identify the sequence of the first column, which may be 0
					// or 1, depending on database implementation.
					final int firstColSeq = ((Short) dbFKs.firstKey())
							.intValue();

					// How many columns are in the PK?
					final int pkColCount = pkTable.getPrimaryKey().getColumns().length;

					// How many FKs do we have?
					final int fkCount = ((List) dbFKs.get(dbFKs.firstKey()))
							.size();

					// Loop through the FKs, and construct each one at a time.
					for (int j = 0; j < fkCount; j++) {
						// Set up an array to hold the FK columns.
						final Column[] candidateFKColumns = new Column[pkColCount];

						// For each FK column name, look up the actual column in
						// the table.
						for (final Iterator k = dbFKs.entrySet().iterator(); k
								.hasNext();) {
							final Map.Entry entry = (Map.Entry) k.next();
							final Short keySeq = (Short) entry.getKey();
							// Convert the db-specific column index to a
							// 0-indexed figure for the array of fk columns.
							final int fkColSeq = keySeq.intValue()
									- firstColSeq;
							candidateFKColumns[fkColSeq] = (Column) ((List) entry
									.getValue()).get(j);
						}

						// Create a template foreign key based around the set
						// of candidate columns we found.
						ForeignKey fk;
						try {
							fk = new ForeignKey(candidateFKColumns);
						} catch (final Throwable t) {
							throw new BioMartError(t);
						}
						final Table fkTable = fk.getTable();

						// If any FK already exists on the target table with the
						// same columns in the same order, then reuse it.
						boolean fkAlreadyExists = false;
						for (final Iterator f = fkTable.getForeignKeys()
								.iterator(); f.hasNext() && !fkAlreadyExists;) {
							final ForeignKey candidateFK = (ForeignKey) f
									.next();
							if (candidateFK.equals(fk)) {
								// Found one. Reuse it!
								fk = candidateFK;
								// Update the status to indicate that the FK is
								// backed by the database, if previously it was
								// handmade.
								if (fk.getStatus().equals(
										ComponentStatus.HANDMADE))
									fk.setStatus(ComponentStatus.INFERRED);
								// Remove the FK from the list to be dropped
								// later, as it definitely exists now.
								fksToBeDropped.remove(candidateFK);
								// Flag the key as existing.
								fkAlreadyExists = true;
							}
						}

						// Has the key been reused, or is it a new one?
						if (!fkAlreadyExists)
							try {
								fkTable.getForeignKeys().add(fk);
							} catch (final Throwable t) {
								throw new BioMartError(t);
							}

						// Work out whether the relation from the FK to
						// the PK should be 1:M or 1:1. The rule is that
						// it will be 1:M in all cases except where the
						// FK table has a PK with identical columns to
						// the FK, in which case it is 1:1, as the FK
						// is unique.
						Cardinality card = Cardinality.MANY_A;
						final PrimaryKey fkPK = fkTable.getPrimaryKey();
						if (fkPK != null
								&& fk.getColumns().equals(fkPK.getColumns()))
							card = Cardinality.ONE;

						// Check to see if it already has a relation.
						boolean relationExists = false;
						for (final Iterator f = fk.getRelations().iterator(); f
								.hasNext();) {
							// Obtain the next relation.
							final Relation candidateRel = (Relation) f.next();

							// a) a relation already exists between the FK
							// and the PK.
							if (candidateRel.getOtherKey(fk).equals(pk)) {
								// If cardinality matches, make it
								// inferred. If doesn't match, make it
								// modified and update original cardinality.
								try {
									if (card.equals(candidateRel
											.getCardinality())) {
										if (!candidateRel
												.getStatus()
												.equals(
														ComponentStatus.INFERRED_INCORRECT))
											candidateRel
													.setStatus(ComponentStatus.INFERRED);
									} else {
										if (!candidateRel
												.getStatus()
												.equals(
														ComponentStatus.INFERRED_INCORRECT))
											candidateRel
													.setStatus(ComponentStatus.MODIFIED);
										candidateRel
												.setOriginalCardinality(card);
									}
								} catch (final AssociationException ae) {
									throw new BioMartError(ae);
								}
								// Don't drop it at the end of the loop.
								relationsToBeDropped.remove(candidateRel);
								// Say we've found it.
								relationExists = true;
							}

							// b) a handmade relation exists elsewhere which
							// should not be dropped. All other relations
							// elsewhere will be dropped.
							else if (candidateRel.getStatus().equals(
									ComponentStatus.HANDMADE))
								// Don't drop it at the end of the loop.
								relationsToBeDropped.remove(candidateRel);
						}

						// If relation did not already exist, create it.
						if (!relationExists) {
							// Establish the relation.
							try {
								final Relation rel = new Relation(pk, fk, card);
								pk.getRelations().add(rel);
								fk.getRelations().add(rel);
							} catch (final Throwable t) {
								throw new BioMartError(t);
							}
						}
					}
				}

				// Remove any relations that we didn't find in the database (but
				// leave the handmade ones behind).
				for (final Iterator<Relation> j = relationsToBeDropped.iterator(); j
						.hasNext();) {
					final Relation r = (Relation) j.next();
					if (r.getStatus().equals(ComponentStatus.HANDMADE))
						continue;
					r.getFirstKey().getRelations().remove(r);
					r.getSecondKey().getRelations().remove(r);
				}
			}
		}

		/**
		 * This method implements the key-guessing algorithm for foreign keys.
		 * Basically, it iterates through all known primary keys, and looks for
		 * sets of matching columns in other tables, either with the same names
		 * or with '_key' appended. Any matching sets found are assumed to be
		 * foreign keys with relations to the current primary key.
		 * <p>
		 * Relations are 1:M, except when the table at the FK end has a PK with
		 * identical column to the FK. In this case, the FK is forced to be
		 * unique, which implies that it can only partake in a 1:1 relation, so
		 * the relation is marked as such.
		 * 
		 * @param fksToBeDropped
		 *            the list of foreign keys to update as we go along. By the
		 *            end of the method, the only keys left in this list should
		 *            be ones that no longer exist in the database and may be
		 *            dropped.
		 * @param stepSize
		 *            the progress step size to increment by.
		 * @throws SQLException
		 *             if there was a problem talking to the database.
		 * @throws DataModelException
		 *             if there was a logical problem during construction of the
		 *             set of foreign keys.
		 */
		private void synchroniseKeysUsingKeyGuessing(
				final Set<ForeignKey> fksToBeDropped, final double stepSize)
				throws SQLException, DataModelException {
			Log.debug("Running non-DMD key synchronisation");
			// Loop through all the tables in the database, which is the same
			// as looping through all the primary keys.
			Log.debug("Finding tables");
			for (final Iterator<Table> i = this.getTables().values().iterator(); i
					.hasNext();) {
				// Update progress;
				this.progress += stepSize;

				// Obtain the table and its primary key.
				final Table pkTable = (Table) i.next();
				final PrimaryKey pk = pkTable.getPrimaryKey();
				// Skip all tables which have no primary key.
				if (pk == null)
					continue;

				Log.debug("Processing primary key " + pk);

				// If an FK exists on the PK table with the same columns as the
				// PK, then we cannot use this PK to make relations to other
				// tables.
				// This is because the FK shows that this table is not the
				// original source of the data in those columns. Some other
				// table is the original source, so we assume that relations
				// will have been established from that other table instead. So,
				// we skip this table.
				boolean pkIsAlsoAnFK = false;
				for (final Iterator<ForeignKey> j = pkTable.getForeignKeys().iterator(); j
						.hasNext()
						&& !pkIsAlsoAnFK;) {
					final Key fk = (Key) j.next();
					if (fk.getColumns().equals(pk.getColumns()))
						pkIsAlsoAnFK = true;
				}
				if (pkIsAlsoAnFK)
					continue;

				// To maintain some degree of sanity here, we assume that a PK
				// is the original source of data (and not a copy of data
				// sourced from some other table) if the first column in the PK
				// has the same name as the table it is in, or with '_id'
				// appended, or is just 'id' on its own. Any PK which does not
				// have this property is skipped.
				final Column firstPKCol = pk.getColumns()[0];
				String firstPKColName = firstPKCol.getName();
				int idPrefixIndex = firstPKColName.indexOf(Resources
						.get("primaryKeySuffix"));
				//then try uppercase, in Oracle, names are uppercase
				if(idPrefixIndex<0) 
					idPrefixIndex = firstPKColName.toUpperCase().indexOf(Resources.get("primaryKeySuffix").toUpperCase());
				if (idPrefixIndex >= 0)
					firstPKColName = firstPKColName.substring(0, idPrefixIndex);
				if (!firstPKColName.equals(pkTable.getName())
						&& !firstPKColName.equals(Resources.get("idCol")))
					continue;

				// Make a list of relations that already exist in this schema,
				// from some previous run. Any relations that are left in this
				// list by the end of the loop for this table no longer exist in
				// the database, and will be dropped.
				final Collection<Relation> relationsToBeDropped = new HashSet<Relation>(pk
						.getRelations());

				// Now we know that we can use this PK for certain, look for all
				// other tables (other than the one the PK itself belongs to),
				// for sets of columns with identical names, or with '_key'
				// appended. Any set that we find is going to be an FK with a
				// relation back to this PK.
				Log.debug("Searching for possible referring foreign keys");
				for (final Iterator<Table> l = this.getTables().values().iterator(); l
						.hasNext();) {
					// Obtain the next table to look at.
					final Table fkTable = (Table) l.next();

					// Make sure the table is not the same as the PK table.
					if (fkTable.equals(pkTable))
						continue;

					// Set up an empty list for the matching columns.
					final Column[] candidateFKColumns = new Column[pk
							.getColumns().length];
					int matchingColumnCount = 0;

					// Iterate through the PK columns and find a column in the
					// target FK table with the same name, or with '_key'
					// appended,
					// or with the PK table name and an underscore prepended.
					// If found, add that target column to the candidate FK
					// column
					// set.
					for (int columnIndex = 0; columnIndex < pk.getColumns().length; columnIndex++) {
						final String pkColumnName = pk.getColumns()[columnIndex]
								.getName();
						// Start out by assuming no match.
						Column candidateFKColumn = null;
						// Don't try to find 'id' or 'id_key' columns as that
						// would be silly and would probably match far too much.
						if (!pkColumnName.equals(Resources.get("idCol"))) {
							// Try equivalent name first.
							candidateFKColumn = (Column) fkTable.getColumns()
									.get(pkColumnName);
							// Then try with '_key' appended, if not found.
							if (candidateFKColumn == null)
								candidateFKColumn = (Column) fkTable
										.getColumns()
										.get(
												pkColumnName
														+ Resources
																.get("foreignKeySuffix"));
						}
						// Then try with PK tablename+'_' prepended, if not
						// found.
						if (candidateFKColumn == null)
							candidateFKColumn = (Column) fkTable
									.getColumns()
									.get(pkTable.getName() + "_" + pkColumnName);
						// Found it? Add it to the candidate list.
						if (candidateFKColumn != null) {
							candidateFKColumns[columnIndex] = candidateFKColumn;
							matchingColumnCount++;
						}
					}

					// We found a matching set, so create a FK on it!
					if (matchingColumnCount == pk.getColumns().length) {
						// Create a template foreign key based around the set
						// of candidate columns we found.
						ForeignKey fk;
						try {
							fk = new ForeignKey(candidateFKColumns);
						} catch (final Throwable t) {
							throw new BioMartError(t);
						}

						// If any FK already exists on the target table with the
						// same columns in the same order, then reuse it.
						boolean fkAlreadyExists = false;
						for (final Iterator<ForeignKey> f = fkTable.getForeignKeys()
								.iterator(); f.hasNext() && !fkAlreadyExists;) {
							final ForeignKey candidateFK = (ForeignKey) f
									.next();
							if (candidateFK.equals(fk)) {
								// Found one. Reuse it!
								fk = candidateFK;
								// Update the status to indicate that the FK is
								// backed by the database, if previously it was
								// handmade.
								if (fk.getStatus().equals(
										ComponentStatus.HANDMADE))
									fk.setStatus(ComponentStatus.INFERRED);
								// Remove the FK from the list to be dropped
								// later, as it definitely exists now.
								fksToBeDropped.remove(candidateFK);
								// Flag the key as existing.
								fkAlreadyExists = true;
							}
						}

						// Has the key been reused, or is it a new one?
						if (!fkAlreadyExists)
							try {
								fkTable.getForeignKeys().add(fk);
							} catch (final Throwable t) {
								throw new BioMartError(t);
							}

						// Work out whether the relation from the FK to
						// the PK should be 1:M or 1:1. The rule is that
						// it will be 1:M in all cases except where the
						// FK table has a PK with identical columns to
						// the FK, in which case it is 1:1, as the FK
						// is unique.
						Cardinality card = Cardinality.MANY_A;
						final PrimaryKey fkPK = fkTable.getPrimaryKey();
						if (fkPK != null
								&& fk.getColumns().equals(fkPK.getColumns()))
							card = Cardinality.ONE;

						// Check to see if it already has a relation.
						boolean relationExists = false;
						for (final Iterator<Relation> f = fk.getRelations().iterator(); f
								.hasNext();) {
							// Obtain the next relation.
							final Relation candidateRel = (Relation) f.next();

							// a) a relation already exists between the FK
							// and the PK.
							if (candidateRel.getOtherKey(fk).equals(pk)) {
								// If cardinality matches, make it
								// inferred. If doesn't match, make it
								// modified and update original cardinality.
								try {
									if (card.equals(candidateRel
											.getCardinality())) {
										if (!candidateRel
												.getStatus()
												.equals(
														ComponentStatus.INFERRED_INCORRECT))
											candidateRel
													.setStatus(ComponentStatus.INFERRED);
									} else {
										if (!candidateRel
												.getStatus()
												.equals(
														ComponentStatus.INFERRED_INCORRECT))
											candidateRel
													.setStatus(ComponentStatus.MODIFIED);
										candidateRel
												.setOriginalCardinality(card);
									}
								} catch (final AssociationException ae) {
									throw new BioMartError(ae);
								}
								// Don't drop it at the end of the loop.
								relationsToBeDropped.remove(candidateRel);
								// Say we've found it.
								relationExists = true;
							}

							// b) a handmade relation exists elsewhere which
							// should not be dropped. All other relations
							// elsewhere will be dropped.
							else if (candidateRel.getStatus().equals(
									ComponentStatus.HANDMADE))
								// Don't drop it at the end of the loop.
								relationsToBeDropped.remove(candidateRel);
						}

						// If relation did not already exist, create it.
						if (!relationExists) {
							// Establish the relation.
							try {
								final Relation rel = new Relation(pk, fk, card);
								pk.getRelations().add(rel);
								fk.getRelations().add(rel);
							} catch (final Throwable t) {
								throw new BioMartError(t);
							}
						}
					}
				}

				// Remove any relations that we didn't find in the database (but
				// leave the handmade ones behind).
				for (final Iterator<Relation> j = relationsToBeDropped.iterator(); j
						.hasNext();) {
					final Relation r = (Relation) j.next();
					if (r.getStatus().equals(ComponentStatus.HANDMADE))
						continue;
					r.getFirstKey().getRelations().remove(r);
					r.getSecondKey().getRelations().remove(r);
				}
			}
		}
	
		private void synchroniseKeysUsingMartKeyGuessing(
			final Collection fksToBeDropped, final double stepSize, Table[] orderedTables)
			throws SQLException, DataModelException {
			Log.debug("Running non-DMD key synchronisation");
			// Loop through all the tables in the database, which is the same
			// as looping through all the primary keys.
			Log.debug("Finding tables");
			//do the main table only
			for (Table pkTable:orderedTables) {
				// Update progress;
				this.progress += stepSize;
	
				// Obtain the table and its primary key.
				final PrimaryKey pk = pkTable.getPrimaryKey();
				// Skip all tables which have no primary key.
				if (pk == null)
					continue;
	
				Log.debug("Processing primary key " + pk);
		
				// Make a list of relations that already exist in this schema,
				// from some previous run. Any relations that are left in this
				// list by the end of the loop for this table no longer exist in
				// the database, and will be dropped.
				final Collection relationsToBeDropped = new HashSet(pk
						.getRelations());
	
				// Now we know that we can use this PK for certain, look for all
				// other tables (other than the one the PK itself belongs to),
				// for sets of columns with identical names. Any set that we find is going to be an FK with a
				// relation back to this PK.
				Log.debug("Searching for possible referring foreign keys");
				for (final Iterator l = this.getTables().values().iterator(); l
						.hasNext();) {
					// Obtain the next table to look at.
					final Table fkTable = (Table) l.next();
	
					// Make sure the table is not the same as the PK table.
					if (fkTable.equals(pkTable))
						continue;
	
					// Set up an empty list for the matching columns.
					final Column[] candidateFKColumns = new Column[pk
							.getColumns().length];
					int matchingColumnCount = 0;
	
					// Iterate through the PK columns and find a column in the
					// target FK table with the same name, add that target column to the candidate FK
					// column set.
					//should be 1 column in pk
					for (int columnIndex = 0; columnIndex < pk.getColumns().length; columnIndex++) {
						final String pkColumnName = pk.getColumns()[columnIndex]
								.getName();
						// Start out by assuming no match.
						Column candidateFKColumn = null;
						// Try equivalent name first.
						candidateFKColumn = (Column) fkTable.getColumns()
									.get(pkColumnName);						
						
						if (candidateFKColumn != null) {
							candidateFKColumns[columnIndex] = candidateFKColumn;
							matchingColumnCount++;
						}
					}
	
					// We found a matching set, so create a FK on it!
					if (matchingColumnCount == pk.getColumns().length) {
						// Create a template foreign key based around the set
						// of candidate columns we found.
						ForeignKey fk;
						try {
							fk = new ForeignKey(candidateFKColumns);
						} catch (final Throwable t) {
							throw new BioMartError(t);
						}
	
						// If any FK already exists on the target table with the
						// same columns in the same order, then reuse it.
						boolean fkAlreadyExists = false;
						for (final Iterator f = fkTable.getForeignKeys()
								.iterator(); f.hasNext() && !fkAlreadyExists;) {
							final ForeignKey candidateFK = (ForeignKey) f
									.next();
							if (candidateFK.equals(fk)) {
								// Found one. Reuse it!
								fk = candidateFK;
								// Update the status to indicate that the FK is
								// backed by the database, if previously it was
								// handmade.
								if (fk.getStatus().equals(
										ComponentStatus.HANDMADE))
									fk.setStatus(ComponentStatus.INFERRED);
								// Remove the FK from the list to be dropped
								// later, as it definitely exists now.
								fksToBeDropped.remove(candidateFK);
								// Flag the key as existing.
								fkAlreadyExists = true;
							}
						}
	
						// Has the key been reused, or is it a new one?
						if (!fkAlreadyExists)
							try {
								fkTable.getForeignKeys().add(fk);
							} catch (final Throwable t) {
								throw new BioMartError(t);
							}
	
						// Work out whether the relation from the FK to
						// the PK should be 1:M or 1:1. The rule is that
						// it will be 1:M in all cases except where the
						// FK table has a PK with identical columns to
						// the FK, in which case it is 1:1, as the FK
						// is unique.
						Cardinality card = Cardinality.MANY_A;
						final PrimaryKey fkPK = fkTable.getPrimaryKey();
						if (fkPK != null
								&& fk.getColumns().equals(fkPK.getColumns()))
							card = Cardinality.ONE;
	
						// Check to see if it already has a relation.
						boolean relationExists = false;
						for (final Iterator f = fk.getRelations().iterator(); f
								.hasNext();) {
							// Obtain the next relation.
							final Relation candidateRel = (Relation) f.next();
	
							// a) a relation already exists between the FK
							// and the PK.
							if (candidateRel.getOtherKey(fk).equals(pk)) {
								// If cardinality matches, make it
								// inferred. If doesn't match, make it
								// modified and update original cardinality.
								try {
									if (card.equals(candidateRel
											.getCardinality())) {
										if (!candidateRel
												.getStatus()
												.equals(
														ComponentStatus.INFERRED_INCORRECT))
											candidateRel
													.setStatus(ComponentStatus.INFERRED);
									} else {
										if (!candidateRel
												.getStatus()
												.equals(
														ComponentStatus.INFERRED_INCORRECT))
											candidateRel
													.setStatus(ComponentStatus.MODIFIED);
										candidateRel
												.setOriginalCardinality(card);
									}
								} catch (final AssociationException ae) {
									throw new BioMartError(ae);
								}
								// Don't drop it at the end of the loop.
								relationsToBeDropped.remove(candidateRel);
								// Say we've found it.
								relationExists = true;
							}
	
							// b) a handmade relation exists elsewhere which
							// should not be dropped. All other relations
							// elsewhere will be dropped.
							else if (candidateRel.getStatus().equals(
									ComponentStatus.HANDMADE))
								// Don't drop it at the end of the loop.
								relationsToBeDropped.remove(candidateRel);
						}
	
						// If relation did not already exist, create it.
						if (!relationExists) {
							// Establish the relation.
							try {
								final Relation rel = new Relation(pk, fk, card);
								pk.getRelations().add(rel);
								fk.getRelations().add(rel);
							} catch (final Throwable t) {
								throw new BioMartError(t);
							}
						}
					}
				}
	
				// Remove any relations that we didn't find in the database (but
				// leave the handmade ones behind).
				for (final Iterator j = relationsToBeDropped.iterator(); j
						.hasNext();) {
					final Relation r = (Relation) j.next();
					if (r.getStatus().equals(ComponentStatus.HANDMADE))
						continue;
					r.getFirstKey().getRelations().remove(r);
					r.getSecondKey().getRelations().remove(r);
				}
			}				
		}

		public String getDriverClassName() {
			return this.conObj.getJdbcType().getDriverClassName();
		}

		public String getPassword() {
			return this.conObj.getPassword();
		}

		public String getUrl() {
			return this.conObj.getJdbcUrl();
		}

		public String getUsername() {
			return this.conObj.getUserName();
		}


		public void init(List<String> tablesInDb) throws DataModelException, SQLException {
			Log.info("Initialize " + this);
			long t1 = McUtils.getCurrentTime();
			long t2 = 0;
			ProgressDialog2.getInstance().setStatus("creating "+this);
			super.init(tablesInDb);
			
			this.setMetaInfo(this.conObj.getDatabaseName(), this.conObj.getSchemaName());
			
			//create table	
			if(this.isSchemaPartitioned()) {
				//get all tables in all selected database
				for(String tbName:this.tblColMap.keySet()) {
					int index = tbName.indexOf(".");
					String tableName = tbName.substring(index+1);
					String ptName = tbName.substring(0,index);
					Table table = this.getTables().get(tableName);
					if(table==null) {
						table = new Table(this,tableName);
						this.getTables().put(tableName, table);
					}
					table.addPartition(ptName);
					//create column
					for(String colName: this.tblColMap.get(tbName)) {
						Column dbTblCol = table.getColumns().get(colName);
						if(dbTblCol == null) {
							dbTblCol = new Column(table,colName);
							table.getColumns().put(colName, dbTblCol);
						}
						dbTblCol.addPartition(ptName);
					}					
				}
				//create PK for each table
				for(Table table:this.getTables().values()) {
					List<Column> pkColList = new ArrayList<Column>();
					if(this.tblPkMap.get(table.getName())!=null)
						for(String pkColName:this.tblPkMap.get(table.getName())) {
							pkColList.add(table.getColumns().get(pkColName));
						}
					this.createPKforTable(table, pkColList);
				}
			}else {
				for(String tbName:this.tblColMap.keySet()) {
					String tableName = tbName.substring(tbName.indexOf(".")+1);
					Table table = new Table(this,tableName);
					this.getTables().put(tableName, table);
					//create column
					for(String colName:this.tblColMap.get(tbName)) {
						Column dbTblCol = new Column(table,colName);
						table.getColumns().put(colName, dbTblCol);
					}
					//create PK
					List<Column> pkColList = new ArrayList<Column>();
					if(this.tblPkMap.get(tableName)!=null)
						for(String pkColName:this.tblPkMap.get(tableName)) {
							pkColList.add(table.getColumns().get(pkColName));
						}
					this.createPKforTable(table, pkColList);
				}
			}

				t2 = McUtils.getCurrentTime();
			
			if (this.isKeyGuessing()) 
				this.synchroniseKeysUsingKeyGuessing(new HashSet<ForeignKey>(), 1);
			else
				this.synchroniseKeysUsingDMD(new HashSet<ForeignKey>(), this.getConnection(null).getMetaData(), 
						this.conObj.getSchemaName(), this.conObj.getDatabaseName());


			long t4 = McUtils.getCurrentTime();
			System.err.println("get data from db "+(t2-t1));
			System.err.println("init "+(t4-t2));
			Log.info("Done synchronising");
			Log.info("forward message to controller");
		}
	
	/**
	 * create a PK for table, if the table has candidate columns, use them, otherwise use keyGuessing
	 * @param table
	 * @param hasPK
	 * @param pkCols
	 */
	private void createPKforTable(Table table, List<Column> pkCols) {
		if(table==null)
			return;
		if(pkCols.isEmpty() && this.isKeyGuessing()) {
			//create PK by keyguessing
			// Did DMD find a PK? If not, which is really unusual but
			// potentially may happen, attempt to find one by looking for a
			// single column with the same name as the table or with '_id'
			// appended if it is source. For the mart, pk is the columns with '_key'.
			// Only do this if we are using key-guessing.
			// Plain version first.
			Column candidateCol = (Column) table.getColumns().get(table.getName());
			// Try with '_id' appended if plain version turned up
			// nothing.
			if (candidateCol == null)
				candidateCol = (Column) table.getColumns().get(table.getName()
								+ Resources.get("primaryKeySuffix"));
			// Found something? Add it to the primary key columns map,
			// with a dummy key of 1. (Use Short for the key because
			// that
			// is what DMD would have used had it found anything
			// itself).
			if (candidateCol != null)
				pkCols.add(candidateCol);

		}
		//create PK
		if(!pkCols.isEmpty()) {
			PrimaryKey candidatePK;
			try {
				candidatePK = new PrimaryKey((Column[]) pkCols
						.toArray(new Column[0]));
			} catch (final Throwable th) {
				throw new BioMartError(th);
			}

			try {
				table.setPrimaryKey(candidatePK);
			} catch (final Throwable th) {
				throw new BioMartError(th);
			}
		}

	}

	/**
	 * get the metadata info from source database, and set to tblColMap,tblPkMap, and tblFkMap
	 * @throws SQLException 
	 */
	private void setMetaInfo(String dbName,String schemaName) {
		this.tblColMap.clear();
		this.tblPkMap.clear();
//		this.tblFkMap.clear();
		
		
		switch(this.conObj.getJdbcType()) {
		case MySQL: //MyISAM
			this.setMySQLMetaInfo(dbName, schemaName);
			break;
		case PostGreSQL:
			this.setPgsMetaInfo(dbName, schemaName);
			break;
		case Oracle:
			break;		
		}
			
	}
	
	private void setMySQLMetaInfo(String dbName,String schemaName) {
		List<String> colList = new ArrayList<String>();
		List<String> pkList = new ArrayList<String>();
		StringBuffer sb = new StringBuffer("select table_schema,table_name,column_name,column_key from information_schema.columns where ");
		if(this.isSchemaPartitioned()) {
			for(Iterator<String> i = this.getMart().getMartLocation().getSelectedTables().keySet().iterator();i.hasNext();) {
				sb.append("table_schema='"+i.next()+"' " );
				if(i.hasNext())
					sb.append(" or ");
			}
		} else
			sb.append(" table_schema='"+schemaName+"' ");
		sb.append("order by table_schema, table_name, ordinal_position");

		String lastTableName = "";		
		String lastSchema = "";
		Connection con = ConnectionPool.Instance.getConnection(this.conObj);
		try {
			Statement st = con.createStatement();
			ResultSet rs = st.executeQuery(sb.toString());
			while(rs.next()) {
				String tableName = rs.getString("table_name");
				String schemaStr = rs.getString("table_schema");
				//finish all columns in one table and move to the next, if previous table doesn't have a PK, 
				//create using keyguessing
				if(!(lastTableName.equals(tableName) && lastSchema.equals(schemaStr))) {
					if(!lastTableName.equals("")) {
						this.tblColMap.put(schemaStr+"."+lastTableName, colList);
						this.tblPkMap.put(lastTableName, pkList);
						//no fk for MyISAM;
						colList = new ArrayList<String>();
						pkList = new ArrayList<String>();
					}
					//this.createPKforTable(currentTable, pkCols);
					//move to next table
					
					//clean flags
					lastTableName = tableName;
					lastSchema = schemaStr;
				}
				
				colList.add(rs.getString("column_name"));

				//PK?
				String priStr = rs.getString("column_key");
				//PRI is the value return from MySQL
				if("PRI".equals(priStr)) {
					pkList.add(rs.getString("column_name"));
				}

			}
			rs.close();

		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		this.tblColMap.put(lastSchema+"."+lastTableName, colList);
		this.tblPkMap.put(lastTableName, pkList);		
	}
	
	/**
	 * new two sql to get all metadata
	 * @param dbName
	 * @param schemaName
	 */
	private void setPgsMetaInfo(String dbName, String schemaName) {
		String lastTableName = "";
		List<String> colList = new ArrayList<String>();
		String sql1 = "select table_name,column_name from information_schema.columns where table_schema='" +
				schemaName+"' order by table_name,ordinal_position";
		Connection con = ConnectionPool.Instance.getConnection(this.conObj);
		Statement st = null;
		try {
			st = con.createStatement();
			ResultSet rs = st.executeQuery(sql1);
			while(rs.next()) {
				String tableName = rs.getString("table_name");
				//finish all columns in one table and move to the next
				if(!lastTableName.equals(tableName)) {
					if(!lastTableName.equals("")) {
						this.tblColMap.put(lastTableName, colList);
						colList = new ArrayList<String>();
					}
					lastTableName = tableName;
				}				
				colList.add(rs.getString("column_name"));
			}
			rs.close();

		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		this.tblColMap.put(lastTableName, colList);
		
		//get all PK
		String sql2 = "SELECT tc.table_name, kcu.column_name " +
				"FROM information_schema.table_constraints tc LEFT JOIN information_schema.key_column_usage kcu " +
				"ON tc.constraint_catalog = kcu.constraint_catalog AND tc.constraint_schema = kcu.constraint_schema " +
				"AND tc.constraint_name = kcu.constraint_name where tc.constraint_schema = '"+schemaName+"' and constraint_type='PRIMARY KEY'" +
				" order by table_name, column_name";

		lastTableName="";
		List<String> pkList = new ArrayList<String>();
		try {
			ResultSet rs = st.executeQuery(sql2);
			while(rs.next()) {
				String tableName = rs.getString("table_name");
				if(!lastTableName.equals(tableName)) {
					if(!lastTableName.equals("")) {
						//not the first time
						this.tblPkMap.put(lastTableName, pkList);
						//this.tblFkMap.put(lastTableName, fkMap);
						pkList = new ArrayList<String>();
						//fkMap = new HashMap<String,List<String>>();
					}
					lastTableName = tableName;
				}
				
				pkList.add(rs.getString("column_name"));
				
			}
			rs.close();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	private boolean isSchemaPartitioned() {
		if(this.conObj.getPartitionRegex()!=null && this.conObj.getPtNameExpression()!=null)
			return true;
		else
			return false;
	}
}
