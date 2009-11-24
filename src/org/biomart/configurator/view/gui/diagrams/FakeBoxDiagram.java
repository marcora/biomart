package org.biomart.configurator.view.gui.diagrams;

import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.border.EtchedBorder;

import org.biomart.common.resources.Resources;
import org.jdom.Element;

public class FakeBoxDiagram extends JPanel {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	
	public FakeBoxDiagram(Element node, boolean child) {
		this.drawComponents(node, child);
	}
	
	public void resetNode(Element node, boolean child) {
		this.removeAll();
		this.drawComponents(node, child);
		this.validate();
	}
	
	private void drawComponents(Element node, boolean child) {
		this.setLayout(new FlowLayout());
		if(child) {
			List<Element> locationList = node.getChildren();
			for(Element location:locationList) {
				JLabel label = new JLabel(location.getAttributeValue(Resources.get("NAME")));
//				label.setBackground(Color.YELLOW);
				label.setBorder(new EtchedBorder());
				this.add(label);
			}	
		}
		else {
			JLabel label = new JLabel(node.getAttributeValue(Resources.get("NAME")));		
//			label.setBackground(Color.YELLOW);
			label.setBorder(new EtchedBorder());
			this.add(label);
		}
			
	}
	
}