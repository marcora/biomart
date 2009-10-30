/*
 * Copyright (c) 2004 NNL Technology AB
 * All rights reserved.
 *
 * "Work" shall mean the contents of this file.
 *
 * Redistribution, copying and use of the Work, with or without
 * modification, is permitted without restrictions.
 *
 * Visit www.infonode.net for information about InfoNode(R)
 * products and how to contact NNL Technology AB.
 *
 * THE WORK IS PROVIDED BY THE COPYRIGHT HOLDERS AND
 * CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES,
 * INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
 * MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
 * NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT
 * LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING
 * IN ANY WAY OUT OF THE USE OF THE WORK, EVEN IF ADVISED OF
 * THE POSSIBILITY OF SUCH DAMAGE.
 */

// $Id: DockingWindowsExample.java,v 1.15 2005/02/16 11:28:14 jesper Exp $
package org.biomart.configurator.test;

import net.infonode.docking.DockingWindow;
import net.infonode.docking.DockingWindowAdapter;
import net.infonode.docking.OperationAbortedException;
import net.infonode.docking.RootWindow;
import net.infonode.docking.SplitWindow;
import net.infonode.docking.TabWindow;
import net.infonode.docking.View;
import net.infonode.docking.ViewSerializer;
import net.infonode.docking.mouse.DockingWindowActionMouseButtonListener;
import net.infonode.docking.properties.RootWindowProperties;
import net.infonode.docking.theme.*;
import net.infonode.docking.util.DockingUtil;
import net.infonode.docking.util.MixedViewHandler;
import javax.swing.*;
import java.util.Map;
import org.biomart.common.resources.Log;
import org.biomart.common.resources.Resources;
import org.biomart.common.resources.Settings;
import org.biomart.common.view.gui.LongProcess;
import org.biomart.configurator.model.McModel;
import org.biomart.configurator.utils.McIcon;
import org.biomart.configurator.utils.type.IdwViewType;
import org.biomart.configurator.view.McView;
import org.biomart.configurator.view.idwViews.McViewAttTable;
import org.biomart.configurator.view.idwViews.McViewSchema;
import org.biomart.configurator.view.idwViews.McViewTree;
import org.biomart.configurator.view.idwViews.McViews;
import org.biomart.configurator.view.menu.McMenus;
import java.awt.*;
import java.io.*;
import java.util.HashMap;


/**
 * A small example on how to use InfoNode Docking Windows. This example shows how to handle both static and
 * dynamic views in the same root window.
 *
 * @author $Author: jesper $
 * @version $Revision: 1.15 $
 */
public class MartConfigurator {
  
	private String resourcesLocation = "org/biomart/builder/resources";
	private McModel model;
	
  
	/**
   	* The one and only root window
   	*/
  	private RootWindow rootWindow;

  	/**
   	* Contains the dynamic views that has been added to the root window
   	*/
  	private HashMap dynamicViews = new HashMap();

  	/**
  	 * The currently applied docking windows theme
  	 */
  	private DockingWindowsTheme currentTheme = new ShapedGradientDockingTheme();

  	/**
   	* A dynamically created view containing an id.
   	*/
  	private static class DynamicView extends View {
  		private int id;

	    /**
	     * Constructor.
	     *
	     * @param title     the view title
	     * @param icon      the view icon
	     * @param component the view component
	     * @param id        the view id
	     */
	    DynamicView(String title, Icon icon, Component component, int id) {
	      super(title, icon, component);
	      this.id = id;
	    }

	    /**
	     * Returns the view id.
	     *
	     * @return the view id
	     */
	    public int getId() {
	      return id;
	    }
  	}

  	/**
 	* In this properties object the modified property values for close buttons etc. are stored. This object is cleared
 	*  * when the theme is changed.
	*/
  	private RootWindowProperties properties = new RootWindowProperties();


  	/**
   	* The application frame
   */
  	private JFrame frame = new JFrame(Resources.get("APPLICATIONTITLE"));

	public MartConfigurator() {	
		this.initSettings();
		this.createRootWindow();
		this.setDefaultLayout();
		this.showFrame();
	}

  /**
   	* Creates a view component containing the specified text.
   *
   * @param text the text
   * @return the view component
   */
	private JComponent createViewComponent(boolean def) {
	  if(def)
		  return new JPanel(new GridLayout(1,1));
	  else
		  return new JPanel(new CardLayout());
  }

  /**
   	* Returns a dynamic view with specified id, reusing an existing view if possible.
   *
   * @param id the dynamic view id
   * @return the dynamic view
   */
  	private View getDynamicView(int id) {
    View view = (View) dynamicViews.get(new Integer(id));

    if (view == null)
      view = new DynamicView("Dynamic View " + id, McIcon.VIEW_ICON, createViewComponent(true), id);

    return view;
  }


