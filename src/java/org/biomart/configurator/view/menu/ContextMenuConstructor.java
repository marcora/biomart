package org.biomart.configurator.view.menu;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.tree.TreePath;

import org.biomart.common.resources.Resources;
import org.biomart.common.view.gui.SwingWorker;
import org.biomart.common.view.gui.dialogs.DsTablesDialog;
import org.biomart.common.view.gui.dialogs.ProgressDialog;
import org.biomart.common.view.gui.dialogs.StackTrace;
import org.biomart.configurator.jdomUtils.JDomNodeAdapter;
import org.biomart.configurator.jdomUtils.JDomUtils;
import org.biomart.configurator.utils.McGuiUtils;
import org.biomart.configurator.utils.type.IdwViewType;
import org.biomart.configurator.utils.type.PortableType;
import org.biomart.configurator.view.MartConfigTree;
import org.biomart.configurator.view.idwViews.McViewSchema;
import org.biomart.configurator.view.idwViews.McViews;
import org.biomart.configurator.view.gui.dialogs.AddProcessorDialog2;
import org.biomart.configurator.view.gui.dialogs.LinkItemsDialog;
import org.biomart.configurator.view.gui.dialogs.MultiPortableDialog;
import org.jdom.input.SAXBuilder;
import org.jdom.Document;
import org.jdom.Element;


/**
 * A singleton class that read contextMenu.xml and construct the popup menu 
 * corresponding to the selected node
 * 
 * @author yliang
 *
 */
public class ContextMenuConstructor implements ActionListener {
	private String contextMenuXML = "data/contextMenu.xml";
	private JPopupMenu contextMenu;
	private static ContextMenuConstructor instance = null;
	private Element root;
	private MartConfigTree ltree;
	private int xPoint, yPoint;

	
	public static ContextMenuConstructor getInstance() {
		if(instance==null)
			instance = new ContextMenuConstructor();
		return instance;
	}
		
	private ContextMenuConstructor () {
		
		contextMenu = new JPopupMenu();
	    try {
	       // Build the document with SAX and Xerces, no validation
	       SAXBuilder builder = new SAXBuilder();
	       // Create the document
	       Document doc = builder.build(new File(contextMenuXML));
	       root = doc.getRootElement();
	    } catch (Exception e) {
	       e.printStackTrace();
	    }
	}
	
	@SuppressWarnings("unchecked")
	//uncheck the warning message from jdom
	public JPopupMenu getContextMenu(String name, MartConfigTree ltree, int x, int y) {
		this.ltree = ltree;
		this.xPoint = x;
		this.yPoint = y;
		contextMenu.removeAll();
		List<Element> nodeList = root.getChildren();
		if (nodeList == null) 
			return null;
		for (Element nodeElement: nodeList) {
			if(nodeElement.getAttributeValue("name").equals(name)) {
				List<Element> childAttr = nodeElement.getChildren();
				if(childAttr==null) 
					return null;
				for (Element item:childAttr) {
					boolean shows = true;
					String inViews = item.getAttributeValue("inViews");
					if(inViews!=null) {
						shows = false;
						String[] inViewsArray = inViews.split(",");
						String currentView = McGuiUtils.INSTANCE.getMcViewType().toString();
						for(String viewItem:inViewsArray) {
							if(viewItem.equalsIgnoreCase(currentView)) {
								shows = true;
								break;
							}
						}
					}
					if(shows) {
						//get submenu
						List<Element> subMenu = item.getChildren();
						if(subMenu.size()>0) {
							JMenu menu = new JMenu(item.getAttributeValue("title"));
							contextMenu.add(menu);
							for(Element subItem:subMenu){
								JMenuItem menuItem = new JMenuItem(subItem.getAttributeValue("title"));
								menuItem.addActionListener(this);
								menuItem.setActionCommand(subItem.getAttributeValue("name"));
								if(isMenuDisabled(subItem)) 
									menuItem.setEnabled(false);
								menu.add(menuItem);
							}
						}else {
							JMenuItem menuItem = new JMenuItem(item.getAttributeValue("title"));
							menuItem.addActionListener(this);
							menuItem.setActionCommand(item.getAttributeValue("name"));
							if(isMenuDisabled(item))
								menuItem.setEnabled(false);
						    contextMenu.add(menuItem);
						}
					}
				}
				break;
			}
		}
		return this.contextMenu;
	}
	
