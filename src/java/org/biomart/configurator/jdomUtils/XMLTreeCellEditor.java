package org.biomart.configurator.jdomUtils;

import java.awt.Component;
import java.util.EventObject;

import javax.swing.DefaultCellEditor;
import javax.swing.JComboBox;
import javax.swing.JTree;
import javax.swing.event.CellEditorListener;
import javax.swing.tree.DefaultTreeCellEditor;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.TreeCellEditor;

import org.jdom.Attribute;

public class XMLTreeCellEditor extends DefaultTreeCellEditor {


	private DefaultCellEditor comboEditor; 

	public XMLTreeCellEditor(JTree arg0, DefaultTreeCellRenderer arg1) {
		super(arg0, arg1);
		JComboBox jcb = new JComboBox(new String[]{"A","B","C"});
		comboEditor = new DefaultCellEditor(jcb);
	}

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public Component getTreeCellEditorComponent(JTree tree, Object value,
			boolean isSelected, boolean expanded, boolean leaf, int row) {
        JDomNodeAdapter adapterNode = (JDomNodeAdapter)value;
        if(adapterNode.getNode().isRootElement()) {
            value = adapterNode.getNode().getName();
        } else if(adapterNode.childCount() > 0) {
        	Attribute att = adapterNode.getNode().getAttribute("name");
        	if(att !=null)
        		value = adapterNode.getNode().getName() + ": "+att.getValue();
        	else
        		value = adapterNode.getNode().getName();
 //       	if(adapterNode.getNode().getName().equals("container"))
//        		return this.comboEditor;
        		
        } else {
            value = adapterNode.getNode().getName() +" ["+adapterNode.getNode().getTextTrim()+"]";
        }
//        
        
        return super.getTreeCellEditorComponent(tree, value, isSelected, expanded, leaf, row);
	}

	public void addCellEditorListener(CellEditorListener arg0) {
		// TODO Auto-generated method stub
		
	}

	public void cancelCellEditing() {
		// TODO Auto-generated method stub
		
	}

	public Object getCellEditorValue() {
		// TODO Auto-generated method stub
		return null;
	}

	public boolean isCellEditable(EventObject arg0) {
		// TODO Auto-generated method stub
		return false;
	}

	public void removeCellEditorListener(CellEditorListener arg0) {
		// TODO Auto-generated method stub
		
	}

	public boolean shouldSelectCell(EventObject arg0) {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean stopCellEditing() {
		// TODO Auto-generated method stub
		return false;
	}
	
}