package org.biomart.configurator.view;



import javax.swing.JScrollPane;

public class McTreeScrollPane extends JScrollPane {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private MartConfigTree tree;
	
	public McTreeScrollPane(MartConfigTree tree) {
		super(tree);
		this.tree = tree;
	}
	
	public MartConfigTree getTree() {
		return this.tree;
	}
}