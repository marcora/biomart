package org.biomart.configurator.view.gui.diagrams;

import java.awt.FlowLayout;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.swing.JLabel;
import javax.swing.JLayeredPane;
import javax.swing.JPanel;
import org.biomart.common.resources.Resources;
import org.biomart.configurator.jdomUtils.JDomNodeAdapter;
import org.biomart.configurator.jdomUtils.JDomUtils;
import org.biomart.configurator.utils.McGuiUtils;
import org.biomart.configurator.utils.type.IdwViewType;
import org.biomart.configurator.view.idwViews.McViews;
import org.jdom.Element;

public class LinkedPortableDiagram extends JLayeredPane {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private JDomNodeAdapter node;
	private Set<LinkComponent> linkSet;
	//for showing all portable
	private List<LinkTableComponent> ltList;
	
	
	/**
	 * if showall, the node is martRegistry, else the node is dataset
	 * @param node
	 * @param showAll
	 */
	public LinkedPortableDiagram(JDomNodeAdapter node, boolean showAll) {
		//add current dataset
		this.node = node;
		if(showAll) {
			JPanel content = this.showAllPortable();
			content.setBounds(McViews.getInstance().getView(IdwViewType.SCHEMA).getBounds());
			this.add(content);

		}
		else {
			JPanel content = this.showLinkedPortable();
			content.setBounds(McViews.getInstance().getView(IdwViewType.SCHEMA).getBounds());
			this.add(content);			
		}
	}
	

	private JPanel showLinkedPortable() {
		JLabel linkedImpLabel = null;
		JLabel linkedExpLabel = null;
		JPanel content = new JPanel();
		FlowLayout flo = new FlowLayout(FlowLayout.LEFT,50,10);
		content.setLayout(flo);
		LinkTableComponent currentDsPanel = new LinkTableComponent(node);
		content.add(currentDsPanel);
				
		List<Element>impList = JDomUtils.searchElementList(node.getNode(), Resources.get("IMPORTABLE"), null);
		List<Element>expList = JDomUtils.searchElementList(node.getNode(), Resources.get("EXPORTABLE"), null);
		List<Element>processorList = JDomUtils.searchElementList(node.getNode(), Resources.get("PROCESSOR"), null);
		
		List<Element> currentImpNodes = JDomUtils.getCurrentConfigElements(impList);
		List<Element> currentExpNodes = JDomUtils.getCurrentConfigElements(expList);
		List<Element> currentProNodes = JDomUtils.getCurrentConfigElements(processorList);
		currentImpNodes.addAll(currentProNodes);
		
		Element rootElement = ((JDomNodeAdapter)node.getRoot()).getNode();
		//find all datasets in the SAME USER
		List<Element> dsList = JDomUtils.searchElementListInUser(rootElement, Resources.get("DATASET"),null,
				McGuiUtils.INSTANCE.getCurrentUser().getUserName());
		
		linkSet = new HashSet<LinkComponent>();
		
		for(Element datasetE: dsList) {
			//don't show itself again
			if(datasetE.equals(node.getNode()))
				continue;
			Element linkedImp = null;
			Element linkedExp = null;
			
			List<Element>linkedImpNodes = JDomUtils.searchElementList(datasetE, Resources.get("IMPORTABLE"), null);
			List<Element>linkedExpNodes = JDomUtils.searchElementList(datasetE, Resources.get("EXPORTABLE"), null);
			List<Element>linkedProNodes = JDomUtils.searchElementList(datasetE, Resources.get("PROCESSOR"), null);
			List<Element>currentLinkedImps = JDomUtils.getCurrentConfigElements(linkedImpNodes);
			List<Element>currentLinkedExps = JDomUtils.getCurrentConfigElements(linkedExpNodes);
			List<Element>currentLinkedPros = JDomUtils.getCurrentConfigElements(linkedProNodes);
			currentLinkedImps.addAll(currentLinkedPros);
			
			boolean linked = false;			
			
			for(Element imp: currentLinkedImps) {
				for(Element currentExp: currentExpNodes) {
					if(imp.getAttributeValue(Resources.get("NAME")).equalsIgnoreCase(
							currentExp.getAttributeValue(Resources.get("NAME")))) {
						linked = true;
						//remember the importable/exportable
						linkedExpLabel = currentDsPanel.getExpComponent(currentExp.getAttributeValue(Resources.get("NAME")));
						linkedImp = imp;
						linkedExp = currentExp;
						break;
					}
				}
			}
			
			if(linked) {
				LinkTableComponent linkedPanel = new LinkTableComponent(new JDomNodeAdapter(datasetE));
				content.add(linkedPanel);
				linkedImpLabel = linkedPanel.getImpComponent(linkedImp.getAttributeValue(Resources.get("NAME")));
				LinkComponent lc = new LinkComponent(linkedImpLabel,linkedExpLabel);
				linkSet.add(lc);
			} else {
			
				for(Element exp: currentLinkedExps) {
					for (Element currentImp: currentImpNodes) {
						if(exp.getAttributeValue(Resources.get("NAME")).equalsIgnoreCase(
								currentImp.getAttributeValue(Resources.get("NAME")))) {
							linked = true;
							//remember the importable/exportable
							linkedImp = currentImp;
							linkedExp = exp;
							linkedImpLabel = currentDsPanel.getImpComponent(exp.getAttributeValue(Resources.get("NAME")));
							break;
						}
					}
				}
				if(linked) {
					LinkTableComponent linkedPanel = new LinkTableComponent(new JDomNodeAdapter(datasetE));
					content.add(linkedPanel);		
					linkedExpLabel = linkedPanel.getExpComponent(linkedExp.getAttributeValue(Resources.get("NAME")));
					LinkComponent lc = new LinkComponent(linkedImpLabel,linkedExpLabel);
					linkSet.add(lc);					
				}
			}
		}
		return content;
	}
	
	private JPanel showAllPortable() {
		linkSet = new HashSet<LinkComponent>();
		ltList = new ArrayList<LinkTableComponent>();
		JPanel content = new JPanel();
		FlowLayout flo = new FlowLayout(FlowLayout.LEFT,50,10);
		content.setLayout(flo);
		List<JDomNodeAdapter> dsNodeList = JDomUtils.searchElementSInUser(node.getNode(), Resources.get("DATASET"), null);

		for(JDomNodeAdapter dsNode: dsNodeList) {
			LinkTableComponent dsPanel = new LinkTableComponent(dsNode);
			ltList.add(dsPanel);
			content.add(dsPanel);
		}
		//add linkSet
		for(int i=0; i<ltList.size(); i++) {
			for(int j=i+1; j<ltList.size(); j++) {
				Set<LinkComponent> lcSet = ltList.get(i).getLinks(ltList.get(j));
				if(lcSet!=null)
					linkSet.addAll(lcSet);
			}
		}
		
		return content;
	}

	
	public Set<LinkComponent> getLinkComponents() {
		return this.linkSet;
	}
	

}