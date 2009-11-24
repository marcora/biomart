package org.biomart.configurator.view.gui.dialogs;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import org.biomart.common.resources.Resources;
import org.biomart.configurator.jdomUtils.JDomNodeAdapter;
import org.biomart.configurator.utils.type.PortableType;
import org.jdom.Element;

public class LinkItemsDialog extends JDialog{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private String impLabelString = "Importable name: ";
	private String expLabelString = "Exportable name: ";
	private String processorString = "Processor name: ";
	private PortableType type;
	private String portableName;
	private JTextField nameField;
	private String defaultName;
	
	// 0 imp 1 exp
	public LinkItemsDialog(PortableType type, String defaultName) {
		this.type = type;
		this.defaultName = defaultName;
		this.setModal(true);
		this.add(this.createGui());
		this.pack();
		this.setLocationRelativeTo(null);
	}
		
	private JPanel createGui() {
		JPanel content = new JPanel(new BorderLayout());
		JPanel namePanel = new JPanel(new FlowLayout());
		JLabel nameLabel = new JLabel();
		if(this.type.equals(PortableType.IMPORTABLE))
			nameLabel.setText(impLabelString);
		else if(this.type.equals(PortableType.EXPORTABLE))
			nameLabel.setText(expLabelString); 
		else
			nameLabel.setText(processorString);
			
		nameField = new JTextField(20);
		nameField.setText(this.defaultName);
		namePanel.add(nameLabel);
		namePanel.add(nameField);
		content.add(namePanel, BorderLayout.NORTH);
		
		JPanel buttonPanel = new JPanel(new FlowLayout());
		JButton okButton = new JButton("OK");
		buttonPanel.add(okButton);
		okButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				setPortableName();				
			}
			
		});
		content.add(buttonPanel, BorderLayout.SOUTH);
		
		return content;
	}
	
	private void setPortableName() {
		if(!this.nameField.getText().equals("")) {
			this.portableName = this.nameField.getText();
			this.setVisible(false);
		}
	}
	
	public String getPortableName() {
		return this.portableName;
	}
	
}