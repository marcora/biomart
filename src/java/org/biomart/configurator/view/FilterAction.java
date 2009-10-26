package org.biomart.configurator.view;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;
import java.util.Map;

import javax.swing.tree.TreePath;

import org.biomart.common.resources.Resources;
import org.biomart.configurator.jdomUtils.JDomNodeAdapter;
import org.biomart.configurator.jdomUtils.McFilter;
import org.biomart.configurator.jdomUtils.McViewsFilter;
import org.biomart.configurator.utils.McEventObject;
import org.biomart.configurator.utils.McGuiUtils;
import org.biomart.configurator.utils.type.EventType;
import org.biomart.configurator.utils.type.IdwViewType;
import org.biomart.configurator.utils.type.McViewType;
import org.biomart.configurator.view.idwViews.McViewSchema;
import org.biomart.configurator.view.idwViews.McViewTree;
import org.biomart.configurator.view.idwViews.McViews;
import org.biomart.configurator.view.menu.McMenus;
import org.jdom.Element;



public class FilterAction implements ActionListener {

	private McMenus mcMenus;
	
	public FilterAction(McMenus mcMenus) {
		this.mcMenus = mcMenus;
	}
	
	public void actionPerformed(ActionEvent e) {
		if(e.getActionCommand().equals(Resources.get("ALLVIEWS"))) {
			this.setDebugView();
		}else if(e.getActionCommand().equals(Resources.get("CONFIGURATIONVIEW"))) {
			this.applyFilter(McViewType.CONFIGURATION);
		}else if(e.getActionCommand().equals(Resources.get("LINKVIEW"))) {
			this.applyFilter(McViewType.LINK);
		}else if(e.getActionCommand().equals(Resources.get("SOURCEVIEW"))) {
			this.applyFilter(McViewType.SOURCE);
		}else if(e.getActionCommand().equals(Resources.get("TARGETVIEW"))) {
			this.applyFilter(McViewType.TARGET);
		}else if(e.getActionCommand().equals(Resources.get("MATERIALIZEVIEW"))) {
			this.applyFilter(McViewType.MATERIALIZE);
		} else if(e.getActionCommand().equals(Resources.get("UPDATEVIEW"))) {
				this.applyFilter(McViewType.UPDATE);
		}else if(e.getActionCommand().equals(Resources.get("PARTITIONVIEW"))) {
			this.applyFilter(McViewType.PARTITION);
		}
		
	}
	
	private void applyFilter(McViewType type) {
		Map<String, HashMap<String, String>>filters = null;
		this.setMcViewType(type);
		switch(type) {
		case CONFIGURATION:
			filters = this.createConfiguratorFilter();
			this.mcMenus.setEnableDropDownMenus(true);
			break;
		case LINK:
			filters = this.createLinkFilter();
			this.mcMenus.setEnableDropDownMenus(true);
			break;
		case SOURCE:
			filters = this.createSourceFilter();
			this.mcMenus.setEnableDropDownMenus(false);
			break;
		case TARGET:
			filters = this.createTargetFilter();
			this.mcMenus.setEnableDropDownMenus(false);
			break;
		case MATERIALIZE:
			filters = this.createMaterializeFilter();
			this.mcMenus.setEnableDropDownMenus(false);
			break;
		case UPDATE:
			filters = this.createUpdateFilter();
			this.mcMenus.setEnableDropDownMenus(false);
			break;
		case PARTITION:
			filters = this.createPartitionFilter();
			this.mcMenus.setEnableDropDownMenus(false);
			break;
		}
		McFilter filter = new McViewsFilter(filters);
		this.setFilter(filter);
	}
	
	/**
	 * update McGuiUtils and send a message to controller
	 * @param type
	 */
	private void setMcViewType(McViewType type) {
		McGuiUtils.INSTANCE.setMcViewType(type);
		McEventObject obj = new McEventObject(EventType.Update_McViewType,type);
		((McViewSchema)McViews.getInstance().getView(IdwViewType.SCHEMA)).getController().processV2Cupdate(obj);
	}

	
	private void setDebugView() {
		this.setFilter(null);
		this.mcMenus.setEnableDropDownMenus(false);
	}
	
