package main.java.framework.visitors.java;

import java.util.ArrayList;
import java.util.List;
import java.util.StringJoiner;

import org.sonar.plugins.java.api.tree.BaseTreeVisitor;
import org.sonar.plugins.java.api.tree.IdentifierTree;

public class FullyQualifiedNameVisitor extends BaseTreeVisitor{
	private List<String> result = new ArrayList<>(); 

	@Override
	public void visitIdentifier(IdentifierTree tree) {
		result.add(tree.name());
	}

	public String getFullyQualifiedName() {
		StringJoiner fullyQualifiedNameJoiner = new StringJoiner("."); 
		result.forEach(x -> fullyQualifiedNameJoiner.add(x));
		return fullyQualifiedNameJoiner.toString();
	}
}