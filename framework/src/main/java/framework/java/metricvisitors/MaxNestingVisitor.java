/**
 *
 */
package main.java.framework.java.metricvisitors;

import org.sonar.plugins.java.api.tree.BlockTree;
import org.sonar.plugins.java.api.tree.MethodTree;
import org.sonar.plugins.java.api.tree.Tree;

import main.java.framework.api.Scope;

/**
 * Max nesting visitor
 * @author Tomas Lestyan
 */
public class MaxNestingVisitor extends AVisitor {

	public static final String KEY = "maxnesting";

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
	 * @see org.sonar.plugins.java.api.tree.BaseTreeVisitor#visitBlock(org.sonar.plugins.java.api.tree.BlockTree)
	 */
	@Override
	public void visitBlock(BlockTree tree) {
		int nesting = 0;
		Tree parent = tree;
		while (!(parent instanceof MethodTree)) {
			nesting++;
			parent = parent.parent();
		}
		if (nesting > count) {
			count = nesting;
		}
		super.visitBlock(tree);
	}

}
