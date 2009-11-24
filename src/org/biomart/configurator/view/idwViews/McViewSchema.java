package org.biomart.configurator.view.idwViews;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.Font;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Observable;
import java.util.Set;
import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.TreePath;
import org.biomart.builder.exceptions.ValidationException;
import org.biomart.builder.model.Column;
import org.biomart.builder.model.ComponentStatus;
import org.biomart.builder.model.DataSet;
import org.biomart.builder.model.DataSetTable;
import org.biomart.builder.model.DataSets;
import org.biomart.builder.model.InheritedColumn;
import org.biomart.builder.model.Key;
import org.biomart.builder.model.Mart;
import org.biomart.builder.model.Relation;
import org.biomart.builder.model.Schema;
import org.biomart.builder.model.Schemas;
import org.biomart.builder.model.WrappedColumn;
import org.biomart.builder.model.ForeignKey;
import org.biomart.builder.model.PrimaryKey;
import org.biomart.builder.model.JDBCSchema;
import org.biomart.builder.model.Table;
import org.biomart.builder.view.gui.diagrams.DataSetDiagram;
import org.biomart.builder.view.gui.diagrams.SchemaDiagram;
import org.biomart.builder.view.gui.dialogs.MartRunnerMonitorDialog;
import org.biomart.builder.view.gui.dialogs.SaveDDLDialog;
import org.biomart.common.exceptions.AssociationException;
import org.biomart.common.exceptions.DataModelException;
import org.biomart.common.resources.Resources;
import org.biomart.common.utils.Transaction;
import org.biomart.configurator.component.DsInConfigPanel;
import org.biomart.configurator.component.PartitionTablePanel;
import org.biomart.configurator.jdomUtils.JDomNodeAdapter;
import org.biomart.configurator.jdomUtils.JDomUtils;
import org.biomart.configurator.model.Location;
import org.biomart.configurator.model.McModel;
import org.biomart.configurator.utils.DbConnectionInfoObject;
import org.biomart.configurator.utils.McEventObject;
import org.biomart.configurator.utils.McGuiUtils;
import org.biomart.configurator.utils.McUtils;
import org.biomart.configurator.utils.type.Cardinality;
import org.biomart.configurator.utils.type.DataSetTableType;
import org.biomart.configurator.utils.type.IdwViewType;
import org.biomart.configurator.utils.type.JdbcType;
import org.biomart.configurator.utils.type.MartType;
import org.biomart.configurator.utils.type.McGuiType;
import org.biomart.configurator.utils.type.McViewType;
import org.biomart.configurator.view.MartConfigTree;
import org.biomart.configurator.view.McView;
import org.biomart.configurator.view.gui.diagrams.DsTableComponent;
import org.biomart.configurator.view.gui.diagrams.FakeBoxDiagram;
import org.biomart.configurator.view.gui.diagrams.LinkComponent;
import org.biomart.configurator.view.gui.diagrams.LinkedPortableDiagram;
import org.biomart.configurator.view.gui.diagrams.SchemaPanel;
import org.ewin.common.util.Log;
import org.jdom.Element;

public class McViewSchema extends McView implements TreeSelectionListener {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	// for everything that doesn't need to show in McViewSchema
	private JPanel emptyPanel = new JPanel();

	private McViewType mcViewType;

	public McViewSchema(String title, Icon icon, Component component,
			McModel model, IdwViewType type) {
		super(title, icon, component, model, type);
	}

	@Override
	public void update(Observable observable, Object obj) {
		switch (((McEventObject) obj).getEventType()) {
		/*
		 * case Request_NewMartStart: { MartTabSet mts =
		 * (MartTabSet)this.getComponent(); //s is "location;martname" String s
		 * = ((McEventObject)obj).getObject().toString(); String[] s1 =
		 * s.split(";"); if(mts.requestNewMart(s1[0],s1[1])) { McEventObject
		 * endObject = new
		 * McEventObject(EventType.Request_NewMartEnd,mts.getSelectedMartTab
		 * ().getMart()); endObject.SetContextString(s);
		 * this.getController().processV2Cupdate(endObject); } break; }
		 */
		case Request_NewLocation: {
			long t1 = McUtils.getCurrentTime();
			if(((McEventObject) obj).getObject() instanceof Location) {
				Location loc = (Location) ((McEventObject) obj).getObject();
				McGuiUtils.INSTANCE.getCurrentUser().addLocation(loc);
			}
			long t2 = McUtils.getCurrentTime();
			System.err.println("update righthandside "+(t2-t1));
			break;
		}
		case Update_McViewType: {
			this.mcViewType = (McViewType) ((McEventObject) obj).getObject();
			break;
		}
		case Update_McGuiType:
			this.updateGUIComponent();
			break;
		}
	}

	public void valueChanged(TreeSelectionEvent e) {
		Object lpc = e.getPath().getLastPathComponent();
		if (lpc instanceof JDomNodeAdapter) {
			JDomNodeAdapter node = (JDomNodeAdapter) lpc;
			this.getViewProperties().setTitle(node.getNode().getName());
			this.showComponent(node);
		}
	}

	public void showComponent(JDomNodeAdapter treeNode) {
		if (!McGuiUtils.INSTANCE.getGuiType().equals(McGuiType.MARTVIEW)) {
			if (this.mcViewType.equals((McViewType.CONFIGURATION))) {
				this.updateGUIComponent();
				return;
			}
		}

		Element node = treeNode.getNode();
		String type = node.getName();
		if (type.equals(Resources.get("MARTREGISTRY"))) {
			this.showMartRegistry(treeNode);
		} else if (type.equals(Resources.get("SOURCESCHEMA"))) {
			this.showJDBCSchemas(treeNode);
		} else if (type.equals(Resources.get("DATASET"))) {
			this.showDataSet(treeNode);
		}
		/**
		 * for now, the below nodes will always go to dataset
		 */
		else if (type.equals(Resources.get("CONTAINER"))
				|| type.equals(Resources.get("FILTER"))
				|| type.equals(Resources.get("ATTRIBUTEPOINTER"))
				|| type.equals(Resources.get("TABLE"))
				|| type.equals(Resources.get("COLUMN"))
				|| type.equals(Resources.get("ATTRIBUTE"))) {
			// show dataset
			Element dsElement = treeNode.findAncestorElement(
					treeNode.getNode(), Resources.get("DATASET"));
			this.showDataSet(new JDomNodeAdapter(dsElement));
		} else if (type.equals(Resources.get("DSTABLE"))) {
			if (this.mcViewType.equals(McViewType.PARTITION)) {
				List<Element> ptList = this.findAppliedPtTable(treeNode);
				this.showPartitions(ptList);
			} else {
				Element dsElement = treeNode.findAncestorElement(treeNode
						.getNode(), Resources.get("DATASET"));
				this.showDataSet(new JDomNodeAdapter(dsElement));
			}

		} else if (type.equals(Resources.get("GUI"))) {
			this.showChildrenBoxes(node);
		} else if (type.equals(Resources.get("CONFIGPOINTER"))) {
			this.showSelfBox(node);
		} else if (type.equals(Resources.get("PARTITIONTABLE"))) {
			this.showPartitionTable(treeNode);
		} else {
			this.showCard(this.emptyPanel);
		}
	}

