/**
 * The MIT License (MIT)
 * Copyright (c) 2016 Tomas Lestyan
 */
package main.java.framework.visitors.java;

import main.java.framework.api.Scope;
import org.sonar.plugins.java.api.tree.ClassTree;
import org.sonar.plugins.java.api.tree.Tree;

/**
 * Visitor for counting number of attributes per class
 * @author Klara Erlebachova
 */
public class NumberOfAttributesVisitor extends AVisitor {

	public final static String KEY = "noa";


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

	@Override
	public void visitClass(ClassTree tree) {
		count = (int) tree.members().stream().filter(x -> x.kind().equals(Tree.Kind.VARIABLE)).count();
		super.visitClass(tree);
	}
}
