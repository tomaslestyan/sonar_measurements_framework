/**
 * The MIT License (MIT)
 * Copyright (c) 2016 FI MUNI
 */
package main.java.framework.visitors.java;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.lang.reflect.FieldUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonar.api.batch.Phase;
import org.sonar.check.Rule;
import org.sonar.plugins.java.api.JavaFileScanner;
import org.sonar.plugins.java.api.JavaFileScannerContext;
import org.sonar.plugins.java.api.semantic.Symbol;
import org.sonar.plugins.java.api.tree.BaseTreeVisitor;
import org.sonar.plugins.java.api.tree.ClassTree;
import org.sonar.plugins.java.api.tree.CompilationUnitTree;
import org.sonar.plugins.java.api.tree.IdentifierTree;
import org.sonar.plugins.java.api.tree.ImportClauseTree;
import org.sonar.plugins.java.api.tree.ListTree;
import org.sonar.plugins.java.api.tree.MethodTree;
import org.sonar.plugins.java.api.tree.ParameterizedTypeTree;
import org.sonar.plugins.java.api.tree.Tree;
import org.sonar.plugins.java.api.tree.Tree.Kind;
import org.sonar.plugins.java.api.tree.TypeTree;

import main.java.framework.api.ICommonVisitor;
import main.java.framework.api.Language;
import main.java.framework.api.Scope;
import main.java.framework.api.metrics.MetricsRegister;
import main.java.framework.db.DataSourceProvider;
import main.java.framework.db.SaveMetricsClient;

/**
 * Class for visiting Java files. The purpose of the class is to visit each class and method and store information about this components including measures of available metrics.
 * @author Tomas
 */
@Phase(name = Phase.Name.PRE)
@Rule(key = "framework", name="framework", description="blank rule")
public class FileVisitor extends BaseTreeVisitor implements JavaFileScanner {

	/** The logger object */
	private final Logger log = LoggerFactory.getLogger(this.getClass());
	/** The scanner context */
	private JavaFileScannerContext context;
	/** The project of the scanned file */
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
		int line = tree.firstToken().line();
		int endLine = tree.lastToken().line();
		SaveMetricsClient client = new SaveMetricsClient(DataSourceProvider.getDataSource());
		String fileKey = context.getFileKey();
		String simpleName = extractTreeSimpleName(tree.symbol());
		String componentID = fileKey + "->" + simpleName;
		TypeTree superClass = tree.superClass();
		ListTree<TypeTree> superInterfaces = tree.superInterfaces();
		client.saveComponent(componentID, fileKey, project, getParentID(tree), 
				Scope.CLASS.getValue(), getPackageName(), extractFullyQualifiedName(superClass), superInterfaces.stream().map(x -> 
				extractFullyQualifiedName(x)).collect(Collectors.toList()), line, endLine);
		saveMetrics(tree, componentID, Scope.CLASS);
		super.visitClass(tree);
	}


	/* (non-Javadoc)
	 * @see org.sonar.plugins.java.api.tree.BaseTreeVisitor#visitMethod(org.sonar.plugins.java.api.tree.MethodTree)
	 */
	@Override
	public void visitMethod(MethodTree tree) {
		SaveMetricsClient client = new SaveMetricsClient(DataSourceProvider.getDataSource());
		String componentID = context.getFileKey() + "->" + extractTreeSimpleName(tree.symbol());
		getParentID(tree);
		client.saveComponent(componentID, context.getFileKey(), project, getParentID(tree), 
				Scope.METHOD.getValue(), getPackageName(), null, Collections.emptyList(), tree.firstToken().line(), tree.lastToken().line());
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
	 * @return
	 */
	private List<ImportClauseTree> getImports() {
		return context.getTree().imports();
	}

	/**
	 * @return
	 */
	private String getPackageName() {
		return context.getTree().packageDeclaration().packageName().firstToken().text();
	}

	/**
	 * @param tree
	 * @return fully qualified name of the given type
	 */
	private String extractFullyQualifiedName(TypeTree tree) {
		String simpleName = extractTreeSimpleName(tree);
		List<ImportClauseTree> imports = getImports();
		String fqName = simpleName; // TODO fqName shold be constructed from belonging import or class fq name if there are in same package
		return fqName;
	}

	/**
	 * @param tree
	 * @return
	 */
	private String extractTreeSimpleName(Symbol symbol) {
		String simpleName = null;
		if (symbol != null) {
			simpleName = symbol.name();
		}  else {
			log.warn("No symbol name found for symbol" + symbol);
		}
		return simpleName;
	}

	/**
	 * @param tree
	 * @return
	 */
	private String extractTreeSimpleName(TypeTree tree) {
		String name = null;
		if (tree != null) {
			switch (tree.kind()) {
			case IDENTIFIER:
				name = ((IdentifierTree)tree).name();
				break;
			case PARAMETERIZED_TYPE:
				name = ((ParameterizedTypeTree)tree).type().toString();
				break;
			default:
				log.warn("No symbol name found for symbol" + tree.symbolType());
				break;
			}
		}
		return name;
	}


}
