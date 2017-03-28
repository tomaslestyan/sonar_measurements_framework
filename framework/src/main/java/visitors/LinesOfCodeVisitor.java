/**
 * The MIT License (MIT)
 * Copyright (c) 2016 Tomas Lestyan
 */
package main.java.visitors;

import org.sonar.plugins.java.api.tree.BlockTree;
import org.sonar.plugins.java.api.tree.ClassTree;
import org.sonar.plugins.java.api.tree.MethodTree;

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
	public VisitorScope getScope() {
		return VisitorScope.ALL;
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
		lines = 0;
		scan(tree);
	}

	/* (non-Javadoc)
	 * @see main.java.visitors.ADisharmonyVisitor#scanClass(org.sonar.plugins.java.api.tree.ClassTree)
	 */
	@Override
	public void scanClass(ClassTree tree) {
		// TODO
	}

	/* (non-Javadoc)
	 * @see org.sonar.plugins.java.api.tree.BaseTreeVisitor#visitMethod(org.sonar.plugins.java.api.tree.MethodTree)
	 */
	@Override
	public void visitMethod(MethodTree tree) {
		BlockTree block = tree.block();
		int start = 0;
		int end = 0;
		if (block != null) {
			start = block.openBraceToken().line();
			end = block.closeBraceToken().line();
		}
		lines = end - start + 1;  //FIXME not accurate, find better way
	}

}
