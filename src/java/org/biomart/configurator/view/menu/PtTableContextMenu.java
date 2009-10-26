package org.biomart.configurator.view.menu;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JMenuItem;

import javax.swing.JPopupMenu;
import javax.swing.JTable;

import org.biomart.common.resources.Resources;
import org.biomart.configurator.component.PtModel;
import org.biomart.configurator.utils.McEventObject;
import org.biomart.configurator.utils.type.EventType;
import org.biomart.configurator.utils.type.IdwViewType;
import org.biomart.configurator.view.idwViews.McViews;
import org.jdom.Element;

public class PtTableContextMenu extends JPopupMenu {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private JTable table;
	private int selectedCol;
	private Element ptElement;
	
	public PtTableContextMenu(JTable table, Element ptElement) {
		this.ptElement = ptElement;
		this.table = table;
		init();
	}
	
	public void setSelectedColumn(int col) {
		this.selectedCol = col;
	}
	
	private void init() {
		JMenuItem copyItem = new JMenuItem("Clone");
		copyItem.addActionListener(new ActionListener() {	
			public void actionPerformed(ActionEvent e) {
				columnClone();				
			}
			
		});
		JMenuItem allCapsItem = new JMenuItem("All Caps");
		allCapsItem.setEnabled(false);
		JMenuItem removeColumnItem = new JMenuItem("Remove Column");
		removeColumnItem.setEnabled(false);
		JMenuItem deleteTableItem = new JMenuItem("Delete Table");
		deleteTableItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				removeTable();
			}			
		});
		
		this.add(copyItem);
		this.add(allCapsItem);
		this.add(removeColumnItem);
		this.add(deleteTableItem);
		this.addPtDependentMenu();
	}
	
	private void  addPtDependentMenu() {
		String type = this.ptElement.getAttributeValue(Resources.get("TYPE"));
		//add flatten in dm
		if(type.equals("2")) {
			JMenuItem flattenItem = new JMenuItem("Flatten");
			flattenItem.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {					
					flatten(true);
				}				
			});
			JMenuItem unflattenItem = new JMenuItem("UnFlatten");
			unflattenItem.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {					
					flatten(false);
				}				
			});
			this.add(flattenItem);
			this.add(unflattenItem);
		}
	}
	
	private void flatten(boolean f) {
		if(f) {
			this.ptElement.setAttribute(Resources.get("FLATTEN"),"1");
		} else {
			this.ptElement.setAttribute(Resources.get("FLATTEN"),"0");
		}
		McEventObject obj = new McEventObject(EventType.Update_SchemaGUI,this.ptElement);
		McViews.getInstance().getView(IdwViewType.SCHEMA).getController().processV2Cupdate(obj);
	}

	/**
	 * make a clone of the first column
	 */
	private void columnClone() {
		PtModel model = (PtModel)this.table.getModel();
		model.cloneColumn(0);
	}
	
	private void removeTable() {
		McEventObject obj = new McEventObject(EventType.Remove_PartitionTable,this.ptElement.getAttributeValue(Resources.get("NAME")));
		McViews.getInstance().getView(IdwViewType.SCHEMA).getController().processV2Cupdate(obj);
	}

}