	private void showPartitions(List<Element> ptList) {
		SchemaPanel sp = new SchemaPanel();
		for (Element pt : ptList) {
			JPanel ptPanel = new PartitionTablePanel(pt);
			sp.add(ptPanel);
		}
		this.showCard(sp.getScrollPane());
	}

	private void showPartitionTable(JDomNodeAdapter treeNode) {
		JPanel content = new PartitionTablePanel(treeNode.getNode());
		this.showCard(content);
	}


	private void showDsInConfig(JDomNodeAdapter treeNode) {
		Element node = treeNode.getNode();
		//dsPanel {mainPanel {partitonPanel,contentPanel}, buttonPanel,resultPanel }
		JPanel dsPanel = new JPanel(new BorderLayout());
		JPanel mainPanel = new JPanel(new BorderLayout());
		JPanel ptPanel = new JPanel();
		JPanel contentPanel = new JPanel(new FlowLayout());
		JPanel buttonPanel = new JPanel(new FlowLayout());
		JPanel resultPanel = new JPanel();

		resultPanel.setBorder(new TitledBorder(new EtchedBorder(), "Result"));
		JButton searchButton = new JButton("Search");
		buttonPanel.add(searchButton);
		//if has processor, add button
		List<Element>processorList = JDomUtils.searchElementList(node, Resources.get("PROCESSOR"), null);
		List<Element> currentProNodes = JDomUtils.getCurrentConfigElements(processorList);
		for(Element pe: currentProNodes) {
			JButton pieButton = new JButton(pe.getAttributeValue(Resources.get("NAME")));
			buttonPanel.add(pieButton);
		}
		
		contentPanel.add(new DsInConfigPanel(node));
		// find all linked datasets;
		List<Element> linkedDSList = JDomUtils.findLinkedDataSets(treeNode, true);
		if(null==linkedDSList || linkedDSList.size() == 0) {
			
		}else {
			for (Element linkedDs : linkedDSList) {
				if(JDomUtils.isElementHiden(linkedDs))
					continue;
				contentPanel.add(new DsInConfigPanel(linkedDs));
			}
		}
		//if has schema partitions
		List<Element> pteList = JDomUtils.searchElementList(node, Resources.get("PARTITIONTABLE"), null);
		Element partitionElement = null;
		if(pteList!=null)
			for(Element pte:pteList) {
				String type = pte.getAttributeValue(Resources.get("TYPE"));
				if("Schema".equals(type)) {
					partitionElement = pte;
					break;
				}
			}
		if(partitionElement!=null) {
			
		}
		else	
			mainPanel.add(contentPanel);
		dsPanel.add(mainPanel, BorderLayout.NORTH);
		dsPanel.add(resultPanel, BorderLayout.SOUTH);
		dsPanel.add(buttonPanel, BorderLayout.CENTER);

		JScrollPane scroll = new JScrollPane(dsPanel);
		this.showCard(scroll);
	}

	private void showCard(final JComponent card) {
		if (card == null) {
			return;
		}
		final JPanel panel = (JPanel) this.getComponent();
		if (panel.getComponents().length > 0)
			panel.remove(0);
		panel.add(card);
		panel.validate();
		panel.repaint();
		this.repaint();
	}


	private void showChildrenBoxes(Element node) {
		/*
		 * FakeBoxDiagram fald = null; int locationTabCount =
		 * this.locationTabSet.getTabCount(); for(int i=0; i<locationTabCount;
		 * i++) { Object obj = this.locationTabSet.getComponentAt(i); if(obj
		 * instanceof FakeBoxDiagram) { fald = (FakeBoxDiagram)obj; } }
		 * 
		 * if(fald == null) { fald = new FakeBoxDiagram(node,true); } else
		 * fald.resetNode(node,true);
		 */
		FakeBoxDiagram fald = new FakeBoxDiagram(node, true);
		JScrollPane scroll = new JScrollPane(fald);
		this.showCard(scroll);
	}

	private void showSelfBox(Element node) {
		FakeBoxDiagram fbd = new FakeBoxDiagram(node, false);
		JScrollPane scroll = new JScrollPane(fbd);
		this.showCard(scroll);
	}

	private void showDataSet(JDomNodeAdapter treeNode) {
		Element node = treeNode.getNode();
		if (this.mcViewType.equals(McViewType.CONFIGURATION)) {
			this.showDsInConfig(treeNode);
		} else if (this.mcViewType.equals(McViewType.TARGET)) {
			String datasetName = node.getAttributeValue(Resources.get("NAME"));
			Element mart = node.getParentElement();
			String martName = mart.getAttributeValue(Resources.get("NAME"));
			Element location = mart.getParentElement();
			String locationName = location.getAttributeValue(Resources
					.get("NAME"));
			Location loc = McGuiUtils.INSTANCE.getCurrentUser()
					.getLocation(locationName);
			if(loc==null) {
				loc = this.createLocationFromXML(treeNode);
				McGuiUtils.INSTANCE.getCurrentUser().addLocation(loc);
			}
	//		Marts mts = loc.getMartTabSet();
			Mart martObj = loc.getMart(martName);
			//if mart is empty, add mart from xml
			if(martObj==null) {
				this.createMartFromXML(loc, mart);
			}
			DataSets dsts = loc.getMart(martName).getDataSetObj();	
			DataSetDiagram dsd = dsts.getDataSetDiagram(datasetName);
			if(dsd==null) {
				Mart currentMart = loc.getMart(martName);
				this.createDataSetFromXML(currentMart, node);
				//TODO create dataset from xml
				this.showSelfBox(mart);
				return;
			}
			JScrollPane sch = new JScrollPane(dsd);
			sch.getViewport().setBackground(dsd.getBackground());
			sch.getHorizontalScrollBar().addAdjustmentListener(dsd);
			sch.getVerticalScrollBar().addAdjustmentListener(dsd);

			Transaction.start(false);
			final JPanel panel = (JPanel) this.getComponent();
			if (panel.getComponents().length > 0)
				panel.remove(0);
			panel.add(sch);
			panel.validate();
			panel.repaint();
			Transaction.end();
		} else if (this.mcViewType.equals(McViewType.SOURCE)) {
			this.showSchemas(treeNode);
		} else if (this.mcViewType.equals(McViewType.LINK)) {
			this.showLinkedPortables(treeNode);
		} else if (this.mcViewType.equals(McViewType.PARTITION)) {
			List<Element> ptList = JDomUtils.searchElementList(treeNode
					.getNode(), Resources.get("PARTITIONTABLE"), null);
			this.showPartitions(ptList);
		} else if(this.mcViewType.equals(McViewType.MATERIALIZE)) {
			this.showMartRunner(node);
		} else if(this.mcViewType.equals(McViewType.UPDATE)) {
			this.showUpdateTime(node);
		}else
			this.showSelfBox(node);
	}

