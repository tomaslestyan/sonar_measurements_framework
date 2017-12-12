/**
 * The MIT License (MIT)
 * Copyright (c) 2016 FI MUNI
 * Copyright (c) 2017 FI MUNI
 */

package main.java.framework.java.measurement;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonar.api.batch.Phase;
import org.sonar.check.Rule;
import org.sonar.plugins.java.api.JavaFileScanner;
import org.sonar.plugins.java.api.JavaFileScannerContext;
import org.sonar.plugins.java.api.tree.BaseTreeVisitor;
import org.sonar.plugins.java.api.tree.ClassTree;
import org.sonar.plugins.java.api.tree.CompilationUnitTree;
import org.sonar.plugins.java.api.tree.ImportTree;
import org.sonar.plugins.java.api.tree.ListTree;
import org.sonar.plugins.java.api.tree.MethodTree;
import org.sonar.plugins.java.api.tree.NewClassTree;
import org.sonar.plugins.java.api.tree.PackageDeclarationTree;
import org.sonar.plugins.java.api.tree.Tree;
import org.sonar.plugins.java.api.tree.TypeTree;

import main.java.framework.api.ICommonVisitor;
import main.java.framework.api.Language;
import main.java.framework.api.Scope;
import main.java.framework.api.metrics.MetricsRegister;
import main.java.framework.db.DataSourceProvider;
import main.java.framework.db.SaveMetricsClient;
import main.java.framework.java.metricvisitors.AVisitor;

import javax.annotation.ParametersAreNonnullByDefault;

/**
 * Class for visiting Java files.
 * The purpose of the class is to visit each class and method to store information
 * about this components including measures of available metrics.
 *
 * @author Tomas Lestyan
 */
@Phase(name = Phase.Name.PRE)
@Rule(key = "framework",
name = "Measurement Framework Activation Rule",
description = "This rule activates the Measurement Framework.")
public class FileVisitor extends BaseTreeVisitor implements JavaFileScanner {

	/**
	 * The logger object
	 */
	private final Logger log = LoggerFactory.getLogger(this.getClass());
	/**
	 * The scanner context
	 */
	private JavaFileScannerContext context;
	/**
	 * The project of the scanned file
	 */
	private String project;
	private String packageName;
	private List<String> imports;

	/* (non-Javadoc)
	 * @see org.sonar.plugins.java.api.JavaFileScanner#scanFile(org.sonar.plugins.java.api.JavaFileScannerContext)
	 */
	@Override
	@ParametersAreNonnullByDefault
	public void scanFile(JavaFileScannerContext context) {
		this.context = context;
		this.project = getProjectKey();
		this.packageName = StringUtils.EMPTY;
		this.imports = new ArrayList<>();
		CompilationUnitTree tree = context.getTree();
		scan(tree);
	}

	/* (non-Javadoc)
	 * @see org.sonar.plugins.java.api.tree.BaseTreeVisitor#visitImport(org.sonar.plugins.java.api.tree.ImportTree)
	 */
	@Override
	public void visitImport(ImportTree tree) {
		FullyQualifiedNameVisitor visitor = new FullyQualifiedNameVisitor();
		tree.accept(visitor);
		imports.add(visitor.getFullyQualifiedName());
		super.visitImport(tree);
	}

	/* (non-Javadoc)
	 * @see org.sonar.plugins.java.api.tree.BaseTreeVisitor#visitPackage(org.sonar.plugins.java.api.tree.PackageDeclarationTree)
	 */
	@Override
	public void visitPackage(PackageDeclarationTree tree) {
		FullyQualifiedNameVisitor visitor = new FullyQualifiedNameVisitor();
		tree.accept(visitor);
		packageName = visitor.getFullyQualifiedName();
		super.visitPackage(tree);
	}

	/**
	 * @return key of the project
	 */
	private String getProjectKey() {
		Object moduleKey = MeasurementUtils.getField(context, "sonarComponents", "fs", "moduleKey");
		return moduleKey == null ? null : moduleKey.toString();
	}

