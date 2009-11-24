package org.biomart.configurator.controller;
import java.awt.*;
import javax.swing.*;
import javax.swing.event.TreeModelEvent;
import javax.swing.tree.*;

import java.awt.dnd.*;
import java.awt.datatransfer.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.biomart.common.resources.Resources;
import org.biomart.configurator.utils.McGuiUtils;
import org.biomart.configurator.view.MartConfigTree;
import org.biomart.configurator.view.gui.dialogs.PartitionsSelectDialog;
import org.biomart.configurator.jdomUtils.JDomNodeAdapter;
import org.biomart.configurator.jdomUtils.JDomTreeModelAdapter;
import org.biomart.configurator.model.PartitionTableModel;
import org.biomart.configurator.model.PartitionMatchModel;
import org.biomart.configurator.model.PartitionMatchBean;
import org.biomart.configurator.model.PartitionMatchTableModel;
import org.jdom.Element;
 
public class DefaultTreeTransferHandler extends AbstractTreeTransferHandler {
	
	private List<String> acceptedTag;
	private final int NP2NP = 0;
	private final int NP2P = 1;
	private final int P2NP = 2;
	private final int P2P = 3;
	
	public DefaultTreeTransferHandler(MartConfigTree tree, int action) {
		super(tree, action, true);
		acceptedTag = new ArrayList<String>();
		acceptedTag.add(Resources.get("ATTRIBUTEPOINTER"));
		acceptedTag.add(Resources.get("FILTER"));
		acceptedTag.add(Resources.get("ATTRIBUTE"));
	}
 
	/**
	 * rule: attribute - attribute
	 */
	public boolean canPerformAction(MartConfigTree target, JDomNodeAdapter draggedNode, int action, Point location) {
		TreePath pathTarget = target.getPathForLocation(location.x, location.y);
		if (pathTarget == null) {
			target.setSelectionPath(null);
			return(false);
		}
		target.setSelectionPath(pathTarget);
		if(action == DnDConstants.ACTION_COPY) {
			JDomNodeAdapter node = (JDomNodeAdapter)pathTarget.getLastPathComponent();
			if(node.getNode().getName().equalsIgnoreCase(Resources.get("ATTRIBUTEPOINTER")) || 
					node.getNode().getName().equalsIgnoreCase(Resources.get("FILTER")))
				return(true);
			else 
				return false;
		}
		else
		if(action == DnDConstants.ACTION_MOVE) {	
			JDomNodeAdapter node = (JDomNodeAdapter)pathTarget.getLastPathComponent();
			if(node.getNode().getName().equalsIgnoreCase(Resources.get("CONTAINER")))
				return(true);	
			
			else {
				return(false);
			}				 
		}
		else {		
			return(false);	
		}
	}
 
	public boolean executeDrop(MartConfigTree target, JDomNodeAdapter draggedNode, JDomNodeAdapter newParentNode, int action) { 
		if (action == DnDConstants.ACTION_COPY) {
			return(true);
		}
		//FIXME should move this part to somewhere
		//so far, assume that the source is attributePointer, the target is container
		if (action == DnDConstants.ACTION_MOVE) {
			Element sourceElement = ((JDomNodeAdapter)draggedNode).getNode();
			Element targetElement = ((JDomNodeAdapter)newParentNode).getNode();
			if(!this.acceptedTag.contains(sourceElement.getName())) {
				JOptionPane.showMessageDialog(target, "cannot move from '"+sourceElement.getName()+"' to '"+targetElement.getName()+"'");
				return false;
			}
			
			String source = sourceElement.toString();
			String tar = targetElement.toString();
			String martString = Resources.get("MART");
			System.out.println("move from "+source+" to "+tar);
			
			String sourceSourceRangeStr="", sourceTargetRangeStr="";
			if(sourceElement.getName().equalsIgnoreCase(Resources.get("ATTRIBUTEPOINTER"))) {
				sourceSourceRangeStr = sourceElement.getAttributeValue(Resources.get("SOURCERANGE"));
				sourceTargetRangeStr = sourceElement.getAttributeValue(Resources.get("TARGETRANGE"));
			}
			Element sourceMart = target.getAncestor(sourceElement, martString);
			Element targetMart = target.getAncestor(targetElement, martString);
			Map<String,PartitionTableModel> sourcePTList = target.getPartitionTable(sourceMart);
			Map<String,PartitionTableModel> targetPTList = target.getPartitionTable(targetMart);
			
			if(sourcePTList.isEmpty() && targetPTList.isEmpty()) {
				//np-np
				//this.elementCopy(sourceElement, targetElement,"", "",this.NP2NP);
				this.elementMove(sourceElement, targetElement);
			} else if(targetPTList.isEmpty() && !sourcePTList.isEmpty()) {
				//p-np
				this.elementCopy(sourceElement, targetElement,"", "",this.P2NP);
			} else {
				PartitionMatchModel pmm = new PartitionMatchModel(sourcePTList,targetPTList,sourceSourceRangeStr,sourceTargetRangeStr);
				List<PartitionMatchBean> modelList = pmm.getCombinationWithBoolean();
				PartitionsSelectDialog psDialog = PartitionsSelectDialog.getInstance();
				PartitionMatchTableModel pmtm = new PartitionMatchTableModel(modelList);
				psDialog.setTableModel(pmtm);
				psDialog.setVisible(true);
				if(psDialog.aborted()) {
					JOptionPane.showMessageDialog(target, "Drag and Drop cancelled by user");
					return false;
				}
				String tRange = psDialog.getRangeString(false);	
				if(!sourcePTList.isEmpty() && !targetPTList.isEmpty()) {
					//p-p
					String sRange = psDialog.getRangeString(true);
					this.elementCopy(sourceElement, targetElement,sRange, tRange,this.P2P);
				}
				else //np-p
					this.elementCopy(sourceElement, targetElement,"", tRange,this.NP2P);
			}
			//FIXME the tree collapsed after nodeStructureChanged
			((JDomTreeModelAdapter)target.getModel()).nodeStructureChanged(newParentNode);
			McGuiUtils.refreshGui(newParentNode);
			return(true);
		}
		return(false);
	}	
		
	private void elementCopy(Element sourceE, Element targetE, String sourceRange, String targetRange, int type) {
    	Element copy = (Element)sourceE.clone();
    	copy.detach();

    	if(type == this.NP2P) {
    		copy.setAttribute(Resources.get("TARGETRANGE"),targetRange);
    	} else if (type == this.P2NP) {
    		copy.setAttribute(Resources.get("TARGETRANGE"),"");
    	} else if (type == this.P2P) {
    		copy.setAttribute(Resources.get("TARGETRANGE"),targetRange);
    		copy.setAttribute(Resources.get("SOURCERANGE"),sourceRange);
    	}
    	copy.setAttribute(Resources.get("POINTER"),"true");
    	copy.setAttribute(Resources.get("DSTABLE"),"");
    	copy.setAttribute(Resources.get("TARGETFIELD"),"");
    	copy.setAttribute(Resources.get("ATTRIBUTEPOINTER"),sourceE.getAttributeValue(Resources.get("NAME")));
    	targetE.addContent(copy); 		
	}
	
	/**
	 * assume type is np2np for now
	 * @param sourceE
	 * @param targetE
	 */
	private void elementMove(Element sourceE, Element targetE) {
		Element copy = (Element)sourceE.detach();
		targetE.addContent(copy);
	}
}
