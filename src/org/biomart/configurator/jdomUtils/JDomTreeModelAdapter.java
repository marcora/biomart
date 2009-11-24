
package org.biomart.configurator.jdomUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.util.List;

import javax.swing.tree.DefaultTreeModel;
import org.jdom.Document;
import org.jdom.output.XMLOutputter;



/**

 * Converts a JDOM document into a TreeModel. Can be used for viewing XML
 * documents in a JTree.
 * 
 * @see http://java.sun.com/webservices/jaxp/dist/1.1/docs/tutorial/index.html
 */
public class JDomTreeModelAdapter extends DefaultTreeModel {

    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	//JDOM Document to view as a tree
    private Document document;
    private McFilter filter;
    private List<String> skippedNodes;

    public JDomTreeModelAdapter(Document doc) {
    	super(null);
    	this.document = doc;
    }

    public void setSkippedNodes(List<String> list) {
    	this.skippedNodes = list;
    }

    public void setFilter(McFilter filter) {
    	this.filter = filter;
    	JDomNodeAdapter root = (JDomNodeAdapter)this.getRoot();
	    Object[] path = {this.getRoot()};
	    int[] childIndices  = new int[root.getChildCount()];      
	    Object[] children  = new Object[root.getChildCount()];
	    for (int i = 0; i < root.getChildCount(); i++) {
	      childIndices[i] = i;
	      children[i] = root.child(i);
	    }
 	    fireTreeStructureChanged(this,path,childIndices, children);
    }
 
    //override from TreeModel
    public Object getRoot() {
        if(document == null) return null;
        return new JDomNodeAdapter(document.getRootElement());
    }

    

    

    //override from TreeModel

    public Object getChild(Object parent, int index) {
        JDomNodeAdapter node = (JDomNodeAdapter) parent;
        if(this.filter==null)
        	return node.child(index);
        else {
	        int originalCount = node.childCount();
	        int current = -1;
	        for(int i=0; i<originalCount; i++) {
	        	Object child = node.child(i);
	        	if(!this.filter.isFiltered((JDomNodeAdapter)child))
	        		current++;
	        	if(current == index)
	        		return child;
	        }
        }
        return null;        
    }

    

    

    //override from TreeModel
    public int getIndexOfChild(Object parent, Object child) {
        JDomNodeAdapter node = (JDomNodeAdapter) parent;
        if(this.filter == null)
        	return node.index((JDomNodeAdapter)child);
        else {
	        int count = node.childCount();
	        for (int i = 0; i < count; i++) {
	            JDomNodeAdapter n = node.child(i);
	            if(this.filter.isFiltered(n))
	            	return -1;
	            if ((JDomNodeAdapter)child == n) {
	                return i;
	            }
	        }
        }
        return -1; // Should never get here.
    }



    //override from TreeModel
    public int getChildCount(Object parent) {
        JDomNodeAdapter jdomNode = (JDomNodeAdapter)parent;
        int realCount = jdomNode.childCount();
        int count = 0;
        if(this.filter!=null) {
        	for(int i=0; i<realCount; i++) {
        		if(!this.filter.isFiltered(jdomNode.child(i))) 
        			count++;
        	}
        	return count;
        }
        //else
        return jdomNode.childCount();
    }

    //override from TreeModel
    public boolean isLeaf(Object node) {
        JDomNodeAdapter jdomNode = (JDomNodeAdapter)node;
        return (jdomNode.childCount() <=0);
    }

    public void save(File file) {
    	XMLOutputter outputter = new XMLOutputter();
     	try {
    		FileOutputStream fos = new FileOutputStream(file);
    		outputter.output(this.document, fos);
    		fos.close();
    	}
    	catch(Exception e) {
    		e.printStackTrace();
    	}
    }

}



