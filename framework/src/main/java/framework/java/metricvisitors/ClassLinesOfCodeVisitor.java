/**
 * The MIT License (MIT)
 * Copyright (c) 2016 Tomas Lestyan
 */
package main.java.framework.java.metricvisitors;

import org.sonar.plugins.java.api.tree.Tree;

import main.java.framework.api.Scope;

/**
 * Lines of code visitor for classses
 * @author Tomas Lestyan
 */
public class ClassLinesOfCodeVisitor extends AVisitor {

	public static final String KEY =  "loc";

	/* (non-Javadoc)
	 * @see main.java.visitors.ADisharmonyVisitor#getID()
	 */
	@Override
	public String getKey() {
		return KEY;
	}

	/* (non-Javadoc)
	 * @see main.java.visitors.ADisharmonyVisitor#getScope()
	 */
	@Override
	public Scope getScope() {
		return Scope.CLASS;
	}

	/* (non-Javadoc)
	 * @see main.java.visitors.AVisitor#scanTree(org.sonar.plugins.java.api.tree.Tree)
	 */
	@Override
	public void scanTree(Tree tree) {
		countLines(tree);
	}

	/**
	 * Count lines in given tree
	 * @param tree
	 */
	private void countLines(Tree tree) {
		int start = tree.firstToken().line();
		int end = tree.lastToken().line();
		count = end - start + 1;
	}
}
