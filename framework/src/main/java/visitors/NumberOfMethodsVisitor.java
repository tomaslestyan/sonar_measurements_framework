/**
 * The MIT License (MIT)
 * Copyright (c) 2016 Tomas Lestyan
 */
package main.java.visitors;

import org.sonar.plugins.java.api.tree.ClassTree;
import org.sonar.plugins.java.api.tree.MethodTree;

/**
 * TODO
 * @author Tomas Lestyan
 */
public class NumberOfMethodsVisitor extends AVisitor {

	public final static String KEY = "nom";
	private int count;

	/* (non-Javadoc)
	 * @see main.java.visitors.ADisharmonyVisitor#scanClass(org.sonar.plugins.java.api.tree.ClassTree)
	 */
	@Override
	public void scanClass(ClassTree tree) {
		count = 0;
		scan(tree);
	}

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
	 * @see main.java.visitors.ADisharmonyVisitor#getResult()
	 */
	@Override
	public int getResult() {
		return count;
	}

	@Override
	public void visitMethod(MethodTree tree) {
		count++;
	}

}