	private void showAllPortables(JDomNodeAdapter treeNode) {
		LinkedPortableDiagram content = new LinkedPortableDiagram(treeNode,
				true);

		JScrollPane scroll = new JScrollPane(content);
		this.showCard(scroll);
		Set<LinkComponent> set = content.getLinkComponents();
		for (LinkComponent item : set) {
			content.add(item, 0);
		}

	}

	private void showLinkedPortables(JDomNodeAdapter treeNode) {
		// treeNode is dataset
		LinkedPortableDiagram content = new LinkedPortableDiagram(treeNode,false);
		JScrollPane scroll = new JScrollPane(content);
		Set<LinkComponent> set = content.getLinkComponents();
		for (JComponent item : set) {
			content.add(item, 0);
		}
		this.showCard(scroll);
	}

	private void showMartRegistry(JDomNodeAdapter treeNode) {
		if (this.mcViewType.equals(McViewType.CONFIGURATION)) {
			this.showAllDataSets(treeNode);
		} else if (this.mcViewType.equals(McViewType.LINK)) {
			this.showAllPortables(treeNode);
		} else {
			this.showCard(this.emptyPanel);
		}
	}

	private void showAllDataSets(JDomNodeAdapter treeNode) {

		JPanel biomartPanel = new JPanel(new BorderLayout());
		JPanel datasetPanel = new JPanel();
		JPanel titlePanel = new JPanel(new BorderLayout());
		JLabel label1 = new JLabel("Welcome to BioMart Portal");
		label1.setFont(new Font("sansserif", Font.BOLD, 20));
		JLabel label2 = new JLabel("DataSets:");
		titlePanel.add(label1,BorderLayout.NORTH);
		titlePanel.add(label2,BorderLayout.CENTER);
		JScrollPane sp1 = new JScrollPane(datasetPanel);

		biomartPanel.add(titlePanel,BorderLayout.NORTH);		
		biomartPanel.add(sp1,BorderLayout.CENTER);
		datasetPanel.setBorder(BorderFactory.createRaisedBevelBorder());
		
		List<Element> dsList = JDomUtils.searchElementList(treeNode.getNode(),
				Resources.get("DATASET"), null);
		for (Element ds : dsList) {
			if(JDomUtils.isElementHiden(ds))
				continue;
			String userlist = JDomUtils.getUserForElement(ds);
			String user = McGuiUtils.INSTANCE.getCurrentUser().getUserName();
			if(JDomUtils.isUserMatched(user, userlist)) {
				DsTableComponent dsComponent = new DsTableComponent(ds
						.getAttributeValue(Resources.get("NAME")), "");
				datasetPanel.add(dsComponent);
			}
		}
		this.showCard(biomartPanel);
	}

	private void showSchemas(JDomNodeAdapter treeNode) {
		//if only one, show jdbcshema
		Element node = treeNode.getNode();
		List<Element> jsList = node.getChildren(Resources.get("SOURCESCHEMA"));
		if(jsList.size()==1) {
			this.showJDBCSchemas(new JDomNodeAdapter(jsList.get(0)));
		}else {
			// All Schemas
			String schemaName = Resources.get("multiSchemaOverviewTab");
			// dataset->mart
			Element mart = node.getParentElement();
			String martName = mart.getAttributeValue(Resources.get("NAME"));
			Element location = mart.getParentElement();
			String locationName = location.getAttributeValue(Resources.get("NAME"));
			Location loc = McGuiUtils.INSTANCE.getCurrentUser()
					.getLocation(locationName);
			if (loc== null) {
				loc = this.createLocationFromXML(treeNode);
				McGuiUtils.INSTANCE.getCurrentUser().addLocation(loc);
			}
			Mart martObj = loc.getMart(martName);
			if(martObj==null) {
				this.createMartFromXML(loc, mart);
			}
			Schemas sts = loc.getMart(martName).getSchemasObj();
			SchemaDiagram sd = ((JDBCSchema)sts.getSchema(schemaName)).getSchemaDiagram();
			JScrollPane sch = new JScrollPane(sd);
			sch.getViewport().setBackground(sd.getBackground());
			sch.getHorizontalScrollBar().addAdjustmentListener(sd);
			sch.getVerticalScrollBar().addAdjustmentListener(sd);

			this.showCard(sch);
		}
	}

	private void showJDBCSchemas(JDomNodeAdapter treeNode) {
		Element node = treeNode.getNode();


			String schemaName = node.getAttributeValue(Resources.get("NAME"));
			// jdbcschema->dataset->mart
			Element mart = node.getParentElement().getParentElement();
			String martType = mart.getAttributeValue(Resources.get("TYPE"));
			if(null == martType|| !martType.equals(Resources.get("MARTTYPESOURCE"))) {
				this.showCard(this.emptyPanel);
				return;
			}
			
			//only show source if the mart is from source
			String martName = mart.getAttributeValue(Resources.get("NAME"));
			Element location = mart.getParentElement();
			String locationName = location.getAttributeValue(Resources
					.get("NAME"));
			Location loc = McGuiUtils.INSTANCE.getCurrentUser().getLocation(locationName);
			if(loc == null) {
				loc = this.createLocationFromXML((JDomNodeAdapter)treeNode.getParent());
				McGuiUtils.INSTANCE.getCurrentUser().addLocation(loc);
			}
	//		Marts mts = loc.getMartTabSet();
			Mart  martObj = loc.getMart(martName);
			if(martObj==null) {
				this.createMartFromXML(loc, mart);
			}
			Schemas sts = loc.getMart(martName).getSchemasObj();
			SchemaDiagram sd = ((JDBCSchema)sts.getSchema(schemaName)).getSchemaDiagram();
			JScrollPane sch = new JScrollPane(sd);
			sch.getViewport().setBackground(sd.getBackground());
			sch.getHorizontalScrollBar().addAdjustmentListener(sd);
			sch.getVerticalScrollBar().addAdjustmentListener(sd);
			
			this.showCard(sch);

	}

	private void updateGUIComponent() {
		if (McGuiUtils.INSTANCE.getGuiType().equals(McGuiType.MARTVIEW)) {
			MartConfigTree tree = ((McViewTree) McViews.getInstance().getView(
					IdwViewType.MCTREE)).getMcTree();
			TreePath oldPath = tree.getSelectionPath();
			JDomNodeAdapter lastNode = null;
			if (oldPath != null) {
				lastNode = (JDomNodeAdapter) oldPath.getLastPathComponent();
				this.showComponent(lastNode);
			} else
				this.showComponent((JDomNodeAdapter) tree.getModel().getRoot());

		} else if (McGuiUtils.INSTANCE.getGuiType()
				.equals(McGuiType.MARTREPORT)
				&& this.mcViewType.equals(McViewType.CONFIGURATION)) {
			ImageIcon icon = new ImageIcon(Resources.get("REPORTIMAGE"));
			this.showImagePanel(icon);
		} else if (McGuiUtils.INSTANCE.getGuiType()
				.equals(McGuiType.MARTSEARCH)
				&& this.mcViewType.equals(McViewType.CONFIGURATION)) {
			ImageIcon icon = new ImageIcon(Resources.get("SEARCHIMAGE"));
			this.showImagePanel(icon);
		}
	}

