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
import org.biomart.configurator.jdomUtils.JDomNodeAdapter;
import org.biomart.configurator.jdomUtils.JDomUtils;
import org.jdom.Element;

public class DsTablesDialog extends JDialog {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private JDomNodeAdapter dataset;
	private JComboBox dsCombo;
	private String partitionColumn;
	private String partitionTable;
	
	public DsTablesDialog(JDomNodeAdapter ds) {
		this.dataset = ds;
		this.init();
		this.setModal(true);
		this.pack();
		this.setLocationRelativeTo(null);
		this.setVisible(true);
	}
	
	private void init() {
		JPanel content = new JPanel(new BorderLayout());
		
		JPanel comboPanel = new JPanel(new FlowLayout());
		JLabel label = new JLabel("Choose Dataset Table: ");
		comboPanel.add(label);
		
		dsCombo = new JComboBox();
		List<Element> dsTableList = JDomUtils.searchElementList(this.dataset.getNode(), Resources.get("DSTABLE"), null);
		for(Element dsTable:dsTableList) {
			dsCombo.addItem(dsTable.getAttributeValue(Resources.get("NAME")));
		}
		comboPanel.add(dsCombo);
		content.add(comboPanel, BorderLayout.NORTH);
		
		JPanel buttonPanel = new JPanel(new FlowLayout());
		JButton okButton = new JButton("OK");
		okButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				DsTablesDialog.this.setVisible(false);
				showColumnsDialog();
			}
			
		});
		
		JButton cancelButton = new JButton("Cancel");
		cancelButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				
			}
		});
		buttonPanel.add(cancelButton);
		buttonPanel.add(okButton);
		content.add(buttonPanel, BorderLayout.SOUTH);
		
		this.add(content);
	}
	
	private void showColumnsDialog() {
		String dsTable = (String)this.dsCombo.getSelectedItem();
		this.setPartitionTable(dsTable);
		Element dsTableElement = JDomUtils.searchElement(this.dataset.getNode(), Resources.get("DSTABLE"), dsTable);
		DsColumnsDialog dscDialog = new DsColumnsDialog(dsTableElement);
		this.setPartitionColumn(dscDialog.getPartitionColumn());
	}
	
	private void setPartitionColumn(String columnName) {
		this.partitionColumn = columnName;
	}
	
	public String getPartitionColumn() {
		return this.partitionColumn;
	}
	
	private void setPartitionTable(String tableName) {
		this.partitionTable = tableName;
	}
	
	public String getPartitionTable() {
		return this.partitionTable;
	}
}