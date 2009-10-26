package org.biomart.configurator.view;

import java.awt.Component;
import java.util.Observable;
import java.util.Observer;
import javax.swing.Icon;
import org.biomart.configurator.controller.McController;
import org.biomart.configurator.model.McModel;
import org.biomart.configurator.utils.type.IdwViewType;
import net.infonode.docking.View;

public abstract class McView extends View implements Observer {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private McModel model;
	private McController controller;
	private IdwViewType type;
	
	public McView(String title, Icon icon, Component component, McModel model, IdwViewType type) {
		super(title, icon, component);
		this.model = model;
		this.type = type;
//		this.model.addObserver(this);
		this.makeController();
	}

	public IdwViewType getType() {
		return this.type;
	}
	
	private void makeController() {
		controller = new McController(this,this.model);
	}
	
	public abstract void update(Observable observable, Object obj);
	/*{
		if(this.type.equals(ViewType.MCTREE)) {
			this.updateMCTree(observable, (McEventObject)obj);
		}
		else if(this.type.equals(ViewType.DATASET)) {
			if(((McEventObject)obj).getEventType().equals(EventType.DATASETUPDATE)) {
	           // DockingUtil.addWindow(this, this.getRootWindow());
	          //  this.restore();
	           // this.restoreFocus();
			}else if(((McEventObject)obj).getEventType().equals(EventType.DIMENSIONPARTITION)) {
	            DockingUtil.addWindow(this, this.getRootWindow());
	            this.restore();
	            McView view = McViews.getInstance().getView(ViewType.MCTREE);
	            DockingUtil.addWindow(view, this.getRootWindow());
	            view.restore();
	            view.restoreFocus();
			}
		}
		else if(this.type.equals(ViewType.MCRUNNER)) {
			if(((McEventObject)obj).getEventType().equals(EventType.REQUESTMARTRUNNER)) {
				this.restore();
				this.restoreFocus();
			}
		}else if(this.type.equals(ViewType.SCHEMA)) {
			if(((McEventObject)obj).getEventType().equals(EventType.REQUESTNEWMARTSTART)) {
				//this.restore();
				//this.restoreFocus();
				MartTabSet mts = (MartTabSet)this.getComponent();
				//s is "location;martname"
				String s = ((McEventObject)obj).getObject().toString();
				String[] s1 = s.split(";");
				if(mts.requestNewMart(s1[0],s1[1])) {
					McEventObject endObject = new McEventObject(EventType.REQUESTNEWMARTEND,mts.getSelectedMartTab().getMart());
					endObject.SetContextString(s);
					this.getController().processV2Cupdate(endObject);
				}									
			} 
			
		}
		
	}*/
	
	public McController getController() {
		return this.controller;
	}
	
	
}