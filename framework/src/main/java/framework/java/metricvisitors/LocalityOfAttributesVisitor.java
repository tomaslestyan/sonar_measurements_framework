/**
 * The MIT License (MIT)
 * Copyright (c) 2016 FI MUNI
 */
package main.java.framework.java.metricvisitors;

import org.sonar.plugins.java.api.tree.Tree;

import main.java.framework.api.Scope;

/**
 * Visitor for LAA metric
 * @author Tomas
 */
public class LocalityOfAttributesVisitor extends AVisitor {

	/* (non-Javadoc)
	 * @see main.java.framework.api.ICommonVisitor#getKey()
	 */
	@Override
	public String getKey() {
		return "laa";
	}

	/* (non-Javadoc)
	 * @see main.java.framework.api.ICommonVisitor#getScope()
	 */
	@Override
	public Scope getScope() {
		return Scope.METHOD;
	}

	/* (non-Javadoc)
	 * @see main.java.framework.visitors.java.AVisitor#scanTree(org.sonar.plugins.java.api.tree.Tree)
	 */
	@Override
	public void scanTree(Tree tree) {
		DataAccessVisitor localDataVisitor = new DataAccessVisitor(false);
		DataAccessVisitor foreignDataVisitor = new DataAccessVisitor(true);
		localDataVisitor.scanTree(tree);
		foreignDataVisitor.scanTree(tree); 
		int localAccesses = localDataVisitor.getResult();
		int foreignAccesses = foreignDataVisitor.getResult();
		int accesses = localAccesses + foreignAccesses;
		if (accesses == 0) {
			count  = 0;
		} else {
			count = localAccesses / accesses;
		}
	}

}
