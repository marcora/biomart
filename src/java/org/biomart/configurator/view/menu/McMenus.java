package org.biomart.configurator.view.menu;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.File;
import java.util.Enumeration;
import java.util.List;
import javax.swing.AbstractButton;
import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JToolBar;
import org.biomart.common.resources.Resources;
import org.biomart.common.resources.Settings;
import org.biomart.common.view.gui.dialogs.AddUserDialog;
import org.biomart.configurator.jdomUtils.JDomNodeAdapter;
import org.biomart.configurator.jdomUtils.JDomUtils;
import org.biomart.configurator.jdomUtils.XMLTreeCellRenderer;
import org.biomart.configurator.model.User;
import org.biomart.configurator.utils.McGuiUtils;
import org.biomart.configurator.utils.type.IdwViewType;
import org.biomart.configurator.utils.type.McGuiType;
import org.biomart.configurator.utils.type.McNewUserType;
import org.biomart.configurator.view.AttributeTable;
import org.biomart.configurator.view.FilterAction;
import org.biomart.configurator.view.MartConfigTree;
import org.biomart.configurator.view.McTreeScrollPane;
import org.biomart.configurator.view.idwViews.McViewAttTable;
import org.biomart.configurator.view.idwViews.McViewSchema;
import org.biomart.configurator.view.idwViews.McViews;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.input.SAXBuilder;

public class McMenus {
	private JRadioButton configurationButton;
	private JRadioButton guiButton;
	private JRadioButton sourceButton;
	private JRadioButton targetButton;
	private JRadioButton materializeButton;
	private JRadioButton allButton;
	private JRadioButton partitionButton;
	private JRadioButton updateButton;	
	private JButton addDatasetButton;
	private JButton addUserButton;	
	private MartConfigTree lTree;  
	private AttributeTable attrTable;	
	private JMenuItem newPortal;
	private JComboBox guiCB;
	private JComboBox usersCB;
	private ButtonGroup group;

  	private JToolBar createViewBar() {
	    JToolBar viewBar = new JToolBar();
	    
	    FilterAction fa = new FilterAction(this);
	        
	    configurationButton = new JRadioButton(Resources.get("CONFIGURATIONVIEW"));
	    configurationButton.setActionCommand(Resources.get("CONFIGURATIONVIEW"));
	    configurationButton.addActionListener(fa);
	    configurationButton.setSelected(true);
	    
	    guiButton = new JRadioButton(Resources.get("LINKVIEW"));
	    guiButton.setActionCommand(Resources.get("LINKVIEW"));
	    guiButton.addActionListener(fa);
	    
	    sourceButton = new JRadioButton(Resources.get("SOURCEVIEW"));
	    sourceButton.setActionCommand(Resources.get("SOURCEVIEW"));
	    sourceButton.addActionListener(fa);
	    
	    targetButton = new JRadioButton(Resources.get("TARGETVIEW"));
	    targetButton.setActionCommand(Resources.get("TARGETVIEW"));
	    targetButton.addActionListener(fa);
	    
	    materializeButton = new JRadioButton(Resources.get("MATERIALIZEVIEW"));
	    materializeButton.setActionCommand(Resources.get("MATERIALIZEVIEW"));
	    materializeButton.addActionListener(fa);
	    
	    updateButton = new JRadioButton(Resources.get("UPDATEVIEW"));
	    updateButton.setActionCommand(Resources.get("UPDATEVIEW"));
	    updateButton.addActionListener(fa);

	    allButton = new JRadioButton(Resources.get("ALLVIEWS"));
	    allButton.setActionCommand(Resources.get("ALLVIEWS"));
	    allButton.addActionListener(fa);
	    
	    partitionButton = new JRadioButton(Resources.get("PARTITIONVIEW"));
	    partitionButton.setActionCommand(Resources.get("PARTITIONVIEW"));
	    partitionButton.addActionListener(fa);
	
	    group = new ButtonGroup();
	    group.add(configurationButton);
	    group.add(guiButton);
	    group.add(sourceButton);
	    group.add(targetButton);
	    group.add(partitionButton);
	    group.add(materializeButton);
	    group.add(updateButton);
	    group.add(allButton);
	    
	    viewBar.add(configurationButton);
	    viewBar.add(guiButton);
	    viewBar.add(sourceButton);
	    viewBar.add(targetButton);
	    viewBar.add(partitionButton);
	    viewBar.add(materializeButton);
	    viewBar.add(updateButton);
//	    viewBar.add(allButton);
	    
	    this.setButtonsStatus(false);
	    return viewBar;
  	}

