package org.biomart.configurator.controller;
import java.awt.*;
import javax.swing.*;
import javax.swing.tree.*;
import java.awt.dnd.*;
import java.awt.datatransfer.*;
import java.awt.image.*;
import java.awt.geom.*;
import org.biomart.configurator.view.MartConfigTree;
import org.biomart.configurator.jdomUtils.JDomTreeModelAdapter;
import org.biomart.configurator.jdomUtils.JDomNodeAdapter;
 
public abstract class AbstractTreeTransferHandler implements DragGestureListener, DragSourceListener, DropTargetListener {
 
	private MartConfigTree tree;
	private DragSource dragSource; // dragsource
	private DropTarget dropTarget; //droptarget
	private static JDomNodeAdapter draggedNode; 
	private JDomNodeAdapter draggedNodeParent; 
	private static BufferedImage image = null; //buff image
	private Rectangle rect2D = new Rectangle();
	private boolean drawImage;
 
	protected AbstractTreeTransferHandler(MartConfigTree tree, int action, boolean drawIcon) {
		this.tree = tree;
		drawImage = drawIcon;
		dragSource = new DragSource();
		dragSource.createDefaultDragGestureRecognizer(tree, action, this);
		dropTarget = new DropTarget(tree, action, this);
	}
 
	/* Methods for DragSourceListener */
	public void dragDropEnd(DragSourceDropEvent dsde) {
		if (dsde.getDropSuccess() && dsde.getDropAction()==DnDConstants.ACTION_MOVE && draggedNodeParent != null) {
			((JDomTreeModelAdapter)tree.getModel()).nodeStructureChanged(draggedNodeParent);				
		}
	}
	public final void dragEnter(DragSourceDragEvent dsde)  {
		int action = dsde.getDropAction();
		if (action == DnDConstants.ACTION_COPY)  {
			dsde.getDragSourceContext().setCursor(DragSource.DefaultCopyDrop);
		} 
		else {
			if (action == DnDConstants.ACTION_MOVE) {
				dsde.getDragSourceContext().setCursor(DragSource.DefaultMoveDrop);
			} 
			else {
				dsde.getDragSourceContext().setCursor(DragSource.DefaultMoveNoDrop);
			}
		}
	}
	public final void dragOver(DragSourceDragEvent dsde) {
		int action = dsde.getDropAction();
		if (action == DnDConstants.ACTION_COPY) {
			dsde.getDragSourceContext().setCursor(DragSource.DefaultCopyDrop);
		} 
		else  {
			if (action == DnDConstants.ACTION_MOVE) {
				dsde.getDragSourceContext().setCursor(DragSource.DefaultMoveDrop);
			} 
			else  {
				dsde.getDragSourceContext().setCursor(DragSource.DefaultMoveNoDrop);
			}
		}
	}
	public final void dropActionChanged(DragSourceDragEvent dsde)  {
		int action = dsde.getDropAction();
		if (action == DnDConstants.ACTION_COPY) {
			dsde.getDragSourceContext().setCursor(DragSource.DefaultCopyDrop);
		}
		else  {
			if (action == DnDConstants.ACTION_MOVE) {
				dsde.getDragSourceContext().setCursor(DragSource.DefaultMoveDrop);
			} 
			else {
				dsde.getDragSourceContext().setCursor(DragSource.DefaultMoveNoDrop);
			}
		}
	}
	public final void dragExit(DragSourceEvent dse) {
	   dse.getDragSourceContext().setCursor(DragSource.DefaultMoveNoDrop);
	}	
		
