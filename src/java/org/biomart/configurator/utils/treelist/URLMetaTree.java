package org.biomart.configurator.utils.treelist;

import general.exceptions.FunctionalException;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.EventObject;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import javax.swing.AbstractCellEditor;
import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.UIManager;
import javax.swing.JTree.DynamicUtilTreeNode;
import javax.swing.event.ChangeEvent;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeCellEditor;
import javax.swing.tree.TreeCellRenderer;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;

import martConfigurator.transformation.helpers.TransformationUtils;
import martService.Configuration;
import martService.objects.DatasetInMart;
import martService.objects.MartInVirtualSchema;

public class URLMetaTree extends TreeListComponent implements TreeSelectionListener{

	private Configuration urlConfig;

	public URLMetaTree() {
		super("Marts");
	}
	
	public void setConfig(Configuration config){
		this.urlConfig = config;
	}
	
	public void valueChanged(TreeSelectionEvent e) {
		TreePath oldpath = e.getOldLeadSelectionPath();
		if(oldpath!=null) {
			DynamicUtilTreeNode oldnode = (DynamicUtilTreeNode)oldpath.getLastPathComponent();
			if(oldnode!=null && oldnode.isLeaf()) {
				CheckBoxNode oldObject = (CheckBoxNode)oldnode.getUserObject();
				this.saveCurrentSelectedDataSets(oldObject);
			}
		}
		DynamicUtilTreeNode lpc = (DynamicUtilTreeNode)e.getPath().getLastPathComponent();
		if(!lpc.isLeaf())
			return;
		CheckBoxNode obj = (CheckBoxNode)lpc.getUserObject();
		if(obj.hasTables()) {
			this.restoreDataSets(obj);
		} else {			
			List<CheckBoxNode> tables = this.getDataSets("default", obj.getText(),false);
			this.checkBoxList.setItems(tables);			
		}		
	}
	
	private List<CheckBoxNode> getDataSets(String virtualSchemaName, String martName, boolean isSelected) {
		List<CheckBoxNode> dsList = new ArrayList<CheckBoxNode>();
        MartInVirtualSchema mart =this.urlConfig.getMartByName(martName);
        try {
        	this.urlConfig.fetchDatasetSet(                   
        			this.urlConfig.getLocalMartServiceStringUrl(mart), virtualSchemaName, mart);
        } catch (MalformedURLException e) {
        	            // TODO Auto-generated catch block
        	            e.printStackTrace();
        } catch (FunctionalException e) {
        	            // TODO Auto-generated catch block
        	            e.printStackTrace();
        } catch (IOException e) {
        	            // TODO Auto-generated catch block
        	            e.printStackTrace();
        }
        List<DatasetInMart> list =
        	this.urlConfig.martDatasetListMap.get(martName);
        for(DatasetInMart dsInMart:list) {
        	if(dsInMart.getVisible() && TransformationUtils.isTableSet(dsInMart.getDatasetType())) {
	        	CheckBoxNode cbn = new CheckBoxNode(dsInMart.datasetName,isSelected);
	        	dsList.add(cbn);
        	}
        }
		return dsList;
	}
	
	public JPanel createGUI() {
		JPanel panel = new JPanel(new GridLayout());
		List<CheckBoxNode> nodeList = new ArrayList<CheckBoxNode>();
		for(String item:this.treeItemStrList) {
			CheckBoxNode node = new CheckBoxNode(item,false);
			nodeList.add(node);
		}
		Vector nodesVector = new NamedVector("Marts",nodeList.toArray());
		Object[] objs = {nodesVector};
		Vector rootVector = new NamedVector("Root", objs);
	    CheckBoxNodeRenderer renderer = new CheckBoxNodeRenderer();
	    tree = new JTree(rootVector);
	    tree.setCellRenderer(renderer);

	    tree.setCellEditor(new CheckBoxNodeEditor(tree));
	    tree.setEditable(true);
	    tree.addTreeSelectionListener(this);

	    
	    JScrollPane scrollPane = new JScrollPane(tree);
	    panel.add(scrollPane);
	    
	    this.checkBoxList = new CheckBoxList();

	    
	    JScrollPane listScrollPane = new JScrollPane(checkBoxList);
	    listScrollPane.setSize(scrollPane.getSize());
	    panel.add(listScrollPane);	    
	    return panel;
		
	}

	public void updateTree(List<String> list) {
		//fill dbItems;
		List<CheckBoxNode> nodeList = new ArrayList<CheckBoxNode>();
		if(list!=null) {
			for(String item:list) {
				CheckBoxNode node = new CheckBoxNode(item,false);
				nodeList.add(node);
			}
		}
		Vector nodesVector = new NamedVector("Databases",nodeList.toArray());
		Object[] objs = {nodesVector};
		
		DefaultMutableTreeNode root = (DefaultMutableTreeNode) tree.getModel().getRoot();
		DynamicUtilTreeNode dbnode = (DynamicUtilTreeNode) root.getChildAt(0);
		dbnode.removeAllChildren();

		JTree.DynamicUtilTreeNode.createChildren(dbnode, nodesVector);
		DefaultTreeModel model = (DefaultTreeModel)tree.getModel();
		model.reload();
		this.expandAllNodes();
	}
		
	
	private void saveCurrentSelectedDataSets(CheckBoxNode node) {
		node.clearTables();
		for(int i=0; i<this.checkBoxList.getModel().getSize(); i++) {
			node.addTable((CheckBoxNode)this.checkBoxList.getModel().getElementAt(i));
		}
	}
	
	private void restoreDataSets(CheckBoxNode node) {
		this.checkBoxList.setItems(node.getTables());
	}
	
	/**
	 * if all=false, only selected tables return;
	 * @param all
	 * @return
	 */
	public Map<String, List<String>> getDBInfo(boolean all) {
		DynamicUtilTreeNode currentNode;
		CheckBoxNode currentCBN=null;
		TreePath path =this.tree.getSelectionPath();
		if(path!=null) {
			currentNode = (DynamicUtilTreeNode)path.getLastPathComponent();
			if(currentNode.isLeaf())
				currentCBN = (CheckBoxNode)currentNode.getUserObject();
		}
		Map<String, List<String>>dbInfo = new LinkedHashMap<String, List<String>>();
		TreeModel model = this.tree.getModel();
		Object obj = model.getRoot();
		Object root = model.getChild(obj,0);
		int count = this.tree.getModel().getChildCount(root);
		for(int i=0;i<count;i++) {
			DynamicUtilTreeNode node = (DynamicUtilTreeNode) this.tree.getModel().getChild(root, i);
			
			CheckBoxNode cbn = (CheckBoxNode)node.getUserObject();
			if(cbn.isSelected()) {
				if(currentCBN!=null && cbn.getText().equals(currentCBN.getText())) 
					dbInfo.put(cbn.getText(), this.checkBoxList.getItems(all));
				else
					dbInfo.put(cbn.getText(),cbn.getTable(all));
			}
		}
		return dbInfo;
	}


}