package main.java.framework.visitors.java;

import main.java.framework.api.Scope;
import org.sonar.java.resolve.JavaSymbol;
import org.sonar.plugins.java.api.semantic.Symbol;
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

    public static final int METHOD_NAME = 0;
    public static final int METHOD_OWNER = 1;
    public static final int CALLING_METHOD = 2;
    public static final int CALLING_CLASS = 3;

    private Set<String[]> encounteredMethods;

    private String visitedClass;
    private String visitedMethod;

    public Set<String[]> getEncounteredMethods() {
        return encounteredMethods;
    }

    public DistinctCallsVisitor() {
        encounteredMethods = new HashSet<>();
    }

    @Override
    public String getKey() {
        return "calls";
    }

    @Override
    public Scope getScope() {
        return Scope.METHOD;
    }

    @Override
    public void scanTree(Tree tree) {
        if (tree instanceof MethodTree){
            MethodTree methodTree = (MethodTree) tree;
            visitedMethod = methodTree.symbol().name();
            visitedClass = getOwnerName(methodTree.symbol());
        } else {
            visitedMethod = tree.toString();
        }
        super.scanTree(tree);
    }

    @Override
    public void visitMethodInvocation(MethodInvocationTree tree){


        String[] credentials = new String[4];
        credentials[METHOD_NAME] = tree.symbol().name();
        credentials[METHOD_OWNER] = getOwnerName(tree.symbol());
        credentials[CALLING_METHOD] = visitedMethod;
        credentials[CALLING_CLASS] = visitedClass;


        if (!encounteredMethods.contains(credentials)){
            encounteredMethods.add(credentials);
            super.count++;
        }
    }

    private String getOwnerName(Symbol symbol){
        Symbol ownerSymbol = symbol.owner();
        if(ownerSymbol.isTypeSymbol()){
            JavaSymbol.TypeJavaSymbol ownerTypeSymbol = (JavaSymbol.TypeJavaSymbol) ownerSymbol;
            return ownerTypeSymbol.getFullyQualifiedName();
        } else {
            return ownerSymbol.name();
        }
    }
}
