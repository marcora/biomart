package org.biomart.configurator.view;

import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableCellEditor;
import java.util.Hashtable;
 
 /**
  * for multiple editors in each column
  * @author yliang
  *
  */
public class CellEditRenderModel {
	private Hashtable<Integer, TableCellEditor> editors;
    private Hashtable<Integer, TableCellRenderer> renderers;
      
    public CellEditRenderModel() {
        editors = new Hashtable<Integer, TableCellEditor>();
        renderers = new Hashtable<Integer, TableCellRenderer>();
    }
    
    public void addEditor(int row, TableCellEditor editor ) {
    	editors.put(row, editor);
    }
    
    public void removeEditor(int row) {
    	editors.remove(row);
    }
    
    public TableCellEditor getEditor(int row) {
        return editors.get(row);
    }
     
    public void addRenderer(int row, TableCellRenderer renderer) {
    	renderers.put(row, renderer);
    }
     
    public TableCellRenderer getRenderer(int row) {
    	 return renderers.get(row);
    }
    
    public void removeRenderer(int row) {
    	renderers.remove(row);
    }
 }
