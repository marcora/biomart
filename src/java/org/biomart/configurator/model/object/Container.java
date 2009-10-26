package org.biomart.configurator.model.object;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;

import org.biomart.common.resources.Resources;
import org.biomart.configurator.component.PartitionedFilterDropDown;
import org.biomart.configurator.jdomUtils.JDomNodeAdapter;
import org.biomart.configurator.jdomUtils.JDomUtils;
import org.biomart.configurator.utils.type.McNodeType;
import org.jdom.Element;

public class Container extends McNode{
	private List<Container> containerList;
	private List<Filter> filterList;
	private List<Attribute> attributeList;
	
	public Container(JDomNodeAdapter node) {
		this.node = node;
		this.type = McNodeType.Container;
	}

	@Override
	public JComponent getGuiComponent() {
		// TODO Auto-generated method stub
		return null;
	}
/*	
	private JPanel createContainerPanel(Element e, Element dsElement, Set<String> flattenTableSet, boolean clone) {
		//element is a container element
		boolean hasFlattenAttribute = false;
		List<ArrayList<String>> flattendata = null;
		String dsTableName = "";
		JPanel containerPanel = new JPanel(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		c.anchor = GridBagConstraints.FIRST_LINE_START;
		c.insets = new Insets(0,5,0,5);
		int labelx = 0;
		int tfx = 1;

		int y = 0;
		containerPanel.setBorder(new  TitledBorder(new EtchedBorder(), e.getAttributeValue(Resources.get("NAME"))));
		List<Element> childList = e.getChildren();
		for(Element child:childList) {
			if(child.getName().equals(Resources.get("CONTAINER"))) {
				c.gridx = labelx;
				c.gridy = y;
				containerPanel.add(createContainerPanel(child, dsElement,flattenTableSet,false),c);
				y++;
			} else {
				dsTableName = child.getAttributeValue(Resources.get("DSTABLE"));
				if(flattenTableSet.contains(dsTableName) && hasFlattenAttribute == false) {
					hasFlattenAttribute = true;
					Element ptElement = JDomUtils.searchElementWithCondition(dsElement, Resources.get("PARTITIONTABLE"),
							Resources.get("DSTABLE"),dsTableName);
					flattendata = JDomUtils.ptElement2Table(ptElement);
				}
				String oldLabelName = child.getAttributeValue(Resources.get("NAME"));
				String labelName = this.renameWithPartitionInfo(oldLabelName, ptTableMap);
				//check masked
				Element dsTableElement = JDomUtils.searchElement(dsElement, Resources.get("DSTABLE"), dsTableName);
				boolean colMasked = this.isDsColMasked(dsTableElement, oldLabelName);
				if(colMasked)
					continue;
				if(child.getName().equals(Resources.get("FILTER"))) {
					String ptName = child.getAttributeValue(Resources.get("PARTITIONTABLE"));
					if(!hasFlattenAttribute && ptName!=null && !ptName.equals("")) {
						Element ptElement = JDomUtils.searchElement(dsElement, Resources.get("PARTITIONTABLE"),ptName);
						List<ArrayList<String>> data = JDomUtils.ptElement2Table(ptElement);
						PartitionedFilterDropDown pfo = new PartitionedFilterDropDown(data, child,this);
						c.gridx = labelx;
						c.gridy = y;
						if(!colMasked) {
							JLabel label = new JLabel(labelName);
							this.updateList.put(label,child);
							containerPanel.add(label,c);
							c.gridx = tfx;
							containerPanel.add(pfo,c);
						}
					} else {							
						JLabel label = new JLabel(labelName);
						c.gridx = labelx;
						c.gridy = y;
						containerPanel.add(label,c);
						this.updateList.put(label,child);
						c.gridx=tfx;
						JTextField tf = new JTextField(5);					
						containerPanel.add(tf,c);
					}
					y++;					
				}else {
					JCheckBox cb = new JCheckBox(labelName);
					this.updateList.put(cb,child);
					c.gridx = labelx;
					c.gridy = y;
					containerPanel.add(cb,c);
					y++;
				}
			}
		}
		if(!clone && hasFlattenAttribute) {
			JPanel panel = new JPanel(new GridLayout(0,1));
			containerPanel.setBorder(new  TitledBorder(new EtchedBorder(), dsTableName+
					"("+flattendata.get(0).get(0)+")"));
			panel.add(containerPanel);
			for(int i=1; i<flattendata.size(); i++) {
				//get partitiontable name
				Element pt = JDomUtils.searchElementWithCondition(dsElement, Resources.get("PARTITIONTABLE"), Resources.get("DSTABLE"),
						dsTableName);
				this.ptCurrentIndex.put(pt.getAttributeValue(Resources.get("NAME")), i);
				JPanel clonePanel = this.createContainerPanel(e, dsElement, flattenTableSet, true);
				clonePanel.setBorder(new  TitledBorder(new EtchedBorder(), dsTableName+
						"("+flattendata.get(i).get(0)+")"));
				panel.add(clonePanel);
			}
			return panel;
		}else
			return containerPanel;
	}
*/
}