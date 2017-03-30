/**
 * The MIT License (MIT)
 * Copyright (c) 2016 Tomas Lestyan
 */
package main.java.visitors;

import org.sonar.check.Rule;
import org.sonar.plugins.java.api.JavaFileScanner;
import org.sonar.plugins.java.api.JavaFileScannerContext;
import org.sonar.plugins.java.api.tree.BaseTreeVisitor;
import org.sonar.plugins.java.api.tree.ClassTree;
import org.sonar.plugins.java.api.tree.MethodTree;
import org.sonar.plugins.java.api.tree.Tree;
import org.sonar.plugins.java.api.tree.Tree.Kind;

import main.java.db.SonarDbClient;
import main.java.metrics.MetricsRegister;

/**
 * TODO
 * @author Tomas Lestyan
 */
@Rule(key = "framework", name="framework", description="blank rule")
public class FileVisitor extends BaseTreeVisitor implements JavaFileScanner{


	private JavaFileScannerContext context;


	@Override
	public void scanFile(JavaFileScannerContext context) {
		this.context = context;
		scan(context.getTree());
	}

	/* (non-Javadoc)
	 * @see org.sonar.plugins.java.api.tree.BaseTreeVisitor#visitClass(org.sonar.plugins.java.api.tree.ClassTree)
	 */
	@Override
	public void visitClass(ClassTree tree) {
		int line = tree.declarationKeyword().line();
		SonarDbClient client = new SonarDbClient(true);
		String componentID = context.getFileKey() + "->" + tree.simpleName().name();
		client.saveComponent(componentID, context.getFileKey(), getParentID(tree), VisitorScope.CLASS.getValue(), line, 100);
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
		client.saveComponent(componentID, context.getFileKey(), getParentID(tree), VisitorScope.METHOD.getValue(), tree.openParenToken().line(), tree.closeParenToken().line());
		client.disconnect();
		saveMethodMetrics(tree, componentID);
		super.visitMethod(tree);
	}

	/**
	 * @param tree
	 */
	private String getParentID(Tree tree) {
		Tree parent = tree.parent();
		if (parent.is(Kind.CLASS)) {
			return context.getFileKey() + "->" + ((ClassTree) parent).simpleName();
		}
		return null;
	}

	private void saveMethodMetrics(MethodTree tree, String componentID) {
		SonarDbClient client = new SonarDbClient(true);
		MetricsRegister.getMetricVisitors().entrySet().stream().filter(x -> x.getValue().getScope() == VisitorScope.ALL || x.getValue().getScope() == VisitorScope.METHOD).forEach(y -> {
			AVisitor visitor = y.getValue();
			visitor.scanMethod(tree);
			client.saveMeasure(y.getKey(), componentID, visitor.getResult());

		});
		client.disconnect();
	}

	private void saveClassMetrics(ClassTree tree, String componentID) {
		SonarDbClient client = new SonarDbClient(true);
		MetricsRegister.getMetricVisitors().entrySet().stream().filter(x -> x.getValue().getScope() == VisitorScope.ALL || x.getValue().getScope() == VisitorScope.CLASS).forEach(y -> {
			AVisitor visitor = y.getValue();
			visitor.scanClass(tree);
			client.saveMeasure(y.getKey(), componentID, visitor.getResult());

		});
		client.disconnect();
	}
}
