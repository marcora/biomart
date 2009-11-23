package org.biomart.configurator.component;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.border.EtchedBorder;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import org.biomart.common.resources.Resources;
import org.biomart.configurator.jdomUtils.JDomUtils;
import org.biomart.configurator.utils.type.IdwViewType;
import org.biomart.configurator.view.idwViews.McViewTree;
import org.biomart.configurator.view.idwViews.McViews;
import org.biomart.configurator.view.menu.PtTableContextMenu;
import org.jdom.Element;


public class PartitionTablePanel extends JPanel implements TableModelListener {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private PtTableContextMenu popupMenu;
	private JTable table;
	private Element ptElement;

	public PartitionTablePanel(Element ptElement) {
		this.ptElement = ptElement;
		List<ArrayList<String>> ptValue = JDomUtils.ptElement2Table(ptElement);
		this.init(ptValue);
	}
	
	private void init(List<ArrayList<String>> ptValue) {
		this.setLayout(new BorderLayout());
		
		PtModel model = new PtModel(ptValue, ptElement.getAttributeValue(Resources.get("NAME")));
		model.addTableModelListener(this);
		McViewTree treeView = (McViewTree)McViews.getInstance().getView(IdwViewType.MCTREE);
		model.addTableModelListener(treeView.getMcTree());
		table = new JTable(model);
		table.setShowGrid(true);
		table.setGridColor(Color.BLACK);
		table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		this.createContextMenu();
		
		MouseListener popupListener = new PopupListener();
		table.addMouseListener(popupListener);
		table.getTableHeader().addMouseListener(popupListener);
		table.setPreferredScrollableViewportSize(table.getPreferredSize());

		JScrollPane scrollPane = new JScrollPane(table);
		this.add(scrollPane, BorderLayout.SOUTH);	
		
		JLabel nameLabel = new JLabel(ptElement.getAttributeValue(Resources.get("NAME")));
		this.add(nameLabel, BorderLayout.NORTH);
		this.setBorder(new EtchedBorder());
	}
	
	private void createContextMenu() {
		popupMenu = new PtTableContextMenu(this.table, ptElement);
	}
	
	
	class PopupListener implements MouseListener {
		public void mousePressed(MouseEvent e) {
			showPopup(e);
		}

		public void mouseReleased(MouseEvent e) {
			showPopup(e);
		}

		private void showPopup(MouseEvent e) {
			if (e.isPopupTrigger()) {
				popupMenu.setSelectedColumn(table.columnAtPoint(e.getPoint()));				
				popupMenu.show(e.getComponent(), e.getX(), e.getY());
			}
		}

		public void mouseClicked(MouseEvent e) {
		}

		public void mouseEntered(MouseEvent e) {
		}

		public void mouseExited(MouseEvent e) {
		}
	}


	public void tableChanged(TableModelEvent e) {
		this.table.setPreferredScrollableViewportSize(this.table.getPreferredSize());
		
	}
	
}