	/* Methods for DragGestureListener */
	public final void dragGestureRecognized(DragGestureEvent dge) {
		TreePath path = tree.getSelectionPath(); 
		if (path != null) { 
			draggedNode = (JDomNodeAdapter)path.getLastPathComponent();
			draggedNodeParent = (JDomNodeAdapter)draggedNode.getParent();
			if (drawImage) {
				Rectangle pathBounds = tree.getPathBounds(path); //getpathbounds of selectionpath
				JComponent lbl = (JComponent)tree.getCellRenderer().getTreeCellRendererComponent(tree, draggedNode, false , tree.isExpanded(path),((JDomTreeModelAdapter)tree.getModel()).isLeaf(path.getLastPathComponent()), 0,false);//returning the label
				lbl.setBounds(pathBounds);//setting bounds to lbl
				image = new BufferedImage(lbl.getWidth(), lbl.getHeight(), java.awt.image.BufferedImage.TYPE_INT_ARGB_PRE);//buffered image reference passing the label's ht and width
				Graphics2D graphics = image.createGraphics();//creating the graphics for buffered image
				graphics.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.5f));	//Sets the Composite for the Graphics2D context
				lbl.setOpaque(false);
				lbl.paint(graphics); //painting the graphics to label
				graphics.dispose();				
			}
			dragSource.startDrag(dge, DragSource.DefaultMoveNoDrop , image, new Point(0,0), new TransferableNode(draggedNode), this);			
		}	 
	}
 
	/* Methods for DropTargetListener */
 
	public final void dragEnter(DropTargetDragEvent dtde) {
		Point pt = dtde.getLocation();
		int action = dtde.getDropAction();
		if (drawImage) {
			paintImage(pt);
		}
		if (canPerformAction(tree, draggedNode, action, pt)) {
			dtde.acceptDrag(action);			
		}
		else {
			dtde.rejectDrag();
		}
	}
 
	public final void dragExit(DropTargetEvent dte) {
		if (drawImage) {
			clearImage();
		}
	}
 
	public final void dragOver(DropTargetDragEvent dtde) {
		Point pt = dtde.getLocation();
		int action = dtde.getDropAction();
		tree.autoscroll(pt);
		if (drawImage) {
			paintImage(pt);
		}
		if (canPerformAction(tree, draggedNode, action, pt)) {
			dtde.acceptDrag(action);			
		}
		else {
			dtde.rejectDrag();
		}
	}
 
	public final void dropActionChanged(DropTargetDragEvent dtde) {
		Point pt = dtde.getLocation();
		int action = dtde.getDropAction();
		if (drawImage) {
			paintImage(pt);
		}
		if (canPerformAction(tree, draggedNode, action, pt)) {
			dtde.acceptDrag(action);			
		}
		else {
			dtde.rejectDrag();
		}
	}
 
	public final void drop(DropTargetDropEvent dtde) {
		try {
			if (drawImage) {
				clearImage();
			}
			int action = dtde.getDropAction();
			Transferable transferable = dtde.getTransferable();
			Point pt = dtde.getLocation();
			if (transferable.isDataFlavorSupported(TransferableNode.NODE_FLAVOR) && canPerformAction(tree, draggedNode, action, pt)) {
				TreePath pathTarget = tree.getPathForLocation(pt.x, pt.y);
				JDomNodeAdapter node = (JDomNodeAdapter) transferable.getTransferData(TransferableNode.NODE_FLAVOR);
				JDomNodeAdapter newParentNode =(JDomNodeAdapter)pathTarget.getLastPathComponent();
				if (executeDrop(tree, node, newParentNode, action)) {
					dtde.acceptDrop(action);				
					dtde.dropComplete(true);
					return;					
				}
			}
			dtde.rejectDrop();
			dtde.dropComplete(false);
		}		
		catch (Exception e) {	
			System.out.println(e.getStackTrace());
			e.printStackTrace();
			dtde.rejectDrop();
			dtde.dropComplete(false);
		}	
	}
	
	private final void paintImage(Point pt) {
		tree.paintImmediately(rect2D.getBounds());
		rect2D.setRect((int) pt.getX(),(int) pt.getY(),image.getWidth(),image.getHeight());
		tree.getGraphics().drawImage(image,(int) pt.getX(),(int) pt.getY(),tree);
	}
 
	private final void clearImage() {
		tree.paintImmediately(rect2D.getBounds());
	}
 
	public abstract boolean canPerformAction(MartConfigTree target, JDomNodeAdapter draggedNode, int action, Point location);
 
	public abstract boolean executeDrop(MartConfigTree tree, JDomNodeAdapter draggedNode, JDomNodeAdapter newParentNode, int action);
}
