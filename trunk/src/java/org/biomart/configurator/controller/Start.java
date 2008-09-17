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
        
        m_view.menuBarListener(new firstActionListener());
        
    }
    
    
    ////////////////////////////////////////// inner class MultiplyListener
    /** When a mulitplication is requested.
     *  1. Get the user input number from the View.
     *  2. Call the model to mulitply by this number.
     *  3. Get the result from the Model.
     *  4. Tell the View to display the result.
     * If there was an error, tell the View to display it.
     */
    class firstActionListener implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            String userInput = "";
            try {
                
            } catch (NumberFormatException nfex) {

            }
        }
    }//end inner class MultiplyListener
    
    
    //////////////////////////////////////////// inner class ClearListener
    /**  1. Reset model.
     *   2. Reset View.
     */    
    class secondActionListener implements ActionListener {
        public void actionPerformed(ActionEvent e) {
        	String userInput = "";
            try {
                
            } catch (NumberFormatException nfex) {

            }
        }
    }// end inner class ClearListener
    
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
