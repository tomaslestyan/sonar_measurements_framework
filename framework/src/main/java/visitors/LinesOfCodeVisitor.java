/**
 * The MIT License (MIT)
 * Copyright (c) 2016 Tomas Lestyan
 */
package main.java.visitors;

import org.sonar.plugins.java.api.tree.ClassTree;
import org.sonar.plugins.java.api.tree.MethodTree;
import org.sonar.plugins.java.api.tree.Tree;

/**
 * TODO
 *
 * @author Tomas Lestyan
 */
public class LinesOfCodeVisitor extends AVisitor {

	public static final String KEY =  "loc";
	private int lines = 0;

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
		return Scope.ALL;
	}

	/* (non-Javadoc)
	 * @see main.java.visitors.ADisharmonyVisitor#getResult()
	 */
	@Override
	public int getResult() {
		return lines;
	}

	/* (non-Javadoc)
	 * @see main.java.visitors.ADisharmonyVisitor#scanMethod(org.sonar.plugins.java.api.tree.MethodTree)
	 */
	@Override
	public void scanMethod(MethodTree tree) {
		countLines(tree);
	}


	/* (non-Javadoc)
	 * @see main.java.visitors.ADisharmonyVisitor#scanClass(org.sonar.plugins.java.api.tree.ClassTree)
	 */
	@Override
	public void scanClass(ClassTree tree) {
		countLines(tree);
	}

	/**
	 * Count lines in given tree
	 * @param tree
	 */
	private void countLines(Tree tree) {
		lines = 0;
		int start = tree.firstToken().line();
		int end = tree.lastToken().line();
		lines = end - start + 1;
	}
}
