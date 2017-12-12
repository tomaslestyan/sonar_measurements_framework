/**
 * The MIT License (MIT)
 * Copyright (c) 2016 FI MUNI
 * Copyright (c) 2017 FI MUNI
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
 * @author Filip Cekovsky
 */
public class MeasurementUtils {

	/**
	 * Derives trees simple name from its type
	 * @param tree
	 * @return name
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

	/**
	 *
	 * @param object
	 * @param fields
	 * @return
	 */
	public static Object getField(Object object, String... fields) {
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

	/**
	 *
	 * @param context
	 * @return
	 */
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
	 * Produces classes fully qualified name
	 * @param tree {@link ClassTree}
	 * @return fully qualified name of the class
	 */
	public static String getClassName(ClassTree tree, String packageName) {
		return packageName + "." + tree.simpleName().name();
	}

	/**
	 * Builds class'es id from its package name, project Id and tree
	 * @param tree {@link ClassTree}
	 * @return class'es id in format projectId:fullyQualifiedName
	 */
	public static String getClassId(ClassTree tree, String packageName, String project) {
		return project + ":" + getClassName(tree, packageName);
	}

	/**
	 * Parses a {@link MethodTree} for it's id used in the database
	 * @param tree
	 * @return method's id in format classId->methodName(parameters)
	 */
	public static String getMethodID(MethodTree tree) {
		String name = tree.simpleName().name();
		StringJoiner methodDeclaration = new StringJoiner(",", name + "(", ")");
		tree.parameters().forEach(x -> methodDeclaration.add(x.simpleName().name()));
		return methodDeclaration.toString();
	}

}
