/**
 *
 */
package main.java.framework.java.metricvisitors;

import org.sonar.plugins.java.api.tree.VariableTree;

import main.java.framework.api.Scope;

/**
 * @author Tomas Lestyan
 */
public class VariableVisitor extends AVisitor {

	public static final String KEY = "noav";

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
		return Scope.METHOD;
	}

	/* (non-Javadoc)
	 * @see org.sonar.plugins.java.api.tree.BaseTreeVisitor#visitVariable(org.sonar.plugins.java.api.tree.VariableTree)
	 */
	@Override
	public void visitVariable(VariableTree tree) {
		count++;
		super.visitVariable(tree);
	}

}
