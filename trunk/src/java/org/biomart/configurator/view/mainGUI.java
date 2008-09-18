package org.biomart.configurator.view;

import java.awt.*;
import java.awt.event.*;
import java.awt.event.ActionListener;

import javax.swing.*;
import javax.swing.event.*;
import java.util.*;
import java.io.IOException;
import javax.imageio.ImageIO;


import org.biomart.configurator.model.Initializer;
import org.biomart.configurator.view.ToolBarMenu;
import org.biomart.configurator.view.SplitPanel;



public class mainGUI extends JPanel implements Observer {


	//... Constants
    private static final String INITIAL_VALUE = "1";
    
    //... Components
    private JFrame m_frame = new JFrame("Mart Configurator - Science is all sorted");
    
    private JTextField m_userInputTf = new JTextField(5);
    private JTextField m_totalTf     = new JTextField(20);
    private JButton    m_multiplyBtn = new JButton("Multiply");
    private JButton    m_clearBtn    = new JButton("Clear");
    
    private Initializer modelObj;
    private JMenuBar menuBar;
    private JMenu menu;
    private JMenuItem menuItemNew, menuItemOpen, menuItemExport, menuItemSaveAll, menuItemUploadAll, menuItemQuit;
    private JSplitPane splitPaneObjTop;
    private JSplitPane splitPaneObjBottom; 
    
    //======================================================= constructor
    /** Constructor */
    public mainGUI(Initializer model) {
        //... Set up the logic
        modelObj = model;
                
        //2. Optional: What happens when the frame closes?
        m_frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        //3. Create components and put them in the frame.
        //...create emptyLabel...
       
        // adding menuBar
        
      //Create the menu bar.
        menuBar = new JMenuBar();

        //Build the first menu.
        menu = new JMenu("File");
        menu.setMnemonic(KeyEvent.VK_F);
        menuBar.add(menu);

        //a group of JMenuItems
        menuItemNew = new JMenuItem("New", KeyEvent.VK_N);
        menuItemOpen = new JMenuItem("Open",KeyEvent.VK_O);
        menuItemExport = new JMenuItem("Export",KeyEvent.VK_E);
        menuItemSaveAll = new JMenuItem("Save All to File System",KeyEvent.VK_S);
        menuItemUploadAll = new JMenuItem("Uplaod All from File System",KeyEvent.VK_U);
        menuItemQuit = new JMenuItem("Quit",KeyEvent.VK_Q);
        menu.add(menuItemNew);
        menu.add(menuItemOpen);
        menu.addSeparator();
        menu.add(menuItemExport);
        menu.addSeparator();
        menu.add(menuItemSaveAll);
        menu.add(menuItemUploadAll);
        menu.addSeparator();
        menu.add(menuItemQuit);
        
        m_frame.setJMenuBar(menuBar);
        
        // adding split panel
        
        splitPaneObjTop = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, this.m_multiplyBtn, this.m_clearBtn);
        splitPaneObjTop.setResizeWeight(0.5);
        splitPaneObjTop.setOneTouchExpandable(true);
        splitPaneObjTop.setContinuousLayout(true);
        splitPaneObjTop.setBorder(null);
        
        JLabel label = new JLabel("hello");
        splitPaneObjBottom = new JSplitPane(JSplitPane.VERTICAL_SPLIT, splitPaneObjTop, label);
        splitPaneObjBottom.setResizeWeight(0.8);
        splitPaneObjBottom.setOneTouchExpandable(true);
        splitPaneObjBottom.setContinuousLayout(true);
        
        splitPaneObjTop.setMinimumSize(new Dimension(100, 50));

        
        //splitPanelObj.addTreeView();
        //splitPanelObj.addPropertEditor();
        //splitPanelObj.addPropertEditor();
        m_frame.getContentPane().add(splitPaneObjBottom);

        // Size the frame.
        m_frame.setSize(800,600);
        m_frame.setIconImage(new ImageIcon("/homes/syed/Desktop/martj/src/java/org/biomart/configurator/view/biomarticon.gif").getImage());
        // Show it.
        m_frame.setVisible(true);
        
        /*JPanel content = new JPanel();
        content.setLayout(new FlowLayout());
        content.add(new JLabel("Input"));
        content.add(m_userInputTf);
        content.add(m_multiplyBtn);
        content.add(new JLabel("Total"));
        content.add(m_totalTf);
        content.add(m_clearBtn);
        
        //... finalize layout
        this.setContentPane(content);
        this.pack();
        
        this.setTitle("Simple Calc - MVC");
        // The window closing event should probably be passed to the 
        // Controller in a real program, but this is a short example.
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        */
    }
    
    public void update(Observable observable, Object arg) {
    	
    }
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}
	
	public void addListenerNew (ActionListener className) {
		this.menuItemNew.addActionListener(className);
	}
	public void addListenerOpen (ActionListener className) {
		this.menuItemOpen.addActionListener(className);
	}
	public void addListenerExport (ActionListener className) {
		this.menuItemExport.addActionListener(className);
	}
	public void addListenerSaveAll (ActionListener className) {
		this.menuItemSaveAll.addActionListener(className);
	}
	public void addListenerUploadAll (ActionListener className) {
		this.menuItemUploadAll.addActionListener(className);
	}
	public void addListenerQuit (ActionListener className) {
		this.menuItemQuit.addActionListener(className);
	}
	

}
