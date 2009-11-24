package org.biomart.configurator.view.idwViews;

import java.awt.Component;
import java.util.List;
import java.util.Observable;

import javax.swing.Icon;
import javax.swing.tree.TreePath;

import org.biomart.builder.model.DataSetColumn;
import org.biomart.builder.model.Mart;
import org.biomart.common.resources.Log;
import org.biomart.common.resources.Resources;
import org.biomart.configurator.jdomUtils.JDomNodeAdapter;
import org.biomart.configurator.jdomUtils.JDomTreeModelAdapter;
import org.biomart.configurator.jdomUtils.JDomUtils;
import org.biomart.configurator.model.Location;
import org.biomart.configurator.model.McModel;
import org.biomart.configurator.utils.DPEventObject;
import org.biomart.configurator.utils.McEventObject;
import org.biomart.configurator.utils.McGuiUtils;
import org.biomart.configurator.utils.McIcon;
import org.biomart.configurator.utils.McUtils;
import org.biomart.configurator.utils.type.EventType;
import org.biomart.configurator.utils.type.IdwViewType;
import org.biomart.configurator.view.MartConfigTree;
import org.biomart.configurator.view.McTreeScrollPane;
import org.biomart.configurator.view.McView;
import org.biomart.configurator.view.menu.McMenus;
import org.jdom.Element;

public class McViewTree extends McView {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	public McViewTree(String title, Icon icon, Component component,
			McModel model, IdwViewType type) {
		super(title, icon, component, model, type);
		
	}

	@Override
	public void update(Observable observable, Object obj) {
		this.updateMCTree(observable, (McEventObject)obj);		
	}

	private void updateMCTree(Observable observable, McEventObject obj) {
			final MartConfigTree tree = this.getMcTree();
			JDomNodeAdapter root = (JDomNodeAdapter)tree.getModel().getRoot();
			//TreePath path = tree.getSelectionPath();			
			if(root==null)
				return;
			switch(obj.getEventType()) {
			case Update_DataSet: {
				Mart mart = (Mart)obj.getObject();
				Element e = JDomUtils.searchElement(root.getNode(), Resources.get("MART"), mart.getMartName());
				if(e==null) //the tree was not established yet at the beginning
					return;
				JDomNodeAdapter node = new JDomNodeAdapter(e);
				node.addDataSet(mart,null);
				((JDomTreeModelAdapter)tree.getModel()).nodeStructureChanged(node);
				break;
			}
			case Request_NewMartEnd: {
				String s = ((McEventObject)obj).getContextString();
				String[] s1 = s.split(";");
				Element e = JDomUtils.searchElement(root.getNode(), Resources.get("LOCATION"), s1[0]);
				final JDomNodeAdapter node = new JDomNodeAdapter(e);
				node.addMart(s1[1],null,true);
			    ((JDomTreeModelAdapter)tree.getModel()).nodeStructureChanged(node);
				final Mart mart = (Mart)obj.getObject();
				final JDomNodeAdapter martnode = new JDomNodeAdapter(JDomUtils.searchElement(node.getNode(), Resources.get("MART"), s1[1]));
		        martnode.addSourceSchemas(mart,null);
				((JDomTreeModelAdapter)tree.getModel()).nodeStructureChanged(martnode);	
				break;
			}
			case Update_PartitionTable: {
				TreePath tp = tree.getSelectionPath();
				if(tp==null)
					return;
				String ptName = obj.getContextString();
				//currentNode is dataset;
				JDomNodeAdapter dsNode = (JDomNodeAdapter)tp.getLastPathComponent();
				JDomNodeAdapter ptNode = new JDomNodeAdapter(JDomUtils.searchElement(dsNode.getNode(), 
						Resources.get("PARTITIONTABLE"), ptName));
//				Element ptElement = ptNode.findAncestorElement(ptNode.getNode(), Resources.get("PARTITIONTABLE"));
				List<String> newCol = (List<String>) obj.getObject();
				ptNode.addPartition(ptNode.getNode(), newCol);
				tree.getModel().nodeStructureChanged(ptNode);
				break;
			}
			case Remove_PartitionTable: {
				//dataset or dstable
				TreePath tp = tree.getSelectionPath();
				if(tp==null)
					return;
				String ptName = (String)obj.getObject();
				JDomNodeAdapter selectedNode = (JDomNodeAdapter)tp.getLastPathComponent();
				Element dsElement = selectedNode.findAncestorElement(selectedNode.getNode(), Resources.get("DATASET"));
				JDomNodeAdapter dsNode = new JDomNodeAdapter(dsElement);
				dsNode.removePartition(ptName);
				tree.getModel().nodeStructureChanged(dsNode);
				McGuiUtils.refreshGui(selectedNode);			
				break;
			}
			case Update_SchemaGUI: {
				//dataset or dstable
				TreePath tp = tree.getSelectionPath();
				if(tp==null)
					return;
				JDomNodeAdapter selectedNode = (JDomNodeAdapter)tp.getLastPathComponent();
				McGuiUtils.refreshGui(selectedNode);
				break;
			}
			case Synchronize_Dataset: {
				//find dataset
				TreePath tp = tree.getSelectionPath();
				if(tp==null)
					return;
				JDomNodeAdapter selectedNode = (JDomNodeAdapter)tp.getLastPathComponent();
				Element dsElement = selectedNode.findAncestorElement(selectedNode.getNode(), Resources.get("DATASET"));
				if(dsElement==null)
					return;
				//only update rdbms for now, no url
				String type = dsElement.getParentElement().getAttributeValue(Resources.get("TYPE"));
				if(null==type || !type.equals(Resources.get("RDBMSTYPE")))
					return;
				((McViewSchema)McViews.getInstance().getView(IdwViewType.SCHEMA)).updateDataSet(new JDomNodeAdapter(dsElement));
				break;
			}
			case Update_DSColumnMasked: {
				TreePath tp = tree.getSelectionPath();
				if(tp==null)
					return;
				JDomNodeAdapter selectedNode = (JDomNodeAdapter)tp.getLastPathComponent();
				//should be a dataset node
				DataSetColumn dsCol = (DataSetColumn)obj.getObject();
				selectedNode.updateMaskedDSCol(dsCol);
				break;
			}
			case Request_NewLocation: {
				long t1 = McUtils.getCurrentTime();
				tree.getModel().nodeStructureChanged(root);
				tree.expandNodeToDataSets(Resources.get("DATASET"),null);
				tree.setSelectionRow(0);
				long t2 = McUtils.getCurrentTime();
				System.err.println("draw tree "+(t2-t1));
			}
			}
			this.getViewProperties().setIcon(McIcon.HIGHLIGHT_ICON);
	}

	public MartConfigTree getMcTree() {
		Component componentS = this.getComponent();
		return ((McTreeScrollPane) componentS).getTree();
	}
	
}