package org.biomart.configurator.view.gui.dialogs;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Vector;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.table.AbstractTableModel;

import org.biomart.common.resources.Resources;
import org.biomart.configurator.jdomUtils.JDomNodeAdapter;
import org.biomart.configurator.jdomUtils.JDomUtils;
import org.biomart.configurator.model.object.PortableObject;
import org.biomart.configurator.utils.McGuiUtils;
import org.biomart.configurator.utils.type.PortableType;
import org.jdom.Element;



public class AddProcessorDialog2 extends JDialog {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private String portableType;
	private PortableType type;

	private JDomNodeAdapter node;
	private List<String> selectedPortable = null;
	private JTable table;
	private PortableObject portableObject;
	private String processorName;

	
	
	public AddProcessorDialog2(JDomNodeAdapter dsNode, PortableType type, String name) {
		portableType = "Processor";
		this.processorName = name;
		this.node = dsNode;
		this.type = type;
		this.init();
		this.setModal(true);
		this.pack();
		this.setLocationRelativeTo(null);
		this.setVisible(true);
	}
	
	private void init() {
		JPanel content = new JPanel(new BorderLayout());
			
		table = new JTable(new MyTableModel(this.node) );
		table.setShowGrid(true);
		table.setGridColor(Color.BLACK);
		JScrollPane scroll = new JScrollPane(table);
		
		JButton okButton = new JButton("OK");
		okButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				createPortableName();
			}			
		});
		JPanel processorPanel = new JPanel();
		JLabel label = new JLabel("processor: Pie Chart");
		JTextArea tarea = new JTextArea();
		tarea.setText("filter1 \nfilter2 \nfilter3");
		processorPanel.add(label);
		processorPanel.add(tarea);
		content.add(scroll, BorderLayout.NORTH);
		content.add(okButton, BorderLayout.SOUTH);
		content.add(processorPanel,BorderLayout.CENTER);
		this.add(content);
	}
	
	private void setSelectedPortable() {
		selectedPortable = new ArrayList<String>();
		for(int i=0; i<table.getModel().getRowCount(); i++) {
			Boolean sel = (Boolean)table.getModel().getValueAt(i, 1);
			if(sel)
				selectedPortable.add((String)table.getModel().getValueAt(i, 0));				
		}
	}
	
	private void createPortableName() {
		setSelectedPortable();
		AddProcessorDialog2.this.setVisible(false);
		LinkItemsDialog linkDialog = new LinkItemsDialog(this.type, this.processorName);
		linkDialog.setVisible(true);
		this.portableObject = new PortableObject(linkDialog.getPortableName(),this.type);
	}
	
	
	public PortableObject getPortableObject() {
		return this.portableObject;
	}
	
    class MyTableModel extends AbstractTableModel {

        /**
		 * 
		 */
		private static final long serialVersionUID = 1L;
		private Object[][] data;
        private String[] columnNames = {portableType,"Selected"};
        
        public MyTableModel(JDomNodeAdapter dsnode) {
    		List<String> portableList = new ArrayList<String>();
    		List<Element> selList  = new ArrayList<Element>();
    		
    		HashMap<String, String> conditions = new HashMap<String, String>(); 
    		List<Element> containerElements = JDomUtils.findChildElements(dsnode.getNode(), Resources.get("CONTAINER"), conditions);
			for(Element containerE:containerElements) {
	    		selList.addAll(JDomUtils.searchElementList(containerE, Resources.get("ATTRIBUTEPOINTER"), null));
				selList.addAll(JDomUtils.searchElementList(containerE, Resources.get("ATTRIBUTE"), null));
			}
    			
    		for(Element item:selList) {
    			String name = item.getAttributeValue(Resources.get("NAME"));
    			portableList.add(name);
    		}
    		data = new Object[portableList.size()][2];
    		for(int i=0; i<portableList.size(); i++) {
    			data[i][0] = portableList.get(i);
    			data[i][1] = new Boolean(false);
    		}

        }

        public int getColumnCount() {
            return columnNames.length;
        }

        public int getRowCount() {
            return data.length;
        }

        public String getColumnName(int col) {
            return columnNames[col];
        }

        public Object getValueAt(int row, int col) {
            return data[row][col];
        }

        /*
         * JTable uses this method to determine the default renderer/
         * editor for each cell.  If we didn't implement this method,
         * then the last column would contain text ("true"/"false"),
         * rather than a check box.
         */
        public Class getColumnClass(int c) {
            return getValueAt(0, c).getClass();
        }

        /*
         * Don't need to implement this method unless your table's
         * editable.
         */
        public boolean isCellEditable(int row, int col) {
            //Note that the data/cell address is constant,
            //no matter where the cell appears onscreen.
            if (col < 1) {
                return false;
            } else {
                return true;
            }
        }

        /*
         * Don't need to implement this method unless your table's
         * data can change.
         */
        public void setValueAt(Object value, int row, int col) {
 
            data[row][col] = value;
            fireTableCellUpdated(row, col);

        }

     }

	
}