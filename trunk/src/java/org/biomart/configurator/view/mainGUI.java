package org.biomart.configurator.view;

import java.awt.*;
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
    private ToolBarMenu menuBarObj = new ToolBarMenu();
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
        m_frame.setJMenuBar(menuBarObj.getMenuBar());
       
        // adding split panel
        
        Font font = new Font("Serif", Font.ITALIC, 24);

         
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
	
	public void menuBarListener (ActionListener className) {
		this.m_multiplyBtn.addActionListener(className);
	}
	

}
