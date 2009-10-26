package org.biomart.configurator.view;



import java.awt.Dimension;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.dnd.Autoscroll;
import java.awt.dnd.DnDConstants;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.SwingUtilities;
import javax.swing.JPopupMenu;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.event.TreeExpansionEvent;
import javax.swing.event.TreeExpansionListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.event.TreeWillExpandListener;
import javax.swing.tree.ExpandVetoException;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;
import org.biomart.common.resources.Resources;
import org.biomart.configurator.jdomUtils.JDomNodeAdapter;
import org.biomart.configurator.jdomUtils.JDomTreeModelAdapter;
import org.biomart.configurator.jdomUtils.JDomUtils;
import org.jdom.Document;
import org.jdom.Element;
import org.biomart.configurator.utils.McGuiUtils;
import org.biomart.configurator.view.AttributeTable;
import org.biomart.configurator.view.menu.ContextMenuConstructor;
import org.biomart.configurator.model.XMLAttributeTableModel;
import org.biomart.configurator.component.PtModel;
import org.biomart.configurator.controller.DefaultTreeTransferHandler;
import org.biomart.configurator.model.PartitionTableModel;



public class MartConfigTree extends JTree implements TreeExpansionListener,
	TreeWillExpandListener, Autoscroll, TableModelListener {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private final Insets defaultScrollInsets = new Insets(8, 8, 8, 8);
	private Insets scrollInsets = defaultScrollInsets;
	private JDomTreeModelAdapter xmlTreeModel;
	
	private MartConfigTree leftTree;
//	private MartConfigTree rightTree;
	private AttributeTable attributeTable;
	private JPopupMenu contextMenu;
//	private MartTabSet martTabSet;


	public void autoscroll(Point location) {
		JScrollPane scroller = (JScrollPane) SwingUtilities.getAncestorOfClass(JScrollPane.class, this);
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

	public Insets getAutoscrollInsets() {
		Rectangle r = getVisibleRect();
		Dimension size = getSize();
		Insets i =
			new Insets(
				r.y + scrollInsets.top,
				r.x + scrollInsets.left,
				size.height - r.y - r.height + scrollInsets.bottom,
				size.width - r.x - r.width + scrollInsets.right);
		return i;
	}

	public MartConfigTree(Document document) {
		
		xmlTreeModel = new JDomTreeModelAdapter(document);
		this.setModel(xmlTreeModel);	
		this.putClientProperty("JTree.lineStyle", "Angled");

		this.setEditable(false);
		this.setShowsRootHandles(true);
		this.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);


		new DefaultTreeTransferHandler(this, DnDConstants.ACTION_COPY_OR_MOVE);
		this.addTreeSelectionListener(new TreeSelectionListener() {
			public void valueChanged(TreeSelectionEvent e) {
				Object lpc = e.getPath().getLastPathComponent();
				if (lpc instanceof JDomNodeAdapter) {
					updateAttributeTable((JDomNodeAdapter)lpc);					
				}
			}
		});
		
		this.addMouseListener (new MouseAdapter ()  {
            public void mousePressed (MouseEvent e)  {
                if (e.isPopupTrigger ()  &&  e.getClickCount () == 1)  {
                    doPopup (e);
                }
            }

            public void mouseReleased(MouseEvent e)  {
                if (e.isPopupTrigger ()  &&  e.getClickCount () == 1)  {
                    doPopup (e);
                }
            }
		});
	

	}

	private void doPopup(MouseEvent e) {
		TreePath path  = this.getPathForLocation(e.getX(),e.getY());
		if(path == null ) return;
		JDomNodeAdapter node = (JDomNodeAdapter) path.getLastPathComponent();
		if(node == null) return;
		System.out.println(node.getNode().getName());
		
		ContextMenuConstructor cmc = ContextMenuConstructor.getInstance();
		contextMenu = cmc.getContextMenu(node.getNode().getName(),this, e.getX(), e.getY());
		if (contextMenu!=null)
			contextMenu.show(this, e.getX(), e.getY());
	}
	
	public JDomTreeModelAdapter getModel() {
		return this.xmlTreeModel;
	}
		
	//FIXME: this code should be handled by model, and should not hard code for the tag
	private void updateAttributeTable(JDomNodeAdapter treeNode) {
//		String typeString=Resources.get("DISPLAYTYPE");
		XMLAttributeTableModel tModel = new XMLAttributeTableModel(treeNode);
		this.attributeTable.setModel(tModel);
	}
	
	@SuppressWarnings("unchecked")
	public  JDomNodeAdapter makeDeepCopy(JDomNodeAdapter node) {
		JDomNodeAdapter nodeCopy = new JDomNodeAdapter(node.getUserObject());
		for (Enumeration<JDomNodeAdapter> e = node.children(); e.hasMoreElements();) {	
			nodeCopy.add(makeDeepCopy(e.nextElement()));
		}
		return(nodeCopy);

	}

	public void setAttributeTable(AttributeTable attributeTable) {
		this.attributeTable = attributeTable;
	}
	

	public AttributeTable getAttributeTable() {
		return attributeTable;
	}

	public void setLeftTree(MartConfigTree leftTree) {
		this.leftTree = leftTree;
	}

	public MartConfigTree getLeftTree() {
		return leftTree;
	}

	
	public String getRange(Element node, boolean source) {
		return "";
	}
	
	/**
	 * source will change after the call
	 * @param source
	 * @param target
	 * @return
	 */
	public Element getAncestor(Element source, String target) {
		if (source == null) return null;
		boolean found = false;
		boolean hasParent = true;
		while(!found && hasParent) {
			if(source.getName().equals(target)) {
				found = true;
				break;
			}
			source = source.getParentElement();
			if(source==null)
				hasParent = false;
		}
		if(found)
			return source;
		else 
			return null;
	}
	
	public Map<String,PartitionTableModel> getPartitionTable(Element martNode) {
		Map<String,PartitionTableModel> ptMap = new HashMap<String,PartitionTableModel>();
		String partitionTable = Resources.get("PARTITIONTABLE");
		List<Element> ptsList = martNode.getChildren(partitionTable);
		if (ptsList == null)
			return null;
		for(Element pts:ptsList) {
			PartitionTableModel ptm = new PartitionTableModel(pts);
			ptMap.put(ptm.getPartitionTableName(), ptm);
		}
		return ptMap;
	}



//	public MartTabSet getMartTabSet() {
//		return this.martTabSet;
//	}

	public JDomNodeAdapter getNode(TreePath path, String nodeName) {
		JDomNodeAdapter result = null;
		Object[] objects = path.getPath();
		for(Object obj:objects) {
			if(((JDomNodeAdapter)obj).getNode().getName().equals(nodeName)) {
				return (JDomNodeAdapter)obj;
			}
		}
		return null;
	}
	
	public void expandAllNodes() {
		int row = 0;
		while (row < this.getRowCount()) {
			this.expandRow(row);
			row++;
      }
	}

	public void collapseAllNodes() {
		int row = 0;
		while (row < this.getRowCount()) {
			this.collapseRow(row);
			row++;
      }
	}
/*
	private TreePath getPath(JDOMNodeAdapter node) {
		List<JDOMNodeAdapter> list = new ArrayList<JDOMNodeAdapter>();
	    
        // Add all nodes to list
        while (node != null) {
            list.add(node);
            node = (JDOMNodeAdapter)node.getParent();
        }
        Collections.reverse(list);
    
        // Convert array of nodes to TreePath
        return new TreePath(list.toArray());

	}
	*/
	
	/**
	 * hardcoded
	 */
	public int expandNodeToDataSets(String nodelevel, JDomNodeAdapter selectedNode) {
		int selectedRow = 0;
		final String[] nodes = {Resources.get("MARTREGISTRY"),
				Resources.get("LOCATION"),
				Resources.get("MART"),
				};
		HashSet<String> hs = new HashSet<String>(Arrays.asList(nodes));
		int row = 0;
		while (row < this.getRowCount()) {
			TreePath tp = this.getPathForRow(row);
			JDomNodeAdapter node = (JDomNodeAdapter)tp.getLastPathComponent();
			if(selectedNode!=null && node.getNode().equals(selectedNode.getNode()))
				selectedRow = row;
			if(node!=null && hs.contains(node.getNode().getName()))
					this.expandRow(row);
			row++;
		}
		return selectedRow;
	}
	
	public int findRowofNode(JDomNodeAdapter selectedNode) {
		int row=0;
		for(int i=0; i<this.getRowCount(); i++) {
			TreePath tp = this.getPathForRow(i);
			JDomNodeAdapter node = (JDomNodeAdapter)tp.getLastPathComponent();
			if(selectedNode!=null && node.getNode().equals(selectedNode.getNode())) {
				row = i;
				break;
			}			
		}
		return row;
	}
	
	public void treeCollapsed(TreeExpansionEvent arg0) {
		// TODO Auto-generated method stub
		System.out.println("collapsed");
	}

	public void treeExpanded(TreeExpansionEvent arg0) {
		// TODO Auto-generated method stub
		System.out.println("expanded");
	}

	public void treeWillCollapse(TreeExpansionEvent arg0)
			throws ExpandVetoException {
		// TODO Auto-generated method stub
		System.out.println("collapse");
	}

	public void treeWillExpand(TreeExpansionEvent arg0)
			throws ExpandVetoException {
		// TODO Auto-generated method stub
		System.out.println("expand");
	}

	public void tableChanged(TableModelEvent e) {
		if(e.getSource().getClass().getName().equals(XMLAttributeTableModel.class.getName())) {
			int row = e.getFirstRow();
			Object obj = e.getSource();
			XMLAttributeTableModel model = (XMLAttributeTableModel)obj;
			String name = (String)model.getValueAt(row, 0);
			String value = (String)model.getValueAt(row, 1);
			TreePath tp = this.getSelectionPath();
			if(tp == null)
				return;
			JDomNodeAdapter node = (JDomNodeAdapter)tp.getLastPathComponent();
			node.getNode().setAttribute(name, value);
			this.getModel().nodeStructureChanged(node.getParent());
			McGuiUtils.refreshGui(node);
			this.setSelectionPath(tp);
		} else if(e.getSource().getClass().getName().equals(PtModel.class.getName())) {
			PtModel model = (PtModel)e.getSource();
			int row = e.getFirstRow();
			int col = e.getColumn();
			if(col<=0 || model.getColumnCount()<=col)
				return;
			String ptName = model.getPartitionTableName();
			TreePath tp = this.getSelectionPath();
			if(tp == null)
				return;
			JDomNodeAdapter node = (JDomNodeAdapter)tp.getLastPathComponent();
			//node is a dataset
			JDomNodeAdapter ptNode = new JDomNodeAdapter(JDomUtils.searchElement(node.getNode(), 
					Resources.get("PARTITIONTABLE"), ptName));
			JDomUtils.updatePartitionTable(ptNode.getNode(), row, col, (String)model.getValueAt(row, col));
			this.getModel().nodeStructureChanged(ptNode);
		}
	}

}