/**
 * The MIT License (MIT)
 * Copyright (c) 2016 FI MUNI
 */
package main.java.framework.visitors.java;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.StringJoiner;
import java.util.stream.Collectors;

import org.apache.commons.lang.reflect.FieldUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonar.api.batch.Phase;
import org.sonar.check.Rule;
import org.sonar.plugins.java.api.JavaFileScanner;
import org.sonar.plugins.java.api.JavaFileScannerContext;
import org.sonar.plugins.java.api.tree.BaseTreeVisitor;
import org.sonar.plugins.java.api.tree.ClassTree;
import org.sonar.plugins.java.api.tree.CompilationUnitTree;
import org.sonar.plugins.java.api.tree.IdentifierTree;
import org.sonar.plugins.java.api.tree.ImportTree;
import org.sonar.plugins.java.api.tree.ListTree;
import org.sonar.plugins.java.api.tree.MemberSelectExpressionTree;
import org.sonar.plugins.java.api.tree.MethodTree;
import org.sonar.plugins.java.api.tree.NewClassTree;
import org.sonar.plugins.java.api.tree.PackageDeclarationTree;
import org.sonar.plugins.java.api.tree.ParameterizedTypeTree;
import org.sonar.plugins.java.api.tree.Tree;
import org.sonar.plugins.java.api.tree.TypeTree;

import main.java.framework.api.ICommonVisitor;
import main.java.framework.api.Language;
import main.java.framework.api.Scope;
import main.java.framework.api.metrics.MetricsRegister;
import main.java.framework.db.DataSourceProvider;
import main.java.framework.db.SaveMetricsClient;

/**
 * Class for visiting Java files. 
 * The purpose of the class is to visit each class and method to store information 
 * about this components including measures of available metrics.
 * @author Tomas Lestyan
 */
@Phase(name = Phase.Name.PRE)
@Rule(key = "framework", 
name="Measurement Framework Activation Rule", 
description="This rule activates the Measurement Framework.")
public class FileVisitor extends BaseTreeVisitor implements JavaFileScanner {

	/** The logger object */
	private final Logger log = LoggerFactory.getLogger(this.getClass());
	/** The scanner context */
	private JavaFileScannerContext context;
	/** The project of the scanned file */
	private String project;
	private String packageName;
	private List<String> imports;

