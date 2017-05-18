package main.java.framework.visitors.java;

import main.java.framework.api.Scope;
import org.sonar.java.resolve.JavaSymbol;
import org.sonar.plugins.java.api.semantic.Symbol;
import org.sonar.plugins.java.api.tree.ClassTree;
import org.sonar.plugins.java.api.tree.MethodInvocationTree;
import org.sonar.plugins.java.api.tree.MethodTree;
import org.sonar.plugins.java.api.tree.Tree;

import java.util.HashSet;
import java.util.Set;

/**
 * @author Filip Čekovský (433588)
 * @version 18.05.2017
 */

public class DistinctCallsVisitor  extends AVisitor{
    /* (non-Javadoc)
 * @see main.java.framework.api.ICommonVisitor#getKey()
 */
    private Set<String[]> encounteredMethods;
    private String visitedClass;
    private String visitedMethod;

    public DistinctCallsVisitor() {
        encounteredMethods = new HashSet<>();
    }

    @Override
    public String getKey() {
        return "calls";
    }

    /* (non-Javadoc)
     * @see main.java.framework.api.ICommonVisitor#getScope()
     */
    @Override
    public Scope getScope() {
        return Scope.METHOD;
    }

    @Override
    public void scanTree(Tree tree) {
        if (tree.parent() instanceof ClassTree){
            ClassTree classTree = (ClassTree) tree.parent();
            visitedClass = classTree.symbol().name();
        } else {
            visitedClass = "";
        }

        if (tree instanceof MethodTree){
            MethodTree methodTree = (MethodTree) tree;
            visitedMethod = methodTree.symbol().name();
        } else {
            int wtf = 5;
        }
        super.scanTree(tree);
    }

    @Override
    public void visitMethodInvocation(MethodInvocationTree tree){
        Symbol owner = tree.symbol().owner();
        String ownerName;

        if(owner instanceof JavaSymbol.TypeJavaSymbol){
            JavaSymbol.TypeJavaSymbol ownerSymbol = (JavaSymbol.TypeJavaSymbol) owner;
            ownerName = ownerSymbol.getFullyQualifiedName();
        } else {
            ownerName = "";
        }

        String[] credentials = new String[4];
        credentials[0] = tree.symbol().name();
        credentials[1] = ownerName;
        credentials[2] = visitedMethod;
        credentials[3] = visitedClass;



        if (!encounteredMethods.contains(credentials)){
            encounteredMethods.add(credentials);
            super.count++;
        }
    }
}
