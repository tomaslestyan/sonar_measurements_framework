/**
 *
 */
package main.java.visitors;

import org.sonar.plugins.java.api.tree.MethodTree;
import org.sonar.plugins.java.api.tree.VariableTree;

/**
 * @author Tomas Lestyan
 */
public class VariableVisitor extends AVisitor {

	public static final String KEY = "noav";
	private int count;

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
		count = 0;
		super.scan(tree);
	}

	/* (non-Javadoc)
	 * @see org.sonar.plugins.java.api.tree.BaseTreeVisitor#visitVariable(org.sonar.plugins.java.api.tree.VariableTree)
	 */
	@Override
	public void visitVariable(VariableTree tree) {
		count++;
		super.visitVariable(tree);
	}

	/* (non-Javadoc)
	 * @see main.java.visitors.ADisharmonyVisitor#getReult()
	 */
	@Override
	public int getResult() {
		return count;
	}

}
