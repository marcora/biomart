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

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

import org.biomart.builder.model.Schema;
import org.biomart.common.exceptions.BioMartError;
import org.biomart.common.general.exceptions.FunctionalException;
import org.biomart.common.general.utils.MyUtils;
import org.biomart.common.resources.Resources;
import org.biomart.common.resources.Settings;
import org.biomart.common.view.gui.dialogs.StackTrace;
import org.biomart.configurator.model.Location;
import org.biomart.configurator.utils.DsConnectionObject;
import org.biomart.configurator.utils.McUtils;
import org.biomart.configurator.utils.treelist.URLMetaTree;
import org.biomart.configurator.utils.type.IdwViewType;
import org.biomart.configurator.utils.type.MartType;
import org.biomart.configurator.view.idwViews.McViews;
import org.biomart.old.martService.Configuration;
import org.biomart.old.martService.objects.MartInVirtualSchema;
import org.jdom.JDOMException;

/**
 * This dialog box allows the user to define or modify a schema, by giving it a
 * name, choosing a type, then displaying the appropriate
 * {@link SchemaConnectionPanel} according to the type chosen. The connection
 * panel then is given the job of actually creating or modifying the schema,
 * before the result is returned to the caller.
 * 
 * @author Richard Holland <holland@ebi.ac.uk>
 * @version $Revision: 1.13 $, $Date: 2007/11/27 10:47:01 $, modified by
 *          $Author: rh4 $
 * @since 0.5
 */
public class LocationConnectionDialog extends JDialog implements ItemListener{
	private static final long serialVersionUID = 1;

	/**
	 * Pop up a dialog asking the user for details for a new schema, then create
	 * and return that schema.
	 * 
	 * @param mart
	 *            the mart to create the schema in.
	 * 
	 * @return the newly created schema, or null if it was cancelled.
	 */
	public static Object showDialog(Map<String,Location> locationMap) {
		final LocationConnectionDialog dialog = new LocationConnectionDialog(
				Resources.get("newLocationDialogTitle"), locationMap);
		dialog.setLocationRelativeTo(McViews.getInstance().getView(IdwViewType.SCHEMA));
		dialog.setVisible(true);
		MartType type = dialog.getLocationType();
		if(type.equals(MartType.SOURCE) ||
				type.equals(MartType.TARGET)) {
			final Location loc = dialog.getMcLocation();
			if(loc!=null) {
				loc.setIsSourceSchema(dialog.isSourceSchema());
				dialog.dispose();
				return loc;
			}				
		}else if(type.equals(MartType.URL)) {
			return dialog.getConnectionObject();
		}
		dialog.dispose();
		return null;
	}

	private JButton cancel;
	private JDBCLocationConnectionPanel connectionPanel;
	private JButton execute;	
	private JComboBox name;
	private JButton test;
	private JComboBox type;	
	private Location location = null;	
	private JPanel typePanel;
	private JPanel connectionPanelHolder;
	//hack for url
	private MartType martType = MartType.URL;
	private URLMetaTree urlMeta;
	private JTextField urlTF;
	private JTextField portTF;
	private JTextField pathTF;
	private Map<String, Location> currentLocations;
	private Map<String, Configuration> configMap;
	private DsConnectionObject conObject;
	//set loaddata status for detecting name combobox selection ignoring additem and removeitem
	private boolean loadData = true;

