/**
 * The MIT License (MIT)
 * Copyright (c) 2016 FI MUNI
 */
package main.java.visitors;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.sonar.check.Rule;
import org.sonar.plugins.java.api.JavaFileScanner;
import org.sonar.plugins.java.api.JavaFileScannerContext;
import org.sonar.plugins.java.api.tree.BaseTreeVisitor;
import org.sonar.plugins.java.api.tree.ClassTree;
import org.sonar.plugins.java.api.tree.CompilationUnitTree;
import org.sonar.plugins.java.api.tree.ListTree;
import org.sonar.plugins.java.api.tree.MethodTree;
import org.sonar.plugins.java.api.tree.Tree;
import org.sonar.plugins.java.api.tree.Tree.Kind;
import org.sonar.plugins.java.api.tree.TypeTree;

import main.java.db.SonarDbClient;
import main.java.metrics.MetricsRegister;

/**
 * Class for visiting Java files. The purpose of the class is to visit each class and method and store information about this components including measures of available metrics.
 * @author Tomas
 */
@Rule(key = "framework", name="framework", description="blank rule")
public class FileVisitor extends BaseTreeVisitor implements JavaFileScanner{

	/** The scanner context */
	private JavaFileScannerContext context;

	/* (non-Javadoc)
	 * @see org.sonar.plugins.java.api.JavaFileScanner#scanFile(org.sonar.plugins.java.api.JavaFileScannerContext)
	 */
	@Override
	public void scanFile(JavaFileScannerContext context) {
		this.context = context;
		CompilationUnitTree tree = context.getTree();
		scan(tree);
	}

	/* (non-Javadoc)
	 * @see org.sonar.plugins.java.api.tree.BaseTreeVisitor#visitClass(org.sonar.plugins.java.api.tree.ClassTree)
	 */
	@Override
	public void visitClass(ClassTree tree) {
		List<String> imports = getImports();
		int line = tree.declarationKeyword().line();
		int endLine = tree.lastToken().line();
		SonarDbClient client = new SonarDbClient(true);
		String componentID = context.getFileKey() + "->" + tree.simpleName().name();
		TypeTree superClass = tree.superClass();
		ListTree<TypeTree> superInterfaces = tree.superInterfaces();
		client.saveComponent(componentID, context.getFileKey(), getParentID(tree), Scope.CLASS.getValue(), 
				getPackageName(), getClassName(superClass), superInterfaces.stream().map(x -> getClassName(x)).collect(Collectors.toList()), line, endLine);
		saveClassMetrics(tree, componentID);
		client.disconnect();
		super.visitClass(tree);
	}

	/* (non-Javadoc)
	 * @see org.sonar.plugins.java.api.tree.BaseTreeVisitor#visitMethod(org.sonar.plugins.java.api.tree.MethodTree)
	 */
	@Override
	public void visitMethod(MethodTree tree) {
		SonarDbClient client = new SonarDbClient(true);
		String componentID = context.getFileKey() + "->" + tree.simpleName().name();
		getParentID(tree);
		client.saveComponent(componentID, context.getFileKey(), getParentID(tree), Scope.METHOD.getValue(), 
				getPackageName(), null, Collections.emptyList(), tree.firstToken().line(), tree.lastToken().line());
		client.disconnect();
		saveMethodMetrics(tree, componentID);
		super.visitMethod(tree);
	}

	/**
	 * Get the parent class of the tree, if parent is something else than return <code>null</code>
	 * @param tree
	 * @return parent class or <code>null</code> if parent is something else
	 */
	private String getParentID(Tree tree) {
		Tree parent = tree.parent();
		if (parent.is(Kind.CLASS)) {
			return context.getFileKey() + "->" + ((ClassTree) parent).simpleName();
		}
		return null;
	}

	/**
	 * Save metrics for methods
	 * @param tree
	 * @param componentID
	 */
	private void saveMethodMetrics(MethodTree tree, String componentID) {
		SonarDbClient client = new SonarDbClient(true);
		MetricsRegister.getMetricVisitors().entrySet().stream().filter(x -> x.getValue().getScope() == Scope.ALL || x.getValue().getScope() == Scope.METHOD).forEach(y -> {
			AVisitor visitor = y.getValue();
			visitor.scanMethod(tree);
			client.saveMeasure(y.getKey(), componentID, visitor.getResult());

		});
		client.disconnect();
	}

	/**
	 * Save metrics for classes
	 * @param tree
	 * @param componentID
	 */
	private void saveClassMetrics(ClassTree tree, String componentID) {
		SonarDbClient client = new SonarDbClient(true);
		MetricsRegister.getMetricVisitors().entrySet().stream().filter(x -> x.getValue().getScope() == Scope.ALL || x.getValue().getScope() == Scope.CLASS).forEach(y -> {
			AVisitor visitor = y.getValue();
			visitor.scanClass(tree);
			client.saveMeasure(y.getKey(), componentID, visitor.getResult());

		});
		client.disconnect();
	}

	/**
	 * @return
	 */
	private List<String> getImports() {
		return context.getTree().imports().stream().map(x -> x.firstToken().text()).collect(Collectors.toList());
	}

	/**
	 * @return
	 */
	private String getPackageName() {
		return context.getTree().packageDeclaration().packageName().firstToken().text();
	}

	private String getClassName(TypeTree tree) {
		if ((tree != null) && tree.is(Kind.IDENTIFIER)) {
			return tree.firstToken().text();
		}
		return null;
	}
}
