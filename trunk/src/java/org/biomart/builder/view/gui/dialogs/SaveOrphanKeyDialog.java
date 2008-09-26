package org.biomart.builder.view.gui.dialogs;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JToolBar;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingUtilities;
import javax.swing.filechooser.FileFilter;

import org.biomart.common.resources.Resources;
import org.biomart.common.resources.Settings;

public class SaveOrphanKeyDialog extends JDialog {
	
	private static boolean isSave = false;

	private SaveOrphanKeyDialog(final String title, final String text) {
		// Create the content pane for the dialog.
		final JPanel content = new JPanel(new BorderLayout());
		this.setContentPane(content);
		this.setModal(true);
		
		this.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);

		
		this.addWindowListener(new WindowAdapter() {
		    public void windowClosing(WindowEvent we) {
		    	 if (!SaveOrphanKeyDialog.this.isSave){
					 Frame frame = new Frame();
					 JOptionPane.showMessageDialog(frame,
							    "You must save the text before closing the window!",
							    "WARNING",
							    JOptionPane.WARNING_MESSAGE);
					
				 }
		    	 else{
		    		 Window parent = SwingUtilities.getWindowAncestor((Component) we.getSource());
                     // Close the popup window
                     parent.dispose(); 
		    	 }
		    }
		});


		// Build the text editor pane.
		final JTextArea editorPane = new JTextArea(text);

		// Make it read-only and word-wrapped.
		editorPane.setEditable(false);
		editorPane.setWrapStyleWord(true);
		editorPane.setLineWrap(true);
		
		// Attach a mouse listener to the editor pane that
		// will open the menu on demand.
		editorPane.addMouseListener(new MouseListener() {
			public void mouseReleased(final MouseEvent e) {
				this.handleMouse(e);
			}

			public void mouseClicked(final MouseEvent e) {
				this.handleMouse(e);
			}

			public void mousePressed(final MouseEvent e) {
				this.handleMouse(e);
			}

			public void mouseEntered(final MouseEvent e) {
				this.handleMouse(e);
			}

			public void mouseExited(final MouseEvent e) {
				this.handleMouse(e);
			}

			private void handleMouse(final MouseEvent e) {
				
					e.consume();
							}
		});

		// Put the editor pane in a scroll pane.
		final JScrollPane editorScrollPane = new JScrollPane(editorPane);
		editorScrollPane
				.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
		editorScrollPane.setPreferredSize(new Dimension(600, 400));

		// Build the toolbar.
		final JToolBar toolBarPane = new JToolBar();
		toolBarPane.setFloatable(false);
		toolBarPane.setRollover(true);

		// Create a file chooser for finding the TXT file we will save.
		final JFileChooser saver = new JFileChooser() {
			private static final long serialVersionUID = 1L;

			public File getSelectedFile() {
				File file = super.getSelectedFile();
				if (file != null && !file.exists()) {
					final String filename = file.getName();
					final String extension = Resources.get("txtExtension");
					if (!filename.endsWith(extension)
							&& filename.indexOf('.') < 0)
						file = new File(file.getParentFile(), filename
								+ extension);
				}
				return file;
			}
		};
		final String currentDir = Settings.getProperty("currentSaveDir");
		saver.setCurrentDirectory(currentDir == null ? null : new File(
				currentDir));
		saver.setFileFilter(new FileFilter() {
			// Accepts only files ending in ".txt".
			public boolean accept(final File f) {
				return f.isDirectory()
						|| f.getName().toLowerCase().endsWith(
								Resources.get("txtExtension"));
			}

			public String getDescription() {
				return Resources.get("TXTFileFilterDescription");
			}
		});

		// Make the save button as an image.
		final JButton saverButton = new JButton(new ImageIcon(Resources
				.getResourceAsURL("save.gif")));
		saverButton.addActionListener(new ActionListener() {
			public void actionPerformed(final ActionEvent e) {
				if (saver.showSaveDialog(SaveOrphanKeyDialog.this) == JFileChooser.APPROVE_OPTION) {
					Settings.setProperty("currentSaveDir", saver
							.getCurrentDirectory().getPath());
					final File file = saver.getSelectedFile();
					// When a file is chosen, save the file.
					if (file != null){
						FileWriter fw = null;
						try {
							fw = new FileWriter(file);
							fw.write(editorPane.getText());
							fw.flush();
						
						} 
						catch(Exception exp){
							exp.printStackTrace();
						}
						
						finally {
							if (fw != null)
								try {
									fw.close();
									SaveOrphanKeyDialog.this.isSave = true;
								} catch (final IOException ex) {
									ex.printStackTrace();
									// Ignore this one.
								}
						}
					}
					
					
				}
			}
		});
		
		// Add the save option to the toolbar.
		toolBarPane.add(saverButton);
		
		// Build the toolbar.
		//final JToolBar closeWindowBarPane = new JToolBar();
		final JPanel closeWindowBarPane = new JPanel();
		//closeWindowBarPane.setFloatable(false);
		//closeWindowBarPane.setRollover(true);
		
		// Make a print button.
		final JButton closeWindowButton = new JButton("Close Window");
		closeWindowButton.setLocation(50,50);
		
	
		closeWindowButton.addActionListener(new ActionListener() {
			public void actionPerformed(final ActionEvent e) {
				 if (e.getSource() instanceof Component)
                 {
					 if (!SaveOrphanKeyDialog.this.isSave){
						 Frame frame = new Frame();
						 JOptionPane.showMessageDialog(frame,
								    "You must save the text before closing the window!",
								    "WARNING",
								    JOptionPane.WARNING_MESSAGE);

					 }
					 else {
                     Window parent = SwingUtilities.getWindowAncestor((Component) e.getSource());
                     // Close the popup window
                     parent.dispose();
					 }
                 }
			}
		});
		closeWindowBarPane.add(closeWindowButton);
		
		
		// Construct the content panel.
		content.add(toolBarPane, BorderLayout.PAGE_START);
		content.add(editorScrollPane, BorderLayout.CENTER);
		content.add(closeWindowBarPane, BorderLayout.SOUTH);

		// Set size of window.
		this.pack();

		// Move ourselves.
		this.setLocationRelativeTo(null);
	}
	
	public static void displayText(final String title, final String textBuffer) {
		// Create and show a window frame.
		new SaveOrphanKeyDialog(title, textBuffer).setVisible(true);
	}
}