  	private JToolBar createButtonBar() {
  		JToolBar buttonBar = new JToolBar();
  		ImageIcon adddsImage = this.createImageIcon(Resources.get("ADDDSIMAGE"));
  		ImageIcon adduserImage = this.createImageIcon(Resources.get("ADDUSERIMAGE"));
  		
  		addDatasetButton = new JButton(adddsImage);
  		addUserButton = new JButton(adduserImage);
  		
  		addDatasetButton.setToolTipText(Resources.get("ADDDSTEXT"));
  		addUserButton.setToolTipText(Resources.get("ADDUSERTEXT"));
  		
  		buttonBar.add(addDatasetButton);
  		buttonBar.add(addUserButton);
  		
  		addDatasetButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if(McMenus.this.lTree == null)
					return;
				JDomNodeAdapter root = (JDomNodeAdapter)McMenus.this.lTree.getModel().getRoot();
				root.addLocations();
			} 			
  		});
  		
  		addUserButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				JDomNodeAdapter root = (JDomNodeAdapter)McMenus.this.lTree.getModel().getRoot();
				AddUserDialog auDialog = new AddUserDialog(root.getNode());
				if(auDialog.getUser() == null)
					return;
				addUser(auDialog.getUser());
				getSelectedButton().doClick();
			}
  			
  		});
  		return buttonBar;
  	}
  	
  	private JToolBar createDropDownBar() {
  		JToolBar dropdownBar = new JToolBar();
  		usersCB = new JComboBox();
  		usersCB.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				if(e.getStateChange() == ItemEvent.SELECTED) {
					final User user = (User)McMenus.this.usersCB.getSelectedItem();
					McGuiUtils.INSTANCE.setCurrentUser(user);
					getSelectedButton().doClick();
				}
			}  			
  		});
