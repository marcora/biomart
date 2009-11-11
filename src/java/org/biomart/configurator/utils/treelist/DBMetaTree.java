package org.biomart.configurator.utils.treelist;


import java.awt.GridLayout;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.Vector;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.SwingUtilities;
import javax.swing.JTree.DynamicUtilTreeNode;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;

import org.biomart.common.view.gui.SwingWorker;
import org.biomart.common.view.gui.dialogs.ProgressDialog2;
import org.biomart.common.view.gui.dialogs.StackTrace;
import org.biomart.configurator.utils.DbConnectionInfoObject;
import org.biomart.configurator.utils.ConnectionPool;


public class DBMetaTree extends TreeListComponent implements TreeSelectionListener {
    
	private DbConnectionInfoObject conObject;
		
	public DBMetaTree() {
		super("Schemas");
	}
	
	public void setConnectionObject(DbConnectionInfoObject object) {
		this.conObject = object;
	}

	public List<String> getDatabases() {
		Connection con = ConnectionPool.Instance.getConnection(this.conObject);
		try {
			DatabaseMetaData dmd = con.getMetaData();			
			ResultSet rs2 = "".equals(dmd.getSchemaTerm()) ? dmd
					.getCatalogs() : dmd.getSchemas();
			//clean all
			this.treeItemStrList.clear();
			//check the first one is information_schema
			rs2.next();
			if(!"information_schema".equals(rs2.getString(1)))
				this.treeItemStrList.add(rs2.getString(1));
			while (rs2.next()) {
				this.treeItemStrList.add(rs2.getString(1));
			}					
		} catch(SQLException ex) {
			ex.printStackTrace();
		}
		ConnectionPool.Instance.releaseConnection(this.conObject);
		return this.treeItemStrList;
	}
	
	public Set<CheckBoxNode> getTables(String schemaName, boolean isSelected) {
		Set<CheckBoxNode> tables = new TreeSet<CheckBoxNode>(new Comparator<CheckBoxNode>() {
		      public int compare(CheckBoxNode a, CheckBoxNode b) {
		          CheckBoxNode itemA = (CheckBoxNode) a;
		          CheckBoxNode itemB = (CheckBoxNode) b;
		          return itemA.getText().compareTo(itemB.getText());
		        }
		      });

		DbConnectionInfoObject dbConObj = new DbConnectionInfoObject(this.conObject.getJdbcUrl()+schemaName,
				this.conObject.getDatabaseName(),schemaName,this.conObject.getUserName(),this.conObject.getPassword(),
				this.conObject.getDriverClassString());
		Connection con = ConnectionPool.Instance.getConnection(dbConObj);
		
		try {
			DatabaseMetaData dmd = con.getMetaData();
			final String catalog = con.getCatalog();
			ResultSet rs2 = dmd.getTables(catalog, schemaName, "%", null);
			while (rs2.next()) {
				CheckBoxNode cbn = new CheckBoxNode(rs2.getString("TABLE_NAME"),isSelected);
				tables.add(cbn);
			}						
		} catch(SQLException ex) {
			ex.printStackTrace();
		}
		ConnectionPool.Instance.releaseConnection(this.conObject);
		return tables;
	}
	
	public JPanel createGUI() {
		JPanel panel = new JPanel(new GridLayout());
		List<CheckBoxNode> nodeList = new ArrayList<CheckBoxNode>();
		for(String item:this.treeItemStrList) {
			CheckBoxNode node = new CheckBoxNode(item,false);
			nodeList.add(node);
		}
		Vector nodesVector = new NamedVector("Schemas",nodeList.toArray());
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
	
	public void updateTree(DbConnectionInfoObject conObject) {
		this.conObject = conObject;
		
		this.getDatabases();
		
		this.resetTree(this.treeItemStrList);
	}
	
	public void resetTree(List<String> list) {
		List<CheckBoxNode> nodeList = new ArrayList<CheckBoxNode>();
		if(list!=null) {
			for(String item:list) {
				CheckBoxNode node = new CheckBoxNode(item,false);
				nodeList.add(node);
			}
		}
		Vector nodesVector = new NamedVector("Schemas",nodeList.toArray());
		
		DefaultMutableTreeNode root = (DefaultMutableTreeNode) tree.getModel().getRoot();
		DynamicUtilTreeNode dbnode = (DynamicUtilTreeNode) root.getChildAt(0);
		dbnode.removeAllChildren();

		JTree.DynamicUtilTreeNode.createChildren(dbnode, nodesVector);
		DefaultTreeModel model = (DefaultTreeModel)tree.getModel();
		model.reload();
		this.expandAllNodes();		
	}
	/**
	 * table info in the current selected node is not stored, need special handle. 
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
		this.treelistInfoMap = new LinkedHashMap<String, List<String>>();
		TreeModel model = this.tree.getModel();
		Object obj = model.getRoot();
		Object root = model.getChild(obj,0);
		int count = this.tree.getModel().getChildCount(root);
		for(int i=0;i<count;i++) {
			DynamicUtilTreeNode node = (DynamicUtilTreeNode) this.tree.getModel().getChild(root, i);
			
			CheckBoxNode cbn = (CheckBoxNode)node.getUserObject();
			if(cbn.isSelected()) {
				if(currentCBN!=null && cbn.getText().equals(currentCBN.getText()))
					this.treelistInfoMap.put(cbn.getText(), this.checkBoxList.getItems(all));
				else
					this.treelistInfoMap.put(cbn.getText(),cbn.getTable(all));
			}
		}
		return this.treelistInfoMap;
	}
	
	public void valueChanged(TreeSelectionEvent e) {
		TreePath oldpath = e.getOldLeadSelectionPath();
		if(oldpath!=null) {
			DynamicUtilTreeNode oldnode = (DynamicUtilTreeNode)oldpath.getLastPathComponent();
			if(oldnode!=null && oldnode.isLeaf()) {
				CheckBoxNode oldObject = (CheckBoxNode)oldnode.getUserObject();
				this.saveCurrentSelectedTables(oldObject);
			}
		}
		DynamicUtilTreeNode lpc = (DynamicUtilTreeNode)e.getPath().getLastPathComponent();
		if(!lpc.isLeaf())
			return;
		final CheckBoxNode obj = (CheckBoxNode)lpc.getUserObject();
		if(obj.hasTables()) {
			this.restoreTables(obj);
		} else {	
			//set progressbar
			final ProgressDialog2 progressMonitor = ProgressDialog2.getInstance();				

			final SwingWorker worker = new SwingWorker() {
				public Object construct() {
					try {
		    		Set<CheckBoxNode> tables = DBMetaTree.this.getTables(obj.getText(),false);
		    		DBMetaTree.this.checkBoxList.setItems(tables);			
					} catch (final Throwable t) {
						SwingUtilities.invokeLater(new Runnable() {
							public void run() {
								StackTrace.showStackTrace(t);
							}
						});
					}finally {
						progressMonitor.setVisible(false);
					//	progressMonitor.dispose();					
					}
					return null;
				}

				public void finished() {
					// Close the progress dialog.
					progressMonitor.setVisible(false);
					//progressMonitor.dispose();
				}
			};
			
			worker.start();
			progressMonitor.start("processing ...");

		}
	}
	
	private void saveCurrentSelectedTables(CheckBoxNode node) {
		node.clearTables();
		for(int i=0; i<this.checkBoxList.getModel().getSize(); i++) {
			node.addTable((CheckBoxNode)this.checkBoxList.getModel().getElementAt(i));
		}
	}
	
	private void restoreTables(CheckBoxNode node) {
		this.checkBoxList.setItems(node.getTables());
	}
}


