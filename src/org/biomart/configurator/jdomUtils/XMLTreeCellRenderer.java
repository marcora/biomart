
package org.biomart.configurator.jdomUtils;


import java.awt.Component;
import javax.swing.JTree;
import javax.swing.tree.DefaultTreeCellRenderer;
import org.biomart.common.resources.Resources;
import org.biomart.configurator.utils.McColorUtils;
import org.jdom.Attribute;
import org.jdom.Element;

/**
 * Changes how the tree displays elements.
 */
public class XMLTreeCellRenderer extends DefaultTreeCellRenderer {
     
    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	//remove icons
    public XMLTreeCellRenderer() {
    }
    
    @Override
    public Component getTreeCellRendererComponent(JTree tree, Object value,
            boolean sel, boolean expanded, boolean leaf, int row, boolean hasFocus) {
        JDomNodeAdapter adapterNode = (JDomNodeAdapter)value;

        if(adapterNode.getNode().isRootElement()) {
            value = adapterNode.getNode().getName();
        } else //if(adapterNode.childCount() > 0) 
        {
        	Attribute att = adapterNode.getNode().getAttribute("name");
        	if(att !=null)
        		value = adapterNode.getNode().getName() + ": "+att.getValue();
        	else
        		value = adapterNode.getNode().getName();
        }
        
        super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus);
        
        if(!sel) 
        	setColor(adapterNode.getNode());
        
        return this;
        
    }
    
    private void setColor(Element element) {
    	//color for dataset
        if(element.getName().equalsIgnoreCase(Resources.get("DATASET"))) {
        	//FIXME should have a separate class to handle the XML error
        	if(element.getAttributeValue(Resources.get("MATERIALIZED"))!=null) {        		
        			if(element.getAttributeValue(Resources.get("MATERIALIZED")).equalsIgnoreCase("true"))
        				setForeground(McColorUtils.materializedColor);
        	}
        	else
        		setForeground(McColorUtils.nonMaterializedColor);
//        } else if(element.getName().equalsIgnoreCase(Resources.get("MART"))) {
        		
        } else if(element.getName().equals(Resources.get("MARTREGISTRY"))) {
        	setForeground(McColorUtils.nonMaterializedColor);
        } else if(element.getName().equals(Resources.get("LOCATION")) ||
        		element.getName().equals(Resources.get("MART"))) {
        	this.setEnabled(false);
        }
        else 
        	setForeground(McColorUtils.elementColor);  	
        
        if(JDomUtils.isElementHiden(element))
        	setForeground(McColorUtils.hidedColor);
    }
    

}

