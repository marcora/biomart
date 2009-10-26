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

package org.biomart.configurator.view.gui.dialogs;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.filechooser.FileFilter;
import org.biomart.builder.model.Mart;
import org.biomart.builder.model.Schema;
import org.biomart.builder.model.JDBCSchema;
import org.biomart.common.resources.Resources;
import org.biomart.common.view.gui.SwingWorker;
import org.biomart.common.view.gui.dialogs.ProgressDialog2;
import org.biomart.common.view.gui.dialogs.StackTrace;
import org.biomart.configurator.model.Location;
import org.biomart.configurator.utils.ConnectionObject;
import org.biomart.configurator.utils.treelist.DBMetaTree;
import org.biomart.configurator.utils.type.MartType;


	/**
	 * This connection panel implementation allows a user to define some JDBC
	 * connection parameters, such as hostname, username, driver class and if
	 * necessary the location where the driver can be found. It uses this to
	 * construct a JDBC URL, dynamically, and ultimately creates a
	 * {@link JDBCSchema} implementation which represents the connection.
	 */
	public class JDBCLocationConnectionPanel extends JPanel
			implements DocumentListener {

		private static final Map<String,String[]> DRIVER_MAP = new HashMap<String,String[]>();

		private static final Map<String,String> DRIVER_NAME_MAP = new HashMap<String,String>();

		private static final long serialVersionUID = 1;
		
		protected MartType type;

		// NOTE: Please add any more default drivers that we support to this
		// pair
		// of static lists.
		static {
			// JDBC URL formats.
			// The keys are driver classes, the values are arrays.
			// The first entry in the array should be the default port number
			// for this
			// JDBC driver type, and the second entry should be an example JDBC
			// URL.
			// Within the URL, the keywords <HOSTNAME>, <PORT> and <DATABASE>
			// must all
			// appear in the order mentioned. Any other order will break the
			// regex
			// replacement function elsewhere in this class.
			JDBCLocationConnectionPanel.DRIVER_MAP.put("com.mysql.jdbc.Driver",
					new String[] { "3306",
							"jdbc:mysql://<HOST>:<PORT>/" });
			JDBCLocationConnectionPanel.DRIVER_MAP.put(
					"oracle.jdbc.driver.OracleDriver", new String[] { "1531",
							"jdbc:oracle:thin:@<HOST>:<PORT>:" });
			JDBCLocationConnectionPanel.DRIVER_MAP.put("org.postgresql.Driver",
					new String[] { "5432",
							"jdbc:postgresql://<HOST>:<PORT>/" });
			// Names.
			// The keys are database names, the values are driver classes.
			JDBCLocationConnectionPanel.DRIVER_NAME_MAP.put(Resources
					.get("driverClassMySQL"), "com.mysql.jdbc.Driver");
			JDBCLocationConnectionPanel.DRIVER_NAME_MAP.put(Resources
					.get("driverClassOracle"),
					"oracle.jdbc.driver.OracleDriver");
			JDBCLocationConnectionPanel.DRIVER_NAME_MAP.put(Resources
					.get("driverClassPostgreSQL"), "org.postgresql.Driver");
		}

		private String currentJDBCURLTemplate;
		private JTextField driverClass;
		private JTextField host;
		private JFileChooser jarFileChooser;
		private JTextField jdbcURL;
		private JPasswordField password;
		private JButton getButton;
		private JFormattedTextField port;
		private JComboBox predefinedDriverClass;
		private JTextField username;
		private DBMetaTree dbPreview;
		private JCheckBox keyguessing;
		private ConnectionObject conObject;

		/**
		 * This constructor creates a panel with all the fields necessary to
		 * construct a {@link JDBCSchema} instance, save the name which will be
		 * passed in elsewhere.
		 * <p>
		 * You must call {@link #copySettingsFromSchema(Schema)} before
		 * displaying this panel, otherwise the values displayed are not defined
		 * and may result in unpredictable behaviour. Or, call
		 * {@link #copySettingsFromProperties(Properties)} to achieve the same
		 * results.
		 * 
		 * @param mart
		 *            the mart this schema will belong to.
		 */
		public JDBCLocationConnectionPanel(MartType type) {
			super();
			this.type = type;
			// Create the layout manager for this panel.
			this.setLayout(new GridBagLayout());

			// Create constraints for labels that are not in the last row.
			final GridBagConstraints labelConstraints = new GridBagConstraints();
			labelConstraints.gridwidth = GridBagConstraints.RELATIVE;
			labelConstraints.fill = GridBagConstraints.HORIZONTAL;
			labelConstraints.anchor = GridBagConstraints.LINE_END;
			labelConstraints.insets = new Insets(0, 2, 0, 0);
			// Create constraints for fields that are not in the last row.
			final GridBagConstraints fieldConstraints = new GridBagConstraints();
			fieldConstraints.gridwidth = GridBagConstraints.REMAINDER;
			fieldConstraints.fill = GridBagConstraints.NONE;
			fieldConstraints.anchor = GridBagConstraints.LINE_START;
			fieldConstraints.insets = new Insets(0, 1, 0, 2);
			// Create constraints for labels that are in the last row.
			final GridBagConstraints labelLastRowConstraints = (GridBagConstraints) labelConstraints
					.clone();
			labelLastRowConstraints.gridheight = GridBagConstraints.REMAINDER;
			// Create constraints for fields that are in the last row.
			final GridBagConstraints fieldLastRowConstraints = (GridBagConstraints) fieldConstraints
					.clone();
			fieldLastRowConstraints.gridheight = GridBagConstraints.REMAINDER;

			// Create all the useful fields in the dialog box.
			this.jdbcURL = new JTextField(40);
			this.host = new JTextField(20);
			this.port = new JFormattedTextField(new DecimalFormat("0"));
			this.port.setColumns(4);
			this.username = new JTextField(10);
			this.password = new JPasswordField(10);
			this.keyguessing = new JCheckBox(Resources.get("myISAMLabel"));

			// The driver class box displays the currently selected driver
			// class. As it changes, other fields become highlighted.
			this.driverClass = new JTextField(20);
			this.driverClass.getDocument().addDocumentListener(this);
			this.driverClass.setText(null); // Force into default state.
			//this.keyguessing.setVisible(false);
			this.updateJDBCURL();
			// The predefined driver class box displays everything we know
			// about by default, as defined by the map at the start of this
			// class.
			this.predefinedDriverClass = new JComboBox(
					JDBCLocationConnectionPanel.DRIVER_NAME_MAP.keySet().toArray(
							new String[0]));
			this.predefinedDriverClass.addActionListener(new ActionListener() {
				public void actionPerformed(final ActionEvent e) {
					// This method is called when the driver class field is
					// changed, either by the user typing in it, or using
					// the drop-down to select a predefine value.

					// Work out which database type was selected.
					final String classType = (String) JDBCLocationConnectionPanel.this.predefinedDriverClass
							.getSelectedItem();

					// Use it to look up the default class for that database
					// type, then reset the drop-down to nothing-selected.
					if (!JDBCLocationConnectionPanel.this.isEmpty(classType)) {
						final String driverClassName = (String) JDBCLocationConnectionPanel.DRIVER_NAME_MAP
								.get(classType);
						if (!JDBCLocationConnectionPanel.this.driverClass
								.getText().equals(driverClassName)) {
							JDBCLocationConnectionPanel.this.keyguessing
									.setVisible(driverClassName
											.indexOf("mysql") >= 0);
							JDBCLocationConnectionPanel.this.keyguessing
									.setSelected(driverClassName
											.indexOf("mysql") >= 0);
							JDBCLocationConnectionPanel.this.driverClass
									.setText(driverClassName);
						}
					}
				}
			});

			// Create a listener that listens for changes on the host, port
			// and database fields, and uses this to automatically update
			// and construct a JDBC URL based on their contents.
			this.host.getDocument().addDocumentListener(this);
			this.port.getDocument().addDocumentListener(this);

			// Create a file chooser for finding the JAR file where the driver
			// lives.
			this.jarFileChooser = new JFileChooser();
			this.jarFileChooser.setFileFilter(new FileFilter() {
				// Accepts only files ending in ".jar".
				public boolean accept(final File f) {
					return f.isDirectory()
							|| f.getName().toLowerCase().endsWith(
									Resources.get("jarExtension"));
				}

				public String getDescription() {
					return Resources.get("JARFileFilterDescription");
				}
			});

			// Two-column string/string panel of matches
			this.dbPreview = new DBMetaTree();			

			// On-change listener for regex+expression to update panel of
			// matches
			// by creating a temporary dummy schema with the specified regexes
			// and
			// seeing what it produces. Alerts if nothing produced.
			// Add the driver class label and field.
			JLabel label = new JLabel(Resources.get("driverClassLabel"));
			this.add(label, labelConstraints);
			JPanel field = new JPanel();
			field.add(this.predefinedDriverClass);
			field.add(this.keyguessing);
			field.add(this.driverClass);
			this.add(field, fieldConstraints);

			// Add the host label, and the host field, port label, port field.
			label = new JLabel(Resources.get("hostLabel"));
			this.add(label, labelConstraints);
			field = new JPanel();
			field.add(this.host);
			label = new JLabel(Resources.get("portLabel"));
			field.add(label);
			field.add(this.port);
			this.add(field, fieldConstraints);

			// Add the JDBC URL label and field.
			label = new JLabel(Resources.get("jdbcURLLabel"));
			this.add(label, labelConstraints);
			field = new JPanel();
			field.add(this.jdbcURL);
			this.add(field, fieldConstraints);

			// Add the username label, and the username field, password
			// label and password field across the username field space
			// in order to save space.
			label = new JLabel(Resources.get("usernameLabel"));
			this.add(label, labelConstraints);
			field = new JPanel();
			field.add(this.username);
			label = new JLabel(Resources.get("passwordLabel"));
			field.add(label);
			field.add(this.password);
			getButton = new JButton(Resources.get("getButton"));
			field.add(this.getButton);
			getButton.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					updateDataBasesList();
				}				
			});
			this.add(field, fieldConstraints);

			// Add the partition stuff.

			// Two-column string/string panel of matches
			label = new JLabel(Resources.get("databasesLabel"));
			this.add(label, labelLastRowConstraints);
			field = this.dbPreview.createGUI();
			this.add(field, fieldLastRowConstraints);
			//set default value
			this.predefinedDriverClass.setSelectedItem(Resources.get("driverClassMySQL"));
			if(this.type.equals(MartType.SOURCE)) {
				this.host.setText("ensembldb.ensembl.org");
				this.port.setText("5306");
			}else {
				this.jdbcURL.setText("martdb.ensembl.org");
				this.port.setText("5316");
			}
		}

		public void copySettingsFromProperties(final Properties template) {
			// Copy the driver class.
			this.driverClass.setText(template.getProperty("driverClass"));

			// Make sure the right fields get enabled.
			this.driverClassChanged();

			// Carry on copying.
			final String jdbcURL = template.getProperty("jdbcURL");
			this.jdbcURL.setText(jdbcURL);
			this.username.setText(template.getProperty("username"));
			this.password.setText(template.getProperty("password"));
			this.keyguessing.setSelected(Boolean.valueOf(
					template.getProperty("keyguessing")).booleanValue());

			// Parse the JDBC URL into host, port and database, if the
			// driver is known to us (defined in the map at the start
			// of this class).
			String regexURL = this.currentJDBCURLTemplate;
			if (regexURL != null) {
				// Replace the three placeholders in the JDBC URL template
				// with regex patterns. Obviously, this depends on the
				// three placeholders appearing in the correct order.
				// If they don't, then you're stuffed.
				regexURL = regexURL.replaceAll("<HOST>", "(.*)");
				regexURL = regexURL.replaceAll("<PORT>", "(.*)");
				regexURL = regexURL.replaceAll("<DATABASE>", "(.*)");

				// Use the regex to parse out the host, port and database
				// from the JDBC URL.
				final Pattern regex = Pattern.compile(regexURL);
				final Matcher matcher = regex.matcher(jdbcURL);
				if (matcher.matches()) {
					this.host.setText(matcher.group(1));
					this.port.setText(matcher.group(2));
				}
			}
		}

		private void documentEvent(final DocumentEvent e) {
			if (e.getDocument().equals(this.driverClass.getDocument()))
				this.driverClassChanged();
			else
				this.updateJDBCURL();
		}

		private void driverClassChanged() {
			// Work out which class we should try out.
			final String className = this.driverClass.getText();
			

			// If it's not empty...
			if (!this.isEmpty(className)) {
				this.keyguessing
				.setVisible(className
						.indexOf("mysql") >= 0);

				// Is this a preset class?
				for (final Iterator i = JDBCLocationConnectionPanel.DRIVER_NAME_MAP
						.entrySet().iterator(); i.hasNext();) {
					final Map.Entry entry = (Map.Entry) i.next();
					final String mapName = (String) entry.getKey();
					final String mapClassName = (String) entry.getValue();
					if (mapClassName.equals(className)) 
						this.predefinedDriverClass.setSelectedItem(mapName);
				}

				// Do we know about this, as defined in the map at the start
				// of this class?
				if (JDBCLocationConnectionPanel.DRIVER_MAP.containsKey(className)) {
					// Yes, so we can use the map to construct a JDBC URL
					// template, into which host, port, and database can be
					// placed as required.

					// Obtain the template and split it.
					final String[] parts = (String[]) JDBCLocationConnectionPanel.DRIVER_MAP
							.get(className);

					// The second part is the JDBC URL template itself. Remember
					// which template was selected, then disable the JDBC URL
					// field in the interface as its contents will now be
					// computed automatically. Enable the host/database/port
					// fields instead.
					this.currentJDBCURLTemplate = parts[1];
					this.jdbcURL.setEnabled(false);
					this.host.setEnabled(true);
					this.port.setEnabled(true);

					// The first part of the template is the default port
					// number, so set the port field to that number.
					this.port.setText(parts[0]);
				}

				// This else statement deals with JDBC drivers that we do not
				// have a template for.
				else {
					// Blank out our current template, so that we don't try
					// and use it by accident.
					this.currentJDBCURLTemplate = null;

					// Enable the user-specified JDBC URL field, and disable
					// the host/port/database fields as they're no longer
					// required.
					this.jdbcURL.setEnabled(true);
					this.host.setEnabled(false);
					this.port.setEnabled(false);
				}
			}

			// If it's empty, disable the fields that depend on it.
			else {
				this.keyguessing.setVisible(false);
				this.host.setEnabled(false);
				this.port.setEnabled(false);
				this.jdbcURL.setEnabled(false);
			}
		}

		public Class<? extends Location> getLocationClass() {
			return Location.class;
		}

		private boolean isEmpty(final String string) {
			// Strings are empty if they are null or all whitespace.
			return string == null || string.trim().length() == 0;
		}

		private void updateJDBCURL() {
			// If we don't have a current template, we can't parse it,
			// so don't even attempt to do so.
			if (this.currentJDBCURLTemplate == null)
				return;

			// Update the JDBC URL based on our current settings. Do this
			// by replacing the placeholders in the template with the
			// current values of the host/port/database fields. If there
			// are no values in these fields, leave the placeholders
			// as they are.
			String newURL = this.currentJDBCURLTemplate;
			if (!this.isEmpty(this.host.getText()))
				newURL = newURL.replaceAll("<HOST>", this.host.getText());
			if (!this.isEmpty(this.port.getText()))
				newURL = newURL.replaceAll("<PORT>", this.port.getText());
			// Set the JDBC URL field to contain the URL we constructed.
			this.jdbcURL.setText(newURL);
		}

		public void changedUpdate(final DocumentEvent e) {
			this.documentEvent(e);
		}

		public Schema createSchemaFromSettings(final String name) {
			// If the fields aren't valid, we can't create it.
			if (!this.validateFields(true))
				return null;

			try {
				// Return that schema.
				return this.privateCreateSchemaFromSettings(name);
			} catch (final Throwable t) {
				StackTrace.showStackTrace(t);
			}

			// If we got here, something went wrong, so behave
			// as though validation failed.
			return null;
		}

		private Schema privateCreateSchemaFromSettings(final String name)
				throws Exception {
			// Record the user's specifications.
			final String driverClassName = this.driverClass.getText();
			final String url = this.jdbcURL.getText();
			final String username = this.username.getText();
			final String password = new String(this.password.getPassword());
			// Construct a JDBCSchema based on them.
			ConnectionObject conObj = new ConnectionObject(url,"",username,password,driverClassName);
			final JDBCSchema schema = new JDBCSchema(null, conObj,
					 "", name, this.keyguessing.isSelected(), "", 
					"");

			// Return that schema.
			return schema;
		}

		public void insertUpdate(final DocumentEvent e) {
			this.documentEvent(e);
		}


		public void removeUpdate(final DocumentEvent e) {
			this.documentEvent(e);
		}


		public boolean validateFields(final boolean report) {
			// Make a list to hold any validation messages that may occur.
			final List<String> messages = new ArrayList<String>();

			// If we don't have a class, complain.
			if (this.isEmpty(this.driverClass.getText()))
				messages.add(Resources.get("fieldIsEmpty", Resources
						.get("driverClass")));

			// If the user had to specify their own JDBC URL, make sure
			// they have done so.
			if (this.jdbcURL.isEnabled()) {
				if (this.isEmpty(this.jdbcURL.getText()))
					messages.add(Resources.get("fieldIsEmpty", Resources
							.get("jdbcURL")));
			}

			// Otherwise, make sure they have specified all three of host, port
			// and database.
			else {
				if (this.isEmpty(this.host.getText()))
					messages.add(Resources.get("fieldIsEmpty", Resources
							.get("host")));
				if (this.isEmpty(this.port.getText()))
					messages.add(Resources.get("fieldIsEmpty", Resources
							.get("port")));
			}

			// Make sure they have given a schema name.

			// Make sure they have given a username. (Password is optional as
			// not all databases require one).
			if (this.isEmpty(this.username.getText()))
				messages.add(Resources.get("fieldIsEmpty", Resources
						.get("username")));

			// If there any messages to show the user, show them.
			if (report && !messages.isEmpty())
				JOptionPane.showMessageDialog(null, messages
						.toArray(new String[0]), Resources
						.get("validationTitle"),
						JOptionPane.INFORMATION_MESSAGE);

			// Validation succeeds if there were no messages.
			return messages.isEmpty();
		}


		public void updateDataBasesList() {
			if (!this.validateFields(true))
				return;

			conObject = new ConnectionObject(this.jdbcURL.getText(),"",
					this.username.getText(),
					new String(this.password.getPassword()),
					this.driverClass.getText());
			final ProgressDialog2 progressMonitor = ProgressDialog2.getInstance();				
 
    		final SwingWorker worker = new SwingWorker() {
    			public Object construct() {
    				try {
		        		dbPreview.updateTree(conObject);
    				} catch (final Throwable t) {
    					SwingUtilities.invokeLater(new Runnable() {
    						public void run() {
    							StackTrace.showStackTrace(t);
    						}
    					});
    				}finally {
    					progressMonitor.setVisible(false);
    			//		progressMonitor.dispose();					
    				}
    				return null;
    			}

    			public void finished() {
    				// Close the progress dialog.
    				progressMonitor.setVisible(false);
    			//	progressMonitor.dispose();
    			}
    		};
    		worker.start();
    		progressMonitor.start("testing");
		}
		
		public void clearDataBasesList() {
			this.dbPreview.resetTree(null);
		}
		

		
		public boolean test() {
			boolean res = this.validateFields(true);
			return res;
		}

		
		public Location createLocation(String name, MartType type, Map<String, Location> locations) {
			if(this.dbPreview.getDBInfo().size()<=0) 
				return null;

			//check if this location already exist
			Location loc = null;
			if(null==locations || locations.get(name) == null) {
				loc = new Location(name);
				
				ConnectionObject conObj = new ConnectionObject(this.jdbcURL.getText(),"",
						this.username.getText(),
						new String(this.password.getPassword()),
						this.driverClass.getText());
				loc.setConnectionObject(conObj);
				loc.setKeyGuessing(true);
			} else
				loc = locations.get(name);
			
			loc.addSelectedTables(this.dbPreview.getDBInfo());
			for(Iterator<String> it = this.dbPreview.getDBInfo().keySet().iterator(); it.hasNext();) {
				String dbName = it.next();
				//check if mart is already exist
				if(loc.getMart(dbName) == null) {
					final Mart mart = new Mart(loc,dbName,type);
					// Construct a JDBCSchema based on them.
					loc.getConnectionObject().setDatabaseName(dbName);
					final JDBCSchema schema = new JDBCSchema(mart, loc.getConnectionObject(),
							 dbName, dbName, true, "", "");
					mart.getSchemas().put(schema.getOriginalName(), schema);
					
					loc.addMart(mart);
				}
			}
			return loc;
		}


		
		public void setType(MartType type) {
			this.type = type;
			this.predefinedDriverClass.setSelectedItem(Resources.get("driverClassMySQL"));
			if(type.equals(MartType.SOURCE)) {
				this.host.setText(Resources.get("ensemblSourceHost"));
				this.port.setText(Resources.get("ensemblSourcePort"));
			}else {
				this.host.setText(Resources.get("ensemblMartHost"));
				this.port.setText(Resources.get("ensemblMartPort"));
			}
			this.username.setText(Resources.get("ANONYMOUSUSER"));
			this.updateJDBCURL();
		}
	
		public String getHostPortValue() {
			return this.host.getText()+":"+this.port.getText();
		}
	}