	private Map<String, HashMap<String, String>> createConfiguratorFilter() {		
		  Map<String, HashMap<String, String>>filters = new HashMap<String, HashMap<String, String>>();
		  filters.put(Resources.get("SYNUSERTABLE"), null);
		  filters.put(Resources.get("MARTUSERS"), null);
		  filters.put(Resources.get("SOURCESCHEMA"), null);
		  filters.put(Resources.get("PARTITIONTABLE"), null);
		  filters.put(Resources.get("DSTABLE"), null);
		  filters.put(Resources.get("IMPORTABLE"), null);
		  filters.put(Resources.get("EXPORTABLE"), null);
		  filters.put(Resources.get("SubclassRelation"), null);
		  HashMap<String,String> pMap = new HashMap<String,String>();
		  String userName = McGuiUtils.INSTANCE.getCurrentUser().getUserName();
		  pMap.put(Resources.get("USER"), userName);
		  filters.put(Resources.get("LOCATION"), pMap);
		  return filters;
	 }
	  
	private Map<String, HashMap<String, String>> createLinkFilter() {
		Map<String, HashMap<String, String>>filters = new HashMap<String, HashMap<String, String>>();
		filters.put(Resources.get("SYNUSERTABLE"), null);
		filters.put(Resources.get("MARTUSERS"), null);
		filters.put(Resources.get("SOURCESCHEMA"), null);
		filters.put(Resources.get("PARTITIONTABLE"), null);
		filters.put(Resources.get("DSTABLE"), null);
		filters.put(Resources.get("SubclassRelation"), null);
			  
		String userName = McGuiUtils.INSTANCE.getCurrentUser().getUserName();
		String guiName = McGuiUtils.INSTANCE.getGuiType().toString();
		filters.put(Resources.get("CONTAINER"), null);
				
		HashMap<String, String> pMap = new HashMap<String, String>();
		pMap.put(Resources.get("GUI"), guiName);
		pMap.put(Resources.get("USER"), userName);
		filters.put(Resources.get("IMPORTABLE"), pMap);
		filters.put(Resources.get("EXPORTABLE"),pMap);
		filters.put(Resources.get("LOCATION"),pMap);		
		return filters;
	  }
	  
	  private Map<String, HashMap<String, String>> createSourceFilter() {
		  Map<String, HashMap<String, String>>filters = new HashMap<String, HashMap<String, String>>();
		  filters.put(Resources.get("SYNUSERTABLE"), null);
		  filters.put(Resources.get("MARTUSERS"), null);
		  filters.put(Resources.get("PARTITIONTABLE"), null);
		  filters.put(Resources.get("CONTAINER"), null);
		  filters.put(Resources.get("DSTABLE"),null);
		  filters.put(Resources.get("IMPORTABLE"), null);
		  filters.put(Resources.get("EXPORTABLE"), null);
		  filters.put(Resources.get("RELATION"), null);
		  filters.put(Resources.get("SubclassRelation"), null);
		  HashMap<String,String> pMap = new HashMap<String,String>();
		  String userName = McGuiUtils.INSTANCE.getCurrentUser().getUserName();
		  pMap.put(Resources.get("USER"), userName);
		  filters.put(Resources.get("LOCATION"), pMap);
		  return filters;
	  }
	  
	  private Map<String, HashMap<String, String>> createTargetFilter() {
		  Map<String, HashMap<String, String>>filters = new HashMap<String, HashMap<String, String>>();
		  filters.put(Resources.get("SYNUSERTABLE"), null);
		  filters.put(Resources.get("MARTUSERS"), null);
		  filters.put(Resources.get("SOURCESCHEMA"), null);
		  filters.put(Resources.get("CONTAINER"), null);
		  filters.put(Resources.get("IMPORTABLE"), null);
		  filters.put(Resources.get("EXPORTABLE"), null);
		  filters.put(Resources.get("PARTITIONTABLE"),null);
		  filters.put(Resources.get("SubclassRelation"), null);
		  HashMap<String,String> pMap = new HashMap<String,String>();
		  String userName = McGuiUtils.INSTANCE.getCurrentUser().getUserName();
		  pMap.put(Resources.get("USER"), userName);
		  filters.put(Resources.get("LOCATION"), pMap);
		  return filters;
	  }
	  