  /**
   	* Creates the root window and the views.
   */
  	private void createRootWindow() {
    // Create the views
	  model = new McModel();
	  McViews mcViews = McViews.getInstance();

	  mcViews.addView(new McViewSchema("Content", McIcon.VIEW_ICON, createViewComponent(true),model,IdwViewType.SCHEMA));
	  mcViews.addView(new McViewTree("MC Tree", McIcon.VIEW_ICON, createViewComponent(true),model,IdwViewType.MCTREE));
	  mcViews.addView(new McViewAttTable("Attribute Table", McIcon.VIEW_ICON, createViewComponent(true),model,IdwViewType.ATTRIBUTETABLE));
    // The mixed view map makes it easy to mix static and dynamic views inside the same root window
    MixedViewHandler handler = new MixedViewHandler(mcViews.getViewMap(), new ViewSerializer() {
      public void writeView(View view, ObjectOutputStream out) throws IOException {
        out.writeInt(((DynamicView) view).getId());
      }

      public View readView(ObjectInputStream in) throws IOException {
        return getDynamicView(in.readInt());
      }
    });

    rootWindow = DockingUtil.createRootWindow(mcViews.getViewMap(), handler, true);

    // Set gradient theme. The theme properties object is the super object of our properties object, which
    // means our property value settings will override the theme values
    properties.addSuperObject(currentTheme.getRootWindowProperties());

    // Our properties object is the super object of the root window properties object, so all property values of the
    // theme and in our property object will be used by the root window
    rootWindow.getRootWindowProperties().addSuperObject(properties);

    // Enable the bottom window bar
//    rootWindow.getWindowBar(Direction.DOWN).setEnabled(true);

    // Add a listener which shows dialogs when a window is closing or closed.
    rootWindow.addListener(new DockingWindowAdapter() {
      public void windowAdded(DockingWindow addedToWindow, DockingWindow addedWindow) {
        updateViews(addedWindow, true);
      }

      public void windowRemoved(DockingWindow removedFromWindow, DockingWindow removedWindow) {
        updateViews(removedWindow, false);
      }

      public void windowClosing(DockingWindow window) throws OperationAbortedException {
        if (JOptionPane.showConfirmDialog(frame, "Really close window '" + window + "'?") != JOptionPane.YES_OPTION)
          throw new OperationAbortedException("Window close was aborted!");
      }

    });

    // Add a mouse button listener that closes a window when it's clicked with the middle mouse button.
    rootWindow.addTabMouseButtonListener(DockingWindowActionMouseButtonListener.MIDDLE_BUTTON_CLOSE_LISTENER);
  }

  /**
   	* Update view menu items and dynamic view map.
   *
   * @param window the window in which to search for views
   * @param added  if true the window was added
   */
  	private void updateViews(DockingWindow window, boolean added) {
    if (window instanceof View) {
      if (window instanceof DynamicView) {
        if (added)
          dynamicViews.put(new Integer(((DynamicView) window).getId()), window);
        else
          dynamicViews.remove(new Integer(((DynamicView) window).getId()));
      }
   }
    else {
      for (int i = 0; i < window.getChildWindowCount(); i++)
        updateViews(window.getChildWindow(i), added);
    }
  }

  /**
   	* Sets the default window layout.
   */
  	private void setDefaultLayout() {
	  Map<IdwViewType,McView> map = McViews.getInstance().getAllViews();
	  View[] views = (View[])map.values().toArray(new View[map.size()]);

    TabWindow tabWindow = new TabWindow(views);
    
    SplitWindow swSchema = new SplitWindow(false,0.7f, new TabWindow(new View[]{map.get(IdwViewType.SCHEMA)}), tabWindow);
    SplitWindow swMcTree = new SplitWindow(false,0.7f,map.get(IdwViewType.MCTREE),map.get(IdwViewType.ATTRIBUTETABLE));
    

    rootWindow.setWindow(new SplitWindow(true,
                                         0.4f,
                                         swMcTree,
                                         swSchema));
  }

	  /**
	   	* Initializes the frame and shows it.
	   */
  	private void showFrame() {
		McMenus menusGUI = new McMenus();
	    frame.getContentPane().add(menusGUI.createToolBar(), BorderLayout.NORTH);
	    frame.getContentPane().add(rootWindow, BorderLayout.CENTER);
	    frame.setJMenuBar(menusGUI.createMenuBar());
	    frame.setSize(900, 700);
	    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	    //default new portal
	    menusGUI.newPortal();
	    frame.setVisible(true);
	}



	private void initSettings() {
		System.out.println("..." + Settings.getApplication() + " started.");
		Settings.setApplication(Settings.MARTCONFIGURATOR);
		Resources.setResourceLocation(resourcesLocation);

		// Attach ourselves as the main window for hourglass use.
		LongProcess.setMainWindow(this.frame);

		// Load our cache of settings.
		Settings.load();

		// Set the look and feel to the one specified by the user, or the system
		// default if not specified by the user. This may be null.
		Log.info("Loading look-and-feel settings");
		String lookAndFeelClass = Settings.getProperty("lookandfeel");
		try {
			UIManager.setLookAndFeel(lookAndFeelClass);
		} catch (final Exception e) {
			// Ignore, as we'll end up with the system one if this one doesn't
			// work.
			if (lookAndFeelClass != null)
				// only worry if we were actually given one.
				Log.warn("Bad look-and-feel: " + lookAndFeelClass, e);
			// Use system default.
			lookAndFeelClass = UIManager.getSystemLookAndFeelClassName();
			try {
				UIManager.setLookAndFeel(lookAndFeelClass);
			} catch (final Exception e2) {
				// Ignore, as we'll end up with the cross-platform one if there
				// is no system one.
				Log.warn("Bad look-and-feel: " + lookAndFeelClass, e2);
			}
		}
  }
  	
	public static void main(String[] args) throws Exception {
    // Docking windwos should be run in the Swing thread
    SwingUtilities.invokeLater(new Runnable() {
      public void run() {
        new MartConfigurator();
      }
    });
  }

  
}