	/* (non-Javadoc)
	 * @see org.sonar.plugins.java.api.tree.BaseTreeVisitor#visitClass(org.sonar.plugins.java.api.tree.ClassTree)
	 */
	@Override
	public void visitClass(ClassTree tree) {
		SaveMetricsClient client = new SaveMetricsClient(DataSourceProvider.getDataSource());
		int line = tree.firstToken().line();
		int endLine = tree.lastToken().line();
		String parentID = null;
		if (tree.parent() instanceof ClassTree) {
			parentID = MeasurementUtils.getClassId((ClassTree) tree.parent(), packageName, project);
		}
		String fileKey = getFileKey();
		String componentID = MeasurementUtils.getClassId(tree, packageName, project);
		TypeTree superClass = tree.superClass();
		ListTree<TypeTree> superInterfaces = tree.superInterfaces();
		boolean isInterface = tree.declarationKeyword().text().equals("interface");
		client.saveComponent(componentID, fileKey, context.getFileKey(), project, parentID,
				Scope.CLASS.getValue(), packageName, MeasurementUtils.getClassName(tree, packageName), extractFullyQualifiedName(superClass), superInterfaces.stream().map(x ->
				extractFullyQualifiedName(x)).collect(Collectors.toList()), isInterface, "", line, endLine);
		saveMetrics(tree, componentID, Scope.CLASS);
		super.visitClass(tree);
	}

	@Override
	public void visitNewClass(NewClassTree tree) {
		// TODO temporary hack - nested classes support will be added
	}

	/* (non-Javadoc)
	 * @see org.sonar.plugins.java.api.tree.BaseTreeVisitor#visitMethod(org.sonar.plugins.java.api.tree.MethodTree)
	 */
	@Override
	public void visitMethod(MethodTree tree) {
		SaveMetricsClient client = new SaveMetricsClient(DataSourceProvider.getDataSource());
		Tree parent = tree.parent();
		if (parent instanceof ClassTree) {
			String parentID = MeasurementUtils.getClassId((ClassTree) parent, packageName, project);
			String componentID = parentID + "->" + MeasurementUtils.getMethodID(tree);
			MethodReturnVisitor returnVisitor = new MethodReturnVisitor(packageName, imports);
			tree.accept(returnVisitor);
			String returnType = returnVisitor.getResult();
			client.saveComponent(componentID, getFileKey(), context.getFileKey(), project, parentID, Scope.METHOD.getValue(),
					packageName, null, null, Collections.emptyList(), false, returnType, tree.firstToken().line(), tree.lastToken().line());
			saveMetrics(tree, componentID, Scope.METHOD);
		} else {
			log.error("No enclosing class found for method " + tree.simpleName().name());
		}
		super.visitMethod(tree);
	}

	/**
	 * @param tree
	 * @param componentID
	 */
	@SuppressWarnings("unchecked")
	private void saveMetrics(Tree tree, String componentID, Scope scope) {
		SaveMetricsClient client = new SaveMetricsClient(DataSourceProvider.getDataSource());
		MetricsRegister.getFrameworkMetrics().forEach(x -> {
			ICommonVisitor javaVisitor = MetricsRegister.getMetricVisitorForLanguage(x, Language.JAVA);
			if (javaVisitor == null) {
				return;
			}
			boolean isInScope = javaVisitor.getScope() == Scope.ALL || javaVisitor.getScope() == scope;
			if (javaVisitor instanceof AVisitor && isInScope) {
				AVisitor visitor = (AVisitor) javaVisitor;
				try {
					// do not fail project scan if one of the visitors throws an exception
					visitor.scanTree(tree);
				} catch (Exception e) {
					log.error("Scan not completed for visitor: " + visitor.getKey() + " on file: " + context.getFileKey(), e);
				}
				client.saveMeasure(x, componentID, visitor.getResult());
			}
		});
	}

	/**
	 * @param tree
	 * @return fully qualified name of the given type
	 */
	private String extractFullyQualifiedName(TypeTree tree) {
		if (tree == null) {
			return null;
		}
		return extractFullyQualifiedName(MeasurementUtils.extractTreeSimpleName(tree));
	}

	/**
	 * Tries to extract fully qualified name of a class by searching trough the imports
	 * @param simpleName
	 * @return fully qualified name if possible, simpleName otherwise
	 */
	private String extractFullyQualifiedName(String simpleName){
		String fqName = null;
		for (String importSymbol : imports) {
			if ((importSymbol != null) && importSymbol.endsWith(simpleName)) {
				fqName = importSymbol;
			}
		}
		return (fqName != null) ? fqName : packageName.concat("." + simpleName);
	}


	private String getFileKey() {
		String key = context.getFileKey();

		String dir = MeasurementUtils.getBaseDir(context);
		if (dir == null) {
			return key;
		}

		key = key.substring(dir.length() + 1);
		key = key.replace('\\', '/');
		key = project + ":" + key;

		return key;
	}
}