	private boolean isMenuDisabled(Element element) {
		String disabled = element.getAttributeValue("disabled");
		if("1".equals(disabled))
			return true;
		//hard code for materialize and partition
		if(element.getAttributeValue("title").equals("Materialize") ||
				element.getAttributeValue("title").equals("partition")) {
			if(ltree == null)
				return false;
			TreePath pathTarget = ltree.getPathForLocation(xPoint,yPoint);
			JDomNodeAdapter node = (JDomNodeAdapter) pathTarget.getLastPathComponent();
			if(node == null) 
				return false;
			//only for source schema
			//node is dataset
			String martType = node.getNode().getParentElement().getAttributeValue(Resources.get("TYPE"));
			if(Resources.get("MARTTYPESOURCE").equals(martType))
				return false;
			else
				return true;
		} else if(element.getAttributeValue("name").equals("hide4user")) {
			//disable if it is not synchronized user
			List<String> users = McGuiUtils.INSTANCE.getSynchronizedUserList(McGuiUtils.INSTANCE.getCurrentUser().getUserName());
			if(users == null || users.size() == 0)
				return true;
			else
				return false;
		} else if(element.getAttributeValue("name").equals("addLinkedDataSet") ||
				element.getAttributeValue("name").equals("addLinkIndices")) {
			//disable if no link available
			if(ltree == null)
				return false;
			TreePath pathTarget = ltree.getPathForLocation(xPoint,yPoint);
			JDomNodeAdapter node = (JDomNodeAdapter) pathTarget.getLastPathComponent();
			if(node == null) 
				return false;
			//node is dataset
			List<Element> linkedDSList = JDomUtils.findLinkedDataSets(node, false);
			if(linkedDSList==null || linkedDSList.size()==0)
				return true;
			else
				return false;
		}
		return false;
	}
	
