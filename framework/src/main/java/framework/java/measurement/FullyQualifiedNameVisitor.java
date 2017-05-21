package main.java.framework.java.measurement;

import java.util.ArrayList;
import java.util.List;
import java.util.StringJoiner;

import org.sonar.plugins.java.api.tree.BaseTreeVisitor;
import org.sonar.plugins.java.api.tree.IdentifierTree;

/**
 * Visitor to obtain fqn of trees
 * @author Tomas
 */
public class FullyQualifiedNameVisitor extends BaseTreeVisitor{
	private List<String> result = new ArrayList<>(); 

	/* (non-Javadoc)
	 * @see org.sonar.plugins.java.api.tree.BaseTreeVisitor#visitIdentifier(org.sonar.plugins.java.api.tree.IdentifierTree)
	 */
	@Override
	public void visitIdentifier(IdentifierTree tree) {
		result.add(tree.name());
	}

	/**
	 * @return
	 */
	public String getFullyQualifiedName() {
		StringJoiner fullyQualifiedNameJoiner = new StringJoiner("."); 
		result.forEach(x -> fullyQualifiedNameJoiner.add(x));
		return fullyQualifiedNameJoiner.toString();
	}
}