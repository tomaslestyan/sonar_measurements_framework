package main.java.framework.visitors.java;

import com.sonar.sslr.api.AstNode;
import main.java.framework.api.Scope;
import org.apache.commons.lang.reflect.FieldUtils;
import org.sonar.plugins.java.api.semantic.Symbol;
import org.sonar.plugins.java.api.tree.IdentifierTree;
import org.sonar.plugins.java.api.tree.MethodInvocationTree;
import org.sonar.plugins.java.api.tree.MethodTree;
import org.sonar.plugins.java.api.tree.Tree;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author Filip Čekovský (433588)
 * @version 18.05.2017
 */

public class DistinctCallsVisitor  extends AVisitor{
    /* (non-Javadoc)
 * @see main.java.framework.api.ICommonVisitor#getKey()
 */

    public class Credentials{
        public String methodName;
        public String methodOwnerClass;
        public String callingMethod;
        public String callingClass;

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            Credentials that = (Credentials) o;

            if (methodName != null ? !methodName.equals(that.methodName) : that.methodName != null) return false;
            if (methodOwnerClass != null ? !methodOwnerClass.equals(that.methodOwnerClass) : that.methodOwnerClass != null)
                return false;
            if (callingMethod != null ? !callingMethod.equals(that.callingMethod) : that.callingMethod != null)
                return false;
            return callingClass != null ? callingClass.equals(that.callingClass) : that.callingClass == null;
        }

        @Override
        public int hashCode() {
            int result = methodName != null ? methodName.hashCode() : 0;
            result = 31 * result + (methodOwnerClass != null ? methodOwnerClass.hashCode() : 0);
            result = 31 * result + (callingMethod != null ? callingMethod.hashCode() : 0);
            result = 31 * result + (callingClass != null ? callingClass.hashCode() : 0);
            return result;
        }
    }

    private Set<Credentials> encounteredMethods;

    private String visitedClass;
    private String visitedMethod;

    public Set<Credentials> getEncounteredMethods() {
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
        Credentials credentials = new Credentials();

        credentials.methodOwnerClass = getOwnerName(tree.symbol());
        credentials.methodName = tree.symbol().name();

        if (credentials.methodName == null){
            try{
                List<AstNode> children = (List<AstNode>) FieldUtils.readField(tree,
                        "children", true);

                Object zeroIndex = children.get(0);
                IdentifierTree identifier;

                if (zeroIndex instanceof IdentifierTree) {
                    identifier =  (IdentifierTree) zeroIndex;
                } else {
                    identifier = (IdentifierTree) FieldUtils.readField(zeroIndex,
                            "identifier", true);
                }

                credentials.methodName = identifier.name();
            } catch (Exception e){
                //At least I tried XD
            }
        }

        credentials.callingMethod = visitedMethod;
        credentials.callingClass = visitedClass;


        if (!encounteredMethods.contains(credentials) || credentials.methodName == null){
            encounteredMethods.add(credentials);
            super.count++;
        }
    }

    private String getOwnerName(Symbol symbol){
        Symbol ownerSymbol = symbol.owner();
        if(ownerSymbol.isTypeSymbol()){
            try{
                return (String) FieldUtils.readField(ownerSymbol, "fullyQualifiedName", true);
            } catch (IllegalAccessException e){
                return ownerSymbol.name();
            }
        } else {
            return ownerSymbol.name();
        }
    }
}
