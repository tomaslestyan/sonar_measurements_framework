package main.java.framework.visitors.java;

import java.util.HashSet;
import java.util.Set;

import org.sonar.plugins.java.api.tree.ClassTree;
import org.sonar.plugins.java.api.tree.MemberSelectExpressionTree;
import org.sonar.plugins.java.api.tree.MethodInvocationTree;
import org.sonar.plugins.java.api.tree.Tree;

import com.google.common.base.Objects;

import main.java.framework.api.Scope;

/**
 * Data access visitor
 * @author Tomas
 */
public class DataAccessVisitor extends AVisitor {


	private boolean foreignAccessVisitor;
	private Set<String> providers;
	private String className;

	/**
	 * Constructor
	 * @param foreignAccessVisitor
	 */
	public DataAccessVisitor(boolean foreignAccessVisitor) {
		providers = new HashSet<>();
		this.foreignAccessVisitor = foreignAccessVisitor;
	}

	/* (non-Javadoc)
	 * @see main.java.framework.visitors.java.AVisitor#scanTree(org.sonar.plugins.java.api.tree.Tree)
	 */
	@Override
	public void scanTree(Tree tree) {
		processParentClass(tree);
		super.scanTree(tree);
	}

	/**
	 * @param tree
	 */
	private void processParentClass(Tree tree) {
		Tree parent = tree.parent();
		if (parent instanceof ClassTree) {
			ClassTree classTree = (ClassTree) parent;
			this.className = classTree.symbol().name();
		}
	}

	@Override
	public String getKey() {
		return "undefined";
	}

	/* (non-Javadoc)
	 * @see main.java.framework.api.ICommonVisitor#getScope()
	 */
	@Override
	public Scope getScope() {
		return Scope.METHOD;
	}

	/* (non-Javadoc)
	 * @see org.sonar.plugins.java.api.tree.BaseTreeVisitor#visitMethodInvocation(org.sonar.plugins.java.api.tree.MethodInvocationTree)
	 */
	@Override
	public void visitMethodInvocation(MethodInvocationTree tree) {
		//format: $expression.$identifier
		String identifier = tree.methodSelect().lastToken().text();
		String ownerClass = tree.symbol().owner().name();
		if (identifier.startsWith("get") && (isForeignCall(ownerClass, className) == foreignAccessVisitor)) {
			count++;
			providers.add(ownerClass);
		}
	}

	@Override
	public void visitMemberSelectExpression(MemberSelectExpressionTree tree) {
		//format: $expression.$identifier
		boolean variableSymbol = tree.identifier().symbol().isVariableSymbol();
		String ownerClass = tree.expression().symbolType().symbol().name();
		if (variableSymbol && isForeignCall(ownerClass, className) == foreignAccessVisitor) {
			count++;
			providers.add(ownerClass); 
		}
		super.visitMemberSelectExpression(tree);
	}

	private boolean isForeignCall(String ownerClass, String currentClass) {
		// called from owner class in case of syntax
		// 1. this."method"
		// 2. "method"
		return !(Objects.equal(ownerClass, currentClass) || "this".equals(ownerClass));
	}
	/**
	 * @return the providers
	 */
	public Set<String> getProviders() {
		return providers;
	}

}