	/* (non-Javadoc)
	 * @see org.sonar.plugins.java.api.JavaFileScanner#scanFile(org.sonar.plugins.java.api.JavaFileScannerContext)
	 */
	@Override
	public void scanFile(JavaFileScannerContext context) {
		this.context = context;
		this.project = getProjectKey();
		this.packageName = null;
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
	 * @return 
	 */
	private String getProjectKey() {
		try {
			Object sonarComponentsField = FieldUtils.readField(context, "sonarComponents", true);
			if (sonarComponentsField != null) {
				Object fileSystem = FieldUtils.readField(sonarComponentsField, "fs", true);
				if (fileSystem != null) {
					Object moduleKey = FieldUtils.readField(fileSystem, "moduleKey", true);
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
		SaveMetricsClient client = new SaveMetricsClient(DataSourceProvider.getDataSource());
		int line = tree.firstToken().line();
		int endLine = tree.lastToken().line();
		String parentID = null;
		if (tree.parent() instanceof ClassTree) {
			parentID = getClassId((ClassTree) tree.parent());
		}
		String fileKey = getFileKey();
		String componentID = getClassId(tree);
		TypeTree superClass = tree.superClass();
		ListTree<TypeTree> superInterfaces = tree.superInterfaces();
		client.saveComponent(componentID, fileKey, project, parentID,
				Scope.CLASS.getValue(), packageName, getClassName(tree), extractFullyQualifiedName(superClass), superInterfaces.stream().map(x ->
				extractFullyQualifiedName(x)).collect(Collectors.toList()), line, endLine);
		saveMetrics(tree, componentID, Scope.CLASS);
		super.visitClass(tree);
	}

	@Override
	public void visitNewClass(NewClassTree tree) {
		// TODO temporary hack - nested classes support will be added
	}

	/**
	 * @param tree
	 * @return
	 */
	private String getClassId(ClassTree tree) {
		return project + ":" + getClassName(tree);
	}

	/**
	 * @param tree
	 * @return
	 */
	private String getClassName(ClassTree tree) {
		return packageName + "." + tree.simpleName().name();
	}


	/* (non-Javadoc)
	 * @see org.sonar.plugins.java.api.tree.BaseTreeVisitor#visitMethod(org.sonar.plugins.java.api.tree.MethodTree)
	 */
	@Override
	public void visitMethod(MethodTree tree) {
		SaveMetricsClient client = new SaveMetricsClient(DataSourceProvider.getDataSource());
		Tree parent = tree.parent();
		if (parent instanceof ClassTree) {
			String parentID = getClassId((ClassTree) parent);
			String componentID = parentID + "->" + getMethodID(tree);
			client.saveComponent(componentID, getFileKey(), project, parentID, Scope.METHOD.getValue(),
					packageName, null, null, Collections.emptyList(), tree.firstToken().line(), tree.lastToken().line());
			saveMetrics(tree, componentID, Scope.METHOD);
		} else {
			log.error("No enclosing class found for method " + tree.simpleName().name());
		}
		super.visitMethod(tree);
	}

	/**
	 * @param tree
	 * @return
	 */
	private String getMethodID(MethodTree tree) {
		String name = tree.simpleName().name();
		StringJoiner methodDeclaration = new StringJoiner(",",name + "(",")");
		tree.parameters().forEach(x -> methodDeclaration.add(x.simpleName().name()));
		return methodDeclaration.toString();
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
			boolean isInScope = javaVisitor.getScope() == Scope.ALL || javaVisitor.getScope() == scope;
			if ((javaVisitor != null) && javaVisitor instanceof AVisitor && isInScope) {
				AVisitor visitor = (AVisitor) javaVisitor;
				visitor.scanTree(tree);
				client.saveMeasure(x, componentID, visitor.getResult());

			};
		});
	}

	/**
	 * @param tree
	 * @return fully qualified name of the given type
	 */
	private String extractFullyQualifiedName(TypeTree tree) {
		if (tree == null ) {
			return null;
		}
		String simpleName = extractTreeSimpleName(tree);
		String fqName = null;
		for (String importSymbol : imports) {
			if (importSymbol.endsWith(simpleName)) {
				fqName = importSymbol;
			}
		}
		return (fqName != null) ? fqName : packageName.concat("." + simpleName);
	}

	/**
	 * @param tree
	 * @return
	 */
	private String extractTreeSimpleName(TypeTree tree) {
		String name = null;
		switch (tree.kind()) {
		case IDENTIFIER:
			name = ((IdentifierTree)tree).name();
			break;
		case PARAMETERIZED_TYPE:
			name = ((ParameterizedTypeTree)tree).firstToken().text();
			break;
		case MEMBER_SELECT:
			name = ((MemberSelectExpressionTree)tree).identifier().name();
		default:
			log.warn("No symbol name found for symbol" + tree.symbolType());
			break;
		}
		return name;
	}

	private String getFileKey(){
		String key = context.getFileKey();
		try {
			Object sonarComponentsField = FieldUtils.readField(context, "sonarComponents", true);
			if (sonarComponentsField != null) {
				Object fileSystem = FieldUtils.readField(sonarComponentsField, "fs", true);
				if (fileSystem != null) {
					Object baseDir  = FieldUtils.readField(fileSystem, "baseDir", true);
					if (baseDir != null){
						String dir;
						Object dirPath = FieldUtils.readField(baseDir, "path", true);
						if (dirPath instanceof Path){
							Path projectDirectory = (Path) dirPath;
							dir = projectDirectory.toString();
						} else if (dirPath instanceof String){
							dir = (String) dirPath;
						} else {
							return key;
						}

						key = key.substring(dir.length() + 1);
						key = key.replace('\\', '/');
						key = project + ":" + key;
					}
				}
			}
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		}
		return key;
	}

}
