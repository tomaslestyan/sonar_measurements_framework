/**
 * The MIT License (MIT)
 * Copyright (c) 2016 FI MUNI
 */
package main.java.framework.java.measurement;

import java.nio.file.Path;
import java.util.StringJoiner;

import org.apache.commons.lang.reflect.FieldUtils;
import org.sonar.plugins.java.api.JavaFileScannerContext;
import org.sonar.plugins.java.api.tree.*;

/**
 * Utils class for java measurements
 * @author Tomas
 */
public class MeasurementUtils {

	/**
	 * @param tree
	 * @return
	 */
	static String extractTreeSimpleName(TypeTree tree) {
		String name = null;
		switch (tree.kind()) {
		case IDENTIFIER:
			name = ((IdentifierTree) tree).name();
			break;
		case PARAMETERIZED_TYPE:
			name = ((ParameterizedTypeTree) tree).firstToken().text();
			break;
		case MEMBER_SELECT:
			name = ((MemberSelectExpressionTree) tree).identifier().name();
		default:
			break;
		}
		return name;
	}

	static Object getField(Object object, String... fields) {
		Object result = object;
		for (String field : fields) {
			try {
				result = FieldUtils.readField(result, field, true);
			} catch (IllegalAccessException e) {
				return null;
			}
		}
		return result;
	}

	static String getBaseDir(JavaFileScannerContext context) {
		Object baseDir = getField(context, "sonarComponents", "fs", "baseDir");
		if (baseDir == null) {
			return null;
		}
		if (baseDir instanceof Path) {
			Path projectDirectory = (Path) baseDir;
			return projectDirectory.toString();
		}
		Object dir = getField(baseDir, "path");
		if (dir instanceof String) {
			return (String) baseDir;
		}
		return null;

	}

	/**
	 * @param tree
	 * @return
	 */
	static String getClassName(ClassTree tree, String packageName) {
		return packageName + "." + tree.simpleName().name();
	}

	/**
	 * @param tree
	 * @return
	 */
	public static String getClassId(ClassTree tree, String packageName, String project) {
		return project + ":" + getClassName(tree, packageName);
	}

	public static String getMethodID(MethodTree tree) {
		String name = tree.simpleName().name();
		StringJoiner methodDeclaration = new StringJoiner(",", name + "(", ")");
		tree.parameters().forEach(x -> methodDeclaration.add(x.simpleName().name()));
		return methodDeclaration.toString();
	}

}