	  private Map<String, HashMap<String, String>> createMaterializeFilter() {
		  Map<String, HashMap<String, String>>filters = new HashMap<String, HashMap<String, String>>();
		  filters.put(Resources.get("SYNUSERTABLE"), null);
		  filters.put(Resources.get("MARTUSERS"), null);
		  filters.put(Resources.get("SOURCESCHEMA"), null);
		  filters.put(Resources.get("CONTAINER"), null);
		  filters.put(Resources.get("PARTITIONTABLE"),null);
		  filters.put(Resources.get("IMPORTABLE"), null);
		  filters.put(Resources.get("EXPORTABLE"), null);
		  filters.put(Resources.get("SubclassRelation"), null);
		  HashMap<String,String> pMap = new HashMap<String,String>();
		  String userName = McGuiUtils.INSTANCE.getCurrentUser().getUserName();
		  pMap.put(Resources.get("USER"), userName);
		  filters.put(Resources.get("LOCATION"), pMap);
		  return filters;
	  }
	  
	  private Map<String, HashMap<String, String>> createUpdateFilter() {
		  Map<String, HashMap<String, String>>filters = new HashMap<String, HashMap<String, String>>();
		  filters.put(Resources.get("SYNUSERTABLE"), null);
		  filters.put(Resources.get("MARTUSERS"), null);
		  filters.put(Resources.get("SOURCESCHEMA"), null);
		  filters.put(Resources.get("CONTAINER"), null);
		  filters.put(Resources.get("PARTITIONTABLE"),null);
		  filters.put(Resources.get("IMPORTABLE"), null);
		  filters.put(Resources.get("EXPORTABLE"), null);
		  filters.put(Resources.get("SubclassRelation"), null);
		  HashMap<String,String> pMap = new HashMap<String,String>();
		  String userName = McGuiUtils.INSTANCE.getCurrentUser().getUserName();
		  pMap.put(Resources.get("USER"), userName);
		  filters.put(Resources.get("LOCATION"), pMap);
		  return filters;
	  }

	  
	  private Map<String, HashMap<String, String>> createPartitionFilter() {
		  Map<String, HashMap<String, String>>filters = new HashMap<String, HashMap<String, String>>();
		  filters.put(Resources.get("SYNUSERTABLE"), null);
		  filters.put(Resources.get("MARTUSERS"), null);
		  filters.put(Resources.get("SOURCESCHEMA"), null);
		  filters.put(Resources.get("CONTAINER"), null);
		  filters.put(Resources.get("IMPORTABLE"), null);
		  filters.put(Resources.get("EXPORTABLE"), null);
		  filters.put(Resources.get("PARTITIONTABLE"),null);
		  filters.put(Resources.get("SubclassRelation"), null);
		  HashMap<String,String> pMap = new HashMap<String,String>();
		  String userName = McGuiUtils.INSTANCE.getCurrentUser().getUserName();
		  pMap.put(Resources.get("USER"), userName);
		  filters.put(Resources.get("LOCATION"), pMap);
		  return filters;
	  }
	  
	  /**
	   * apply filter and highlight the last selected node, if not the same node available, highlight row(0);
	   * @param filter
	   */
	  private void setFilter(McFilter filter) {
		  MartConfigTree tree = ((McViewTree)McViews.getInstance().getView(IdwViewType.MCTREE)).getMcTree();
		  TreePath oldPath = tree.getSelectionPath();
		  JDomNodeAdapter lastNode = null;
		  if(oldPath!=null) {
			 lastNode = (JDomNodeAdapter)oldPath.getLastPathComponent();
		  }
		  //go back to dataset or root
		  JDomNodeAdapter dsNode = null;
		  if(lastNode!=null) {
			  Element dsElement = lastNode.findAncestorElement(lastNode.getNode(), Resources.get("DATASET"));
			  if(dsElement!=null)
				  dsNode = new JDomNodeAdapter(dsElement);
			  else
				  dsNode = (JDomNodeAdapter)tree.getModel().getRoot();
		  }
		  tree.getModel().setFilter(filter);
		  int row = tree.expandNodeToDataSets(Resources.get("DATASET"),dsNode);
		  tree.setSelectionRow(row);
	  }
	  	
}