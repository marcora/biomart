package org.biomart.common.view.gui.dialogs;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JRadioButton;
import javax.swing.JTextField;

import org.biomart.common.resources.Resources;
import org.biomart.configurator.model.User;
import org.biomart.configurator.utils.McGuiUtils;
import org.biomart.configurator.utils.type.McNewUserType;
import org.jdom.Element;

public class AddUserDialog extends JDialog {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private JTextField userNameField;
	private JPasswordField pwField;
	private JPasswordField pw2Field;
	private User newUser;
	private Element rootElement;
	private JRadioButton emptyRB;
	private JRadioButton copyRB;
	private JRadioButton synRB;
	private JComboBox userCB;


	public AddUserDialog(Element root) {
		this.rootElement = root;
		this.init();
		this.setModal(true);
		this.pack();
		this.setLocationRelativeTo(null);
		this.setVisible(true);
	}

	private void init() {
		JPanel content = new JPanel(new BorderLayout());
		newUser = null;

		JPanel buttonPanel = new JPanel(new FlowLayout());
		JButton okButton = new JButton("Ok");

		okButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (createUser())
					AddUserDialog.this.setVisible(false);
			}
		});

		JButton cancelButton = new JButton("Cancel");
		cancelButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				AddUserDialog.this.setVisible(false);
			}			
		});

		buttonPanel.add(cancelButton);
		buttonPanel.add(okButton);

		JPanel userInfoPanel = new JPanel(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();

		JLabel unLabel = new JLabel("User Name: ");
		JLabel pwLabel = new JLabel("Password: ");
		JLabel repwLabel = new JLabel("Password: ");

		userNameField = new JTextField(20);
		pwField = new JPasswordField(20);
		pw2Field = new JPasswordField(20);

		c.gridx = 0;
		c.gridy = 0;

		userInfoPanel.add(unLabel, c);
		c.gridx = 1;
		userInfoPanel.add(userNameField, c);
		c.gridx = 0;
		c.gridy = 1;
		userInfoPanel.add(pwLabel, c);
		c.gridx = 1;
		userInfoPanel.add(pwField, c);
		c.gridx = 0;
		c.gridy = 2;
		userInfoPanel.add(repwLabel, c);
		c.gridx = 1;
		userInfoPanel.add(pw2Field, c);

		JPanel optionPanel = new JPanel(new GridBagLayout());
		JPanel rbPanel = new JPanel(new GridLayout(0, 1));
		emptyRB = new JRadioButton("empty user");
		copyRB = new JRadioButton("copy user from: ");
		synRB = new JRadioButton("Synchronize with user: ");
		rbPanel.add(synRB);
		rbPanel.add(copyRB);
		rbPanel.add(emptyRB);
		
		ButtonGroup group = new ButtonGroup();
		group.add(emptyRB);
		group.add(copyRB);
		group.add(synRB);
		synRB.setSelected(true);

		userCB = new JComboBox(this.getUsersFromTree());
		c.gridy = 0;
		c.gridx = 0;
		optionPanel.add(rbPanel,c);
		c.gridx = 1;
		c.anchor = GridBagConstraints.FIRST_LINE_START;
		c.insets = new Insets(15,0,0,0);
		optionPanel.add(userCB,c);

		content.add(optionPanel, BorderLayout.NORTH);
		content.add(userInfoPanel, BorderLayout.CENTER);
		content.add(buttonPanel, BorderLayout.SOUTH);

		this.add(content);
	}

	private String[] getUsersFromTree() {
		Element martusers = this.rootElement.getChild(Resources.get("MARTUSERS"));
		List<Element> userList = martusers.getChildren(Resources.get("USER"));
		String[] result = new String[userList.size()];
		for(int i=0; i<userList.size(); i++) {
			result[i] = userList.get(i).getAttributeValue(Resources.get("NAME"));
		}
		return result;
	}

	private boolean createUser() {
		if (!this.validateField())
			return false;
		newUser = new User(this.userNameField.getText(),
				this.pwField.getPassword().toString());
		if(this.emptyRB.isSelected())
			newUser.setType(McNewUserType.EMPTY);
		else if(this.copyRB.isSelected()) {
			newUser.setType(McNewUserType.COPY);
			newUser.setSynchronizedUser(userCB.getSelectedItem().toString());
		} else {
			newUser.setType(McNewUserType.SYNCHRONIZE);
			newUser.setSynchronizedUser(userCB.getSelectedItem().toString());
		}
		return true;
	}

	private boolean validateField() {
		if(this.userNameField.getText() == null ||
				this.userNameField.getText().equals(""))
			return false;
		return true;
	}

	public User getUser() {
		return this.newUser;
	}
}