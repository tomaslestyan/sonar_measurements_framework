/**
 * The MIT License (MIT)
 * Copyright (c) 2016 FI MUNI
 */
package main.java.framework.visitors.java;

import org.sonar.plugins.java.api.tree.Tree;

import main.java.framework.api.Scope;

/**
 * Visitor for FDP metric
 * @author Tomas
 */
public class ForeignDataProvidersVisitor extends AVisitor {

	/* (non-Javadoc)
	 * @see main.java.framework.api.ICommonVisitor#getKey()
	 */
	@Override
	public String getKey() {
		return "fdp";
	}

	/* (non-Javadoc)
	 * @see main.java.framework.api.ICommonVisitor#getScope()
	 */
	@Override
	public Scope getScope() {
		return Scope.METHOD;
	}

	@Override
	public void scanTree(Tree tree) {
		DataAccessVisitor visitor = new DataAccessVisitor(true);
		visitor.scanTree(tree);
		count = visitor.getProviders().size();
	}

}