//  		usersCB.addItem(new User(Resources.get("ANONYMOUSUSER"),null));
  		
  		guiCB = new JComboBox();
  		guiCB.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				if (e.getStateChange() == ItemEvent.SELECTED) {
					final McGuiType item = (McGuiType)McMenus.this.guiCB.getSelectedItem();
					setGuiType(item);
					getSelectedButton().doClick();
				}
				
			} 			
  		});
  		guiCB.addItem(McGuiType.MARTVIEW);
  		guiCB.addItem(McGuiType.MARTREPORT);
  		guiCB.addItem(McGuiType.MARTSEARCH);
  		dropdownBar.add(usersCB);
  		dropdownBar.add(guiCB);
  		
  		
  		return dropdownBar;
  	}
  	
	private void setButtonsStatus(boolean b) {
		this.configurationButton.setEnabled(b);
		this.guiButton.setEnabled(b);
		this.sourceButton.setEnabled(b);
		this.targetButton.setEnabled(b);
		this.materializeButton.setEnabled(b);
		this.allButton.setEnabled(b);
		this.partitionButton.setEnabled(b);
		this.updateButton.setEnabled(b);
	}

	private void requestLoadPortal(File file) {
		// Open the file chooser.
		McViewSchema viewSchema = (McViewSchema)McViews.getInstance().getView(IdwViewType.SCHEMA);
		//clear schema
		viewSchema.clear();
		this.usersCB.removeAllItems();
		if(file==null) {
			final String currentDir = Settings.getProperty("currentOpenDir");
			final JFileChooser xmlFileChooser = new JFileChooser();
			xmlFileChooser.setCurrentDirectory(currentDir == null ? new File(".")
					: new File(currentDir));
			if (xmlFileChooser.showOpenDialog(McViews.getInstance().getView(IdwViewType.SCHEMA)) == JFileChooser.APPROVE_OPTION) {
				// Update the load dialog.
				Settings.setProperty("currentOpenDir", xmlFileChooser
						.getCurrentDirectory().getPath());
	
				final File loadFile = xmlFileChooser.getSelectedFile();
				openMcXML(loadFile);
			}
		}else {
			openMcXML(file);
		}
		this.setButtonsStatus(true);
		this.configurationButton.doClick();
	}

	private void requestSavePortal() {
		// Show a file chooser. If the user didn't cancel it, process the
		// response.
		final String currentDir = Settings.getProperty("currentSaveDir");
		final JFileChooser xmlFileChooser = new JFileChooser();
		xmlFileChooser.setCurrentDirectory(currentDir == null ? null
				: new File(currentDir));
		if (xmlFileChooser.showSaveDialog(McViews.getInstance().getView(IdwViewType.SCHEMA)) == JFileChooser.APPROVE_OPTION) {
			Settings.setProperty("currentSaveDir", xmlFileChooser
					.getCurrentDirectory().getPath());

			// Find out the file the user chose.
			final File saveAsFile = xmlFileChooser.getSelectedFile();

			// Skip the rest if they cancelled the save box.
			if (saveAsFile == null)
				return;
			//update subclassrelation in datasets
			McViewSchema viewSchema = (McViewSchema)McViews.getInstance().getView(IdwViewType.SCHEMA);
			viewSchema.updateSubclassRelation((JDomNodeAdapter)lTree.getModel().getRoot());
			//update synchronize user;
			viewSchema.updateSynUserList((JDomNodeAdapter)lTree.getModel().getRoot());
			lTree.getModel().save(saveAsFile);

			// Save it, and save the reference to the XML file for later.
//			this.martXMLFile.put(currentMart, saveAsFile);
//			this.requestSaveMart();
			// Save XML filename in history of accessed files.
//			final Properties history = new Properties();
//			history.setProperty("location", saveAsFile.getPath());
//			Settings.saveHistoryProperties(MartTabSet.class, this
//					.suggestTabName(currentMart, false), history);
		}

	}
	
	  /**
   	* Creates the frame tool bar.
   *
   * @return the frame tool bar
   */
  	public JPanel createToolBar() {
  		JPanel toolBarsPanel = new JPanel(new BorderLayout());
  		toolBarsPanel.add(this.createButtonBar(),BorderLayout.WEST);
	    toolBarsPanel.add(this.createViewBar(),BorderLayout.EAST);
  		toolBarsPanel.add(this.createDropDownBar(),BorderLayout.CENTER);
	    return toolBarsPanel;
  	}

  	/**
  	 * Creates the frame menu bar.
  	 *
  	 * @return the menu bar
  	 */
  	public JMenuBar createMenuBar() {
  		JMenuBar mbar = new JMenuBar();
  		mbar.add(createMcTreeMenu());
  		return mbar;
  	}
  	
  	private JMenu createMcTreeMenu() {
  		JMenu fileMenu = new JMenu("File");
  	  
  		newPortal = new JMenuItem("New");
  		newPortal.addActionListener(new ActionListener() {

  		public void actionPerformed(ActionEvent e) {
  			File file = new File(Resources.get("EMPTYPORTAL"));
  				requestLoadPortal(file);
  			} 		  
  		});
  	  
  		JMenuItem openMcXML = new JMenuItem("Open");
  		openMcXML.addActionListener(new ActionListener() {
  			public void actionPerformed(ActionEvent e) {
  				requestLoadPortal(null);
  			}
  		});
  	  
  		JMenuItem saveMcXML = new JMenuItem("Save");
  		saveMcXML.addActionListener(new ActionListener() {
  			public void actionPerformed(ActionEvent e) {
  				requestSavePortal();
  			}
  		});
  		  	  
  		fileMenu.add(newPortal);
  		fileMenu.add(openMcXML);
  		fileMenu.add(saveMcXML);
  		return fileMenu;
    }

	private void openMcXML(File xmlFile) {
		Document document = null;
		try {
			SAXBuilder saxBuilder = new SAXBuilder("org.apache.xerces.parsers.SAXParser", false);
			document = saxBuilder.build(xmlFile);
		}
		catch (Exception e) {
			e.printStackTrace();
			System.exit(1);
		}
		this.createTreeViews(document);	
		this.createUsersFromXML(document);
	}

	private void createTreeViews(Document document) {
		lTree = new MartConfigTree(document);
        XMLTreeCellRenderer treeCellRenderer = new XMLTreeCellRenderer();
        lTree.setCellRenderer(treeCellRenderer);
        McTreeScrollPane ltreeScrollPane = new McTreeScrollPane(lTree);
                
        attrTable = new AttributeTable(lTree);
        lTree.setAttributeTable(attrTable);
        JScrollPane tableScroll = new JScrollPane(attrTable);
        
		McViews.getInstance().getView(IdwViewType.MCTREE).setComponent(ltreeScrollPane);
		McViews.getInstance().getView(IdwViewType.ATTRIBUTETABLE).setComponent(tableScroll);
		
		lTree.addTreeSelectionListener((McViewAttTable)McViews.getInstance().getView(IdwViewType.ATTRIBUTETABLE));
		McViewSchema viewSchema = (McViewSchema)McViews.getInstance().getView(IdwViewType.SCHEMA);
		//clear schema
		lTree.addTreeSelectionListener(viewSchema);
	}
	
	private void createUsersFromXML(Document document) {
		Element martusersElement = document.getRootElement().getChild(Resources.get("MARTUSERS"));
		List<Element> userList = martusersElement.getChildren(Resources.get("USER"));
		for(Element user:userList) {
			User newUser = new User(user.getAttributeValue(Resources.get("NAME")), Resources.get("PASSWORD"));
			this.usersCB.addItem(newUser);
		}
	}
	
    /** Returns an ImageIcon, or null if the path was invalid. */
    private ImageIcon createImageIcon(String path) {
         return new ImageIcon(path);
    }

    public void newPortal() {
    	if(this.newPortal!=null)
    		this.newPortal.doClick();
    }
    
    private void setGuiType(McGuiType type) {
    	McGuiUtils.INSTANCE.setGuiType(type);
//    	McEventObject obj = new McEventObject(EventType.Update_McGuiType,type);
//    	((McViewSchema)McViews.getInstance().getView(IdwViewType.SCHEMA)).getController().processV2Cupdate(obj);
    }
    
    public void setEnableDropDownMenus(boolean b) {
    	this.guiCB.setEnabled(b);
    	this.usersCB.setEnabled(b);
    	this.addUserButton.setEnabled(b);
    }

    private void addUser(User user) {
    	this.usersCB.addItem(user);
    	this.usersCB.setSelectedItem(user);
    	//add user to XML
    	JDomNodeAdapter root = (JDomNodeAdapter)this.lTree.getModel().getRoot();
    	Element usersElement = root.getNode().getChild(Resources.get("MARTUSERS"));
    	Element userElement = new Element(Resources.get("USER"));
    	userElement.setAttribute(Resources.get("NAME"),user.getUserName());
    	userElement.setAttribute("password",user.getPassword());
    	usersElement.addContent(userElement);
    	
    	if(user.getType()==McNewUserType.COPY) { //copy 
    		//find all locations
    		List<Element> locList = JDomUtils.searchElementListInUser(root.getNode(), Resources.get("LOCATION"), null,
    				user.getSynchronizedUser());
    		for(Element loc:locList) {
    			Element tmpElement = (Element)loc.clone();
    			tmpElement.detach();
    			tmpElement.setAttribute(Resources.get("USER"),user.getUserName());
    			root.getNode().addContent(tmpElement);
    		}
    	}else if(user.getType() == McNewUserType.SYNCHRONIZE) { //synchronize
    		//find all locations
    		List<Element> locList = JDomUtils.searchElementListInUser(root.getNode(), Resources.get("LOCATION"), null,
    				user.getSynchronizedUser());
    		for(Element loc:locList) {
    			String userStr = loc.getAttributeValue(Resources.get("USER"));
    			loc.setAttribute(Resources.get("USER"),userStr+","+user.getUserName());
    		}   
    		McGuiUtils.INSTANCE.setUserSynchronized(user.getSynchronizedUser(), user.getUserName());
    	}
    }
    
    private JRadioButton getSelectedButton() {
    	Enumeration<AbstractButton> buttons = this.group.getElements();
    	while(buttons.hasMoreElements()) {
    		JRadioButton b = (JRadioButton)buttons.nextElement();
    		if(b.isSelected())
    			return b;
    	}
    	return null;   		
    }
    

 }