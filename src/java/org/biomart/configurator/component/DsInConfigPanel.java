package org.biomart.configurator.component;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;

import org.biomart.common.resources.Resources;
import org.biomart.configurator.jdomUtils.JDomUtils;
import org.biomart.configurator.utils.McGuiUtils;
import org.jdom.Element;

public class DsInConfigPanel extends JPanel implements ItemListener {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private Element dsElement;
	private Map<String, Integer> ptCurrentIndex;
	private Map<String, List<ArrayList<String>>> ptTableMap;
	private Map<JComponent, Element> updateList;

	public DsInConfigPanel(Element dsElement) {
		this.dsElement = dsElement;
		ptTableMap = new HashMap<String, List<ArrayList<String>>>();
		ptCurrentIndex = new HashMap<String, Integer>();
		updateList = new HashMap<JComponent, Element>();
		this.initGui();
	}
	
	private void initGui() {
		this.setLayout(new GridBagLayout());
		this.setBorder(new TitledBorder(new EtchedBorder(),dsElement.getAttributeValue(Resources.get("NAME"))));
		//find all partition tables in this dataset
		List<Element>ptList = dsElement.getChildren(Resources.get("PARTITIONTABLE"));
		for(Element ptElement:ptList) {
			List<ArrayList<String>> ptTable = JDomUtils.ptElement2Table(ptElement);
			ptTableMap.put(ptElement.getAttributeValue(Resources.get("NAME")), ptTable);
			//default 0
			ptCurrentIndex.put(ptElement.getAttributeValue(Resources.get("NAME")), 0);
		}
		//find the one has the same user and gui
		HashMap<String, String> conditions = new HashMap<String, String>();
		List<Element> myContainers = JDomUtils.findChildElements(dsElement, Resources.get("CONTAINER"), conditions);
		Set<String> ftSet = this.getFlattenPTableName();
		GridBagConstraints c = new GridBagConstraints();
		c.gridy = 0;
		c.gridx = 0;
		c.anchor = GridBagConstraints.FIRST_LINE_START;
		for(Element container:myContainers) {
			if(JDomUtils.isElementHiden(container))
				continue;
				//if container is empty, don't add it
			if(!this.isContainerEmpty(container)) {
				this.add(createContainerPanel(container, dsElement,ftSet,false), c);
				c.gridx = c.gridx + 1;
			}
		}		
	}
	
	private boolean isContainerEmpty(Element container) {
		if(JDomUtils.searchElement(container, Resources.get("FILTER"), null) == null &&
				JDomUtils.searchElement(container, Resources.get("ATTRIBUTE"), null) == null &&
				JDomUtils.searchElement(container, Resources.get("ATTRIBUTEPOINTER"), null) == null) {
			return true;
		}else
			return false;
	}
	
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
			if(JDomUtils.isElementHiden(child))
				continue;
			if(child.getName().equals(Resources.get("CONTAINER"))) {
				if(!this.isContainerEmpty(child)) {
					c.gridx = labelx;
					c.gridy = y;
					containerPanel.add(createContainerPanel(child, dsElement,flattenTableSet,false),c);
					y++;
				}
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
	
	private List<Element> getFlattenPTable() {
		Map<String, String> conditions = new HashMap<String, String>();
		conditions.put(Resources.get("FLATTEN"), "1");
		List<Element> flatPtList = JDomUtils.findDescendentElements(dsElement, Resources.get("PARTITIONTABLE"), 
				conditions);
		return flatPtList;
	}
	
	private Set<String> getFlattenPTableName() {
		Set<String> ptSet = new HashSet<String>();
		List<Element> ptEList = this.getFlattenPTable();
		for(Element ptElement: ptEList) {
			ptSet.add(ptElement.getAttributeValue(Resources.get("DSTABLE")));
		}
		return ptSet;
	}
	
	private boolean hasFlattenTables() {
		return this.getFlattenPTable().size()>0;
	}

	private String renamePartitionReference(String reference, Map<String, List<ArrayList<String>>> ptTableMap) {
		String[] arr = reference.split(Resources.get("COLPREFIX"));
		String res = "";
		if(arr.length!=2)
			return res;
		String ptName = arr[0];
		String colStr = arr[1];
		int col = 0;
		try{
			col = Integer.parseInt(colStr);
		}catch(Exception e) {
			return res;
		}
		List<ArrayList<String>> ptTable = ptTableMap.get(ptName);
		int index = ptCurrentIndex.get(ptName);
		try {
			res = "("+ptTable.get(index).get(col-1)+")";
		}catch(Exception e) {
			return res;
		}
		return res;
	}

	private String renameWithPartitionInfo(String oldName, Map<String, List<ArrayList<String>>> ptTableMap) {
		if(ptTableMap.isEmpty())
			return oldName;
		if(!JDomUtils.hasPartitionReference(oldName))
			return oldName;
		List<String> list = JDomUtils.extractPartitionReferences(oldName, false);
		String tmp="";
		for(int i=0; i<list.size(); i++) {
			if(i % 2 == 0 ) 
				tmp = tmp+list.get(i);
			else {
				String tmp1 = list.get(i);
				tmp = tmp + this.renamePartitionReference(tmp1.substring(1, tmp1.length()-1), ptTableMap);
			}
		}
		return tmp;
	}	

	private boolean canFlatten() {
		List<Element> flattenPtList = this.getFlattenPTable();
		if(flattenPtList==null || flattenPtList.size() == 0)
			return true;
		for(Element pt:flattenPtList) {
			//TODO find attribute, see if there are mixed attributes in this container
			pt.getAttributeValue("");
		}
		return true;
	}
	

	public void itemStateChanged(ItemEvent e) {
		PartitionedFilterDropDown pf = (PartitionedFilterDropDown)e.getSource();
		ptCurrentIndex.put(pf.getPartitionTable(), pf.getSelectedIndex());
		this.updateLabels();
	}
	
	private void updateLabels() {
		Iterator<Entry<JComponent, Element>> it = this.updateList.entrySet().iterator();
		while(it.hasNext()) {
			Entry<JComponent, Element> entry = it.next();
			String oldName = entry.getValue().getAttributeValue(Resources.get("NAME"));
			String newName = this.renameWithPartitionInfo(oldName, ptTableMap);
			if(entry.getKey().getClass().getName().equals(JLabel.class.getName()))
				((JLabel)entry.getKey()).setText(newName);
			else
				((JCheckBox)entry.getKey()).setText(newName);
		}
	}
	
	private boolean isDsColMasked(Element dsTableElement, String dsColName) {
		if(dsTableElement==null) //if it is dragged from other dataset
			return false;
		Element attributeElement = JDomUtils.searchElement(dsTableElement, Resources.get("ATTRIBUTE"),dsColName);
		if(attributeElement==null) //check later
			return false;
		String maskStr = attributeElement.getAttributeValue(Resources.get("maskColumnTitle"));
		if("1".equals(maskStr))
			return true;
		else 
			return false;
	}
	
}