	private LocationConnectionDialog(final String title, Map<String, Location> locations) {
		// Create the basic dialog centred on the main mart builder window.
		super();
		this.location = null;
		this.currentLocations = locations;
		this.setTitle(title);
		this.setModal(true);

		// Create the content pane for the dialog, ie. the bit that will hold
		// all the various questions and answers.
		final JPanel content = new JPanel(new BorderLayout());
		this.setContentPane(content);
		
		final JPanel northPanel = new JPanel(new GridBagLayout());
		
		typePanel = new JPanel(new CardLayout());
		final JPanel southPanel = new JPanel(new FlowLayout());
		
		content.add(northPanel,BorderLayout.NORTH);
		content.add(typePanel,BorderLayout.CENTER);
		content.add(southPanel, BorderLayout.SOUTH);
		
		final JPanel schemaPanel = new JPanel(new GridBagLayout());

		// Create some constraints for labels, except those on the last row
		// of the dialog.
		final GridBagConstraints labelConstraints = new GridBagConstraints();
		labelConstraints.gridwidth = GridBagConstraints.RELATIVE;
		labelConstraints.fill = GridBagConstraints.HORIZONTAL;
		labelConstraints.anchor = GridBagConstraints.LINE_END;
		labelConstraints.insets = new Insets(0, 2, 0, 0);
		// Create some constraints for fields, except those on the last row
		// of the dialog.
		final GridBagConstraints fieldConstraints = new GridBagConstraints();
		fieldConstraints.gridwidth = GridBagConstraints.REMAINDER;
		fieldConstraints.fill = GridBagConstraints.NONE;
		fieldConstraints.anchor = GridBagConstraints.LINE_START;
		fieldConstraints.insets = new Insets(0, 1, 0, 2);
		// Create some constraints for labels on the last row of the dialog.
		final GridBagConstraints labelLastRowConstraints = (GridBagConstraints) labelConstraints
				.clone();
		labelLastRowConstraints.gridheight = GridBagConstraints.REMAINDER;
		// Create some constraints for fields on the last row of the dialog.
		final GridBagConstraints fieldLastRowConstraints = (GridBagConstraints) fieldConstraints
				.clone();
		fieldLastRowConstraints.gridheight = GridBagConstraints.REMAINDER;

		// Create the input fields for the type, and the
		// holder for the connection panel details.
		this.type = new JComboBox(new MartType[] { MartType.URL, MartType.SOURCE, 
				MartType.TARGET, MartType.XML, MartType.FILE });
		connectionPanelHolder = new JPanel();
		this.type.addItemListener(this);

		this.connectionPanel = new JDBCLocationConnectionPanel(MartType.SOURCE);
		connectionPanelHolder.add(this.connectionPanel);
		// Make a default selection for the connection panel holder. Use JDBC
		// as it is the most obvious choice. We have to do something here else
		// the box won't size properly without one.
		this.type.setSelectedItem(MartType.URL);

		// Build a combo box that allows the user to change the name
		// of a schema, or select one from history to copy settings from.
		this.name = new JComboBox();
		this.name.setEditable(true);
		this.updateNameReference(MartType.URL);

		// Create buttons in dialog.
		this.test = new JButton(Resources.get("testButton"));
		this.cancel = new JButton(Resources.get("cancelButton"));
		this.execute = new JButton(Resources.get("addButton"));

		// In the name field, also include the type label and field, to save
		// space.
		JPanel field = new JPanel();
		
		JLabel typelabel = new JLabel(Resources.get("typeLabel"));
		field.add(typelabel);
		field.add(this.type);
		
		// Add the name label and name field.
		JLabel namelabel = new JLabel(Resources.get("nameLabel"));
		field.add(namelabel);
		field.add(this.name);

		//schemaPanel.add(field, fieldConstraints);
		northPanel.add(field, fieldConstraints);

		// Add the connection panel holder.
		schemaPanel.add(connectionPanelHolder, fieldConstraints);

		// Add the buttons.
		JLabel label = new JLabel();
		schemaPanel.add(label, labelLastRowConstraints);
		
//		southPanel.add(this.test);
		southPanel.add(this.cancel);
		southPanel.add(this.execute);

		// Intercept the cancel button, which closes the dialog
		// without taking any action.
		this.cancel.addActionListener(new ActionListener() {
			public void actionPerformed(final ActionEvent e) {
				LocationConnectionDialog.this.location = null;
				LocationConnectionDialog.this.setVisible(false);
			}
		});

		// Intercept the test button, which causes the schema
		// details as currently entered to be used to create
		// a temporary schema object, which is then tested.
		this.test.addActionListener(new ActionListener() {
			public void actionPerformed(final ActionEvent e) {
				final Schema testSchema = LocationConnectionDialog.this
						.createSchemaFromSettings();
				if (testSchema != null)
					LocationConnectionDialog.this.requestTestSchema(testSchema);
			}
		});

		// Intercept the execute button, which causes the
		// schema to be created as a temporary schema object. If
		// successful, the dialog closes.
		this.execute.addActionListener(new ActionListener() {
			public void actionPerformed(final ActionEvent e) {
				execute();
			}
		});
		
		this.name.addItemListener(this);
		JPanel urlPanel = this.createURLPanel();
		JPanel xmlPanel = this.createXMLPanel();
		JPanel filePanel = this.createFilePanel();
		
		
		typePanel.add(urlPanel,MartType.URL.toString());
		typePanel.add(schemaPanel,MartType.SOURCE.toString());
		typePanel.add(xmlPanel,MartType.XML.toString());
		typePanel.add(filePanel,MartType.FILE.toString());
		
		this.loadData = false;
		// Make the execute button the default button.
		this.getRootPane().setDefaultButton(this.execute);

		// Reset the fields to their default values.

		// Pack and resize the window.
		this.pack();

		// Move ourselves.
		this.setLocationRelativeTo(null);
	}
	
