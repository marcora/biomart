package org.biomart.configurator.utils.treelist;
 
import java.awt.*;
import javax.swing.*;
import javax.swing.event.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class CheckBoxList extends JList implements ListSelectionListener {

    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	

    private DefaultListModel defModel;
    public CheckBoxList() {
        super();
        setCellRenderer (new CheckListRenderer());
        addListSelectionListener (this);
        this.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    	defModel = new DefaultListModel();
        this.setModel (defModel);
    }

    public void setItems(Collection<CheckBoxNode> items) {
    	DefaultListModel model = new DefaultListModel();
    	for(CheckBoxNode item:items)   		
    		model.addElement(item);
    	try{
    		this.setModel(model);
    	}catch(Exception e) {
    		e.printStackTrace();
    	}    	
    }
    
    
    // ListSelectionListener implementation
    public void valueChanged (ListSelectionEvent lse) {
    	if(!lse.getValueIsAdjusting()) {
	    	CheckBoxNode item = (CheckBoxNode)this.getSelectedValue();
	    	if(item!=null)
	    		item.setSelected(!item.isSelected());
    	}
    	
    }
    
    public List<String> getSelectedItems() {
    	DefaultListModel model = (DefaultListModel)this.getModel();
    	List<String> result = new ArrayList<String>();
    	for(int i=0; i<model.getSize(); i++) {
    		CheckBoxNode node = (CheckBoxNode)model.get(i);
    		if(node.isSelected())
    			result.add(node.getText());
    	}
    	return result;
    }
    

    class CheckListRenderer extends JCheckBox implements ListCellRenderer {

    	private static final long serialVersionUID = 1L;

    	public Component getListCellRendererComponent(
             JList list, Object value, int index,
             boolean isSelected, boolean hasFocus)
    	{
    		setEnabled(true);
    		setSelected(((CheckBoxNode)value).isSelected());
    		setFont(list.getFont());
    		if(hasFocus)
    			setBackground(Color.GRAY);
    		else
    			setBackground(list.getBackground());
    		setForeground(list.getForeground());
    		setText(((CheckBoxNode)value).getText());
    		return this;
    	}
    }
}

