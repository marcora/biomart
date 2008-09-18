package org.biomart.configurator.controller;

import javax.swing.*;
import java.awt.event.*;
import org.biomart.configurator.model.Initializer;
import org.biomart.configurator.view.mainGUI;

public class Start {

	//... The Controller needs to interact with both the Model and View.
    private Initializer m_model;
    private mainGUI  m_view;
    
    //========================================================== constructor
    /** Constructor 
     * @param model 
     * @param view */
    public Start(Initializer model, mainGUI view) {
        m_model = model;
        m_view  = view;
        
        m_model.addObserver(m_view);
        
        m_view.addListenerNew(new menuBarNew());
        m_view.addListenerOpen(new menuBarOpen());
        m_view.addListenerExport(new menuBarExport());
        m_view.addListenerSaveAll(new menuBarSaveAll());
        m_view.addListenerUploadAll(new menuBarUploadAll());
        m_view.addListenerQuit(new menuBarQuit());
    }
    
    
    ////////////////////////////////////////// 
    // MenuBar Options ActionListeners
    //////////////////////////////////////////
    class menuBarNew implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            String userInput = "";
            try {
                System.out.println("NEW");
            } catch (NumberFormatException nfex) {

            }
        }
    }
    
    class menuBarOpen implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            String userInput = "";
            try {
            	System.out.println("OPEN");
                
            } catch (NumberFormatException nfex) {

            }
        }
    }
    
    class menuBarExport implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            String userInput = "";
            try {
            	System.out.println("EXPORT");
            } catch (NumberFormatException nfex) {

            }
        }
    }    
        
    class menuBarSaveAll implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            String userInput = "";
            try {
            	System.out.println("SAVEALL");
            } catch (NumberFormatException nfex) {

            }
        }
    }
    
    class menuBarUploadAll implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            String userInput = "";
            try {
            	System.out.println("UPLOADALL");
            } catch (NumberFormatException nfex) {

            }
        }
    }    
        
    class menuBarQuit implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            String userInput = "";
            try {
            	System.out.println("QUIT");       
            } catch (NumberFormatException nfex) {

            }
        }
    }    

    /**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		Initializer model = new Initializer();
        mainGUI view = new mainGUI(model);
        Start controller = new Start(model, view);
        
        //view.setVisible(true);
	}
    

}
