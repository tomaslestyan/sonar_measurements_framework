/**
 *
 */
package main.java.visitors;

import org.sonar.plugins.java.api.tree.BlockTree;
import org.sonar.plugins.java.api.tree.MethodTree;
import org.sonar.plugins.java.api.tree.Tree;

/**
 * Max nesting visitor
 * @author Tomas Lestyan
 */
public class MaxNestingVisitor extends AVisitor {

	public static final String KEY = "maxnesting";
	public static final int ID = 3;
	private int maxValue;

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
	 * @see main.java.visitors.ADisharmonyVisitor#scanMethod(org.sonar.plugins.java.api.tree.MethodTree)
	 */
	@Override
	public void scanMethod(MethodTree tree) {
		maxValue = 0;
		super.scan(tree);
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
		if (nesting > maxValue) {
			maxValue = nesting;
		}
		super.visitBlock(tree);
	}

	/* (non-Javadoc)
	 * @see main.java.visitors.ADisharmonyVisitor#getResult()
	 */
	@Override
	public int getResult() {
		return maxValue;
	}

}