	private void showImagePanel(ImageIcon icon) {
		JPanel reportPanel = new JPanel(new BorderLayout());
		JLabel label = new JLabel();
		label.setIcon(icon);
		reportPanel.add(label, BorderLayout.CENTER);

		JPanel idPanel = new JPanel(new FlowLayout());
		JLabel geneLabel = new JLabel("Gene: ");
		JTextField geneId = new JTextField(20);
		geneId.setText("ENSG00000221622");
		JButton button = new JButton("Search");
		idPanel.add(geneLabel);
		idPanel.add(geneId);
		idPanel.add(button);
		reportPanel.add(idPanel, BorderLayout.NORTH);
		JScrollPane scroll = new JScrollPane(reportPanel);
		this.showCard(scroll);

	}

	private List<Element> findAppliedPtTable(JDomNodeAdapter treeNode) {
		List<Element> appliedPtList = new ArrayList<Element>();
		String dsTableName = treeNode.getNode().getAttributeValue(
				Resources.get("NAME"));
		JDomNodeAdapter dsNode = (JDomNodeAdapter) treeNode.getParent()
				.getParent();
		List<Element> ptList = JDomUtils.searchElementList(dsNode.getNode(),
				Resources.get("PARTITIONTABLE"), null);
		for (Element pt : ptList) {
			if (dsTableName.equals(pt.getAttributeValue(Resources
					.get("DSTABLE")))) {
				appliedPtList.add(pt);
			}
		}
		return appliedPtList;
	}

	/**
	 * Create a location, marts, datasets, schemas from XML
	 * 
	 * @param treeNode
	 *            -- a dataset treeNode
	 * @throws AssociationException
	 */
	private Location createLocationFromXML(JDomNodeAdapter treeNode) {
		Element dsElement = treeNode.getNode();
		// get location name, mart name
		Element martElement = treeNode.findAncestorElement(dsElement,
				Resources.get("MART"));
		Element locElement = martElement.getParentElement();
		// construct a location
		Location loc = new Location(locElement.getAttributeValue(Resources.get("NAME")));
		this.createMartFromXML(loc, martElement);
		return loc;
	}

	/**
	 * construct all datasets in this mart
	 * @param Location
	 * @param martElement
	 */
	private void createMartFromXML(Location loc, Element martElement) {
		String typeStr = martElement.getAttributeValue(Resources.get("TYPE"));
		MartType martType = Resources.get("MARTTYPESOURCE").equals(typeStr) ? MartType.SOURCE:MartType.TARGET; 
		Mart mart = new Mart(loc, martElement
				.getAttributeValue(Resources.get("NAME")),martType);
		Map<String, Table> tableMap = new HashMap<String, Table>();
		Map<String, Key> keyMap = new HashMap<String, Key>();
		// construct jdbcSchema
		List<Element> schemaList = JDomUtils.searchElementList(martElement, Resources.get("SOURCESCHEMA"), null);
		List<Relation> subRelations = new ArrayList<Relation>();
		for (Element se : schemaList) {
			String schemaName = se.getAttributeValue(Resources.get("NAME"));
			if(mart.getSchemas().get(schemaName)!=null)
				continue;
			DbConnectionInfoObject conObj = new DbConnectionInfoObject(
					se.getAttributeValue("url"),
					se.getAttributeValue("databaseName"),"",
					se.getAttributeValue("userName"), 
					se.getAttributeValue("password"),
					JdbcType.MySQL,
					//JdbcType.valueOf(se.getAttributeValue("driverClassName"))
					se.getAttributeValue("regex"),
					se.getAttributeValue("expression")
					);
			Schema schema = new JDBCSchema(mart, conObj, se
					.getAttributeValue("schemaName"),  schemaName, true, "", "");
			mart.addSchema(schema);
			// construct table
			List<Element> teList = JDomUtils.searchElementList(se, Resources
					.get("TABLE"), null);
			for (Element te : teList) {
				Table table = new Table(schema, te.getAttributeValue(Resources
						.get("NAME")));
				tableMap.put(table.getName(), table);
				schema.getTables().put(table.getName(), table);
				// construct column
				List<Element> ceList = JDomUtils.searchElementList(te,
						Resources.get("COLUMN"), null);
				for (Element cd : ceList) {
					Column column = new Column(table, cd
							.getAttributeValue(Resources.get("NAME")));
					table.getColumns().put(column.getName(), column);
				}
				// fk
				List<Element> fkeList = JDomUtils.searchElementList(te,
						Resources.get("ForeignKey"), null);
				for (Element fke : fkeList) {
					final ComponentStatus status = ComponentStatus
							.get((String) fke.getAttributeValue(Resources
									.get("Status")));
					String colStr = fke.getAttributeValue(Resources
							.get("inColumns"));
					String[] colArray = colStr.split(",");
					Column[] colsArr = new Column[colArray.length];
					for (int index = 0; index < colArray.length; index++) {
						colsArr[index] = (Column) table.getColumns().get(
								colArray[index]);
					}
					ForeignKey fk = new ForeignKey(colsArr);
					fk.setStatus(status);
					table.getForeignKeys().add(fk);
					keyMap.put(table.getName() + "." + colStr, fk);
				}
				// pk
				Element pkElement = JDomUtils.searchElement(te, Resources
						.get("PrimaryKey"), null);
				if (pkElement != null) {
					final ComponentStatus status = ComponentStatus
							.get((String) pkElement.getAttributeValue(Resources
									.get("Status")));
					String colStr = pkElement.getAttributeValue(Resources
							.get("inColumns"));
					String[] colArray = colStr.split(",");
					Column[] colsArr = new Column[colArray.length];
					for (int index = 0; index < colArray.length; index++) {
						colsArr[index] = (Column) table.getColumns().get(
								colArray[index]);
					}
					PrimaryKey pk = new PrimaryKey(colsArr);
					pk.setStatus(status);
					table.setPrimaryKey(pk);
					keyMap.put(table.getName() + "." + colStr, pk);
				}
			} //end of for
			// relation
			List<Element> reList = JDomUtils.searchElementList(se, Resources
					.get("RELATION"), null);
			for (Element re : reList) {
				final ComponentStatus status = ComponentStatus.get((String) re
						.getAttributeValue(Resources.get("Status")));
				String cardinality = re.getAttributeValue("cardinality");
				if(cardinality==null || cardinality.equals(""))
					cardinality = "M(a)";
				final Cardinality card = Cardinality.get(cardinality);
				String originalCard = re.getAttributeValue("originalCardinality");
				if(originalCard==null || originalCard.equals(""))
					originalCard = "M(a)";
				final Cardinality origCard = Cardinality.get(originalCard);
				String fKeyStr = re.getAttributeValue("firstTable")+"."+
					re.getAttributeValue(Resources.get("FirstKey"));
				String sKeyStr = re.getAttributeValue("secondTable")+"."+
					re.getAttributeValue(Resources.get("SecondKey"));
				Key fKey = keyMap.get(fKeyStr);
				Key sKey = keyMap.get(sKeyStr);
				if (fKey == null || sKey == null) {
				} else {
					Relation rel;
					try {
						rel = new Relation(fKey, sKey, card);
						fKey.getRelations().add(rel);
						sKey.getRelations().add(rel);
						if (origCard != null)
							rel.setOriginalCardinality(origCard);
						rel.setStatus(status);
						if(null!=re.getAttributeValue("subclass") && re.getAttributeValue("subclass").equals("true")) 
							subRelations.add(rel);
					} catch (AssociationException e) {
						e.printStackTrace();
					}

				}
			}
		}
		//construct datasets
		List<Element> dseList = JDomUtils.searchElementList(martElement, Resources.get("DATASET"), null);
		for(Element dse:dseList) {
			final String dsname = dse.getAttributeValue(Resources.get("NAME"));
			final String centralTableName = dse.getAttributeValue("centralTable");
			final boolean invisible = Boolean.valueOf(dse.getAttributeValue("invisible")).booleanValue();
			final boolean masked = Boolean.valueOf(
					dse.getAttributeValue("masked")).booleanValue();
			final boolean hideMasked = Boolean.valueOf(
					dse.getAttributeValue("hideMasked")).booleanValue();
			//subclass relations
			List<Element> scRelationList = dse.getChildren(Resources.get("SubclassRelation"));
			for(Element scRelation:scRelationList) {
				String fKeyStr = scRelation.getAttributeValue(Resources.get("FirstKey"));
				String sKeyStr = scRelation.getAttributeValue(Resources.get("SecondKey"));
				final Cardinality card = Cardinality.get(scRelation.getAttributeValue("cardinality"));
				Key fKey = keyMap.get(fKeyStr);
				Key sKey = keyMap.get(sKeyStr);
				if (fKey == null || sKey == null) {
				} else {
					Relation rel = this.findRelation(mart, fKey, sKey);
					subRelations.add(rel);
				}
			}
			
			try {
				Table centralTable = tableMap.get(centralTableName);
				if(centralTable==null) {
					System.err.println(centralTableName);
					continue;
				}
				final DataSet ds = new DataSet(mart,tableMap.get(centralTableName), dsname);
				for(Relation subrelation:subRelations) {
					subrelation.setSubclassRelation(ds, true);
				}
				ds.setMasked(masked);
				ds.setInvisible(invisible);
				ds.setHideMasked(hideMasked);
				mart.addDataSet(ds);
			} catch (ValidationException e) {
				e.printStackTrace();
			}			
		}
		loc.addMart(mart);
		for(Iterator i = mart.getDataSets().values().iterator();i.hasNext();)
			try {
				((DataSet)(i.next())).synchronise();
			} catch (DataModelException e) {
				e.printStackTrace();
			} catch (SQLException e) {
				e.printStackTrace();
			}
	}