	private void execute() {
		if(this.martType.equals(MartType.SOURCE) || this.martType.equals(MartType.TARGET)) {
			// Assume we've failed.
			boolean passedTest = true;
	
/*			try {
				// Attempt to pass the test.
				passedTest = LocationConnectionDialog.this.test();
			} catch (final Throwable t) {
					// If we get an exception, we failed the test, and
					// should
					// tell the user why.
				passedTest = false;
				StackTrace.showStackTrace(t);
			}
*/	
				// Tell the user if we passed or failed.
			if (passedTest) {
				this.setVisible(false);
				String locName;
				boolean isNameEmpty = false;
				if(this.name.getSelectedItem() == null || (this.isEmpty(this.name.getSelectedItem().toString())))
					isNameEmpty = true;
				if(isNameEmpty)
					locName = this.connectionPanel.getHostPortValue();
				else
					locName = this.name.getSelectedItem().toString();
				long t1 = McUtils.getCurrentTime();	
				this.location = this.connectionPanel.createLocation(
						locName, this.isSourceSchema()? MartType.SOURCE: MartType.TARGET,this.currentLocations);
				long t2 = McUtils.getCurrentTime();
				if(this.location!=null && !isNameEmpty)
					this.location.storeInHistory();
				long t3 = McUtils.getCurrentTime();
				System.err.println("execute - createlocation "+(t2-t1));
				System.err.println("execute - store "+(t3-t2));
			} else
				JOptionPane.showMessageDialog(null, Resources
						.get("schemaTestFailed"), Resources
						.get("testTitle"), JOptionPane.ERROR_MESSAGE);	
		}else if(this.martType.equals(MartType.URL)) {
			this.setVisible(false);
			conObject = new DsConnectionObject();
			conObject.setType(MartType.URL);
			conObject.setDsInfoMap(this.urlMeta.getDBInfo(false));
			String host = this.urlTF.getText();
			int index = host.indexOf("://");
			host = host.substring(index+3);
			conObject.setHost(host);
			conObject.setPort(this.portTF.getText());
			conObject.setPath(this.pathTF.getText());
			conObject.setConfigMap(this.configMap);
			
			if(this.name.getSelectedItem()!=null && !this.name.getSelectedItem().equals("")) {
				String nameStr = this.name.getSelectedItem().toString();
				conObject.setName(nameStr);
				final Properties history = new Properties();
				history.setProperty("host", this.urlTF.getText());
				history.setProperty("path", this.pathTF.getText());
				history.setProperty("port", this.portTF.getText());
				Settings.saveHistoryProperties(MartType.class, nameStr, history);							
			} else
				conObject.setName(this.urlTF.getText()+this.portTF.getText());

		}
	}
	

