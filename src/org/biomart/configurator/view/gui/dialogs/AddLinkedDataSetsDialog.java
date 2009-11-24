package org.biomart.configurator.view.gui.dialogs;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;

import org.biomart.common.resources.Resources;
import org.biomart.configurator.jdomUtils.JDomNodeAdapter;
import org.biomart.configurator.jdomUtils.JDomUtils;
import org.jdom.Element;


public class AddLinkedDataSetsDialog extends JDialog {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private List<JCheckBox> cbList;
	private JDomNodeAdapter dsNode;
	private List<String> selectedList;
	
	public AddLinkedDataSetsDialog(JDomNodeAdapter treeNode) {
		this.dsNode = treeNode;
		this.init();
		this.setModal(true);
		this.pack();
		this.setLocationRelativeTo(null);
		this.setVisible(true);
	}
	
	private void init() {
		JPanel content = new JPanel(new BorderLayout());	
		cbList = new ArrayList<JCheckBox>();
		
		JButton okButton = new JButton("OK");
		okButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				selectLinkedDataSets();
			}			
		});
		JPanel dsPanel = new JPanel(new GridLayout(0,1));
		dsPanel.setBorder(new TitledBorder(new EtchedBorder(),"Linked DataSets"));
		
		List<Element> linkedDSList = JDomUtils.findLinkedDataSets(this.dsNode, false);
		for (Element linkedDs : linkedDSList) {
			String locStr = linkedDs.getParentElement().getParentElement().getAttributeValue(Resources.get("NAME"));
			String martStr = linkedDs.getParentElement().getAttributeValue(Resources.get("NAME"));
			String dsStr = linkedDs.getAttributeValue(Resources.get("NAME"));
			JCheckBox cb = new JCheckBox(locStr+"->"+martStr+"->"+dsStr);
			cbList.add(cb);
			dsPanel.add(cb);
		}
		
		content.add(okButton, BorderLayout.SOUTH);
		content.add(dsPanel,BorderLayout.CENTER);
		this.add(content);
	}
	
	private void selectLinkedDataSets() {
		selectedList = new ArrayList<String>();
		for(JCheckBox cb:cbList) {
			if(cb.isSelected()) {
				selectedList.add(cb.getText());
			}				
		}
		this.setVisible(false);
	}
	
	public List<String> getSelectedList() {
		return this.selectedList;
	}
}