	private void createDataSetFromXML(Mart mart, Element dsElement) {

		Element schemaElement = dsElement.getChild(Resources.get("SOURCESCHEMA"));
		String schemaName = schemaElement.getAttributeValue(Resources.get("NAME"));

		DbConnectionInfoObject conObj = new DbConnectionInfoObject(
				schemaElement.getAttributeValue("url"),
				schemaElement.getAttributeValue("databaseName"),"",
				schemaElement.getAttributeValue("userName"), 
				schemaElement.getAttributeValue("password"),
				JdbcType.valueOf(schemaElement.getAttributeValue("driverClassName")),
				schemaElement.getAttributeValue("regex"),
				schemaElement.getAttributeValue("expression")
				);
		Schema schema = new JDBCSchema(mart, conObj, schemaElement
				.getAttributeValue(Resources.get("NAME")),  schemaName, true, "", "");
		mart.addSchema(schema);
		// construct table
		List<Element> teList = schemaElement.getChildren(Resources.get("TABLE"));
		List<Relation> subRelations = new ArrayList<Relation>();
		Map<String, Table> tableMap = new HashMap<String, Table>();
		Map<String, Key> keyMap = new HashMap<String, Key>();

		for (Element te : teList) {
			Table table = new Table(schema, te.getAttributeValue(Resources.get("NAME")));
			tableMap.put(table.getName(), table);
			schema.getTables().put(table.getName(), table);
			// construct column
			List<Element> ceList = te.getChildren(Resources.get("COLUMN"));
			for (Element cd : ceList) {
				Column column = new Column(table, cd
					.getAttributeValue(Resources.get("NAME")));
				table.getColumns().put(column.getName(), column);
			}
			// fk
			List<Element> fkeList = te.getChildren(Resources.get("ForeignKey"));
			for (Element fke : fkeList) {
				final ComponentStatus status = ComponentStatus
					.get((String) fke.getAttributeValue(Resources
							.get("Status")));
				String colStr = fke.getAttributeValue(Resources
					.get("inColumns"));
				String[] colArray = colStr.split(",");
				Column[] colsArr = new Column[colArray.length];
				for (int index = 0; index < colArray.length; index++) {
					colsArr[index] = (Column) table.getColumns().get(
						colArray[index]);
				}
				ForeignKey fk = new ForeignKey(colsArr);
				fk.setStatus(status);
				table.getForeignKeys().add(fk);
				keyMap.put(table.getName() + "." + colStr, fk);
			}
			// pk
			Element pkElement = te.getChild(Resources.get("PrimaryKey"));
			if (pkElement != null) {
				final ComponentStatus status = ComponentStatus
					.get((String) pkElement.getAttributeValue(Resources
							.get("Status")));
				String colStr = pkElement.getAttributeValue(Resources
					.get("inColumns"));
				String[] colArray = colStr.split(",");
				Column[] colsArr = new Column[colArray.length];
				for (int index = 0; index < colArray.length; index++) {
					colsArr[index] = (Column) table.getColumns().get(
						colArray[index]);
				}
				PrimaryKey pk = new PrimaryKey(colsArr);
				pk.setStatus(status);
				table.setPrimaryKey(pk);
				keyMap.put(table.getName() + "." + colStr, pk);
			}
		} //end of for
		// relation
		List<Element> reList = schemaElement.getChildren(Resources.get("RELATION"));
		for (Element re : reList) {
			final ComponentStatus status = ComponentStatus.get((String) re
					.getAttributeValue(Resources.get("Status")));
			String cardinality = re.getAttributeValue("cardinality");
			if(cardinality==null || cardinality.equals(""))
				cardinality = "M(a)";
			final Cardinality card = Cardinality.get(cardinality);
			String originalCard = re.getAttributeValue("originalCardinality");
			if(originalCard==null || originalCard.equals(""))
				originalCard = "M(a)";
			final Cardinality origCard = Cardinality.get(originalCard);
			String fKeyStr = re.getAttributeValue("firstTable")+"."+
				re.getAttributeValue(Resources.get("FirstKey"));
			String sKeyStr = re.getAttributeValue("secondTable")+"."+
				re.getAttributeValue(Resources.get("SecondKey"));
			Key fKey = keyMap.get(fKeyStr);
			Key sKey = keyMap.get(sKeyStr);
			if (fKey == null || sKey == null) {
			} else {
				Relation rel;
				try {
					rel = new Relation(fKey, sKey, card);
					fKey.getRelations().add(rel);
					sKey.getRelations().add(rel);
					if (origCard != null)
						rel.setOriginalCardinality(origCard);
					rel.setStatus(status);
					if(null!=re.getAttributeValue("subclass") && re.getAttributeValue("subclass").equals("true")) 
						subRelations.add(rel);
				} catch (AssociationException e) {
					e.printStackTrace();
				}

			}
		}
		//construct dataset
		final String dsname = dsElement.getAttributeValue(Resources.get("NAME"));
		final String centralTableName = dsElement.getAttributeValue("centralTable");
		//TODO change to visible
		final boolean invisible = Boolean.valueOf(dsElement.getAttributeValue("invisible")).booleanValue();
		final boolean masked = Boolean.valueOf(
				dsElement.getAttributeValue("masked")).booleanValue();
		final boolean hideMasked = Boolean.valueOf(
				dsElement.getAttributeValue("hideMasked")).booleanValue();
		//subclass relations
		List<Element> scRelationList = dsElement.getChildren(Resources.get("SubclassRelation"));
		for(Element scRelation:scRelationList) {
			String fKeyStr = scRelation.getAttributeValue(Resources.get("FirstKey"));
			String sKeyStr = scRelation.getAttributeValue(Resources.get("SecondKey"));
			final Cardinality card = Cardinality.get(scRelation.getAttributeValue("cardinality"));
			Key fKey = keyMap.get(fKeyStr);
			Key sKey = keyMap.get(sKeyStr);
			if (fKey == null || sKey == null) {
			} else {
				Relation rel = this.findRelation(mart, fKey, sKey);
				subRelations.add(rel);
			}
		}
		
		try {
			Table centralTable = tableMap.get(centralTableName);
			if(centralTable==null) {
				System.err.println(centralTableName);
				return;
			}
			final DataSet ds = new DataSet(mart,tableMap.get(centralTableName), dsname);
			for(Relation subrelation:subRelations) {
				subrelation.setSubclassRelation(ds, true);
			}
			ds.setMasked(masked);
			ds.setInvisible(invisible);
			ds.setHideMasked(hideMasked);
			//synchronise later
//			ds.synchronise();
			mart.addDataSet(ds);
			ds.synchronise();
		} catch (ValidationException e) {
			e.printStackTrace();
		} catch (DataModelException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		

	}
	/*
	 * clear all subclass relation first, subclass 
	 */
	public void updateSubclassRelation(JDomNodeAdapter root) {
		Iterator<Location> li = McGuiUtils.INSTANCE
				.getCurrentUser().getLocationMap().values().iterator();
		while(li.hasNext()) {
			Location loc = li.next();
			Iterator<Mart> mi = loc.getMarts().values().iterator();
			while(mi.hasNext()) {
				Mart mart = mi.next();
				for(Iterator<Schema> si = mart.getSchemas().values().iterator(); si.hasNext();) {
					Schema schema = (Schema)si.next();
					for (final Iterator<Relation> ri = schema.getRelations().iterator(); ri.hasNext();) {
						Relation relation = (Relation)ri.next();
						for(Iterator<DataSet> dsi = mart.getDataSets().values().iterator(); dsi.hasNext();) {
							DataSet dataset = (DataSet)dsi.next();
							if(relation.isSubclassRelation(dataset)) {
								Element locElement = JDomUtils.searchElement(root.getNode(), Resources.get("LOCATION"), loc.getName());
								Element martElement = JDomUtils.searchElement(locElement, Resources.get("MART"), mart.getMartName());
								Element dsElement = JDomUtils.searchElement(martElement, 
										Resources.get("DATASET"), dataset.getName());
								Element srElement = new Element(Resources.get("SubclassRelation"));
								srElement.setAttribute("cardinality",relation.getCardinality().getName());
								srElement.setAttribute("originalCardinality",relation.getOriginalCardinality().getName());
								srElement.setAttribute(Resources.get("Status"),relation.getStatus().toString());
								
								Column[] colArray = relation.getFirstKey().getColumns();
								String tmpColumns = "";
								for(int colIndex=1; colIndex<colArray.length; colIndex++) {
									tmpColumns = tmpColumns+","+colArray[colIndex].getTable().getName()+"."+colArray[colIndex].getName();
								}
								tmpColumns = colArray[0].getTable().getName()+"."+colArray[0].getName()+tmpColumns;						
								
								srElement.setAttribute(Resources.get("FirstKey"),tmpColumns);
								Column[] colArray2 = relation.getSecondKey().getColumns();
								
								tmpColumns = "";
								for(int colIndex=1; colIndex<colArray2.length; colIndex++) {
									tmpColumns = tmpColumns+","+colArray2[colIndex].getTable().getName()+"."+colArray2[colIndex].getName();
								}
								tmpColumns = colArray2[0].getTable().getName()+"."+colArray2[0].getName()+tmpColumns;						

								srElement.setAttribute(Resources.get("SecondKey"),tmpColumns);	
								dsElement.addContent(srElement);
							}
						}
					}
				}
				
			}
		}
	}
	
	public void updateSynUserList(JDomNodeAdapter root) {
		List<ArrayList<String>> synUserList = McGuiUtils.INSTANCE.getSynchronizedUserList();
		if(synUserList.size()==0)
			return;
		Element synUserTable = root.getNode().getChild(Resources.get("SYNUSERTABLE"));
		if(synUserTable==null) {
			synUserTable = new Element(Resources.get("SYNUSERTABLE"));
			root.getNode().addContent(synUserTable);
		} else
			synUserTable.removeContent();
		for(ArrayList<String> synlist: synUserList) {
			String tmp = synlist.get(0);
			for(int i=1;i<synlist.size(); i++) 
				tmp = tmp+","+synlist.get(i);
			Element synusers = new Element(Resources.get("SYNUSERS"));
			synusers.setAttribute(Resources.get("USER"),tmp);
			synUserTable.addContent(synusers);
		}
		
	}
	
	private Relation findRelation(Mart mart, Key firstKey, Key secondKey) {
		for(Iterator<Schema> si = mart.getSchemas().values().iterator(); si.hasNext(); ) {
			Schema schema = (Schema)si.next();
			for(Iterator<Relation> ri = schema.getRelations().iterator(); ri.hasNext();){
				Relation relation = (Relation)ri.next();
				if((relation.getFirstKey().equals(firstKey) && relation.getSecondKey().equals(secondKey)) || 
						(relation.getSecondKey().equals(firstKey) && relation.getFirstKey().equals(secondKey)))
					return relation;
			}
		}
		return null;
	}
	
	public void clear() {
		McGuiUtils.INSTANCE.getUserMap().clear();
	}

	public void updateDataSet(JDomNodeAdapter treeNode) {
		Element dsElement = treeNode.getNode();
		//find dataset in mbuilder		
		String locName = dsElement.getParentElement().getParentElement().getAttributeValue(Resources.get("NAME"));
		String martName = dsElement.getParentElement().getAttributeValue(Resources.get("NAME"));
		String dsName = dsElement.getAttributeValue(Resources.get("NAME"));
		Location loc = McGuiUtils.INSTANCE.getCurrentUser().getLocation(locName);
		if(loc==null) //hack for now
			return;
		if(loc.getMarts()==null)
			return;
		Mart mart = loc.getMarts().get(martName);
		if(mart==null)
			return;
		if(mart.getDataSets() == null)
			return;
		DataSet dataset = (DataSet)mart.getDataSets().get(dsName);
		if(dataset == null)
			return;
		//update dataset table
		List<Element> dsTableList = JDomUtils.searchElementList(dsElement, Resources.get("DSTABLE"), null);
		
		//find the one has the same user and gui
		HashMap<String, String> conditions = new HashMap<String, String>();
//		conditions.put(Resources.get("USER"), McGuiUtils.INSTANCE.getCurrentUser().getUserName());
//		conditions.put(Resources.get("GUI"), McGuiUtils.INSTANCE.getGuiType().toString());
		List<Element> myContainers = JDomUtils.findChildElements(dsElement, Resources.get("CONTAINER"), conditions);	
		List<Column> newColList = new ArrayList<Column>();
		Set<ColumnObject> unUsedColSet = new HashSet<ColumnObject>();	
		List<Element> oldColEList = new ArrayList<Element>();
		
		for(Element containerE:myContainers) {
			this.getAllColsFromContainer(containerE, unUsedColSet, oldColEList);
		}	
		
		//remove all dataset tables
		for(Element dsTable:dsTableList) {
			dsElement.removeContent(dsTable);
		}

		//add new dstable element
		for(Iterator ti = dataset.getTables().values().iterator();ti.hasNext();){
			DataSetTable table = (DataSetTable)ti.next();
			//masked?
			if(table.isMasked())
				continue;
			if(table.isDimensionMasked())
				continue;
			Element dstElement = new Element(Resources.get("DSTABLE"));
			dstElement.setAttribute(Resources.get("NAME"),table.getName());
			DataSetTableType type = table.getType();
			if(type.equals(DataSetTableType.MAIN))
				dstElement.setAttribute("type","0");
			else if(type.equals(DataSetTableType.MAIN_SUBCLASS))
				dstElement.setAttribute("type","1");
			else
				dstElement.setAttribute("type","2");

			dsElement.addContent(dstElement);
			//attribute
			if(table==null || table.getColumns() == null || table.getColumns().values() == null) { //unpredicable error 
				Log.ERROR("UPDATE DATASET ERROR");
				return;
			}
				
			for (final Iterator<Column> ci = table.getColumns().values().iterator(); ci.hasNext();) {
				final Column col = ci.next();
				final Element colElement = new Element(Resources.get("ATTRIBUTE"));
				dstElement.addContent(colElement);
				colElement.setAttribute("name",col.getName());
				//no masked for now
				colElement.setAttribute(Resources.get("maskColumnTitle"),"0");
				
				//get all unused, new column
				String colName = col.getName();
				ColumnObject obj = new ColumnObject(table.getName(),colName);
				if(unUsedColSet.contains(obj)) {
					unUsedColSet.remove(obj);
				}
				else
					newColList.add(col);
			}			
		}
		
		//update containers, remove unused column
		if(unUsedColSet.size()>0) {
			for(Iterator<Element> it = oldColEList.iterator(); it.hasNext();) {
				Element colE = it.next();
				String name = colE.getAttributeValue(Resources.get("TARGETFIELD")); 
				String dsTable = colE.getAttributeValue(Resources.get("DSTABLE"));
				ColumnObject obj = new ColumnObject(dsTable,name);
				if(unUsedColSet.contains(obj)) {				
					//remove from container
					Element parent = colE.getParentElement();
					parent.removeContent(colE);
					//remove parent if empty
					if(parent.getContentSize()==0) {
						Element pp = parent.getParentElement();
						pp.removeContent(parent);
					}
					it.remove();				
				}
			}
		}
		//add new Columns in a new container
		if(newColList.size()>0) {
			Element newFilterCon = new Element(Resources.get("CONTAINER"));
			newFilterCon.setAttribute(Resources.get("NAME"),"New Filters");
			Element newAttributeCon = new Element(Resources.get("CONTAINER"));
			newAttributeCon.setAttribute(Resources.get("NAME"),"New Attributes");
			
			//add them to filter/attribute containers, if not found, add them to dataset
			Element fcElement = JDomUtils.searchElementWithCondition(dsElement, Resources.get("CONTAINER"), Resources.get("NAME"), Resources.get("FILTER"));
			if(fcElement==null)
				dsElement.addContent(newFilterCon);
			else
				fcElement.addContent(newFilterCon);
			
			Element acElement = JDomUtils.searchElementWithCondition(dsElement, Resources.get("CONTAINER"), 
					Resources.get("NAME"), Resources.get("ATTRIBUTE"));
			if(acElement==null)
				dsElement.addContent(newAttributeCon);
			else
				acElement.addContent(newAttributeCon);
			//add filters and attributes to the new container
			for(Column col:newColList) {
				//attribute
				Element attributeElement = new Element(Resources.get("ATTRIBUTEPOINTER"));
				attributeElement.setAttribute(Resources.get("NAME"),col.getName());
				attributeElement.setAttribute(Resources.get("TARGETFIELD"),col.getName());
				attributeElement.setAttribute(Resources.get("POINTER"),"false");
    			if(col instanceof WrappedColumn) {
    				attributeElement.setAttribute(Resources.get("SOURCEFIELD"),
    						((WrappedColumn)col).getWrappedColumn().getName());
    				attributeElement.setAttribute(Resources.get("SOURCETABLE"),
    						((WrappedColumn)col).getWrappedColumn().getTable().getName());
    			}else if(col instanceof InheritedColumn) {
    				attributeElement.setAttribute(Resources.get("SOURCEFIELD"),
    						((InheritedColumn)col).getInheritedColumn().getName());
    				attributeElement.setAttribute(Resources.get("SOURCETABLE"),
    						((InheritedColumn)col).getInheritedColumn().getTable().getName());    				
    			}
    			attributeElement.setAttribute(Resources.get("LOCATION"),locName);
    			attributeElement.setAttribute(Resources.get("MART"),martName);
    			attributeElement.setAttribute(Resources.get("VERSION"),"");
    			attributeElement.setAttribute(Resources.get("DATASET"),dsName);
    			attributeElement.setAttribute(Resources.get("CONFIG"),"naive");
    			attributeElement.setAttribute(Resources.get("DSTABLE"),col.getTable().getName());
    			attributeElement.setAttribute(Resources.get("SOURCERANGE"),"");
    			attributeElement.setAttribute(Resources.get("TARGETRANGE"),"");
    			attributeElement.setAttribute(Resources.get("REPORT"),"true");
    			attributeElement.setAttribute(Resources.get("DISPLAYNAME"),col.getName());    	
				newAttributeCon.addContent(attributeElement);
				
				//filter
    			Element filterElement = new Element(Resources.get("FILTER"));
    			filterElement.setAttribute(Resources.get("NAME"), col.getName());
    			filterElement.setAttribute(Resources.get("POINTER"),"false");
    			filterElement.setAttribute(Resources.get("TARGETFIELD"),col.getName());
    			if(col instanceof WrappedColumn) {
    				filterElement.setAttribute(Resources.get("SOURCEFIELD"),
    						((WrappedColumn)col).getWrappedColumn().getName());
    				filterElement.setAttribute(Resources.get("SOURCETABLE"),
    						((WrappedColumn)col).getWrappedColumn().getTable().getName());
    			}
    			filterElement.setAttribute(Resources.get("LOCATION"),locName);
    			filterElement.setAttribute(Resources.get("MART"),martName);
    			filterElement.setAttribute(Resources.get("VERSION"),"");
    			filterElement.setAttribute(Resources.get("DATASET"),dsName);
    			filterElement.setAttribute(Resources.get("CONFIG"),"naive");
    			filterElement.setAttribute(Resources.get("DSTABLE"),col.getTable().getName());
    			filterElement.setAttribute(Resources.get("SOURCERANGE"),"");
    			filterElement.setAttribute(Resources.get("TARGETRANGE"),"");
    			filterElement.setAttribute(Resources.get("REPORT"),"true");
    			filterElement.setAttribute(Resources.get("DISPLAYNAME"),col.getName());    	
    			newFilterCon.addContent(filterElement);
			}
		}
		McGuiUtils.refreshGui(treeNode);
	}
	
	private void getAllColsFromContainer(Element containerElement, Set<ColumnObject> colNameSet, List<Element> colEList) {
		Iterator it = containerElement.getDescendants(); 
		while(it.hasNext()) {
			Object o = it.next();
			if(o instanceof Element) {
				Element e = (Element)o;
				if(e.getName().equals(Resources.get("FILTER")) ||
						e.getName().equals(Resources.get("ATTRIBUTEPOINTER"))) {
					colNameSet.add(new ColumnObject(e.getAttributeValue(Resources.get("DSTABLE")),
							e.getAttributeValue(Resources.get("TARGETFIELD"))));
					colEList.add(e);
				}
			}
		}
	}
	
	private class ColumnObject {
		private String colName;
		private String tableName;
		
		public ColumnObject(String tName, String cName) {
			colName = cName;
			tableName = tName;
		}
		
		public String getColumnName() {
			return colName;
		}
		
		public String getTableName() {
			return tableName;
		}
		
		@Override
		public boolean equals(Object o) {
			if(!(o instanceof ColumnObject))
				return false;
			if(((ColumnObject)o).getColumnName().equals(colName) && 
					((ColumnObject)o).getTableName().equals(tableName)) {
				return true;
			}else
				return false;
		}
		
		@Override
		public int hashCode() {
			final int PRIME = 31;
			return PRIME+colName.hashCode()+tableName.hashCode();
		}
	}

	public void updateSchema(JDomNodeAdapter treeNode) {
		//treeNode is a dataset
		Element martElement = treeNode.getNode().getParentElement();
		//set update time
		treeNode.getNode().setAttribute(Resources.get("TIME"),McUtils.getCurrentTimeString());
		//only update rdbms type for now
		if(!Resources.get("RDBMSTYPE").equals(martElement.getAttributeValue(Resources.get("TYPE")))) {
			McGuiUtils.refreshGui(treeNode);
			return;
		}
		//find the schema element, assume only one child at this time
		Element schemaElement = treeNode.getNode().getChild(Resources.get("SOURCESCHEMA"));
		String schemaName = schemaElement.getAttributeValue(Resources.get("NAME"));
		String martName = martElement.getAttributeValue(Resources.get("NAME"));			
		Element locElement = martElement.getParentElement();
		Location loc = McGuiUtils.INSTANCE.getCurrentUser().getLocation(locElement.getAttributeValue(Resources.get("NAME")));
	//	Marts mts = loc.getMartTabSet();
		Mart mart = loc.getMart(martName);
		Schema schema = (Schema)mart.getSchemas().get(schemaName);
		JDBCSchema jschema = (JDBCSchema)schema;
		DbConnectionInfoObject conObj = new DbConnectionInfoObject(schemaElement.getAttributeValue("url"),
				schemaElement.getAttributeValue("databaseName"),"",schemaElement.getAttributeValue("username"),
				schemaElement.getAttributeValue(Resources.get("PASSWORD")),
				JdbcType.valueOf(schemaElement.getAttributeValue("driverClassName")),
				schemaElement.getAttributeValue("regex"),
				schemaElement.getAttributeValue("expression"));
		jschema.setDataLinkDatabase(schemaElement.getAttributeValue("databaseName"));
		jschema.setDataLinkSchema(schemaElement.getAttributeValue("schemaName"));
		jschema.setKeyGuessing(true);
		jschema.setConnectionObject(conObj);
		mart.getSchemasObj().requestSynchroniseSchema(schema,true);
	}

	public void requestRunDDL(JDomNodeAdapter treeNode) {
		//dataset
		Element martElement = treeNode.getNode().getParentElement();
		Element locationElement = martElement.getParentElement();
		Location loc = McGuiUtils.INSTANCE.getCurrentUser().getLocation(locationElement.getAttributeValue(Resources.get("NAME")));
		// If the mart has no datasets, ignore the request.
		final Mart mart = loc.getMart(martElement.getAttributeValue(Resources.get("NAME")));
		final List<DataSet> datasets = new ArrayList<DataSet>(mart.getDataSets().values());
		// Remove partition table datasets from the list.
		// Also remove masked datasets.
		for (final Iterator<DataSet> i = datasets.iterator(); i.hasNext();) {
			final DataSet ds = i.next();
			if ( ds.isMasked())
				i.remove();
		}
		if (datasets.size() == 0)
			JOptionPane.showMessageDialog(null, Resources
					.get("noDatasetsToGenerate"),
					Resources.get("messageTitle"),
					JOptionPane.INFORMATION_MESSAGE);
		else
			// Open the DDL creation dialog and let it do it's stuff.
			(new SaveDDLDialog(mart, datasets, null,
					SaveDDLDialog.RUN_DDL)).setVisible(true);
	}

	private void showMartRunner(Element dsNode) {
		JPanel panel = MartRunnerMonitorDialog.monitor("","");
		this.showCard(panel);
	}

	public void showMartRunnerPanel(JPanel panel){
		this.showCard(panel);
	}

	private void showUpdateTime(Element dsNode) {
		String updatetime = dsNode.getAttributeValue("time");
		String timelabel = "Last Update: "+updatetime;
		JPanel panel = new JPanel();
		JLabel label = new JLabel(timelabel);
		panel.add(label);
		this.showCard(panel);
	}
		
	public Map<String, Location> getLocations() {
		return McGuiUtils.INSTANCE.getCurrentUser().getLocationMap();
	}
}