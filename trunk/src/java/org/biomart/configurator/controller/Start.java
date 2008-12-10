package org.biomart.configurator.controller;

import javax.swing.*;
import java.awt.event.*;
import org.biomart.configurator.view.mainGUI;
import org.biomart.configurator.model.LibraryAdaptor;

import javax.swing.filechooser.*;
import java.io.File;

import java.util.logging.Logger;

// the configuration file for the logger is  /jdk1.5.0/jre/lib/logging.properties

public class Start {

	//... The Controller needs to interact with both the Model and View.
    private  LibraryAdaptor m_model;
    private mainGUI  m_view;
    public Logger log = Logger.getLogger(Start.class.getName());
    //========================================================== constructor
    /** Constructor 
     * @param model 
     * @param view */
    public Start() {
    }
    public Start(LibraryAdaptor model, mainGUI view) {
    	
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
            	log.info("NEW");
            } catch (NumberFormatException nfex) {

            }
        }
    }
    
    class menuBarOpen implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            String userInput = "";
            try {
            	log.info("OPEN");
            	
            	final JFileChooser fc = new JFileChooser();
            	
                XMLFileFilter filter = new XMLFileFilter();  
            	fc.setFileFilter(filter);
                
            	// the parent window is of class m_view
            	// TODO: UNCOMMENT THIS so that OPEN dialog would work
            	int returnVal = 1; // = fc.showOpenDialog(m_view);
            	
            	// TODO: remove this hardcoded line when you uncomment the above line
            	m_model.processRegistryFile("C:/Documents and Settings/jzhang/My Documents/BM_0.8/registry_XML.xml");
            	// m_model.processRegistryFile("/work/syed/Desktop/registry_XML.xml");
            	
            	/* comment out this for now
            	//In response to a button click:
            	if (returnVal == JFileChooser.APPROVE_OPTION) {
                    File file = fc.getSelectedFile();
                    log.info("Opening: " + file.getAbsolutePath() + ".");
                    
                    // call Initializer to process the XML file and make an object representation
                    m_model.processRegistryFile(file.getAbsolutePath());
                                        
                    
                } else {
                    log.info("Open command cancelled by user.");
                }
                */
            } catch (NumberFormatException nfex) {
            	log.info("error reading XML file");
            }
        }
        // helper class to filter the contents of OPEN DIALOG to XML files only
        class XMLFileFilter extends javax.swing.filechooser.FileFilter {
            public boolean accept(File f) {
                return f.isDirectory() || f.getName().toLowerCase().endsWith(".xml");
            }
            
            public String getDescription() {
                return ".xml files";
            }
        }
    }
    
    class menuBarExport implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            String userInput = "";
            try {
            	log.info("EXPORT");
            } catch (NumberFormatException nfex) {

            }
        }
    }    
        
    class menuBarSaveAll implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            String userInput = "";
            try {
            	log.info("SAVEALL");
            } catch (NumberFormatException nfex) {

            }
        }
    }
    
    class menuBarUploadAll implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            String userInput = "";
            try {
            	log.info("UPLOADALL");
            } catch (NumberFormatException nfex) {

            }
        }
    }    
        
    class menuBarQuit implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            String userInput = "";
            try {
            	log.info("QUIT");
            	System.exit(0);
            } catch (NumberFormatException nfex) {

            }
        }
    }    

    /**
	 * @param args MAIN
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		LibraryAdaptor model = new LibraryAdaptor();
        mainGUI view = new mainGUI(model);
        Start controller = new Start(model, view);
        
        //view.setVisible(true);
	}	

}
