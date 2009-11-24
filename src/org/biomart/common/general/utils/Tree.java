package org.biomart.common.general.utils;

import java.util.ArrayList;
import java.util.List;

public class Tree {

	private String node = null;
	private List<Tree> children = null;
	
	public Tree(String node) {
		super();
		this.node = node;
	}
	
	public void addChildren(Tree tree) {
		if (this.children==null) {
			this.children = new ArrayList<Tree>();
		}
		this.children.add(tree);
	}

	public String getNode() {
		return node;
	}

	public List<Tree> getChildren() {
		return children;
	}
}
