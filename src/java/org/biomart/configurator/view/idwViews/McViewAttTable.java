package org.biomart.configurator.view.idwViews;

import java.awt.Component;
import java.util.List;
import java.util.Observable;
import javax.swing.Icon;
import javax.swing.JScrollPane;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import org.biomart.common.resources.Resources;
import org.biomart.configurator.jdomUtils.JDomNodeAdapter;
import org.biomart.configurator.model.McModel;
import org.biomart.configurator.model.XMLAttributeTableModel;
import org.biomart.configurator.utils.type.IdwViewType;
import org.biomart.configurator.view.AttributeTable;
import org.biomart.configurator.view.McView;
import org.jdom.Element;

public class McViewAttTable extends McView implements TreeSelectionListener{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	public McViewAttTable(String title, Icon icon, Component component,
			McModel model, IdwViewType type) {
		super(title, icon, component, model, type);
		
	}

	@Override
	public void update(Observable observable, Object obj) {
		// TODO Auto-generated method stub
		
	}

	public void valueChanged(TreeSelectionEvent e) {
		Object lpc = e.getPath().getLastPathComponent();
		if (lpc instanceof JDomNodeAdapter) {
			Element node = ((JDomNodeAdapter) lpc).getNode();
			this.getTable().setContextMenuEnv((JDomNodeAdapter)lpc);
			updateAttributeTable((JDomNodeAdapter) lpc);
			//FIXME: hardcode for config
			if (node.getName().equalsIgnoreCase(Resources.get("CONFIG"))) {
				List<Element> ptList = node.getParentElement().getChildren(Resources.get("PARTITIONTABLE"));
				if(ptList.isEmpty())
					return;
			}
			
		}

		
	}

	private void updateAttributeTable(JDomNodeAdapter treeNode) {
//		String typeString=Resources.get("DISPLAYTYPE");
		XMLAttributeTableModel tModel = new XMLAttributeTableModel(treeNode);
/*		for (int i=0; i<list.size(); i++) {
			if(list.get(i).getName().equalsIgnoreCase(typeString)) {
				String[] type = {"boolean","list","text","tree"};
				JComboBox comboBox = new JComboBox(type);
				DefaultCellEditor cd = new DefaultCellEditor(comboBox);
				ComboBoxRenderer cr = new ComboBoxRenderer();
				this.getTable().getCellEditorRendererModel().addEditor(i, cd);
				this.getTable().getCellEditorRendererModel().addRenderer(i, cr);
			}
		}*/
		this.getTable().setModel(tModel);
	}

	public AttributeTable getTable() {
		JScrollPane componentS = (JScrollPane)this.getComponent();
		Component c = componentS.getViewport().getView();
		return (AttributeTable) c;

	}
	
}