	private JPanel createURLPanel() {
		JPanel urlPanel = new JPanel(new BorderLayout());
		JPanel northPanel = new JPanel(new FlowLayout());
		urlTF = new JTextField(20);
		urlTF.setText(Resources.get("biomartUrl"));
		portTF = new JTextField(5);
		portTF.setText("80");
		pathTF = new JTextField(32);
		pathTF.setText(Resources.get("biomartPath"));
		JLabel hostLabel = new JLabel("Host:");
		JLabel pathLabel = new JLabel("Path:");
		JLabel portLabel = new JLabel("Port:");
		JButton fetchButton = new JButton(Resources.get("getButton"));
		fetchButton.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent arg0) {
				String url = "";
				if(portTF.getText() == null || portTF.getText().equals(""))
					url = urlTF.getText()+pathTF.getText();
				else
					url = urlTF.getText()+":"+portTF.getText()+pathTF.getText();
				getMartsFromURL(url);
			}			
		});
		
		northPanel.add(hostLabel);
		northPanel.add(urlTF);
		northPanel.add(portLabel);
		northPanel.add(portTF);
		northPanel.add(pathLabel);
		northPanel.add(pathTF);
		northPanel.add(fetchButton);
		urlPanel.add(northPanel, BorderLayout.NORTH);
		urlMeta = new URLMetaTree();
		urlPanel.add(urlMeta.createGUI(),BorderLayout.CENTER);
		return urlPanel;
	}
	
	private void getMartsFromURL(String url){
		MyUtils.CHECK = true;
		MyUtils.EXCEPTION = true;
		MyUtils.EXIT_PROGRAM = false;

		Configuration urlConfiguration = new Configuration(url,false);
		this.urlMeta.setConfig(urlConfiguration);
		if(this.configMap==null)
			this.configMap = new HashMap<String,Configuration>();
		this.configMap.put(url,urlConfiguration);
		try {
			urlConfiguration.fetchMartSet();
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (JDOMException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (FunctionalException e) {
			e.printStackTrace();
		}
		List<String> martList = new ArrayList<String>();
		for (Iterator<String> it =urlConfiguration.virtualSchemaMartSetMap.keySet().iterator();it.hasNext();) {
			String currentServerVirtualSchema = it.next();

			Set<MartInVirtualSchema> martSet =
				urlConfiguration.virtualSchemaMartSetMap.get(currentServerVirtualSchema);
			for (MartInVirtualSchema mart : martSet) {
				martList.add(mart.martName);
			}
		}
		this.urlMeta.setItems(martList);
		this.urlMeta.updateTree(martList);
	}
	
	private JPanel createXMLPanel() {
		JPanel urlPanel = new JPanel(new FlowLayout());
		JTextField url = new JTextField(20);
		JLabel label = new JLabel(MartType.XML.toString());
		JButton upload = new JButton(Resources.get("fileButton"));
		urlPanel.add(label);
		urlPanel.add(url);
		urlPanel.add(upload);
		return urlPanel;
	}

	private JPanel createFilePanel() {
		JPanel urlPanel = new JPanel(new FlowLayout());
		JTextField url = new JTextField(20);
		JLabel label = new JLabel(MartType.FILE.toString());
		JButton upload = new JButton(Resources.get("fileButton"));
		urlPanel.add(label);
		urlPanel.add(url);
		urlPanel.add(upload);
		return urlPanel;
	}

	

	private void requestTestSchema(final Schema schema) {
		// Assume we've failed.
		boolean passedTest = false;

		try {
			// Attempt to pass the test.
			passedTest = schema.test();
		} catch (final Throwable t) {
			// If we get an exception, we failed the test, and should
			// tell the user why.
			passedTest = false;
			StackTrace.showStackTrace(t);
		}

		// Tell the user if we passed or failed.
		if (passedTest)
			JOptionPane.showMessageDialog(null, Resources
					.get("schemaTestPassed"), Resources.get("testTitle"),
					JOptionPane.INFORMATION_MESSAGE);
		else
			JOptionPane.showMessageDialog(null, Resources
					.get("schemaTestFailed"), Resources.get("testTitle"),
					JOptionPane.ERROR_MESSAGE);
	}

	private Schema createSchemaFromSettings() {
		// Refuse to create a temporary schema object if we can't validate it.
		if (!this.validateFields())
			return null;

		try {
			// Look up the type and use the appropriate schema type to
			// actually create the object.
			final MartType type = (MartType)this.type.getSelectedItem();
			if (type.equals(MartType.SOURCE) || type.equals(MartType.TARGET))
				return ((JDBCLocationConnectionPanel) this.connectionPanel)
						.createSchemaFromSettings((String) this.name
								.getSelectedItem());

			// What kind of type is it then??
			else
				throw new BioMartError();
		} catch (final Throwable t) {
			StackTrace.showStackTrace(t);
		}
		return null;
	}

	private boolean isEmpty(final String string) {
		// Return true if the string is null or empty.
		return string == null || string.trim().length() == 0;
	}


	private boolean validateFields() {
		// Make a list to hold messages.
		final List<String> messages = new ArrayList<String>();

//		if(this.name.getSelectedItem() == null || (this.isEmpty(this.name.getSelectedItem().toString())))
		// We don't like missing names.
//			messages.add(Resources.get("fieldIsEmpty", Resources.get("name")));

		// We don't like missing types either.
		if (this.type.getSelectedIndex() == -1)
			messages.add(Resources.get("fieldIsEmpty", Resources.get("type")));

		// If we have any messages, show them.
		if (!messages.isEmpty())
			JOptionPane.showMessageDialog(null,
					messages.toArray(new String[0]), Resources
							.get("validationTitle"),
					JOptionPane.INFORMATION_MESSAGE);

		// If there were no messages, then validated OK if the connection
		// panel also validated OK.
		return messages.isEmpty() && this.connectionPanel.validateFields(true);
	}

	
	public Location getMcLocation() {
		return this.location;
	}

	public boolean isSourceSchema() {
		if(this.martType.equals(MartType.TARGET) ||
				this.martType.equals(MartType.URL))
			return false;
		else
			return true;
	}

	public MartType getLocationType() {
		return this.martType;
	}
	
	public DsConnectionObject getConnectionObject() {
		return this.conObject;
	}

	private void updateNameReference(MartType type) {
		this.loadData = true;
		this.name.removeAllItems();
		List names = null;
		if(type.equals(MartType.SOURCE) || type.equals(MartType.TARGET)) {
			names = new ArrayList(
				Settings.getHistoryNamesForClass(LocationConnectionDialog.this.connectionPanel
								.getLocationClass()));
		}else if(type.equals(MartType.URL))
			names = new ArrayList(
					Settings.getHistoryNamesForClass(MartType.class));
			
		if(names==null)
			return;
		Collections.reverse(names);
		for (final Iterator i = names.iterator(); i.hasNext();)
			this.name.addItem(i.next());

		this.name.setSelectedIndex(-1);	
		this.loadData = false;
	}

	public void itemStateChanged(ItemEvent e) {
		if(e.getSource().equals(this.type)) {
			if (e.getStateChange() == ItemEvent.SELECTED) {
				CardLayout cl = (CardLayout)(typePanel.getLayout());
				MartType type = (MartType)this.type.getSelectedItem();
				this.updateNameReference(type);
				// JDBC specific stuff.
				if (type.equals(MartType.SOURCE) ||
						type.equals(MartType.TARGET)) {
					this.martType = type;
					cl.show(typePanel, MartType.SOURCE.toString());
					if (!(this.connectionPanel instanceof JDBCLocationConnectionPanel)) {
						connectionPanelHolder.removeAll();
						this.connectionPanel = new JDBCLocationConnectionPanel(type);
						connectionPanelHolder.add(this.connectionPanel);
					}else
						this.connectionPanel.setType(type);
					this.connectionPanel.clearDataBasesList();
				} else {
					this.martType = type;
					if(type.equals(MartType.URL)) {
						this.urlTF.setText(Resources.get("biomartUrl"));
						this.pathTF.setText(Resources.get("biomartPath"));
						this.portTF.setText("80");
						this.urlMeta.updateTree(null);
					}
					cl.show(typePanel, type.toString());
				} 
			}
			this.pack();
		}else if(e.getSource() == this.name) {
			if (e.getStateChange() == ItemEvent.SELECTED) {
				final String obj = (String)this.name.getSelectedItem();
				if(obj!=null && !obj.toString().equals("")) {
						// Load the schema settings from our history.
					switch(this.martType) {
					case SOURCE:
					case TARGET: {
						final Properties historyProps = Settings.getHistoryProperties(
								this.connectionPanel.getLocationClass(), (String) obj);				
										// Copy the settings, if we found any that matched.
						if (historyProps != null && !this.loadData) {
							this.connectionPanel.copySettingsFromProperties(historyProps);							
							this.connectionPanel.updateDataBasesList();
							this.pack();
						}
						break;
					}
					case URL: {
						final Properties historyProps = Settings.getHistoryProperties(
								MartType.class, (String) obj);				
										// Copy the settings, if we found any that matched.
						if (historyProps != null && !this.loadData) {
//							this.connectionPanel.copySettingsFromProperties(historyProps);							
//							this.connectionPanel.updateDataBasesList();
//							this.pack();
						}						
						break;
					}
					}
				}					
			}
		}
	}
 }
