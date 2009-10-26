package org.biomart.common.view.gui.dialogs;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.biomart.common.resources.Resources;
import org.jdom.Element;

public class DsColumnsDialog extends JDialog {
	
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private Element dstElement;
	private String partitionColumn;
	private JComboBox dsCombo;
	
	public DsColumnsDialog(Element dsTable) {
		this.dstElement = dsTable;
		this.init();
		this.setModal(true);
		this.pack();
		this.setLocationRelativeTo(null);
		this.setVisible(true);
	}
	
	private void init() {
		JPanel content = new JPanel(new BorderLayout());
		this.partitionColumn = null;
		JPanel comboPanel = new JPanel(new FlowLayout());
		JLabel label = new JLabel("Choose column: ");
		comboPanel.add(label);
		
		dsCombo = new JComboBox();
		List<Element> dsTableList = dstElement.getChildren(Resources.get("COLUMN"));
		for(Element dsTable:dsTableList) {
			dsCombo.addItem(dsTable.getAttributeValue(Resources.get("NAME")));
		}
		comboPanel.add(dsCombo);
		content.add(comboPanel, BorderLayout.NORTH);
		
		JPanel buttonPanel = new JPanel(new FlowLayout());
		JButton okButton = new JButton("OK");
		okButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				setPartitionColumn();	
				DsColumnsDialog.this.setVisible(false);
			}
			
		});
		
		JButton cancelButton = new JButton("Cancel");
		buttonPanel.add(cancelButton);
		buttonPanel.add(okButton);
		content.add(buttonPanel, BorderLayout.SOUTH);
		
		this.add(content);

	}
	
	private void setPartitionColumn() {
		this.partitionColumn = (String)this.dsCombo.getSelectedItem();
	}
	
	public String getPartitionColumn() {
		return this.partitionColumn;
	}
}