package org.biomart.configurator.view.idwViews;


import java.util.HashMap;
import java.util.Map;
import org.biomart.configurator.utils.type.IdwViewType;
import org.biomart.configurator.view.McView;
import org.ewin.common.util.Log;
import net.infonode.docking.util.ViewMap;

/**
 * a singleton class to store all views
 * it is not thread safe
 * FIXME may change later 
 * @author yliang
 *
 */
public class McViews {
	private Map<IdwViewType,McView> mcViews;
	//just to satisfy the example
	private ViewMap viewMap;
	
	private static McViews instance = null;
	
	private McViews() {
		mcViews = new HashMap<IdwViewType, McView>();
		viewMap = new ViewMap();
	}
	
	public static McViews getInstance() {
		if(instance == null)
			instance = new McViews();
		return instance;
	}
	
	public Map<IdwViewType, McView> getAllViews() {
		return instance.mcViews;
	}
	
	public McView getView(IdwViewType type) {
		return instance.mcViews.get(type);
	}
	
	/**
	 * for example
	 * @return
	 */
	public ViewMap getViewMap() {
		return instance.viewMap;
	}

	
	
	public void addView(McView view) {
		if(view instanceof McViewAttTable) {
			instance.mcViews.put(IdwViewType.ATTRIBUTETABLE, view);
			instance.viewMap.addView(IdwViewType.ATTRIBUTETABLE.ordinal(), view);
		}		
		else if(view instanceof McViewSchema) {
			instance.mcViews.put(IdwViewType.SCHEMA, view);
			instance.viewMap.addView(IdwViewType.SCHEMA.ordinal(), view);
		}
		else if(view instanceof McViewTree) {
			instance.mcViews.put(IdwViewType.MCTREE, view);
			instance.viewMap.addView(IdwViewType.MCTREE.ordinal(), view);
		}
		else
			Log.ERROR("error");
	}
}