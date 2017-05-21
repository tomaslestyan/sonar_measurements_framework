/**
 * The MIT License (MIT)
 * Copyright (c) 2016 Tomas Lestyan
 */
package main.java.framework.java.metricvisitors;

import org.sonar.plugins.java.api.tree.BaseTreeVisitor;
import org.sonar.plugins.java.api.tree.Tree;

import main.java.framework.api.ICommonVisitor;
import main.java.framework.api.Language;

/**
 * Abstract visitor class
 * @author Tomas Lestyan
 */
public abstract class AVisitor extends BaseTreeVisitor implements ICommonVisitor {

	protected int count;

	/* (non-Javadoc)
	 * @see main.java.framework.api.ICommonVisitor#getLanguage()
	 */
	@Override
	public Language getLanguage() {
		return Language.JAVA;
	}

	/**
	 * Scan the given tree. Reset counter
	 * @param tree
	 */
	public void scanTree(Tree tree) {
		count = 0;
		super.scan(tree);
	}

	/* (non-Javadoc)
	 * @see main.java.framework.api.ICommonVisitor#getResult()
	 */
	@Override
	public int getResult() {
		return count;
	}
}
