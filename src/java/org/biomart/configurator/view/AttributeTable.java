/*
	Copyright (C) 2003 EBI, GRL

	This library is free software; you can redistribute it and/or
	modify it under the terms of the GNU Lesser General Public
	License as published by the Free Software Foundation; either
	version 2.1 of the License, or (at your option) any later version.

	This library is distributed in the hope that it will be useful,
	but WITHOUT ANY WARRANTY; without even the implied warranty of
	MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
	Lesser General Public License for more details.

	You should have received a copy of the GNU Lesser General Public
	License along with this library; if not, write to the Free Software
	Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
*/

package org.biomart.configurator.view;


import java.awt.Insets;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.Color;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;

import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;

import javax.swing.table.TableModel;

import org.biomart.configurator.jdomUtils.JDomNodeAdapter;
import org.biomart.configurator.model.XMLAttributeTableModel;
import org.biomart.configurator.view.menu.AttrTableContextMenu;



/**
 * Class DatasetConfigAttributesTable extending JTable.
 *
 * <p>This class is written for the attributes table to implement auscroll
 * </p>
 *
 * @author <a href="mailto:katerina@ebi.ac.uk">Katerina Tzouvara</a>
 * //@see org.ensembl.mart.config.DatasetConfig
 */

public class AttributeTable extends JTable 
	//implements Autoscroll 
{

     /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private CellEditRenderModel cellEditRenderModel;
	public static final Insets defaultScrollInsets = new Insets(8, 8, 8, 8);
    protected Insets scrollInsets = defaultScrollInsets;
    private XMLAttributeTableModel xmlAttributeTableModel;
    private AttrTableContextMenu popupMenu;
    private MartConfigTree tree;
//     protected DatasetConfig dsConfig = null;

    public AttributeTable(MartConfigTree tree) {
      super(null);
      this.tree = tree;
      xmlAttributeTableModel = new XMLAttributeTableModel(null);
      cellEditRenderModel = new CellEditRenderModel();
      this.setRowSelectionAllowed(false);
      this.setColumnSelectionAllowed(false);
      this.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
      this.setShowGrid(true);
      this.setGridColor(Color.BLACK);
      this.popupMenu = new AttrTableContextMenu(this);
      
      MouseListener popupListener = new PopupListener();
      this.addMouseListener(popupListener);
    }

	public void setTableModel(XMLAttributeTableModel model) {
		this.xmlAttributeTableModel =  model;
		this.setModel(model);
	}

/*     // Autoscrolling support
    public void setScrollInsets(Insets insets) {
        this.scrollInsets = insets;
    }

    public Insets getScrollInsets() {
        return scrollInsets;
    }

    // Implementation of Autoscroll interface
    public Insets getAutoscrollInsets() {
        Rectangle r = getVisibleRect();
        Dimension size = getSize();
        Insets i = new Insets(r.y + scrollInsets.top, r.x + scrollInsets.left,
                size.height - r.y - r.height + scrollInsets.bottom,
                size.width - r.x - r.width + scrollInsets.right);
        return i;
    }

    public void autoscroll(Point location) {
        JScrollPane scroller =
                (JScrollPane) SwingUtilities.getAncestorOfClass(JScrollPane.class, this);
        if (scroller != null) {
            JScrollBar hBar = scroller.getHorizontalScrollBar();
            JScrollBar vBar = scroller.getVerticalScrollBar();
            Rectangle r = getVisibleRect();
            if (location.x <= r.x + scrollInsets.left) {
                // Need to scroll left
                hBar.setValue(hBar.getValue() - hBar.getUnitIncrement(-1));
            }
            if (location.y <= r.y + scrollInsets.top) {
                // Need to scroll up
                vBar.setValue(vBar.getValue() - vBar.getUnitIncrement(-1));
            }
            if (location.x >= r.x + r.width - scrollInsets.right) {
                // Need to scroll right
                hBar.setValue(hBar.getValue() + hBar.getUnitIncrement(1));
            }
            if (location.y >= r.y + r.height - scrollInsets.bottom) {
                // Need to scroll down
                vBar.setValue(vBar.getValue() + vBar.getUnitIncrement(1));
            }
        }

    }
*/
    public TableCellEditor getCellEditor(int row, int col) {
    	if(col==0) return super.getCellEditor(row, col);
        TableCellEditor editor = null;
        if (cellEditRenderModel!=null)
            editor = cellEditRenderModel.getEditor(row);
        if (editor!=null)
            return editor;
        return super.getCellEditor(row,col);
    }
    
    public TableCellRenderer getCellRenderer(int row, int col) {
    	if(col==0) return super.getCellRenderer(row, col);
	   	 TableCellRenderer renderer = null;
	   	 if(cellEditRenderModel!=null)
	   		 renderer = cellEditRenderModel.getRenderer(row);
	   	 if(renderer!=null)
	   		 return renderer;
	   	 return super.getCellRenderer(row, col);
    }
    
    public void setCellEditorRendererModel(CellEditRenderModel erm) {
        this.cellEditRenderModel = erm;
    }

    public CellEditRenderModel getCellEditorRendererModel() {
        return this.cellEditRenderModel;
    }

    public void setModel(TableModel dataModel) {
    	super.setModel(dataModel);
    	dataModel.addTableModelListener(this.tree);
    }
    
    public void setContextMenuEnv(JDomNodeAdapter treeNode) {
    	this.popupMenu.setTreeNode(treeNode);
    }
    
	class PopupListener implements MouseListener {
		public void mousePressed(MouseEvent e) {
			showPopup(e);
		}

		public void mouseReleased(MouseEvent e) {
			showPopup(e);
		}

		private void showPopup(MouseEvent e) {
			if (e.isPopupTrigger()) {
				//popupMenu.setSelectedColumn(AttributeTable.this.columnAtPoint(e.getPoint()));				
				popupMenu.show(e.getComponent(), e.getX(), e.getY());
			}
		}

		public void mouseClicked(MouseEvent e) {
		}

		public void mouseEntered(MouseEvent e) {
		}

		public void mouseExited(MouseEvent e) {
		}
	}
 

}
