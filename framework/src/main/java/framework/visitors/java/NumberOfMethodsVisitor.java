/**
 * The MIT License (MIT)
 * Copyright (c) 2016 Tomas Lestyan
 */
package main.java.framework.visitors.java;

import org.sonar.plugins.java.api.tree.MethodTree;

import main.java.framework.api.Scope;

/**
 * TODO
 * @author Tomas Lestyan
 */
public class NumberOfMethodsVisitor extends AVisitor {

	public final static String KEY = "nom";


	/* (non-Javadoc)
	 * @see main.java.visitors.ADisharmonyVisitor#getKey()
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
	 * @see org.sonar.plugins.java.api.tree.BaseTreeVisitor#visitMethod(org.sonar.plugins.java.api.tree.MethodTree)
	 */
	@Override
	public void visitMethod(MethodTree tree) {
		count++;
	}

}
