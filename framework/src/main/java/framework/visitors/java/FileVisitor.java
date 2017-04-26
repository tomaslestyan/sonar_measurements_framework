/**
 * The MIT License (MIT)
 * Copyright (c) 2016 FI MUNI
 */
package main.java.framework.visitors.java;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.lang.reflect.FieldUtils;
import org.sonar.api.batch.Phase;
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

import main.java.framework.api.ICommonVisitor;
import main.java.framework.api.Language;
import main.java.framework.api.Scope;
import main.java.framework.api.metrics.MetricsRegister;
import main.java.framework.db.SonarDbClient;

/**
 * Class for visiting Java files. The purpose of the class is to visit each class and method and store information about this components including measures of available metrics.
 * @author Tomas
 */
@Phase(name = Phase.Name.PRE)
@Rule(key = "framework", name="framework", description="blank rule")
public class FileVisitor extends BaseTreeVisitor implements JavaFileScanner {

	/** The scanner context */
	private JavaFileScannerContext context;
	private String project;

	/* (non-Javadoc)
	 * @see org.sonar.plugins.java.api.JavaFileScanner#scanFile(org.sonar.plugins.java.api.JavaFileScannerContext)
	 */
	@Override
	public void scanFile(JavaFileScannerContext context) {
		this.context = context;
		CompilationUnitTree tree = context.getTree();
		project = getProjectKey();
		scan(tree);
	}

	/**
	 * @param context
	 * @return 
	 */
	private String getProjectKey() {
		try {
			Object sonarComponentsField = FieldUtils.readField(context, "sonarComponents", true);
			if (sonarComponentsField != null) {
				Object fs = FieldUtils.readField(sonarComponentsField, "fs", true);
				if (fs != null) {
					Object moduleKey = FieldUtils.readField(fs, "moduleKey", true);
					return moduleKey.toString();
				}
			}
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		}
		return null;
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
		client.saveComponent(componentID, context.getFileKey(), project, getParentID(tree), 
				Scope.CLASS.getValue(), getPackageName(), getClassName(superClass), superInterfaces.stream().map(x -> getClassName(x)).collect(Collectors.toList()), line, endLine);
		saveMetrics(tree, componentID, Scope.CLASS);
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
		saveMetrics(tree, componentID, Scope.METHOD);
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
	 * @param tree
	 * @param componentID
	 */
	@SuppressWarnings("unchecked")
	private void saveMetrics(Tree tree, String componentID, Scope scope) {
		SonarDbClient client = new SonarDbClient(true);
		MetricsRegister.getFrameworkMetrics().forEach(x -> {
			ICommonVisitor javaVisitor = MetricsRegister.getMetricVisitorForLanguage(x, Language.JAVA);
			boolean isInScope = javaVisitor.getScope() == Scope.ALL || javaVisitor.getScope() == scope;
			if ((javaVisitor != null) && javaVisitor instanceof AVisitor && isInScope) {
				AVisitor visitor = (AVisitor) javaVisitor;
				visitor.scanTree(tree);
				client.saveMeasure(x, componentID, visitor.getResult());

			};
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

	/**
	 * @param tree
	 * @return
	 */
	private String getClassName(TypeTree tree) {
		if ((tree != null) && tree.is(Kind.IDENTIFIER)) {
			return tree.firstToken().text();
		}
		return null;
	}
}
