package org.biomart.configurator.view.menu;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;
import java.util.regex.Pattern;

import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JTable;

import org.biomart.common.resources.Resources;
import org.biomart.configurator.jdomUtils.JDomNodeAdapter;
import org.biomart.configurator.jdomUtils.JDomUtils;
import org.jdom.Element;

public class AttrTableContextMenu extends JPopupMenu implements ActionListener {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private JTable table;
	private JDomNodeAdapter treeNode;
	
	public AttrTableContextMenu(JTable table) {
		this.table = table;
	}
		
	public void setTreeNode(JDomNodeAdapter treeNode) {
		this.treeNode = treeNode;
		this.setMenu();
	}
	
	@SuppressWarnings("unchecked")
	private void setMenu() {
		this.removeAll();
		Element node = treeNode.getNode();
		if(node.getName().equals(Resources.get("FILTER")) || 
				node.getName().equals(Resources.get("ATTRIBUTEPOINTER")) ||
				node.getName().equals(Resources.get("ATTRIBUTE"))) {
			//get dstable type: main, submain, dm			
			String dstName = node.getAttributeValue(Resources.get("DSTABLE"));
			if(dstName==null || "".equals(dstName))
				return;
			Element dsElement = treeNode.findAncestorElement(treeNode.getNode(), Resources.get("DATASET"));
			Element dsTableElement = JDomUtils.searchElement(dsElement, Resources.get("DSTABLE"), dstName);
			//dsTableElement is null when the filter/attribute is dragged from other containers
			//skip this case for now
			if(dsTableElement == null) 
				return;
			String dstType = dsTableElement.getAttributeValue(Resources.get("TYPE"));
			JMenu changeNameMenu = new JMenu("bind name with");

			List<Element> ptList = dsElement.getChildren(Resources.get("PARTITIONTABLE"));
			for(Element pt:ptList) {
				String ptName = pt.getAttributeValue(Resources.get("NAME"));
				String ptType = pt.getAttributeValue(Resources.get("TYPE"));
				String ptDstName = pt.getAttributeValue(Resources.get("DSTABLE"));
				if(ptType.compareTo(dstType)<=0) {
					//one dm partition should not affect another dm
					if(ptType.equals("2")) {
						if(!ptDstName.equals(dstName))
							continue;
					}
					JMenu ptMenu = new JMenu(ptName);
					changeNameMenu.add(ptMenu);
					int cols = this.getPartitionColumns(ptName);
					for(int i=1; i<=cols; i++) {
						String colName = "col"+i;
						JMenuItem item = new JMenuItem(colName);
						item.setActionCommand(colName);
						item.addActionListener(this);
						ptMenu.add(item);
					}	
				}
			}
			if(changeNameMenu.getItemCount()>0)
				this.add(changeNameMenu);
		}
	}
	
	
	private int getPartitionColumns(String partitionTable) {
		int colCount = 0;
		Element dsElement = treeNode.findAncestorElement(treeNode.getNode(), Resources.get("DATASET"));
		Element ptElement = JDomUtils.searchElement(dsElement, Resources.get("PARTITIONTABLE"), partitionTable);
		String cols = ptElement.getAttributeValue("cols");
		try {
			colCount = Integer.parseInt(cols);
		}catch(Exception e) {
			
		}
		return colCount;
	}

	public void actionPerformed(ActionEvent e) {
		String actionString = e.getActionCommand();
		JMenuItem item = (JMenuItem)e.getSource();
		JPopupMenu pop = (JPopupMenu)item.getParent();
		String ptName = ((JMenu)pop.getInvoker()).getText();
		String colString = actionString.replaceFirst("col", "c");
		int rowCount = this.table.getRowCount();
		for(int i=0; i<rowCount; i++) {
			if(((String)this.table.getValueAt(i, 0)).equals(Resources.get("NAME"))) {
				String newName = this.rename((String)this.table.getValueAt(i, 1), colString, ptName);
				this.table.setValueAt(newName, i, 1);
				break;
			}
		}
	}
	
	private String rename(String oldName, String col, String partitionTable) {
		if(oldName.indexOf("(")<0)
			return oldName+"("+partitionTable+col+")";
		else {
			String pat = "\\(\\w*\\)";
			Pattern p = Pattern.compile(pat);
			String[] arr = p.split(oldName);
			String tmp="";
			for(String item:arr) {
				tmp = tmp + item;
			}
			return tmp+"("+partitionTable+col+")";
		}
			
	}
}