	public void actionPerformed(final ActionEvent e) {
		if (ltree == null) return;
		TreePath pathTarget = ltree.getPathForLocation(xPoint,yPoint);
		final JDomNodeAdapter node = (JDomNodeAdapter) pathTarget.getLastPathComponent();
		if(node == null) 
			return;

		if(e.getActionCommand().equals("add location")) {
			final ProgressDialog progressMonitor = new ProgressDialog(null, 0, 100,
					false);
			final SwingWorker worker = new SwingWorker() {
				public Object construct() {
					try {

			node.addLocations();
			ltree.getModel().nodeStructureChanged(node.getParent());
			ltree.expandNodeToDataSets(Resources.get("DATASET"),null);
					} catch (final Throwable t) {
						SwingUtilities.invokeLater(new Runnable() {
							public void run() {
								StackTrace.showStackTrace(t);
							}
						});
					}
					return null;
				}

				public void finished() {
					// Close the progress dialog.
					progressMonitor.setVisible(false);
					progressMonitor.dispose();
				}
			};
			
			final Timer timer = new Timer(300, null);
			timer.setInitialDelay(0); // Start immediately upon request.
			timer.setCoalesce(true); // Coalesce delayed events.
			timer.addActionListener(new ActionListener() {
				public void actionPerformed(final ActionEvent e) {
					SwingUtilities.invokeLater(new Runnable() {
						public void run() {
							
							// Did the job complete yet?
							if (progressMonitor.isVisible())
								// If not, update the progress report.
								progressMonitor.setProgress(20);
							else {
								// If it completed, close the task and tidy up.
								// Stop the timer.
								timer.stop();
							}
						}
					});
				}
			});

			// Start the timer.
			timer.start();
			worker.start();
			progressMonitor.setVisible(true);
		}else if(e.getActionCommand().equals("create importable")) {
			LinkItemsDialog linkDialog = new LinkItemsDialog(PortableType.IMPORTABLE,"");
			linkDialog.setVisible(true);
			String portableName = linkDialog.getPortableName();
			if(null== portableName || portableName.equals(""))
				return;
			node.addImportable(portableName);
			ltree.getModel().nodeStructureChanged(node.getParent());
			
			Element dsElement = node.findAncestorElement(node.getNode(), Resources.get("DATASET"));
			JDomNodeAdapter dsNode = null;
			if(dsElement!=null)
				dsNode = new JDomNodeAdapter(dsElement);
			int row = ltree.expandNodeToDataSets(Resources.get("DATASET"),dsNode);
			  ltree.setSelectionRow(row);
			
		}else if(e.getActionCommand().equals("create exportable")) {
			LinkItemsDialog linkDialog = new LinkItemsDialog(PortableType.EXPORTABLE,"");
			linkDialog.setVisible(true);
			String portableName = linkDialog.getPortableName();
			if(null==portableName || portableName.equals(""))
				return;

			node.addExportable(portableName);
			ltree.getModel().nodeStructureChanged(node.getParent());	
			
			Element dsElement = node.findAncestorElement(node.getNode(), Resources.get("DATASET"));
			JDomNodeAdapter dsNode = null;
			if(dsElement!=null)
				dsNode = new JDomNodeAdapter(dsElement);
			int row = ltree.expandNodeToDataSets(Resources.get("DATASET"),dsNode);
			  ltree.setSelectionRow(row);

		}else if(e.getActionCommand().equals("create exportables")) {
			MultiPortableDialog mpd = new MultiPortableDialog(node,PortableType.EXPORTABLE);
			String expName = mpd.getPortableObject().getName();
			if(expName==null || "".equals(expName))
				return;
			node.addExportable(expName);
			ltree.getModel().nodeStructureChanged(node.getParent());
			McGuiUtils.refreshGui(node);
		}else if(e.getActionCommand().equals("create importables")) {
			MultiPortableDialog mpd = new MultiPortableDialog(node,PortableType.IMPORTABLE);
			String impName = mpd.getPortableObject().getName();
			if(impName==null || "".equals(impName))
				return;
			node.addImportable(impName);
			ltree.getModel().nodeStructureChanged(node.getParent());
			
			Element dsElement = node.findAncestorElement(node.getNode(), Resources.get("DATASET"));
			JDomNodeAdapter dsNode = null;
			if(dsElement!=null)
				dsNode = new JDomNodeAdapter(dsElement);
			int row = ltree.expandNodeToDataSets(Resources.get("DATASET"),dsNode);
			if(row>0)
				ltree.setSelectionRow(row-1);
			ltree.setSelectionRow(row);			
		}else if(e.getActionCommand().equals("partition")) {
			DsTablesDialog dsDialog = new DsTablesDialog(node);
			if(dsDialog.getPartitionColumn()==null)
				return;
			List<String> values = node.getPartitionValue(dsDialog.getPartitionColumn());
			if(values.size()>0) {
				node.addPartition(node.getNode().getAttributeValue(Resources.get("NAME")),
						dsDialog.getPartitionColumn().getDataSetTableName(),dsDialog.getPartitionColumn().getName(), values);
				ltree.getModel().nodeStructureChanged(node);
				McGuiUtils.refreshGui(node);
			}
		}else if(e.getActionCommand().equals("add container")) {
			node.addContainer();
			ltree.getModel().nodeStructureChanged(node);
			McGuiUtils.refreshGui(node);
		}else if(e.getActionCommand().equals("remove importable") || 
				e.getActionCommand().equals("remove exportable")) {
			JDomNodeAdapter parent = (JDomNodeAdapter)node.getParent();
			node.removePortable();
			ltree.getModel().nodeStructureChanged(parent);
			McGuiUtils.refreshGui(parent);
		}else if(e.getActionCommand().equals("update")) {
			//node is a dataset
			((McViewSchema)McViews.getInstance().getView(IdwViewType.SCHEMA)).updateSchema(node); 
		}else if(e.getActionCommand().equals("pieChart")) {
			//hardcode processor
			AddProcessorDialog2 apd = new AddProcessorDialog2(node,PortableType.PROCESSOR,((JMenuItem)e.getSource()).getText());
			String expName = apd.getPortableObject().getName();
			if(null != expName && !"".equals(expName)) { 
			node.addProcessorElement(expName);
				ltree.getModel().nodeStructureChanged(node.getParent());
				McGuiUtils.refreshGui(node);
			}
		}else if(e.getActionCommand().equals("Materialize")){
			((McViewSchema)McViews.getInstance().getView(IdwViewType.SCHEMA)).requestRunDDL(node); 
		}else if(e.getActionCommand().equals("addLinkedDataSet")) {
			node.addLinkedDataSet();
		}else if(e.getActionCommand().equals("hide")) {
			node.hide();
			McGuiUtils.refreshGui(node);
		}else if(e.getActionCommand().equals("hide4user")) {
			node.hideForCurrentUser();
			McGuiUtils.refreshGui(node);
		}
	}
	
}